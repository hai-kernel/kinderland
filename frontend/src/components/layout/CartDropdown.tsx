import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { Trash2, ShoppingCart, Minus, Plus } from 'lucide-react';

export default function CartDropdown() {
  const { cart, removeFromCart, updateCartItem, user } = useApp();
  const navigate = useNavigate();

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

  const updateQuantity = (productId: string, currentQuantity: number, newQuantity: number, type?: string) => {
    if (newQuantity > 0 && newQuantity <= 99) {
      updateCartItem(productId, newQuantity, type);
    }
  };

  const handleCheckout = () => {
    if (!user) {
      navigate('/login');
      return;
    }
    navigate('/checkout');
  };

  if (cart.length === 0) {
    return (
      <div className="absolute top-full right-0 w-96 z-50 -mt-2 pt-4 pb-2">
        <div className="bg-white rounded-2xl shadow-2xl border-2 border-[#78A2D2]/30 p-6">
          <div className="text-center py-8">
            <ShoppingCart className="size-16 text-gray-300 mx-auto mb-3" />
            <p className="text-[#2C2C2C] font-semibold">Giỏ hàng trống</p>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="absolute top-full right-0 w-96 z-50 -mt-2 pt-4 pb-2">
      <div className="bg-white rounded-2xl shadow-2xl border-2 border-[#78A2D2]/30 p-6">
        <h3 className="font-bold text-[#2C2C2C] text-lg mb-4">
          Giỏ hàng ({cart.reduce((sum, item) => sum + item.quantity, 0)})
        </h3>
        
        {/* Cart Items - Max height with scroll */}
        <div className="max-h-[300px] overflow-y-auto space-y-4 mb-4">
          {cart.map((item, index) => (
            <div
              key={`${item.product.id}-${item.type || 'default'}-${index}`}
              className="flex gap-3 p-3 hover:bg-[#78A2D2]/10 rounded-lg transition-colors border border-gray-100"
            >
              {/* Product Image */}
              <div className="w-20 h-20 flex-shrink-0 bg-gray-50 rounded-lg overflow-hidden border border-[#78A2D2]/20">
                <img
                  src={item.product.image}
                  alt={item.product.name}
                  className="w-full h-full object-cover"
                />
              </div>

              {/* Product Info */}
              <div className="flex-1 min-w-0">
                <h4 className="font-semibold text-[#2C2C2C] text-sm line-clamp-2 mb-1">
                  {item.product.name}
                </h4>
                {item.type && (
                  <p className="text-xs text-gray-500 mb-2">Loại: {item.type}</p>
                )}
                
                {/* Quantity Controls */}
                <div className="flex items-center gap-2">
                  <button
                    onClick={() => updateQuantity(item.product.id, item.quantity, item.quantity - 1, item.type)}
                    className="w-7 h-7 flex items-center justify-center border-2 border-[#78A2D2]/40 text-[#78A2D2] rounded-full hover:bg-[#78A2D2]/20 transition-colors"
                  >
                    <Minus className="size-4" />
                  </button>
                  <input
                    type="number"
                    value={item.quantity}
                    onChange={(e) => {
                      const val = parseInt(e.target.value) || 1;
                      updateQuantity(item.product.id, item.quantity, val, item.type);
                    }}
                    className="w-12 h-7 text-center border-2 border-[#78A2D2]/30 bg-white text-[#2C2C2C] rounded-lg font-semibold text-sm focus:outline-none focus:border-[#78A2D2]"
                    min="1"
                    max="99"
                  />
                  <button
                    onClick={() => updateQuantity(item.product.id, item.quantity, item.quantity + 1, item.type)}
                    className="w-7 h-7 flex items-center justify-center border-2 border-[#78A2D2]/40 text-[#78A2D2] rounded-full hover:bg-[#78A2D2]/20 transition-colors"
                  >
                    <Plus className="size-4" />
                  </button>
                </div>
              </div>

              {/* Price and Delete */}
              <div className="flex flex-col items-end justify-between">
                <button
                  onClick={() => removeFromCart(item.product.id)}
                  className="text-red-400 hover:text-red-300 p-1 hover:bg-red-500/20 rounded transition-colors"
                >
                  <Trash2 className="size-4" />
                </button>
                <p className="font-bold text-[#78A2D2] text-sm">
                  {formatPrice(item.product.price * item.quantity)}
                </p>
              </div>
            </div>
          ))}
        </div>

        {/* Divider */}
        <div className="border-t-2 border-dashed border-[#78A2D2]/30 my-4"></div>

        {/* Total */}
        <div className="bg-[#FEFFAF]/30 rounded-xl p-4 mb-4 border border-[#78A2D2]/20">
          <div className="flex justify-between items-center">
            <span className="font-semibold text-[#2C2C2C]">Tổng cộng</span>
            <span className="font-bold text-[#78A2D2] text-xl">
              {formatPrice(subtotal)}
            </span>
          </div>
        </div>

        {/* Terms */}
        <div className="mb-4">
          <label className="flex items-start gap-2 text-xs text-gray-600 cursor-pointer">
            <input
              type="checkbox"
              defaultChecked
              className="mt-0.5 rounded border-[#78A2D2]/30"
            />
            <span>
              Tôi đã đọc và đồng ý với{' '}
              <a href="#" className="text-[#78A2D2] hover:underline">
                Chính sách bảo mật
              </a>{' '}
              và{' '}
              <a href="#" className="text-[#78A2D2] hover:underline">
                Điều kiện thanh toán
              </a>
            </span>
          </label>
        </div>

        {/* Action Buttons */}
        <div className="flex gap-3">
          <Link
            to="/cart"
            className="flex-1 flex items-center justify-center gap-2 px-4 py-3 border-2 border-[#78A2D2] text-[#78A2D2] rounded-xl hover:bg-[#78A2D2]/20 transition-colors font-semibold"
          >
            <ShoppingCart className="size-4" />
            Xem giỏ hàng
          </Link>
          <button
            onClick={handleCheckout}
            className="flex-1 flex items-center justify-center gap-2 px-4 py-3 bg-[#FEFFAF] text-[#2C2C2C] rounded-xl hover:bg-[#F0F09F] transition-colors font-semibold shadow-lg border-2 border-[#78A2D2]"
          >
            Thanh toán ngay
          </button>
        </div>
      </div>
    </div>
  );
}
