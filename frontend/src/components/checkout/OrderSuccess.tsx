import React from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { CheckCircle, Package, Home } from 'lucide-react';

export default function OrderSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const orderInfo = location.state?.orderInfo;

  if (!orderInfo) {
    navigate('/');
    return null;
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const orderNumber = 'DH' + Date.now().toString().slice(-8);

  return (
    <div className="bg-gray-50 min-h-screen">
      {/* Hero Banner */}
      <div className="bg-gradient-to-r from-[#78A2D2] via-[#6A94C4] to-[#78A2D2] text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-3 bg-white/20 px-6 py-3 rounded-full mb-4 backdrop-blur-sm">
            <CheckCircle className="size-6" />
            <span className="font-bold text-lg">ĐẶT HÀNG THÀNH CÔNG</span>
          </div>
          <h1 className="text-5xl font-bold mb-4">
            Cảm Ơn Quý Khách!
          </h1>
          <p className="text-xl text-white/90">
            🎉 Đơn hàng của bạn đã được xác nhận
          </p>
        </div>
      </div>
      
      <div className="container mx-auto px-4 py-8">
        <div className="max-w-2xl mx-auto">
          <div className="bg-white rounded-3xl shadow-2xl p-8 text-center border-2 border-gray-200">
            <div className="mb-6">
              <div className="bg-[#78A2D2] rounded-full p-4 inline-block mb-4">
                <CheckCircle className="size-20 text-white" />
              </div>
              <h2 className="text-4xl font-bold mb-2 text-[#78A2D2]">
                Đặt Hàng Thành Công!
              </h2>
              <p className="text-gray-600 text-lg">
                🎉 Cảm ơn bạn đã mua sắm tại Kinderland
              </p>
            </div>

            <div className="bg-[#FEFFAF]/30 rounded-2xl p-6 mb-6 border-2 border-[#78A2D2]/30">
              <div className="flex items-center justify-center gap-2 mb-4">
                <Package className="size-6 text-[#78A2D2]" />
                <h2 className="text-xl font-bold text-gray-800">
                  Thông Tin Đơn Hàng
                </h2>
              </div>

              <div className="space-y-3 text-left bg-white rounded-xl p-4">
                <div className="flex justify-between">
                  <span className="text-gray-600 font-medium">Mã đơn hàng:</span>
                  <span className="font-bold text-[#78A2D2]">{orderNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 font-medium">Người nhận:</span>
                  <span className="font-bold">{orderInfo.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 font-medium">Số điện thoại:</span>
                  <span className="font-bold">{orderInfo.phone}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 font-medium">Địa chỉ:</span>
                  <span className="font-bold text-right max-w-xs">
                    {orderInfo.address}
                  </span>
                </div>
                <div className="border-t-2 border-dashed pt-3 flex justify-between">
                  <span className="text-gray-600 font-medium">Tổng tiền:</span>
                  <span className="font-bold text-2xl text-[#78A2D2]">
                    {formatPrice(orderInfo.total)}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 font-medium">Thanh toán:</span>
                  <span className="font-bold">
                    {orderInfo.paymentMethod === 'COD'
                      ? '💵 COD'
                      : orderInfo.paymentMethod === 'BANK'
                      ? '🏦 Chuyển khoản'
                      : '💳 Thẻ'}
                  </span>
                </div>
              </div>
            </div>

            <div className="bg-[#78A2D2]/10 rounded-2xl p-5 mb-6 border border-[#78A2D2]/30">
              <h3 className="font-bold text-gray-800 mb-3 text-lg">
                📦 Thông tin giao hàng:
              </h3>
              <ul className="text-sm text-gray-700 space-y-2">
                <li className="flex items-center gap-2">
                  <span className="text-[#78A2D2]">✓</span>
                  <span>Đơn hàng sẽ được xử lý trong 24 giờ</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#78A2D2]">✓</span>
                  <span>Thời gian giao hàng dự kiến: 3-5 ngày</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#78A2D2]">✓</span>
                  <span>Chúng tôi đã gửi xác nhận đến email của bạn</span>
                </li>
                <li className="flex items-center gap-2">
                  <span className="text-[#78A2D2]">✓</span>
                  <span>Bạn có thể theo dõi đơn hàng qua số điện thoại</span>
                </li>
              </ul>
            </div>

            <div className="flex gap-4">
              <button
                onClick={() => navigate('/')}
                className="flex-1 bg-[#FEFFAF] text-[#2C2C2C] py-4 rounded-2xl hover:bg-[#F0F09F] transition-all shadow-lg font-bold text-lg flex items-center justify-center gap-2 border-2 border-[#78A2D2]"
              >
                <Home className="size-5" />
                Về Trang Chủ
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}