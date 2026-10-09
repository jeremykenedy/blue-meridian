package com.jeremykenedy.bluemeridian;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

public final class SettingsProvider extends ContentProvider {
    public static final String AUTHORITY = "com.jeremykenedy.bluemeridian.settings";
    private static final int SCHEMA = 1;
    private static final int SETTINGS = 2;
    private static final UriMatcher URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        URI_MATCHER.addURI(AUTHORITY, "schema", SCHEMA);
        URI_MATCHER.addURI(AUTHORITY, "settings", SETTINGS);
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        int match = URI_MATCHER.match(uri);
        if (match == SCHEMA) return schemaCursor();
        if (match == SETTINGS) return settingsCursor();
        throw new IllegalArgumentException("Unknown settings URI");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (URI_MATCHER.match(uri) != SETTINGS || values == null) throw new IllegalArgumentException("Unknown settings URI");
        String key = values.getAsString("key");
        String value = values.getAsString("value");
        if (!SettingsValues.isSupported(key, value)) throw new IllegalArgumentException("Unsupported setting value");
        Context context = getContext();
        if (context == null) return 0;
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        if ("randomize_all".equals(key)) editor.putBoolean(key, Boolean.parseBoolean(value));
        else editor.putString(key, value);
        editor.apply();
        return 1;
    }

    private Cursor schemaCursor() {
        MatrixCursor cursor = new MatrixCursor(new String[] {"key", "title", "type", "default", "choices", "randomAllowed"});
        cursor.addRow(new Object[] {"star_density", "Stars", "choice", "normal", "sparse|normal|dense|random", true});
        cursor.addRow(new Object[] {"spin_speed", "Planet rotation", "choice", "natural", "slow|natural|fast|random", true});
        cursor.addRow(new Object[] {"camera_motion", "Camera movement", "choice", "gentle", "still|gentle|sweeping|random", true});
        cursor.addRow(new Object[] {"atmosphere", "Atmosphere glow", "choice", "on", "on|off|random", true});
        cursor.addRow(new Object[] {"night_lights", "Night lights", "choice", "on", "on|off|random", true});
        cursor.addRow(new Object[] {"clouds", "Cloud cover", "choice", "on", "off|on|random", true});
        cursor.addRow(new Object[] {"randomize_all", "Randomize all settings each start", "boolean", "false", "true|false", false});
        return cursor;
    }

    private Cursor settingsCursor() {
        MatrixCursor cursor = new MatrixCursor(new String[] {"key", "value"});
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        cursor.addRow(new Object[] {"star_density", preferences.getString("star_density", "normal")});
        cursor.addRow(new Object[] {"spin_speed", preferences.getString("spin_speed", "natural")});
        cursor.addRow(new Object[] {"camera_motion", preferences.getString("camera_motion", "gentle")});
        cursor.addRow(new Object[] {"atmosphere", preferences.getString("atmosphere", "on")});
        cursor.addRow(new Object[] {"night_lights", preferences.getString("night_lights", "on")});
        cursor.addRow(new Object[] {"clouds", preferences.getString("clouds", "on")});
        cursor.addRow(new Object[] {"randomize_all", Boolean.toString(preferences.getBoolean("randomize_all", false))});
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        int match = URI_MATCHER.match(uri);
        if (match == SCHEMA) return "vnd.android.cursor.dir/vnd.bluemeridian.setting-schema.v1";
        if (match == SETTINGS) return "vnd.android.cursor.dir/vnd.bluemeridian.setting.v1";
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Insert is not supported");
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete is not supported");
    }
}
