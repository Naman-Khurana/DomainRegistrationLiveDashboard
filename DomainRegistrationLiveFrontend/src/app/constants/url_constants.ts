// BASE_URL resolution by environment:
//
// Local dev (npm run dev, no .env.local):
//   NEXT_PUBLIC_API_URL is unset → BASE_URL = ""
//   Browser fetches /v1/live/snapshot → Next.js rewrite proxy → localhost:8080
//
// Self-hosted production (Caddy, all services on one server):
//   NEXT_PUBLIC_API_URL is unset → BASE_URL = ""
//   Browser fetches /v1/live/snapshot → Caddy routes /v1/* → backend:8080
//   (no CORS issue - frontend and backend share the same domain)
//
// Vercel + separate API domain (not default setup):
//   Set NEXT_PUBLIC_API_URL=https://api.example.com in the Vercel dashboard
//   Browser fetches https://api.example.com/v1/live/snapshot directly

export const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "";
export const LIVE_URL_PREFIX = BASE_URL + "/v1/live";
export const SNAPSHOT_URL = LIVE_URL_PREFIX + "/snapshot";
export const FEED_URL = LIVE_URL_PREFIX + "/feed";
export const SEARCH_URL = LIVE_URL_PREFIX + "/search";
export const DAILY_URL = LIVE_URL_PREFIX + "/daily";