package app.revanced.extension.dudulauncher

@Suppress("unused")
class Utils {
    companion object {
        @JvmStatic
        fun mapsDrivingMode(_className: String): String {
            var className = _className
            if (className.equals("com.google.android.maps.MapsActivity", true)) {
                className = "com.google.android.maps.MapsActivity -a android.intent.action.VIEW -d google.navigation:?free=1&mode=d&entry=fnls"
            }
            return className
        }
    }
}