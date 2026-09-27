import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminLayout } from './components/layout/AdminLayout';
import { SellerLayout } from './components/layout/SellerLayout';
import { BrandLayout } from './components/layout/BrandLayout';
import { StorefrontLayout } from './components/layout/StorefrontLayout';
import { BrandAuthProvider } from './context/BrandAuthContext';
import { BrandRouteGuard } from './components/brand/BrandRouteGuard';
import { BrandGuard } from './components/brand/BrandGuard';
import { SellerGuard } from './components/seller/SellerGuard';
import { AdminGuard } from './components/admin/AdminGuard';
import { CustomerAuthProvider } from './context/CustomerAuthContext';

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

// Storefront Customer Pages (Enterprise Portal v1.1.0)
import { HomePage } from './pages/storefront/HomePage';
import { ProductListingPage } from './pages/storefront/ProductListingPage';
import { ProductDetailPage } from './pages/storefront/ProductDetailPage';
import { CartPage } from './pages/storefront/CartPage';
import { CheckoutPage } from './pages/storefront/CheckoutPage';
import { OrdersPage } from './pages/storefront/OrdersPage';
import { WishlistPage } from './pages/storefront/WishlistPage';

function App() {
  return (
    <CustomerAuthProvider>
      <Routes>
        {/* Customer Storefront (Public Marketplace & Corporate Portal) */}
        <Route path="/" element={<StorefrontLayout />}>
          <Route index element={<HomePage />} />
          <Route path="products" element={<ProductListingPage />} />
          <Route path="products/:id" element={<ProductDetailPage />} />
          <Route path="cart" element={<CartPage />} />
          <Route path="checkout" element={<CheckoutPage />} />
          <Route path="orders" element={<OrdersPage />} />
          <Route path="wishlist" element={<WishlistPage />} />
        </Route>

        {/* Admin Operations Console (RBAC Protected) */}
        <Route path="/admin" element={<AdminGuard><AdminLayout /></AdminGuard>}>
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

        {/* Enterprise Merchant Portal (Seller Dashboard – RBAC Protected) */}
        <Route path="/seller" element={<SellerGuard><SellerLayout /></SellerGuard>}>
          <Route index element={<Navigate to="analytics" replace />} />
          <Route path="analytics" element={<SellerAnalyticsPage />} />
          <Route path="listings" element={<SellerListingsPage />} />
          <Route path="orders" element={<SellerOrdersPage />} />
          <Route path="profile" element={<SellerProfilePage />} />
        </Route>

        {/* Enterprise Brand Registry Portal (Brand Dashboard – RBAC Protected) */}
        <Route
          path="/brand"
          element={
            <BrandAuthProvider>
              <BrandGuard>
                <BrandLayout />
              </BrandGuard>
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

        {/* Wildcard Fallback */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </CustomerAuthProvider>
  );
}

export default App;
