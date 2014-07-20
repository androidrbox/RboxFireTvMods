package rbox.xposed.firetvmods;

import java.io.File;
import java.util.Map.Entry;

import android.content.Context;
import android.util.SparseArray;
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
	private static final int LONG_PRESS = 0x80000000;
	SparseArray<String> bindings;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		bindings = new SparseArray<String>();

		// Add the home long press to recents by default
		bindings.put(LONG_PRESS | KeyEvent.KEYCODE_HOME, null);

		XSharedPreferences prefs = new XSharedPreferences(new File("/data/media/0/key_bindings.xml"));
		for (Entry<String, ?> e : prefs.getAll().entrySet())
		{
			String key = e.getKey();
			int longPress = 0;
			if (key.endsWith("_LONG"))
			{
				longPress = LONG_PRESS;
				key = key.substring(0, key.indexOf("_LONG"));
			}
			bindings.put(longPress | XposedHelpers.getStaticIntField(KeyEvent.class, key), (String)e.getValue());
		}
	}

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		// Don't do the hook if the prefs were empty
		if (!lpparam.packageName.equals("android") || bindings.size() == 0)
			return;

		// Add a hook for setting up recents on menu long press
		if (bindings.get(LONG_PRESS | KeyEvent.KEYCODE_HOME) == null)
		{
			XposedHelpers.findAndHookConstructor("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					// Set the long press behavior to bring up the Recents dialog
					XposedHelpers.setIntField(param.thisObject, "mLongPressOnHomeBehavior", 1);
				}
			});
		}

		// For some reason, findAndHookMethod doesn't work for this
		XposedBridge.hookAllMethods(XposedHelpers.findClass("com.android.internal.policy.impl.PhoneWindowManager", lpparam.classLoader), "interceptKeyBeforeDispatching", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				KeyEvent event = (KeyEvent)param.args[1];
				if (event.getAction() == KeyEvent.ACTION_DOWN)
				{
					int longPress = (event.getFlags() & KeyEvent.FLAG_LONG_PRESS) != 0 ? LONG_PRESS : 0;
					String value = bindings.get(longPress | event.getKeyCode());
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