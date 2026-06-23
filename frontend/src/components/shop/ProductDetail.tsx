import React, { useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { products } from '../../data/products';
import { stores } from '../../data/stores';
import { useApp } from '../../context/AppContext';
import { ShoppingCart, Heart, Share2, Star, Truck, Shield, RefreshCw, MapPin, ChevronRight, ArrowLeft, Plus, Minus } from 'lucide-react';
import StoreAvailabilityModal from './StoreAvailabilityModal';
import { toast } from 'sonner@2.0.3';

export default function ProductDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToCart } = useApp();
  
  const product = products.find((p) => p.id === id);
  const [quantity, setQuantity] = useState(1);
  const [selectedType, setSelectedType] = useState(product?.types?.[0] || '');
  const [showStoreModal, setShowStoreModal] = useState(false);

  if (!product) {
    return (
      <div className="container mx-auto px-4 py-20 text-center">
        <p className="text-gray-500 text-lg">Không tìm thấy sản phẩm</p>
      </div>
    );
  }

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  // Check store availability
  const storesWithStock = stores.filter(store => {
    const stock = store.inventory[product.id];
    return stock !== undefined && stock > 0;
  });

  const handleAddToCart = () => {
    // Guest có thể thêm vào giỏ hàng, không cần đăng nhập
    addToCart(product, quantity, product.types ? selectedType : undefined);
    toast.success('✅ Thêm vào giỏ hàng thành công!', {
      description: `${quantity} x ${product.name}`,
      duration: 2000,
    });
    navigate('/cart');
  };

  const decreaseQuantity = () => {
    if (quantity > 1) setQuantity(quantity - 1);
  };

  const increaseQuantity = () => {
    if (quantity < product.stock) setQuantity(quantity + 1);
  };

  return (
    <div className="container mx-auto px-4 py-8 bg-gray-50 min-h-screen">
      <button
        onClick={() => navigate(-1)}
        className="flex items-center gap-2 text-[#78A2D2] hover:text-[#6A94C4] mb-6 font-semibold"
      >
        <ArrowLeft className="size-5" />
        Quay lại
      </button>

      <div className="grid md:grid-cols-2 gap-8">
        <div className="bg-white rounded-3xl overflow-hidden shadow-xl border border-gray-100">
          <img
            src={product.image}
            alt={product.name}
            className="w-full h-full object-cover"
          />
        </div>

        <div className="bg-white rounded-3xl shadow-xl p-8 border border-gray-100">
          <div className="mb-4">
            <span className="text-sm bg-[#78A2D2]/20 text-[#78A2D2] px-4 py-2 rounded-full font-semibold">
              {product.category}
            </span>
          </div>

          <h1 className="text-4xl font-bold text-gray-800 mb-4">
            {product.name}
          </h1>

          <div className="mb-4">
            <span className="text-sm text-gray-600">
              Thương hiệu: <span className="font-bold text-[#78A2D2]">{product.brand}</span>
            </span>
          </div>

          <p className="text-gray-600 mb-6 text-lg leading-relaxed">{product.description}</p>

          {product.discount ? (
            <div className="mb-6">
              <div className="flex items-center gap-3 mb-2">
                <span className="text-4xl font-bold text-[#78A2D2]">
                  {formatPrice(product.price)}
                </span>
                <span className="bg-red-500 text-white px-3 py-1 rounded-full font-bold">
                  Giảm {product.discount}%
                </span>
              </div>
              <span className="text-lg text-gray-400 line-through">
                Giá gốc: {formatPrice(product.originalPrice || 0)}
              </span>
            </div>
          ) : (
            <div className="text-4xl font-bold text-[#78A2D2] mb-6">
              {formatPrice(product.price)}
            </div>
          )}

          {product.types && product.types.length > 0 && (
            <div className="mb-6">
              <label className="block text-sm font-bold text-gray-700 mb-3">
                Chọn loại:
              </label>
              <div className="flex gap-2 flex-wrap">
                {product.types.map((type) => (
                  <button
                    key={type}
                    onClick={() => setSelectedType(type)}
                    className={`px-5 py-3 rounded-xl border-2 transition-all font-semibold ${
                      selectedType === type
                        ? 'border-[#78A2D2] bg-[#78A2D2] text-white shadow-lg'
                        : 'border-gray-300 hover:border-[#78A2D2] bg-white'
                    }`}
                  >
                    {type}
                  </button>
                ))}
              </div>
            </div>
          )}

          <div className="mb-6">
            <label className="block text-sm font-bold text-gray-700 mb-3">
              Số lượng:
            </label>
            <div className="flex items-center gap-4">
              <button
                onClick={decreaseQuantity}
                className="p-3 border-2 border-gray-300 rounded-xl hover:bg-[#78A2D2] hover:text-white hover:border-[#78A2D2] transition-all"
              >
                <Minus className="size-5" />
              </button>
              <span className="text-2xl font-bold w-16 text-center">
                {quantity}
              </span>
              <button
                onClick={increaseQuantity}
                className="p-3 border-2 border-gray-300 rounded-xl hover:bg-[#78A2D2] hover:text-white hover:border-[#78A2D2] transition-all"
                disabled={quantity >= product.stock}
              >
                <Plus className="size-5" />
              </button>
            </div>
            <p className="text-sm text-gray-500 mt-2 font-medium">
              Còn lại: {product.stock} sản phẩm
            </p>
          </div>

          <button
            onClick={handleAddToCart}
            className="w-full bg-[#FEFFAF] text-[#2C2C2C] py-4 rounded-2xl hover:bg-[#F0F09F] transition-all shadow-xl flex items-center justify-center gap-3 font-bold text-lg border-2 border-[#78A2D2]"
          >
            <ShoppingCart className="size-6" />
            Thêm vào giỏ hàng
          </button>

          {/* Find in Store Button */}
          <button
            onClick={() => setShowStoreModal(true)}
            className="w-full mt-4 bg-white border-2 border-[#78A2D2] text-[#78A2D2] py-4 rounded-2xl hover:bg-[#78A2D2]/10 transition-all flex items-center justify-center gap-3 font-bold text-lg"
          >
            <MapPin className="size-6" />
            Tìm tại cửa hàng
          </button>

          <div className="mt-6 p-5 bg-[#78A2D2]/10 rounded-2xl border-2 border-[#78A2D2]/30">
            <h3 className="font-bold text-gray-800 mb-3 text-lg">🎯 Chính sách:</h3>
            <ul className="text-sm text-gray-700 space-y-2">
              <li className="flex items-center gap-2">
                <span className="text-[#78A2D2]">✓</span>
                <span>Miễn phí vận chuyển đơn từ 500.000đ</span>
              </li>
              <li className="flex items-center gap-2">
                <span className="text-[#78A2D2]">✓</span>
                <span>Đổi trả trong 7 ngày</span>
              </li>
              <li className="flex items-center gap-2">
                <span className="text-[#78A2D2]">✓</span>
                <span>Bảo hành chính hãng</span>
              </li>
              <li className="flex items-center gap-2">
                <span className="text-[#78A2D2]">✓</span>
                <span>Hỗ trợ 24/7</span>
              </li>
            </ul>
          </div>
        </div>
      </div>

      {/* Store Availability */}
      <div className="bg-white rounded-3xl shadow-xl p-6 border border-gray-100 mb-6">
        <div className="flex items-center gap-2 mb-4">
          <MapPin className="size-5 text-[#78A2D2]" />
          <h3 className="font-bold text-gray-800">Tình Trạng Tại Cửa Hàng</h3>
        </div>
        
        {storesWithStock.length > 0 ? (
          <div className="space-y-3">
            {storesWithStock.slice(0, 3).map((store) => (
              <div key={store.id} className="p-3 bg-gray-50 rounded-xl">
                <div className="flex items-start justify-between gap-2">
                  <div className="flex-1 min-w-0">
                    <p className="font-semibold text-gray-800 line-clamp-1">
                      {store.name.replace('Kinderland ', '')}
                    </p>
                    <p className="text-sm text-gray-600 line-clamp-1">{store.district}</p>
                  </div>
                  <span className="text-[#78A2D2] font-bold text-sm whitespace-nowrap">
                    Còn {store.inventory[product.id]} sp
                  </span>
                </div>
              </div>
            ))}
            
            <Link
              to="/stores"
              className="flex items-center justify-center gap-2 text-[#78A2D2] hover:text-[#6A94C4] font-semibold text-sm py-2"
            >
              Xem tất cả {storesWithStock.length} cửa hàng
              <ChevronRight className="size-4" />
            </Link>
          </div>
        ) : (
          <p className="text-gray-500 text-sm">
            Sản phẩm này hiện không có tại cửa hàng. Vui lòng đặt hàng online.
          </p>
        )}
      </div>

      {/* Features */}
      <div className="grid grid-cols-1 gap-4 mb-6">
        <div className="bg-white rounded-3xl shadow-xl p-6 border border-gray-100">
          <h3 className="font-bold text-gray-800 mb-3 text-lg">🎯 Đặc Điểm:</h3>
          <ul className="text-sm text-gray-700 space-y-2">
            <li className="flex items-center gap-2">
              <span className="text-[#78A2D2]">✓</span>
              <span>Chất lượng cao</span>
            </li>
            <li className="flex items-center gap-2">
              <span className="text-[#78A2D2]">✓</span>
              <span>Thiết kế đẹp mắt</span>
            </li>
            <li className="flex items-center gap-2">
              <span className="text-[#78A2D2]">✓</span>
              <span>Chống nước</span>
            </li>
            <li className="flex items-center gap-2">
              <span className="text-[#78A2D2]">✓</span>
              <span>Kháng khuẩn</span>
            </li>
          </ul>
        </div>
      </div>

      {/* Store Availability Modal */}
      <StoreAvailabilityModal
        isOpen={showStoreModal}
        onClose={() => setShowStoreModal(false)}
        product={product}
      />
    </div>
  );
}