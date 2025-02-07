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
  note?: string;
}

export function GameManagement() {
  const [games, setGames] = useState<Game[]>([]);
  const [showAddModal, setShowAddModal] = useState(false);
  const [editingGame, setEditingGame] = useState<Game | null>(null);
  const [formData, setFormData] = useState({
    gameName: '',
    gameType: '',
    downloadUrl: '',
    password: '',
    extractPassword: '',
    note: ''
  });
  const [loading, setLoading] = useState(false);
  const [selectedFile, setSelectedFile] = useState<File | null>(null);
  const [uploadMode, setUploadMode] = useState<'overwrite' | 'merge'>('overwrite');
  const [showUploadModal, setShowUploadModal] = useState(false);

  useEffect(() => {
    loadGames();
  }, []);

  const loadGames = async () => {
    try {
      setLoading(true);
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/admin/games`, {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      const data = await response.json();
      if (data.status === 'ok') {
        setGames(data.data);
      }
    } catch (error) {
      console.error('加载游戏列表失败:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const url = editingGame ? `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/admin/games/${editingGame.id}` : `${process.env.NEXT_PUBLIC_API_BASE_URL}/api/admin/games`;
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
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/admin/games/${id}`, {
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
        if (data.message.includes('外键约束')) {
          showToast('已有用户购买此游戏，无法删除', 'error');
        } else {
          throw new Error(data.message);
        }
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '删除失败', 'error');
    }
  };

  const handleUploadGames = async () => {
    if (!selectedFile) {
      showToast('请选择一个文件', 'error');
      return;
    }

    const formData = new FormData();
    formData.append('file', selectedFile);

    try {
      const response = await fetch(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/admin/games/upload?mode=${uploadMode}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: formData
      });

      const data = await response.json();
      if (data.status === 'ok') {
        showToast('上传成功', 'success');
        loadGames();
      } else {
        throw new Error(data.message);
      }
    } catch (error) {
      showToast(error instanceof Error ? error.message : '上传失败', 'error');
    }
  };

  return (
    <div className="p-6">
      <div className="flex justify-between mb-6">
        <h2 className="text-xl font-semibold">游戏列表</h2>
        <div className="flex space-x-4">
          <button
            onClick={() => setShowUploadModal(true)}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            上传游戏文件覆盖
          </button>
          <button
            onClick={() => {
              setFormData({
                gameName: '',
                gameType: '',
                downloadUrl: '',
                password: '',
                extractPassword: '',
                note: ''
              });
              setShowAddModal(true);
            }}
            className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
          >
            添加游戏
          </button>
        </div>
      </div>
      
      <div className="overflow-x-auto">
        <table className="w-full divide-y divide-gray-200">
          <thead>
            <tr>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[10%]">
                ID
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[60%]">
                游戏名称
              </th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider w-[30%]">
                操作
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {games
              .sort((a, b) => Number(a.id) - Number(b.id))
              .map((game) => (
                <tr key={game.id} className="hover:bg-gray-50">
                  <td className="px-4 py-4 text-sm text-gray-900">
                    {game.id}
                  </td>
                  <td className="px-4 py-4 text-sm text-gray-900 truncate max-w-0">
                    {game.gameName}
                  </td>
                  <td className="px-4 py-4 text-sm text-gray-500 space-x-4">
                    <button
                      onClick={() => {
                        setFormData({
                          gameName: game.gameName,
                          gameType: game.gameType,
                          downloadUrl: game.downloadUrl,
                          password: game.password || '',
                          extractPassword: game.extractPassword || '',
                          note: game.note || ''
                        });
                        setEditingGame(game);
                        setShowAddModal(true);
                      }}
                      className="text-blue-500 hover:text-blue-700"
                    >
                      编辑
                    </button>
                    <button
                      onClick={() => handleDelete(game.id)}
                      className="text-red-500 hover:text-red-700"
                    >
                      删除
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
                  value={formData.note || ''}
                  onChange={(e) => setFormData(prev => ({
                    ...prev,
                    note: e.target.value
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

      {showUploadModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center">
          <div className="bg-white rounded-lg p-6 w-full max-w-md">
            <h3 className="text-lg font-medium mb-4">上传游戏文件</h3>
            <input
              type="file"
              accept=".csv, .xlsx"
              onChange={(e) => setSelectedFile(e.target.files?.[0] || null)}
              className="block w-full text-sm text-gray-700 border border-gray-300 rounded-md p-2"
            />
            <select
              value={uploadMode}
              onChange={(e) => setUploadMode(e.target.value as 'overwrite' | 'merge')}
              className="block w-full text-sm text-gray-700 border border-gray-300 rounded-md p-2 mt-2"
            >
              <option value="overwrite">覆盖</option>
              <option value="merge">合并</option>
            </select>
            <div className="flex justify-end space-x-4 mt-6">
              <button
                type="button"
                onClick={() => setShowUploadModal(false)}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded hover:bg-gray-400"
              >
                取消
              </button>
              <button
                type="button"
                onClick={handleUploadGames}
                className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
              >
                确定
              </button>
            </div>
          </div>
        </div>
      )}

      {loading && <div>加载中...</div>}
    </div>
  );
} 