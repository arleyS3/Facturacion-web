"use client";

import React from "react";
import { useLocation, Outlet, useNavigate } from "react-router";
import { motion, AnimatePresence } from "motion/react";
import {
  LogOut,
  LayoutDashboard,
  FileText,
  Truck,
  Shield,
  ChevronRight,
  Upload,
  Settings,
  Users,
  User as UserIcon,
  Eye,
  EyeOff,
  Loader2,
  Check,
} from "lucide-react";
import { api, setAccessToken } from "@/lib/api";
import { toast } from "sonner";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";

import logoTransparente from "@/assets/logo-transparente.png";

/** Gets initials from an email like "arley@gmail.com" → "AR" */
function getInitials(email: string): string {
  return email.split("@")[0].slice(0, 2).toUpperCase();
}

const navItems = [
  { label: "Inicio", path: "/home", icon: LayoutDashboard },
  { label: "Documentos de Venta", path: "/sales-documents", icon: FileText },
  { label: "Guías de Remisión", path: "/shipping-guide", icon: Truck },
];

/** Logo component */
function BrandLogo({ className = "" }: { className?: string }) {
  return (
    <div className={`size-9 rounded-lg flex items-center justify-center ${className}`}>
      <img src={logoTransparente} alt="Logo" className="size-8" />
    </div>
  );
}

/** Navigation handler that shows loader for 2 seconds then navigates */
function handleNavigation(path: string, navigate: (path: string) => void) {
  sessionStorage.setItem("navigating", "true");
  navigate(path);
}

/** Loading overlay with Motion animation */
function NavigationLoader() {
  const [showLoader, setShowLoader] = React.useState(false);

  React.useEffect(() => {
    const isNavigating = sessionStorage.getItem("navigating");

    if (isNavigating) {
      setShowLoader(true);

      const timer = setTimeout(() => {
        sessionStorage.removeItem("navigating");
        setShowLoader(false);
      }, 2000);

      return () => clearTimeout(timer);
    }
  }, []);

  return (
    <AnimatePresence>
      {showLoader && (
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          exit={{ opacity: 0 }}
          transition={{ duration: 0.2 }}
          className="fixed inset-0 z-[100] bg-background/95 backdrop-blur-md flex flex-col items-center justify-center"
        >
          <motion.div
            initial={{ scale: 0.8, opacity: 0 }}
            animate={{ scale: 1, opacity: 1 }}
            transition={{ type: "spring", stiffness: 300, damping: 20 }}
            className="flex flex-col items-center gap-6"
          >
            <div className="relative">
              <div className="absolute inset-0 rounded-3xl bg-gradient-to-br from-primary/30 via-primary/10 to-transparent blur-2xl scale-150" />
              <motion.div
                animate={{
                  scale: [1, 1.02, 1],
                  rotate: [0, 1, 0, -1, 0],
                }}
                transition={{
                  scale: { duration: 3, repeat: Infinity, ease: "easeInOut" },
                  rotate: { duration: 8, repeat: Infinity, ease: "linear" }
                }}
                className="relative flex items-center justify-center"
              >
                <img
                  src={logoTransparente}
                  alt="Logo"
                  className="w-28 h-28 object-contain drop-shadow-lg"
                />
              </motion.div>
            </div>

            <div className="flex flex-col items-center gap-3">
              <span className="text-xl font-semibold text-foreground">Cargando…</span>
              <div className="flex items-center gap-1.5">
                {[0, 1, 2].map((i) => (
                  <motion.span
                    key={i}
                    animate={{ y: [-4, 0, -4] }}
                    transition={{
                      duration: 0.6,
                      repeat: Infinity,
                      delay: i * 0.15
                    }}
                    className="w-2.5 h-2.5 rounded-full bg-primary"
                  />
                ))}
              </div>
            </div>
          </motion.div>
        </motion.div>
      )}
    </AnimatePresence>
  );
}

/** Nav button component */
function NavButton({
  item,
  isActive,
  className = ""
}: {
  item: typeof navItems[0];
  isActive: boolean;
  className?: string;
}) {
  const navigate = useNavigate();

  return (
    <button
      onClick={() => handleNavigation(item.path, navigate)}
      className={`flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors cursor-pointer focus-visible:ring-2 ${className} ${
        isActive
          ? "bg-primary-soft text-primary"
          : "text-muted-foreground hover:text-foreground hover:bg-muted"
      }`}
      aria-current={isActive ? "page" : undefined}
    >
      <item.icon className="size-4" strokeWidth={1.5} aria-hidden="true" />
      {item.label}
    </button>
  );
}

/** Mobile nav button */
function MobileNavButton({
  item,
  isActive,
  className = ""
}: {
  item: typeof navItems[0];
  isActive: boolean;
  className?: string;
}) {
  const navigate = useNavigate();

  return (
    <button
      onClick={() => handleNavigation(item.path, navigate)}
      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium whitespace-nowrap transition-colors cursor-pointer shrink-0 ${className} ${
        isActive
          ? "bg-primary text-primary-foreground"
          : "text-muted-foreground bg-muted hover:bg-border/40"
      }`}
    >
      <item.icon className="size-3.5" strokeWidth={1.5} aria-hidden="true" />
      {item.label}
    </button>
  );
}

export function AppLayout() {
  const location = useLocation();
  const navigate = useNavigate();
  const [userEmail, setUserEmail] = React.useState(
    () => localStorage.getItem("user_email") ?? "Usuario"
  );
  const [userRole, setUserRole] = React.useState<string | null>(
    () => localStorage.getItem("user_role")
  );

  // Modal para editar datos personales (Mi Perfil)
  const [profileDialogOpen, setProfileDialogOpen] = React.useState(false);
  const [profileEmail, setProfileEmail] = React.useState("");
  const [profileOldPassword, setProfileOldPassword] = React.useState("");
  const [profileNewPassword, setProfileNewPassword] = React.useState("");
  const [showOldPassword, setShowOldPassword] = React.useState(false);
  const [showNewPassword, setShowNewPassword] = React.useState(false);
  const [savingProfile, setSavingProfile] = React.useState(false);

  // Sync role from /auth/me on mount
  React.useEffect(() => {
    api
      .get<{ email: string; role: string }>("/auth/me")
      .then((res) => {
        if (res.data?.email) {
          setUserEmail(res.data.email);
          localStorage.setItem("user_email", res.data.email);
        }
        const role = res.data?.role || "USER";
        setUserRole(role);
        localStorage.setItem("user_role", role);
      })
      .catch(() => {});
  }, []);

  const abrirModalPerfil = () => {
    setProfileEmail(userEmail);
    setProfileOldPassword("");
    setProfileNewPassword("");
    setShowOldPassword(false);
    setShowNewPassword(false);
    setProfileDialogOpen(true);
  };

  const handleUpdateProfile = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!profileEmail.trim()) {
      toast.error("El correo electrónico es requerido");
      return;
    }
    if (profileNewPassword && !profileOldPassword) {
      toast.error("Ingrese su contraseña actual para establecer una nueva contraseña");
      return;
    }

    setSavingProfile(true);
    try {
      const payload: Record<string, string> = { email: profileEmail.trim() };
      if (profileOldPassword) payload.oldPassword = profileOldPassword;
      if (profileNewPassword) payload.newPassword = profileNewPassword;

      const res = await api.put<{ email: string }>("/auth/profile", payload);

      if (res.data?.email) {
        setUserEmail(res.data.email);
        localStorage.setItem("user_email", res.data.email);
      }
      toast.success("Perfil actualizado correctamente");
      setProfileDialogOpen(false);
    } catch (err: unknown) {
      const errorObj = err as { response?: { data?: { error?: string; message?: string } } };
      const msg = errorObj.response?.data?.error || errorObj.response?.data?.message || "Error al actualizar perfil";
      toast.error(msg);
    } finally {
      setSavingProfile(false);
    }
  };

  const handleLogout = async () => {
    sessionStorage.setItem("navigating", "true");
    try {
      await api.post("/auth/logout");
    } catch {}
    setAccessToken(null);
    localStorage.removeItem("user_email");
    localStorage.removeItem("user_role");
    window.location.href = "/login";
  };

  return (
    <div className="min-h-screen bg-background flex flex-col">
      {/* Loader overlay */}
      <NavigationLoader />

      {/* Header */}
      <header className="sticky top-0 z-40 w-full border-b border-border bg-card/80 backdrop-blur-sm">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between gap-4">

          {/* Brand */}
          <button onClick={() => handleNavigation("/home", navigate)} className="flex items-center gap-3 shrink-0 cursor-pointer focus-visible:ring-2">
            <BrandLogo />
            <span className="text-sm font-bold text-foreground hidden sm:block tracking-tight">
              AutonomiFlow
            </span>
          </button>

          {/* Nav (desktop) */}
          <nav className="hidden md:flex items-center gap-1" aria-label="Navegación principal">
            {navItems.map((item) => (
              <NavButton
                key={item.path}
                item={item}
                isActive={location.pathname === item.path}
              />
            ))}
          </nav>

          {/* User dropdown */}
          <DropdownMenu>
            <DropdownMenuTrigger asChild>
              <button
                className="flex items-center gap-2 pl-2 pr-3 py-1.5 rounded-full hover:bg-muted transition-colors cursor-pointer focus:outline-none focus:ring-2 focus:ring-ring/50"
                aria-label="Menú de usuario"
              >
                <Avatar className="size-8">
                  <AvatarFallback className="bg-primary-soft text-primary text-xs font-semibold">
                    {getInitials(userEmail)}
                  </AvatarFallback>
                </Avatar>
                <span className="text-sm text-muted-foreground hidden sm:block max-w-32 truncate font-medium">
                  {userEmail}
                </span>
                <ChevronRight className="size-3 text-muted-foreground" aria-hidden="true" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-52">
              <div className="px-2 py-1.5 border-b">
                <p className="text-xs font-semibold text-foreground truncate">{userEmail}</p>
                <p className="text-[11px] text-muted-foreground capitalize">Rol: {userRole || "USER"}</p>
              </div>
              <DropdownMenuItem
                onClick={abrirModalPerfil}
                className="cursor-pointer"
              >
                <UserIcon className="size-4" aria-hidden="true" />
                Mi Perfil / Editar Datos
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => handleNavigation("/certificado", navigate)}
                className="cursor-pointer"
              >
                <Shield className="size-4" aria-hidden="true" />
                Certificado Digital
              </DropdownMenuItem>
              <DropdownMenuItem
                onClick={() => handleNavigation("/ose-sender", navigate)}
                className="cursor-pointer"
              >
                <Upload className="size-4" aria-hidden="true" />
                Enviar a OSE
              </DropdownMenuItem>
              {userRole === "ADMIN" && (
                <>
                  <DropdownMenuSeparator />
                  <DropdownMenuItem
                    onClick={() => handleNavigation("/user-management", navigate)}
                    className="cursor-pointer"
                  >
                    <Users className="size-4" aria-hidden="true" />
                    Usuarios y Roles
                  </DropdownMenuItem>
                  <DropdownMenuItem
                    onClick={() => handleNavigation("/catalog-management", navigate)}
                    className="cursor-pointer"
                  >
                    <Settings className="size-4" aria-hidden="true" />
                    Mantenimientos
                  </DropdownMenuItem>
                </>
              )}
              <DropdownMenuSeparator />
              <DropdownMenuItem
                variant="destructive"
                onClick={handleLogout}
                className="cursor-pointer"
              >
                <LogOut className="size-4" aria-hidden="true" />
                Cerrar sesión
              </DropdownMenuItem>
            </DropdownMenuContent>
          </DropdownMenu>
        </div>

        {/* Nav mobile */}
        <div className="md:hidden flex items-center gap-1 px-4 pb-3 overflow-x-auto scrollbar-none">
          {navItems.map((item) => (
            <MobileNavButton
              key={item.path}
              item={item}
              isActive={location.pathname === item.path}
            />
          ))}
        </div>
      </header>

      {/* Content */}
      <main className="flex-1">
        <Outlet />
      </main>

      {/* Modal Dialog Editar Mi Perfil */}
      <Dialog open={profileDialogOpen} onOpenChange={setProfileDialogOpen}>
        <DialogContent className="sm:max-w-md">
          <form onSubmit={handleUpdateProfile}>
            <DialogHeader>
              <DialogTitle className="flex items-center gap-2 text-lg">
                <UserIcon className="size-5 text-primary" aria-hidden="true" />
                Mi Perfil / Editar Datos
              </DialogTitle>
              <DialogDescription className="text-xs">
                Actualice su correo electrónico o contraseña. Para establecer una nueva contraseña, debe ingresar su contraseña actual guardada en la base de datos.
              </DialogDescription>
            </DialogHeader>

            <div className="space-y-4 py-4">
              {/* Email */}
              <div className="space-y-1.5">
                <Label htmlFor="profile-email" className="text-xs font-semibold">
                  Correo Electrónico <span className="text-destructive">*</span>
                </Label>
                <Input
                  id="profile-email"
                  type="email"
                  placeholder="usuario@empresa.com…"
                  autoComplete="off"
                  spellCheck={false}
                  value={profileEmail}
                  onChange={(e) => setProfileEmail(e.target.value)}
                  className="h-10 text-sm"
                  required
                />
              </div>

              {/* Contraseña Actual */}
              <div className="space-y-1.5">
                <Label htmlFor="profile-old-pass" className="text-xs font-semibold flex items-center justify-between">
                  <span>Contraseña Actual (de la BD)</span>
                  <span className="text-muted-foreground font-normal text-[11px]">(Requerida para cambiar clave)</span>
                </Label>
                <div className="relative">
                  <Input
                    id="profile-old-pass"
                    type={showOldPassword ? "text" : "password"}
                    placeholder="Contraseña actual guardada en BD…"
                    autoComplete="off"
                    value={profileOldPassword}
                    onChange={(e) => setProfileOldPassword(e.target.value)}
                    className="pr-10 h-10 text-sm"
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
                <Label htmlFor="profile-new-pass" className="text-xs font-semibold">
                  Nueva Contraseña <span className="text-muted-foreground font-normal text-[11px]">(Opcional)</span>
                </Label>
                <div className="relative">
                  <Input
                    id="profile-new-pass"
                    type={showNewPassword ? "text" : "password"}
                    placeholder="Nueva contraseña…"
                    autoComplete="off"
                    value={profileNewPassword}
                    onChange={(e) => setProfileNewPassword(e.target.value)}
                    className="pr-10 h-10 text-sm"
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
            </div>

            <DialogFooter className="gap-2">
              <Button type="button" variant="outline" onClick={() => setProfileDialogOpen(false)} disabled={savingProfile} className="cursor-pointer">
                Cancelar
              </Button>
              <Button type="submit" disabled={savingProfile} className="cursor-pointer bg-primary hover:bg-primary/90">
                {savingProfile ? (
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
    </div>
  );
}

export default AppLayout;
