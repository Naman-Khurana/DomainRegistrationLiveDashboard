import { NextResponse } from "next/server";
import { getMockFeed } from "@/lib/api";
import type { FeedEntry } from "@/types/api";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function GET(request: Request) {
  const { searchParams } = new URL(request.url);
  const afterSeq = searchParams.get("afterSeq");

  try {
    const url = afterSeq
      ? `${BASE_URL}/v1/feed?afterSeq=${afterSeq}`
      : `${BASE_URL}/v1/feed`;

    const res = await fetch(url, { cache: "no-store" });
    if (!res.ok) throw new Error(`Backend ${res.status}`);

    const data: FeedEntry[] = await res.json();
    return NextResponse.json(data);
  } catch {
    // Fallback mock
    const mock = getMockFeed()
      .slice(0, 3)
      .map((e) => ({ ...e, seq: e.seq + Date.now(), t: Date.now() - Math.random() * 5000 }));
    return NextResponse.json(mock);
  }
}
