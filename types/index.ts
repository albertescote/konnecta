export type ActionResponse = {
  success: boolean;
  error?: string;
};

export type Group = {
  id: string;
  created_at: string;
  name: string;
  slug: string;
  invite_token?: string;
  created_by: string;
  description: string | null;
  // Convenience for UI
  role?: "admin" | "member";
};

export type GroupMembership = {
  group_id: string;
  user_id: string;
  role: "admin" | "member";
  joined_at: string;
};

export type GroupMembershipWithProfile = GroupMembership & {
  profiles: Profile;
};

export type Profile = {
  id: string;
  full_name: string | null;
  avatar_url: string | null;
  email: string;
  updated_at: string;
};

export type Activity = {
  id: string;
  created_at: string;
  title: string;
  description: string | null;
  group_id?: string;
  start_date?: string;
  end_date?: string | null;
  start_time: string | null;
  end_time?: string | null;
  creator_id: string;
  activity_participants?: ActivityParticipant[];
  // Legacy fields (required until refactor is complete)
  weekend_date: string;
  day_of_week: "divendres" | "dissabte" | "diumenge";
};

export type ActivityParticipant = {
  activity_id: string;
  user_id: string;
  additional_participants: number;
  profiles: Profile;
};

export type WeekendPlan = {
  id: string;
  user_id: string;
  group_id: string;
  weekend_date: string;
  status: "going" | "not_going" | "pending";
  comment: string | null;
  updated_at: string;
};
