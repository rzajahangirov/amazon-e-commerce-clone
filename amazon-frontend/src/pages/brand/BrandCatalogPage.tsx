import React, { useEffect, useState } from 'react';
import { brandApi } from '../../api/brandApi';
import type {
  CreateBrandCatalogProductRequestDto,
  CreateVariantRequestDto,
  GovernanceStatus,
  ProductResponseDto,
} from '../../api/brandTypes';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';
import '../../components/brand/BrandCommon.css';

export const BrandCatalogPage: React.FC = () => {
  const { permissions, activeRole } = useBrandRBAC();
  const [products, setProducts] = useState<ProductResponseDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchQuery, setSearchQuery] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('ALL');
  const [statusFilter, setStatusFilter] = useState('ALL');

  // Modals
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showVariantModal, setShowVariantModal] = useState(false);
  const [selectedProductId, setSelectedProductId] = useState<string | null>(null);

  // Form states
  const [newTitle, setNewTitle] = useState('');
  const [newMasterSku, setNewMasterSku] = useState('');
  const [newCategory, setNewCategory] = useState('cat-audio');
  const [newGovernanceStatus, setNewGovernanceStatus] = useState<GovernanceStatus>('ACTIVE_LOCKED');
  const [newDescription, setNewDescription] = useState('');

  // Variant form states
  const [varAsin, setVarAsin] = useState('');
  const [varName, setVarName] = useState('');
  const [varColor, setVarColor] = useState('Graphite');

  const fetchProducts = async () => {
    try {
      setLoading(true);
      const res = await brandApi.getBrandProducts(0, 50);
      setProducts(res.content);
      if (res.content.length > 0 && !selectedProductId) {
        setSelectedProductId(res.content[0].id);
      }
    } catch (err) {
      console.error('Failed to load products:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let ignore = false;
    const run = async () => {
      try {
        setLoading(true);
        const res = await brandApi.getBrandProducts(0, 50);
        if (!ignore) {
          setProducts(res.content);
          if (res.content.length > 0) {
            setSelectedProductId((prev) => prev || res.content[0].id);
          }
        }
      } catch (err) {
        console.error('Failed to load products:', err);
      } finally {
        if (!ignore) setLoading(false);
      }
    };
    void run();
    return () => { ignore = true; };
  }, []);

  const handleCreateProduct = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newTitle || !newMasterSku) return;

    const dto: CreateBrandCatalogProductRequestDto = {
      categoryId: newCategory,
      title: newTitle,
      masterSku: newMasterSku,
      governanceStatus: newGovernanceStatus,
      description: newDescription || 'Authoritative brand specification registered under USPTO trademark.',
    };

    await brandApi.createBrandProductTemplate(dto);
    setShowCreateModal(false);
    setNewTitle('');
    setNewMasterSku('');
    setNewDescription('');
    void fetchProducts();
  };

  const handleCreateVariant = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedProductId || !varAsin || !varName) return;

    const dto: CreateVariantRequestDto = {
      asin: varAsin,
      variantName: varName,
      variantAttributes: { Color: varColor },
    };

    await brandApi.createBrandProductVariant(selectedProductId, dto);
    setShowVariantModal(false);
    setVarAsin('');
    setVarName('');
    void fetchProducts();
  };

  const handleExportCatalog = () => {
    const blob = new Blob([JSON.stringify(products, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `brand-catalog-${new Date().toISOString().split('T')[0]}.json`;
    a.click();
    URL.revokeObjectURL(url);
  };

  const selectedProduct = products.find((p) => p.id === selectedProductId) || products[0];

  const filteredProducts = products.filter((p) => {
    const matchesSearch =
      p.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
      (p.masterSku && p.masterSku.toLowerCase().includes(searchQuery.toLowerCase())) ||
      (p.variants && p.variants.some((v) => v.asin.toLowerCase().includes(searchQuery.toLowerCase())));

    const matchesCat = categoryFilter === 'ALL' || p.categoryName === categoryFilter;
    const matchesStatus = statusFilter === 'ALL' || p.governanceStatus === statusFilter;

    return matchesSearch && matchesCat && matchesStatus;
  });

  if (loading && products.length === 0) {
    return (
      <div className="brand-card" style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Loading authoritative brand catalog &amp; ASIN variant clusters...</p>
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
            Catalog Governance Engine &bull; v4.19-PROD
          </div>
          <h1 className="brand-page-title" style={{ margin: 0 }}>
            Brand Catalog &amp; ASIN Governance
          </h1>
          <p className="brand-page-desc">
            Authoritative master product definitions, ASIN variant registration, and catalog lockdown across marketplace sellers.
          </p>
        </div>

        <div className="brand-page-actions">
          <button
            type="button"
            className="brand-btn brand-btn-outline"
            onClick={handleExportCatalog}
            title="Download catalog definitions as JSON"
          >
            <span>📊 Export Catalog (JSON)</span>
          </button>

          {/* DYNAMIC RBAC GUARD: Hide/Disable "+ Create Master Product Template" for BRAND_SELLER and BRAND_MARKETING_MEMBER */}
          {permissions.canManageCatalog ? (
            <button
              type="button"
              className="brand-btn brand-btn-primary"
              onClick={() => setShowCreateModal(true)}
            >
              <span>+ Create Master Product Template</span>
            </button>
          ) : (
            <button
              type="button"
              className="brand-btn brand-btn-outline"
              disabled
              title={`Read-only access: ${activeRole} cannot create product templates`}
            >
              <span>🔒 Template Creation Restricted</span>
            </button>
          )}
        </div>
      </div>

      {/* RBAC Notice if Seller or Marketing */}
      {!permissions.canManageCatalog && (
        <div className="brand-guard-banner">
          <p>
            <strong>Role Notice:</strong> Logged in as <code>{activeRole}</code>. You have read-only access to view catalog templates and governed ASIN clusters. Modifications require <code>BRAND_ADMIN</code> or higher.
          </p>
        </div>
      )}

      {/* KPI Cards (4 Cards) */}
      <div className="brand-kpi-grid" style={{ gridTemplateColumns: 'repeat(4, 1fr)' }}>
        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Locked Master Templates</span>
            <div className="brand-kpi-icon">🔒</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">148</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>↗ +12 this mo</span>
          </div>
          <span style={{ fontSize: '0.675rem', color: '#64748b', marginTop: '0.2rem' }}>
            100% cryptographic checksum synced
          </span>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Governed ASINs</span>
            <div className="brand-kpi-icon">🆔</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">842</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>✓ Active Lockdown</span>
          </div>
          <span style={{ fontSize: '0.675rem', color: '#64748b', marginTop: '0.2rem' }}>
            Across NA, UK, and DE nodes
          </span>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Encroachment Attempts</span>
            <div className="brand-kpi-icon" style={{ color: '#dc2626' }}>🛡️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">0</span>
          </div>
          <div className="brand-kpi-subtext" style={{ color: '#047857' }}>
            <span>✓ Protected</span>
          </div>
          <span style={{ fontSize: '0.675rem', color: '#64748b', marginTop: '0.2rem' }}>
            19 unauthorized edits rejected (48h)
          </span>
        </div>

        <div className="brand-kpi-card">
          <div className="brand-kpi-header">
            <span>Catalog Health Index</span>
            <div className="brand-kpi-icon">⚙️</div>
          </div>
          <div className="brand-kpi-value-wrap">
            <span className="brand-kpi-value">99.8%</span>
          </div>
          <div className="brand-kpi-subtext">
            <span>↗ Elite</span>
          </div>
          <span style={{ fontSize: '0.675rem', color: '#64748b', marginTop: '0.2rem' }}>
            Attributes complete &amp; verified
          </span>
        </div>
      </div>

      {/* Toolbar Filters */}
      <div className="brand-toolbar">
        <div className="brand-toolbar-group">
          <span style={{ color: '#94a3b8' }}>🔍</span>
          <input
            type="text"
            className="brand-input-search"
            placeholder="Search by Product Title, Master SKU, or ASIN..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
          />
        </div>

        <div className="brand-toolbar-group">
          <select
            className="brand-select"
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
          >
            <option value="ALL">All Approved Categories</option>
            <option value="Audio & Hi-Fi">Audio &amp; Hi-Fi</option>
            <option value="Power Accessories">Power Accessories</option>
            <option value="Mobile Stands">Mobile Stands</option>
          </select>

          <select
            className="brand-select"
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
          >
            <option value="ALL">All Governance Statuses</option>
            <option value="ACTIVE_LOCKED">Active Locked</option>
            <option value="UNDER_REVIEW">Under Review</option>
            <option value="DRAFT">Draft Specification</option>
          </select>

          <button
            type="button"
            className="brand-btn brand-btn-outline"
            style={{ padding: '0.45rem 0.75rem', fontSize: '0.8rem' }}
            onClick={() => {
              setSearchQuery('');
              setCategoryFilter('ALL');
              setStatusFilter('ALL');
            }}
          >
            ↻ Clear Filters
          </button>
        </div>
      </div>

      {/* Catalog Table */}
      <div className="brand-table-wrap">
        <table className="brand-table">
          <thead>
            <tr>
              <th style={{ width: 40 }}>
                <input type="checkbox" aria-label="Select all products" />
              </th>
              <th>Product Title &amp; Master ID</th>
              <th>Category</th>
              <th>Variants / ASINs</th>
              <th>Governance Status</th>
              <th>Created / Authority</th>
            </tr>
          </thead>
          <tbody>
            {filteredProducts.map((p) => {
              const isSelected = p.id === selectedProductId;
              const variantCount = p.variants?.length || 0;

              return (
                <tr
                  key={p.id}
                  onClick={() => setSelectedProductId(p.id)}
                  style={{
                    cursor: 'pointer',
                    backgroundColor: isSelected ? '#f8fafc' : undefined,
                  }}
                >
                  <td onClick={(e) => e.stopPropagation()}>
                    <input
                      type="checkbox"
                      checked={isSelected}
                      onChange={() => setSelectedProductId(p.id)}
                      aria-label={`Select ${p.title}`}
                    />
                  </td>
                  <td>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                      <div
                        style={{
                          width: 44,
                          height: 44,
                          backgroundColor: '#f1f5f9',
                          borderRadius: 6,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          fontSize: '1.25rem',
                          border: '1px solid #e2e8f0',
                        }}
                      >
                        🎧
                      </div>
                      <div>
                        <div style={{ fontWeight: 700, color: '#0f172a' }}>{p.title}</div>
                        {/* Display Master SKU */}
                        <div style={{ display: 'flex', alignItems: 'center', gap: '0.35rem', marginTop: '0.2rem' }}>
                          <code style={{ fontSize: '0.75rem', color: '#475569', background: '#f1f5f9', padding: '0.1rem 0.35rem', borderRadius: 4 }}>
                            {p.masterSku || 'MSKU-NEX-000'}
                          </code>
                          <button
                            type="button"
                            className="brand-icon-btn"
                            style={{ padding: '0.1rem', fontSize: '0.75rem' }}
                            title="Copy Master SKU"
                            onClick={(e) => {
                              e.stopPropagation();
                              if (p.masterSku) navigator.clipboard.writeText(p.masterSku);
                            }}
                          >
                            📋
                          </button>
                        </div>
                      </div>
                    </div>
                  </td>
                  <td>
                    <span className="brand-badge brand-badge-draft" style={{ background: '#eff6ff', color: '#1d4ed8' }}>
                      {p.categoryName || 'Consumer Tech'}
                    </span>
                  </td>
                  <td>
                    <span className="brand-badge brand-badge-locked" style={{ background: '#f0fdfa', color: '#0f766e' }}>
                      🔗 {variantCount} Registered ASINs
                    </span>
                  </td>
                  <td>
                    {/* Governance Status Badge */}
                    {p.governanceStatus === 'ACTIVE_LOCKED' && (
                      <span className="brand-badge brand-badge-locked">
                        &bull; Active Locked
                      </span>
                    )}
                    {p.governanceStatus === 'UNDER_REVIEW' && (
                      <span className="brand-badge brand-badge-review">
                        &bull; Under Review
                      </span>
                    )}
                    {p.governanceStatus === 'DRAFT' && (
                      <span className="brand-badge brand-badge-draft">
                        &bull; Draft Specification
                      </span>
                    )}
                  </td>
                  <td>
                    <div style={{ fontSize: '0.775rem', color: '#334155', fontWeight: 500 }}>
                      Oct 12, 2024
                    </div>
                    <div style={{ fontSize: '0.7rem', color: '#64748b' }}>
                      by Elena Vance (Owner)
                    </div>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {/* Bottom Section: Variant Lock Registry & Catalog Hierarchy Rules */}
      <div style={{ display: 'grid', gridTemplateColumns: 'minmax(0, 1.4fr) minmax(0, 1fr)', gap: '1.5rem', alignItems: 'start' }}>
        {/* Variant Lock Registry Hierarchy */}
        <div className="brand-card">
          <div className="brand-card-header">
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                <span style={{ fontSize: '1.1rem' }}>🧬</span>
                <h3 className="brand-card-title" style={{ margin: 0 }}>Variant Lock Registry Hierarchy</h3>
              </div>
              <p style={{ margin: '0.2rem 0 0', fontSize: '0.75rem', color: '#64748b' }}>
                Authoritative parent-child cluster for:{' '}
                <code style={{ color: '#0f172a', fontWeight: 700 }}>
                  {selectedProduct?.masterSku || 'MSKU-NEX-042'}
                </code>
              </p>
            </div>

            {/* DYNAMIC RBAC GUARD: Register New ASIN */}
            {permissions.canRegisterAsin ? (
              <button
                type="button"
                className="brand-btn brand-btn-outline"
                style={{ padding: '0.4rem 0.75rem', fontSize: '0.8rem' }}
                onClick={() => setShowVariantModal(true)}
              >
                <span>+ Register New ASIN</span>
              </button>
            ) : (
              <span style={{ fontSize: '0.725rem', color: '#94a3b8' }}>🔒 Read-only</span>
            )}
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.75rem' }}>
            {selectedProduct?.variants && selectedProduct.variants.length > 0 ? (
              selectedProduct.variants.map((v, i) => (
                <div
                  key={v.id || v.asin}
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    padding: '0.75rem 1rem',
                    backgroundColor: '#f8fafc',
                    borderRadius: 6,
                    border: '1px solid #e2e8f0',
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.75rem' }}>
                    <span style={{ color: '#047857', fontWeight: 700 }}>✓</span>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                        <code style={{ fontWeight: 700, color: '#0f172a' }}>{v.asin}</code>
                        <span style={{ fontSize: '0.725rem', color: '#64748b' }}>
                          UPC: 8401293817{29 + i}
                        </span>
                      </div>
                      <div style={{ fontSize: '0.775rem', color: '#334155', marginTop: '0.15rem' }}>
                        {v.variantName}
                      </div>
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
                    <span
                      className={`brand-badge ${
                        i === 2 ? 'brand-badge-draft' : 'brand-badge-locked'
                      }`}
                      style={{ fontSize: '0.675rem' }}
                    >
                      {i === 2 ? 'AMAZON SYNCED' : 'LOCKED BY REGISTRY'}
                    </span>
                  </div>
                </div>
              ))
            ) : (
              <div style={{ padding: '1.5rem', textAlign: 'center', color: '#64748b', background: '#f8fafc', borderRadius: 6 }}>
                No ASIN variants registered for this master template yet.
              </div>
            )}
          </div>
        </div>

        {/* Catalog Hierarchy Rules */}
        <div className="brand-card">
          <h3 className="brand-card-title" style={{ margin: '0 0 0.5rem' }}>
            <span>🛡️</span> Catalog Hierarchy Rules
          </h3>
          <p style={{ margin: '0 0 1rem', fontSize: '0.775rem', color: '#64748b', lineHeight: 1.45 }}>
            Master Product Templates govern the single source of truth. Under NexusBrand Tier 1 Registry enforcement:
          </p>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '0.85rem', fontSize: '0.8rem' }}>
            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.5rem' }}>
              <span style={{ color: '#047857', fontWeight: 700 }}>✓</span>
              <div>
                <strong style={{ color: '#0f172a' }}>Product Title &amp; Bullets</strong> cannot be overwritten by 3P resellers or dropshippers.
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.5rem' }}>
              <span style={{ color: '#047857', fontWeight: 700 }}>✓</span>
              <div>
                <strong style={{ color: '#0f172a' }}>Barcodes (UPC/GTIN)</strong> are permanently bound to your USPTO trademark registry ID.
              </div>
            </div>

            <div style={{ display: 'flex', alignItems: 'flex-start', gap: '0.5rem' }}>
              <span style={{ color: '#047857', fontWeight: 700 }}>✓</span>
              <div>
                <strong style={{ color: '#0f172a' }}>ASIN Hijack Protection:</strong> Automated bot triggers freeze and revert rogue detail page edits in &lt;60s.
              </div>
            </div>
          </div>

          <div
            style={{
              marginTop: '1.25rem',
              paddingTop: '0.75rem',
              borderTop: '1px solid #e2e8f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              fontSize: '0.725rem',
              color: '#64748b',
            }}
          >
            <span>Policy Version: 2024.3-AMZ-API</span>
            <a href="#audit" style={{ color: '#0284c7', fontWeight: 600, textDecoration: 'none' }} onClick={(e) => e.preventDefault()}>
              Audit Logs &rarr;
            </a>
          </div>
        </div>
      </div>

      {/* Modal: Create Master Product Template */}
      {showCreateModal && (
        <div className="brand-modal-backdrop">
          <div className="brand-modal">
            <div className="brand-modal-header">
              <span className="brand-modal-title">+ Create Master Product Template</span>
              <button
                type="button"
                className="brand-icon-btn"
                onClick={() => setShowCreateModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleCreateProduct}>
              <div className="brand-modal-body">
                <div className="brand-form-group">
                  <label className="brand-label">Product Title *</label>
                  <input
                    type="text"
                    className="brand-input"
                    placeholder="e.g. ApexPro Noise-Cancelling Headphones Wireless"
                    value={newTitle}
                    onChange={(e) => setNewTitle(e.target.value)}
                    required
                  />
                </div>

                <div className="brand-form-row">
                  <div className="brand-form-group">
                    <label className="brand-label">Master SKU *</label>
                    <input
                      type="text"
                      className="brand-input"
                      placeholder="e.g. MSKU-NEX-042"
                      value={newMasterSku}
                      onChange={(e) => setNewMasterSku(e.target.value)}
                      required
                    />
                  </div>

                  <div className="brand-form-group">
                    <label className="brand-label">Governance Status *</label>
                    <select
                      className="brand-input"
                      value={newGovernanceStatus}
                      onChange={(e) => setNewGovernanceStatus(e.target.value as GovernanceStatus)}
                    >
                      <option value="ACTIVE_LOCKED">Active Locked</option>
                      <option value="UNDER_REVIEW">Under Review</option>
                      <option value="DRAFT">Draft</option>
                    </select>
                  </div>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Catalog Category</label>
                  <select
                    className="brand-input"
                    value={newCategory}
                    onChange={(e) => setNewCategory(e.target.value)}
                  >
                    <option value="cat-audio">Audio &amp; Hi-Fi</option>
                    <option value="cat-power">Power Accessories</option>
                    <option value="cat-stands">Mobile Stands</option>
                  </select>
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Technical Description &amp; Specifications</label>
                  <textarea
                    rows={3}
                    className="brand-textarea"
                    placeholder="High-density wireless audio transmission with hybrid active noise cancellation..."
                    value={newDescription}
                    onChange={(e) => setNewDescription(e.target.value)}
                  />
                </div>
              </div>

              <div className="brand-modal-footer">
                <button
                  type="button"
                  className="brand-btn brand-btn-outline"
                  onClick={() => setShowCreateModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="brand-btn brand-btn-primary">
                  Save &amp; Lock Master Template
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Modal: Register ASIN Variant */}
      {showVariantModal && (
        <div className="brand-modal-backdrop">
          <div className="brand-modal" style={{ maxWidth: 480 }}>
            <div className="brand-modal-header">
              <span className="brand-modal-title">+ Register ASIN Variant</span>
              <button
                type="button"
                className="brand-icon-btn"
                onClick={() => setShowVariantModal(false)}
              >
                ✕
              </button>
            </div>
            <form onSubmit={handleCreateVariant}>
              <div className="brand-modal-body">
                <div className="brand-form-group">
                  <label className="brand-label">ASIN (Amazon Standard ID) *</label>
                  <input
                    type="text"
                    className="brand-input"
                    placeholder="e.g. B09V3HM1LR"
                    value={varAsin}
                    onChange={(e) => setVarAsin(e.target.value.toUpperCase())}
                    required
                  />
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Variant Name *</label>
                  <input
                    type="text"
                    className="brand-input"
                    placeholder="e.g. Midnight Black / Studio Pro Over-Ear"
                    value={varName}
                    onChange={(e) => setVarName(e.target.value)}
                    required
                  />
                </div>

                <div className="brand-form-group">
                  <label className="brand-label">Color / Finish</label>
                  <input
                    type="text"
                    className="brand-input"
                    placeholder="e.g. Midnight Black"
                    value={varColor}
                    onChange={(e) => setVarColor(e.target.value)}
                  />
                </div>
              </div>

              <div className="brand-modal-footer">
                <button
                  type="button"
                  className="brand-btn brand-btn-outline"
                  onClick={() => setShowVariantModal(false)}
                >
                  Cancel
                </button>
                <button type="submit" className="brand-btn brand-btn-primary">
                  Register &amp; Bind ASIN
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
