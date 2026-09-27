import React from 'react';
import { Navigate } from 'react-router-dom';
import type { BrandRole } from '../../api/brandTypes';
import { useBrandRBAC } from '../../hooks/useBrandRBAC';

interface BrandRouteGuardProps {
  allowedRoles: BrandRole[];
  children: React.ReactNode;
  fallbackPath?: string;
}

export const BrandRouteGuard: React.FC<BrandRouteGuardProps> = ({
  allowedRoles,
  children,
  fallbackPath = '/brand/analytics',
}) => {
  const { activeRole, loading } = useBrandRBAC();

  if (loading) {
    return (
      <div style={{ padding: '3rem', textAlign: 'center', color: '#64748b' }}>
        <p>Verifying Enterprise Registry cryptographic credentials &amp; RBAC clearance...</p>
      </div>
    );
  }

  if (!activeRole || !allowedRoles.includes(activeRole)) {
    return <Navigate to={fallbackPath} replace />;
  }

  return <>{children}</>;
};
