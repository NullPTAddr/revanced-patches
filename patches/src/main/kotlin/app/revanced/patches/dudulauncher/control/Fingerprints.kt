package app.revanced.patches.dudulauncher.control

import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.fingerprint

val windowsControlFingerprint = fingerprint {
    parameters("[I", "[I")
    returns("Z")
    val keywords = listOf(
        "BODYWORK_LF_WINDOW_CTRL_SET",
        "BODYWORK_RF_WINDOW_CTRL_SET",
        "BODYWORK_LR_WINDOW_CTRL_SET",
        "BODYWORK_RR_WINDOW_CTRL_SET"
    )
    custom { method, classDef ->
        method.instructions.any { ins ->
            keywords.all { key -> ins.toString().contains(key) }
        }
    }
}