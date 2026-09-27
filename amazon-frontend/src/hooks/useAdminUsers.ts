import { useCallback, useEffect, useState } from 'react';
import { fetchAdminUsers } from '../api/adminApi';
import type { AdminUsersQuery, PaginationPayload, UserResponse } from '../api/types';
import { ApiError } from '../api/client';

export function useAdminUsers(filters: AdminUsersQuery) {
  const [page, setPage] = useState(filters.page ?? 0);
  const [result, setResult] = useState<PaginationPayload<UserResponse> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { active, email, role, size } = filters;

  const [prevFilterKey, setPrevFilterKey] = useState(() => `${active}_${email}_${role}`);
  const currentFilterKey = `${active}_${email}_${role}`;
  if (prevFilterKey !== currentFilterKey) {
    setPrevFilterKey(currentFilterKey);
    setPage(0);
  }

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const payload = await fetchAdminUsers({ active, email, role, size, page });
      setResult(payload);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load users';
      setError(message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  }, [active, email, role, size, page]);

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        const payload = await fetchAdminUsers({ active, email, role, size, page });
        if (!ignore) setResult(payload);
      } catch (err) {
        if (!ignore) {
          const message = err instanceof ApiError ? err.message : 'Failed to load users';
          setError(message);
          setResult(null);
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => {
      ignore = true;
    };
  }, [active, email, role, size, page]);

  return {
    users: result?.content ?? [],
    pagination: result,
    loading,
    error,
    page,
    setPage,
    refetch: load,
  };
}
