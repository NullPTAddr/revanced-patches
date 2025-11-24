import app.revanced.patcher.fingerprint

val musicTitleFingerprint = fingerprint {
    custom { method, classDef ->
        return@custom classDef.equals("Lcom/dudu/autoui/service/musicInfo/GetMusicInfoService\$b;")
    }
    parameters("Landroid/media/MediaMetadata;")
    strings("android.media.metadata.TITLE")
}