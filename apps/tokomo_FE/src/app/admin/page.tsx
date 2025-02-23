"use client";
import { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { GameManagement } from '@/components/admin/GameManagement';
import { UserManagement } from '@/components/admin/UserManagement';
import { CodeManagement } from '@/components/admin/CodeManagement';
import { PaymentCodeManagement } from '@/components/admin/PaymentCodeManagement';

// 导航项配置
const navItems = [
  { id: 'games', label: '游戏管理', icon: '🎮' },
  { id: 'users', label: '用户管理', icon: '👥' },
  { id: 'vipcodes', label: '兑换码管理', icon: '🎫' },
  { id: 'paymentcodes', label: '支付码管理', icon: '💳' },
];

export default function AdminPage() {
  const router = useRouter();
  const [activeNav, setActiveNav] = useState('games');
  const [isAdmin, setIsAdmin] = useState(false);
  const [collapsed, setCollapsed] = useState(false);

  useEffect(() => {
    // 检查是否是管理员
    const checkAdmin = async () => {
      try {
        const response = await fetch('//api.tokomoapp.org/api/user/profile', {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}`
          }
        });
        const data = await response.json();
        if (!data.data.isAdmin) {
          router.push('/');
        } else {
          setIsAdmin(true);
        }
      } catch (error) {
        console.error('检查管理员状态失败:', error);
        router.push('/');
      }
    };
    checkAdmin();
  }, [router]);

  if (!isAdmin) return null;

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* 左侧导航栏 */}
      <div className={`bg-white shadow-lg transition-all ${collapsed ? 'w-16' : 'w-64'}`}>
        <div className="p-4 border-b flex items-center justify-between">
          <h1 className={`font-bold text-xl ${collapsed ? 'hidden' : 'block'}`}>
            管理后台
          </h1>
          <button
            onClick={() => setCollapsed(!collapsed)}
            className="p-2 rounded hover:bg-gray-100"
          >
            {collapsed ? '→' : '←'}
          </button>
        </div>
        <nav className="p-4">
          {navItems.map((item) => (
            <button
              key={item.id}
              onClick={() => setActiveNav(item.id)}
              className={`w-full flex items-center p-3 rounded-lg mb-2 transition-colors
                ${activeNav === item.id 
                  ? 'bg-blue-50 text-blue-600' 
                  : 'hover:bg-gray-50'}`}
            >
              <span className="text-xl">{item.icon}</span>
              <span className={`ml-3 ${collapsed ? 'hidden' : 'block'}`}>
                {item.label}
              </span>
            </button>
          ))}
        </nav>
      </div>

      {/* 主内容区域 */}
      <div className="flex-1 p-8 overflow-auto">
        <div className="max-w-7xl mx-auto">
          {/* 面包屑导航 */}
          <div className="mb-6 flex items-center text-gray-600">
            <span>管理后台</span>
            <span className="mx-2">/</span>
            <span className="text-gray-900">
              {navItems.find(item => item.id === activeNav)?.label}
            </span>
          </div>

          {/* 内容区域 */}
          <div className="bg-white rounded-lg shadow">
            {activeNav === 'games' && <GameManagement />}
            {activeNav === 'users' && <UserManagement />}
            {activeNav === 'vipcodes' && <CodeManagement />}
            {activeNav === 'paymentcodes' && <PaymentCodeManagement />}
          </div>
        </div>
      </div>
    </div>
  );
} 