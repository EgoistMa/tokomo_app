import defaultConfig from '../../public/config/site-config.json';

let siteConfig = defaultConfig;

// 在客户端动态加载配置
if (typeof window !== 'undefined') {
  fetch('/config/site-config.json')
    .then(res => res.json())
    .then(config => {
      siteConfig = config;
    })
    .catch(err => {
      console.error('加载配置失败:', err);
    });
}

export { siteConfig }; 