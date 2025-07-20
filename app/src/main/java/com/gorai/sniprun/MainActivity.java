package com.gorai.sniprun;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    
    private EditText codeEditor;
    private TextView outputConsole;
    private FloatingActionButton runButton;
    private FloatingActionButton newFileButton;
    private ImageButton copyOutputButton;
    
    private ExecutorService executorService;
    private JavaExecutor javaExecutor;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupToolbar();
        setupListeners();
        
        executorService = Executors.newSingleThreadExecutor();
        javaExecutor = new JavaExecutor(this);
        
        loadSampleCode();
    }
    
    private void initializeViews() {
        codeEditor = findViewById(R.id.code_editor);
        outputConsole = findViewById(R.id.output_console);
        runButton = findViewById(R.id.run_button);
        newFileButton = findViewById(R.id.new_file_button);
        copyOutputButton = findViewById(R.id.copy_output_button);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SnipRun IDE - Android Java Interpreter");
    }
    
    private void setupListeners() {
        runButton.setOnClickListener(v -> runCode());
        
        newFileButton.setOnClickListener(v -> {
            codeEditor.setText("");
            outputConsole.setText("Ready for new code...");
        });
        
        copyOutputButton.setOnClickListener(v -> copyOutputToClipboard());
    }
    
    private void runCode() {
        String code = codeEditor.getText().toString();
        if (code.trim().isEmpty()) {
            showError("Please enter some Java code to run");
            return;
        }
        
        outputConsole.setText("Parsing and executing Java code...\nUsing Android-compatible Java interpreter.\n");
        runButton.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                JavaExecutor.ExecutionResult result = javaExecutor.executeJavaCode(code);
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    StringBuilder output = new StringBuilder();
                    
                    if (result.isSuccess()) {
                        
                        output.append(result.getOutput());
                    } else {
                        output.append("Error: \n").append(result.getErrorMessage());
                    }
                    
                    outputConsole.setText(output.toString());
                    runButton.setEnabled(true);
                });
                
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    outputConsole.setText("Unexpected error: " + e.getMessage());
                    runButton.setEnabled(true);
                });
            }
        });
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
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About SnipRun IDE");
        builder.setMessage("SnipRun IDE v3.0\n\nAndroid Java Interpreter\n\nFeatures:\n• Android-compatible Java interpretation\n• Real-time code parsing and execution\n• JavaParser-based syntax validation\n• Clean and simple interface\n• Educational Java environment\n• Copy output to clipboard\n\nPowered by JavaParser 3.25.5\nInspired by Cosmic IDE architecture");
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
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
