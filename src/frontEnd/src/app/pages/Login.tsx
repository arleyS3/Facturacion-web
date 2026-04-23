import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { api } from '../lib/api.ts'; 
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';

export const Login: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      
      const response = await api.post('/auth/login', {
        email,
        password
      });

     
      const data = response.data;

      if (data.token) {
        localStorage.setItem('token', data.token);
      }

      navigate('/home');

    } catch (err: any) {
   
      if (err.response) {
     
        setError(err.response.data.message || 'Credenciales inválidas o usuario no encontrado');
      } else {
      
        setError('Error de conexión. Verifica que el servidor Backend esté encendido.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen w-full bg-white">
      
      {/* Lado izquierdo */}
      <div className="hidden lg:flex w-1/2 bg-slate-100 items-center justify-center relative">
        <div className="text-slate-400 text-lg font-medium border-2 border-dashed border-slate-300 p-8 rounded-lg">
          [Aquí irá el logo]
        </div>
      </div>

      {/* Lado derecho: Formulario */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-8 sm:p-12">
        <div className="w-full max-w-md space-y-8">
          
          <div className="text-left">
            <h2 className="text-3xl font-bold tracking-tight text-slate-900">
              ¡Bienvenido de AutonomiFlow!
            </h2>
            <p className="mt-2 text-sm text-slate-500">
              Emisor de comprobantes electrónicos
            </p>
          </div>

          <form className="space-y-6" onSubmit={handleLogin}>
            
            {error && (
              <div className="p-3 bg-red-50 border border-red-200 text-red-600 text-sm rounded-md">
                {error}
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="email">Correo electrónico</Label>
              <Input 
                id="email" 
                type="email" 
                placeholder="Example@email.com" 
                required 
                className="w-full"
                value={email}
                onChange={(e) => setEmail(e.target.value)} 
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
                value={password}
                onChange={(e) => setPassword(e.target.value)} 
              />
              <div className="flex justify-end mt-1">
                <a href="#" className="text-sm font-medium text-blue-600 hover:text-blue-500">
                  ¿Olvidaste tu contraseña?
                </a>
              </div>
            </div>

            <Button 
              className="w-full bg-slate-900 hover:bg-slate-800 text-white" 
              type="submit"
              disabled={isLoading}
            >
              {isLoading ? 'Conectando...' : 'Iniciar sesión'}
            </Button>
          </form>

          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-white px-2 text-slate-500">O iniciar sesión con</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Button variant="outline" className="w-full text-slate-700">Google</Button>
            <Button variant="outline" className="w-full text-slate-700">Facebook</Button>
          </div>

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