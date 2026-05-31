import {headers} from "next/headers";
import {createClient} from "@/lib/supabase/server";
import {Smartphone} from "lucide-react";

// Replace with your App Store listing URL once published
const APP_STORE_URL = "https://apps.apple.com/app/konnecta/idXXXXXXXXXX";

function isIOS(userAgent: string) {
  return /iphone|ipad|ipod/i.test(userAgent);
}

export default async function JoinPage({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = await params;
  const headersList = await headers();
  const userAgent = headersList.get("user-agent") ?? "";
  const ios = isIOS(userAgent);

  const supabase = await createClient();
  const { data: group } = await supabase
    .from("groups")
    .select("name, description")
    .eq("slug", token)
    .single();

  const canonicalUrl = `${process.env.NEXT_PUBLIC_SITE_URL ?? ""}/join/${token}`;

  if (!group) {
    return (
      <main className="min-h-screen flex flex-col items-center justify-center p-6 text-center">
        <link rel="canonical" href={canonicalUrl} />
        <h1 className="text-2xl font-black text-zinc-950 dark:text-white mb-2">
          Invitació no vàlida
        </h1>
        <p className="text-zinc-500">
          Aquest enllaç d&apos;invitació no és vàlid o ha caducat.
        </p>
        <a
          href="/"
          className="mt-8 px-8 py-3 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black"
        >
          TORNA A L&apos;INICI
        </a>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-background flex flex-col items-center justify-center p-6 animate-in fade-in duration-500">
      <link rel="canonical" href={canonicalUrl} />

      <div className="w-full max-w-sm bg-white dark:bg-zinc-900 rounded-[3rem] p-10 shadow-2xl border border-zinc-100 dark:border-zinc-800 text-center space-y-8">
        <div className="w-20 h-20 bg-blue-50 dark:bg-blue-900/30 rounded-full flex items-center justify-center text-blue-500 mx-auto">
          <Smartphone size={40} />
        </div>

        <div className="space-y-2">
          <p className="text-[10px] font-black uppercase tracking-[0.2em] text-blue-500">
            T&apos;han convidat a
          </p>
          <h1 className="text-3xl font-black text-zinc-950 dark:text-white tracking-tight leading-tight">
            {group.name}
          </h1>
          {group.description && (
            <p className="text-sm text-zinc-500 leading-relaxed italic">
              &quot;{group.description}&quot;
            </p>
          )}
        </div>

        {ios ? (
          <div className="space-y-3">
            <a
              href={`konnecta://join/${token}`}
              className="flex items-center justify-center gap-3 w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl hover:scale-[1.02] active:scale-95 transition-all"
            >
              OBRIR A KONNECTA
            </a>
            <a
              href={APP_STORE_URL}
              target="_blank"
              rel="noopener noreferrer"
              className="flex items-center justify-center gap-3 w-full py-4 bg-zinc-100 dark:bg-zinc-800 text-zinc-950 dark:text-white rounded-2xl font-black hover:scale-[1.02] active:scale-95 transition-all"
            >
              DESCARREGAR A L&apos;APP STORE
            </a>
          </div>
        ) : (
          <div className="space-y-4">
            <p className="text-sm text-zinc-500 leading-relaxed">
              Konnecta és una aplicació per a iOS. Obre aquest enllaç des del
              teu iPhone o iPad per unir-te al grup.
            </p>
            <div className="p-4 bg-zinc-50 dark:bg-zinc-800 rounded-2xl text-xs font-mono text-zinc-400 break-all">
              {canonicalUrl}
            </div>
          </div>
        )}
      </div>

      <footer className="mt-12 text-[10px] font-bold uppercase tracking-widest text-zinc-400">
        KONNECTA — L&apos;app dels teus findes
      </footer>
    </main>
  );
}
