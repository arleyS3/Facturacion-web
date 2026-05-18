import { Navigate } from "react-router";

interface Props {
  children: React.ReactNode;
}

export const ProtectedRoute = ({ children }: Props) => {
  const token = localStorage.getItem('token');

  // Si no hay token, redirigimos al login ("/")
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  // Si hay token, permitimos ver la página
  return <>{children}</>;
};
