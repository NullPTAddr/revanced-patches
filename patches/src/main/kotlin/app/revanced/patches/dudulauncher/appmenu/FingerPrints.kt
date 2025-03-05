package app.revanced.patches.dudulauncher.appmenu

import app.revanced.patcher.fingerprint

val appListSortingFingerprint = fingerprint {
    parameters("Ljava/lang/String;", "Ljava/lang/String;")
    strings("com.dudu", "com.wow")
    returns("I")
    custom { method, classDef ->
        method.parameters.size == 2 && classDef.superclass == "Lcom/dudu/autoui/manage/ContextEx;"
    }
}

val appListSortingFingerprint2 = fingerprint {
    strings("com.dudu.setting", "com.dudu.action.restart_auto")
    returns("I")
    custom { method, classDef ->
        method.parameters.size == 2 && classDef.superclass == "Lcom/dudu/autoui/manage/ContextEx;"
    }
}