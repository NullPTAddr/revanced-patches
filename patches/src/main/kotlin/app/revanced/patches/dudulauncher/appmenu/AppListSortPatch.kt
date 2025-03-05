package app.revanced.patches.dudulauncher.appmenu

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans
import app.revanced.util.getReference
import com.android.tools.smali.dexlib2.iface.reference.Reference

@Suppress("unused")
val sortingAppMenuPatch = bytecodePatch("Sort App") {
//    compatibleWith(Constans.PACKAGE_NAME("比亚迪专享版1.013022"))
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        appListSortingFingerprint.method.apply {
            instructions.forEachIndexed { index, builderInstruction ->
                if (builderInstruction.getReference<Reference>()
                        .toString() == "Ljava/lang/String;->compareTo(Ljava/lang/String;)I"
                ) {
                    replaceInstruction(
                        index, """
                        invoke-virtual {p1, p2}, Ljava/lang/String;->compareTo(Ljava/lang/String;)I
                    """.trimIndent()
                    )
                }
            }
        }

        val size = appListSortingFingerprint2.method.instructions.size
        appListSortingFingerprint2.method.apply {
            replaceInstruction(size - 5, "nop")
            replaceInstruction(size - 4, "nop")
            val params = parameters[0].toString()
            val fieldName = classes.find { it.type == params }
                ?.fields?.firstOrNull { it.type == "Ljava/lang/CharSequence;" }
                ?.name.orEmpty()
            addInstructions(
                size - 3, """
                iget-object p1, p1, $params->$fieldName:Ljava/lang/CharSequence;
                invoke-virtual {p1}, Ljava/lang/Object;->toString()Ljava/lang/String;
                move-result-object p1
                
                iget-object p2, p2, $params->$fieldName:Ljava/lang/CharSequence;
                invoke-virtual {p2}, Ljava/lang/Object;->toString()Ljava/lang/String;
                move-result-object p2
            """.trimIndent()
            )
        }
    }
}