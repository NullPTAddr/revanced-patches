package app.revanced.extension.dudulauncher.shell;

import android.content.*;
import java.lang.reflect.*;

public class ShellCommandReceiver extends BroadcastReceiver {
    String TAG = "ShellCommandReceiver";
    public static String ACTION_NAME = "com.kangrio.dudumod.action.shellcommand";
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action.equalsIgnoreCase(ACTION_NAME)) {
            String command = intent.getDataString();
            if (command == null) return;
            try {
                executeCommand(command);
            } catch (Throwable e) {
            }
        }
    }

    void executeCommand(String command) throws SecurityException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, ClassNotFoundException {
        Class<?> shellManageClass = Class.forName("com.dudu.autoui.manage.shellManage.k");
        Method getInstanceMethod = shellManageClass.getDeclaredMethod("h");
        Object instance = getInstanceMethod.invoke(null);
        Method methodA = shellManageClass.getDeclaredMethod("a", String.class);
        methodA.invoke(instance, command);
    }
}
