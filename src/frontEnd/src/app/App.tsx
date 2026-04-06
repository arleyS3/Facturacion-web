import { RouterProvider } from "react-router";
import { router } from "./routes";
import { Toaster } from "sileo";

/**
 * Componente raíz de la aplicación frontEnd. Define el router y el toaster.
 */
export default function App() {
  return (
    <>
      <RouterProvider router={router} />
      <Toaster position="top-right" theme="dark" />
    </>
  );
}
