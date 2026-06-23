import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { useApp } from '../../context/AppContext';
import { Card, CardContent, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Badge } from '../ui/badge';
import { Progress } from '../ui/progress';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../ui/tabs';
import { toast } from 'sonner@2.0.3';
import {
  Star,
  Gift,
  TrendingUp,
  Award,
  Calendar,
  ArrowLeft,
  ShoppingBag,
  Sparkles,
  Crown,
  Trophy,
} from 'lucide-react';

interface Reward {
  id: string;
  name: string;
  points: number;
  image: string;
  description: string;
  category: 'voucher' | 'gift' | 'product';
  stock: number;
}

interface PointHistory {
  id: string;
  date: string;
  description: string;
  points: number;
  type: 'earn' | 'redeem';
}

export default function LoyaltyPoints() {
  const { user } = useApp();
  const [selectedTab, setSelectedTab] = useState('overview');

  // Mock data
  const customerPoints = {
    current: user?.points || 0,
    lifetime: (user?.points || 0) + 1700,
    expiringSoon: 200,
    expiryDate: '2026-03-31',
    tier: user?.membershipTier || 'bronze',
    nextTier: user?.membershipTier === 'gold' ? 'platinum' : user?.membershipTier === 'silver' ? 'gold' : 'silver',
    pointsToNextTier: 500,
  };

  const tierBenefits = {
    bronze: {
      name: 'Đồng',
      icon: '🥉',
      color: 'orange',
      benefits: ['Tích 1 điểm cho 10,000₫', 'Sinh nhật giảm 5%'],
    },
    silver: {
      name: 'Bạc',
      icon: '🥈',
      color: 'gray',
      benefits: ['Tích 1.5 điểm cho 10,000₫', 'Sinh nhật giảm 10%', 'Miễn phí ship đơn >500k'],
    },
    gold: {
      name: 'Vàng',
      icon: '👑',
      color: 'yellow',
      benefits: [
        'Tích 2 điểm cho 10,000₫',
        'Sinh nhật giảm 15%',
        'Miễn phí ship mọi đơn',
        'Ưu tiên hỗ trợ',
      ],
    },
    platinum: {
      name: 'Bạch kim',
      icon: '💎',
      color: 'purple',
      benefits: [
        'Tích 3 điểm cho 10,000₫',
        'Sinh nhật giảm 20%',
        'Miễn phí ship + đóng gói VIP',
        'Hỗ trợ 24/7',
        'Truy cập sớm sản phẩm mới',
      ],
    },
  };

  const rewards: Reward[] = [
    {
      id: 'r1',
      name: 'Voucher giảm 50,000₫',
      points: 500,
      image: 'https://images.unsplash.com/photo-1607083206869-4c7672e72a8a?w=300',
      description: 'Áp dụng cho đơn hàng từ 500,000₫',
      category: 'voucher',
      stock: 100,
    },
    {
      id: 'r2',
      name: 'Voucher giảm 100,000₫',
      points: 900,
      image: 'https://images.unsplash.com/photo-1607083206968-13611e3d76db?w=300',
      description: 'Áp dụng cho đơn hàng từ 1,000,000₫',
      category: 'voucher',
      stock: 50,
    },
    {
      id: 'r3',
      name: 'Miễn phí vận chuyển',
      points: 300,
      image: 'https://images.unsplash.com/photo-1566576721346-d4a3b4eaeb55?w=300',
      description: 'Freeship cho 1 đơn hàng bất kỳ',
      category: 'voucher',
      stock: 200,
    },
    {
      id: 'r4',
      name: 'Gấu bông Teddy',
      points: 1200,
      image: 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=300',
      description: 'Gấu bông Teddy 30cm đáng yêu',
      category: 'gift',
      stock: 20,
    },
    {
      id: 'r5',
      name: 'Set 12 bút màu cao cấp',
      points: 800,
      image: 'https://images.unsplash.com/photo-1513542789411-b6a5d4f31634?w=300',
      description: 'Set bút màu chất lượng cao cho bé',
      category: 'product',
      stock: 30,
    },
    {
      id: 'r6',
      name: 'Bộ đồ chơi xếp hình mini',
      points: 600,
      image: 'https://images.unsplash.com/photo-1515488764276-beab7607c1e6?w=300',
      description: 'Bộ đồ chơi xếp hình 50 chi tiết',
      category: 'product',
      stock: 40,
    },
  ];

  const pointHistory: PointHistory[] = [
    {
      id: 'h1',
      date: '2026-01-10',
      description: 'Mua đơn hàng #KDL2026011401',
      points: 260,
      type: 'earn',
    },
    {
      id: 'h2',
      date: '2026-01-08',
      description: 'Đổi Voucher giảm 50,000₫',
      points: -500,
      type: 'redeem',
    },
    {
      id: 'h3',
      date: '2026-01-05',
      description: 'Mua đơn hàng #KDL2026010501',
      points: 178,
      type: 'earn',
    },
    {
      id: 'h4',
      date: '2025-12-28',
      description: 'Mua đơn hàng #KDL2025122801',
      points: 690,
      type: 'earn',
    },
    {
      id: 'h5',
      date: '2025-12-25',
      description: 'Điểm thưởng sinh nhật',
      points: 200,
      type: 'earn',
    },
  ];

  const handleRedeemReward = (reward: Reward) => {
    if (customerPoints.current >= reward.points) {
      toast.success(`Đã đổi thành công: ${reward.name}!`);
    } else {
      toast.error('Bạn không đủ điểm để đổi quà này!');
    }
  };

  const tierProgress = (customerPoints.current / (customerPoints.current + customerPoints.pointsToNextTier)) * 100;

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="container mx-auto px-4">
        {/* Header */}
        <div className="mb-6">
          <Link to="/account">
            <Button variant="ghost" className="mb-4">
              <ArrowLeft className="w-4 h-4 mr-2" />
              Quay lại tài khoản
            </Button>
          </Link>
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            Điểm tích lũy & Phần thưởng
          </h1>
          <p className="text-gray-600">
            Quản lý điểm thưởng và đổi quà hấp dẫn
          </p>
        </div>

        <Tabs value={selectedTab} onValueChange={setSelectedTab}>
          <TabsList className="mb-6">
            <TabsTrigger value="overview">Tổng quan</TabsTrigger>
            <TabsTrigger value="rewards">Đổi quà</TabsTrigger>
            <TabsTrigger value="history">Lịch sử điểm</TabsTrigger>
          </TabsList>

          {/* Overview Tab */}
          <TabsContent value="overview" className="space-y-6">
            {/* Current Points Card */}
            <Card className="bg-gradient-to-r from-indigo-600 to-blue-600 text-white">
              <CardContent className="p-8">
                <div className="flex items-center justify-between mb-6">
                  <div>
                    <p className="text-blue-100 mb-2">Điểm hiện tại</p>
                    <div className="flex items-baseline gap-2">
                      <span className="text-5xl font-bold">
                        {customerPoints.current.toLocaleString('vi-VN')}
                      </span>
                      <span className="text-2xl text-blue-100">điểm</span>
                    </div>
                  </div>
                  <Star className="w-16 h-16 text-yellow-300 fill-yellow-300" />
                </div>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-blue-100">
                    Tổng điểm tích lũy: {customerPoints.lifetime.toLocaleString('vi-VN')}
                  </span>
                  <span className="text-yellow-300 flex items-center gap-1">
                    <Sparkles className="w-4 h-4" />
                    {customerPoints.expiringSoon} điểm hết hạn {customerPoints.expiryDate}
                  </span>
                </div>
              </CardContent>
            </Card>

            {/* Membership Progress */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Crown className="w-5 h-5 text-yellow-500" />
                  Hạng thành viên
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="mb-4">
                  <div className="flex items-center justify-between mb-2">
                    <div className="flex items-center gap-2">
                      <span className="text-2xl">
                        {tierBenefits[customerPoints.tier as keyof typeof tierBenefits].icon}
                      </span>
                      <span className="font-bold text-lg">
                        {tierBenefits[customerPoints.tier as keyof typeof tierBenefits].name}
                      </span>
                    </div>
                    <div className="text-right">
                      <p className="text-sm text-gray-600">Cần thêm</p>
                      <p className="font-bold text-indigo-600">
                        {customerPoints.pointsToNextTier} điểm
                      </p>
                    </div>
                  </div>
                  <Progress value={tierProgress} className="h-3" />
                  <p className="text-sm text-gray-600 mt-2">
                    để lên hạng{' '}
                    <span className="font-bold">
                      {tierBenefits[customerPoints.nextTier as keyof typeof tierBenefits].name}
                    </span>
                  </p>
                </div>

                {/* Current Tier Benefits */}
                <div className="bg-gray-50 p-4 rounded-lg">
                  <p className="font-medium mb-2">Quyền lợi hiện tại:</p>
                  <ul className="space-y-2">
                    {tierBenefits[customerPoints.tier as keyof typeof tierBenefits].benefits.map(
                      (benefit, index) => (
                        <li key={index} className="flex items-center gap-2 text-sm">
                          <Award className="w-4 h-4 text-green-600" />
                          {benefit}
                        </li>
                      )
                    )}
                  </ul>
                </div>
              </CardContent>
            </Card>

            {/* All Tiers */}
            <Card>
              <CardHeader>
                <CardTitle>Các hạng thành viên</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  {Object.entries(tierBenefits).map(([key, tier]) => (
                    <div
                      key={key}
                      className={`p-4 rounded-lg border-2 ${
                        key === customerPoints.tier
                          ? 'border-indigo-600 bg-indigo-50'
                          : 'border-gray-200'
                      }`}
                    >
                      <div className="text-center mb-3">
                        <span className="text-4xl">{tier.icon}</span>
                        <p className="font-bold mt-2">{tier.name}</p>
                      </div>
                      <ul className="space-y-1">
                        {tier.benefits.slice(0, 2).map((benefit, index) => (
                          <li key={index} className="text-xs text-gray-600">
                            • {benefit}
                          </li>
                        ))}
                        {tier.benefits.length > 2 && (
                          <li className="text-xs text-gray-500">
                            +{tier.benefits.length - 2} quyền lợi khác
                          </li>
                        )}
                      </ul>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>

            {/* How to Earn Points */}
            <Card>
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <TrendingUp className="w-5 h-5 text-green-600" />
                  Cách tích điểm
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div className="flex items-start gap-3 p-4 bg-green-50 rounded-lg">
                    <ShoppingBag className="w-5 h-5 text-green-600 mt-1" />
                    <div>
                      <p className="font-medium">Mua hàng</p>
                      <p className="text-sm text-gray-600">
                        Tích 2 điểm cho mỗi 10,000₫ (Hạng Vàng)
                      </p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3 p-4 bg-blue-50 rounded-lg">
                    <Gift className="w-5 h-5 text-blue-600 mt-1" />
                    <div>
                      <p className="font-medium">Sinh nhật</p>
                      <p className="text-sm text-gray-600">
                        Nhận 200 điểm vào ngày sinh nhật
                      </p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3 p-4 bg-purple-50 rounded-lg">
                    <Star className="w-5 h-5 text-purple-600 mt-1" />
                    <div>
                      <p className="font-medium">Đánh giá sản phẩm</p>
                      <p className="text-sm text-gray-600">
                        Nhận 10 điểm cho mỗi đánh giá
                      </p>
                    </div>
                  </div>
                  <div className="flex items-start gap-3 p-4 bg-orange-50 rounded-lg">
                    <Trophy className="w-5 h-5 text-orange-600 mt-1" />
                    <div>
                      <p className="font-medium">Giới thiệu bạn bè</p>
                      <p className="text-sm text-gray-600">
                        Nhận 100 điểm khi bạn bè đăng ký
                      </p>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          {/* Rewards Tab */}
          <TabsContent value="rewards">
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {rewards.map((reward) => (
                <Card key={reward.id} className="hover:shadow-lg transition-shadow">
                  <CardContent className="p-4">
                    <img
                      src={reward.image}
                      alt={reward.name}
                      className="w-full h-48 object-cover rounded-lg mb-4"
                    />
                    <div className="mb-4">
                      <div className="flex items-start justify-between mb-2">
                        <h3 className="font-bold text-lg">{reward.name}</h3>
                        <Badge
                          variant={
                            reward.category === 'voucher'
                              ? 'default'
                              : reward.category === 'gift'
                              ? 'secondary'
                              : 'outline'
                          }
                        >
                          {reward.category === 'voucher'
                            ? 'Voucher'
                            : reward.category === 'gift'
                            ? 'Quà tặng'
                            : 'Sản phẩm'}
                        </Badge>
                      </div>
                      <p className="text-sm text-gray-600 mb-3">
                        {reward.description}
                      </p>
                      <div className="flex items-center justify-between mb-2">
                        <div className="flex items-center gap-1">
                          <Star className="w-5 h-5 text-yellow-500 fill-yellow-500" />
                          <span className="font-bold text-lg text-indigo-600">
                            {reward.points.toLocaleString('vi-VN')}
                          </span>
                          <span className="text-sm text-gray-500">điểm</span>
                        </div>
                        <span className="text-sm text-gray-500">
                          Còn {reward.stock}
                        </span>
                      </div>
                    </div>
                    <Button
                      onClick={() => handleRedeemReward(reward)}
                      disabled={customerPoints.current < reward.points}
                      className="w-full"
                    >
                      {customerPoints.current >= reward.points ? (
                        <>
                          <Gift className="w-4 h-4 mr-2" />
                          Đổi ngay
                        </>
                      ) : (
                        `Cần thêm ${(reward.points - customerPoints.current).toLocaleString('vi-VN')} điểm`
                      )}
                    </Button>
                  </CardContent>
                </Card>
              ))}
            </div>
          </TabsContent>

          {/* History Tab */}
          <TabsContent value="history">
            <Card>
              <CardHeader>
                <CardTitle>Lịch sử giao dịch điểm</CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-4">
                  {pointHistory.map((item) => (
                    <div
                      key={item.id}
                      className="flex items-center justify-between p-4 border rounded-lg hover:bg-gray-50 transition-colors"
                    >
                      <div className="flex items-center gap-4">
                        <div
                          className={`w-10 h-10 rounded-full flex items-center justify-center ${
                            item.type === 'earn'
                              ? 'bg-green-100'
                              : 'bg-orange-100'
                          }`}
                        >
                          {item.type === 'earn' ? (
                            <TrendingUp className="w-5 h-5 text-green-600" />
                          ) : (
                            <Gift className="w-5 h-5 text-orange-600" />
                          )}
                        </div>
                        <div>
                          <p className="font-medium">{item.description}</p>
                          <div className="flex items-center gap-2 text-sm text-gray-500">
                            <Calendar className="w-4 h-4" />
                            {new Date(item.date).toLocaleDateString('vi-VN')}
                          </div>
                        </div>
                      </div>
                      <div
                        className={`text-lg font-bold ${
                          item.type === 'earn' ? 'text-green-600' : 'text-orange-600'
                        }`}
                      >
                        {item.type === 'earn' ? '+' : ''}
                        {item.points.toLocaleString('vi-VN')}
                      </div>
                    </div>
                  ))}
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </div>
  );
}