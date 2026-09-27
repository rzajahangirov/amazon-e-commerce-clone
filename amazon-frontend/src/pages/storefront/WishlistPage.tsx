import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { storefrontApi } from '../../api/storefrontApi';
import type { WishlistItem } from '../../api/storefrontTypes';
import { RatingStars } from '../../components/storefront/RatingStars';
import './WishlistPage.css';

export const WishlistPage: React.FC = () => {
  const { isAuthenticated, openAuthModal, addToCart, refreshWishlist } = useCustomerAuth();
  const [items, setItems] = useState<WishlistItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [actioningId, setActioningId] = useState<string | null>(null);

  // Helper to ensure each wishlist item displays its true product image and details
  const enrichWishlistItems = useCallback(async (rawItems: WishlistItem[]): Promise<WishlistItem[]> => {
    const promises = rawItems.map(async (item) => {
      const existingImage = item.productMainImageUrl || item.mainImageUrl;
      const existingPrice = item.productBasePrice ?? item.basePrice;
      const existingRating = item.productAverageRating ?? item.averageRating;
      const itemId = item.id || item.wishlistItemId || item.productId;

      if (existingImage && existingPrice != null && existingRating != null) {
        return {
          ...item,
          id: itemId,
          productMainImageUrl: existingImage,
          productBasePrice: existingPrice,
          productAverageRating: existingRating,
        };
      }

      // Fetch authentic product details to display the real image and metadata
      try {
        const prod = await storefrontApi.getProductById(item.productId);
        return {
          ...item,
          id: itemId,
          productMainImageUrl: prod.mainImageUrl || existingImage || null,
          productBasePrice: prod.buyBoxPrice ?? prod.basePrice ?? existingPrice ?? 0,
          productAverageRating: prod.averageRating ?? existingRating ?? 4.8,
          listingId: prod.variants?.[0]?.listings?.[0]?.id || item.listingId || null,
        };
      } catch {
        return {
          ...item,
          id: itemId,
          productMainImageUrl: existingImage || null,
          productBasePrice: existingPrice ?? 0,
          productAverageRating: existingRating ?? 4.8,
        };
      }
    });

    return Promise.all(promises);
  }, []);

  const fetchWishlist = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const res = await storefrontApi.getWishlist(0, 50);
      const raw = res.content || [];
      const enriched = await enrichWishlistItems(raw);
      setItems(enriched);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated, enrichWishlistItems]);

  useEffect(() => {
    if (!isAuthenticated) return;
    let ignore = false;
    const run = async () => {
      try {
        const res = await storefrontApi.getWishlist(0, 50);
        const raw = res.content || [];
        const enriched = await enrichWishlistItems(raw);
        if (!ignore) setItems(enriched);
      } catch {
        if (!ignore) setItems([]);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, [isAuthenticated, enrichWishlistItems]);

  const handleRemove = async (productId: string) => {
    try {
      setActioningId(productId);
      await storefrontApi.removeFromWishlist(productId);
      await fetchWishlist();
      await refreshWishlist();
    } finally {
      setActioningId(null);
    }
  };

  const handleMoveToCart = async (item: WishlistItem) => {
    try {
      setActioningId(item.productId);
      const targetListingId = item.listingId || item.productId;
      await addToCart(targetListingId, 1, {
        id: item.productId,
        title: item.productTitle,
        buyBoxPrice: item.productBasePrice ?? item.basePrice ?? 0,
        mainImageUrl: item.productMainImageUrl || item.mainImageUrl,
      });
      await storefrontApi.removeFromWishlist(item.productId);
      await fetchWishlist();
      await refreshWishlist();
    } finally {
      setActioningId(null);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="wl-unauth-container">
        <h2>Your Wish List</h2>
        <p>Sign in to view items saved to your corporate wishlist.</p>
        <button
          type="button"
          onClick={() => openAuthModal('signin')}
          className="wl-signin-btn"
        >
          Sign In
        </button>
      </div>
    );
  }

  return (
    <div className="wl-container">
      <div className="wl-header">
        <h1 className="wl-title">Your Enterprise Wish List</h1>
        <div className="wl-count">
          {items.length} {items.length === 1 ? 'item' : 'items'} saved for future procurement
        </div>
      </div>

      {loading ? (
        <div className="wl-loading">Loading saved items...</div>
      ) : items.length === 0 ? (
        <div className="wl-empty-card">
          <h3>Your Wish List is currently empty</h3>
          <p>Click the heart icon on any product in the catalog to save it here.</p>
          <Link to="/products" className="wl-shop-btn">
            Browse Products
          </Link>
        </div>
      ) : (
        <div className="wl-grid">
          {items.map((item) => {
            const displayImage =
              item.productMainImageUrl ||
              item.mainImageUrl ||
              'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&auto=format&fit=crop&q=80';
            const displayPrice = item.productBasePrice ?? item.basePrice ?? 0;
            const displayRating = item.productAverageRating ?? item.averageRating ?? 4.8;
            const itemId = item.id || item.wishlistItemId || item.productId;

            return (
              <div key={itemId} className="wl-card">
                <Link to={`/products/${item.productId}`} className="wl-thumb-link">
                  <img
                    src={displayImage}
                    alt={item.productTitle}
                  />
                </Link>

                <div className="wl-info">
                  <Link to={`/products/${item.productId}`} className="wl-item-title">
                    {item.productTitle}
                  </Link>

                  <div className="wl-rating">
                    <RatingStars rating={displayRating} size="sm" />
                  </div>

                  <div className="wl-price">
                    ${displayPrice.toFixed(2)}
                  </div>

                  <div className="wl-added-date">
                    Added on {new Date(item.addedAt).toLocaleDateString()}
                  </div>

                  <div className="wl-actions">
                    <button
                      type="button"
                      onClick={() => handleMoveToCart(item)}
                      disabled={actioningId === item.productId}
                      className="wl-btn add"
                    >
                      Move to Cart
                    </button>
                    <button
                      type="button"
                      onClick={() => handleRemove(item.productId)}
                      disabled={actioningId === item.productId}
                      className="wl-btn remove"
                    >
                      Remove
                    </button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
