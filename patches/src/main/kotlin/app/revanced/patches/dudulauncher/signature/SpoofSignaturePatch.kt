package app.revanced.patches.dudulauncher.signature

import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans
import app.revanced.patches.dudulauncher.extension.sharedExtensionPatch

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    name = "Spoof Signature"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    dependsOn(sharedExtensionPatch)
    execute {
        applicationFingerprint.method.addInstructions(0, "invoke-static {}, Lapp/revanced/extension/dudulauncher/signature/SignaturePatch;->killPM()V")
    }
}