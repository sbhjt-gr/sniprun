package com.gorai.sniprun;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    
    private AppSettings appSettings;
    
    private Switch syntaxHighlightingSwitch;
    private Switch lineNumbersSwitch;
    private Switch autoIndentSwitch;
    private Switch wordWrapSwitch;
    private Switch autoSaveSwitch;
    private Switch showWhitespaceSwitch;
    
    private SeekBar fontSizeSeekBar;
    private TextView fontSizeValue;
    
    private TextView themeSelection;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        appSettings = new AppSettings(this);
        
        setupToolbar();
        initializeViews();
        loadCurrentSettings();
        setupListeners();
        setupOnBackPressed();
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Settings");
        }
    }
    
    private void initializeViews() {
        syntaxHighlightingSwitch = findViewById(R.id.switch_syntax_highlighting);
        lineNumbersSwitch = findViewById(R.id.switch_line_numbers);
        autoIndentSwitch = findViewById(R.id.switch_auto_indent);
        wordWrapSwitch = findViewById(R.id.switch_word_wrap);
        autoSaveSwitch = findViewById(R.id.switch_auto_save);
        showWhitespaceSwitch = findViewById(R.id.switch_show_whitespace);
        
        fontSizeSeekBar = findViewById(R.id.seekbar_font_size);
        fontSizeValue = findViewById(R.id.text_font_size_value);
        
        themeSelection = findViewById(R.id.text_theme_selection);
        
        fontSizeSeekBar.setMin(8);
        fontSizeSeekBar.setMax(24);
    }
    
    private void loadCurrentSettings() {
        syntaxHighlightingSwitch.setChecked(appSettings.isSyntaxHighlightingEnabled());
        lineNumbersSwitch.setChecked(appSettings.isLineNumbersEnabled());
        autoIndentSwitch.setChecked(appSettings.isAutoIndentEnabled());
        wordWrapSwitch.setChecked(appSettings.isWordWrapEnabled());
        autoSaveSwitch.setChecked(appSettings.isAutoSaveEnabled());
        showWhitespaceSwitch.setChecked(appSettings.isShowWhitespaceEnabled());
        
        int fontSize = appSettings.getFontSize();
        fontSizeSeekBar.setProgress(fontSize);
        fontSizeValue.setText(fontSize + "sp");
        
        AppSettings.Theme theme = appSettings.getTheme();
        themeSelection.setText(theme.name());
    }
    
    private void setupListeners() {
        syntaxHighlightingSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setSyntaxHighlightingEnabled(isChecked);
        });
        
        lineNumbersSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setLineNumbersEnabled(isChecked);
        });
        
        autoIndentSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setAutoIndentEnabled(isChecked);
        });
        
        wordWrapSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setWordWrapEnabled(isChecked);
        });
        
        autoSaveSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setAutoSaveEnabled(isChecked);
        });
        
        showWhitespaceSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appSettings.setShowWhitespaceEnabled(isChecked);
        });
        
        fontSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    appSettings.setFontSize(progress);
                    fontSizeValue.setText(progress + "sp");
                }
            }
            
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        
        themeSelection.setOnClickListener(v -> showThemeDialog());
        
        findViewById(R.id.button_reset_defaults).setOnClickListener(v -> resetToDefaults());
    }
    
    private void showThemeDialog() {
        String[] themes = {"DARK", "LIGHT"};
        
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Select Theme");
        builder.setItems(themes, (dialog, which) -> {
            AppSettings.Theme selectedTheme = AppSettings.Theme.values()[which];
            appSettings.setTheme(selectedTheme);
            themeSelection.setText(selectedTheme.name());
            
            Intent intent = new Intent();
            intent.putExtra("theme_changed", true);
            setResult(RESULT_OK, intent);
        });
        
        builder.show();
    }
    
    private void resetToDefaults() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Reset Settings");
        builder.setMessage("Are you sure you want to reset all settings to default values?");
        
        builder.setPositiveButton("Reset", (dialog, which) -> {
            appSettings.resetToDefaults();
            loadCurrentSettings();
            
            Intent intent = new Intent();
            intent.putExtra("settings_reset", true);
            setResult(RESULT_OK, intent);
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private void setupOnBackPressed() {
        androidx.activity.OnBackPressedCallback callback = new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent();
                intent.putExtra("settings_changed", true);
                setResult(RESULT_OK, intent);
                finish();
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
}
