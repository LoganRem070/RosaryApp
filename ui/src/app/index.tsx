import * as Device from 'expo-device';
import { Platform, Pressable, StyleSheet } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';

import { AnimatedIcon } from '@/components/animated-icon';
import { HintRow } from '@/components/hint-row';
import { ThemedText } from '@/components/themed-text';
import { ThemedView } from '@/components/themed-view';
import { WebBadge } from '@/components/web-badge';
import { BottomTabInset, MaxContentWidth, Spacing } from '@/constants/theme';
import { useRouter } from 'expo-router';
import { SymbolView } from 'expo-symbols';
import { useTheme } from '@/hooks/use-theme';
import { Dropdown } from 'react-native-element-dropdown';
import { useState } from 'react';

function getDevMenuHint() {
  if (Platform.OS === 'web') {
    return <ThemedText type="small">use browser devtools</ThemedText>;
  }
  if (Device.isDevice) {
    return (
      <ThemedText type="small">
        shake device or press <ThemedText type="code">m</ThemedText> in terminal
      </ThemedText>
    );
  }
  const shortcut = Platform.OS === 'android' ? 'cmd+m (or ctrl+m)' : 'cmd+d';
  return (
    <ThemedText type="small">
      press <ThemedText type="code">{shortcut}</ThemedText>
    </ThemedText>
  );
}

export default function HomeScreen() {
  const theme = useTheme();
  const router = useRouter();

  const mysteries = ["Joyful", "Glorious", "Sorrowful", "Luminous"];

  const languages = [
    { label: 'English', value: 'English' },
    { label: 'Latin', value: 'Latin' },
    { label: 'Spanish', value: 'Spanish' },
    { label: 'Polish', value: 'Polish' },
    { label: 'Italian', value: 'Italian' },
    { label: 'French', value: 'French' },
  ];
  const [currentLang, setLang] = useState('English');

  return (
    <ThemedView style={styles.container}>
      <SafeAreaView style={styles.safeArea}>
        <AnimatedIcon />
        <ThemedText type="title" style={styles.title}>
          Rosary App
        </ThemedText>

        <ThemedText type="code" style={styles.code}>
          Select a Mystery
        </ThemedText>

        <ThemedView type="backgroundElement" style={styles.stepContainer}>
          {mysteries.map((mystery) => (
            <Pressable
              key={mystery}
              onPress={() =>
                router.push({
                  pathname: '/prayer',
                  params: { mystery, language: currentLang },
                })
              }
              style={({ pressed }) => pressed && styles.pressed}
            >
              <ThemedView type="backgroundElement" style={styles.linkButton}>
                <ThemedText type="link">{mystery} Mysteries</ThemedText>
              </ThemedView>
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
          data={languages}
          labelField="label"
          valueField="value"
          placeholder="Select a language"
          onChange={(item) => setLang(item.value)}
          accessibilityLabel="Select prayer language"
        />

      </SafeAreaView>
    </ThemedView>
  );
}

const styles = StyleSheet.create({
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
  heroSection: {
    alignItems: 'center',
    justifyContent: 'center',
    flex: 1,
    paddingHorizontal: Spacing.four,
    gap: Spacing.four,
  },
  title: {
    textAlign: 'center',
  },
  code: {
    textTransform: 'uppercase',
  },
  stepContainer: {
    gap: Spacing.three,
    alignSelf: 'stretch',
    paddingHorizontal: Spacing.three,
    paddingVertical: Spacing.four,
    borderRadius: Spacing.four,
  },
  pressed: {
    opacity: 0.7,
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
});
