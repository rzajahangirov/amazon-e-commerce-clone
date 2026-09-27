import { useCallback, useEffect, useState } from 'react';
import { fetchSellerListings, updateListingStock } from '../api/sellerApi';
import type { ProductListing, UpdateListingStockRequest } from '../api/sellerTypes';
import type { PaginationPayload } from '../api/types';
import { ApiError } from '../api/client';

export function useSellerListings(pageSize = 10) {
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PaginationPayload<ProductListing> | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const payload = await fetchSellerListings(page, pageSize);
      setResult(payload);
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Failed to load listings';
      setError(message);
      setResult(null);
    } finally {
      setLoading(false);
    }
  }, [page, pageSize]);

  const updateStock = useCallback(
    async (listingId: string, body: UpdateListingStockRequest) => {
      try {
        await updateListingStock(listingId, body);
        await load();
        return true;
      } catch (err) {
        const message =
          err instanceof ApiError ? err.message : 'Failed to update listing';
        setError(message);
        return false;
      }
    },
    [load],
  );

  useEffect(() => {
    void load();
  }, [load]);

  return {
    listings: result?.content ?? [],
    pagination: result,
    loading,
    error,
    page,
    setPage,
    refetch: load,
    updateStock,
  };
}
