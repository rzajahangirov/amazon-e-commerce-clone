// ────────────────────────────────────────────────────────────────
// Storefront API Types — Enterprise Customer Portal v1.1.0
// ────────────────────────────────────────────────────────────────

export type BadgeTag =
  | 'BEST_SELLER'
  | 'OVERALL_PICK'
  | 'LIMITED_STOCK'
  | 'DEAL_OF_THE_DAY'
  | 'ENTERPRISE_TIER'
  | 'INDUSTRIAL_CHOICE'
  | 'APPLE_MAG_OPTIMIZED'
  | string;

export type ProductSortBy =
  | 'FEATURED'
  | 'BEST_SELLERS'
  | 'AVG_CUSTOMER_REVIEW'
  | 'MOST_VIEWED'
  | 'PRICE_ASC'
  | 'PRICE_DESC'
  | 'NEWEST';

export interface ProductListingDto {
  id: string;
  productVariantId: string;
  variantAsin: string;
  variantName: string;
  sellerId: string;
  sellerName: string;
  sellerSku: string;
  price: number;
  minPriceFloor?: number | null;
  stockQuantity: number;
  fulfillmentType?: string;
  isBuyboxWinner?: boolean;
  status: string;
  createdAt: string;
}

export interface ProductVariant {
  id: string;
  productId: string;
  asin: string;
  variantName: string;
  variantAttributes: Record<string, unknown>;
  listings?: ProductListingDto[];
  createdAt: string;
}

export interface Product {
  id: string;
  sellerId: string | null;
  sellerName: string | null;
  brandId: string | null;
  brandName: string | null;
  categoryId: string | null;
  categoryName: string | null;
  title: string;
  masterSku?: string | null;
  governanceStatus?: string | null;
  description: string | null;
  basePrice: number | null;
  status: string;
  variants: ProductVariant[];
  averageRating: number;
  totalReviews: number;
  totalUnitsSold: number;
  viewCount: number;
  buyBoxPrice: number | null;
  mainImageUrl: string | null;
  isFavorited: boolean;
  createdAt: string;
  updatedAt: string;
  // Enterprise Storefront v1.1.0 Fields
  listPrice: number | null;
  discountPercentage: number | null;
  badgeTag: BadgeTag | null;
  modelNumber: string | null;
  deliveryEstimate: string | null;
  salesVolumeText: string | null;
  specifications: Record<string, string> | null;
}

export interface ProductSearchParams {
  query?: string;
  categoryId?: string;
  categorySlug?: string;
  brandId?: string;
  minPrice?: number;
  maxPrice?: number;
  minRating?: number;
  sortBy?: ProductSortBy;
  page?: number;
  size?: number;
}

export interface CartItem {
  id: string;
  listingId: string;
  productVariantId: string;
  productTitle: string;
  productMainImageUrl?: string | null;
  mainImageUrl?: string | null;
  variantName: string | null;
  asin: string | null;
  unitPrice: number;
  quantity: number;
  subtotal: number;
  isSavedForLater: boolean;
  addedAt: string;
  // Enterprise Storefront v1.1.0 Fields
  sellerSku: string | null;
  stockWarning: string | null;
  badgeTag: BadgeTag | null;
}

export interface Cart {
  id: string;
  userId: string;
  items: CartItem[];
  totalActiveItems: number;
  totalSavedForLaterItems: number;
  activeSubtotal: number;
}

export interface Review {
  id: string;
  productId: string;
  userId: string;
  userFullName: string;
  rating: number;
  comment: string | null;
  // Enterprise Storefront v1.1.0 Fields
  userTitleRole: string | null;
  isVerifiedEnterprise: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface Category {
  id: string;
  parentId: string | null;
  name: string;
  slug: string;
  level: number;
  isApproved: boolean;
  subCategories?: Category[];
}

export interface WishlistItem {
  id?: string;
  wishlistItemId?: string;
  productId: string;
  productTitle: string;
  productMainImageUrl?: string | null;
  mainImageUrl?: string | null;
  productBasePrice?: number | null;
  basePrice?: number | null;
  productAverageRating?: number;
  averageRating?: number;
  listingId?: string | null;
  addedAt: string;
}

export interface OrderItem {
  id: string;
  listingId: string;
  productTitle: string;
  variantName: string | null;
  asin: string | null;
  quantity: number;
  unitPrice: number;
  subtotal: number;
  status: string;
}

export interface Order {
  id: string;
  orderNumber: string;
  totalAmount: number;
  status: string;
  placedAt: string;
  items: OrderItem[];
}

export interface CheckoutRequest {
  shippingAddressId?: string;
  idempotencyKey: string;
  shippingAddress?: {
    fullName: string;
    streetLine1: string;
    streetLine2?: string;
    city: string;
    state: string;
    postalCode: string;
    country: string;
    phone?: string;
  };
  paymentMethod?: string;
}

export interface CreateReviewRequest {
  rating: number;
  comment?: string;
}

export interface AuthResponse {
  token: string;
  type?: string;
  tokenType?: string;
  email?: string;
  role?: string;
  fullName?: string;
  user?: {
    id?: string;
    fullName?: string;
    email?: string;
    phone?: string;
    roles?: string[];
    role?: string;
  };
}

export interface CustomerUser {
  id?: string;
  email: string;
  fullName: string;
  role: string;
}

export interface GuestCartItem {
  id?: string;
  listingId: string;
  productId: string;
  productTitle: string;
  variantName?: string | null;
  asin?: string | null;
  productMainImageUrl?: string | null;
  mainImageUrl?: string | null;
  unitPrice: number;
  quantity: number;
  subtotal?: number;
  sellerSku?: string | null;
  badgeTag?: BadgeTag | null;
  stockWarning?: string | null;
  addedAt: string;
  isSavedForLater?: boolean;
}
