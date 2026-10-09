FROM node:24.21.0-alpine3.23 AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY index.html tsconfig*.json vite.config.ts ./
COPY src ./src
# Cadena vacía: el navegador usa /api/health en el mismo origen Nginx.
# No se copia .env local ni se introduce un secreto en el build.
ENV VITE_API_BASE_URL=""
RUN npm run build

FROM nginx:1.30.5-alpine3.24 AS runtime
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist /usr/share/nginx/html
EXPOSE 80
