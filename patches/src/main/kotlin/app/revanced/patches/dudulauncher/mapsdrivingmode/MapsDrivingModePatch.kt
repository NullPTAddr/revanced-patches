package app.revanced.patches.dudulauncher.mapsdrivingmode

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans

@Suppress("unused")
val mapsDrivingModePatch = bytecodePatch(
    "Maps Driving Mode"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        mapsDrivingModeFingerprint.method.addInstructions(
            0, """
            invoke-static {p1}, Lapp/revanced/extension/dudulauncher/Utils;->mapsDrivingMode(Ljava/lang/String;)Ljava/lang/String;
            move-result-object p1
        """.trimIndent()
        )
    }
}