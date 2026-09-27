import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { userInitials as formatUserInitials } from '../../utils/format';
import './SellerLayout.css';

interface NavItem {
  to: string;
  label: string;
  end?: boolean;
}

interface NavSection {
  label: string;
  items: NavItem[];
}

const navSections: NavSection[] = [
  {
    label: 'Overview',
    items: [
      { to: '/seller/analytics', label: 'Analytics & Overview', end: true },
    ],
  },
  {
    label: 'Inventory',
    items: [
      { to: '/seller/listings', label: 'Offers & ASIN Catalog' },
    ],
  },
  {
    label: 'Fulfillment',
    items: [
      { to: '/seller/orders', label: 'Order Fulfillment' },
    ],
  },
  {
    label: 'Settings',
    items: [
      { to: '/seller/profile', label: 'Store Profile & Settlement' },
    ],
  },
];

export function SellerLayout() {
  const { user, logout } = useCustomerAuth();
  const navigate = useNavigate();

  // Dynamic user data from auth context
  const displayName = user?.fullName || user?.email?.split('@')?.[0] || 'Merchant';
  const displayInitials = formatUserInitials(displayName);
  const userRole = user?.role === 'ROLE_ADMIN' ? 'Platform Admin' : 'Merchant Ops';

  const handleLogout = () => {
    logout();
    navigate('/');
  };

  return (
    <div className="seller-shell">
      <aside className="seller-sidebar">
        <div className="seller-brand">
          <span className="seller-brand-mark">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M12 2L2 7l10 5 10-5-10-5z" />
              <path d="M2 17l10 5 10-5" />
              <path d="M2 12l10 5 10-5" />
            </svg>
          </span>
          <div>
            <strong>ApexMerchant</strong>
            <small>PRO EDITION <span className="seller-edition-badge">Enterprise</span></small>
          </div>
        </div>

        <div className="seller-tenant-card">
          <div className="seller-tenant-flag">🇺🇸</div>
          <div className="seller-tenant-info">
            <strong>Apex Retail US (Default)</strong>
            <small>ID: ATVP0KIKX0DER</small>
          </div>
          <span className="seller-tenant-chevron">⌄</span>
        </div>

        {navSections.map((section) => (
          <div key={section.label} className="seller-nav-section">
            <p className="seller-nav-label">{section.label.toUpperCase()}</p>
            <nav className="seller-nav">
              {section.items.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  end={item.end}
                  className={({ isActive }) =>
                    isActive ? 'seller-nav-link active' : 'seller-nav-link'
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>
        ))}

        <footer className="seller-sidebar-footer">
          <span className="seller-status-dot" />
          API: Online 99.98% <span className="seller-footer-sep">•</span> 2m ago
          <div className="seller-version">v4.18.2 Enterprise</div>
        </footer>
      </aside>

      <div className="seller-main">
        <header className="seller-topbar">
          <div className="seller-search-wrap">
            <span className="seller-search-icon">🔍</span>
            <input
              className="seller-search"
              type="search"
              placeholder="Search ASIN, SKU, Order ID, or Product na..."
              aria-label="Seller global search"
            />
            <kbd className="seller-shortcut">⌘K</kbd>
          </div>
          <div className="seller-topbar-badges">
            <span className="seller-verified-badge">✓ Verified Merchant (EIN Validated)</span>
            <span className="seller-market-badge">US Marketplace (USD $)</span>
          </div>
          <div className="seller-topbar-user">
            <span className="seller-user-avatar">{displayInitials}</span>
            <div className="seller-user-info">
              <strong>{displayName}</strong>
              <small>{userRole}</small>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              className="seller-logout-btn"
              title="Sign out of Seller Portal"
              style={{
                background: 'none',
                border: '1px solid rgba(255,255,255,0.2)',
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
                e.currentTarget.style.borderColor = 'rgba(255,255,255,0.2)';
              }}
            >
              Sign Out
            </button>
          </div>
        </header>

        <main className="seller-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
