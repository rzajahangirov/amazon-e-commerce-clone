import { useState } from 'react';
import { useSellerOrders } from '../hooks/useSellerOrders';
import type { OrderItemStatus } from '../api/sellerTypes';
import { formatCurrency } from '../utils/format';
import { formatTimeAgo } from '../utils/timeAgo';
import '../components/seller/SellerCommon.css';

export function SellerOrdersPage() {
  const [selectedStatus, setSelectedStatus] = useState<OrderItemStatus | undefined>(undefined);
  const [searchKey, setSearchKey] = useState('');
  const [selectedItems, setSelectedItems] = useState<string[]>([]);
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  const { orders, loading, refetch, transitionStatus } = useSellerOrders({
    status: selectedStatus,
    searchKey: searchKey || undefined,
  });

  const toggleSelectAll = () => {
    if (selectedItems.length === orders.length) {
      setSelectedItems([]);
    } else {
      setSelectedItems(orders.map((o) => o.orderItemId));
    }
  };

  const toggleSelectItem = (id: string) => {
    if (selectedItems.includes(id)) {
      setSelectedItems(selectedItems.filter((i) => i !== id));
    } else {
      setSelectedItems([...selectedItems, id]);
    }
  };

  const handleStatusChange = async (orderItemId: string, newStatus: OrderItemStatus) => {
    setUpdatingId(orderItemId);
    await transitionStatus(orderItemId, newStatus);
    setUpdatingId(null);
  };

  // Status counts
  const pendingCount = orders.filter((o) => o.itemStatus === 'PENDING').length;
  const shippedCount = orders.filter((o) => o.itemStatus === 'SHIPPED').length;
  const deliveredCount = orders.filter((o) => o.itemStatus === 'DELIVERED').length;
  const cancelledCount = orders.filter((o) => o.itemStatus === 'CANCELLED').length;

  return (
    <div>
      {/* Page Header */}
      <div className="seller-page-header">
        <div>
          <div style={{ fontSize: '0.72rem', fontWeight: 700, letterSpacing: '0.06em', color: '#2563eb', textTransform: 'uppercase', marginBottom: '0.2rem' }}>
            LOGISTICS STAGING ENGINE • v4.20 Live Queue
          </div>
          <h1>Order Fulfillment &amp; Multi-Vendor Line Items</h1>
        </div>
        <div className="seller-header-actions">
          <button
            type="button"
            className="seller-btn seller-btn-primary"
            disabled={selectedItems.length === 0}
            onClick={async () => {
              for (const id of selectedItems) {
                await transitionStatus(id, 'SHIPPED');
              }
              setSelectedItems([]);
            }}
          >
            📦 Bulk Mark as Shipped ({selectedItems.length})
          </button>
          <button type="button" className="seller-btn seller-btn-outline">
            📄 Download Packing Slips
          </button>
          <button type="button" className="seller-btn seller-btn-outline" onClick={() => void refetch()}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* Status Filter Tabs */}
      <div className="seller-tabs">
        <button
          type="button"
          className={`seller-tab-btn ${selectedStatus === undefined ? 'active' : ''}`}
          onClick={() => setSelectedStatus(undefined)}
        >
          All Items ({orders.length})
        </button>
        <button
          type="button"
          className={`seller-tab-btn ${selectedStatus === 'PENDING' ? 'active' : ''}`}
          onClick={() => setSelectedStatus('PENDING')}
        >
          ⏳ Pending Dispatch ({pendingCount})
        </button>
        <button
          type="button"
          className={`seller-tab-btn ${selectedStatus === 'SHIPPED' ? 'active' : ''}`}
          onClick={() => setSelectedStatus('SHIPPED')}
        >
          🚚 Shipped / In-Transit ({shippedCount})
        </button>
        <button
          type="button"
          className={`seller-tab-btn ${selectedStatus === 'DELIVERED' ? 'active' : ''}`}
          onClick={() => setSelectedStatus('DELIVERED')}
        >
          ✓ Delivered ({deliveredCount})
        </button>
        <button
          type="button"
          className={`seller-tab-btn ${selectedStatus === 'CANCELLED' ? 'active' : ''}`}
          onClick={() => setSelectedStatus('CANCELLED')}
        >
          ✕ Cancelled ({cancelledCount})
        </button>
      </div>

      {/* SLA Alert Banner */}
      <div
        style={{
          background: '#fffbeb',
          border: '1px solid #fde68a',
          borderRadius: '8px',
          padding: '0.75rem 1rem',
          display: 'flex',
          alignItems: 'center',
          gap: '0.6rem',
          marginBottom: '1.25rem',
          fontSize: '0.85rem',
          color: '#92400e',
        }}
      >
        <span style={{ fontSize: '1.1rem' }}>⏰</span>
        <strong>SLA Urgent Queue:</strong> 7 Shipments &lt; 2h SLA remaining. Priority dispatch requested by Amazon Fulfillment Network.
      </div>

      {/* Search & Filter Bar */}
      <div className="seller-card" style={{ padding: '0.85rem 1.25rem', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', gap: '1rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <div style={{ flex: 1, minWidth: '260px' }}>
            <input
              type="text"
              className="seller-input"
              style={{ width: '100%' }}
              placeholder="Filter by Order ID, Buyer Name, Tracking Number, or ASIN..."
              value={searchKey}
              onChange={(e) => setSearchKey(e.target.value)}
            />
          </div>
          <div style={{ fontSize: '0.75rem', color: '#10b981', display: 'flex', alignItems: 'center', gap: '0.35rem' }}>
            <span className="seller-status-dot" /> Auto-sync: Active (15s)
          </div>
        </div>
      </div>

      {/* Orders Table */}
      <div className="seller-card">
        <div className="seller-table-wrap">
          <table className="seller-table">
            <thead>
              <tr>
                <th style={{ width: '30px' }}>
                  <input
                    type="checkbox"
                    checked={orders.length > 0 && selectedItems.length === orders.length}
                    onChange={toggleSelectAll}
                  />
                </th>
                <th>Order Item &amp; ID</th>
                <th>Product Title</th>
                <th>ASIN &amp; SKU</th>
                <th>Buyer &amp; Destination</th>
                <th>Order Date &amp; SLA</th>
                <th>Status &amp; Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading && orders.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '2.5rem', color: '#64748b' }}>
                    Loading order line items...
                  </td>
                </tr>
              ) : orders.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '2.5rem', color: '#64748b' }}>
                    No order fulfillment records found matching the query.
                  </td>
                </tr>
              ) : (
                orders.map((item) => (
                  <tr key={item.orderItemId}>
                    <td>
                      <input
                        type="checkbox"
                        checked={selectedItems.includes(item.orderItemId)}
                        onChange={() => toggleSelectItem(item.orderItemId)}
                      />
                    </td>
                    <td>
                      <div style={{ fontFamily: 'monospace', fontWeight: 700, fontSize: '0.75rem', color: '#0f172a' }}>
                        ITM-{item.orderItemId.slice(0, 8)}
                      </div>
                      <small style={{ color: '#64748b', fontSize: '0.7rem' }}>
                        ORD-{item.orderId.slice(0, 8)}
                      </small>
                    </td>
                    <td style={{ maxWidth: '240px' }}>
                      <strong style={{ display: 'block', fontSize: '0.85rem' }}>{item.productTitle}</strong>
                      <span style={{ fontSize: '0.72rem', color: '#64748b' }}>
                        {item.variantName ? `Variant: ${item.variantName} • ` : ''}Qty: {item.quantity} • {formatCurrency(Number(item.subtotal))}
                      </span>
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                        <span className="asin-tag">{item.asin || 'B09XS7JWHH'}</span>
                        <span style={{ fontSize: '0.7rem', color: '#64748b', fontFamily: 'monospace' }}>
                          SKU: {item.sellerSku}
                        </span>
                      </div>
                    </td>
                    <td>
                      <strong>{item.buyerName || 'Verified Buyer'}</strong>
                      <div style={{ fontSize: '0.72rem', color: '#64748b' }}>
                        {item.buyerDestination || 'Seattle, WA • 98101'}
                      </div>
                    </td>
                    <td>
                      <div style={{ fontSize: '0.78rem' }}>{formatTimeAgo(item.orderDate)}</div>
                      {item.shipByDeadline ? (
                        <div style={{ fontSize: '0.72rem', color: '#d97706', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '0.2rem' }}>
                          ⏰ Ship by: {new Date(item.shipByDeadline).toLocaleDateString()}
                        </div>
                      ) : (
                        <div style={{ fontSize: '0.72rem', color: '#10b981' }}>Within SLA Window</div>
                      )}
                    </td>
                    <td>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <span
                          className={
                            item.itemStatus === 'PENDING'
                              ? 'status-pill-pending'
                              : item.itemStatus === 'SHIPPED'
                              ? 'status-pill-shipped'
                              : item.itemStatus === 'DELIVERED'
                              ? 'status-pill-delivered'
                              : 'status-pill-cancelled'
                          }
                        >
                          {item.itemStatus}
                        </span>

                        {/* Status Transition buttons */}
                        {item.itemStatus === 'PENDING' && (
                          <button
                            type="button"
                            className="seller-btn seller-btn-primary"
                            style={{ padding: '0.2rem 0.5rem', fontSize: '0.72rem' }}
                            disabled={updatingId === item.orderItemId}
                            onClick={() => void handleStatusChange(item.orderItemId, 'SHIPPED')}
                          >
                            Mark Shipped
                          </button>
                        )}
                        {item.itemStatus === 'SHIPPED' && (
                          <button
                            type="button"
                            className="seller-btn seller-btn-outline"
                            style={{ padding: '0.2rem 0.5rem', fontSize: '0.72rem' }}
                            disabled={updatingId === item.orderItemId}
                            onClick={() => void handleStatusChange(item.orderItemId, 'DELIVERED')}
                          >
                            Mark Delivered
                          </button>
                        )}
                        {item.itemStatus === 'PENDING' && (
                          <button
                            type="button"
                            className="seller-btn seller-btn-outline"
                            style={{ padding: '0.2rem 0.4rem', fontSize: '0.7rem', color: '#ef4444' }}
                            disabled={updatingId === item.orderItemId}
                            onClick={() => void handleStatusChange(item.orderItemId, 'CANCELLED')}
                          >
                            Cancel
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Bottom KPI summary bar */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '1rem', marginTop: '1.25rem' }}>
        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Carrier Handoff Rate</span>
            <span className="seller-badge-green">98.4%</span>
          </div>
          <div className="seller-kpi-value">98.4%</div>
          <div className="seller-kpi-subtext">+1.2% SLA Compliance</div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Pending Dispatch Value</span>
            <span className="seller-badge-amber">42 Packages</span>
          </div>
          <div className="seller-kpi-value">$14,892.40</div>
          <div className="seller-kpi-subtext">Immediate fulfillment queue</div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Active Delivery Partners</span>
          </div>
          <div style={{ display: 'flex', gap: '0.4rem', marginTop: '0.4rem' }}>
            <span className="seller-badge-blue">UPS 58%</span>
            <span className="seller-badge-blue">FedEx 27%</span>
            <span className="seller-badge-blue">USPS 15%</span>
          </div>
        </div>
      </div>
    </div>
  );
}
