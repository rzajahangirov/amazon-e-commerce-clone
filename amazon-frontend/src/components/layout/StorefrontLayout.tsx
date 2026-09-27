import React from 'react';
import { Outlet } from 'react-router-dom';
import { StorefrontHeader } from '../storefront/StorefrontHeader';
import { StorefrontSubNav } from '../storefront/StorefrontSubNav';
import { StorefrontFooter } from '../storefront/StorefrontFooter';
import { AuthModal } from '../storefront/AuthModal';
import './StorefrontLayout.css';

export const StorefrontLayout: React.FC = () => {
  return (
    <div className="storefront-root">
      <StorefrontHeader />
      <StorefrontSubNav />
      <main className="storefront-main">
        <Outlet />
      </main>
      <StorefrontFooter />
      <AuthModal />
    </div>
  );
};
