"use client";
import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import { CheckCircle2, CircleAlert, X } from "lucide-react";

type Toast = { id: number; message: string; type: "success" | "error" };
const ToastContext = createContext<{
  success: (m: string) => void;
  error: (m: string) => void;
}>({ success: () => {}, error: () => {} });
export function ToastProvider({ children }: { children: React.ReactNode }) {
  const [toasts, setToasts] = useState<Toast[]>([]);
  const show = useCallback((message: string, type: Toast["type"]) => {
    const id = Date.now() + Math.random();
    setToasts((v) => [...v, { id, message, type }]);
    setTimeout(() => setToasts((v) => v.filter((t) => t.id !== id)), 4500);
  }, []);
  const value = useMemo(
    () => ({
      success: (m: string) => show(m, "success"),
      error: (m: string) => show(m, "error"),
    }),
    [show],
  );
  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-stack" aria-live="polite">
        {toasts.map((t) => (
          <div key={t.id} className={`toast ${t.type}`}>
            {t.type === "success" ? <CheckCircle2 /> : <CircleAlert />}
            <span>{t.message}</span>
            <button
              aria-label="Fechar"
              onClick={() => setToasts((v) => v.filter((x) => x.id !== t.id))}
            >
              <X size={16} />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}
export const useToast = () => useContext(ToastContext);
