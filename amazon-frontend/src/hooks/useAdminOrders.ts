import { useCallback, useEffect, useState } from 'react';
import { fetchAdminOrders } from '../api/adminApi';
import type { AdminOrder, AdminOrdersQuery, PaginationPayload } from '../api/types';
import { ApiError } from '../api/client';

export function useAdminOrders(filters: AdminOrdersQuery) {
  const [page, setPage] = useState(filters.page ?? 0);
  const [result, setResult] = useState<PaginationPayload<AdminOrder> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const payload = await fetchAdminOrders({ ...filters, page });
      setResult(payload);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load orders';
      setError(message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  }, [filters.endDate, filters.startDate, filters.status, filters.size, page]);

  useEffect(() => {
    void load();
  }, [load]);

  useEffect(() => {
    setPage(0);
  }, [filters.endDate, filters.startDate, filters.status]);

  return {
    orders: result?.content ?? [],
    pagination: result,
    loading,
    error,
    page,
    setPage,
    refetch: load,
  };
}
