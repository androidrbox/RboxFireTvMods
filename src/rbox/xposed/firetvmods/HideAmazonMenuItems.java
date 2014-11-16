package rbox.xposed.firetvmods;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.XModuleResources;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import de.robv.android.xposed.IXposedHookInitPackageResources;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources.InitPackageResourcesParam;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

public class HideAmazonMenuItems implements IXposedHookZygoteInit, IXposedHookInitPackageResources, IXposedHookLoadPackage
{
	private String MODULE_PATH;
	private int DRAWABLE_MENU_ITEMS;
	private XSharedPreferences prefs;

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable
	{
		MODULE_PATH = startupParam.modulePath;
		prefs = new XSharedPreferences("com.amazon.tv.csapp", "com.amazon.tv.csapp_preferences");
	}

	@Override
	public void handleInitPackageResources(InitPackageResourcesParam resparam) throws Throwable
	{
		if (!resparam.packageName.equals("com.amazon.tv.launcher"))
			return;

		// Add the menu_items drawable to the package resources
		Resources res = XModuleResources.createInstance(MODULE_PATH, resparam.res);
		DRAWABLE_MENU_ITEMS = resparam.res.addResource(res, R.drawable.menu_items);
	}

	@Override
	public void handleLoadPackage(final LoadPackageParam lpparam) throws Throwable
	{
		if (lpparam.packageName.equals("com.amazon.tv.launcher"))
		{
			final Class<?> SettingsItem = XposedHelpers.findClass("com.amazon.tv.launcher.ui.list.adapter.SettingsItem", lpparam.classLoader);

			XposedHelpers.findAndHookMethod(SettingsItem, "getSettingsItems", Context.class, boolean.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					@SuppressWarnings("unchecked")
					List<Object> settingsItems = (List<Object>)param.getResult();

					// Create the new item with the help intent and delete the help item
					Object helpItem = settingsItems.get(5);
					settingsItems.add(0, XposedHelpers.newInstance(SettingsItem,
					        0,
					        "Menu Items",
					        "Select what menu items should be visible on the home screen.",
					        DRAWABLE_MENU_ITEMS,
					        XposedHelpers.getObjectField(helpItem, "mIntent"),
					        false));
					settingsItems.remove(helpItem);

					// Go through each item in the list and adjust their id
					for (int i = 1; i < settingsItems.size(); i++)
						XposedHelpers.setObjectField(settingsItems.get(i), "mId", i);
				}
			});

			XposedHelpers.findAndHookMethod("com.amazon.tv.launcher.ui.leftmenu.MenuInfoCollection", lpparam.classLoader, "initMenuInfo", Context.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					// Get the menu list
					@SuppressWarnings("unchecked")
					ArrayList<Object> mMenuInfo = (ArrayList<Object>)XposedHelpers.getObjectField(param.thisObject, "mMenuInfo");

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

		if (lpparam.packageName.equals("com.amazon.tv.csapp"))
		{
			// Menu items that can be removed
			@SuppressWarnings("serial")
			final LinkedHashMap<String, String> menuItems = new LinkedHashMap<String, String>() {
				{
					put("search", "Search");
					// put("home", "Home"); // Don't allow remove home, must be there
					put("prime_video", "Prime Video");
					put("movies", "Movies");
					put("tv", "TV");
					put("watchlist", "Watchlist");
					put("video library", "Video Library");
					put("Games", "Games");
					put("Apps", "Apps");
					put("Music", "Music");
					put("Photos", "Photos");
					// put("settings", "Setting"); // Don't allow remove settings
				}
			};

			XposedHelpers.findAndHookMethod("com.amazon.tv.csapp.CSAppActivity", lpparam.classLoader, "onCreate", Bundle.class, new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable
				{
					// Get the activity and remove everything in it
					PreferenceActivity activity = (PreferenceActivity)param.thisObject;
					@SuppressWarnings("deprecation")
					PreferenceScreen preferences = activity.getPreferenceScreen();
					preferences.removeAll();

					// Add the menu items to the activity
					for (Map.Entry<String, String> item : menuItems.entrySet())
					{
						// Create the SwitchPreference, set it's settings, add it to
						// the PreferenceScreen and tell the activity it was added.
						MySwitchPreference preference = new MySwitchPreference(activity, lpparam.classLoader);
						preference.setChecked(prefs.getBoolean(item.getKey(), true));
						preference.setKey(item.getKey());
						preference.getExtras().putString("settings_details_key", "Select which items should be hidden.  Afterwards, a reboot is required.");
						preference.setTitle(item.getValue());
						preferences.addPreference(preference);
						XposedHelpers.callMethod(activity, "onPreferenceAdded", preference);

					}
				}
			});
		}
	}

	private class MySwitchPreference extends SwitchPreference
	{
		private int widget_frame;

		public MySwitchPreference(Context context, ClassLoader classLoader)
		{
			super(context);
			widget_frame = XposedHelpers.getStaticIntField(XposedHelpers.findClass("com.android.internal.R.id", classLoader), "widget_frame");
		}

		@Override
		protected View onCreateView(ViewGroup parent)
		{
			// Call the super to create the view
			View layout = super.onCreateView(parent);

			// Find the frame
			ViewGroup widgetFrame = (ViewGroup)layout.findViewById(widget_frame);

			// Find the ToggleButton and change the on/off text
			ToggleButton button = (ToggleButton)widgetFrame.getChildAt(0);
			button.setTextOn("VISIBLE");
			button.setTextOff("HIDDEN");

			return layout;
		}
	}
}