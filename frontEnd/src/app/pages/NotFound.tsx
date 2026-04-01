import { useNavigate } from "react-router";
import { Button } from "../components/ui/button";
import { FileQuestion, Home } from "lucide-react";

export function NotFound() {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-50 to-slate-100">
      <div className="text-center">
        <div className="inline-flex items-center justify-center size-24 bg-slate-200 rounded-full mb-6">
          <FileQuestion className="size-12 text-slate-600" />
        </div>
        <h1 className="text-6xl font-bold text-slate-900 mb-4">404</h1>
        <p className="text-xl text-slate-600 mb-8">Página no encontrada</p>
        <Button onClick={() => navigate("/")} size="lg">
          <Home className="size-4 mr-2" />
          Volver al inicio
        </Button>
      </div>
    </div>
  );
}
