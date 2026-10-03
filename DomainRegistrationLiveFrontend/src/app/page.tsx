import { getMockSnapshot, getMockFeed } from "@/lib/api";
import type { StatsPayload } from "@/types/api";

// Client components
import Navbar from "@/components/Navbar";
import LiveDashboardClient from "@/components/LiveDashboardClient";

async function getSnapshot(): Promise<StatsPayload> {
  const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";
  try {
    const res = await fetch(`${BASE_URL}/v1/snapshot`, {
      next: { revalidate: 10 },
    });
    if (!res.ok) throw new Error(`${res.status}`);
    return res.json();
  } catch {
    return getMockSnapshot();
  }
}

export default async function LivePage() {
  const snapshot = await getSnapshot();
  const initialFeed = getMockFeed();

  return (
    <>
      <Navbar />
      <LiveDashboardClient initialSnapshot={snapshot} initialFeed={initialFeed} />
    </>
  );
}
