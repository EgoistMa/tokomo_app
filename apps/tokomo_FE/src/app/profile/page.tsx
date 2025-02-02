"use client";
import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/auth';
import Link from 'next/link';
import { GameDetailCard } from '@/components/GameDetailCard';

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

interface PaymentResponse {
  status: string;
  message: string;
  data: {
    transaction: {
      transactionId: number;
      type: string;
      amount: number;
      fromUser: number;
      externalTransactionKey: string;
      createdAt: string;
      status: string;
    }
  }
}

interface ResolveResponse {
  status: string;
  message: string;
  data: {
    transactionId: number;
    type: string;
    amount: number;
    fromUser: number;
    externalTransactionKey: string;
    createdAt: string;
    status: string;
  }
}

// 添加充值历史接口
interface PaymentHistory {
  id: string;
  code: string;
  points: number;
  usedAt: string;
}

export default function ProfilePage() {
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfileData | null>(null);
  const [loading, setLoading] = useState(true);
  const [showQRCode, setShowQRCode] = useState(false);
  const [depositStatus, setDepositStatus] = useState<'idle' | 'loading' | 'pending' | 'success' | 'error'>('idle');
  const [depositMessage, setDepositMessage] = useState('');
  const [amount, setAmount] = useState<number>(100);
  const [currentTransaction, setCurrentTransaction] = useState<string>('');
  const [vipCode, setVipCode] = useState('');
  const [redeemStatus, setRedeemStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [redeemMessage, setRedeemMessage] = useState('');
  const [paymentCode, setPaymentCode] = useState('');
  const [paymentStatus, setPaymentStatus] = useState<'idle' | 'loading' | 'success' | 'error'>('idle');
  const [paymentMessage, setPaymentMessage] = useState('');
  const [selectedGame, setSelectedGame] = useState<Game | null>(null);
  const [showGameDetail, setShowGameDetail] = useState(false);
  const [paymentHistory, setPaymentHistory] = useState<PaymentHistory[]>([]);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      router.push('/login');
      return;
    }

    loadProfile();
    loadPaymentHistory();
  }, [router]);

  const loadProfile = async () => {
    try {
      const response = await authService.getProfile();
      setProfile(response.data);
      loadPurchaseHistory();
    } catch (error) {
      console.error('获取用户资料失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadPurchaseHistory = async () => {
    try {
      const response = await fetch('/api/games/purchased', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setProfile(prev => prev ? { ...prev, purchasedGames: data.data } : null);
      }
    } catch (error) {
      console.error('获取购买历史失败:', error);
    }
  };

  const loadPaymentHistory = async () => {
    try {
      const response = await fetch('/api/user/payment-history', {
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

  const handleDeposit = async () => {
    try {
      setDepositStatus('loading');
      const response = await fetch('/api/payment/create', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ amount })
      });

      const data: PaymentResponse = await response.json();
      if (response.status === 200 && data.status === 'ok') {
        setCurrentTransaction(data.data.transaction.externalTransactionKey);
        setDepositStatus('pending');
        setDepositMessage('请扫描二维码完成支付');
        setShowQRCode(true);
      } else {
        throw new Error(data.message || '创建支付订单失败');
      }
    } catch (err) {
      console.error('创建充值订单失败:', err);
      setDepositStatus('error');
      setDepositMessage('创建充值订单失败，请稍后重试');
    }
  };

  const handlePaymentComplete = async () => {
    if (!currentTransaction) return;
    
    try {
      setDepositStatus('loading');
      const response = await fetch('/api/payment/resolve', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          externalTransactionKey: currentTransaction,
          resolveType: 'success'
        })
      });

      const data: ResolveResponse = await response.json();
      if (response.status === 200 && data.status === 'ok' && data.data.status === 'COMPLETED') {
        setDepositStatus('success');
        setDepositMessage('充值成功！');
        
        // 1秒后刷新用户资料并关闭弹窗
        setTimeout(() => {
          loadProfile();
          setShowQRCode(false);
          setDepositStatus('idle');
          setDepositMessage('');
          setCurrentTransaction('');
        }, 1000);
      } else {
        throw new Error(data.message || '支付确认失败');
      }
    } catch (err) {
      console.error('支付确认失败:', err);
      setDepositStatus('error');
      setDepositMessage('支付确认失败，请联系客服');
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
    } catch (error: unknown) {
      if (error instanceof Error) {
        setRedeemMessage(error.message);
      } else {
        setRedeemMessage('VIP兑换失败，请检查兑换码是否正确');
      }
      setRedeemStatus('error');
    }
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
        setPaymentMessage(`充值成功! 获得 ${response.data.points} 许愿币`);
        setPaymentCode('');
        
        // 1秒后刷新用户资料和充值历史
        setTimeout(() => {
          loadProfile();
          loadPaymentHistory();
          setPaymentStatus('idle');
          setPaymentMessage('');
        }, 1000);
      } else {
        throw new Error(response.message || '充值失败');
      }
    } catch (error: unknown) {
      if (error instanceof Error) {
        setPaymentMessage(error.message);
      } else {
        setPaymentMessage('充值失败，请检查充值码是否正确');
      }
      setPaymentStatus('error');
    }
  };

  const handleViewGame = async (gameId: string) => {
    try {
      const response = await fetch(`/api/games/${gameId}`, {
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

  if (loading) {
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
                {paymentHistory.map((payment) => (
                  <tr key={payment.id}>
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

      {/* 支付二维码弹窗 */}
      {showQRCode && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white p-6 rounded-lg shadow-xl max-w-sm w-full mx-4">
            <div className="flex flex-col items-center space-y-4">
              <h3 className="text-lg font-medium text-gray-900">
                微信支付
              </h3>
              
              {depositStatus === 'idle' && (
                <div className="flex flex-col items-center space-y-4">
                  <p className="text-lg font-medium">
                    充值金额：{amount} 元
                  </p>
                  <button
                    onClick={handleDeposit}
                    className="px-4 py-2 bg-blue-500 text-white rounded-md hover:bg-blue-600 transition-colors"
                  >
                    确认充值
                  </button>
                </div>
              )}

              {depositStatus === 'pending' && currentTransaction && (
                <>
                  {/* 使用订单号生成二维码 */}
                  <div 
                    className="w-48 h-48 bg-gray-100 flex items-center justify-center border cursor-pointer"
                    onClick={handlePaymentComplete}
                    title="点击模拟支付完成"
                  >
                    <div className="text-center">
                      <div className="grid grid-cols-8 grid-rows-8 gap-1 p-4">
                        {Array.from({ length: 64 }).map((_, i) => (
                          <div
                            key={i}
                            className={`w-4 h-4 ${
                              Math.random() > 0.5 ? 'bg-black' : 'bg-white'
                            }`}
                          />
                        ))}
                      </div>
                      <p className="text-xs text-gray-500 mt-2">
                        订单号：{currentTransaction}
                      </p>
                    </div>
                  </div>
                  <p className="text-sm text-gray-500">
                    请使用微信扫描二维码支付
                  </p>
                  <p className="text-xs text-gray-400">
                    (点击二维码模拟支付完成)
                  </p>
                </>
              )}

              {depositStatus === 'loading' && (
                <div className="text-gray-600">
                  处理中...
                </div>
              )}

              {depositStatus === 'success' && (
                <div className="text-green-500">
                  {depositMessage}
                </div>
              )}

              {depositStatus === 'error' && (
                <div className="text-red-500">
                  {depositMessage}
                </div>
              )}

              <button
                onClick={() => {
                  setShowQRCode(false);
                  setDepositStatus('idle');
                  setDepositMessage('');
                  setCurrentTransaction('');
                }}
                className="mt-4 px-4 py-2 text-gray-600 hover:text-gray-900"
              >
                关闭
              </button>
            </div>
          </div>
        </div>
      )}

      {/* 游戏详情弹窗 */}
      <GameDetailCard
        game={selectedGame}
        visible={showGameDetail}
        onClose={() => {
          setShowGameDetail(false);
          setSelectedGame(null);
        }}
      />
    </div>
  );
} 