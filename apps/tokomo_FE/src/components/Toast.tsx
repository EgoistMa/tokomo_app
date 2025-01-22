import { createRoot } from 'react-dom/client';

interface ToastProps {
  message: string;
  type: 'success' | 'error' | 'warning';
}

const Toast = ({ message, type }: ToastProps) => {
  return (
    <div className={`fixed top-4 left-1/2 -translate-x-1/2 px-6 py-3 rounded-lg shadow-lg text-white z-[9999]
      animate-fade-in
      ${type === 'success' ? 'bg-green-500' : 
        type === 'error' ? 'bg-red-500' : 
        'bg-yellow-500'}`}
    >
      {message}
    </div>
  );
};

export const showToast = (message: string, type: ToastProps['type'] = 'success') => {
  // 移除已存在的 toast
  const existingToast = document.querySelector('.toast-container');
  if (existingToast) {
    existingToast.remove();
  }

  const container = document.createElement('div');
  container.className = 'toast-container';
  document.body.appendChild(container);
  
  const root = createRoot(container);
  root.render(<Toast message={message} type={type} />);
  
  setTimeout(() => {
    container.classList.add('fade-out');
    setTimeout(() => {
      root.unmount();
      container.remove();
    }, 300);
  }, 3000);
}; 