package rbox.xposed.firetvmods;

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;

import java.util.List;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class AlternateLauncher implements IXposedHookLoadPackage
{
	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable
	{
		if (!lpparam.packageName.equals("android"))
			return;

		// Use the alternate launcher instead of the Amazon launcher
		findAndHookMethod("com.android.server.pm.PackageManagerService", lpparam.classLoader, "chooseBestActivity", Intent.class, String.class, int.class, List.class, int.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				@SuppressWarnings("unchecked")
				List<ResolveInfo> query = (List<ResolveInfo>)param.args[3];

				// If there are at least 2 activities and this is Amazon home activity,
				// swap them so the 2nd one is used
				if (query.size() >= 2 && "com.amazon.tv.launcher.ui.HomeActivity".equals(query.get(0).activityInfo.name))
				{
					ResolveInfo secondLauncher = query.get(1);
					query.set(1, query.get(0));
					query.set(0, secondLauncher);
				}
			}
		});
	}
}