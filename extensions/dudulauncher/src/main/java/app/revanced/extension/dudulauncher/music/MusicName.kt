package app.revanced.extension.dudulauncher.music

import android.media.MediaMetadata
import app.revanced.extension.shared.Utils

class MusicName {
    companion object {
        @JvmStatic
        fun musicInfoRename(mediaMetadata: MediaMetadata?): MediaMetadata? {
            if (mediaMetadata == null) return mediaMetadata
            val musicName = mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE).replace(Regex("[(\\[|【「]"), "_")
                .split("_")[0].trim()
            Utils.runOnBackgroundThread {
                LyricFinder.searchLyricAndSave(musicName)
            }
            val newMediaMetadata = MediaMetadata.Builder(mediaMetadata)
                .putString(
                    MediaMetadata.METADATA_KEY_TITLE,
                    musicName
                )
                .build()
            return newMediaMetadata
        }
    }
}