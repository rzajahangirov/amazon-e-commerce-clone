import React from 'react';
import { NavLink, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import './BrandLayout.css';

export const BrandLayout: React.FC = () => {
  const { permissions, profile, roleTitle } = useBrandRBAC();
  const { user, logout } = useCustomerAuth();
  const location = useLocation();
  const navigate = useNavigate();

  // Dynamic user data — prefer brand profile owner, fall back to logged-in customer
  const ownerName = profile?.brand?.ownerName || user?.fullName || user?.email?.split('@')?.[0] || 'Brand Member';
  const ownerInitials = ownerName
    .split(' ')
    .map((n: string) => n[0])
    .join('')
    .slice(0, 2)
    .toUpperCase();

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  // Compute breadcrumb title based on path
  const getBreadcrumbTitle = () => {
    const path = location.pathname;
    if (path.includes('analytics')) return 'Brand Analytics & Marketplace Overview';
    if (path.includes('catalog')) return 'Brand Catalog & ASIN Governance';
    if (path.includes('team')) return 'Brand Team & Access Management';
    if (path.includes('marketing')) return 'Brand Marketing Posts & Social Feeds';
    if (path.includes('profile')) return 'Brand Profile & Trademark Governance';
    if (path.includes('categories')) return 'Category Proposals & Taxonomy Requests';
    return 'Brand Overview';
  };

  return (
    <div className="brand-shell">
      {/* Sidebar Navigation */}
      <aside className="brand-sidebar">
        <div className="brand-sidebar-header">
          <div className="brand-logo-icon">
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
              <path d="M9 12l2 2 4-4" />
            </svg>
          </div>
          <div className="brand-logo-text">
            <span className="brand-logo-title">NexusBrand</span>
            <span className="brand-logo-subtitle">ENTERPRISE REGISTRY</span>
          </div>
        </div>

        {/* Entity Switcher Card */}
        <div className="brand-entity-card">
          <div className="brand-entity-info">
            <div className="brand-entity-name">
              {profile?.brand?.name || 'No Brand Selected'}
              {profile?.brand?.name && <span className="brand-verified-check">✓</span>}
            </div>
            <div className="brand-entity-reg">
              {profile?.brand?.trademarkRegistrationNumber
                ? `USPTO Reg #${profile.brand.trademarkRegistrationNumber}`
                : 'Trademark pending verification'}
            </div>
          </div>
          <div className="brand-entity-chevrons">
            <span>▴</span>
            <span>▾</span>
          </div>
        </div>

        {/* Navigation Items */}
        <nav className="brand-nav">
          <NavLink
            to="/brand/analytics"
            className={({ isActive }) => (isActive ? 'brand-nav-link active' : 'brand-nav-link')}
          >
            <span className="brand-nav-icon">📊</span>
            <span>Brand Analytics</span>
          </NavLink>

          <NavLink
            to="/brand/catalog"
            className={({ isActive }) => (isActive ? 'brand-nav-link active' : 'brand-nav-link')}
          >
            <span className="brand-nav-icon">📦</span>
            <span>Catalog &amp; ASINs</span>
          </NavLink>

          {/* DYNAMIC RBAC GUARD: Hide Team & Access if NOT BRAND_OWNER or BRAND_SUPER_ADMIN */}
          {permissions.canViewTeam && (
            <NavLink
              to="/brand/team"
              className={({ isActive }) => (isActive ? 'brand-nav-link active' : 'brand-nav-link')}
            >
              <span className="brand-nav-icon">👥</span>
              <span>Team &amp; Access</span>
            </NavLink>
          )}

          <NavLink
            to="/brand/marketing"
            className={({ isActive }) => (isActive ? 'brand-nav-link active' : 'brand-nav-link')}
          >
            <span className="brand-nav-icon">📢</span>
            <span>Marketing &amp; Posts</span>
          </NavLink>

          <NavLink
            to="/brand/profile"
            className={({ isActive }) => (isActive ? 'brand-nav-link active' : 'brand-nav-link')}
          >
            <span className="brand-nav-icon">🏛️</span>
            <span>Brand Profile</span>
          </NavLink>

          <NavLink
            to="/brand/categories"
            className={({ isActive }) => (isActive ? 'brand-nav-link active' : 'brand-nav-link')}
          >
            <span className="brand-nav-icon">🏷️</span>
            <span>Category Proposals</span>
          </NavLink>
        </nav>

        {/* Sidebar Footer */}
        <div className="brand-sidebar-footer">
          <div className="brand-footer-status">
            <span className="brand-status-dot" />
            <span>Marketplace Unified</span>
          </div>
          <div className="brand-footer-desc">NA &amp; EU Region Synced</div>
        </div>
      </aside>

      {/* Main Content View */}
      <div className="brand-main">
        {/* Topbar Header */}
        <header className="brand-topbar">
          <div className="brand-topbar-left">
            <div className="brand-breadcrumb">
              <span>Portal</span>
              <span>›</span>
              <span className="brand-breadcrumb-curr">{getBreadcrumbTitle()}</span>
            </div>

            <div className="brand-search-box">
              <span className="brand-search-icon">🔍</span>
              <input
                type="text"
                className="brand-search-input"
                placeholder="Search ASIN, trademark, violation..."
              />
              <kbd className="brand-search-kbd">Ctrl K</kbd>
            </div>
          </div>

          <div className="brand-topbar-right">
            {/* Automatically Detected Brand Role Badge */}
            <div className="brand-role-badge-wrap" title={`Detected Role: ${roleTitle}`}>
              <span className="brand-role-badge">
                <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                  <path d="M9 12l2 2 4-4" />
                </svg>
                {roleTitle}
              </span>
            </div>

            <div className="brand-topbar-market">
              <span>🌐 US Marketplace</span>
            </div>

            {/* Current Brand User Profile */}
            <div className="brand-user-card" title={`Logged in as ${roleTitle}`}>
              <div className="brand-user-avatar">
                {ownerInitials}
              </div>
              <div className="brand-user-details">
                <span className="brand-user-name">
                  {ownerName}
                </span>
                <span className="brand-user-role">{roleTitle}</span>
              </div>
              <button
                type="button"
                onClick={handleLogout}
                title="Sign out of Brand Portal"
                style={{
                  background: 'none',
                  border: '1px solid rgba(255,255,255,0.15)',
                  color: '#94a3b8',
                  borderRadius: '6px',
                  padding: '4px 10px',
                  fontSize: '0.72rem',
                  fontWeight: 600,
                  cursor: 'pointer',
                  marginLeft: '10px',
                  transition: 'all 0.15s ease',
                }}
                onMouseEnter={(e) => {
                  e.currentTarget.style.color = '#f87171';
                  e.currentTarget.style.borderColor = '#f87171';
                }}
                onMouseLeave={(e) => {
                  e.currentTarget.style.color = '#94a3b8';
                  e.currentTarget.style.borderColor = 'rgba(255,255,255,0.15)';
                }}
              >
                Sign Out
              </button>
            </div>
          </div>
        </header>

        {/* Content Body */}
        <main className="brand-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
