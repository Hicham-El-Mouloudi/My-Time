package com.ensao.mytime.sleep.view;

import android.graphics.drawable.Drawable;

/**
 * Modèle de données simple pour représenter une application installée.
 */
public class AppInfo {
    public String name;
    public String packageName;
    public Drawable icon;

    public AppInfo(String name, String packageName, Drawable icon) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
    }
}