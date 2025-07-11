package com.gorai.sniprun;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    
    private EditText codeEditor;
    private TextView outputConsole;
    private ListView fileListView;
    private FloatingActionButton runButton;
    private FloatingActionButton newFileButton;
    
    private File currentProjectDir;
    private List<String> fileList;
    private ArrayAdapter<String> fileAdapter;
    private ExecutorService executorService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        initializeViews();
        setupToolbar();
        setupFileSystem();
        setupListeners();
        
        executorService = Executors.newSingleThreadExecutor();
        
        loadSampleCode();
    }
    
    private void initializeViews() {
        codeEditor = findViewById(R.id.code_editor);
        outputConsole = findViewById(R.id.output_console);
        fileListView = findViewById(R.id.file_list_view);
        runButton = findViewById(R.id.run_button);
        newFileButton = findViewById(R.id.new_file_button);
    }
    
    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("SnipRun IDE");
    }
    
    private void setupFileSystem() {
        currentProjectDir = new File(getFilesDir(), "projects");
        if (!currentProjectDir.exists()) {
            currentProjectDir.mkdirs();
        }
        
        fileList = new ArrayList<>();
        fileAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, fileList);
        fileListView.setAdapter(fileAdapter);
        
        refreshFileList();
    }
    
    private void setupListeners() {
        runButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runCode();
            }
        });
        
        newFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewFileDialog();
            }
        });
        
        fileListView.setOnItemClickListener((parent, view, position, id) -> {
            String fileName = fileList.get(position);
            loadFile(fileName);
        });
    }
    
    private void loadSampleCode() {
        String sampleCode = "public class HelloWorld {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello, SnipRun IDE!\");\n" +
                "        \n" +
                "        // Try some basic Java operations\n" +
                "        int sum = 0;\n" +
                "        for (int i = 1; i <= 10; i++) {\n" +
                "            sum += i;\n" +
                "        }\n" +
                "        System.out.println(\"Sum of 1-10: \" + sum);\n" +
                "    }\n" +
                "}";
        codeEditor.setText(sampleCode);
    }
    
    private void refreshFileList() {
        fileList.clear();
        File[] files = currentProjectDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".java")) {
                    fileList.add(file.getName());
                }
            }
        }
        fileAdapter.notifyDataSetChanged();
    }
    
    private void loadFile(String fileName) {
        File file = new File(currentProjectDir, fileName);
        try {
            StringBuilder content = new StringBuilder();
            java.util.Scanner scanner = new java.util.Scanner(file);
            while (scanner.hasNextLine()) {
                content.append(scanner.nextLine()).append("\n");
            }
            scanner.close();
            codeEditor.setText(content.toString());
            Toast.makeText(this, "Loaded: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            showError("Error loading file: " + e.getMessage());
        }
    }
    
    private void showNewFileDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Java File");
        
        final EditText input = new EditText(this);
        input.setHint("Enter filename (without .java extension)");
        builder.setView(input);
        
        builder.setPositiveButton("Create", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString().trim();
                if (!fileName.isEmpty()) {
                    createNewFile(fileName);
                }
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void createNewFile(String fileName) {
        if (!fileName.endsWith(".java")) {
            fileName += ".java";
        }
        
        String className = fileName.replace(".java", "");
        String template = "public class " + className + " {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"Hello from " + className + "!\");\n" +
                "    }\n" +
                "}";
        
        File file = new File(currentProjectDir, fileName);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(template);
            writer.close();
            
            codeEditor.setText(template);
            refreshFileList();
            Toast.makeText(this, "Created: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            showError("Error creating file: " + e.getMessage());
        }
    }
    
    private void runCode() {
        String code = codeEditor.getText().toString();
        if (code.trim().isEmpty()) {
            showError("Please enter some Java code to run");
            return;
        }
        
        outputConsole.setText("Running code...\n");
        runButton.setEnabled(false);
        
        executorService.execute(() -> {
            try {
                String output = executeJavaCode(code);
                
                new Handler(Looper.getMainLooper()).post(() -> {
                    outputConsole.setText(output);
                    runButton.setEnabled(true);
                });
            } catch (Exception e) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    outputConsole.setText("Error: " + e.getMessage());
                    runButton.setEnabled(true);
                });
            }
        });
    }
    
    private String executeJavaCode(String code) {
        try {
            File tempFile = new File(getCacheDir(), "TempClass.java");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(code);
            writer.close();
            
            StringBuilder output = new StringBuilder();
            output.append("Code execution simulation:\n");
            output.append("========================\n\n");
            
            if (code.contains("System.out.println")) {
                String[] lines = code.split("\n");
                for (String line : lines) {
                    if (line.trim().startsWith("System.out.println")) {
                        String content = extractPrintContent(line);
                        if (content != null) {
                            output.append(content).append("\n");
                        }
                    }
                }
            } else {
                output.append("Code compiled successfully!\n");
                output.append("(Note: This is a simulation. For full execution, integrate with a Java compiler)\n");
            }
            
            output.append("\nExecution completed.");
            return output.toString();
            
        } catch (Exception e) {
            return "Error during execution: " + e.getMessage();
        }
    }
    
    private String extractPrintContent(String line) {
        try {
            int start = line.indexOf("\"");
            int end = line.lastIndexOf("\"");
            if (start != -1 && end != -1 && start < end) {
                return line.substring(start + 1, end);
            }
            
            if (line.contains("System.out.println(") && line.contains("+")) {
                return "Result of expression";
            }
        } catch (Exception e) {
            return "Output";
        }
        return null;
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
        
        if (id == R.id.action_save) {
            saveCurrentFile();
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
    
    private void saveCurrentFile() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Save File");
        
        final EditText input = new EditText(this);
        input.setHint("Enter filename (without .java extension)");
        builder.setView(input);
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            String fileName = input.getText().toString().trim();
            if (!fileName.isEmpty()) {
                saveFile(fileName);
            }
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void saveFile(String fileName) {
        if (!fileName.endsWith(".java")) {
            fileName += ".java";
        }
        
        File file = new File(currentProjectDir, fileName);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(codeEditor.getText().toString());
            writer.close();
            
            refreshFileList();
            Toast.makeText(this, "Saved: " + fileName, Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            showError("Error saving file: " + e.getMessage());
        }
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About SnipRun IDE");
        builder.setMessage("SnipRun IDE v1.0\n\nA simple Java IDE for Android\n\nFeatures:\n• Code editing\n• File management\n• Code execution simulation\n• Syntax-aware interface");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executorService != null) {
            executorService.shutdown();
        }
    }
} 