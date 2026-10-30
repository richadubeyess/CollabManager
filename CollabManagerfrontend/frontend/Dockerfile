
# Stage 1: Build React application
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci

# Copy frontend source code
COPY . .

# Build production files
RUN npm run build


# Stage 2: Serve React application using Nginx
FROM nginx:alpine

# Copy React build output
COPY --from=build /app/dist /usr/share/nginx/html

# Copy custom Nginx configuration for React Router
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expose Nginx port
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
