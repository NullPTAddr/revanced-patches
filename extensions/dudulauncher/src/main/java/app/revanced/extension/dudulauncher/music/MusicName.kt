package app.revanced.extension.dudulauncher.music

import android.media.MediaMetadata

class MusicName {
    companion object {
        @JvmStatic
        fun musicInfoRename(mediaMetadata: MediaMetadata): MediaMetadata {
            val newMediaMetadata = MediaMetadata.Builder(mediaMetadata)
                .putString(
                    MediaMetadata.METADATA_KEY_TITLE,
                    mediaMetadata.getString(MediaMetadata.METADATA_KEY_TITLE).replace(Regex("[\\\\/:*?\"<>|]"), "_")
                )
                .build()
            return newMediaMetadata
        }
    }
}