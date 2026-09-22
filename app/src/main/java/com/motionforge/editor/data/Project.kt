package com.motionforge.editor.data

import java.util.UUID

/** Domain-level project model used throughout the UI layer. */
data class Project(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val clips: List<Clip> = emptyList()
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.trimmedDurationMs }
}
