import { createClient } from "@supabase/supabase-js";

export async function sendPushNotification({
  headings,
  contents,
  date,
  excludedUserId,
  groupId,
  playerIds: manualPlayerIds,
}: {
  headings: string;
  contents: string;
  date: string;
  excludedUserId?: string;
  groupId?: string;
  playerIds?: string[];
}) {
  if (process.env.SILENT_NOTIFICATIONS === "true") {
    console.log("Notifications are silenced (SILENT_NOTIFICATIONS=true)");
    return;
  }

  let playerIds = manualPlayerIds;

  if (!playerIds) {
    const supabase = createClient(
      process.env.NEXT_PUBLIC_SUPABASE_URL!,
      process.env.SUPABASE_SERVICE_ROLE_KEY!,
    );

    let query = supabase
      .from("profiles")
      .select("onesignal_id")
      .not("onesignal_id", "is", null)
      .not("id", "eq", excludedUserId || "");

    if (groupId) {
      // Fetch group name and member IDs
      const [{ data: groupData }, { data: memberIds }] = await Promise.all([
        supabase.from("groups").select("name").eq("id", groupId).single(),
        supabase.from("group_memberships").select("user_id").eq("group_id", groupId)
      ]);

      if (groupData) {
        headings = `[${groupData.name}] ${headings}`;
      }

      if (memberIds) {
        const ids = memberIds.map((m) => m.user_id);
        query = query.in("id", ids);
      }
    }

    const { data: profiles } = await query;

    if (!profiles || profiles.length === 0) return;

    playerIds = profiles.map((p) => p.onesignal_id);
  }

  if (!playerIds || playerIds.length === 0) return;

  try {
    const url = new URL("https://weekend-tracker-five.vercel.app");
    url.searchParams.set("date", date);
    if (groupId) url.searchParams.set("group_id", groupId);

    const response = await fetch("https://onesignal.com/api/v1/notifications", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Basic ${process.env.ONESIGNAL_REST_API_KEY}`,
      },
      body: JSON.stringify({
        app_id: process.env.NEXT_PUBLIC_ONESIGNAL_APP_ID,
        include_player_ids: playerIds,
        headings: { en: headings, ca: headings },
        contents: { en: contents, ca: contents },
        url: url.toString(),
      }),
    });
    return await response.json();
  } catch (e) {
    console.error("Error enviant notificació:", e);
  }
}
