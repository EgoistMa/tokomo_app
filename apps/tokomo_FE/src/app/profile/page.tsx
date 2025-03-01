"use client";
import { useEffect, useState, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/auth';
import Link from 'next/link';
import { GameDetailCard } from '@/components/GameDetailCard';
import Image from 'next/image';
import { showToast } from "@/components/Toast";
import type { SiteConfig } from "@/services/config";
import { getSiteConfig } from "@/services/config";


interface Game {
  id: string;
  gameName: string;
  gameType: string;
  downloadUrl: string;
  createdAt: string;
  password: string | null;
  extractPassword: string | null;
}

interface UserProfileData {
  username: string;
  createdAt: string;
  vipExpireDate: string | null;
  lastLoginAt: string;
  inviteCode: string;
  points: number;
  isAdmin: boolean;
  isActive: boolean;
  purchasedGames: Game[];
}

// 添加充值历史接口
interface PaymentHistory {
  id: string;
  code: string;
  points: number;
  usedAt: string;
}

// 添加 Purchase 接口定义
interface Purchase {
  id: number;
  userId: number;
  game: Game;
  purchaseDate: string;
}

// 定义平台类型
type Platform = 'xhs' | 'tb' | 'xy';

export default function ProfilePage() {
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [vipCode, setVipCode] = useState('');
  const [redeemStatus, setRedeemStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [redeemMessage, setRedeemMessage] = useState('');
  const [paymentCode, setPaymentCode] = useState('');
  const [paymentStatus, setPaymentStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [paymentMessage, setPaymentMessage] = useState('');
  const [selectedGame, setSelectedGame] = useState<Game | null>(null);
  const [showGameDetail, setShowGameDetail] = useState(false);
  const [paymentHistory, setPaymentHistory] = useState<PaymentHistory[]>([]);
  const [showPurchaseGuide, setShowPurchaseGuide] = useState(false);
  const [selectedPlatform, setSelectedPlatform] = useState<Platform | null>(null);
  const [siteConfig, setSiteConfig] = useState<SiteConfig | null>(null);

  // 获取站点配置
  useEffect(() => {
    const fetchConfig = async () => {
      try {
        const config = await getSiteConfig();
        setSiteConfig(config);
      } catch (error) {
        console.error('获取站点配置失败:', error);
        showToast('获取站点配置失败', 'error');
      }
    };

    fetchConfig();
  }, []);

  // 使用配置中的平台和步骤
  const platforms = siteConfig?.purchaseGuide.platforms.map(platform => ({
    ...platform,
  })) || [];
  
  const platformSteps = siteConfig?.purchaseGuide.steps || {};

  const loadProfile = useCallback(async () => {
    try {
      const response = await authService.getProfile();
      setProfile(response.data);
      loadPurchaseHistory();
    } catch (error) {
      console.error('获取用户资料失败:', error);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/login');
      return;
    }

    loadProfile();
    loadPurchaseHistory();
    loadPaymentHistory();
  }, [router, loadProfile]);

  const loadPurchaseHistory = async () => {
    try {
      const token = localStorage.getItem('token');
      if (!token) return;

      const response = await fetch(`//api.tokomoapp.org/api/user/purchase-history`, {
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json'
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        const games = data.data.map((purchase: Purchase) => ({
          ...purchase.game,
          createdAt: purchase.purchaseDate
        }));
        setProfile(prev => prev ? { ...prev, purchasedGames: games } : null);
      }
    } catch (error) {
      console.error('获取购买历史失败:', error);
    }
  };

  const loadPaymentHistory = async () => {
    try {
      const response = await fetch('//api.tokomoapp.org/api/user/payment-history', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setPaymentHistory(data.data);
      }
    } catch (error) {
      console.error('获取充值历史失败:', error);
    }
  };

  const handleRedeemVIP = async () => {
    if (!vipCode.trim()) {
      setRedeemStatus('error');
      setRedeemMessage('请输入兑换码');
      return;
    }

    try {
      setRedeemStatus('loading');
      const response = await authService.redeemVIP(vipCode.trim());
      console.log('VIP兑换响应:', response);
      setRedeemStatus('success');
      setRedeemMessage('VIP兑换成功！');
      setVipCode('');
      
      // 1秒后刷新用户资料
      setTimeout(() => {
        loadProfile();
        setRedeemStatus('idle');
        setRedeemMessage('');
      }, 1000);
    } catch (error : unknown) {
        console.error(error);
        setRedeemMessage('VIP兑换失败，请检查兑换码是否正确');
        setRedeemStatus('error');
    };
  };

  const handleRedeemPayment = async () => {
    if (!paymentCode.trim()) {
      setPaymentStatus('error');
      setPaymentMessage('请输入充值码');
      return;
    }

    try {
      setPaymentStatus('loading');
      const response = await authService.redeemPayment(paymentCode.trim());
      
      if (response.status === 'ok') {
        setPaymentStatus('success');
        setPaymentMessage(`充值成功!`);
        setPaymentCode('');
        
        // 1秒后刷新用户资料和充值历史
        setTimeout(() => {
          loadProfile();
          loadPaymentHistory();
          setPaymentStatus('idle');
          setPaymentMessage('');
        }, 1000);
      } else {
        throw new Error(response.message || 'VIP兑换失败，请检查兑换码是否正确');
      }
    } catch (error : unknown) {
      console.error(error);
      setPaymentMessage('充值失败，请检查充值码是否正确');
      setPaymentStatus('error');
    }
  };

  const handleViewGame = async (gameId: string) => {
    try {
      const response = await fetch(`//api.tokomoapp.org/api/games/${gameId}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      const data = await response.json();
      if (response.status === 200 && data.status === 'ok') {
        setSelectedGame(data.data.game);
        setShowGameDetail(true);
      } else {
        console.error('获取游戏详情失败:', data.message);
      }
    } catch (error) {
      console.error('获取游戏详情错误:', error);
    }
  };

  if (loading || !siteConfig) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-600">加载中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 顶部导航 */}
      <nav className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link href="/" className="text-gray-600 hover:text-gray-900">
                返回首页
              </Link>
            </div>
          </div>
        </div>
      </nav>

      {/* 个人资料内容 */}
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8 space-y-6">
        <div className="bg-white shadow rounded-lg">
          {/* 头部信息 */}
          <div className="px-4 py-5 sm:px-6 border-b">
            <h3 className="text-lg font-medium leading-6 text-gray-900">
              个人资料
            </h3>
          </div>

          {/* 资料详情 */}
          <div className="px-4 py-5 sm:p-6">
            <div className="grid grid-cols-1 gap-6 sm:grid-cols-2">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  用户名
                </label>
                <div className="mt-1 text-gray-900">
                  {profile?.username}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  许愿币
                </label>
                <div className="mt-1 text-gray-900">
                  {profile?.points || 0}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  VIP状态
                </label>
                <div className="mt-1 text-gray-900">
                  {profile?.vipExpireDate 
                    ? `有效期至：${new Date(profile.vipExpireDate).toLocaleDateString()}`
                    : '未开通'}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  注册时间
                </label>
                <div className="mt-1 text-gray-900">
                  {profile?.createdAt && new Date(profile.createdAt).toLocaleString()}
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700">
                  最后登录
                </label>
                <div className="mt-1 text-gray-900">
                  {profile?.lastLoginAt && new Date(profile.lastLoginAt).toLocaleString()}
                </div>
              </div>
            </div>
          </div>

          {/* VIP兑换和充值区域 */}
          <div className="px-4 py-5 sm:p-6 border-t">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* VIP兑换 */}
              <div className="flex flex-col space-y-4">
                <h4 className="text-lg font-medium text-gray-900">VIP兑换</h4>
                <div className="flex flex-col space-y-2">
                  <div className="flex space-x-2">
                    <input
                      type="text"
                      value={vipCode}
                      onChange={(e) => setVipCode(e.target.value)}
                      className="flex-1 px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                      placeholder="请输入VIP兑换码"
                    />
                    <button
                      onClick={handleRedeemVIP}
                      disabled={redeemStatus === 'loading'}
                      className={`px-4 py-2 text-white rounded-md transition-colors ${
                        redeemStatus === 'loading'
                          ? 'bg-gray-400 cursor-not-allowed'
                          : 'bg-blue-500 hover:bg-blue-600'
                      }`}
                    >
                      {redeemStatus === 'loading' ? '处理中...' : '兑换'}
                    </button>
                  </div>
                  {redeemStatus === 'success' && (
                    <p className="text-sm text-green-500">{redeemMessage}</p>
                  )}
                  {redeemStatus === 'error' && (
                    <p className="text-sm text-red-500">{redeemMessage}</p>
                  )}
                </div>
              </div>

              {/* 许愿币充值 */}
              <div className="flex flex-col space-y-4">
                <h4 className="text-lg font-medium text-gray-900">充值许愿币</h4>
                <div className="flex space-x-2">
                  <input
                    type="text"
                    value={paymentCode}
                    onChange={(e) => setPaymentCode(e.target.value)}
                    className="flex-1 px-3 py-2 border rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    placeholder="请输入充值码"
                  />
                  <button
                    onClick={handleRedeemPayment}
                    disabled={paymentStatus === 'loading'}
                    className={`px-4 py-2 bg-blue-500 text-white rounded-md transition-colors ${
                      paymentStatus === 'loading' 
                        ? 'opacity-50 cursor-not-allowed' 
                        : 'hover:bg-blue-600'
                    }`}
                  >
                    {paymentStatus === 'loading' ? '处理中...' : '兑换'}
                  </button>
                </div>
                
                {paymentStatus === 'success' && (
                  <div className="text-green-500 text-sm">
                    {paymentMessage}
                  </div>
                )}
                
                {paymentStatus === 'error' && (
                  <div className="text-red-500 text-sm">
                    {paymentMessage}
                  </div>
                )}
              </div>
            </div>
            {/* 在充值区域下方添加购买按钮 */}
            <div className="mt-4">
              <button
                onClick={() => setShowPurchaseGuide(true)}
                className="text-blue-500 hover:text-blue-600 underline"
              >
                如何购买兑换码？
              </button>
            </div>
          </div>
        </div>

        {/* 购买历史卡片 */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:px-6 border-b">
            <h3 className="text-lg font-medium leading-6 text-gray-900">
              购买历史
            </h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    游戏名称
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    类型
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    操作
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {profile?.purchasedGames?.map((game) => (
                  <tr key={game.id}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {game.gameName}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {game.gameType}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm">
                      <button
                        onClick={() => handleViewGame(game.id)}
                        className="text-blue-500 hover:text-blue-700"
                      >
                        查看详情
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        {/* 在购买历史后添加充值历史卡片 */}
        <div className="bg-white shadow rounded-lg">
          <div className="px-4 py-5 sm:px-6 border-b">
            <h3 className="text-lg font-medium leading-6 text-gray-900">
              充值历史
            </h3>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    充值码
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    许愿币
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    充值时间
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {paymentHistory.map((payment, index) => (
                  <tr key={payment.id || `payment-${index}`}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {payment.code}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {payment.points}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {new Date(payment.usedAt).toLocaleString()}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      </div>

      {/* 游戏详情弹窗 */}
      <GameDetailCard
        game={selectedGame}
        visible={showGameDetail}
        onClose={() => {
          setShowGameDetail(false);
          setSelectedGame(null);
        }}
      />

      {/* 购买指南弹窗 */}
      {showPurchaseGuide && (
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
          onClick={(e) => {
            // 如果点击的是最外层的遮罩层，则关闭弹窗
            if (e.target === e.currentTarget) {
              setShowPurchaseGuide(false);
              setSelectedPlatform(null);
            }
          }}
        >
          <div 
            className="bg-white rounded-lg p-6 w-full max-w-3xl max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()} // 阻止点击内容区域时触发外层关闭
          >
            <div className="flex justify-between items-center mb-6">
              <h3 className="text-xl font-semibold">购买指南</h3>
              <button
                onClick={() => {
                  setShowPurchaseGuide(false);
                  setSelectedPlatform(null);
                }}
                className="text-gray-500 hover:text-gray-700"
              >
                <span className="text-2xl">×</span>
              </button>
            </div>

            {!selectedPlatform ? (
              // 平台选择界面
              <div className="space-y-6">
                <h4 className="text-lg font-medium text-gray-900">请选择购买平台</h4>
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                  {platforms.map((platform: SiteConfig['purchaseGuide']['platforms'][0]) => (
                    <button
                      key={platform.id}
                      onClick={() => setSelectedPlatform(platform.id as Platform)}
                      className={`p-4 rounded-lg ${platform.color} text-white flex flex-col items-center justify-center space-y-2 transition-transform hover:scale-105`}
                    >
                      <span className="text-2xl">{platform.icon}</span>
                      <span>{platform.name}</span>
                    </button>
                  ))}
                </div>
              </div>
            ) : (
              // 平台特定的购买步骤
              <div className="space-y-8">
                <div className="flex items-center space-x-4">
                  <button
                    onClick={() => setSelectedPlatform(null)}
                    className="text-gray-600 hover:text-gray-900"
                  >
                    ← 返回选择平台
                  </button>
                  <h4 className="text-lg font-medium text-gray-900">
                    {platforms.find(p => p.id === selectedPlatform)?.name}购买流程
                  </h4>
                </div>
                
                {platformSteps[selectedPlatform].map((step, index) => (
                  <div key={index} className="border-b pb-6 last:border-b-0">
                    <h4 className="text-lg font-medium mb-4 text-blue-600">
                      {step.title}
                    </h4>
                    {index === 0 && (
                      <a
                        href={platforms.find(p => p.id === selectedPlatform)?.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        className={`inline-flex items-center px-4 py-2 ${
                          platforms.find(p => p.id === selectedPlatform)?.color
                        } text-white rounded transition-colors mb-4`}
                      >
                        <span className="mr-2">
                          {platforms.find(p => p.id === selectedPlatform)?.icon}
                        </span>
                        前往购买
                      </a>
                    )}
                    <p className="text-gray-600 mb-4">{step.description}</p>
                    <div className="relative h-48 w-full">
                      {step.image && (
                        <Image
                          src={step.image}
                          alt={step.title}
                          fill
                          className="object-contain"
                        />
                      )}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
} 