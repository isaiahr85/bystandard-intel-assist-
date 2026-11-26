package com.example.bystander

import androidx.compose.runtime.Immutable

enum class BystanderSideMenuOption(
    val number: Int,
    val title: String,
    val shortSubtitle: String
) {
    SUBMIT_CONCERN(1, "Submit Community Concern", "Structured observable incident & clarification form"),
    REFER_ASSISTANCE(2, "Refer Individual for Assistance", "Voluntary supportive care & non-punitive referral"),
    REQUEST_WELFARE_CHECK(3, "Request Welfare Check", "Well-being outreach & crisis resource coordination"),
    CLARIFY_ANOMALY(4, "Clarify Observed Anomaly", "7 public examples & non-accusatory clarification"),
    COMMUNITY_RESOURCE_REFERRAL(5, "Community Resource Referral", "211, 988, behavioral health, school & tribal aid"),
    PUBLIC_SAFETY_LIBRARY(6, "Public Safety Education Library", "Plain-language BTAM prevention principles"),
    NTAC_LEARNING_CENTER(7, "NTAC Learning Center", "U.S. Secret Service NTAC guides & summaries"),
    TOWN_HALL_RESOURCES(8, "Town Hall Resources", "Discussion guides for churches, schools & elders"),
    REPORTING_FAQS(9, "Reporting FAQs", "Privacy, civil liberties & misuse safeguards"),
    ACKNOWLEDGEMENT_REVIEW(10, "Acknowledgement & Submission Review", "Mandatory certification, 4-tier routing & ledger")
}

enum class ObservationSourceType(val id: String, val label: String) {
    DIRECTLY_OBSERVED("direct", "Directly Observed"),
    HEARD_FROM_ANOTHER("second_hand", "Heard from Another Person"),
    ONLINE_OBSERVATION("online", "Online Observation"),
    COMMUNITY_RUMOR("rumor", "Community Rumor"),
    UNVERIFIED_INFO("unverified", "Unverified Information"),
    REQUESTING_HELP_UNDERSTANDING("help_understanding", "Requesting Assistance Understanding Situation")
}

enum class ConfidenceScaleLevel(
    val level: Int,
    val label: String,
    val description: String
) {
    LIMITED(1, "1 = Limited information", "Single ambiguous cue needing context or clarification."),
    POSSIBLE(2, "2 = Possible concern", "Early observable indicator warranting supportive check-in."),
    MULTIPLE(3, "3 = Multiple observations", "Repeated or corroborated behaviors across settings."),
    SIGNIFICANT(4, "4 = Significant concern", "Escalating distress, fixation, or pathway progression."),
    IMMEDIATE(5, "5 = Immediate safety concern", "Time-critical hazard — call 911 immediately if imminent threat.")
}

enum class ValidationChecklistItem(val id: String, val statement: String) {
    OBJECTIVE_OBSERVATIONS("obj", "I have provided objective observations."),
    UNCERTAINTY_EXISTS("uncert", "I understand uncertainty exists."),
    NO_CRIMINAL_ACCUSATION("no_accuse", "I am not making a criminal accusation."),
    REQUIRES_CLARIFICATION("clarify", "I understand this report may require clarification."),
    ADDITIONAL_VERIFICATION("verify", "I understand additional verification may be necessary."),
    NO_INTENT_CONCLUSION("no_intent", "I understand behavioral concerns alone do not establish intent."),
    PROFESSIONALS_EVALUATE("prof_eval", "I understand that professionals evaluate context before conclusions are reached.")
}

enum class CoordinationTier(
    val tierNumber: Int,
    val title: String,
    val description: String
) {
    TIER_1_CLARIFICATION(
        1,
        "Tier 1: Community review and clarification",
        "Neutral verification of context, dates, and ambiguity without accusation."
    ),
    TIER_2_SCREENING(
        2,
        "Tier 2: Behavioral concern screening",
        "Multidisciplinary review of observable patterns using BTAM prevention standards."
    ),
    TIER_3_RESOURCE_REFERRAL(
        3,
        "Tier 3: Referral to appropriate community resources",
        "Connects individuals with voluntary local support, crisis care, or school/workplace help."
    ),
    TIER_4_AUTHORIZED_ESCALATION(
        4,
        "Tier 4: Escalation when legally authorized and appropriate",
        "Strict legal, policy, and evidentiary standard verification prior to authorized agency escalation."
    )
}

enum class CoordinationPartner(val title: String, val scope: String) {
    LOCAL_SUPPORT("Local support services", "211 navigation, housing stability, food & family assistance"),
    CRISIS_RESOURCES("Crisis resources", "988 Lifeline, mobile crisis response & immediate de-escalation"),
    BEHAVIORAL_HEALTH("Behavioral health professionals", "Licensed counseling, trauma support & voluntary clinical care"),
    SCHOOL_TEAMS("School support teams", "Counselors, MTSS multidisciplinary teams & trusted staff"),
    WORKPLACE_SUPPORT("Workplace support resources", "HR employee assistance (EAP), ombuds & mediation"),
    COMMUNITY_ORGS("Community-based organizations", "Churches, tribal councils, elder centers & neighborhood hubs"),
    PUBLIC_SAFETY_AGENCIES("Lawfully authorized public safety agencies", "Engaged strictly when legal, policy & evidentiary standards are met")
}

@Immutable
data class NtacPublicationResource(
    val id: String,
    val title: String,
    val publishedNote: String,
    val url: String,
    val plainSummary: String,
    val elderFriendlyTakeaway: String
)

@Immutable
data class CommunityFaqEntry(
    val id: String,
    val question: String,
    val answer: String
)

@Immutable
data class BystanderConcernSubmission(
    val id: String,
    val referenceCode: String,
    val workflowCategory: BystanderSideMenuOption,
    val submittedAtUtc: String,
    val whatObserved: String,
    val dateAndTime: String,
    val location: String,
    val infoSourceDetail: String,
    val isDirectObservation: Boolean,
    val supportingEvidence: String,
    val knownContext: String,
    val clarificationReason: String,
    val specificConcernBehavior: String,
    val selectedSourceTypes: Set<ObservationSourceType>,
    val confidenceLevel: ConfidenceScaleLevel,
    val assignedTier: CoordinationTier,
    val recommendedPartners: List<CoordinationPartner>,
    val piiRedactedCount: Int,
    val statusNote: String
)

object BystanderPortalConstants {
    const val PORTAL_TITLE = "BYSTANDER REFERRALS & COMMUNITY CONCERN REPORTING"

    const val ANTI_SPECULATION_WARNING =
        "Unverified assumptions, personal disagreements, political disputes, rumors, stereotypes, protected characteristics, or unsupported accusations should not be submitted."

    const val CLARIFICATION_REASSURANCE =
        "Reporting an observation does not mean a person has committed wrongdoing. Many situations are resolved through clarification, support, referral, and intervention."

    val MANDATORY_ACKNOWLEDGEMENT_PARAGRAPHS: List<String> = listOf(
        "I understand that this reporting portal supports prevention, clarification, support, and responsible information sharing.",
        "I understand that observable behaviors and facts are more valuable than assumptions.",
        "I understand that reporting does not establish guilt, intent, threat level, or wrongdoing.",
        "I acknowledge that I have been provided access to U.S. Secret Service National Threat Assessment Center educational materials and am encouraged to review at least one available publication before submitting information.",
        "I understand that false, knowingly misleading, harassing, retaliatory, discriminatory, or malicious reports may be rejected.",
        "I certify that the information provided is accurate to the best of my knowledge."
    )

    val BTAM_EDUCATION_POINTS: List<Pair<String, String>> = listOf(
        "BTAM is a prevention process." to "It aims to spot distress or safety challenges early so problems can be solved peacefully before harm happens.",
        "BTAM focuses on behaviors, not labels." to "Reviewers look at what someone specifically said or did—never stereotypes, race, religion, politics, or personal identity.",
        "BTAM seeks understanding, support, and intervention." to "The main goal is helping people get counseling, family aid, medical care, or mediation—not punishing them.",
        "BTAM uses multidisciplinary review." to "Educators, counselors, community leaders, HR, and authorized safety advisors compare context together so no single person guesses alone.",
        "Concerning behavior does not automatically mean violence." to "Stress, grief, confusion, or frustration often looks alarming but needs care and clarification rather than law enforcement.",
        "Early reporting allows support resources to be offered sooner." to "Speaking up respectfully gives neighbors, elders, students, and coworkers timely support."
    )

    val CLARIFYING_ANOMALY_EXAMPLES: List<Pair<String, String>> = listOf(
        "Sudden major behavioral change" to "A usually steady neighbor, student, or coworker abruptly stops attending routines, withdraws, or shows sharp agitation.",
        "Repeated expressions of hopelessness" to "Statements about feeling trapped, having no future, or giving away meaningful possessions.",
        "Escalating conflicts" to "Repeated unresolved disputes at work, school, or in neighborhood spaces where tensions keep rising.",
        "Concerning communications" to "Ambiguous posts, notes, or verbal remarks mentioning revenge, weapons, or 'making people pay' that need calm context review.",
        "Observable signs of distress" to "Visible exhaustion, confusion, severe isolation, or inability to manage daily basic needs.",
        "Requests for help" to "Direct or indirect hints that an individual feels overwhelmed and does not know where to turn.",
        "Community safety concerns" to "Unusual boundary testing, repeated trespassing, or unverified rumors alarming local families or elders."
    )

    val NTAC_PUBLICATIONS: List<NtacPublicationResource> = listOf(
        NtacPublicationResource(
            id = "ntac_hub",
            title = "Official NTAC Resource Hub",
            publishedNote = "U.S. Secret Service National Threat Assessment Center (Educational Reference — Independent Non-Affiliated Access)",
            url = "https://www.secretservice.gov/protection/ntac",
            plainSummary = "Central public library of research studies, operational guides, and bystander awareness tools focused on preventing targeted violence before it occurs.",
            elderFriendlyTakeaway = "Free public guides teaching families, churches, and communities how to notice warning signs early and connect people to help."
        ),
        NtacPublicationResource(
            id = "ntac_school_bystander_2023",
            title = "Improving School Safety Through Bystander Reporting (May 2023)",
            publishedNote = "CISA / USSS NTAC Bystander Reporting Toolkit (May 2023)",
            url = "https://schoolshooters.info/sites/default/files/cisa-usss-k-12-bystander-reporting-toolkit-508_final_0.pdf",
            plainSummary = "Explains why students, teachers, and neighbors often notice concerning behavior first and how trusted, supportive reporting channels remove fear of 'snitching'.",
            elderFriendlyTakeaway = "Shows how safe, confidential reporting lets schools help struggling young people early instead of waiting for a crisis."
        ),
        NtacPublicationResource(
            id = "ntac_what_is_bta",
            title = "What Is a Behavioral Threat Assessment in School? (NTAC)",
            publishedNote = "USSS NTAC Public Education Overview",
            url = "https://www.secretservice.gov/protection/ntac",
            plainSummary = "Clarifies that behavioral threat assessment is a fact-finding, supportive team approach—not profiling or zero-tolerance punishment.",
            elderFriendlyTakeaway = "Explains in simple words that a team checks facts kindly and fairly rather than jumping to conclusions."
        ),
        NtacPublicationResource(
            id = "ntac_units_2024",
            title = "Behavioral Threat Assessment Units: A Guide for State and Local Law Enforcement to Prevent Targeted Violence (2024)",
            publishedNote = "USSS NTAC Law Enforcement Prevention Guide (2024)",
            url = "https://www.secretservice.gov/sites/default/files/reports/2024-10/Behavioral-Threat-Assessment-Units-A-Guide-for-State-and-Local-Law-Enforcement-to-Prevent-Targeted-Violence.pdf",
            plainSummary = "Outlines legal boundaries, multidisciplinary collaboration with mental health/social services, and evidentiary rigor required before escalation.",
            elderFriendlyTakeaway = "Describes strict rules so community responders work alongside counselors and respect people's legal rights."
        ),
        NtacPublicationResource(
            id = "ntac_mtss_2025",
            title = "Aligning Behavioral Threat Assessment and Management With a Multi-Tiered System of Support (2025)",
            publishedNote = "USSS NTAC MTSS Alignment Report (August 2025)",
            url = "https://www.secretservice.gov/sites/default/files/reports/2025-08/Aligning-Behavioral-Threat-Assessment-And-Management-With-A-Multi-Tiered-System-Of-Support.pdf",
            plainSummary = "Connects behavioral concern assessment directly with tiered care: universal community prevention, targeted group support, and individualized wraparound services.",
            elderFriendlyTakeaway = "Matches each level of concern with the right level of caring support—from friendly check-ins up to specialized care."
        )
    )

    val REPORTING_FAQS: List<CommunityFaqEntry> = listOf(
        CommunityFaqEntry(
            id = "faq_1",
            question = "Does submitting a community concern accuse someone of a crime?",
            answer = "No. Submitting an observation only asks trained reviewers to clarify context or offer voluntary community resources. It never establishes guilt, intent, or wrongdoing."
        ),
        CommunityFaqEntry(
            id = "faq_2",
            question = "Can I report political disagreements, rumors, or protected traits?",
            answer = "No. Reports rooted in personal grudges, political views, stereotypes, race, religion, disability, or unsupported speculation are blocked by anti-profiling safeguards."
        ),
        CommunityFaqEntry(
            id = "faq_3",
            question = "What happens if I only heard something second-hand or saw a confusing post?",
            answer = "Mark 'Heard from Another Person', 'Online Observation', or 'Requesting Assistance Understanding Situation' and choose a low confidence score (1 or 2). Reviewers start at Tier 1 clarification."
        ),
        CommunityFaqEntry(
            id = "faq_4",
            question = "How is my privacy and civil liberties protected?",
            answer = "Automatic PII scrubbing strips emails, phone numbers, social handles, and device IDs where applicable. All Tier 4 escalations require documented legal, policy, and evidentiary standards."
        ),
        CommunityFaqEntry(
            id = "faq_5",
            question = "What should I do if someone is in immediate danger right now?",
            answer = "Do NOT wait for a portal form. Tap the 911 dialer button or call 911 / local emergency services immediately, or call/text 988 for a mental health crisis."
        )
    )
}
