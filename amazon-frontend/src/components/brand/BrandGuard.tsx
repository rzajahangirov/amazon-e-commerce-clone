import React from 'react';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { useBrandAuth } from '../../context/BrandAuthContext';
import { AccessDeniedState } from '../common/AccessDeniedState';

export const BrandGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useCustomerAuth();
  const { hasBrand, loading } = useBrandAuth();

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
            borderTopColor: '#2563eb',
            borderRadius: '50%',
            animation: 'spin 0.8s linear infinite',
          }}
        />
        <p style={{ fontSize: '0.9rem', fontWeight: 500 }}>
          Verifying Brand Registry credentials &amp; enterprise membership...
        </p>
        <style>{`@keyframes spin { to { transform: rotate(360deg); } }`}</style>
      </div>
    );
  }

  if (!isAuthenticated || !hasBrand) {
    return <AccessDeniedState portalType="brand" />;
  }

  return <>{children}</>;
};
