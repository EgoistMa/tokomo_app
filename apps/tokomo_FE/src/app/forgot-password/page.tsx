"use client";
import { useState } from 'react';
import Link from 'next/link';
import { useRouter } from 'next/navigation';
import { showToast } from '@/components/Toast';

export default function ForgotPasswordPage() {
  const router = useRouter();
  const [step, setStep] = useState(1); // 1: 输入用户名, 2: 回答安全问题
  const [loading, setLoading] = useState(false);
  const [username, setUsername] = useState('');
  const [securityQuestion, setSecurityQuestion] = useState('');
  const [answer, setAnswer] = useState('');
  const [newPassword, setNewPassword] = useState('');

  const handleGetQuestion = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!username.trim()) {
      showToast('请输入用户名', 'warning');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch(
        `//api.tokomoapp.org/api/user/password/security-question?username=${encodeURIComponent(username.trim())}`, 
        {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          }
        }
      );

      const data = await response.json();
      if (response.status === 200 && data.status === 'ok') {
        setSecurityQuestion(data.data);
        setStep(2);
      } else {
        showToast(data.message || '获取安全问题失败', 'error');
      }
    } catch (error: unknown) {
      console.error('错误详情:', error);
      showToast('系统错误，请稍后重试', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleResetPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!answer.trim() || !newPassword.trim()) {
      showToast('请填写所有必填项', 'warning');
      return;
    }

    setLoading(true);
    try {
      const response = await fetch('//api.tokomoapp.org/api/user/password/reset', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: username.trim(),
          answer: answer.trim(),
          newPassword: newPassword.trim()
        })
      });

      const data = await response.json();
      if (response.status === 200 && data.status === 'ok') {
        showToast('密码重置成功', 'success');
        router.push('/login');
      } else {
        showToast(data.message || '密码重置失败', 'error');
      }
    } catch (error: unknown) {
      console.error('错误详情:', error);
      showToast('系统错误，请稍后重试', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50">
      <div className="max-w-md w-full space-y-8 p-8 bg-white rounded-lg shadow-lg">
        <div className="text-center">
          <h2 className="text-3xl font-bold text-gray-900">
            找回密码
          </h2>
        </div>

        {step === 1 ? (
          <form onSubmit={handleGetQuestion} className="mt-8 space-y-6">
            <div>
              <label htmlFor="username" className="block text-sm font-medium text-gray-700">
                用户名
              </label>
              <input
                id="username"
                name="username"
                type="text"
                required
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                placeholder="请输入用户名"
              />
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 ${
                loading ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading ? '获取中...' : '获取安全问题'}
            </button>
          </form>
        ) : (
          <form onSubmit={handleResetPassword} className="mt-8 space-y-6">
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  安全问题
                </label>
                <div className="mt-1 p-2 bg-gray-50 rounded-md">
                  {securityQuestion}
                </div>
              </div>

              <div>
                <label htmlFor="answer" className="block text-sm font-medium text-gray-700">
                  答案
                </label>
                <input
                  id="answer"
                  type="text"
                  required
                  value={answer}
                  onChange={(e) => setAnswer(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="请输入安全问题答案"
                />
              </div>

              <div>
                <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700">
                  新密码
                </label>
                <input
                  id="newPassword"
                  type="password"
                  required
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="mt-1 block w-full px-3 py-2 border border-gray-300 rounded-md shadow-sm focus:outline-none focus:ring-blue-500 focus:border-blue-500"
                  placeholder="请输入新密码"
                />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading}
              className={`w-full flex justify-center py-2 px-4 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 ${
                loading ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {loading ? '重置中...' : '重置密码'}
            </button>
          </form>
        )}

        <div className="text-center mt-4">
          <Link href="/login" className="text-sm text-gray-600 hover:text-gray-900">
            返回登录
          </Link>
        </div>
      </div>
    </div>
  );
} 