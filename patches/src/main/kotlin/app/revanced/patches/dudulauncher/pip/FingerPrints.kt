package app.revanced.patches.dudulauncher.pip

import app.revanced.patcher.fingerprint

val pipDipLevelFingerprint = fingerprint {
    parameters("Ljava/lang/Integer;")
    strings("SDATA_PIP_DPI_LEVEL")
}

val injectInputEventFingerprint = fingerprint {
    parameters("Landroid/view/InputEvent;", "I")
    returns("Z")
}