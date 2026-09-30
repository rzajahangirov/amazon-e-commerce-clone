import React from 'react';
import { Link } from 'react-router-dom';

export const StorefrontSubNav: React.FC = () => {
  return (
    <nav
      className="storefront-subnav"
      style={{
        backgroundColor: '#232f3e',
        color: '#ffffff',
        padding: '0 16px',
        display: 'flex',
        alignItems: 'center',
        gap: '18px',
        fontSize: '0.84rem',
        overflowX: 'auto',
        whiteSpace: 'nowrap',
        scrollbarWidth: 'none',
      }}
    >
      <Link
        to="/products"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          color: '#ffffff',
          textDecoration: 'none',
          padding: '8px 6px',
          fontWeight: 700,
        }}
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5">
          <line x1="3" y1="12" x2="21" y2="12" />
          <line x1="3" y1="6" x2="21" y2="6" />
          <line x1="3" y1="18" x2="21" y2="18" />
        </svg>
        All Products
      </Link>

      <Link
        to="/products?badge=DEAL_OF_THE_DAY"
        style={{ color: '#ffffff', textDecoration: 'none', padding: '8px 4px', fontWeight: 500 }}
      >
        Today's Deals
      </Link>

      <Link
        to="/products?sortBy=BEST_SELLERS"
        style={{ color: '#ffffff', textDecoration: 'none', padding: '8px 4px', fontWeight: 500 }}
      >
        Best Sellers
      </Link>

      <Link
        to="/products?query=monitor"
        style={{ color: '#ffffff', textDecoration: 'none', padding: '8px 4px', fontWeight: 500 }}
      >
        Monitors & Displays
      </Link>

      <Link
        to="/products?query=cable"
        style={{ color: '#ffffff', textDecoration: 'none', padding: '8px 4px', fontWeight: 500 }}
      >
        Enterprise Accessories
      </Link>

      <Link
        to="/wishlist"
        style={{ color: '#ffffff', textDecoration: 'none', padding: '8px 4px', fontWeight: 500 }}
      >
        Your Wish List
      </Link>

      <div style={{ marginLeft: 'auto', display: 'flex', alignItems: 'center', gap: '12px' }}>
        <Link
          to="/brand"
          style={{
            color: '#febd69',
            textDecoration: 'none',
            fontSize: '0.78rem',
            fontWeight: 700,
            display: 'flex',
            alignItems: 'center',
            gap: '4px',
            backgroundColor: 'rgba(254, 189, 105, 0.1)',
            padding: '4px 8px',
            borderRadius: '4px',
            border: '1px solid rgba(254, 189, 105, 0.25)',
          }}
        >
          Brand Registry
        </Link>
        <Link
          to="/seller"
          style={{
            color: '#a0aec0',
            textDecoration: 'none',
            fontSize: '0.78rem',
            fontWeight: 600,
          }}
        >
          Seller Hub
        </Link>
        <Link
          to="/admin"
          style={{
            color: '#a0aec0',
            textDecoration: 'none',
            fontSize: '0.78rem',
            fontWeight: 600,
          }}
        >
          Admin Console
        </Link>
      </div>
    </nav>
  );
};
