import { useEffect, useState } from 'react';
import { ThemedView } from "./themed-view";
import { ThemedText } from "./themed-text";
import { Image, Pressable, StyleSheet } from 'react-native';
import { useTheme } from '@/hooks/use-theme';
import { http } from "@/services/http";
import { imageHandeler } from '@/services/imageHandeler';

type PrayerPage = {
  reading: string;
  text: string;
  image: string | null;
};

type PrayerSectionProps = {
  mystery: string;
};

export function PrayerSection({ mystery }: PrayerSectionProps) {
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
    http.getPrayerData(mystery, currentDecade, currentNumber)
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
  }, [mystery, currentDecade, currentNumber]);

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
    console.log(`Current Decade: ${currentDecade}, Current Number: ${currentNumber}, Max for this Decade: ${maxes[currentDecade]}`);
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
      {/* Page Content Box */}
      <ThemedView style={styles.pageBox}>
        {loading ? (
          <ThemedText>Loading prayer…</ThemedText>
        ) : error ? (
          <ThemedText>{error}</ThemedText>
        ) : currentPage ? (
          <>
            <ThemedText style={styles.reading}>{currentPage.reading}</ThemedText>
            <ThemedText style={styles.content}>{currentPage.text}</ThemedText>
          </>
        ) : (
          <ThemedText>No prayer found for this position.</ThemedText>
        )}
      </ThemedView>

      {/* Navigation Bar with Arrows */}
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
    color: '#ffffff',
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