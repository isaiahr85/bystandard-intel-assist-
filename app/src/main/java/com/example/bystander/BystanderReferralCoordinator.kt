package com.example.bystander

import com.example.model.BtamAntiProfilingGuard
import com.example.model.BtamPiiScrubber
import com.example.model.BtamTokenGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

data class BystanderPortalUiState(
    val activeOption: BystanderSideMenuOption = BystanderSideMenuOption.SUBMIT_CONCERN,
    val elderFriendlyMode: Boolean = false,
    // Intake Form Fields
    val whatObserved: String = "",
    val dateAndTime: String = "",
    val location: String = "",
    val infoSourceDetail: String = "",
    val isDirectObservation: Boolean = true,
    val supportingEvidence: String = "",
    val knownContext: String = "",
    val clarificationReason: String = "",
    val specificConcernBehavior: String = "",
    val selectedSourceTypes: Set<ObservationSourceType> = setOf(ObservationSourceType.DIRECTLY_OBSERVED),
    val confidenceLevel: ConfidenceScaleLevel = ConfidenceScaleLevel.POSSIBLE,
    // 7-Item Validation Checklist + Mandatory Acknowledgement
    val checkedValidationItems: Set<ValidationChecklistItem> = emptySet(),
    val reviewedNtacEducation: Boolean = false,
    val mandatoryAcknowledgementAccepted: Boolean = false,
    // Guardrails & Submission Ledger
    val guardError: String? = null,
    val lastSubmissionMessage: String? = null,
    val submissions: List<BystanderConcernSubmission> = emptyList(),
    // Educational filter state
    val resourceSearchQuery: String = ""
) {
    val allValidationChecked: Boolean
        get() = checkedValidationItems.size == ValidationChecklistItem.entries.size
}

class BystanderReferralCoordinator {
    private val _state = MutableStateFlow(BystanderPortalUiState())
    val state: StateFlow<BystanderPortalUiState> = _state.asStateFlow()

    private val prohibitedPoliticalDisputeTerms = listOf(
        "vote for", "vote against", "political enemy", "liberal", "conservative",
        "democrat", "republican", "election rigging", "party loyalty"
    )

    fun selectOption(option: BystanderSideMenuOption) {
        _state.update { current ->
            val updatedReviewed = if (option == BystanderSideMenuOption.NTAC_LEARNING_CENTER ||
                option == BystanderSideMenuOption.PUBLIC_SAFETY_LIBRARY
            ) {
                true
            } else {
                current.reviewedNtacEducation
            }
            current.copy(
                activeOption = option,
                reviewedNtacEducation = updatedReviewed,
                guardError = null
            )
        }
    }

    fun toggleElderFriendlyMode(enabled: Boolean) {
        _state.update { it.copy(elderFriendlyMode = enabled) }
    }

    fun updateIntakeFields(
        whatObserved: String? = null,
        dateAndTime: String? = null,
        location: String? = null,
        infoSourceDetail: String? = null,
        isDirectObservation: Boolean? = null,
        supportingEvidence: String? = null,
        knownContext: String? = null,
        clarificationReason: String? = null,
        specificConcernBehavior: String? = null
    ) {
        _state.update { current ->
            val next = current.copy(
                whatObserved = whatObserved ?: current.whatObserved,
                dateAndTime = dateAndTime ?: current.dateAndTime,
                location = location ?: current.location,
                infoSourceDetail = infoSourceDetail ?: current.infoSourceDetail,
                isDirectObservation = isDirectObservation ?: current.isDirectObservation,
                supportingEvidence = supportingEvidence ?: current.supportingEvidence,
                knownContext = knownContext ?: current.knownContext,
                clarificationReason = clarificationReason ?: current.clarificationReason,
                specificConcernBehavior = specificConcernBehavior ?: current.specificConcernBehavior,
                lastSubmissionMessage = null
            )
            val combined = listOf(
                next.whatObserved,
                next.specificConcernBehavior,
                next.knownContext,
                next.clarificationReason
            ).joinToString(" ")
            next.copy(guardError = detectProhibitedContent(combined))
        }
    }

    fun toggleSourceType(source: ObservationSourceType) {
        _state.update { current ->
            val updated = current.selectedSourceTypes.toMutableSet()
            if (updated.contains(source)) updated.remove(source) else updated.add(source)
            current.copy(
                selectedSourceTypes = updated,
                isDirectObservation = updated.contains(ObservationSourceType.DIRECTLY_OBSERVED),
                guardError = null
            )
        }
    }

    fun selectConfidenceLevel(level: ConfidenceScaleLevel) {
        _state.update { it.copy(confidenceLevel = level) }
    }

    fun toggleValidationItem(item: ValidationChecklistItem) {
        _state.update { current ->
            val updated = current.checkedValidationItems.toMutableSet()
            if (updated.contains(item)) updated.remove(item) else updated.add(item)
            current.copy(checkedValidationItems = updated, guardError = null)
        }
    }

    fun checkAllValidationItems() {
        _state.update {
            it.copy(
                checkedValidationItems = ValidationChecklistItem.entries.toSet(),
                guardError = null
            )
        }
    }

    fun setMandatoryAcknowledgement(accepted: Boolean) {
        _state.update { it.copy(mandatoryAcknowledgementAccepted = accepted, guardError = null) }
    }

    fun markNtacEducationReviewed() {
        _state.update { it.copy(reviewedNtacEducation = true) }
    }

    fun updateResourceSearchQuery(query: String) {
        _state.update { it.copy(resourceSearchQuery = query) }
    }

    fun determineCoordinationRouting(
        workflowCategory: BystanderSideMenuOption,
        confidence: ConfidenceScaleLevel,
        sources: Set<ObservationSourceType>
    ): Pair<CoordinationTier, List<CoordinationPartner>> {
        val isUnverifiedOnly = sources.all {
            it == ObservationSourceType.COMMUNITY_RUMOR ||
                it == ObservationSourceType.UNVERIFIED_INFO ||
                it == ObservationSourceType.REQUESTING_HELP_UNDERSTANDING
        }
        return when {
            workflowCategory == BystanderSideMenuOption.CLARIFY_ANOMALY || isUnverifiedOnly ->
                CoordinationTier.TIER_1_CLARIFICATION to listOf(
                    CoordinationPartner.COMMUNITY_ORGS,
                    CoordinationPartner.LOCAL_SUPPORT
                )
            workflowCategory == BystanderSideMenuOption.REFER_ASSISTANCE ||
                workflowCategory == BystanderSideMenuOption.REQUEST_WELFARE_CHECK ->
                CoordinationTier.TIER_3_RESOURCE_REFERRAL to listOf(
                    CoordinationPartner.CRISIS_RESOURCES,
                    CoordinationPartner.BEHAVIORAL_HEALTH,
                    CoordinationPartner.LOCAL_SUPPORT
                )
            confidence.level <= 2 ->
                CoordinationTier.TIER_1_CLARIFICATION to listOf(
                    CoordinationPartner.COMMUNITY_ORGS,
                    CoordinationPartner.LOCAL_SUPPORT
                )
            confidence == ConfidenceScaleLevel.IMMEDIATE &&
                sources.contains(ObservationSourceType.DIRECTLY_OBSERVED) ->
                CoordinationTier.TIER_4_AUTHORIZED_ESCALATION to listOf(
                    CoordinationPartner.PUBLIC_SAFETY_AGENCIES,
                    CoordinationPartner.CRISIS_RESOURCES,
                    CoordinationPartner.BEHAVIORAL_HEALTH
                )
            else ->
                CoordinationTier.TIER_2_SCREENING to listOf(
                    CoordinationPartner.SCHOOL_TEAMS,
                    CoordinationPartner.WORKPLACE_SUPPORT,
                    CoordinationPartner.BEHAVIORAL_HEALTH
                )
        }
    }

    fun submitConcern(): Result<BystanderConcernSubmission> {
        val current = _state.value
        val combinedText = listOf(
            current.whatObserved,
            current.dateAndTime,
            current.location,
            current.infoSourceDetail,
            current.supportingEvidence,
            current.knownContext,
            current.clarificationReason,
            current.specificConcernBehavior
        ).joinToString(" ")

        val guardMsg = detectProhibitedContent(combinedText)
        if (guardMsg != null) {
            _state.update { it.copy(guardError = guardMsg) }
            return Result.failure(IllegalArgumentException(guardMsg))
        }

        if (current.whatObserved.isBlank() ||
            current.dateAndTime.isBlank() ||
            current.location.isBlank() ||
            current.infoSourceDetail.isBlank() ||
            current.clarificationReason.isBlank() ||
            current.specificConcernBehavior.isBlank()
        ) {
            val missingErr = "Please complete all required observation fields (what observed, date/time, location, information source, why clarification is requested, and specific concerning behavior)."
            _state.update { it.copy(guardError = missingErr) }
            return Result.failure(IllegalArgumentException(missingErr))
        }

        if (current.selectedSourceTypes.isEmpty()) {
            val srcErr = "Select at least one observation source type before submitting."
            _state.update { it.copy(guardError = srcErr) }
            return Result.failure(IllegalArgumentException(srcErr))
        }

        if (!current.allValidationChecked) {
            val valErr = "Complete all 7 items on the Validation & Vetting Checklist before submission."
            _state.update { it.copy(guardError = valErr) }
            return Result.failure(IllegalStateException(valErr))
        }

        if (!current.mandatoryAcknowledgementAccepted) {
            val ackErr = "Review and accept the Mandatory Acknowledgement before submitting."
            _state.update { it.copy(guardError = ackErr) }
            return Result.failure(IllegalStateException(ackErr))
        }

        val scrubObs = BtamPiiScrubber.scrub(current.whatObserved)
        val scrubSpec = BtamPiiScrubber.scrub(current.specificConcernBehavior)
        val scrubCtx = BtamPiiScrubber.scrub(current.knownContext)
        val scrubClarify = BtamPiiScrubber.scrub(current.clarificationReason)
        val scrubEv = BtamPiiScrubber.scrub(current.supportingEvidence)
        val totalRedacted = scrubObs.redactionCount + scrubSpec.redactionCount +
            scrubCtx.redactionCount + scrubClarify.redactionCount + scrubEv.redactionCount

        val workflowCategory = when (current.activeOption) {
            BystanderSideMenuOption.REFER_ASSISTANCE,
            BystanderSideMenuOption.REQUEST_WELFARE_CHECK,
            BystanderSideMenuOption.CLARIFY_ANOMALY -> current.activeOption
            else -> BystanderSideMenuOption.SUBMIT_CONCERN
        }

        val (tier, partners) = determineCoordinationRouting(
            workflowCategory = workflowCategory,
            confidence = current.confidenceLevel,
            sources = current.selectedSourceTypes
        )

        val submission = BystanderConcernSubmission(
            id = "br_${System.currentTimeMillis() % 100000}",
            referenceCode = BtamTokenGenerator.generateCaseToken(),
            workflowCategory = workflowCategory,
            submittedAtUtc = BtamTokenGenerator.formatUtcNow(),
            whatObserved = scrubObs.cleanedText,
            dateAndTime = current.dateAndTime.trim(),
            location = current.location.trim(),
            infoSourceDetail = current.infoSourceDetail.trim(),
            isDirectObservation = current.isDirectObservation,
            supportingEvidence = scrubEv.cleanedText.ifBlank { "None attached — clarification requested" },
            knownContext = scrubCtx.cleanedText.ifBlank { "Context pending Tier 1 review" },
            clarificationReason = scrubClarify.cleanedText,
            specificConcernBehavior = scrubSpec.cleanedText,
            selectedSourceTypes = current.selectedSourceTypes,
            confidenceLevel = current.confidenceLevel,
            assignedTier = tier,
            recommendedPartners = partners,
            piiRedactedCount = totalRedacted,
            statusNote = if (tier == CoordinationTier.TIER_4_AUTHORIZED_ESCALATION) {
                "Routed to Tier 4 review — legal, policy & evidentiary validation required prior to escalation. Call 911 if threat is imminent."
            } else {
                "Routed to ${tier.title} • Non-accusatory supportive evaluation."
            }
        )

        _state.update {
            it.copy(
                submissions = listOf(submission) + it.submissions,
                activeOption = BystanderSideMenuOption.ACKNOWLEDGEMENT_REVIEW,
                guardError = null,
                lastSubmissionMessage = "Submitted (${submission.referenceCode}) -> ${tier.title}",
                whatObserved = "",
                dateAndTime = "",
                location = "",
                infoSourceDetail = "",
                supportingEvidence = "",
                knownContext = "",
                clarificationReason = "",
                specificConcernBehavior = "",
                checkedValidationItems = emptySet(),
                mandatoryAcknowledgementAccepted = false
            )
        }
        return Result.success(submission)
    }

    private fun detectProhibitedContent(text: String): String? {
        if (text.isBlank()) return null
        val lower = text.lowercase(Locale.US)
        if (BtamAntiProfilingGuard.containsProtectedProfilingFocus(lower)) {
            return "Blocked by Anti-Profiling Guardrail: Remove protected characteristics (race, religion, ethnicity, disability, orientation, political affiliation). Describe observable actions only."
        }
        if (prohibitedPoliticalDisputeTerms.any { lower.contains(it) }) {
            return "Blocked by Non-Partisan Guardrail: Political disputes and partisan reporting are prohibited."
        }
        return null
    }
}
