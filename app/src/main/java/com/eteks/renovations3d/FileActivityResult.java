package com.eteks.renovations3d;

import android.content.Context;
import android.content.Intent;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileActivityResult<Input, Result> {
    /**
     * Register activity result using a {@link ActivityResultContract} and an in-place activity result callback like
     * the default approach. You can still customise callback using {@link #launch(Object, OnActivityResult)}.
     */
    @NonNull
    public static <Input, Result> FileActivityResult<Input, Result> registerForActivityResult(
            @NonNull ActivityResultCaller caller,
            @NonNull ActivityResultContract<Input, Result> contract,
            @Nullable OnActivityResult<Result> onActivityResult) {
        return new FileActivityResult<>(caller, contract, onActivityResult);
    }

    /**
     * Same as {@link #registerForActivityResult(ActivityResultCaller, ActivityResultContract, OnActivityResult)} except
     * the last argument is set to {@code null}.
     */
    @NonNull
    public static <Input, Result> FileActivityResult<Input, Result> registerForActivityResult(
            @NonNull ActivityResultCaller caller,
            @NonNull ActivityResultContract<Input, Result> contract) {
        return registerForActivityResult(caller, contract, null);
    }

    /**
     * Specialised method for launching new activities.
     */
    @NonNull
    public static FileActivityResult<Intent, ActivityResult> registerActivityForResult(
            @NonNull ActivityResultCaller caller) {
        return registerForActivityResult(caller, new ActivityResultContracts.StartActivityForResult());
    }

    /**
     * Callback interface
     */
    public interface OnActivityResult<O> {
        /**
         * Called after receiving a result from the target activity
         */
        void onActivityResult(O result);
    }

    private final ActivityResultLauncher<Input> launcher;
    @Nullable
    private OnActivityResult<Result> onActivityResult;

    private FileActivityResult(@NonNull ActivityResultCaller caller,
                               @NonNull ActivityResultContract<Input, Result> contract,
                               @Nullable OnActivityResult<Result> onActivityResult) {
        this.onActivityResult = onActivityResult;
        this.launcher = caller.registerForActivityResult(contract, this::callOnActivityResult);
    }

    public void setOnActivityResult(@Nullable OnActivityResult<Result> onActivityResult) {
        this.onActivityResult = onActivityResult;
    }

    /**
     * Launch activity, same as {@link ActivityResultLauncher#launch(Object)} except that it allows a callback
     * executed after receiving a result from the target activity.
     */
    public void launch(Input input, @Nullable OnActivityResult<Result> onActivityResult) {
        if (onActivityResult != null) {
            this.onActivityResult = onActivityResult;
        }
        launcher.launch(input);
    }

    /**
     * Same as {@link #launch(Object, OnActivityResult)} with last parameter set to {@code null}.
     */
    public void launch(Input input) {
        launch(input, this.onActivityResult);
    }

    private void callOnActivityResult(Result result) {
        if (onActivityResult != null) onActivityResult.onActivityResult(result);
    }


    // Copy an InputStream to a File.
    public static File copyInputStreamToTempFile(InputStream in, String fileName, Context context) {
        File outputFile = new File(context.getCacheDir(), fileName);
        if(outputFile.exists()) {
            outputFile.delete();
        }
        copyInputStreamToFile(in, outputFile);
        return outputFile;
    }

    // Copy an InputStream to a File.
    public static void copyInputStreamToFile(InputStream in, File file) {
        OutputStream out = null;

        try {
            out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while((len=in.read(buf))>0){
                out.write(buf,0,len);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            // Ensure that the InputStreams are closed even if there's an exception.
            try {
                if ( out != null ) {
                    out.close();
                }

                // If you want to close the "in" InputStream yourself then remove this
                // from here but ensure that you close it yourself eventually.
                in.close();
            }
            catch ( IOException e ) {
                e.printStackTrace();
            }
        }
    }
}
