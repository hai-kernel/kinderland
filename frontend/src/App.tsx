import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AppProvider, useApp } from './context/AppContext';
import { AdminProvider, useAdmin } from './context/AdminContext';
import Navbar from './components/layout/Navbar';
import Footer from './components/layout/Footer';
import HomePage from './components/home/HomePage';
import LoginPage from './components/auth/LoginPage';
import ProductsPage from './components/pages/ProductsPage';
import CategoriesPage from './components/pages/CategoriesPage';
import BrandsPage from './components/pages/BrandsPage';
import DiscountsPage from './components/pages/DiscountsPage';
import NewArrivalsPage from './components/pages/NewArrivalsPage';
import StoreFinderPage from './components/pages/StoreFinderPage';
import BlogListPage from './components/pages/BlogListPage';
import BlogDetailPage from './components/pages/BlogDetailPage';
import ProductDetail from './components/shop/ProductDetail';
import Cart from './components/cart/Cart';
import Checkout from './components/checkout/Checkout';
import Payment from './components/checkout/Payment';
import OrderSuccess from './components/checkout/OrderSuccess';

// Admin & Staff
import AdminLogin from './components/admin/AdminLogin';
import AdminDashboard from './components/admin/AdminDashboard';
import ProductManagement from './components/admin/ProductManagement';
import OrderManagement from './components/admin/OrderManagement';
import StaffDashboard from './components/staff/StaffDashboard';
import QRScanner from './components/staff/QRScanner';
import InventoryCheck from './components/staff/InventoryCheck';
import StoreTransfer from './components/staff/StoreTransfer';
import DefectiveReport from './components/staff/DefectiveReport';
import ReturnManagement from './components/staff/ReturnManagement';

// Customer Account
import CustomerDashboard from './components/customer/CustomerDashboard';
import OrderHistory from './components/customer/OrderHistory';
import CustomerProfile from './components/customer/CustomerProfile';
import LoyaltyPoints from './components/customer/LoyaltyPoints';

function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const { user } = useApp();
  return user ? <>{children}</> : <Navigate to="/login" />;
}

function AdminProtectedRoute({ children, allowedRoles }: { children: React.ReactNode; allowedRoles: ('admin' | 'staff')[] }) {
  const { adminUser } = useAdmin();
  
  if (!adminUser) {
    return <Navigate to="/admin/login" />;
  }
  
  if (!allowedRoles.includes(adminUser.role)) {
    return <Navigate to={adminUser.role === 'admin' ? '/admin/dashboard' : '/staff/dashboard'} />;
  }
  
  return <>{children}</>;
}

function AppContent() {
  const { user } = useApp();

  return (
    <BrowserRouter>
      <div className="min-h-screen bg-gray-50">
        <Routes>
          {/* Customer Routes */}
          <Route path="/login" element={<LoginPage />} />
          
          {/* Admin/Staff Routes */}
          <Route path="/admin/login" element={<AdminLogin />} />
          
          <Route
            path="/admin/dashboard"
            element={
              <AdminProtectedRoute allowedRoles={['admin']}>
                <AdminDashboard />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/admin/products"
            element={
              <AdminProtectedRoute allowedRoles={['admin']}>
                <ProductManagement />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/admin/orders"
            element={
              <AdminProtectedRoute allowedRoles={['admin']}>
                <OrderManagement />
              </AdminProtectedRoute>
            }
          />
          
          <Route
            path="/staff/dashboard"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <StaffDashboard />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/qr-scanner"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <QRScanner />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/inventory"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <InventoryCheck />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/transfer"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <StoreTransfer />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/defective"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <DefectiveReport />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/returns"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <ReturnManagement />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/stock-check"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <InventoryCheck />
              </AdminProtectedRoute>
            }
          />
          <Route
            path="/staff/availability"
            element={
              <AdminProtectedRoute allowedRoles={['staff']}>
                <InventoryCheck />
              </AdminProtectedRoute>
            }
          />
          
          {/* Customer Site Routes */}
          <Route
            path="/*"
            element={
              <>
                <Navbar />
                <Routes>
                  <Route path="/" element={<HomePage />} />
                  <Route path="/products" element={<ProductsPage />} />
                  <Route path="/categories" element={<CategoriesPage />} />
                  <Route path="/categories/:categoryName" element={<CategoriesPage />} />
                  <Route path="/brands" element={<BrandsPage />} />
                  <Route path="/brands/:brandName" element={<BrandsPage />} />
                  <Route path="/discounts" element={<DiscountsPage />} />
                  <Route path="/new-arrivals" element={<NewArrivalsPage />} />
                  <Route path="/stores" element={<StoreFinderPage />} />
                  <Route path="/blog" element={<BlogListPage />} />
                  <Route path="/blog/:id" element={<BlogDetailPage />} />
                  <Route path="/product/:id" element={<ProductDetail />} />
                  <Route path="/cart" element={<Cart />} />
                  <Route
                    path="/checkout"
                    element={
                      <ProtectedRoute>
                        <Checkout />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/payment"
                    element={
                      <ProtectedRoute>
                        <Payment />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/order-success"
                    element={
                      <ProtectedRoute>
                        <OrderSuccess />
                      </ProtectedRoute>
                    }
                  />
                  {/* Customer Account Routes */}
                  <Route
                    path="/account"
                    element={
                      <ProtectedRoute>
                        <CustomerDashboard />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/account/orders"
                    element={
                      <ProtectedRoute>
                        <OrderHistory />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/account/profile"
                    element={
                      <ProtectedRoute>
                        <CustomerProfile />
                      </ProtectedRoute>
                    }
                  />
                  <Route
                    path="/account/loyalty"
                    element={
                      <ProtectedRoute>
                        <LoyaltyPoints />
                      </ProtectedRoute>
                    }
                  />
                </Routes>
                <Footer />
              </>
            }
          />
        </Routes>
      </div>
    </BrowserRouter>
  );
}

export default function App() {
  return (
    <AppProvider>
      <AdminProvider>
        <AppContent />
      </AdminProvider>
    </AppProvider>
  );
}