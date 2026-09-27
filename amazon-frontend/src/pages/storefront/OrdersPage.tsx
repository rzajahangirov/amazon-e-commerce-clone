import React, { useEffect, useState, useCallback } from 'react';
import { Link } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { storefrontApi } from '../../api/storefrontApi';
import type { Order } from '../../api/storefrontTypes';
import './OrdersPage.css';

export const OrdersPage: React.FC = () => {
  const { isAuthenticated, openAuthModal } = useCustomerAuth();
  const [orders, setOrders] = useState<Order[]>([]);
  const [loading, setLoading] = useState(true);
  const [cancellingId, setCancellingId] = useState<string | null>(null);

  const fetchOrders = useCallback(async () => {
    if (!isAuthenticated) return;
    try {
      setLoading(true);
      const res = await storefrontApi.getMyOrders(0, 20);
      setOrders(res.content || []);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleCancelOrder = async (orderId: string) => {
    if (!window.confirm('Are you sure you want to cancel this enterprise order? Restocked inventory will be returned.')) {
      return;
    }
    try {
      setCancellingId(orderId);
      await storefrontApi.cancelOrder(orderId);
      await fetchOrders();
    } catch (err: unknown) {
      alert(err instanceof Error ? err.message : 'Failed to cancel order');
    } finally {
      setCancellingId(null);
    }
  };

  if (!isAuthenticated) {
    return (
      <div className="orders-unauth-container">
        <h2>Please Sign In</h2>
        <p>You must be signed in to view your orders and procurement history.</p>
        <button
          type="button"
          onClick={() => openAuthModal('signin')}
          className="orders-signin-btn"
        >
          Sign In
        </button>
      </div>
    );
  }

  return (
    <div className="orders-container">
      <div className="orders-header">
        <h1 className="orders-title">Your Orders</h1>
        <div className="orders-subtitle">Procurement and dispatch history for your enterprise account</div>
      </div>

      {loading ? (
        <div className="orders-loading">Loading order records...</div>
      ) : orders.length === 0 ? (
        <div className="orders-empty-card">
          <h3>No orders placed yet</h3>
          <p>Looking for enterprise hardware or QD-OLED monitors?</p>
          <Link to="/products" className="orders-shop-btn">
            Explore Catalog
          </Link>
        </div>
      ) : (
        <div className="orders-list">
          {orders.map((order) => {
            const isCancellable =
              order.status === 'PENDING' || order.status === 'CONFIRMED' || order.status === 'PROCESSING';

            return (
              <div key={order.id} className="order-card">
                {/* Header Bar */}
                <div className="order-card-header">
                  <div className="order-meta-col">
                    <span className="order-meta-label">ORDER PLACED</span>
                    <span className="order-meta-val">
                      {order.placedAt ? new Date(order.placedAt).toLocaleDateString() : 'Recent'}
                    </span>
                  </div>

                  <div className="order-meta-col">
                    <span className="order-meta-label">TOTAL</span>
                    <span className="order-meta-val">${order.totalAmount?.toFixed(2)}</span>
                  </div>

                  <div className="order-meta-col">
                    <span className="order-meta-label">SHIP TO</span>
                    <span className="order-meta-val">Enterprise Logistics</span>
                  </div>

                  <div className="order-meta-col right">
                    <span className="order-meta-label">ORDER # {order.orderNumber}</span>
                    <span className={`order-status-badge status-${order.status?.toLowerCase()}`}>
                      {order.status}
                    </span>
                  </div>
                </div>

                {/* Items Body */}
                <div className="order-card-body">
                  <div className="order-items-column">
                    {(order.items || []).map((item) => (
                      <div key={item.id} className="order-item-row">
                        <div className="order-item-desc">
                          <h4>{item.productTitle}</h4>
                          {item.variantName && (
                            <div className="order-item-variant">Variant: {item.variantName}</div>
                          )}
                          {item.asin && (
                            <div className="order-item-asin">ASIN: {item.asin}</div>
                          )}
                          <div className="order-item-qty">
                            Quantity: {item.quantity} • ${item.unitPrice.toFixed(2)} each
                          </div>
                        </div>

                        <div className="order-item-subtotal">
                          ${(item.subtotal ?? item.unitPrice * item.quantity).toFixed(2)}
                        </div>
                      </div>
                    ))}
                  </div>

                  {/* Actions column */}
                  <div className="order-card-actions">
                    <Link to="/products" className="order-action-btn primary">
                      Buy it again
                    </Link>
                    {isCancellable && (
                      <button
                        type="button"
                        onClick={() => handleCancelOrder(order.id)}
                        disabled={cancellingId === order.id}
                        className="order-action-btn cancel"
                      >
                        {cancellingId === order.id ? 'Cancelling...' : 'Cancel Order'}
                      </button>
                    )}
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
