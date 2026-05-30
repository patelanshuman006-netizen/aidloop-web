"use client";

import React, { useState } from "react";
import { 
  Heart, 
  Map, 
  MessageSquare, 
  Plus, 
  Award, 
  Users, 
  CheckCircle2, 
  Sparkles, 
  ArrowRight, 
  Lock, 
  ShieldCheck,
  Zap,
  Globe,
  Compass,
  ArrowUpRight
} from "lucide-react";

export default function LandingPage() {
  const [activeLoop, setActiveLoop] = useState("Kalyani Nagar Loop");
  
  const availableLoops = [
    { name: "Kalyani Nagar Loop", desc: "🏡 Hyperlocal Mutual Aid circle", members: "1,240 members" },
    { name: "FC Road Tech Loop", desc: "💻 Academic & tech gear sharing", members: "890 members" },
    { name: "Baner Green Loop", desc: "🌱 Composting & organic food swap", members: "650 members" },
    { name: "Pune Medical Response", desc: "🩺 Clinical & emergency supply network", members: "1,820 members" }
  ];

  const mockFeeds = [
    {
      id: "feed-1",
      creator: "Anshuman Patel",
      username: "anshuman",
      locality: "Lunkad Sky Caronde",
      area: "Kalyani Nagar",
      tags: ["Emergency", "Mutual Aid"],
      title: "Urgently seeking wheelchair for senior resident recovery",
      content: "A senior resident in our block recently post-surgery, needs a lightweight wheelchair for 3 weeks of recovery. Happy to pick it up and return it washed and sterilized! 🙏",
      comments: 3,
      stars: 12,
      time: "2 mins ago"
    },
    {
      id: "feed-2",
      creator: "Priya Sharma",
      username: "priya_s",
      locality: "Pancard Club Road",
      area: "Baner",
      tags: ["Local Exchange"],
      title: "Giving away active organic sourdough starter!",
      content: "Have excess highly active sourdough starter ready to bake with. Packaged in custom mason jars, free to anyone in the Baner Green Loop! Pickup tonight.",
      comments: 5,
      stars: 8,
      time: "15 mins ago"
    },
    {
      id: "feed-3",
      creator: "Rahul K.",
      username: "rahul_k",
      locality: "Viman Nagar Core",
      area: "Viman Nagar",
      tags: ["Discussions"],
      title: "Viman Nagar Loop Event: Coordinated fresh dinner savior loop",
      content: "We have packed food for 80 family plates left over from local catering event. Looking for an active loop member with empty vehicle space to assist distribution routes safely tonight.",
      comments: 8,
      stars: 20,
      time: "42 mins ago"
    }
  ];

  return (
    <div className="min-h-screen bg-[#0E0E11] text-white overflow-x-hidden selection:bg-amber-400 selection:text-black">
      {/* Dynamic Ambient Background Glows */}
      <div className="absolute top-0 left-1/4 w-[500px] h-[500px] bg-amber-500/10 rounded-full blur-[120px] pointer-events-none" />
      <div className="absolute top-[60vh] right-10 w-[600px] h-[600px] bg-purple-600/10 rounded-full blur-[150px] pointer-events-none" />
      <div className="absolute bottom-10 left-10 w-[400px] h-[400px] bg-[#00E676]/5 rounded-full blur-[100px] pointer-events-none" />

      {/* Navigation Header */}
      <header className="sticky top-0 z-50 w-full glass-effect border-b border-white/5 backdrop-blur-md">
        <div className="max-w-7xl mx-auto px-6 h-18 flex items-center justify-between py-4">
          <div className="flex items-center space-x-3">
            <div className="w-10 h-10 rounded-xl bg-gradient-to-r from-amber-500 to-purple-600 flex items-center justify-center shadow-lg shadow-amber-500/10">
              <Zap className="w-5 h-5 text-black stroke-[2.5]" />
            </div>
            <div>
              <span className="text-xl font-black tracking-tight bg-gradient-to-r from-white to-neutral-400 bg-clip-text text-transparent">
                AidLoop
              </span>
              <span className="block text-[9px] font-bold text-amber-500 tracking-wider uppercase -mt-1">
                Hyperlocal Social Network
              </span>
            </div>
          </div>
          
          <nav className="hidden md:flex items-center space-x-8 text-sm font-semibold text-neutral-400">
            <a href="#features" className="hover:text-white transition-colors">Key Pillars</a>
            <a href="#loops" className="hover:text-white transition-colors">Pune Neighbourhoods</a>
            <a href="#demo" className="hover:text-white transition-colors">District Feed</a>
            <a href="#rules" className="hover:text-white transition-colors">Trust Shield</a>
          </nav>

          <div className="flex items-center space-x-4">
            <a 
              href="https://ai.studio/build" 
              target="_blank" 
              rel="noreferrer"
              className="px-5 py-2 rounded-xl text-xs font-bold text-neutral-300 border border-white/10 hover:border-white/20 transition-all flex items-center space-x-1"
            >
              <span>Build Studio</span>
              <ArrowUpRight className="w-3.5 h-3.5" />
            </a>
            <a 
              href="#download" 
              className="px-5 py-2.5 rounded-xl text-xs font-bold bg-amber-500 hover:bg-amber-400 text-black transition-all shadow-md shadow-amber-500/20 glow-amber flex items-center space-x-1.5"
            >
              <span>Install Launcher</span>
            </a>
          </div>
        </div>
      </header>

      {/* Hero Section */}
      <section className="relative pt-16 pb-20 max-w-7xl mx-auto px-6 text-center">
        {/* Localization Microtag */}
        <div className="inline-flex items-center space-x-2 bg-neutral-900 border border-white/10 px-3.5 py-1.5 rounded-full text-xs font-bold text-amber-500 mb-8 animate-pulse">
          <Globe className="w-3.5 h-3.5 text-amber-500" />
          <span>Active in Pune Enclaves</span>
        </div>

        {/* Catchy Visual Headline */}
        <h1 className="text-4xl sm:text-6xl md:text-7xl font-extrabold tracking-tight max-w-5xl mx-auto leading-[1.05] mb-6">
          Hyperlocal Mutual Aid. <br className="hidden sm:inline" />
          <span className="bg-gradient-to-r from-amber-400 via-amber-200 to-purple-400 bg-clip-text text-transparent">
            Synchronized in Real-Time.
          </span>
        </h1>

        <p className="text-lg text-neutral-400 max-w-3xl mx-auto font-medium leading-relaxed mb-10">
          Spark emergency community needs, trade resource listings, coordinate local discussions, and build neighborhood trust circles. Protected by an active Location Privacy Shield.
        </p>

        {/* Dual Actions CTAs */}
        <div className="flex flex-col sm:flex-row items-center justify-center gap-4 max-w-md mx-auto mb-20">
          <a
            href="#download"
            className="w-full sm:w-auto px-8 py-4 rounded-2xl bg-amber-500 hover:bg-amber-400 text-black font-extrabold text-sm transition-all shadow-xl shadow-amber-500/20 hover:-translate-y-0.5 duration-200 text-center flex items-center justify-center space-x-2"
          >
            <span>Download Android Client</span>
            <ArrowRight className="w-4 h-4" />
          </a>
          <a
            href="#demo"
            className="w-full sm:w-auto px-8 py-4 rounded-2xl border border-white/10 bg-white/5 hover:bg-white/10 font-bold text-sm transition-all text-neutral-300 hover:text-white hover:-translate-y-0.5 duration-200 text-center flex items-center justify-center"
          >
            Explore Active Feeds
          </a>
        </div>

        {/* Centered Desktop Simulated Portal Mockup */}
        <div id="demo" className="relative max-w-5xl mx-auto mt-8 border border-white/10 rounded-3xl overflow-hidden glass-card shadow-2xl p-6 text-left">
          <div className="absolute top-0 left-0 right-0 h-[1px] bg-gradient-to-r from-transparent via-white/15 to-transparent" />
          
          {/* Mock Top bar */}
          <div className="flex items-center justify-between border-b border-white/5 pb-4 mb-6">
            <div className="flex items-center space-x-2">
              <span className="w-3 h-3 rounded-full bg-red-500/60" />
              <span className="w-3 h-3 rounded-full bg-yellow-500/60" />
              <span className="w-3 h-3 rounded-full bg-green-500/60" />
              <span className="text-xs text-neutral-500 font-bold ml-2">AIDLOOP LIVE PORTAL DECK</span>
            </div>
            
            {/* Live Privacy Indicator widget */}
            <div className="inline-flex items-center bg-emerald-500/10 border border-emerald-500/20 px-3 py-1 rounded-full text-[10px] text-emerald-400 font-extrabold tracking-wider uppercase">
              <span className="w-2 h-2 rounded-full bg-emerald-500 mr-1.5 animate-ping" />
              Privacy Shield Active
            </div>
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            {/* Left side feed view */}
            <div className="lg:col-span-2 space-y-4">
              <div className="flex items-center justify-between">
                <h3 className="text-sm font-black tracking-wider text-amber-500 uppercase">
                  Pune Hyperlocal Feed
                </h3>
                <span className="text-xs text-neutral-500">Showing posts within 2.5km</span>
              </div>
              
              <div className="space-y-4">
                {mockFeeds.map((feed) => (
                  <div key={feed.id} className="p-4 rounded-2xl bg-white/[0.02] border border-white/5 hover:border-amber-500/20 transition-all">
                    {/* User identifier capsule */}
                    <div className="flex items-start justify-between mb-2">
                      <div className="flex items-center space-x-2.5">
                        <div className="w-8 h-8 rounded-full bg-gradient-to-tr from-purple-500 to-amber-500 flex items-center justify-center font-black text-xs text-black uppercase">
                          {feed.creator.charAt(0)}
                        </div>
                        <div>
                          <h4 className="text-xs font-bold text-white flex items-center">
                            {feed.creator}
                            <span className="text-[10px] text-neutral-500 font-medium ml-1.5">@{feed.username}</span>
                          </h4>
                          <p className="text-[9px] text-neutral-400">
                            📍 {feed.locality}, <span className="text-amber-500 font-semibold">{feed.area}</span>
                          </p>
                        </div>
                      </div>
                      
                      <div className="flex flex-wrap gap-1">
                        {feed.tags.map(t => (
                          <span key={t} className="px-2 py-0.5 rounded-md bg-white/5 border border-white/10 text-[9px] text-neutral-300 font-semibold">
                            {t}
                          </span>
                        ))}
                      </div>
                    </div>

                    <h5 className="text-xs sm:text-sm font-bold text-white mb-1.5">
                      {feed.title}
                    </h5>
                    <p className="text-xs text-neutral-400 leading-normal mb-3">
                      {feed.content}
                    </p>

                    {/* Meta bar */}
                    <div className="flex items-center justify-between text-[10px] text-neutral-500 font-bold border-t border-white/5 pt-2">
                      <span>🕒 {feed.time}</span>
                      <div className="flex items-center space-x-3">
                        <span className="hover:text-amber-500 transition-colors flex items-center gap-1 cursor-pointer">
                          💬 {feed.comments} Responses
                        </span>
                        <span className="hover:text-amber-500 transition-colors flex items-center gap-0.5 text-amber-400 cursor-pointer">
                          ⭐ {feed.stars} Stars
                        </span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Right side live stats panel */}
            <div className="space-y-6">
              {/* Privacy Shield Info Widget */}
              <div className="p-4 rounded-2xl bg-white/[0.03] border border-white/15">
                <div className="flex items-center space-x-2 text-amber-500 mb-2">
                  <Lock className="w-4 h-4" />
                  <h4 className="text-xs font-black tracking-wider uppercase">Distance Shielding</h4>
                </div>
                <p className="text-[11px] text-neutral-400 leading-normal">
                  Location anchors are processed with randomized spatial jittering. Nearby neighbors can request resources, coordinate aid, and view radar listings with strict zero precision leaks.
                </p>
                <div className="mt-3 bg-neutral-900 border border-white/5 rounded-xl h-12 flex items-center justify-center">
                  <span className="text-[10px] font-bold text-neutral-400">⚡ Location Precision Sealed</span>
                </div>
              </div>

              {/* Verified Loops Widget */}
              <div id="loops" className="p-4 rounded-2xl bg-white/[0.03] border border-white/15">
                <div className="flex items-center space-x-2 text-purple-400 mb-3">
                  <Users className="w-4 h-4" />
                  <h4 className="text-xs font-black tracking-wider uppercase">Active Pune Loops</h4>
                </div>
                
                <div className="space-y-2">
                  {availableLoops.map((loop) => (
                    <div 
                      key={loop.name} 
                      onClick={() => setActiveLoop(loop.name)}
                      className={`p-2.5 rounded-xl border text-left cursor-pointer transition-all ${
                        activeLoop === loop.name 
                          ? "bg-amber-500/10 border-amber-500" 
                          : "bg-white/[0.01] border-white/5 hover:border-white/10"
                      }`}
                    >
                      <div className="flex items-center justify-between">
                        <span className="text-xs font-black text-white">{loop.name}</span>
                        <span className="text-[9px] text-neutral-500 font-semibold">{loop.members}</span>
                      </div>
                      <p className="text-[10px] text-neutral-400 line-clamp-1">{loop.desc}</p>
                    </div>
                  ))}
                </div>
              </div>

              <div className="p-4 rounded-2xl bg-gradient-to-b from-amber-500/10 to-purple-500/10 border border-white/5 text-center">
                <span className="text-xs text-neutral-400 font-bold block mb-1">District Trust Index</span>
                <span className="text-2xl font-black text-amber-500 block">4.92 / 5.0</span>
                <span className="text-[9px] text-neutral-500 font-semibold uppercase">Based on 12K+ verified handshakes</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Pillars Section */}
      <section id="features" className="py-24 border-t border-b border-white/5 bg-white/[0.01] relative">
        <div className="max-w-7xl mx-auto px-6">
          <div className="text-center max-w-3xl mx-auto mb-16">
            <h2 className="text-3xl sm:text-5xl font-extrabold tracking-tight mb-4">
              Designed Around Absolute Trust.
            </h2>
            <p className="text-neutral-400 font-semibold text-sm">
              We ditched toxic algorithmic feeds for raw hyperlocal community coordination.
            </p>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
            {/* Pillar 1 */}
            <div className="p-6 rounded-3xl bg-neutral-900/60 border border-white/5 hover:border-amber-500/20 transition-all glow-amber">
              <div className="w-12 h-12 rounded-xl bg-amber-500/10 border border-amber-500/20 flex items-center justify-center mb-6">
                <Compass className="w-6 h-6 text-amber-500" />
              </div>
              <h3 className="text-lg font-extrabold text-white mb-2">Geolocation Radar</h3>
              <p className="text-sm text-neutral-400 leading-relaxed">
                Scan your local district for active mutual aid requests, items, and events happening nearby. Set custom enclave zones in Pune Neighborhoods easily.
              </p>
            </div>

            {/* Pillar 2 */}
            <div className="p-6 rounded-3xl bg-neutral-900/60 border border-white/5 hover:border-purple-500/20 transition-all glow-purple">
              <div className="w-12 h-12 rounded-xl bg-purple-500/10 border border-purple-500/20 flex items-center justify-center mb-6">
                <MessageSquare className="w-6 h-6 text-purple-400" />
              </div>
              <h3 className="text-lg font-extrabold text-white mb-2">Secure Direct Channels</h3>
              <p className="text-sm text-neutral-400 leading-relaxed">
                Coordinate pickup operations, drop-off timetables, and support messages safely with end-to-end chat channels built directly into our mobile dashboards.
              </p>
            </div>

            {/* Pillar 3 */}
            <div className="p-6 rounded-3xl bg-neutral-900/60 border border-white/5 hover:border-emerald-500/20 transition-all">
              <div className="w-12 h-12 rounded-xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center mb-6">
                <Award className="w-6 h-6 text-[#00E676]" />
              </div>
              <h3 className="text-lg font-extrabold text-white mb-2">Spark Stars & Reputation</h3>
              <p className="text-sm text-neutral-400 leading-relaxed">
                Accumulate community stars and point scores by fulfilling verified requests. Build authentic social proof and local credibility within your area loops.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Rules & Trust Shield */}
      <section id="rules" className="py-20 max-w-5xl mx-auto px-6">
        <div className="p-8 sm:p-12 rounded-3xl bg-gradient-to-r from-neutral-950 to-neutral-900 border border-white/10 relative overflow-hidden flex flex-col md:flex-row items-center justify-between gap-8">
          <div className="absolute top-0 right-0 w-[300px] h-[300px] bg-amber-500/5 rounded-full blur-[100px] pointer-events-none" />
          
          <div className="max-w-xl space-y-4">
            <div className="inline-flex items-center space-x-2 bg-amber-500/10 border border-amber-500/20 px-3 py-1 rounded-full text-xs font-bold text-amber-500">
              <ShieldCheck className="w-4 h-4" />
              <span>AidLoop Safety Guidelines</span>
            </div>
            <h2 className="text-2xl sm:text-4xl font-extrabold text-white tracking-tight">
              A Sanitized, Verified Local Space
            </h2>
            <p className="text-sm text-neutral-400 leading-relaxed">
              To keep our community safe, every request and dialogue goes through our strict moderation desk. Zero advertisement noise. Zero toxic posturing. Only authentic cooperation.
            </p>
            
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 pt-2 text-xs text-neutral-300 font-semibold">
              <div className="flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-amber-500" />
                <span>Zero tolerance for scams</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-amber-500" />
                <span>Strict content screening</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-amber-500" />
                <span>Verified neighborhood zones</span>
              </div>
              <div className="flex items-center space-x-2">
                <CheckCircle2 className="w-4 h-4 text-amber-500" />
                <span>Community-led moderation</span>
              </div>
            </div>
          </div>

          <div className="w-full md:w-auto flex-shrink-0 flex justify-center">
            <div className="p-6 rounded-2xl bg-white/[0.02] border border-white/5 space-y-3 w-full sm:w-[280px]">
              <span className="text-[10px] font-bold text-amber-500 uppercase tracking-widest block">Active Moderation stats</span>
              <div className="flex justify-between border-b border-white/5 pb-2">
                <span className="text-xs text-neutral-400">Total Flagged Posts</span>
                <span className="text-xs font-bold">14</span>
              </div>
              <div className="flex justify-between border-b border-white/5 pb-2">
                <span className="text-xs text-neutral-400">Approved Reports</span>
                <span className="text-xs font-bold text-[#00E676]">210</span>
              </div>
              <div className="flex justify-between">
                <span className="text-xs text-neutral-400">Response Speed</span>
                <span className="text-xs font-bold text-purple-400">&lt; 8 mins</span>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Launcher & Download Section */}
      <section id="download" className="py-20 text-center max-w-4xl mx-auto px-6 relative">
        <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[350px] h-[350px] bg-purple-500/10 rounded-full blur-[120px] pointer-events-none" />
        
        <Sparkles className="w-12 h-12 text-amber-500 mx-auto mb-6 stroke-[1.5]" />
        
        <h2 className="text-3xl sm:text-5xl font-black mb-4">
          Ready to Spark Local Support?
        </h2>
        
        <p className="text-neutral-400 font-medium max-w-2xl mx-auto mb-10 text-sm leading-relaxed">
          The Android application features a rich, glassmorphic layout, localized notifications, offline persistence, and seamless mobile radar scanning. Download our official release below to get started.
        </p>

        <div className="inline-flex flex-col sm:flex-row items-center gap-4 bg-neutral-900/80 border border-white/10 p-4 rounded-3xl max-w-md mx-auto w-full">
          <div className="text-left flex-1 px-2">
            <span className="text-[10px] text-neutral-500 uppercase font-black tracking-widest block">Active Stable Version</span>
            <span className="text-xs text-white font-bold block">aidloop-stable-v1.0.1.apk</span>
            <span className="text-[10px] text-amber-500 font-medium block">Build succeeded — verified safe</span>
          </div>
          
          <button 
            onClick={() => alert("Downloading AidLoop APK file...")}
            className="w-full sm:w-auto px-6 py-3.5 bg-gradient-to-r from-amber-500 to-amber-400 hover:from-amber-400 hover:to-amber-300 text-black font-extrabold text-xs rounded-2xl shadow-xl transition-all glow-amber"
          >
            Download APK
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-white/5 bg-[#0A0A0C] text-neutral-500 py-12 text-center text-xs">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row items-center justify-between gap-6">
          <div className="flex items-center space-x-2 text-left">
            <div className="w-8 h-8 rounded-lg bg-amber-500 flex items-center justify-center">
              <Zap className="w-4 h-4 text-black stroke-[2.5]" />
            </div>
            <div>
              <span className="text-sm font-bold text-white block">AidLoop</span>
              <span className="text-[9px] -mt-1 block">Hyperlocal Community Sync</span>
            </div>
          </div>
          
          <div className="flex gap-6 font-semibold">
            <a href="#features" className="hover:text-white transition-colors">Key Pillars</a>
            <a href="#loops" className="hover:text-white transition-colors">Neighbourhoods</a>
            <a href="#rules" className="hover:text-white transition-colors">Moderation Desk</a>
          </div>

          <p>© {new Date().getFullYear()} AidLoop Community Team. Built with Google AI Studio.</p>
        </div>
      </footer>

      {/* Floating Action Button (FAB) */}
      <div className="fixed bottom-6 right-6 z-50">
        <button
          onClick={() => alert("Hyperlocal Dispatch: To spark an active support loop or list emergency medical supplies, please install the official AidLoop Android App or launch the regional APK client.")}
          className="group px-4 py-3.5 sm:px-5 sm:py-4 rounded-full bg-gradient-to-tr from-amber-500 via-amber-400 to-amber-300 text-black font-black text-xs tracking-wider uppercase transition-all shadow-2xl shadow-amber-500/30 hover:shadow-amber-500/50 hover:-translate-y-1 hover:scale-105 duration-200 flex items-center gap-2 border border-black/10"
          aria-label="Spark Local Need"
        >
          <Plus className="w-5 h-5 stroke-[3]" />
          <span className="font-extrabold tracking-widest text-[11px] sm:inline">Spark Need</span>
        </button>
      </div>
    </div>
  );
}
