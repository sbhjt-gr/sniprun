package com.gorai.sniprun;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettings {
    
    private static final String PREFS_NAME = "SnipRunSettings";
    private static final String KEY_THEME = "theme";
    private static final String KEY_FONT_SIZE = "font_size";
    private static final String KEY_AUTO_SAVE = "auto_save";
    private static final String KEY_SYNTAX_HIGHLIGHTING = "syntax_highlighting";
    private static final String KEY_LINE_NUMBERS = "line_numbers";
    private static final String KEY_AUTO_INDENT = "auto_indent";
    private static final String KEY_WORD_WRAP = "word_wrap";
    private static final String KEY_SHOW_WHITESPACE = "show_whitespace";
    
    public enum Theme {
        DARK
    }
    
    private final SharedPreferences prefs;
    private final Context context;
    
    public AppSettings(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public Theme getTheme() {
        return Theme.DARK;
    }
    
    public void setTheme(Theme theme) {
        prefs.edit().putString(KEY_THEME, theme.name()).apply();
    }
    
    public int getFontSize() {
        return prefs.getInt(KEY_FONT_SIZE, 14);
    }
    
    public void setFontSize(int fontSize) {
        prefs.edit().putInt(KEY_FONT_SIZE, fontSize).apply();
    }
    
    public boolean isAutoSaveEnabled() {
        return prefs.getBoolean(KEY_AUTO_SAVE, true);
    }
    
    public void setAutoSaveEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_SAVE, enabled).apply();
    }
    
    public boolean isSyntaxHighlightingEnabled() {
        return prefs.getBoolean(KEY_SYNTAX_HIGHLIGHTING, true);
    }
    
    public void setSyntaxHighlightingEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SYNTAX_HIGHLIGHTING, enabled).apply();
    }
    
    public boolean isLineNumbersEnabled() {
        return prefs.getBoolean(KEY_LINE_NUMBERS, true);
    }
    
    public void setLineNumbersEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_LINE_NUMBERS, enabled).apply();
    }
    
    public boolean isAutoIndentEnabled() {
        return prefs.getBoolean(KEY_AUTO_INDENT, true);
    }
    
    public void setAutoIndentEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_AUTO_INDENT, enabled).apply();
    }
    
    public boolean isWordWrapEnabled() {
        return prefs.getBoolean(KEY_WORD_WRAP, false);
    }
    
    public void setWordWrapEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_WORD_WRAP, enabled).apply();
    }
    
    public boolean isShowWhitespaceEnabled() {
        return prefs.getBoolean(KEY_SHOW_WHITESPACE, false);
    }
    
    public void setShowWhitespaceEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_SHOW_WHITESPACE, enabled).apply();
    }
    
    public void resetToDefaults() {
        prefs.edit().clear().apply();
    }
}
