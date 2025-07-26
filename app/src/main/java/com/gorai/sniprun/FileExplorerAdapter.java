package com.gorai.sniprun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FileExplorerAdapter extends RecyclerView.Adapter<FileExplorerAdapter.FileViewHolder> {
    
    private List<FileManager.FileNode> files;
    private final FileManager fileManager;
    private final OnFileClickListener listener;
    private final Context context;
    
    public interface OnFileClickListener {
        void onFileClick(FileManager.FileNode file);
        void onDirectoryClick(FileManager.FileNode directory);
        void onFileOptionsClick(FileManager.FileNode file);
    }
    
    public FileExplorerAdapter(Context context, List<FileManager.FileNode> files, 
                              FileManager fileManager, OnFileClickListener listener) {
        this.context = context;
        this.files = files;
        this.fileManager = fileManager;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_file_node, parent, false);
        return new FileViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        FileManager.FileNode file = files.get(position);
        holder.bind(file);
    }
    
    @Override
    public int getItemCount() {
        return files.size();
    }
    
    public void updateFiles(List<FileManager.FileNode> newFiles) {
        this.files = newFiles;
        notifyDataSetChanged();
    }
    
    class FileViewHolder extends RecyclerView.ViewHolder {
        private final ImageView fileIcon;
        private final TextView fileName;
        private final TextView fileDetails;
        private final ImageView optionsIcon;
        
        public FileViewHolder(@NonNull View itemView) {
            super(itemView);
            fileIcon = itemView.findViewById(R.id.file_icon);
            fileName = itemView.findViewById(R.id.file_name);
            fileDetails = itemView.findViewById(R.id.file_details);
            optionsIcon = itemView.findViewById(R.id.options_icon);
        }
        
        public void bind(FileManager.FileNode file) {
            fileName.setText(file.getName());
            
            if (file.isDirectory()) {
                fileIcon.setImageResource(R.drawable.ic_folder);
                fileDetails.setText("Folder");
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDirectoryClick(file);
                    }
                });
            } else {
                setFileIcon(file);
                
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String size = formatFileSize(file.getSize());
                String date = sdf.format(new Date(file.getLastModified()));
                fileDetails.setText(size + " • " + date);
                
                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onFileClick(file);
                    }
                });
            }
            
            optionsIcon.setOnClickListener(v -> showFileOptions(file));
        }
        
        private void setFileIcon(FileManager.FileNode file) {
            String extension = file.getExtension().toLowerCase();
            switch (extension) {
                case "java":
                    fileIcon.setImageResource(R.drawable.ic_java_file);
                    break;
                case "txt":
                    fileIcon.setImageResource(R.drawable.ic_text_file);
                    break;
                case "xml":
                    fileIcon.setImageResource(R.drawable.ic_xml_file);
                    break;
                case "json":
                    fileIcon.setImageResource(R.drawable.ic_json_file);
                    break;
                default:
                    fileIcon.setImageResource(R.drawable.ic_file);
                    break;
            }
        }
        
        private String formatFileSize(long size) {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format(Locale.getDefault(), "%.1f KB", size / 1024.0);
            return String.format(Locale.getDefault(), "%.1f MB", size / (1024.0 * 1024.0));
        }
        
        private void showFileOptions(FileManager.FileNode file) {
            String[] options = file.isDirectory() ? 
                new String[]{"Open", "Rename", "Delete"} :
                new String[]{"Open", "Rename", "Delete", "Copy Path"};
            
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(file.getName())
                   .setItems(options, (dialog, which) -> {
                       switch (which) {
                           case 0:
                               if (file.isDirectory()) {
                                   listener.onDirectoryClick(file);
                               } else {
                                   listener.onFileClick(file);
                               }
                               break;
                           case 1:
                               showRenameDialog(file);
                               break;
                           case 2:
                               showDeleteConfirmation(file);
                               break;
                           case 3:
                               if (!file.isDirectory()) {
                                   copyPathToClipboard(file);
                               }
                               break;
                       }
                   })
                   .show();
        }
        
        private void showRenameDialog(FileManager.FileNode file) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Rename " + (file.isDirectory() ? "Folder" : "File"));
            
            EditText editText = new EditText(context);
            editText.setText(file.getName());
            editText.selectAll();
            builder.setView(editText);
            
            builder.setPositiveButton("Rename", (dialog, which) -> {
                String newName = editText.getText().toString().trim();
                if (!newName.isEmpty() && !newName.equals(file.getName())) {
                    if (fileManager.renameFile(file.getPath(), newName)) {
                        Toast.makeText(context, "Renamed successfully", Toast.LENGTH_SHORT).show();
                        refreshFileList();
                    } else {
                        Toast.makeText(context, "Failed to rename", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
        
        private void showDeleteConfirmation(FileManager.FileNode file) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Delete " + (file.isDirectory() ? "Folder" : "File"));
            builder.setMessage("Are you sure you want to delete \"" + file.getName() + "\"?" +
                              (file.isDirectory() ? " This will delete all contents." : ""));
            
            builder.setPositiveButton("Delete", (dialog, which) -> {
                if (fileManager.deleteFile(file.getPath())) {
                    Toast.makeText(context, "Deleted successfully", Toast.LENGTH_SHORT).show();
                    refreshFileList();
                } else {
                    Toast.makeText(context, "Failed to delete", Toast.LENGTH_SHORT).show();
                }
            });
            
            builder.setNegativeButton("Cancel", null);
            builder.show();
        }
        
        private void copyPathToClipboard(FileManager.FileNode file) {
            android.content.ClipboardManager clipboard = 
                (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("File Path", file.getPath());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Path copied to clipboard", Toast.LENGTH_SHORT).show();
        }
        
        private void refreshFileList() {
            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    List<FileManager.FileNode> updatedFiles = 
                        fileManager.listFiles(fileManager.getCurrentDirectory());
                    updateFiles(updatedFiles);
                });
            }
        }
    }
}
