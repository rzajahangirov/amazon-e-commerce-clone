import React from 'react';
import { Link } from 'react-router-dom';

export const StorefrontFooter: React.FC = () => {
  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <footer style={{ marginTop: 'auto', backgroundColor: '#232f3e', color: '#ffffff' }}>
      {/* Back to top bar */}
      <button
        type="button"
        onClick={scrollToTop}
        style={{
          width: '100%',
          backgroundColor: '#37475a',
          color: '#ffffff',
          border: 'none',
          padding: '14px',
          fontSize: '0.82rem',
          fontWeight: 600,
          cursor: 'pointer',
          transition: 'background-color 0.2s',
          textAlign: 'center',
        }}
        onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#485769')}
        onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#37475a')}
      >
        Back to top
      </button>

      {/* Main Footer Links */}
      <div
        style={{
          maxWidth: '1200px',
          margin: '0 auto',
          padding: '40px 24px',
          display: 'grid',
          gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))',
          gap: '32px',
          fontSize: '0.82rem',
        }}
      >
        <div>
          <h4 style={{ fontSize: '0.94rem', fontWeight: 700, marginBottom: '14px', color: '#ffffff' }}>
            Get to Know Us
          </h4>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <li><span style={{ color: '#dddddd' }}>Careers</span></li>
            <li><span style={{ color: '#dddddd' }}>About Amazon Enterprise</span></li>
            <li><span style={{ color: '#dddddd' }}>Sustainability</span></li>
            <li><span style={{ color: '#dddddd' }}>Press Center</span></li>
          </ul>
        </div>

        <div>
          <h4 style={{ fontSize: '0.94rem', fontWeight: 700, marginBottom: '14px', color: '#ffffff' }}>
            Make Money with Us
          </h4>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <li><Link to="/seller" style={{ color: '#dddddd', textDecoration: 'none' }}>Sell products on Amazon</Link></li>
            <li><Link to="/brand" style={{ color: '#dddddd', textDecoration: 'none' }}>Protect your Brand with Brand Registry</Link></li>
            <li><span style={{ color: '#dddddd' }}>Become an Affiliate</span></li>
            <li><span style={{ color: '#dddddd' }}>Fulfillment by Amazon</span></li>
          </ul>
        </div>

        <div>
          <h4 style={{ fontSize: '0.94rem', fontWeight: 700, marginBottom: '14px', color: '#ffffff' }}>
            Enterprise Payments
          </h4>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <li><span style={{ color: '#dddddd' }}>Amazon Business Card</span></li>
            <li><span style={{ color: '#dddddd' }}>Corporate Invoicing</span></li>
            <li><span style={{ color: '#dddddd' }}>Reload Your Balance</span></li>
            <li><span style={{ color: '#dddddd' }}>Amazon Currency Converter</span></li>
          </ul>
        </div>

        <div>
          <h4 style={{ fontSize: '0.94rem', fontWeight: 700, marginBottom: '14px', color: '#ffffff' }}>
            Let Us Help You
          </h4>
          <ul style={{ listStyle: 'none', padding: 0, margin: 0, display: 'flex', flexDirection: 'column', gap: '8px' }}>
            <li><Link to="/orders" style={{ color: '#dddddd', textDecoration: 'none' }}>Your Account & Orders</Link></li>
            <li><Link to="/wishlist" style={{ color: '#dddddd', textDecoration: 'none' }}>Your Wish List</Link></li>
            <li><span style={{ color: '#dddddd' }}>Shipping Rates & Policies</span></li>
            <li><span style={{ color: '#dddddd' }}>Customer Service Support</span></li>
          </ul>
        </div>
      </div>

      {/* Bottom Legal bar */}
      <div
        style={{
          borderTop: '1px solid #3a4553',
          backgroundColor: '#131a22',
          padding: '24px 16px',
          textAlign: 'center',
          fontSize: '0.74rem',
          color: '#cccccc',
        }}
      >
        <div style={{ marginBottom: '8px' }}>
          <span style={{ fontSize: '1.2rem', fontWeight: 800, color: '#ffffff' }}>
            amazon<span style={{ color: '#ff9900' }}>.enterprise</span>
          </span>
        </div>
        <div>© 2026 Amazon-Clone Enterprise Platform, Inc. or its affiliates. All rights reserved.</div>
      </div>
    </footer>
  );
};
