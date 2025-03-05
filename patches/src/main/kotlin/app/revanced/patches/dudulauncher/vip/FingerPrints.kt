package app.revanced.patches.dudulauncher.vip

import app.revanced.patcher.fingerprint

val setVipTypeFingerprint = fingerprint {
    parameters("Ljava/lang/Integer;")
    custom { method, classDef ->
        classDef.type == "Lcom/dudu/autoui/user/LocalUser;" && method.name == "setVipType"
    }
}

val setVipExpireTimeFingerprint = fingerprint {
    parameters("Ljava/lang/Integer;")
    custom { method, classDef ->
        classDef.type == "Lcom/dudu/autoui/user/LocalUser;" && method.name == "setVipExpireTime"
    }
}