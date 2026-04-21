import React from 'react';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';

export const Login: React.FC = () => {
  return (
    <div className="flex min-h-screen w-full bg-white">
      
    
      <div className="hidden lg:flex w-1/2 bg-slate-100 items-center justify-center relative">
        
        <div className="text-slate-400 text-lg font-medium border-2 border-dashed border-slate-300 p-8 rounded-lg">
          [IMAGEN]
        </div>
      </div>

      {/* Lado derecho: Formulario de Login */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-8 sm:p-12">
        <div className="w-full max-w-md space-y-8">
          
          {/* Títulos según el Figma */}
          <div className="text-left">
            <h2 className="text-3xl font-bold tracking-tight text-slate-900">
              ¡Bienvenido de Nuevo!
            </h2>
            <p className="mt-2 text-sm text-slate-500">
              Emisor de boletas y facturas
            </p>
          </div>

          {/* Formulario */}
          <form className="space-y-6" onSubmit={(e) => e.preventDefault()}>
            <div className="space-y-2">
              <Label htmlFor="email">Correo electrónico</Label>
              <Input 
                id="email" 
                type="email" 
                placeholder="Example@email.com" 
                required 
                className="w-full"
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="password">Contraseña</Label>
              <Input 
                id="password" 
                type="password" 
                placeholder="Al menos 8 caracteres"
                required 
                className="w-full"
              />
              <div className="flex justify-end mt-1">
                <a href="#" className="text-sm font-medium text-blue-600 hover:text-blue-500">
                  ¿Olvidaste tu contraseña?
                </a>
              </div>
            </div>

            <Button className="w-full bg-slate-900 hover:bg-slate-800 text-white" type="submit">
              Iniciar sesión
            </Button>
          </form>


          {/* Enlace de Registro */}
          <p className="text-center text-sm text-slate-600 mt-8">
            ¿No tienes una cuenta?{' '}
            <a href="/registro" className="font-medium text-blue-600 hover:text-blue-500">
              Regístrate
            </a>
          </p>

        </div>
      </div>

    </div>
  );
};

export default Login;