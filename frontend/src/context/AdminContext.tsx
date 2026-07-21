import React, { createContext, useContext, useState, ReactNode } from 'react';
import type { AdminUser } from '../components/admin/AdminLogin';

interface AdminContextType {
  adminUser: AdminUser | null;
  loginAdmin: (user: AdminUser) => void;
  logoutAdmin: () => void;
}

const AdminContext = createContext<AdminContextType | undefined>(undefined);

export const useAdmin = () => {
  const context = useContext(AdminContext);
  if (!context) {
    throw new Error('useAdmin must be used within AdminProvider');
  }
  return context;
};

export const AdminProvider = ({ children }: { children: ReactNode }) => {
  const [adminUser, setAdminUser] = useState<AdminUser | null>(null);

  const loginAdmin = (user: AdminUser) => {
    setAdminUser(user);
    localStorage.setItem('adminUser', JSON.stringify(user));
  };

  const logoutAdmin = () => {
    setAdminUser(null);
    localStorage.removeItem('adminUser');
    localStorage.removeItem('storeId');
  };

  // Load admin user from localStorage on mount
  React.useEffect(() => {
    const stored = localStorage.getItem('adminUser');
    if (stored) {
      try {
        setAdminUser(JSON.parse(stored));
      } catch (e) {
        localStorage.removeItem('adminUser');
      }
    }
  }, []);

  return (
    <AdminContext.Provider value={{ adminUser, loginAdmin, logoutAdmin }}>
      {children}
    </AdminContext.Provider>
  );
};