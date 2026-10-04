interface Props {
    // True once the first snapshot request has failed. 
    failed?: boolean;
}

// Circular loader shown below the header until the first snapshot arrives.
export default function DashboardLoader({ failed = false }: Props) {
    return (
        <div
            role="status"
            aria-live="polite"
            className="flex flex-col items-center justify-center gap-3 py-24"
        >
            <span
                className="h-8 w-8 animate-spin rounded-full border-[3px] border-gray-200 border-t-gray-600"
                aria-hidden="true"
            />

            {failed ? (
                <p className="text-[13px] text-gray-400">
                    Can&apos;t reach the server yet. Retrying…
                </p>
            ) : (
                <p className="text-[13px] text-gray-400">
                    Connecting to Live Feed...
                </p>
            )}
        </div>
    );
}