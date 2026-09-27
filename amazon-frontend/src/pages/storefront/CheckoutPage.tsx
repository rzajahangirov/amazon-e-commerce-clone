import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { storefrontApi } from '../../api/storefrontApi';
import type { Order } from '../../api/storefrontTypes';
import './CheckoutPage.css';

export const CheckoutPage: React.FC = () => {
  const { user, isAuthenticated, cart, guestCart, cartSubtotal, refreshCart, openAuthModal } =
    useCustomerAuth();
  const navigate = useNavigate();

  // Active items
  const activeItems = isAuthenticated
    ? (cart?.items || []).filter((i) => !i.isSavedForLater)
    : guestCart.filter((i) => !i.isSavedForLater);

  // Form State
  const [address, setAddress] = useState({
    fullName: user?.fullName || 'Ericsson Lindqvist',
    streetLine1: '800 5th Avenue, Suite 4100',
    city: 'Seattle',
    state: 'WA',
    postalCode: '98104',
    country: 'United States',
    phone: '+1 (555) 234-5678',
  });

  const [paymentMethod, setPaymentMethod] = useState<'INVOICE' | 'CARD' | 'AMAZON_PAY'>('INVOICE');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [confirmedOrder, setConfirmedOrder] = useState<Order | null>(null);

  if (!isAuthenticated) {
    return (
      <div className="chk-unauth-container">
        <h2>Enterprise Checkout Required</h2>
        <p>Please sign in with your corporate account to access checkout and billing.</p>
        <button
          type="button"
          onClick={() => openAuthModal('signin', 'Sign in to complete your enterprise order')}
          className="chk-signin-btn"
        >
          Sign In to Continue
        </button>
      </div>
    );
  }

  if (activeItems.length === 0 && !confirmedOrder) {
    return (
      <div className="chk-empty-container">
        <h2>Your cart is empty</h2>
        <p>Add products to your cart before proceeding to checkout.</p>
        <Link to="/products" className="chk-shop-btn">
          Browse Catalog
        </Link>
      </div>
    );
  }

  const handlePlaceOrder = async () => {
    try {
      setLoading(true);
      setError(null);

      // Generate unique idempotency key
      const idempotencyKey = `chk-${Date.now()}-${Math.random().toString(36).substring(2, 9)}`;

      const order = await storefrontApi.checkout({
        idempotencyKey,
        shippingAddress: address,
        paymentMethod,
      });

      setConfirmedOrder(order);
      await refreshCart();
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError('Order placement failed. Please verify your billing information.');
      }
    } finally {
      setLoading(false);
    }
  };

  // Order Confirmation Success View
  if (confirmedOrder) {
    return (
      <div className="chk-success-container">
        <div className="chk-success-card">
          <div className="chk-success-icon">✓</div>
          <h1 className="chk-success-title">Order Placed, Thank You!</h1>
          <p className="chk-success-subtitle">
            Confirmation has been sent to <strong>{user?.email}</strong>.
          </p>

          <div className="chk-order-details-box">
            <div className="chk-order-detail-row">
              <span>Order Number:</span>
              <strong>{confirmedOrder.orderNumber}</strong>
            </div>
            <div className="chk-order-detail-row">
              <span>Total Amount:</span>
              <strong>${confirmedOrder.totalAmount?.toFixed(2)}</strong>
            </div>
            <div className="chk-order-detail-row">
              <span>Fulfillment Status:</span>
              <span className="chk-status-pill">{confirmedOrder.status}</span>
            </div>
            <div className="chk-order-detail-row">
              <span>Delivery:</span>
              <span>FREE Enterprise Priority Delivery</span>
            </div>
          </div>

          <div className="chk-success-actions">
            <button
              type="button"
              onClick={() => navigate('/orders')}
              className="chk-view-orders-btn"
            >
              View Your Orders
            </button>
            <Link to="/products" className="chk-continue-btn">
              Continue Shopping
            </Link>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="chk-container">
      <div className="chk-header">
        <h1 className="chk-title">
          Checkout <span>({activeItems.length} {activeItems.length === 1 ? 'item' : 'items'})</span>
        </h1>
        <div className="chk-security-lock">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#565959" strokeWidth="2">
            <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
            <path d="M7 11V7a5 5 0 0 1 10 0v4" />
          </svg>
          SSL Secured Enterprise Channel
        </div>
      </div>

      {error && <div className="chk-error-banner">{error}</div>}

      <div className="chk-layout">
        {/* Main Steps */}
        <div className="chk-steps-col">
          {/* Step 1: Shipping Address */}
          <div className="chk-step-card">
            <div className="chk-step-num">1</div>
            <div className="chk-step-content">
              <h3 className="chk-step-title">Shipping Address</h3>
              <div className="chk-address-grid">
                <div>
                  <label className="chk-label">Full Name</label>
                  <input
                    type="text"
                    value={address.fullName}
                    onChange={(e) => setAddress({ ...address, fullName: e.target.value })}
                    className="chk-input"
                  />
                </div>
                <div>
                  <label className="chk-label">Street Address</label>
                  <input
                    type="text"
                    value={address.streetLine1}
                    onChange={(e) => setAddress({ ...address, streetLine1: e.target.value })}
                    className="chk-input"
                  />
                </div>
                <div style={{ display: 'grid', gridTemplateColumns: '2fr 1fr 1fr', gap: '10px' }}>
                  <div>
                    <label className="chk-label">City</label>
                    <input
                      type="text"
                      value={address.city}
                      onChange={(e) => setAddress({ ...address, city: e.target.value })}
                      className="chk-input"
                    />
                  </div>
                  <div>
                    <label className="chk-label">State</label>
                    <input
                      type="text"
                      value={address.state}
                      onChange={(e) => setAddress({ ...address, state: e.target.value })}
                      className="chk-input"
                    />
                  </div>
                  <div>
                    <label className="chk-label">ZIP Code</label>
                    <input
                      type="text"
                      value={address.postalCode}
                      onChange={(e) => setAddress({ ...address, postalCode: e.target.value })}
                      className="chk-input"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Step 2: Payment Method */}
          <div className="chk-step-card">
            <div className="chk-step-num">2</div>
            <div className="chk-step-content">
              <h3 className="chk-step-title">Payment Method</h3>
              <div className="chk-payment-options">
                <label className={`chk-payment-option ${paymentMethod === 'INVOICE' ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name="payment"
                    checked={paymentMethod === 'INVOICE'}
                    onChange={() => setPaymentMethod('INVOICE')}
                  />
                  <div>
                    <strong>Corporate Net-30 Invoicing</strong>
                    <p>Direct invoice dispatched to AP department with 30-day terms.</p>
                  </div>
                </label>

                <label className={`chk-payment-option ${paymentMethod === 'CARD' ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name="payment"
                    checked={paymentMethod === 'CARD'}
                    onChange={() => setPaymentMethod('CARD')}
                  />
                  <div>
                    <strong>Corporate Credit / Purchasing Card</strong>
                    <p>Ending in 8842 • Expedited authorization</p>
                  </div>
                </label>

                <label className={`chk-payment-option ${paymentMethod === 'AMAZON_PAY' ? 'selected' : ''}`}>
                  <input
                    type="radio"
                    name="payment"
                    checked={paymentMethod === 'AMAZON_PAY'}
                    onChange={() => setPaymentMethod('AMAZON_PAY')}
                  />
                  <div>
                    <strong>Amazon Enterprise Pay Balance</strong>
                    <p>Pre-funded corporate purchasing balance</p>
                  </div>
                </label>
              </div>
            </div>
          </div>

          {/* Step 3: Review Items and Delivery */}
          <div className="chk-step-card">
            <div className="chk-step-num">3</div>
            <div className="chk-step-content">
              <h3 className="chk-step-title">Review Items & Delivery Speed</h3>
              <div className="chk-delivery-speed-box">
                <span className="chk-prime-tag">Prime</span>
                <strong>FREE Guaranteed Enterprise Priority Delivery</strong>
                <p>Delivery Tomorrow with SLA carrier telemetry</p>
              </div>

              <div className="chk-items-review">
                {activeItems.map((item) => {
                  const itemId = item.id || (item as { listingId: string }).listingId;
                  const itemSubtotal = item.subtotal ?? item.unitPrice * item.quantity;
                  return (
                    <div key={itemId} className="chk-item-review-row">
                      <div className="chk-item-thumb">
                        <img
                          src={
                            (item as { mainImageUrl?: string }).mainImageUrl ||
                            'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?w=150&auto=format&fit=crop&q=60'
                          }
                          alt={item.productTitle}
                        />
                      </div>
                      <div className="chk-item-desc">
                        <h4>{item.productTitle}</h4>
                        <div className="chk-item-sub">
                          Qty: {item.quantity} • ${item.unitPrice.toFixed(2)} each
                        </div>
                      </div>
                      <div className="chk-item-price">${itemSubtotal.toFixed(2)}</div>
                    </div>
                  );
                })}
              </div>
            </div>
          </div>
        </div>

        {/* Order Summary Sidebar */}
        <div className="chk-summary-col">
          <div className="chk-summary-card">
            <button
              type="button"
              onClick={handlePlaceOrder}
              disabled={loading}
              className="chk-place-order-btn"
            >
              {loading ? 'Processing Order...' : 'Place your order'}
            </button>
            <p className="chk-terms-note">
              By placing your order, you agree to Amazon Enterprise Terms of Sale.
            </p>

            <hr className="chk-summary-divider" />

            <h3 className="chk-order-summary-title">Order Summary</h3>
            <div className="chk-summary-row">
              <span>Items ({activeItems.length}):</span>
              <span>${cartSubtotal.toFixed(2)}</span>
            </div>
            <div className="chk-summary-row">
              <span>Shipping & handling:</span>
              <span style={{ color: '#067d62' }}>$0.00 (FREE)</span>
            </div>
            <div className="chk-summary-row">
              <span>Estimated tax:</span>
              <span>$0.00</span>
            </div>

            <hr className="chk-summary-divider" />

            <div className="chk-summary-total-row">
              <span>Order Total:</span>
              <span className="chk-total-price">${cartSubtotal.toFixed(2)}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
