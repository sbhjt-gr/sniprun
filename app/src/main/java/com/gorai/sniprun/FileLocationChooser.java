package com.gorai.sniprun;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileLocationChooser {
    
    public interface OnLocationSelectedListener {
        void onLocationSelected(String path, String displayName);
    }
    
    /**
     * Show dialog to choose file save location
     */
    public static void showLocationChooser(Context context, OnLocationSelectedListener listener) {
        List<LocationOption> locations = getAvailableLocations(context);
        
        String[] displayNames = new String[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            displayNames[i] = locations.get(i).displayName;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Save Location")
               .setIcon(android.R.drawable.ic_menu_save)
               .setSingleChoiceItems(displayNames, 0, null)
               .setPositiveButton("Select", (dialog, which) -> {
                   int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                   if (selectedIndex >= 0 && selectedIndex < locations.size()) {
                       LocationOption selected = locations.get(selectedIndex);
                       listener.onLocationSelected(selected.path, selected.displayName);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    /**
     * Get list of available storage locations
     */
    private static List<LocationOption> getAvailableLocations(Context context) {
        List<LocationOption> locations = new ArrayList<>();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File externalDir = new File(Environment.getExternalStorageDirectory(), "SnipRun");
            locations.add(new LocationOption(
                externalDir.getAbsolutePath(),
                "📱 Device Storage (/storage/emulated/0/SnipRun/)",
                "Accessible from file managers, persists after uninstall"
            ));
        }

        File documentsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "SnipRun");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            locations.add(new LocationOption(
                documentsDir.getAbsolutePath(),
                "Documents Folder",
                "Easy to find in Documents, good for sharing"
            ));
        }

        File downloadsDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SnipRun");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            locations.add(new LocationOption(
                downloadsDir.getAbsolutePath(),
                "Downloads Folder",
                "Quick access from Downloads"
            ));
        }

        File appExternalDir = new File(context.getExternalFilesDir(null), "SnipRunProjects");
        locations.add(new LocationOption(
            appExternalDir.getAbsolutePath(),
            "App External Storage",
            "App-specific, removed when app is uninstalled"
        ));

        File internalDir = new File(context.getFilesDir(), "SnipRunProjects");
        locations.add(new LocationOption(
            internalDir.getAbsolutePath(),
            "App Internal Storage",
            "Private to app, not accessible externally"
        ));
        
        return locations;
    }
    
    /**
     * Show detailed location chooser with descriptions
     */
    public static void showDetailedLocationChooser(Context context, OnLocationSelectedListener listener) {
        List<LocationOption> locations = getAvailableLocations(context);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Choose Save Location");

        ArrayAdapter<LocationOption> adapter = new ArrayAdapter<LocationOption>(context, 
            android.R.layout.select_dialog_singlechoice, locations) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                LocationOption option = locations.get(position);
                TextView textView = (TextView) view;
                textView.setText(option.displayName + "\n" + option.description);
                return view;
            }
        };
        
        builder.setSingleChoiceItems(adapter, 0, null)
               .setPositiveButton("Select", (dialog, which) -> {
                   int selectedIndex = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                   if (selectedIndex >= 0 && selectedIndex < locations.size()) {
                       LocationOption selected = locations.get(selectedIndex);
                       listener.onLocationSelected(selected.path, selected.displayName);
                   }
               })
               .setNegativeButton("Cancel", null)
               .show();
    }
    
    private static class LocationOption {
        final String path;
        final String displayName;
        final String description;
        
        LocationOption(String path, String displayName, String description) {
            this.path = path;
            this.displayName = displayName;
            this.description = description;
        }
        
        @Override
        public String toString() {
            return displayName;
        }
    }
}