import { useEffect, useRef, useState } from 'react';
import { ThemedView } from "./themed-view";
import { ThemedText } from "./themed-text";
import { Image, Pressable, StyleSheet } from 'react-native';
import { useTheme } from '@/hooks/use-theme';
import { http } from "@/services/http";
import { imageHandeler } from '@/services/imageHandeler';
import type { RoomState } from '@/lib/roomContext';

export type GroupControls = {
  step: RoomState;
  onPrev: () => void;
  onNext: () => void;
};

type PrayerSectionProps =
  | { mystery: string; language: string }
  | { group: GroupControls };

export function PrayerSection(props: PrayerSectionProps) {
  if ('group' in props) {
    return <GroupPrayer group={props.group} />;
  }
  return <SoloPrayer mystery={props.mystery} language={props.language} />;
}

// ---------- Group mode: shows the room's current bead ----------

function GroupPrayer({ group }: { group: GroupControls }) {
  const { step, onPrev, onNext } = group;
  const theme = useTheme();
  const [image, setImage] = useState<string | null>(null);
  const imageCache = useRef<Map<string, string | null>>(new Map());

  const finished = step.type === 'complete';
  const decade = step.decade ?? 0;

  // Images are stored per mystery and decade. The room message carries no image,
  // so fetch it once per decade. English is requested so the server doesn't call Gemini.
  useEffect(() => {
    if (finished || !step.mystery) {
      return;
    }
    const key = `${step.mystery}-${decade}`;
    if (imageCache.current.has(key)) {
      setImage(imageCache.current.get(key) ?? null);
      return;
    }

    let active = true;
    http.getPrayerData(step.mystery, decade, 0, 'English')
      .then(async (rows) => {
        const row = rows[0];
        const base64 = row?.image
          ? await imageHandeler.byteArrayToBase64(row.image)
          : null;
        imageCache.current.set(key, base64);
        if (active) {
          setImage(base64);
        }
      })
      .catch(() => {
        if (active) {
          setImage(null);
        }
      });

    return () => {
      active = false;
    };
  }, [step.mystery, decade, finished]);

  const prevDisabled = !finished && step.index <= 0;
  const nextDisabled = finished;
  const indicator = finished
    ? `${step.total} / ${step.total}`
    : `${step.index + 1} / ${step.total}`;

  return (
    <ThemedView style={styles.container}>
      <Image
        source={
          image
            ? { uri: `data:image/png;base64,${image}` }
            : require('@/assets/images/rosary.png')
        }
        style={styles.image}
      />

      <ThemedView style={styles.pageBox}>
        {finished ? (
          <ThemedText>Rosary complete.</ThemedText>
        ) : (
          <>
            <ThemedText style={styles.reading}>{step.reading ?? step.prayerName}</ThemedText>
            <ThemedText style={[styles.content, { color: theme.text }]}>{step.prayerText}</ThemedText>
          </>
        )}
      </ThemedView>

      <ThemedView style={styles.arrowContainer}>
        <NavButton label="← Prev" disabled={prevDisabled} onPress={onPrev} />
        <ThemedText style={styles.pageIndicator}>{indicator}</ThemedText>
        <NavButton label="Next →" disabled={nextDisabled} onPress={onNext} />
      </ThemedView>
    </ThemedView>
  );
}

function NavButton({
  label,
  disabled,
  onPress,
}: {
  label: string;
  disabled: boolean;
  onPress: () => void;
}) {
  const theme = useTheme();
  return (
    <Pressable
      style={[
        styles.arrowButton,
        { backgroundColor: disabled ? theme.backgroundSelected : theme.backgroundElement },
      ]}
      onPress={onPress}
      disabled={disabled}
    >
      <ThemedText style={[styles.arrowText, { color: disabled ? theme.textSecondary : theme.text }]}>
        {label}
      </ThemedText>
    </Pressable>
  );
}

// ---------- Solo mode: unchanged from the original ----------

type PrayerPage = {
  reading: string;
  text: string;
  image: string | null;
};

function SoloPrayer({ mystery, language }: { mystery: string; language: string }) {
  const [currentNumber, setCurrentNumber] = useState(0);
  const [currentDecade, setCurrentDecade] = useState(0);
  const [currentMystery, setCurrentMystery] = useState(mystery);
  const [currentPage, setCurrentPage] = useState<PrayerPage | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const theme = useTheme();

  const maxes: Record<number, number> = { 0: 7, 1: 12, 2: 12, 3: 12, 4: 12, 5: 12, 6: 3 };
  const mysteryLength = 77;

  if (currentMystery !== mystery) {
    setCurrentMystery(mystery);
    setCurrentDecade(0);
    setCurrentNumber(0);
  }

  useEffect(() => {
    let active = true;

    setLoading(true);
    setError(null);
    http.getPrayerData(mystery, currentDecade, currentNumber, language)
      .then(async (rows) => {
        const row = rows[0];
        const image = row?.image
          ? await imageHandeler.byteArrayToBase64(row.image)
          : null;

        if (active) {
          setCurrentPage(row ? {
            reading: row.reading ?? row.prayerName,
            text: row.prayerText,
            image,
          } : null);
        }
      })
      .catch((requestError: unknown) => {
        if (active) {
          setCurrentPage(null);
          setError(
            requestError instanceof Error
              ? requestError.message
              : 'Unable to load this prayer.'
          );
        }
      })
      .finally(() => {
        if (active) {
          setLoading(false);
        }
      });

    return () => {
      active = false;
    };
  }, [mystery, language, currentDecade, currentNumber]);

  const handlePrev = () => {
    if (currentDecade < 0) {return;}
    if (currentDecade == 0 && currentNumber == 0) {return;}
    if (currentNumber == 0) {
      setCurrentDecade(currentDecade - 1);
      setCurrentNumber(maxes[currentDecade-1]);
    } else {
      setCurrentNumber(currentNumber - 1);
    }
  };

  const handleNext = () => {
    if (currentDecade > 6) {return;}
    if (currentDecade == 6 && currentNumber == 3) {return;}
    if (currentNumber == maxes[currentDecade]) {
      setCurrentDecade(currentDecade + 1);
      setCurrentNumber(0);
    } else {
      setCurrentNumber(currentNumber + 1);
    }
  };

  const getPastDecadeMaxes = (decade: number): number => {
    if (decade < 0) {
      return 0;
    }
    return maxes[decade] + getPastDecadeMaxes(decade-1);
  };

  return (
    <ThemedView style={styles.container}>
      <Image
        source={
          currentPage?.image
            ? { uri: `data:image/png;base64,${currentPage.image}` }
            : require('@/assets/images/rosary.png')
        }
        style={styles.image}
      />
      <ThemedView style={styles.pageBox}>
        {loading ? (
          <ThemedText>Loading prayer…</ThemedText>
        ) : error ? (
          <ThemedText>{error}</ThemedText>
        ) : currentPage ? (
          <>
            <ThemedText style={styles.reading}>{currentPage.reading}</ThemedText>
            <ThemedText style={[styles.content, { color: theme.text }]}>{currentPage.text}</ThemedText>
          </>
        ) : (
          <ThemedText>No prayer found for this position.</ThemedText>
        )}
      </ThemedView>

      <ThemedView style={styles.arrowContainer}>
        <Pressable
          style={[
            styles.arrowButton,
            { backgroundColor: (currentNumber === 0 && currentDecade === 0) ? theme.backgroundSelected : theme.backgroundElement },
          ]}
          onPress={handlePrev}
          disabled={currentNumber === 0 && currentDecade === 0}
        >
          <ThemedText style={[styles.arrowText, { color: (currentNumber === 0 && currentDecade === 0) ? theme.textSecondary : theme.text }]}>← Prev</ThemedText>
        </Pressable>

        <ThemedText style={styles.pageIndicator}>
          {currentNumber + 1 + currentDecade + getPastDecadeMaxes(currentDecade-1)} / {mysteryLength}
        </ThemedText>

        <Pressable
          style={[
            styles.arrowButton,
            { backgroundColor: (currentDecade == 6 && currentNumber == 3) ? theme.backgroundSelected : theme.backgroundElement },
          ]}
          onPress={handleNext}
          disabled={(currentDecade == 6 && currentNumber == 3)}
        >
          <ThemedText style={[styles.arrowText, { color: (currentDecade == 6 && currentNumber == 3) ? theme.textSecondary : theme.text }]}>Next →</ThemedText>
        </Pressable>
      </ThemedView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    width: '100%',
    padding: 16,
    borderRadius: 12,
    elevation: 3,
  },
  image: {
    width: 120,
    height: 120,
    alignSelf: 'center',
    marginBottom: 16,
  },
  pageBox: {
    minHeight: 150,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 20,
  },
  reading: {
    fontSize: 14,
    lineHeight: 18,
    color: '#ecc71e',
    textAlign: 'center',
    marginBottom: 20,
  },
  content: {
    fontSize: 12,
    lineHeight: 16,
    textAlign: 'center',
  },
  arrowContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    borderTopWidth: 1,
    borderTopColor: '#eee',
    paddingTop: 12,
  },
  arrowButton: {
    paddingVertical: 8,
    paddingHorizontal: 12,
    borderRadius: 6,
  },
  arrowText: {
    fontWeight: '600',
  },
  pageIndicator: {
    fontSize: 14,
    fontWeight: '500',
  },
});