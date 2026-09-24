package com.example.alphabetlauncher;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Loads and organizes installed launchable applications.
 */
public final class AppRepository {


    /*
     * Private constructor.
     *
     * This class only contains static methods.
     */
    private AppRepository() {
    }


    /**
     * Loads all launchable applications from the device.
     *
     * IMPORTANT:
     * This method should be called once when the app starts.
     *
     * We should NOT call PackageManager every time
     * the user moves their finger.
     */
    public static List<AppInfo> loadLaunchableApps(
            Context context
    ) {

        /*
         * Android PackageManager.
         */
        PackageManager packageManager =
                context.getPackageManager();


        /*
         * Create an Intent that asks:
         *
         * "Which applications can be launched
         * from the Android launcher?"
         */
        Intent intent =
                new Intent(Intent.ACTION_MAIN);


        /*
         * Only applications with a launcher
         * entry are required.
         */
        intent.addCategory(Intent.CATEGORY_LAUNCHER);


        /*
         * Ask Android for all matching activities.
         */
        List<ResolveInfo> results =
                packageManager.queryIntentActivities(
                        intent,
                        PackageManager.MATCH_ALL
                );


        /*
         * HashMap prevents duplicate packages.
         *
         * Some applications can have more than
         * one launcher activity.
         */
        Map<String, AppInfo> uniqueApps =
                new HashMap<>();


        /*
         * Process every launcher result.
         */
        for (ResolveInfo info : results) {

            /*
             * Safety check.
             */
            if (info.activityInfo == null) {
                continue;
            }


            /*
             * Get package name.
             */
            String packageName =
                    info.activityInfo.packageName;


            /*
             * Get ApplicationInfo.
             */
            ApplicationInfo applicationInfo =
                    info.activityInfo.applicationInfo;


            /*
             * Get the visible application name.
             */
            CharSequence labelText =
                    packageManager.getApplicationLabel(
                            applicationInfo
                    );


            /*
             * Convert the name to String.
             */
            String label;

            if (labelText == null) {

                label = packageName;

            } else {

                label = labelText
                        .toString()
                        .trim();
            }


            /*
             * Fallback if label is empty.
             */
            if (label.isEmpty()) {

                label = packageName;
            }


            /*
             * Get application icon.
             */
            android.graphics.drawable.Drawable icon =
                    packageManager.getApplicationIcon(
                            applicationInfo
                    );


            /*
             * Create our AppInfo object.
             */
            AppInfo app =
                    new AppInfo(
                            label,
                            packageName,
                            icon
                    );


            /*
             * Store it using package name.
             *
             * If duplicate launcher activities
             * exist for the same application,
             * only one entry remains.
             */
            uniqueApps.put(
                    packageName,
                    app
            );
        }


        /*
         * Convert HashMap into ArrayList.
         */
        List<AppInfo> apps =
                new ArrayList<>(
                        uniqueApps.values()
                );


        /*
         * Sort applications alphabetically.
         */
        apps.sort(
                Comparator.comparing(
                        app -> app.label.toLowerCase()
                )
        );


        /*
         * Return the cached list.
         */
        return apps;
    }


    /**
     * Groups applications by first letter.
     *
     * Example:
     *
     * A -> Android Studio
     *     Amazon
     *
     * B -> Browser
     *
     * G -> Gmail
     *     Google
     */
    public static Map<Character, List<AppInfo>> groupByLetter(
            List<AppInfo> apps
    ) {

        /*
         * Map:
         *
         * A -> apps
         * B -> apps
         * ...
         * Z -> apps
         */
        Map<Character, List<AppInfo>> grouped =
                new HashMap<>();


        /*
         * Create an empty list for
         * every letter.
         */
        for (char letter = 'A';
             letter <= 'Z';
             letter++) {

            grouped.put(
                    letter,
                    new ArrayList<>()
            );
        }


        /*
         * Put each application
         * into the correct letter.
         */
        for (AppInfo app : apps) {

            char letter =
                    app.firstLetter();


            /*
             * Ignore numbers/symbols.
             */
            if (letter >= 'A' &&
                    letter <= 'Z') {

                grouped
                        .get(letter)
                        .add(app);
            }
        }


        /*
         * Sort each letter group.
         */
        for (List<AppInfo> list :
                grouped.values()) {

            Collections.sort(
                    list,
                    Comparator.comparing(
                            app -> app.label.toLowerCase()
                    )
            );
        }


        /*
         * Return grouped applications.
         */
        return grouped;
    }
}