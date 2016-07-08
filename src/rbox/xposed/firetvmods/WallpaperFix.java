package rbox.xposed.firetvmods;

import android.content.res.XResources;
import de.robv.android.xposed.IXposedHookZygoteInit;

public class WallpaperFix implements IXposedHookZygoteInit
{
    @Override
    public void initZygote( StartupParam startupParam ) throws Throwable
    {
        XResources.setSystemWideReplacement( "android", "bool", "disable_system_wallpapers", false );
    }
}
