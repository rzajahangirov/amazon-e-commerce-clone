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

  const fetchWishlist = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const res = await storefrontApi.getWishlist(0, 50);
      setItems(res.content || []);
    } catch {
      setItems([]);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchWishlist();
  }, [fetchWishlist]);

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
      await addToCart(item.productId, 1, {
        id: item.productId,
        title: item.productTitle,
        buyBoxPrice: item.productBasePrice,
        mainImageUrl: item.productMainImageUrl,
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
          {items.map((item) => (
            <div key={item.id} className="wl-card">
              <Link to={`/products/${item.productId}`} className="wl-thumb-link">
                <img
                  src={
                    item.productMainImageUrl ||
                    'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=300&auto=format&fit=crop&q=60'
                  }
                  alt={item.productTitle}
                />
              </Link>

              <div className="wl-info">
                <Link to={`/products/${item.productId}`} className="wl-item-title">
                  {item.productTitle}
                </Link>

                <div className="wl-rating">
                  <RatingStars rating={item.productAverageRating || 4.8} size="sm" />
                </div>

                <div className="wl-price">
                  ${(item.productBasePrice ?? 0).toFixed(2)}
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
          ))}
        </div>
      )}
    </div>
  );
};
