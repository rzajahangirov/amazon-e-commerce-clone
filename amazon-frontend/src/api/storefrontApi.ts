import { apiGet, apiRequest } from './client';
import type { PaginationPayload } from './types';
import type {
  AuthResponse,
  Cart,
  Category,
  CheckoutRequest,
  CreateReviewRequest,
  Order,
  Product,
  ProductListingDto,
  ProductVariant,
  ProductSearchParams,
  Review,
  WishlistItem,
} from './storefrontTypes';

export const storefrontApi = {
  // ── Products & Catalog ───────────────────────────────────────
  async searchProducts(params: ProductSearchParams = {}): Promise<PaginationPayload<Product>> {
    return apiGet<PaginationPayload<Product>>('/products/search', {
      query: params.query,
      categoryId: params.categoryId,
      categorySlug: params.categorySlug,
      brandId: params.brandId,
      minPrice: params.minPrice,
      maxPrice: params.maxPrice,
      minRating: params.minRating,
      sortBy: params.sortBy,
      page: params.page ?? 0,
      size: params.size ?? 20,
    });
  },

  async getProductById(id: string): Promise<Product> {
    return apiGet<Product>(`/products/${id}`);
  },

  async getFrequentlyBoughtTogether(id: string): Promise<Product[]> {
    return apiGet<Product[]>(`/products/${id}/frequently-bought-together`);
  },

  async getVariantBuybox(variantId: string): Promise<ProductListingDto> {
    return apiGet<ProductListingDto>(`/products/variants/${variantId}/buybox`);
  },

  async getVariantByAsin(asin: string): Promise<ProductVariant> {
    return apiGet<ProductVariant>(`/products/variants/asin/${encodeURIComponent(asin)}`);
  },

  async getVariantListings(variantId: string): Promise<ProductListingDto[]> {
    return apiGet<ProductListingDto[]>(`/products/variants/${variantId}/listings`);
  },

  // ── Categories ───────────────────────────────────────────────
  async getRootCategories(): Promise<Category[]> {
    return apiGet<Category[]>('/categories');
  },

  async getCategoryById(id: string): Promise<Category> {
    return apiGet<Category>(`/categories/${id}`);
  },

  async getCategoryBySlug(slug: string): Promise<Category> {
    return apiGet<Category>(`/categories/slug/${slug}`);
  },

  // ── Shopping Cart ────────────────────────────────────────────
  async getCart(): Promise<Cart> {
    return apiGet<Cart>('/cart');
  },

  async addToCart(listingId: string, quantity: number = 1): Promise<Cart> {
    const res = await apiRequest<Cart>('/cart/items', {
      method: 'POST',
      body: JSON.stringify({ listingId, quantity }),
    });
    return res.data!;
  },

  async updateCartItem(itemId: string, quantity: number): Promise<Cart> {
    const res = await apiRequest<Cart>(`/cart/items/${itemId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    });
    return res.data!;
  },

  async removeCartItem(itemId: string): Promise<Cart> {
    const res = await apiRequest<Cart>(`/cart/items/${itemId}`, {
      method: 'DELETE',
    });
    return res.data!;
  },

  async toggleSaveForLater(itemId: string): Promise<Cart> {
    const res = await apiRequest<Cart>(`/cart/items/${itemId}/save-for-later`, {
      method: 'POST',
    });
    return res.data!;
  },

  async clearCart(): Promise<void> {
    await apiRequest<void>('/cart', {
      method: 'DELETE',
    });
  },

  // ── Orders & Checkout ────────────────────────────────────────
  async checkout(request: CheckoutRequest): Promise<Order> {
    const res = await apiRequest<Order>('/orders/checkout', {
      method: 'POST',
      body: JSON.stringify(request),
    });
    return res.data!;
  },

  async getMyOrders(page: number = 0, size: number = 10): Promise<PaginationPayload<Order>> {
    return apiGet<PaginationPayload<Order>>('/orders/my-orders', { page, size });
  },

  async getOrderById(id: string): Promise<Order> {
    return apiGet<Order>(`/orders/${id}`);
  },

  async cancelOrder(id: string): Promise<Order> {
    const res = await apiRequest<Order>(`/orders/${id}/cancel`, {
      method: 'POST',
    });
    return res.data!;
  },

  // ── Reviews & Ratings ────────────────────────────────────────
  async getProductReviews(
    productId: string,
    page: number = 0,
    size: number = 10,
  ): Promise<PaginationPayload<Review>> {
    return apiGet<PaginationPayload<Review>>(`/products/${productId}/reviews`, { page, size });
  },

  async createReview(productId: string, request: CreateReviewRequest): Promise<Review> {
    const res = await apiRequest<Review>(`/products/${productId}/reviews`, {
      method: 'POST',
      body: JSON.stringify(request),
    });
    return res.data!;
  },

  // ── Wishlist (Favorites) ─────────────────────────────────────
  async getWishlist(page: number = 0, size: number = 20): Promise<PaginationPayload<WishlistItem>> {
    return apiGet<PaginationPayload<WishlistItem>>('/wishlist', { page, size });
  },

  async addToWishlist(productId: string): Promise<WishlistItem> {
    const res = await apiRequest<WishlistItem>(`/wishlist/${productId}`, {
      method: 'POST',
    });
    return res.data!;
  },

  async removeFromWishlist(productId: string): Promise<void> {
    await apiRequest<void>(`/wishlist/${productId}`, {
      method: 'DELETE',
    });
  },

  // ── Authentication ───────────────────────────────────────────
  async login(email: string, password: string): Promise<AuthResponse> {
    const res = await apiRequest<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    return res.data!;
  },

  async register(payload: {
    fullName: string;
    email: string;
    password: string;
    phone?: string;
  }): Promise<AuthResponse> {
    const res = await apiRequest<AuthResponse>('/auth/register', {
      method: 'POST',
      body: JSON.stringify(payload),
    });
    return res.data!;
  },
};
