import { NavLink, Outlet } from 'react-router-dom';
import './AdminLayout.css';

const navItems = [
  { to: '/admin/dashboard', label: 'Dashboard', end: true },
  { to: '/admin/brand-registry', label: 'Brand Registry', badge: 'pendingBrands' },
  { to: '/admin/users-sellers', label: 'Users & Sellers' },
  { to: '/admin/categories', label: 'Category Governance', badge: 'pendingCategories' },
  { to: '/admin/orders', label: 'Global Orders' },
];

export function AdminLayout() {
  return (
    <div className="admin-shell">
      <aside className="admin-sidebar">
        <div className="admin-brand">
          <span className="admin-brand-mark">A</span>
          <div>
            <strong>Amazon Seller Admin</strong>
            <small>Global Console</small>
          </div>
        </div>
        <p className="admin-sidebar-label">Operations Core</p>
        <nav className="admin-nav">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                isActive ? 'admin-nav-link active' : 'admin-nav-link'
              }
            >
              {item.label}
              {item.badge === 'pendingBrands' && (
                <span className="nav-badge" data-source="analytics">
                  —
                </span>
              )}
              {item.badge === 'pendingCategories' && (
                <span className="nav-badge muted" data-source="categories">
                  —
                </span>
              )}
            </NavLink>
          ))}
        </nav>
        <footer className="admin-sidebar-footer">
          <span className="status-dot" />
          API v4.12 • ACTIVE
        </footer>
      </aside>
      <div className="admin-main">
        <header className="admin-topbar">
          <div className="admin-breadcrumb">Platform / Console</div>
          <input
            className="admin-search"
            type="search"
            placeholder="Search ASIN, merchant, order ID..."
            aria-label="Global search"
          />
          <div className="admin-topbar-meta">
            <span className="env-pill">PRODUCTION • US-EAST</span>
            <div className="admin-user">
              <span className="admin-user-name">Platform Supervisor</span>
              <small>SUPER ADMIN</small>
            </div>
          </div>
        </header>
        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
