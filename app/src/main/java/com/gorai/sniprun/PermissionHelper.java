package com.gorai.sniprun;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
    
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 100;
    public static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    
    private static final String[] STORAGE_PERMISSIONS = {
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static boolean hasStoragePermissions(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (String permission : STORAGE_PERMISSIONS) {
                if (ContextCompat.checkSelfPermission(context, permission) 
                    != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void requestStoragePermissions(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            showPermissionExplanationDialog(activity, () -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                activity.startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            });
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            showPermissionExplanationDialog(activity, () -> {
                ActivityCompat.requestPermissions(
                    activity,
                    STORAGE_PERMISSIONS,
                    STORAGE_PERMISSION_REQUEST_CODE
                );
            });
        }
    }

    private static void showPermissionExplanationDialog(Activity activity, Runnable onAccept) {
        new AlertDialog.Builder(activity)
            .setTitle("Storage Permission Required")
            .setMessage("SnipRun IDE needs storage permission to save your Java files to external storage.\n\n" +
                       "This allows you to:\n" +
                       "• Access files from other apps\n" +
                       "• Keep files even if app is uninstalled\n" +
                       "• Share projects easily\n\n" +
                       "Your files will be saved to: /storage/emulated/0/SnipRun/")
            .setPositiveButton("Grant Permission", (dialog, which) -> onAccept.run())
            .setNegativeButton("Cancel", (dialog, which) -> {
                new AlertDialog.Builder(activity)
                    .setTitle("Limited Functionality")
                    .setMessage("Without storage permission, files will be saved to app-specific storage and won't be accessible from other apps.")
                    .setPositiveButton("OK", null)
                    .show();
            })
            .setCancelable(false)
            .show();
    }

    public static boolean isPermissionGranted(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return grantResults.length > 0;
    }

    public static boolean handleActivityResult(int requestCode) {
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && Environment.isExternalStorageManager();
        }
        return false;
    }
}