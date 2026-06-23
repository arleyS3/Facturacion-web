import { useEffect, useState } from "react";
import { Navigate } from "react-router";
import { api } from "@/lib/api";

interface Props {
  children: React.ReactNode;
}

export const ProtectedRoute = ({ children }: Props) => {
  const [auth, setAuth] = useState<"loading" | "ok" | "fail">("loading");

  useEffect(() => {
    api
      .get("/auth/me")
      .then(() => setAuth("ok"))
      .catch(() => setAuth("fail"));
  }, []);

  if (auth === "loading") return null;
  if (auth === "fail") return <Navigate to="/login" replace />;
  return <>{children}</>;
};
