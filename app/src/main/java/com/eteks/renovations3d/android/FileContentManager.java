/*
 * FileContentManager.java 4 juil. 07
 *
 * Sweet Home 3D, Copyright (c) 2007 Emmanuel PUYBARET / eTeks <info@eteks.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package com.eteks.renovations3d.android;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.eteks.renovations3d.FileActivityResult;
import com.eteks.renovations3d.Renovations3DActivity;
import com.mindblowing.swingish.JOptionPane;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.URLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.swingish.JFileChooser;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;


/**
 * Content manager for files with Swing file choosers.
 * @author Emmanuel Puybaret
 */
public class FileContentManager implements ContentManager {
  private static final String OBJ_EXTENSION = ".obj";
  /**
   * Supported OBJ filter.
   */
  private static final FileFilter [] OBJ_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .obj files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(OBJ_EXTENSION);
        }
        

        public String getDescription() {
          return "OBJ - Wavefront";
        }
      }};
  /**
   * Supported 3D model file filters.
   */
  private static final String LWS_EXTENSION = ".lws";
  private static final String THREEDS_EXTENSION = ".3ds";
  private static final String DAE_EXTENSION = ".dae";
  private static final String KMZ_EXTENSION = ".kmz";
  private static final String ZIP_EXTENSION = ".zip";
	//PJPJ made public for activitt to open home files
  public static final FileFilter [] MODEL_FILTERS = {
     OBJ_FILTER [0],
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and LWS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(LWS_EXTENSION);
       }
   

       public String getDescription() {
         return "LWS - LightWave Scene";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and 3DS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(THREEDS_EXTENSION);
       }
   

       public String getDescription() {
         return "3DS - 3D Studio";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and 3DS files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(DAE_EXTENSION);
       }
   

       public String getDescription() {
         return "DAE - Collada";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and ZIP files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(KMZ_EXTENSION);
       }
   

       public String getDescription() {
         return "KMZ";
       }
     },
     new FileFilter() {
       @Override
       public boolean accept(File file) {
         // Accept directories and ZIP files
         return file.isDirectory()
                || file.getName().toLowerCase().endsWith(ZIP_EXTENSION);
       }
   

       public String getDescription() {
         return "ZIP";
       }
     }};
  private static final String PNG_EXTENSION = ".png";
  /**
   * Supported PNG filter.
   */
  private static final FileFilter [] PNG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .png files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(PNG_EXTENSION);
        }
        

        public String getDescription() {
          return "PNG";
        }
      }};
  private static final String JPEG_EXTENSION = ".jpg";
  /**
   * Supported JPEG filter.
   */
  private static final FileFilter [] JPEG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .png files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(JPEG_EXTENSION)
              || file.getName().toLowerCase().endsWith("jpeg");
        }
        

        public String getDescription() {
          return "JPEG";
        }
      }};
  /**
   * Supported image file filters.
   */
  private static final String BMP_EXTENSION = ".bmp";
  private static final String WBMP_EXTENSION = ".wbmp";
  private static final String GIF_EXTENSION = ".gif";
  private static final FileFilter [] IMAGE_FILTERS = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .bmp files
          return file.isDirectory()
                 || file.getName().toLowerCase().endsWith(BMP_EXTENSION)
                 || file.getName().toLowerCase().endsWith(WBMP_EXTENSION);
        }
    

        public String getDescription() {
          return "BMP";
        }
      },
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and GIF files
          return file.isDirectory()
                 || file.getName().toLowerCase().endsWith(GIF_EXTENSION);
        }
    

        public String getDescription() {
          return "GIF";
        }
      },
      JPEG_FILTER [0], 
      PNG_FILTER [0]};
  private static final String MOV_EXTENSION = ".mov";
  /**
   * Supported MOV filter.
   */
  private static final FileFilter [] MOV_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .mov files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(MOV_EXTENSION);
        }
        

        public String getDescription() {
          return "MOV";
        }
      }};
  private static final String PDF_EXTENSION = ".pdf";
  /**
   * Supported PDF filter.
   */
  private static final FileFilter [] PDF_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .pdf files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(PDF_EXTENSION);
        }
        

        public String getDescription() {
          return "PDF";
        }
      }};
  private static final String CSV_EXTENSION = ".csv";
  /**
   * Supported CSV filter.
   */
  private static final FileFilter [] CSV_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .csv files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(CSV_EXTENSION);
        }
        

        public String getDescription() {
          return "CSV - Tab Separated Values";
        }
      }};
  private static final String SVG_EXTENSION = ".svg";
  /**
   * Supported SVG filter.
   */
  private static final FileFilter [] SVG_FILTER = {
      new FileFilter() {
        @Override
        public boolean accept(File file) {
          // Accept directories and .svg files
          return file.isDirectory()
              || file.getName().toLowerCase().endsWith(SVG_EXTENSION);
        }
        

        public String getDescription() {
          return "SVG - Scalable Vector Graphics";
        }
      }};
  
  private final UserPreferences           preferences;
  private final String                    sweetHome3DFileExtension;
  private final String                    sweetHome3DFileExtension2;
  private final String                    languageLibraryFileExtension;
  private final String                    furnitureLibraryFileExtension;
  private final String                    texturesLibraryFileExtension;
  private final String                    pluginFileExtension;
  private Map<ContentType, File>          lastDirectories;
  private Map<ContentType, FileFilter []> fileFilters;
  private Map<ContentType, String []>     fileExtensions;

private Renovations3DActivity activity;//for dialogs etc

  public FileContentManager(final UserPreferences preferences, Renovations3DActivity activity) {
   this. activity = activity;
    this.preferences = preferences;
    this.sweetHome3DFileExtension = preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "homeExtension");
    String homeExtension2;
    try {
      // Get optional second extension
      homeExtension2 = preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "homeExtension2");
    } catch (IllegalArgumentException ex) {
      homeExtension2 = null;
    }
    this.sweetHome3DFileExtension2 = homeExtension2;
    this.languageLibraryFileExtension = preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "languageLibraryExtension");
    this.furnitureLibraryFileExtension = preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "furnitureLibraryExtension");
    this.texturesLibraryFileExtension = preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "texturesLibraryExtension");
    this.pluginFileExtension = preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "pluginExtension");
    this.lastDirectories = new HashMap<ContentManager.ContentType, File>();
    
    // Fill file filters map
    this.fileFilters = new HashMap<ContentType, FileFilter[]>();
    this.fileFilters.put(ContentType.MODEL, MODEL_FILTERS);
    this.fileFilters.put(ContentType.IMAGE, IMAGE_FILTERS);
    this.fileFilters.put(ContentType.MOV, MOV_FILTER);
    this.fileFilters.put(ContentType.PNG, PNG_FILTER);
    this.fileFilters.put(ContentType.JPEG, JPEG_FILTER);
    this.fileFilters.put(ContentType.PDF, PDF_FILTER);
    this.fileFilters.put(ContentType.CSV, CSV_FILTER);
    this.fileFilters.put(ContentType.SVG, SVG_FILTER);
    this.fileFilters.put(ContentType.OBJ, OBJ_FILTER);
    this.fileFilters.put(ContentType.SWEET_HOME_3D, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories, .sh3d and .sh3x files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(sweetHome3DFileExtension)
                || (sweetHome3DFileExtension2 != null
                     && file.getName().toLowerCase().endsWith(sweetHome3DFileExtension2));
          }


          public String getDescription() {
            return preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "homeDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.LANGUAGE_LIBRARY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(languageLibraryFileExtension);
          }
         

          public String getDescription() {
            return preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "languageLibraryDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.FURNITURE_LIBRARY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(furnitureLibraryFileExtension);
          }
          

          public String getDescription() {
            return preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "furnitureLibraryDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.TEXTURES_LIBRARY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(texturesLibraryFileExtension);
          }
         

          public String getDescription() {
            return preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "texturesLibraryDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.PLUGIN, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories and .sh3f files
            return file.isDirectory()
                || file.getName().toLowerCase().endsWith(pluginFileExtension);
          }
         

          public String getDescription() {
            return preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "pluginDescription");
          }
        }
      });
    this.fileFilters.put(ContentType.PHOTOS_DIRECTORY, new FileFilter [] {
        new FileFilter() {
          @Override
          public boolean accept(File file) {
            // Accept directories only
            return file.isDirectory();
          }
         

          public String getDescription() {
            return "Photos";
          }
        }
      });

    // Fill file default extension map
    this.fileExtensions = new HashMap<ContentType, String []>();
    String [] sweetHome3DFileExtensions = this.sweetHome3DFileExtension2 != null
        ? new String [] {this.sweetHome3DFileExtension, this.sweetHome3DFileExtension2}
        : new String [] {this.sweetHome3DFileExtension};
    this.fileExtensions.put(ContentType.SWEET_HOME_3D,     sweetHome3DFileExtensions);
    this.fileExtensions.put(ContentType.LANGUAGE_LIBRARY,  new String [] {this.languageLibraryFileExtension});
    this.fileExtensions.put(ContentType.FURNITURE_LIBRARY, new String [] {this.furnitureLibraryFileExtension});
    this.fileExtensions.put(ContentType.TEXTURES_LIBRARY,  new String [] {this.texturesLibraryFileExtension});
    this.fileExtensions.put(ContentType.PLUGIN,            new String [] {this.pluginFileExtension});
    this.fileExtensions.put(ContentType.PNG,               new String [] {PNG_EXTENSION});
    this.fileExtensions.put(ContentType.JPEG,              new String [] {JPEG_EXTENSION});
    this.fileExtensions.put(ContentType.MOV,               new String [] {MOV_EXTENSION});
    this.fileExtensions.put(ContentType.PDF,               new String [] {PDF_EXTENSION});
    this.fileExtensions.put(ContentType.CSV,               new String [] {CSV_EXTENSION});
    this.fileExtensions.put(ContentType.SVG,               new String [] {SVG_EXTENSION});
    this.fileExtensions.put(ContentType.OBJ,               new String [] {OBJ_EXTENSION});
    this.fileExtensions.put(ContentType.MODEL,             
        new String [] {OBJ_EXTENSION, LWS_EXTENSION, THREEDS_EXTENSION, DAE_EXTENSION, ZIP_EXTENSION, KMZ_EXTENSION});
    this.fileExtensions.put(ContentType.IMAGE,             
        new String [] {PNG_EXTENSION, JPEG_EXTENSION, BMP_EXTENSION, WBMP_EXTENSION, GIF_EXTENSION} );
  }
  
  /**
   * Returns a {@link URLContent URL content} object that references 
   * the given file path.
   */
  public Content getContent(String contentPath) throws RecorderException {
    try {
      return new URLContent(new File(contentPath).toURI().toURL());
    } catch (IOException ex) {
      throw new RecorderException("Couldn't access to content " + contentPath);
    }
  }
  
  /**
   * Returns the file name of the file path in parameter.
   */
  public String getPresentationName(String contentPath, 
                                    ContentType contentType) {
    switch (contentType) {
      case SWEET_HOME_3D :
      case FURNITURE_LIBRARY :
      case TEXTURES_LIBRARY :
      case LANGUAGE_LIBRARY :
      case PLUGIN :
        return new File(contentPath).getName();
      default :
        String fileName = new File(contentPath).getName();
        int pointIndex = fileName.lastIndexOf('.');
        if (pointIndex != -1) {
          fileName = fileName.substring(0, pointIndex);
        }
        return fileName;
    }    
  }
  
  /**
   * Returns the file filters available for a given content type.
   * This method may be overridden to add some file filters to existing content types
   * or to define the filters of a user defined content type.
   */
  protected FileFilter [] getFileFilter(ContentType contentType) {
    if (contentType == ContentType.USER_DEFINED) {
      throw new IllegalArgumentException("Unknown user defined content type");
    } else {
      return this.fileFilters.get(contentType);
    }
  }
  
  /**
   * Returns the default file extension of a given content type. 
   * If not <code>null</code> this extension will be appended automatically 
   * to the file name chosen by user in save dialog.
   * This method may be overridden to change the default file extension of an existing content type
   * or to define the default file extension of a user defined content type.
   */
  public String getDefaultFileExtension(ContentType contentType) {
    String [] fileExtensions = this.fileExtensions.get(contentType);
    if (fileExtensions != null) {
      return fileExtensions [0];
    }
    return null;
  }
  
  /**
   * Returns the supported file extensions for a given content type. 
   * This method may be overridden to change the file extensions of an existing content type
   * or to define the file extensions of a user defined content type.
   */
  protected String [] getFileExtensions(ContentType contentType) {
    return this.fileExtensions.get(contentType);
  }
  
  /**
   * Returns <code>true</code> if the file path in parameter is accepted
   * for <code>contentType</code>.
   */
  public boolean isAcceptable(String contentPath, 
                              ContentType contentType) {
    File file = new File(contentPath);
    for (FileFilter filter : getFileFilter(contentType)) {
      if (filter.accept(file)) {
        return true;
      }
    }
    return false;
  }
  
  /**
   * Returns <code>true</code> if the given content type is for directories.
   */
  protected boolean isDirectory(ContentType contentType) {
    return contentType == ContentType.PHOTOS_DIRECTORY;
  }
  
  /**
   * Returns the file path chosen by user with an open file dialog.
   * @return the file path or <code>null</code> if user canceled its choice.
   */
  public String showOpenDialog(View        parentView,
                               String      dialogTitle,
                               ContentType contentType) {
    // Use native file dialog under Mac OS X
    if (OperatingSystem.isMacOSX()
        && !isDirectory(contentType)) {
      return showFileDialog(parentView, dialogTitle, contentType, null, false);
    } else {
      return showFileChooser(parentView, dialogTitle, contentType, null, false);
    }




  }
  
  /**
   * Returns the file path chosen by user with a save file dialog.
   * If this file already exists, the user will be prompted whether 
   * he wants to overwrite this existing file. 
   * @return the chosen file path or <code>null</code> if user canceled its choice.
   */
  public String showSaveDialog(View        parentView,
                               String      dialogTitle,
                               ContentType contentType,
                               String      path) {
    String defaultExtension = getDefaultFileExtension(contentType);
    if (path != null) {
      // If path has an extension, remove it and build a path that matches contentType
      int extensionIndex = path.lastIndexOf('.');
      if (extensionIndex != -1) {
        path = path.substring(0, extensionIndex);
        if (defaultExtension != null) {
          path += defaultExtension;
        }
      }
    }
    
    String savedPath;
    // Use native file dialog under Mac OS X    
    if (OperatingSystem.isMacOSX()
        && !isDirectory(contentType)) {
      savedPath = showFileDialog(parentView, dialogTitle, contentType, path, true);
    } else {
      savedPath = showFileChooser(parentView, dialogTitle, contentType, path, true);
    }
    
    boolean addedExtension = false;
    if (savedPath != null) {
      if (defaultExtension != null) {
        if (!savedPath.toLowerCase().endsWith(defaultExtension)) {
          savedPath += defaultExtension;
          addedExtension = true;
        }
      }

      // If no extension was added to file under Mac OS X, 
      // FileDialog already asks to user if he wants to overwrite savedName
      if (OperatingSystem.isMacOSX()
          && !addedExtension) {
        return savedPath;
      }
      if (!isDirectory(contentType)) {
        // If the file exists, prompt user if he wants to overwrite it
        File savedFile = new File(savedPath);
        if (savedFile.exists()
            && !confirmOverwrite(parentView, savedFile.getName())) {
          return showSaveDialog(parentView, dialogTitle, contentType, savedPath);
        }
      }
    }
    return savedPath;
  }
  
  /**
   * Displays an AWT open file dialog.
   */
  private String showFileDialog(View               parentView,
                                String             dialogTitle,
                                final ContentType  contentType,
                                String             path, 
                                boolean            save) {


	//PJ currently only MacOS will get here so don't worry too much
	  System.err.println("FileContentManager asked to showFileDialog ");
	  return "";

  /*  FileDialog fileDialog = new FileDialog(
        JOptionPane.getFrameForComponent((JComponent)parentView));

    // Set selected file
    if (save && path != null) {
      fileDialog.setFile(new File(path).getName());
    }
    // Set supported files filter 
    fileDialog.setFilenameFilter(new FilenameFilter() {
        public boolean accept(File dir, String name) {          
          return isAcceptable(new File(dir, name).toString(), contentType);
        }
      });

    // Update directory
    File directory = getLastDirectory(contentType);
    if (directory != null && directory.exists()) {
      if (isDirectory(contentType)) {
        fileDialog.setDirectory(directory.getParent());
        fileDialog.setFile(directory.getName());
      } else {
        fileDialog.setDirectory(directory.toString());
      }
    }
    if (save) {
      fileDialog.setMode(FileDialog.SAVE);
    } else {
      fileDialog.setMode(FileDialog.LOAD);
    }

    if (dialogTitle == null) {
      dialogTitle = getFileDialogTitle(save);
    }
    fileDialog.setTitle(dialogTitle);
    
    fileDialog.setVisible(true);

    String selectedFile = fileDialog.getFile();
    // If user chose a file
    if (selectedFile != null) {
      selectedFile = new File(fileDialog.getDirectory(), selectedFile).toString();
      // Retrieve directory for future calls
      if (isDirectory(contentType)) {
        directory = new File(selectedFile);
      } else {
        directory = new File(fileDialog.getDirectory());
      }
      // Store current directory
      setLastDirectory(contentType, directory);
      // Return selected file
      return selectedFile;
    } else {
      return null;
    }*/
  }

  /**
   * Returns the last directory used for the given content type.
   * @return the last directory for <code>contentType</code> or the default last directory 
   *         if it's not set. If <code>contentType</code> is <code>null</code>, the
   *         returned directory will be the default last one or <code>null</code> if it's not set yet.
   */
  protected File getLastDirectory(ContentType contentType) {
    File directory = this.lastDirectories.get(contentType);
    if (directory == null) {
      directory = this.lastDirectories.get(null);
    }
    return directory;
  }

  /**
   * Stores the last directory for the given content type.
   */
  protected void setLastDirectory(ContentType contentType, File directory) {
    this.lastDirectories.put(contentType, directory);
    // Store default last directory in null content 
    this.lastDirectories.put(null, directory);
  }

  /**
   * Displays a Swing open file chooser.
   */
  private String showFileChooser(View                parentView,
                                 String              dialogTitle,
								 final ContentType   contentType,
                                 final String        path,
                                 final boolean       save) {
	  if(Looper.getMainLooper().getThread() == Thread.currentThread()) {
		  new Throwable().printStackTrace();
		  System.err.println("FileContentManager asked to showFileChooser on EDT thread, it MUST not be called from EDT as it is blocking!");
		  return null;
	  }

	  final File selectedFile[] = new File[1];
	  final Semaphore dialogSemaphore = new Semaphore(0, true);

      //android 30 onwards has a different storage model
      if (Build.VERSION.SDK_INT > Build.VERSION_CODES.Q) {

          if (save == false) {
              //OPEN save == false

              // these ones use the Intent system with camera shot and is unrelated to open dialog
              //BackgroundImageWizardStepsPanel.showImageChoiceDialog()
              //ImportedTextureWizardStepsPanel.showImageChoiceDialog()

              //showOpenDialog (save = false) used by
              if(contentType == ContentManager.ContentType.SWEET_HOME_3D
                      || contentType == ContentManager.ContentType.LANGUAGE_LIBRARY
                      || contentType == ContentManager.ContentType.FURNITURE_LIBRARY
                      || contentType == ContentManager.ContentType.TEXTURES_LIBRARY
                      || contentType == ContentManager.ContentType.MODEL) {

                  // all four file requested here, but loaded by Renovations3DActivity.loadFile(File)
                  //HomePane.showOpenDialog()
                  //   -> Renovation3DActivity.loadSh3dFile() / which replaces HomeController.open()
                  //ContentManager.ContentType.SWEET_HOME_3D,
                  //HomePane.showImportLanguageLibraryDialog()
                  //ContentManager.ContentType.LANGUAGE_LIBRARY,
                  //HomePane.showImportFurnitureLibraryDialog()
                  //ContentManager.ContentType.FURNITURE_LIBRARY,
                  //HomePane.showImportTexturesLibraryDialog()
                  //ContentManager.ContentType.TEXTURES_LIBRARY,

                  //ContentManager.ContentType.MODEL <- is a blocking call and the file is copied to temp and returned

                //TODO: perhaps use the picker library for better file filtering?
                 // https://github.com/informramiz/AndroidFilePickerLibrary


                  Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                  intent.addCategory(Intent.CATEGORY_OPENABLE);
                  intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                  intent.setType("*/*");
                  intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                  String[] mimeTypes = {
                          "application/sweethome3d" //sh3l files hav this set, pity not all sh3d files have i in the manifest
                          ,"application/octet-stream"
                          ,"application/x-zip"
                          ,"application/x-bzip"
                          ,"application/x-bzip2"
                          ,"application/gzip"
                          ,"application/zip"
                          ,"application/x-7z-compressed"
                          //,"application/java-archive"

                  }; //binaries and zips
                  intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

                  activity.fileActivityLauncher.launch(intent, result -> {
                      if (result.getResultCode() == Activity.RESULT_OK) {
                          // There are no request codes
                          Intent data = result.getData();

                          // Get the Uri of the selected file
                          Uri uri = data.getData();
                          String uriString = uri.toString();
                          File loadFile = null;
                          if (uriString.startsWith("content://")) {
                              Cursor cursor = null;
                              try {
                                  cursor = activity.getContentResolver().query(uri, null, null, null, null);
                                  if (cursor != null && cursor.moveToFirst()) {
                                      String libraryName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                                      try {
                                          InputStream in = activity.getContentResolver().openInputStream(uri);
                                          loadFile = FileActivityResult.copyInputStreamToTempFile(in, libraryName, activity);
                                      } catch (FileNotFoundException e) {
                                          e.printStackTrace();
                                      }
                                  }
                              } finally {
                                  cursor.close();
                              }
                          } else if (uriString.startsWith("file://")) {
                              loadFile = new File(uri.getPath());
                          }

                          // In most cases we leave selectedFile[0] = null so the callee assumes a cancel and moves on
                          if (loadFile != null) {
                              Renovations3DActivity.logFireBaseLevelUp("loadFile", loadFile.getName());
                              if (contentType == ContentManager.ContentType.MODEL) {
                                  //TODO: this will only work for "singleton" file zip, dae etc, *.obj will not find textures as they are file relative and the file is well lost
                                  //return null as though a cancel occurs (as loadFile above does the work of loading)
                                  selectedFile[0] = loadFile;
                                  // release the acquire that has been taken below (before this return)
                                  dialogSemaphore.release();
                              } else {
                                  //controller2.importTexturesLibrary(texturesLibraryName);
                                  // use the Activity in case of a zip file requiring unzip, extension dictates what gets imported
                                  activity.loadFile(loadFile);
                              }

                          }
                      }
                  });

                  //Only model wants to wait for return value like a modal dialog
                  if (contentType == ContentManager.ContentType.MODEL) {
                      try {
                          //NOTE: this is a reverse semaphore see(0,true) above, it waits here until the dialog above releases it, reverse of a sync block
                          dialogSemaphore.acquire();
                      } catch (InterruptedException e) {
                      }
                  } else {
                      //return null as though a cancel occurs (as loadFile above does the work of loading)
                      selectedFile[0] = null;
                  }
              }
          } else {
              //SAVE save == true

              //showOpenDialog (save = true) used by
              //HomePane.showPrintToPDFDialog() / not enabled
              //ContentManager.ContentType.PDF,
              //HomePane.showExportToCSVDialog() / not enabled
              //ContentManager.ContentType.CSV,
              //HomePane.showExportToSVGDialog() / not enabled
              //ContentManager.ContentType.SVG,
              //HomePane.showExportToOBJDialog() / not enabled
              //ContentManager.ContentType.OBJ,

              //PhotosPanel.startPhotosCreation() / not ported across (requires the DOCUMENT_TREE picker option)
              //ContentManager.ContentType.PHOTOS_DIRECTORY, // used by photos panel, not in renovations3d-android but probably should be at some point
              //Plugins are not supported
              //ContentManager.ContentType.PLUGIN,
              //not used even in base code
              //ContentManager.ContentType.USER_DEFINED;


              //PhotoPanel.savePhoto()
              //ContentManager.ContentType.IMAGE, - used as image opener not save
              //ContentManager.ContentType.JPEG, - not used
              //ContentManager.ContentType.PNG,
              //MediaStore.Images
              //https://developer.android.com/training/data-storage/shared/media
              // This is now handled in PhotoPanel as the Uri and OutputStream need to be opened and closed in one operation


              //VideoPanel.saveVideo()
              //ContentManager.ContentType.MOV,
              //MediaStore.Video
              //https://developer.android.com/training/data-storage/shared/media
              // This is now handled in VideoPanel as the Uri and OutputStream need to be opened and closed in one operation


              //HomePane.showSaveDialog()
              // -> HomeController.saveAs()
              // -> HomeController.saveAs(...)
              // -> HomeController.saveAsAndCompress()
              // -> -> all called by Renovation3DActivity.saveAsSh3dFile()
              //ContentManager.ContentType.SWEET_HOME_3D,
              // this is now handled by Renovations3DActivity.saveSh3dFile or Renovations3DActivity.saveasSh3dFile

          }
      } else {
          String ok = save ? this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "saveDialog.title")
                  : this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "openDialog.title");
          String cancel = this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "confirmOverwrite.cancel");
          final String[] okCancel = new String[]{ok, cancel};

          FileContentManager.this.activity.getDownloadsLocation(new Renovations3DActivity.DownloadsLocationRequestor() {
              public void location(File downloadsLocation) {
                  // just a name picker for the save as system
                  if (save) {
                      final File parent;
                      if (path == null || path.length() == 0 || path.contains("com.mindblowing.renovations3d")) {
                          parent = downloadsLocation;
                      } else {
                          parent = new File(path).getParentFile();
                      }
                      activity.runOnUiThread(new Runnable() {
                          public void run() {
                              final JFileChooser fileChooser = new JFileChooser(FileContentManager.this.activity, parent, true, false, okCancel);
                              fileChooser.setFileFilters(fileFilters.get(contentType));
                              fileChooser.getDialog().setTitle(getFileDialogTitle(false));
                              fileChooser.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                               @Override
                                                                               public void onDismiss(DialogInterface dialog) {
                                                                                   // release the acquire that has been taken below (before this return)
                                                                                   dialogSemaphore.release();
                                                                               }
                                                                           }
                              );

                              fileChooser.setFileListener(new JFileChooser.FileSelectedListener() {
                                  @Override
                                  public void fileSelected(final File file) {
                                      selectedFile[0] = file;
                                  }
                              });
                              fileChooser.showDialog();
                          }
                      });

                  } else {
                      // in this case we want to show a real file picker (with an extension filter of the right type
                      //TODO: if(contentType == ContentType.FURNITURE_LIBRARY ||contentType == ContentType.TEXTURES_LIBRARY ) need  zip as well one day
                      //TODO: filefilter array should be used like getFileFilter(ContentType contentType) which can be used for sh3d as well etc
                      //MUST use filefilter array cos furniture model has loads of options, note furniture model import handles teh zip files as well
                      final File parent = downloadsLocation;
                      activity.runOnUiThread(new Runnable() {
                          public void run() {
                              final JFileChooser fileChooser = new JFileChooser(FileContentManager.this.activity, parent, false, false, okCancel);
                              fileChooser.setFileFilters(fileFilters.get(contentType));
                              fileChooser.getDialog().setTitle(getFileDialogTitle(false));
                              fileChooser.getDialog().setOnDismissListener(new DialogInterface.OnDismissListener() {
                                                                               @Override
                                                                               public void onDismiss(DialogInterface dialog) {
                                                                                   // release the acquire that has been taken below (before this return)
                                                                                   dialogSemaphore.release();
                                                                               }
                                                                           }
                              );

                              fileChooser.setFileListener(new JFileChooser.FileSelectedListener() {
                                  @Override
                                  public void fileSelected(final File file) {
                                      selectedFile[0] = file;
                                  }
                              });
                              fileChooser.showDialog();
                          }
                      });
                  }
              }
          });

          try {
              //NOTE: this is a reverse semaphore see(0,true) above, it waits here until the dialog above releases it, reverse of a sync block
              dialogSemaphore.acquire();
          } catch (InterruptedException e) {
          }

          Renovations3DActivity.logFireBaseContent("showFileChooser" + (save ? "Save" : "Open"), "Selected file " + selectedFile[0]);
      }

	  if( selectedFile[0] == null)
		  return null;
	  else
		  return selectedFile[0].getAbsolutePath();

   /* final JFileChooser fileChooser;
    if (isDirectory(contentType)) {
      fileChooser = new DirectoryChooser(this.preferences);
    } else {
      fileChooser = new JFileChooser();
    }
    if (dialogTitle == null) {
      dialogTitle = getFileDialogTitle(save);
    }
    fileChooser.setDialogTitle(dialogTitle);

    // Update directory
    File directory = getLastDirectory(contentType);
    if (directory != null && directory.exists()) {
      if (isDirectory(contentType)) {
        fileChooser.setCurrentDirectory(directory.getParentFile());
        fileChooser.setSelectedFile(directory);
      } else {
        fileChooser.setCurrentDirectory(directory);
      }
    }    
    // Set selected file
    if (save 
        && path != null
        && (directory == null || !isDirectory(contentType))) {
      fileChooser.setSelectedFile(new File(path));
    }    
    // Set supported files filter 
    FileFilter acceptAllFileFilter = fileChooser.getAcceptAllFileFilter();
    fileChooser.addChoosableFileFilter(acceptAllFileFilter);
    FileFilter [] contentFileFilters = getFileFilter(contentType);
    for (FileFilter filter : contentFileFilters) {
      fileChooser.addChoosableFileFilter(filter);
    }
    // If there's only one file filter, select it 
    if (contentFileFilters.length == 1) {
      fileChooser.setFileFilter(contentFileFilters [0]);
    } else {
      fileChooser.setFileFilter(acceptAllFileFilter);
    }
    int option;
    if (isDirectory(contentType)) {
      option = fileChooser.showDialog((JComponent)parentView,
          this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "selectFolderButton.text"));
    } else if (save) {
      option = fileChooser.showSaveDialog((JComponent)parentView);
    } else {
      option = fileChooser.showOpenDialog((JComponent)parentView);
    }    
    if (option == JFileChooser.APPROVE_OPTION) {
      // Retrieve last directory for future calls
      if (isDirectory(contentType)) {
        directory = fileChooser.getSelectedFile();
      } else {
        directory = fileChooser.getCurrentDirectory();
      }
      // Store last directory
      setLastDirectory(contentType, directory);
      // Return selected file
      return fileChooser.getSelectedFile().toString();
    } else {
      return null;
    }*/
  }

  /**
   * Returns default file dialog title.
   */
  protected String getFileDialogTitle(boolean save) {
    if (save) {
      return this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "saveDialog.title");
    } else {
      return this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "openDialog.title");
    }
  }
    
  /**
   * Displays a dialog that let user choose whether he wants to overwrite 
   * file <code>path</code> or not.
   * @return <code>true</code> if user confirmed to overwrite.
   */
  protected boolean confirmOverwrite(View parentView, String path) {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "confirmOverwrite.message", path);
    String title = this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "confirmOverwrite.title");
    String replace = this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "confirmOverwrite.overwrite");
    String cancel = this.preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "confirmOverwrite.cancel");

    boolean replaceAnswer =  JOptionPane.showOptionDialog(activity, message, title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
			null, new Object[]{replace, cancel}, replace) == JOptionPane.OK_OPTION;

	  Renovations3DActivity.logFireBaseContent("confirmOverwrite-"+replaceAnswer, path);
	  return replaceAnswer;
  }
  
  /**
   * A file chooser dedicated to directory choice.
   */
/*  private static class DirectoryChooser extends JFileChooser {
    private Executor               fileSystemViewExecutor;
    private JTree                  directoriesTree;
    private TreeSelectionListener  treeSelectionListener;
    private PropertyChangeListener selectedFileListener;
    private Action                 createDirectoryAction;

    public DirectoryChooser(final UserPreferences preferences) {
      setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      this.fileSystemViewExecutor = Executors.newSingleThreadExecutor();
      this.directoriesTree = new JTree(new DefaultTreeModel(new DirectoryNode()));
      this.directoriesTree.setRootVisible(false);
      this.directoriesTree.setEditable(false);
      this.directoriesTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
      this.directoriesTree.setCellRenderer(new DefaultTreeCellRenderer() {
          @Override
          public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                        boolean leaf, int row, boolean hasFocus) {
            DirectoryNode node = (DirectoryNode)value;
            File file = (File)node.getUserObject();
            super.getTreeCellRendererComponent(tree, DirectoryChooser.this.getName(file), 
                selected, expanded, leaf, row, hasFocus);
            setIcon(DirectoryChooser.this.getIcon(file));
            if (!node.isWritable()) {
              setForeground(Color.GRAY);
            } 
            return this;
          }
        });
      this.treeSelectionListener = new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent ev) {
            TreePath selectionPath = directoriesTree.getSelectionPath();
            if (selectionPath != null) {
              DirectoryNode selectedNode = (DirectoryNode)selectionPath.getLastPathComponent();
              setSelectedFile((File)selectedNode.getUserObject());
            }
          }
        };
      this.directoriesTree.addTreeSelectionListener(this.treeSelectionListener);
      
      this.selectedFileListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            showSelectedFile();
          }
        };
      addPropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, this.selectedFileListener);
      
      this.directoriesTree.addTreeExpansionListener(new TreeExpansionListener() {
          public void treeCollapsed(TreeExpansionEvent ev) {
            if (ev.getPath().isDescendant(directoriesTree.getSelectionPath())) {
              // If selected node becomes hidden select not hidden parent
              removePropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, selectedFileListener);
              directoriesTree.setSelectionPath(ev.getPath());
              addPropertyChangeListener(SELECTED_FILE_CHANGED_PROPERTY, selectedFileListener);
            }
          }
          
          public void treeExpanded(TreeExpansionEvent ev) {
          }
        });
      
      // Create an action used to create additional directories
      final String newDirectoryText = UIManager.getString("FileChooser.win32.newFolder");
      this.createDirectoryAction = new AbstractAction(newDirectoryText) {
          public void actionPerformed(ActionEvent ev) {
            String newDirectoryNameBase = OperatingSystem.isWindows() || OperatingSystem.isMacOSX()
                ? newDirectoryText
                : UIManager.getString("FileChooser.other.newFolder");
            String newDirectoryName = newDirectoryNameBase;
            // Search a new directory name that doesn't exist
            DirectoryNode parentNode = (DirectoryNode)directoriesTree.getLastSelectedPathComponent();
            File parentDirectory = (File)parentNode.getUserObject();
            for (int i = 2; new File(parentDirectory, newDirectoryName).exists(); i++) {
              newDirectoryName = newDirectoryNameBase;
              if (OperatingSystem.isWindows() || OperatingSystem.isMacOSX()) {
                newDirectoryName += " ";
              }
              newDirectoryName += i;
            }
            newDirectoryName = (String)JOptionPane.showInputDialog(DirectoryChooser.this, 
                preferences.getLocalizedString(com.eteks.sweethome3d.swing.FileContentManager.class, "createFolder.message"),
                newDirectoryText, JOptionPane.QUESTION_MESSAGE, null, null, newDirectoryName);
            if (newDirectoryName != null) {
              File newDirectory = new File(parentDirectory, newDirectoryName);
              if (!newDirectory.mkdir()) {
                String newDirectoryErrorText = UIManager.getString("FileChooser.newFolderErrorText");
                JOptionPane.showMessageDialog(DirectoryChooser.this, 
                    newDirectoryErrorText, newDirectoryErrorText, JOptionPane.ERROR_MESSAGE);
              } else {
                parentNode.updateChildren(parentNode.getChildDirectories());
                ((DefaultTreeModel)directoriesTree.getModel()).nodeStructureChanged(parentNode);
                setSelectedFile(newDirectory);
              }
            }
          }
        };
      
      setSelectedFile(getFileSystemView().getHomeDirectory());
    }*/
    
    /**
     * Selects the given directory or its parent if it's a file.
     */
 /*   @Override
    public void setSelectedFile(File file) {
      if (file != null && file.isFile()) {
        file = file.getParentFile();
      }
      super.setSelectedFile(file);
    }*/
    
    /**
     * Shows asynchronously the selected file in the directories tree, 
     * filling the parents siblings hierarchy if necessary.
     */
 /*   private void showSelectedFile() {
      final File selectedFile = getSelectedFile();
      if (selectedFile != null) {
        final DirectoryNode rootNode = (DirectoryNode)this.directoriesTree.getModel().getRoot();
        this.fileSystemViewExecutor.execute(new Runnable() {
            public void run() {
              try {
                EventQueue.invokeAndWait(new Runnable() {
                    public void run() {
                      createDirectoryAction.setEnabled(false);
                      if (directoriesTree.isShowing()) {
                        directoriesTree.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                      }
                    }
                  });
                File cononicalFile = selectedFile.getCanonicalFile();
                // Search parents of the selected file
                List<File> parentsAndFile = new ArrayList<File>();
                for (File file = cononicalFile; 
                    file != null;
                    file = getFileSystemView().getParentDirectory(file)) {
                  parentsAndFile.add(0, file);
                }
                // Build path of tree nodes
                final List<DirectoryNode> pathToFileNode = new ArrayList<DirectoryNode>();
                DirectoryNode node = rootNode;
                pathToFileNode.add(node);
                for (final File file : parentsAndFile) {
                  final File [] childDirectories = node.isLoaded() 
                      ? null 
                      : node.getChildDirectories();
                  // Search in a child of the node has a user object equal to file
                  final DirectoryNode currentNode = node;
                  EventQueue.invokeAndWait(new Runnable() {
                      public void run() {
                        if (!currentNode.isLoaded()) {
                          currentNode.updateChildren(childDirectories);
                          ((DefaultTreeModel)directoriesTree.getModel()).nodeStructureChanged(currentNode);
                        }
                        for (int i = 0, n = currentNode.getChildCount(); i < n; i++) {
                          DirectoryNode child = (DirectoryNode)currentNode.getChildAt(i);
                          if (file.equals(child.getUserObject())) {
                            pathToFileNode.add(child);
                            break;
                          }
                        }
                      }
                    });
                  node = pathToFileNode.get(pathToFileNode.size() - 1);
                  if (currentNode == node) {
                    // Give up since file wasn't found
                    break;
                  }
                }
                  
                if (pathToFileNode.size() > 1) {
                  final TreePath path = new TreePath(pathToFileNode.toArray(new TreeNode [pathToFileNode.size()]));
                  EventQueue.invokeAndWait(new Runnable() {
                      public void run() {
                        directoriesTree.removeTreeSelectionListener(treeSelectionListener);
                        directoriesTree.expandPath(path);
                        directoriesTree.setSelectionPath(path);
                        directoriesTree.scrollRowToVisible(directoriesTree.getRowForPath(path));
                        directoriesTree.addTreeSelectionListener(treeSelectionListener);
                      }
                    });                    
                }
                
              } catch (IOException ex) {
                // Ignore directories that can't be found 
              } catch (InterruptedException ex) {
                // Give up if interrupted
              } catch (InvocationTargetException ex) {
                ex.printStackTrace();
              } finally {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      createDirectoryAction.setEnabled(directoriesTree.getSelectionCount() > 0 
                          && ((DirectoryNode)directoriesTree.getSelectionPath().getLastPathComponent()).isWritable());
                      directoriesTree.setCursor(Cursor.getDefaultCursor());
                    }
                  });
              }
            }
          });
      }
    }

    @Override
    public int showDialog(Component parent, final String approveButtonText) {
      final JButton createDirectoryButton = new JButton(this.createDirectoryAction);
      final JButton approveButton = new JButton(approveButtonText);
      Object cancelOption = UIManager.get("FileChooser.cancelButtonText");
      Object [] options;
      if (OperatingSystem.isMacOSX()) {
        options = new Object [] {approveButton, cancelOption, createDirectoryButton};
      } else {
        options = new Object [] {createDirectoryButton, approveButton, cancelOption};
      }
      // Display chooser in a resizable dialog
      final JOptionPane optionPane = new JOptionPane(SwingTools.createScrollPane(this.directoriesTree),
          JOptionPane.PLAIN_MESSAGE, JOptionPane.DEFAULT_OPTION, null, options, approveButton); 
      final JDialog dialog = optionPane.createDialog(SwingUtilities.getRootPane(parent), getDialogTitle());
      dialog.setResizable(true);
      dialog.pack();
      if (this.directoriesTree.getSelectionCount() > 0) {
        this.directoriesTree.scrollPathToVisible(this.directoriesTree.getSelectionPath());
        boolean validDirectory = ((DirectoryNode)this.directoriesTree.getSelectionPath().getLastPathComponent()).isWritable();
        approveButton.setEnabled(validDirectory);
        createDirectoryAction.setEnabled(validDirectory);
      }
      this.directoriesTree.addTreeSelectionListener(new TreeSelectionListener() {
          public void valueChanged(TreeSelectionEvent ev) {
            TreePath selectedPath = ev.getPath();
            boolean validDirectory = selectedPath != null 
                && ((DirectoryNode)ev.getPath().getLastPathComponent()).isWritable();
            approveButton.setEnabled(validDirectory);
            createDirectoryAction.setEnabled(validDirectory); 
          }
        });
      approveButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            optionPane.setValue(approveButtonText);
            dialog.setVisible(false);
          }
        });
      dialog.setMinimumSize(dialog.getPreferredSize());
      dialog.setVisible(true);
      dialog.dispose();
      if (approveButtonText.equals(optionPane.getValue())) {
        return JFileChooser.APPROVE_OPTION;
      } else {
        return JFileChooser.CANCEL_OPTION;
      }
    }*/

    /**
     * Directory nodes which children are loaded when needed. 
     */
 /*   private class DirectoryNode extends DefaultMutableTreeNode {
      private boolean loaded;
      private boolean writable;

      private DirectoryNode() {
        super(null);
      }

      private DirectoryNode(File file) {
        super(file);
        this.writable = file.canWrite();
      }

      public boolean isWritable() {
        return this.writable;
      }
      
      @Override
      public int getChildCount() {
        if (!this.loaded) {
          this.loaded = true;
          return updateChildren(getChildDirectories());
        } else {
          return super.getChildCount();
        }
      }

      public File [] getChildDirectories() {
        File [] childFiles = getUserObject() == null
            ? getFileSystemView().getRoots()
            : getFileSystemView().getFiles((File)getUserObject(), true);
        if (childFiles != null) {
          List<File> childDirectories = new ArrayList<File>(childFiles.length);
          for (File childFile : childFiles) {
            if (isTraversable(childFile)) {
              childDirectories.add(childFile);
            }
          }
          return childDirectories.toArray(new File [childDirectories.size()]);
        } else {
          return new File [0];
        }
      }
      
      public boolean isLoaded() {
        return this.loaded;
      }

      public int updateChildren(File [] childDirectories) {
        if (this.children == null) {
          this.children = new Vector<File>(childDirectories.length); 
        }          
        synchronized (this.children) {
          removeAllChildren();
          for (File childFile : childDirectories) {
            add(new DirectoryNode(childFile));
          }
          return childDirectories.length;
        }
      }
    }
  }*/

    //https://stackoverflow.com/questions/3401579/get-filename-and-path-from-uri-from-mediastore/51227392#51227392
    public static String getFullPathFromContentUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if ("com.android.externalstorage.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }//non-primary e.g sd card
                else {
                    String filePath = "";
                    if (Build.VERSION.SDK_INT > 20) {
                        //getExternalMediaDirs() added in API 21
                        File extenal[] = context.getExternalMediaDirs();
                        for (File f : extenal) {
                            filePath = f.getAbsolutePath();
                            if (filePath.contains(type)) {
                                int endIndex = filePath.indexOf("Android");
                                filePath = filePath.substring(0, endIndex) + split[1];
                            }
                        }
                    }else{
                        filePath = "/storage/" + type + "/" + split[1];
                    }
                    return filePath;
                }
            }
            // DownloadsProvider
            else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                Cursor cursor = null;
                final String column = "_data";
                final String[] projection = {
                        column
                };

                try {
                    cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                            null);
                    if (cursor != null && cursor.moveToFirst()) {
                        final int column_index = cursor.getColumnIndexOrThrow(column);
                        return cursor.getString(column_index);
                    }
                } finally {
                    if (cursor != null)
                        cursor.close();
                }
                return null;
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }
}
