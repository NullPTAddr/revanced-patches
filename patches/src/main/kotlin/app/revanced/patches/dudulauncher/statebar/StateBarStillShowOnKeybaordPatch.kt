package app.revanced.patches.dudulauncher.statebar

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction31i

@Suppress("unused")
val stateBarStillShowOnKeybaordPatch = bytecodePatch(
    "fix StateBar"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        statusBarStillShowOnKeybaordFingerprint.method.apply {
            instructions.forEachIndexed { index, builderInstruction ->
                if (builderInstruction !is Instruction31i) return@forEachIndexed
                if (builderInstruction.narrowLiteral == 0x20528) {
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    replaceInstruction(
                        index, """
                            const v$register, 0x10528
                        """.trimIndent()
                    )
                    return@apply
                }
            }
        }
        stateBarStillShowOnKeybaordFingerprint.method.apply {
            instructions.forEachIndexed { index, builderInstruction ->
                if (builderInstruction !is Instruction31i) return@forEachIndexed
                if (builderInstruction.narrowLiteral == 0x20528) {
                    val register = getInstruction<OneRegisterInstruction>(index).registerA
                    replaceInstruction(
                        index, """
                            const v$register, 0x10528
                        """.trimIndent()
                    )
                    return@apply
                }
            }
        }
    }
}