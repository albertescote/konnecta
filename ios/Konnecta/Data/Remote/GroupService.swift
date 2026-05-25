import Foundation
import Supabase

private struct MembershipWithGroup: Codable {
    val role: String
    val groups: Group
}

class GroupService {
    private let client = SupabaseManager.shared.client
    
    func getUserGroups(userId: String) async -> [Group] {
        do {
            let memberships: [MembershipWithGroup] = try await client.database
                .from("group_memberships")
                .select(columns: "role, groups(*)")
                .eq(column: "user_id", value: userId)
                .execute()
                .value
            
            return memberships.map { 
                var g = $0.groups
                g.role = $0.role
                return g
            }
        } catch {
            print("Error fetching groups: \(error)")
            return []
        }
    }
    
    func joinGroupByToken(token: String, userId: String) async -> Bool {
        do {
            let group: Group = try await client.database
                .from("groups")
                .select()
                .eq(column: "invite_token", value: token)
                .single()
                .execute()
                .value
            
            try await client.database
                .from("group_memberships")
                .insert(values: [
                    "group_id": group.id,
                    "user_id": userId,
                    "role": "member"
                ])
                .execute()
            
            return true
        } catch {
            print("Error joining group: \(error)")
            return false
        }
    }
}
