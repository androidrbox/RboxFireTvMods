package rbox.xposed.firetvmods;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HideLauncherAds implements IXposedHookLoadPackage
{
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("com.amazon.tv.launcher"))
			return;

		Class<?> CacheableObject = XposedHelpers.findClass("com.amazon.tv.mediabrowse.model.CacheableObject", lpparam.classLoader);
		Class<?> ScreenChannelUpdateCallback = XposedHelpers.findClass("com.amazon.tv.launcher.ui.carousel.fragment.MediaFragment2D$ScreenChannelUpdateCallback", lpparam.classLoader);
		XposedHelpers.findAndHookMethod("com.amazon.tv.launcher.ui.carousel.fragment.MediaFragment2D", lpparam.classLoader, "onChannelReturned", String.class, int.class, CacheableObject, ScreenChannelUpdateCallback, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				// If the banner ad channel was returned, override the function so it doesn't add it
				if (param.args[0].equals("[reftype=ad/banner/kso,refid=Launcher.Home.Hero]"))
					param.setResult(null);
			}
		});
	}
}