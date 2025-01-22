import { showToast } from '@/components/Toast';

interface GameDetail {
  id: string;
  gameType: string | null;
  gameName: string;
  downloadUrl: string | null;
  password: string | null;
  extractPassword: string | null;
}

interface GameDetailCardProps {
  game: GameDetail | null;
  onClose: () => void;
  visible: boolean;
}

export function GameDetailCard({ game, onClose, visible }: GameDetailCardProps) {
  if (!visible || !game) return null;

  const handleCopy = (text: string) => {
    if (!text) return;
    navigator.clipboard.writeText(text);
    showToast('复制成功', 'success');
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-md w-full mx-4">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold">{game.gameName}</h3>
        </div>
        
        <div className="space-y-4">
          <div>
            <span className="font-medium">类型：</span>
            <span>{game.gameType || '无密码'}</span>
          </div>
          
          <div className="flex items-center space-x-2">
            <span className="font-medium">下载地址：</span>
            <span className="text-gray-600 break-all flex-1">
              {game.downloadUrl || '无密码'}
            </span>
            <button
              onClick={() => handleCopy(game.downloadUrl!)}
              disabled={!game.downloadUrl}
              className={`px-2 py-1 text-sm rounded flex-shrink-0 ${
                game.downloadUrl 
                  ? 'bg-blue-500 text-white hover:bg-blue-600' 
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
            >
              复制
            </button>
          </div>
          
          <div className="flex items-center space-x-2">
            <span className="font-medium">提取码：</span>
            <span className="font-mono text-gray-600">
              {game.password || '无密码'}
            </span>
            <button
              onClick={() => handleCopy(game.password!)}
              disabled={!game.password}
              className={`px-2 py-1 text-sm rounded ${
                game.password 
                  ? 'bg-blue-500 text-white hover:bg-blue-600' 
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
            >
              复制
            </button>
          </div>
          
          <div className="flex items-center space-x-2">
            <span className="font-medium">解压密码：</span>
            <span className="font-mono text-gray-600">
              {game.extractPassword || '无密码'}
            </span>
            <button
              onClick={() => handleCopy(game.extractPassword!)}
              disabled={!game.extractPassword}
              className={`px-2 py-1 text-sm rounded ${
                game.extractPassword 
                  ? 'bg-blue-500 text-white hover:bg-blue-600' 
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
            >
              复制
            </button>
          </div>
        </div>

        <div className="mt-6 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
          >
            关闭(非vip用户关闭后需要重新付费)
          </button>
        </div>
      </div>
    </div>
  );
} 