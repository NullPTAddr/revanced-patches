package app.revanced.extension.dudulauncher.music

import android.content.Context
import app.revanced.extension.shared.Utils
import app.revanced.extension.shared.requests.Requester
import app.revanced.extension.shared.requests.Route
import java.io.File

class LyricFinder {

    companion object {

        private var hasLyric = false

        const val BASE_URL = "https://lrclib.net/api"
        private var curSearching = ""

        @JvmStatic
        fun searchLyricAndSave(query: String?) {
            if (query.isNullOrBlank() || query == curSearching) return
            hasLyric = false
            curSearching = query

            val sharedPreferences = Utils.getContext().getSharedPreferences("settings", Context.MODE_PRIVATE)
            val lyricsDir = sharedPreferences.getString("SDATA_MUSIC_LRC_LOCAL_PATH", null)
            if (lyricsDir.isNullOrBlank()) return

            val filePath = "$lyricsDir/$query.lrc"
            if (File(filePath).exists()) {
                hasLyric = true
                return
            }

            try {
                val route = Route(Route.Method.GET, "/search" + "?q=${query}").compile()
                val connection = Requester.getConnectionFromCompiledRoute(BASE_URL, route)
                connection.connectTimeout = 10_000
                connection.readTimeout = 10_000
                Utils.showToastShort("Searching: $query")
                if (connection.responseCode == 200) {
                    val jsonObject = Requester.parseJSONArrayAndDisconnect(connection)
                    if (jsonObject.length() == 0) {
                        Utils.showToastShort("Not Found: $query")
                        return
                    }


                    for (index in 0 until jsonObject.length()) {
                        val syncedLyrics = try {
                            jsonObject.getJSONObject(index).getString("syncedLyrics")
                        } catch (e: Exception) {
                            null
                        }
                        if (syncedLyrics != null && !syncedLyrics.equals("null", true)) {
                            saveLyricFile(filePath, syncedLyrics)
                            hasLyric = true
                            return
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            Utils.showToastShort("Not Found: $query")
        }

        @JvmStatic
        fun saveLyricFile(filePath: String, lyric: String) {
            val saveFile = File(filePath)
            saveFile.writeText(lyric)
            Utils.showToastShort("Saved: $filePath")
        }

        @JvmStatic
        fun waitForLyric() {
            val timeout = 10_000
            val start = System.currentTimeMillis()

            while (!hasLyric && System.currentTimeMillis() - start < timeout) {
                Thread.sleep(1_000) // prevent CPU burn
            }
        }
    }
}