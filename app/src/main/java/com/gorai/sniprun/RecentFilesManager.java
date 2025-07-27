package com.gorai.sniprun;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class RecentFilesManager {
    
    private static final String PREFS_NAME = "RecentFiles";
    private static final String KEY_RECENT_FILES = "recent_files";
    private static final int MAX_RECENT_FILES = 10;
    
    private final SharedPreferences prefs;
    private final LinkedList<String> recentFiles;
    
    public RecentFilesManager(Context context) {
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        this.recentFiles = new LinkedList<>();
        loadRecentFiles();
    }
    
    private void loadRecentFiles() {
        Set<String> fileSet = prefs.getStringSet(KEY_RECENT_FILES, new HashSet<>());
        recentFiles.addAll(fileSet);
    }
    
    private void saveRecentFiles() {
        Set<String> fileSet = new HashSet<>(recentFiles);
        prefs.edit().putStringSet(KEY_RECENT_FILES, fileSet).apply();
    }
    
    public void addRecentFile(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            return;
        }
        
        recentFiles.remove(filePath);
        recentFiles.addFirst(filePath);
        
        while (recentFiles.size() > MAX_RECENT_FILES) {
            recentFiles.removeLast();
        }
        
        saveRecentFiles();
    }
    
    public void removeRecentFile(String filePath) {
        if (recentFiles.remove(filePath)) {
            saveRecentFiles();
        }
    }
    
    public List<String> getRecentFiles() {
        return new ArrayList<>(recentFiles);
    }
    
    public void clearRecentFiles() {
        recentFiles.clear();
        saveRecentFiles();
    }
    
    public boolean hasRecentFiles() {
        return !recentFiles.isEmpty();
    }
    
    public String getMostRecentFile() {
        return recentFiles.isEmpty() ? null : recentFiles.getFirst();
    }
    
    public List<RecentFileInfo> getRecentFileInfos() {
        List<RecentFileInfo> infos = new ArrayList<>();
        
        for (String filePath : recentFiles) {
            String fileName = getFileNameFromPath(filePath);
            String directory = getDirectoryFromPath(filePath);
            infos.add(new RecentFileInfo(fileName, filePath, directory));
        }
        
        return infos;
    }
    
    private String getFileNameFromPath(String filePath) {
        if (filePath == null) return "";
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }
    
    private String getDirectoryFromPath(String filePath) {
        if (filePath == null) return "";
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(0, lastSlash) : "";
    }
    
    public static class RecentFileInfo {
        private final String fileName;
        private final String filePath;
        private final String directory;
        
        public RecentFileInfo(String fileName, String filePath, String directory) {
            this.fileName = fileName;
            this.filePath = filePath;
            this.directory = directory;
        }
        
        public String getFileName() {
            return fileName;
        }
        
        public String getFilePath() {
            return filePath;
        }
        
        public String getDirectory() {
            return directory;
        }
        
        public String getDisplayName() {
            return fileName + " - " + directory;
        }
    }
}
