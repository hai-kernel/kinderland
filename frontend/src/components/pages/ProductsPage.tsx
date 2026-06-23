import React, { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import ProductCard from '../shop/ProductCard';
import { products, brands, categories } from '../../data/products';
import { SlidersHorizontal } from 'lucide-react';
import Pagination from '../common/Pagination';

export default function ProductsPage() {
  const [searchParams] = useSearchParams();
  const [sortBy, setSortBy] = useState('default');
  const [priceRange, setPriceRange] = useState('all');
  const [selectedBrand, setSelectedBrand] = useState('all');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [currentPage, setCurrentPage] = useState(1);
  const itemsPerPage = 20;
  const searchTerm = searchParams.get('search') || '';

  const filteredProducts = products.filter((product) => {
    const matchesSearch =
      product.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.category.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.description.toLowerCase().includes(searchTerm.toLowerCase()) ||
      product.brand.toLowerCase().includes(searchTerm.toLowerCase());

    const matchesPrice =
      priceRange === 'all' ||
      (priceRange === 'under300' && product.price < 300000) ||
      (priceRange === '300-600' && product.price >= 300000 && product.price < 600000) ||
      (priceRange === '600-1000' && product.price >= 600000 && product.price < 1000000) ||
      (priceRange === 'over1000' && product.price >= 1000000);

    const matchesBrand = selectedBrand === 'all' || product.brand === selectedBrand;
    const matchesCategory = selectedCategory === 'all' || product.category === selectedCategory;

    return matchesSearch && matchesPrice && matchesBrand && matchesCategory;
  });

  const sortedProducts = [...filteredProducts].sort((a, b) => {
    if (sortBy === 'price-asc') return a.price - b.price;
    if (sortBy === 'price-desc') return b.price - a.price;
    if (sortBy === 'name') return a.name.localeCompare(b.name);
    return 0;
  });

  // Pagination
  const totalPages = Math.ceil(sortedProducts.length / itemsPerPage);
  const startIndex = (currentPage - 1) * itemsPerPage;
  const endIndex = startIndex + itemsPerPage;
  const currentProducts = sortedProducts.slice(startIndex, endIndex);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  // Reset to page 1 when filters change
  useEffect(() => {
    setCurrentPage(1);
  }, [searchTerm, sortBy, priceRange, selectedBrand, selectedCategory]);

  return (
    <div className="bg-gray-50 min-h-screen">
      <div className="container mx-auto px-4 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-5xl font-bold mb-3 text-[#78A2D2]">
            {searchTerm ? `Kết quả tìm kiếm: \"${searchTerm}\"` : 'Tất Cả Sản Phẩm'}
          </h1>
          <p className="text-gray-700 text-lg font-semibold">
            Tìm thấy <span className="font-bold text-[#78A2D2]">{sortedProducts.length}</span> sản phẩm
            {totalPages > 1 && ` - Trang ${currentPage}/${totalPages}`}
          </p>
        </div>

        {/* Filters and Sort */}
        <div className="bg-white rounded-xl shadow-md p-4 mb-6">
          <div className="flex items-center gap-2 mb-4">
            <SlidersHorizontal className="size-5 text-[#78A2D2]" />
            <h3 className="font-bold text-gray-800">Bộ lọc & Sắp xếp</h3>
          </div>
          <div className="grid md:grid-cols-4 gap-4">
            <div>
              <label className="block text-sm font-bold text-gray-800 mb-2">
                Sắp xếp theo:
              </label>
              <select
                value={sortBy}
                onChange={(e) => setSortBy(e.target.value)}
                className="w-full px-4 py-2 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] font-semibold text-gray-800"
              >
                <option value="default">Mặc định</option>
                <option value="price-asc">Giá: Thấp đến Cao</option>
                <option value="price-desc">Giá: Cao đến Thấp</option>
                <option value="name">Tên: A-Z</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-800 mb-2">
                Khoảng giá:
              </label>
              <select
                value={priceRange}
                onChange={(e) => setPriceRange(e.target.value)}
                className="w-full px-4 py-2 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] font-semibold text-gray-800"
              >
                <option value="all">Tất cả</option>
                <option value="under300">Dưới 300.000đ</option>
                <option value="300-600">300.000đ - 600.000đ</option>
                <option value="600-1000">600.000đ - 1.000.000đ</option>
                <option value="over1000">Trên 1.000.000đ</option>
              </select>
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-800 mb-2">
                Thương hiệu:
              </label>
              <select
                value={selectedBrand}
                onChange={(e) => setSelectedBrand(e.target.value)}
                className="w-full px-4 py-2 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] font-semibold text-gray-800"
              >
                <option value="all">Tất cả thương hiệu</option>
                {brands.map((brand) => (
                  <option key={brand} value={brand}>
                    {brand}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label className="block text-sm font-bold text-gray-800 mb-2">
                Danh mục:
              </label>
              <select
                value={selectedCategory}
                onChange={(e) => setSelectedCategory(e.target.value)}
                className="w-full px-4 py-2 border-2 border-gray-200 rounded-lg focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] font-semibold text-gray-800"
              >
                <option value="all">Tất cả danh mục</option>
                {categories.map((category) => (
                  <option key={category} value={category}>
                    {category}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>

        {/* Products Grid */}
        {currentProducts.length === 0 ? (
          <div className="text-center py-20">
            <p className="text-gray-500 text-lg">Không tìm thấy sản phẩm nào</p>
          </div>
        ) : (
          <>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4 md:gap-6">
              {currentProducts.map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>

            {/* Pagination */}
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