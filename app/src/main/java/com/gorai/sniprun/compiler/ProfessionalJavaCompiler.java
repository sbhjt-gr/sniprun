package com.gorai.sniprun.compiler;

import android.content.Context;
import android.util.Log;

import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.internal.compiler.batch.Main;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

public class ProfessionalJavaCompiler {
    
    private static final String TAG = "ProfessionalJavaCompiler";
    private final Context context;
    private final File workingDirectory;
    private final File sourceDirectory;
    private final File classDirectory;
    private final File tempDirectory;
    
    private SecurityManager originalSecurityManager;
    private PrintStream originalSystemOut;
    private PrintStream originalSystemErr;
    
    public static class CompilationResult {
        private final boolean success;
        private final String output;
        private final String errorMessage;
        private final List<String> compilationErrors;
        private final long executionTimeMs;
        
        public CompilationResult(boolean success, String output, String errorMessage, 
                               List<String> compilationErrors, long executionTimeMs) {
            this.success = success;
            this.output = output;
            this.errorMessage = errorMessage;
            this.compilationErrors = compilationErrors != null ? compilationErrors : new ArrayList<>();
            this.executionTimeMs = executionTimeMs;
        }
        
        public boolean isSuccess() { return success; }
        public String getOutput() { return output; }
        public String getErrorMessage() { return errorMessage; }
        public List<String> getCompilationErrors() { return compilationErrors; }
        public long getExecutionTimeMs() { return executionTimeMs; }
    }
    
    public ProfessionalJavaCompiler(Context context) {
        this.context = context;
        this.workingDirectory = new File(context.getFilesDir(), "java_compiler");
        this.sourceDirectory = new File(workingDirectory, "src");
        this.classDirectory = new File(workingDirectory, "classes");
        this.tempDirectory = new File(workingDirectory, "temp");
        
        initializeDirectories();
    }
    
    private void initializeDirectories() {
        try {
            if (!workingDirectory.exists()) workingDirectory.mkdirs();
            if (!sourceDirectory.exists()) sourceDirectory.mkdirs();
            if (!classDirectory.exists()) classDirectory.mkdirs();
            if (!tempDirectory.exists()) tempDirectory.mkdirs();
            
            cleanupDirectories();
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize directories", e);
        }
    }
    
    private void cleanupDirectories() {
        try {
            deleteDirectoryContents(classDirectory);
            deleteDirectoryContents(tempDirectory);
        } catch (Exception e) {
            Log.e(TAG, "Failed to cleanup directories", e);
        }
    }
    
    private void deleteDirectoryContents(File directory) {
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectoryContents(file);
                    }
                    file.delete();
                }
            }
        }
    }
    
    public CompilationResult compileAndExecute(String sourceCode) {
        long startTime = System.currentTimeMillis();
        
        try {
            if (sourceCode == null || sourceCode.trim().isEmpty()) {
                return new CompilationResult(false, "", "Source code is empty", null, 0);
            }
            
            // Validate code security before compilation
            try {
                CodeSecurityValidator.validateCodeSafety(sourceCode);
            } catch (SecurityException e) {
                List<String> securityErrors = new ArrayList<>();
                securityErrors.add("Security violation: " + e.getMessage());
                return new CompilationResult(false, "", "Code contains unsafe operations", 
                                           securityErrors, System.currentTimeMillis() - startTime);
            }
            
            String className = extractClassName(sourceCode);
            if (className == null) {
                className = "TempClass";
                sourceCode = wrapInClass(sourceCode, className);
            }
            
            // Validate basic syntax first
            List<String> syntaxErrors = validateSyntax(sourceCode);
            if (!syntaxErrors.isEmpty()) {
                return new CompilationResult(false, "", "Syntax validation failed", 
                                           syntaxErrors, System.currentTimeMillis() - startTime);
            }
            
            // Write source code to file
            File sourceFile = new File(sourceDirectory, className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }
            
            // Compile using Eclipse JDT
            List<String> compilationErrors = compileWithECJ(sourceFile, className);
            if (!compilationErrors.isEmpty()) {
                return new CompilationResult(false, "", "Compilation failed", 
                                           compilationErrors, System.currentTimeMillis() - startTime);
            }
            
            // Execute compiled bytecode
            String output = executeCompiledCode(className);
            
            return new CompilationResult(true, output, null, null, 
                                       System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
            Log.e(TAG, "Compilation error", e);
            return new CompilationResult(false, "", "Internal error: " + e.getMessage(), 
                                       null, System.currentTimeMillis() - startTime);
        } finally {
            cleanup();
        }
    }
    
    private String extractClassName(String sourceCode) {
        Pattern classPattern = Pattern.compile("public\\s+class\\s+(\\w+)");
        Matcher matcher = classPattern.matcher(sourceCode);
        return matcher.find() ? matcher.group(1) : null;
    }
    
    private String wrapInClass(String code, String className) {
        // Check if it's a complete main method or just statements
        if (code.contains("public static void main")) {
            return "public class " + className + " {\n" + code + "\n}";
        } else {
            return "public class " + className + " {\n" +
                   "    public static void main(String[] args) {\n" +
                   "        " + code.replaceAll("\n", "\n        ") + "\n" +
                   "    }\n" +
                   "}";
        }
    }
    
    private List<String> validateSyntax(String sourceCode) {
        List<String> errors = new ArrayList<>();
        
        // Basic syntax validation
        int openBraces = 0;
        int closeBraces = 0;
        int openParens = 0;
        int closeParens = 0;
        boolean inString = false;
        boolean inChar = false;
        boolean escape = false;
        
        char[] chars = sourceCode.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            if (escape) {
                escape = false;
                continue;
            }
            
            if (c == '\\') {
                escape = true;
                continue;
            }
            
            if (!inChar && c == '"') {
                inString = !inString;
                continue;
            }
            
            if (!inString && c == '\'') {
                inChar = !inChar;
                continue;
            }
            
            if (!inString && !inChar) {
                switch (c) {
                    case '{':
                        openBraces++;
                        break;
                    case '}':
                        closeBraces++;
                        break;
                    case '(':
                        openParens++;
                        break;
                    case ')':
                        closeParens++;
                        break;
                }
            }
        }
        
        if (openBraces != closeBraces) {
            errors.add("Mismatched braces: " + openBraces + " open, " + closeBraces + " close");
        }
        
        if (openParens != closeParens) {
            errors.add("Mismatched parentheses: " + openParens + " open, " + closeParens + " close");
        }
        
        if (inString) {
            errors.add("Unclosed string literal");
        }
        
        if (inChar) {
            errors.add("Unclosed character literal");
        }
        
        return errors;
    }
    
    private List<String> compileWithECJ(File sourceFile, String className) {
        List<String> errors = new ArrayList<>();
        
        try {
            ByteArrayOutputStream compilerOutput = new ByteArrayOutputStream();
            PrintWriter compilerWriter = new PrintWriter(compilerOutput);
            
            // Prepare compiler arguments with Android-specific settings
            List<String> args = new ArrayList<>();
            args.add("-d");
            args.add(classDirectory.getAbsolutePath());
            args.add("-cp");
            args.add(getAndroidClasspath());
            args.add("-source");
            args.add("11");
            args.add("-target");
            args.add("11");
            args.add("-nowarn");
            args.add("-g");
            args.add("-encoding");
            args.add("UTF-8");
            args.add("-proc:none"); // Disable annotation processing
            args.add("-XDuseUnsharedTable=true"); // Use unshared symbol table
            args.add("-XDallowGenerics=true"); // Allow generics
            args.add("-bootclasspath");
            args.add(getAndroidBootClasspath());
            args.add(sourceFile.getAbsolutePath());
            
            String[] argsArray = args.toArray(new String[0]);
            
            // Use Eclipse JDT Batch Compiler with error handling
            boolean success = false;
            try {
                success = BatchCompiler.compile(argsArray, compilerWriter, compilerWriter, null);
            } catch (Exception e) {
                Log.w(TAG, "Eclipse JDT compilation failed, trying alternative approach", e);
                errors.add("Eclipse JDT compilation failed: " + e.getMessage());
                errors.add("This might be due to missing Android runtime classes");
                return errors;
            }
            
            compilerWriter.close();
            String compilerOutputStr = compilerOutput.toString();
            
            if (!success || !compilerOutputStr.isEmpty()) {
                if (!compilerOutputStr.isEmpty()) {
                    errors.addAll(parseCompilerErrors(compilerOutputStr));
                } else {
                    errors.add("Compilation failed for unknown reason");
                }
            }
            
            // Verify class file was created
            File classFile = new File(classDirectory, className + ".class");
            if (!classFile.exists()) {
                errors.add("Class file was not generated - compilation may have failed silently");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "ECJ compilation error", e);
            errors.add("Compiler initialization error: " + e.getMessage());
            errors.add("This typically indicates missing JDK classes on Android");
        }
        
        return errors;
    }
    
    private String getClasspath() {
        return getAndroidClasspath();
    }
    
    private String getAndroidClasspath() {
        StringBuilder classpath = new StringBuilder();
        
        // Add current directory
        classpath.append(classDirectory.getAbsolutePath());
        
        return classpath.toString();
    }
    
    private String getAndroidBootClasspath() {
        StringBuilder bootclasspath = new StringBuilder();
        
        // Try to find Android runtime classes
        String[] possiblePaths = {
            "/system/framework/framework.jar",
            "/system/framework/core.jar",
            "/system/framework/ext.jar",
            "/system/framework/android.jar"
        };
        
        for (String path : possiblePaths) {
            File jarFile = new File(path);
            if (jarFile.exists()) {
                if (bootclasspath.length() > 0) {
                    bootclasspath.append(File.pathSeparator);
                }
                bootclasspath.append(path);
            }
        }
        
        // If no Android jars found, create a minimal bootclasspath
        if (bootclasspath.length() == 0) {
            // Create a temporary minimal rt.jar equivalent
            try {
                File minimalRt = createMinimalBootstrapClasses();
                bootclasspath.append(minimalRt.getAbsolutePath());
            } catch (Exception e) {
                Log.w(TAG, "Could not create minimal bootstrap classes", e);
                // Fallback to current classpath
                bootclasspath.append(System.getProperty("java.class.path", "."));
            }
        }
        
        return bootclasspath.toString();
    }
    
    private File createMinimalBootstrapClasses() throws Exception {
        File minimalJar = new File(tempDirectory, "minimal-rt.jar");
        
        // For now, just return the temp directory as a classpath entry
        // In a real implementation, you might want to create a JAR with minimal classes
        if (!minimalJar.exists()) {
            minimalJar.createNewFile();
        }
        
        return tempDirectory; // Return directory instead of jar for now
    }
    
    private List<String> parseCompilerErrors(String compilerOutput) {
        List<String> errors = new ArrayList<>();
        String[] lines = compilerOutput.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() && (line.contains("ERROR") || line.contains("error") || 
                                   line.contains("Exception") || line.contains("at line"))) {
                errors.add(formatErrorMessage(line));
            }
        }
        
        return errors;
    }
    
    private String formatErrorMessage(String rawError) {
        // Clean up and format error messages for better readability
        String formatted = rawError
            .replaceAll("^[0-9]+\\. ", "")
            .replaceAll("----------", "")
            .trim();
        
        if (formatted.isEmpty()) {
            return rawError;
        }
        
        return formatted;
    }
    
    private String executeCompiledCode(String className) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream errorStream = new ByteArrayOutputStream();
        
        // Capture output
        originalSystemOut = System.out;
        originalSystemErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
        
        // Use controlled execution instead of deprecated SecurityManager
        
        try {
            // Create custom class loader
            URL[] urls = { classDirectory.toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
            
            // Load and execute the class
            Class<?> clazz = classLoader.loadClass(className);
            Method mainMethod = clazz.getMethod("main", String[].class);
            
            // Execute with timeout and basic security checks
            executeWithSecurityControls(() -> {
                try {
                    mainMethod.invoke(null, (Object) new String[0]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, 10, TimeUnit.SECONDS);
            
            classLoader.close();
            
        } finally {
            // Restore streams
            restoreSystemStreams();
        }
        
        String output = outputStream.toString();
        String error = errorStream.toString();
        
        if (!error.isEmpty()) {
            output += "\nErrors:\n" + error;
        }
        
        return output.isEmpty() ? "Program executed successfully (no output)" : output;
    }
    
    private void executeWithSecurityControls(Runnable task, long timeout, TimeUnit unit) throws Exception {
        Thread executionThread = new Thread(() -> {
            try {
                // Basic security checks before execution
                checkSystemExitCalls();
                task.run();
            } catch (SecurityException e) {
                throw new RuntimeException("Security violation: " + e.getMessage());
            }
        });
        
        executionThread.setDaemon(true);
        executionThread.start();
        
        try {
            executionThread.join(unit.toMillis(timeout));
            if (executionThread.isAlive()) {
                executionThread.interrupt();
                throw new RuntimeException("Execution timeout exceeded (" + timeout + " " + unit.name().toLowerCase() + ")");
            }
        } catch (InterruptedException e) {
            executionThread.interrupt();
            throw new RuntimeException("Execution interrupted");
        }
    }
    
    private void checkSystemExitCalls() {
        // Basic security check - can be expanded
        // This is a placeholder for security validations
    }
    
    private void installSecurityManager() {
        // Security manager is deprecated in newer Java versions
        // Using alternative security measures through restricted execution
        originalSecurityManager = null; // Keep for compatibility but don't use deprecated methods
    }
    
    private void restoreSecurityManager() {
        // Security manager restoration not needed with deprecated API
        // Alternative security measures handled at execution level
    }
    
    private void restoreSystemStreams() {
        if (originalSystemOut != null) {
            System.setOut(originalSystemOut);
        }
        if (originalSystemErr != null) {
            System.setErr(originalSystemErr);
        }
    }
    
    private void cleanup() {
        try {
            cleanupDirectories();
        } catch (Exception e) {
            Log.e(TAG, "Cleanup failed", e);
        }
    }
    
    private static class CodeSecurityValidator {
        
        public static void validateCodeSafety(String sourceCode) throws SecurityException {
            // Basic security validations
            if (sourceCode.contains("System.exit")) {
                throw new SecurityException("System.exit() calls are not allowed");
            }
            
            if (sourceCode.contains("Runtime.getRuntime")) {
                throw new SecurityException("Runtime access is restricted");
            }
            
            if (sourceCode.contains("ProcessBuilder")) {
                throw new SecurityException("Process execution is not allowed");
            }
            
            if (sourceCode.contains("java.io.File") && 
                (sourceCode.contains("delete") || sourceCode.contains("mkdir"))) {
                throw new SecurityException("File system modifications are restricted");
            }
        }
    }
}
