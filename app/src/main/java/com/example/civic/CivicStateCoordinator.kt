package com.example.civic

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class CivicWorkspaceSection(val label: String) {
    AI_AND_VERSION("AI & Versions"),
    POLICY_AND_CYCLES("Policy & Cycles"),
    ISSUES_AND_REPORTS("Issues & Reports"),
    FIELD_AND_EVOLUTION("Field & Evolution")
}

data class CivicUiState(
    val activeSection: CivicWorkspaceSection = CivicWorkspaceSection.AI_AND_VERSION,
    val activeMilestoneVersion: PlatformMilestoneVersion = PlatformMilestoneVersion.V4_POLICY_INTELLIGENCE,
    val culturalProfile: CulturalContextProfile = CulturalContextProfile(),
    val culturalAuditInput: String = "",
    val latestBiasAudit: BiasAuditResult? = null,
    val baselineWeights: RecommendationWeights = ContextualBanditPersonalizationEngine.defaultWeights(),
    val currentWeights: RecommendationWeights = ContextualBanditPersonalizationEngine.defaultWeights(),
    val subscribedTopics: Set<SocialIssueTopic> = setOf(
        SocialIssueTopic.HOUSING,
        SocialIssueTopic.DISASTER_RECOVERY,
        SocialIssueTopic.PUBLIC_TRANSIT
    ),
    val policies: List<PolicyRecord> = PolicyAndElectionAdaptationEngine.seedVerifiedPolicies(),
    val cycleComparisons: List<ElectionCycleComparison> = PolicyAndElectionAdaptationEngine.seedCycleComparisons(),
    val transitionEvents: List<AdministrationTransitionEvent> = emptyList(),
    val recommendations: List<ExplainableRecommendation> = emptyList(),
    val federatedSyncOptIn: Boolean = false,
    val latestFederatedDelta: FederatedGradientUpdate? = null,
    val resources: List<ServiceResourceItem> = CommunityAndFieldGovernanceEngine.seedVerifiedResources(),
    val communityReports: List<CommunityReport> = listOf(
        CommunityReport(
            id = "rep_seed_01",
            topic = SocialIssueTopic.PUBLIC_TRANSIT,
            title = "Orchidland Transfer Shelter Lighting Request",
            observableDetail = "Solar street light at bus pullout dim after 19:00 HST; riders requested brighter signage.",
            locationLabel = "Hwy 130 / Pohaku Dr",
            reporterReputation = 0.86f,
            moderationStatus = ModerationStatus.HUMAN_VERIFIED,
            piiRedacted = false
        )
    ),
    val reportDraftTitle: String = "",
    val reportDraftDetail: String = "",
    val reportDraftLocation: String = "",
    val reportDraftTopic: SocialIssueTopic = SocialIssueTopic.HOUSING,
    val fieldCases: List<FieldInvestigatorCase> = CommunityAndFieldGovernanceEngine.seedFieldCases(),
    val improvementProposals: List<SelfImprovementProposal> = SelfRefinementAndUpdateEngine.seedProposals(),
    val optimizationReports: List<OptimizationReport> = listOf(
        SelfRefinementAndUpdateEngine.runScheduledUpdate(
            cadence = UpdateCadence.WEEKLY,
            activeReportsCount = 1,
            approvedProposalsCount = 0
        )
    ),
    val speculativeHorizonNote: String = "[Speculative Projection — Version 10.0 Horizon] Multi-county community resilience graph predicting long-term microgrid & rural transit synergy across 2030–2036 (subject to annual human governance audits)."
) {
    val activeRecommendations: List<ExplainableRecommendation>
        get() = recommendations.ifEmpty {
            ContextualBanditPersonalizationEngine.rankExplainableRecommendations(
                weights = currentWeights,
                subscribedTopics = subscribedTopics,
                policies = policies,
                culturalProfile = culturalProfile
            )
        }
}

class CivicStateCoordinator {
    private val _state = MutableStateFlow(
        CivicUiState().let { initial ->
            initial.copy(
                recommendations = ContextualBanditPersonalizationEngine.rankExplainableRecommendations(
                    weights = initial.currentWeights,
                    subscribedTopics = initial.subscribedTopics,
                    policies = initial.policies,
                    culturalProfile = initial.culturalProfile
                )
            )
        }
    )
    val state: StateFlow<CivicUiState> = _state.asStateFlow()

    fun selectSection(section: CivicWorkspaceSection) {
        _state.update { it.copy(activeSection = section) }
    }

    fun activateMilestoneVersion(version: PlatformMilestoneVersion) {
        _state.update { it.copy(activeMilestoneVersion = version) }
    }

    fun recordRecommendationFeedback(topic: SocialIssueTopic, reward: Float) {
        _state.update { current ->
            val nextWeights = ContextualBanditPersonalizationEngine.applyInteractionReward(
                weights = current.currentWeights,
                topic = topic,
                reward = reward
            )
            val ranked = ContextualBanditPersonalizationEngine.rankExplainableRecommendations(
                weights = nextWeights,
                subscribedTopics = current.subscribedTopics,
                policies = current.policies,
                culturalProfile = current.culturalProfile
            )
            current.copy(currentWeights = nextWeights, recommendations = ranked)
        }
    }

    fun toggleFederatedOptInAndExport(optIn: Boolean) {
        _state.update { current ->
            val delta = if (optIn) {
                ContextualBanditPersonalizationEngine.buildFederatedUpdate(
                    baseline = current.baselineWeights,
                    current = current.currentWeights
                )
            } else null
            current.copy(
                federatedSyncOptIn = optIn,
                latestFederatedDelta = delta
            )
        }
    }

    fun toggleCulturalIndigenousContext(enabled: Boolean) {
        _state.update { current ->
            val nextProfile = current.culturalProfile.copy(indigenousProtocolRespect = enabled)
            val ranked = ContextualBanditPersonalizationEngine.rankExplainableRecommendations(
                weights = current.currentWeights,
                subscribedTopics = current.subscribedTopics,
                policies = current.policies,
                culturalProfile = nextProfile
            )
            current.copy(culturalProfile = nextProfile, recommendations = ranked)
        }
    }

    fun updateCulturalAuditInput(text: String) {
        _state.update {
            it.copy(
                culturalAuditInput = text,
                latestBiasAudit = if (text.isBlank()) null else CulturalSensitivityEngine.auditCommunication(text)
            )
        }
    }

    fun simulateAdministrationTransition(
        cycleLabel: String = "2026–2030 Administration Cycle",
        priorities: List<String> = listOf("Affordable Housing Supply", "Disaster Resilience Microgrids", "Workforce Healthcare Training")
    ) {
        _state.update { current ->
            val transition = AdministrationTransitionEvent(
                id = "tr_${System.currentTimeMillis() % 10000}",
                cycleLabel = cycleLabel,
                transitionType = "Declared Public Priorities Update",
                effectiveDateIso = CivicClock.todayIso(),
                declaredPriorities = priorities,
                detectedSource = "Public Budget & Council Charter Notice"
            )
            val (recalculatedPolicies, comparison) = PolicyAndElectionAdaptationEngine.applyAdministrationTransition(
                policies = current.policies,
                transition = transition
            )
            val ranked = ContextualBanditPersonalizationEngine.rankExplainableRecommendations(
                weights = current.currentWeights,
                subscribedTopics = current.subscribedTopics,
                policies = recalculatedPolicies,
                culturalProfile = current.culturalProfile
            )
            current.copy(
                policies = recalculatedPolicies,
                cycleComparisons = listOf(comparison) + current.cycleComparisons,
                transitionEvents = listOf(transition) + current.transitionEvents,
                recommendations = ranked
            )
        }
    }

    fun toggleTopicSubscription(topic: SocialIssueTopic) {
        _state.update { current ->
            val updated = current.subscribedTopics.toMutableSet()
            if (updated.contains(topic)) updated.remove(topic) else updated.add(topic)
            val ranked = ContextualBanditPersonalizationEngine.rankExplainableRecommendations(
                weights = current.currentWeights,
                subscribedTopics = updated,
                policies = current.policies,
                culturalProfile = current.culturalProfile
            )
            current.copy(subscribedTopics = updated, recommendations = ranked)
        }
    }

    fun updateCommunityReportDraft(
        topic: SocialIssueTopic? = null,
        title: String? = null,
        detail: String? = null,
        location: String? = null
    ) {
        _state.update {
            it.copy(
                reportDraftTopic = topic ?: it.reportDraftTopic,
                reportDraftTitle = title ?: it.reportDraftTitle,
                reportDraftDetail = detail ?: it.reportDraftDetail,
                reportDraftLocation = location ?: it.reportDraftLocation
            )
        }
    }

    fun submitCommunityReport() {
        val current = _state.value
        val created = CommunityAndFieldGovernanceEngine.screenAndCreateCommunityReport(
            topic = current.reportDraftTopic,
            title = current.reportDraftTitle,
            rawDetail = current.reportDraftDetail,
            locationLabel = current.reportDraftLocation
        )
        _state.update {
            it.copy(
                communityReports = listOf(created) + it.communityReports,
                reportDraftTitle = "",
                reportDraftDetail = "",
                reportDraftLocation = ""
            )
        }
    }

    fun humanVerifyCommunityReport(reportId: String) {
        _state.update { current ->
            current.copy(
                communityReports = current.communityReports.map {
                    if (it.id == reportId) it.copy(moderationStatus = ModerationStatus.HUMAN_VERIFIED) else it
                }
            )
        }
    }

    fun toggleFieldLocationConsent(caseId: String) {
        _state.update { current ->
            current.copy(
                fieldCases = current.fieldCases.map { fc ->
                    if (fc.id == caseId) {
                        val nextConsent = !fc.locationLoggingConsentGranted
                        fc.copy(
                            locationLoggingConsentGranted = nextConsent,
                            consentBoundaryNote = if (nextConsent) {
                                "Operator consent GRANTED for public infrastructure site coordinates only (no individual tracking)."
                            } else {
                                "Consent-gated: GPS logging OFF until operator taps explicit consent switch; zero private-individual surveillance."
                            }
                        )
                    } else fc
                }
            )
        }
    }

    fun runUpdateCadence(cadence: UpdateCadence) {
        _state.update { current ->
            val approvedCount = current.improvementProposals.count { it.humanApproved == true }
            val report = SelfRefinementAndUpdateEngine.runScheduledUpdate(
                cadence = cadence,
                activeReportsCount = current.communityReports.size,
                approvedProposalsCount = approvedCount
            )
            current.copy(
                optimizationReports = listOf(report) + current.optimizationReports
            )
        }
    }

    fun reviewImprovementProposal(proposalId: String, approved: Boolean) {
        _state.update { current ->
            current.copy(
                improvementProposals = current.improvementProposals.map { prop ->
                    if (prop.id == proposalId) prop.copy(humanApproved = approved) else prop
                }
            )
        }
    }
}
