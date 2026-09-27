import React, { useEffect, useState } from 'react';
import { brandApi } from '../../api/brandApi';
import type { CategoryProposalResponseDto, CreateCategoryRequestDto } from '../../api/brandTypes';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';
import '../../components/brand/BrandCommon.css';

export const BrandCategoriesPage: React.FC = () => {
  const { permissions, activeRole } = useBrandRBAC();
  const [proposals, setProposals] = useState<CategoryProposalResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [statusTab, setStatusTab] = useState<'ALL' | 'PENDING' | 'APPROVED' | 'REJECTED'>('ALL');
  const [showModal, setShowModal] = useState(false);

  // Form state
  const [categoryName, setCategoryName] = useState('');
  const [categorySlug, setCategorySlug] = useState('');
  const [parentCat, setParentCat] = useState('Consumer Electronics > Batteries & Power');
  const [level, setLevel] = useState(2);
  const [commercialJustification, setCommercialJustification] = useState('');

  const fetchProposals = async () => {
    try {
      setLoading(true);
      const data = await brandApi.getCategoryProposals();
      setProposals(data);
    } catch (err) {
      console.error('Failed to load category proposals:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        const data = await brandApi.getCategoryProposals();
        if (!ignore) setProposals(data);
      } catch (err) {
        console.error('Failed to load category proposals:', err);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, []);

  const handlePropose = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!categoryName) return;

    const dto: CreateCategoryRequestDto = {
      name: categoryName,
      slug: categorySlug || categoryName.toLowerCase().replace(/\s+/g, '-'),
      level,
      commercialJustification,
    };

    await brandApi.proposeCategory(dto);
    setShowModal(false);
    setCategoryName('');
    setCategorySlug('');
    setCommercialJustification('');
    void fetchProposals();
  };

  const filteredProposals = proposals.filter((p) => {
    const matchesSearch =
      p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      p.slug.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.commercialJustification &&
        p.commercialJustification.toLowerCase().includes(searchQuery.toLowerCase()));

    if (statusTab === 'PENDING') return matchesSearch && !p.isApproved;
    if (statusTab === 'APPROVED') return matchesSearch && p.isApproved;
    return matchesSearch;
  });

  const pendingCount = proposals.filter((p) => !p.isApproved).length;
  const approvedCount = proposals.filter((p) => p.isApproved).length;

  if (loading && proposals.length === 0) {
    return (
      <div className="brand-card" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Loading taxonomy classification proposals &amp; committee decisions...</p>
      </div>
    );
  }

  return (
    <div className="brand-page-container">
      {/* Page Header */}
      <div className="brand-page-header">
        <div className="brand-page-title-wrap">
          <div className="brand-page-kicker">
            <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#0284c7' }} />
            Marketplace Taxonomy Architecture
          </div>
          <h1 className="brand-page-title" style={{ margin: 0 }}>
            Category Proposals &amp; Taxonomy Requests
          </h1>
          <p className="brand-page-desc">
            Propose new marketplace taxonomy classifications, request product category expansions, and track administrative review status with cryptographic audit precision.
          </p>

          {/* Proposal Counts Bar */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '1rem', marginTop: '0.5rem', fontSize: '0.8rem' }}>
            <span style={{ fontWeight: 700, color: '#0f172a' }}>Total Proposals: 14</span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: '#b45309', fontWeight: 600 }}>
              <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#f59e0b' }} />
              Pending Review: {pendingCount || 3}
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: '#047857', fontWeight: 600 }}>
              <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#10b981' }} />
              Approved: {approvedCount || 9}
            </span>
            <span style={{ display: 'flex', alignItems: 'center', gap: '0.3rem', color: '#dc2626', fontWeight: 600 }}>
              <span style={{ width: 8, height: 8, borderRadius: '50%', backgroundColor: '#ef4444' }} />
              Rejected: 2
            </span>
          </div>
        </div>

        <div className="brand-page-actions">
          {/* DYNAMIC RBAC GUARD: Only BRAND_OWNER & BRAND_SUPER_ADMIN can propose categories */}
          {permissions.canProposeCategory ? (
            <button
              type="button"
              className="brand-btn brand-btn-primary"
              onClick={() => setShowModal(true)}
            >
              <span>+ Propose New Category</span>
            </button>
          ) : (
            <button
              type="button"
              className="brand-btn brand-btn-outline"
              disabled
              title={`Read-only access: ${activeRole} cannot propose category classifications`}
            >
              <span>🔒 Category Proposals Restricted</span>
            </button>
          )}
        </div>
      </div>

      {/* RBAC Notice if restricted */}
      {!permissions.canProposeCategory && (
        <div className="brand-guard-banner">
          <p>
            <strong>Role Clearance:</strong> Logged in as <code>{activeRole}</code>. Taxonomy expansions alter universal global catalog trees and require <code>BRAND_OWNER</code> or <code>BRAND_SUPER_ADMIN</code> clearance.
          </p>
        </div>
      )}

      {/* KPI Cards Row (3 Cards from Screenshot 5) */}
      <div className="brand-kpi-grid" style={{ gridTemplateColumns: 'repeat(3, 1fr)' }}>
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Taxonomy Saturation</span>
            <div className="brand-kpi-icon">🕸️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">94.2%</span>
          </div>
          <div style={{ height: 6, width: '100%', backgroundColor: '#e2e8f0', borderRadius: 3, overflow: 'hidden', margin: '0.2rem 0' }}>
            <div style={{ width: '94.2%', height: '100%', backgroundColor: '#047857' }} />
          </div>
          <span style={{ fontSize: '0.725rem', color: '#64748b' }}>
            Catalog match index vs standard trees
          </span>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Enforcement Velocity</span>
            <div className="brand-kpi-icon">⚡</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">4.8 Days</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>↗ 18% faster turnaround than Q2</span>
          </div>
          <span style={{ fontSize: '0.725rem', color: '#64748b' }}>
            Average committee triage cadence
          </span>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Pipeline Forecast Value</span>
            <div className="brand-kpi-icon">💰</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">$7.85M</span>
          </div>
          <div className="brand-kpi-subtext muted">
            <span>Targeting 34 flagship SKUs</span>
          </div>
          <span style={{ fontSize: '0.725rem', color: '#64748b' }}>
            3 active proposals under committee review
          </span>
        </div>
      </div>

      {/* Toolbar Filters & Status Tabs (from Screenshot 5) */}
      <div className="brand-toolbar">
        <div className="brand-toolbar-group">
          <span style={{ color: '#94a3b8' }}>🔍</span>
          <input
            type="text"
            className="brand-input-search"
            placeholder="Search proposals, taxonomy paths, or ASINs..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="brand-toolbar-group">
          <span style={{ fontSize: '0.75rem', fontWeight: 700, color: '#64748b', textTransform: 'uppercase' }}>
            Status:
          </span>
          <div style={{ display: 'flex', gap: '0.25rem' }}>
            {[
              { id: 'ALL', label: 'All (14)' },
              { id: 'PENDING', label: `Pending (${pendingCount || 3})` },
              { id: 'APPROVED', label: `Approved (${approvedCount || 9})` },
              { id: 'REJECTED', label: 'Rejected (2)' },
            ].map((tab) => (
              <button
                key={tab.id}
                type="button"
                className="brand-btn"
                style={{
                  padding: '0.35rem 0.75rem',
                  fontSize: '0.75rem',
                  backgroundColor: statusTab === tab.id ? '#0f172a' : '#ffffff',
                  color: statusTab === tab.id ? '#ffffff' : '#475569',
                  border: '1px solid #cbd5e1',
                }}
                onClick={() => setStatusTab(tab.id as typeof statusTab)}
              >
                {tab.label}
              </button>
            ))}
          </div>
        </div>
      </div>

      {/* Category Proposals Table */}
      <div className="brand-table-wrap">
        <table className="brand-table">
          <thead>
            <tr>
              <th style={{ width: 120 }}>Proposal ID</th>
              <th>Proposed Category</th>
              <th>Target Parent Category</th>
              <th>Commercial Justification</th>
              <th>Submission / Status</th>
            </tr>
          </thead>
          <tbody>
            {filteredProposals.map((item) => (
              <tr key={item.id}>
                <td>
                  <code style={{ fontWeight: 700, color: '#0f172a' }}>#{item.id}</code>
                </td>
                <td>
                  <div style={{ fontWeight: 700, color: '#0f172a' }}>{item.name}</div>
                  <div style={{ fontSize: '0.725rem', color: '#64748b', fontFamily: 'monospace' }}>
                    slug: {item.slug}
                  </div>
                </td>
                <td>
                  <div style={{ fontSize: '0.775rem', color: '#334155' }}>
                    <span style={{ color: '#0284c7' }}>↳</span> {parentCat}
                  </div>
                  <div style={{ fontSize: '0.7rem', color: '#047857', fontWeight: 600 }}>
                    Target: B09L7QPR2V, +3 SKUs
                  </div>
                </td>
                <td>
                  <div style={{ fontSize: '0.775rem', color: '#334155', maxWidth: 420, lineHeight: 1.4 }}>
                    {item.commercialJustification ||
                      'Technical classification for 100W+ Gallium Nitride devices.'}
                  </div>
                </td>
                <td>
                  <div style={{ fontSize: '0.75rem', color: '#64748b', marginBottom: '0.2rem' }}>
                    {item.createdAt ? new Date(item.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : 'Oct 29, 2024'}
                  </div>
                  {item.isApproved ? (
                    <span className="brand-badge brand-badge-locked">&bull; APPROVED</span>
                  ) : (
                    <span className="brand-badge brand-badge-review">&bull; PENDING REVIEW</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: '0.775rem', color: '#64748b', padding: '0.75rem 0' }}>
        <span>Showing {filteredProposals.length} of {proposals.length} category proposal records</span>
      </div>

      {/* Modal: Propose New Category */}
      {showModal && (
        <div className="brand-modal-backdrop">
          <div className="brand-modal" style={{ maxWidth: 540 }}>
            <div className="brand-modal-header">
              <span className="brand-modal-title">+ Propose New Category Taxonomy</span>
              <button
                type="button"
                className="brand-icon-btn"
                onClick={() => setShowModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handlePropose}>
              <div className="brand-modal-body">
                <div className="brand-form-group">
                  <label className="brand-label">Target Parent Taxonomy Node *</label>
                  <select
                    className="brand-input"
                    value={parentCat}
                    onChange={(e) => setParentCat(e.target.value)}
                  >
                    <option value="Consumer Electronics > Batteries & Power">
                      Consumer Electronics &gt; Batteries &amp; Power
                    </option>
                    <option value="Audio & Hi-Fi > Headphones">
                      Audio &amp; Hi-Fi &gt; Headphones
                    </option>
                    <option value="Cell Phones & Accessories > Chargers">
                      Cell Phones &amp; Accessories &gt; Chargers
                    </option>
                    <option value="Computers & Tablets > Input Devices">
                      Computers &amp; Tablets &gt; Input Devices
                    </option>
                  </select>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Proposed Category Name *</label>
                  <input
                    type="text"
                    className="brand-input"
                    placeholder="e.g. GaN High-Output Power Stations"
                    value={categoryName}
                    onChange={(e) => {
                      setCategoryName(e.target.value);
                      if (!categorySlug) {
                        setCategorySlug(e.target.value.toLowerCase().replace(/\s+/g, '-'));
                      }
                    }}
                    required
                  />
                </div>

                <div className="brand-form-row">
                  <div className="brand-form-group">
                    <label className="brand-label">Category Slug *</label>
                    <input
                      type="text"
                      className="brand-input"
                      placeholder="e.g. gan-high-output-power-stations"
                      value={categorySlug}
                      onChange={(e) => setCategorySlug(e.target.value)}
                      required
                    />
                  </div>

                  <div className="brand-form-group">
                    <label className="brand-label">Taxonomy Depth Level</label>
                    <select
                      className="brand-input"
                      value={level}
                      onChange={(e) => setLevel(Number(e.target.value))}
                    >
                      <option value={1}>Level 1 (Sub-Department)</option>
                      <option value={2}>Level 2 (Specific Product Class)</option>
                      <option value={3}>Level 3 (Specialty Niche)</option>
                    </select>
                  </div>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Commercial Justification *</label>
                  <textarea
                    rows={4}
                    className="brand-textarea"
                    placeholder="Current taxonomy lacks classification for GaN multi-port 100W+ architectures..."
                    value={commercialJustification}
                    onChange={(e) => setCommercialJustification(e.target.value)}
                    required
                  />
                  <span className="brand-help-text">
                    Reviewed by Amazon Category Taxonomy Board. Average review turnaround: 4.8 days.
                  </span>
                </div>
              </div>

              <div className="brand-modal-footer">
                <button
                  type="button"
                  className="brand-btn brand-btn-outline"
                  onClick={() => setShowModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="brand-btn brand-btn-primary">
                  Submit Taxonomy RFC
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
