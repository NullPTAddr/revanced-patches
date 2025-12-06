package app.revanced.extension.gmscore.spoof;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class SignaturePatch extends Application {
    static List<String> knowGooglePackage = Arrays.asList(
            "com.google.android.gms",
            "com.android.vending",
            "com.google.android.gsf",
            "com.google.android.gm",
            "com.google.android.inputmethod.latin",
            "com.google.android.tts",
            "com.google.android.googlequicksearchbox",
            "com.google.android.apps.maps",
            "com.google.android.keep",
            "com.google.android.play.games",
            "com.android.chrome",
            "com.google.android.calendar",
            "com.google.android.apps.docs"
    );
    static String fakeGoogleSignatureData =
            "MIIEQzCCAyugAwIBAgIJAMLgh0ZkSjCNMA0GCSqGSIb3DQEBBAUAMHQxCzAJBgNVBAYTAlVTMRMw\n" +
                    "EQYDVQQIEwpDYWxpZm9ybmlhMRYwFAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29n\n" +
                    "bGUgSW5jLjEQMA4GA1UECxMHQW5kcm9pZDEQMA4GA1UEAxMHQW5kcm9pZDAeFw0wODA4MjEyMzEz\n" +
                    "MzRaFw0zNjAxMDcyMzEzMzRaMHQxCzAJBgNVBAYTAlVTMRMwEQYDVQQIEwpDYWxpZm9ybmlhMRYw\n" +
                    "FAYDVQQHEw1Nb3VudGFpbiBWaWV3MRQwEgYDVQQKEwtHb29nbGUgSW5jLjEQMA4GA1UECxMHQW5k\n" +
                    "cm9pZDEQMA4GA1UEAxMHQW5kcm9pZDCCASAwDQYJKoZIhvcNAQEBBQADggENADCCAQgCggEBAKtW\n" +
                    "LgDYO6IIrgqWbxJOKdoR8qtW0I9Y4sypEwPpt1TTcvZApxsdyxMJZ2JORland2qSGT2y5b+3JKke\n" +
                    "dxiLDmpHpDsz2WCbdxgxRczfey5YZnTJ4VZbH0xqWVW/8lGmPav5xVwnIiJS6HXk+BVKZF+JcWjA\n" +
                    "sb/GEuq/eFdpuzSqeYTcfi6idkyugwfYwXFU1+5fZKUaRKYCwkkFQVfcAs1fXA5V+++FGfvjJ/Cx\n" +
                    "URaSxaBvGdGDhfXE28LWuT9ozCl5xw4Yq5OGazvV24mZVSoOO0yZ31j7kYvtwYK6NeADwbSxDdJE\n" +
                    "qO4k//0zOHKrUiGYXtqw/A0LFFtqoZKFjnkCAQOjgdkwgdYwHQYDVR0OBBYEFMd9jMIhF1Ylmn/T\n" +
                    "gt9r45jk14alMIGmBgNVHSMEgZ4wgZuAFMd9jMIhF1Ylmn/Tgt9r45jk14aloXikdjB0MQswCQYD\n" +
                    "VQQGEwJVUzETMBEGA1UECBMKQ2FsaWZvcm5pYTEWMBQGA1UEBxMNTW91bnRhaW4gVmlldzEUMBIG\n" +
                    "A1UEChMLR29vZ2xlIEluYy4xEDAOBgNVBAsTB0FuZHJvaWQxEDAOBgNVBAMTB0FuZHJvaWSCCQDC\n" +
                    "4IdGZEowjTAMBgNVHRMEBTADAQH/MA0GCSqGSIb3DQEBBAUAA4IBAQBt0lLO74UwLDYKqs6Tm8/y\n" +
                    "zKkEu116FmH4rkaymUIE0P9KaMftGlMexFlaYjzmB2OxZyl6euNXEsQH8gjwyxCUKRJNexBiGcCE\n" +
                    "yj6z+a1fuHHvkiaai+KL8W1EyNmgjmyy8AW7P+LLlkR+ho5zEHatRbM/YAnqGcFh5iZBqpknHf1S\n" +
                    "KMXFh4dd239FJ1jWYfbMDMy3NS5CTMQ2XFI1MvcyUTdZPErjQfTbQe3aDQsQcafEQPD+nqActifK\n" +
                    "Z0Np0IS9L9kR/wbNvyz6ENwPiTrjV2KRkEjH78ZMcUQXg0L3BYHJ3lc69Vs5Ddf9uUGGMYldX3Wf\n" +
                    "MBEmh/9iFBDAaTCK\n";
    // Source
    public final String URL = "https://github.com/L-JINBIN/ApkSignatureKillerEx";
    String TAG = "KillerApplication";
    HashMap<String, Signature> packagesSignaturesSpoofCache = new HashMap<>();

    @Override
    public void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        killPM();
    }

    private void killPM() {
        Signature fakeGoogleSignature = new Signature(Base64.decode(fakeGoogleSignatureData, Base64.DEFAULT));
        Parcelable.Creator<PackageInfo> originalCreator = PackageInfo.CREATOR;
        Parcelable.Creator<PackageInfo> creator = null;
        creator = new Parcelable.Creator<PackageInfo>() {
            @Override
            public PackageInfo createFromParcel(Parcel source) {
                PackageInfo packageInfo = originalCreator.createFromParcel(source);
                if (packageInfo.packageName != null && packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                    if (knowGooglePackage.contains(packageInfo.packageName.toLowerCase())) {
                        //noinspection ConstantConditions
                        return spoofSignature(packageInfo, fakeGoogleSignature);
                    } else {
                        Signature fakeSignature = packagesSignaturesSpoofCache.get(packageInfo.packageName);
                        if (fakeSignature != null) {
                            return spoofSignature(packageInfo, fakeSignature);
                        } else {
                            try {
                                Bundle metaData = getPackageManager().getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA).applicationInfo.metaData;
                                if (metaData != null) {
                                    String sigData = metaData.getString("org.microg.gms.spoofed_certificates", null);
                                    if (sigData == null) {
                                        return packageInfo;
                                    }
                                    Signature spoofedSignature = new Signature(Base64.decode(sigData, Base64.DEFAULT));
                                    packagesSignaturesSpoofCache.put(packageInfo.packageName, spoofedSignature);
                                    return spoofSignature(packageInfo, spoofedSignature);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, Objects.requireNonNull(e.getMessage()) + " " + packageInfo.packageName);
                            }
                        }
                    }
                }
                return packageInfo;
            }

            @Override
            public PackageInfo[] newArray(int size) {
                Log.d(TAG, "newArray: " + size);
                return originalCreator.newArray(size);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            addHiddenApiExemptions();
        }
        try {
            findField(PackageInfo.class, "CREATOR").set(null, creator);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try {
            Object cache = findField(PackageManager.class, "sPackageInfoCache").get(null);
//            noinspection ConstantConditions
            cache.getClass().getMethod("clear").invoke(cache);
        } catch (Throwable ignored) {
        }
        try {
            Map<?, ?> mCreators = (Map<?, ?>) findField(Parcel.class, "mCreators").get(null);
            //noinspection ConstantConditions
            mCreators.clear();
        } catch (Throwable ignored) {
        }
        try {
            Map<?, ?> sPairedCreators = (Map<?, ?>) findField(Parcel.class, "sPairedCreators").get(null);
            //noinspection ConstantConditions
            sPairedCreators.clear();
        } catch (Throwable ignored) {
        }
    }

    private PackageInfo spoofSignature(PackageInfo packageInfo, Signature signature) {
        if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
            packageInfo.signatures[0] = signature;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (packageInfo.signingInfo != null) {
                Signature[] signaturesArray = packageInfo.signingInfo.getApkContentsSigners();
                if (signaturesArray != null && signaturesArray.length > 0) {
                    signaturesArray[0] = signature;
                }
            }
        }
        return packageInfo;
    }

    private Field findField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            while (true) {
                clazz = clazz.getSuperclass();
                if (clazz == null || clazz.equals(Object.class)) {
                    break;
                }
                try {
                    Field field = clazz.getDeclaredField(fieldName);
                    field.setAccessible(true);
                    return field;
                } catch (NoSuchFieldException ignored) {
                }
            }
            throw e;
        }
    }

    public static void addHiddenApiExemptions() {
        try {
            Method forName = Class.class.getDeclaredMethod("forName", String.class);
            Method getDeclaredMethod = Class.class.getDeclaredMethod(
                    "getDeclaredMethod",
                    String.class,
                    Class[].class
            );

            Class<?> vmRuntimeClass = (Class<?>) forName.invoke(null, "dalvik.system.VMRuntime");

            Method getRuntime = (Method) getDeclaredMethod.invoke(
                    vmRuntimeClass,
                    "getRuntime",
                    (Object) null
            );

            Method setHiddenApiExemptions = (Method) getDeclaredMethod.invoke(
                    vmRuntimeClass,
                    "setHiddenApiExemptions",
                    new Class[]{String[].class}
            );

            Object vmRuntime = getRuntime.invoke(null);

            setHiddenApiExemptions.invoke(vmRuntime, (Object) new String[]{"L"});
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}