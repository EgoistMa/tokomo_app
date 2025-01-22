interface LoginData {
  username: string;
  password: string;
}

interface RegisterData {
  username: string;
  password: string;
  question: string;
  answer: string;
  vipCode?: string;
}

interface LoginResponse {
  status: string;
  message: string;
  data: {
    token: string;
  }
}

interface RegisterResponse {
  status: string;
  message: string;
  data: {
    token: string;
  }
}

// 添加充值相关接口
interface DepositTransaction {
  transactionId: number;
  type: string;
  amount: number;
  fromUser: number;
  externalTransactionKey: string;
  createdAt: string;
  status: string;
}

interface DepositResponse {
  status: string;
  message: string;
  data: {
    transaction: DepositTransaction;
  }
}

// 在authService中添加解决交易的方法
interface ResolveTransactionResponse {
  status: string;
  message: string;
  data: {
    transaction: DepositTransaction;
  }
}

// 添加兑换VIP接口
interface RedeemVIPResponse {
  status: string;
  message: string;
  data: {
    expireDate: string;
  }
}

export const authService = {
  // 登录
  async login(data: LoginData): Promise<LoginResponse> {
    try {
      console.log('发送登录请求:', {
        url: '/api/user/login',
        data: {
          username: data.username,
          password: data.password
        }
      });

      const response = await fetch('/api/user/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: data.username,
          password: data.password
        }),
      });

      const result = await response.json();
      console.log('登录响应结果:', result);

      if (result.status !== 'ok') {
        throw new Error(result.message || '登录失败');
      }

      return result;
    } catch (error) {
      console.error('登录错误:', error);
      throw error;
    }
  },

  // 注册
  async register(data: RegisterData): Promise<RegisterResponse> {
    try {
      const response = await fetch('/api/user/register', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          username: data.username,
          password: data.password,
          question: data.question,
          answer: data.answer,
          vipCode: data.vipCode
        }),
      });

      const result = await response.json();

      if (result.status !== 'ok') {
        throw new Error(result.message || '注册失败');
      }

      return result;
    } catch (error) {
      throw error;
    }
  },

  // 获取用户资料
  getProfile: async () => {
    const token = localStorage.getItem('token');
    const response = await fetch('/api/user/profile', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    return response.json();
  },

  // 充值请求
  async deposit(amount: number): Promise<DepositResponse> {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/user/deposit', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ amount }),
      });

      const result = await response.json();

      if (result.status !== 'ok') {
        throw new Error(result.message || '充值请求失败');
      }

      return result;
    } catch (error) {
      throw error;
    }
  },

  // 解决交易
  async resolveTransaction(externalTransactionKey: string): Promise<ResolveTransactionResponse> {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/user/transaction/resolve', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          externalTransactionKey,
          resolveType: 'success'
        }),
      });

      const result = await response.json();

      if (result.status !== 'ok') {
        throw new Error(result.message || '交易确认失败');
      }

      return result;
    } catch (error) {
      throw error;
    }
  },

  // VIP兑换
  async redeemVIP(code: string): Promise<RedeemVIPResponse> {
    try {
      const token = localStorage.getItem('token');
      const response = await fetch('/api/user/redeem-vip', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ code }),
      });

      const result = await response.json();

      if (result.status !== 'ok') {
        throw new Error(result.message || 'VIP兑换失败');
      }

      return result;
    } catch (error) {
      throw error;
    }
  },
}; 