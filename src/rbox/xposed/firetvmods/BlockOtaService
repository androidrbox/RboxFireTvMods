package rbox.xposed.firetvmods;

import android.content.pm.PackageManager;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class BlockOtaService implements IXposedHookLoadPackage
{
    @Override
    public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
    {
        if (lpparam.packageName.equals("android"))
        {
            final Class<?> cls = XposedHelpers.findClass("com.android.server.pm.PackageManagerService", lpparam.classLoader);
            XposedHelpers.findAndHookMethod(cls, "systemReady",new XC_MethodHook()
            {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable
                {
                    //get objects
                    Object mSettings = XposedHelpers.getObjectField(param.thisObject, "mSettings");
                    Object mPackages = XposedHelpers.getObjectField( mSettings, "mPackages" );
                    Object pkgSetting = XposedHelpers.callMethod( mPackages, "get", "com.amazon.device.software.ota");

                    //set new state to disabled
                    XposedHelpers.callMethod(pkgSetting, "setEnabled", PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0, null);
                }
            });
        }
    }
}
