import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchAdminAnalytics } from '../api/adminApi';
import type { AdminAnalytics } from '../api/types';
import { ApiError } from '../api/client';

const STALE_MS = 60_000;

export function useAdminAnalytics() {
  const [data, setData] = useState<AdminAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const lastFetchedAt = useRef<number>(0);

  const load = useCallback(async (force = false) => {
    const now = Date.now();
    if (!force && lastFetchedAt.current && now - lastFetchedAt.current < STALE_MS) {
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);
    try {
      const analytics = await fetchAdminAnalytics();
      setData(analytics);
      lastFetchedAt.current = Date.now();
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Failed to load platform analytics';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        const analytics = await fetchAdminAnalytics();
        if (!ignore) {
          setData(analytics);
          lastFetchedAt.current = Date.now();
        }
      } catch (err) {
        if (!ignore) {
          const message =
            err instanceof ApiError ? err.message : 'Failed to load platform analytics';
          setError(message);
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, []);

  return { data, loading, error, refetch: () => load(true) };
}
