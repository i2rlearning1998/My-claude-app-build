package com.motionforge.editor

import android.app.Application
import com.motionforge.editor.data.AppDatabase
import com.motionforge.editor.data.ProjectRepository

class MotionForgeApp : Application() {

    val repository: ProjectRepository by lazy {
        ProjectRepository(AppDatabase.getInstance(this).projectDao())
    }
}
