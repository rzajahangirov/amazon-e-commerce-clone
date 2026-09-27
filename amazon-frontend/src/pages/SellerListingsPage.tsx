import { useState } from 'react';
import { useSellerListings } from '../hooks/useSellerListings';
import type { ProductListing } from '../api/sellerTypes';
import { formatCount, formatCurrency } from '../utils/format';
import '../components/seller/SellerCommon.css';

export function SellerListingsPage() {
  const { listings, pagination, loading, page, setPage, refetch, updateStock } =
    useSellerListings(10);

  const [searchKey, setSearchKey] = useState('');
  const [editingListing, setEditingListing] = useState<ProductListing | null>(null);
  const [modalPrice, setModalPrice] = useState<number>(0);
  const [modalMinPrice, setModalMinPrice] = useState<number>(0);
  const [modalStock, setModalStock] = useState<number>(0);
  const [saving, setSaving] = useState(false);
  const [showAttachModal, setShowAttachModal] = useState(false);

  const openEditModal = (listing: ProductListing) => {
    setEditingListing(listing);
    setModalPrice(Number(listing.price));
    setModalMinPrice(listing.minPriceFloor ? Number(listing.minPriceFloor) : 0);
    setModalStock(listing.stockQuantity);
  };

  const handleSaveModal = async () => {
    if (!editingListing) return;
    setSaving(true);
    await updateStock(editingListing.id, {
      price: modalPrice,
      minPriceFloor: modalMinPrice > 0 ? modalMinPrice : undefined,
      stockQuantity: modalStock,
    });
    setSaving(false);
    setEditingListing(null);
  };

  const filteredListings = listings.filter(
    (l) =>
      l.variantAsin?.toLowerCase().includes(searchKey.toLowerCase()) ||
      l.variantName?.toLowerCase().includes(searchKey.toLowerCase()) ||
      l.sellerSku?.toLowerCase().includes(searchKey.toLowerCase()),
  );

  return (
    <div>
      {/* Page Header */}
      <div className="seller-page-header">
        <div>
          <div style={{ fontSize: '0.72rem', fontWeight: 700, letterSpacing: '0.06em', color: '#2563eb', textTransform: 'uppercase', marginBottom: '0.2rem' }}>
            CATALOG / OFFERS &amp; INVENTORY • LIVE SYNC
          </div>
          <h1>Catalog Offers &amp; Inventory Management</h1>
          <p>
            Manage pricing, stock levels, min price floors, fulfillment channels, and Buy Box competitiveness.
          </p>
        </div>
        <div className="seller-header-actions">
          <button
            type="button"
            className="seller-btn seller-btn-primary"
            onClick={() => setShowAttachModal(true)}
          >
            ➕ Attach Offer to Catalog ASIN
          </button>
          <button type="button" className="seller-btn seller-btn-outline">
            📤 Bulk Upload Flat File
          </button>
          <button type="button" className="seller-btn seller-btn-outline" onClick={() => void refetch()}>
            🔄 Refresh
          </button>
        </div>
      </div>

      {/* 4 KPI Metrics matching Screenshot 2 */}
      <div className="seller-kpi-row" style={{ gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))' }}>
        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Total Active SKUs</span>
            <span className="seller-badge-green">+12 this wk</span>
          </div>
          <div className="seller-kpi-value">{pagination?.totalElements ?? 1482}</div>
          <div className="seller-kpi-subtext">Active offerings in catalog</div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Buy Box Dominance</span>
            <span className="seller-badge-green">🏆 94.3%</span>
          </div>
          <div className="seller-kpi-value">94.3%</div>
          <div className="seller-kpi-subtext">+1.8% vs last week</div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">FBA Network Units</span>
            <span className="seller-badge-blue">98.1% Capacity</span>
          </div>
          <div className="seller-kpi-value">18,940</div>
          <div className="seller-kpi-subtext">Multi-node warehouse allocation</div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Stock Alerts</span>
            <span className="seller-badge-red">Action req.</span>
          </div>
          <div className="seller-kpi-value" style={{ color: '#ef4444' }}>5 SKUs</div>
          <div className="seller-kpi-subtext">Critical depletion threshold</div>
        </div>
      </div>

      {/* Filter Bar */}
      <div className="seller-card" style={{ padding: '0.85rem 1.25rem', marginBottom: '1.25rem' }}>
        <div style={{ display: 'flex', gap: '0.75rem', alignItems: 'center', flexWrap: 'wrap' }}>
          <input
            type="text"
            className="seller-input"
            style={{ width: '280px' }}
            placeholder="Search ASIN, SKU, or Title..."
            value={searchKey}
            onChange={(e) => setSearchKey(e.target.value)}
          />
          <select className="seller-select">
            <option>Status: Active Only</option>
            <option>All Statuses</option>
          </select>
          <select className="seller-select">
            <option>Fulfillment: All Channels</option>
            <option>FBA - Amazon</option>
            <option>FBM - Merchant</option>
          </select>
          <select className="seller-select">
            <option>Buy Box: All States</option>
            <option>Winning Only</option>
            <option>Suppressed</option>
          </select>
          {searchKey && (
            <button
              type="button"
              className="seller-btn seller-btn-outline"
              style={{ padding: '0.35rem 0.6rem', fontSize: '0.75rem' }}
              onClick={() => setSearchKey('')}
            >
              Clear
            </button>
          )}
        </div>
      </div>

      {/* Listings Table */}
      <div className="seller-card">
        <div className="seller-table-wrap">
          <table className="seller-table">
            <thead>
              <tr>
                <th style={{ width: '30px' }}>
                  <input type="checkbox" />
                </th>
                <th>ASIN &amp; SKU</th>
                <th>Product Title / Variant</th>
                <th style={{ textAlign: 'right' }}>Offer Price</th>
                <th style={{ textAlign: 'right' }}>Min Price Floor</th>
                <th style={{ textAlign: 'center' }}>Stock Qty</th>
                <th style={{ textAlign: 'center' }}>Channel</th>
                <th>Buy Box Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {loading && listings.length === 0 ? (
                <tr>
                  <td colSpan={9} style={{ textAlign: 'center', padding: '2.5rem', color: '#64748b' }}>
                    Loading catalog listings...
                  </td>
                </tr>
              ) : filteredListings.length === 0 ? (
                <tr>
                  <td colSpan={9} style={{ textAlign: 'center', padding: '2.5rem', color: '#64748b' }}>
                    No listings found.
                  </td>
                </tr>
              ) : (
                filteredListings.map((listing) => (
                  <tr key={listing.id}>
                    <td>
                      <input type="checkbox" />
                    </td>
                    <td>
                      <div style={{ display: 'flex', flexDirection: 'column', gap: '0.2rem' }}>
                        <span className="asin-tag">{listing.variantAsin || 'B08N5WRWNW'}</span>
                        <span style={{ fontSize: '0.7rem', color: '#64748b', fontFamily: 'monospace' }}>
                          SKU: {listing.sellerSku}
                        </span>
                      </div>
                    </td>
                    <td style={{ maxWidth: '280px' }}>
                      <strong style={{ display: 'block', fontSize: '0.85rem' }}>
                        {listing.variantName || 'Apple Watch Series 9 GPS 45mm Midnight'}
                      </strong>
                      <span style={{ fontSize: '0.72rem', color: '#64748b' }}>
                        ID: {listing.id.slice(0, 8)} • Seller: {listing.sellerName}
                      </span>
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 700, fontSize: '0.9rem' }}>
                      {formatCurrency(Number(listing.price))}
                    </td>
                    <td style={{ textAlign: 'right', color: '#059669', fontWeight: 600 }}>
                      {listing.minPriceFloor
                        ? formatCurrency(Number(listing.minPriceFloor))
                        : '—'}
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span
                        style={{
                          display: 'inline-block',
                          padding: '0.2rem 0.5rem',
                          borderRadius: '4px',
                          fontWeight: 700,
                          fontSize: '0.78rem',
                          background: listing.stockQuantity < 10 ? '#fef2f2' : '#f8fafc',
                          color: listing.stockQuantity < 10 ? '#dc2626' : '#0f172a',
                        }}
                      >
                        {formatCount(listing.stockQuantity)}
                      </span>
                    </td>
                    <td style={{ textAlign: 'center' }}>
                      <span className={listing.fulfillmentType === 'FBA' ? 'seller-badge-blue' : 'seller-badge-amber'}>
                        {listing.fulfillmentType || 'FBA'}
                      </span>
                    </td>
                    <td>
                      {listing.isBuyboxWinner ? (
                        <span className="seller-badge-green" style={{ display: 'inline-flex', alignItems: 'center', gap: '0.25rem' }}>
                          ⭐ Buy Box Winner
                        </span>
                      ) : (
                        <span className="seller-badge-amber">Competitive Offer</span>
                      )}
                    </td>
                    <td style={{ textAlign: 'right' }}>
                      <button
                        type="button"
                        className="seller-btn seller-btn-outline"
                        style={{ padding: '0.25rem 0.55rem', fontSize: '0.75rem' }}
                        onClick={() => openEditModal(listing)}
                      >
                        ✏️ Edit Stock / Floor
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination Bar */}
        {pagination && pagination.totalPages > 1 && (
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid #f1f5f9' }}>
            <span style={{ fontSize: '0.8rem', color: '#64748b' }}>
              Page {page + 1} of {pagination.totalPages} ({pagination.totalElements} items total)
            </span>
            <div style={{ display: 'flex', gap: '0.35rem' }}>
              <button
                type="button"
                className="seller-btn seller-btn-outline"
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
              >
                Previous
              </button>
              <button
                type="button"
                className="seller-btn seller-btn-outline"
                disabled={pagination.last}
                onClick={() => setPage(page + 1)}
              >
                Next
              </button>
            </div>
          </div>
        )}
      </div>

      {/* Edit Stock / Price / MinPriceFloor Modal */}
      {editingListing && (
        <div className="seller-modal-backdrop" onClick={() => setEditingListing(null)}>
          <div className="seller-modal" onClick={(e) => e.stopPropagation()}>
            <div className="seller-modal-header">
              <h3 style={{ margin: 0 }}>Adjust Offer Pricing &amp; Inventory</h3>
              <button
                type="button"
                style={{ background: 'transparent', border: 'none', fontSize: '1.2rem', cursor: 'pointer' }}
                onClick={() => setEditingListing(null)}
              >
                ✕
              </button>
            </div>

            <div className="seller-modal-body">
              <div style={{ marginBottom: '1rem', background: '#f8fafc', padding: '0.75rem', borderRadius: '8px' }}>
                <strong style={{ fontSize: '0.85rem' }}>{editingListing.variantName}</strong>
                <div style={{ fontSize: '0.75rem', color: '#64748b', marginTop: '0.2rem' }}>
                  ASIN: {editingListing.variantAsin} • SKU: {editingListing.sellerSku}
                </div>
              </div>

              <div className="seller-form-group">
                <label>Listing Price ($)</label>
                <input
                  type="number"
                  step="0.01"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={modalPrice}
                  onChange={(e) => setModalPrice(parseFloat(e.target.value) || 0)}
                />
                <div className="seller-form-help">Current customer-facing buy price on Amazon.</div>
              </div>

              <div className="seller-form-group">
                <label>Minimum Price Floor ($)</label>
                <input
                  type="number"
                  step="0.01"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={modalMinPrice}
                  onChange={(e) => setModalMinPrice(parseFloat(e.target.value) || 0)}
                />
                <div className="seller-form-help">
                  Algorithmic lower boundary to protect profit margins against competitor repricing engines.
                </div>
              </div>

              <div className="seller-form-group">
                <label>Stock Quantity (Units)</label>
                <input
                  type="number"
                  className="seller-input"
                  style={{ width: '100%' }}
                  value={modalStock}
                  onChange={(e) => setModalStock(parseInt(e.target.value, 10) || 0)}
                />
                <div className="seller-form-help">Available sellable inventory in the fulfillment warehouse.</div>
              </div>
            </div>

            <div className="seller-modal-footer">
              <button
                type="button"
                className="seller-btn seller-btn-outline"
                onClick={() => setEditingListing(null)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="seller-btn seller-btn-primary"
                disabled={saving}
                onClick={() => void handleSaveModal()}
              >
                {saving ? 'Saving Changes...' : 'Save & Publish Offer'}
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Attach Offer Modal (Screenshot 2 Modal Preview) */}
      {showAttachModal && (
        <div className="seller-modal-backdrop" onClick={() => setShowAttachModal(false)}>
          <div className="seller-modal" onClick={(e) => e.stopPropagation()}>
            <div className="seller-modal-header">
              <h3 style={{ margin: 0 }}>Attach New Offer to Catalog ASIN</h3>
              <button
                type="button"
                style={{ background: 'transparent', border: 'none', fontSize: '1.2rem', cursor: 'pointer' }}
                onClick={() => setShowAttachModal(false)}
              >
                ✕
              </button>
            </div>

            <div className="seller-modal-body">
              <div className="seller-form-group">
                <label>Marketplace ASIN Identifier *</label>
                <input
                  type="text"
                  className="seller-input"
                  style={{ width: '100%' }}
                  defaultValue="B09G96TFF7"
                  placeholder="e.g. B09G96TFF7"
                />
              </div>

              <div style={{ background: '#ecfdf5', border: '1px solid #a7f3d0', padding: '0.75rem', borderRadius: '8px', marginBottom: '1rem', display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                <span style={{ fontSize: '1.2rem' }}>⌚</span>
                <div>
                  <strong style={{ fontSize: '0.82rem', color: '#065f46' }}>Apple Watch Series 9 GPS 45mm Midnight Aluminum</strong>
                  <div style={{ fontSize: '0.7rem', color: '#047857' }}>Buy Box: $399.00 • 14 Competitors • Brand: Apple Inc.</div>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem' }}>
                <div className="seller-form-group">
                  <label>Your Custom SKU *</label>
                  <input type="text" className="seller-input" style={{ width: '100%' }} defaultValue="APL-WCH-S9-45-MID" />
                </div>
                <div className="seller-form-group">
                  <label>Listing Condition *</label>
                  <select className="seller-select" style={{ width: '100%' }}>
                    <option>New (Factory Sealed in Box)</option>
                    <option>Refurbished - Like New</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '0.75rem' }}>
                <div className="seller-form-group">
                  <label>Your Price ($) *</label>
                  <input type="number" step="0.01" className="seller-input" style={{ width: '100%' }} defaultValue="395.00" />
                </div>
                <div className="seller-form-group">
                  <label>Quantity *</label>
                  <input type="number" className="seller-input" style={{ width: '100%' }} defaultValue="50" />
                </div>
                <div className="seller-form-group">
                  <label>Min Floor ($) *</label>
                  <input type="number" step="0.01" className="seller-input" style={{ width: '100%' }} defaultValue="375.00" />
                </div>
              </div>

              <div className="seller-form-group">
                <label>Fulfillment Channel Specification</label>
                <div style={{ display: 'flex', gap: '1rem', marginTop: '0.35rem' }}>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', textTransform: 'none', fontWeight: 600 }}>
                    <input type="radio" name="modalFulfillment" defaultChecked /> FBA - Fulfilled by Amazon
                  </label>
                  <label style={{ display: 'flex', alignItems: 'center', gap: '0.4rem', textTransform: 'none', fontWeight: 600 }}>
                    <input type="radio" name="modalFulfillment" /> FBM - Merchant Fulfilled
                  </label>
                </div>
              </div>
            </div>

            <div className="seller-modal-footer">
              <button
                type="button"
                className="seller-btn seller-btn-outline"
                onClick={() => setShowAttachModal(false)}
              >
                Cancel
              </button>
              <button
                type="button"
                className="seller-btn seller-btn-primary"
                onClick={() => setShowAttachModal(false)}
              >
                🚀 Publish Offer to ASIN
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
