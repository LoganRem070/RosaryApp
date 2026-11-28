import { useLocalSearchParams, useRouter } from 'expo-router';
import { Platform, Pressable, ScrollView, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { PrayerSection } from '@/components/prayer-section';
import { useTheme } from '@/hooks/use-theme';
import { useRoom } from '@/lib/roomContext';

export default function PrayerScreen() {
  const { mystery, language } = useLocalSearchParams<{
    mystery?: string;
    language?: string;
  }>();
  const theme = useTheme();
  const router = useRouter();
  const { room, next, prev, leave } = useRoom();

  const inGroup = room !== null;

  const exitGroup = () => {
    leave();
    router.navigate('/');
  };

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <ScrollView
          style={[styles.scrollView, { backgroundColor: theme.background }]}
          contentContainerStyle={styles.scrollContent}
        >
          <ThemedView style={styles.content}>
            {inGroup ? (
              <GroupBody room={room} onPrev={prev} onNext={next} />
            ) : !mystery ? (
              <ThemedText type="title" style={styles.title}>
                Select a Mystery on the Rosary Page
              </ThemedText>
            ) : (
              <>
                <ThemedText type="title" style={styles.title}>
                  {`${mystery} Mysteries`}
                </ThemedText>
                <PrayerSection mystery={mystery as string} language={language ?? 'English'} />
              </>
            )}
          </ThemedView>
        </ScrollView>

        {inGroup && (
          <Pressable
            onPress={exitGroup}
            style={({ pressed }) => [styles.leaveBar, pressed && styles.pressed]}
          >
            <ThemedView type="backgroundElement" style={styles.leaveInner}>
              <ThemedText type="link">Leave room</ThemedText>
            </ThemedView>
          </Pressable>
        )}
      </SafeAreaView>
    </ThemedView>
  );
}

function GroupBody({
  room,
  onPrev,
  onNext,
}: {
  room: NonNullable<ReturnType<typeof useRoom>['room']>;
  onPrev: () => void;
  onNext: () => void;
}) {
  if (room.type === 'lobby') {
    return (
      <ThemedText type="title" style={styles.title}>
        Waiting for the group to start
      </ThemedText>
    );
  }

  return (
    <>
      <ThemedText type="title" style={styles.title}>
        {room.type === 'complete' ? 'Rosary complete' : `${room.mystery} Mysteries`}
      </ThemedText>
      <PrayerSection group={{ step: room, onPrev, onNext }} />
    </>
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
    paddingBottom: Spacing.four,
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
  leaveBar: {
    alignSelf: 'stretch',
    paddingHorizontal: Spacing.four,
    paddingBottom: BottomTabInset,
  },
  leaveInner: {
    paddingVertical: Spacing.two,
    borderRadius: Spacing.five,
    alignItems: 'center',
  },
  pressed: {
    opacity: 0.7,
  },
});