import React, { useState, useEffect } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { ShoppingCart, User, LogOut, Search, Menu, X, Home, Percent, Grid, Package, Sparkles, BookOpen, Award, MapPin, UserCircle } from 'lucide-react';
import { useApp } from '../../context/AppContext';
import CartDropdown from './CartDropdown';
import { Logo } from '../common/Logo';
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu';

export default function Navbar() {
  const { user, logout, cart } = useApp();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [searchTerm, setSearchTerm] = useState('');
  const [showCartDropdown, setShowCartDropdown] = useState(false);
  const [isScrolled, setIsScrolled] = useState(false);

  // Detect scroll
  useEffect(() => {
    const handleScroll = () => {
      setIsScrolled(window.scrollY > 20);
    };

    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (searchTerm.trim()) {
      navigate(`/products?search=${searchTerm}`);
      setIsMenuOpen(false);
    }
  };

  const cartItemsCount = cart.reduce((sum, item) => sum + item.quantity, 0);

  const menuItems = [
    { name: 'Trang chủ', path: '/', icon: Home, color: 'from-blue-400 to-blue-500' },
    { name: 'Sản phẩm', path: '/products', icon: Package, color: 'from-blue-400 to-blue-500' },
    { name: 'Danh mục', path: '/categories', icon: Grid, color: 'from-blue-400 to-blue-500' },
    { name: 'Thương hiệu', path: '/brands', icon: Award, color: 'from-yellow-400 to-yellow-500' },
    { name: 'Khuyến mãi', path: '/discounts', icon: Percent, color: 'from-yellow-400 to-yellow-500' },
    { name: 'Hàng mới', path: '/new-arrivals', icon: Sparkles, color: 'from-yellow-400 to-yellow-500' },
    { name: 'Cửa hàng', path: '/stores', icon: MapPin, color: 'from-blue-400 to-blue-500' },
    { name: 'Blog', path: '/blog', icon: BookOpen, color: 'from-blue-400 to-blue-500' },
  ];

  const isActive = (path: string) => {
    if (path === '/') return location.pathname === '/';
    return location.pathname.startsWith(path);
  };

  return (
    <>
      {/* Header - Logo, Search, Cart, User */}
      <header className={`bg-[#78A2D2] border-b border-white/20 sticky top-0 z-50 transition-all duration-300 ${
        isScrolled ? 'shadow-2xl bg-[#78A2D2]/98 backdrop-blur-md' : 'shadow-lg'
      }`}>
        <div className="container mx-auto px-4">
          <div className={`flex items-center justify-between gap-4 transition-all duration-300 ${
            isScrolled ? 'py-3' : 'py-4'
          }`}>
            {/* Logo */}
            <Link to="/" className="flex-shrink-0">
              <Logo size="small" className="md:hidden" />
              <Logo size="default" className="hidden md:block" />
            </Link>

            {/* Search Bar - Desktop */}
            <form onSubmit={handleSearch} className="hidden lg:flex flex-1 max-w-2xl">
              <div className="relative w-full">
                <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 size-5 text-white/70" />
                <input
                  type="text"
                  placeholder="Tìm kiếm đồ chơi..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  className="w-full pl-12 pr-4 py-3 border-2 border-white/30 rounded-2xl focus:ring-2 focus:ring-[#FEFFAF] focus:border-[#FEFFAF] transition-all bg-white/10 text-white placeholder:text-white/60"
                />
              </div>
            </form>

            {/* Right Actions */}
            <div className="flex items-center gap-3">
              {/* Cart */}
              <div
                className="relative"
                onMouseEnter={() => setShowCartDropdown(true)}
                onMouseLeave={() => setShowCartDropdown(false)}
              >
                <Link
                  to="/cart"
                  className="relative p-3 hover:bg-white/10 rounded-xl transition-all"
                >
                  <ShoppingCart className="size-6 text-white" />
                  {cartItemsCount > 0 && (
                    <span className="absolute -top-1 -right-1 bg-[#FEFFAF] text-[#2C2C2C] text-xs rounded-full size-6 flex items-center justify-center font-bold shadow-lg">
                      {cartItemsCount}
                    </span>
                  )}
                </Link>

                {/* Cart Dropdown */}
                {showCartDropdown && <CartDropdown />}
              </div>

              {/* User Account */}
              {user ? (
                <div className="hidden md:block">
                  <DropdownMenu>
                    <DropdownMenuTrigger asChild>
                      <button className="flex items-center gap-2 px-4 py-2.5 bg-white rounded-xl hover:bg-white/90 transition-all shadow-md">
                        <User className="size-5 text-[#78A2D2]" />
                        <span className="text-sm font-semibold text-[#2C2C2C]">{user.name}</span>
                      </button>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent align="end" className="w-56">
                      <DropdownMenuLabel>Tài khoản của tôi</DropdownMenuLabel>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem asChild>
                        <Link to="/account" className="cursor-pointer">
                          <UserCircle className="mr-2 h-4 w-4" />
                          <span>Tổng quan</span>
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuItem asChild>
                        <Link to="/account/orders" className="cursor-pointer">
                          <Package className="mr-2 h-4 w-4" />
                          <span>Đơn hàng của tôi</span>
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuItem asChild>
                        <Link to="/account/loyalty" className="cursor-pointer">
                          <Award className="mr-2 h-4 w-4" />
                          <span>Điểm tích lũy</span>
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuItem asChild>
                        <Link to="/account/profile" className="cursor-pointer">
                          <User className="mr-2 h-4 w-4" />
                          <span>Thông tin tài khoản</span>
                        </Link>
                      </DropdownMenuItem>
                      <DropdownMenuSeparator />
                      <DropdownMenuItem onClick={handleLogout} className="cursor-pointer text-red-600">
                        <LogOut className="mr-2 h-4 w-4" />
                        <span>Đăng xuất</span>
                      </DropdownMenuItem>
                    </DropdownMenuContent>
                  </DropdownMenu>
                </div>
              ) : (
                <Link
                  to="/login"
                  className="hidden md:flex px-6 py-2.5 bg-[#FEFFAF] text-[#2C2C2C] rounded-xl hover:bg-[#F0F09F] transition-all shadow-md font-bold"
                >
                  Đăng Nhập
                </Link>
              )}

              {/* Mobile Menu Button */}
              <button
                onClick={() => setIsMenuOpen(!isMenuOpen)}
                className="lg:hidden p-2 hover:bg-white/10 rounded-lg text-white"
              >
                {isMenuOpen ? <X className="size-6" /> : <Menu className="size-6" />}
              </button>
            </div>
          </div>

          {/* Mobile Search */}
          <form onSubmit={handleSearch} className="lg:hidden pb-4">
            <div className="relative">
              <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 size-5 text-white/70" />
              <input
                type="text"
                placeholder="Tìm kiếm đồ chơi..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="w-full pl-12 pr-4 py-3 border-2 border-white/30 rounded-xl focus:ring-2 focus:ring-[#FEFFAF] focus:border-[#FEFFAF] transition-all bg-white/10 text-white placeholder:text-white/60"
              />
            </div>
          </form>
        </div>
      </header>

      {/* Navigation Menu - Icon on top, text below */}
      <nav className="bg-[#FEFFAF] shadow-2xl sticky top-[88px] z-40 border-b-4 border-[#78A2D2]/30">
        <div className="container mx-auto px-4">
          {/* Desktop Menu */}
          <div className="hidden lg:flex items-center justify-center gap-1 py-3">
            {menuItems.map((item) => {
              const Icon = item.icon;
              const active = isActive(item.path);
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={`flex flex-col items-center justify-center gap-2 px-5 py-3 rounded-2xl font-semibold transition-all min-w-[110px] ${
                    active
                      ? 'bg-[#78A2D2] text-white shadow-lg transform scale-105'
                      : 'text-[#2C2C2C] hover:bg-[#78A2D2]/20 hover:scale-105'
                  }`}
                >
                  <div className={`p-2.5 rounded-xl ${active ? 'bg-white/30 shadow-lg' : 'bg-[#78A2D2]/10'}`}>
                    <Icon className={`size-6 ${active ? 'text-white' : 'text-[#78A2D2]'}`} />
                  </div>
                  <span className="text-xs font-bold tracking-wide">{item.name}</span>
                </Link>
              );
            })}
          </div>
        </div>
      </nav>

      {/* Mobile Menu */}
      {isMenuOpen && (
        <div className="lg:hidden fixed inset-0 bg-black/50 z-50" onClick={() => setIsMenuOpen(false)}>
          <div className="bg-white w-80 h-full shadow-2xl overflow-y-auto" onClick={(e) => e.stopPropagation()}>
            <div className="p-4">
              {/* Close button */}
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-bold text-gray-800">Menu</h2>
                <button onClick={() => setIsMenuOpen(false)} className="p-2 hover:bg-gray-100 rounded-lg">
                  <X className="size-6" />
                </button>
              </div>

              {/* Mobile Menu Items */}
              <div className="space-y-2 mb-6">
                {menuItems.map((item) => {
                  const Icon = item.icon;
                  const active = isActive(item.path);
                  return (
                    <Link
                      key={item.path}
                      to={item.path}
                      onClick={() => setIsMenuOpen(false)}
                      className={`flex items-center gap-3 px-4 py-3 rounded-xl font-semibold transition-all ${
                        active
                          ? `bg-gradient-to-r ${item.color} text-white`
                          : 'text-gray-700 hover:bg-gray-100'
                      }`}
                    >
                      <Icon className="size-5" />
                      {item.name}
                    </Link>
                  );
                })}
              </div>

              {/* Mobile User Actions */}
              {user ? (
                <div className="pt-4 border-t border-gray-200 space-y-2">
                  <div className="flex items-center gap-2 px-4 py-3 bg-blue-50 rounded-xl">
                    <User className="size-5 text-blue-600" />
                    <span className="text-sm font-semibold text-gray-700">{user.name}</span>
                  </div>
                  <Link
                    to="/account"
                    onClick={() => setIsMenuOpen(false)}
                    className="flex items-center gap-2 px-4 py-3 text-gray-700 hover:bg-gray-100 rounded-xl font-semibold"
                  >
                    <UserCircle className="size-5" />
                    Tài khoản của tôi
                  </Link>
                  <button
                    onClick={() => {
                      handleLogout();
                      setIsMenuOpen(false);
                    }}
                    className="w-full flex items-center gap-2 px-4 py-3 text-red-600 hover:bg-red-50 rounded-xl font-semibold"
                  >
                    <LogOut className="size-5" />
                    Đăng xuất
                  </button>
                </div>
              ) : (
                <Link
                  to="/login"
                  onClick={() => setIsMenuOpen(false)}
                  className="flex items-center justify-center gap-2 px-6 py-3 bg-[#FEFFAF] text-[#2C2C2C] rounded-xl hover:bg-[#F0F09F] transition-all shadow-md font-bold"
                >
                  <User className="size-5" />
                  Đăng Nhập
                </Link>
              )}
            </div>
          </div>
        </div>
      )}
    </>
  );
}
