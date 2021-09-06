package com.eteks.renovations3d;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mindblowing.renovations3d.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ImportManager {

    private Renovations3DActivity activity;
    public ImportManager(Renovations3DActivity renovations3DActivity) {
        this.activity = renovations3DActivity;
    }


    public void importLibrary(final ImportInfo importInfo, final MenuItem menuItem) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {
            // in android 11 there is a new storage model so simply use the private storage for the app, but try external private first
            File downloadRoot = activity.getExternalFilesDir(null);
            if (downloadRoot == null)
                downloadRoot = activity.getFilesDir();
            importLibrary(importInfo, menuItem, downloadRoot);
        } else {
            // prior version ask for the public downloads folder
            activity.getDownloadsLocation(new Renovations3DActivity.DownloadsLocationRequestor() {
                public void location(final File downloadsLocation) {
                    importLibrary(importInfo, menuItem, downloadsLocation);
                }
            });
        }
    }

    private void importLibrary(ImportInfo importInfo, MenuItem menuItem, File downloadsLocation) {
        String fileName = importInfo.libraryName;
        if( menuItem != null)
            menuItem.setEnabled(false);

        // now present the notice about length and the license info
        final String close = activity.getUserPreferences().getLocalizedString(com.eteks.sweethome3d.swing.HomePane.class, "about.close");
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton(close, null);

        String libraryDownLoadNotice = activity.getString(R.string.libraryDownLoadNotice);
        String license = "";
        if (importInfo.license != null) {
            try {
                AssetManager am = activity.getAssets();
                InputStream is = am.open("licenses/" + importInfo.license);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String line;
                while ((line = reader.readLine()) != null) {
                    license += line + System.getProperty("line.separator");
                }
                reader.close();
            }
            catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }

        ScrollView scrollView = new ScrollView(activity);
        TextView textView = new TextView(activity);
        textView.setPadding(10, 10, 10, 10);
        textView.setText(libraryDownLoadNotice + "\n\n"  + license);
        scrollView.addView(textView);
        builder.setView(scrollView);
        AlertDialog dialog = builder.create();
        dialog.show();

        String url = importInfo.url;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setDescription(fileName + " download");
        request.setTitle(fileName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // oddly see https://stackoverflow.com/questions/16749845/android-download-manager-setdestinationinexternalfilesdir
        // request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, fileName);

        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);

        // get download service and enqueue file
        DownloadManager manager = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        Renovations3DActivity.logFireBaseContent("DownloadManager.enqueue", "fileName: " + fileName);

        activity.registerReceiver(activity.onCompleteHTTPIntent, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    }

    public enum ImportType {
        FURNITURE, TEXTURE, LANGUAGE
    }

    public static class ImportInfo {
        public ImportType type;
        public String id = "";
        public String label = "";
        public String libraryName = "";
        public String license = "";
        public String url;

        public ImportInfo(ImportType type, String id, String label, String libraryName, String license, String url) {
            this.id = id;
            this.type = type;
            this.label = label;
            this.libraryName = libraryName;
            this.license = license;
            this.url = url;
        }
    }



    public static ImportInfo[] importInfos = new ImportInfo[]{
            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-Contributions-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#ContributionsModels", "Contributions",
                    "3DModels-Contributions-1.8.zip",
                    "ALL_LICENSEContributions.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-Contributions-1.8.zip/download"),

            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-LucaPresidente-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#LucaPresidenteModels", "LucaPresidente",
                    "3DModels-LucaPresidente-1.8.zip",
                    "ALL_LICENSELucaPresidente.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-LucaPresidente-1.8.zip/download"),

            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-Scopia-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#ScopiaModels", "Scopia",
                    "3DModels-Scopia-1.8.zip",
                    "ALL_LICENSEScopia.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-Scopia-1.8.zip/download"),

            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-KatorLegaz-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#KatorLegazModels", "KatorLegaz",
                    "3DModels-KatorLegaz-1.8.zip",
                    "ALL_LICENSEKatorLegaz.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-KatorLegaz-1.8.zip/download"),

            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-Reallusion-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#ReallusionModels", "Reallusion",
                    "3DModels-Reallusion-1.8.zip",
                    "ALL_LICENSEReallusion.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-Reallusion-1.8.zip/download"),

            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-BlendSwap-CC-0-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#BlendSwap-CC-0-Models", "BlendSwap-CC-0",
                    "3DModels-BlendSwap-CC-0-1.8.zip",
                    null,
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-BlendSwap-CC-0-1.8.zip/download"),

            //http://prdownloads.sourceforge.net/sweethome3d/3DModels-BlendSwap-CC-BY-1.8.zip
            new ImportInfo(ImportType.FURNITURE, "SweetHome3D#BlendSwap-CC-BY-Models", "BlendSwap-CC-BY",
                    "3DModels-BlendSwap-CC-BY-1.8.sh3f",
                    "ALL_LICENSEBlendSwap-CC-BY-v2.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-BlendSwap-CC-BY-1.8.zip/download"),

            //https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.8/3DModels-Trees-1.8.zip/download
            //trees not included for each tree model being too big generally (800kb average)

            new ImportInfo(ImportType.FURNITURE, "Local_Furniture", "Local", null, null, null),


            new ImportInfo(ImportType.TEXTURE, "SweetHome3D#ContributionsTextures", "TextureContributions",
                    "Textures-Contributions-1.2.zip",
                    "ALL_LICENSETextureContributions.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-textures/Textures-1.2/Textures-Contributions-1.2.zip/download"),

            new ImportInfo(ImportType.TEXTURE, "SweetHome3D#eTeksScopiaTextures", "eTeksScopia",
                    "Textures-eTeksScopia-1.2.zip",
                    "ALL_LICENSEeTeksScopia.TXT",
                    "https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-textures/Textures-1.2/Textures-eTeksScopia-1.2.zip/download"),

            new ImportInfo(ImportType.TEXTURE, "Local_Texture", "Local", null, null, null),


            new ImportInfo(ImportType.LANGUAGE, "pt_PT", "português (Portugal)",
                    "Portugal-6.6.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Portugal-6.6.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "bas", "euskara",
                    "Basque-6.4.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Basque-6.4.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "sl", "slovenščina",
                    "Slovenian-5.7.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Slovenian-5.7.sh3l"),


            new ImportInfo(ImportType.LANGUAGE, "fi", "suomi",
                    "Finnish-6.6.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Finnish-6.6.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "sr", "српски",
                    "Serbian-4.3.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/"),

            new ImportInfo(ImportType.LANGUAGE, "uk", "українська",
                    "Ukrainian-5.7.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Ukrainian-5.7.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "tr", "Türkçe",
                    "Turkish-6.6.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Turkish-6.6.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "ar", "العربية",
                    "Arabic-6.2.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Arabic-6.2.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "in", "Indonesian",
                    "Indonesian-6.2.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Indonesian-6.2.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "th", "ไทย",
                    "Thai-6.4.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Thai-6.4.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "ko", "한국어",
                    "Korean-6.6.sh3l",
                    null,
                    "http://www.sweethome3d.com/translations/Korean-6.6.sh3l"),

            new ImportInfo(ImportType.LANGUAGE, "Local_Language", "Local", null, null, null),
    };
}
