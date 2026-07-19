import { useEffect, useState } from "react";
import { Navigate } from "react-router";
import api from "@/lib/api";
import { Loader2 } from "lucide-react";

interface Props {
  children: React.ReactNode;
}

export const AdminRoute = ({ children }: Props) => {
  const [roleState, setRoleState] = useState<"loading" | "admin" | "forbidden">(() => {
    const cachedRole = localStorage.getItem("user_role");
    if (cachedRole === "ADMIN") return "admin";
    if (cachedRole && cachedRole !== "ADMIN") return "forbidden";
    return "loading";
  });

  useEffect(() => {
    const checkRole = async () => {
      try {
        const res = await api.get<{ email: string; role: string }>("/auth/me");
        localStorage.setItem("user_role", res.data.role);
        if (res.data.role === "ADMIN") {
          setRoleState("admin");
        } else {
          setRoleState("forbidden");
        }
      } catch {
        setRoleState("forbidden");
      }
    };

    checkRole();
  }, []);

  if (roleState === "loading") {
    return (
      <div className="flex h-64 items-center justify-center">
        <Loader2 className="size-8 animate-spin text-primary" />
      </div>
    );
  }

  if (roleState === "forbidden") {
    return <Navigate to="/home" replace />;
  }

  return <>{children}</>;
};
