import { NextResponse } from "next/server";
import { getMockFeed } from "@/lib/api";
import type { FeedEntry } from "@/types/api";
import { FEED_URL } from "@/app/constants/url_constants";



export async function GET(request: Request) {
  try {
    const res = await fetch(FEED_URL, { cache: "no-store" });
    if (!res.ok) throw new Error(`Backend ${res.status}`);

    const data = await res.json();
    // Backend may return: array directly, { items: [] }, or { feed: [] }
    const items: FeedEntry[] = Array.isArray(data)
      ? data
      : (data.items ?? data.feed ?? []);
    return NextResponse.json(items);
  } catch {
    // Fallback mock
    const mock = getMockFeed()
      .slice(0, 3)
      .map((e) => ({ ...e, seq: e.seq + Date.now(), t: Date.now() - Math.random() * 5000 }));
    return NextResponse.json(mock);
  }
}
