export interface SiteConfig {
  customerService: {
    title: string;
    qq: {
      number: string;
      label: string;
    };
    qrCode: {
      url: string;
      width: string;
      height: string;
    };
  };
  carousel: {
    items: Array<{
      image: string;
      title: string;
      description: string;
      link: string;
    }>;
    settings: {
      autoplay: boolean;
      interval: number;
      showDots: boolean;
      showArrows: boolean;
    };
  };
  banners: {
    left: {
      text: string;
      image: string;
      link: string;
    };
    right: {
      text: string;
      image: string;
      link: string;
    };
  };
  footer: {
    sections: Array<{
      title: string;
      links: Array<{
        text: string;
        href: string;
      }>;
    }>;
    copyright: string;
  };
  purchaseGuide: {
    platforms: Array<{
      id: string;
      name: string;
      icon: string;
      url: string;
      color: string;
    }>;
    steps: {
      [key: string]: Array<{
        title: string;
        description: string;
        image: string;
      }>;
    };
  };
}

export async function getSiteConfig(): Promise<SiteConfig> {
  const response = await fetch('//api.tokomoapp.org/api/site-config');
  if (!response.ok) {
    throw new Error('Failed to fetch site config');
  }
  const { data } = await response.json();
  return typeof data === 'string' ? JSON.parse(data) : data;
} 