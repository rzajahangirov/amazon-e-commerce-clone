import React, { createContext, useContext, useEffect, useState, useMemo, useCallback } from 'react';
import { storefrontApi } from '../api/storefrontApi';
import {
  CUSTOMER_TOKEN_KEY,
  setCustomerAccessToken,
  removeCustomerAccessToken,
} from '../api/client';
import type {
  Cart,
  CustomerUser,
  GuestCartItem,
  Product,
} from '../api/storefrontTypes';

const GUEST_CART_STORAGE_KEY = 'amazon_guest_cart_v1';
const CUSTOMER_USER_KEY = 'amazon_customer_user_v1';

export interface CustomerAuthContextType {
  user: CustomerUser | null;
  token: string | null;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (payload: { fullName: string; email: string; password: string; phone?: string }) => Promise<void>;
  logout: () => void;

  // Cart
  cart: Cart | null;
  guestCart: GuestCartItem[];
  cartCount: number;
  cartSubtotal: number;
  loadingCart: boolean;
  addToCart: (listingId: string, quantity?: number, productDetails?: Partial<Product>) => Promise<void>;
  updateCartQuantity: (idOrListingId: string, quantity: number) => Promise<void>;
  removeCartItem: (idOrListingId: string) => Promise<void>;
  toggleSaveForLater: (itemId: string) => Promise<void>;
  refreshCart: () => Promise<void>;

  // Wishlist
  wishlistIds: Set<string>;
  isFavorited: (productId: string) => boolean;
  toggleFavorite: (product: Product) => Promise<boolean>;
  refreshWishlist: () => Promise<void>;

  // Auth Modal
  authModalOpen: boolean;
  authModalMode: 'signin' | 'register';
  authModalMessage: string;
  openAuthModal: (mode?: 'signin' | 'register', message?: string) => void;
  closeAuthModal: () => void;
}

const CustomerAuthContext = createContext<CustomerAuthContextType | undefined>(undefined);

export const CustomerAuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem(CUSTOMER_TOKEN_KEY));
  const [user, setUser] = useState<CustomerUser | null>(() => {
    const raw = localStorage.getItem(CUSTOMER_USER_KEY);
    return raw ? JSON.parse(raw) : null;
  });

  const [serverCart, setServerCart] = useState<Cart | null>(null);
  const [guestCart, setGuestCart] = useState<GuestCartItem[]>(() => {
    try {
      const raw = localStorage.getItem(GUEST_CART_STORAGE_KEY);
      return raw ? JSON.parse(raw) : [];
    } catch {
      return [];
    }
  });

  const [loadingCart, setLoadingCart] = useState(false);
  const [wishlistIds, setWishlistIds] = useState<Set<string>>(new Set());

  // Auth modal
  const [authModalOpen, setAuthModalOpen] = useState(false);
  const [authModalMode, setAuthModalMode] = useState<'signin' | 'register'>('signin');
  const [authModalMessage, setAuthModalMessage] = useState('');

  const isAuthenticated = Boolean(token && user);

  // Persist guest cart
  useEffect(() => {
    localStorage.setItem(GUEST_CART_STORAGE_KEY, JSON.stringify(guestCart));
  }, [guestCart]);

  // Load wishlist when authenticated
  const refreshWishlist = useCallback(async () => {
    if (!isAuthenticated) {
      setWishlistIds(new Set());
      return;
    }
    try {
      const payload = await storefrontApi.getWishlist(0, 100);
      const ids = new Set((payload.content || []).map((w) => w.productId));
      setWishlistIds(ids);
    } catch {
      // ignore
    }
  }, [isAuthenticated]);

  // Load server cart when authenticated
  const refreshCart = useCallback(async () => {
    if (!isAuthenticated) {
      setServerCart(null);
      return;
    }
    try {
      setLoadingCart(true);
      const data = await storefrontApi.getCart();
      setServerCart(data);
    } catch {
      setServerCart(null);
    } finally {
      setLoadingCart(false);
    }
  }, [isAuthenticated]);

  // Synchronize guest cart items upon login
  const syncGuestCart = async () => {
    if (guestCart.length === 0) return;
    try {
      for (const item of guestCart) {
        if (item.listingId) {
          try {
            await storefrontApi.addToCart(item.listingId, item.quantity);
          } catch {
            // continue syncing others
          }
        }
      }
      setGuestCart([]);
      localStorage.removeItem(GUEST_CART_STORAGE_KEY);
    } catch {
      // ignore
    }
  };

  useEffect(() => {
    if (!isAuthenticated) return;
    let ignore = false;
    const sync = async () => {
      try {
        const [cartRes, wishRes] = await Promise.all([
          storefrontApi.getCart().catch(() => null),
          storefrontApi.getWishlist(0, 100).catch(() => null),
        ]);
        if (!ignore) {
          if (cartRes) {
            setServerCart(cartRes);
          }
          if (wishRes) {
            setWishlistIds(new Set((wishRes.content || []).map((w: { productId: string }) => w.productId)));
          }
        }
      } catch {
        // ignore
      }
    };
    void sync();
    return () => { ignore = true; };
  }, [isAuthenticated]);

  const login = async (email: string, password: string) => {
    const res = await storefrontApi.login(email, password);
    setCustomerAccessToken(res.token);
    setToken(res.token);

    const resolvedEmail = res.user?.email || res.email || email;
    const emailPrefix = resolvedEmail?.split('@')?.[0] ?? '';
    const resolvedFullName =
      res.user?.fullName ||
      res.fullName ||
      emailPrefix ||
      'Customer';
    const resolvedRole =
      (res.user?.roles && res.user.roles.length > 0 ? res.user.roles[0] : undefined) ||
      res.user?.role ||
      res.role ||
      'ROLE_CUSTOMER';

    const userData: CustomerUser = {
      id: res.user?.id,
      email: resolvedEmail,
      fullName: resolvedFullName,
      role: resolvedRole,
    };
    setUser(userData);
    localStorage.setItem(CUSTOMER_USER_KEY, JSON.stringify(userData));

    // Automatically synchronize guest cart to server
    try {
      await syncGuestCart();
      await refreshCart();
      await refreshWishlist();
    } catch {
      // ignore non-critical sync errors
    }
    closeAuthModal();
  };

  const register = async (payload: { fullName: string; email: string; password: string; phone?: string }) => {
    const res = await storefrontApi.register(payload);
    setCustomerAccessToken(res.token);
    setToken(res.token);

    const resolvedEmail = res.user?.email || res.email || payload.email;
    const emailPrefix = resolvedEmail?.split('@')?.[0] ?? '';
    const resolvedFullName =
      res.user?.fullName ||
      res.fullName ||
      payload.fullName ||
      emailPrefix ||
      'Customer';
    const resolvedRole =
      (res.user?.roles && res.user.roles.length > 0 ? res.user.roles[0] : undefined) ||
      res.user?.role ||
      res.role ||
      'ROLE_CUSTOMER';

    const userData: CustomerUser = {
      id: res.user?.id,
      email: resolvedEmail,
      fullName: resolvedFullName,
      role: resolvedRole,
    };
    setUser(userData);
    localStorage.setItem(CUSTOMER_USER_KEY, JSON.stringify(userData));

    // Automatically synchronize guest cart to server
    try {
      await syncGuestCart();
      await refreshCart();
      await refreshWishlist();
    } catch {
      // ignore non-critical sync errors
    }
    closeAuthModal();
  };

  const logout = () => {
    removeCustomerAccessToken();
    localStorage.removeItem(CUSTOMER_USER_KEY);
    setToken(null);
    setUser(null);
    setServerCart(null);
    setWishlistIds(new Set());
  };

  // Add to cart (Guest vs Auth)
  const addToCart = async (listingId: string, quantity: number = 1, productDetails?: Partial<Product>) => {
    if (isAuthenticated) {
      const updated = await storefrontApi.addToCart(listingId, quantity);
      setServerCart(updated);
    } else {
      setGuestCart((prev) => {
        const existingIdx = prev.findIndex((i) => i.listingId === listingId);
        if (existingIdx >= 0) {
          const updated = [...prev];
          updated[existingIdx] = {
            ...updated[existingIdx],
            quantity: updated[existingIdx].quantity + quantity,
          };
          return updated;
        }

        const newItem: GuestCartItem = {
          listingId,
          productId: productDetails?.id || '',
          productTitle: productDetails?.title || 'Selected Product',
          variantName: productDetails?.variants?.[0]?.variantName || null,
          mainImageUrl: productDetails?.mainImageUrl || null,
          unitPrice: productDetails?.buyBoxPrice ?? productDetails?.basePrice ?? 0,
          quantity,
          sellerSku: productDetails?.masterSku || null,
          badgeTag: productDetails?.badgeTag || null,
          stockWarning: null,
          addedAt: new Date().toISOString(),
          isSavedForLater: false,
        };
        return [...prev, newItem];
      });
    }
  };

  const updateCartQuantity = async (idOrListingId: string, quantity: number) => {
    if (isAuthenticated) {
      if (quantity <= 0) {
        const updated = await storefrontApi.removeCartItem(idOrListingId);
        setServerCart(updated);
      } else {
        const updated = await storefrontApi.updateCartItem(idOrListingId, quantity);
        setServerCart(updated);
      }
    } else {
      if (quantity <= 0) {
        setGuestCart((prev) => prev.filter((i) => i.listingId !== idOrListingId));
      } else {
        setGuestCart((prev) =>
          prev.map((i) => (i.listingId === idOrListingId ? { ...i, quantity } : i))
        );
      }
    }
  };

  const removeCartItem = async (idOrListingId: string) => {
    if (isAuthenticated) {
      const updated = await storefrontApi.removeCartItem(idOrListingId);
      setServerCart(updated);
    } else {
      setGuestCart((prev) => prev.filter((i) => i.listingId !== idOrListingId));
    }
  };

  const toggleSaveForLater = async (itemId: string) => {
    if (isAuthenticated) {
      const updated = await storefrontApi.toggleSaveForLater(itemId);
      setServerCart(updated);
    } else {
      setGuestCart((prev) =>
        prev.map((i) => (i.listingId === itemId ? { ...i, isSavedForLater: !i.isSavedForLater } : i))
      );
    }
  };

  // Cart Metrics
  const cartCount = useMemo(() => {
    if (isAuthenticated && serverCart) {
      return serverCart.totalActiveItems ?? serverCart.items.filter((i) => !i.isSavedForLater).reduce((acc, i) => acc + i.quantity, 0);
    }
    return guestCart.filter((i) => !i.isSavedForLater).reduce((acc, i) => acc + i.quantity, 0);
  }, [isAuthenticated, serverCart, guestCart]);

  const cartSubtotal = useMemo(() => {
    if (isAuthenticated && serverCart) {
      return serverCart.activeSubtotal ?? serverCart.items.filter((i) => !i.isSavedForLater).reduce((acc, i) => acc + i.subtotal, 0);
    }
    return guestCart
      .filter((i) => !i.isSavedForLater)
      .reduce((acc, i) => acc + i.unitPrice * i.quantity, 0);
  }, [isAuthenticated, serverCart, guestCart]);

  // Wishlist / Favorites
  const isFavorited = (productId: string) => wishlistIds.has(productId);

  const toggleFavorite = async (product: Product): Promise<boolean> => {
    if (!isAuthenticated) {
      openAuthModal('signin', 'Sign in to add items to your Wish List');
      return false;
    }

    const productId = product.id;
    if (wishlistIds.has(productId)) {
      await storefrontApi.removeFromWishlist(productId);
      setWishlistIds((prev) => {
        const next = new Set(prev);
        next.delete(productId);
        return next;
      });
      return false;
    } else {
      await storefrontApi.addToWishlist(productId);
      setWishlistIds((prev) => new Set([...prev, productId]));
      return true;
    }
  };

  const openAuthModal = (mode: 'signin' | 'register' = 'signin', message: string = '') => {
    setAuthModalMode(mode);
    setAuthModalMessage(message);
    setAuthModalOpen(true);
  };

  const closeAuthModal = () => {
    setAuthModalOpen(false);
    setAuthModalMessage('');
  };

  return (
    <CustomerAuthContext.Provider
      value={{
        user,
        token,
        isAuthenticated,
        login,
        register,
        logout,
        cart: serverCart,
        guestCart,
        cartCount,
        cartSubtotal,
        loadingCart,
        addToCart,
        updateCartQuantity,
        removeCartItem,
        toggleSaveForLater,
        refreshCart,
        wishlistIds,
        isFavorited,
        toggleFavorite,
        refreshWishlist,
        authModalOpen,
        authModalMode,
        authModalMessage,
        openAuthModal,
        closeAuthModal,
      }}
    >
      {children}
    </CustomerAuthContext.Provider>
  );
};

// oxlint-disable-next-line react/only-export-components
export const useCustomerAuth = () => {
  const ctx = useContext(CustomerAuthContext);
  if (!ctx) {
    throw new Error('useCustomerAuth must be used within CustomerAuthProvider');
  }
  return ctx;
};
