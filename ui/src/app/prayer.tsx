import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { useLocalSearchParams } from 'expo-router';
import { Image, Platform, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { PrayerSection } from '@/components/prayer-section';

export default function PrayerScreen() {
  const { mystery } = useLocalSearchParams<{ mystery?: string }>();

  if (!mystery) {
    return (
      <ThemedView style={styles.container}>
        <SafeAreaView style={styles.safeArea}>
            <ThemedText type="title" style={styles.title}>
                Select a Mystery on the Rosary Page
            </ThemedText>
        </SafeAreaView>
      </ThemedView>
    );
  }

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <ThemedText type="title" style={styles.title}>
          {`${mystery} Mysteries`}
        </ThemedText>

        <PrayerSection pages={[{ reading: "1", text: "hi" }, { reading: "2", text: "no" }, { reading: "3", text: "bye" }]} initialIndex={0} />


      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    flexDirection: 'row',
  },
  safeArea: {
    flex: 1,
    paddingTop: Platform.OS === 'web' ? Spacing.six : 0,
    paddingHorizontal: Spacing.four,
    alignItems: 'center',
    gap: Spacing.three,
    paddingBottom: BottomTabInset + Spacing.three,
    maxWidth: MaxContentWidth,
  },
  title: {
    textAlign: 'center',
  },
  image: {
    width: 128,
    height: 128,
  },
});