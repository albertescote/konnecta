"use server";

import { cookies } from "next/headers";
import { revalidatePath } from "next/cache";
import { createClient } from "@/lib/supabase/server";
import { z } from "zod";
import { ActionResponse, GroupMembershipWithProfile } from "@/types";

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
});

function slugify(text: string) {
  return text
    .toString()
    .toLowerCase()
    .normalize("NFD") // Split accents from letters
    .replace(/[\u0300-\u036f]/g, "") // Remove accents
    .trim()
    .replace(/\s+/g, "-") // Replace spaces with -
    .replace(/[^\w-]+/g, "") // Remove all non-word chars
    .replace(/--+/g, "-"); // Replace multiple - with single -
}

export async function createGroup(formData: FormData): Promise<ActionResponse & { groupId?: string }> {
  try {
    const supabase = await createClient();
    const { data: { user } } = await supabase.auth.getUser();
    if (!user) return { success: false, error: "Sessió no iniciada" };

    const secret = formData.get("secret");
    const validSecret = process.env.GROUP_CREATION_SECRET;

    if (!validSecret || secret !== validSecret) {
      return { success: false, error: "Clau de creació incorrecta" };
    }

    const validatedData = CreateGroupSchema.safeParse({
      name: formData.get("name"),
    });

    if (!validatedData.success) {
      return { success: false, error: validatedData.error.issues[0].message };
    }

    const { name } = validatedData.data;
    let slug = slugify(name);

    // 1. Create the group
    const { data: group, error: groupError } = await supabase
      .from("groups")
      .insert({ name, slug, created_by: user.id })
      .select()
      .single();

    if (groupError) {
      if (groupError.code === "23505") {
        // Retry with a short random suffix if slug exists
        const randomSuffix = Math.random().toString(36).substring(2, 6);
        const { data: retryGroup, error: retryError } = await supabase
          .from("groups")
          .insert({ name, slug: `${slug}-${randomSuffix}`, created_by: user.id })
          .select()
          .single();
        
        if (retryError) throw retryError;
        if (retryGroup) {
          await setActiveGroup(retryGroup.id);
          return { success: true, groupId: retryGroup.id };
        }
      }
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

export async function getGroupMembers(groupId: string): Promise<{ success: boolean; data?: GroupMembershipWithProfile[]; error?: string }> {
  try {
    const supabase = await createClient();
    const { data, error } = await supabase
      .from("group_memberships")
      .select(`
        *,
        profiles (*)
      `)
      .eq("group_id", groupId);

    if (error) throw error;
    return { success: true, data: data as unknown as GroupMembershipWithProfile[] };
  } catch (e) {
    console.error("Error fetching members:", e);
    return { success: false, error: "Error al carregar els membres" };
  }
}

export async function updateMemberRole(groupId: string, userId: string, role: "admin" | "member"): Promise<ActionResponse> {
  try {
    const supabase = await createClient();
    const { error } = await supabase
      .from("group_memberships")
      .update({ role })
      .eq("group_id", groupId)
      .eq("user_id", userId);

    if (error) throw error;
    revalidatePath("/");
    return { success: true };
  } catch (e) {
    console.error("Error updating role:", e);
    return { success: false, error: "No s'ha pogut actualitzar el rol" };
  }
}

export async function removeMember(groupId: string, userId: string): Promise<ActionResponse> {
  try {
    const supabase = await createClient();
    const { error } = await supabase
      .from("group_memberships")
      .delete()
      .eq("group_id", groupId)
      .eq("user_id", userId);

    if (error) throw error;
    revalidatePath("/");
    return { success: true };
  } catch (e) {
    console.error("Error removing member:", e);
    return { success: false, error: "No s'ha pogut eliminar el membre" };
  }
}

export async function deleteGroup(groupId: string): Promise<ActionResponse> {
  try {
    const supabase = await createClient();
    const { error } = await supabase
      .from("groups")
      .delete()
      .eq("id", groupId);

    if (error) throw error;

    const cookieStore = await cookies();
    cookieStore.delete("konnecta_group_id");

    revalidatePath("/");
    return { success: true };
  } catch (e) {
    console.error("Error deleting group:", e);
    return { success: false, error: "No s'ha pogut esborrar el grup" };
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

    // 2. Add member
    const { error: joinError } = await supabase
      .from("group_memberships")
      .insert({ group_id: group.id, user_id: user.id, role: "member" });

    if (joinError) {
      if (joinError.code === "23505") {
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
