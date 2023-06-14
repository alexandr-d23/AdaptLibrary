package com.diner.plugin.bytecode

import javassist.ClassPool
import javassist.CtClass

object Utils {

    fun hasParent(className: String?, parents: List<String>, classPool: ClassPool): Boolean {
        println("Has parent ")
        if (className == null) {
            return false
        }
        try {
            var currentClass: CtClass? = classPool.get(className.replace("/", "."))

            while (currentClass != null) {
                if (parents.contains(currentClass.name)) {
                    return true
                }
                currentClass = currentClass.superclass
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }

    fun extendsActivity(
        cl: CtClass
    ): Boolean{
        val parents = listOf(
            "android.app.Activity",
            "androidx.appcompat.app.AppCompatActivity"
        )
        try {
            var currentClass: CtClass? = cl
            while (currentClass != null) {
                if (parents.contains(currentClass.name)) {
                    return true
                }
                currentClass = currentClass.superclass
            }
            return false
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
}