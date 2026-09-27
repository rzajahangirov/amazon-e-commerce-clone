import { useCallback, useEffect, useState } from 'react';
import { fetchAdminUsers } from '../api/adminApi';
import type { AdminUsersQuery, PaginationPayload, UserResponse } from '../api/types';
import { ApiError } from '../api/client';

export function useAdminUsers(filters: AdminUsersQuery) {
  const [page, setPage] = useState(filters.page ?? 0);
  const [result, setResult] = useState<PaginationPayload<UserResponse> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const payload = await fetchAdminUsers({ ...filters, page });
      setResult(payload);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load users';
      setError(message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  }, [filters.active, filters.email, filters.role, filters.size, page]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [filters.active, filters.email, filters.role]);

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
