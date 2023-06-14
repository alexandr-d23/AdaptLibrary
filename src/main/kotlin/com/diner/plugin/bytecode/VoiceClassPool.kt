package com.diner.plugin.bytecode

import com.android.build.api.transform.TransformInput
import com.android.build.gradle.AppExtension
import javassist.ClassPath
import javassist.ClassPool
import org.gradle.api.Project
import java.io.Closeable

class VoiceClassPool(
    project: Project,
    inputs: Collection<TransformInput>,
    referencedInputs: Collection<TransformInput>
) : ClassPool(), Closeable {

    private val pathElements = ArrayList<ClassPath>()

    init {
        pathElements.add(appendSystemPath())
        pathElements.add(
            appendClassPath(project.extensions.findByType(AppExtension::class.java)!!.bootClasspath[0].toString())
        )
        inputs.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                pathElements.add(insertClassPath(directoryInput.file.absolutePath))
            }
            input.jarInputs.forEach { jarInput ->
                pathElements.add(insertClassPath(jarInput.file.absolutePath))
            }
        }
        referencedInputs.forEach { input ->
            input.directoryInputs.forEach { directoryInput ->
                pathElements.add(insertClassPath(directoryInput.file.absolutePath))
            }

            input.jarInputs.forEach { jarInput ->
                pathElements.add(insertClassPath(jarInput.file.absolutePath))
            }
        }
    }

    override fun close() {
        val iterator = pathElements.iterator ()
        while (iterator.hasNext()) {
            val classPath = iterator.next ()
            removeClassPath(classPath)
            iterator.remove()
        }
    }
}