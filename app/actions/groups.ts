"use server";

import { cookies } from "next/headers";
import { revalidatePath } from "next/cache";
import { createClient } from "@/lib/supabase/server";
import { z } from "zod";
import { ActionResponse } from "@/types";

export async function setActiveGroup(groupId: string) {
  const cookieStore = await cookies();
  cookieStore.set("konnecta_group_id", groupId, {
    maxAge: 60 * 60 * 24 * 365, // 1 year
    path: "/",
  });
  revalidatePath("/");
}

const CreateGroupSchema = z.object({
  name: z.string().min(3, "El nom ha de tenir almenys 3 caràcters"),
  slug: z.string().min(3, "L'identificador ha de tenir almenys 3 caràcters").regex(/^[a-z0-9-]+$/, "Només lletres minúscules, números i guions"),
});

export async function createGroup(formData: FormData): Promise<ActionResponse & { groupId?: string }> {
  try {
    const supabase = await createClient();
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return { success: false, error: "Sessió no iniciada" };

    const validatedData = CreateGroupSchema.safeParse({
      name: formData.get("name"),
      slug: formData.get("slug"),
    });

    if (!validatedData.success) {
      return { success: false, error: validatedData.error.issues[0].message };
    }

    const { name, slug } = validatedData.data;

    // 1. Create the group
    const { data: group, error: groupError } = await supabase
      .from("groups")
      .insert({ name, slug, created_by: user.id })
      .select()
      .single();

    if (groupError) {
      if (groupError.code === "23505") return { success: false, error: "Aquest identificador ja està en ús" };
      throw groupError;
    }

    // 2. Add the creator as an admin member
    const { error: memberError } = await supabase
      .from("group_memberships")
      .insert({ group_id: group.id, user_id: user.id, role: "admin" });

    if (memberError) throw memberError;

    // 3. Set as active group
    await setActiveGroup(group.id);

    return { success: true, groupId: group.id };
  } catch (e) {
    console.error("Error creating group:", e);
    return { success: false, error: "No s'ha pogut crear el grup" };
  }
}

export async function joinGroupBySlug(slug: string): Promise<ActionResponse> {
  try {
    const supabase = await createClient();
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return { success: false, error: "Sessió no iniciada" };

    // 1. Find the group
    const { data: group, error: groupError } = await supabase
      .from("groups")
      .select("id")
      .eq("slug", slug)
      .single();

    if (groupError || !group) return { success: false, error: "Grup no trobat" };

    // 2. Add member (RLS or UNIQUE constraint will handle duplicates)
    const { error: joinError } = await supabase
      .from("group_memberships")
      .insert({ group_id: group.id, user_id: user.id, role: "member" });

    if (joinError) {
      if (joinError.code === "23505") {
        // Already a member, just set active
        await setActiveGroup(group.id);
        return { success: true };
      }
      throw joinError;
    }

    // 3. Set as active group
    await setActiveGroup(group.id);

    return { success: true };
  } catch (e) {
    console.error("Error joining group:", e);
    return { success: false, error: "No s'ha pogut unir al grup" };
  }
}
