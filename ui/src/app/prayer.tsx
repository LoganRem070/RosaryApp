import { useEffect, useState } from 'react';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { useLocalSearchParams } from 'expo-router';
import { Platform, ScrollView, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { PrayerSection } from '@/components/prayer-section';
import { useTheme } from '@/hooks/use-theme';

export default function PrayerScreen() {
  const { mystery, language } = useLocalSearchParams<{
    mystery?: string;
    language?: string;
  }>();
  const theme = useTheme();

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <ScrollView
          style={[styles.scrollView, { backgroundColor: theme.background }]}
          contentContainerStyle={styles.scrollContent}
        >
          <ThemedView style={styles.content}>
            {!mystery ? (
              <ThemedText type="title" style={styles.title}>
                Select a Mystery on the Rosary Page
              </ThemedText>
            ) :/* loading ? (
              <ThemedText>Loading prayers…</ThemedText>
            ) : error ? (
              <ThemedText>{error}</ThemedText>
            ) : mysteryData.length === 0 ? (
              <ThemedText>No prayers found for this mystery.</ThemedText>
            ) : */(
              <>
                <ThemedText type="title" style={styles.title}>
                  {`${mystery} Mysteries`}
                </ThemedText>
                <PrayerSection
                  mystery={mystery as string}
                  language={language ?? 'English'}
                />
              </>
            )}
          </ThemedView>
        </ScrollView>
      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  safeArea: {
    flex: 1,
  },
  scrollView: {
    flex: 1,
  },
  scrollContent: {
    flexGrow: 1,
    alignItems: 'center',
    paddingTop: Platform.OS === 'web' ? Spacing.six : Spacing.three,
    paddingBottom: BottomTabInset + Spacing.four,
    paddingHorizontal: Spacing.four,
  },
  content: {
    width: '100%',
    maxWidth: MaxContentWidth,
    alignItems: 'center',
    gap: Spacing.three,
  },
  title: {
    textAlign: 'center',
    maxWidth: '100%',
    fontSize: 24,
  },
});