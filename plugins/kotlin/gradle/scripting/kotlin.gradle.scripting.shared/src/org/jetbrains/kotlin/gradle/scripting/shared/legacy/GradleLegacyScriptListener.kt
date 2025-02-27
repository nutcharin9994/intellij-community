// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

package org.jetbrains.kotlin.gradle.scripting.shared.legacy

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.kotlin.gradle.scripting.shared.isGradleKotlinScript
import org.jetbrains.kotlin.gradle.scripting.shared.roots.GradleBuildRootsManager
import org.jetbrains.kotlin.idea.core.script.configuration.listener.ScriptChangeListener

// called from GradleScriptListener
// todo(gradle6): remove
class GradleLegacyScriptListener(project: Project) : ScriptChangeListener(project) {
    private val buildRootsManager
        get() = GradleBuildRootsManager.getInstanceSafe(project)

    override fun isApplicable(vFile: VirtualFile) =
        isGradleKotlinScript(vFile)

    override fun editorActivated(vFile: VirtualFile) =
        checkUpToDate(vFile)

    override fun documentChanged(vFile: VirtualFile) =
        checkUpToDate(vFile)

    private fun checkUpToDate(vFile: VirtualFile) {
        if (!buildRootsManager.isAffectedGradleProjectFile(vFile.path)) return

        val file = getAnalyzableKtFileForScript(vFile)
        if (file != null) {
            // *.gradle.kts file was changed
            default.ensureUpToDatedConfigurationSuggested(file)
        }
    }
}