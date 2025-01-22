interface CustomerServiceModalProps {
  visible: boolean;
  onClose: () => void;
}

export function CustomerServiceModal({ visible, onClose }: CustomerServiceModalProps) {
  if (!visible) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-sm w-full mx-4">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold">联系客服</h3>
        </div>
        
        <div className="space-y-4">
          {/* 模拟二维码 */}
          <div className="w-48 h-48 mx-auto bg-gray-200 flex items-center justify-center">
            <div className="w-32 h-32 bg-gray-400"></div>
          </div>
          
          {/* QQ号 */}
          <div className="text-center text-gray-700">
            QQ号：12345678
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