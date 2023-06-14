package com.diner.plugin.bytecode

import com.android.SdkConstants
import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import javassist.ClassPool
import javassist.CtClass
import javassist.CtField
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class VoicePlugin : Transform(), Plugin<Project> {

    private var isLibrary = false
    private val pool = ClassPool.getDefault()
    private lateinit var project: Project

    @Override
    override fun apply(target: Project) {
        this.project = target
        val ext = project.extensions
        val android =
            if (project.plugins.hasPlugin(LibraryPlugin::class.java)) {
                ext.getByType(LibraryExtension::class.java)
            } else {
                ext.getByType(AppExtension::class.java)
            }
        android.registerTransform(this)
    }

    override fun getName(): String {
        return "assist"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        if (isLibrary) {
            return TransformManager.PROJECT_ONLY
        }
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return false
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        project.extensions.findByType(AppExtension::class.java)!!.bootClasspath.forEach {
            pool.appendClassPath(it.absolutePath)
        }
        transformInvocation?.inputs?.forEach { transform ->
            transform.jarInputs.forEach {
                pool.insertClassPath(it.file.absolutePath)
                val dest = transformInvocation.outputProvider.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.JAR
                )
                FileUtils.copyFile(it.file, dest)
            }
            transform.directoryInputs.forEach {
                val preFileName = it.file.absolutePath
                println("preFileName" + preFileName)
                pool.insertClassPath(preFileName)
                findTarget(it.file, preFileName)
                val dest = transformInvocation.outputProvider.getContentLocation(
                    it.name,
                    it.contentTypes,
                    it.scopes,
                    Format.DIRECTORY
                )

                FileUtils.copyDirectory(it.file, dest)
            }
        }
    }

    fun findTarget(dir: File, fileName: String) {
        if (dir.isDirectory) {
            dir.listFiles()?.forEach {
                findTarget(it, fileName)
            }
        } else {
            modify(dir, fileName)
        }
    }

    fun modify(dir: File, fileName: String) {
        val filePath = dir.absolutePath

        if (!filePath.endsWith(SdkConstants.DOT_CLASS)) {
            return
        }
        if (filePath.contains("R$") || filePath.contains("R.class")
            || filePath.contains("BuildConfig.class")
        ) {
            return
        }
        val className = filePath.replace(fileName, "")
            .replace("\\", ".")
            .replace("/", ".")
        val name = className.replace(SdkConstants.DOT_CLASS, "")
            .substring(1)
        val activity: CtClass = pool.get(name)
        if (!Utils.extendsActivity(activity)) return
        activity.defrost()
        val plugin = ClassPool.getDefault().get("com.diner.voice_assistant.VoiceAdapter")
        val voiceField = "voiceAssistant194856"
        val hasField = activity.fields.find {
            it.name == voiceField
        } != null
        if (hasField) return
        val field = CtField(plugin, voiceField, activity)
        activity.addField(field)
        activity.methods.forEach {
            println("Method: " + it.name)
            if (it.name == "onCreate") {

                println("FOUND ${activity.name}")
                val init =
                    "$voiceField = new com.diner.voice_assistant.VoiceAdapter(\$0);"
                val invokeStart =
                    "$voiceField.start(\$0.getWindow().getDecorView().getRootView());"
                val code = init + invokeStart
                it.insertAfter(code)
            }
        }
        activity.writeFile(fileName)
    }
}