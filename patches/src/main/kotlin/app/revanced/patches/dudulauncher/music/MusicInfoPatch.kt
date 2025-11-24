import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patches.dudulauncher.Constans
import app.revanced.patches.dudulauncher.extension.sharedExtensionPatch

@Suppress("unused")
val musicInfoPatch = bytecodePatch(
    "Music name"
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
    }
}