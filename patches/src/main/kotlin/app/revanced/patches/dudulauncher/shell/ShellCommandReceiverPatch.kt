package app.revanced.patches.dudulauncher.shell

import app.revanced.patcher.patch.resourcePatch
import app.revanced.patches.dudulauncher.Constans
import org.w3c.dom.Element
import org.w3c.dom.Node

val shellCommandReceiverPatch = resourcePatch(
    "Shell Command Patch"
) {
    compatibleWith(Constans.PACKAGE_NAME)
    execute {
        fun Node.adoptChild(
            tagName: String,
            block: Element.() -> Unit,
        ): Element {
            val child = ownerDocument.createElement(tagName)
            child.block()
            appendChild(child)
            return child
        }
        document("AndroidManifest.xml").use { document ->
            val applicationNode =
                document
                    .getElementsByTagName("application")
                    .item(0)
            val receiver = applicationNode.adoptChild("receiver") {
                setAttribute("android:name", "app.revanced.patches.dudulauncher.shell.ShellCommandReceiver")
                setAttribute("android:exported", "true")
            }

            val intentFilter = receiver.adoptChild("intent-filter") {}
            intentFilter.adoptChild("action") {
                setAttribute("android:name", "com.kangrio.dudumod.action.shellcommand")
            }
        }
    }
}