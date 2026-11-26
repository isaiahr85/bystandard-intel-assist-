package com.example.tools

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Locale

enum class TextScalePreset(
    val id: String,
    val shortLabel: String,
    val displayName: String,
    val scaleFactor: Float
) {
    STANDARD("standard", "1.0x", "Standard Text", 1.0f),
    LARGE("large", "1.2x", "Large Readable", 1.22f),
    EXTRA_LARGE("extra_large", "1.4x", "Extra Large", 1.42f);

    fun next(): TextScalePreset {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

enum class AudioSpeechRate(
    val label: String,
    val rate: Float
) {
    SLOW("0.85x", 0.85f),
    NORMAL("1.0x", 1.0f),
    FAST("1.25x", 1.25f),
    RAPID("1.5x", 1.5f);

    fun next(): AudioSpeechRate {
        val all = entries
        return all[(ordinal + 1) % all.size]
    }
}

data class AudioPlaybackState(
    val activeSourceId: String? = null,
    val activeTitle: String = "",
    val activeText: String = "",
    val isPlaying: Boolean = false,
    val isPaused: Boolean = false,
    val speechRate: AudioSpeechRate = AudioSpeechRate.NORMAL,
    val currentChunkIndex: Int = 0,
    val totalChunks: Int = 0,
    val activeSentence: String = "",
    val engineReady: Boolean = false,
    val statusNote: String = "Select any report or text box to listen"
) {
    val isActive: Boolean
        get() = activeSourceId != null && (isPlaying || isPaused)

    val progressFraction: Float
        get() = if (totalChunks <= 0) 0f else ((currentChunkIndex + 1).toFloat() / totalChunks.toFloat()).coerceIn(0f, 1f)
}

data class AccessibilityAudioUiBridge(
    val visualAudioEnabled: Boolean = true,
    val textScalePreset: TextScalePreset = TextScalePreset.STANDARD,
    val playbackState: AudioPlaybackState = AudioPlaybackState(),
    val onPlayOrToggle: (sourceId: String, title: String, text: String) -> Unit = { _, _, _ -> },
    val onPauseOrResume: () -> Unit = {},
    val onStop: () -> Unit = {},
    val onCycleSpeechRate: () -> Unit = {},
    val onSetSpeechRate: (AudioSpeechRate) -> Unit = {},
    val onToggleVisualAudio: () -> Unit = {},
    val onCycleTextScale: () -> Unit = {},
    val onSetTextScale: (TextScalePreset) -> Unit = {},
    val onOpenAccessibilitySheet: () -> Unit = {}
)

val LocalAccessibilityAudio = compositionLocalOf { AccessibilityAudioUiBridge() }

/**
 * On-device Android TextToSpeech manager with sentence chunking, live spoken-chunk tracking,
 * speech rate control, and safe fallback behavior when running in headless JVM/Robolectric tests.
 */
class AccessibilityAudioManager(
    context: Context,
    autoInitialize: Boolean = false
) {

    internal data class TextChunk(
        val text: String,
        val range: IntRange
    )

    private val appContext = context.applicationContext ?: context
    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    @Volatile
    private var tts: TextToSpeech? = null
    @Volatile
    private var ttsInitialized: Boolean = false
    @Volatile
    private var currentChunks: List<TextChunk> = emptyList()
    @Volatile
    private var sessionNonce: Long = 0L

    init {
        if (autoInitialize) {
            initializeTtsEngine()
        }
    }

    fun ensureInitialized() {
        if (!ttsInitialized) {
            initializeTtsEngine()
        }
    }

    private fun initializeTtsEngine() {
        if (ttsInitialized) return
        ttsInitialized = true
        runCatching {
            var created: TextToSpeech? = null
            created = TextToSpeech(appContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val engine = created
                    val langResult = runCatching { engine?.setLanguage(Locale.US) }.getOrNull()
                    val supported = langResult != TextToSpeech.LANG_MISSING_DATA &&
                        langResult != TextToSpeech.LANG_NOT_SUPPORTED
                    runCatching {
                        engine?.setSpeechRate(_state.value.speechRate.rate)
                        engine?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {
                                val parsed = parseUtteranceId(utteranceId) ?: return
                                if (parsed.first != sessionNonce) return
                                val idx = parsed.second
                                val chunkText = currentChunks.getOrNull(idx)?.text.orEmpty()
                                _state.update {
                                    it.copy(
                                        isPlaying = true,
                                        isPaused = false,
                                        currentChunkIndex = idx,
                                        activeSentence = chunkText,
                                        statusNote = "Speaking section ${idx + 1} of ${currentChunks.size.coerceAtLeast(1)}"
                                    )
                                }
                            }

                            override fun onDone(utteranceId: String?) {
                                val parsed = parseUtteranceId(utteranceId) ?: return
                                if (parsed.first != sessionNonce) return
                                val idx = parsed.second
                                if (idx >= currentChunks.lastIndex) {
                                    _state.update {
                                        it.copy(
                                            isPlaying = false,
                                            isPaused = false,
                                            activeSourceId = null,
                                            activeTitle = "",
                                            activeSentence = "",
                                            currentChunkIndex = 0,
                                            totalChunks = 0,
                                            statusNote = "Finished reading report"
                                        )
                                    }
                                }
                            }

                            @Deprecated("Deprecated in Java")
                            override fun onError(utteranceId: String?) {
                                handleUtteranceError(utteranceId)
                            }

                            override fun onError(utteranceId: String?, errorCode: Int) {
                                handleUtteranceError(utteranceId)
                            }
                        })
                    }
                    _state.update {
                        it.copy(
                            engineReady = supported,
                            statusNote = if (supported) "Voice engine ready" else "TTS language unavailable; visual reader active"
                        )
                    }
                } else {
                    _state.update {
                        it.copy(
                            engineReady = false,
                            statusNote = "Visual reader mode active (no system TTS voice)"
                        )
                    }
                }
            }
            tts = created
        }.onFailure {
            tts = null
            _state.update {
                it.copy(
                    engineReady = false,
                    statusNote = "Visual reader mode active"
                )
            }
        }
    }

    private fun handleUtteranceError(utteranceId: String?) {
        val parsed = parseUtteranceId(utteranceId)
        if (parsed != null && parsed.first != sessionNonce) return
        _state.update {
            it.copy(
                isPlaying = false,
                isPaused = true,
                statusNote = "TTS engine interrupted; tap Play to retry"
            )
        }
    }

    fun playOrToggle(sourceId: String, title: String, rawText: String) {
        ensureInitialized()
        val cleanText = rawText.trim().replace(Regex("\\s+"), " ")
        if (cleanText.isEmpty()) {
            _state.update {
                it.copy(statusNote = "No readable text found in selected box.")
            }
            return
        }

        val current = _state.value
        if (current.activeSourceId == sourceId) {
            if (current.isPlaying) {
                pause()
                return
            } else if (current.isPaused) {
                resume()
                return
            }
        }

        val chunks = buildChunks(cleanText)
        currentChunks = chunks
        sessionNonce += 1L
        val activeNonce = sessionNonce

        _state.update {
            it.copy(
                activeSourceId = sourceId,
                activeTitle = title.ifBlank { "Selected Report" },
                activeText = cleanText,
                isPlaying = true,
                isPaused = false,
                currentChunkIndex = 0,
                totalChunks = chunks.size,
                activeSentence = chunks.firstOrNull()?.text.orEmpty(),
                statusNote = if (it.engineReady) {
                    "Speaking $title (${chunks.size} sections)"
                } else {
                    "Visual audio reader active: $title"
                }
            )
        }

        speakChunksFrom(index = 0, nonce = activeNonce)
    }

    fun pause() {
        val current = _state.value
        if (!current.isActive) return
        runCatching { tts?.stop() }
        _state.update {
            it.copy(
                isPlaying = false,
                isPaused = true,
                statusNote = "Paused at section ${it.currentChunkIndex + 1} of ${it.totalChunks.coerceAtLeast(1)}"
            )
        }
    }

    fun resume() {
        val current = _state.value
        if (current.activeSourceId == null) return
        if (currentChunks.isEmpty() && current.activeText.isNotBlank()) {
            currentChunks = buildChunks(current.activeText)
        }
        val startIndex = current.currentChunkIndex.coerceIn(0, currentChunks.lastIndex.coerceAtLeast(0))
        sessionNonce += 1L
        val activeNonce = sessionNonce

        _state.update {
            it.copy(
                isPlaying = true,
                isPaused = false,
                activeSentence = currentChunks.getOrNull(startIndex)?.text.orEmpty(),
                statusNote = "Resumed ${current.activeTitle}"
            )
        }
        speakChunksFrom(index = startIndex, nonce = activeNonce)
    }

    fun pauseOrResume() {
        val current = _state.value
        if (current.isPlaying) pause() else if (current.isPaused) resume()
    }

    fun stop() {
        sessionNonce += 1L
        currentChunks = emptyList()
        runCatching { tts?.stop() }
        _state.update {
            it.copy(
                activeSourceId = null,
                activeTitle = "",
                activeText = "",
                isPlaying = false,
                isPaused = false,
                currentChunkIndex = 0,
                totalChunks = 0,
                activeSentence = "",
                statusNote = "Audio playback stopped"
            )
        }
    }

    fun setSpeechRate(rate: AudioSpeechRate) {
        runCatching { tts?.setSpeechRate(rate.rate) }
        val wasPlaying = _state.value.isPlaying
        val idx = _state.value.currentChunkIndex
        _state.update {
            it.copy(
                speechRate = rate,
                statusNote = "Speech rate set to ${rate.label}"
            )
        }
        if (wasPlaying && currentChunks.isNotEmpty()) {
            sessionNonce += 1L
            speakChunksFrom(index = idx.coerceIn(0, currentChunks.lastIndex), nonce = sessionNonce)
        }
    }

    fun cycleSpeechRate() {
        setSpeechRate(_state.value.speechRate.next())
    }

    fun shutdown() {
        runCatching {
            tts?.stop()
            tts?.shutdown()
        }
        tts = null
    }

    private fun speakChunksFrom(index: Int, nonce: Long) {
        val engine = tts ?: return
        if (!_state.value.engineReady || currentChunks.isEmpty()) return

        runCatching {
            engine.setSpeechRate(_state.value.speechRate.rate)
            for (i in index until currentChunks.size) {
                val queueMode = if (i == index) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
                val utteranceId = "${nonce}_$i"
                val params = Bundle().apply {
                    putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
                }
                engine.speak(currentChunks[i].text, queueMode, params, utteranceId)
            }
        }
    }

    private fun parseUtteranceId(utteranceId: String?): Pair<Long, Int>? {
        if (utteranceId.isNullOrBlank()) return null
        val parts = utteranceId.split('_')
        if (parts.size != 2) return null
        val nonce = parts[0].toLongOrNull() ?: return null
        val idx = parts[1].toIntOrNull() ?: return null
        return nonce to idx
    }

    companion object {
        fun splitIntoChunks(text: String, maxChunkChars: Int = 220): List<Pair<String, IntRange>> {
            val clean = text.trim().replace(Regex("\\s+"), " ")
            if (clean.isEmpty()) return emptyList()

            val results = mutableListOf<Pair<String, IntRange>>()
            var cursor = 0
            while (cursor < clean.length) {
                while (cursor < clean.length && clean[cursor] == ' ') cursor++
                if (cursor >= clean.length) break

                val hardLimit = (cursor + maxChunkChars).coerceAtMost(clean.length)
                var splitAt = hardLimit

                if (hardLimit < clean.length) {
                    val window = clean.substring(cursor, hardLimit)
                    val sentenceBreak = listOf(". ", "! ", "? ", "; ", ": ")
                        .mapNotNull { delim ->
                            val pos = window.lastIndexOf(delim)
                            if (pos >= 25) cursor + pos + 1 else null
                        }
                        .maxOrNull()

                    splitAt = sentenceBreak ?: run {
                        val lastSpace = window.lastIndexOf(' ')
                        if (lastSpace > 20) cursor + lastSpace else hardLimit
                    }
                }

                val slice = clean.substring(cursor, splitAt).trim()
                if (slice.isNotEmpty()) {
                    results.add(slice to (cursor until splitAt))
                }
                cursor = splitAt
            }
            return results
        }

        private fun buildChunks(text: String): List<TextChunk> =
            splitIntoChunks(text).map { (chunkText, range) -> TextChunk(chunkText, range) }
    }
}
