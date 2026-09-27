import { apiGet, apiRequest } from './client';
import type { PaginationPayload } from './types';
import type {
  ProductListing,
  SellerAnalytics,
  SellerOrderItem,
  SellerOrdersQuery,
  SellerProfile,
  UpdateListingStockRequest,
  UpdateSellerProfileRequest,
} from './sellerTypes';

const BASE = '/seller-dashboard';

// ---- Profile ----

export function fetchSellerProfile(): Promise<SellerProfile> {
  return apiGet<SellerProfile>(`${BASE}/profile`);
}

export async function updateSellerProfile(
  body: UpdateSellerProfileRequest,
): Promise<SellerProfile> {
  const envelope = await apiRequest<SellerProfile>(`${BASE}/profile`, {
    method: 'PUT',
    body: JSON.stringify(body),
  });
  return envelope.data!;
}

// ---- Analytics ----

export function fetchSellerAnalytics(): Promise<SellerAnalytics> {
  return apiGet<SellerAnalytics>(`${BASE}/analytics`);
}

// ---- Orders ----

export function fetchSellerOrders(
  query: SellerOrdersQuery = {},
): Promise<SellerOrderItem[]> {
  return apiGet<SellerOrderItem[]>(`${BASE}/orders`, {
    status: query.status,
    searchKey: query.searchKey,
    startDate: query.startDate,
    endDate: query.endDate,
  });
}

export async function updateOrderItemStatus(
  orderItemId: string,
  newStatus: string,
): Promise<void> {
  await apiRequest(`${BASE}/orders/${orderItemId}/status`, {
    method: 'PUT',
    body: JSON.stringify({ newStatus }),
  });
}

// ---- Listings ----

export function fetchSellerListings(
  page = 0,
  size = 10,
): Promise<PaginationPayload<ProductListing>> {
  return apiGet<PaginationPayload<ProductListing>>(`${BASE}/listings`, {
    page,
    size,
  });
}

export async function updateListingStock(
  listingId: string,
  body: UpdateListingStockRequest,
): Promise<ProductListing> {
  const envelope = await apiRequest<ProductListing>(
    `${BASE}/listings/${listingId}/stock`,
    {
      method: 'PUT',
      body: JSON.stringify(body),
    },
  );
  return envelope.data!;
}
