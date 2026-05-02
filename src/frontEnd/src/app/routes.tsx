import { createBrowserRouter, Navigate } from "react-router";
import { Login } from "./pages/Login";
import { Registro } from "./pages/Registro";
import { Home } from "./pages/Home";
import { SalesDocuments } from "./pages/SalesDocuments";
import { ShippingGuide } from "./pages/ShippingGuide";
import { NotFound } from "./pages/NotFound";
import { ProtectedRoute } from "./components/ProtectedRoute";


const PublicRoute = ({ children }: { children: React.ReactNode }) => {
  const token = localStorage.getItem('token');
  return token ? <Navigate to="/home" replace /> : <>{children}</>;
};

export const router = createBrowserRouter([
  // --- RUTAS PÚBLICAS (Con redirección si ya está logueado) ---
  {
    path: "/",
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

  // --- RUTAS PROTEGIDAS (Requieren autenticación) ---
  {
    path: "/home",
    element: (
      <ProtectedRoute>
        <Home />
      </ProtectedRoute>
    ),
  },
  {
    path: "/sales-documents",
    element: (
      <ProtectedRoute>
        <SalesDocuments />
      </ProtectedRoute>
    ),
  },
  {
    path: "/shipping-guide",
    element: (
      <ProtectedRoute>
        <ShippingGuide />
      </ProtectedRoute>
    ),
  },

  // --- RUTA 404 (No encontrada) ---
  {
    path: "*",
    element: <NotFound />,
  },
]);