// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.idea.maven.project

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.jetbrains.idea.maven.model.MavenModel
import org.jetbrains.idea.maven.server.MavenRemoteObjectWrapper
import org.jetbrains.idea.maven.server.MavenServerResultTransformer
import org.jetbrains.idea.maven.server.RemotePathTransformerFactory
import java.io.File
import java.nio.file.Path

open class MavenProjectModelServerModelReadHelper(protected val myProject: Project) : MavenProjectModelReadHelper {
  override suspend fun interpolate(basedir: Path,
                                   mavenModuleFile: VirtualFile,
                                   model: MavenModel): MavenModel {
    val manager = MavenProjectsManager.getInstance(myProject).embeddersManager
    val embedder = manager.getEmbedder(MavenEmbeddersManager.FOR_MODEL_READ, basedir.toString())
    val pomDir = mavenModuleFile.parent.toNioPath()
    val transformer = RemotePathTransformerFactory.createForProject(myProject)
    val targetPomDir = File(transformer.toRemotePathOrSelf(pomDir.toString()))
    val m = embedder.interpolateAndAlignModel(model, targetPomDir, MavenRemoteObjectWrapper.ourToken)
    MavenServerResultTransformer.transformPaths(transformer, m)
    return m
  }

  override suspend fun assembleInheritance(basedir: Path, parent: MavenModel, model: MavenModel, mavenModuleFile: VirtualFile): MavenModel {
    val manager = MavenProjectsManager.getInstance(myProject).embeddersManager
    val embedder = manager.getEmbedder(MavenEmbeddersManager.FOR_MODEL_READ, basedir.toString())
    return embedder.assembleInheritance(model, parent, MavenRemoteObjectWrapper.ourToken)
  }

  override fun filterModules(modules: List<String>, mavenModuleFile: VirtualFile): List<String> {
    return modules;
  }
}
