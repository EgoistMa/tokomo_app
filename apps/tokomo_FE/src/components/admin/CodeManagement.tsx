"use client";
import { useState, useEffect } from 'react';
import { showToast } from '@/components/Toast';

interface Code {
  id: string;
  code: string;
  type: 'VIP' | 'PAYMENT';
  points?: number;
  days?: number;
  used: boolean;
  usedBy?: string;
  usedAt?: string;
  createdAt: string;
}

export function CodeManagement() {
  const [codes, setCodes] = useState<Code[]>([]);
  const [loading, setLoading] = useState(true);
  const [showGenerateModal, setShowGenerateModal] = useState(false);
  const [codeType, setCodeType] = useState<'VIP' | 'PAYMENT'>('VIP');
  const [formData, setFormData] = useState({
    count: 1,
    points: 100,
    days: 30
  });

  useEffect(() => {
    loadCodes();
  }, []);

  const loadCodes = async () => {
    try {
      const response = await fetch('/api/admin/codes', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setCodes(data.data);
      }
    } catch (error) {
      showToast('加载兑换码列表失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleGenerateCodes = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch('/api/admin/codes/generate', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          type: codeType,
          count: formData.count,
          points: codeType === 'PAYMENT' ? formData.points : undefined,
          days: codeType === 'VIP' ? formData.days : undefined
        })
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('生成成功', 'success');
        setShowGenerateModal(false);
        loadCodes();
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '生成失败', 'error');
    }
  };

  const handleExport = () => {
    const csvContent = [
      ['兑换码', '类型', '点数/天数', '状态', '使用者', '使用时间', '创建时间'].join(','),
      ...codes.map(code => [
        code.code,
        code.type,
        code.type === 'PAYMENT' ? code.points : code.days,
        code.used ? '已使用' : '未使用',
        code.usedBy || '',
        code.usedAt || '',
        code.createdAt
      ].join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `兑换码列表_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  if (loading) {
    return <div className="text-center py-4">加载中...</div>;
  }

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex justify-between mb-6">
        <h2 className="text-xl font-semibold">兑换码管理</h2>
        <div className="space-x-4">
          <button
            onClick={() => setShowGenerateModal(true)}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            生成兑换码
          </button>
          <button
            onClick={handleExport}
            className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          >
            导出CSV
          </button>
        </div>
      </div>
      
      <table className="min-w-full divide-y divide-gray-200">
        <thead>
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              兑换码
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              类型
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              点数/天数
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              状态
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              使用者
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              使用时间
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              创建时间
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {codes.map((code) => (
            <tr key={code.id}>
              <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                {code.code}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {code.type === 'VIP' ? 'VIP兑换码' : '充值码'}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {code.type === 'PAYMENT' ? `${code.points}点` : `${code.days}天`}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm">
                <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                  ${code.used ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}`}>
                  {code.used ? '已使用' : '未使用'}
                </span>
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {code.usedBy || '-'}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {code.usedAt ? new Date(code.usedAt).toLocaleString() : '-'}
              </td>
              <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                {new Date(code.createdAt).toLocaleString()}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* 生成兑换码弹窗 */}
      {showGenerateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-medium mb-4">
              生成兑换码
            </h3>
            <form onSubmit={handleGenerateCodes} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  兑换码类型
                </label>
                <select
                  value={codeType}
                  onChange={(e) => setCodeType(e.target.value as 'VIP' | 'PAYMENT')}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                >
                  <option value="VIP">VIP兑换码</option>
                  <option value="PAYMENT">充值码</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  生成数量
                </label>
                <input
                  type="number"
                  min="1"
                  max="100"
                  value={formData.count}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    count: parseInt(e.target.value)
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  required
                />
              </div>
              {codeType === 'PAYMENT' ? (
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    许愿币数量
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={formData.points}
                    onChange={(e) => setFormData(prev => ({
                      ...prev,
                      points: parseInt(e.target.value)
                    }))}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    required
                  />
                </div>
              ) : (
                <div>
                  <label className="block text-sm font-medium text-gray-700">
                    VIP天数
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={formData.days}
                    onChange={(e) => setFormData(prev => ({
                      ...prev,
                      days: parseInt(e.target.value)
                    }))}
                    className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                    required
                  />
                </div>
              )}
              <div className="flex justify-end space-x-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowGenerateModal(false)}
                  className="px-4 py-2 text-gray-700 hover:text-gray-900"
                >
                  取消
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                  生成
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
} 