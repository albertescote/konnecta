import { createClient } from "@/lib/supabase/server";
import NotAuthorized from "@/components/NotAuthorized";
import { redirect } from "next/navigation";

export default async function NotAuthorizedPage() {
  const supabase = await createClient();
  const {
    data: { user },
  } = await supabase.auth.getUser();

  if (!user) {
    redirect("/login");
  }

  // Double check whitelist here to avoid manual navigation to this page by whitelisted users
  const email = user.email?.toLowerCase();
  const whitelistStr = process.env.WHITELIST_EMAILS || "";
  const whitelist = whitelistStr
    .split(",")
    .map((e) => e.trim().toLowerCase())
    .filter(Boolean);
  const isWhitelisted = email && whitelist.includes(email);

  if (isWhitelisted) {
    redirect("/");
  }

  return <NotAuthorized email={user.email!} />;
}
