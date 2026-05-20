"use client";

import React from "react";
import { useLocation, Outlet} from "react-router";
import { motion, AnimatePresence } from "motion/react";
import { LogOut, LayoutDashboard, FileText, Truck, ChevronRight, Upload } from "lucide-react";

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
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
    <div className={`size-8 rounded-lg bg-primary flex items-center justify-center ${className}`}>
      <span className="text-primary-foreground text-xs font-bold">AF</span>
    </div>
  );
}

/** Navigation handler that shows loader for 3 seconds then navigates */
function handleNavigation(path: string) {
  // Set loading state via sessionStorage to persist across page load
  sessionStorage.setItem("navigating", "true");
  // Force navigation
  window.location.href = path;
}

/** Loading overlay with Motion animation - always shows for 3 seconds */
function NavigationLoader() {
  const [showLoader, setShowLoader] = React.useState(false);

  React.useEffect(() => {
    // Check if we're navigating (from sessionStorage)
    const isNavigating = sessionStorage.getItem("navigating");
    
    if (isNavigating) {
      setShowLoader(true);
      
      // Clear after 2 seconds and remove the flag
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
            {/* Logo con efecto glow */}
            <div className="relative">
              {/* Glow effect */}
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

            {/* Loading text */}
            <div className="flex flex-col items-center gap-3">
              <span className="text-xl font-semibold text-foreground">Cargando...</span>
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

/** Nav button component with click handler */
function NavButton({ 
  item, 
  isActive, 
  className = "" 
}: { 
  item: typeof navItems[0]; 
  isActive: boolean;
  className?: string;
}) {
  return (
    <button
      onClick={() => handleNavigation(item.path)}
      className={`flex items-center gap-2 px-3 py-2 rounded-md text-sm font-medium transition-colors cursor-pointer ${className} ${
        isActive
          ? "bg-primary-soft text-primary"
          : "text-muted-foreground hover:text-foreground hover:bg-muted"
      }`}
      aria-current={isActive ? "page" : undefined}
    >
      <item.icon className="size-4" strokeWidth={1.5} />
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
  return (
    <button
      onClick={() => handleNavigation(item.path)}
      className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-medium whitespace-nowrap transition-colors cursor-pointer shrink-0 ${className} ${
        isActive
          ? "bg-primary text-primary-foreground"
          : "text-muted-foreground bg-muted hover:bg-border/40"
      }`}
    >
      <item.icon className="size-3.5" strokeWidth={1.5} />
      {item.label}
    </button>
  );
}

export function AppLayout() {
  const location = useLocation();
  const userEmail = localStorage.getItem("user_email") ?? localStorage.getItem("token") ?? "Usuario";

  const handleLogout = () => {
    sessionStorage.setItem("navigating", "true");
    localStorage.removeItem("token");
    localStorage.removeItem("refreshToken");
    localStorage.removeItem("user_email");
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
          <button onClick={() => handleNavigation("/home")} className="flex items-center gap-3 shrink-0 cursor-pointer">
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
                <span className="text-sm text-muted-foreground hidden sm:block max-w-32 truncate">
                  {userEmail}
                </span>
                <ChevronRight className="size-3 text-muted-foreground" />
              </button>
            </DropdownMenuTrigger>
            <DropdownMenuContent align="end" className="w-48">
              <div className="px-2 py-1.5">
                <p className="text-xs text-muted-foreground truncate">{userEmail}</p>
              </div>
              <DropdownMenuItem
                onClick={() => handleNavigation("/ose-sender")}
                className="cursor-pointer"
              >
                <Upload className="size-4" />
                Enviar a OSE
              </DropdownMenuItem>
              <DropdownMenuSeparator />
              <DropdownMenuItem
                variant="destructive"
                onClick={handleLogout}
                className="cursor-pointer"
              >
                <LogOut className="size-4" />
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
    </div>
  );
}
