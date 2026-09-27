import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { storefrontApi } from '../../api/storefrontApi';
import type { Category } from '../../api/storefrontTypes';

export const StorefrontHeader: React.FC = () => {
  const { user, isAuthenticated, logout, cartCount, openAuthModal } = useCustomerAuth();
  const [categories, setCategories] = useState<Category[]>([]);
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();

  const urlQuery = searchParams.get('query') || '';
  const urlCategory = searchParams.get('categoryId') || '';

  const [query, setQuery] = useState(urlQuery);
  const [selectedCategory, setSelectedCategory] = useState(urlCategory);
  const [prevUrlState, setPrevUrlState] = useState({ query: urlQuery, categoryId: urlCategory });
  const [accountMenuOpen, setAccountMenuOpen] = useState(false);

  if (prevUrlState.query !== urlQuery || prevUrlState.categoryId !== urlCategory) {
    setPrevUrlState({ query: urlQuery, categoryId: urlCategory });
    setQuery(urlQuery);
    setSelectedCategory(urlCategory);
  }

  useEffect(() => {
    let ignore = false;
    storefrontApi
      .getRootCategories()
      .then((data) => { if (!ignore) setCategories(data); })
      .catch(() => { if (!ignore) setCategories([]); });
    return () => { ignore = true; };
  }, []);

  const handleSearchSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const params = new URLSearchParams();
    if (query.trim()) params.set('query', query.trim());
    if (selectedCategory) params.set('categoryId', selectedCategory);
    navigate(`/products?${params.toString()}`);
  };

  return (
    <header
      style={{
        backgroundColor: '#131921',
        color: '#ffffff',
        padding: '8px 16px',
        display: 'flex',
        alignItems: 'center',
        gap: '14px',
        fontSize: '0.85rem',
        position: 'sticky',
        top: 0,
        zIndex: 1000,
        boxShadow: '0 2px 4px rgba(0,0,0,0.15)',
      }}
    >
      {/* Brand Logo */}
      <Link
        to="/"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '2px',
          textDecoration: 'none',
          color: '#ffffff',
          padding: '4px 6px',
          borderRadius: '2px',
          border: '1px solid transparent',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#ffffff')}
        onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
      >
        <span style={{ fontSize: '1.4rem', fontWeight: 800, letterSpacing: '-0.5px' }}>
          amazon
        </span>
        <span
          style={{
            color: '#ff9900',
            fontSize: '0.72rem',
            fontWeight: 700,
            textTransform: 'uppercase',
            marginLeft: '2px',
            backgroundColor: 'rgba(255,153,0,0.15)',
            padding: '2px 5px',
            borderRadius: '3px',
            border: '1px solid rgba(255,153,0,0.4)',
          }}
        >
          Pro
        </span>
      </Link>

      {/* Deliver To Selector */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          padding: '4px 8px',
          borderRadius: '2px',
          border: '1px solid transparent',
          cursor: 'pointer',
          whiteSpace: 'nowrap',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#ffffff')}
        onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#ffffff" strokeWidth="2">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z" />
          <circle cx="12" cy="9" r="2.5" />
        </svg>
        <div style={{ lineHeight: 1.15 }}>
          <div style={{ fontSize: '0.68rem', color: '#cccccc' }}>Deliver to</div>
          <div style={{ fontSize: '0.8rem', fontWeight: 700, color: '#ffffff' }}>Enterprise HQ</div>
        </div>
      </div>

      {/* Search Bar */}
      <form
        onSubmit={handleSearchSubmit}
        style={{
          display: 'flex',
          flex: 1,
          height: '40px',
          borderRadius: '4px',
          overflow: 'hidden',
          backgroundColor: '#ffffff',
          boxShadow: '0 1px 2px rgba(0,0,0,0.2)',
        }}
      >
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          style={{
            backgroundColor: '#f3f3f3',
            color: '#555555',
            border: 'none',
            borderRight: '1px solid #cdcdcd',
            padding: '0 8px',
            fontSize: '0.78rem',
            cursor: 'pointer',
            maxWidth: '150px',
            outline: 'none',
          }}
        >
          <option value="">All Departments</option>
          {categories.map((c) => (
            <option key={c.id} value={c.id}>
              {c.name}
            </option>
          ))}
        </select>

        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search Amazon Enterprise products, QD-OLED monitors, accessories..."
          style={{
            flex: 1,
            border: 'none',
            padding: '0 12px',
            fontSize: '0.9rem',
            color: '#0f1111',
            outline: 'none',
          }}
        />

        <button
          type="submit"
          aria-label="Search"
          style={{
            backgroundColor: '#febd69',
            border: 'none',
            width: '46px',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            cursor: 'pointer',
            transition: 'background-color 0.15s ease',
          }}
          onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#f3a847')}
          onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#febd69')}
        >
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#131921" strokeWidth="2.5">
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
        </button>
      </form>

      {/* Language / Region */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: '4px',
          padding: '6px 8px',
          borderRadius: '2px',
          border: '1px solid transparent',
          cursor: 'pointer',
          fontWeight: 700,
        }}
        onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#ffffff')}
        onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
      >
        <span>🇺🇸</span>
        <span style={{ fontSize: '0.78rem' }}>EN</span>
      </div>

      {/* Account & Lists */}
      <div
        style={{ position: 'relative' }}
        onMouseEnter={() => setAccountMenuOpen(true)}
        onMouseLeave={() => setAccountMenuOpen(false)}
      >
        <div
          onClick={() => {
            if (!isAuthenticated) openAuthModal('signin');
          }}
          style={{
            padding: '4px 8px',
            borderRadius: '2px',
            border: '1px solid transparent',
            cursor: 'pointer',
            lineHeight: 1.15,
          }}
          onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#ffffff')}
          onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
        >
          <div style={{ fontSize: '0.68rem', color: '#cccccc' }}>
            {isAuthenticated
              ? `Hello, ${user?.fullName?.split(' ')?.[0] || user?.email?.split('@')?.[0] || 'Customer'}`
              : 'Hello, sign in'}
          </div>
          <div style={{ fontSize: '0.82rem', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '3px' }}>
            Account & Lists
            <svg width="10" height="10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="3">
              <path d="M6 9l6 6 6-6" />
            </svg>
          </div>
        </div>

        {/* Dropdown Menu */}
        {accountMenuOpen && (
          <div
            style={{
              position: 'absolute',
              top: '100%',
              right: 0,
              width: '260px',
              backgroundColor: '#ffffff',
              color: '#0f1111',
              borderRadius: '4px',
              boxShadow: '0 8px 24px rgba(0,0,0,0.25)',
              padding: '16px',
              zIndex: 1100,
              fontSize: '0.84rem',
            }}
          >
            {!isAuthenticated ? (
              <div style={{ textAlign: 'center', borderBottom: '1px solid #e7e7e7', paddingBottom: '14px' }}>
                <button
                  type="button"
                  onClick={() => {
                    setAccountMenuOpen(false);
                    openAuthModal('signin');
                  }}
                  style={{
                    backgroundColor: '#ffd814',
                    border: '1px solid #fcd200',
                    borderRadius: '4px',
                    padding: '8px 24px',
                    fontWeight: 600,
                    cursor: 'pointer',
                    width: '100%',
                    marginBottom: '8px',
                  }}
                >
                  Sign in
                </button>
                <div style={{ fontSize: '0.74rem' }}>
                  New customer?{' '}
                  <span
                    onClick={() => {
                      setAccountMenuOpen(false);
                      openAuthModal('register');
                    }}
                    style={{ color: '#007185', cursor: 'pointer', textDecoration: 'underline' }}
                  >
                    Start here.
                  </span>
                </div>
              </div>
            ) : (
              <div style={{ borderBottom: '1px solid #e7e7e7', paddingBottom: '10px', marginBottom: '10px' }}>
                <div style={{ fontWeight: 700, fontSize: '0.9rem' }}>{user?.fullName}</div>
                <div style={{ fontSize: '0.75rem', color: '#565959' }}>{user?.email}</div>
              </div>
            )}

            <div style={{ marginTop: '8px' }}>
              <div style={{ fontWeight: 700, fontSize: '0.8rem', color: '#333333', marginBottom: '6px' }}>
                Your Account
              </div>
              <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '6px' }}>
                <li>
                  <Link
                    to="/orders"
                    onClick={() => setAccountMenuOpen(false)}
                    style={{ color: '#007185', textDecoration: 'none', display: 'block', padding: '3px 0' }}
                  >
                    Your Orders
                  </Link>
                </li>
                <li>
                  <Link
                    to="/wishlist"
                    onClick={() => setAccountMenuOpen(false)}
                    style={{ color: '#007185', textDecoration: 'none', display: 'block', padding: '3px 0' }}
                  >
                    Your Wish List
                  </Link>
                </li>
              </ul>
            </div>

            {/* Quick Portals Switcher */}
            <div style={{ marginTop: '14px', borderTop: '1px solid #e7e7e7', paddingTop: '10px' }}>
              <div style={{ fontWeight: 700, fontSize: '0.75rem', color: '#777777', textTransform: 'uppercase', marginBottom: '6px' }}>
                Enterprise Portals
              </div>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
                <Link to="/brand" style={{ color: '#007185', textDecoration: 'none', fontSize: '0.78rem' }}>
                  Brand Registry Portal →
                </Link>
                <Link to="/seller" style={{ color: '#007185', textDecoration: 'none', fontSize: '0.78rem' }}>
                  Seller Merchant Hub →
                </Link>
                <Link to="/admin" style={{ color: '#007185', textDecoration: 'none', fontSize: '0.78rem' }}>
                  Platform Admin Console →
                </Link>
              </div>
            </div>

            {isAuthenticated && (
              <div style={{ marginTop: '14px', borderTop: '1px solid #e7e7e7', paddingTop: '10px' }}>
                <button
                  type="button"
                  onClick={() => {
                    logout();
                    setAccountMenuOpen(false);
                  }}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: '#cc0c39',
                    fontWeight: 600,
                    cursor: 'pointer',
                    padding: 0,
                  }}
                >
                  Sign Out
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Returns & Orders */}
      <Link
        to="/orders"
        style={{
          textDecoration: 'none',
          color: '#ffffff',
          padding: '4px 8px',
          borderRadius: '2px',
          border: '1px solid transparent',
          lineHeight: 1.15,
          whiteSpace: 'nowrap',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#ffffff')}
        onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
      >
        <div style={{ fontSize: '0.68rem', color: '#cccccc' }}>Returns</div>
        <div style={{ fontSize: '0.82rem', fontWeight: 700 }}>& Orders</div>
      </Link>

      {/* Cart Icon & Badge */}
      <Link
        to="/cart"
        style={{
          textDecoration: 'none',
          color: '#ffffff',
          display: 'flex',
          alignItems: 'center',
          gap: '6px',
          padding: '4px 8px',
          borderRadius: '2px',
          border: '1px solid transparent',
          position: 'relative',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.borderColor = '#ffffff')}
        onMouseLeave={(e) => (e.currentTarget.style.borderColor = 'transparent')}
      >
        <div style={{ position: 'relative' }}>
          <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="#ffffff" strokeWidth="2">
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
          </svg>
          <span
            style={{
              position: 'absolute',
              top: '-2px',
              left: '14px',
              color: '#ff9900',
              fontWeight: 800,
              fontSize: '0.85rem',
            }}
          >
            {cartCount}
          </span>
        </div>
        <span style={{ fontSize: '0.85rem', fontWeight: 700, alignSelf: 'flex-end', paddingBottom: '2px' }}>
          Cart
        </span>
      </Link>
    </header>
  );
};
