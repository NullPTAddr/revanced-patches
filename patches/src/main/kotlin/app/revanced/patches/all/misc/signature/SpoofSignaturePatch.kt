package app.revanced.patches.all.misc.signature

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.signature.applicationFingerprint

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    name = "Spoof Signature",
    use = false
) {
    extendWith("extensions/shared.rve")

    execute {
        applicationFingerprint.method.addInstructions(
            0,
            """
                nop
                nop
            """.trimIndent()
        )
    }
}