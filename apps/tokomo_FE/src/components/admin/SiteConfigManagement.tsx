import { useState, useEffect } from 'react';
import { toast } from 'react-hot-toast';

interface QRCode {
  url: string;
  width: string;
  height: string;
}

interface CustomerService {
  title: string;
  qq: {
    number: string;
    label: string;
  };
  qrCode: QRCode;
}

interface CarouselItem {
  image: string;
  title: string;
  description: string;
  link: string;
}

interface Banner {
  text: string;
  image: string;
  link: string;
}

interface SiteConfig {
  customerService: CustomerService;
  carousel: {
    items: CarouselItem[];
    settings: {
      autoplay: boolean;
      interval: number;
      showDots: boolean;
      showArrows: boolean;
    };
  };
  banners: {
    left: Banner;
    right: Banner;
  };
}

export function SiteConfigManagement() {
  const [config, setConfig] = useState<SiteConfig | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchConfig();
  }, []);

  const fetchConfig = async () => {
    try {
      const response = await fetch('//api.tokomoapp.org/api/admin/site-config', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      if (!response.ok) throw new Error('获取配置失败');
      const { data } = await response.json();
      setConfig(typeof data === 'string' ? JSON.parse(data) : data);
      setLoading(false);
    } catch (error) {
      console.error('获取配置失败:', error);
      toast.error('获取配置失败');
    }
  };

  const handleSave = async () => {
    try {
      const response = await fetch('//api.tokomoapp.org/api/admin/site-config', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify(config)
      });
      
      if (!response.ok) throw new Error('更新失败');
      toast.success('配置更新成功');
    } catch (error) {
      console.error('保存配置失败:', error);
      toast.error('保存配置失败');
    }
  };

  if (loading || !config) {
    return <div className="p-6">加载中...</div>;
  }

  return (
    <div className="p-6">
      <div className="flex justify-between mb-6">
        <h2 className="text-xl font-bold">站点配置管理</h2>
        <button
          onClick={handleSave}
          className="px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600"
        >
          保存更改
        </button>
      </div>

      <div className="space-y-6">
        <div className="border rounded-lg p-4">
          <h3 className="font-bold mb-4">客服配置</h3>
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1">QQ号码</label>
              <input
                type="text"
                value={config.customerService.qq.number}
                onChange={(e) => setConfig({
                  ...config,
                  customerService: {
                    ...config.customerService,
                    qq: { ...config.customerService.qq, number: e.target.value }
                  }
                })}
                className="border rounded px-3 py-2 w-full"
              />
            </div>
            <div>
              <label className="block text-sm font-medium mb-1">二维码URL</label>
              <input
                type="text"
                value={config.customerService.qrCode.url}
                onChange={(e) => setConfig({
                  ...config,
                  customerService: {
                    ...config.customerService,
                    qrCode: { ...config.customerService.qrCode, url: e.target.value }
                  }
                })}
                className="border rounded px-3 py-2 w-full"
              />
            </div>
          </div>
        </div>

        <div className="border rounded-lg p-4">
          <div className="flex justify-between items-center mb-4">
            <h3 className="font-bold">轮播图配置</h3>
            <button
              onClick={() => {
                const newItems = [...config.carousel.items, {
                  image: "",
                  title: "",
                  description: "",
                  link: ""
                }];
                setConfig({
                  ...config,
                  carousel: { ...config.carousel, items: newItems }
                });
              }}
              className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600"
            >
              添加轮播图
            </button>
          </div>
          
          {config.carousel.items.map((item: CarouselItem, index: number) => (
            <div key={index} className="mb-4 p-4 border rounded relative">
              <button
                onClick={() => {
                  const newItems = config.carousel.items.filter((_: CarouselItem, i: number) => i !== index);
                  setConfig({
                    ...config,
                    carousel: { ...config.carousel, items: newItems }
                  });
                }}
                className="absolute top-2 right-2 px-2 py-1 bg-red-500 text-white rounded hover:bg-red-600"
              >
                删除
              </button>
              <h4 className="font-medium mb-2">轮播图 {index + 1}</h4>
              <div className="space-y-2">
                <input
                  type="text"
                  placeholder="图片URL"
                  value={item.image}
                  onChange={(e) => {
                    const newItems = [...config.carousel.items];
                    newItems[index] = { ...newItems[index], image: e.target.value };
                    setConfig({
                      ...config,
                      carousel: { ...config.carousel, items: newItems }
                    });
                  }}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="标题"
                  value={item.title}
                  onChange={(e) => {
                    const newItems = [...config.carousel.items];
                    newItems[index] = { ...newItems[index], title: e.target.value };
                    setConfig({
                      ...config,
                      carousel: { ...config.carousel, items: newItems }
                    });
                  }}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="描述"
                  value={item.description}
                  onChange={(e) => {
                    const newItems = [...config.carousel.items];
                    newItems[index] = { ...newItems[index], description: e.target.value };
                    setConfig({
                      ...config,
                      carousel: { ...config.carousel, items: newItems }
                    });
                  }}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="链接"
                  value={item.link}
                  onChange={(e) => {
                    const newItems = [...config.carousel.items];
                    newItems[index] = { ...newItems[index], link: e.target.value };
                    setConfig({
                      ...config,
                      carousel: { ...config.carousel, items: newItems }
                    });
                  }}
                  className="border rounded px-3 py-2 w-full"
                />
              </div>
            </div>
          ))}
        </div>

        {/* Banner配置 */}
        <div className="border rounded-lg p-4">
          <h3 className="font-bold mb-4">Banner配置</h3>
          <div className="space-y-6">
            {/* 左侧Banner */}
            <div>
              <h4 className="font-medium mb-2">左侧Banner</h4>
              <div className="space-y-2">
                <input
                  type="text"
                  placeholder="文本"
                  value={config.banners.left.text}
                  onChange={(e) => setConfig({
                    ...config,
                    banners: {
                      ...config.banners,
                      left: { ...config.banners.left, text: e.target.value }
                    }
                  })}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="图片URL"
                  value={config.banners.left.image}
                  onChange={(e) => setConfig({
                    ...config,
                    banners: {
                      ...config.banners,
                      left: { ...config.banners.left, image: e.target.value }
                    }
                  })}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="链接"
                  value={config.banners.left.link}
                  onChange={(e) => setConfig({
                    ...config,
                    banners: {
                      ...config.banners,
                      left: { ...config.banners.left, link: e.target.value }
                    }
                  })}
                  className="border rounded px-3 py-2 w-full"
                />
              </div>
            </div>

            {/* 右侧Banner */}
            <div>
              <h4 className="font-medium mb-2">右侧Banner</h4>
              <div className="space-y-2">
                <input
                  type="text"
                  placeholder="文本"
                  value={config.banners.right.text}
                  onChange={(e) => setConfig({
                    ...config,
                    banners: {
                      ...config.banners,
                      right: { ...config.banners.right, text: e.target.value }
                    }
                  })}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="图片URL"
                  value={config.banners.right.image}
                  onChange={(e) => setConfig({
                    ...config,
                    banners: {
                      ...config.banners,
                      right: { ...config.banners.right, image: e.target.value }
                    }
                  })}
                  className="border rounded px-3 py-2 w-full"
                />
                <input
                  type="text"
                  placeholder="链接"
                  value={config.banners.right.link}
                  onChange={(e) => setConfig({
                    ...config,
                    banners: {
                      ...config.banners,
                      right: { ...config.banners.right, link: e.target.value }
                    }
                  })}
                  className="border rounded px-3 py-2 w-full"
                />
              </div>
            </div>
          </div>
        </div>

        {/* 可以根据需要添加更多配置项的编辑区域 */}
      </div>
    </div>
  );
} 