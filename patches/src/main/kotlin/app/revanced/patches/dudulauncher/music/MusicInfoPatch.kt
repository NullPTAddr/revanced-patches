import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans
import app.revanced.patches.dudulauncher.extension.sharedExtensionPatch

@Suppress("unused")
val musicInfoPatch = bytecodePatch(
    "Music Lyric",
    "search music lyric and auto download to selected folder"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    dependsOn(sharedExtensionPatch)
    execute {
        musicTitleFingerprint.method.addInstructions(
            0, """
            invoke-static {p1}, Lapp/revanced/extension/dudulauncher/music/MusicName;->musicInfoRename(Landroid/media/MediaMetadata;)Landroid/media/MediaMetadata;
            move-result-object p1
        """.trimIndent()
        )

        musicLyricLoadingFingerprint.method.addInstructions(
            0, """
            invoke-static {}, Lapp/revanced/extension/dudulauncher/music/LyricFinder;->waitForLyric()V
    """.trimIndent()
        )
    }
}