import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAdmin } from '../../context/AdminContext';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import {
  BarChart3,
  Package,
  ShoppingCart,
  Users,
  TrendingUp,
  AlertTriangle,
  DollarSign,
  ArrowUpRight,
  ArrowDownRight,
  LogOut,
  Settings,
  Bell,
  Store,
  FileText,
  Star,
  Gift,
  MessageSquare,
  Archive,
  Calendar,
} from 'lucide-react';
import { toast } from 'sonner@2.0.3';
import { stores } from '../../data/stores';
import { products } from '../../data/products';

export default function AdminDashboard() {
  const { adminUser, logoutAdmin } = useAdmin();
  const navigate = useNavigate();

  const handleLogout = () => {
    logoutAdmin();
    toast.success('Đã đăng xuất');
    navigate('/admin/login');
  };

  // Calculate inventory alerts
  const getInventoryAlerts = () => {
    const alerts: Array<{ type: 'low' | 'excess'; store: string; product: string; qty: number; storeId: string }> = [];
    
    stores.forEach((store) => {
      Object.entries(store.inventory).forEach(([productId, qty]) => {
        if (qty <= 5 && qty > 0) {
          const product = products.find((p) => p.id === productId);
          alerts.push({
            type: 'low',
            store: store.name,
            product: product?.name || 'Unknown',
            qty,
            storeId: store.id,
          });
        } else if (qty >= 25) {
          const product = products.find((p) => p.id === productId);
          alerts.push({
            type: 'excess',
            store: store.name,
            product: product?.name || 'Unknown',
            qty,
            storeId: store.id,
          });
        }
      });
    });
    
    return alerts;
  };

  const inventoryAlerts = getInventoryAlerts();
  const lowStockAlerts = inventoryAlerts.filter((a) => a.type === 'low');
  const excessStockAlerts = inventoryAlerts.filter((a) => a.type === 'excess');

  // Mock statistics
  const stats = {
    revenue: {
      value: '2.4 tỷ',
      change: 12.5,
      trend: 'up' as const,
      label: 'Doanh thu tháng này',
    },
    orders: {
      value: '1,234',
      change: 8.2,
      trend: 'up' as const,
      label: 'Đơn hàng',
    },
    customers: {
      value: '5,678',
      change: 15.3,
      trend: 'up' as const,
      label: 'Khách hàng',
    },
    products: {
      value: products.length.toString(),
      change: -2.1,
      trend: 'down' as const,
      label: 'Sản phẩm',
    },
  };

  const recentOrders = [
    { id: 'ORD-1234', customer: 'Nguyễn Văn A', total: '1.250.000đ', status: 'Đang giao', time: '10 phút trước' },
    { id: 'ORD-1233', customer: 'Trần Thị B', total: '890.000đ', status: 'Đã xác nhận', time: '25 phút trước' },
    { id: 'ORD-1232', customer: 'Lê Văn C', total: '2.100.000đ', status: 'Đang xử lý', time: '1 giờ trước' },
    { id: 'ORD-1231', customer: 'Phạm Thị D', total: '450.000đ', status: 'Hoàn thành', time: '2 giờ trước' },
  ];

  const pendingReturns = [
    { id: 'RET-101', order: 'ORD-1220', customer: 'Hoàng Văn E', reason: 'Sản phẩm lỗi', time: '1 giờ trước' },
    { id: 'RET-102', order: 'ORD-1215', customer: 'Vũ Thị F', reason: 'Giao sai hàng', time: '3 giờ trước' },
  ];

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'Đang giao':
        return 'bg-blue-100 text-blue-800';
      case 'Đã xác nhận':
        return 'bg-green-100 text-green-800';
      case 'Đang xử lý':
        return 'bg-yellow-100 text-yellow-800';
      case 'Hoàn thành':
        return 'bg-gray-100 text-gray-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white border-b sticky top-0 z-10">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 bg-indigo-600 rounded-lg flex items-center justify-center">
                <Package className="w-6 h-6 text-white" />
              </div>
              <div>
                <h1 className="font-bold text-xl text-indigo-600">Kinderland Admin</h1>
                <p className="text-xs text-gray-500">Quản trị hệ thống</p>
              </div>
            </div>
            <div className="flex items-center gap-4">
              <Button variant="ghost" size="icon" className="relative">
                <Bell className="w-5 h-5" />
                <span className="absolute -top-1 -right-1 w-5 h-5 bg-red-500 text-white text-xs rounded-full flex items-center justify-center">
                  {lowStockAlerts.length}
                </span>
              </Button>
              <div className="text-right">
                <p className="text-sm font-medium">{adminUser?.name}</p>
                <p className="text-xs text-gray-500">Administrator</p>
              </div>
              <Button variant="ghost" size="icon" onClick={handleLogout}>
                <LogOut className="w-5 h-5" />
              </Button>
            </div>
          </div>
        </div>
      </header>

      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Quick Stats */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {Object.entries(stats).map(([key, stat]) => (
            <Card key={key}>
              <CardHeader className="flex flex-row items-center justify-between pb-2">
                <CardTitle className="text-sm font-medium text-gray-600">
                  {stat.label}
                </CardTitle>
                {key === 'revenue' && <DollarSign className="w-4 h-4 text-gray-400" />}
                {key === 'orders' && <ShoppingCart className="w-4 h-4 text-gray-400" />}
                {key === 'customers' && <Users className="w-4 h-4 text-gray-400" />}
                {key === 'products' && <Package className="w-4 h-4 text-gray-400" />}
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{stat.value}</div>
                <div className="flex items-center gap-1 mt-1">
                  {stat.trend === 'up' ? (
                    <ArrowUpRight className="w-4 h-4 text-green-600" />
                  ) : (
                    <ArrowDownRight className="w-4 h-4 text-red-600" />
                  )}
                  <span
                    className={`text-xs font-medium ${
                      stat.trend === 'up' ? 'text-green-600' : 'text-red-600'
                    }`}
                  >
                    {Math.abs(stat.change)}%
                  </span>
                  <span className="text-xs text-gray-500">so với tháng trước</span>
                </div>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Inventory Alerts */}
        {lowStockAlerts.length > 0 && (
          <Card className="mb-8 border-l-4 border-l-orange-500">
            <CardHeader>
              <div className="flex items-center gap-2">
                <AlertTriangle className="w-5 h-5 text-orange-600" />
                <CardTitle>Cảnh báo tồn kho thấp</CardTitle>
                <Badge variant="destructive">{lowStockAlerts.length}</Badge>
              </div>
              <CardDescription>
                Các sản phẩm cần được bổ sung kho ngay
              </CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-2">
                {lowStockAlerts.slice(0, 5).map((alert, idx) => (
                  <div
                    key={idx}
                    className="flex items-center justify-between p-3 bg-orange-50 rounded-lg"
                  >
                    <div className="flex-1">
                      <p className="font-medium text-sm">{alert.product}</p>
                      <p className="text-xs text-gray-600">{alert.store}</p>
                    </div>
                    <div className="text-right">
                      <Badge variant="outline" className="bg-white">
                        {alert.qty} sản phẩm
                      </Badge>
                    </div>
                  </div>
                ))}
                {lowStockAlerts.length > 5 && (
                  <Button
                    variant="link"
                    className="w-full text-orange-600"
                    onClick={() => navigate('/admin/inventory')}
                  >
                    Xem tất cả {lowStockAlerts.length} cảnh báo →
                  </Button>
                )}
              </div>
            </CardContent>
          </Card>
        )}

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
          {/* Recent Orders */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <ShoppingCart className="w-5 h-5" />
                Đơn hàng gần đây
              </CardTitle>
              <CardDescription>Đơn hàng mới nhất trong hệ thống</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {recentOrders.map((order) => (
                  <div key={order.id} className="flex items-center justify-between pb-4 border-b last:border-0 last:pb-0">
                    <div className="flex-1">
                      <p className="font-medium text-sm">{order.id}</p>
                      <p className="text-xs text-gray-600">{order.customer}</p>
                      <p className="text-xs text-gray-500 mt-1">{order.time}</p>
                    </div>
                    <div className="text-right">
                      <p className="font-medium text-sm mb-1">{order.total}</p>
                      <Badge className={getStatusColor(order.status)} variant="outline">
                        {order.status}
                      </Badge>
                    </div>
                  </div>
                ))}
              </div>
              <Button
                variant="outline"
                className="w-full mt-4"
                onClick={() => navigate('/admin/orders')}
              >
                Xem tất cả đơn hàng →
              </Button>
            </CardContent>
          </Card>

          {/* Pending Returns */}
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Archive className="w-5 h-5" />
                Yêu cầu trả hàng
              </CardTitle>
              <CardDescription>Đang chờ xử lý</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                {pendingReturns.map((ret) => (
                  <div key={ret.id} className="p-4 bg-yellow-50 rounded-lg border border-yellow-200">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <p className="font-medium text-sm">{ret.id}</p>
                        <p className="text-xs text-gray-600">Đơn hàng: {ret.order}</p>
                      </div>
                      <Badge variant="outline" className="bg-white">
                        Chờ duyệt
                      </Badge>
                    </div>
                    <p className="text-sm text-gray-700 mb-1">{ret.customer}</p>
                    <p className="text-xs text-gray-600 mb-2">Lý do: {ret.reason}</p>
                    <p className="text-xs text-gray-500">{ret.time}</p>
                  </div>
                ))}
              </div>
              <Button
                variant="outline"
                className="w-full mt-4"
                onClick={() => navigate('/admin/returns')}
              >
                Xem tất cả yêu cầu →
              </Button>
            </CardContent>
          </Card>
        </div>

        {/* Quick Actions */}
        <Card>
          <CardHeader>
            <CardTitle>Quản lý nhanh</CardTitle>
            <CardDescription>Truy cập các chức năng quản trị chính</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/products')}
              >
                <Package className="w-6 h-6" />
                <span className="text-sm">Sản phẩm</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/orders')}
              >
                <ShoppingCart className="w-6 h-6" />
                <span className="text-sm">Đơn hàng</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/users')}
              >
                <Users className="w-6 h-6" />
                <span className="text-sm">Người dùng</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/promotions')}
              >
                <Gift className="w-6 h-6" />
                <span className="text-sm">Khuyến mãi</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/inventory')}
              >
                <Store className="w-6 h-6" />
                <span className="text-sm">Tồn kho</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/blog')}
              >
                <FileText className="w-6 h-6" />
                <span className="text-sm">Bài viết</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/reviews')}
              >
                <Star className="w-6 h-6" />
                <span className="text-sm">Đánh giá</span>
              </Button>
              <Button
                variant="outline"
                className="h-24 flex flex-col gap-2"
                onClick={() => navigate('/admin/reports')}
              >
                <BarChart3 className="w-6 h-6" />
                <span className="text-sm">Báo cáo</span>
              </Button>
            </div>
          </CardContent>
        </Card>
      </div>
    </div>
  );
}
