import { useRouter } from 'expo-router';
import { useState } from 'react';
import { Platform, Pressable, ScrollView, StyleSheet, TextInput } from 'react-native';
import { Dropdown } from 'react-native-element-dropdown';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AnimatedIcon } from '@/components/animated-icon';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useTheme } from '@/hooks/use-theme';
import { RoomState, useRoom } from '@/lib/roomContext';

const MYSTERIES = ['Joyful', 'Glorious', 'Sorrowful', 'Luminous'];

const LANGUAGES = [
  { label: 'English', value: 'English' },
  { label: 'Latin', value: 'Latin' },
  { label: 'Spanish', value: 'Spanish' },
  { label: 'Polish', value: 'Polish' },
  { label: 'Italian', value: 'Italian' },
  { label: 'French', value: 'French' },
];

type Mode = 'regular' | 'group';

export default function HomeScreen() {
  const theme = useTheme();
  const router = useRouter();
  const { status, room, error, createRoom, joinRoom, start, next, leave } = useRoom();

  const [mode, setMode] = useState<Mode>('regular');
  const [language, setLanguage] = useState('English');
  const [selected, setSelected] = useState<string[]>(['Joyful']);
  const [joinCode, setJoinCode] = useState('');

  // Standard order, so every group prays the mysteries in the same sequence
  const orderedSelection = MYSTERIES.filter((m) => selected.includes(m));
  const codeReady = joinCode.trim().length === 6;
  const inRoom = mode === 'group' && status !== 'idle';

  const toggleMystery = (mystery: string) =>
    setSelected((prev) =>
      prev.includes(mystery) ? prev.filter((m) => m !== mystery) : [...prev, mystery]
    );

  const openPrayer = (mystery: string) =>
    router.push({ pathname: '/prayer', params: { mystery, language } });

  const handleCreate = () => createRoom(orderedSelection, language);

  const handleJoin = () => joinRoom(joinCode.trim());

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <AnimatedIcon />
        <ThemedText type="title" style={styles.title}>
          Rosary App
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.modeSwitch}>
          {(['regular', 'group'] as const).map((m) => (
            <Pressable
              key={m}
              onPress={() => setMode(m)}
              style={[styles.modeOption, mode === m && { backgroundColor: theme.backgroundSelected }]}
            >
              <ThemedText type="link">{m === 'regular' ? 'Regular' : 'Group'}</ThemedText>
            </Pressable>
          ))}
        </ThemedView>

        <Dropdown
          style={styles.dropdown}
          containerStyle={[styles.dropdownMenu, { backgroundColor: theme.backgroundElement }]}
          placeholderStyle={[styles.dropdownText, { color: theme.textSecondary }]}
          selectedTextStyle={[styles.dropdownText, { color: theme.text }]}
          itemTextStyle={{ color: theme.text }}
          activeColor={theme.backgroundSelected}
          data={LANGUAGES}
          labelField="label"
          valueField="value"
          placeholder="English"
          onChange={(item) => setLanguage(item.value)}
          accessibilityLabel="Select prayer language"
        />

        {mode === 'regular' ? (
          <>
            <ThemedText type="code" style={styles.code}>
              Select a Mystery
            </ThemedText>
            <ThemedView type="backgroundElement" style={styles.stepContainer}>
              {MYSTERIES.map((m) => (
                <Pressable
                  key={m}
                  onPress={() => openPrayer(m)}
                  style={({ pressed }) => pressed && styles.pressed}
                >
                  <ThemedView type="backgroundElement" style={styles.linkButton}>
                    <ThemedText type="link">{m} Mysteries</ThemedText>
                  </ThemedView>
                </Pressable>
              ))}
            </ThemedView>
          </>
        ) : inRoom ? (
          <RoomPanel
            status={status}
            room={room}
            error={error}
            onStart={start}
            onNext={next}
            onLeave={leave}
          />
        ) : (
          <>
            <ThemedText type="code" style={styles.code}>
              Choose mysteries for the group
            </ThemedText>
            <ThemedView style={styles.chipRow}>
              {MYSTERIES.map((m) => (
                <Chip key={m} label={m} on={selected.includes(m)} onPress={() => toggleMystery(m)} />
              ))}
            </ThemedView>

            <ActionButton
              label="Create group room"
              disabled={orderedSelection.length === 0}
              onPress={handleCreate}
            />

            <ThemedText type="code" style={styles.code}>
              Or join a room
            </ThemedText>
            <TextInput
              value={joinCode}
              onChangeText={(text) => setJoinCode(text.toUpperCase())}
              placeholder="Room code"
              placeholderTextColor={theme.textSecondary}
              autoCapitalize="characters"
              maxLength={6}
              style={[styles.input, { color: theme.text, borderColor: theme.textSecondary }]}
            />
            <ActionButton label="Join group room" disabled={!codeReady} onPress={handleJoin} />
          </>
        )}
      </SafeAreaView>
    </ThemedView>
  );
}

function RoomPanel({
  status,
  room,
  error,
  onStart,
  onNext,
  onLeave,
}: {
  status: string;
  room: RoomState | null;
  error: string | null;
  onStart: () => void;
  onNext: () => void;
  onLeave: () => void;
}) {
  return (
    <ThemedView style={styles.panel}>
      <ThemedText type="subtitle">Room {room?.roomId ?? '...'}</ThemedText>

      <ThemedText type="small">
        {room
          ? `${room.participants} ${room.participants === 1 ? 'person' : 'people'} in the room`
          : status}
      </ThemedText>

      {room ? (
        <ThemedText type="small">{room.mysteries.map((m) => `${m} Mysteries`).join(', ')}</ThemedText>
      ) : null}

      {error ? <ThemedText type="small">{error}</ThemedText> : null}

      <ScrollView style={styles.beadScroll} contentContainerStyle={styles.beadContent}>
        {!room && <ThemedText>Connecting...</ThemedText>}

        {room?.type === 'lobby' && (
          <ThemedText>Share the code above. Press Start when everyone is ready.</ThemedText>
        )}

        {room?.type === 'step' && (
          <>
            <ThemedText type="small">
              {room.mystery} Mysteries, decade {room.index + 1} of {room.total}
            </ThemedText>
            <ThemedText type="subtitle">{room.reading ?? ''}</ThemedText>
            <ThemedText type="small">{room.prayerName}</ThemedText>
            <ThemedText>{room.prayerText}</ThemedText>
          </>
        )}

        {room?.type === 'complete' && <ThemedText type="subtitle">Rosary complete.</ThemedText>}
      </ScrollView>

      {room?.type === 'lobby' && <ActionButton label="Start" onPress={onStart} />}
      {room?.type === 'step' && <ActionButton label="Next" onPress={onNext} />}
      <ActionButton label="Leave room" onPress={onLeave} />
    </ThemedView>
  );
}

function Chip({ label, on, onPress }: { label: string; on: boolean; onPress: () => void }) {
  const theme = useTheme();
  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [
        styles.chip,
        {
          backgroundColor: on ? theme.backgroundSelected : theme.backgroundElement,
          borderColor: theme.textSecondary,
        },
        pressed && styles.pressed,
      ]}
    >
      <ThemedText type="small">
        {on ? '✓ ' : ''}
        {label}
      </ThemedText>
    </Pressable>
  );
}

function ActionButton({
  label,
  onPress,
  disabled = false,
}: {
  label: string;
  onPress: () => void;
  disabled?: boolean;
}) {
  return (
    <Pressable
      disabled={disabled}
      onPress={onPress}
      style={({ pressed }) => [styles.button, pressed && styles.pressed, disabled && styles.disabled]}
    >
      <ThemedView type="backgroundElement" style={styles.linkButton}>
        <ThemedText type="link">{label}</ThemedText>
      </ThemedView>
    </Pressable>
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
    width: '100%',
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
  code: {
    textTransform: 'uppercase',
  },
  modeSwitch: {
    flexDirection: 'row',
    alignSelf: 'stretch',
    borderRadius: Spacing.five,
    padding: Spacing.one,
  },
  modeOption: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: Spacing.two,
    borderRadius: Spacing.five,
  },
  dropdown: {
    width: '100%',
    height: 40,
    borderColor: '#888',
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 12,
  },
  dropdownMenu: {
    borderRadius: 8,
  },
  dropdownText: {
    fontSize: 16,
  },
  input: {
    width: '100%',
    height: 40,
    borderWidth: 1,
    borderRadius: 8,
    paddingHorizontal: 12,
    fontSize: 16,
    textAlign: 'center',
    letterSpacing: 4,
  },
  stepContainer: {
    gap: Spacing.three,
    alignSelf: 'stretch',
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.four,
    borderRadius: Spacing.four,
  },
  chipRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'center',
    gap: Spacing.two,
  },
  chip: {
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.two,
    borderRadius: Spacing.five,
    borderWidth: 1,
  },
  panel: {
    flex: 1,
    alignSelf: 'stretch',
    gap: Spacing.two,
  },
  beadScroll: {
    flex: 1,
    alignSelf: 'stretch',
  },
  beadContent: {
    gap: Spacing.two,
    paddingVertical: Spacing.two,
  },
  button: {
    alignSelf: 'stretch',
  },
  linkButton: {
    flexDirection: 'row',
    paddingHorizontal: Spacing.four,
    paddingVertical: Spacing.two,
    borderRadius: Spacing.five,
    justifyContent: 'center',
    gap: Spacing.one,
    alignItems: 'center',
  },
  pressed: {
    opacity: 0.7,
  },
  disabled: {
    opacity: 0.4,
  },
});