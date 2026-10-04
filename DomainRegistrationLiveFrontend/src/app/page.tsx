import Navbar from "@/components/Navbar";
import LiveDashboardClient from "@/components/LiveDashboardClient";

// No data fetching here.  The header renders instantly (client components are also rendered on the
// server), and LiveDashboardClient loads the snapshot from SNAPSHOT_URL, showing a spinner until it arrives.
export default function LivePage() {
  return (
    <>
      <Navbar />
      <LiveDashboardClient />
    </>
  );
}