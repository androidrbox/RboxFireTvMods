package rbox.xposed.firetvmods;

import java.io.File;
import java.util.Hashtable;
import java.util.Map.Entry;

import android.content.Context;
import android.view.KeyEvent;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class KeyBindings implements IXposedHookZygoteInit, IXposedHookLoadPackage
{
	Hashtable<Integer, String> bindings;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		bindings = new Hashtable<Integer, String>();

		XSharedPreferences prefs = new XSharedPreferences(new File("/data/media/0/key_bindings.xml"));
		for (Entry<String, ?> e : prefs.getAll().entrySet())
			bindings.put(XposedHelpers.getStaticIntField(KeyEvent.class, e.getKey()), (String)e.getValue());
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		// Don't do the hook if the prefs were empty
		if (!lpparam.packageName.equals("android") || bindings.size() == 0)
			return;

		// For some reason, findAndHookMethod doesn't work for this
		XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader), "interceptKeyBeforeDispatching", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				KeyEvent event = (KeyEvent)param.args[1];
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					String value = bindings.get(event.getKeyCode());
					if (value != null)
					{
						Context mContext = (Context)XposedHelpers.getObjectField(param.thisObject, "mContext");
						mContext.startActivity(mContext.getPackageManager().getLaunchIntentForPackage(value));
						param.setResult(-1);
					}
				}
			}
		});
	}
}