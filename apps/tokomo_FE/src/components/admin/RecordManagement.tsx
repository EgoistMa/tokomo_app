"use client";
import { useState, useEffect } from 'react';
import { showToast } from '@/components/Toast';

interface Record {
  id: string;
  type: 'PURCHASE' | 'PAYMENT' | 'VIP';
  userId: number;
  username: string;
  amount?: number;
  points?: number;
  days?: number;
  gameId?: string;
  gameName?: string;
  code?: string;
  status: string;
  createdAt: string;
}

export function RecordManagement() {
  const [records, setRecords] = useState<Record[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState({
    type: 'ALL',
    startDate: '',
    endDate: '',
    username: '',
    status: 'ALL'
  });

  useEffect(() => {
    loadRecords();
  }, []);

  const loadRecords = async () => {
    try {
      const queryParams = new URLSearchParams();
      if (filter.type !== 'ALL') queryParams.append('type', filter.type);
      if (filter.startDate) queryParams.append('startDate', filter.startDate);
      if (filter.endDate) queryParams.append('endDate', filter.endDate);
      if (filter.username) queryParams.append('username', filter.username);
      if (filter.status !== 'ALL') queryParams.append('status', filter.status);

      const response = await fetch(`/api/admin/records?${queryParams.toString()}`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setRecords(data.data);
      }
    } catch (error) {
      showToast('加载记录失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleExport = () => {
    const csvContent = [
      ['ID', '类型', '用户', '金额', '点数', '天数', '游戏', '兑换码', '状态', '时间'].join(','),
      ...records.map(record => [
        record.id,
        record.type,
        record.username,
        record.amount || '',
        record.points || '',
        record.days || '',
        record.gameName || '',
        record.code || '',
        record.status,
        new Date(record.createdAt).toLocaleString()
      ].join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `记录列表_${new Date().toISOString().split('T')[0]}.csv`;
    link.click();
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    loadRecords();
  };

  if (loading) {
    return <div className="text-center py-4">加载中...</div>;
  }

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex justify-between mb-6">
        <h2 className="text-xl font-semibold">记录查询</h2>
        <button
          onClick={handleExport}
          className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
        >
          导出CSV
        </button>
      </div>

      {/* 搜索表单 */}
      <form onSubmit={handleSearch} className="mb-6 grid grid-cols-1 md:grid-cols-4 gap-4">
        <div>
          <label className="block text-sm font-medium text-gray-700">类型</label>
          <select
            value={filter.type}
            onChange={(e) => setFilter(prev => ({ ...prev, type: e.target.value }))}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          >
            <option value="ALL">全部</option>
            <option value="PURCHASE">游戏购买</option>
            <option value="PAYMENT">充值</option>
            <option value="VIP">VIP兑换</option>
          </select>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">开始日期</label>
          <input
            type="date"
            value={filter.startDate}
            onChange={(e) => setFilter(prev => ({ ...prev, startDate: e.target.value }))}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">结束日期</label>
          <input
            type="date"
            value={filter.endDate}
            onChange={(e) => setFilter(prev => ({ ...prev, endDate: e.target.value }))}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
          />
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700">用户名</label>
          <input
            type="text"
            value={filter.username}
            onChange={(e) => setFilter(prev => ({ ...prev, username: e.target.value }))}
            className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
            placeholder="输入用户名搜索"
          />
        </div>
        <div className="md:col-span-4 flex justify-end">
          <button
            type="submit"
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            搜索
          </button>
        </div>
      </form>

      {/* 记录列表 */}
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                类型
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                用户
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                详情
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                状态
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                时间
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {records.map((record) => (
              <tr key={record.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                  {record.type === 'PURCHASE' && '游戏购买'}
                  {record.type === 'PAYMENT' && '充值'}
                  {record.type === 'VIP' && 'VIP兑换'}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {record.username}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {record.type === 'PURCHASE' && record.gameName && (
                    <span>购买游戏: {record.gameName}</span>
                  )}
                  {record.type === 'PAYMENT' && record.points && (
                    <span>充值 {record.points} 许愿币</span>
                  )}
                  {record.type === 'VIP' && record.days && (
                    <span>兑换 {record.days} 天VIP</span>
                  )}
                  {record.code && <span className="ml-2 text-gray-400">({record.code})</span>}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm">
                  <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full 
                    ${record.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                    {record.status === 'COMPLETED' ? '完成' : '处理中'}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {new Date(record.createdAt).toLocaleString()}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {records.length === 0 && (
        <div className="text-center py-4 text-gray-500">
          没有找到相关记录
        </div>
      )}
    </div>
  );
} 