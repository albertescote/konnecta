import Foundation
import Supabase

struct PlanWithProfile: Codable {
    val user_id: String
    val status: String
    val comment: String?
    val profiles: Profile
}

struct MembershipWithProfile: Codable {
    val profiles: Profile
}

class AttendanceService {
    private let client = SupabaseManager.shared.client
    
    func getAttendance(weekendDate: String, groupId: String) async throws -> [PlanWithProfile] {
        return try await client.database
            .from("weekend_plans")
            .select(columns: "user_id, status, comment, profiles(id, full_name, avatar_url, email, updated_at)")
            .eq(column: "weekend_date", value: weekendDate)
            .eq(column: "group_id", value: groupId)
            .execute()
            .value
    }
    
    func getGroupMembers(groupId: String) async throws -> [Profile] {
        let memberships: [MembershipWithProfile] = try await client.database
            .from("group_memberships")
            .select(columns: "profiles(id, full_name, avatar_url, email, updated_at)")
            .eq(column: "group_id", value: groupId)
            .execute()
            .value
        return memberships.map { $0.profiles }
    }
}
