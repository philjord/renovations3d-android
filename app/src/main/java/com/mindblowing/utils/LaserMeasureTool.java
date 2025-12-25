package com.mindblowing.utils;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.companion.AssociationInfo;
import android.companion.AssociationRequest;
import android.companion.BluetoothDeviceFilter;
import android.companion.CompanionDeviceManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.regex.Pattern;
/**
 *
 */
public class LaserMeasureTool {
    public static int SELECT_DEVICE_REQUEST_CODE = 8642135;

    private Activity activity;

    public LaserMeasureTool(Activity activity) {
        this.activity = activity;
        // Use this check to determine whether Bluetooth classic is supported on the device.
        // Then you can selectively disable BLE-related features.
        boolean bluetoothAvailable = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH);

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        boolean bluetoothLEAvailable = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);

        System.out.println("XXXXXXXXXXXXX bluetoothAvailable " + bluetoothAvailable);//true
        System.out.println("XXXXXXXXXXXXX bluetoothLEAvailable " + bluetoothLEAvailable);//true

        boolean FEATURE_COMPANION_DEVICE_SETUP = activity.getPackageManager().hasSystemFeature(PackageManager.FEATURE_COMPANION_DEVICE_SETUP);
        System.out.println("XXXXXXXXXXXXX FEATURE_COMPANION_DEVICE_SETUP " + FEATURE_COMPANION_DEVICE_SETUP);//true


        BluetoothDeviceFilter deviceFilter = new BluetoothDeviceFilter.Builder()
                // Match only Bluetooth devices whose name matches the pattern.
                //.setNamePattern(Pattern.compile("My device"))
                // Match only Bluetooth devices whose service UUID matches this pattern.
                //.addServiceUuid(new ParcelUuid(new UUID(0x123abcL, -1L)), null)
                .build();

        AssociationRequest pairingRequest = new AssociationRequest.Builder()
                // Find only devices that match this request filter.
                .addDeviceFilter(deviceFilter)
                // Stop scanning as soon as one device matching the filter is found.
                .setSingleDevice(true)
                .build();


        CompanionDeviceManager deviceManager =
                (CompanionDeviceManager) activity.getSystemService(Context.COMPANION_DEVICE_SERVICE);

        CompanionDeviceManager.Callback callback = new CompanionDeviceManager.Callback() {

            // Called when a device is found. Launch the IntentSender so the user can
            // select the device they want to pair with.
            @Override
            public void onDeviceFound(IntentSender chooserLauncher) {
                try {
                    activity.startIntentSenderForResult(
                            chooserLauncher, SELECT_DEVICE_REQUEST_CODE, null, 0, 0, 0
                    );
                } catch (IntentSender.SendIntentException e) {
                    Log.e("MainActivity", "Failed to send intent");
                }
            }

            @Override
            public void onAssociationCreated(AssociationInfo associationInfo) {
                // An association is created.
            }

            @Override
            public void onFailure(CharSequence errorMessage) {
                // To handle the failure.
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Executor executor = new Executor() {
                @Override
                public void execute(Runnable runnable) {
                    runnable.run();
                }
            };
            deviceManager.associate(pairingRequest, executor, callback);
        } else {
            deviceManager.associate(pairingRequest, callback, null);
        }

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == LaserMeasureTool.SELECT_DEVICE_REQUEST_CODE && data != null) {
            // this seems simpler?
            //https://stackoverflow.com/questions/35447252/android-ble-how-is-onscanresult-method-being-called-in-scancallback
            BluetoothDevice deviceToPair =
                    data.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE);
            if (deviceToPair != null) {
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                deviceToPair.createBond();
                // Continue to interact with the paired device.
            }
        }
    }
}
