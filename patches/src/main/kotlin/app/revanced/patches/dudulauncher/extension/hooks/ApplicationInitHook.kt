package app.revanced.patches.dudulauncher.extension.hooks

import app.revanced.patches.shared.misc.extension.extensionHook

internal val applicationInitHook = extensionHook {
    custom { method, classDef ->
        val superClass = classDef.superclass
        if (superClass != null && superClass.equals("Landroid/app/Application;", true)) {
            if (method.name == "onCreate") {
                return@custom true
            }
        }
        false
    }
}