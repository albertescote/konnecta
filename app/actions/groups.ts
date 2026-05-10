"use server";

import { cookies } from "next/headers";
import { revalidatePath } from "next/cache";

export async function setActiveGroup(groupId: string) {
  const cookieStore = await cookies();
  cookieStore.set("konnecta_group_id", groupId, {
    maxAge: 60 * 60 * 24 * 365, // 1 year
    path: "/",
  });
  revalidatePath("/");
}
