package com.example.model

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ---------------------------------------------------------------------------
// 1. Mandatory Startup Role-Selection Gateway & Organizational Affiliation
// ---------------------------------------------------------------------------

enum class BtamOperationalRole(
    val id: String,
    val title: String,
    val subtitle: String,
    val clearanceSummary: String
) {
    PUBLIC_BYSTANDER(
        id = "public_bystander",
        title = "Public / Bystander",
        subtitle = "Report observable concerning behavior or access violence-prevention resources",
        clearanceSummary = "5-stage NTAC intake, anonymous case token lookup & 911 emergency guidance"
    ),
    BTAM_INVESTIGATOR(
        id = "btam_investigator",
        title = "BTAM Investigator",
        subtitle = "Conduct structured behavioral intake review and case triage for an affiliated organization",
        clearanceSummary = "Affiliated intake queue, anonymous follow-up prompts & dashboard access"
    ),
    TEAM_LEAD(
        id = "team_lead",
        title = "Multidisciplinary BTAM Team Lead",
        subtitle = "Coordinate cross-functional case management (HR, security, legal, clinical/support services) and escalation decisions",
        clearanceSummary = "Cross-functional escalation sign-off, team coordination & full command workspace"
    ),
    COMPLIANCE_AUDITOR(
        id = "compliance_auditor",
        title = "Compliance & Privacy Auditor",
        subtitle = "Review PII-redaction logs, token integrity, and anti-profiling compliance",
        clearanceSummary = "PII-redaction audit trail, token verification & anti-profiling compliance checks"
    )
}

enum class BtamAffiliationPathway(
    val id: String,
    val label: String,
    val description: String
) {
    AFFILIATED(
        id = "affiliated",
        label = "Participating Organization",
        description = "Workplace, school, or venue enrolled in VeriScope BTAM — routes directly to authorized BTAM Investigator queue."
    ),
    UNAFFILIATED_PUBLIC(
        id = "unaffiliated_public",
        label = "Unaffiliated Public Pathway",
        description = "Strips all PII, archives non-emergency reports in the Volunteer Prevention Pool & shows U.S. Secret Service NTAC resources."
    )
}

// ---------------------------------------------------------------------------
// 2. Public / Bystander 5-Stage NTAC Workflow Models
// ---------------------------------------------------------------------------

enum class BtamBystanderStage(val stepNumber: Int, val label: String) {
    STAGE_1_LIFE_SAFETY(1, "Life-Safety"),
    STAGE_2_AFFILIATION(2, "Affiliation"),
    STAGE_3_BEHAVIORAL_INTAKE(3, "Indicators"),
    STAGE_4_URGENCY_PACKAGE(4, "Triage Brief"),
    STAGE_5_CASE_TOKEN(5, "Case Token")
}

enum class BtamUrgencyTier(
    val tierNumber: Int,
    val label: String,
    val guidance: String
) {
    EMERGENCY(
        tierNumber = 1,
        label = "Tier 1: Emergency",
        guidance = "Time-critical indicators of imminent harm requiring immediate 911 contact and emergency human responder escalation."
    ),
    PRIORITY(
        tierNumber = 2,
        label = "Tier 2: Priority",
        guidance = "Corroborated leakage, fixation, or pathway progression requiring rapid multidisciplinary BTAM team assessment."
    ),
    STANDARD(
        tierNumber = 3,
        label = "Tier 3: Standard",
        guidance = "Early-stage concerning behavior logged for baseline context, supportive outreach evaluation, and ongoing monitoring."
    )
}

enum class NtacIndicatorCategory(
    val id: String,
    val title: String,
    val description: String
) {
    LEAKAGE_COMMUNICATIONS(
        id = "leakage",
        title = "Leakage & Communications",
        description = "Direct, veiled, or conditional threats; written, verbal, or digital statements signaling intent or fixation."
    ),
    GRIEVANCE_FIXATION(
        id = "grievance",
        title = "Escalating Grievance & Fixation",
        description = "Targeting of specific individuals, groups, or sites; escalating hostility or perceived injustice."
    ),
    PROBES_PREATTACK(
        id = "pre_attack",
        title = "Pre-Attack / Probing Behaviors",
        description = "Unusual surveillance, boundary testing, rehearsal, or sudden acquisition/display of weapons."
    ),
    ACUTE_STRESSORS(
        id = "stressors",
        title = "Acute Stressors & Context",
        description = "Recent destabilizing events or emotional distress logged strictly for supportive, non-punitive evaluation (non-clinical)."
    )
}

data class BtamFollowUpMessage(
    val id: String,
    val senderRole: String,
    val timestampUtc: String,
    val body: String
)

data class BtamCaseSubmission(
    val token: String,
    val createdAtUtc: String,
    val urgencyTier: BtamUrgencyTier,
    val isLifeSafetyEmergency: Boolean,
    val pathway: BtamAffiliationPathway,
    val organizationName: String,
    val indicators: Set<NtacIndicatorCategory>,
    val leakageSummary: String,
    val grievanceSummary: String,
    val probingSummary: String,
    val contextAndArtifacts: String,
    val observationDateTimeLocation: String,
    val witnessCountNote: String,
    val statusLabel: String,
    val assignedDisciplines: Set<String> = emptySet(),
    val humanAuthorityNote: String = "Awaiting human BTAM review (human-in-the-loop governance active).",
    val piiScrubApplied: Boolean,
    val piiRedactionCount: Int,
    val distressSupportFlagged: Boolean,
    val followUpMessages: List<BtamFollowUpMessage> = emptyList()
)

data class BtamPiiAuditLogEntry(
    val id: String,
    val token: String,
    val timestampUtc: String,
    val pathway: BtamAffiliationPathway,
    val redactedCategories: List<String>,
    val redactionCount: Int,
    val antiProfilingPassed: Boolean
)

// ---------------------------------------------------------------------------
// 3. Cryptographic Token, PII Minimization, Anti-Profiling & 911 Dialer Helpers
// ---------------------------------------------------------------------------

object BtamTokenGenerator {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val secureRandom = SecureRandom()

    fun generateCaseToken(): String {
        fun randomBlock(len: Int): String = buildString(len) {
            repeat(len) {
                append(ALPHABET[secureRandom.nextInt(ALPHABET.length)])
            }
        }
        return "VS-BTAM-${randomBlock(4)}-${randomBlock(4)}"
    }

    fun formatUtcNow(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
    }
}

object BtamPiiScrubber {
    data class ScrubResult(
        val cleanedText: String,
        val redactionCount: Int,
        val redactedCategories: List<String>
    )

    private val emailRegex = Regex("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}")
    private val phoneRegex = Regex("(\\+?1[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}")
    private val handleRegex = Regex("(?<!\\w)@[A-Za-z0-9_]{2,32}")
    private val ssnRegex = Regex("\\b\\d{3}-\\d{2}-\\d{4}\\b")
    private val namedPrefixRegex = Regex(
        "\\b(?:my name is|named|mr\\.|mrs\\.|ms\\.|reported by|contact)\\s+[A-Z][a-z]{1,20}(?:\\s+[A-Z][a-z]{1,20})?",
        RegexOption.IGNORE_CASE
    )
    private val deviceMetadataRegex = Regex(
        "\\b(?:IMEI|UDID|MAC|IPv4|IP)\\s*[:=]\\s*[A-Za-z0-9.:-]+",
        RegexOption.IGNORE_CASE
    )

    fun scrub(rawText: String): ScrubResult {
        var text = rawText.trim()
        if (text.isEmpty()) return ScrubResult("", 0, emptyList())

        var count = 0
        val categories = linkedSetOf<String>()

        fun applyRule(regex: Regex, replacement: String, category: String) {
            val matches = regex.findAll(text).count()
            if (matches > 0) {
                count += matches
                categories.add(category)
                text = regex.replace(text, replacement)
            }
        }

        applyRule(emailRegex, "[REDACTED-EMAIL]", "Email Address")
        applyRule(phoneRegex, "[REDACTED-PHONE]", "Phone Number")
        applyRule(ssnRegex, "[REDACTED-ID]", "Government ID")
        applyRule(handleRegex, "[REDACTED-HANDLE]", "Social Handle")
        applyRule(deviceMetadataRegex, "[REDACTED-DEVICE-META]", "Device Metadata")
        applyRule(namedPrefixRegex, "[REDACTED-NAME]", "Personal Name")

        return ScrubResult(
            cleanedText = text,
            redactionCount = count,
            redactedCategories = categories.toList()
        )
    }
}

object BtamAntiProfilingGuard {
    const val NTAC_OFFICIAL_URL = "https://www.secretservice.gov/protection/ntac"

    private val protectedCharacteristicTerms = listOf(
        "race", "racial", "ethnicity", "ethnic", "nationality", "national origin",
        "religion", "muslim", "christian", "jewish", "hindu", "buddhist",
        "sexual orientation", "gender identity", "disability", "political affiliation"
    )

    fun containsProtectedProfilingFocus(text: String): Boolean {
        val lower = text.lowercase(Locale.US)
        return protectedCharacteristicTerms.any { term ->
            Regex("\\b${Regex.escape(term)}\\b").containsMatchIn(lower)
        }
    }

    fun evaluateComplianceNote(texts: List<String>): Pair<Boolean, String> {
        val flagged = texts.any { containsProtectedProfilingFocus(it) }
        return if (flagged) {
            Pair(
                false,
                "Anti-Profiling Guardrail: Remove protected demographic attributes; triage must cite observable behaviors, communications, and situational context only."
            )
        } else {
            Pair(
                true,
                "Verified: Report relies strictly on observable NTAC behavioral indicators with zero protected-characteristic profiling."
            )
        }
    }
}

object EmergencyDialerBridge {
    /**
     * Opens the device's default Android mobile Phone/Dialer pre-filled with 911 using ACTION_DIAL.
     * ACTION_DIAL requires zero dangerous permissions and prevents background silent dialing.
     */
    fun openDefaultDialerFor911(context: Context): Result<String> = runCatching {
        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:911")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(dialIntent)
        "Android Phone dialer opened with 911 pre-filled."
    }
}
