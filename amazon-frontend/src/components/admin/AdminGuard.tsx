import React from 'react';
import { useCustomerAuth } from '../../context/CustomerAuthContext';
import { AccessDeniedState } from '../common/AccessDeniedState';

export const AdminGuard: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated, user } = useCustomerAuth();

  if (!isAuthenticated || user?.role !== 'ROLE_ADMIN') {
    return <AccessDeniedState portalType="admin" />;
  }

  return <>{children}</>;
};
