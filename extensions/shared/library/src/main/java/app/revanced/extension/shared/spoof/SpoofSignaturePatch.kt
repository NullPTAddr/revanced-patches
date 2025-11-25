package app.revanced.extension.shared.spoof

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import java.lang.reflect.Field
import java.lang.reflect.Method

@Suppress("unused")
class SignaturePatch {
    companion object {
        @JvmStatic
        fun killPM(packageName: String, rawSignatureBase64: String) {
            val originalSignature = Signature(Base64.decode(rawSignatureBase64, Base64.DEFAULT))
            val originalCreator = PackageInfo.CREATOR

            val creator = object : Parcelable.Creator<PackageInfo> {
                override fun createFromParcel(source: Parcel?): PackageInfo {
                    var packageInfo = originalCreator.createFromParcel(source)
                    if (packageInfo.packageName != null) {
                        if (packageInfo.packageName.equals(packageName, true)) {
                            packageInfo = spoofSignature(packageInfo, originalSignature)
                        }
                    }
                    return packageInfo
                }

                override fun newArray(size: Int): Array<PackageInfo> {
                    return originalCreator.newArray(size)
                }
            }

            try {
                findField(PackageInfo::class.java, "CREATOR").set(null, creator)
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                addHiddenApiExemptions()
            }
            try {
                val cache = findField(PackageManager::class.java, "sPackageInfoCache").get(null)
                cache.javaClass.getDeclaredMethod("clear").invoke(cache)
            } catch (ignored: Throwable) {
            }
            try {
                val mCreators: HashMap<*, *> = findField(Parcel::class.java, "mCreators").get(null) as HashMap<*, *>
                mCreators.clear()
            } catch (ignored: Throwable) {
            }
            try {
                val sPairedCreators: HashMap<*, *> =
                    findField(Parcel::class.java, "sPairedCreators").get(null) as HashMap<*, *>
                sPairedCreators.clear()
            } catch (ignored: Throwable) {
            }
        }

        fun spoofSignature(_packageInfo: PackageInfo, _signature: Signature): PackageInfo {
            var packageInfo = _packageInfo
            if (packageInfo.signatures != null && packageInfo.signatures.size > 0) {
                packageInfo.signatures[0] = _signature
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                if (packageInfo.signingInfo != null) {
                    val signaturesArray = packageInfo.signingInfo.apkContentsSigners
                    signaturesArray[0] = _signature
                }
            }
            return packageInfo
        }

        fun findField(_clazz: Class<*>, _fieldName: String): Field {
            var clazz = _clazz
            try {
                val field: Field = clazz.getDeclaredField(_fieldName)
                field.isAccessible = true
                return field
            } catch (e: NoSuchFieldException) {
                while (true) {
                    clazz = clazz.superclass
                    if (clazz == null || clazz.equals(Object::class.java)) {
                        break
                    }
                    try {
                        val field: Field = clazz.getDeclaredField(_fieldName)
                        field.isAccessible = true
                        return field
                    } catch (e: NoSuchFieldException) {

                    }
                }
                throw e
            }
        }

        fun addHiddenApiExemptions() {
            val forName = Class::class.java.getDeclaredMethod("forName", String::class.java)
            val getDeclaredMethod = Class::class.java.getDeclaredMethod(
                "getDeclaredMethod",
                String::class.java,
                arrayOf<Class<*>>()::class.java
            )

            val vmRuntimeClass = forName.invoke(null, "dalvik.system.VMRuntime") as Class<*>
            val getRuntime = getDeclaredMethod.invoke(vmRuntimeClass, "getRuntime", null) as Method
            val setHiddenApiExemptions = getDeclaredMethod.invoke(
                vmRuntimeClass,
                "setHiddenApiExemptions",
                arrayOf(arrayOf<String>()::class.java)
            ) as Method

            val vmRuntime = getRuntime.invoke(null)

            setHiddenApiExemptions.invoke(vmRuntime, arrayOf("L"))
        }
    }
}