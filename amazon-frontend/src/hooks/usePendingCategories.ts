import { useCallback, useEffect, useState } from 'react';
import { fetchPendingCategories } from '../api/adminApi';
import type { CategoryResponse } from '../api/types';
import { ApiError } from '../api/client';

export function usePendingCategories() {
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const list = await fetchPendingCategories();
      setCategories(list);
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Failed to load pending categories';
      setError(message);
      setCategories([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return { categories, loading, error, refetch: load };
}
