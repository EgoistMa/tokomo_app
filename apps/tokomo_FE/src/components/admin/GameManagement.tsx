"use client";
import { useState, useEffect } from 'react';
import { showToast } from '@/components/Toast';

interface Game {
  id: string;
  gameName: string;
  gameType: string;
  downloadUrl: string;
  password?: string;
  extractPassword?: string;
  remark?: string;
}

export function GameManagement() {
  const [games, setGames] = useState<Game[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingGame, setEditingGame] = useState<Game | null>(null);
  const [formData, setFormData] = useState({
    gameName: '',
    gameType: '',
    downloadUrl: '',
    password: '',
    extractPassword: '',
    remark: ''
  });

  useEffect(() => {
    loadGames();
  }, []);

  const loadGames = async () => {
    try {
      const response = await fetch('/api/admin/games', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setGames(data.data);
      }
    } catch (error) {
      showToast('加载游戏列表失败', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const url = editingGame ? `/api/admin/games/${editingGame.id}` : '/api/admin/games';
      const method = editingGame ? 'PUT' : 'POST';
      
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(formData)
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast(editingGame ? '更新成功' : '添加成功', 'success');
        setShowAddModal(false);
        setEditingGame(null);
        loadGames();
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '操作失败', 'error');
    }
  };

  const handleDelete = async (id: string) => {
    if (!confirm('确定要删除这个游戏吗？')) return;
    
    try {
      const response = await fetch(`/api/admin/games/${id}`, {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('删除成功', 'success');
        loadGames();
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '删除失败', 'error');
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between mb-6">
        <h2 className="text-xl font-semibold">游戏列表</h2>
        <button 
          onClick={() => {
            setFormData({
              gameName: '',
              gameType: '',
              downloadUrl: '',
              password: '',
              extractPassword: '',
              remark: ''
            });
            setShowAddModal(true);
          }}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          添加游戏
        </button>
      </div>
      
      <div className="overflow-x-auto">
        <table className="w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[70%]">
                游戏名称
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[30%]">
                操作
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {games.map((game) => (
              <tr key={game.id} className="hover:bg-gray-50">
                <td className="px-4 py-4 text-sm text-gray-900 truncate max-w-0">
                  {game.gameName}
                </td>
                <td className="px-4 py-4 text-sm text-gray-500">
                  <button
                    onClick={() => {
                      setFormData(game);
                      setEditingGame(game);
                      setShowAddModal(true);
                    }}
                    className="text-blue-500 hover:text-blue-700"
                  >
                    编辑
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 添加/编辑游戏弹窗 */}
      {showAddModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-medium mb-4">
              {editingGame ? '编辑游戏' : '添加游戏'}
            </h3>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  游戏名称
                </label>
                <input
                  type="text"
                  value={formData.gameName}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    gameName: e.target.value
                  }))}
                  className={`mt-1 block w-full rounded-md border-gray-300 shadow-sm 
                    ${editingGame 
                      ? 'bg-gray-100 cursor-not-allowed' 
                      : 'focus:border-blue-500 focus:ring-blue-500'
                    }`}
                  required
                  disabled={!!editingGame}
                  title={editingGame ? "游戏名称不可修改" : ""}
                />
                {editingGame && (
                  <p className="mt-1 text-sm text-gray-500">
                    游戏名称不可修改
                  </p>
                )}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  游戏类型
                </label>
                <input
                  type="text"
                  value={formData.gameType}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    gameType: e.target.value
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  下载链接
                </label>
                <input
                  type="url"
                  value={formData.downloadUrl}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    downloadUrl: e.target.value
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  required
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  解压密码
                </label>
                <input
                  type="text"
                  value={formData.extractPassword}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    extractPassword: e.target.value
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  下载密码
                </label>
                <input
                  type="text"
                  value={formData.password}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    password: e.target.value
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">
                  备注
                </label>
                <input
                  type="text"
                  value={formData.remark || ''}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    remark: e.target.value
                  }))}
                  className="mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500"
                  placeholder="可选"
                />
              </div>
              <div className="flex justify-end space-x-4 mt-6">
                <button
                  type="button"
                  onClick={() => {
                    setShowAddModal(false);
                    setEditingGame(null);
                  }}
                  className="px-4 py-2 text-gray-700 hover:text-gray-900"
                >
                  取消
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
                >
                  {editingGame ? '保存' : '添加'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
} 