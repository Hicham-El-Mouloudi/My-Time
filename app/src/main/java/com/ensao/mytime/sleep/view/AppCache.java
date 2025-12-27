package com.ensao.mytime.sleep.view;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AppCache {
    // Cette liste statique stocke les apps en mémoire vive
    public static List<AppInfo> cachedApps = new ArrayList<>();
    private static boolean isLoaded = false;

    public static void preloadApps(Context context) {
        if (isLoaded) return;

        new Thread(() -> {
            PackageManager pm = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);

            List<AppInfo> tempApps = new ArrayList<>();
            for (ResolveInfo info : resolveInfos) {
                if (!info.activityInfo.packageName.equals(context.getPackageName())) {
                    tempApps.add(new AppInfo(
                            info.loadLabel(pm).toString(),
                            info.activityInfo.packageName,
                            info.loadIcon(pm)
                    ));
                }
            }
            Collections.sort(tempApps, (a, b) -> a.name.compareToIgnoreCase(b.name));
            cachedApps = tempApps;
            isLoaded = true;
        }).start();
    }
}
