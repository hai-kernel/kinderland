import React, { useState, useMemo } from 'react';
import { X, MapPin, Search, ChevronDown, CheckCircle, AlertCircle, XCircle, Package } from 'lucide-react';
import { stores } from '../../data/stores';
import { Product } from '../../data/products';

interface StoreAvailabilityModalProps {
  isOpen: boolean;
  onClose: () => void;
  product: Product;
}

export default function StoreAvailabilityModal({ isOpen, onClose, product }: StoreAvailabilityModalProps) {
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCity, setSelectedCity] = useState('TP. Hồ Chí Minh');
  const [onlyShowInStock, setOnlyShowInStock] = useState(false);
  const [showAvailable, setShowAvailable] = useState(true);
  const [showLowStock, setShowLowStock] = useState(true);
  const [showOutOfStock, setShowOutOfStock] = useState(true);
  const [selectedStore, setSelectedStore] = useState(stores[0]);
  const [expandedStoreId, setExpandedStoreId] = useState<string | null>(null);

  const getStockStatus = (store: typeof stores[0]) => {
    const stock = store.inventory[product.id];
    if (stock === undefined) return { status: 'unavailable', text: 'Không bán', icon: Package, color: 'text-gray-400', bgColor: 'bg-gray-100' };
    if (stock === 0) return { status: 'out', text: 'Hết hàng', icon: XCircle, color: 'text-red-600', bgColor: 'bg-red-50' };
    if (stock < 5) return { status: 'low', text: 'Còn ít', icon: AlertCircle, color: 'text-orange-600', bgColor: 'bg-orange-50' };
    return { status: 'available', text: 'Có sẵn', icon: CheckCircle, color: 'text-green-600', bgColor: 'bg-green-50' };
  };

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND',
    }).format(price);
  };

  const filteredStores = useMemo(() => {
    return stores.filter((store) => {
      const stockInfo = getStockStatus(store);
      const matchesCity = store.city === selectedCity;
      const matchesSearch = 
        store.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
        store.address.toLowerCase().includes(searchQuery.toLowerCase()) ||
        store.district.toLowerCase().includes(searchQuery.toLowerCase());
      
      const hasStock = stockInfo.status !== 'unavailable' && stockInfo.status !== 'out';
      const matchesStockFilter = !onlyShowInStock || hasStock;

      const matchesStatusFilter = 
        (showAvailable && stockInfo.status === 'available') ||
        (showLowStock && stockInfo.status === 'low') ||
        (showOutOfStock && (stockInfo.status === 'out' || stockInfo.status === 'unavailable'));

      return matchesCity && matchesSearch && matchesStockFilter && matchesStatusFilter;
    });
  }, [selectedCity, searchQuery, onlyShowInStock, showAvailable, showLowStock, showOutOfStock, product.id]);

  const storesInStock = stores.filter(store => {
    const stock = store.inventory[product.id];
    return stock !== undefined && stock > 0;
  }).length;

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 z-50 flex items-center justify-center p-4">
      <div className="bg-white rounded-2xl shadow-2xl max-w-6xl w-full max-h-[90vh] overflow-hidden flex flex-col">
        {/* Header */}
        <div className="flex items-center justify-between p-6 border-b-2 border-gray-100">
          <h2 className="text-2xl font-bold text-gray-800">Tìm tại cửa hàng</h2>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-full transition-all"
          >
            <X className="size-6" />
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto">
          <div className="grid lg:grid-cols-2 gap-6 p-6">
            {/* Left Side - Filters & List */}
            <div className="space-y-6">
              {/* Product Info */}
              <div className="bg-gray-50 rounded-xl p-4 flex items-center gap-4">
                <img
                  src={product.image}
                  alt={product.name}
                  className="size-20 object-cover rounded-lg"
                />
                <div className="flex-1 min-w-0">
                  <h3 className="font-bold text-gray-800 line-clamp-2">{product.name}</h3>
                  <p className="text-lg font-bold text-blue-600 mt-1">
                    {formatPrice(product.price)}
                  </p>
                  <p className="text-sm text-gray-600">{product.brand}</p>
                </div>
              </div>

              {/* Link to full store finder */}
              <a
                href="/stores"
                target="_blank"
                rel="noopener noreferrer"
                className="text-sm text-blue-600 hover:underline font-semibold uppercase tracking-wide"
              >
                Kiểm tra tình trạng còn hàng →
              </a>

              {/* City Selector */}
              <div>
                <label className="block text-sm font-bold text-gray-700 mb-2">
                  Thành phố:
                </label>
                <select
                  value={selectedCity}
                  onChange={(e) => setSelectedCity(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500 font-semibold"
                >
                  <option value="TP. Hồ Chí Minh">TP. Hồ Chí Minh</option>
                  <option value="Hà Nội">Hà Nội</option>
                </select>
              </div>

              {/* Search */}
              <div className="relative">
                <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 size-5 text-gray-400" />
                <input
                  type="text"
                  placeholder="Tìm kiếm theo thành phố, địa chỉ hoặc mã bưu điện..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="w-full pl-12 pr-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                />
              </div>

              {/* Stock Filter */}
              <div className="flex items-center gap-3 p-4 bg-blue-50 rounded-xl border-2 border-blue-200">
                <input
                  type="checkbox"
                  id="onlyInStock"
                  checked={onlyShowInStock}
                  onChange={(e) => setOnlyShowInStock(e.target.checked)}
                  className="size-5 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
                />
                <label htmlFor="onlyInStock" className="font-semibold text-gray-800 cursor-pointer">
                  Chỉ hiển thị các cửa hàng còn sản phẩm
                </label>
              </div>

              <div className="text-sm text-gray-600">
                <strong>{storesInStock}</strong> trên tổng <strong>{stores.length}</strong> cửa hàng có sản phẩm này
              </div>

              {/* Status Filters */}
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="showAvailable"
                    checked={showAvailable}
                    onChange={(e) => setShowAvailable(e.target.checked)}
                    className="size-5 rounded border-gray-300 text-green-600 focus:ring-green-500"
                  />
                  <CheckCircle className="size-5 text-green-600" />
                  <label htmlFor="showAvailable" className="text-sm font-semibold text-gray-700 cursor-pointer">
                    Có sẵn
                  </label>
                </div>

                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="showLowStock"
                    checked={showLowStock}
                    onChange={(e) => setShowLowStock(e.target.checked)}
                    className="size-5 rounded border-gray-300 text-orange-600 focus:ring-orange-500"
                  />
                  <AlertCircle className="size-5 text-orange-600" />
                  <label htmlFor="showLowStock" className="text-sm font-semibold text-gray-700 cursor-pointer">
                    Còn ít
                  </label>
                </div>

                <div className="flex items-center gap-3">
                  <input
                    type="checkbox"
                    id="showOutOfStock"
                    checked={showOutOfStock}
                    onChange={(e) => setShowOutOfStock(e.target.checked)}
                    className="size-5 rounded border-gray-300 text-red-600 focus:ring-red-500"
                  />
                  <XCircle className="size-5 text-red-600" />
                  <label htmlFor="showOutOfStock" className="text-sm font-semibold text-gray-700 cursor-pointer">
                    Hết hàng
                  </label>
                </div>
              </div>

              {/* Store List */}
              <div className="space-y-3 max-h-96 overflow-y-auto">
                {filteredStores.map((store) => {
                  const stockInfo = getStockStatus(store);
                  const Icon = stockInfo.icon;
                  const isExpanded = expandedStoreId === store.id;
                  const stock = store.inventory[product.id];

                  return (
                    <div
                      key={store.id}
                      className="border-2 border-gray-200 rounded-xl overflow-hidden hover:border-blue-300 transition-all"
                    >
                      <button
                        onClick={() => {
                          setSelectedStore(store);
                          setExpandedStoreId(isExpanded ? null : store.id);
                        }}
                        className="w-full p-4 text-left flex items-start justify-between gap-3"
                      >
                        <div className="flex-1 min-w-0">
                          <h3 className="font-bold text-gray-800 mb-1">
                            {store.name.replace('Kinderland ', '')}
                          </h3>
                          <p className="text-sm text-gray-600 line-clamp-1">{store.address}</p>
                          <p className="text-xs text-gray-500">{store.district}</p>
                        </div>
                        <div className="flex flex-col items-end gap-2 flex-shrink-0">
                          <div className={`flex items-center gap-2 px-3 py-1 rounded-full ${stockInfo.bgColor}`}>
                            <Icon className={`size-4 ${stockInfo.color}`} />
                            <span className={`text-sm font-bold ${stockInfo.color}`}>
                              {stockInfo.text}
                            </span>
                          </div>
                          <ChevronDown className={`size-5 text-gray-400 transition-transform ${isExpanded ? 'rotate-180' : ''}`} />
                        </div>
                      </button>

                      {isExpanded && (
                        <div className="px-4 pb-4 pt-2 border-t-2 border-gray-100 space-y-2">
                          <div className="flex items-start gap-2">
                            <MapPin className="size-4 text-blue-600 mt-0.5 flex-shrink-0" />
                            <p className="text-sm text-gray-700">{store.address}, {store.district}, {store.city}</p>
                          </div>
                          <p className="text-sm text-gray-700">📞 {store.phone}</p>
                          <p className="text-sm text-gray-700">🕐 {store.openHours}</p>
                          {stock !== undefined && stock > 0 && (
                            <p className="text-sm font-bold text-green-600">Còn {stock} sản phẩm</p>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}

                {filteredStores.length === 0 && (
                  <div className="text-center py-8">
                    <p className="text-gray-500">Không tìm thấy cửa hàng</p>
                  </div>
                )}
              </div>
            </div>

            {/* Right Side - Map */}
            <div className="lg:sticky lg:top-0">
              <div className="bg-gray-100 rounded-xl overflow-hidden shadow-lg h-[600px]">
                <iframe
                  width="100%"
                  height="100%"
                  frameBorder="0"
                  scrolling="no"
                  marginHeight={0}
                  marginWidth={0}
                  src={`https://maps.google.com/maps?q=${selectedStore.lat},${selectedStore.lng}&hl=vi&z=14&output=embed`}
                  title={selectedStore.name}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}
