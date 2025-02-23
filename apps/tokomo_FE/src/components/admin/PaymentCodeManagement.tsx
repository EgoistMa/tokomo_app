"use client";
import { useState, useEffect } from 'react';
import { showToast } from '@/components/Toast';

interface PaymentCode {
  id: string;
  code: string;
  points: number;
  used: boolean;
  usedBy?: string | null;
  usedAt?: string | null;
}

export function PaymentCodeManagement() {
  const [codes, setCodes] = useState<PaymentCode[]>([]);
  const [loading, setLoading] = useState(true);
  const [editingCode, setEditingCode] = useState<PaymentCode | null>(null);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showImportModal, setShowImportModal] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [showGenerateModal, setShowGenerateModal] = useState(false);
  const [showGeneratedCodesModal, setShowGeneratedCodesModal] = useState(false);
  const [generatedCodes, setGeneratedCodes] = useState<PaymentCode[]>([]);
  const [formData, setFormData] = useState({ amount: 5, points: 100 });

  useEffect(() => {
    loadCodes();
  }, []);

  const loadCodes = async () => {
    try {
      const response = await fetch(`api.tokomoapp.org/api/admin/payment/codes`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setCodes(data.data);
      }
    } catch (error) {
      console.error('加载支付码列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleEditCode = (code: PaymentCode) => {
    setEditingCode(code);
    setShowEditModal(true);
  };

  const handleDeleteCode = async (id: number) => {
    if (!confirm('确定要删除这个支付码吗？')) return;

    try {
      const response = await fetch(`api.tokomoapp.org/api/admin/payment/codes/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('删除成功', 'success');
        loadCodes();
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '删除失败', 'error');
    }
  };

  const handleUpdateCode = async (e: React.FormEvent, id: number) => {
    e.preventDefault();
    if (!editingCode) return;

    try {
      const response = await fetch(`api.tokomoapp.org/api/admin/payment/codes/${id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(editingCode)
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('更新成功', 'success');
        setShowEditModal(false);
        loadCodes();
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '更新失败', 'error');
    }
  };

  const handleExportAllCSV = () => {
    const csvContent = "data:text/csv;charset=utf-8," 
      + ["支付码,金额,状态,使用者ID,使用时间"]
      .concat(codes.map(code => 
        `${code.code},${code.points},${code.used ? '已使用' : '未使用'},${code.usedBy || ''},${code.usedAt || ''}`
      ))
      .join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "all_payment_codes.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  const handleGenerateCodes = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const response = await fetch(`api.tokomoapp.org/api/admin/payment/genPay`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          amount: formData.amount,
          points: formData.points
        })
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('生成成功', 'success');
        setShowGenerateModal(false);
        loadCodes();
        setGeneratedCodes(data.data.codes);
        setShowGeneratedCodesModal(true);
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '生成失败', 'error');
    }
  };

  const handleExportCSV = () => {
    const csvContent = "data:text/csv;charset=utf-8," 
      + ["支付码,金额,状态,使用者ID,使用时间"]
      .concat(generatedCodes.map(code => 
        `${code.code},${code.points},${code.used ? '已使用' : '未使用'},${code.usedBy || ''},${code.usedAt || ''}`
      ))
      .join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", "generated_payment_codes.csv");
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  if (loading) {
    return <div className="text-center py-4">加载中...</div>;
  }

  return (
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex justify-between mb-6">
        <h2 className="text-xl font-semibold">支付码管理</h2>
        <div className="space-x-4">
          <button
            onClick={handleExportAllCSV}
            className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
          >
            导出全部记录为CSV
          </button>
          <button
            onClick={() => setShowImportModal(true)}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            覆盖导入记录
          </button>
          <button
            onClick={() => setShowGenerateModal(true)}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            生成支付码
          </button>
        </div>
      </div>
      
      <table className="min-w-full divide-y divide-gray-200">
        <thead>
          <tr>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              支付码
            </th>
            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
              金额
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
            <th className="px-6 py-3 text-right text-sm font-medium">
              Actions
            </th>
          </tr>
        </thead>
        <tbody className="bg-white divide-y divide-gray-200">
          {codes
            .sort((a, b) => parseInt(a.id) - parseInt(b.id))
            .map((code) => (
              <tr key={code.id}>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-mono text-gray-900">
                  {code.code}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {code.points}币
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
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <button
                    onClick={() => handleEditCode(code)}
                    className="text-blue-600 hover:text-blue-900 mr-2"
                  >
                    编辑
                  </button>
                  <button
                    onClick={() => handleDeleteCode(parseInt(code.id))}
                    className="text-red-600 hover:text-red-900"
                  >
                    删除
                  </button>
                </td>
              </tr>
            ))}
        </tbody>
      </table>

      {showEditModal && editingCode && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-medium mb-4">编辑支付码</h3>
            <form onSubmit={(e) => handleUpdateCode(e, parseInt(editingCode.id))}>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  支付码
                </label>
                <input
                  type="text"
                  value={editingCode.code}
                  readOnly
                  className="mt-1 block w-full rounded-md border-gray-300 bg-gray-100 text-gray-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  金额
                </label>
                <input
                  type="number"
                  value={editingCode.points}
                  onChange={(e) => setEditingCode({ ...editingCode, points: parseFloat(e.target.value) })}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  使用者ID
                </label>
                <input
                  type="number"
                  value={editingCode.usedBy?.toString() || ''}
                  onChange={(e) => setEditingCode({ ...editingCode, usedBy: e.target.value ? e.target.value : null })}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  使用时间
                </label>
                <input
                  type="datetime-local"
                  value={editingCode.usedAt ? new Date(editingCode.usedAt).toISOString().slice(0, 16) : ''}
                  onChange={(e) => setEditingCode({ ...editingCode, usedAt: e.target.value ? new Date(e.target.value).toISOString() : null })}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>
              <div className="flex justify-end space-x-4 mt-6">
                <button
                  type="button"
                  onClick={() => setShowEditModal(false)}
                  className="px-4 py-2 text-gray-700 hover:text-gray-900"
                >
                  取消
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                  保存
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showImportModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md shadow-lg">
            <h3 className="text-lg font-semibold mb-4 text-center">导入记录</h3>
            <input
              type="file"
              accept=".csv, .xlsx"
              onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
              className="block w-full text-sm text-gray-700 border border-gray-300 rounded-md p-2"
            />
            <div className="flex justify-end space-x-4 mt-6">
              <button
                type="button"
                onClick={() => setShowImportModal(false)}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400"
              >
                取消
              </button>
              <button
                type="button"
                onClick={async () => {
                  if (!selectedFile) {
                    showToast('请选择一个文件', 'error');
                    return;
                  }

                  const formData = new FormData();
                  formData.append('file', selectedFile);

                  try {
                    const response = await fetch(`api.tokomoapp.org/api/admin/payment/setPay`, {
                      method: 'POST',
                      headers: {
                        'Authorization': `Bearer ${localStorage.getItem('token')}`
                      },
                      body: formData
                    });

                    const data = await response.json();
                    if (data.status === 'ok') {
                      showToast('导入成功', 'success');
                      loadCodes();
                      setShowImportModal(false);
                    } else {
                      throw new Error(data.message);
                    }
                  } catch (error) {
                    showToast(error instanceof Error ? error.message : '导入失败', 'error');
                  }
                }}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      )}

      {showGenerateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-medium mb-4">生成支付码</h3>
            <form onSubmit={handleGenerateCodes}>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  生成数量
                </label>
                <input
                  type="number"
                  min="1"
                  value={formData.amount}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    amount: parseInt(e.target.value)
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  金额
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

      {showGeneratedCodesModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <div className="flex justify-between items-center mb-4">
              <h3 className="font-semibold">生成的支付码</h3>
              <button
                onClick={() => setShowGeneratedCodesModal(false)}
                className="text-gray-500 hover:text-gray-700"
              >
                关闭
              </button>
            </div>
            <div className="space-y-3 mb-4">
              {generatedCodes.slice(0, 10).map((code, index) => (
                <div key={index} className="bg-gray-100 rounded shadow">
                  <code className="text-lg text-gray-800">{code.code}</code>
                  <div className="text-sm text-gray-600 mt-1">金额: {code.points}币</div>
                </div>
              ))}
              {generatedCodes.length > 10 && (
                <div className="text-center text-gray-500 text-lg">...</div>
              )}
            </div>
            <button
              onClick={handleExportCSV}
              className="px-4 py-2 bg-green-500 text-white rounded hover:bg-green-600"
            >
              导出全部生成的支付码为CSV
            </button>
          </div>
        </div>
      )}
    </div>
  );
}