import React from 'react';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { useSellerProfile } from '../../hooks/useSellerProfile';
import { AccessDeniedState } from '../common/AccessDeniedState';

export const SellerGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, user } = useCustomerAuth();
  const { data, loading, error } = useSellerProfile();

  if (loading) {
    return (
      <div
        style={{
          minHeight: '60vh',
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          gap: '1rem',
          color: '#64748b',
        }}
      >
        <div
          style={{
            width: '40px',
            height: '40px',
            border: '3px solid #e2e8f0',
            borderTopColor: '#0284c7',
            borderRadius: '50%',
            animation: 'spin 0.8s linear infinite',
          }}
        />
        <p style={{ fontSize: '0.9rem', fontWeight: 500 }}>
          Verifying ApexMerchant seller credentials &amp; store profile...
        </p>
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  const isSellerRole = user?.role === 'ROLE_SELLER' || user?.role === 'ROLE_ADMIN';
  const hasSellerAccount = Boolean(data && data.id) || isSellerRole;

  if (!isAuthenticated || !hasSellerAccount || error) {
    return <AccessDeniedState portalType="seller" />;
  }

  return <>{children}</>;
};
