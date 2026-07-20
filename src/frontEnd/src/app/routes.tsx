import { createBrowserRouter, Navigate } from "react-router";
import { Login } from "@/features/autenticacion/pages/Login";
import { Registro } from "@/features/autenticacion/pages/Registro";
import { Home } from "@/features/shared/pages/Home";
import { SalesDocuments } from "@/features/ventas/pages/SalesDocuments";
import { ShippingGuide } from "@/features/guias-remision/pages/ShippingGuide";
import { OseSender } from "@/features/shared/pages/OseSender";
import { CertificadoConfig } from "@/features/shared/pages/CertificadoConfig";
import { CatalogManagementPage } from "@/features/shared/pages/CatalogManagementPage";
import { UserManagementPage } from "@/features/shared/pages/UserManagementPage";
import { NotFound } from "@/features/shared/pages/NotFound";
import { NormativaSunatPage } from "@/features/normativa/pages/NormativaSunatPage";
import { ProtectedRoute } from "@/components/layout/ProtectedRoute";
import { AdminRoute } from "@/components/layout/AdminRoute";
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
      { path: "normativa", element: <NormativaSunatPage /> },
      { path: "certificado", element: <CertificadoConfig /> },
      {
        path: "catalog-management",
        element: (
          <AdminRoute>
            <CatalogManagementPage />
          </AdminRoute>
        ),
      },
      {
        path: "user-management",
        element: (
          <AdminRoute>
            <UserManagementPage />
          </AdminRoute>
        ),
      },
    ],
  },

  // --- RUTA 404 ---
  {
    path: "*",
    element: <NotFound />,
  },
]);
