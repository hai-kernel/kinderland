import React from 'react';
import { Link, useParams } from 'react-router-dom';
import ProductCard from '../shop/ProductCard';
import { products, brands } from '../../data/products';
import { ArrowLeft, Award } from 'lucide-react';
import Pagination from '../common/Pagination';

export default function BrandsPage() {
  const { brandName } = useParams();
  const [currentPage, setCurrentPage] = React.useState(1);
  const itemsPerPage = 20;

  const brandLogos: Record<string, string> = {
    'Hasbro': '🎮',
    'LEGO': '🧱',
    'Mattel': '🎪',
    'Bandai': '🤖',
    'Disney': '🏰',
    'Marvel': '⚡',
    'Hot Wheels': '🏎️',
    'NERF': '🎯',
    'Play-Doh': '🎨',
    'Fisher-Price': '👶',
    'Melissa & Doug': '🌈',
    'Little Tikes': '🎡',
    'Crayola': '🖍️',
    'Build-A-Bear': '🧸',
    'Pokemon': '⚡',
    'National Geographic': '🌍',
    'Robocar Poli': '🚓',
    'MGA Entertainment': '✨',
    'Nintendo': '🎮',
    'DJI': '🚁',
  };

  if (brandName) {
    const filteredProducts = products.filter((p) => p.brand === brandName);
    
    // Pagination
    const totalPages = Math.ceil(filteredProducts.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const currentProducts = filteredProducts.slice(startIndex, startIndex + itemsPerPage);

    return (
      <div className="bg-gray-50 min-h-screen">
        {/* Brand Header */}
        <div className="bg-gradient-to-r from-[#78A2D2] via-[#6A94C4] to-[#78A2D2] text-white py-12">
          <div className="container mx-auto px-4">
            <Link
              to="/brands"
              className="inline-flex items-center gap-2 text-white/90 hover:text-white mb-4"
            >
              <ArrowLeft className="size-5" />
              Quay lại thương hiệu
            </Link>
            <div className="flex items-center gap-4">
              <div className="text-6xl">{brandLogos[brandName] || '🎁'}</div>
              <div>
                <h1 className="text-4xl font-bold">{brandName}</h1>
                <p className="text-white/90 mt-2">
                  {filteredProducts.length} sản phẩm
                  {totalPages > 1 && ` - Trang ${currentPage}/${totalPages}`}
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Products */}
        <div className="container mx-auto px-4 py-8">
          {currentProducts.length === 0 ? (
            <div className="text-center py-20">
              <p className="text-gray-500 text-lg">Chưa có sản phẩm của thương hiệu này</p>
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

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="container mx-auto px-4 py-8">
        <h1 className="text-5xl font-bold mb-3 text-[#78A2D2]">
          Thương Hiệu
        </h1>
        <p className="text-gray-700 text-lg mb-8 font-semibold">
          Khám phá các thương hiệu đồ chơi hàng đầu thế giới
        </p>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-6">
          {brands.map((brand) => {
            const count = products.filter((p) => p.brand === brand).length;
            const logo = brandLogos[brand] || '🎁';

            return (
              <Link
                key={brand}
                to={`/brands/${brand}`}
                className="group"
              >
                <div className="bg-white rounded-2xl shadow-md hover:shadow-xl transition-all overflow-hidden p-8 text-center border-2 border-gray-100 hover:border-[#78A2D2]">
                  <div className="text-6xl mb-4">{logo}</div>
                  <h3 className="font-bold text-gray-800 mb-2 group-hover:text-[#78A2D2] transition-colors">
                    {brand}
                  </h3>
                  <p className="text-sm text-gray-500">{count} sản phẩm</p>
                </div>
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
}