
export function toMillis(value: number | string | null | undefined): number | undefined {
    if (typeof value === "number") return Number.isFinite(value) ? value : undefined;
    if (typeof value === "string") {
        const ms = Date.parse(value);
        return Number.isNaN(ms) ? undefined : ms;
    }
    return undefined;
}