"use client";
import Image from "next/image";
import { siteConfig } from "@/config/site";
import { useState, useEffect } from "react";
import Link from 'next/link';
import { authService } from "@/services/auth";
import { useRouter } from 'next/navigation';
import { showToast } from "@/components/Toast";
import { CustomerServiceModal } from '@/components/CustomerServiceModal';

export default function Home() {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isLoggedIn, setIsLoggedIn] = useState(false);
  const [username, setUsername] = useState('');
  const [userProfile, setUserProfile] = useState<{
    vipExpireDate: string | null;
    points: number;
  } | null>(null);
  const totalSlides = siteConfig.carousel.items.length;
  const [searchKeyword, setSearchKeyword] = useState('');
  const router = useRouter();
  const [isSearching, setIsSearching] = useState(false);
  const [showCustomerService, setShowCustomerService] = useState(false);

  // 检查登录状态和获取用户资料
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (token) {
      setIsLoggedIn(true);
      
      // 获取用户资料
      const fetchProfile = async () => {
        try {
          const response = await authService.getProfile();
          if (response.status === 'ok') {
            setUsername(response.data.username);
            setUserProfile({
              vipExpireDate: response.data.vipExpireDate,
              points: response.data.points
            });
          } else {
            console.error('获取用户资料失败:', response.message);
            // 获取失败时登出用户
            localStorage.removeItem('token');
            setIsLoggedIn(false);
            setUsername('');
            setUserProfile(null);
          }
        } catch (error) {
          console.error('获取用户资料失败:', error);
          // 发生错误时也登出用户
          localStorage.removeItem('token');
          setIsLoggedIn(false);
          setUsername('');
          setUserProfile(null);
        }
      };

      fetchProfile();
    }
  }, []);

  // 自动轮播
  useEffect(() => {
    if (!siteConfig.carousel.settings.autoplay) return;

    const timer = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % totalSlides);
    }, siteConfig.carousel.settings.interval);

    return () => clearInterval(timer);
  }, [totalSlides]);

  // 切换到下一张
  const nextSlide = () => {
    setCurrentSlide((prev) => (prev + 1) % totalSlides);
  };

  // 切换到上一张
  const prevSlide = () => {
    setCurrentSlide((prev) => (prev - 1 + totalSlides) % totalSlides);
  };

  // 直接跳转到指定幻灯片
  const goToSlide = (index: number) => {
    setCurrentSlide(index);
  };

  // 添加刷新用户资料的函数
  const refreshUserProfile = async () => {
    try {
      const response = await authService.getProfile();
      setUserProfile({
        vipExpireDate: response.data.vipExpireDate,
        points: response.data.points
      });
    } catch (error) {
      console.error('刷新用户资料失败:', error);
    }
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleWish();
  };

  // 修改许愿按钮点击处理函数
  const handleWish = async () => {
    if (!isLoggedIn) {
      showToast('请先登录后再许愿', 'warning');
      return;
    }

    if (!searchKeyword.trim()) {
      showToast('请输入要许愿的内容', 'warning');
      return;
    }

    if (isSearching) return;

    setIsSearching(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`//api.tokomoapp.org/api/games/search?keyword=${encodeURIComponent(searchKeyword.trim())}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });
      
      const data = await response.json();
      if (response.status === 200 && data.status === 'ok') {
        // 存储完整的API响应
        sessionStorage.setItem('searchResult', JSON.stringify(data));
        router.push(`/result?keyword=${encodeURIComponent(searchKeyword.trim())}`);
      } else {
        showToast('搜索失败，请稍后重试', 'error');
      }
    } catch (error) {
      console.error('搜索出错:', error);
      showToast('系统错误，请稍后重试', 'error');
    } finally {
      setIsSearching(false);
    }
  };

  return (
    <div className="min-h-screen flex flex-col">
      {/* Navbar */}
      <nav className="h-16 px-6 flex items-center justify-end border-b">
        {isLoggedIn ? (
          <div className="flex items-center space-x-4">
            <div className="relative group">
              <Link href="/profile">
                <span className="text-gray-600 cursor-pointer hover:text-gray-900">
                  欢迎：{username}
                </span>
              </Link>
              {/* 悬浮提示框 */}
              <div className="absolute right-0 top-full mt-2 w-48 bg-white rounded-lg shadow-lg opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 p-4 border z-50">
                <div className="space-y-2">
                  <div className="flex items-center justify-between">
                    <p className="text-sm text-gray-600">
                      许愿币：{userProfile?.points || 0}
                    </p>
                    {/* 刷新按钮 */}
                    <button
                      onClick={refreshUserProfile}
                      className="text-blue-500 hover:text-blue-600 p-1 rounded-full hover:bg-blue-50 transition-colors"
                      title="刷新数据"
                    >
                      <svg
                        className="w-4 h-4"
                        fill="none"
                        stroke="currentColor"
                        viewBox="0 0 24 24"
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          strokeWidth={2}
                          d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"
                        />
                      </svg>
                    </button>
                  </div>
                  <p className="text-sm text-gray-600">
                    VIP到期：
                    {userProfile?.vipExpireDate 
                      ? new Date(userProfile.vipExpireDate).toLocaleDateString()
                      : '未开通'}
                  </p>
                </div>
              </div>
            </div>
            <button 
              onClick={() => {
                localStorage.removeItem('token');
                setIsLoggedIn(false);
                setUsername('');
                setUserProfile(null);
              }}
              className="text-sm text-gray-600 hover:text-gray-900"
            >
              退出登录
            </button>
          </div>
        ) : (
          <Link 
            href="/login" 
            className="px-4 py-2 text-gray-600 hover:text-gray-900"
          >
            登录/注册
          </Link>
        )}
      </nav>

      {/* 主要内容区域 */}
      <main className="flex flex-col space-y-5">
        {/* Banner轮播图 */}
        <div className="w-full h-[400px] relative overflow-hidden">
          {siteConfig.carousel.items.map((item, index) => (
            <div
              key={index}
              className={`absolute inset-0 transition-transform duration-500 ease-in-out ${
                index === currentSlide ? "translate-x-0" : "translate-x-full"
              }`}
              style={{
                transform: `translateX(${(index - currentSlide) * 100}%)`
              }}
            >
              <a 
                href={item.link}
                className="block w-full h-full relative cursor-pointer"
                target="_blank"
                rel="noopener noreferrer"
              >
                <Image
                  src={item.image}
                  alt={item.title}
                  fill
                  className="object-cover"
                  priority={index === 0}
                />
                {(item.title || item.description) && (
                  <div className="absolute inset-0 bg-black/30 flex items-center justify-center text-white">
                    <div className="text-center">
                      {item.title && <h2 className="text-3xl font-bold mb-2">{item.title}</h2>}
                      {item.description && <p className="text-lg">{item.description}</p>}
                    </div>
                  </div>
                )}
              </a>
            </div>
          ))}
          
          {/* 轮播控制按钮 */}
          {siteConfig.carousel.settings.showArrows && (
            <>
              <button 
                onClick={prevSlide}
                className="absolute left-4 top-1/2 -translate-y-1/2 bg-white/50 p-2 rounded-full hover:bg-white/70 transition-colors"
              >
                ←
              </button>
              <button 
                onClick={nextSlide}
                className="absolute right-4 top-1/2 -translate-y-1/2 bg-white/50 p-2 rounded-full hover:bg-white/70 transition-colors"
              >
                →
              </button>
            </>
          )}
          
          {/* 轮播指示点 */}
          {siteConfig.carousel.settings.showDots && (
            <div className="absolute bottom-4 left-1/2 -translate-x-1/2 flex space-x-2">
              {siteConfig.carousel.items.map((_, index) => (
                <button
                  key={index}
                  onClick={() => goToSlide(index)}
                  className={`w-2 h-2 rounded-full transition-colors ${
                    index === currentSlide 
                      ? "bg-white" 
                      : "bg-white/50 hover:bg-white/80"
                  }`}
                />
              ))}
            </div>
          )}
        </div>

        {/* 内容区域 - 设置最小高度确保有足够空间显示内容 */}
        <div className="min-h-[400px] relative flex flex-col items-center">
          {/* 左侧banner */}
          <div className="absolute left-5 inset-y-0 w-[150px] bg-gray-100 overflow-hidden">
            <a 
              href={siteConfig.banners.left.link}
              target="_blank"
              rel="noopener noreferrer"
              className="block w-full h-full relative group"
            >
              <Image
                src={siteConfig.banners.left.image}
                alt={siteConfig.banners.left.text}
                fill
                className="object-cover"
              />
              <div className="absolute inset-0 flex items-center justify-center bg-black/30 group-hover:bg-black/40 transition-colors">
                <span className="text-white tracking-wider font-medium writing-vertical-rl">
                  {siteConfig.banners.left.text}
                </span>
              </div>
            </a>
          </div>

          {/* 右侧banner */}
          <div className="absolute right-5 inset-y-0 w-[150px] bg-gray-100 overflow-hidden">
            <a 
              href={siteConfig.banners.right.link}
              target="_blank"
              rel="noopener noreferrer"
              className="block w-full h-full relative group"
            >
              <Image
                src={siteConfig.banners.right.image}
                alt={siteConfig.banners.right.text}
                fill
                className="object-cover"
              />
              <div className="absolute inset-0 flex items-center justify-center bg-black/30 group-hover:bg-black/40 transition-colors">
                <span className="text-white tracking-wider font-medium writing-vertical-rl">
                  {siteConfig.banners.right.text}
                </span>
              </div>
            </a>
          </div>

          {/* 搜索区域 - 包含搜索框和许愿按钮 */}
          <div className="w-[400px] flex flex-col items-center space-y-4 mt-[120px] search-container">
            {/* 搜索框 */}
            <div className="relative w-full search-input-container">
              <form onSubmit={handleSubmit}>
                <input 
                  type="text" 
                  value={searchKeyword}
                  onChange={(e) => setSearchKeyword(e.target.value)}
                  className="w-full px-4 py-2 border-2 border-gray-300 rounded-lg focus:outline-none focus:border-gray-500 shadow-lg"
                  placeholder="搜索..."
                  disabled={isSearching}
                />
              </form>
            </div>

            {/* 许愿按钮 */}
            <button 
              onClick={handleWish}
              disabled={isSearching}
              className={`px-8 py-2 bg-blue-500 text-white rounded-lg transition-colors shadow-md
                ${isSearching ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-600'}`}
            >
              {isSearching ? '搜索中...' : '许愿'}
            </button>
          </div>

          {/* 联系客服按钮 - 固定在屏幕底部 */}
          <div className="fixed bottom-[-5px] left-3/4 -translate-x-1/2">
            <button 
              onClick={() => setShowCustomerService(true)}
              className="px-8 py-2 bg-blue-500 text-white rounded-lg hover:bg-blue-600 transition-colors shadow-md"
            >
              联系客服
            </button>
          </div>
        </div>
      </main>

      {/* Footer */}
      <footer className="bg-gray-100 py-8 mt-5">
        <div className="container mx-auto px-6">
          <div className="grid grid-cols-4 gap-8">
            {siteConfig.footer.sections.map((section, index) => (
              <div key={index}>
                <h3 className="font-bold mb-4">{section.title}</h3>
                <ul className="space-y-2 text-gray-600">
                  {section.links.map((link, linkIndex) => (
                    <li key={linkIndex}>
                      <a href={link.href}>{link.text}</a>
                    </li>
                  ))}
                </ul>
              </div>
            ))}
          </div>
          <div className="mt-8 pt-8 border-t text-center text-gray-500">
            <p>{siteConfig.footer.copyright}</p>
          </div>
        </div>
      </footer>

      {/* 客服联系弹窗 */}
      <CustomerServiceModal
        visible={showCustomerService}
        onClose={() => setShowCustomerService(false)}
      />
    </div>
  );
}
