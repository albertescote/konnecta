package com.konnecta.app.data.remote

import com.konnecta.app.data.model.Group
import io.github.jan_tennert.supabase.postgrest.postgrest
import io.github.jan_tennert.supabase.postgrest.query.Columns
import kotlinx.serialization.Serializable

@Serializable
private data class MembershipWithGroup(
    val role: String,
    val groups: Group
)

class GroupService {
    private val client = SupabaseClient.client

    suspend fun getUserGroups(userId: String): List<Group> {
        return try {
            val memberships = client.postgrest["group_memberships"]
                .select(columns = Columns.raw("role, groups(*)")) {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<MembershipWithGroup>()
            
            memberships.map { it.groups.copy(role = it.role) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun joinGroupByToken(token: String, userId: String): Boolean {
        return try {
            // In a real app, you'd call a Supabase Function or a specific endpoint
            // Since we can't use the Admin client on the mobile device directly,
            // we assume there's an RPC or a public view for this.
            // For now, we mirror the logic of joining if we have the token.
            
            val group = client.postgrest["groups"]
                .select() {
                    filter {
                        eq("invite_token", token)
                    }
                }
                .decodeSingle<Group>()

            client.postgrest["group_memberships"]
                .insert(mapOf(
                    "group_id" to group.id,
                    "user_id" to userId,
                    "role" to "member"
                ))
            true
        } catch (e: Exception) {
            false
        }
    }
}
