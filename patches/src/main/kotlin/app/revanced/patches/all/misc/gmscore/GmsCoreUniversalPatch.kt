package app.revanced.patches.all.misc.gmscore

import app.revanced.patcher.patch.Option
import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.shared.misc.gms.gmsCoreSupportUniversalPatch

@Suppress("unused")
val gmsCoreSupportPatch = gmsCoreSupportUniversalPatch(
    extensionPatch = sharedExtensionPatch(),
    gmsCoreSupportResourcePatchFactory = ::gmsCoreSupportResourcePatch,
)

private fun gmsCoreSupportResourcePatch(
    gmsCoreVendorGroupIdOption: Option<String>,
) = app.revanced.patches.shared.misc.gms.gmsCoreSupportResourceUniversalPatch(
    gmsCoreVendorGroupIdOption = gmsCoreVendorGroupIdOption,
)