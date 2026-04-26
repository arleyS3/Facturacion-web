import React, { useState } from 'react';
import { useNavigate } from 'react-router';
import { api } from '../lib/api'; 
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';
import logoEmpresa from '../assets/logo-empresa.png';

export const Registro: React.FC = () => {
  // 1. Estados del formulario
  const [nombre, setNombre] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  // Estados de control
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  
  const navigate = useNavigate();

  // 2. Función para manejar el registro
  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');

    // Validación básica en el frontend
    if (password !== confirmPassword) {
      setError('Las contraseñas no coinciden.');
      return;
    }

    if (password.length < 8) {
      setError('La contraseña debe tener al menos 8 caracteres.');
      return;
    }

    setIsLoading(true);

    try {
      // 3. Petición al backend (Ajusta los campos según lo que espere el DTO del backend)
      await api.post('/auth/register', {
        name: nombre, // A veces el backend lo llama "nombre", "firstName", etc.
        email: email,
        password: password
      });

      // Si es exitoso, redirigimos al login para que inicie sesión
      // Podrías poner un toast/alerta de éxito aquí si tu equipo lo usa
      navigate('/');

    } catch (err: any) {
      if (err.response) {
        setError(err.response.data.message || 'Error al registrar el usuario. Es posible que el correo ya exista.');
      } else {
        setError('Error de conexión con el servidor.');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen w-full bg-white">
      
      {/* Lado izquierdo: Espacio para la imagen */}
      <div className="hidden lg:flex w-1/2 bg-white relative overflow-hidden">
             <img
               src={logoEmpresa}
               alt="Logo de la Empresa"
               className="w-full  object-cover absolute inset-0"
             />
           </div>

      {/* Lado derecho: Formulario de Registro */}
      <div className="flex w-full lg:w-1/2 items-center justify-center p-8 sm:p-12">
        <div className="w-full max-w-md space-y-8">
          
          <div className="text-left">
            <h2 className="text-3xl font-bold tracking-tight text-slate-900">
              Crea tu cuenta
            </h2>
            <p className="mt-2 text-sm text-slate-500">
              Únete a nosotros y empieza a gestionar tus facturas fácilmente.
            </p>
          </div>

          <form className="space-y-5" onSubmit={handleRegister}>
            
            {error && (
              <div className="p-3 bg-red-50 border border-red-200 text-red-600 text-sm rounded-md">
                {error}
              </div>
            )}

            <div className="space-y-2">
              <Label htmlFor="nombre">Nombre completo</Label>
              <Input 
                id="nombre" 
                type="text" 
                placeholder="Juan Pérez" 
                required 
                className="w-full"
                value={nombre}
                onChange={(e) => setNombre(e.target.value)} 
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="email">Correo electrónico</Label>
              <Input 
                id="email" 
                type="email" 
                placeholder="ejemplo@empresa.com" 
                required 
                className="w-full"
                value={email}
                onChange={(e) => setEmail(e.target.value)} 
              />
            </div>

            <div className="grid grid-cols-1 gap-5 sm:grid-cols-2">
              <div className="space-y-2">
                <Label htmlFor="password">Contraseña</Label>
                <Input 
                  id="password" 
                  type="password" 
                  placeholder="Min. 8 caracteres"
                  required 
                  className="w-full"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)} 
                />
              </div>
              <div className="space-y-2">
                <Label htmlFor="confirmPassword">Confirmar Contraseña</Label>
                <Input 
                  id="confirmPassword" 
                  type="password" 
                  placeholder="Repite la contraseña"
                  required 
                  className="w-full"
                  value={confirmPassword}
                  onChange={(e) => setConfirmPassword(e.target.value)} 
                />
              </div>
            </div>

            <Button 
              className="w-full bg-slate-900 hover:bg-slate-800 text-white mt-4" 
              type="submit"
              disabled={isLoading}
            >
              {isLoading ? 'Registrando...' : 'Crear cuenta'}
            </Button>
          </form>

          <div className="relative">
            <div className="absolute inset-0 flex items-center">
              <div className="w-full border-t border-slate-200"></div>
            </div>
            <div className="relative flex justify-center text-sm">
              <span className="bg-white px-2 text-slate-500">O regístrate con</span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <Button variant="outline" className="w-full text-slate-700">Google</Button>
            <Button variant="outline" className="w-full text-slate-700">Facebook</Button>
          </div>

          <p className="text-center text-sm text-slate-600 mt-8">
            ¿Ya tienes una cuenta?{' '}
            <a href="/" className="font-medium text-blue-600 hover:text-blue-500">
              Inicia sesión
            </a>
          </p>

        </div>
      </div>
    </div>
  );
};

export default Registro;