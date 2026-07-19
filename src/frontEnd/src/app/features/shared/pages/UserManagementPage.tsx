"use client";

import React, { useState, useMemo } from "react";
import {
  Users,
  UserPlus,
  ShieldCheck,
  UserCheck,
  Search,
  Trash2,
  RefreshCw,
  Mail,
  Lock,
  ShieldAlert,
  Loader2,
  Calendar,
  MoreVertical,
} from "lucide-react";
import { toast } from "sonner";

import { useUserManagement, UserItem } from "@/hooks/useUserManagement";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
  Table,
  TableHeader,
  TableBody,
  TableRow,
  TableHead,
  TableCell,
} from "@/components/ui/table";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";

export function UserManagementPage() {
  const { users, loading, error, fetchUsers, createUser, updateRole, deleteUser } =
    useUserManagement();

  // Filters & Search
  const [searchTerm, setSearchTerm] = useState("");
  const [roleFilter, setRoleFilter] = useState<string>("ALL");

  // Create Modal
  const [createOpen, setCreateOpen] = useState(false);
  const [createEmail, setCreateEmail] = useState("");
  const [createPassword, setCreatePassword] = useState("");
  const [createRole, setCreateRole] = useState<string>("USER");
  const [submitting, setSubmitting] = useState(false);

  // Delete Modal
  const [deleteTarget, setDeleteTarget] = useState<UserItem | null>(null);
  const [deleting, setDeleting] = useState(false);

  // Role Change state
  const [updatingId, setUpdatingId] = useState<string | null>(null);

  // Metrics
  const metrics = useMemo(() => {
    const total = users.length;
    const admins = users.filter((u) => u.role === "ADMIN").length;
    const standard = users.filter((u) => u.role === "USER").length;
    return { total, admins, standard };
  }, [users]);

  // Filtered Users
  const filteredUsers = useMemo(() => {
    return users.filter((u) => {
      const matchesSearch = u.email.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
      return matchesSearch && matchesRole;
    });
  }, [users, searchTerm, roleFilter]);

  // Helper for Initials
  const getInitials = (email: string) => {
    if (!email) return "U";
    const namePart = email.split("@")[0];
    const parts = namePart.split(/[._-]/);
    if (parts.length >= 2) {
      return (parts[0][0] + parts[1][0]).toUpperCase();
    }
    return namePart.substring(0, 2).toUpperCase();
  };

  // Create Submit
  const handleCreateSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!createEmail.trim() || !createPassword.trim()) {
      toast.error("Por favor ingresá todos los campos requeridos");
      return;
    }

    setSubmitting(true);
    try {
      await createUser({
        email: createEmail.trim(),
        password: createPassword.trim(),
        role: createRole,
      });
      toast.success("Usuario creado exitosamente");
      setCreateOpen(false);
      setCreateEmail("");
      setCreatePassword("");
      setCreateRole("USER");
    } catch (err: any) {
      const msg = err?.response?.data?.error || "Error al crear el usuario";
      toast.error(msg);
    } finally {
      setSubmitting(false);
    }
  };

  // Role Update
  const handleRoleChange = async (userId: string, newRole: string) => {
    setUpdatingId(userId);
    try {
      await updateRole(userId, newRole);
      toast.success(`Rol actualizado a ${newRole}`);
    } catch (err: any) {
      const msg = err?.response?.data?.error || "Error al actualizar rol";
      toast.error(msg);
    } finally {
      setUpdatingId(null);
    }
  };

  // Delete Confirm
  const handleDeleteConfirm = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await deleteUser(deleteTarget.id);
      toast.success("Usuario eliminado correctamente");
      setDeleteTarget(null);
    } catch (err: any) {
      const msg = err?.response?.data?.error || "Error al eliminar usuario";
      toast.error(msg);
    } finally {
      setDeleting(false);
    }
  };

  const formatDate = (dateStr: string | null) => {
    if (!dateStr) return "-";
    try {
      const d = new Date(dateStr);
      return d.toLocaleDateString("es-PE", {
        year: "numeric",
        month: "short",
        day: "numeric",
        hour: "2-digit",
        minute: "2-digit",
      });
    } catch {
      return dateStr;
    }
  };

  return (
    <div className="space-y-6 max-w-7xl mx-auto p-4 sm:p-6 lg:p-8">
      {/* Header section */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 border-b pb-5">
        <div>
          <h1 className="text-2xl font-bold tracking-tight text-foreground flex items-center gap-2">
            <Users className="size-6 text-primary" />
            Gestión de Usuarios y Roles
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Administrá el acceso a la plataforma, asigná roles y controlá las cuentas de usuario.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="sm"
            onClick={fetchUsers}
            disabled={loading}
            className="gap-2"
          >
            <RefreshCw className={`size-4 ${loading ? "animate-spin" : ""}`} />
            Actualizar
          </Button>
          <Button
            size="sm"
            onClick={() => setCreateOpen(true)}
            className="gap-2 bg-primary text-primary-foreground hover:bg-primary/90 shadow-sm"
          >
            <UserPlus className="size-4" />
            Nuevo Usuario
          </Button>
        </div>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <Card className="bg-card/50 backdrop-blur-sm border shadow-xs">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Total Usuarios
              </p>
              <h3 className="text-2xl font-bold tracking-tight mt-1">
                {loading ? <Skeleton className="h-8 w-12" /> : metrics.total}
              </h3>
            </div>
            <div className="size-10 rounded-full bg-primary/10 flex items-center justify-center text-primary">
              <Users className="size-5" />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-card/50 backdrop-blur-sm border shadow-xs">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Administradores
              </p>
              <h3 className="text-2xl font-bold tracking-tight mt-1 text-amber-600 dark:text-amber-400">
                {loading ? <Skeleton className="h-8 w-12" /> : metrics.admins}
              </h3>
            </div>
            <div className="size-10 rounded-full bg-amber-500/10 flex items-center justify-center text-amber-600 dark:text-amber-400">
              <ShieldCheck className="size-5" />
            </div>
          </CardContent>
        </Card>

        <Card className="bg-card/50 backdrop-blur-sm border shadow-xs">
          <CardContent className="p-4 flex items-center justify-between">
            <div>
              <p className="text-xs font-medium text-muted-foreground uppercase tracking-wider">
                Usuarios Estándar
              </p>
              <h3 className="text-2xl font-bold tracking-tight mt-1 text-blue-600 dark:text-blue-400">
                {loading ? <Skeleton className="h-8 w-12" /> : metrics.standard}
              </h3>
            </div>
            <div className="size-10 rounded-full bg-blue-500/10 flex items-center justify-center text-blue-600 dark:text-blue-400">
              <UserCheck className="size-5" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Filter & Action Bar */}
      <div className="flex flex-col sm:flex-row items-center justify-between gap-3 bg-card p-3 rounded-lg border shadow-2xs">
        <div className="relative w-full sm:w-80">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
          <Input
            placeholder="Buscar por email..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-9 bg-background h-9 text-sm"
          />
        </div>

        <div className="flex items-center gap-2 w-full sm:w-auto">
          <span className="text-xs text-muted-foreground whitespace-nowrap hidden sm:inline">
            Filtrar por rol:
          </span>
          <Select value={roleFilter} onValueChange={setRoleFilter}>
            <SelectTrigger className="h-9 w-full sm:w-40 bg-background text-sm">
              <SelectValue placeholder="Todos los roles" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="ALL">Todos los roles</SelectItem>
              <SelectItem value="ADMIN">ADMIN</SelectItem>
              <SelectItem value="USER">USER</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      {/* Main Table */}
      <div className="rounded-lg border bg-card shadow-xs overflow-hidden">
        {loading ? (
          <div className="p-6 space-y-4">
            {Array.from({ length: 4 }).map((_, i) => (
              <div key={i} className="flex items-center justify-between gap-4">
                <div className="flex items-center gap-3">
                  <Skeleton className="size-10 rounded-full" />
                  <div className="space-y-1">
                    <Skeleton className="h-4 w-48" />
                    <Skeleton className="h-3 w-24" />
                  </div>
                </div>
                <Skeleton className="h-8 w-24" />
              </div>
            ))}
          </div>
        ) : error ? (
          <div className="p-8 text-center space-y-3">
            <ShieldAlert className="size-10 text-destructive mx-auto" />
            <p className="text-sm font-medium text-destructive">{error}</p>
            <Button variant="outline" size="sm" onClick={fetchUsers}>
              Reintentar
            </Button>
          </div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-12 text-center space-y-3">
            <Users className="size-10 text-muted-foreground mx-auto opacity-40" />
            <p className="text-base font-medium text-muted-foreground">
              No se encontraron usuarios
            </p>
            <p className="text-xs text-muted-foreground max-w-sm mx-auto">
              Prueba cambiando la búsqueda o agregá un nuevo usuario para comenzar.
            </p>
          </div>
        ) : (
          <Table>
            <TableHeader className="bg-muted/50">
              <TableRow>
                <TableHead className="w-[350px]">Usuario</TableHead>
                <TableHead className="w-[180px]">Rol de Sistema</TableHead>
                <TableHead>Fecha de Registro</TableHead>
                <TableHead className="text-right w-[100px]">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map((user) => {
                const isAdmin = user.role === "ADMIN";
                const isUpdating = updatingId === user.id;

                return (
                  <TableRow key={user.id} className="hover:bg-muted/30 transition-colors">
                    {/* User info */}
                    <TableCell>
                      <div className="flex items-center gap-3">
                        <Avatar className="size-9 border border-border">
                          <AvatarFallback
                            className={
                              isAdmin
                                ? "bg-amber-500/15 text-amber-700 dark:text-amber-300 font-bold text-xs"
                                : "bg-primary/10 text-primary font-bold text-xs"
                            }
                          >
                            {getInitials(user.email)}
                          </AvatarFallback>
                        </Avatar>
                        <div className="flex flex-col">
                          <span className="font-medium text-sm text-foreground flex items-center gap-1.5">
                            {user.email}
                          </span>
                          <span className="text-xs text-muted-foreground font-mono truncate max-w-48">
                            ID: {user.id}
                          </span>
                        </div>
                      </div>
                    </TableCell>

                    {/* Role selector */}
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Select
                          value={user.role}
                          onValueChange={(val) => handleRoleChange(user.id, val)}
                          disabled={isUpdating}
                        >
                          <SelectTrigger
                            className={`h-8 text-xs font-medium w-32 border ${
                              isAdmin
                                ? "bg-amber-500/10 text-amber-700 dark:text-amber-400 border-amber-500/30"
                                : "bg-blue-500/10 text-blue-700 dark:text-blue-400 border-blue-500/30"
                            }`}
                          >
                            <div className="flex items-center gap-1.5">
                              {isAdmin ? (
                                <ShieldCheck className="size-3.5" />
                              ) : (
                                <UserCheck className="size-3.5" />
                              )}
                              <SelectValue />
                            </div>
                          </SelectTrigger>
                          <SelectContent>
                            <SelectItem value="USER">USER</SelectItem>
                            <SelectItem value="ADMIN">ADMIN</SelectItem>
                          </SelectContent>
                        </Select>
                        {isUpdating && <Loader2 className="size-3.5 animate-spin text-muted-foreground" />}
                      </div>
                    </TableCell>

                    {/* Registration Date */}
                    <TableCell className="text-xs text-muted-foreground">
                      <div className="flex items-center gap-1.5">
                        <Calendar className="size-3.5 text-muted-foreground/70" />
                        {formatDate(user.createdAt)}
                      </div>
                    </TableCell>

                    {/* Actions */}
                    <TableCell className="text-right">
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="size-8">
                            <MoreVertical className="size-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem
                            variant="destructive"
                            onClick={() => setDeleteTarget(user)}
                            className="cursor-pointer gap-2"
                          >
                            <Trash2 className="size-4" />
                            Eliminar Usuario
                          </DropdownMenuItem>
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </div>

      {/* Modal: Crear Usuario */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2">
              <UserPlus className="size-5 text-primary" />
              Crear Nuevo Usuario
            </DialogTitle>
            <DialogDescription>
              Ingresá los datos del nuevo usuario para registrarlo en el sistema.
            </DialogDescription>
          </DialogHeader>

          <form onSubmit={handleCreateSubmit} className="space-y-4 py-2">
            <div className="space-y-2">
              <Label htmlFor="email" className="text-xs font-semibold">
                Correo Electrónico
              </Label>
              <div className="relative">
                <Mail className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
                <Input
                  id="email"
                  type="email"
                  placeholder="usuario@empresa.com"
                  value={createEmail}
                  onChange={(e) => setCreateEmail(e.target.value)}
                  className="pl-9"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="password" className="text-xs font-semibold">
                Contraseña Initial
              </Label>
              <div className="relative">
                <Lock className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" />
                <Input
                  id="password"
                  type="password"
                  placeholder="••••••••"
                  value={createPassword}
                  onChange={(e) => setCreatePassword(e.target.value)}
                  className="pl-9"
                  required
                />
              </div>
            </div>

            <div className="space-y-2">
              <Label htmlFor="role" className="text-xs font-semibold">
                Rol Inicial
              </Label>
              <Select value={createRole} onValueChange={setCreateRole}>
                <SelectTrigger id="role" className="w-full">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="USER">Usuario Estándar (USER)</SelectItem>
                  <SelectItem value="ADMIN">Administrador (ADMIN)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <DialogFooter className="pt-4">
              <Button
                type="button"
                variant="outline"
                onClick={() => setCreateOpen(false)}
                disabled={submitting}
              >
                Cancelar
              </Button>
              <Button type="submit" disabled={submitting} className="gap-2">
                {submitting && <Loader2 className="size-4 animate-spin" />}
                Guardar Usuario
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Modal: Confirmar Eliminación */}
      <Dialog open={!!deleteTarget} onOpenChange={(open) => !open && setDeleteTarget(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="flex items-center gap-2 text-destructive">
              <ShieldAlert className="size-5 text-destructive" />
              Confirmar Eliminación
            </DialogTitle>
            <DialogDescription>
              ¿Estás seguro de que deseas eliminar la cuenta de{" "}
              <strong className="text-foreground">{deleteTarget?.email}</strong>? Esta acción no se
              puede deshacer.
            </DialogDescription>
          </DialogHeader>

          <DialogFooter className="pt-2">
            <Button variant="outline" onClick={() => setDeleteTarget(null)} disabled={deleting}>
              Cancelar
            </Button>
            <Button
              variant="destructive"
              onClick={handleDeleteConfirm}
              disabled={deleting}
              className="gap-2"
            >
              {deleting && <Loader2 className="size-4 animate-spin" />}
              Eliminar Definitivamente
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
