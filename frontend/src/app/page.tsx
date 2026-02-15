import UploadWidget from "@/components/UploadWidget";

export default function Home() {
  return (
    <main className="min-h-screen flex flex-col items-center justify-center p-4 font-mono">
      <div className="w-full max-w-2xl mb-8 flex justify-between items-end border-b-2 border-retro-dark pb-2">
        <div>
          <h1 className="text-4xl uppercase tracking-tighter">StreamForge</h1>
          <p className="text-xs text-retro-dark/60 mt-1">v0.0.1</p>
        </div>
        <div className="text-right text-xs hidden sm:block">
          <p>LOC: Wherever you need it to be</p>
          <p>SYS: ONLINE</p>
        </div>
      </div>

      <UploadWidget />

      <div className="mt-12 text-[10px] text-retro-dark/40 uppercase tracking-widest">
        © 2026 StreamForge Systems Inc. // All rights reserved
      </div>
    </main>
  )
}
