import React from 'react';
import { Link, useParams } from 'react-router-dom';
import ProductCard from '../shop/ProductCard';
import { products, categories } from '../../data/products';
import { ArrowLeft, Grid } from 'lucide-react';
import Pagination from '../common/Pagination';

export default function CategoriesPage() {
  const { categoryName } = useParams();
  const [currentPage, setCurrentPage] = React.useState(1);
  const itemsPerPage = 20;

  const allCategories = Array.from(new Set(products.map((p) => p.category)));

  const categoryColors: Record<string, string> = {
    'Mô hình & Robot': 'from-blue-500 to-cyan-500',
    'Đồ chơi xếp hình': 'from-green-500 to-emerald-500',
    'Búp bê & Phụ kiện': 'from-pink-500 to-rose-500',
    'Xe điều khiển': 'from-orange-500 to-red-500',
    'Thú nhồi bông': 'from-purple-500 to-violet-500',
    'Đồ chơi nhập vai': 'from-yellow-500 to-amber-500',
    'Đồ chơi sáng tạo': 'from-indigo-500 to-blue-500',
    'Trò chơi trí tuệ': 'from-teal-500 to-cyan-500',
    'Đồ chơi vận động': 'from-red-500 to-pink-500',
  };

  const categoryImages: Record<string, string> = {
    'Mô hình & Robot': 'https://images.unsplash.com/photo-1546776230-bb86256870ce?w=400',
    'Đồ chơi xếp hình': 'https://images.unsplash.com/photo-1672267273720-053bee27b9a2?w=400',
    'Búp bê & Phụ kiện': 'https://images.unsplash.com/photo-1612506001235-f0d0892aa11b?w=400',
    'Xe điều khiển': 'https://images.unsplash.com/photo-1613404196612-e058bb5aa01a?w=400',
    'Thú nhồi bông': 'https://images.unsplash.com/photo-1602734846297-9299fc2d4703?w=400',
    'Đồ chơi nhập vai': 'https://images.unsplash.com/photo-1765918158524-5cb518ab23af?w=400',
    'Đồ chơi sáng tạo': 'https://images.unsplash.com/photo-1727768351795-2390d19b2b41?w=400',
    'Trò chơi trí tuệ': 'https://images.unsplash.com/photo-1661352960828-1225fa3001f6?w=400',
    'Đồ chơi vận động': 'https://images.unsplash.com/photo-1594950988426-374080113536?w=400',
  };

  if (categoryName) {
    const filteredProducts = products.filter((p) => p.category === categoryName);
    
    // Pagination
    const totalPages = Math.ceil(filteredProducts.length / itemsPerPage);
    const startIndex = (currentPage - 1) * itemsPerPage;
    const currentProducts = filteredProducts.slice(startIndex, startIndex + itemsPerPage);

    return (
      <div className="bg-gray-50 min-h-screen">
        {/* Category Header */}
        <div className="bg-gradient-to-r from-[#78A2D2] via-[#6A94C4] to-[#78A2D2] text-white py-12">
          <div className="container mx-auto px-4">
            <Link
              to="/categories"
              className="inline-flex items-center gap-2 text-white/90 hover:text-white mb-4"
            >
              <ArrowLeft className="size-5" />
              Quay lại danh mục
            </Link>
            <h1 className="text-4xl font-bold">{categoryName}</h1>
            <p className="text-white/90 mt-2">
              {filteredProducts.length} sản phẩm
              {totalPages > 1 && ` - Trang ${currentPage}/${totalPages}`}
            </p>
          </div>
        </div>

        {/* Products */}
        <div className="container mx-auto px-4 py-8">
          {currentProducts.length === 0 ? (
            <div className="text-center py-20">
              <p className="text-gray-500 text-lg">Chưa có sản phẩm trong danh mục này</p>
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
          Danh Mục Sản Phẩm
        </h1>
        <p className="text-gray-700 text-lg mb-8 font-semibold">
          Khám phá các danh mục đồ chơi đa dạng
        </p>

        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {allCategories.map((category) => {
            const gradient = categoryColors[category] || 'from-blue-500 to-indigo-500';
            const image = categoryImages[category] || 'https://images.unsplash.com/photo-1545558014-8692077e9b5c?w=400';
            const count = products.filter((p) => p.category === category).length;

            return (
              <Link
                key={category}
                to={`/categories/${category}`}
                className="group"
              >
                <div className="bg-white rounded-2xl shadow-md hover:shadow-xl transition-all overflow-hidden">
                  <div className="relative aspect-square overflow-hidden">
                    <img
                      src={image}
                      alt={category}
                      className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-300"
                    />
                    <div className={`absolute inset-0 bg-gradient-to-t ${gradient} opacity-60 group-hover:opacity-70 transition-opacity`}></div>
                    <div className="absolute inset-0 flex items-center justify-center">
                      <div className="text-center text-white">
                        <h3 className="font-bold text-lg mb-1">{category}</h3>
                        <p className="text-sm text-white/90">{count} sản phẩm</p>
                      </div>
                    </div>
                  </div>
                </div>
              </Link>
            );
          })}
        </div>
      </div>
    </div>
  );
}