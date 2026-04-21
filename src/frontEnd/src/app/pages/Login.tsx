
import React from 'react';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '../components/ui/card';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Button } from '../components/ui/button';

export const Login: React.FC = () => {
  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-50 p-4">
      <Card className="w-full max-w-md shadow-lg">
        <CardHeader className="space-y-1 text-center">
          {/* Aquí luego pondremos el logo beta */}
          <div className="flex justify-center mb-4">
            <div className="h-12 w-12 bg-blue-600 rounded-full flex items-center justify-center text-white font-bold text-xl">
              FF
            </div>
          </div>
          <CardTitle className="text-2xl font-bold tracking-tight">Bienvenido de nuevo</CardTitle>
          <CardDescription>
            Ingresa tu correo y contraseña para acceder al sistema
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="space-y-2">
            <Label htmlFor="email">Correo electrónico</Label>
            <Input id="email" type="email" placeholder="ejemplo@empresa.com" required />
          </div>
          <div className="space-y-2">
            <Label htmlFor="password">Contraseña</Label>
            <Input id="password" type="password" required />
          </div>
        </CardContent>
        <CardFooter className="flex flex-col space-y-4">
          <Button className="w-full bg-blue-600 hover:bg-blue-700">Iniciar Sesión</Button>
          <div className="text-sm text-center text-gray-500">
            ¿No tienes una cuenta?{' '}
            <a href="/registro" className="text-blue-600 hover:underline">
              Regístrate aquí
            </a>
          </div>
        </CardFooter>
      </Card>
    </div>
  );
};

export default Login;