import { useEffect, useState } from "react";
import { Navigate } from "react-router";
import { api, setAccessToken } from "@/lib/api";

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
        try {
          // El refresh token viaja en cookie httpOnly (path=/api/v1/auth/refresh)
          const response = await api.post("/auth/refresh");
          setAccessToken(response.data?.accessToken ?? null);
          await api.get("/auth/me");
          setAuth("ok");
        } catch {
          setAccessToken(null);
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
