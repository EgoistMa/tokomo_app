"use client";
import { Suspense, useEffect, useState } from 'react';
import { useSearchParams, useRouter } from 'next/navigation';
import Link from 'next/link';
import { GameDetailCard } from '@/components/GameDetailCard';
import { showToast } from '@/components/Toast';
import { authService } from '@/services/auth';

interface Game {
  id: string;
  gameType: string | null;
  gameName: string;
  downloadUrl: string | null;
  password: string | null;
  extractPassword: string | null;
}

interface SearchResult {
  remainingPoints: number;
  games: Game[];
}

interface ApiResponse {
  status: string;
  message: string;
  data: Game[];
}

interface GameResponse {
  game: Game;
  remainingPoints: number;
}

function SearchResults() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const [searchResult, setSearchResult] = useState<SearchResult>({
    remainingPoints: 0,
    games: []
  });
  const [loading, setLoading] = useState(true);
  const [selectedGame, setSelectedGame] = useState<Game | null>(null);
  const [showDetail, setShowDetail] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    const keyword = searchParams.get('keyword');
    if (!keyword) {
      router.push('/');
      return;
    }

    // 尝试从 sessionStorage 获取搜索结果
    const savedResult = sessionStorage.getItem('searchResult');
    if (savedResult) {
      try {
        const apiResponse: ApiResponse = JSON.parse(savedResult);
        if (apiResponse.status === 'ok') {
          setSearchResult({
            remainingPoints: 0, // 如果API没有返回这个值，就默认为0
            games: apiResponse.data || []
          });
        }
        setLoading(false);
        sessionStorage.removeItem('searchResult');
        return;
      } catch (error) {
        console.error('解析搜索结果失败:', error);
      }
    }

    // 如果没有存储的结果，则重新请求
    const fetchResults = async () => {
      try {
        const token = localStorage.getItem('token');
        if (!token) {
          router.push('/login');
          return;
        }

        const response = await fetch(`/api/games/search?keyword=${encodeURIComponent(keyword)}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
          },
        });

        const apiResponse: ApiResponse = await response.json();
        if (response.status === 200 && apiResponse.status === 'ok') {
          setSearchResult({
            remainingPoints: 0, // 如果API没有返回这个值，就默认为0
            games: apiResponse.data || []
          });
        } else {
          console.error('搜索失败:', apiResponse.message);
        }
      } catch (error) {
        console.error('搜索错误:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchResults();
  }, [searchParams, router]);

  useEffect(() => {
    const fetchPoints = async () => {
      try {
        const response = await authService.getProfile();
        if (response.status === 'ok') {
          setSearchResult(prev => ({
            ...prev,
            remainingPoints: response.data.points
          }));
        }
      } catch (error) {
        console.error('获取用户积分失败:', error);
      }
    };

    fetchPoints();
  }, []);

  const handleClaim = async (gameId: string) => {
    setIsLoading(true);
    try {
      const token = localStorage.getItem('token');
      const response = await fetch(`/api/games/${gameId}`, {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      });

      const data = await response.json();
      console.log('API Response:', data);

      if (response.status === 200 && data.status === 'ok') {
        const gameDetail: GameResponse = data.data;
        console.log('Game Detail:', gameDetail);
        
        if (gameDetail && gameDetail.game) {
          setSelectedGame(gameDetail.game);
          setSearchResult(prev => ({
            ...prev,
            remainingPoints: gameDetail.remainingPoints
          }));
          setShowDetail(true);
        } else {
          showToast('游戏详情数据不完整', 'error');
        }
      } else if (data.status === 'error' && data.message.includes('Insufficient points')) {
        const requiredPoints = data.message.match(/Required: (\d+)/)?.[1];
        const message = requiredPoints 
          ? `积分不足，需要${requiredPoints}积分。请充值或开通VIP来领取愿望！`
          : '积分不足，请充值或开通VIP来领取愿望！';
        
        showToast(message, 'warning');
      } else {
        showToast('获取游戏详情失败', 'error');
      }
    } catch (error) {
      console.error('获取游戏详情错误:', error);
      showToast('系统错误，请稍后重试', 'error');
    } finally {
      setIsLoading(false);
    }
  };

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-600">加载中...</div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 导航栏 */}
      <nav className="bg-white shadow">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16">
            <div className="flex items-center">
              <Link href="/" className="text-gray-600 hover:text-gray-900">
                返回首页
              </Link>
            </div>
            <div className="flex items-center">
              <span className="text-gray-600">
                剩余许愿币：{searchResult?.remainingPoints || 0}
              </span>
            </div>
          </div>
        </div>
      </nav>

      {/* 搜索结果 */}
      <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <h1 className="text-2xl font-bold text-gray-900 mb-8">搜索结果</h1>
        
        <div className="grid gap-6">
          {searchResult.games.map((game) => (
            <div 
              key={game.id}
              className="bg-white shadow rounded-lg p-6 flex items-center justify-between"
            >
              <div className="flex-1">
                <h2 className="text-xl font-semibold text-gray-900">
                  {game.gameName}
                </h2>
                {game.gameType && (
                  <p className="text-sm text-gray-500 mt-1">
                    类型：{game.gameType}
                  </p>
                )}
              </div>
              <button
                onClick={() => handleClaim(game.id)}
                disabled={isLoading}
                className={`ml-4 px-4 py-2 bg-blue-500 text-white rounded-md transition-colors
                  ${isLoading ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-600'}`}
              >
                {isLoading ? '加载中...' : '领取愿望(花费80许愿币)'}
              </button>
            </div>
          ))}
        </div>

        {searchResult.games.length === 0 && (
          <div className="text-center text-gray-600">
            没有找到相关游戏
          </div>
        )}
      </div>

      {/* 添加游戏详情卡片 */}
      <GameDetailCard
        game={selectedGame}
        visible={showDetail}
        onClose={() => {
          setShowDetail(false);
          setSelectedGame(null);
        }}
      />
    </div>
  );
}

export default function ResultPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-gray-600">加载中...</div>
      </div>
    }>
      <SearchResults />
    </Suspense>
  );
} 