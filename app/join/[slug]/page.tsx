import { createClient } from "@/lib/supabase/server";
import { joinGroupBySlug } from "@/app/actions/groups";
import { redirect } from "next/navigation";
import { Users, LogIn } from "lucide-react";

export default async function JoinPage({
  params,
}: {
  params: Promise<{ slug: string }>;
}) {
  const { slug } = await params;
  const supabase = await createClient();
  const { data: { user } } = await supabase.auth.getUser();

  // Fetch group details
  const { data: group } = await supabase
    .from("groups")
    .select("name, description")
    .eq("slug", slug)
    .single();

  if (!group) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-6 text-center">
        <h1 className="text-2xl font-black text-zinc-950 dark:text-white mb-2">Grup no trobat</h1>
        <p className="text-zinc-500">Aquest enllaç d&apos;invitació no és vàlid o ha caducat.</p>
        <a href="/" className="mt-8 px-8 py-3 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black">TORNA A L&apos;INICI</a>
      </div>
    );
  }

  const handleJoin = async () => {
    "use server";
    await joinGroupBySlug(slug);
    redirect("/");
  };

  return (
    <main className="min-h-screen bg-background flex flex-col items-center justify-center p-6 animate-in fade-in duration-500">
      <div className="w-full max-w-sm bg-white dark:bg-zinc-900 rounded-[3rem] p-10 shadow-2xl border border-zinc-100 dark:border-zinc-800 text-center space-y-8">
        <div className="w-20 h-20 bg-blue-50 dark:bg-blue-900/30 rounded-full flex items-center justify-center text-blue-500 mx-auto">
          <Users size={40} />
        </div>

        <div className="space-y-2">
          <p className="text-[10px] font-black uppercase tracking-[0.2em] text-blue-500">T&apos;han convidat a</p>
          <h1 className="text-3xl font-black text-zinc-950 dark:text-white tracking-tight leading-tight">{group.name}</h1>
          {group.description && (
            <p className="text-sm text-zinc-500 leading-relaxed italic">&quot;{group.description}&quot;</p>
          )}
        </div>

        {!user ? (
          <div className="space-y-4">
            <p className="text-sm text-zinc-400">Inicia sessió per acceptar la invitació i veure els plans del grup.</p>
            <a 
              href={`/login?returnTo=/join/${slug}`}
              className="flex items-center justify-center gap-3 w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl hover:scale-[1.02] active:scale-95 transition-all"
            >
              <LogIn size={20} />
              INICIA SESSIÓ
            </a>
          </div>
        ) : (
          <form action={handleJoin}>
            <button
              type="submit"
              className="w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl hover:scale-[1.02] active:scale-95 transition-all"
            >
              UNIR-ME AL GRUP
            </button>
          </form>
        )}
      </div>
      
      <footer className="mt-12 text-[10px] font-bold uppercase tracking-widest text-zinc-400">
        KONNECTA - L&apos;app dels teus findes
      </footer>
    </main>
  );
}
