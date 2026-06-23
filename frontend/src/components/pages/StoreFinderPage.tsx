import React, { useState } from 'react';
import { stores, cities } from '../../data/stores';
import { MapPin, Phone, Clock, Search, ChevronDown } from 'lucide-react';

export default function StoreFinderPage() {
  const [selectedCity, setSelectedCity] = useState('TP. Hồ Chí Minh');
  const [selectedStore, setSelectedStore] = useState(stores[0]);
  const [searchQuery, setSearchQuery] = useState('');
  const [expandedStoreId, setExpandedStoreId] = useState<string | null>(null);

  const filteredStores = stores.filter((store) => {
    const matchesCity = store.city === selectedCity;
    const matchesSearch = 
      store.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
      store.address.toLowerCase().includes(searchQuery.toLowerCase()) ||
      store.district.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCity && matchesSearch;
  });

  return (
    <div className="bg-gray-50 min-h-screen">
      {/* Header */}
      <div className="bg-gradient-to-r from-[#78A2D2] via-[#6A94C4] to-[#78A2D2] text-white py-16">
        <div className="container mx-auto px-4 text-center">
          <div className="inline-flex items-center gap-3 bg-white/20 px-6 py-3 rounded-full mb-4 backdrop-blur-sm">
            <MapPin className="size-6" />
            <span className="font-bold text-lg">HỆ THỐNG CỬA HÀNG</span>
          </div>
          <h1 className="text-5xl font-bold mb-4">
            Tìm Cửa Hàng Kinderland
          </h1>
          <p className="text-xl text-white/90">
            {stores.length} cửa hàng trên toàn quốc
          </p>
        </div>
      </div>

      <div className="container mx-auto px-4 py-8">
        <div className="grid lg:grid-cols-3 gap-6">
          {/* Left Sidebar - Store List */}
          <div className="lg:col-span-1">
            <div className="bg-white rounded-2xl shadow-lg p-6 sticky top-4">
              {/* City Selector */}
              <div className="mb-4">
                <label className="block text-sm font-bold text-gray-800 mb-2">
                  Chọn thành phố:
                </label>
                <select
                  value={selectedCity}
                  onChange={(e) => setSelectedCity(e.target.value)}
                  className="w-full px-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2] font-semibold text-gray-800"
                >
                  {cities.map((city) => (
                    <option key={city} value={city}>
                      {city}
                    </option>
                  ))}
                </select>
              </div>

              {/* Search */}
              <div className="mb-6">
                <div className="relative">
                  <Search className="absolute left-4 top-1/2 transform -translate-y-1/2 size-5 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Tìm cửa hàng..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-12 pr-4 py-3 border-2 border-gray-200 rounded-xl focus:ring-2 focus:ring-[#78A2D2] focus:border-[#78A2D2]"
                  />
                </div>
              </div>

              <div className="mb-4">
                <p className="text-sm text-gray-700 font-semibold">
                  <strong>{filteredStores.length}</strong> cửa hàng tại {selectedCity}
                </p>
              </div>

              {/* Store List */}
              <div className="space-y-3 max-h-[600px] overflow-y-auto">
                {filteredStores.map((store) => {
                  const isExpanded = expandedStoreId === store.id;
                  
                  return (
                    <div
                      key={store.id}
                      className={`border-2 rounded-xl overflow-hidden transition-all ${
                        selectedStore.id === store.id
                          ? 'border-[#78A2D2] bg-[#78A2D2]/10'
                          : 'border-gray-200 hover:border-[#78A2D2]/50'
                      }`}
                    >
                      <button
                        onClick={() => {
                          setSelectedStore(store);
                          setExpandedStoreId(isExpanded ? null : store.id);
                        }}
                        className="w-full p-4 text-left flex items-center justify-between gap-2"
                      >
                        <div className="flex-1 min-w-0">
                          <h3 className="font-bold text-gray-800 line-clamp-1">
                            {store.name.replace('Kinderland ', '')}
                          </h3>
                          <p className="text-sm text-gray-600 line-clamp-1">{store.address}</p>
                          <p className="text-xs text-gray-500">{store.district}</p>
                        </div>
                        <ChevronDown className={`size-5 text-gray-400 flex-shrink-0 transition-transform ${isExpanded ? 'rotate-180' : ''}`} />
                      </button>

                      {isExpanded && (
                        <div className="px-4 pb-4 pt-2 border-t-2 border-gray-100 space-y-2 bg-white">
                          <div className="flex items-start gap-2">
                            <MapPin className="size-4 text-[#78A2D2] mt-0.5 flex-shrink-0" />
                            <p className="text-sm text-gray-700">
                              {store.address}, {store.district}, {store.city}
                            </p>
                          </div>
                          <div className="flex items-center gap-2">
                            <Phone className="size-4 text-[#78A2D2] flex-shrink-0" />
                            <p className="text-sm text-gray-700">{store.phone}</p>
                          </div>
                          <div className="flex items-center gap-2">
                            <Clock className="size-4 text-[#78A2D2] flex-shrink-0" />
                            <p className="text-sm text-gray-700">{store.openHours}</p>
                          </div>
                          <div className="mt-3 pt-3 border-t border-gray-200">
                            <p className="text-xs text-[#78A2D2] font-semibold">• Đang mở cửa</p>
                          </div>
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>

              {filteredStores.length === 0 && (
                <div className="text-center py-8">
                  <p className="text-gray-500">Không tìm thấy cửa hàng</p>
                </div>
              )}
            </div>
          </div>

          {/* Right Content - Map & Store Details */}
          <div className="lg:col-span-2 space-y-6">
            {/* Map */}
            <div className="bg-white rounded-2xl shadow-lg overflow-hidden">
              <div className="aspect-video bg-gradient-to-br from-[#78A2D2]/20 to-[#6A94C4]/20 relative">
                <iframe
                  width="100%"
                  height="100%"
                  frameBorder="0"
                  scrolling="no"
                  marginHeight={0}
                  marginWidth={0}
                  src={`https://maps.google.com/maps?q=${selectedStore.lat},${selectedStore.lng}&hl=vi&z=16&output=embed`}
                  title={selectedStore.name}
                />
                <div className="absolute top-4 left-4 bg-white px-4 py-2 rounded-xl shadow-lg">
                  <p className="text-sm font-bold text-gray-800">📍 {filteredStores.length} cửa hàng</p>
                </div>
              </div>
            </div>

            {/* Store Details Card */}
            <div className="bg-white rounded-2xl shadow-lg p-8">
              <h2 className="text-3xl font-bold text-gray-800 mb-6">
                {selectedStore.name}
              </h2>
              
              <div className="grid md:grid-cols-2 gap-6">
                <div className="space-y-4">
                  <div className="flex items-start gap-3">
                    <MapPin className="size-6 text-[#78A2D2] mt-1 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-bold text-gray-700 mb-1">Địa chỉ</p>
                      <p className="text-gray-600">{selectedStore.address}</p>
                      <p className="text-gray-600">{selectedStore.district}, {selectedStore.city}</p>
                    </div>
                  </div>
                  
                  <div className="flex items-start gap-3">
                    <Phone className="size-6 text-[#78A2D2] mt-1 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-bold text-gray-700 mb-1">Số điện thoại</p>
                      <a href={`tel:${selectedStore.phone}`} className="text-[#78A2D2] hover:underline">
                        {selectedStore.phone}
                      </a>
                    </div>
                  </div>
                  
                  <div className="flex items-start gap-3">
                    <Clock className="size-6 text-[#78A2D2] mt-1 flex-shrink-0" />
                    <div>
                      <p className="text-sm font-bold text-gray-700 mb-1">Giờ mở cửa</p>
                      <p className="text-gray-600">{selectedStore.openHours}</p>
                      <p className="text-sm text-[#78A2D2] font-semibold mt-1">• Đang mở cửa</p>
                    </div>
                  </div>
                </div>

                {/* Right side - Image placeholder or additional info */}
                <div className="bg-gradient-to-br from-[#78A2D2]/20 to-[#6A94C4]/20 rounded-xl p-6 flex items-center justify-center">
                  <div className="text-center">
                    <div className="text-6xl mb-4">🏪</div>
                    <h3 className="font-bold text-gray-800 mb-2">
                      Chào mừng đến Kinderland
                    </h3>
                    <p className="text-sm text-gray-600">
                      Hơn 1000+ đồ chơi chất lượng cao
                    </p>
                  </div>
                </div>
              </div>

              {/* Additional Features */}
              <div className="mt-8 pt-6 border-t-2 border-gray-100">
                <h3 className="font-bold text-gray-800 mb-4">Dịch vụ tại cửa hàng</h3>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                  <div className="text-center p-4 bg-[#FEFFAF]/30 rounded-xl border border-[#78A2D2]/20">
                    <div className="text-3xl mb-2">🎁</div>
                    <p className="text-sm font-semibold text-gray-700">Gói quà miễn phí</p>
                  </div>
                  <div className="text-center p-4 bg-[#78A2D2]/10 rounded-xl border border-[#78A2D2]/20">
                    <div className="text-3xl mb-2">✅</div>
                    <p className="text-sm font-semibold text-gray-700">Đổi trả 30 ngày</p>
                  </div>
                  <div className="text-center p-4 bg-[#FEFFAF]/30 rounded-xl border border-[#78A2D2]/20">
                    <div className="text-3xl mb-2">🎮</div>
                    <p className="text-sm font-semibold text-gray-700">Khu vui chơi</p>
                  </div>
                  <div className="text-center p-4 bg-[#78A2D2]/10 rounded-xl border border-[#78A2D2]/20">
                    <div className="text-3xl mb-2">🅿️</div>
                    <p className="text-sm font-semibold text-gray-700">Bãi đỗ xe miễn phí</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}