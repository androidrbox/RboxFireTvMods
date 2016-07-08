package rbox.xposed.firetvmods;

import android.content.ContentValues;

import java.lang.reflect.Method;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class DownloadProviderFix implements IXposedHookLoadPackage
{
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
    {
        if (lpparam.packageName.equals("com.android.providers.downloads"))
        {
            final Class<?> cls = XposedHelpers.findClass("com.android.providers.downloads.DownloadProvider", lpparam.classLoader);
            Method m = XposedHelpers.findMethodExact(cls, "checkInsertPermissions", ContentValues.class);
            XposedBridge.hookMethod(m, XC_MethodReplacement.returnConstant(0));
        }
    }
}
