package app.revanced.patches.shared

import app.revanced.patcher.fingerprint

internal val castContextFetchFingerprint = fingerprint {
    strings("Error fetching CastContext.")
}

internal val primeMethodFingerprint = fingerprint {
    strings("com.google.android.GoogleCamera", "com.android.vending")
}

internal val applicationFingerprint = fingerprint {
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