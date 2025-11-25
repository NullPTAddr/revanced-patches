package app.revanced.patches.shared.misc.spoof

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import android.util.Base64
import app.revanced.patcher.extensions.InstructionExtensions.addInstructions
import app.revanced.patcher.patch.bytecodePatch
import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.applicationFingerprint
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.util.getNode
import org.w3c.dom.Element

@Suppress("unused")
val spoofSignaturePatch = bytecodePatch(
    "Spoof Signature Universal",
    use = false
) {
    dependsOn(
        resourceMappingPatch
    )

    var rawSignatureBase64: String? = null
    var originalPackageName: String? = null

    resourcePatch {
        execute {

            document("AndroidManifest.xml").use { document ->
                val manifest = document.getNode("manifest") as Element
                originalPackageName = manifest.getAttribute("package")
                rawSignatureBase64 = getPackageSignature(get("../../in.apk").path)
            }

        }
    }

    if (rawSignatureBase64 == null) {
        throw Exception("Cant find original signature!!!")
    }

    execute {
        execute {
            applicationFingerprint.method.addInstructions(
                0, """
                const-string v0, $originalPackageName
                const-string v1, $rawSignatureBase64
                invoke-static {v0, v1}, Lapp/revanced/extension/shared/spoof/SpoofSignaturePatch;->killPM(Ljava/lang/String;Ljava/lang/String;)V
            """.trimIndent()
            )
        }
    }
}

@SuppressLint("PrivateApi")
internal fun getPackageManager(): PackageManager {
    val activityThreadClass = Class.forName("android.app.ActivityThread")
    val application = activityThreadClass.getDeclaredMethod("currentApplication").invoke(null) as Application
    val packageManager = application.packageManager
    return packageManager
}

internal fun getPackageSignature(apkPath: String): String? {
    // For API 33+ use PackageInfoFlags
    val pm = getPackageManager()
    val packageInfo = if (android.os.Build.VERSION.SDK_INT >= 33) {
        pm.getPackageArchiveInfo(
            apkPath,
            PackageManager.PackageInfoFlags.of(PackageManager.GET_SIGNING_CERTIFICATES.toLong())
        )
    } else {
        pm.getPackageArchiveInfo(
            apkPath,
            PackageManager.GET_SIGNING_CERTIFICATES
        )
    } ?: return null

    val signingInfo = packageInfo.signingInfo ?: return null
    val signatures = signingInfo.apkContentsSigners

    if (signatures.isEmpty()) return null
    val cert = signatures[0].toByteArray()

    return Base64.encodeToString(cert, Base64.NO_WRAP)
}