import { useRouter } from 'expo-router';
import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import type { ReactNode } from 'react';

export type RoomState = {
  type: 'lobby' | 'step' | 'complete';
  roomId: string;
  mysteries: string[];
  language: string;
  participants: number;
  index: number;
  total: number;
  mystery?: string;
  decade?: number;
  number?: number;
  reading?: string | null;
  prayerName?: string;
  prayerText?: string;
};

export type ConnectionStatus = 'idle' | 'connecting' | 'connected' | 'closed' | 'error';

type RoomContextValue = {
  status: ConnectionStatus;
  room: RoomState | null;
  error: string | null;
  createRoom: (mysteries: string[], language: string) => void;
  joinRoom: (code: string) => void;
  start: () => void;
  next: () => void;
  prev: () => void;
  leave: () => void;
};

const RoomContext = createContext<RoomContextValue | null>(null);

const ROOM_URL = process.env.EXPO_PUBLIC_ROOM_URL ?? 'ws://localhost:8090';

export function RoomProvider({ children }: { children: ReactNode }) {
  const router = useRouter();
  const [status, setStatus] = useState<ConnectionStatus>('idle');
  const [room, setRoom] = useState<RoomState | null>(null);
  const [error, setError] = useState<string | null>(null);
  const socketRef = useRef<WebSocket | null>(null);
  const lastTypeRef = useRef<string | null>(null);

  const openSocket = useCallback(
    (firstMessage: object) => {
      const existing = socketRef.current;
      if (existing) {
        existing.onopen = null;
        existing.onmessage = null;
        existing.onerror = null;
        existing.onclose = null;
        existing.close();
        socketRef.current = null;
      }

      lastTypeRef.current = null;
      setRoom(null);
      setError(null);
      setStatus('connecting');

      const ws = new WebSocket(ROOM_URL);
      socketRef.current = ws;

      const timeout = setTimeout(() => {
        if (ws.readyState !== WebSocket.OPEN) {
          setStatus('error');
          setError(`No connection to ${ROOM_URL}. Is the Java app running?`);
          ws.close();
        }
      }, 5000);

      ws.onopen = () => {
        clearTimeout(timeout);
        setStatus('connected');
        ws.send(JSON.stringify(firstMessage));
      };

      ws.onmessage = (event) => {
        const msg = JSON.parse(String(event.data));
        if (msg.type === 'error') {
          setError(msg.message);
          return;
        }

        const previous = lastTypeRef.current;
        lastTypeRef.current = msg.type;
        setError(null);
        setRoom(msg as RoomState);

        // Move everyone into the prayer screen once, when the group starts
        if (msg.type === 'step' && previous !== 'step' && previous !== 'complete') {
          router.navigate('/prayer');
        }
      };

      ws.onerror = () => {
        clearTimeout(timeout);
        setStatus('error');
        setError('Could not reach the room server.');
      };

      ws.onclose = () => {
        clearTimeout(timeout);
        if (socketRef.current === ws) {
          socketRef.current = null;
          setRoom(null);
          setStatus((prev) => (prev === 'error' ? prev : 'closed'));
        }
      };
    },
    [router]
  );

  const createRoom = useCallback(
    (mysteries: string[], language: string) =>
      openSocket({ type: 'create', mysteries, language }),
    [openSocket]
  );

  const joinRoom = useCallback(
    (code: string) => openSocket({ type: 'join', roomId: code }),
    [openSocket]
  );

  const sendIfOpen = useCallback((payload: object) => {
    const ws = socketRef.current;
    if (ws && ws.readyState === WebSocket.OPEN) {
      ws.send(JSON.stringify(payload));
    } else {
      setError('Not connected to the room server.');
    }
  }, []);

  const start = useCallback(() => sendIfOpen({ type: 'start' }), [sendIfOpen]);
  const next = useCallback(() => sendIfOpen({ type: 'next' }), [sendIfOpen]);
  const prev = useCallback(() => sendIfOpen({ type: 'prev' }), [sendIfOpen]);

  const leave = useCallback(() => {
    const ws = socketRef.current;
    socketRef.current = null;
    if (ws) {
      if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify({ type: 'leave' }));
      }
      ws.close();
    }
    lastTypeRef.current = null;
    setRoom(null);
    setError(null);
    setStatus('idle');
  }, []);

  const value = useMemo(
    () => ({ status, room, error, createRoom, joinRoom, start, next, prev, leave }),
    [status, room, error, createRoom, joinRoom, start, next, prev, leave]
  );

  return <RoomContext.Provider value={value}>{children}</RoomContext.Provider>;
}

export function useRoom() {
  const ctx = useContext(RoomContext);
  if (!ctx) {
    throw new Error('useRoom must be used inside RoomProvider');
  }
  return ctx;
}