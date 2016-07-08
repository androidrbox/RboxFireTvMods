package rbox.xposed.firetvmods;

import android.content.Context;

import java.util.ArrayList;
import java.util.Iterator;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HideAmazonMenuItems implements IXposedHookZygoteInit, IXposedHookLoadPackage
{
	private XSharedPreferences prefs;

	@Override
	public void initZygote(IXposedHookZygoteInit.StartupParam startupParam) throws Throwable
	{
		prefs = new XSharedPreferences("com.amazon.tv.csapp", "com.amazon.tv.csapp_preferences");
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
	{
		if (lpparam.packageName.equals("com.amazon.tv.launcher"))
		{
			XposedHelpers.findAndHookMethod("com.amazon.tv.launcher.ui.leftmenu.MenuInfoCollection", lpparam.classLoader, "initMenuInfo", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable
				{
					// Get the menu list
					@SuppressWarnings("unchecked")
					ArrayList<Object> mMenuInfo = (ArrayList<Object>) XposedHelpers.getObjectField(param.thisObject, "mMenuInfo");

					// Loop over every MenuInfo, and remove it if it's in the preferences
					for (Iterator<Object> it = mMenuInfo.iterator(); it.hasNext();)
					{
						Object menuInfo = it.next();
						if (prefs.getBoolean(menuInfo.toString(), true) == false)
							it.remove();
					}
				}
			});
		}
	}		
}
