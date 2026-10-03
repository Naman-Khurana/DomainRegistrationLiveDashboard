import { NextResponse } from "next/server";
import { getMockSnapshot } from "@/lib/api";
import type { StatsPayload } from "@/types/api";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL ?? "http://localhost:8080";

export async function GET() {
  try {
    const res = await fetch(`${BASE_URL}/v1/snapshot`, {
      next: { revalidate: 10 },
    });
    if (!res.ok) throw new Error(`Backend ${res.status}`);
    const data: StatsPayload = await res.json();
    return NextResponse.json(data);
  } catch {
    return NextResponse.json(getMockSnapshot());
  }
}
