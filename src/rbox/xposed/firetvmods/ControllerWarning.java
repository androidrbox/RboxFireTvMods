package rbox.xposed.firetvmods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import org.json.JSONArray;

import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class ControllerWarning implements IXposedHookLoadPackage
{
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("com.amazon.venezia"))
			return;

		// Prevent controller warning by clearing out the list of necessary controllers.
		findAndHookMethod("com.amazon.venezia.pdi.AppLaunchActivity", lpparam.classLoader, "performInterstitial", Bundle.class, JSONArray.class, String.class, String.class, String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				param.args[1] = new JSONArray();
			}
		});
	}
}