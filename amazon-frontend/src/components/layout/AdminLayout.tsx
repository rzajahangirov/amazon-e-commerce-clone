import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import './AdminLayout.css';

const navItems = [
  { to: '/admin/dashboard', label: 'Dashboard', end: true },
  { to: '/admin/brand-registry', label: 'Brand Registry', badge: 'pendingBrands' },
  { to: '/admin/users-sellers', label: 'Users & Sellers' },
  { to: '/admin/categories', label: 'Category Governance', badge: 'pendingCategories' },
  { to: '/admin/orders', label: 'Global Orders' },
];

export function AdminLayout() {
  const { user, logout } = useCustomerAuth();
  const navigate = useNavigate();

  const displayName = user?.fullName || user?.email?.split('@')?.[0] || 'Administrator';
  const userRole = user?.role === 'ROLE_ADMIN' ? 'SUPER ADMIN' : (user?.role || 'ADMIN');

  const handleLogout = () => {
    logout();
    navigate('/');
  };

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
              <span className="admin-user-name">{displayName}</span>
              <small>{userRole}</small>
            </div>
            <button
              type="button"
              onClick={handleLogout}
              title="Sign out of Admin Console"
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
        <main className="admin-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
