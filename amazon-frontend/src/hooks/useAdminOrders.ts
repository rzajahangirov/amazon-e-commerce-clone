import { useCallback, useEffect, useState } from 'react';
import { fetchAdminOrders } from '../api/adminApi';
import type { AdminOrder, AdminOrdersQuery, PaginationPayload } from '../api/types';
import { ApiError } from '../api/client';

export function useAdminOrders(filters: AdminOrdersQuery) {
  const [page, setPage] = useState(filters.page ?? 0);
  const [result, setResult] = useState<PaginationPayload<AdminOrder> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { endDate, startDate, status, size } = filters;

  const [prevFilterKey, setPrevFilterKey] = useState(() => `${endDate}_${startDate}_${status}`);
  const currentFilterKey = `${endDate}_${startDate}_${status}`;
  if (prevFilterKey !== currentFilterKey) {
    setPrevFilterKey(currentFilterKey);
    setPage(0);
  }

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const payload = await fetchAdminOrders({ endDate, startDate, status, size, page });
      setResult(payload);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load orders';
      setError(message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  }, [endDate, startDate, status, size, page]);

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        const payload = await fetchAdminOrders({ endDate, startDate, status, size, page });
        if (!ignore) setResult(payload);
      } catch (err) {
        if (!ignore) {
          const message = err instanceof ApiError ? err.message : 'Failed to load orders';
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
  }, [endDate, startDate, status, size, page]);

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
