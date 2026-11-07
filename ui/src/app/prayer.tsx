import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { useLocalSearchParams } from 'expo-router';
import { Platform, ScrollView, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { PrayerSection } from '@/components/prayer-section';
import { useTheme } from '@/hooks/use-theme';

export default function PrayerScreen() {
  const { mystery } = useLocalSearchParams<{ mystery?: string }>();
  const theme = useTheme();

  const hailMary = "Hail Mary, full of grace, the Lord is with thee. Blessed art thou among women, and blessed is the fruit of thy womb, Jesus. Holy Mary, Mother of God, pray for us sinners, now and at the hour of our death. Amen.";

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
            ) : (
              <>
                <ThemedText type="title" style={styles.title}>
                  {`${mystery} Mysteries`}
                </ThemedText>
                <PrayerSection
                  pages={[
                    { reading: '1', text: 'hi' },
                    {
                      reading:
                        'When the steward of the feast tasted the water, now become wine, and did not know where it came from (though the servants who had drawn the water knew), the steward of the feast called the bridegroom. (John 2:9)',
                      text: hailMary,
                    },
                    {
                      reading:
                        'Like the sun shining upon the temple of the Most High, and like the rainbow gleaming in glorious clouds. (Sirach 50:7)',
                      text: hailMary,
                    },
                    { reading: '3', text: 'bye' },
                  ]}
                  initialIndex={0}
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