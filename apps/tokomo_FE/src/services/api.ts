export const searchProducts = async (keyword: string) => {
  try {
    const response = await fetch(`/api/games/search?keyword=${encodeURIComponent(keyword)}`);
    const data = await response.json();
    return {
      success: true,
      data
    };
  } catch (error) {
    console.error('搜索失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '未知错误'
    };
  }
};