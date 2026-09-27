import { useCallback, useEffect, useState } from 'react';
import { fetchSellerProfile, updateSellerProfile } from '../api/sellerApi';
import type { SellerProfile, UpdateSellerProfileRequest } from '../api/sellerTypes';
import { ApiError } from '../api/client';

export function useSellerProfile() {
  const [data, setData] = useState<SellerProfile | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const profile = await fetchSellerProfile();
      setData(profile);
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Failed to load seller profile';
      setError(message);
    } finally {
      setLoading(false);
    }
  }, []);

  const save = useCallback(async (body: UpdateSellerProfileRequest) => {
    setSaving(true);
    setError(null);
    try {
      const updated = await updateSellerProfile(body);
      setData(updated);
      return true;
    } catch (err) {
      const message =
        err instanceof ApiError ? err.message : 'Failed to save profile';
      setError(message);
      return false;
    } finally {
      setSaving(false);
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return { data, loading, saving, error, refetch: load, save };
}
