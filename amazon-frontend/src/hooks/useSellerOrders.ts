import { useCallback, useEffect, useState } from 'react';
import { fetchSellerOrders, updateOrderItemStatus } from '../api/sellerApi';
import type { SellerOrderItem, SellerOrdersQuery } from '../api/sellerTypes';
import { ApiError } from '../api/client';

export function useSellerOrders(filters: SellerOrdersQuery) {
  const { status, searchKey, startDate, endDate } = filters;
  const [orders, setOrders] = useState<SellerOrderItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await fetchSellerOrders({ status, searchKey, startDate, endDate });
      setOrders(data);
    } catch (err) {
      const message = err instanceof ApiError ? err.message : 'Failed to load orders';
      setError(message);
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [status, searchKey, startDate, endDate]);

  const transitionStatus = useCallback(
    async (orderItemId: string, newStatus: string) => {
      try {
        await updateOrderItemStatus(orderItemId, newStatus);
        await load();
        return true;
      } catch (err) {
        const message =
          err instanceof ApiError ? err.message : 'Failed to update status';
        setError(message);
        return false;
      }
    },
    [load],
  );

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        const data = await fetchSellerOrders({ status, searchKey, startDate, endDate });
        if (!ignore) setOrders(data);
      } catch (err) {
        if (!ignore) {
          const message = err instanceof ApiError ? err.message : 'Failed to load orders';
          setError(message);
          setOrders([]);
        }
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, [status, searchKey, startDate, endDate]);

  return { orders, loading, error, refetch: load, transitionStatus };
}
