package app.revanced.patches.dudulauncher.strings

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.shared.misc.mapping.get
import app.revanced.patches.shared.misc.mapping.resourceMappingPatch
import app.revanced.patches.shared.misc.mapping.resourceMappings

val stringsPatch = resourcePatch("Strings Patch") {
    dependsOn(resourceMappingPatch)
    execute {
        resourceMappings["string", "total_power_consumption"]
    }
}