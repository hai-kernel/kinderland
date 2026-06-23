import React, { createContext, useContext, useState, ReactNode } from 'react';
import { DEMO_CUSTOMERS } from '../data/users';

interface User {
  id: string;
  email: string;
  name: string;
  phone?: string;
  address?: string;
  membershipTier?: 'bronze' | 'silver' | 'gold';
  points?: number;
}

interface Product {
  id: string;
  name: string;
  price: number;
  image: string;
  description: string;
  category: string;
  stock: number;
  types?: string[];
}

interface CartItem {
  product: Product;
  quantity: number;
  type?: string;
}

interface Voucher {
  code: string;
  discount: number;
  type: 'percentage' | 'fixed';
}

interface AppContextType {
  user: User | null;
  login: (email: string, password: string) => void;
  register: (email: string, password: string, name: string) => void;
  logout: () => void;
  cart: CartItem[];
  addToCart: (product: Product, quantity: number, type?: string) => void;
  removeFromCart: (productId: string) => void;
  updateCartItem: (productId: string, quantity: number, type?: string) => void;
  clearCart: () => void;
  voucher: Voucher | null;
  applyVoucher: (code: string) => boolean;
  removeVoucher: () => void;
}

const AppContext = createContext<AppContextType | undefined>(undefined);

export const useApp = () => {
  const context = useContext(AppContext);
  if (!context) {
    throw new Error('useApp must be used within AppProvider');
  }
  return context;
};

const MOCK_VOUCHERS: Voucher[] = [
  { code: 'GIAM10', discount: 10, type: 'percentage' },
  { code: 'GIAM50K', discount: 50000, type: 'fixed' },
  { code: 'FREESHIP', discount: 30000, type: 'fixed' },
];

export const AppProvider = ({ children }: { children: ReactNode }) => {
  const [user, setUser] = useState<User | null>(null);
  const [cart, setCart] = useState<CartItem[]>([]);
  const [voucher, setVoucher] = useState<Voucher | null>(null);

  const login = (email: string, password: string) => {
    // Check against demo customers
    const foundCustomer = DEMO_CUSTOMERS.find(
      (c) => c.email === email && c.password === password
    );
    
    if (foundCustomer) {
      setUser({
        id: foundCustomer.id,
        email: foundCustomer.email,
        name: foundCustomer.name,
        phone: foundCustomer.phone,
        address: foundCustomer.address,
        membershipTier: foundCustomer.membershipTier,
        points: foundCustomer.points,
      });
    } else {
      // Fallback to mock login for any email (for backward compatibility)
      setUser({
        id: '1',
        email,
        name: email.split('@')[0],
        membershipTier: 'bronze',
        points: 0,
      });
    }
  };

  const register = (email: string, password: string, name: string) => {
    // Mock register
    setUser({
      id: '1',
      email,
      name,
    });
  };

  const logout = () => {
    setUser(null);
    setCart([]);
    setVoucher(null);
  };

  const addToCart = (product: Product, quantity: number, type?: string) => {
    setCart((prev) => {
      const existingIndex = prev.findIndex(
        (item) => item.product.id === product.id && item.type === type
      );
      if (existingIndex > -1) {
        const newCart = [...prev];
        newCart[existingIndex].quantity += quantity;
        return newCart;
      }
      return [...prev, { product, quantity, type }];
    });
  };

  const removeFromCart = (productId: string) => {
    setCart((prev) => prev.filter((item) => item.product.id !== productId));
  };

  const updateCartItem = (productId: string, quantity: number, type?: string) => {
    setCart((prev) => {
      return prev.map((item) => {
        if (item.product.id === productId && item.type === type) {
          return { ...item, quantity };
        }
        return item;
      });
    });
  };

  const clearCart = () => {
    setCart([]);
  };

  const applyVoucher = (code: string): boolean => {
    const foundVoucher = MOCK_VOUCHERS.find(
      (v) => v.code.toLowerCase() === code.toLowerCase()
    );
    if (foundVoucher) {
      setVoucher(foundVoucher);
      return true;
    }
    return false;
  };

  const removeVoucher = () => {
    setVoucher(null);
  };

  return (
    <AppContext.Provider
      value={{
        user,
        login,
        register,
        logout,
        cart,
        addToCart,
        removeFromCart,
        updateCartItem,
        clearCart,
        voucher,
        applyVoucher,
        removeVoucher,
      }}
    >
      {children}
    </AppContext.Provider>
  );
};