package app.revanced.patches.dudulauncher.signature

import app.revanced.patcher.fingerprint

val applicationFingerprint = fingerprint {
    custom { method, classDef ->
        val superClass = classDef.superclass
        if (superClass != null && superClass.equals("Landroid/app/Application;", true)) {
            if (method.name.equals("<init>", true)) {
                return@custom true
            }
        }
        false
    }
}