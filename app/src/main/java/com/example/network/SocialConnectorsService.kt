package com.example.network

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit

// ---------------------------------------------------------------------------
// Domain Models for Facebook & Telegram Ingestion, Triage, and Dispatch
// ---------------------------------------------------------------------------

enum class SocialPlatform(val id: String, val label: String) {
    FACEBOOK("facebook", "Facebook"),
    TELEGRAM("telegram", "Telegram")
}

enum class TriageClassification(val wireName: String, val label: String) {
    ACTIONABLE_BLIND_ITEM("actionable_blind_item", "Actionable Blind Item"),
    VIRAL_DISINFORMATION("viral_disinformation", "Viral Disinformation"),
    LOCAL_DISPUTE_FRAMING("local_dispute_framing", "Local Dispute Framing"),
    EMERGING_GRIEVANCE("emerging_grievance", "Emerging Grievance"),
    LOW_SIGNAL_NOISE("low_signal_noise", "Low-Signal Noise")
}

enum class IncidentQueueStatus(val label: String) {
    PENDING_APPROVAL("Pending Approval"),
    DISPATCHING("Dispatching..."),
    DISPATCHED("Dispatched"),
    DISPATCH_ERROR("Dispatch Failed")
}

data class IncomingSocialSignal(
    val platform: SocialPlatform,
    val replyType: String,
    val targetId: String,
    val replyToMessageId: Long? = null,
    val senderId: String = "local_monitor",
    val senderName: String,
    val text: String
)

data class NarrativeTriageResult(
    val classification: TriageClassification,
    val viralityScore: Int,
    val fidelityScore: Int,
    val decodedSummary: String,
    val detectedKeywords: List<String>,
    val contextNote: String
)

data class CounterNarrativeDrafts(
    val communityReply: String,
    val officialClarification: String,
    val groundVerificationQuestion: String
)

data class NarrativeTriagePackage(
    val triage: NarrativeTriageResult,
    val drafts: CounterNarrativeDrafts,
    val isSamplePlaceholder: Boolean,
    val aiEngineNotice: String
)

data class NarrativeIncidentRecord(
    val id: String,
    val timestampIso: String,
    val signal: IncomingSocialSignal,
    val triagePackage: NarrativeTriagePackage,
    val status: IncidentQueueStatus = IncidentQueueStatus.PENDING_APPROVAL,
    val finalSentText: String? = null,
    val dispatchResultNote: String? = null
)

data class WebhookVerifyResult(
    val statusCode: Int,
    val challengeResponse: String?
)

// ---------------------------------------------------------------------------
// 1. Facebook Webhook Parser & Verification Handshake (facebookConnector.js)
// ---------------------------------------------------------------------------

object FacebookConnectorParser {

    fun verifyWebhookHandshake(
        mode: String?,
        verifyToken: String?,
        challenge: String?,
        expectedToken: String = BuildConfig.FB_VERIFY_TOKEN
    ): WebhookVerifyResult {
        val validSecret = expectedToken.isNotBlank()
        return if (mode == "subscribe" && validSecret && verifyToken == expectedToken) {
            WebhookVerifyResult(statusCode = 200, challengeResponse = challenge ?: "")
        } else {
            WebhookVerifyResult(statusCode = 403, challengeResponse = null)
        }
    }

    fun parseFacebookWebhookPayload(rawJson: String): List<IncomingSocialSignal> {
        val trimmed = rawJson.trim()
        if (!trimmed.startsWith("{")) return emptyList()
        val body = JSONObject(trimmed)
        if (!body.optString("object", "").equals("page", ignoreCase = true)) {
            return emptyList()
        }

        val extracted = mutableListOf<IncomingSocialSignal>()
        val entries = body.optJSONArray("entry") ?: return emptyList()

        for (i in 0 until entries.length()) {
            val entry = entries.optJSONObject(i) ?: continue

            // Page Feed posts & comments
            val changes = entry.optJSONArray("changes")
            if (changes != null) {
                for (c in 0 until changes.length()) {
                    val change = changes.optJSONObject(c) ?: continue
                    if (change.optString("field", "") == "feed") {
                        val value = change.optJSONObject("value") ?: continue
                        val message = value.optString("message", "").trim()
                        if (message.isNotEmpty()) {
                            val targetId = value.optString("comment_id", "")
                                .ifBlank { value.optString("post_id", "unknown_post") }
                            val from = value.optJSONObject("from")
                            val senderId = from?.optString("id", "unknown_user")
                                ?.ifBlank { "unknown_user" } ?: "unknown_user"
                            val senderName = from?.optString("name", "Facebook User")
                                ?.ifBlank { "Facebook User" } ?: "Facebook User"

                            extracted.add(
                                IncomingSocialSignal(
                                    platform = SocialPlatform.FACEBOOK,
                                    replyType = "comment",
                                    targetId = targetId,
                                    senderId = senderId,
                                    senderName = senderName,
                                    text = message
                                )
                            )
                        }
                    }
                }
            }

            // Messenger direct messages (ignore echoes)
            val messaging = entry.optJSONArray("messaging")
            if (messaging != null) {
                for (m in 0 until messaging.length()) {
                    val event = messaging.optJSONObject(m) ?: continue
                    val msgObj = event.optJSONObject("message") ?: continue
                    val isEcho = msgObj.optBoolean("is_echo", false)
                    val msgText = msgObj.optString("text", "").trim()
                    if (!isEcho && msgText.isNotEmpty()) {
                        val senderId = event.optJSONObject("sender")
                            ?.optString("id", "unknown_psid")
                            ?.ifBlank { "unknown_psid" } ?: "unknown_psid"

                        extracted.add(
                            IncomingSocialSignal(
                                platform = SocialPlatform.FACEBOOK,
                                replyType = "dm",
                                targetId = senderId,
                                senderId = senderId,
                                senderName = "PSID_$senderId",
                                text = msgText
                            )
                        )
                    }
                }
            }
        }
        return extracted
    }
}

// ---------------------------------------------------------------------------
// 2. Telegram Webhook Parser (telegramConnector.js)
// ---------------------------------------------------------------------------

object TelegramConnectorParser {

    fun parseTelegramUpdatePayload(rawJson: String): IncomingSocialSignal? {
        val trimmed = rawJson.trim()
        if (!trimmed.startsWith("{")) return null
        val update = JSONObject(trimmed)
        val msg = update.optJSONObject("message") ?: update.optJSONObject("channel_post") ?: return null
        val text = msg.optString("text", "").trim()
        if (text.isEmpty()) return null

        val chat = msg.optJSONObject("chat") ?: return null
        val chatId = chat.opt("id")?.toString()?.trim().orEmpty()
        if (chatId.isEmpty() || chatId == "null") return null

        val messageId = if (msg.has("message_id")) msg.optLong("message_id") else null
        val from = msg.optJSONObject("from")
        val senderId = from?.opt("id")?.toString()?.takeIf { it.isNotBlank() && it != "null" } ?: chatId
        val senderName = listOfNotNull(
            from?.optString("username", "")?.takeIf { it.isNotBlank() },
            from?.optString("first_name", "")?.takeIf { it.isNotBlank() },
            chat.optString("title", "")?.takeIf { it.isNotBlank() }
        ).firstOrNull() ?: "Telegram User"

        return IncomingSocialSignal(
            platform = SocialPlatform.TELEGRAM,
            replyType = "reply",
            targetId = chatId,
            replyToMessageId = messageId,
            senderId = senderId,
            senderName = senderName,
            text = text
        )
    }
}

// ---------------------------------------------------------------------------
// 3. Narrative AI Interface + Clearly Labeled Sample Implementation
//    (Firebase AI Logic was declined -> sample placeholder implementation used)
// ---------------------------------------------------------------------------

interface NarrativeAiRepository {
    val isFirebaseBacked: Boolean
    suspend fun triageAndDraft(signal: IncomingSocialSignal): NarrativeTriagePackage
}

class SampleNarrativeAiRepository : NarrativeAiRepository {
    override val isFirebaseBacked: Boolean = false

    private val priorityWatchlist = listOf(
        "resbak", "rambol", "hulidap", "ayuda", "blind item",
        "convoy", "flood", "rant", "alert", "road closed"
    )

    override suspend fun triageAndDraft(signal: IncomingSocialSignal): NarrativeTriagePackage {
        val lower = signal.text.lowercase(Locale.US)
        val matched = priorityWatchlist.filter { lower.contains(it) }

        val classification = when {
            lower.contains("blind item") || (lower.contains("who") && matched.isNotEmpty()) ->
                TriageClassification.ACTIONABLE_BLIND_ITEM
            lower.contains("resbak") || lower.contains("rambol") || lower.contains("rant") ->
                TriageClassification.LOCAL_DISPUTE_FRAMING
            lower.contains("hulidap") || lower.contains("ayuda") ->
                TriageClassification.EMERGING_GRIEVANCE
            signal.text.length > 120 || lower.contains("urgent") || lower.contains("share") ->
                TriageClassification.VIRAL_DISINFORMATION
            else -> TriageClassification.LOW_SIGNAL_NOISE
        }

        val virality = (4 + matched.size * 2 + if (signal.text.length > 100) 2 else 0).coerceIn(1, 10)
        val fidelity = when (classification) {
            TriageClassification.ACTIONABLE_BLIND_ITEM -> 6
            TriageClassification.EMERGING_GRIEVANCE -> 5
            TriageClassification.LOCAL_DISPUTE_FRAMING -> 4
            TriageClassification.VIRAL_DISINFORMATION -> 2
            TriageClassification.LOW_SIGNAL_NOISE -> 3
        }

        val kwNote = if (matched.isEmpty()) "none detected" else matched.joinToString(", ")

        return NarrativeTriagePackage(
            triage = NarrativeTriageResult(
                classification = classification,
                viralityScore = virality,
                fidelityScore = fidelity,
                decodedSummary = "[SAMPLE RESPONSE] Signal from ${signal.senderName} (${signal.platform.label}) " +
                    "analyzed deterministically under ${classification.label}. Watchlist hits: $kwNote.",
                detectedKeywords = matched.ifEmpty { listOf("baseline_chatter") },
                contextNote = "[SAMPLE] Enable Firebase AI Logic for live Gemini semantic blind-item decoding & faction neutral context."
            ),
            drafts = CounterNarrativeDrafts(
                communityReply = "[SAMPLE DRAFT] Mahalo for flagging this update. Local responders and community coordinators " +
                    "are verifying conditions on the ground directly—please avoid sharing unconfirmed names or rumors while official facts are confirmed.",
                officialClarification = "[SAMPLE CLARIFICATION] Current official feeds show no verified broader escalation matching this claim; " +
                    "verified advisories will be posted through official channels.",
                groundVerificationQuestion = "[SAMPLE CHECK] Can field monitors near ${signal.senderName}'s sector confirm exact time, location, and independent visual corroboration?"
            ),
            isSamplePlaceholder = true,
            aiEngineNotice = "AI features require Firebase AI Logic. Showing clearly labeled sample triage & counter-narrative responses."
        )
    }
}

// ---------------------------------------------------------------------------
// 4. Retrofit Facebook Graph API v21.0 & Telegram Bot API Dispatch Clients
// ---------------------------------------------------------------------------

data class FacebookDmRecipient(val id: String)
data class FacebookDmMessage(val text: String)
data class FacebookDmRequest(val recipient: FacebookDmRecipient, val message: FacebookDmMessage)
data class FacebookCommentRequest(val message: String)

data class TelegramSendMessageRequest(
    val chat_id: String,
    val text: String,
    val reply_to_message_id: Long? = null
)

interface FacebookGraphApi {
    @POST("v21.0/me/messages")
    suspend fun sendDirectMessage(
        @Query("access_token") accessToken: String,
        @Body payload: FacebookDmRequest
    ): ResponseBody

    @POST("v21.0/{targetId}/comments")
    suspend fun postCommentReply(
        @Path("targetId") targetId: String,
        @Query("access_token") accessToken: String,
        @Body payload: FacebookCommentRequest
    ): ResponseBody
}

interface TelegramBotApi {
    @POST("bot{token}/sendMessage")
    suspend fun sendMessage(
        @Path("token") token: String,
        @Body payload: TelegramSendMessageRequest
    ): ResponseBody
}

class SocialDispatchRepository(
    private val facebookApi: FacebookGraphApi = createDefaultFacebookApi(),
    private val telegramApi: TelegramBotApi = createDefaultTelegramApi(),
    private val fbTokenProvider: () -> String = { BuildConfig.FB_PAGE_ACCESS_TOKEN },
    private val tgTokenProvider: () -> String = { BuildConfig.TELEGRAM_BOT_TOKEN }
) {
    fun isFacebookConfigured(): Boolean {
        val tok = fbTokenProvider().trim()
        return tok.isNotEmpty() && tok != "MY_FB_PAGE_ACCESS_TOKEN"
    }

    fun isTelegramConfigured(): Boolean {
        val tok = tgTokenProvider().trim()
        return tok.isNotEmpty() && tok != "MY_TELEGRAM_BOT_TOKEN"
    }

    suspend fun dispatchReply(
        incident: NarrativeIncidentRecord,
        replyText: String
    ): Result<String> {
        val sig = incident.signal
        return when (sig.platform) {
            SocialPlatform.FACEBOOK -> {
                val token = fbTokenProvider().trim()
                if (!isFacebookConfigured()) {
                    Result.success("[DRY-RUN] FB_PAGE_ACCESS_TOKEN not configured in AI Studio Secrets. Payload ready for ${sig.replyType} -> ${sig.targetId}.")
                } else {
                    try {
                        val res = if (sig.replyType == "dm") {
                            facebookApi.sendDirectMessage(
                                accessToken = token,
                                payload = FacebookDmRequest(
                                    recipient = FacebookDmRecipient(sig.targetId),
                                    message = FacebookDmMessage(replyText)
                                )
                            )
                        } else {
                            facebookApi.postCommentReply(
                                targetId = sig.targetId,
                                accessToken = token,
                                payload = FacebookCommentRequest(replyText)
                            )
                        }
                        Result.success("Dispatched via Meta Graph v21.0 (${sig.replyType}): ${res.string().take(120)}")
                    } catch (e: Exception) {
                        Result.failure(IllegalStateException("Facebook Graph API Error: ${e.localizedMessage}"))
                    }
                }
            }

            SocialPlatform.TELEGRAM -> {
                val token = tgTokenProvider().trim()
                if (!isTelegramConfigured()) {
                    Result.success("[DRY-RUN] TELEGRAM_BOT_TOKEN not configured in AI Studio Secrets. Payload ready for chat ${sig.targetId}.")
                } else {
                    try {
                        val res = telegramApi.sendMessage(
                            token = token,
                            payload = TelegramSendMessageRequest(
                                chat_id = sig.targetId,
                                text = replyText,
                                reply_to_message_id = sig.replyToMessageId
                            )
                        )
                        Result.success("Dispatched via Telegram Bot API: ${res.string().take(120)}")
                    } catch (e: Exception) {
                        Result.failure(IllegalStateException("Telegram Bot API Error: ${e.localizedMessage}"))
                    }
                }
            }
        }
    }

    companion object {
        fun formatUtcTimestamp(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            return sdf.format(Date())
        }

        fun nextRecordId(): String = UUID.randomUUID().toString().take(8)

        fun createDefaultFacebookApi(): FacebookGraphApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            return Retrofit.Builder()
                .baseUrl("https://graph.facebook.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(FacebookGraphApi::class.java)
        }

        fun createDefaultTelegramApi(): TelegramBotApi {
            val client = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .build()
            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
            return Retrofit.Builder()
                .baseUrl("https://api.telegram.org/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(TelegramBotApi::class.java)
        }
    }
}
