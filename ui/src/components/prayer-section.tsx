import { useState } from "react";
import { ThemedView } from "./themed-view";
import { ThemedText } from "./themed-text";
import { Image, Pressable, StyleSheet } from 'react-native';
import { useTheme } from '@/hooks/use-theme';

type PrayerSectionProps = {
  pages: any[];
  initialIndex: number;
};
type PrayerPage = {
  reading: string;
  text: string;
};
export function PrayerSection({ pages, initialIndex }: PrayerSectionProps) {
  const [currentIndex, setCurrentIndex] = useState(initialIndex);
  const theme = useTheme();

  const handlePrev = () => {
    if (currentIndex > 0) {
      setCurrentIndex(currentIndex - 1);
    }
  };

  const handleNext = () => {
    if (currentIndex < pages.length - 1) {
      setCurrentIndex(currentIndex + 1);
    }
  };

  const currentPage: PrayerPage = pages[currentIndex];

  return (
    <ThemedView style={styles.container}>
        
      <Image style={styles.image} source={require('@/assets/images/rosary.png')} />
      {/* Page Content Box */}
      <ThemedView style={styles.pageBox}>
        <ThemedText style={styles.reading}>{currentPage.reading}</ThemedText>
        <ThemedText style={styles.content}>{currentPage.text}</ThemedText>
      </ThemedView>

      {/* Navigation Bar with Arrows */}
      <ThemedView style={styles.arrowContainer}>
        <Pressable
          style={[
            styles.arrowButton,
            { backgroundColor: currentIndex === 0 ? theme.backgroundSelected : theme.backgroundElement },
          ]}
          onPress={handlePrev}
          disabled={currentIndex === 0}
        >
          <ThemedText style={[styles.arrowText, { color: currentIndex === 0 ? theme.textSecondary : theme.text }]}>← Prev</ThemedText>
        </Pressable>

        <ThemedText style={styles.pageIndicator}>
          {currentIndex + 1} / {pages.length}
        </ThemedText>

        <Pressable
          style={[
            styles.arrowButton,
            { backgroundColor: currentIndex === pages.length - 1 ? theme.backgroundSelected : theme.backgroundElement },
          ]}
          onPress={handleNext}
          disabled={currentIndex === pages.length - 1}
        >
          <ThemedText style={[styles.arrowText, { color: currentIndex === pages.length - 1 ? theme.textSecondary : theme.text }]}>Next →</ThemedText>
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