import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Badge } from '../ui/badge';
import { Button } from '../ui/button';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import {
  Package,
  Truck,
  CheckCircle,
  XCircle,
  Clock,
  Eye,
  Download,
  RotateCcw,
  ArrowLeft,
} from 'lucide-react';

interface Order {
  id: string;
  orderNumber: string;
  date: string;
  status: 'pending' | 'processing' | 'shipping' | 'delivered' | 'cancelled';
  total: number;
  items: {
    id: string;
    name: string;
    image: string;
    quantity: number;
    price: number;
  }[];
  shippingAddress: string;
  paymentMethod: string;
}

export default function OrderHistory() {
  const { user } = useApp();
  const [selectedTab, setSelectedTab] = useState('all');

  // Mock orders data
  const mockOrders: Order[] = [
    {
      id: 'order-1',
      orderNumber: 'KDL2026011401',
      date: '2026-01-10',
      status: 'delivered',
      total: 1299000,
      items: [
        {
          id: 'p1',
          name: 'LEGO City Police Station',
          image: 'https://images.unsplash.com/photo-1587654780291-39c9404d746b?w=200',
          quantity: 1,
          price: 1299000,
        },
      ],
      shippingAddress: '123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
      paymentMethod: 'Thẻ tín dụng',
    },
    {
      id: 'order-2',
      orderNumber: 'KDL2026011301',
      date: '2026-01-08',
      status: 'shipping',
      total: 2150000,
      items: [
        {
          id: 'p2',
          name: 'Barbie Dreamhouse Adventures',
          image: 'https://images.unsplash.com/photo-1571609190641-1ebf01b57a67?w=200',
          quantity: 1,
          price: 1850000,
        },
        {
          id: 'p3',
          name: 'Hot Wheels Track Set',
          image: 'https://images.unsplash.com/photo-1558060370-d644479cb6f7?w=200',
          quantity: 1,
          price: 300000,
        },
      ],
      shippingAddress: '123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
      paymentMethod: 'COD',
    },
    {
      id: 'order-3',
      orderNumber: 'KDL2026010501',
      date: '2026-01-05',
      status: 'processing',
      total: 890000,
      items: [
        {
          id: 'p4',
          name: 'Monopoly Classic Board Game',
          image: 'https://images.unsplash.com/photo-1566694271453-390536dd1f0d?w=200',
          quantity: 1,
          price: 890000,
        },
      ],
      shippingAddress: '123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
      paymentMethod: 'Chuyển khoản',
    },
    {
      id: 'order-4',
      orderNumber: 'KDL2025122801',
      date: '2025-12-28',
      status: 'delivered',
      total: 3450000,
      items: [
        {
          id: 'p5',
          name: 'LEGO Star Wars Millennium Falcon',
          image: 'https://images.unsplash.com/photo-1585366119957-e9730b6d0f60?w=200',
          quantity: 1,
          price: 3450000,
        },
      ],
      shippingAddress: '123 Nguyễn Huệ, Quận 1, TP. Hồ Chí Minh',
      paymentMethod: 'Thẻ tín dụng',
    },
  ];

  const getStatusInfo = (status: Order['status']) => {
    const statusMap = {
      pending: {
        label: 'Chờ xác nhận',
        icon: Clock,
        color: 'bg-gray-100 text-gray-800',
        iconColor: 'text-gray-600',
      },
      processing: {
        label: 'Đang xử lý',
        icon: Package,
        color: 'bg-blue-100 text-blue-800',
        iconColor: 'text-blue-600',
      },
      shipping: {
        label: 'Đang giao hàng',
        icon: Truck,
        color: 'bg-orange-100 text-orange-800',
        iconColor: 'text-orange-600',
      },
      delivered: {
        label: 'Đã giao',
        icon: CheckCircle,
        color: 'bg-green-100 text-green-800',
        iconColor: 'text-green-600',
      },
      cancelled: {
        label: 'Đã hủy',
        icon: XCircle,
        color: 'bg-red-100 text-red-800',
        iconColor: 'text-red-600',
      },
    };
    return statusMap[status];
  };

  const filterOrders = (status: string) => {
    if (status === 'all') return mockOrders;
    return mockOrders.filter((order) => order.status === status);
  };

  const OrderCard = ({ order }: { order: Order }) => {
    const statusInfo = getStatusInfo(order.status);
    const StatusIcon = statusInfo.icon;

    return (
      <Card className="mb-4 hover:shadow-md transition-shadow">
        <CardHeader className="pb-3">
          <div className="flex items-start justify-between">
            <div>
              <CardTitle className="text-lg mb-2">
                Đơn hàng #{order.orderNumber}
              </CardTitle>
              <p className="text-sm text-gray-500">
                Đặt ngày: {new Date(order.date).toLocaleDateString('vi-VN')}
              </p>
            </div>
            <Badge className={statusInfo.color}>
              <StatusIcon className={`w-4 h-4 mr-1 ${statusInfo.iconColor}`} />
              {statusInfo.label}
            </Badge>
          </div>
        </CardHeader>
        <CardContent>
          {/* Order Items */}
          <div className="space-y-3 mb-4">
            {order.items.map((item) => (
              <div key={item.id} className="flex items-center gap-4">
                <img
                  src={item.image}
                  alt={item.name}
                  className="w-16 h-16 object-cover rounded-lg"
                />
                <div className="flex-1">
                  <p className="font-medium text-sm">{item.name}</p>
                  <p className="text-sm text-gray-500">
                    Số lượng: {item.quantity} × {item.price.toLocaleString('vi-VN')}₫
                  </p>
                </div>
              </div>
            ))}
          </div>

          {/* Order Summary */}
          <div className="border-t pt-3 mb-4">
            <div className="flex justify-between items-center mb-2">
              <span className="text-sm text-gray-600">Phương thức thanh toán:</span>
              <span className="text-sm font-medium">{order.paymentMethod}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm font-medium">Tổng cộng:</span>
              <span className="text-lg font-bold text-indigo-600">
                {order.total.toLocaleString('vi-VN')}₫
              </span>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex gap-2">
            <Button variant="outline" className="flex-1" size="sm">
              <Eye className="w-4 h-4 mr-2" />
              Xem chi tiết
            </Button>
            {order.status === 'delivered' && (
              <>
                <Button variant="outline" size="sm">
                  <Download className="w-4 h-4 mr-2" />
                  Hóa đơn
                </Button>
                <Button variant="outline" size="sm">
                  <RotateCcw className="w-4 h-4 mr-2" />
                  Mua lại
                </Button>
              </>
            )}
            {order.status === 'shipping' && (
              <Button variant="outline" size="sm">
                <Truck className="w-4 h-4 mr-2" />
                Theo dõi
              </Button>
            )}
          </div>
        </CardContent>
      </Card>
    );
  };

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="mb-6">
          <Link to="/account">
            <Button variant="ghost" className="mb-4">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Quay lại tài khoản
            </Button>
          </Link>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">Đơn hàng của tôi</h1>
          <p className="text-gray-600">Quản lý và theo dõi các đơn hàng của bạn</p>
        </div>

        {/* Tabs for filtering orders */}
        <Tabs value={selectedTab} onValueChange={setSelectedTab} className="mb-6">
          <TabsList className="grid w-full grid-cols-5 lg:w-auto lg:inline-grid">
            <TabsTrigger value="all">
              Tất cả ({mockOrders.length})
            </TabsTrigger>
            <TabsTrigger value="processing">
              Đang xử lý ({filterOrders('processing').length})
            </TabsTrigger>
            <TabsTrigger value="shipping">
              Đang giao ({filterOrders('shipping').length})
            </TabsTrigger>
            <TabsTrigger value="delivered">
              Đã giao ({filterOrders('delivered').length})
            </TabsTrigger>
            <TabsTrigger value="cancelled">
              Đã hủy ({filterOrders('cancelled').length})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="all" className="mt-6">
            {mockOrders.length === 0 ? (
              <Card>
                <CardContent className="p-12 text-center">
                  <Package className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-600 mb-4">Bạn chưa có đơn hàng nào</p>
                  <Link to="/products">
                    <Button>Mua sắm ngay</Button>
                  </Link>
                </CardContent>
              </Card>
            ) : (
              mockOrders.map((order) => <OrderCard key={order.id} order={order} />)
            )}
          </TabsContent>

          <TabsContent value="processing" className="mt-6">
            {filterOrders('processing').map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </TabsContent>

          <TabsContent value="shipping" className="mt-6">
            {filterOrders('shipping').map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </TabsContent>

          <TabsContent value="delivered" className="mt-6">
            {filterOrders('delivered').map((order) => (
              <OrderCard key={order.id} order={order} />
            ))}
          </TabsContent>

          <TabsContent value="cancelled" className="mt-6">
            {filterOrders('cancelled').length === 0 ? (
              <Card>
                <CardContent className="p-12 text-center">
                  <XCircle className="w-16 h-16 text-gray-400 mx-auto mb-4" />
                  <p className="text-gray-600">Không có đơn hàng đã hủy</p>
                </CardContent>
              </Card>
            ) : (
              filterOrders('cancelled').map((order) => (
                <OrderCard key={order.id} order={order} />
              ))
            )}
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}
