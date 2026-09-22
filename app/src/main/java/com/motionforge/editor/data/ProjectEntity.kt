package com.motionforge.editor.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey val id: String,
    val name: String,
    val createdAtMs: Long,
    val updatedAtMs: Long,
    val clipsJson: String
)

fun ProjectEntity.toDomain(json: Json): Project = Project(
    id = id,
    name = name,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
    clips = if (clipsJson.isBlank()) emptyList() else json.decodeFromString(clipsJson)
)

fun Project.toEntity(json: Json): ProjectEntity = ProjectEntity(
    id = id,
    name = name,
    createdAtMs = createdAtMs,
    updatedAtMs = updatedAtMs,
    clipsJson = json.encodeToString(clips)
)
