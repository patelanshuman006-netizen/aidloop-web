"use client";

import Link from "next/link";

export default function NotFound() {
  return (
    <div className="min-h-screen bg-[#0E0E11] text-white flex flex-col items-center justify-center p-6 text-center">
      <h1 className="text-4xl font-extrabold mb-4 text-amber-500">404 — Node Not Found</h1>
      <p className="text-neutral-400 mb-8 max-w-sm">
        The hyperlocal loop or portal view you are trying to access does not exist in Pune enclaves.
      </p>
      <Link 
        href="/" 
        className="px-6 py-3 rounded-xl bg-amber-500 hover:bg-amber-400 text-black font-black text-xs tracking-wider uppercase transition-all shadow-md shadow-amber-500/10"
      >
        Return to Main Portal
      </Link>
    </div>
  );
}
