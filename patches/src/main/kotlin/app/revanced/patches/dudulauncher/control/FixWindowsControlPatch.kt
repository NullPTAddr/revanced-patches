package app.revanced.patches.dudulauncher.control

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch

@Suppress("unused")
val fixWindowsControlPatch = bytecodePatch(
    name = "Fix Windows Control"
) {
    execute {
        windowsControlFingerprint.method.addInstructions(
            0,
            """
                
            """.trimIndent()
        )
    }
}