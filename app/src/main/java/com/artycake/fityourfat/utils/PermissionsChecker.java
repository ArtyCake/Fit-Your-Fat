package com.artycake.fityourfat.utils;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.artycake.fityourfat.R;

import java.util.HashMap;

/**
 * Created by artycake on 3/14/17.
 */

public class PermissionsChecker {
    private static PermissionsChecker instance;
    private static final int PHONE_STATE_PERMISSION = 101;
    private Activity activity;
    private HashMap<Integer, OnPermissionGranted> callbacks = new HashMap<>();

    public static PermissionsChecker getInstance(Activity activity) {
        if (instance == null) {
            instance = new PermissionsChecker(activity);
        }
        return instance;
    }

    private PermissionsChecker(Activity activity) {
        this.activity = activity;
    }

    public boolean checkPhoneStatePermission(OnPermissionGranted onPermissionGranted) {
        callbacks.put(PHONE_STATE_PERMISSION, onPermissionGranted);
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
                    Manifest.permission.READ_PHONE_STATE)) {
                showPhoneAlert();
            } else {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PHONE_STATE_PERMISSION);
            }
            return false;
        }
        return true;
    }

    private void showPhoneAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, R.style.AppTheme_Dialog);
        builder.setMessage(R.string.permission_phone_state);
        builder.setPositiveButton(R.string.permissions_grant, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        PHONE_STATE_PERMISSION);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.permissions_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    public void checkResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PHONE_STATE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    OnPermissionGranted callback = callbacks.get(PHONE_STATE_PERMISSION);
                    if (callback != null) {
                        callback.onGranted();
                    }
                } else {
                    showPhoneAlert();
                }
                break;
            }
        }
    }

    public interface OnPermissionGranted {
        void onGranted();
    }
}
