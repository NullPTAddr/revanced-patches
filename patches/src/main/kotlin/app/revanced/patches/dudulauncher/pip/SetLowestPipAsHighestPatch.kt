package app.revanced.patches.dudulauncher.pip

import app.revanced.patcher.extensions.InstructionExtensions.getInstruction
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.ArrayPayload
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21s

@Suppress("unused")
val setLowestPipAsHighestPatch = bytecodePatch(
    "Lowest as Highest"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        pipDipLevelFingerprint.classDef.methods.apply {
            forEach method@{ mutableMethod ->
                mutableMethod.instructions.forEachIndexed { index, builderInstruction ->
                    when (builderInstruction) {
                        is Instruction21s -> {
                            if (builderInstruction.narrowLiteral == 0x90) {
                                val register = mutableMethod.getInstruction<OneRegisterInstruction>(index).registerA
                                mutableMethod.replaceInstruction(
                                    index, """
                                    const/16 v$register, 0x140
                                """.trimIndent()
                                )
                            }
                        }

                        is ArrayPayload -> {
                            mutableMethod.replaceInstruction(
                                index, """
                                .array-data 4
                                        0x140
                                        0xb0
                                        0xd0
                                        0xf0
                                    .end array-data

                            """.trimIndent()
                            )
                        }

                        else -> {}
                    }
                }
            }
        }
    }
}