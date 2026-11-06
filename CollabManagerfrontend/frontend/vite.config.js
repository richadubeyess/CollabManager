
import path from "path";
import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

export default defineConfig({
  plugins: [react()],

  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },

  server: {
    proxy: {
      "/auth": {
        target: "http://localhost:5054",
        changeOrigin: true,
      },

      "/api": {
        target: "http://localhost:5054",
        changeOrigin: true,
      },

      "/reset-password": {
        target: "http://localhost:5054",
        changeOrigin: true,
      },

      "/ws": {
        target: "http://localhost:5054",
        changeOrigin: true,
        ws: true,
      },
    },
  },
});
