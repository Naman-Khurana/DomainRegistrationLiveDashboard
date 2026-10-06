import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  output: "standalone",


  async rewrites() {
    // If an explicit API URL is configured (Vercel production), don't add rewrites.
    if (process.env.NEXT_PUBLIC_API_URL) {
      return [];
    }
    // Local dev: proxy /v1/* → the local Spring Boot backend.
    return [
      {
        source: "/v1/:path*",
        destination: `${process.env.BACKEND_URL || "http://localhost:8080"}/v1/:path*`,
      },
    ];
  },
};

export default nextConfig;
