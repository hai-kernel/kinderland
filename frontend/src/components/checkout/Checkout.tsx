import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { CreditCard, Truck, Tag, AlertCircle } from 'lucide-react';

export default function Checkout() {
  const { cart, user, voucher, applyVoucher, removeVoucher, clearCart } = useApp();
  const navigate = useNavigate();

  const [customerInfo, setCustomerInfo] = useState({
    name: user?.name || '',
    phone: user?.phone || '',
    address: user?.address || '',
  });

  const [voucherCode, setVoucherCode] = useState('');
  const [voucherError, setVoucherError] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'COD' | 'BANK' | 'CARD'>('COD');

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const subtotal = cart.reduce(
    (sum, item) => sum + item.product.price * item.quantity,
    0
  );

  const shippingFee = subtotal >= 500000 ? 0 : 30000;
  
  let discount = 0;
  if (voucher) {
    discount = voucher.type === 'percentage' 
      ? (subtotal * voucher.discount) / 100
      : voucher.discount;
  }

  const total = subtotal + shippingFee - discount;

  const handleApplyVoucher = () => {
    setVoucherError('');
    const success = applyVoucher(voucherCode);
    if (success) {
      setVoucherCode('');
    } else {
      setVoucherError('Mã giảm giá không hợp lệ');
    }
  };

  const handlePlaceOrder = (e: React.FormEvent) => {
    e.preventDefault();

    if (paymentMethod === 'COD') {
      // COD - Direct to success
      clearCart();
      navigate('/order-success', { 
        state: { 
          orderInfo: { 
            ...customerInfo, 
            total, 
            paymentMethod,
            orderDate: new Date().toISOString()
          } 
        } 
      });
    } else {
      // Other payment methods - Show payment processing
      navigate('/payment', { 
        state: { 
          orderInfo: { 
            ...customerInfo, 
            total, 
            paymentMethod,
            orderDate: new Date().toISOString()
          } 
        } 
      });
    }
  };

  if (cart.length === 0) {
    navigate('/');
    return null;
  }

  return (
    <div className="bg-gray-50 min-h-screen">
      {/* Hero Banner */}
      <div className="bg-gradient-to-r from-[#78A2D2] via-[#6A94C4] to-[#78A2D2] text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-3 bg-white/20 px-6 py-3 rounded-full mb-4 backdrop-blur-sm">
            <CreditCard className="size-6" />
            <span className="font-bold text-lg">THANH TOÁN</span>
          </div>
          <h1 className="text-5xl font-bold mb-4">
            Hoàn Tất Đơn Hàng
          </h1>
          <p className="text-xl text-white/90">
            {cart.length} sản phẩm trong giỏ hàng
          </p>
        </div>
      </div>
      
      <div className="container mx-auto px-4 py-8">
        <form onSubmit={handlePlaceOrder}>
          <div className="grid lg:grid-cols-3 gap-8">
            <div className="lg:col-span-2 space-y-6">
              {/* Customer Information */}
              <div className="bg-white rounded-2xl shadow-lg p-6 border-2 border-gray-200">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 bg-[#78A2D2] rounded-xl">
                    <Truck className="size-6 text-white" />
                  </div>
                  <h2 className="text-xl font-bold text-gray-800">
                    Thông Tin Giao Hàng
                  </h2>
                </div>

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-2">
                      Họ và Tên *
                    </label>
                    <input
                      type="text"
                      value={customerInfo.name}
                      onChange={(e) =>
                        setCustomerInfo({ ...customerInfo, name: e.target.value })
                      }
                      className="w-full px-4 py-3 border-2 border-gray-200 bg-white text-gray-800 rounded-xl focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] transition-all"
                      placeholder="Nguyễn Văn A"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-2">
                      Số Điện Thoại *
                    </label>
                    <input
                      type="tel"
                      value={customerInfo.phone}
                      onChange={(e) =>
                        setCustomerInfo({ ...customerInfo, phone: e.target.value })
                      }
                      className="w-full px-4 py-3 border-2 border-gray-200 bg-white text-gray-800 rounded-xl focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] transition-all"
                      placeholder="0912345678"
                      required
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-bold text-gray-700 mb-2">
                      Địa Chỉ *
                    </label>
                    <textarea
                      value={customerInfo.address}
                      onChange={(e) =>
                        setCustomerInfo({ ...customerInfo, address: e.target.value })
                      }
                      className="w-full px-4 py-3 border-2 border-gray-200 bg-white text-gray-800 rounded-xl focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] transition-all"
                      placeholder="Số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố"
                      rows={3}
                      required
                    />
                  </div>
                </div>
              </div>

              {/* Voucher */}
              <div className="bg-white rounded-2xl shadow-lg p-6 border-2 border-gray-200">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 bg-[#FEFFAF] rounded-xl border-2 border-[#78A2D2]">
                    <Tag className="size-6 text-[#78A2D2]" />
                  </div>
                  <h2 className="text-xl font-bold text-gray-800">Mã Giảm Giá</h2>
                </div>

                {voucher ? (
                  <div className="flex items-center justify-between p-4 bg-[#FEFFAF]/30 border-2 border-[#78A2D2] rounded-xl">
                    <div>
                      <p className="font-bold text-[#78A2D2] text-lg">
                        Mã: {voucher.code}
                      </p>
                      <p className="text-sm text-gray-600">
                        Giảm {voucher.type === 'percentage' 
                          ? `${voucher.discount}%` 
                          : formatPrice(voucher.discount)}
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={removeVoucher}
                      className="text-red-500 hover:text-red-600 text-sm font-semibold px-3 py-1 hover:bg-red-50 rounded-lg transition-all"
                    >
                      Xóa
                    </button>
                  </div>
                ) : (
                  <div>
                    <div className="flex gap-2">
                      <input
                        type="text"
                        value={voucherCode}
                        onChange={(e) => {
                          setVoucherCode(e.target.value.toUpperCase());
                          setVoucherError('');
                        }}
                        className="flex-1 px-4 py-3 border-2 border-gray-200 bg-white text-gray-800 rounded-xl focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] transition-all font-semibold"
                        placeholder="Nhập mã giảm giá"
                      />
                      <button
                        type="button"
                        onClick={handleApplyVoucher}
                        className="px-6 py-3 bg-[#FEFFAF] text-[#2C2C2C] rounded-xl hover:bg-[#F0F09F] transition-all shadow-lg font-bold border-2 border-[#78A2D2]"
                      >
                        Áp dụng
                      </button>
                    </div>
                    {voucherError && (
                      <p className="text-red-500 text-sm mt-2 flex items-center gap-1 font-semibold">
                        <AlertCircle className="size-4" />
                        {voucherError}
                      </p>
                    )}
                    <div className="mt-4 p-4 bg-[#78A2D2]/10 rounded-xl border border-[#78A2D2]/30">
                      <p className="font-bold mb-2 text-gray-800">🎁 Mã khả dụng:</p>
                      <div className="space-y-1 text-sm text-gray-600">
                        <p className="font-semibold">• GIAM10 - Giảm 10%</p>
                        <p className="font-semibold">• GIAM50K - Giảm 50.000đ</p>
                        <p className="font-semibold">• FREESHIP - Giảm 30.000đ phí ship</p>
                      </div>
                    </div>
                  </div>
                )}
              </div>

              {/* Payment Method */}
              <div className="bg-white rounded-2xl shadow-lg p-6 border-2 border-gray-200">
                <div className="flex items-center gap-3 mb-4">
                  <div className="p-2 bg-[#78A2D2] rounded-xl">
                    <CreditCard className="size-6 text-white" />
                  </div>
                  <h2 className="text-xl font-bold text-gray-800">
                    Phương Thức Thanh Toán
                  </h2>
                </div>

                <div className="space-y-3">
                  <label className="flex items-center gap-3 p-4 border-2 border-gray-200 rounded-xl cursor-pointer hover:bg-gray-50 transition-all has-[:checked]:border-[#78A2D2] has-[:checked]:bg-[#78A2D2]/10">
                    <input
                      type="radio"
                      name="payment"
                      value="COD"
                      checked={paymentMethod === 'COD'}
                      onChange={(e) => setPaymentMethod(e.target.value as 'COD')}
                      className="size-5 text-[#78A2D2]"
                    />
                    <div className="flex-1">
                      <p className="font-bold text-gray-800">💵 Thanh toán khi nhận hàng (COD)</p>
                      <p className="text-sm text-gray-600">
                        Thanh toán bằng tiền mặt khi nhận hàng
                      </p>
                    </div>
                  </label>

                  <label className="flex items-center gap-3 p-4 border-2 border-gray-200 rounded-xl cursor-pointer hover:bg-gray-50 transition-all has-[:checked]:border-[#78A2D2] has-[:checked]:bg-[#78A2D2]/10">
                    <input
                      type="radio"
                      name="payment"
                      value="BANK"
                      checked={paymentMethod === 'BANK'}
                      onChange={(e) => setPaymentMethod(e.target.value as 'BANK')}
                      className="size-5 text-[#78A2D2]"
                    />
                    <div className="flex-1">
                      <p className="font-bold text-gray-800">🏦 Chuyển khoản ngân hàng</p>
                      <p className="text-sm text-gray-600">
                        Chuyển khoản qua tài khoản ngân hàng
                      </p>
                    </div>
                  </label>

                  <label className="flex items-center gap-3 p-4 border-2 border-gray-200 rounded-xl cursor-pointer hover:bg-gray-50 transition-all has-[:checked]:border-[#78A2D2] has-[:checked]:bg-[#78A2D2]/10">
                    <input
                      type="radio"
                      name="payment"
                      value="CARD"
                      checked={paymentMethod === 'CARD'}
                      onChange={(e) => setPaymentMethod(e.target.value as 'CARD')}
                      className="size-5 text-[#78A2D2]"
                    />
                    <div className="flex-1">
                      <p className="font-bold text-gray-800">💳 Thẻ tín dụng / Ghi nợ</p>
                      <p className="text-sm text-gray-600">
                        Thanh toán bằng thẻ Visa, Mastercard
                      </p>
                    </div>
                  </label>
                </div>
              </div>
            </div>

            {/* Order Summary */}
            <div className="lg:col-span-1">
              <div className="bg-white rounded-2xl shadow-lg p-6 sticky top-24 border-2 border-gray-200">
                <h2 className="text-2xl font-bold text-gray-800 mb-4">
                  📦 Đơn Hàng
                </h2>

                <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
                  {cart.map((item, index) => (
                    <div key={`${item.product.id}-${item.type}-${index}`} className="flex gap-3 p-2 hover:bg-gray-50 rounded-lg transition-all">
                      <img
                        src={item.product.image}
                        alt={item.product.name}
                        className="w-16 h-16 object-cover rounded-lg"
                      />
                      <div className="flex-1">
                        <p className="text-sm font-bold text-gray-800">{item.product.name}</p>
                        {item.type && <p className="text-xs text-gray-500">{item.type}</p>}
                        <p className="text-sm text-gray-600 font-semibold">x{item.quantity}</p>
                      </div>
                      <p className="text-sm font-bold text-[#78A2D2]">
                        {formatPrice(item.product.price * item.quantity)}
                      </p>
                    </div>
                  ))}
                </div>

                <div className="border-t-2 border-dashed border-gray-300 pt-4 space-y-3">
                  <div className="flex justify-between text-gray-600">
                    <span>Tạm tính:</span>
                    <span className="font-semibold">{formatPrice(subtotal)}</span>
                  </div>
                  <div className="flex justify-between text-gray-600">
                    <span>Phí vận chuyển:</span>
                    <span className="font-semibold">{formatPrice(shippingFee)}</span>
                  </div>
                  {discount > 0 && (
                    <div className="flex justify-between text-[#78A2D2]">
                      <span className="font-semibold">Giảm giá:</span>
                      <span className="font-bold">-{formatPrice(discount)}</span>
                    </div>
                  )}
                  <div className="border-t-2 border-dashed border-gray-300 pt-3 flex justify-between font-bold text-xl">
                    <span className="text-gray-800">Tổng cộng:</span>
                    <span className="text-[#78A2D2] text-2xl">{formatPrice(total)}</span>
                  </div>
                </div>

                <button
                  type="submit"
                  className="w-full mt-6 bg-[#FEFFAF] text-[#2C2C2C] py-4 rounded-xl hover:bg-[#F0F09F] transition-all shadow-lg font-bold text-lg border-2 border-[#78A2D2]"
                >
                  Đặt Hàng
                </button>

                <p className="text-xs text-gray-500 text-center mt-4">
                  Bằng việc đặt hàng, bạn đồng ý với điều khoản sử dụng
                </p>
              </div>
            </div>
          </div>
        </form>
      </div>
    </div>
  );
}
