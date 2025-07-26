package com.gorai.sniprun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class FileExplorerFragment extends Fragment implements FileExplorerAdapter.OnFileClickListener {
    
    private RecyclerView recyclerView;
    private FileExplorerAdapter adapter;
    private FileManager fileManager;
    private FloatingActionButton fabNewFile;
    private OnFileSelectedListener listener;
    
    public interface OnFileSelectedListener {
        void onFileSelected(String filePath, String fileName);
    }
    
    public static FileExplorerFragment newInstance() {
        return new FileExplorerFragment();
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFileSelectedListener) {
            listener = (OnFileSelectedListener) context;
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, 
                            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_explorer, container, false);
        
        initializeViews(view);
        setupFileManager();
        setupRecyclerView();
        loadFiles();
        
        return view;
    }
    
    private void initializeViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view_files);
        fabNewFile = view.findViewById(R.id.fab_new_file);
        
        fabNewFile.setOnClickListener(v -> showNewFileDialog());
    }
    
    private void setupFileManager() {
        fileManager = new FileManager(requireContext());
    }
    
    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    }
    
    private void loadFiles() {
        List<FileManager.FileNode> files = fileManager.listFiles(null);
        
        if (adapter == null) {
            adapter = new FileExplorerAdapter(getContext(), files, fileManager, this);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.updateFiles(files);
        }
    }
    
    @Override
    public void onFileClick(FileManager.FileNode file) {
        if (listener != null) {
            listener.onFileSelected(file.getPath(), file.getName());
        }
    }
    
    @Override
    public void onDirectoryClick(FileManager.FileNode directory) {
        fileManager.setCurrentDirectory(directory.getPath());
        loadFiles();
    }
    
    @Override
    public void onFileOptionsClick(FileManager.FileNode file) {
        
    }
    
    private void showNewFileDialog() {
        String[] options = {"New Java File", "New Text File", "New Folder"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New")
               .setItems(options, (dialog, which) -> {
                   switch (which) {
                       case 0:
                           showCreateFileDialog(".java");
                           break;
                       case 1:
                           showCreateFileDialog(".txt");
                           break;
                       case 2:
                           showCreateFolderDialog();
                           break;
                   }
               })
               .show();
    }
    
    private void showCreateFileDialog(String extension) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New File");
        
        EditText editText = new EditText(getContext());
        editText.setHint("Enter file name");
        builder.setView(editText);
        
        builder.setPositiveButton("Create", (dialog, which) -> {
            String fileName = editText.getText().toString().trim();
            if (!fileName.isEmpty()) {
                if (!fileName.endsWith(extension)) {
                    fileName += extension;
                }
                
                if (fileManager.createFile(fileName, fileManager.getCurrentDirectory())) {
                    Toast.makeText(getContext(), "File created successfully", Toast.LENGTH_SHORT).show();
                    loadFiles();
                } else {
                    Toast.makeText(getContext(), "Failed to create file", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showCreateFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create New Folder");
        
        EditText editText = new EditText(getContext());
        editText.setHint("Enter folder name");
        builder.setView(editText);
        
        builder.setPositiveButton("Create", (dialog, which) -> {
            String folderName = editText.getText().toString().trim();
            if (!folderName.isEmpty()) {
                if (fileManager.createDirectory(folderName, fileManager.getCurrentDirectory())) {
                    Toast.makeText(getContext(), "Folder created successfully", Toast.LENGTH_SHORT).show();
                    loadFiles();
                } else {
                    Toast.makeText(getContext(), "Failed to create folder", Toast.LENGTH_SHORT).show();
                }
            }
        });
        
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    public void navigateUp() {
        String currentDir = fileManager.getCurrentDirectory();
        String projectRoot = fileManager.getProjectRoot();
        
        if (!currentDir.equals(projectRoot)) {
            String parentDir = new java.io.File(currentDir).getParent();
            if (parentDir != null) {
                fileManager.setCurrentDirectory(parentDir);
                loadFiles();
            }
        }
    }
    
    public void refreshFiles() {
        loadFiles();
    }
    
    public String getCurrentPath() {
        return fileManager.getCurrentDirectory();
    }
}
