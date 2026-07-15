import { createBrowserRouter, Navigate } from "react-router";
import { Login } from "@/features/autenticacion/pages/Login";
import { Registro } from "@/features/autenticacion/pages/Registro";
import { Home } from "@/features/shared/pages/Home";
import { SalesDocuments } from "@/features/ventas/pages/SalesDocuments";
import { ShippingGuide } from "@/features/guias-remision/pages/ShippingGuide";
import { OseSender } from "@/features/shared/pages/OseSender";
import { NotFound } from "@/features/shared/pages/NotFound";
import { ProtectedRoute } from "@/components/layout/ProtectedRoute";
import { AppLayout } from "@/components/layout/AppLayout";

const PublicRoute = ({ children }: { children: React.ReactNode }) => {
  // La cookie httpOnly no es accesible desde JS — siempre renderizamos.
  // ProtectedRoute dentro de / maneja la redirección si está autenticado.
  return <>{children}</>;
};

export const router = createBrowserRouter([
  // --- RUTAS PÚBLICAS ---
  {
    path: "/login",
    element: (
      <PublicRoute>
        <Login />
      </PublicRoute>
    ),
  },
  {
    path: "/registro",
    element: (
      <PublicRoute>
        <Registro />
      </PublicRoute>
    ),
  },

  // --- RUTAS PROTEGIDAS (todas bajo AppLayout) ---
  {
    path: "/",
    element: (
      <ProtectedRoute>
        <AppLayout />
      </ProtectedRoute>
    ),
    children: [
      { index: true, element: <Navigate to="/home" replace /> },
      { path: "home", element: <Home /> },
      { path: "sales-documents", element: <SalesDocuments /> },
      { path: "shipping-guide", element: <ShippingGuide /> },
      { path: "ose-sender", element: <OseSender /> },
    ],
  },

  // --- RUTA 404 ---
  {
    path: "*",
    element: <NotFound />,
  },
]);
