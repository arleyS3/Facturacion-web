import { createBrowserRouter } from "react-router";
import { Home } from "./pages/Home";
import { SalesDocuments } from "./pages/SalesDocuments";
import { ShippingGuide } from "./pages/ShippingGuide";
import { NotFound } from "./pages/NotFound";

export const router = createBrowserRouter([
  {
    path: "/",
    Component: Home,
  },
  {
    path: "/sales-documents",
    Component: SalesDocuments,
  },
  {
    path: "/shipping-guide",
    Component: ShippingGuide,
  },
  {
    path: "*",
    Component: NotFound,
  },
]);