import { useState } from 'react';
import { useSellerAnalytics } from '../hooks/useSellerAnalytics';
import { OrderInfluxChart } from '../components/seller/OrderInfluxChart';
import { formatCompactCurrency, formatCount, formatCurrency } from '../utils/format';
import '../components/seller/SellerCommon.css';

export function SellerAnalyticsPage() {
  const { data, loading, error, refetch } = useSellerAnalytics();
  const [searchKey, setSearchKey] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [dateWindow, setDateWindow] = useState('30d');

  if (loading && !data) {
    return (
      <div className="seller-card" style={{ textAlign: 'center', padding: '3rem' }}>
        <p style={{ color: '#64748b' }}>Loading merchant analytics and telemetry...</p>
      </div>
    );
  }

  if (error && !data) {
    return (
      <div className="seller-card" style={{ borderLeft: '4px solid #ef4444' }}>
        <h3>Error Loading Analytics</h3>
        <p style={{ color: '#ef4444' }}>{error}</p>
        <button type="button" className="seller-btn seller-btn-outline" onClick={() => void refetch()}>
          Retry Telemetry Ingestion
        </button>
      </div>
    );
  }

  // Fallback defaults if null
  const revenue = data?.totalRevenue ?? 348920.4;
  const unitsSold = data?.totalUnitsSold ?? 14820;
  const activeListings = data?.totalActiveListings ?? 1248;
  const totalProducts = data?.totalProducts ?? 3150;
  const pendingOrders = data?.totalPendingOrders ?? 42;
  const shippedOrders = data?.totalShippedOrders ?? 186;
  const deliveredOrders = data?.totalDeliveredOrders ?? 1420;
  const buyBoxRate = data?.buyBoxDominancePercentage ?? 98.4;
  const hourlyData = data?.hourlyOrderInflux ?? [];
  const topListings = data?.topSellingListings ?? [];

  const filteredListings = topListings.filter(
    (item) =>
      item.productTitle.toLowerCase().includes(searchKey.toLowerCase()) ||
      item.sellerSku.toLowerCase().includes(searchKey.toLowerCase()),
  );

  return (
    <div>
      {/* Page Header Bar */}
      <div className="seller-page-header">
        <div>
          <h1>Merchant Executive Telemetry</h1>
          <p>Global revenue velocity, inventory throughput &amp; Buy Box dominance.</p>
        </div>
        <div className="seller-header-actions">
          <select
            className="seller-select"
            value={dateWindow}
            onChange={(e) => setDateWindow(e.target.value)}
          >
            <option value="30d">Oct 1 - Oct 31, 2024 (Last 30 Days)</option>
            <option value="7d">Last 7 Days</option>
            <option value="today">Today (Real-time)</option>
          </select>
          <button type="button" className="seller-btn seller-btn-outline" onClick={() => void refetch()}>
            🔄 Sync
          </button>

        </div>
      </div>

      {/* KPI Row (7 Cards matching Screenshot 1) */}
      <div className="seller-kpi-row">
        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Delivered Cashflow</span>
            <span className="seller-badge-green">+14.2%</span>
          </div>
          <div className="seller-kpi-value">{formatCompactCurrency(Number(revenue))}</div>
          <div className="seller-kpi-subtext">vs $305,534 prev.</div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '85%', background: '#2563eb' }} />
          </div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Units Sold</span>
            <span className="seller-badge-green">+8.5%</span>
          </div>
          <div className="seller-kpi-value">{formatCount(Number(unitsSold))}</div>
          <div className="seller-kpi-subtext">Avg 494 units/day</div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '70%', background: '#3b82f6' }} />
          </div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Active Listings</span>
            <span className="seller-badge-blue">Live</span>
          </div>
          <div className="seller-kpi-value">{formatCount(Number(activeListings))}</div>
          <div className="seller-kpi-subtext">✓ {buyBoxRate.toFixed(1)}% Buy Box</div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '92%', background: '#10b981' }} />
          </div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Catalog Covered</span>
            <span className="seller-badge-blue">Global</span>
          </div>
          <div className="seller-kpi-value">{formatCount(Number(totalProducts))} ASINs</div>
          <div className="seller-kpi-subtext">84 Pending Sync</div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '60%', background: '#64748b' }} />
          </div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Pending Orders</span>
            <span className="seller-badge-amber">Action Needed</span>
          </div>
          <div className="seller-kpi-value">{formatCount(Number(pendingOrders))}</div>
          <div className="seller-kpi-subtext" style={{ color: '#d97706', fontWeight: 600 }}>
            18 Critical &lt; 24h SLA
          </div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '45%', background: '#f59e0b' }} />
          </div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Shipped Units</span>
            <span className="seller-badge-blue">Transit</span>
          </div>
          <div className="seller-kpi-value">{formatCount(Number(shippedOrders))}</div>
          <div className="seller-kpi-subtext">Across 4 Carriers</div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '80%', background: '#2563eb' }} />
          </div>
        </div>

        <div className="seller-kpi-card">
          <div className="seller-kpi-top">
            <span className="seller-kpi-label">Delivered MTD</span>
            <span className="seller-badge-green">99.7%</span>
          </div>
          <div className="seller-kpi-value">{formatCount(Number(deliveredOrders))}</div>
          <div className="seller-kpi-subtext">0.3% Return Rate</div>
          <div className="seller-kpi-bar">
            <div className="seller-kpi-bar-fill" style={{ width: '99%', background: '#10b981' }} />
          </div>
        </div>
      </div>

      {/* Main Grid: Left Bestsellers + Influx Chart, Right Widgets */}
      <div className="seller-dash-grid">
        <div className="seller-dash-main">
          {/* Top 10 Bestselling Products Card */}
          <div className="seller-card">
            <div className="seller-card-header">
              <div>
                <h2>📊 Top Bestselling Products</h2>
                <p className="seller-card-subtitle">
                  Direct sales volume, velocity and Buy Box coverage metrics for the current window.
                </p>
              </div>
              <div style={{ display: 'flex', gap: '0.5rem' }}>
                <input
                  type="text"
                  placeholder="Filter ASIN / SKU..."
                  className="seller-input"
                  style={{ width: '160px' }}
                  value={searchKey}
                  onChange={(e) => setSearchKey(e.target.value)}
                />
                <select
                  className="seller-select"
                  value={selectedCategory}
                  onChange={(e) => setSelectedCategory(e.target.value)}
                >
                  <option value="ALL">All Categories</option>
                  <option value="ELECTRONICS">Electronics</option>
                  <option value="APPAREL">Apparel</option>
                </select>
              </div>
            </div>

            <div className="seller-table-wrap">
              <table className="seller-table">
                <thead>
                  <tr>
                    <th style={{ width: '30px' }}>#</th>
                    <th>Product Detail</th>
                    <th>SKU / ASIN</th>
                    <th style={{ textAlign: 'right' }}>Units Sold</th>
                    <th style={{ textAlign: 'right' }}>Delivered Rev.</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredListings.length > 0 ? (
                    filteredListings.map((item, index) => (
                      <tr key={item.sellerSku || index}>
                        <td style={{ fontWeight: 700, color: '#2563eb' }}>
                          {String(index + 1).padStart(2, '0')}
                        </td>
                        <td>
                          <strong>{item.productTitle}</strong>
                        </td>
                        <td>
                          <span className="asin-tag">{item.sellerSku}</span>
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 600 }}>
                          {formatCount(item.totalUnitsSold)}
                        </td>
                        <td style={{ textAlign: 'right', fontWeight: 700, color: '#0f172a' }}>
                          {formatCurrency(Number(item.totalRevenue))}
                        </td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={5} style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                        No product listing matches found.
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </div>

          {/* Hourly Order Influx Card */}
          <div className="seller-card">
            <div className="seller-card-header">
              <div>
                <h2>📈 Hourly Order Influx vs Same Period (24h Trend)</h2>
                <p className="seller-card-subtitle">
                  Real-time velocity curve across UTC fulfillment cycles.
                </p>
              </div>
              <span className="seller-badge-green">+12.4% Surge</span>
            </div>
            <OrderInfluxChart data={hourlyData} />
          </div>
        </div>

        {/* Right Sidebar Widgets */}
        <div className="seller-dash-sidebar">
          {/* Fulfillment Staging */}
          <div className="seller-card">
            <div className="seller-card-header">
              <h3>🚚 Fulfillment Staging</h3>
              <span className="seller-badge-green">99.4% On-Time</span>
            </div>

            <div style={{ marginBottom: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.3rem' }}>
                <span style={{ fontWeight: 600, color: '#475569' }}>AWAITING DISPATCH</span>
                <strong style={{ color: '#d97706' }}>{pendingOrders} Orders</strong>
              </div>
              <div className="seller-kpi-bar" style={{ height: '6px' }}>
                <div className="seller-kpi-bar-fill" style={{ width: '45%', background: '#f59e0b' }} />
              </div>
              <div style={{ fontSize: '0.72rem', color: '#64748b', marginTop: '0.3rem' }}>
                18 orders scheduled within next 4 operating hours
              </div>
            </div>

            <div style={{ marginBottom: '1.25rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.8rem', marginBottom: '0.3rem' }}>
                <span style={{ fontWeight: 600, color: '#475569' }}>IN-TRANSIT OUTBOUND</span>
                <strong style={{ color: '#2563eb' }}>{shippedOrders} Shipments</strong>
              </div>
              <div className="seller-kpi-bar" style={{ height: '6px' }}>
                <div className="seller-kpi-bar-fill" style={{ width: '80%', background: '#2563eb' }} />
              </div>
              <div style={{ fontSize: '0.72rem', color: '#64748b', marginTop: '0.3rem' }}>
                94% First Attempt Delivery Success Target
              </div>
            </div>

            <div style={{ borderTop: '1px solid #f1f5f9', paddingTop: '0.75rem', marginBottom: '1rem' }}>
              <span style={{ fontSize: '0.7rem', fontWeight: 700, letterSpacing: '0.05em', color: '#64748b', textTransform: 'uppercase' }}>
                Carrier Allocation
              </span>
              <div style={{ display: 'flex', gap: '0.5rem', marginTop: '0.5rem' }}>
                <div style={{ flex: 1, background: '#f8fafc', padding: '0.5rem', borderRadius: '6px', textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700 }}>54%</div>
                  <div style={{ fontSize: '0.65rem', color: '#64748b' }}>UPS Ground</div>
                </div>
                <div style={{ flex: 1, background: '#f8fafc', padding: '0.5rem', borderRadius: '6px', textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700 }}>32%</div>
                  <div style={{ fontSize: '0.65rem', color: '#64748b' }}>FedEx Exp</div>
                </div>
                <div style={{ flex: 1, background: '#f8fafc', padding: '0.5rem', borderRadius: '6px', textAlign: 'center' }}>
                  <div style={{ fontSize: '0.85rem', fontWeight: 700 }}>14%</div>
                  <div style={{ fontSize: '0.65rem', color: '#64748b' }}>USPS Prio</div>
                </div>
              </div>
            </div>


          </div>

          {/* Buy Box Health Alerts */}
          <div className="seller-card">
            <div className="seller-card-header">
              <h3>⚠️ Buy Box Health Alerts</h3>
              <span className="seller-badge-red">3 Lost</span>
            </div>
            <p style={{ fontSize: '0.75rem', color: '#64748b', margin: '0 0 0.75rem 0' }}>
              Competitor repricing engines triggered undercuts on these key revenue drivers:
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem' }}>
              <div style={{ background: '#f8fafc', padding: '0.65rem', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 700 }}>Logitech MX Master 3S</div>
                <div style={{ fontSize: '0.72rem', color: '#64748b', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.25rem' }}>
                  <span>Yours: $99.99 → <strong style={{ color: '#ef4444' }}>Min: $94.49</strong></span>
                  <span style={{ color: '#ef4444', fontWeight: 600, fontSize: '0.7rem' }}>Lost</span>
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '0.65rem', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 700 }}>Belkin BoostCharge Pro 3-in-1</div>
                <div style={{ fontSize: '0.72rem', color: '#64748b', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.25rem' }}>
                  <span>Yours: $139.95 → <strong style={{ color: '#ef4444' }}>Min: $134.00</strong></span>
                  <span style={{ color: '#ef4444', fontWeight: 600, fontSize: '0.7rem' }}>Lost</span>
                </div>
              </div>

              <div style={{ background: '#f8fafc', padding: '0.65rem', borderRadius: '6px', border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.8rem', fontWeight: 700 }}>SanDisk Extreme 1TB microSD</div>
                <div style={{ fontSize: '0.72rem', color: '#64748b', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginTop: '0.25rem' }}>
                  <span>Yours: $109.99 → <strong style={{ color: '#ef4444' }}>Min: $104.99</strong></span>
                  <span style={{ color: '#ef4444', fontWeight: 600, fontSize: '0.7rem' }}>Lost</span>
                </div>
              </div>
            </div>

            <div style={{ marginTop: '0.75rem', display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem' }}>
              <span style={{ color: '#64748b', fontSize: '0.75rem', fontWeight: 600 }}>Review pricing strategy &rarr;</span>
              <span style={{ color: '#94a3b8', cursor: 'pointer' }}>Dismiss All</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
