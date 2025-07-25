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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EnhancedMainActivity extends AppCompatActivity implements FileExplorerFragment.OnFileSelectedListener {
    
    private CodeEditor codeEditor;
    private TextView outputConsole;
    private TabLayout tabLayout;
    private FloatingActionButton runButton;
    private FloatingActionButton newFileButton;
    private MaterialButton copyOutputButton;
    
    private LottieAnimationView toolbarLogoAnimation;
    private LottieAnimationView explorerIcon;
    private LottieAnimationView editorStatusAnimation;
    private LottieAnimationView runButtonLoading;
    
    private ExecutorService executorService;
    private JavaExecutor javaExecutor;
    private FileManager fileManager;
    private AppSettings appSettings;
    
    private boolean isFileExplorerOpen = false;
    private String currentFileName = "Untitled.java";
    private String currentFilePath = null;
    
    private static final String SAMPLE_CODE = 
        "public class Main {\n" +
        "    public static void main(String[] args) {\n" +
        "        System.out.println(\"Hello, World!\");\n" +
        "        \n" +
        "        // Try some basic operations\n" +
        "        int a = 10;\n" +
        "        int b = 20;\n" +
        "        System.out.println(\"Sum: \" + (a + b));\n" +
        "        \n" +
        "        // Array example\n" +
        "        int[] numbers = {1, 2, 3, 4, 5};\n" +
        "        for (int num : numbers) {\n" +
        "            System.out.print(num + \" \");\n" +
        "        }\n" +
        "        System.out.println();\n" +
        "    }\n" +
        "}";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enhanced_main);
        
        initializeViews();
        initializeLottieAnimations();
        setupToolbar();
        setupListeners();
        
        executorService = Executors.newSingleThreadExecutor();
        javaExecutor = new JavaExecutor(this);
        fileManager = new FileManager(this);
        appSettings = new AppSettings(this);
        
        loadSampleCode();
        startInitialAnimations();
        setupOnBackPressed();
    }
    
    private void initializeViews() {
        codeEditor = findViewById(R.id.code_editor);
        outputConsole = findViewById(R.id.output_console);
        tabLayout = findViewById(R.id.tab_layout);
        runButton = findViewById(R.id.run_button);
        newFileButton = findViewById(R.id.new_file_button);
        copyOutputButton = findViewById(R.id.copy_output_button);
        
        toolbarLogoAnimation = findViewById(R.id.toolbar_logo_animation);
        explorerIcon = findViewById(R.id.explorer_icon);
        editorStatusAnimation = findViewById(R.id.editor_status_animation);
        runButtonLoading = findViewById(R.id.run_button_loading);
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
                explorerIcon.setAnimation("folder_animation.json");
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
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("");
        }
    }
    
    private void setupListeners() {
        runButton.setOnClickListener(v -> runCode());
        newFileButton.setOnClickListener(v -> createNewFile());
        copyOutputButton.setOnClickListener(v -> copyOutputToClipboard());
        
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
    }
    
    private void loadSampleCode() {
        if (codeEditor != null) {
            codeEditor.setText(SAMPLE_CODE);
        }
    }
    
    private void runCode() {
        String code = codeEditor.getText().toString().trim();
        
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter some code to run", Toast.LENGTH_SHORT).show();
            return;
        }
        
        runButton.setVisibility(View.GONE);
        if (runButtonLoading != null) {
            runButtonLoading.setVisibility(View.VISIBLE);
            try {
                runButtonLoading.playAnimation();
            } catch (Exception e) {
            }
        }
        
        outputConsole.setText("Running code...\n");
        
        executorService.execute(() -> {
            try {
                JavaExecutor.ExecutionResult result = javaExecutor.executeJavaCode(code);
                
                runOnUiThread(() -> {
                    if (result.isSuccess()) {
                        outputConsole.setText(result.getOutput());
                    } else {
                        outputConsole.setText("Error: " + result.getErrorMessage());
                    }
                    
                    if (runButtonLoading != null) {
                        runButtonLoading.setVisibility(View.GONE);
                        try {
                            runButtonLoading.pauseAnimation();
                        } catch (Exception e) {
                        }
                    }
                    runButton.setVisibility(View.VISIBLE);
                });
                
            } catch (Exception e) {
                runOnUiThread(() -> {
                    outputConsole.setText("Error: " + e.getMessage());
                    
                    if (runButtonLoading != null) {
                        runButtonLoading.setVisibility(View.GONE);
                        try {
                            runButtonLoading.pauseAnimation();
                        } catch (Exception ex) {
                        }
                    }
                    runButton.setVisibility(View.VISIBLE);
                });
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
        TabLayout.Tab newTab = tabLayout.newTab().setText(fileName);
        tabLayout.addTab(newTab);
        tabLayout.selectTab(newTab);
        
        currentFileName = fileName;
        currentFilePath = null;
        codeEditor.setText("");
    }
    
    private void switchToTab(int position) {
        TabLayout.Tab tab = tabLayout.getTabAt(position);
        if (tab != null && tab.getText() != null) {
            currentFileName = tab.getText().toString();
        }
    }
    
    private void copyOutputToClipboard() {
        String output = outputConsole.getText().toString();
        if (!output.isEmpty()) {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Code Output", output);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Output copied to clipboard", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No output to copy", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void toggleFileExplorer() {
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
            
            Toast.makeText(this, "Opened: " + fileName, Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Error opening file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.enhanced_main_menu, menu);
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
            showFindReplaceDialog();
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
        Toast.makeText(this, "Settings not implemented yet", Toast.LENGTH_SHORT).show();
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About SnipRun");
        builder.setMessage("SnipRun v1.0\n\nA simple Java code runner for Android.\n\nDeveloped with ❤️");
        builder.setPositiveButton("OK", null);
        builder.setNeutralButton("GitHub", (dialog, which) -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/yourrepo/sniprun"));
            startActivity(intent);
        });
        builder.show();
    }
    
    private void showFindReplaceDialog() {
        FindReplaceDialog dialog = new FindReplaceDialog(this, codeEditor);
        dialog.show();
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
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }
}
