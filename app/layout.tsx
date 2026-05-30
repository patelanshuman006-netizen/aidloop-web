import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "AidLoop — Hyperlocal Modern Social Community Aid Platform",
  description: "Post local needs, share medical relief, trade locally with Neighborhood Loops and location Privacy Shield on modern glassmorphic web & Android dashboards.",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en" className="dark scroll-smooth">
      <body className="antialiased bg-[#0e0e11] text-white selection:bg-amber-400 selection:text-black">
        {children}
      </body>
    </html>
  );
}
