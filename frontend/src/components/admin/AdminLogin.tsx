import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAdmin } from '../../context/AdminContext';
import { Button } from '../ui/button';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { toast } from 'sonner@2.0.3';
import { Package, ShieldCheck } from 'lucide-react';

export type UserRole = 'admin' | 'staff';

export interface AdminUser {
  id: string;
  email: string;
  name: string;
  role: UserRole;
  storeId?: string; // For staff members
  storeName?: string;
}

// Mock users for demo
const MOCK_USERS: { email: string; password: string; user: AdminUser }[] = [
  {
    email: 'admin@kinderland.vn',
    password: 'admin123',
    user: {
      id: 'admin-1',
      email: 'admin@kinderland.vn',
      name: 'Nguyễn Văn Admin',
      role: 'admin',
    },
  },
  {
    email: 'staff1@kinderland.vn',
    password: 'staff123',
    user: {
      id: 'staff-1',
      email: 'staff1@kinderland.vn',
      name: 'Trần Thị Nhân Viên',
      role: 'staff',
      storeId: 'store-1',
      storeName: 'Kinderland Vincom Center Đồng Khởi',
    },
  },
  {
    email: 'staff2@kinderland.vn',
    password: 'staff123',
    user: {
      id: 'staff-2',
      email: 'staff2@kinderland.vn',
      name: 'Lê Văn Thành',
      role: 'staff',
      storeId: 'store-6',
      storeName: 'Kinderland Royal City Hà Nội',
    },
  },
];

export default function AdminLogin() {
  const navigate = useNavigate();
  const { loginAdmin } = useAdmin();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    // Mock login check
    setTimeout(() => {
      const found = MOCK_USERS.find(
        (u) => u.email === email && u.password === password
      );

      if (found) {
        toast.success(`Đăng nhập thành công! Xin chào ${found.user.name}`);
        loginAdmin(found.user);
        if (found.user.role === 'admin') {
          navigate('/admin/dashboard');
        } else {
          navigate('/staff/dashboard');
        }
      } else {
        toast.error('Email hoặc mật khẩu không đúng!');
      }
      setIsLoading(false);
    }, 800);
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#78A2D2]/20 via-white to-[#FEFFAF]/30 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        {/* Logo */}
        <div className="text-center mb-8">
          <div className="inline-flex items-center gap-2 mb-4">
            <div className="w-12 h-12 bg-[#78A2D2] rounded-xl flex items-center justify-center">
              <Package className="w-7 h-7 text-white" />
            </div>
            <h1 className="text-3xl font-bold text-[#78A2D2]">Kinderland</h1>
          </div>
          <p className="text-gray-600">Hệ thống quản trị</p>
        </div>

        <Card className="border-0 shadow-xl">
          <CardHeader className="space-y-1">
            <div className="flex items-center justify-center mb-4">
              <div className="w-16 h-16 bg-[#78A2D2]/20 rounded-full flex items-center justify-center">
                <ShieldCheck className="w-8 h-8 text-[#78A2D2]" />
              </div>
            </div>
            <CardTitle className="text-2xl text-center">Đăng nhập</CardTitle>
            <CardDescription className="text-center">
              Dành cho quản trị viên và nhân viên cửa hàng
            </CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleLogin} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="email">Email</Label>
                <Input
                  id="email"
                  type="email"
                  placeholder="admin@kinderland.vn"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="password">Mật khẩu</Label>
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
              </div>
              <Button
                type="submit"
                className="w-full bg-[#78A2D2] hover:bg-[#6A94C4]"
                disabled={isLoading}
              >
                {isLoading ? 'Đang đăng nhập...' : 'Đăng nhập'}
              </Button>
            </form>

            {/* Demo credentials */}
            <div className="mt-6 pt-6 border-t">
              <p className="text-xs text-gray-600 mb-3 font-medium">Tài khoản demo:</p>
              <div className="space-y-2 text-xs">
                <div className="bg-[#78A2D2]/10 p-2 rounded border border-[#78A2D2]/30">
                  <p className="font-medium text-[#2C2C2C]">Admin:</p>
                  <p className="text-[#78A2D2]">admin@kinderland.vn / admin123</p>
                </div>
                <div className="bg-[#FEFFAF]/30 p-2 rounded border border-[#78A2D2]/30">
                  <p className="font-medium text-[#2C2C2C]">Nhân viên HCM:</p>
                  <p className="text-gray-700">staff1@kinderland.vn / staff123</p>
                </div>
                <div className="bg-[#FEFFAF]/30 p-2 rounded border border-[#78A2D2]/30">
                  <p className="font-medium text-[#2C2C2C]">Nhân viên Hà Nội:</p>
                  <p className="text-gray-700">staff2@kinderland.vn / staff123</p>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>

        {/* Back to customer site */}
        <div className="text-center mt-6">
          <Button
            variant="ghost"
            onClick={() => navigate('/')}
            className="text-gray-600 hover:text-[#78A2D2]"
          >
            ← Quay lại trang chủ Kinderland
          </Button>
        </div>
      </div>
    </div>
  );
}