import { useEffect, useState } from "react";
import { Navigate } from "react-router";
import { api, REFRESH_TOKEN_KEY, setAccessToken } from "@/lib/api";

interface Props {
  children: React.ReactNode;
}

export const ProtectedRoute = ({ children }: Props) => {
  const [auth, setAuth] = useState<"loading" | "ok" | "fail">("loading");

  useEffect(() => {
    const checkAuth = async () => {
      try {
        await api.get("/auth/me");
        setAuth("ok");
        return;
      } catch {
        const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
        if (!refreshToken) {
          setAuth("fail");
          return;
        }

        try {
          const response = await api.post("/auth/refresh", { refreshToken });
          setAccessToken(response.data?.accessToken ?? null);
          await api.get("/auth/me");
          setAuth("ok");
        } catch {
          setAccessToken(null);
          localStorage.removeItem(REFRESH_TOKEN_KEY);
          setAuth("fail");
        }
      }
    };

    checkAuth();
  }, []);

  if (auth === "loading") return null;
  if (auth === "fail") return <Navigate to="/login" replace />;
  return <>{children}</>;
};
