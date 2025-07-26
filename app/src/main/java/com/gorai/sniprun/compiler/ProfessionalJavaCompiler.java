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
        }
    }
    
    private void cleanupDirectories() {
        try {
            deleteDirectoryContents(classDirectory);
            deleteDirectoryContents(tempDirectory);
        } catch (Exception e) {
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
            
            List<String> syntaxErrors = validateSyntax(sourceCode);
            if (!syntaxErrors.isEmpty()) {
                return new CompilationResult(false, "", "Syntax validation failed", 
                                           syntaxErrors, System.currentTimeMillis() - startTime);
            }
            
            File sourceFile = new File(sourceDirectory, className + ".java");
            try (FileWriter writer = new FileWriter(sourceFile)) {
                writer.write(sourceCode);
            }
            
            List<String> compilationErrors = compileWithECJ(sourceFile, className);
            if (!compilationErrors.isEmpty()) {
                return new CompilationResult(false, "", "Compilation failed", 
                                           compilationErrors, System.currentTimeMillis() - startTime);
            }
            
            String output = executeCompiledCode(className);
            
            return new CompilationResult(true, output, null, null, 
                                       System.currentTimeMillis() - startTime);
            
        } catch (Exception e) {
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
            
            List<String> args = new ArrayList<>();
            args.add("-d");
            args.add(classDirectory.getAbsolutePath());
            args.add("-cp");  
            args.add(getAndroidClasspath());
            args.add("-source");
            args.add("8");
            args.add("-target");
            args.add("8");
            args.add("-encoding");
            args.add("UTF-8");
            args.add("-proc:none");
            
            String bootcp = getAndroidBootClasspath();
            if (!bootcp.isEmpty()) {
                args.add("-bootclasspath");
                args.add(bootcp);
            }
            
            args.add(sourceFile.getAbsolutePath());
            
            String[] argsArray = args.toArray(new String[0]);
            
            boolean success = false;
            try {
                Log.d(TAG, "Compiling with args: " + Arrays.toString(argsArray));
                success = BatchCompiler.compile(argsArray, compilerWriter, compilerWriter, null);
                Log.d(TAG, "Compilation success: " + success);
            } catch (Exception e) {
                Log.w(TAG, "Eclipse JDT compilation failed", e);
                errors.add("Eclipse JDT compilation failed: " + e.getMessage());
                errors.add("Trying to use system classpath: " + System.getProperty("java.class.path", "not available"));
                return errors;
            }
            
            compilerWriter.close();
            String compilerOutputStr = compilerOutput.toString();
            
            Log.d(TAG, "Compiler output: " + compilerOutputStr);
            
            if (!success || !compilerOutputStr.isEmpty()) {
                if (!compilerOutputStr.isEmpty()) {
                    errors.addAll(parseCompilerErrors(compilerOutputStr));
                } else {
                    errors.add("Compilation failed for unknown reason");
                }
            }
            
            File classFile = new File(classDirectory, className + ".class");
            Log.d(TAG, "Looking for class file: " + classFile.getAbsolutePath());
            Log.d(TAG, "Class file exists: " + classFile.exists());
            
            if (!classFile.exists()) {
                errors.add("Class file was not generated - compilation may have failed silently");
                
                File[] files = classDirectory.listFiles();
                if (files != null) {
                    Log.d(TAG, "Files in class directory: " + Arrays.toString(files));
                } else {
                    Log.d(TAG, "Class directory is empty or doesn't exist");
                }
            }
            
        } catch (Exception e) {
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
        
        classpath.append(classDirectory.getAbsolutePath());
        
        return classpath.toString();
    }
    
    private String getAndroidBootClasspath() {
        StringBuilder bootclasspath = new StringBuilder();
        
        try {
            String javaHome = System.getProperty("java.home");
            if (javaHome != null) {
                File rtJar = new File(javaHome, "lib/rt.jar");
                if (rtJar.exists()) {
                    bootclasspath.append(rtJar.getAbsolutePath());
                } else {
                    File jmodDir = new File(javaHome, "jmods");
                    if (jmodDir.exists()) {
                        File[] jmods = jmodDir.listFiles((dir, name) -> name.endsWith(".jmod"));
                        if (jmods != null) {
                            for (File jmod : jmods) {
                                if (bootclasspath.length() > 0) {
                                    bootclasspath.append(File.pathSeparator);
                                }
                                bootclasspath.append(jmod.getAbsolutePath());
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not determine Java home path", e);
        }
        
        if (bootclasspath.length() == 0) {
            String classPath = System.getProperty("java.class.path", "");
            if (!classPath.isEmpty()) {
                bootclasspath.append(classPath);
            } else {
                bootclasspath.append(".");
            }
        }
        
        return bootclasspath.toString();
    }
    

    
    private List<String> parseCompilerErrors(String compilerOutput) {
        List<String> errors = new ArrayList<>();
        String[] lines = compilerOutput.split("\n");
        
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                if (line.contains("ERROR") || line.contains("error") || 
                    line.contains("Exception") || line.contains("at line") ||
                    line.contains("cannot find symbol") || line.contains("package does not exist") ||
                    line.matches(".*\\.java:[0-9]+:.*")) {
                    errors.add(formatErrorMessage(line));
                } else if (line.contains("warning") || line.contains("WARNING")) {
                    Log.w(TAG, "Compiler warning: " + line);
                } else if (!line.isEmpty() && !line.startsWith("Note:")) {
                    Log.d(TAG, "Compiler output: " + line);
                }
            }
        }
        
        return errors;
    }
    
    private String formatErrorMessage(String rawError) {
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
        
        originalSystemOut = System.out;
        originalSystemErr = System.err;
        System.setOut(new PrintStream(outputStream));
        System.setErr(new PrintStream(errorStream));
        
        
        try {
            URL[] urls = { classDirectory.toURI().toURL() };
            URLClassLoader classLoader = new URLClassLoader(urls, getClass().getClassLoader());
            
            Class<?> clazz = classLoader.loadClass(className);
            Method mainMethod = clazz.getMethod("main", String[].class);
            
            executeWithSecurityControls(() -> {
                try {
                    mainMethod.invoke(null, (Object) new String[0]);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }, 10, TimeUnit.SECONDS);
            
            classLoader.close();
            
        } finally {
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
    }
    
    private void installSecurityManager() {
        originalSecurityManager = null;
    }
    
    private void restoreSecurityManager() {
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
        }
    }
    
    private static class CodeSecurityValidator {
        
        public static void validateCodeSafety(String sourceCode) throws SecurityException {
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
