package app.revanced.extension.dudulauncher.signature

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.util.Base64
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.lang.reflect.Field

@Suppress("unused")
class SignaturePatch {
    companion object {
        val packageName = "com.dudu.autoui"
        val signatureData =
            "MIIDUzCCAjugAwIBAgIELrQHXTANBgkqhkiG9w0BAQsFADBaMQ0wCwYDVQQGEwRkdWR1MQ0wCwYD\nVQQIEwRkdWR1MQ0wCwYDVQQHEwRkdWR1MQ0wCwYDVQQKEwRkdWR1MQ0wCwYDVQQLEwRkdWR1MQ0w\nCwYDVQQDEwRkdWR1MB4XDTIyMDExNzEzMDExOFoXDTQ3MDExMTEzMDExOFowWjENMAsGA1UEBhME\nZHVkdTENMAsGA1UECBMEZHVkdTENMAsGA1UEBxMEZHVkdTENMAsGA1UEChMEZHVkdTENMAsGA1UE\nCxMEZHVkdTENMAsGA1UEAxMEZHVkdTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAM95\nxAJhAAd/1qg/Alet0mFPiLtM9+BtE+q7Vjpy6fjh0zAYXkPWurJUy4kQRL8lo/pql/hE2cji6o7s\n1Z57HPT25z+bXEeom77v+I6+lJ3r0gNm41gI1zF1g4ijmv2zPcuSSsH3sBus20QvXK5f+jQFJvTI\nxnoGPnq8TgPYKrxdm4tdWaViy2X5i0V/d9ee0arCIz5KRVlJ3E1FCWs4aWjp0b+ciCvntpRhVQfd\n5ArnkiUtsHnVPV/1833TLhPYreMB5n8uDUhuRH2QJcW+PTrShU62fMjGfb/uxc7zMgQsRM22qY0r\nJBZCISxg0B+4TYiR+WIeD1qK+vZD+8tgqf8CAwEAAaMhMB8wHQYDVR0OBBYEFBsHcUm73QnsS91w\nPbxLWQn3Rws9MA0GCSqGSIb3DQEBCwUAA4IBAQBdyCSjPPEPFVcxy+gsY0kvIX95LyNStIvGs4J8\n+6DiQbj+46qdRW4spxrrT0iwDzGcKrKCq4UFU/tUa0v5TOgPgQqAUYjTZ09yc4QX0vzwMQDZoQG3\nDwyMrOucNzyGMOdiL9814oMEBq+82kiFuEWc1ZoivuxF9yluGQVUXUzr1ej4S9RGwMGT+q2aPDrl\nWsYVeaGNvRWiYPjAnwr+ahqLKdV0YOPax5WRN+uQ9e2cuhaEbC17a6cDNGlYhU1gccKYx2Z6dRlJ\ngGTHOB+BS/Xmwg8dsl0RVSxPmhmHJLM6FdDVmuTy8DxpMaZx5oTwGLQHc4WF1UhXzc6pEt0tBi+L"

        @JvmStatic
        fun killPM() {
            val originalSignature = Signature(Base64.decode(signatureData, Base64.DEFAULT))
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
                HiddenApiBypass.addHiddenApiExemptions("Landroid/os/Parcel;", "Landroid/content/pm", "Landroid/app")
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
    }
}