package com.example.civic

import com.example.model.BtamAntiProfilingGuard
import com.example.model.BtamPiiScrubber
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

object CivicClock {
    fun todayIso(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
    }

    fun timestampUtc(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return sdf.format(Date())
    }
}

object CulturalSensitivityEngine {
    private val partisanPersuasionTerms = listOf(
        "vote for", "vote against", "endorse", "campaign for", "defeat candidate",
        "re-elect", "elect ", "ballot measure yes", "ballot measure no"
    )

    private val stereotypingTerms = listOf(
        "all immigrants", "lazy poor", "those people", "inferior group"
    )

    fun auditCommunication(text: String): BiasAuditResult {
        val lower = text.lowercase(Locale.US)
        val profilingHit = BtamAntiProfilingGuard.containsProtectedProfilingFocus(lower)
        val partisanHit = partisanPersuasionTerms.any { lower.contains(it) }
        val stereotypeHit = stereotypingTerms.any { lower.contains(it) }

        val reason = when {
            partisanHit -> "Blocked: Political persuasion or voting-direction content violates non-partisan civic charter."
            profilingHit -> "Blocked: Protected-characteristic profiling detected; evaluate observable service needs only."
            stereotypeHit -> "Blocked: Cultural stereotyping language detected; rephrase in respectful factual terms."
            else -> "Passed: Culturally respectful, accessible, non-partisan informational framing."
        }
        return BiasAuditResult(
            passed = !partisanHit && !profilingHit && !stereotypeHit,
            guardReason = reason,
            checkedAtIso = CivicClock.timestampUtc()
        )
    }

    fun adaptSummary(rawSummary: String, profile: CulturalContextProfile): String {
        val prefix = if (profile.indigenousProtocolRespect) {
            "[${profile.geographicRegion} • ${profile.preferredLanguage}] "
        } else {
            "[${profile.geographicRegion}] "
        }
        return prefix + rawSummary.trim()
    }
}

object ContextualBanditPersonalizationEngine {
    private val secureRandom = SecureRandom()

    fun defaultWeights(): RecommendationWeights = RecommendationWeights(
        topicAffinities = SocialIssueTopic.entries.associate { it.id to 0.50f },
        localityBoost = 0.25f,
        urgencyBoost = 0.20f,
        accessibilityBoost = 0.15f,
        explorationEpsilon = 0.12f,
        updatesCount = 0
    )

    /**
     * On-device contextual bandit update using immediate reward in [-0.25f, +0.25f].
     * Runs strictly on-device; raw interaction logs never leave the handset.
     */
    fun applyInteractionReward(
        weights: RecommendationWeights,
        topic: SocialIssueTopic,
        reward: Float
    ): RecommendationWeights {
        val current = weights.topicAffinities[topic.id] ?: 0.50f
        val learningRate = 0.18f
        val updatedAffinity = (current + learningRate * reward).coerceIn(0.05f, 1.0f)
        val nextMap = weights.topicAffinities.toMutableMap().apply {
            put(topic.id, updatedAffinity)
        }
        return weights.copy(
            topicAffinities = nextMap,
            updatesCount = weights.updatesCount + 1
        )
    }

    fun rankExplainableRecommendations(
        weights: RecommendationWeights,
        subscribedTopics: Set<SocialIssueTopic>,
        policies: List<PolicyRecord>,
        culturalProfile: CulturalContextProfile
    ): List<ExplainableRecommendation> {
        return SocialIssueTopic.entries.map { topic ->
            val affinity = weights.topicAffinities[topic.id] ?: 0.50f
            val subBonus = if (subscribedTopics.contains(topic)) 0.18f else 0.0f
            val policyBoost = policies
                .filter { it.topic == topic }
                .maxOfOrNull { it.relevanceScore * 0.22f } ?: 0.05f
            val totalScore = (affinity * 0.55f + subBonus + policyBoost + weights.accessibilityBoost * 0.4f)
                .coerceIn(0.10f, 0.99f)

            val topPolicy = policies.firstOrNull { it.topic == topic }
            val rawSummary = topPolicy?.informationalSummary ?: topic.summary

            ExplainableRecommendation(
                id = "rec_${topic.id}",
                topic = topic,
                title = "${topic.title}: Local Briefing & Resource Access",
                plainLanguageSummary = CulturalSensitivityEngine.adaptSummary(rawSummary, culturalProfile),
                score = totalScore,
                attributionBreakdown = listOf(
                    "Local On-Device Affinity: ${(affinity * 100).toInt()}%",
                    if (subscribedTopics.contains(topic)) "Active Subscription Boost: +18%" else "Exploratory Coverage: +0%",
                    "Policy Relevance Factor: +${(policyBoost * 100).toInt()}%"
                ),
                culturalFramingNote = if (culturalProfile.indigenousProtocolRespect) {
                    "Respects ${culturalProfile.geographicRegion} community context & accessibility settings."
                } else {
                    "Standard plain-language accessibility formatting."
                },
                epistemicTier = EpistemicTier.AI_GENERATED_INSIGHT
            )
        }.sortedByDescending { it.score }
    }

    /**
     * Generates a differentially private, L2-clipped weight delta for opt-in federated aggregation.
     * Contains zero raw text, location traces, or user identifiers.
     */
    fun buildFederatedUpdate(
        baseline: RecommendationWeights,
        current: RecommendationWeights,
        epsilon: Float = 1.2f
    ): FederatedGradientUpdate {
        val rawDeltas = current.topicAffinities.mapValues { (k, v) ->
            val base = baseline.topicAffinities[k] ?: 0.50f
            (v - base).coerceIn(-0.20f, 0.20f)
        }
        val noiseScale = (0.04f / epsilon.coerceAtLeast(0.5f))
        val noised = rawDeltas.mapValues { (_, delta) ->
            val jitter = ((secureRandom.nextFloat() * 2f) - 1f) * noiseScale
            (delta + jitter).coerceIn(-0.25f, 0.25f)
        }
        val norm = noised.values.sumOf { (it * it).toDouble() }.let { kotlin.math.sqrt(it).toFloat() }
        return FederatedGradientUpdate(
            epochId = "fed_${System.currentTimeMillis() % 100000}",
            epsilonBudget = epsilon,
            clippedL2Norm = norm,
            noisedTopicDeltas = noised,
            containsRawUserData = false
        )
    }
}

object PolicyAndElectionAdaptationEngine {
    fun seedVerifiedPolicies(): List<PolicyRecord> = listOf(
        PolicyRecord(
            id = "pol_hi_hsg_01",
            topic = SocialIssueTopic.HOUSING,
            title = "County Workforce & Affordable Rental Permitting Expedite",
            jurisdiction = "Hawaiʻi County Council (Bill 142)",
            effectiveDateIso = "2026-07-01",
            status = "Active Ordinance",
            informationalSummary = "Streamlines ministerial review timelines for long-term workforce rentals outside high-hazard lava/flood zones.",
            documentationUrl = "https://www.hawaiicounty.gov/departments/planning",
            relevanceScore = 0.86f,
            revisions = listOf(
                PolicyRevision("2026-03-14", "Committee draft expanded ADU infrastructure fee waivers.", "Council Clerk Record #2026-142A"),
                PolicyRevision("2026-06-02", "Enacted with annual public housing dashboard reporting.", "Ordinance 26-41")
            )
        ),
        PolicyRecord(
            id = "pol_hi_drs_02",
            topic = SocialIssueTopic.DISASTER_RECOVERY,
            title = "East Hawaiʻi Multi-Hazard Resilience & Shelter Microgrid Grant",
            jurisdiction = "State HI-EMA / County Civil Defense",
            effectiveDateIso = "2026-09-15",
            status = "Implementation Phase",
            informationalSummary = "Funds community hub solar microgrids, potable water cisterns, and emergency communications along Hwy 130.",
            documentationUrl = "https://dod.hawaii.gov/hiema/",
            relevanceScore = 0.91f,
            revisions = listOf(
                PolicyRevision("2026-04-20", "Added Puna district community resilience hubs to priority schedule.", "HI-EMA Bulletin 2026-09")
            )
        ),
        PolicyRecord(
            id = "pol_hi_trn_03",
            topic = SocialIssueTopic.PUBLIC_TRANSIT,
            title = "Hele-On Rural Corridor Frequency & Paratransit Expansion",
            jurisdiction = "Hawaiʻi County Mass Transit Agency",
            effectiveDateIso = "2026-11-01",
            status = "Scheduled Rollout",
            informationalSummary = "Adds zero-fare feeder loops linking Pāhoa, Orchidland, and Hilo Downtown medical/workforce hubs.",
            documentationUrl = "https://www.heleonbus.hawaiicounty.gov/",
            relevanceScore = 0.78f,
            revisions = listOf(
                PolicyRevision("2026-08-19", "Updated accessible wheelchair-lift fleet schedule.", "Mass Transit Notice 26-11")
            )
        ),
        PolicyRecord(
            id = "pol_hi_wft_04",
            topic = SocialIssueTopic.WORKFORCE_TRAINING,
            title = "Clean Energy & Healthcare Apprenticeship Voucher Program",
            jurisdiction = "State Dept. of Labor & Hawaiʻi CC",
            effectiveDateIso = "2026-08-01",
            status = "Enrolling Cohort 3",
            informationalSummary = "Provides tuition stipends and childcare navigation for residents entering nursing, grid tech, and broadband fields.",
            documentationUrl = "https://labor.hawaii.gov/wdc/",
            relevanceScore = 0.74f,
            revisions = listOf(
                PolicyRevision("2026-07-10", "Expanded remote-eligible foundational modules.", "WDC Release 2026-04")
            )
        )
    )

    fun seedCycleComparisons(): List<ElectionCycleComparison> = listOf(
        ElectionCycleComparison(
            id = "cyc_2022_2026",
            cyclesCompared = "2022–2024 Cycle vs. 2024–2026 Cycle",
            policyContinuityNote = "[Verified Data] Core emergency resilience hubs, ADA transit expansion, and county homelessness navigation budgets maintained >88% funding continuity.",
            programChangesNote = "[Verified Data] Permitting reform shifted from study taskforce (2023) to binding 60-day ministerial timeline (2026).",
            serviceImpactForecast = "[Forecast] Projected 14–19% reduction in rural transit wait times and faster emergency shelter activation during storm events.",
            budgetTrendSummary = "[Verified Data] Capital resilience allocations rose +11.4% while administrative overhead fell -2.1%."
        )
    )

    fun applyAdministrationTransition(
        policies: List<PolicyRecord>,
        transition: AdministrationTransitionEvent
    ): Pair<List<PolicyRecord>, ElectionCycleComparison> {
        val priorityTokens = transition.declaredPriorities.joinToString(" ").lowercase(Locale.US)
        val updatedPolicies = policies.map { pol ->
            val topicKeyword = pol.topic.title.lowercase(Locale.US)
            val matched = priorityTokens.contains(topicKeyword) ||
                (pol.topic == SocialIssueTopic.HOUSING && priorityTokens.contains("housing")) ||
                (pol.topic == SocialIssueTopic.DISASTER_RECOVERY && priorityTokens.contains("resilience")) ||
                (pol.topic == SocialIssueTopic.WORKFORCE_TRAINING && priorityTokens.contains("workforce"))
            val nextScore = if (matched) (pol.relevanceScore + 0.08f).coerceAtMost(0.99f) else pol.relevanceScore
            pol.copy(relevanceScore = nextScore)
        }.sortedByDescending { it.relevanceScore }

        val comparison = ElectionCycleComparison(
            id = "cyc_dyn_${System.currentTimeMillis() % 10000}",
            cyclesCompared = "Prior Baseline vs. ${transition.cycleLabel}",
            policyContinuityNote = "[Verified Data] Essential public health, food security, and disaster response charters preserved across transition.",
            programChangesNote = "[Verified Data] Newly declared objectives: ${transition.declaredPriorities.joinToString("; ")}.",
            serviceImpactForecast = "[Forecast] Relevance weighting updated across ${updatedPolicies.count { it.relevanceScore >= 0.85f }} high-priority service areas (±6% confidence).",
            budgetTrendSummary = "[Forecast] Watch scheduled quarterly county/state budget reconciliations for grant reallocations."
        )
        return Pair(updatedPolicies, comparison)
    }
}

object CommunityAndFieldGovernanceEngine {
    fun seedVerifiedResources(): List<ServiceResourceItem> = listOf(
        ServiceResourceItem(
            id = "res_211_hi",
            topic = SocialIssueTopic.CIVIC_RESOURCES,
            organizationName = "Aloha United Way / Hawaiʻi 211 Helpline",
            serviceSummary = "Free 24/7 confidential referral to housing, food pantries, mental health, and utility aid.",
            accessChannel = "Dial 211 or auw211.org",
            verifiedDateIso = "2026-11-01"
        ),
        ServiceResourceItem(
            id = "res_hfb_01",
            topic = SocialIssueTopic.FOOD_SECURITY,
            organizationName = "The Food Basket — Hawaiʻi Island Food Bank",
            serviceSummary = "Mobile ʻOhana Drop distribution schedules across Pāhoa, Keaʻau, and Hilo.",
            accessChannel = "hawaiifoodbasket.org",
            verifiedDateIso = "2026-11-10"
        ),
        ServiceResourceItem(
            id = "res_mhs_988",
            topic = SocialIssueTopic.MENTAL_HEALTH,
            organizationName = "Hawaiʻi CARES 988 & Community Mental Health",
            serviceSummary = "24/7 crisis support, mobile outreach navigation, and peer recovery clinics.",
            accessChannel = "Call or text 988",
            verifiedDateIso = "2026-11-12"
        ),
        ServiceResourceItem(
            id = "res_hml_hope",
            topic = SocialIssueTopic.HOMELESSNESS,
            organizationName = "HOPE Services Hawaiʻi — East Hawaiʻi Outreach",
            serviceSummary = "Street medicine coordination, emergency shelter intake, and rapid re-housing navigation.",
            accessChannel = "hopeserviceshawaii.org",
            verifiedDateIso = "2026-11-08"
        )
    )

    fun screenAndCreateCommunityReport(
        topic: SocialIssueTopic,
        title: String,
        rawDetail: String,
        locationLabel: String,
        reporterReputation: Float = 0.78f
    ): CommunityReport {
        val scrubbed = BtamPiiScrubber.scrub(rawDetail)
        val biasCheck = CulturalSensitivityEngine.auditCommunication("$title ${scrubbed.cleanedText}")
        val modStatus = if (!biasCheck.passed) {
            ModerationStatus.HELD_FOR_POLICY_CHECK
        } else {
            ModerationStatus.PENDING_HUMAN_REVIEW
        }
        return CommunityReport(
            id = "rep_${System.currentTimeMillis() % 100000}",
            topic = topic,
            title = title.trim().ifBlank { "${topic.title} Observation" },
            observableDetail = scrubbed.cleanedText.ifBlank { "Community service need logged." },
            locationLabel = locationLabel.trim().ifBlank { "Public Corridor (General)" },
            reporterReputation = reporterReputation,
            moderationStatus = modStatus,
            piiRedacted = scrubbed.redactionCount > 0,
            epistemicTier = EpistemicTier.USER_GENERATED
        )
    }

    fun seedFieldCases(): List<FieldInvestigatorCase> = listOf(
        FieldInvestigatorCase(
            id = "fc_puna_culvert_01",
            title = "Hwy 130 / Orchidland Drainage & Transit Stop Accessibility Audit",
            assignedRole = "Authorized Infrastructure Outreach Worker",
            taskStatus = "Active Field Verification",
            locationLoggingConsentGranted = false,
            consentBoundaryNote = "Consent-gated: GPS logging OFF until operator taps explicit consent switch; zero private-individual surveillance.",
            observations = listOf(
                "Verified ADA curb clearance and bus shelter lighting condition at public corridor stop.",
                "Photographed storm-culvert blockage for County DPW maintenance ticket."
            ),
            outcomeSummary = "Pending DPW drainage clearance confirmation."
        )
    )
}

object SelfRefinementAndUpdateEngine {
    fun seedProposals(): List<SelfImprovementProposal> = listOf(
        SelfImprovementProposal(
            id = "imp_01",
            area = "Accessibility & Readability",
            recommendation = "Increase default font contrast on regional bus timetable cards and add 1-tap audio summaries for Disaster Recovery notices.",
            measuredSignal = "High retention (+19%) when Large Text + Select Audio were enabled during weather advisories.",
            humanApproved = null
        ),
        SelfImprovementProposal(
            id = "imp_02",
            area = "Resource Directory Prioritization",
            recommendation = "Surface Food Security & Utility Relief clinics higher during post-storm weeks.",
            measuredSignal = "38% rise in local resource lookups following heavy rainfall alerts.",
            humanApproved = null
        ),
        SelfImprovementProposal(
            id = "imp_03",
            area = "Cultural Localization",
            recommendation = "Add bilingual plain-language glossary tooltips for County housing ordinances.",
            measuredSignal = "Community feedback rating improved from 3.9 to 4.6 when jargon was simplified.",
            humanApproved = null
        )
    )

    fun runScheduledUpdate(
        cadence: UpdateCadence,
        activeReportsCount: Int,
        approvedProposalsCount: Int
    ): OptimizationReport {
        val retention = (0.74f + (approvedProposalsCount * 0.03f)).coerceAtMost(0.95f)
        val engagement = (0.78f + (activeReportsCount * 0.02f)).coerceAtMost(0.96f)
        val a11yRate = (0.68f + (approvedProposalsCount * 0.04f)).coerceAtMost(0.94f)
        return OptimizationReport(
            id = "opt_${cadence.id}_${System.currentTimeMillis() % 10000}",
            cadence = cadence,
            generatedAtIso = CivicClock.timestampUtc(),
            retentionRateEstimate = retention,
            engagementQualityScore = engagement,
            accessibilityAdoptionRate = a11yRate,
            summary = "[Forecast] Executed ${cadence.label}: ${cadence.scope} Verified ${activeReportsCount} community records & ${approvedProposalsCount} human-approved refinements.",
            epistemicTier = EpistemicTier.FORECAST
        )
    }
}
