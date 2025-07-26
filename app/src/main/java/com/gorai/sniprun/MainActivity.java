package com.gorai.sniprun;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    
    private EditText codeEditor;
    private TextView outputConsole;
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
        
        // Test our compilers
        testCompilers();
        
        loadSampleCode();
        startInitialAnimations();
    }
    
    private void initializeViews() {
        codeEditor = findViewById(R.id.code_editor);
        outputConsole = findViewById(R.id.output_console);
        runButton = findViewById(R.id.run_button);
        newFileButton = findViewById(R.id.new_file_button);
        copyOutputButton = findViewById(R.id.copy_output_button);
        animationOverlay = findViewById(R.id.animation_overlay);
    }
    
    private void initializeLottieAnimations() {
        toolbarLogoAnimation = findViewById(R.id.toolbar_logo_animation);
        explorerIcon = findViewById(R.id.explorer_icon);
        editorStatusAnimation = findViewById(R.id.editor_status_animation);
        runButtonLoading = findViewById(R.id.run_button_loading);
        runButtonPulse = findViewById(R.id.run_button_pulse);
        newFilePulse = findViewById(R.id.new_file_pulse);
        resultAnimation = findViewById(R.id.result_animation);
    }
    
    private void startInitialAnimations() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (toolbarLogoAnimation != null) {
                toolbarLogoAnimation.playAnimation();
            }
        }, 500);
        
        if (explorerIcon != null) {
            explorerIcon.setSpeed(0.5f);
            explorerIcon.playAnimation();
        }
        
        if (runButtonPulse != null) {
            runButtonPulse.setSpeed(0.8f);
            runButtonPulse.playAnimation();
        }
        
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (newFilePulse != null && codeEditor.getText().toString().trim().isEmpty()) {
                newFilePulse.setSpeed(0.6f);
                newFilePulse.playAnimation();
            }
        }, 2000);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
    }
    
    private void setupListeners() {
        runButton.setOnClickListener(v -> runCodeWithAnimation());
        
        newFileButton.setOnClickListener(v -> {
            newFileWithAnimation();
        });
        
        copyOutputButton.setOnClickListener(v -> copyOutputToClipboard());
        
        animationOverlay.setOnClickListener(v -> hideAnimationOverlay());
        
        codeEditor.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && editorStatusAnimation != null) {
                editorStatusAnimation.setAnimation("typing_animation.json");
                editorStatusAnimation.setSpeed(1.5f);
                editorStatusAnimation.playAnimation();
                
                if (newFilePulse != null && !codeEditor.getText().toString().trim().isEmpty()) {
                    newFilePulse.cancelAnimation();
                }
            } else if (editorStatusAnimation != null) {
                editorStatusAnimation.cancelAnimation();
            }
        });
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
            editorStatusAnimation.setAnimation("loading_animation.json");
            editorStatusAnimation.playAnimation();
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
                        // Show detailed compilation errors
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
                        editorStatusAnimation.cancelAnimation();
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
                        editorStatusAnimation.cancelAnimation();
                    }
                });
            }
        });
    }
    
    private void newFileWithAnimation() {
        if (newFilePulse != null) {
            newFilePulse.cancelAnimation();
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
                            newFilePulse.playAnimation();
                        }
                    }, 1000);
                }
            })
            .start();
    }
    
    private void showLoadingAnimation() {
        runButton.setVisibility(View.INVISIBLE);
        runButtonLoading.setVisibility(View.VISIBLE);
        runButtonLoading.playAnimation();
        
        if (runButtonPulse != null) {
            runButtonPulse.pauseAnimation();
        }
    }
    
    private void hideLoadingAnimation() {
        runButtonLoading.cancelAnimation();
        runButtonLoading.setVisibility(View.GONE);
        runButton.setVisibility(View.VISIBLE);
        
        if (runButtonPulse != null) {
            runButtonPulse.resumeAnimation();
        }
    }
    
    private void showSuccessAnimation() {
        resultAnimation.setAnimation("success_animation.json");
        showAnimationOverlay();
        resultAnimation.playAnimation();
        
        new Handler(Looper.getMainLooper()).postDelayed(this::hideAnimationOverlay, 2000);
    }
    
    private void showErrorAnimation() {
        resultAnimation.setAnimation("error_animation.json");
        resultAnimation.setSpeed(1.0f);
        showAnimationOverlay();
        resultAnimation.playAnimation();
        
        new Handler(Looper.getMainLooper()).postDelayed(this::hideAnimationOverlay, 2000);
    }
    
    private void showAnimationOverlay() {
        animationOverlay.setVisibility(View.VISIBLE);
        animationOverlay.setAlpha(0f);
        animationOverlay.animate()
            .alpha(1f)
            .setDuration(300)
            .start();
    }
    
    private void hideAnimationOverlay() {
        animationOverlay.animate()
            .alpha(0f)
            .setDuration(300)
            .setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    animationOverlay.setVisibility(View.GONE);
                    resultAnimation.cancelAnimation();
                }
            })
            .start();
    }
    
    private void loadSampleCode() {
        String sampleCode = "import java.util.*;\n\n" +
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
        codeEditor.setText(sampleCode);
    }
    
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        outputConsole.setText("Error: " + message);
    }
    
    private void testCompilers() {
        android.util.Log.d("MainActivity", "Testing compilers on startup...");
        
        // Simple test code
        String testCode = "System.out.println(\"Test output: \" + (5 + 3));";
        
        executorService.execute(() -> {
            try {
                android.util.Log.d("MainActivity", "Executing test code: " + testCode);
                JavaExecutor.ExecutionResult result = javaExecutor.executeJavaCode(testCode);
                
                if (result.isSuccess()) {
                    android.util.Log.d("MainActivity", "✓ Compiler test SUCCESS - Output: " + result.getOutput());
                } else {
                    android.util.Log.d("MainActivity", "✗ Compiler test FAILED - Error: " + result.getErrorMessage());
                    String formatted = result.getFormattedErrorMessage();
                    if (formatted != null && !formatted.trim().isEmpty()) {
                        android.util.Log.d("MainActivity", "Formatted error: " + formatted);
                    }
                }
                android.util.Log.d("MainActivity", "Test execution time: " + result.getExecutionTimeMs() + "ms");
                
            } catch (Exception e) {
                android.util.Log.e("MainActivity", "Compiler test failed with exception", e);
            }
        });
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_templates) {
            showCodeTemplatesDialog();
            return true;
        } else if (id == R.id.action_clear_console) {
            outputConsole.setText("");
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
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
                editorStatusAnimation.setAnimation("success_animation.json");
                editorStatusAnimation.playAnimation();
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About SnipRun IDE");
        builder.setMessage("SnipRun IDE v4.0\n\nModern Android Java Interpreter\n\nFeatures:\n• Beautiful Lottie animations\n• Modern Material Design 3\n• Android-compatible Java interpretation\n• Real-time code parsing and execution\n• JavaParser-based syntax validation\n• Enhanced user experience\n• Educational Java environment\n• Copy output to clipboard\n\nPowered by:\n• Lottie Android 6.6.7\n• JavaParser 3.25.5\n• Material Design 3\n\nDesigned for modern coding experience");
        builder.setPositiveButton("OK", null);
        builder.show();
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
    
    @Override
    protected void onPause() {
        super.onPause();
        if (toolbarLogoAnimation != null && toolbarLogoAnimation.isAnimating()) {
            toolbarLogoAnimation.pauseAnimation();
        }
        if (explorerIcon != null && explorerIcon.isAnimating()) {
            explorerIcon.pauseAnimation();
        }
        if (runButtonPulse != null && runButtonPulse.isAnimating()) {
            runButtonPulse.pauseAnimation();
        }
        if (newFilePulse != null && newFilePulse.isAnimating()) {
            newFilePulse.pauseAnimation();
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (toolbarLogoAnimation != null) {
            toolbarLogoAnimation.resumeAnimation();
        }
        if (explorerIcon != null) {
            explorerIcon.resumeAnimation();
        }
        if (runButtonPulse != null) {
            runButtonPulse.resumeAnimation();
        }
        if (newFilePulse != null && codeEditor.getText().toString().trim().isEmpty()) {
            newFilePulse.resumeAnimation();
        }
    }
}
