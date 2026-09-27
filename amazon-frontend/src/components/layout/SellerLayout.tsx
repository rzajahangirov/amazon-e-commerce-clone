import { NavLink, Outlet } from 'react-router-dom';
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
            <span className="seller-user-avatar">SJ</span>
            <div className="seller-user-info">
              <strong>Sarah Jenkins</strong>
              <small>Lead Merchant Ops</small>
            </div>
          </div>
        </header>

        <main className="seller-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
