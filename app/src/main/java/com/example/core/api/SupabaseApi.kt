package com.example.core.api

import com.example.core.config.SupabaseConfig
import com.example.core.models.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*

// Request & Response DTOs for Supabase GoTrue Auth
data class SupabaseSignUpRequest(
  val email: String,
  val password: String,
  val data: Map<String, Any>? = null
)

data class SupabaseSignInRequest(
  val email: String,
  val password: String
)

data class SupabaseRecoverPasswordRequest(
  val email: String
)

data class SupabaseUserResponse(
  val id: String,
  val email: String?,
  val user_metadata: Map<String, Any>?
)

data class SupabaseAuthResponse(
  val access_token: String,
  val token_type: String,
  val expires_in: Long,
  val user: SupabaseUserResponse?
)

// DTOs for Database Tables matching the schema
data class DbConversation(
  val id: String? = null,
  val name: String,
  val description: String? = null,
  val is_group: Boolean = false,
  val avatar_url: String? = null,
  val invite_link: String? = null,
  val qr_code_text: String? = null,
  val created_by: String? = null
)

data class DbGroupMember(
  val id: String? = null,
  val conversation_id: String,
  val member_id: String,
  val role: String = "Verified Member",
  val is_muted: Boolean = false,
  val is_approved: Boolean = true
)

data class DbMessage(
  val id: String? = null,
  val conversation_id: String,
  val sender_id: String,
  val sender_name: String,
  val sender_role_icon: String = "🙏",
  val content: String,
  val type: String = "TEXT",
  val status: String = "READ",
  val replied_to_id: String? = null,
  val is_forwarded: Boolean = false,
  val is_pinned: Boolean = false,
  val is_starred: Boolean = false,
  val scheduled_time: String? = null
)

data class DbStatus(
  val id: String? = null,
  val member_id: String,
  val member_name: String,
  val type: String = "TEXT",
  val content: String? = null,
  val media_url: String? = null
)

data class DbBroadcast(
  val id: String? = null,
  val title: String,
  val content: String,
  val target_roles: List<String> = emptyList(),
  val sender_id: String? = null
)

data class DbTypingStatus(
  val conversation_id: String,
  val member_id: String,
  val is_typing: Boolean,
  val updated_at: String? = null
)

data class DbPresence(
  val member_id: String,
  val online_status: String,
  val last_seen: String? = null
)

interface SupabaseApi {

  // ==========================================
  // AUTH (GoTrue) ENDPOINTS
  // ==========================================
  @POST("auth/v1/signup")
  suspend fun signUp(
    @Body request: SupabaseSignUpRequest
  ): Response<SupabaseUserResponse>

  @POST("auth/v1/token?grant_type=password")
  suspend fun signInWithEmail(
    @Body request: SupabaseSignInRequest
  ): Response<SupabaseAuthResponse>

  @POST("auth/v1/recover")
  suspend fun recoverPassword(
    @Body request: SupabaseRecoverPasswordRequest
  ): Response<Unit>

  @GET("auth/v1/user")
  suspend fun getCurrentUser(
    @Header("Authorization") bearerToken: String
  ): Response<SupabaseUserResponse>


  // ==========================================
  // DATABASE (PostgREST) ENDPOINTS
  // ==========================================

  // 1. Conversations
  @GET("rest/v1/conversations?select=*")
  suspend fun getConversations(): Response<List<DbConversation>>

  @POST("rest/v1/conversations")
  @Headers("Prefer: return=representation")
  suspend fun createConversation(
    @Body conversation: DbConversation
  ): Response<List<DbConversation>>

  @PATCH("rest/v1/conversations")
  suspend fun updateConversation(
    @Query("id") filter: String,
    @Body updates: Map<String, String>
  ): Response<Unit>

  // 2. Group Members
  @GET("rest/v1/group_members?select=*")
  suspend fun getGroupMembers(
    @Query("conversation_id") conversationFilter: String? = null
  ): Response<List<DbGroupMember>>

  @POST("rest/v1/group_members")
  suspend fun addGroupMember(
    @Body member: DbGroupMember
  ): Response<Unit>

  @DELETE("rest/v1/group_members")
  suspend fun removeGroupMember(
    @Query("conversation_id") conversationFilter: String,
    @Query("member_id") memberFilter: String
  ): Response<Unit>

  // 3. Messages
  @GET("rest/v1/messages?select=*&order=timestamp.asc")
  suspend fun getMessages(
    @Query("conversation_id") conversationFilter: String
  ): Response<List<DbMessage>>

  @POST("rest/v1/messages")
  @Headers("Prefer: return=representation")
  suspend fun insertMessage(
    @Body message: DbMessage
  ): Response<List<DbMessage>>

  @PATCH("rest/v1/messages")
  suspend fun updateMessage(
    @Query("id") filter: String,
    @Body updates: Map<String, String>
  ): Response<Unit>

  @DELETE("rest/v1/messages")
  suspend fun deleteMessage(
    @Query("id") filter: String
  ): Response<Unit>

  // 4. Statuses
  @GET("rest/v1/status?select=*")
  suspend fun getStatuses(): Response<List<DbStatus>>

  @POST("rest/v1/status")
  suspend fun insertStatus(
    @Body status: DbStatus
  ): Response<Unit>

  // 5. Broadcasts
  @GET("rest/v1/broadcasts?select=*")
  suspend fun getBroadcasts(): Response<List<DbBroadcast>>

  @POST("rest/v1/broadcasts")
  suspend fun insertBroadcast(
    @Body broadcast: DbBroadcast
  ): Response<Unit>

  // 6. Typing Status
  @GET("rest/v1/typing_status?select=*")
  suspend fun getTypingStatuses(
    @Query("conversation_id") conversationFilter: String
  ): Response<List<DbTypingStatus>>

  @GET("rest/v1/typing_status?select=*")
  suspend fun getAllTypingStatuses(): Response<List<DbTypingStatus>>

  @POST("rest/v1/typing_status")
  @Headers("Prefer: resolution=merge-duplicates")
  suspend fun upsertTypingStatus(
    @Body typingStatus: DbTypingStatus
  ): Response<Unit>

  // 7. Presence
  @GET("rest/v1/presence?select=*")
  suspend fun getPresence(
    @Query("member_id") memberFilter: String
  ): Response<List<DbPresence>>

  @GET("rest/v1/presence?select=*")
  suspend fun getAllPresence(): Response<List<DbPresence>>

  @POST("rest/v1/presence")
  @Headers("Prefer: resolution=merge-duplicates")
  suspend fun upsertPresence(
    @Body presence: DbPresence
  ): Response<Unit>


  companion object {
    private var instance: SupabaseApi? = null

    fun get(): SupabaseApi {
      if (instance == null) {
        val logging = HttpLoggingInterceptor().apply {
          level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
          .addInterceptor(logging)
          .addInterceptor { chain ->
            val request = chain.request().newBuilder()
              .addHeader("apikey", SupabaseConfig.supabaseAnonKey)
              .addHeader("Authorization", "Bearer ${SupabaseConfig.supabaseAnonKey}")
              .build()
            chain.proceed(request)
          }
          .build()

        val moshi = Moshi.Builder()
          .addLast(KotlinJsonAdapterFactory())
          .build()

        var rawUrl = SupabaseConfig.supabaseUrl
        if (rawUrl.contains("/rest/v1")) {
          rawUrl = rawUrl.substringBefore("/rest/v1")
        }
        val baseUrl = if (rawUrl.endsWith("/")) {
          rawUrl
        } else {
          "$rawUrl/"
        }

        instance = Retrofit.Builder()
          .baseUrl(baseUrl)
          .client(client)
          .addConverterFactory(MoshiConverterFactory.create(moshi))
          .build()
          .create(SupabaseApi::class.java)
      }
      return instance!!
    }
  }
}
