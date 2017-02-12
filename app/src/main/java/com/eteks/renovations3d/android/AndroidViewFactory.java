

/*
 * SwingViewFactory.java 28 oct. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import android.widget.Toast;

import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.CompassController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.HelpController;
import com.eteks.sweethome3d.viewcontroller.HelpView;
import com.eteks.sweethome3d.viewcontroller.Home3DAttributesController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeController3D;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.HomeView;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardController;
import com.eteks.sweethome3d.viewcontroller.ImportedFurnitureWizardStepsView;
import com.eteks.sweethome3d.viewcontroller.ImportedTextureWizardController;
import com.eteks.sweethome3d.viewcontroller.LabelController;
import com.eteks.sweethome3d.viewcontroller.LevelController;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.ObserverCameraController;
import com.eteks.sweethome3d.viewcontroller.PageSetupController;
import com.eteks.sweethome3d.viewcontroller.PhotoController;
import com.eteks.sweethome3d.viewcontroller.PhotosController;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import com.eteks.sweethome3d.viewcontroller.PolylineController;
import com.eteks.sweethome3d.viewcontroller.PrintPreviewController;
import com.eteks.sweethome3d.viewcontroller.RoomController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceView;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskController;
import com.eteks.sweethome3d.viewcontroller.ThreadedTaskView;
import com.eteks.sweethome3d.viewcontroller.UserPreferencesController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.eteks.sweethome3d.viewcontroller.VideoController;
import com.eteks.sweethome3d.viewcontroller.ViewFactory;
import com.eteks.sweethome3d.viewcontroller.WallController;
import com.eteks.sweethome3d.viewcontroller.WizardController;
import com.eteks.renovations3d.SweetHomeAVRActivity;

import java.security.AccessControlException;

/**
 * View factory that instantiates the Swing components of this package.
 *
 * @author Emmanuel Puybaret
 */
public class AndroidViewFactory implements ViewFactory // could extend ViewFactoryAdapter
{
	private SweetHomeAVRActivity activity;

	private AndroidDialogView currentDialog = null;
	private DialogView dummy;

	public AndroidViewFactory(SweetHomeAVRActivity activity)
	{
		this.activity = activity;
		//AndroidTools.updateComponentDefaults();
		dummy = new DialogView(){
			@Override
			public void displayView(View view){	}
		};
	}

	/**
	 * Returns a new view that displays furniture <code>catalog</code>.
	 */
	public View createFurnitureCatalogView(FurnitureCatalog catalog,
										   UserPreferences preferences,
										   FurnitureCatalogController furnitureCatalogController)
	{
//		if (preferences == null || preferences.isFurnitureCatalogViewedInTree())
		{
//			return null;//return new FurnitureCatalogTree(catalog, preferences, furnitureCatalogController);
		}
//		else
		{
			FurnitureCatalogListPanel furnitureCatalogListPanel = new FurnitureCatalogListPanel();
			furnitureCatalogListPanel.init(catalog, preferences, furnitureCatalogController);
			return furnitureCatalogListPanel;
		}
	}

	/**
	 * Returns a new table that displays <code>home</code> furniture.
	 */
	public View createFurnitureView(Home home, UserPreferences preferences,
									FurnitureController furnitureController)
	{
		FurnitureTable furnitureTable = new FurnitureTable();
		furnitureTable.init(home, preferences, furnitureController);
		return furnitureTable;
	}

	/**
	 * Returns a new view that displays <code>home</code> plan.
	 */
	public PlanView createPlanView(Home home, UserPreferences preferences,
								   PlanController planController)
	{

		MultipleLevelsPlanPanel multipleLevelsPlanPanel = new MultipleLevelsPlanPanel();
		multipleLevelsPlanPanel.init(home, preferences, planController);
		return multipleLevelsPlanPanel;
	}

	/**
	 * Returns a new view that displays <code>home</code> in 3D.
	 */
	public View createView3D(Home home, UserPreferences preferences,
							 HomeController3D homeController3D)
	{
		try
		{
			if (!Boolean.getBoolean("com.eteks.sweethome3d.no3D"))
			{
				// nope must ask controller for it, must have controller by now
				HomeComponent3D homeComponent3D = new HomeComponent3D();
				homeComponent3D.init(home, preferences, homeController3D);
				return homeComponent3D;
			}
		}
		catch (AccessControlException ex)
		{
			// If com.eteks.sweethome3d.no3D property can't be read,
			// security manager won't allow to access to Java 3D DLLs required by HomeComponent3D class too
		}
		return null;
	}

	/**
	 * Returns a new view that displays <code>home</code> and its sub views.
	 */
	public HomeView createHomeView(Home home, UserPreferences preferences,
								   HomeController homeController)
	{
		return new HomePane(home, preferences, homeController, activity);
	}

	/**
	 * Returns a new view that displays a wizard.
	 */
	public DialogView createWizardView(UserPreferences preferences,
									   WizardController wizardController)
	{
		throw new UnsupportedOperationException();//return new WizardPane(preferences, wizardController);
	}

	/**
	 * Returns a new view that displays the different steps that helps user to choose a background image.
	 */
	public View createBackgroundImageWizardStepsView(BackgroundImage backgroundImage,
													   UserPreferences preferences,
													   BackgroundImageWizardController backgroundImageWizardController)
	{
		throw new UnsupportedOperationException();//return new BackgroundImageWizardStepsPanel(backgroundImage, preferences,
		//		backgroundImageWizardController);
	}

	/**
	 * Returns a new view that displays the different steps that helps user to import furniture.
	 */
	public ImportedFurnitureWizardStepsView createImportedFurnitureWizardStepsView(
			CatalogPieceOfFurniture piece,
			String modelName, boolean importHomePiece,
			UserPreferences preferences,
			ImportedFurnitureWizardController importedFurnitureWizardController)
	{
		throw new UnsupportedOperationException();//return new ImportedFurnitureWizardStepsPanel(piece, modelName, importHomePiece,
		//		preferences, importedFurnitureWizardController);
	}

	/**
	 * Returns a new view that displays the different steps that helps the user to import a texture.
	 */
	public View createImportedTextureWizardStepsView(
			CatalogTexture texture, String textureName,
			UserPreferences preferences,
			ImportedTextureWizardController importedTextureWizardController)
	{
		throw new UnsupportedOperationException();//return new ImportedTextureWizardStepsPanel(texture, textureName, preferences,
		//		importedTextureWizardController);
	}

	/**
	 * Returns a new view that displays message for a threaded task.
	 */
	public ThreadedTaskView createThreadedTaskView(String taskMessage,
												   UserPreferences preferences,
												   ThreadedTaskController threadedTaskController)
	{
		return new ThreadedTaskPanel(taskMessage, preferences, threadedTaskController, activity);
	}

	/**
	 * Returns a new view that edits user preferences.
	 */
	public DialogView createUserPreferencesView(UserPreferences preferences,
												UserPreferencesController userPreferencesController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new UserPreferencesPanel(preferences, userPreferencesController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits level values.
	 */
	public DialogView createLevelView(UserPreferences preferences, LevelController levelController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new LevelPanel(preferences, levelController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits furniture values.
	 */
	public DialogView createHomeFurnitureView(UserPreferences preferences,
											  HomeFurnitureController homeFurnitureController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new HomeFurniturePanel(preferences, homeFurnitureController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits wall values.
	 */
	public DialogView createWallView(UserPreferences preferences,
									 WallController wallController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new WallPanel(preferences, wallController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits room values.
	 */
	public DialogView createRoomView(UserPreferences preferences,
									 RoomController roomController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new RoomPanel(preferences, roomController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits polyline values.
	 */
	public DialogView createPolylineView(UserPreferences preferences,
										 PolylineController polylineController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new PolylinePanel(preferences, polylineController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits label values.
	 */
	public DialogView createLabelView(boolean modification,
									  UserPreferences preferences,
									  LabelController labelController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new LabelPanel(modification, preferences, labelController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits compass values.
	 */
	public DialogView createCompassView(UserPreferences preferences,
										CompassController compassController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new CompassPanel(preferences, compassController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits 3D attributes.
	 */
	public DialogView createHome3DAttributesView(UserPreferences preferences,
												 Home3DAttributesController home3DAttributesController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new Home3DAttributesPanel(preferences, home3DAttributesController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits observer camera values.
	 */
	public DialogView createObserverCameraView(UserPreferences preferences,
											   ObserverCameraController observerCameraController)
	{
		if(currentDialog == null || !currentDialog.isShowing())
		{
			currentDialog = new ObserverCameraPanel(preferences, observerCameraController, activity);
			return currentDialog;
		}
		else
		{
			return dummy;
		}
	}

	/**
	 * Returns a new view that edits the texture of the given controller.
	 */
	public TextureChoiceView createTextureChoiceView(UserPreferences preferences,
													 TextureChoiceController textureChoiceController)
	{
		return new TextureChoiceComponent(preferences, textureChoiceController, activity);
	}

	/**
	 * Returns a new view that edits the baseboard of its controller.
	 */
	public View createBaseboardChoiceView(UserPreferences preferences,
										  BaseboardChoiceController baseboardChoiceController)
	{
		throw new UnsupportedOperationException();//return new BaseboardChoiceComponent(preferences, baseboardChoiceController, activity);
	}

	/**
	 * Returns a new view that edits the materials of its controller.
	 */
	public View createModelMaterialsView(UserPreferences preferences,
										 ModelMaterialsController controller)
	{
		throw new UnsupportedOperationException();//return new ModelMaterialsComponent(preferences, controller);
	}

	/**
	 * Creates a new view that edits page setup.
	 */
	public DialogView createPageSetupView(UserPreferences preferences,
										  PageSetupController pageSetupController)
	{
		throw new UnsupportedOperationException();//return new PageSetupPanel(preferences, pageSetupController);
	}

	/**
	 * Returns a new view that displays <code>home</code> print preview.
	 */
	public DialogView createPrintPreviewView(Home home,
											 UserPreferences preferences,
											 HomeController homeController,
											 PrintPreviewController printPreviewController)
	{
		throw new UnsupportedOperationException();//return new PrintPreviewPanel(home, preferences, homeController, printPreviewController);
	}

	/**
	 * Returns a new view able to compute a photos of a home from its stored points of view.
	 */
	public DialogView createPhotosView(Home home, UserPreferences preferences,
									   PhotosController photosController)
	{
		throw new UnsupportedOperationException();//return new PhotosPanel(home, preferences, photosController);
	}

	/**
	 * Returns a new view able to create photo realistic images of the given home.
	 */
	public DialogView createPhotoView(Home home,
									  UserPreferences preferences,
									  PhotoController photoController)
	{
		throw new UnsupportedOperationException();//return new PhotoPanel(home, preferences, photoController);
	}

	/**
	 * Returns a new view able to create 3D videos of the given home.
	 */
	public DialogView createVideoView(Home home,
									  UserPreferences preferences,
									  VideoController videoController)
	{
		throw new UnsupportedOperationException();//return new VideoPanel(home, preferences, videoController);
	}

	/**
	 * Returns a new view that displays Sweet Home 3D help.
	 */
	public HelpView createHelpView(UserPreferences preferences,
								   HelpController helpController)
	{
		Toast.makeText(this.activity, "createHelpView", Toast.LENGTH_SHORT).show();
		return new HelpView(){public void displayView(){}};
		//return new HelpPane(preferences, helpController);
	}
}
