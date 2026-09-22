package com.motionforge.editor.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import java.util.UUID

class ProjectRepository(private val dao: ProjectDao) {

    private val json = Json { ignoreUnknownKeys = true }

    fun observeProjects(): Flow<List<Project>> =
        dao.observeProjects().map { entities -> entities.map { it.toDomain(json) } }

    fun observeProject(id: String): Flow<Project?> =
        dao.observeProject(id).map { it?.toDomain(json) }

    suspend fun getProject(id: String): Project? = dao.getProject(id)?.toDomain(json)

    suspend fun createProject(name: String): Project {
        val now = System.currentTimeMillis()
        val project = Project(
            id = UUID.randomUUID().toString(),
            name = name.ifBlank { "Untitled Project" },
            createdAtMs = now,
            updatedAtMs = now,
            clips = emptyList()
        )
        dao.upsert(project.toEntity(json))
        return project
    }

    suspend fun saveProject(project: Project) {
        dao.upsert(project.copy(updatedAtMs = System.currentTimeMillis()).toEntity(json))
    }

    suspend fun deleteProject(project: Project) {
        dao.delete(project.toEntity(json))
    }
}
