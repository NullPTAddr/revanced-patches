package app.revanced.patches.shared.misc.gms

import android.annotation.SuppressLint
import android.app.Application
import android.content.pm.PackageManager
import app.revanced.patcher.Fingerprint
import app.revanced.patcher.extensions.InstructionExtensions.instructions
import app.revanced.patcher.extensions.InstructionExtensions.replaceInstruction
import app.revanced.patcher.patch.*
import app.revanced.patches.all.misc.packagename.changePackageNamePatch
import app.revanced.patches.all.misc.packagename.setOrGetFallbackPackageName
import app.revanced.patches.all.misc.resources.addResources
import app.revanced.patches.all.misc.resources.addResourcesPatch
import app.revanced.patches.shared.misc.gms.Constants.ACTIONS
import app.revanced.patches.shared.misc.gms.Constants.AUTHORITIES
import app.revanced.patches.shared.misc.gms.Constants.PERMISSIONS
import app.revanced.util.getReference
import app.revanced.util.returnEarly
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.builder.instruction.BuilderInstruction21c
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.formats.Instruction21c
import com.android.tools.smali.dexlib2.iface.reference.StringReference
import com.android.tools.smali.dexlib2.immutable.reference.ImmutableStringReference
import com.android.tools.smali.dexlib2.util.MethodUtil
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.security.MessageDigest

private const val PACKAGE_NAME_REGEX_PATTERN = "^[a-zA-Z]\\w*(\\.[a-zA-Z]\\w*)+\$"

var fromPackageName: String = ""
var toPackageName: String = ""

/**
 * A patch that allows patched Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param primeMethodFingerprint The fingerprint of the "prime" method that needs to be patched.
 * @param earlyReturnFingerprints The fingerprints of methods that need to be returned early.
 * @param extensionPatch The patch responsible for the extension.
 * @param gmsCoreSupportResourcePatchFactory The factory for the corresponding resource patch
 * that is used to patch the resources.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */

fun gmsCoreSupportUniversalPatch(
    primeMethodFingerprint: Fingerprint? = null,
    earlyReturnFingerprints: Set<Fingerprint> = setOf(),
    extensionPatch: Patch<*>,
    gmsCoreSupportResourcePatchFactory: (gmsCoreVendorGroupIdOption: Option<String>) -> Patch<*>,
    executeBlock: BytecodePatchContext.() -> Unit = {},
    block: BytecodePatchBuilder.() -> Unit = {},
) = bytecodePatch(
    name = "GmsCore support universal",
    description = "Allows patched Google apps to run without root and under a different package name " +
            "by using GmsCore instead of Google Play Services.",
) {
    val gmsCoreVendorGroupIdOption = stringOption(
        key = "gmsCoreVendorGroupId",
        default = "app.revanced",
        values =
        mapOf(
            "ReVanced" to "app.revanced",
        ),
        title = "GmsCore vendor group ID",
        description = "The vendor's group ID for GmsCore.",
        required = true,
    ) { it!!.matches(Regex(PACKAGE_NAME_REGEX_PATTERN)) }

    dependsOn(
        changePackageNamePatch,
        gmsCoreSupportResourcePatchFactory(gmsCoreVendorGroupIdOption),
        extensionPatch,
    )

    val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

    execute {
        fun transformStringReferences(transform: (str: String) -> String?) = classes.forEach {
            val mutableClass by lazy {
                proxy(it).mutableClass
            }

            it.methods.forEach classLoop@{ method ->
                val implementation = method.implementation ?: return@classLoop

                val mutableMethod by lazy {
                    mutableClass.methods.first { MethodUtil.methodSignaturesMatch(it, method) }
                }

                implementation.instructions.forEachIndexed insnLoop@{ index, instruction ->
                    val string = ((instruction as? Instruction21c)?.reference as? StringReference)?.string
                        ?: return@insnLoop

                    // Apply transformation.
                    val transformedString = transform(string) ?: return@insnLoop

                    mutableMethod.replaceInstruction(
                        index,
                        BuilderInstruction21c(
                            Opcode.CONST_STRING,
                            instruction.registerA,
                            ImmutableStringReference(transformedString),
                        ),
                    )
                }
            }
        }

        // region Collection of transformations that are applied to all strings.

        fun commonTransform(referencedString: String): String? =
            when (referencedString) {
                "com.google",
                "com.google.android.gms",
                in PERMISSIONS,
                in ACTIONS,
                in AUTHORITIES,
                -> referencedString.replace("com.google", gmsCoreVendorGroupId!!)

                // No vendor prefix for whatever reason...
                "subscribedfeeds" -> "$gmsCoreVendorGroupId.subscribedfeeds"
                else -> null
            }

        fun contentUrisTransform(str: String): String? {
            // only when content:// uri
            if (str.startsWith("content://")) {
                // check if matches any authority
                for (authority in AUTHORITIES) {
                    val uriPrefix = "content://$authority"
                    if (str.startsWith(uriPrefix)) {
                        return str.replace(
                            uriPrefix,
                            "content://${authority.replace("com.google", gmsCoreVendorGroupId!!)}",
                        )
                    }
                }

                // gms also has a 'subscribedfeeds' authority, check for that one too
                val subFeedsUriPrefix = "content://subscribedfeeds"
                if (str.startsWith(subFeedsUriPrefix)) {
                    return str.replace(subFeedsUriPrefix, "content://$gmsCoreVendorGroupId.subscribedfeeds")
                }
            }

            return null
        }

        fun packageNameTransform(fromPackageName: String, toPackageName: String): (String) -> String? = { string ->
            when (string) {
                "$fromPackageName.SuggestionsProvider",
                "$fromPackageName.fileprovider",
                -> string.replace(fromPackageName, toPackageName)

                else -> null
            }
        }

        fun transformPrimeMethod(packageName: String) {
            primeMethodFingerprint!!.method.apply {
                var register = 2

                val index = instructions.indexOfFirst {
                    if (it.getReference<StringReference>()?.string != fromPackageName) return@indexOfFirst false

                    register = (it as OneRegisterInstruction).registerA
                    return@indexOfFirst true
                }

                replaceInstruction(index, "const-string v$register, \"$packageName\"")
            }
        }

        // endregion

        val packageName = setOrGetFallbackPackageName(toPackageName)

        // Transform all strings using all provided transforms, first match wins.
        val transformations = arrayOf(
            ::commonTransform,
            ::contentUrisTransform,
            packageNameTransform(fromPackageName, packageName),
        )
        transformStringReferences transform@{ string ->
            transformations.forEach { transform ->
                transform(string)?.let { transformedString -> return@transform transformedString }
            }

            return@transform null
        }

        // Specific method that needs to be patched.
        primeMethodFingerprint?.let { transformPrimeMethod(packageName) }

        // Return these methods early to prevent the app from crashing.
        earlyReturnFingerprints.forEach { it.method.returnEarly() }
        serviceCheckFingerprint.method.returnEarly()
        if (googlePlayUtilityFingerprint2.methodOrNull != null) {
            googlePlayUtilityFingerprint2.method.returnEarly()
        }

        // Google Play Utility is not present in all apps, so we need to check if it's present.
        if (googlePlayUtilityFingerprint.methodOrNull != null) {
            googlePlayUtilityFingerprint.method.returnEarly()
        }

        if (googlePlayUtilityFingerprint2.methodOrNull != null) {
            googlePlayUtilityFingerprint2.method.returnEarly()
        }

        // Change the vendor of GmsCore in the extension.
        gmsCoreSupportFingerprint.classDef.methods
            .single { it.name == GET_GMS_CORE_VENDOR_GROUP_ID_METHOD_NAME }
            .replaceInstruction(0, "const-string v0, \"$gmsCoreVendorGroupId\"")

        executeBlock()
    }

    block()
}

/**
 * Abstract resource patch that allows Google apps to run without root and under a different package name
 * by using GmsCore instead of Google Play Services.
 *
 * @param gmsCoreVendorGroupIdOption The option to get the vendor group ID of GmsCore.
 * @param executeBlock The additional execution block of the patch.
 * @param block The additional block to build the patch.
 */

fun gmsCoreSupportResourceUniversalPatch(
    gmsCoreVendorGroupIdOption: Option<String>,
    executeBlock: ResourcePatchContext.() -> Unit = {},
    block: ResourcePatchBuilder.() -> Unit = {},
) = resourcePatch {
    dependsOn(
        changePackageNamePatch,
        addResourcesPatch,
    )

    val gmsCoreVendorGroupId by gmsCoreVendorGroupIdOption

    execute {
        addResources("shared", "misc.gms.gmsCoreSupportResourcePatch")

        /**
         * Add metadata to manifest to support spoofing the package name and signature of GmsCore.
         */
        fun addSpoofingMetadata() {
            fun Node.adoptChild(
                tagName: String,
                block: Element.() -> Unit,
            ) {
                val child = ownerDocument.createElement(tagName)
                child.block()
                appendChild(child)
            }

            document("AndroidManifest.xml").use { document ->
                val sha1Signature = getPackageSignature(get("../../in.apk").path)
                fromPackageName = document.documentElement.getAttribute("package")
                toPackageName = "$fromPackageName.revanced"
                val applicationNode =
                    document
                        .getElementsByTagName("application")
                        .item(0)

                // Spoof package name and signature.
                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_NAME")
                    setAttribute("android:value", fromPackageName)
                }

                applicationNode.adoptChild("meta-data") {
                    setAttribute("android:name", "$gmsCoreVendorGroupId.android.gms.SPOOFED_PACKAGE_SIGNATURE")
                    setAttribute("android:value", sha1Signature)
                }

                // GmsCore presence detection in extension.
                applicationNode.adoptChild("meta-data") {
                    // TODO: The name of this metadata should be dynamic.
                    setAttribute("android:name", "app.revanced.MICROG_PACKAGE_NAME")
                    setAttribute("android:value", "$gmsCoreVendorGroupId.android.gms")
                }
            }
        }

        /**
         * Patch the manifest to support GmsCore.
         */
        fun patchManifest() {
            val packageName = setOrGetFallbackPackageName(toPackageName)

            val transformations = mapOf(
                "package=\"$fromPackageName" to "package=\"$packageName",
                "android:authorities=\"$fromPackageName" to "android:authorities=\"$packageName",
                "$fromPackageName.permission.C2D_MESSAGE" to "$packageName.permission.C2D_MESSAGE",
                "$fromPackageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION" to "$packageName.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION",
                "com.google.android.c2dm" to "$gmsCoreVendorGroupId.android.c2dm",
                "com.google.android.libraries.photos.api.mars" to "$gmsCoreVendorGroupId.android.apps.photos.api.mars",
                "</queries>" to "<package android:name=\"$gmsCoreVendorGroupId.android.gms\"/></queries>",
            )

            val manifest = get("AndroidManifest.xml")
            manifest.writeText(
                transformations.entries.fold(manifest.readText()) { acc, (from, to) ->
                    acc.replace(
                        from,
                        to,
                    )
                },
            )
        }

        addSpoofingMetadata()
        patchManifest()

        executeBlock()
    }

    block()
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

    // SHA-1 digest
    val md = MessageDigest.getInstance("SHA1")
    val sha1Bytes = md.digest(cert)

    // Convert to hex string
    return sha1Bytes.joinToString("") { "%02X".format(it) }
}