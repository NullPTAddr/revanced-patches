package app.revanced.patches.dudulauncher.pip

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans

@Suppress("unused")
val pipEventInjectionSmoothTouchPatch = bytecodePatch(
    "Pip Smooth Touch"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        injectInputEventFingerprint.method.addInstructions(
            0,
            """
            const/4 p2, 0x0
        """.trimIndent()
        )
    }
}