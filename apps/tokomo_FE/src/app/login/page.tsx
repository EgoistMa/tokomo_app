"use client";
import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { authService } from '@/services/auth';

interface RegisterFormData {
  username: string;
  password: string;
  securityQuestion: string;
  securityAnswer: string;
  vipCode?: string;  // 改为可选
}

export default function LoginPage() {
  const router = useRouter();
  const [isLogin, setIsLogin] = useState(true);
  const [formData, setFormData] = useState<RegisterFormData>({
    username: '',
    password: '',
    securityQuestion: '',
    securityAnswer: '',
    vipCode: '',  // 改名为 vipCode
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const { name, value, type } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? (e.target as HTMLInputElement).checked : value
    }));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      if (isLogin) {
        // 登录
        const response = await authService.login({
          username: formData.username,
          password: formData.password,
        });
        
        console.log('登录响应:', response); // 添加调试日志

        if (response.data && response.data.token) {
          // 只存储token
          localStorage.setItem('token', response.data.token);
          
          // 验证是否成功存储
          const storedToken = localStorage.getItem('token');
          console.log('存储的token:', storedToken); // 添加调试日志
          
          if (storedToken) {
            console.log('登录成功，即将跳转'); // 添加调试日志
            router.push('/');
          } else {
            throw new Error('Token存储失败');
          }
        } else {
          throw new Error('未收到有效的token');
        }
      } else {
        // 注册
        const registerData = {
          username: formData.username,
          password: formData.password,
          question: formData.securityQuestion,
          answer: formData.securityAnswer,
          vipCode: formData.vipCode || undefined  // 如果为空则不包含此字段
        };

        const response = await authService.register(registerData);
        
        console.log('注册响应:', response); // 添加调试日志

        if (response.data && response.data.token) {
          // 存储token
          localStorage.setItem('token', response.data.token);
          
          // 验证是否成功存储
          const storedToken = localStorage.getItem('token');
          console.log('存储的token:', storedToken); // 添加调试日志
          
          if (storedToken) {
            console.log('注册成功，即将跳转'); // 添加调试日志
            router.push('/');
          } else {
            throw new Error('Token存储失败');
          }
        } else {
          throw new Error('注册失败，请稍后重试');
        }
      }
    } catch (err: unknown) {
      console.error('错误详情:', err);
      setError(err instanceof Error ? err.message : '操作失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow-lg">
        {/* 标题部分 */}
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900">
            {isLogin ? "登录账号" : "注册账号"}
          </h2>
          <p className="mt-2 text-sm text-gray-600">
            {isLogin ? "还没有账号？" : "已有账号？"}
            <button 
              onClick={() => {
                setIsLogin(!isLogin);
                setError('');
              }} 
              className="text-blue-500 hover:text-blue-600 font-medium ml-1"
            >
              {isLogin ? "立即注册" : "去登录"}
            </button>
          </p>
        </div>

        {/* 错误提示 */}
        {error && (
          <div className="bg-red-50 text-red-500 p-3 rounded-md text-sm">
            {error}
          </div>
        )}

        {/* 表单部分 */}
        <form onSubmit={handleSubmit} className="mt-8 space-y-6">
          <div className="space-y-4">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                用户名
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                value={formData.username}
                onChange={handleInputChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="请输入用户名"
              />
            </div>
            <div>
              <label htmlFor="password" className="block text-sm font-medium text-gray-700">
                密码
              </label>
              <input
                id="password"
                name="password"
                type="password"
                required
                value={formData.password}
                onChange={handleInputChange}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="请输入密码"
              />
            </div>

            {/* 注册时显示的额外字段 */}
            {!isLogin && (
              <>
                <div>
                  <label htmlFor="question" className="block text-sm font-medium text-gray-700">
                    安全问题
                  </label>
                  <input
                    id="question"
                    name="question"
                    type="text"
                    required
                    value={formData.securityQuestion}
                    onChange={handleInputChange}
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="请输入安全问题"
                  />
                </div>
                <div>
                  <label htmlFor="answer" className="block text-sm font-medium text-gray-700">
                    安全问题答案
                  </label>
                  <input
                    id="answer"
                    name="answer"
                    type="text"
                    required
                    value={formData.securityAnswer}
                    onChange={handleInputChange}
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="请输入答案"
                  />
                </div>
                <div>
                  <label htmlFor="vipCode" className="block text-sm font-medium text-gray-700">
                    邀请码 (选填)
                  </label>
                  <input
                    id="vipCode"
                    name="vipCode"
                    type="text"
                    value={formData.vipCode}
                    onChange={handleInputChange}
                    className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                    placeholder="请输入邀请码（选填）"
                  />
                </div>
              </>
            )}
          </div>

          {isLogin && (
            <div className="flex items-center justify-between">
              <div className="text-sm">
                <Link href="/forgot-password" className="font-medium text-blue-600 hover:text-blue-500">
                  忘记密码？
                </Link>
              </div>
            </div>
          )}

          <div>
            <button
              type="submit"
              disabled={loading}
              className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 ${
                loading ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading ? '处理中...' : (isLogin ? "登录" : "注册")}
            </button>
          </div>
        </form>

        {/* 返回首页链接 */}
        <div className="text-center mt-4">
          <Link href="/" className="text-sm text-gray-600 hover:text-gray-900">
            返回首页
          </Link>
        </div>
      </div>
    </div>
  );
} 