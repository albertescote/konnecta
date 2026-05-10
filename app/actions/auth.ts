"use server";

import { createClient } from "@/lib/supabase/server";
import { redirect } from "next/navigation";

import { cookies } from "next/headers";

export async function signOut() {
  const supabase = await createClient();
  await supabase.auth.signOut();
  
  const cookieStore = await cookies();
  cookieStore.delete("konnecta_group_id");

  redirect("/login");
}
