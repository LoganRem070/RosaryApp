import { Platform } from 'react-native';

export type PrayerPage = {
  reading: string;
  text: string;
  image: string;
};

type RosaryApiRow = {
  reading: string | null;
  prayerName: string;
  prayerText: string;
  image: string | null;
};

const baseUrl = 'https://rosaryapp-production.up.railway.app/rosary'; 

async function getJson<T>(url: string): Promise<T> {
  let response: Response;
  try {
    response = await fetch(url);
  } catch (error) {
    console.error('[Rosary API] Request failed before receiving a response:', {
      url,
      errorName: error instanceof Error ? error.name : typeof error,
      errorMessage: error instanceof Error ? error.message : String(error),
      errorStack: error instanceof Error ? error.stack : undefined,
    });
    throw error;
  }

  const data = await response.json();
  console.log('[Rosary API] Response:', {
    url,
    status: response.status,
    ok: response.ok,
  });

  if (!response.ok) {
    const message =
      typeof data === 'object' && data !== null && 'error' in data
        ? String(data.error)
        : `Request failed with status ${response.status}`;
    throw new Error(message);
  }

  return data as T;
}

export class http {
  static async getPrayerData(
    mystery: string,
    decade: number,
    prayer: number,
    language = 'English'
  ): Promise<RosaryApiRow[]> {
    const params = new URLSearchParams({
      mystery,
      decade: String(decade),
      number: String(prayer),
      language,
    });
    const rows = await getJson<RosaryApiRow[]>(`${baseUrl}?${params.toString()}`);
    console.log('[Rosary API] Received data:', rows);
    return rows;
  }
}
