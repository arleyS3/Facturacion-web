"use client";

import React, { useState, useEffect } from "react";
import {
  Trash2,
  Shield,
  User as UserIcon,
  Loader2,
  Search,
  UserPlus,
  AlertTriangle,
  Pencil,
  Eye,
  EyeOff,
  KeyRound,
  Check,
} from "lucide-react";
import { api } from "@/lib/api";
import { toast } from "sonner";

import {
  Table,
  TableHeader,
  TableBody,
  TableHead,
  TableRow,
  TableCell,
} from "@/components/ui/table";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface UserItem {
  id: string;
  email: string;
  role: "ADMIN" | "USER";
  createdAt: string;
}

export function UserManagementPage() {
  const [users, setUsers] = useState<UserItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState<string>("ALL");

  // State para dialog crear usuario
  const [createOpen, setCreateOpen] = useState(false);
  const [newEmail, setNewEmail] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [newRole, setNewRole] = useState<"ADMIN" | "USER">("USER");
  const [saving, setSaving] = useState(false);

  // State para dialog editar usuario
  const [editOpen, setEditOpen] = useState(false);
  const [editTarget, setEditTarget] = useState<UserItem | null>(null);
  const [editEmail, setEditEmail] = useState("");
  const [editOldPassword, setEditOldPassword] = useState("");
  const [editNewPassword, setEditNewPassword] = useState("");
  const [editRole, setEditRole] = useState<"ADMIN" | "USER">("USER");
  const [showOldPassword, setShowOldPassword] = useState(false);
  const [showNewPassword, setShowNewPassword] = useState(false);
  const [updating, setUpdating] = useState(false);

  // State para eliminar usuario (individual)
  const [deleteTarget, setDeleteTarget] = useState<UserItem | null>(null);
  const [deleting, setDeleting] = useState(false);

  // State para eliminar masivo
  const [selectedIds, setSelectedIds] = useState<Set<string>>(new Set());
  const [bulkDeleteOpen, setBulkDeleteOpen] = useState(false);
  const [bulkDeleting, setBulkDeleting] = useState(false);

  const fetchUsers = async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await api.get<UserItem[]>("/auth/users");
      setUsers(res.data);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { message?: string; error?: string } } };
      const msg = errorObj?.response?.data?.message || errorObj?.response?.data?.error || "Error al cargar la lista de usuarios.";
      setError(msg);
      toast.error(msg);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  const handleRoleChange = async (userId: string, targetRole: "ADMIN" | "USER") => {
    try {
      await api.patch(`/auth/users/${userId}/role`, { role: targetRole });
      setUsers((prev) =>
        prev.map((u) => (u.id === userId ? { ...u, role: targetRole } : u))
      );
      toast.success(`Rol actualizado a ${targetRole}`);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { error?: string } } };
      toast.error(errorObj?.response?.data?.error || "No se pudo actualizar el rol.");
    }
  };

  const handleCreateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newEmail.trim() || !newPassword.trim()) {
      toast.error("Por favor completa todos los campos requeridos.");
      return;
    }

    setSaving(true);
    try {
      const res = await api.post<UserItem>("/auth/users", {
        email: newEmail.trim(),
        password: newPassword,
        role: newRole,
      });
      setUsers((prev) => [res.data, ...prev]);
      setCreateOpen(false);
      setNewEmail("");
      setNewPassword("");
      setNewRole("USER");
      toast.success("Usuario creado exitosamente");
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { error?: string; message?: string } } };
      const msg = errorObj?.response?.data?.error || errorObj?.response?.data?.message || "Error al crear el usuario.";
      toast.error(msg);
    } finally {
      setSaving(false);
    }
  };

  const abrirModalEditar = (user: UserItem) => {
    setEditTarget(user);
    setEditEmail(user.email);
    setEditOldPassword("");
    setEditNewPassword("");
    setEditRole(user.role);
    setShowOldPassword(false);
    setShowNewPassword(false);
    setEditOpen(true);
  };

  const handleUpdateUser = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editTarget) return;

    if (!editEmail.trim()) {
      toast.error("El correo electrónico es requerido");
      return;
    }

    if (editNewPassword && !editOldPassword) {
      toast.error("Debe ingresar la contraseña actual para establecer una nueva contraseña");
      return;
    }

    setUpdating(true);
    try {
      const payload: Record<string, string> = {
        email: editEmail.trim(),
        role: editRole,
      };
      if (editOldPassword) payload.oldPassword = editOldPassword;
      if (editNewPassword) payload.newPassword = editNewPassword;

      const res = await api.put<UserItem>(`/auth/users/${editTarget.id}`, payload);
      setUsers((prev) =>
        prev.map((u) => (u.id === editTarget.id ? res.data : u))
      );
      setEditOpen(false);
      toast.success("Datos de usuario actualizados correctamente");
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { error?: string; message?: string } } };
      const msg = errorObj?.response?.data?.error || errorObj?.response?.data?.message || "Error al actualizar usuario";
      toast.error(msg);
    } finally {
      setUpdating(false);
    }
  };

  const handleDeleteUser = async () => {
    if (!deleteTarget) return;
    setDeleting(true);
    try {
      await api.delete(`/auth/users/${deleteTarget.id}`);
      setUsers((prev) => prev.filter((u) => u.id !== deleteTarget.id));
      setDeleteTarget(null);
      toast.success("Usuario eliminado correctamente");
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { error?: string } } };
      toast.error(errorObj?.response?.data?.error || "No se pudo eliminar el usuario.");
    } finally {
      setDeleting(false);
    }
  };

  const toggleSelectAll = () => {
    if (selectedIds.size === filteredUsers.length) {
      setSelectedIds(new Set());
    } else {
      setSelectedIds(new Set(filteredUsers.map((u) => u.id)));
    }
  };

  const toggleSelect = (id: string) => {
    setSelectedIds((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  };

  const handleBulkDelete = async () => {
    setBulkDeleting(true);
    try {
      await api.post("/auth/users/bulk-delete", Array.from(selectedIds));
      setUsers((prev) => prev.filter((u) => !selectedIds.has(u.id)));
      setSelectedIds(new Set());
      setBulkDeleteOpen(false);
      toast.success(`${selectedIds.size} usuarios eliminados correctamente`);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { error?: string } } };
      toast.error(errorObj?.response?.data?.error || "No se pudieron eliminar los usuarios seleccionados.");
    } finally {
      setBulkDeleting(false);
    }
  };

  const filteredUsers = users.filter((u) => {
    const matchesSearch = u.email.toLowerCase().includes(searchQuery.toLowerCase());
    const matchesRole = roleFilter === "ALL" || u.role === roleFilter;
    return matchesSearch && matchesRole;
  });

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      {/* Encabezado */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl sm:text-3xl font-bold tracking-tight text-foreground flex items-center gap-2 text-balance">
            <Shield className="size-7 text-primary" aria-hidden="true" />
            Gestión de Usuarios y Roles
          </h1>
          <p className="text-sm text-muted-foreground mt-1">
            Administrá el acceso a la plataforma, modificá credenciales y controlá los roles de usuario.
          </p>
        </div>
        <Button onClick={() => setCreateOpen(true)} className="gap-2 cursor-pointer shrink-0 focus-visible:ring-2">
          <UserPlus className="size-4" aria-hidden="true" />
          Nuevo Usuario
        </Button>
      </div>

      {/* Filtros */}
      <div className="flex flex-col sm:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground" aria-hidden="true" />
          <Input
            placeholder="Buscar por correo electrónico…"
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="pl-9 h-10"
            autoComplete="off"
            spellCheck={false}
          />
        </div>
        <Select value={roleFilter} onValueChange={setRoleFilter}>
          <SelectTrigger className="w-full sm:w-48 h-10">
            <SelectValue placeholder="Todos los roles" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ALL">Todos los roles</SelectItem>
            <SelectItem value="ADMIN">Solo Admins</SelectItem>
            <SelectItem value="USER">Solo Usuarios</SelectItem>
          </SelectContent>
        </Select>
      </div>

      {/* Barra de acciones masivas */}
      {selectedIds.size > 0 && (
        <div className="flex items-center justify-between bg-primary/5 border border-primary/20 rounded-xl px-5 py-3">
          <span className="text-sm font-medium text-foreground">
            {selectedIds.size} usuario{selectedIds.size !== 1 ? "s" : ""} seleccionado{selectedIds.size !== 1 ? "s" : ""}
          </span>
          <div className="flex gap-2">
            <Button variant="outline" size="sm" onClick={() => setSelectedIds(new Set())} className="cursor-pointer">
              Cancelar
            </Button>
            <Button
              variant="destructive"
              size="sm"
              className="gap-2 cursor-pointer"
              onClick={() => setBulkDeleteOpen(true)}
            >
              <Trash2 className="size-4" aria-hidden="true" />
              Eliminar seleccionados
            </Button>
          </div>
        </div>
      )}

      {/* Tabla */}
      <div className="border border-border rounded-xl bg-card overflow-hidden shadow-sm">
        {loading ? (
          <div className="p-6 space-y-4" aria-live="polite">
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
            <Skeleton className="h-10 w-full" />
          </div>
        ) : error ? (
          <div className="p-8 text-center text-destructive">{error}</div>
        ) : filteredUsers.length === 0 ? (
          <div className="p-12 text-center text-muted-foreground">
            No se encontraron usuarios que coincidan con la búsqueda.
          </div>
        ) : (
          <Table>
            <TableHeader className="bg-muted/50">
              <TableRow>
                <TableHead className="w-12">
                  <Checkbox
                    checked={filteredUsers.length > 0 && selectedIds.size === filteredUsers.length}
                    onCheckedChange={toggleSelectAll}
                    aria-label="Seleccionar todos los usuarios"
                  />
                </TableHead>
                <TableHead>Usuario / Email</TableHead>
                <TableHead>Rol Asignado</TableHead>
                <TableHead>Fecha de Registro</TableHead>
                <TableHead className="text-right">Acciones</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {filteredUsers.map((user) => (
                <TableRow
                  key={user.id}
                  className={selectedIds.has(user.id) ? "bg-primary/5 hover:bg-primary/10" : "hover:bg-muted/50"}
                >
                  <TableCell>
                    <Checkbox
                      checked={selectedIds.has(user.id)}
                      onCheckedChange={() => toggleSelect(user.id)}
                      aria-label={`Seleccionar usuario ${user.email}`}
                    />
                  </TableCell>
                  <TableCell className="font-medium">
                    <div className="flex items-center gap-3">
                      <div className="size-9 rounded-full bg-primary/10 flex items-center justify-center text-primary font-semibold text-xs shrink-0">
                        {user.email.slice(0, 2).toUpperCase()}
                      </div>
                      <div>
                        <div className="text-sm font-semibold text-foreground">{user.email}</div>
                        <div className="text-xs text-muted-foreground font-mono tabular-nums">ID: {user.id.slice(0, 8)}…</div>
                      </div>
                    </div>
                  </TableCell>
                  <TableCell>
                    <Select
                      value={user.role}
                      onValueChange={(val) => handleRoleChange(user.id, val as "ADMIN" | "USER")}
                    >
                      <SelectTrigger className="w-36 h-8 text-xs font-semibold" aria-label={`Rol de ${user.email}`}>
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="ADMIN">
                          <span className="flex items-center gap-1.5 text-primary">
                            <Shield className="size-3.5" aria-hidden="true" /> Administrador
                          </span>
                        </SelectItem>
                        <SelectItem value="USER">
                          <span className="flex items-center gap-1.5 text-muted-foreground">
                            <UserIcon className="size-3.5" aria-hidden="true" /> Usuario Estándar
                          </span>
                        </SelectItem>
                      </SelectContent>
                    </Select>
                  </TableCell>
                  <TableCell className="text-sm text-muted-foreground font-mono tabular-nums">
                    {user.createdAt ? new Date(user.createdAt).toLocaleDateString() : "—"}
                  </TableCell>
                  <TableCell className="text-right">
                    <div className="flex items-center justify-end gap-1">
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-muted-foreground hover:text-foreground hover:bg-muted cursor-pointer"
                        onClick={() => abrirModalEditar(user)}
                        title="Editar datos de usuario"
                        aria-label={`Editar usuario ${user.email}`}
                      >
                        <Pencil className="size-4" aria-hidden="true" />
                      </Button>
                      <Button
                        variant="ghost"
                        size="icon"
                        className="h-8 w-8 text-destructive hover:bg-destructive/10 cursor-pointer"
                        onClick={() => setDeleteTarget(user)}
                        title="Eliminar cuenta"
                        aria-label={`Eliminar cuenta de ${user.email}`}
                      >
                        <Trash2 className="size-4" aria-hidden="true" />
                      </Button>
                    </div>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </div>

      {/* Modal Crear Usuario */}
      <Dialog open={createOpen} onOpenChange={setCreateOpen}>
        <DialogContent className="sm:max-w-md">
          <form onSubmit={handleCreateUser}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2">
                <UserPlus className="size-5 text-primary" aria-hidden="true" />
                Registrar Nuevo Usuario
              </DialogTitle>
              <DialogDescription className="text-xs">
                Crea una cuenta para dar acceso a la plataforma de facturación.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              <div className="space-y-1.5">
                <Label htmlFor="create-email" className="text-xs font-semibold">Correo Electrónico <span className="text-destructive">*</span></Label>
                <Input
                  id="create-email"
                  type="email"
                  placeholder="usuario@empresa.com…"
                  autoComplete="off"
                  spellCheck={false}
                  value={newEmail}
                  onChange={(e) => setNewEmail(e.target.value)}
                  className="h-10"
                  required
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="create-password" className="text-xs font-semibold">Contraseña <span className="text-destructive">*</span></Label>
                <Input
                  id="create-password"
                  type="password"
                  placeholder="••••••••"
                  autoComplete="off"
                  value={newPassword}
                  onChange={(e) => setNewPassword(e.target.value)}
                  className="h-10"
                  required
                />
              </div>

              <div className="space-y-1.5">
                <Label htmlFor="create-role" className="text-xs font-semibold">Rol de Usuario</Label>
                <Select value={newRole} onValueChange={(v) => setNewRole(v as "ADMIN" | "USER")}>
                  <SelectTrigger id="create-role" className="h-10">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">Usuario Estándar</SelectItem>
                    <SelectItem value="ADMIN">Administrador</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <DialogFooter className="gap-2">
              <DialogClose asChild>
                <Button type="button" variant="outline" className="cursor-pointer">
                  Cancelar
                </Button>
              </DialogClose>
              <Button type="submit" disabled={saving} className="cursor-pointer bg-primary hover:bg-primary/90">
                {saving ? <Loader2 className="size-4 animate-spin mr-2" aria-hidden="true" /> : null}
                Guardar Usuario
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Modal Editar Usuario (Correo, Contraseña Antigua, Nueva Contraseña, Rol) */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent className="sm:max-w-md">
          <form onSubmit={handleUpdateUser}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2 text-lg">
                <Pencil className="size-5 text-primary" aria-hidden="true" />
                Editar Datos de Usuario
              </DialogTitle>
              <DialogDescription className="text-xs">
                Modifique la cuenta de {editTarget?.email}. Para cambiar la contraseña, debe validar la contraseña actual guardada en la base de datos.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {/* Correo Electrónico */}
              <div className="space-y-1.5">
                <Label htmlFor="edit-email" className="text-xs font-semibold">
                  Correo Electrónico <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="edit-email"
                  type="email"
                  placeholder="usuario@empresa.com…"
                  autoComplete="off"
                  spellCheck={false}
                  value={editEmail}
                  onChange={(e) => setEditEmail(e.target.value)}
                  className="h-10"
                  required
                />
              </div>

              {/* Contraseña Antigua */}
              <div className="space-y-1.5">
                <Label htmlFor="edit-old-password" className="text-xs font-semibold flex items-center justify-between">
                  <span>Contraseña Actual (de la BD)</span>
                  <span className="text-muted-foreground font-normal">(Requerida para cambiar clave)</span>
                </Label>
                <div className="relative">
                  <Input
                    id="edit-old-password"
                    type={showOldPassword ? "text" : "password"}
                    placeholder="Contraseña actual guardada en BD…"
                    autoComplete="off"
                    value={editOldPassword}
                    onChange={(e) => setEditOldPassword(e.target.value)}
                    className="pr-10 h-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowOldPassword((prev) => !prev)}
                    aria-label={showOldPassword ? "Ocultar contraseña actual" : "Mostrar contraseña actual"}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground p-1 cursor-pointer"
                  >
                    {showOldPassword ? <EyeOff className="size-4" aria-hidden="true" /> : <Eye className="size-4" aria-hidden="true" />}
                  </button>
                </div>
              </div>

              {/* Nueva Contraseña */}
              <div className="space-y-1.5">
                <Label htmlFor="edit-new-password" className="text-xs font-semibold">
                  Nueva Contraseña <span className="text-muted-foreground font-normal">(Opcional)</span>
                </Label>
                <div className="relative">
                  <Input
                    id="edit-new-password"
                    type={showNewPassword ? "text" : "password"}
                    placeholder="Nueva contraseña…"
                    autoComplete="off"
                    value={editNewPassword}
                    onChange={(e) => setEditNewPassword(e.target.value)}
                    className="pr-10 h-10"
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword((prev) => !prev)}
                    aria-label={showNewPassword ? "Ocultar nueva contraseña" : "Mostrar nueva contraseña"}
                    className="absolute right-3 top-1/2 -translate-y-1/2 text-muted-foreground hover:text-foreground p-1 cursor-pointer"
                  >
                    {showNewPassword ? <EyeOff className="size-4" aria-hidden="true" /> : <Eye className="size-4" aria-hidden="true" />}
                  </button>
                </div>
              </div>

              {/* Rol de usuario */}
              <div className="space-y-1.5">
                <Label htmlFor="edit-role" className="text-xs font-semibold">Rol de Usuario</Label>
                <Select value={editRole} onValueChange={(v) => setEditRole(v as "ADMIN" | "USER")}>
                  <SelectTrigger id="edit-role" className="h-10">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">Usuario Estándar</SelectItem>
                    <SelectItem value="ADMIN">Administrador</SelectItem>
                  </SelectContent>
                </Select>
              </div>
            </div>

            <DialogFooter className="gap-2">
              <Button type="button" variant="outline" onClick={() => setEditOpen(false)} disabled={updating} className="cursor-pointer">
                Cancelar
              </Button>
              <Button type="submit" disabled={updating} className="cursor-pointer bg-primary hover:bg-primary/90">
                {updating ? (
                  <>
                    <Loader2 className="size-4 animate-spin mr-2" aria-hidden="true" />
                    Actualizando…
                  </>
                ) : (
                  <>
                    <Check className="size-4 mr-2" aria-hidden="true" />
                    Guardar Cambios
                  </>
                )}
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

      {/* Modal Confirmar Eliminar (individual) */}
      <Dialog open={!!deleteTarget} onOpenChange={(open) => !open && setDeleteTarget(null)}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-destructive flex items-center gap-2 text-lg">
              <Trash2 className="size-5" aria-hidden="true" />
              ¿Eliminar usuario?
            </DialogTitle>
            <DialogDescription className="text-xs">
              ¿Estás seguro de que deseas eliminar la cuenta de <strong>{deleteTarget?.email}</strong>? Esta acción no se puede deshacer.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setDeleteTarget(null)} className="cursor-pointer">
              Cancelar
            </Button>
            <Button variant="destructive" onClick={handleDeleteUser} disabled={deleting} className="cursor-pointer">
              {deleting ? <Loader2 className="size-4 animate-spin mr-2" aria-hidden="true" /> : null}
              Eliminar
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Modal Confirmar Eliminar Masivo */}
      <Dialog open={bulkDeleteOpen} onOpenChange={setBulkDeleteOpen}>
        <DialogContent className="sm:max-w-md">
          <DialogHeader>
            <DialogTitle className="text-destructive flex items-center gap-2 text-lg">
              <AlertTriangle className="size-5" aria-hidden="true" />
              ¿Eliminar {selectedIds.size} usuarios?
            </DialogTitle>
            <DialogDescription className="text-xs">
              Vas a eliminar <strong>{selectedIds.size} cuentas de usuario</strong> de forma permanente.
              Esta acción no se puede deshacer.
            </DialogDescription>
          </DialogHeader>

          <div className="max-h-48 overflow-y-auto border rounded-lg p-3 space-y-1 text-sm">
            {users
              .filter((u) => selectedIds.has(u.id))
              .map((u) => (
                <div key={u.id} className="flex items-center gap-2 text-muted-foreground">
                  <UserIcon className="size-3.5 shrink-0" aria-hidden="true" />
                  <span className="font-medium text-foreground">{u.email}</span>
                  <Badge variant="outline" className="ml-auto text-xs">
                    {u.role === "ADMIN" ? "Admin" : "User"}
                  </Badge>
                </div>
              ))}
          </div>

          <DialogFooter className="gap-2">
            <Button variant="outline" onClick={() => setBulkDeleteOpen(false)} className="cursor-pointer">
              Cancelar
            </Button>
            <Button variant="destructive" onClick={handleBulkDelete} disabled={bulkDeleting} className="cursor-pointer">
              {bulkDeleting ? <Loader2 className="size-4 animate-spin mr-2" aria-hidden="true" /> : null}
              Eliminar {selectedIds.size} usuarios
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}

export default UserManagementPage;
