import { Navigate, Route, Routes } from 'react-router-dom';
import { AdminLayout } from './components/layout/AdminLayout';
import { SellerLayout } from './components/layout/SellerLayout';
import { AdminCategoriesPage } from './pages/AdminCategoriesPage';
import { AdminDashboardPage } from './pages/AdminDashboardPage';
import { AdminOrdersPage } from './pages/AdminOrdersPage';
import { AdminPlaceholderPage } from './pages/AdminPlaceholderPage';
import { AdminUsersSellersPage } from './pages/AdminUsersSellersPage';
import { SellerAnalyticsPage } from './pages/SellerAnalyticsPage';
import { SellerListingsPage } from './pages/SellerListingsPage';
import { SellerOrdersPage } from './pages/SellerOrdersPage';
import { SellerProfilePage } from './pages/SellerProfilePage';

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
              description="Pending brand applications are summarized on the executive dashboard KPI card."
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

      <Route path="*" element={<Navigate to="/admin/dashboard" replace />} />
    </Routes>
  );
}

export default App;
