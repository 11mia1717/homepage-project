# Build Stage
FROM node:lts-alpine AS build
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Production Stage (Ubuntu Base)
FROM ubuntu:22.04

# Install Nginx
RUN apt-get update && \
    apt-get install -y nginx curl && \
    rm -rf /var/lib/apt/lists/*

# Copy build artifacts to Nginx default directory
COPY --from=build /app/dist /var/www/html

# Nginx Configuration
# We need to configure Nginx to handle SPA routing (try_files)
# Creating a simple default config directly
RUN echo 'server { \
    listen 80; \
    server_name localhost; \
    root /var/www/html; \
    index index.html index.htm; \
    location / { \
        try_files $uri $uri/ /index.html; \
    } \
}' > /etc/nginx/sites-available/default

EXPOSE 80

# Start Nginx in foreground
CMD ["nginx", "-g", "daemon off;"]
