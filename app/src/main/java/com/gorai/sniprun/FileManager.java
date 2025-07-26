package com.gorai.sniprun;

import android.content.Context;
import android.os.Environment;
import java.io.*;
import java.util.*;

public class FileManager {
    
    private final Context context;
    private File currentDirectory;
    private File projectRoot;
    
    public static class FileNode {
        private final String name;
        private final String path;
        private final boolean isDirectory;
        private final long size;
        private final long lastModified;
        
        public FileNode(String name, String path, boolean isDirectory, long size, long lastModified) {
            this.name = name;
            this.path = path;
            this.isDirectory = isDirectory;
            this.size = size;
            this.lastModified = lastModified;
        }
        
        public String getName() { return name; }
        public String getPath() { return path; }
        public boolean isDirectory() { return isDirectory; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
        
        public String getExtension() {
            if (isDirectory) return "";
            int lastDot = name.lastIndexOf('.');
            return lastDot > 0 ? name.substring(lastDot + 1) : "";
        }
    }
    
    public FileManager(Context context) {
        this.context = context;
        initializeFileSystem();
    }
    
    private void initializeFileSystem() {
        File appDirectory = new File(context.getFilesDir(), "SnipRunProjects");
        if (!appDirectory.exists()) {
            appDirectory.mkdirs();
        }
        this.projectRoot = appDirectory;
        this.currentDirectory = appDirectory;
        
        createDefaultProject();
    }
    
    private void createDefaultProject() {
        File mainProject = new File(projectRoot, "Main");
        if (!mainProject.exists()) {
            mainProject.mkdirs();
            
            File srcDir = new File(mainProject, "src");
            srcDir.mkdirs();
            
            File mainJava = new File(srcDir, "Main.java");
            try {
                FileWriter writer = new FileWriter(mainJava);
                writer.write("public class Main {\n" +
                           "    public static void main(String[] args) {\n" +
                           "        System.out.println(\"Welcome to SnipRun IDE!\");\n" +
                           "    }\n" +
                           "}");
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    public List<FileNode> listFiles(String directoryPath) {
        File directory = directoryPath != null ? new File(directoryPath) : currentDirectory;
        List<FileNode> fileNodes = new ArrayList<>();
        
        if (directory.exists() && directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                Arrays.sort(files, (f1, f2) -> {
                    if (f1.isDirectory() && !f2.isDirectory()) return -1;
                    if (!f1.isDirectory() && f2.isDirectory()) return 1;
                    return f1.getName().compareToIgnoreCase(f2.getName());
                });
                
                for (File file : files) {
                    fileNodes.add(new FileNode(
                        file.getName(),
                        file.getAbsolutePath(),
                        file.isDirectory(),
                        file.length(),
                        file.lastModified()
                    ));
                }
            }
        }
        
        return fileNodes;
    }
    
    public String readFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            throw new IOException("File not found or is a directory");
        }
        
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        
        return content.toString();
    }
    
    public void saveFile(String filePath, String content) throws IOException {
        File file = new File(filePath);
        File parentDir = file.getParentFile();
        
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
    }
    
    public boolean createFile(String fileName, String directoryPath) {
        try {
            File directory = directoryPath != null ? new File(directoryPath) : currentDirectory;
            File newFile = new File(directory, fileName);
            
            if (!newFile.exists()) {
                newFile.createNewFile();
                
                if (fileName.endsWith(".java")) {
                    String className = fileName.substring(0, fileName.lastIndexOf('.'));
                    String template = "public class " + className + " {\n" +
                                    "    public static void main(String[] args) {\n" +
                                    "        \n" +
                                    "    }\n" +
                                    "}";
                    saveFile(newFile.getAbsolutePath(), template);
                }
                
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public boolean createDirectory(String dirName, String parentPath) {
        File parentDir = parentPath != null ? new File(parentPath) : currentDirectory;
        File newDir = new File(parentDir, dirName);
        return newDir.mkdirs();
    }
    
    public boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isDirectory()) {
            return deleteDirectory(file);
        } else {
            return file.delete();
        }
    }
    
    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }
    
    public boolean renameFile(String oldPath, String newName) {
        File oldFile = new File(oldPath);
        File newFile = new File(oldFile.getParent(), newName);
        return oldFile.renameTo(newFile);
    }
    
    public List<FileNode> searchFiles(String query, String directory) {
        List<FileNode> results = new ArrayList<>();
        File searchDir = directory != null ? new File(directory) : projectRoot;
        
        searchFilesRecursive(searchDir, query.toLowerCase(), results);
        return results;
    }
    
    private void searchFilesRecursive(File directory, String query, List<FileNode> results) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().toLowerCase().contains(query)) {
                    results.add(new FileNode(
                        file.getName(),
                        file.getAbsolutePath(),
                        file.isDirectory(),
                        file.length(),
                        file.lastModified()
                    ));
                }
                
                if (file.isDirectory()) {
                    searchFilesRecursive(file, query, results);
                }
            }
        }
    }
    
    public String getCurrentDirectory() {
        return currentDirectory.getAbsolutePath();
    }
    
    public void setCurrentDirectory(String path) {
        File newDir = new File(path);
        if (newDir.exists() && newDir.isDirectory()) {
            this.currentDirectory = newDir;
        }
    }
    
    public String getProjectRoot() {
        return projectRoot.getAbsolutePath();
    }
    
    public List<String> getRecentFiles() {
        return new ArrayList<>();
    }
    
    public void addToRecentFiles(String filePath) {
        
    }
}
