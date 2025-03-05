package app.revanced.patches.dudulauncher.mapsdrivingmode

import app.revanced.patcher.fingerprint

val mapsDrivingModeFingerprint = fingerprint {
    strings("moveAppToDisplay:")
}