package app.revanced.patches.dudulauncher.vip

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans

@Suppress("unused")
val vipPatch = bytecodePatch(
    name = "Vip Patch"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        setVipTypeFingerprint.method.addInstructions(
            0, """
            const p1, 0x1
            invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object p1
        """.trimIndent()
        )

        setVipExpireTimeFingerprint.method.addInstructions(
            0, """
            const p1, 0x7fffffff
            invoke-static {p1}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;
            move-result-object p1
        """.trimIndent()
        )
    }
}