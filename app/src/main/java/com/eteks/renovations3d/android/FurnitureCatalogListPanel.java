/*
 * FurnitureCatalogListPanel.java 10 janv 2010
 *
 * Sweet Home 3D, Copyright (c) 2010 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.IntentFilter;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.eteks.renovations3d.Tutorial;
import com.mindblowing.swingish.DefaultComboBoxModel;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JTextField;
import com.eteks.renovations3d.android.utils.WelcomeDialog;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;

import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Library;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;

import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.HomeController;

import com.eteks.renovations3d.Renovations3DActivity;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import javaawt.EventQueue;
import javaawt.Graphics;
import javaxswing.Icon;
import javaxswing.ImageIcon;


/**
 * A furniture catalog view that displays furniture in a list, with a combo and search text field.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class FurnitureCatalogListPanel extends JComponent implements com.eteks.sweethome3d.viewcontroller.View {
	public static final String WELCOME_SCREEN_UNWANTED = "CATALOG_WELCOME_SCREEN_UNWANTED";

	private static String localString = "Local";

	// if we are not initialized then ignore onCreateViews
	private boolean initialized = false;

	private int iconHeightPx = 75;// used by imageviews
	private static int DEFAULT_ICON_HEIGHT_DP = 75;// must relate to the layout R.id.main_grid

	public FurnitureCatalogListPanel() {

	}

	@Override
	public void paintComponent(Graphics g) {
		//PJ do I ever need this
	}

	private GridView gridView;
	private FurnitureImageView selectedFiv = null;

	private View rootView;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.furniture_catalog_list, container, false);
		if (initialized) {
			this.setHasOptionsMenu(true);

			createComponents(catalog, preferences, controller);
			layoutComponents();

			gridView = (GridView) rootView.findViewById(R.id.furniture_cat_main_grid);
			gridView.setAdapter(new ImageAdapter(this.getContext()));

			gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					selectedFiv = (FurnitureImageView) v;
					for (int j = 0; j < gridView.getChildCount(); j++) {
						final ImageView iv = (ImageView) gridView.getChildAt(j);
						if (iv == v)
							iv.setBackgroundColor(Color.CYAN);
						else
							iv.setBackgroundColor(Color.WHITE);
					}
				}
			});

			gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				public boolean onItemLongClick(AdapterView<?> parent, View v, int position, long id) {
					selectedFiv = (FurnitureImageView) v;
					//highlight it so we can tell what we just did
					for (int j = 0; j < gridView.getChildCount(); j++) {
						final ImageView iv = (ImageView) gridView.getChildAt(j);
						if (iv == v)
							iv.setBackgroundColor(Color.CYAN);
						else
							iv.setBackgroundColor(Color.WHITE);
					}
					addFurniture();
					return true;
				}
			});

			// Convert the dps to pixels, based on density scale
			final float scale = getResources().getDisplayMetrics().density;
			iconHeightPx = (int) (DEFAULT_ICON_HEIGHT_DP * scale + 0.5f);
		}

		// make the left and right swipers work
		ImageButton furnitureCatalogLeftSwiper = (ImageButton)rootView.findViewById(R.id.furnitureCatalogLeftSwiper);
		furnitureCatalogLeftSwiper.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Renovations3DActivity)getActivity()).getViewPager().setCurrentItem(1, true);
			}
		});
		ImageButton furnitureCatalogRightSwiper = (ImageButton)rootView.findViewById(R.id.furnitureCatalogRightSwiper);
		furnitureCatalogRightSwiper.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				((Renovations3DActivity)getActivity()).getViewPager().setCurrentItem(3, true);
			}
		});
		return rootView;
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
		// this gets called heaps of time, wat until we have an activity
		if (isVisibleToUser && getActivity() != null) {
			WelcomeDialog.possiblyShowWelcomeScreen((Renovations3DActivity) getActivity(), WELCOME_SCREEN_UNWANTED, R.string.welcometext_catalogview, preferences);

			// tell the tutorial we've been shown
			((Renovations3DActivity) getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.FURNITURE_CATALOG_SHOWN);

		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.furniture_cat_list_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}


	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		menu.findItem(R.id.furnitureAdd).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "ADD_HOME_FURNITURE.Name"));
		menu.findItem(R.id.import_furniture_lib).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_FURNITURE_LIBRARY.Name"));
		menu.findItem(R.id.import_texture_lib).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_TEXTURES_LIBRARY.Name"));

		menu.findItem(R.id.importTexture).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_TEXTURE.Name"));
		menu.findItem(R.id.importFurniture).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_FURNITURE.Name"));


		localString = getActivity().getString(R.string.local);

		updateImportSubMenus(menu.findItem(R.id.import_furniture_lib), menu.findItem(R.id.import_texture_lib));

		super.onPrepareOptionsMenu(menu);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getGroupId() == MENU_IMPORT_FURNITURE || item.getGroupId() == MENU_IMPORT_TEXTURE) {
			String importName = item.getTitle().toString();

			if (importName != null) {
				if (importName.equals(localString)) {
					if (item.getGroupId() == MENU_IMPORT_FURNITURE) {
						importFurnitureLibrary();
					} else {
						importTextureLibrary();
					}
					return true;
				}

				for (ImportInfo importInfo : importInfos) {
					if (importInfo.label.equals(importName)) {
						importLibrary(importInfo, item);
						return true;
					}
				}
			}
		} else {
			Renovations3DActivity renovations3DActivity = ((Renovations3DActivity) getActivity());
			// Handle item selection
			switch (item.getItemId()) {
				case R.id.furnitureAdd:
					addFurniture();
					return true;
				case R.id.importTexture:
					if (renovations3DActivity.getHomeController() != null)
						renovations3DActivity.getHomeController().importTexture();
					return true;
				case R.id.importFurniture:
					if (renovations3DActivity.getHomeController() != null)
						renovations3DActivity.getHomeController().importFurniture();
					return true;
				default:
					return super.onOptionsItemSelected(item);
			}
		}
		return false;
	}

	private void importLibrary(final ImportInfo importInfo, MenuItem menuItem) {
		String fileName = importInfo.libraryName;
		if (!(new File(Renovations3DActivity.downloadsLocation, fileName).exists())) {
			menuItem.setEnabled(false);

			String url = importInfo.url;

			DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
			request.setDescription(fileName + " download");
			request.setTitle(fileName);
			request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

			if (Renovations3DActivity.writeExternalStorageGranted) {
				request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
			} else {
				request.setDestinationInExternalFilesDir(getActivity(), Environment.DIRECTORY_DOWNLOADS, fileName);
			}

			// get download service and enqueue file
			DownloadManager manager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
			manager.enqueue(request);
			Renovations3DActivity.logFireBaseContent("DownloadManager.enqueue", "fileName: " + fileName);

			getActivity().registerReceiver(((Renovations3DActivity) getActivity()).onCompleteHTTPIntent, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

			// now present the notice about length and the license info
			final String close = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.HomePane.class, "about.close");
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setPositiveButton(close, null);

			String libraryDownLoadNotice = getActivity().getString(R.string.libraryDownLoadNotice);
			String license = "";
			if (importInfo.license != null) {
				try {
					AssetManager am = getActivity().getAssets();
					InputStream is = am.open("licenses/" + importInfo.license);
					BufferedReader reader = new BufferedReader(new InputStreamReader(is));

					String line;
					while ((line = reader.readLine()) != null) {
						license += line + System.getProperty("line.separator");
					}
					reader.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}

			ScrollView scrollView = new ScrollView(getActivity());
			TextView textView = new TextView(getActivity());
			textView.setPadding(10,10,10,10);
			textView.setText(libraryDownLoadNotice + "\n\n" + license);
			scrollView.addView(textView);
			builder.setView(scrollView);
			AlertDialog dialog = builder.create();
			dialog.show();
		} else {
			//If it already exists in the downloads folder, just import it
			Thread t3 = new Thread() {
				public void run() {
					HomeController controller = ((Renovations3DActivity) FurnitureCatalogListPanel.this.getActivity()).getHomeController();
					if (controller != null) {
						String fileName = importInfo.libraryName;
						File importFile = new File(Renovations3DActivity.downloadsLocation, fileName);

						if (importInfo.type == ImportType.FURNITURE) {
							controller.importFurnitureLibrary(importFile.getAbsolutePath());
							Renovations3DActivity.logFireBaseLevelUp("importFurnitureLibrary", importFile.getName());
						} else if (importInfo.type == ImportType.TEXTURE) {
							controller.importTexturesLibrary(importFile.getAbsolutePath());
							Renovations3DActivity.logFireBaseLevelUp("importTexturesLibrary", importFile.getName());
						}
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								getActivity().invalidateOptionsMenu();
							}
						});

					}
				}
			};
			t3.start();
		}
	}

	enum ImportType {
		FURNITURE, TEXTURE
	}

	class ImportInfo {
		public ImportType type;
		String id = "";
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


	//trees not included for being too big generally
//	https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-Trees-1.6.3.zip/download

	ImportInfo[] importInfos = new ImportInfo[]{
			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#BlendSwap-CC-0-Models", "BlendSwap-CC-0",
							"3DModels-BlendSwap-CC-0-1.6.3.zip",
							null,
					//"https://dl.dropboxusercontent.com/s/we0yep4m0nxu5xw/BlendSwap-CC-0.sh3f"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-BlendSwap-CC-0-1.6.3.zip/download"),

			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#BlendSwap-CC-BY-Models", "BlendSwap-CC-BY",
							"3DModels-BlendSwap-CC-BY-1.6.3v2.sh3f",
							"ALL_LICENSEBlendSwap-CC-BY-v2.TXT",
					//"https://dl.dropboxusercontent.com/s/o4u6yy5ylxgmdl3/BlendSwap-CC-BY-v2.sh3f"),
							"https://dl.dropboxusercontent.com/s/wzxvo0q4lrlncgo/BlendSwap-CC-BY-1.6.3v2.sh3f"),
					//"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-BlendSwap-CC-BY-1.6.3.zip/download"),

					// NOTE! comes from dropbox to lose the smallOven model that has a bad license
			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#ContributionsModels", "Contributions",
							"3DModels-Contributions-1.6.3.zip",
							"ALL_LICENSEContributions.TXT",
					//"https://dl.dropboxusercontent.com/s/glz7vsr04yzszro/Contributions.sh3f"),
							"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-Contributions-1.6.3.zip/download"),

			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#KatorLegazModels", "KatorLegaz",
							"3DModels-KatorLegaz-1.6.3.zip",
							"ALL_LICENSEKatorLegaz.TXT",
					//"https://dl.dropboxusercontent.com/s/ppzfsl6uldqfxy1/KatorLegaz.sh3f"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-KatorLegaz-1.6.3.zip/download"),

			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#LucaPresidenteModels", "LucaPresidente",
							"3DModels-LucaPresidente-1.6.3.zip",
							"ALL_LICENSELucaPresidente.TXT",
					//"https://dl.dropboxusercontent.com/s/a7jcxe0xdtkhw9b/LucaPresidente.sh3f"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-LucaPresidente-1.6.3.zip/download"),

			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#ReallusionModels", "Reallusion",
							"3DModels-Reallusion-1.6.3.zip",
							 "ALL_LICENSEReallusion.TXT",
					//"https://dl.dropboxusercontent.com/s/xrnt81rpke271jo/Reallusion.sh3f"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-Reallusion-1.6.3.zip/download"),

			new ImportInfo(ImportType.FURNITURE, "SweetHome3D#ScopiaModels", "Scopia",
							"3DModels-Scopia-1.6.3.zip",
							"ALL_LICENSEScopia.TXT",
					//"https://dl.dropboxusercontent.com/s/ij2ly5qrau9t4la/Scopia.sh3f"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-models/3DModels-1.6.3/3DModels-Scopia-1.6.3.zip/download"),

			new ImportInfo(ImportType.FURNITURE, "Local_Furniture", "Local", null, null, null),


			new ImportInfo(ImportType.TEXTURE, "SweetHome3D#ContributionsTextures", "TextureContributions",
							"Textures-Contributions-1.2.zip",
							"ALL_LICENSETextureContributions.TXT",
					//"https://dl.dropboxusercontent.com/s/smlz0f6rwowc1gp/TextureContributions.sh3t"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-textures/Textures-1.2/Textures-Contributions-1.2.zip/download"),

			new ImportInfo(ImportType.TEXTURE, "SweetHome3D#eTeksScopiaTextures", "eTeksScopia",
							"Textures-eTeksScopia-1.2.zip",
							"ALL_LICENSEeTeksScopia.TXT",
					//"https://dl.dropboxusercontent.com/s/5b6b0kkwlu48kui/eTeksScopia.sh3t"),
					"https://sourceforge.net/projects/sweethome3d/files/SweetHome3D-textures/Textures-1.2/Textures-eTeksScopia-1.2.zip/download"),

			new ImportInfo(ImportType.TEXTURE, "Local_Texture", "Local", null, null, null),
	};

	private static int MENU_IMPORT_FURNITURE = 10;
	private static int MENU_IMPORT_TEXTURE = 11;


	private void updateImportSubMenus(MenuItem furnitureMenu,
									  MenuItem textureMenu) {
		furnitureMenu.getSubMenu().clear();
		textureMenu.getSubMenu().clear();
		List<Library> libs = ((Renovations3DActivity) getActivity()).getUserPreferences().getLibraries();

		int menuId = Menu.FIRST;
		for (ImportInfo importInfo : importInfos) {
			if( importInfo.id.equals("Local_Furniture") || importInfo.id.equals("Local_Texture"))
				importInfo.label = localString;

			boolean enable = true;
			for (Library lib : libs) {
				if (lib.getId() != null && lib.getId().equals(importInfo.id)) {
					enable = false;
					break;
				}
			}
			if (importInfo.type == ImportType.FURNITURE) {
				MenuItem menuitem = furnitureMenu.getSubMenu().add(MENU_IMPORT_FURNITURE, menuId++, Menu.NONE, importInfo.label);
				menuitem.setEnabled(enable);
			} else {
				MenuItem menuitem = textureMenu.getSubMenu().add(MENU_IMPORT_TEXTURE, menuId++, Menu.NONE, importInfo.label);
				menuitem.setEnabled(enable);
			}
		}
	}

	private void importTextureLibrary() {
		if(!getActivity().isFinishing())
			Toast.makeText(getActivity(), getActivity().getString(R.string.pleaseSelectSh3t) , Toast.LENGTH_LONG).show();
			Thread t3 = new Thread() {
				public void run() {
					HomeController controller2 = ((Renovations3DActivity) getActivity()).getHomeController();
					if (controller2 != null) {
					//We can't use this as it gets onto the the EDT and cause much trouble, so we just copy out
					//	controller.importTexturesLibrary();

					String texturesLibraryName = controller2.getView().showImportTexturesLibraryDialog();
					if (texturesLibraryName != null) {
						controller2.importTexturesLibrary(texturesLibraryName);
						Renovations3DActivity.logFireBaseLevelUp("importTexturesLibrary", texturesLibraryName);

						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								getActivity().invalidateOptionsMenu();
							}
						});
					}
				}
			}
		};
		t3.start();
	}

	private void importFurnitureLibrary() {
		// so this business is only when the dialog last "Other" item is selected
		if(!getActivity().isFinishing())
			Toast.makeText(getActivity(), getActivity().getString(R.string.pleaseSelectSh3f) , Toast.LENGTH_LONG).show();

		Thread t2 = new Thread() {
			public void run() {
				HomeController controller = ((Renovations3DActivity) getActivity()).getHomeController();
				if (controller != null) {
					//We can't use this as it gets onto the the EDT and cause much trouble, so we just copy out
					//controller.importFurnitureLibrary();
					String furnitureLibraryName = controller.getView().showImportFurnitureLibraryDialog();
					if (furnitureLibraryName != null) {
						controller.importFurnitureLibrary(furnitureLibraryName);
						Renovations3DActivity.logFireBaseLevelUp("importFurnitureLibrary", furnitureLibraryName);
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								getActivity().invalidateOptionsMenu();
							}
						});
					}
				}
			}
		};
		t2.start();
	}

	private void addFurniture() {
		if (selectedFiv != null) {
			ArrayList<CatalogPieceOfFurniture> al = new ArrayList<CatalogPieceOfFurniture>();
			al.add(selectedFiv.getCatalogPieceOfFurniture());
			HomeController homeController = ((Renovations3DActivity) getActivity()).getHomeController();
			if (homeController != null) {
				homeController.getFurnitureCatalogController().setSelectedFurniture(al);
				homeController.addHomeFurniture();

				Renovations3DActivity.logFireBaseContent("addFurniture", selectedFiv.getCatalogPieceOfFurniture().getName());

				((Renovations3DActivity) getActivity()).getViewPager().setCurrentItem(1, true);
			}
		}
	}

	private Paint mTextPaint = new Paint();

	public class ImageAdapter extends BaseAdapter {
		private Context mContext;

		public ImageAdapter(Context c) {
			mContext = c;
		}

		public int getCount() {
			return catalogListModel.getSize();
		}

		public Object getItem(int position) {
			return catalogListModel.getElementAt(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			CatalogPieceOfFurniture catalogPieceOfFurniture = (CatalogPieceOfFurniture) catalogListModel.getElementAt(position);
			FurnitureImageView imageView;
			if (convertView == null) {
				// if it's not recycled, initialize some attributes
				imageView = new FurnitureImageView(mContext, catalogPieceOfFurniture);

				imageView.setLayoutParams(new GridView.LayoutParams(iconHeightPx + 10, iconHeightPx + 10));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
				imageView.setBackgroundColor(Color.WHITE);

				//int DEFAULT_ICON_HEIGHT = 80;//see below
				Icon icon = IconManager.getInstance().getIcon(catalogPieceOfFurniture.getIcon(), iconHeightPx, null);
				if (icon instanceof ImageIcon) {
					imageView.setImageBitmap(((Bitmap) ((ImageIcon) icon).getImage().getDelegate()));
				}

			} else {
				imageView = (FurnitureImageView) convertView;
				if (imageView.getCatalogPieceOfFurniture() != catalogPieceOfFurniture) {
					imageView.setCatalogPieceOfFurniture(catalogPieceOfFurniture);
					Icon icon = IconManager.getInstance().getIcon(catalogPieceOfFurniture.getIcon(), iconHeightPx, null);
					if (icon instanceof ImageIcon) {
						imageView.setImageBitmap(((Bitmap) ((ImageIcon) icon).getImage().getDelegate()));
					}
				}
			}

			return imageView;
		}
	}

	class FurnitureImageView extends ImageView {
		private CatalogPieceOfFurniture catalogPieceOfFurniture;

		public FurnitureImageView(Context c, CatalogPieceOfFurniture catalogPieceOfFurniture) {
			super(c);
			this.catalogPieceOfFurniture = catalogPieceOfFurniture;
		}

		public CatalogPieceOfFurniture getCatalogPieceOfFurniture() {
			return catalogPieceOfFurniture;
		}

		public void setCatalogPieceOfFurniture(CatalogPieceOfFurniture catalogPieceOfFurniture) {
			this.catalogPieceOfFurniture = catalogPieceOfFurniture;
		}

		public void onDraw(Canvas canvas) {
			super.onDraw(canvas);
			mTextPaint.setTextSize(16);
			mTextPaint.setFakeBoldText(true);
			// note +5 because icon is smaller than grid size
			canvas.drawText(catalogPieceOfFurniture.getName(), 10, iconHeightPx + 5, mTextPaint);
		}
	}

	private FurnitureCatalogListModel catalogListModel;

	private FurnitureCatalog catalog;
	private UserPreferences preferences;
	private FurnitureCatalogController controller;


	//private ListSelectionListener listSelectionListener;
	private JLabel                categoryFilterLabel;
	private JComboBox             categoryFilterComboBox;
	//private JLabel                searchLabel;
	private JTextField            searchTextField;
	//private JList                 catalogFurnitureList;

	private FurnitureCategory dummyAllCategory =  new FurnitureCategory("All");//TODO: localize this word!

	/**
	 * Creates a panel that displays <code>catalog</code> furniture in a list with a filter combo box
	 * and a search field.
	 */
	public void init(FurnitureCatalog catalog,
									 UserPreferences preferences,
									 FurnitureCatalogController controller) {
		initialized = true;

		this.catalog = catalog;
		this.preferences = preferences;
		this.controller = controller;

		// moved to on create view
		//createComponents(catalog, preferences, controller);
		//setMnemonics(preferences);
		//layoutComponents();
	}

	/**
	 * Creates the components displayed by this panel.
	 */
	private void createComponents(FurnitureCatalog catalog,
								  final UserPreferences preferences,
								  final FurnitureCatalogController controller) {
		catalogListModel = new FurnitureCatalogListModel(catalog);
/*		this.catalogFurnitureList = new JList(catalogListModel) {
			private CatalogItemToolTip toolTip = new CatalogItemToolTip(false, preferences);
			private boolean mousePressed;
			private boolean firstScroll;

			{
				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent ev) {
						firstScroll = true;
						mousePressed = true;
					}

					@Override
					public void mouseReleased(MouseEvent ev) {
						mousePressed = false;
					}
				});
			}

			@Override
			public JToolTip createToolTip() {
				if (this.toolTip.isTipTextComplete()) {
					// Use toolTip object only for its text returned in getToolTipText
					return super.createToolTip();
				} else {
					this.toolTip.setComponent(this);
					return this.toolTip;
				}
			}

			@Override
			public String getToolTipText(MouseEvent ev) {
				// Return a tooltip for furniture pieces described in the list.
				int index = locationToIndex(ev.getPoint());
				if (index != -1) {
					this.toolTip.setCatalogItem((CatalogPieceOfFurniture)getModel().getElementAt(index));
					return this.toolTip.getTipText();
				} else {
					return null;
				}
			}

			@Override
			public void scrollRectToVisible(Rectangle rectangle) {
				if (!this.mousePressed
						|| this.firstScroll) {
					// During a drag and drop, let's accept only the first viewport adjustment provoked by UI
					this.firstScroll = false;
					super.scrollRectToVisible(rectangle);
				}
			}
		};
		this.catalogFurnitureList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.catalogFurnitureList.setCellRenderer(new CatalogCellRenderer());
		this.catalogFurnitureList.setAutoscrolls(false);
		if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
			this.catalogFurnitureList.setDragEnabled(true);
		}
		this.catalogFurnitureList.setTransferHandler(null);
		// Remove Select all action
		this.catalogFurnitureList.getActionMap().getParent().remove("selectAll");
		addDragListener(this.catalogFurnitureList);
		addMouseListeners(this.catalogFurnitureList, controller);
*/
/*		catalogListModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void intervalAdded(ListDataEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void intervalRemoved(ListDataEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}
		});*/
/*		this.catalogFurnitureList.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void ancestorMoved(AncestorEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void ancestorRemoved(AncestorEvent ev) {
			}
		});*/
/*		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}
		});*/

		updateListSelectedFurniture(catalog, controller);
		addSelectionListeners(catalog, controller);

		this.categoryFilterLabel = new JLabel(getActivity(), SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.FurnitureCatalogListPanel.class, "categoryFilterLabel.text"));
		ArrayList<FurnitureCategory> categories = new ArrayList<FurnitureCategory>();
		categories.add(dummyAllCategory);//PJ blank was not clear
		categories.addAll(catalog.getCategories());
		this.categoryFilterComboBox = new JComboBox(getActivity(), new DefaultComboBoxModel(categories));
		/*{
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(60, super.getMinimumSize().height);
			}
		};
		this.categoryFilterComboBox.setMaximumRowCount(20);*/

		this.categoryFilterComboBox.setAdapter(new ArrayAdapter<FurnitureCategory>(getActivity(), android.R.layout.simple_list_item_1, categories) {
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				return getDropDownView(position, convertView, parent);
			}
			@Override
			public View getDropDownView (int position, View convertView, ViewGroup parent) {
				TextView ret = new TextView(getContext());
				ret.setText(((FurnitureCategory)categoryFilterComboBox.getItemAtPosition(position)).getName());
				ret.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
				return ret;
			}
		});
		/*this.categoryFilterComboBox.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value,
														  int index, boolean isSelected, boolean cellHasFocus) {
				if (value == null) {
					return super.getListCellRendererComponent(list,
							preferences.getLocalizedString(com.eteks.sweethome3d.android_props.FurnitureCatalogListPanel.class, "categoryFilterComboBox.noCategory"),
							index, isSelected, cellHasFocus);
				} else {
					return super.getListCellRendererComponent(list,
							((FurnitureCategory)value).getName(), index, isSelected, cellHasFocus);
				}
			}
		});*/
		this.categoryFilterComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				if(categoryFilterComboBox.getSelectedItem() == dummyAllCategory) {
					catalogListModel.setFilterCategory(null);
				} else {
					catalogListModel.setFilterCategory((FurnitureCategory) categoryFilterComboBox.getSelectedItem());
				}
				//catalogFurnitureList.clearSelection();
			}
		});

		//PJPJ moved to hint this.searchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences, FurnitureCatalogListPanel.class, "searchLabel.text"));

		this.searchTextField = new JTextField(getActivity(), 5);
		searchTextField.setHint(R.string.search_hint);
		searchTextField.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
				System.out.println("catalogListModel.setFilterText " +searchTextField.getText() );
				catalogListModel.setFilterText(searchTextField.getText().toString());
			}
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
			public void afterTextChanged(Editable arg0) {
				((Renovations3DActivity)getActivity()).getTutorial().actionComplete(Tutorial.TutorialAction.SEARCH_DONE, arg0.toString());
			}
		});
		/*this.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent ev) {
				Object selectedValue = catalogFurnitureList.getSelectedValue();
				catalogListModel.setFilterText(searchTextField.getText());
				catalogFurnitureList.clearSelection();
				catalogFurnitureList.setSelectedValue(selectedValue, true);

				if (catalogListModel.getSize() == 1) {
					catalogFurnitureList.setSelectedIndex(0);
				}
			}

			public void insertUpdate(DocumentEvent ev) {
				changedUpdate(ev);
			}

			public void removeUpdate(DocumentEvent ev) {
				changedUpdate(ev);
			}
		});
		this.searchTextField.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "deleteContent");
		this.searchTextField.getActionMap().put("deleteContent", new AbstractAction() {
			public void actionPerformed(ActionEvent ev) {
				searchTextField.setText("");
			}
		});*/


		PreferencesChangeListener preferencesChangeListener = new PreferencesChangeListener(this);
		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, preferencesChangeListener);
		catalog.addFurnitureListener(preferencesChangeListener);
	}

	/**
	 * Language and catalog listener bound to this component with a weak reference to avoid
	 * strong link between preferences and this component.
	 */
	private static class PreferencesChangeListener implements PropertyChangeListener, CollectionListener<CatalogPieceOfFurniture> {
		private final WeakReference<FurnitureCatalogListPanel> furnitureCatalogPanel;

		public PreferencesChangeListener(FurnitureCatalogListPanel furnitureCatalogPanel) {
			this.furnitureCatalogPanel = new WeakReference<FurnitureCatalogListPanel>(furnitureCatalogPanel);
		}

		public void propertyChange(PropertyChangeEvent ev) {
			// If panel was garbage collected, remove this listener from preferences
			FurnitureCatalogListPanel furnitureCatalogPanel = this.furnitureCatalogPanel.get();
			UserPreferences preferences = (UserPreferences) ev.getSource();
			if (furnitureCatalogPanel == null) {
				preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
			} else {
			furnitureCatalogPanel.categoryFilterLabel.setText(SwingTools.getLocalizedLabelText(preferences,
					com.eteks.sweethome3d.android_props.FurnitureCatalogListPanel.class, "categoryFilterLabel.text"));
			/*furnitureCatalogPanel.searchLabel.setText(SwingTools.getLocalizedLabelText(preferences,
					FurnitureCatalogListPanel.class, "searchLabel.text"));*/

				// Categories listed in combo box are updated through collectionChanged
			}
		}

		public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
			// If panel was garbage collected, remove this listener from catalog
			FurnitureCatalogListPanel furnitureCatalogPanel = this.furnitureCatalogPanel.get();
			FurnitureCatalog catalog = (FurnitureCatalog) ev.getSource();
			if (furnitureCatalogPanel == null) {
				catalog.removeFurnitureListener(this);
			} else {
/*			DefaultComboBoxModel model =
					(DefaultComboBoxModel)furnitureCatalogPanel.categoryFilterComboBox.getModel();
			FurnitureCategory category = ev.getItem().getCategory();
			List<FurnitureCategory> categories = catalog.getCategories();
			if (!categories.contains(category)) {
				model.removeElement(category);
				furnitureCatalogPanel.categoryFilterComboBox.setSelectedIndex(0);
			} else if (model.getIndexOf(category) == -1) {
				model.insertElementAt(category, categories.indexOf(category) + 1);
			}*/
			}
		}
	}

	/**
	 * Adds mouse listeners that will select only the piece under mouse cursor in the furniture list
	 * before the start of a drag operation, ensuring only one piece can be dragged at a time.
	 */
/*	private void addDragListener(final JList catalogFurnitureList) {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			private CatalogPieceOfFurniture exportedPiece;

			@Override
			public void mousePressed(MouseEvent ev) {
				this.exportedPiece = null;
				if (SwingUtilities.isLeftMouseButton(ev)
						&& catalogFurnitureList.getSelectedValue() != null
						&& catalogFurnitureList.getTransferHandler() != null) {
					int index = catalogFurnitureList.locationToIndex(ev.getPoint());
					if (index != -1) {
						this.exportedPiece = (CatalogPieceOfFurniture)catalogFurnitureList.getModel().getElementAt(index);
					}
				}
			}

			public void mouseDragged(MouseEvent ev) {
				if (this.exportedPiece != null) {
					if (catalogFurnitureList.getSelectedIndices().length > 1) {
						catalogFurnitureList.clearSelection();
						catalogFurnitureList.setSelectedValue(this.exportedPiece, false);
					}
					if (!OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
						catalogFurnitureList.getTransferHandler().exportAsDrag(catalogFurnitureList, ev, DnDConstants.ACTION_COPY);
					}
					this.exportedPiece = null;
				}
			}
		};

		catalogFurnitureList.addMouseListener(mouseListener);
		catalogFurnitureList.addMouseMotionListener(mouseListener);
	}*/

	/**
	 * Adds mouse listeners to the furniture list to modify selected furniture
	 * and manage links in piece information.
	 */
/*	private void addMouseListeners(final JList catalogFurnitureList,
								   final FurnitureCatalogController controller) {
		final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
		MouseInputAdapter mouseListener = new MouseInputAdapter () {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (SwingUtilities.isLeftMouseButton(ev)) {
					if (ev.getClickCount() == 2) {
						int clickedPieceIndex = catalogFurnitureList.locationToIndex(ev.getPoint());
						if (clickedPieceIndex != -1) {
							controller.modifySelectedFurniture();
						}
					} else {
						URL url = getURLAt(ev.getPoint(), catalogFurnitureList);
						if (url != null) {
							SwingTools.showDocumentInBrowser(url);
						}
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent ev) {
				final URL url = getURLAt(ev.getPoint(), catalogFurnitureList);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (url != null) {
							setCursor(handCursor);
						} else {
							setCursor(Cursor.getDefaultCursor());
						}
					}
				});
			}

			private URL getURLAt(Point point, JList list) {
				int pieceIndex = list.locationToIndex(point);
				if (pieceIndex != -1) {
					CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)list.getModel().getElementAt(pieceIndex);
					String information = piece.getInformation();
					if (information != null) {
						JComponent rendererComponent = (JComponent)list.getCellRenderer().
								getListCellRendererComponent(list, piece, pieceIndex, list.isSelectedIndex(pieceIndex), false);
						for (JEditorPane pane : SwingTools.findChildren(rendererComponent, JEditorPane.class)) {
							Rectangle cellBounds = list.getCellBounds(pieceIndex, pieceIndex);
							point.x -= cellBounds.x;
							point.y -= cellBounds.y + pane.getY();
							if (point.x > 0 && point.y > 0) {
								// Search in information pane if point is over a HTML link
								int position = pane.viewToModel(point);
								if (position > 1
										&& pane.getDocument() instanceof HTMLDocument) {
									HTMLDocument hdoc = (HTMLDocument)pane.getDocument();
									Element element = hdoc.getCharacterElement(position);
									AttributeSet a = element.getAttributes();
									AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
									if (anchor != null) {
										String href = (String)anchor.getAttribute(HTML.Attribute.HREF);
										if (href != null) {
											try {
												return new URL(href);
											} catch (MalformedURLException ex) {
												// Ignore malformed URL
											}
										}
									}
								}
							}
						}
					}
				}
				return null;
			}
		};
		catalogFurnitureList.addMouseListener(mouseListener);
		catalogFurnitureList.addMouseMotionListener(mouseListener);
	}*/

	/**
	 * Sets components mnemonics and label / component associations.
	 */
	private void setMnemonics(UserPreferences preferences) {
	}

	/**
	 * Layouts the components displayed by this panel.
	 */
	private void layoutComponents() {
		// First row
		swapOut(this.categoryFilterLabel, R.id.furniture_cat_categoryLabel);
		swapOut(this.categoryFilterComboBox, R.id.furniture_cat_category);
    // Second row
		//PJ label made into hint add(this.searchLabel
		swapOut(this.searchTextField, R.id.furniture_cat_search);
    // Last row
		//PJ main grid just left alone
	}

	protected void swapOut(View newView, int placeHolderId) {
		View placeHolder = rootView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		replaceView(placeHolder, newView);
	}


	public static ViewGroup getParent(View view) {
		return (ViewGroup)view.getParent();
	}

	public static void removeViewFromParent(View view) {
		ViewGroup parent = getParent(view);
		if(parent != null) {
			parent.removeView(view);
		}
	}

	public static void replaceView(View currentView, View newView) {
		ViewGroup parent = getParent(currentView);
		if(parent == null) {
			return;
		}
		final int index = parent.indexOfChild(currentView);
		removeViewFromParent(currentView);
		removeViewFromParent(newView);
		parent.addView(newView, index);
	}

	/**
	 * Computes furniture list visible row count to ensure its horizontal scrollbar
	 * won't be seen.
	 */
/*	private void spreadFurnitureIconsAlongListWidth() {
		ListModel model = this.catalogFurnitureList.getModel();
		int size = model.getSize();
		int extentWidth = ((JViewport)this.catalogFurnitureList.getParent()).getExtentSize().width;
		DefaultListCellRenderer cellRenderer = this.catalogFurnitureList.getCellRenderer();
		// Search max width and height
		int maxCellWidth = 1;
		int maxCellHeight = 0;
		for (int i = 0; i < size; i++) {
			Dimension cellPreferredSize = cellRenderer.getListCellRendererComponent(this.catalogFurnitureList, model.getElementAt(i),
					i, this.catalogFurnitureList.isSelectedIndex(i), false).getPreferredSize();
			maxCellWidth = Math.max(maxCellWidth, cellPreferredSize.width);
			maxCellHeight = Math.max(maxCellHeight, cellPreferredSize.height);
		}
		// Compute a fixed cell width that will spread
		int visibleItemsPerRow = Math.max(1, extentWidth / maxCellWidth);
		this.catalogFurnitureList.setVisibleRowCount(size % visibleItemsPerRow == 0
				? size / visibleItemsPerRow
				: size / visibleItemsPerRow + 1);
		this.catalogFurnitureList.setFixedCellWidth(maxCellWidth + (extentWidth % maxCellWidth) / visibleItemsPerRow);
		// Set also cell height otherwise first calls to repaint done by icon manager won't repaint it
		// because the list have a null size at the beginning
		this.catalogFurnitureList.setFixedCellHeight(maxCellHeight);
	}*/

	/**
	 * Adds the listeners that manage selection synchronization in this tree.
	 */
	private void addSelectionListeners(final FurnitureCatalog catalog,
									   final FurnitureCatalogController controller) {
		final SelectionListener modelSelectionListener = new SelectionListener() {
			public void selectionChanged(SelectionEvent selectionEvent) {
				updateListSelectedFurniture(catalog, controller);
			}
		};
/*		this.listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				// Updates selected furniture in catalog from selected nodes in tree.
				controller.removeSelectionListener(modelSelectionListener);
				controller.setSelectedFurniture(getSelectedFurniture());
				controller.addSelectionListener(modelSelectionListener);
			}
		};*/

		controller.addSelectionListener(modelSelectionListener);
//		this.catalogFurnitureList.getSelectionModel().addListSelectionListener(this.listSelectionListener);
	}

	/**
	 * Updates selected items in list from <code>controller</code> selected furniture.
	 */
	private void updateListSelectedFurniture(FurnitureCatalog catalog,
											 FurnitureCatalogController controller) {
/*		if (this.listSelectionListener != null) {
			this.catalogFurnitureList.getSelectionModel().removeListSelectionListener(this.listSelectionListener);
		}

		this.catalogFurnitureList.clearSelection();
		List<CatalogPieceOfFurniture> selectedFurniture = controller.getSelectedFurniture();
		if (selectedFurniture.size() > 0) {
			ListModel model = this.catalogFurnitureList.getModel();
			List<Integer> selectedIndices = new ArrayList<Integer>();
			for (CatalogPieceOfFurniture piece : selectedFurniture) {
				for (int i = 0, n = model.getSize(); i < n; i++) {
					if (piece == model.getElementAt(i)) {
						selectedIndices.add(i);
						break;
					}
				}
			}
			if (selectedIndices.size() > 0) {
				int [] indices = new int [selectedIndices.size()];
				for (int i = 0; i < indices.length; i++) {
					indices [i] = selectedIndices.get(i);
				}
				this.catalogFurnitureList.setSelectedIndices(indices);
				this.catalogFurnitureList.ensureIndexIsVisible(indices [0]);
			}
		}

		if (this.listSelectionListener != null) {
			this.catalogFurnitureList.getSelectionModel().addListSelectionListener(this.listSelectionListener);
		}*/
	}

	/**
	 * Returns the selected furniture in list.
	 */
/*	private List<CatalogPieceOfFurniture> getSelectedFurniture() {
		Object [] selectedValues = this.catalogFurnitureList.getSelectedValues();
		CatalogPieceOfFurniture [] selectedFurniture = new CatalogPieceOfFurniture [selectedValues.length];
		System.arraycopy(selectedValues, 0, selectedFurniture, 0, selectedValues.length);
		return Arrays.asList(selectedFurniture);
	}*/

	/**
	 * Sets the transfer handler of the list displayed by this panel.
	 */
/*	@Override
	public void setTransferHandler(TransferHandler handler) {
		this.catalogFurnitureList.setTransferHandler(handler);
	}*/

	/**
	 * Returns the transfer handler of the list displayed by this panel.
	 */
/*	@Override
	public TransferHandler getTransferHandler() {
		return this.catalogFurnitureList.getTransferHandler();
	}*/

	/**
	 * Sets the tooltippopup menu of the list displayed by this panel.
	 */
/*	@Override
	public void setComponentPopupMenu(JPopupMenu tooltippopup) {
		this.catalogFurnitureList.setComponentPopupMenu(tooltippopup);
	}*/

	/**
	 * Returns the tooltippopup menu of the list displayed by this panel.
	 */
/*	@Override
	public JPopupMenu getComponentPopupMenu() {
		return this.catalogFurnitureList.getComponentPopupMenu();
	}*/

  /**
   * Cell renderer for the furniture list.
   */
/*private static class CatalogCellRenderer extends JComponent implements DefaultListCellRenderer {
	private static final int DEFAULT_ICON_HEIGHT = Math.round(48 * SwingTools.getResolutionScale());
	private Font                    defaultFont;
	private Font                    modifiablePieceFont;
	private DefaultListCellRenderer nameLabel;
	private JEditorPane             informationPane;

	public CatalogCellRenderer() {
		setLayout(null);
		this.nameLabel = new DefaultListCellRenderer() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(DEFAULT_ICON_HEIGHT * 3 / 2 + 5, super.getPreferredSize().height);
			}
		};
		this.nameLabel.setHorizontalTextPosition(JLabel.CENTER);
		this.nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
		this.nameLabel.setHorizontalAlignment(JLabel.CENTER);
		this.nameLabel.setText("-");
		this.nameLabel.setIcon(IconManager.getInstance().getWaitIcon(DEFAULT_ICON_HEIGHT));
		this.defaultFont = UIManager.getFont("ToolTip.font");
		this.modifiablePieceFont = new Font(this.defaultFont.getFontName(), Font.ITALIC, this.defaultFont.getSize());
		this.nameLabel.setFont(this.defaultFont);

		this.informationPane = new JEditorPane("text/html", "-");
		this.informationPane.setOpaque(false);
		this.informationPane.setEditable(false);
		String bodyRule = "body { font-family: " + this.defaultFont.getFamily() + "; "
				+ "font-size: " + this.defaultFont.getSize() + "pt; "
				+ "text-align: center; }";
		((HTMLDocument)this.informationPane.getDocument()).getStyleSheet().addRule(bodyRule);

		add(this.nameLabel);
		add(this.informationPane);
	}

	public Component getListCellRendererComponent(JList list,
												  Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus) {
		CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)value;
		// Configure name label with its icon, background and focus colors
		this.nameLabel.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		this.nameLabel.setText(" " + piece.getName() + " ");
		this.nameLabel.setIcon(getLabelIcon(list, piece.getIcon()));
		this.nameLabel.setFont(piece.isModifiable()
				? this.modifiablePieceFont : this.defaultFont);

		this.informationPane.setText(piece.getInformation());
		return this;
	}

	@Override
	public void doLayout() {
		Dimension namePreferredSize = this.nameLabel.getPreferredSize();
		this.nameLabel.setSize(getWidth(), namePreferredSize.height);
		this.informationPane.setBounds(0, namePreferredSize.height,
				getWidth(), getHeight() - namePreferredSize.height);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = this.nameLabel.getPreferredSize();
		preferredSize.height += this.informationPane.getPreferredSize().height + 2;
		return preferredSize;
	}*/

	/**
	 * The following methods are overridden for performance reasons.
	 */
/*	@Override
	public void revalidate() {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void repaint(Rectangle r) {
	}

	@Override
	public void repaint() {
	}

	private Icon getLabelIcon(JList list, Content content) {
		return IconManager.getInstance().getIcon(content, DEFAULT_ICON_HEIGHT, list);
	}

	@Override
	protected void paintChildren(Graphics g) {
		// Force text anti aliasing on texts
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintChildren(g);
	}
}*/

	/**
	 * List model adaptor to CatalogPieceOfFurniture instances of catalog.
	 */
	private class FurnitureCatalogListModel {//extends AbstractListModel {
		private FurnitureCatalog catalog;
		private List<CatalogPieceOfFurniture> furniture;
		private FurnitureCategory filterCategory;
		private String filterText;

		public FurnitureCatalogListModel(FurnitureCatalog catalog) {
			this.catalog = catalog;
			this.filterText = "";
			catalog.addFurnitureListener(new FurnitureCatalogListener(this));
		}

		public void setFilterCategory(FurnitureCategory filterCategory) {
			this.filterCategory = filterCategory;
			resetFurnitureList();
		}

		public void setFilterText(String filterText) {
			this.filterText = filterText;
			resetFurnitureList();
		}

		public Object getElementAt(int index) {
			checkFurnitureList();
			return this.furniture.get(index);
		}

		public int getSize() {
			checkFurnitureList();
			return this.furniture.size();
		}

		private void resetFurnitureList() {
			if (this.furniture != null) {
				this.furniture = null;
				//PJ added now to force an update, before the fireupdate refreshs the list
				checkFurnitureList();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						//fireContentsChanged(this, -1, -1);
						// called in cases like where language changes or perhaps catalog imported
						gridView.setAdapter(new ImageAdapter(FurnitureCatalogListPanel.this.getContext()));
						FurnitureCatalogListPanel.this.getView().postInvalidate();
					}
				});
			}
		}

		private void checkFurnitureList() {
			if (this.furniture == null) {
				this.furniture = new ArrayList<CatalogPieceOfFurniture>();
				this.furniture.clear();
				for (FurnitureCategory category : this.catalog.getCategories()) {
					for (CatalogPieceOfFurniture piece : category.getFurniture()) {
						/*if ((this.filterCategory == null
								|| piece.getCategory().equals(this.filterCategory))
								&& piece.matchesFilter(this.filterText))
						{
							furniture.add(piece);
						}*/

						//the java.text.Collator systems differs on Android compared with jdk
						// and it hits everything, so instead a dummy lowercase contains is us
						//FIXME: this is really poor searching

						if ((this.filterCategory == null
								|| piece.getCategory().equals(this.filterCategory))
								&&
								(this.filterText == null || this.filterText == "" ||
								piece.getName().toLowerCase().contains(this.filterText.toLowerCase()))) {
							furniture.add(piece);
						}

					}
				}
				Collections.sort(this.furniture);
			}
		}
	}

	/**
	 * Catalog furniture listener bound to this list model with a weak reference to avoid
	 * strong link between catalog and this list.
	 */
	private static class FurnitureCatalogListener implements CollectionListener<CatalogPieceOfFurniture> {
		private WeakReference<FurnitureCatalogListModel> listModel;

		public FurnitureCatalogListener(FurnitureCatalogListModel catalogListModel) {
			this.listModel = new WeakReference<FurnitureCatalogListModel>(catalogListModel);
		}

		public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
			// If catalog list model was garbage collected, remove this listener from catalog
			FurnitureCatalogListModel listModel = this.listModel.get();
			FurnitureCatalog catalog = (FurnitureCatalog) ev.getSource();
			if (listModel == null) {
				catalog.removeFurnitureListener(this);
			} else {
				listModel.resetFurnitureList();
			}
		}
	}
}

