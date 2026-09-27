// ============================================================
// Seller Dashboard API Types
// Mirrors backend DTOs for the Seller Dashboard endpoints
// ============================================================

// ---- Enums ----

export type OrderItemStatus = 'PENDING' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | 'RETURNED';

export type ListingStatus = 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'PENDING_REVIEW';

export type FulfillmentType = 'FBA' | 'FBM';

// ---- Seller Profile ----

export interface SellerProfile {
  id: string;
  userId: string;
  fullName: string;
  email: string;
  storeName: string;
  taxNumber: string;
  businessAddress: string;
  bankAccountDetails: string;
  isVerified: boolean;
  brandId: string | null;
  brandName: string | null;
  supportEmail: string | null;
  merchantPhone: string | null;
  returnPolicyUrl: string | null;
  legalName: string | null;
  stateTaxPermitNumber: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface UpdateSellerProfileRequest {
  storeName?: string;
  businessAddress?: string;
  bankAccountDetails?: string;
  supportEmail?: string;
  merchantPhone?: string;
  returnPolicyUrl?: string;
  legalName?: string;
  stateTaxPermitNumber?: string;
}

// ---- Seller Analytics ----

export interface HourlyOrderInflux {
  hour: string;
  orderCount: number;
}

export interface TopSellingListing {
  productTitle: string;
  sellerSku: string;
  totalUnitsSold: number;
  totalRevenue: number;
}

export interface SellerAnalytics {
  totalActiveListings: number;
  totalProducts: number;
  totalRevenue: number;
  totalUnitsSold: number;
  totalPendingOrders: number;
  totalShippedOrders: number;
  totalDeliveredOrders: number;
  buyBoxDominancePercentage: number;
  stockAlertsCount: number;
  hourlyOrderInflux: HourlyOrderInflux[];
  topSellingListings: TopSellingListing[];
}

// ---- Seller Orders ----

export interface SellerOrderItem {
  orderItemId: string;
  orderId: string;
  productTitle: string;
  variantName: string;
  asin: string;
  sellerSku: string;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  itemStatus: OrderItemStatus;
  buyerName: string;
  orderDate: string;
  buyerDestination: string | null;
  shipByDeadline: string | null;
}

export interface SellerOrdersQuery {
  status?: OrderItemStatus;
  searchKey?: string;
  startDate?: string;
  endDate?: string;
}

// ---- Product Listings ----

export interface ProductListing {
  id: string;
  productVariantId: string;
  variantAsin: string;
  variantName: string;
  sellerId: string;
  sellerName: string;
  sellerSku: string;
  price: number;
  minPriceFloor: number | null;
  stockQuantity: number;
  fulfillmentType: FulfillmentType;
  isBuyboxWinner: boolean;
  status: ListingStatus;
  createdAt: string;
}

export interface UpdateListingStockRequest {
  price?: number;
  stockQuantity?: number;
  minPriceFloor?: number;
}
