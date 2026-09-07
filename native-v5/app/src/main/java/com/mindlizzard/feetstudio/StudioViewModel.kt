package com.mindlizzard.feetstudio

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mindlizzard.feetstudio.ai.GeminiClient
import com.mindlizzard.feetstudio.data.DeviceExportRepository
import com.mindlizzard.feetstudio.data.GalleryRepository
import com.mindlizzard.feetstudio.data.SecureKeyStore
import com.mindlizzard.feetstudio.domain.DesignState
import com.mindlizzard.feetstudio.domain.FixTarget
import com.mindlizzard.feetstudio.domain.ReferenceAsset
import com.mindlizzard.feetstudio.domain.RenderContract
import com.mindlizzard.feetstudio.domain.RenderEngine
import com.mindlizzard.feetstudio.domain.RenderRecord
import com.mindlizzard.feetstudio.domain.RenderMode
import com.mindlizzard.feetstudio.domain.StudioSettings
import com.mindlizzard.feetstudio.domain.WorkspaceState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.ArrayDeque

data class StudioUiState(
    val workspace: WorkspaceState = WorkspaceState(),
    val references: List<ReferenceAsset> = emptyList(),
    val gallery: List<RenderRecord> = emptyList(),
    val active: RenderRecord? = null,
    val loading: Boolean = false,
    val progress: String = "",
    val error: String? = null,
    val apiKeyPresent: Boolean = false
)

class StudioViewModel(application: Application) : AndroidViewModel(application) {
    private val keyStore = SecureKeyStore(application)
    private val galleryRepo = GalleryRepository(application)
    private val exportRepo = DeviceExportRepository(application)
    private val gemini = GeminiClient(application.contentResolver)
    private val undo = ArrayDeque<WorkspaceState>()
    private val redo = ArrayDeque<WorkspaceState>()

    private val _ui = MutableStateFlow(
        StudioUiState(
            gallery = galleryRepo.list(),
            apiKeyPresent = keyStore.loadApiKey().isNotBlank()
        )
    )
    val ui: StateFlow<StudioUiState> = _ui.asStateFlow()

    fun updateDesign(transform: (DesignState) -> DesignState) {
        pushUndo()
        _ui.value = _ui.value.copy(
            workspace = _ui.value.workspace.copy(
                design = transform(_ui.value.workspace.design)
            )
        )
    }

    fun updateSettings(transform: (StudioSettings) -> StudioSettings) {
        pushUndo()
        _ui.value = _ui.value.copy(
            workspace = _ui.value.workspace.copy(
                settings = transform(_ui.value.workspace.settings)
            )
        )
    }

    private fun pushUndo() {
        undo.addLast(_ui.value.workspace)
        while (undo.size > 80) undo.removeFirst()
        redo.clear()
    }

    fun undo() {
        if (undo.isEmpty()) return
        redo.addLast(_ui.value.workspace)
        _ui.value = _ui.value.copy(workspace = undo.removeLast())
    }

    fun redo() {
        if (redo.isEmpty()) return
        undo.addLast(_ui.value.workspace)
        _ui.value = _ui.value.copy(workspace = redo.removeLast())
    }

    fun addReferences(items: List<ReferenceAsset>) {
        _ui.value = _ui.value.copy(
            references = (_ui.value.references + items).take(5)
        )
    }

    fun clearReferences() {
        _ui.value = _ui.value.copy(references = emptyList())
    }

    fun updateReferenceRole(index: Int, role: com.mindlizzard.feetstudio.domain.ReferenceRole) {
        updateReference(index) { it.copy(role = role) }
    }

    fun updateReferenceStrength(index: Int, strength: com.mindlizzard.feetstudio.domain.ReferenceStrength) {
        updateReference(index) { it.copy(strength = strength) }
    }

    fun updateReferenceEnabled(index: Int, enabled: Boolean) {
        updateReference(index) { it.copy(enabled = enabled) }
    }

    fun removeReference(index: Int) {
        val list = _ui.value.references.toMutableList()
        if (index !in list.indices) return
        list.removeAt(index)
        _ui.value = _ui.value.copy(references = list)
    }

    private fun updateReference(
        index: Int,
        transform: (ReferenceAsset) -> ReferenceAsset
    ) {
        val list = _ui.value.references.toMutableList()
        if (index !in list.indices) return
        list[index] = transform(list[index])
        _ui.value = _ui.value.copy(references = list)
    }

    fun saveApiKey(value: String) {
        keyStore.saveApiKey(value)
        _ui.value = _ui.value.copy(apiKeyPresent = value.isNotBlank())
    }

    fun apiKeyForEditor(): String = keyStore.loadApiKey()

    fun previewContract(): RenderContract =
        RenderEngine.buildContract(_ui.value.workspace, _ui.value.references)

    fun select(record: RenderRecord) {
        _ui.value = _ui.value.copy(active = record)
    }

    fun clearActive() {
        _ui.value = _ui.value.copy(active = null)
    }

    fun generate() {
        if (_ui.value.loading) return

        val apiKey = keyStore.loadApiKey()
        if (apiKey.isBlank()) {
            _ui.value = _ui.value.copy(
                error = "Vul eerst je Gemini API-key in bij Render."
            )
            return
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = null)
            try {
                val batch = _ui.value.workspace.settings.batchCount.coerceIn(1, 4)

                repeat(batch) { index ->
                    _ui.value = _ui.value.copy(
                        progress = "Native render ${index + 1}/$batch"
                    )

                    val contract = previewContract()

                    val bytes = withContext(Dispatchers.IO) {
                        gemini.generate(apiKey, contract, _ui.value.references)
                    }

                    val record = withContext(Dispatchers.IO) {
                        galleryRepo.save(bytes, contract)
                    }

                    val all = withContext(Dispatchers.IO) {
                        galleryRepo.list()
                    }

                    _ui.value = _ui.value.copy(gallery = all, active = record)
                }
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    error = t.message ?: "Generatie mislukt."
                )
            } finally {
                _ui.value = _ui.value.copy(loading = false, progress = "")
            }
        }
    }

    fun targetedFix(target: FixTarget) {
        val active = _ui.value.active ?: return
        val apiKey = keyStore.loadApiKey()

        if (apiKey.isBlank()) {
            _ui.value = _ui.value.copy(error = "Vul eerst je Gemini API-key in.")
            return
        }

        val instruction = when (target) {
            FixTarget.ANATOMY ->
                "Correct only foot anatomy, toe count, proportions, arch and ankle plausibility."
            FixTarget.HOSIERY ->
                "Correct only hosiery coverage, transparency/mesh, fabric tension and clipping."
            FixTarget.NAILS ->
                "Correct only visible toenails, polish placement, shape, color and requested nail art."
            FixTarget.FOOTWEAR ->
                "Correct only footwear geometry, straps, state and clipping."
            FixTarget.POSE ->
                "Correct only physically implausible foot, ankle or leg pose."
            FixTarget.REALISM ->
                "Reduce AI artifacts only. Restore believable skin/material microtexture, sharper edges, realistic shadows and premium photo realism."
        }

        viewModelScope.launch {
            _ui.value = _ui.value.copy(
                loading = true,
                progress = "Targeted Fix: ${target.name.lowercase()}",
                error = null
            )

            try {
                val source = withContext(Dispatchers.IO) {
                    File(active.imagePath).readBytes()
                }

                val contract = previewContract()

                val bytes = withContext(Dispatchers.IO) {
                    gemini.generate(
                        apiKey = apiKey,
                        contract = contract,
                        references = _ui.value.references,
                        sourceImage = source,
                        editInstruction = instruction
                    )
                }

                val record = withContext(Dispatchers.IO) {
                    galleryRepo.save(
                        imageBytes = bytes,
                        contract = contract,
                        parentId = active.id,
                        fixTarget = target.name.lowercase()
                    )
                }

                val all = withContext(Dispatchers.IO) { galleryRepo.list() }
                _ui.value = _ui.value.copy(gallery = all, active = record)
            } catch (t: Throwable) {
                _ui.value = _ui.value.copy(
                    error = t.message ?: "Targeted Fix mislukt."
                )
            } finally {
                _ui.value = _ui.value.copy(loading = false, progress = "")
            }
        }
    }

    fun exportRecordAsPng(record: RenderRecord): String =
        exportRepo.exportPng(record.imagePath, "feet-studio-${record.id}")

    fun exportRecordAsJpg(record: RenderRecord): String =
        exportRepo.exportJpg(record.imagePath, "feet-studio-${record.id}")

    fun exportRecordAs8kPng(record: RenderRecord): String =
        exportRepo.export8kPng(record.imagePath, "feet-studio-${record.id}")

    fun exportRecordAs8kJpg(record: RenderRecord): String =
        exportRepo.export8kJpg(record.imagePath, "feet-studio-${record.id}")

    fun dismissError() {
        _ui.value = _ui.value.copy(error = null)
    }
}
