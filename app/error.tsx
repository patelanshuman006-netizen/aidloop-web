"use client";

import React, { useEffect } from "react";

export default function Error({
  error,
  reset,
}: {
  error: Error & { digest?: string };
  reset: () => void;
}) {
  useEffect(() => {
    console.error("Local Web Portal failure boundary:", error);
  }, [error]);

  return (
    <div className="min-h-screen bg-[#0E0E11] text-white flex flex-col items-center justify-center p-6 text-center">
      <h1 className="text-4xl font-extrabold mb-4 text-[#FF1744]">Grid Signal Disruption</h1>
      <p className="text-neutral-400 mb-8 max-w-sm font-medium">
        An unexpected connection or data mapping error occurred on the hyperlocal database. Let&apos;s try to restore synchronicity.
      </p>
      <button 
        onClick={() => reset()} 
        className="px-6 py-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-black font-black text-xs tracking-wider uppercase transition-all shadow-md shadow-amber-500/10"
      >
        Re-synchronize Grid
      </button>
    </div>
  );
}
