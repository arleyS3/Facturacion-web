import { useState, useCallback, useEffect } from "react";
import api from "@/lib/api";

export interface UserItem {
  id: string;
  email: string;
  role: "ADMIN" | "USER" | string;
  createdAt: string | null;
}

export interface CreateUserData {
  email: string;
  password: string;
  role: string;
}

export function useUserManagement() {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [error, setError] = useState<string | null>(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<UserItem[]>("/auth/users");
      setUsers(res.data);
    } catch (err: any) {
      setError(err?.response?.data?.error || "Error al cargar la lista de usuarios");
    } finally {
      setLoading(false);
    }
  }, []);

  const createUser = async (data: CreateUserData) => {
    const res = await api.post<UserItem>("/auth/users", data);
    await fetchUsers();
    return res.data;
  };

  const updateRole = async (userId: string, newRole: string) => {
    const res = await api.patch<{ message: string }>(`/auth/users/${userId}/role`, {
      role: newRole,
    });
    setUsers((prev) =>
      prev.map((u) => (u.id === userId ? { ...u, role: newRole } : u))
    );
    return res.data;
  };

  const deleteUser = async (userId: string) => {
    const res = await api.delete<{ message: string }>(`/auth/users/${userId}`);
    setUsers((prev) => prev.filter((u) => u.id !== userId));
    return res.data;
  };

  useEffect(() => {
    fetchUsers();
  }, [fetchUsers]);

  return {
    users,
    loading,
    error,
    fetchUsers,
    createUser,
    updateRole,
    deleteUser,
  };
}
