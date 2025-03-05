package app.revanced.patches.dudulauncher.extension

import app.revanced.patches.shared.misc.extension.sharedExtensionPatch
import app.revanced.patches.dudulauncher.extension.hooks.applicationInitHook

val sharedExtensionPatch = sharedExtensionPatch("dudulauncher", applicationInitHook)