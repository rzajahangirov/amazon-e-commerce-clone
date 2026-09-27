import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { BadgePill } from '../../components/storefront/BadgePill';
import './CartPage.css';

export const CartPage: React.FC = () => {
  const {
    isAuthenticated,
    cart,
    guestCart,
    cartCount,
    cartSubtotal,
    loadingCart,
    updateCartQuantity,
    removeCartItem,
    toggleSaveForLater,
    openAuthModal,
  } = useCustomerAuth();

  const navigate = useNavigate();

  // Combine items depending on authentication
  const activeItems = isAuthenticated
    ? (cart?.items || []).filter((i) => !i.isSavedForLater)
    : guestCart.filter((i) => !i.isSavedForLater);

  const savedItems = isAuthenticated
    ? (cart?.items || []).filter((i) => i.isSavedForLater)
    : guestCart.filter((i) => i.isSavedForLater);

  const handleCheckoutClick = () => {
    if (!isAuthenticated) {
      openAuthModal('signin', 'Please sign in to complete enterprise checkout');
    } else {
      navigate('/checkout');
    }
  };

  return (
    <div className="cart-container">
      <div className="cart-layout">
        {/* Main Cart Items Column */}
        <div className="cart-main-col">
          {/* Guest Sign-in Alert Banner */}
          {!isAuthenticated && (
            <div className="cart-guest-banner">
              <div>
                <strong>Working as a Guest?</strong> Sign in to sync your enterprise cart across
                devices and unlock Net-30 Corporate Invoicing.
              </div>
              <button
                type="button"
                onClick={() => openAuthModal('signin')}
                className="cart-guest-signin-btn"
              >
                Sign In
              </button>
            </div>
          )}

          {/* Active Cart Section */}
          <div className="cart-section-card">
            <div className="cart-header-row">
              <h1 className="cart-title">Shopping Cart</h1>
              <span className="cart-price-header">Price</span>
            </div>

            {loadingCart ? (
              <div className="cart-loading">Updating your cart...</div>
            ) : activeItems.length === 0 ? (
              <div className="cart-empty-state">
                <h2>Your Amazon Enterprise Cart is empty.</h2>
                <p>Explore thousands of workstation displays, accessories, and enterprise hardware.</p>
                <Link to="/products" className="cart-shop-btn">
                  Continue Shopping
                </Link>
              </div>
            ) : (
              <div className="cart-items-list">
                {activeItems.map((item) => {
                  const itemId = item.id || (item as { listingId: string }).listingId;
                  const itemTitle = item.productTitle;
                  const itemUnitPrice = item.unitPrice;
                  const itemQuantity = item.quantity;
                  const itemSubtotal = item.subtotal ?? itemUnitPrice * itemQuantity;

                  return (
                    <div key={itemId} className="cart-item-row">
                      <div className="cart-item-thumb">
                        <img
                          src={
                            (item as { mainImageUrl?: string }).mainImageUrl ||
                            'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=300&auto=format&fit=crop&q=60'
                          }
                          alt={itemTitle}
                        />
                      </div>

                      <div className="cart-item-info">
                        <div className="cart-item-title-row">
                          <h3 className="cart-item-title">{itemTitle}</h3>
                          <div className="cart-item-price-mobile">${itemSubtotal.toFixed(2)}</div>
                        </div>

                        {/* Variant / ASIN / Seller SKU */}
                        <div className="cart-item-meta">
                          {item.variantName && <span>Variant: {item.variantName} • </span>}
                          {item.asin && <span>ASIN: {item.asin} • </span>}
                          {item.sellerSku && (
                            <span className="cart-item-sku">SKU: {item.sellerSku}</span>
                          )}
                        </div>

                        {/* Item Badge Tag */}
                        {item.badgeTag && (
                          <div style={{ margin: '4px 0' }}>
                            <BadgePill tag={item.badgeTag} size="sm" />
                          </div>
                        )}

                        {/* Stock Warning Alert Banner */}
                        {item.stockWarning && (
                          <div className="cart-stock-warning">
                            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                              <circle cx="12" cy="12" r="10" />
                              <line x1="12" y1="8" x2="12" y2="12" />
                              <line x1="12" y1="16" x2="12.01" y2="16" />
                            </svg>
                            <span>{item.stockWarning}</span>
                          </div>
                        )}

                        <div className="cart-item-stock-status">In Stock</div>

                        {/* Controls: Stepper, Delete, Save for Later */}
                        <div className="cart-item-controls">
                          <div className="cart-qty-stepper">
                            <button
                              type="button"
                              onClick={() => updateCartQuantity(itemId, itemQuantity - 1)}
                              disabled={itemQuantity <= 1}
                              className="cart-qty-btn"
                            >
                              -
                            </button>
                            <span className="cart-qty-value">{itemQuantity}</span>
                            <button
                              type="button"
                              onClick={() => updateCartQuantity(itemId, itemQuantity + 1)}
                              className="cart-qty-btn"
                            >
                              +
                            </button>
                          </div>

                          <span className="cart-control-divider">|</span>

                          <button
                            type="button"
                            onClick={() => removeCartItem(itemId)}
                            className="cart-action-link delete"
                          >
                            Delete
                          </button>

                          <span className="cart-control-divider">|</span>

                          <button
                            type="button"
                            onClick={() => toggleSaveForLater(itemId)}
                            className="cart-action-link"
                          >
                            Save for later
                          </button>
                        </div>
                      </div>

                      <div className="cart-item-price-desktop">
                        <span className="cart-price-bold">${itemSubtotal.toFixed(2)}</span>
                        {itemQuantity > 1 && (
                          <span className="cart-unit-price">
                            (${itemUnitPrice.toFixed(2)} each)
                          </span>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
            )}

            {activeItems.length > 0 && (
              <div className="cart-subtotal-footer">
                Subtotal ({cartCount} {cartCount === 1 ? 'item' : 'items'}):{' '}
                <span className="cart-subtotal-amount">${cartSubtotal.toFixed(2)}</span>
              </div>
            )}
          </div>

          {/* Saved for Later Section */}
          {savedItems.length > 0 && (
            <div className="cart-section-card" style={{ marginTop: '24px' }}>
              <h2 className="cart-saved-title">Saved for later ({savedItems.length} items)</h2>
              <div className="cart-items-list">
                {savedItems.map((item) => {
                  const itemId = item.id || (item as { listingId: string }).listingId;
                  return (
                    <div key={itemId} className="cart-item-row saved">
                      <div className="cart-item-thumb">
                        <img
                          src={
                            (item as { mainImageUrl?: string }).mainImageUrl ||
                            'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=300&auto=format&fit=crop&q=60'
                          }
                          alt={item.productTitle}
                        />
                      </div>
                      <div className="cart-item-info">
                        <h4 className="cart-item-title">{item.productTitle}</h4>
                        <div className="cart-price-bold">${item.unitPrice.toFixed(2)}</div>
                        <div className="cart-item-controls" style={{ marginTop: '10px' }}>
                          <button
                            type="button"
                            onClick={() => toggleSaveForLater(itemId)}
                            className="cart-move-btn"
                          >
                            Move to cart
                          </button>
                          <button
                            type="button"
                            onClick={() => removeCartItem(itemId)}
                            className="cart-action-link delete"
                          >
                            Delete
                          </button>
                        </div>
                      </div>
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>

        {/* Right Sticky Checkout Sidebar */}
        <div className="cart-sidebar-col">
          <div className="cart-summary-card">
            {/* Free Shipping Meter */}
            <div className="cart-shipping-banner">
              <div className="cart-shipping-check">✓</div>
              <div className="cart-shipping-text">
                Your order qualifies for <strong>FREE Enterprise Delivery</strong>.
              </div>
            </div>

            <div className="cart-summary-subtotal">
              Subtotal ({cartCount} {cartCount === 1 ? 'item' : 'items'}):{' '}
              <span className="cart-summary-amount">${cartSubtotal.toFixed(2)}</span>
            </div>

            <button
              type="button"
              onClick={handleCheckoutClick}
              disabled={activeItems.length === 0}
              className="cart-checkout-btn"
            >
              Proceed to checkout
            </button>

            <div className="cart-security-badge">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#565959" strokeWidth="2">
                <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                <path d="M7 11V7a5 5 0 0 1 10 0v4" />
              </svg>
              <span>Encrypted corporate checkout with SLA fulfillment</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
