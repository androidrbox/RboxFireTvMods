package rbox.xposed.firetvmods;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class RecentsOnMenuLong implements IXposedHookLoadPackage
{
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("android"))
			return;

		XposedHelpers.findAndHookConstructor("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable
			{
				// Set the long press behavior to bring up the Recents dialog
				XposedHelpers.setIntField(param.thisObject, "mLongPressOnHomeBehavior", 1);
			}
		});
	}
}