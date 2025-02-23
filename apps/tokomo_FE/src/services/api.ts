export const searchProducts = async (keyword: string) => {
  try {
    const response = await fetch(`//api.tokomoapp.org/api/games/search?keyword=${encodeURIComponent(keyword)}`);
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

export const fetchWithAuth = async (url: string, options: RequestInit = {}) => {
  try {
    const response = await fetch(url, {
      ...options,
      headers: {
        ...options.headers,
        'Authorization': `Bearer ${localStorage.getItem('token')}`
      }
    });
    const data = await response.json();
    return {
      success: true,
      data
    };
  } catch (error) {
    console.error('请求失败:', error);
    return {
      success: false,
      error: error instanceof Error ? error.message : '未知错误'
    };
  }
};