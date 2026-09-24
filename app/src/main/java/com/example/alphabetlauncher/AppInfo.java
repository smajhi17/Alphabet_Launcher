package com.example.alphabetlauncher;

import android.graphics.drawable.Drawable;


/**
 * Represents one installed launchable application.
 */
public final class AppInfo {

    /*
     * Application name.
     *
     * Example:
     * Gmail
     * WhatsApp
     * YouTube
     */
    public final String label;


    /*
     * Android package name.
     *
     * Example:
     * com.google.android.gm
     */
    public final String packageName;


    /*
     * Application icon.
     */
    public final Drawable icon;


    /**
     * Constructor.
     */
    public AppInfo(
            String label,
            String packageName,
            Drawable icon
    ) {

        this.label = label;

        this.packageName = packageName;

        this.icon = icon;
    }


    /**
     * Returns the first letter of the application name.
     *
     * Example:
     *
     * Gmail -> G
     * WhatsApp -> W
     * YouTube -> Y
     */
    public char firstLetter() {

        /*
         * Protect against an empty application name.
         */
        if (label == null || label.trim().isEmpty()) {

            return '#';
        }


        /*
         * Remove spaces and get first character.
         */
        char character =
                Character.toUpperCase(
                        label.trim().charAt(0)
                );


        /*
         * We only want A-Z.
         */
        if (character >= 'A' && character <= 'Z') {

            return character;
        }


        /*
         * Applications starting with numbers
         * or symbols go here.
         */
        return '#';
    }
}
