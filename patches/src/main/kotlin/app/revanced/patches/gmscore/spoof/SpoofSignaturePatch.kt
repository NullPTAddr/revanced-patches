package app.revanced.patches.gmscore.spoof

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch

@Suppress("unused")
val spoofSignaturePatch = resourcePatch(
    "Spoof Signature"
) {
    compatibleWith("com.google.android.gms")
    dependsOn(sharedExtensionPatch("gmscore"))
    execute {
        val flag = "android:name"

        document("AndroidManifest.xml").use { document ->
            with(document.getElementsByTagName("application").item(0)) {
                if (attributes.getNamedItem(flag) != null) return@with

                document.createAttribute(flag)
                    .apply { value = "app.revanced.extension.gmscore.spoof.SignaturePatch" }
                    .let(attributes::setNamedItem)
            }
        }
    }
}