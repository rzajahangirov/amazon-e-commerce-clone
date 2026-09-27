import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminLayout } from './components/layout/AdminLayout';
import { SellerLayout } from './components/layout/SellerLayout';
import { BrandLayout } from './components/layout/BrandLayout';
import { BrandAuthProvider } from './context/BrandAuthContext';
import { BrandRouteGuard } from './components/brand/BrandRouteGuard';

// Admin Pages
import { AdminCategoriesPage } from './pages/AdminCategoriesPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { AdminOrdersPage } from './pages/AdminOrdersPage';
import { AdminPlaceholderPage } from './pages/AdminPlaceholderPage';
import { AdminUsersSellersPage } from './pages/AdminUsersSellersPage';

// Seller Pages
import { SellerAnalyticsPage } from './pages/SellerAnalyticsPage';
import { SellerListingsPage } from './pages/SellerListingsPage';
import { SellerOrdersPage } from './pages/SellerOrdersPage';
import { SellerProfilePage } from './pages/SellerProfilePage';

// Brand Registry Pages
import { BrandAnalyticsPage } from './pages/brand/BrandAnalyticsPage';
import { BrandCatalogPage } from './pages/brand/BrandCatalogPage';
import { BrandTeamPage } from './pages/brand/BrandTeamPage';
import { BrandMarketingPage } from './pages/brand/BrandMarketingPage';
import { BrandProfilePage } from './pages/brand/BrandProfilePage';
import { BrandCategoriesPage } from './pages/brand/BrandCategoriesPage';

function App() {
  return (
    <Routes>
      <Route path="/" element={<Navigate to="/admin/dashboard" replace />} />

      {/* Admin Operations Console */}
      <Route path="/admin" element={<AdminLayout />}>
        <Route index element={<Navigate to="dashboard" replace />} />
        <Route path="dashboard" element={<AdminDashboardPage />} />
        <Route path="orders" element={<AdminOrdersPage />} />
        <Route path="users-sellers" element={<AdminUsersSellersPage />} />
        <Route path="categories" element={<AdminCategoriesPage />} />
        <Route
          path="brand-registry"
          element={
            <AdminPlaceholderPage
              title="Brand Registry Governance"
              description="Navigate to /brand to manage enterprise brand registry identity, catalog governance, and trademark protection."
            />
          }
        />
      </Route>

      {/* Enterprise Merchant Portal (Seller Dashboard) */}
      <Route path="/seller" element={<SellerLayout />}>
        <Route index element={<Navigate to="analytics" replace />} />
        <Route path="analytics" element={<SellerAnalyticsPage />} />
        <Route path="listings" element={<SellerListingsPage />} />
        <Route path="orders" element={<SellerOrdersPage />} />
        <Route path="profile" element={<SellerProfilePage />} />
      </Route>

      {/* Enterprise Brand Registry Portal (Brand Dashboard with Dynamic RBAC) */}
      <Route
        path="/brand"
        element={
          <BrandAuthProvider>
            <BrandLayout />
          </BrandAuthProvider>
        }
      >
        <Route index element={<Navigate to="analytics" replace />} />
        <Route path="analytics" element={<BrandAnalyticsPage />} />
        <Route path="catalog" element={<BrandCatalogPage />} />
        <Route
          path="team"
          element={
            <BrandRouteGuard allowedRoles={['BRAND_OWNER', 'BRAND_SUPER_ADMIN']}>
              <BrandTeamPage />
            </BrandRouteGuard>
          }
        />
        <Route path="marketing" element={<BrandMarketingPage />} />
        <Route path="profile" element={<BrandProfilePage />} />
        <Route path="categories" element={<BrandCategoriesPage />} />
      </Route>

      <Route path="*" element={<Navigate to="/brand/analytics" replace />} />
    </Routes>
  );
}

export default App;
