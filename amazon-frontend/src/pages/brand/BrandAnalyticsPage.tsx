import React, { useEffect, useState } from 'react';
import { brandApi } from '../../api/brandApi';
import type { BrandAnalyticsResponseDto } from '../../api/brandTypes';
import '../../components/brand/BrandCommon.css';

export const BrandAnalyticsPage: React.FC = () => {
  const [analytics, setAnalytics] = useState<BrandAnalyticsResponseDto | null>(null);
  const [loading, setLoading] = useState(true);
  const [filterQuery, setFilterQuery] = useState('');
  const [selectedFilter, setSelectedFilter] = useState('ALL');
  const [syncing, setSyncing] = useState(false);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const data = await brandApi.getBrandAnalytics();
        setAnalytics(data);
      } catch (err) {
        console.error('Failed to load brand analytics:', err);
      } finally {
        setLoading(false);
      }
    };
    void loadData();
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    try {
      const data = await brandApi.getBrandAnalytics();
      setAnalytics(data);
    } finally {
      setTimeout(() => setSyncing(false), 600);
    }
  };

  const handleExportMetrics = () => {
    if (!analytics) return;
    const blob = new Blob([JSON.stringify(analytics, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `brand-analytics-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const products = analytics?.topSellingProducts || [];

  if (loading && !analytics) {
    return (
      <div className="brand-card" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Loading enterprise brand analytics &amp; marketplace telemetry...</p>
      </div>
    );
  }

  const filteredProducts = products.filter((p) => {
    const matchesSearch =
      p.productTitle.toLowerCase().includes(filterQuery.toLowerCase()) ||
      p.asin.toLowerCase().includes(filterQuery.toLowerCase());
    return matchesSearch;
  });

  return (
    <div className="brand-page-container">
      {/* Page Header */}
      <div className="brand-page-header">
        <div className="brand-page-title-wrap">
          <div className="brand-page-kicker">
            <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#0284c7' }} />
            Enterprise Brand Registry &bull; Executive Telemetry
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
            <h1 className="brand-page-title" style={{ margin: 0 }}>
              Brand Analytics &amp; Marketplace Overview
            </h1>
            <span className="brand-badge brand-badge-locked" style={{ fontSize: '0.75rem' }}>
              LIVE SYNCHRONIZED
            </span>
          </div>
          <p className="brand-page-desc">
            Real-time performance metrics, verified catalog coverage, and marketplace offer distribution across Amazon Unified accounts.
          </p>
        </div>

        <div className="brand-page-actions" style={{ flexDirection: 'column', alignItems: 'flex-end', gap: '0.4rem' }}>
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <select className="brand-select" defaultValue="30d">
              <option value="30d">📅 Last 30 Days: Oct 1 - Oct 31, 2024</option>
              <option value="7d">📅 Last 7 Days</option>
              <option value="90d">📅 Last Quarter</option>
            </select>
            <button
              type="button"
              className="brand-btn brand-btn-primary"
              onClick={handleSync}
              disabled={syncing}
            >
              <span>{syncing ? '⟳ Syncing...' : '↻ Sync Marketplace Data'}</span>
            </button>
            <button
              type="button"
              className="brand-btn brand-btn-outline"
              onClick={handleExportMetrics}
              title="Export analytics telemetry as JSON"
            >
              <span>📊 Export Metrics (JSON)</span>
            </button>
          </div>
          <span style={{ fontSize: '0.7rem', color: '#64748b' }}>
            Last synced: Today, 14:32:08 UTC
          </span>
        </div>
      </div>

      {/* KPI Cards Row (7 Metrics) */}
      <div className="brand-kpi-grid">
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Total Brand Revenue</span>
            <div className="brand-kpi-icon">💵</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              ${analytics?.totalBrandRevenue?.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) ?? '4,892,430.00'}
            </span>
          </div>
          <div className="brand-kpi-subtext">
            <span>↗ +14.2%</span>
            <span className="muted">vs prior 30d</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Total Units Sold</span>
            <div className="brand-kpi-icon">🛍️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {analytics?.totalUnitsSold?.toLocaleString() ?? '142,850'}
            </span>
            <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>Units</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>↗ +8.6%</span>
            <span className="muted">velocity</span>
          </div>
        </div>

        {/* Master Catalog with dynamic pendingCatalogCount */}
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Master Catalog</span>
            <div className="brand-kpi-icon">📑</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {analytics?.totalBrandProducts ?? 184}
            </span>
            <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>Products</span>
          </div>
          <div className="brand-kpi-subtext warning">
            <span>{analytics?.pendingCatalogCount ?? 12} pending review</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Registered SKUs</span>
            <div className="brand-kpi-icon">🏷️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {analytics?.totalBrandVariants ?? 642}
            </span>
            <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>ASINs</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>98.4% win share</span>
          </div>
        </div>

        {/* Seller Offers with dynamic unauthorizedSellerAlertsCount */}
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Seller Offers</span>
            <div className="brand-kpi-icon">🏪</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {analytics?.totalActiveListings ?? 1289}
            </span>
            <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>Offers</span>
          </div>
          <div className="brand-kpi-subtext danger">
            <span className="brand-kpi-alert-badge">
              ⚠️ {analytics?.unauthorizedSellerAlertsCount ?? 4} hijacked
            </span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Governance Team</span>
            <div className="brand-kpi-icon">👥</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {analytics?.totalTeamMembers ?? 18}
            </span>
            <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>Members</span>
          </div>
          <div className="brand-kpi-subtext muted">
            <span>4 regional tiers</span>
          </div>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Marketing Posts</span>
            <div className="brand-kpi-icon">📢</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">
              {analytics?.totalBrandPosts ?? 86}
            </span>
            <span style={{ fontSize: '0.85rem', color: '#64748b', fontWeight: 600 }}>Live</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>482k engg.</span>
          </div>
        </div>
      </div>

      {/* Main Grid: Left Products Table & Right Security / Footprint Sidebar */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) 360px', gap: '1.5rem', alignItems: 'start' }}>
        {/* Left Column: Top Brand Products Performance */}
        <div className="brand-card">
          <div className="brand-card-header">
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <h3 className="brand-card-title" style={{ margin: 0 }}>Top Brand Products Performance</h3>
                <span className="brand-badge brand-badge-review">Ranked by Velocity</span>
              </div>
              <p style={{ margin: '0.25rem 0 0', fontSize: '0.775rem', color: '#64748b' }}>
                Live telemetry from Amazon Unified Selling Partner API
              </p>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
              <input
                type="text"
                className="brand-input-search"
                style={{ width: 200 }}
                placeholder="Filter ASIN or name..."
                value={filterQuery}
                onChange={(e) => setFilterQuery(e.target.value)}
              />
              <div style={{ display: 'flex', gap: '0.25rem' }}>
                {['ALL', 'Audio', 'Power'].map((cat) => (
                  <button
                    key={cat}
                    type="button"
                    className={`brand-btn brand-btn-outline ${selectedFilter === cat ? 'active' : ''}`}
                    style={{
                      padding: '0.3rem 0.6rem',
                      fontSize: '0.75rem',
                      backgroundColor: selectedFilter === cat ? '#0f172a' : '#ffffff',
                      color: selectedFilter === cat ? '#ffffff' : '#475569',
                    }}
                    onClick={() => setSelectedFilter(cat)}
                  >
                    {cat === 'ALL' ? 'All SKUs' : cat}
                  </button>
                ))}
              </div>
            </div>
          </div>

          <div className="brand-table-wrap">
            <table className="brand-table">
              <thead>
                <tr>
                  <th style={{ width: 60 }}>Rank</th>
                  <th>Product Title</th>
                  <th>ASIN</th>
                  <th style={{ textAlign: 'right' }}>Units Sold</th>
                  <th style={{ textAlign: 'right' }}>Total Revenue</th>
                </tr>
              </thead>
              <tbody>
                {filteredProducts.map((p, idx) => (
                  <tr key={p.asin}>
                    <td>
                      <span
                        style={{
                          backgroundColor: idx === 0 ? '#fef3c7' : '#f1f5f9',
                          color: idx === 0 ? '#b45309' : '#475569',
                          fontWeight: 700,
                          fontSize: '0.75rem',
                          padding: '0.2rem 0.5rem',
                          borderRadius: 4,
                        }}
                      >
                        #{idx + 1}
                      </span>
                    </td>
                    <td>
                      <div style={{ fontWeight: 600, color: '#0f172a' }}>{p.productTitle}</div>
                      <div style={{ fontSize: '0.725rem', color: '#64748b' }}>
                        Category: Verified Consumer Tech &bull; High Velocity
                      </div>
                    </td>
                    <td>
                      <code style={{ background: '#f1f5f9', padding: '0.2rem 0.4rem', borderRadius: 4, fontSize: '0.8rem', color: '#0369a1', fontWeight: 600 }}>
                        {p.asin}
                      </code>
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 600 }}>
                      {p.totalUnitsSold.toLocaleString()}
                    </td>
                    <td style={{ textAlign: 'right', fontWeight: 700, color: '#047857' }}>
                      ${p.totalRevenue.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
          <div style={{ padding: '0.75rem 0 0', display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.775rem', color: '#64748b' }}>
            <span>Showing {filteredProducts.length} of {products.length} active brand products</span>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <button type="button" className="brand-btn brand-btn-outline" style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }} disabled>Previous</button>
              <button type="button" className="brand-btn brand-btn-outline" style={{ padding: '0.25rem 0.6rem', fontSize: '0.75rem' }} disabled>Next</button>
            </div>
          </div>
        </div>

        {/* Right Column: Security, Footprint & Unauthorized Resellers Alert */}
        <div style={{ display: 'flex', flexDirection: 'column', gap: '1.25rem' }}>
          {/* Brand Footprint Summary */}
          <div className="brand-card">
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '0.75rem' }}>
              <h4 style={{ margin: 0, fontSize: '0.925rem', fontWeight: 700, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
                <span>🌐</span> Brand Footprint Summary
              </h4>
              <span style={{ fontSize: '0.7rem', fontWeight: 700, color: '#64748b', background: '#f1f5f9', padding: '0.15rem 0.45rem', borderRadius: 4 }}>
                RATIO 1 : 2.01
              </span>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '0.75rem', marginBottom: '1rem' }}>
              <div style={{ background: '#f8fafc', padding: '0.75rem', borderRadius: 6, border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.65rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Registered ASINs</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#0f172a' }}>642</div>
                <div style={{ fontSize: '0.675rem', color: '#047857', fontWeight: 600 }}>100% Brand Owned</div>
              </div>
              <div style={{ background: '#f8fafc', padding: '0.75rem', borderRadius: 6, border: '1px solid #e2e8f0' }}>
                <div style={{ fontSize: '0.65rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>Marketplace Offers</div>
                <div style={{ fontSize: '1.25rem', fontWeight: 800, color: '#0f172a' }}>1,289</div>
                <div style={{ fontSize: '0.675rem', color: '#64748b' }}>Active Seller Nodes</div>
              </div>
            </div>

            {/* Distribution Bar */}
            <div style={{ marginBottom: '1rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '0.35rem', fontWeight: 600 }}>
                <span>Listing Origin Distribution</span>
                <span style={{ color: '#047857' }}>68% Direct Brand</span>
              </div>
              <div style={{ height: 8, borderRadius: 4, display: 'flex', overflow: 'hidden' }}>
                <div style={{ width: '68%', backgroundColor: '#047857' }} title="Direct (68%)" />
                <div style={{ width: '24%', backgroundColor: '#0284c7' }} title="Auth T1 (24%)" />
                <div style={{ width: '8%', backgroundColor: '#e2e8f0' }} title="3P (8%)" />
              </div>
              <div style={{ display: 'flex', gap: '0.75rem', fontSize: '0.675rem', color: '#64748b', marginTop: '0.4rem' }}>
                <span>&bull; Direct (68%)</span>
                <span>&bull; Auth T1 (24%)</span>
                <span>&bull; 3P (8%)</span>
              </div>
            </div>

            {/* Buy Box Integrity */}
            <div style={{ background: '#f0fdf4', border: '1px solid #bbf7d0', padding: '0.75rem', borderRadius: 6, display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '1.1rem' }}>🛡️</span>
                <div>
                  <div style={{ fontSize: '0.775rem', fontWeight: 700, color: '#166534' }}>Buy Box Integrity</div>
                  <div style={{ fontSize: '0.7rem', color: '#15803d' }}>98.6% Brand Protected</div>
                </div>
              </div>
              <span className="brand-badge brand-badge-locked" style={{ background: '#dcfce7', color: '#15803d' }}>
                OPTIMAL
              </span>
            </div>

            {/* DYNAMIC KPI WIDGET: Unauthorized Seller Alerts */}
            <div style={{ marginTop: '0.85rem', background: '#fef2f2', border: '1px solid #fecaca', padding: '0.85rem', borderRadius: 6 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', color: '#b91c1c', fontWeight: 700, fontSize: '0.825rem' }}>
                <span>⚠️</span>
                <span>{analytics?.unauthorizedSellerAlertsCount ?? 4} Unauthorized Seller Alerts</span>
              </div>
              <p style={{ margin: '0.35rem 0 0', fontSize: '0.75rem', color: '#991b1b', lineHeight: 1.4 }}>
                Suspected rogue mapping detected on 2 core audio variants in EU/UK regions.
              </p>
            </div>
          </div>

          {/* Trademark & IP Protection */}
          <div className="brand-card">
            <h4 style={{ margin: '0 0 0.85rem', fontSize: '0.925rem', fontWeight: 700, color: '#0f172a', display: 'flex', alignItems: 'center', gap: '0.4rem' }}>
              <span>🛡️</span> Trademark &amp; IP Protection
            </h4>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '0.65rem', fontSize: '0.8rem' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '0.5rem', borderBottom: '1px solid #f1f5f9' }}>
                <div>
                  <div style={{ fontWeight: 600, color: '#0f172a' }}>USPTO Principal Register</div>
                  <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Reg #97412854 (Class 09, 14, 28)</div>
                </div>
                <span className="brand-badge brand-badge-locked">VALIDATED</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '0.5rem', borderBottom: '1px solid #f1f5f9' }}>
                <div>
                  <div style={{ fontWeight: 600, color: '#0f172a' }}>Registry Level Tier</div>
                  <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Autonomous IP Enforcement</div>
                </div>
                <span style={{ fontWeight: 700, color: '#0f172a', fontSize: '0.775rem' }}>Level 3 Active</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: '0.5rem', borderBottom: '1px solid #f1f5f9' }}>
                <div>
                  <div style={{ fontWeight: 600, color: '#0f172a' }}>Project Zero &amp; Transparency</div>
                  <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Self-service counterfeit removal</div>
                </div>
                <span className="brand-badge brand-badge-locked">✓ Enrolled</span>
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <div style={{ fontWeight: 600, color: '#0f172a' }}>Automated Shield Blocks</div>
                  <div style={{ fontSize: '0.7rem', color: '#64748b' }}>Interceptions past 30 days</div>
                </div>
                <span style={{ fontWeight: 800, color: '#dc2626', fontSize: '0.85rem' }}>19 Blocked</span>
              </div>
            </div>

            <div style={{ marginTop: '1rem', paddingTop: '0.75rem', borderTop: '1px solid #e2e8f0', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span style={{ fontSize: '0.725rem', color: '#64748b' }}>Need to report high-volume copyright theft?</span>
              <a href="#dossier" style={{ fontSize: '0.75rem', color: '#0284c7', fontWeight: 600, textDecoration: 'none' }} onClick={(e) => e.preventDefault()}>
                Open Dossier &rarr;
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
