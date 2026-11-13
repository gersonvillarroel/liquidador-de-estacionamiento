// Producción (build de Docker). El navegador llama directo al backend publicado en el puerto 8080.
export const environment = {
  production: true,
  apiUrl: 'http://localhost:8080/api'
};
