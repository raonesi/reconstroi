"use client";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useState,
} from "react";
import { api, authStorage } from "@/lib/api";
import type { AuthResponse, Usuario } from "@/lib/types";

type AuthContextValue = {
  user: Usuario | null;
  loading: boolean;
  establish: (a: AuthResponse) => void;
  logout: () => void;
  refresh: () => Promise<void>;
};
const AuthContext = createContext<AuthContextValue>({
  user: null,
  loading: true,
  establish: () => {},
  logout: () => {},
  refresh: async () => {},
});
export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<Usuario | null>(null);
  const [loading, setLoading] = useState(true);
  const clearSession = useCallback(() => {
    authStorage.clear();
    setUser(null);
  }, []);
  const logout = useCallback(() => {
    // Encerra a sessão no servidor (best effort) antes de esquecer o token local.
    if (authStorage.get()) api.logout().catch(() => undefined);
    clearSession();
  }, [clearSession]);
  const refresh = useCallback(async () => {
    if (!authStorage.get()) {
      setLoading(false);
      return;
    }
    try {
      setUser(await api.me());
    } catch {
      clearSession();
    } finally {
      setLoading(false);
    }
  }, [clearSession]);
  useEffect(() => {
    Promise.resolve().then(refresh);
    const handle = () => clearSession();
    window.addEventListener("reconstroi:unauthorized", handle);
    return () => window.removeEventListener("reconstroi:unauthorized", handle);
  }, [refresh, clearSession]);
  const establish = (auth: AuthResponse) => {
    authStorage.set(auth.token);
    setUser(auth.usuario);
    setLoading(false);
  };
  return (
    <AuthContext.Provider value={{ user, loading, establish, logout, refresh }}>
      {children}
    </AuthContext.Provider>
  );
}
export const useAuth = () => useContext(AuthContext);
