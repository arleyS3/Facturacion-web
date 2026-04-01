import { RouterProvider } from "react-router";
import { router } from "./routes";
import { Toaster } from "sileo";
export default function App() {
  return (
    <>
      <RouterProvider router={router} />
      <Toaster position="top-right" theme="dark" />
    </>
  );
}