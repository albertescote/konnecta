import { createClient, createAdminClient } from "@/lib/supabase/server";
import { joinGroupByToken } from "@/app/actions/groups";
import { redirect } from "next/navigation";
import { Users } from "lucide-react";

export default async function JoinPage({
  params,
}: {
  params: Promise<{ token: string }>;
}) {
  const { token } = await params;
  const supabase = await createClient();
  const { data: { user } } = await supabase.auth.getUser();

  // Fem servir createAdminClient per comprovar si el token és vàlid fins i tot si no estem loguejats
  // o si encara no som membres del grup (RLS).
  const adminSupabase = await createAdminClient();
  const { data: group } = await adminSupabase
    .from("groups")
    .select("name, description")
    .eq("invite_token", token)
    .single();

  if (!group) {
    return (
      <div className="min-h-screen flex flex-col items-center justify-center p-6 text-center">
        <h1 className="text-2xl font-black text-zinc-950 dark:text-white mb-2">Invitació no vàlida</h1>
        <p className="text-zinc-500">Aquest enllaç d&apos;invitació no existeix o ha caducat.</p>
        <a href="/" className="mt-8 px-8 py-3 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black">TORNA A L&apos;INICI</a>
      </div>
    );
  }

  // Si el token és vàlid però no estem loguejats, redirigim a login
  if (!user) {
    redirect(`/login?returnTo=/join/${token}`);
  }

  const handleJoin = async () => {
    "use server";
    await joinGroupByToken(token);
    redirect("/");
  };
...
        </div>

        <form action={handleJoin}>
          <button
            type="submit"
            className="w-full py-4 bg-zinc-900 dark:bg-zinc-100 text-white dark:text-zinc-900 rounded-2xl font-black shadow-xl hover:scale-[1.02] active:scale-95 transition-all"
          >
            UNIR-ME AL GRUP
          </button>
        </form>
      </div>
...
      
      <footer className="mt-12 text-[10px] font-bold uppercase tracking-widest text-zinc-400">
        KONNECTA - L&apos;app dels teus findes
      </footer>
    </main>
  );
}
