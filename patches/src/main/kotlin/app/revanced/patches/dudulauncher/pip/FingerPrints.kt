package app.revanced.patches.dudulauncher.pip

import app.revanced.patcher.fingerprint

val pipDipLevelFingerprint = fingerprint {
    parameters("Ljava/lang/Integer;")
    strings("SDATA_PIP_DPI_LEVEL")
}