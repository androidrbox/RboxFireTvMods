package rbox.xposed.firetvmods;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class RecentsOnMenuLong implements IXposedHookLoadPackage
{
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		/* This is handled by KeyBindings, but leave the empty class as a symbol of the feature. */
	}
}