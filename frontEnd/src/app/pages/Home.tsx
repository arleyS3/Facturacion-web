import { useNavigate } from "react-router";
import { Receipt, Truck, ArrowRight } from "lucide-react";
import startupIdea from "../assets/startup-idea.png";

export function Home() {
  const navigate = useNavigate();

  const options = [
    {
      id: "sales",
      title: "Documentos de Venta",
      description: "Facturas, Boletas y Notas de Crédito, Notas de Débito",
      icon: Receipt,
      gradient: "from-blue-600 to-indigo-600",
      bgLight: "bg-blue-50",
      borderLight: "border-blue-100",
      iconColor: "text-blue-600",
      path: "/sales-documents",
    },
    {
      id: "shipping",
      title: "Guías de Remisión",
      description: "Guia de Remitente, Guia de Remision Transportista",
      icon: Truck,
      gradient: "from-emerald-500 to-teal-600",
      bgLight: "bg-emerald-50",
      borderLight: "border-emerald-100",
      iconColor: "text-emerald-600",
      path: "/shipping-guide",
    },
  ];

  return (
    <div className="min-h-screen bg-[#FAFAFA] flex flex-col relative overflow-hidden font-sans">
      {/* Subtle background ambient glows */}
      <div className="absolute top-0 left-1/4 w-96 h-96 bg-blue-400/10 rounded-full blur-[120px] pointer-events-none -translate-y-1/2" />
      <div className="absolute bottom-0 right-1/4 w-96 h-96 bg-emerald-400/10 rounded-full blur-[120px] pointer-events-none translate-y-1/2" />

      <main className="flex-1 flex flex-col items-center justify-center p-6 md:p-12 relative z-10">
        <div className="max-w-5xl w-full space-y-12 md:space-y-16">
          
          {/* Header & Illustration Split */}
          <div className="flex flex-col lg:flex-row items-center justify-between gap-12">
            <div className="flex-1 text-center lg:text-left space-y-6">
              <h1 className="text-4xl md:text-5xl lg:text-6xl font-extrabold text-slate-900 tracking-tight leading-tight">
                Emite comprobantes <span className="text-blue-600 block mt-2">fácil y rápido</span>
              </h1>
              <p className="text-slate-500 text-lg md:text-xl max-w-xl mx-auto lg:mx-0">
                Selecciona una de las opciones para comenzar a generar tus documentos electrónicos
              </p>
            </div>
            <div className="flex-1 w-full max-w-md lg:max-w-lg relative flex justify-center lg:justify-end">
              <img 
                src={startupIdea}
                alt="Ilustración de facturación electrónica" 
                className="w-full h-auto max-w-xs md:max-w-sm lg:max-w-md object-contain hover:scale-[1.02] transition-transform duration-700 ease-out"
              />
            </div>
          </div>

          {/* Options Grid */}
          <div className="grid md:grid-cols-2 gap-6 md:gap-8">
            {options.map((option) => {
              const Icon = option.icon;
              return (
                <button
                  key={option.id}
                  onClick={() => navigate(option.path)}
                  className="group relative flex flex-col p-8 bg-white rounded-3xl border border-slate-200 shadow-sm hover:shadow-2xl hover:border-slate-300 transition-all duration-500 text-left w-full overflow-hidden focus:outline-none focus:ring-4 focus:ring-slate-100"
                >
                  {/* Hover background gradient effect */}
                  <div
                    className={`absolute inset-0 bg-gradient-to-br ${option.gradient} opacity-0 group-hover:opacity-[0.02] transition-opacity duration-500 pointer-events-none`}
                  />

                  {/* Icon Container */}
                  <div
                    className={`w-16 h-16 rounded-2xl ${option.bgLight} ${option.borderLight} border flex items-center justify-center mb-8 group-hover:scale-110 group-hover:-rotate-3 transition-transform duration-500`}
                  >
                    <Icon className={`w-8 h-8 ${option.iconColor}`} strokeWidth={1.5} />
                  </div>

                  {/* Content */}
                  <div className="flex-1 space-y-2 relative z-10">
                    <h3 className="text-2xl font-bold text-slate-900 tracking-tight">
                      {option.title}
                    </h3>
                    <p className="text-slate-500 font-medium leading-relaxed">
                      {option.description}
                    </p>
                  </div>

                  {/* Footer Action */}
                  <div className="mt-8 flex items-center font-semibold text-slate-400 group-hover:text-slate-900 transition-colors relative z-10">
                    <span className="text-sm uppercase tracking-wider">Continuar</span>
                    <ArrowRight className="w-5 h-5 ml-2 transform group-hover:translate-x-1.5 transition-transform duration-300" />
                  </div>
                </button>
              );
            })}
          </div>
        </div>
      </main>
    </div>
  );
}
