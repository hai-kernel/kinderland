import React from 'react';
import ProductCard from '../shop/ProductCard';
import { products } from '../../data/products';
import { Percent, TrendingDown } from 'lucide-react';
import Pagination from '../common/Pagination';

export default function DiscountsPage() {
  const [currentPage, setCurrentPage] = React.useState(1);
  const itemsPerPage = 20;
  
  // Filter products with discounts
  const discountedProducts = products.filter((p) => p.discount);
  
  // Pagination
  const totalPages = Math.ceil(discountedProducts.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const currentProducts = discountedProducts.slice(startIndex, startIndex + itemsPerPage);

  const vouchers = [
    { code: 'GIAM10', discount: '10%', desc: 'Giảm 10% cho tất cả đơn hàng', color: 'from-red-500 to-orange-500' },
    { code: 'GIAM50K', discount: '50.000đ', desc: 'Giảm 50.000đ cho đơn từ 500.000đ', color: 'from-blue-500 to-blue-600' },
    { code: 'FREESHIP', discount: '30.000đ', desc: 'Giảm 30.000đ phí vận chuyển', color: 'from-yellow-400 to-yellow-500' },
    { code: 'NEWUSER', discount: '15%', desc: 'Giảm 15% cho khách hàng mới', color: 'from-purple-500 to-pink-500' },
  ];

  return (
    <div className="bg-gray-50 min-h-screen">
      {/* Hero Banner */}
      <div className="bg-gradient-to-r from-[#78A2D2] via-[#6A94C4] to-[#78A2D2] text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-3 bg-white/20 px-6 py-3 rounded-full mb-4 backdrop-blur-sm">
            <Percent className="size-6" />
            <span className="font-bold text-lg">KHUYẾN MÃI ĐẶC BIỆT</span>
          </div>
          <h1 className="text-5xl font-bold mb-4">
            Giảm Giá Khủng
          </h1>
          <p className="text-xl text-white/90">
            Tiết kiệm tối đa với các ưu đãi hấp dẫn
          </p>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="flex items-center gap-3 mb-2">
          <Percent className="size-8 text-[#78A2D2]" />
          <h1 className="text-4xl font-bold text-[#78A2D2]">Khuyến Mãi Hot</h1>
        </div>
        <p className="text-gray-700 mb-8 font-semibold">
          {discountedProducts.length} sản phẩm đang giảm giá
        </p>

        {currentProducts.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500 text-lg">Hiện không có sản phẩm khuyến mãi</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 md:gap-6">
              {currentProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            <Pagination
              currentPage={currentPage}
              totalPages={totalPages}
              onPageChange={(page) => {
                setCurrentPage(page);
                window.scrollTo({ top: 0, behavior: 'smooth' });
              }}
            />
          </>
        )}
      </div>
    </div>
  );
}