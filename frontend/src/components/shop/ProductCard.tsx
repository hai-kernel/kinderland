import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Star, ShoppingCart, Heart } from 'lucide-react';
import { Product } from '../../types';
import { useApp } from '../../context/AppContext';
import { toast } from 'sonner@2.0.3';

interface ProductCardProps {
  product: Product;
  featured?: boolean;
}

export default function ProductCard({ product, featured = false }: ProductCardProps) {
  const { addToCart } = useApp();
  const navigate = useNavigate();
  
  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  // Calculate discount percentage if there's a sale
  const discountPercent = product.originalPrice 
    ? Math.round(((product.originalPrice - product.price) / product.originalPrice) * 100)
    : 0;

  // Determine badge
  const getBadge = () => {
    if (discountPercent > 0) {
      return { text: `-${discountPercent}%`, color: 'bg-red-500' };
    }
    if (product.isNew) {
      return { text: 'MỚI', color: 'bg-[#FEFFAF] text-[#2C2C2C]' };
    }
    if (product.isBestSeller) {
      return { text: 'PHỔ BIẾN', color: 'bg-[#78A2D2]' };
    }
    return null;
  };

  const badge = getBadge();

  // Generate rating stars
  const rating = product.rating || 4.5;
  const fullStars = Math.floor(rating);
  const hasHalfStar = rating % 1 !== 0;

  return (
    <Link
      to={`/product/${product.id}`}
      className={`group block bg-white rounded-2xl overflow-hidden shadow-lg hover:shadow-2xl transition-all duration-300 border-2 border-[#78A2D2]/30 hover:border-[#78A2D2] ${
        featured ? 'transform hover:scale-105' : 'hover:-translate-y-1'
      }`}
    >
      <div className="relative overflow-hidden aspect-square bg-gray-50">
        <img
          src={product.image}
          alt={product.name}
          className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"
        />
        
        {/* Badge */}
        {badge && (
          <span className={`absolute top-3 right-3 ${badge.color} text-white text-xs font-bold px-3 py-1.5 rounded-full shadow-lg`}>
            {badge.text}
          </span>
        )}

        {/* Wishlist Icon */}
        <button
          onClick={(e) => {
            e.preventDefault();
            // Handle wishlist
          }}
          className="absolute top-3 left-3 bg-white/90 backdrop-blur-sm p-2.5 rounded-full hover:bg-[#FEFFAF] hover:scale-110 transition-all shadow-md group/heart"
        >
          <Heart className="size-5 text-[#78A2D2] group-hover/heart:fill-[#78A2D2]" />
        </button>
      </div>

      {/* Content */}
      <div className="p-4 bg-white">
        {/* Category */}
        <p className="text-xs text-[#78A2D2] font-semibold mb-1 uppercase tracking-wide">
          {product.category}
        </p>

        {/* Name */}
        <h3 className="font-bold text-[#2C2C2C] mb-2 line-clamp-2 min-h-[2.5rem] group-hover:text-[#78A2D2] transition-colors text-[15px]">
          {product.name}
        </h3>

        {/* Rating */}
        <div className="flex items-center gap-2 mb-3">
          <div className="flex items-center gap-0.5">
            {[...Array(fullStars)].map((_, i) => (
              <Star key={i} className="size-4 fill-[#FEFFAF] text-[#FEFFAF] stroke-[#78A2D2]" />
            ))}
            {hasHalfStar && (
              <Star className="size-4 fill-[#FEFFAF] text-[#FEFFAF] stroke-[#78A2D2]" style={{ clipPath: 'inset(0 50% 0 0)' }} />
            )}
            {[...Array(5 - fullStars - (hasHalfStar ? 1 : 0))].map((_, i) => (
              <Star key={`empty-${i}`} className="size-4 text-gray-300" />
            ))}
          </div>
          <span className="text-xs text-gray-500">({product.reviewCount || 0})</span>
        </div>

        {/* Price */}
        <div className="flex items-baseline gap-2 mb-3">
          <span className="text-xl font-bold text-[#78A2D2]">
            {formatPrice(product.price)}
          </span>
          {product.originalPrice && (
            <span className="text-sm text-gray-400 line-through">
              {formatPrice(product.originalPrice)}
            </span>
          )}
        </div>

        {/* Add to Cart Button */}
        <button
          onClick={(e) => {
            e.preventDefault();
            addToCart(product, 1);
            toast.success('✅ Thêm vào giỏ hàng thành công!', {
              description: `${product.name}`,
              duration: 2000,
            });
          }}
          className="w-full bg-[#FEFFAF] text-[#2C2C2C] hover:bg-[#F0F09F] py-2.5 rounded-lg font-bold flex items-center justify-center gap-2 opacity-0 group-hover:opacity-100 transition-all duration-300 shadow-md hover:shadow-lg border-2 border-[#78A2D2]"
        >
          <ShoppingCart className="size-5" />
          Thêm vào giỏ
        </button>
      </div>
    </Link>
  );
}
