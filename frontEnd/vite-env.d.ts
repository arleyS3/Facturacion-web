/// <reference types="vite/client" />

interface ImportMetaEnv {
  // otras vars VITE_... opcionales
  readonly [key: string]: string | undefined;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
