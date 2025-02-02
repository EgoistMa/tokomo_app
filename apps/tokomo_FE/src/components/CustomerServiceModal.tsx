import { siteConfig } from "@/config/site";
import Image from "next/image";

interface CustomerServiceModalProps {
  visible: boolean;
  onClose: () => void;
}

export function CustomerServiceModal({ visible, onClose }: CustomerServiceModalProps) {
  if (!visible) return null;
  
  const { title, qq, qrCode } = siteConfig.customerService;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold">{title}</h3>
        </div>
        
        <div className="space-y-4">
          {/* 二维码图片 */}
          <div className={`${qrCode.width} ${qrCode.height} mx-auto relative`}>
            <Image
              src={qrCode.url}
              alt="客服二维码"
              fill
              className="object-contain"
            />
          </div>
          
          {/* QQ号 */}
          <div className="text-center text-gray-700">
            {qq.label}{qq.number}
          </div>
        </div>

        <div className="mt-6 flex justify-end">
          <button
            onClick={onClose}
            className="px-4 py-2 bg-gray-100 text-gray-700 rounded hover:bg-gray-200"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  );
} 