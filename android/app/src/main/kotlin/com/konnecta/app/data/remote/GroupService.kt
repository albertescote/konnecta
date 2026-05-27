package com.konnecta.app.data.remote

import com.konnecta.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GroupService @Inject constructor() {
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
            Timber.e(e, "GroupService: Error fetching user groups")
            emptyList()
        }
    }

    suspend fun refreshInviteToken(groupId: String): Group? {
        return try {
            val newToken = UUID.randomUUID().toString()
            val expiresAt = Instant.now().plus(48, ChronoUnit.HOURS).toString()

            client.postgrest["groups"]
                .update(
                    mapOf(
                        "invite_token" to newToken,
                        "invite_token_expires_at" to expiresAt
                    )
                ) {
                    filter { eq("id", groupId) }
                    select()
                }
                .decodeSingleOrNull<Group>()
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error refreshing invite token")
            null
        }
    }

    suspend fun getGroupMembers(groupId: String): List<MembershipWithProfile> {
        return try {
            client.postgrest["group_memberships"]
                .select(columns = Columns.raw("*, profiles(*)")) {
                    filter {
                        eq("group_id", groupId)
                    }
                }
                .decodeList<MembershipWithProfile>()
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error fetching group members")
            emptyList()
        }
    }

    suspend fun joinGroupByToken(token: String, userId: String): Boolean {
        return try {
            val group = client.postgrest["groups"]
                .select {
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
            Timber.e(e, "GroupService: Error joining group by token")
            false
        }
    }

    suspend fun createGroup(userId: String, name: String, slug: String): Group? {
        return try {
            val group = client.postgrest["groups"]
                .insert(
                    mapOf(
                        "name" to name,
                        "slug" to slug,
                        "created_by" to userId,
                        "invite_token" to UUID.randomUUID().toString(),
                        "invite_token_expires_at" to Instant.now().plus(48, ChronoUnit.HOURS).toString()
                    )
                ) {
                    select()
                }
                .decodeSingle<Group>()

            client.postgrest["group_memberships"]
                .insert(
                    mapOf(
                        "group_id" to group.id,
                        "user_id" to userId,
                        "role" to "admin"
                    )
                )

            group
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error creating group")
            null
        }
    }

    suspend fun updateMemberRole(groupId: String, userId: String, role: String): Boolean {
        return try {
            client.postgrest["group_memberships"]
                .update(mapOf("role" to role)) {
                    filter {
                        eq("group_id", groupId)
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error updating member role")
            false
        }
    }

    suspend fun removeMember(groupId: String, userId: String): Boolean {
        return try {
            client.postgrest["group_memberships"]
                .delete {
                    filter {
                        eq("group_id", groupId)
                        eq("user_id", userId)
                    }
                }
            true
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error removing member")
            false
        }
    }

    suspend fun deleteGroup(groupId: String): Boolean {
        return try {
            client.postgrest["groups"]
                .delete {
                    filter {
                        eq("id", groupId)
                    }
                }
            true
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error deleting group")
            false
        }
    }

    suspend fun leaveGroup(groupId: String, userId: String): Boolean {
        return removeMember(groupId, userId)
    }

    suspend fun getGroupInviteToken(groupId: String): String? {
        return try {
            val group = client.postgrest["groups"]
                .select(columns = Columns.list("invite_token")) {
                    filter {
                        eq("id", groupId)
                    }
                }
                .decodeSingle<Map<String, String>>()
            group["invite_token"]
        } catch (e: Exception) {
            Timber.e(e, "GroupService: Error fetching invite token")
            null
        }
    }
}
