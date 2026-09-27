import { useCallback, useEffect, useRef, useState } from 'react';
import { fetchSellerAnalytics } from '../api/sellerApi';
import type { SellerAnalytics } from '../api/sellerTypes';
import { ApiError } from '../api/client';

const STALE_MS = 60_000;

export function useSellerAnalytics() {
  const [data, setData] = useState<SellerAnalytics | null>(null);
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
      const analytics = await fetchSellerAnalytics();
      setData(analytics);
      lastFetchedAt.current = Date.now();
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Failed to load seller analytics';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      const now = Date.now();
      if (lastFetchedAt.current && now - lastFetchedAt.current < STALE_MS) {
        if (!ignore) setLoading(false);
        return;
      }
      if (!ignore) {
        setLoading(true);
        setError(null);
      }
      try {
        const analytics = await fetchSellerAnalytics();
        if (!ignore) {
          setData(analytics);
          lastFetchedAt.current = Date.now();
        }
      } catch (err) {
        if (!ignore) {
          const message =
            err instanceof ApiError ? err.message : 'Failed to load seller analytics';
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
