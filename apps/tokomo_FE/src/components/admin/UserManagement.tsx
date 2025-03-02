"use client";
import { useState, useEffect } from 'react';
import { showToast } from '@/components/Toast';

interface User {
  id: number;
  username: string;
  points: number;
  vipExpireDate: string | null;
  isActive: boolean;
  isAdmin: boolean;
  createdAt: string;
  lastLoginAt: string;
}

export function UserManagement() {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [showEditModal, setShowEditModal] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formData, setFormData] = useState({
    points: 0,
    vipExpireDate: '',
    isAdmin: false,
  });

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      const response = await fetch(`//api.tokomoapp.org/api/admin/users`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setUsers(data.data);
      }
    } catch (error) {
      console.error('加载用户列表失败', error);
      showToast('加载用户列表失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleToggleActive = async (userId: number, currentStatus: boolean) => {
    try {
      const response = await fetch(`//api.tokomoapp.org/api/admin/users/${userId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({
          isActive: !currentStatus  // 只发送isActive字段
        })
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast(currentStatus ? '用户已禁用' : '用户已启用', 'success');
        // 更新本地状态
        setUsers(users.map(user => 
          user.id === userId 
            ? { ...user, isActive: !currentStatus }
            : user
        ));
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '操作失败', 'error');
    }
  };

  const handleEdit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingUser) return;

    try {
      const response = await fetch(`//api.tokomoapp.org/api/admin/users/${editingUser.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(formData)
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('更新成功', 'success');
        setShowEditModal(false);
        setEditingUser(null);
        // 更新本地状态
        setUsers(users.map(user => 
          user.id === editingUser.id
            ? { ...user, points: formData.points, vipExpireDate: formData.vipExpireDate }
            : user
        ));
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '操作失败', 'error');
    }
  };

  if (loading) {
    return <div className="text-center py-4">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="overflow-x-auto">
        <table className="w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[70%]">
                用户名
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[30%]">
                操作
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50">
                <td className="px-4 py-4 text-sm text-gray-900 truncate max-w-0">
                  {user.username}
                </td>
                <td className="px-4 py-4 text-sm text-gray-500 space-x-4">
                  <button
                    onClick={() => {
                      setFormData({
                        points: user.points,
                        vipExpireDate: user.vipExpireDate || '',
                        isAdmin: user.isAdmin,
                      });
                      setEditingUser(user);
                      setShowEditModal(true);
                    }}
                    className="text-blue-500 hover:text-blue-700"
                  >
                    编辑
                  </button>
                  <button
                    onClick={() => handleToggleActive(user.id, user.isActive)}
                    className={`${
                      user.isActive 
                        ? 'text-red-500 hover:text-red-700' 
                        : 'text-green-500 hover:text-green-700'
                    }`}
                  >
                    {user.isActive ? '禁用' : '启用'}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 编辑用户弹窗 */}
      {showEditModal && editingUser && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-medium mb-4">
              编辑用户: {editingUser.username}
            </h3>
            <form onSubmit={handleEdit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  许愿币
                </label>
                <input
                  type="number"
                  value={formData.points}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    points: parseInt(e.target.value)
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  VIP到期时间
                </label>
                <input
                  type="datetime-local"
                  value={formData.vipExpireDate}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    vipExpireDate: e.target.value
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>
              <div className="flex items-center space-x-2">
                <input
                  type="checkbox"
                  id="isAdmin"
                  checked={formData.isAdmin}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    isAdmin: e.target.checked
                  }))}
                  className="rounded border-gray-300 text-blue-500 focus:ring-blue-500"
                />
                <label htmlFor="isAdmin" className="text-sm font-medium text-gray-700">
                  管理员权限
                </label>
              </div>
              <div className="flex justify-end space-x-4 mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowEditModal(false);
                    setEditingUser(null);
                  }}
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
    </div>
  );
}