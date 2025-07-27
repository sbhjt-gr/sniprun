package com.gorai.sniprun;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements FileExplorerFragment.OnFileSelectedListener {
    
    private EditText codeEditor;
    private TextView outputConsole;
    private TabLayout tabLayout;
    private FloatingActionButton runButton;
    private FloatingActionButton newFileButton;
    private MaterialButton copyOutputButton;
    
    private LottieAnimationView toolbarLogoAnimation;
    private LottieAnimationView explorerIcon;
    private LottieAnimationView editorStatusAnimation;
    private LottieAnimationView runButtonLoading;
    private LottieAnimationView runButtonPulse;
    private LottieAnimationView newFilePulse;
    private LottieAnimationView resultAnimation;
    private FrameLayout animationOverlay;
    
    private ExecutorService executorService;
    private JavaExecutor javaExecutor;
    private FileManager fileManager;
    private AppSettings appSettings;
    private UndoRedoManager undoRedoManager;
    private RecentFilesManager recentFilesManager;
    private ErrorHighlightManager errorHighlightManager;
    private CodeFoldingManager codeFoldingManager;
    private LineNumberView lineNumberView;
    
    private boolean isFileExplorerOpen = false;
    private String currentFileName = "Untitled.java";
    private String currentFilePath = null;
    
    private static final String SAMPLE_CODE = 
        "import java.util.*;\n\n" +
        "public class HelloWorld {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"Hello, SnipRun IDE!\");\n" +
        "        \n" +
        "        int sum = 0;\n" +
        "        for (int i = 1; i <= 10; i++) {\n" +
        "            sum += i;\n" +
        "        }\n" +
        "        System.out.println(\"Sum of 1-10: \" + sum);\n" +
        "        \n" +
        "        List<String> languages = new ArrayList<>();\n" +
        "        languages.add(\"Java\");\n" +
        "        languages.add(\"Kotlin\");\n" +
        "        languages.add(\"Python\");\n" +
        "        \n" +
        "        System.out.println(\"Programming languages:\");\n" +
        "        for (String lang : languages) {\n" +
        "            System.out.println(\"- \" + lang);\n" +
        "        }\n" +
        "        \n" +
        "        String message = \"Java on Android!\";\n" +
        "        System.out.println(\"Uppercase: \" + message.toUpperCase());\n" +
        "        System.out.println(\"Length: \" + message.length());\n" +
        "        \n" +
        "        System.out.println(\"Current time: \" + new java.util.Date());\n" +
        "    }\n" +
        "}";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        initializeLottieAnimations();
        setupToolbar();
        setupListeners();
        
        executorService = Executors.newSingleThreadExecutor();
        javaExecutor = new JavaExecutor(this);
        fileManager = new FileManager(this);
        appSettings = new AppSettings(this);
        
        initializeEditorFeatures();
        
        loadSampleCode();
        startInitialAnimations();
        setupOnBackPressed();
        
        testCompilers();
        runQuickTest();
    }
    
    private void initializeViews() {
        codeEditor = findViewById(R.id.code_editor);
        outputConsole = findViewById(R.id.output_console);
        tabLayout = findViewById(R.id.tab_layout);
        runButton = findViewById(R.id.run_button);
        newFileButton = findViewById(R.id.new_file_button);
        copyOutputButton = findViewById(R.id.copy_output_button);
        lineNumberView = findViewById(R.id.line_number_view);
        animationOverlay = findViewById(R.id.animation_overlay);
        
        toolbarLogoAnimation = findViewById(R.id.toolbar_logo_animation);
        explorerIcon = findViewById(R.id.explorer_icon);
        editorStatusAnimation = findViewById(R.id.editor_status_animation);
        runButtonLoading = findViewById(R.id.run_button_loading);
        runButtonPulse = findViewById(R.id.run_button_pulse);
        newFilePulse = findViewById(R.id.new_file_pulse);
        resultAnimation = findViewById(R.id.result_animation);
    }
    
    private void initializeEditorFeatures() {
        undoRedoManager = new UndoRedoManager(codeEditor);
        recentFilesManager = new RecentFilesManager(this);
        errorHighlightManager = new ErrorHighlightManager(codeEditor);
        codeFoldingManager = new CodeFoldingManager(codeEditor);
        
        // TODO: Fix LineNumberView to work with EditText instead of CodeEditor
        // if (lineNumberView != null) {
        //     lineNumberView.attachToCodeEditor(codeEditor);
        // }
        
        codeEditor.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            
            @Override
            public void afterTextChanged(android.text.Editable s) {
                String code = s.toString();
                errorHighlightManager.highlightErrors(code);
                codeFoldingManager.analyzeFoldRegions(code);
                codeFoldingManager.applyFolding();
            }
        });
    }
    
    private void initializeLottieAnimations() {
        if (toolbarLogoAnimation != null) {
            try {
                toolbarLogoAnimation.setAnimation("coding_animation.json");
                toolbarLogoAnimation.setRepeatCount(0);
            } catch (Exception e) {
                toolbarLogoAnimation.setVisibility(View.GONE);
            }
        }
        
        if (explorerIcon != null) {
            try {
                explorerIcon.setAnimation("coding_animation.json");
                explorerIcon.setRepeatCount(0);
                explorerIcon.setOnClickListener(v -> toggleFileExplorer());
            } catch (Exception e) {
                explorerIcon.setOnClickListener(v -> toggleFileExplorer());
            }
        }
        
        if (editorStatusAnimation != null) {
            try {
                editorStatusAnimation.setAnimation("typing_animation.json");
                editorStatusAnimation.setRepeatCount(0);
            } catch (Exception e) {
                editorStatusAnimation.setVisibility(View.GONE);
            }
        }
        
        if (runButtonLoading != null) {
            try {
                runButtonLoading.setAnimation("loading_animation.json");
                runButtonLoading.setRepeatCount(-1);
                runButtonLoading.setVisibility(View.GONE);
            } catch (Exception e) {
                runButtonLoading.setVisibility(View.GONE);
            }
        }
        
        if (runButtonPulse != null) {
            try {
                runButtonPulse.setAnimation("pulse_animation.json");
                runButtonPulse.setRepeatCount(-1);
                runButtonPulse.setSpeed(0.8f);
            } catch (Exception e) {
                runButtonPulse.setVisibility(View.GONE);
            }
        }
        
        if (newFilePulse != null) {
            try {
                newFilePulse.setAnimation("pulse_animation.json");
                newFilePulse.setRepeatCount(-1);
                newFilePulse.setSpeed(0.6f);
            } catch (Exception e) {
                newFilePulse.setVisibility(View.GONE);
            }
        }
        
        if (resultAnimation != null) {
            try {
                resultAnimation.setRepeatCount(0);
            } catch (Exception e) {
                resultAnimation.setVisibility(View.GONE);
            }
        }
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }
    
    private void setupListeners() {
        if (runButton != null) {
            runButton.setOnClickListener(v -> runCodeWithAnimation());
        }
        if (newFileButton != null) {
            newFileButton.setOnClickListener(v -> newFileWithAnimation());
        }
        if (copyOutputButton != null) {
            copyOutputButton.setOnClickListener(v -> copyOutputToClipboard());
        }
        
        if (animationOverlay != null) {
            animationOverlay.setOnClickListener(v -> hideAnimationOverlay());
        }
        
        if (codeEditor != null) {
            codeEditor.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus && editorStatusAnimation != null) {
                    try {
                        editorStatusAnimation.setAnimation("typing_animation.json");
                        editorStatusAnimation.setSpeed(1.5f);
                        editorStatusAnimation.playAnimation();
                    } catch (Exception e) {
                    }
                    
                    if (newFilePulse != null && !codeEditor.getText().toString().trim().isEmpty()) {
                        try {
                            newFilePulse.cancelAnimation();
                        } catch (Exception e) {
                        }
                    }
                } else if (editorStatusAnimation != null) {
                    try {
                        editorStatusAnimation.cancelAnimation();
                    } catch (Exception e) {
                    }
                }
            });
        }
        
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    switchToTab(tab.getPosition());
                }
                
                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}
                
                @Override
                public void onTabReselected(TabLayout.Tab tab) {}
            });
            
            TabLayout.Tab mainTab = tabLayout.newTab().setText("Main");
            tabLayout.addTab(mainTab);
        }
    }
    
    private void startInitialAnimations() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (toolbarLogoAnimation != null) {
                try {
                    toolbarLogoAnimation.playAnimation();
                } catch (Exception e) {
                }
            }
            
            if (editorStatusAnimation != null) {
                try {
                    editorStatusAnimation.playAnimation();
                } catch (Exception e) {
                }
            }
        }, 500);
        
        if (explorerIcon != null) {
            try {
                explorerIcon.setSpeed(0.5f);
                explorerIcon.playAnimation();
            } catch (Exception e) {
            }
        }
        
        if (runButtonPulse != null) {
            try {
                runButtonPulse.setSpeed(0.8f);
                runButtonPulse.playAnimation();
            } catch (Exception e) {
            }
        }
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (newFilePulse != null && codeEditor.getText().toString().trim().isEmpty()) {
                try {
                    newFilePulse.setSpeed(0.6f);
                    newFilePulse.playAnimation();
                } catch (Exception e) {
                }
            }
        }, 2000);
    }
    
    private void loadSampleCode() {
        if (codeEditor != null) {
            codeEditor.setText(SAMPLE_CODE);
        }
    }
    
    private void runCodeWithAnimation() {
        String code = codeEditor.getText().toString();
        if (code.trim().isEmpty()) {
            showError("Please enter some Java code to run");
            return;
        }
        
        showLoadingAnimation();
        
        outputConsole.setText("Compiling Java code with Eclipse JDT Compiler...\nUsing professional-grade compilation engine.\n");
        runButton.setEnabled(false);
        
        if (editorStatusAnimation != null) {
            try {
                editorStatusAnimation.setAnimation("loading_animation.json");
                editorStatusAnimation.playAnimation();
            } catch (Exception e) {
            }
        }
        
        executorService.execute(() -> {
            try {
                JavaExecutor.ExecutionResult result = javaExecutor.executeJavaCode(code);
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    hideLoadingAnimation();
                    
                    if (result.isSuccess()) {
                        outputConsole.setText(result.getOutput());
                        showSuccessAnimation();
                    } else {
                        String errorOutput = result.getFormattedErrorMessage();
                        if (errorOutput == null || errorOutput.trim().isEmpty()) {
                            errorOutput = "Compilation failed: " + 
                                (result.getErrorMessage() != null ? result.getErrorMessage() : "Unknown error");
                        }
                        outputConsole.setText(errorOutput);
                        showErrorAnimation();
                    }
                    
                    runButton.setEnabled(true);
                    
                    if (editorStatusAnimation != null) {
                        try {
                            editorStatusAnimation.cancelAnimation();
                        } catch (Exception e) {
                        }
                    }
                });
                
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    hideLoadingAnimation();
                    outputConsole.setText("✗ System Error: " + e.getMessage() + 
                        "\n\nThis might be due to a configuration issue. Please try again.");
                    runButton.setEnabled(true);
                    showErrorAnimation();
                    
                    if (editorStatusAnimation != null) {
                        try {
                            editorStatusAnimation.cancelAnimation();
                        } catch (Exception e2) {
                        }
                    }
                });
            }
        });
    }
    
    private void newFileWithAnimation() {
        if (newFilePulse != null) {
            try {
                newFilePulse.cancelAnimation();
            } catch (Exception e) {
            }
        }
        
        newFileButton.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    codeEditor.setText("");
                    outputConsole.setText("Ready for new code...");
                    
                    newFileButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .setListener(null)
                        .start();
                    
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (newFilePulse != null) {
                            try {
                                newFilePulse.playAnimation();
                            } catch (Exception e) {
                            }
                        }
                    }, 1000);
                }
            })
            .start();
    }
    
    private void showLoadingAnimation() {
        runButton.setVisibility(View.INVISIBLE);
        if (runButtonLoading != null) {
            runButtonLoading.setVisibility(View.VISIBLE);
            try {
                runButtonLoading.playAnimation();
            } catch (Exception e) {
            }
        }
        
        if (runButtonPulse != null) {
            try {
                runButtonPulse.pauseAnimation();
            } catch (Exception e) {
            }
        }
    }
    
    private void hideLoadingAnimation() {
        if (runButtonLoading != null) {
            try {
                runButtonLoading.cancelAnimation();
                runButtonLoading.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }
        runButton.setVisibility(View.VISIBLE);
        
        if (runButtonPulse != null) {
            try {
                runButtonPulse.resumeAnimation();
            } catch (Exception e) {
            }
        }
    }
    
    private void showSuccessAnimation() {
        if (resultAnimation != null && animationOverlay != null) {
            try {
                resultAnimation.setAnimation("success_animation.json");
                showAnimationOverlay();
                resultAnimation.playAnimation();
                
                new Handler(Looper.getMainLooper()).postDelayed(this::hideAnimationOverlay, 2000);
            } catch (Exception e) {
            }
        }
    }
    
    private void showErrorAnimation() {
        if (resultAnimation != null && animationOverlay != null) {
            try {
                resultAnimation.setAnimation("error_animation.json");
                resultAnimation.setSpeed(1.0f);
                showAnimationOverlay();
                resultAnimation.playAnimation();
                
                new Handler(Looper.getMainLooper()).postDelayed(this::hideAnimationOverlay, 2000);
            } catch (Exception e) {
            }
        }
    }
    
    private void showAnimationOverlay() {
        if (animationOverlay != null) {
            animationOverlay.setVisibility(View.VISIBLE);
            animationOverlay.setAlpha(0f);
            animationOverlay.animate()
                .alpha(1f)
                .setDuration(300)
                .start();
        }
    }
    
    private void hideAnimationOverlay() {
        if (animationOverlay != null) {
            animationOverlay.animate()
                .alpha(0f)
                .setDuration(300)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        animationOverlay.setVisibility(View.GONE);
                        if (resultAnimation != null) {
                            try {
                                resultAnimation.cancelAnimation();
                            } catch (Exception e) {
                            }
                        }
                    }
                })
                .start();
        }
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        outputConsole.setText("Error: " + message);
    }
    
    private void testCompilers() {
        String testCode = "System.out.println(\"Test output: \" + (5 + 3));";
        
        executorService.execute(() -> {
            try {
                JavaExecutor.ExecutionResult result = javaExecutor.executeJavaCode(testCode);
                
                if (result.isSuccess()) {
                } else {
                    String formatted = result.getFormattedErrorMessage();
                    if (formatted != null && !formatted.trim().isEmpty()) {
                    }
                }
                
            } catch (Exception e) {
            }
        });
    }
    
    private void runQuickTest() {
        executorService.execute(() -> {
            try {
                Log.d("MainActivity", "Running quick test of Java compilation");
                
                String testCode = "System.out.println(\"Quick test: \" + (2 + 3));";
                JavaExecutor.ExecutionResult result = javaExecutor.executeJavaCode(testCode);
                
                if (result.isSuccess()) {
                    Log.d("MainActivity", "✓ Quick test SUCCESS - Output: " + result.getOutput());
                } else {
                    Log.e("MainActivity", "✗ Quick test FAILED - Error: " + result.getErrorMessage());
                    if (result.getFormattedErrorMessage() != null) {
                        Log.e("MainActivity", "Formatted error: " + result.getFormattedErrorMessage());
                    }
                }
                
            } catch (Exception e) {
                Log.e("MainActivity", "Quick test failed with exception", e);
            }
        });
    }
    
    private void createNewFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New File");
        
        final EditText input = new EditText(this);
        input.setHint("Enter filename (e.g., MyClass.java)");
        builder.setView(input);
        
        builder.setPositiveButton("Create", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.isEmpty()) {
                if (!fileName.endsWith(".java")) {
                    fileName += ".java";
                }
                addNewTab(fileName);
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void addNewTab(String fileName) {
        if (tabLayout != null) {
            TabLayout.Tab newTab = tabLayout.newTab().setText(fileName);
            tabLayout.addTab(newTab);
            tabLayout.selectTab(newTab);
        }
        
        currentFileName = fileName;
        currentFilePath = null;
        codeEditor.setText("");
    }
    
    private void switchToTab(int position) {
        if (tabLayout != null) {
            TabLayout.Tab tab = tabLayout.getTabAt(position);
            if (tab != null && tab.getText() != null) {
                currentFileName = tab.getText().toString();
            }
        }
    }
    
    private void copyOutputToClipboard() {
        String output = outputConsole.getText().toString();
        
        if (output.trim().isEmpty()) {
            Toast.makeText(this, "No output to copy", Toast.LENGTH_SHORT).show();
            return;
        }
        
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("SnipRun Output", output);
        clipboard.setPrimaryClip(clip);
        
        Toast.makeText(this, "Output copied to clipboard", Toast.LENGTH_SHORT).show();
        
        copyOutputButton.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(100)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    copyOutputButton.animate()
                        .scaleX(1.0f)
                        .scaleY(1.0f)
                        .setDuration(100)
                        .setListener(null)
                        .start();
                }
            })
            .start();
    }
    
    private void toggleFileExplorer() {
        // Fragment container doesn't exist in current layout, skip file explorer functionality
        if (findViewById(R.id.fragment_container) == null) {
            return;
        }
        
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment existingFragment = fragmentManager.findFragmentById(R.id.fragment_container);
        
        if (existingFragment == null && !isFileExplorerOpen) {
            FileExplorerFragment fileExplorerFragment = new FileExplorerFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container, fileExplorerFragment);
            transaction.addToBackStack(null);
            transaction.commit();
            
            isFileExplorerOpen = true;
            
            if (explorerIcon != null) {
                try {
                    explorerIcon.playAnimation();
                } catch (Exception e) {
                }
            }
            
        } else if (existingFragment != null && isFileExplorerOpen) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.remove(existingFragment);
            transaction.commit();
            
            isFileExplorerOpen = false;
        }
    }
    
    @Override
    public void onFileSelected(String filePath, String fileName) {
        try {
            String content = fileManager.readFile(filePath);
            codeEditor.setText(content);
            currentFileName = fileName;
            currentFilePath = filePath;
            
            recentFilesManager.addRecentFile(filePath);
            
            if (tabLayout != null) {
                boolean tabExists = false;
                for (int i = 0; i < tabLayout.getTabCount(); i++) {
                    TabLayout.Tab tab = tabLayout.getTabAt(i);
                    if (tab != null && tab.getText() != null && tab.getText().toString().equals(fileName)) {
                        tabLayout.selectTab(tab);
                        tabExists = true;
                        break;
                    }
                }
                
                if (!tabExists) {
                    TabLayout.Tab newTab = tabLayout.newTab().setText(fileName);
                    tabLayout.addTab(newTab);
                    tabLayout.selectTab(newTab);
                }
            }
            
            Toast.makeText(this, "Opened: " + fileName, Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_save) {
            saveCurrentFile();
            return true;
        } else if (id == R.id.action_open) {
            toggleFileExplorer();
            return true;
        } else if (id == R.id.action_settings) {
            openSettings();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_find_replace) {
            showAdvancedSearchReplaceDialog();
            return true;
        } else if (id == R.id.action_undo) {
            undoRedoManager.undo();
            return true;
        } else if (id == R.id.action_redo) {
            undoRedoManager.redo();
            return true;
        } else if (id == R.id.action_fold_all) {
            codeFoldingManager.foldAll();
            return true;
        } else if (id == R.id.action_unfold_all) {
            codeFoldingManager.unfoldAll();
            return true;
        } else if (id == R.id.action_recent_files) {
            showRecentFilesDialog();
            return true;
        } else if (id == R.id.action_templates) {
            showCodeTemplatesDialog();
            return true;
        } else if (id == R.id.action_clear_console) {
            outputConsole.setText("");
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void saveCurrentFile() {
        String code = codeEditor.getText().toString();
        
        if (currentFilePath != null) {
            try {
                fileManager.saveFile(currentFilePath, code);
                Toast.makeText(this, "File saved: " + currentFileName, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } else {
            saveAsNewFile(code);
        }
    }
    
    private void saveAsNewFile(String code) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save As");
        
        final EditText input = new EditText(this);
        input.setText(currentFileName);
        builder.setView(input);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.isEmpty()) {
                if (!fileName.endsWith(".java")) {
                    fileName += ".java";
                }
                
                try {
                    String filePath = fileManager.getProjectRoot() + "/" + fileName;
                    fileManager.saveFile(filePath, code);
                    currentFileName = fileName;
                    currentFilePath = filePath;
                    Toast.makeText(this, "File saved: " + fileName, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(this, "Error saving file: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void openSettings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 1001);
    }
    
    private void showCodeTemplatesDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose Code Template");
        
        String[] templateNames = CodeTemplates.getTemplateNames();
        
        builder.setItems(templateNames, (dialog, which) -> {
            String selectedTemplate = CodeTemplates.getTemplate(which);
            codeEditor.setText(selectedTemplate);
            Toast.makeText(MainActivity.this, "Template loaded: " + templateNames[which], Toast.LENGTH_SHORT).show();
            
            if (editorStatusAnimation != null) {
                try {
                    editorStatusAnimation.setAnimation("success_animation.json");
                    editorStatusAnimation.playAnimation();
                } catch (Exception e) {
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About SnipRun IDE");
        builder.setMessage("SnipRun IDE v4.0\n\nModern Android Java Interpreter\n\nFeatures:\n• Beautiful Lottie animations\n• Modern Material Design 3\n• Android-compatible Java interpretation\n• Real-time code parsing and execution\n• JavaParser-based syntax validation\n• Enhanced user experience\n• Educational Java environment\n• Copy output to clipboard\n• File management with tabs\n• Syntax highlighting\n• Code folding\n• Undo/Redo support\n• Recent files\n• Code templates\n\nPowered by:\n• Lottie Android 6.6.7\n• JavaParser 3.25.5\n• Material Design 3\n\nDesigned for modern coding experience");
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("GitHub", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yourrepo/sniprun"));
            startActivity(intent);
        });
        builder.show();
    }
    
    private void showFindReplaceDialog() {
        // TODO: Update FindReplaceDialog to work with EditText instead of CodeEditor
        // FindReplaceDialog dialog = new FindReplaceDialog(this, codeEditor);
        // dialog.show();
        Toast.makeText(this, "Find/Replace temporarily disabled", Toast.LENGTH_SHORT).show();
    }
    
    private void showAdvancedSearchReplaceDialog() {
        // TODO: Update AdvancedSearchReplaceDialog to work with EditText instead of CodeEditor
        // AdvancedSearchReplaceDialog dialog = new AdvancedSearchReplaceDialog(this, codeEditor);
        // dialog.show();
        Toast.makeText(this, "Advanced Search/Replace temporarily disabled", Toast.LENGTH_SHORT).show();
    }
    
    private void showRecentFilesDialog() {
        List<RecentFilesManager.RecentFileInfo> recentFiles = recentFilesManager.getRecentFileInfos();
        
        if (recentFiles.isEmpty()) {
            Toast.makeText(this, "No recent files", Toast.LENGTH_SHORT).show();
            return;
        }
        
        String[] fileNames = new String[recentFiles.size()];
        for (int i = 0; i < recentFiles.size(); i++) {
            fileNames[i] = recentFiles.get(i).getDisplayName();
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recent Files");
        builder.setItems(fileNames, (dialog, which) -> {
            RecentFilesManager.RecentFileInfo fileInfo = recentFiles.get(which);
            openRecentFile(fileInfo.getFilePath());
        });
        
        builder.setNegativeButton("Clear Recent", (dialog, which) -> {
            recentFilesManager.clearRecentFiles();
            Toast.makeText(this, "Recent files cleared", Toast.LENGTH_SHORT).show();
        });
        
        builder.show();
    }
    
    private void openRecentFile(String filePath) {
        try {
            String content = fileManager.readFile(filePath);
            codeEditor.setText(content);
            
            String fileName = filePath.substring(filePath.lastIndexOf('/') + 1);
            currentFileName = fileName;
            currentFilePath = filePath;
            
            Toast.makeText(this, "Opened: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    private void setupOnBackPressed() {
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isFileExplorerOpen) {
                    toggleFileExplorer();
                } else {
                    finish();
                }
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            if (data.getBooleanExtra("theme_changed", false)) {
                recreate();
            }
            if (data.getBooleanExtra("settings_reset", false)) {
                Toast.makeText(this, "Settings reset to defaults", Toast.LENGTH_SHORT).show();
            }
            if (data.getBooleanExtra("settings_changed", false)) {
                applySettings();
            }
        }
    }
    
    private void applySettings() {
        if (appSettings.isLineNumbersEnabled() && lineNumberView != null) {
            lineNumberView.setVisibility(View.VISIBLE);
        } else if (lineNumberView != null) {
            lineNumberView.setVisibility(View.GONE);
        }
        
        errorHighlightManager.setHighlightingEnabled(appSettings.isSyntaxHighlightingEnabled());
        codeFoldingManager.setFoldingEnabled(true);
        
        codeEditor.setTextSize(appSettings.getFontSize());
        
        if (appSettings.isWordWrapEnabled()) {
            codeEditor.setHorizontallyScrolling(false);
        } else {
            codeEditor.setHorizontallyScrolling(true);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if (toolbarLogoAnimation != null && toolbarLogoAnimation.isAnimating()) {
            try {
                toolbarLogoAnimation.pauseAnimation();
            } catch (Exception e) {
            }
        }
        if (explorerIcon != null && explorerIcon.isAnimating()) {
            try {
                explorerIcon.pauseAnimation();
            } catch (Exception e) {
            }
        }
        if (runButtonPulse != null && runButtonPulse.isAnimating()) {
            try {
                runButtonPulse.pauseAnimation();
            } catch (Exception e) {
            }
        }
        if (newFilePulse != null && newFilePulse.isAnimating()) {
            try {
                newFilePulse.pauseAnimation();
            } catch (Exception e) {
            }
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (toolbarLogoAnimation != null) {
            try {
                toolbarLogoAnimation.resumeAnimation();
            } catch (Exception e) {
            }
        }
        if (explorerIcon != null) {
            try {
                explorerIcon.resumeAnimation();
            } catch (Exception e) {
            }
        }
        if (runButtonPulse != null) {
            try {
                runButtonPulse.resumeAnimation();
            } catch (Exception e) {
            }
        }
        if (newFilePulse != null && codeEditor.getText().toString().trim().isEmpty()) {
            try {
                newFilePulse.resumeAnimation();
            } catch (Exception e) {
            }
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
