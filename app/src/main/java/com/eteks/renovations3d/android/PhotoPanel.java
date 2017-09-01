/*
 * PhotoPanel.java 5 mai 2009
 *
 * Sweet Home 3D, Copyright (c) 2009 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.sweethome3d.model.RecorderException;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.DefaultComboBoxModel;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JSpinnerDate;
import com.mindblowing.swingish.SpinnerDateModel;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Camera.Lens;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.AbstractPhotoController;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;
import com.eteks.sweethome3d.viewcontroller.PhotoController;
import com.mindblowing.renovations3d.BuildConfig;
import com.mindblowing.renovations3d.R;

import org.sunflow.system.UI;
import org.sunflow.system.UserInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javaawt.EventQueue;
import javaawt.Graphics2D;
import javaawt.Image;
import javaawt.image.BufferedImage;
import javaawt.image.ImageObserver;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.ImageIO;
import javaxswing.Icon;
import javaxswing.ImageIcon;

import static android.os.Build.VERSION_CODES.M;

/**
 * A panel to edit photo creation. 
 * @author Emmanuel Puybaret
 */
public class PhotoPanel extends AndroidDialogView implements DialogView
{
	//private enum ActionType {START_PHOTO_CREATION, STOP_PHOTO_CREATION, SAVE_PHOTO, CLOSE}

	//private static final String PHOTO_DIALOG_X_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.PhotoPanel.PhotoDialogX";
	//private static final String PHOTO_DIALOG_Y_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.PhotoPanel.PhotoDialogY";

	private static final int MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING = 30000;

	//private static final String WAIT_CARD  = "wait";
	//private static final String PHOTO_CARD = "photo";

	private final Home home;
	private final UserPreferences preferences;
	private final Object3DFactory object3dFactory;
	private final PhotoController controller;
	private TextView statusLabel;
	private ScaledImageComponent photoComponent;
	//private JLabel 					animatedWaitLabel;
	private PhotoSizeAndQualityPanel sizeAndQualityPanel;
	private JLabel dateLabel;
	private JSpinnerDate dateSpinner;
	private JLabel timeLabel;
	private JSpinnerDate timeSpinner;
	private ImageView dayNightLabel;
	private JLabel lensLabel;
	private JComboBox lensComboBox;
	private JCheckBox ceilingLightEnabledCheckBox;
	private String dialogTitle;
	//private JPanel                   photoPanel;
	//private CardLayout               photoCardLayout;
	private ExecutorService photoCreationExecutor;
	private long photoCreationStartTime;
	private JButton createButton;
	private JButton saveButton;
	private JButton shareButton;
	private JButton closeButton;

	//private static PhotoPanel        currentPhotoPanel; // Support only one photo panel opened at a time

	public PhotoPanel(Home home,
					  UserPreferences preferences,
					  PhotoController controller, Activity activity)
	{
		this(home, preferences, null, controller, activity);
	}

	public PhotoPanel(Home home,
					  UserPreferences preferences,
					  Object3DFactory object3dFactory,
					  PhotoController controller, Activity activity)
	{
		super(preferences, activity, R.layout.dialog_photopanel);
		this.home = home;
		this.preferences = preferences;
		this.object3dFactory = object3dFactory;
		this.controller = controller;
		createComponents(home, preferences, controller);
		layoutComponents();

		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, new LanguageChangeListener(this));

		Renovations3DActivity.logFireBaseLevelUp("Photo Panel Opened");
	}

	/**
	 * Creates and initializes components.
	 */
	private void createComponents(final Home home,
								  final UserPreferences preferences,
								  final PhotoController controller)
	{

		this.statusLabel = new TextView(activity);
		UI.set(new AndroidInterface(statusLabel));
		statusLabel.setText("Ready.");

		this.photoComponent = new ScaledImageComponent(null, true, activity);
		this.photoComponent.setPreferredSize(320);
		//this.photoComponent.setPreferredSize(new Dimension(getToolkit().getScreenSize().width <= 1024 ? 320 : 400, 400));

		//this.animatedWaitLabel = new JLabel(activity, new ImageIcon(com.eteks.sweethome3d.android_props.PhotoPanel.class.getResource("resources/animatedWait.gif")));

		// Create size and quality panel
		this.sizeAndQualityPanel = new PhotoSizeAndQualityPanel(home, preferences, controller, activity);
		controller.addPropertyChangeListener(AbstractPhotoController.Property.QUALITY,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent ev)
					{
						updateAdvancedComponents();
					}
				});


		// Create date and time labels and spinners bound to TIME controller property
		Date time = new Date(Camera.convertTimeToTimeZone(controller.getTime(), TimeZone.getDefault().getID()));
		this.dateLabel = new JLabel(activity, "");


		final SpinnerDateModel dateSpinnerModel = new SpinnerDateModel();
		dateSpinnerModel.setValue(time);
		this.dateSpinner = new JSpinnerDate(activity, dateSpinnerModel);
		String datePattern = ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT)).toPattern();
		if (datePattern.indexOf("yyyy") == -1)
		{
			datePattern = datePattern.replace("yy", "yyyy");
		}
		dateSpinner.setTimePattern(datePattern);
		//JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(this.dateSpinner, datePattern);
		//this.dateSpinner.setEditor(dateEditor);
		//SwingTools.addAutoSelectionOnFocusGain(dateEditor.getTextField());

		this.timeLabel = new JLabel(activity, "");

		final SpinnerDateModel timeSpinnerModel = new SpinnerDateModel();
		timeSpinnerModel.setCalendarField(11);//11 is the hour of day field, PJ not sure hwo this gets set in teh desktop version exactly
		timeSpinnerModel.setValue(time);
		this.timeSpinner = new JSpinnerDate(activity, timeSpinnerModel);
		// From http://en.wikipedia.org/wiki/12-hour_clock#Use_by_country
		String[] twelveHoursCountries = {
				"AU",  // Australia
				"BD",  // Bangladesh
				"CA",  // Canada (excluding Quebec, in French)
				"CO",  // Colombia
				"EG",  // Egypt
				"HN",  // Honduras
				"JO",  // Jordan
				"MX",  // Mexico
				"MY",  // Malaysia
				"NI",  // Nicaragua
				"NZ",  // New Zealand
				"PH",  // Philippines
				"PK",  // Pakistan
				"SA",  // Saudi Arabia
				"SV",  // El Salvador
				"US",  // United States
				"VE"}; // Venezuela
		SimpleDateFormat timeInstance;
		if ("en".equals(Locale.getDefault().getLanguage()))
		{
			if (Arrays.binarySearch(twelveHoursCountries, Locale.getDefault().getCountry()) >= 0)
			{
				timeInstance = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US); // 12 hours notation
			}
			else
			{
				timeInstance = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT, Locale.UK); // 24 hours notation
			}
		}
		else
		{
			timeInstance = (SimpleDateFormat) DateFormat.getTimeInstance(DateFormat.SHORT);
		}
		timeSpinner.setTimePattern(timeInstance.toPattern());
		//JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(this.timeSpinner, timeInstance.toPattern());
		//this.timeSpinner.setEditor(timeEditor);
		//SwingTools.addAutoSelectionOnFocusGain(timeEditor.getTextField());

		final PropertyChangeListener timeChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				Date date = new Date(Camera.convertTimeToTimeZone(controller.getTime(), TimeZone.getDefault().getID()));
				dateSpinnerModel.setValue(date);
				timeSpinnerModel.setValue(date);
			}
		};
		controller.addPropertyChangeListener(PhotoController.Property.TIME, timeChangeListener);
		final ChangeListener dateTimeChangeListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent ev)
			{
				controller.removePropertyChangeListener(PhotoController.Property.TIME, timeChangeListener);
				// Merge date and time
				GregorianCalendar dateCalendar = new GregorianCalendar();
				dateCalendar.setTime((Date) dateSpinnerModel.getValue());
				GregorianCalendar timeCalendar = new GregorianCalendar();
				timeCalendar.setTime((Date) timeSpinnerModel.getValue());
				Calendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
				utcCalendar.set(GregorianCalendar.YEAR, dateCalendar.get(GregorianCalendar.YEAR));
				utcCalendar.set(GregorianCalendar.MONTH, dateCalendar.get(GregorianCalendar.MONTH));
				utcCalendar.set(GregorianCalendar.DAY_OF_MONTH, dateCalendar.get(GregorianCalendar.DAY_OF_MONTH));
				utcCalendar.set(GregorianCalendar.HOUR_OF_DAY, timeCalendar.get(GregorianCalendar.HOUR_OF_DAY));
				utcCalendar.set(GregorianCalendar.MINUTE, timeCalendar.get(GregorianCalendar.MINUTE));
				utcCalendar.set(GregorianCalendar.SECOND, timeCalendar.get(GregorianCalendar.SECOND));
				controller.setTime(utcCalendar.getTimeInMillis());
				controller.addPropertyChangeListener(PhotoController.Property.TIME, timeChangeListener);
			}
		};
		dateSpinnerModel.addChangeListener(dateTimeChangeListener);
		timeSpinnerModel.addChangeListener(dateTimeChangeListener);

		this.dayNightLabel = new ImageView(activity);
		final ImageIcon dayIcon = SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.PhotoPanel.class.getResource("resources/day.png"));
		final ImageIcon nightIcon = SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.PhotoPanel.class.getResource("resources/night.png"));

		PropertyChangeListener dayNightListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				if (home.getCompass().getSunElevation(
						Camera.convertTimeToTimeZone(controller.getTime(), home.getCompass().getTimeZone())) > 0)
				{
					dayNightLabel.setImageBitmap(((Bitmap) dayIcon.getImage().getDelegate()));
				}
				else
				{
					dayNightLabel.setImageBitmap(((Bitmap) nightIcon.getImage().getDelegate()));
				}
			}
		};


		controller.addPropertyChangeListener(PhotoController.Property.TIME, dayNightListener);
		home.getCompass().addPropertyChangeListener(dayNightListener);
		dayNightListener.propertyChange(null);

		// Create lens label and combo box
		this.lensLabel = new JLabel(activity, "");
		this.lensComboBox = new JComboBox(activity, new DefaultComboBoxModel(Lens.values()));
		lensComboBox.setAdapter(new ArrayAdapter<Lens>(activity, android.R.layout.simple_list_item_1, Lens.values())
		{
			@Override
			public View getView(int position, View convertView, ViewGroup parent)
			{
				return getDropDownView(position, convertView, parent);
			}

			@Override
			public View getDropDownView(int position, View convertView, ViewGroup parent)
			{
				TextView ret = new TextView(getContext());
				Lens value = (Lens) lensComboBox.getItemAtPosition(position);
				String displayedValue;
				switch (value)
				{
					case NORMAL:
						displayedValue = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "lensComboBox.normalLens.text");
						break;
					case SPHERICAL:
						displayedValue = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "lensComboBox.sphericalLens.text");
						break;
					case FISHEYE:
						displayedValue = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "lensComboBox.fisheyeLens.text");
						break;
					case PINHOLE:
					default:
						displayedValue = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "lensComboBox.pinholeLens.text");
						break;
				}
				ret.setText(displayedValue);
				return ret;
			}
		});

		this.lensComboBox.setSelectedItem(controller.getLens());
		controller.addPropertyChangeListener(PhotoController.Property.LENS,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent ev)
					{
						lensComboBox.setSelectedItem(controller.getLens());
					}
				});
		this.lensComboBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ev)
			{
				Lens lens = (Lens) lensComboBox.getSelectedItem();
				controller.setLens(lens);
				if (lens == Lens.SPHERICAL)
				{
					controller.setAspectRatio(AspectRatio.RATIO_2_1);
				}
				else if (lens == Lens.FISHEYE)
				{
					controller.setAspectRatio(AspectRatio.SQUARE_RATIO);
				}
				updateRatioComponents();
			}
		});

		this.ceilingLightEnabledCheckBox = new JCheckBox(activity, "");
		this.ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
		controller.addPropertyChangeListener(AbstractPhotoController.Property.CEILING_LIGHT_COLOR,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent ev)
					{
						ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
					}
				});
		this.ceilingLightEnabledCheckBox.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ev)
			{
				controller.setCeilingLightColor(ceilingLightEnabledCheckBox.isSelected() ? 0xD0D0D0 : 0);
			}
		});

		//PJPJPJ altered to get the views view
		final HomeComponent3D view3D = (HomeComponent3D) controller.get3DView();
		controller.set3DViewAspectRatio((float) view3D.getView().getWidth() / view3D.getView().getHeight());

		this.createButton = new JButton(activity,
				SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.PhotoPanel.class, "START_PHOTO_CREATION.Name"));
		createButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				startPhotoCreation();
			}
		});
		this.saveButton = new JButton(activity,
				SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.PhotoPanel.class, "SAVE_PHOTO.Name"));
		saveButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				savePhoto(false);
			}
		});
		this.shareButton = new JButton(activity, activity.getResources().getString(R.string.share));
		shareButton.setOnClickListener(new View.OnClickListener(){public void onClick(View view){savePhoto(true);}});

		this.closeButton = new JButton(activity,
				SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.PhotoPanel.class, "CLOSE.Name"));
		closeButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				close();
			}
		});


		setComponentTexts(preferences);
		updateRatioComponents();
	}

	/**
	 * Sets the texts of the components.
	 */
	private void setComponentTexts(UserPreferences preferences)
	{
		this.dateLabel.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.PhotoPanel.class, "dateLabel.text"));
		this.timeLabel.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.PhotoPanel.class, "timeLabel.text"));
		this.lensLabel.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.PhotoPanel.class, "lensLabel.text"));
		this.ceilingLightEnabledCheckBox.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.PhotoPanel.class, "ceilingLightEnabledCheckBox.text"));
		this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "createPhoto.title");

		// Buttons text changes automatically through their action
	}


	/**
	 * Preferences property listener bound to this panel with a weak reference to avoid
	 * strong link between user preferences and this panel.
	 */
	public static class LanguageChangeListener implements PropertyChangeListener
	{
		private final WeakReference<PhotoPanel> photoPanel;

		public LanguageChangeListener(PhotoPanel photoPanel)
		{
			this.photoPanel = new WeakReference<PhotoPanel>(photoPanel);
		}

		public void propertyChange(PropertyChangeEvent ev)
		{
			// If photo panel was garbage collected, remove this listener from preferences
			PhotoPanel photoPanel = this.photoPanel.get();
			UserPreferences preferences = (UserPreferences) ev.getSource();
			if (photoPanel == null)
			{
				preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
			}
			else
			{
				photoPanel.setComponentTexts(preferences);
			}
		}
	}

	/**
	 * Layouts panel components in panel with their labels.
	 */
	private void layoutComponents()
	{

		swapOut(this.statusLabel, R.id.photopanel_statusLabel);

		swapOut(this.photoComponent, R.id.photopanel_photoComponent);
		swapOut(this.sizeAndQualityPanel, R.id.photopanel_sizeAndQualityPanel);
		swapOut(this.dateLabel, R.id.photopanel_dateLabel);
		swapOut(this.dateSpinner, R.id.photopanel_dateSpinner);
		swapOut(this.timeLabel, R.id.photopanel_timeLabel);
		swapOut(this.timeSpinner, R.id.photopanel_timeSpinner);
		swapOut(this.dayNightLabel, R.id.photopanel_dayNightLabel);
		swapOut(this.lensLabel, R.id.photopanel_lensLabel);
		swapOut(this.lensComboBox, R.id.photopanel_lensComboBox);
		swapOut(this.ceilingLightEnabledCheckBox, R.id.photopanel_ceilingLightEnabledCheckBox);

		this.setTitle(dialogTitle);
		swapOut(createButton, R.id.photopanel_createButton);
		swapOut(saveButton, R.id.photopanel_saveButton);
		swapOut(shareButton, R.id.photopanel_shareButton);
		swapOut(closeButton, R.id.photopanel_closeButton);
	}

	private void updateAdvancedComponents()
	{

		boolean highQuality = controller.getQuality() >= 2;

		//PJPJPJ not sure why this didn't always just do it
		//if (highQuality) {
		setAdvancedComponentsVisible(highQuality);
		updateRatioComponents();
		//}

	}

	private void setAdvancedComponentsVisible(boolean visible)
	{
		this.dateLabel.setEnabled(visible);
		this.dateSpinner.setEnabled(visible);
		this.timeLabel.setEnabled(visible);
		this.timeSpinner.setEnabled(visible);
		this.dayNightLabel.setEnabled(visible);
		this.lensLabel.setEnabled(visible);
		this.lensComboBox.setEnabled(visible);
		this.ceilingLightEnabledCheckBox.setEnabled(visible);
    /*this.dateLabel.setVisible(visible);
    this.dateSpinner.setVisible(visible);
    this.timeLabel.setVisible(visible);
    this.timeSpinner.setVisible(visible);
    this.dayNightLabel.setVisible(visible);
    this.lensLabel.setVisible(visible);
    this.lensComboBox.setVisible(visible);
    this.ceilingLightEnabledCheckBox.setVisible(visible);*/
	}

	/**
	 * Updates photo height.
	 */
	private void updateRatioComponents()
	{
		Lens lens = this.controller.getLens();
		boolean fixedProportions = this.lensComboBox.isEnabled()
				&& (lens == Lens.FISHEYE
				|| lens == Lens.SPHERICAL);
		this.sizeAndQualityPanel.setProportionsChoiceEnabled(!fixedProportions);
	}

	/**
	 * Displays this panel in a non modal dialog.
	 */
	public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView)
	{

		this.setOnDismissListener(new OnDismissListener()
		{
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				stopPhotoCreation(false);
			}
		});

		updateAdvancedComponents();

		// this dialog is super expensive, so prevent accidental dismisses
		this.setCanceledOnTouchOutside(false);
		this.show();

	}

	/**
	 * Creates the photo image depending on the quality requested by the user.
	 */
	private void startPhotoCreation()
	{
		int quality = this.controller.getQuality();
		if (quality >= 2)
		{
			JOptionPane.showMessageDialog(activity, activity.getResources().getString(R.string.high_quality_photo_notice),
					activity.getResources().getString(R.string.long_process_title), JOptionPane.INFORMATION_MESSAGE);
		}
		this.photoComponent.setImage(null);
		this.sizeAndQualityPanel.setEnabled(false);
		this.dateSpinner.setEnabled(false);
		this.timeSpinner.setEnabled(false);
		this.lensComboBox.setEnabled(false);
		this.ceilingLightEnabledCheckBox.setEnabled(false);
		this.saveButton.setEnabled(false);
		this.shareButton.setEnabled(false);
		this.createButton.setText(SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.PhotoPanel.class, "STOP_PHOTO_CREATION.Name"));
		createButton.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				stopPhotoCreation(true);
			}
		});

		// Compute photo in an other executor thread
		// Use a clone of home because the user can modify home during photo computation
		final Home home = this.home.clone();
		List<Selectable> emptySelection = Collections.emptyList();
		home.setSelectedItems(emptySelection);
		this.photoCreationExecutor = Executors.newSingleThreadExecutor();
		this.photoCreationExecutor.execute(new Runnable()
		{
			public void run()
			{
				computePhoto(home);
			}
		});
	}

	/**
	 * Computes the photo of the given home.
	 * Caution : this method must be thread safe because it's called from an executor.
	 */
	private void computePhoto(Home home)
	{
		EventQueue.invokeLater(new Runnable()
		{
			public void run()
			{
				statusLabel.setText("Calculating...");
			}
		});
		this.photoCreationStartTime = System.currentTimeMillis();
		BufferedImage image = null;
		try
		{
			int quality = this.controller.getQuality();
			int imageWidth = this.controller.getWidth();
			int imageHeight = this.controller.getHeight();
			Renovations3DActivity.logFireBaseLevelUp("computePhotoStart", "quality " + quality + " width " + imageWidth + " height " + imageHeight);
			if (quality >= 2)
			{
				// Use photo renderer
				PhotoRenderer photoRenderer = new PhotoRenderer(home, this.object3dFactory,
						quality == 2
								? PhotoRenderer.Quality.LOW
								: PhotoRenderer.Quality.HIGH);
				int bestImageHeight;
				// Check correct ratio if lens is fisheye or spherical
				Camera camera = home.getCamera();
				if (camera.getLens() == Lens.FISHEYE)
				{
					bestImageHeight = imageWidth;
				}
				else if (camera.getLens() == Lens.SPHERICAL)
				{
					bestImageHeight = imageWidth / 2;
				}
				else
				{
					bestImageHeight = imageHeight;
				}
				if (photoCreationExecutor != null)
				{
					final BufferedImage image2 = new BufferedImage(imageWidth, bestImageHeight, BufferedImage.TYPE_INT_ARGB);
					image = image2;
					this.photoComponent.setImage(image);
					//photoRenderer.render(image, camera, this.photoComponent);
					ImageObserver io = new ImageObserver()
					{
						@Override
						public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height)
						{
							photoComponent.setImage(image2);
							return false;
						}

						@Override
						public Object getDelegate()
						{
							throw new UnsupportedOperationException();
						}
					};
					photoRenderer.render(image, camera, io);
					photoRenderer.dispose();

				}
			}
			else
			{
				// Compute 3D view offscreen image
				HomeComponent3D homeComponent3D = new HomeComponent3D();
				homeComponent3D.init(home, this.preferences, this.object3dFactory, quality == 1, null);
				image = homeComponent3D.getOffScreenImage(imageWidth, imageHeight);

				// must be carefully swapped to get BGRA to RGBA
				int[] imagePixels = PlanComponent.getImagePixels(image);
				Bitmap bm = Bitmap.createBitmap(imagePixels, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
				image = new VMBufferedImage(bm);

			}
			Renovations3DActivity.logFireBaseLevelUp("computePhotoEnd", "quality " + quality + " width " + imageWidth + " height " + imageHeight);
		}
		catch (OutOfMemoryError ex)
		{
			image = getErrorImage();
			throw ex;
		}
		catch (IllegalStateException ex)
		{
			image = getErrorImage();
			throw ex;
		}
		catch (IOException ex)
		{
			image = getErrorImage();
		}
		finally
		{

			final BufferedImage photoImage = this.photoCreationExecutor != null
					? image
					: null;
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					statusLabel.setText("Ready");

					saveButton.setEnabled(photoImage != null);
					shareButton.setEnabled(photoImage != null);

					createButton.setText(SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.PhotoPanel.class, "START_PHOTO_CREATION.Name"));
					createButton.setOnClickListener(new View.OnClickListener()
					{
						public void onClick(View view)
						{
							startPhotoCreation();
						}
					});
					photoComponent.setImage(photoImage);
					sizeAndQualityPanel.setEnabled(true);
					updateAdvancedComponents();
					updateRatioComponents();

					photoCreationExecutor = null;
				}
			});
		}
	}

	/**
	 * Returns the image used in case of an error.
	 */
	private BufferedImage getErrorImage()
	{
		Icon errorIcon = IconManager.getInstance().getErrorIcon(16);
		BufferedImage errorImage = new BufferedImage(
				errorIcon.getIconWidth(), errorIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2D = (Graphics2D) errorImage.getGraphics();
		errorIcon.paintIcon(this, g2D, 0, 0);
		g2D.dispose();
		return errorImage;
	}

	/**
	 * Stops photo creation.
	 */
	private void stopPhotoCreation(final boolean confirmStop)
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				if (photoCreationExecutor != null
						// Confirm the stop if a rendering has been running for more than 30 s
						&& (!confirmStop
						|| System.currentTimeMillis() - photoCreationStartTime < MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING
						|| JOptionPane.showConfirmDialog(activity,
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "confirmStopCreation.message"),
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "confirmStopCreation.title"),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION))
				{
					if (photoCreationExecutor != null)
					{ // Check a second time in case rendering stopped meanwhile
						// Will interrupt executor thread
						photoCreationExecutor.shutdownNow();
						photoCreationExecutor = null;
						Renovations3DActivity.logFireBaseLevelUp("Photo Stopped");
						EventQueue.invokeLater(new Runnable()
						{
							public void run()
							{
								createButton.setText(SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.PhotoPanel.class, "START_PHOTO_CREATION.Name"));
								createButton.setOnClickListener(new View.OnClickListener()
								{
									public void onClick(View view)
									{
										startPhotoCreation();
									}
								});
								statusLabel.setText("Stopped.");
								sizeAndQualityPanel.setEnabled(true);
								updateAdvancedComponents();
								updateRatioComponents();

							}
						});
					}
				}
			}
		});
		t.start();
	}

	/**
	 * Saves the created image.
	 */
	private void savePhoto(final boolean share)
	{
		Thread t = new Thread(new Runnable()
		{
			public void run()
			{
				String pngFile = controller.getContentManager().showSaveDialog(PhotoPanel.this,
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "savePhotoDialog.title"),
						ContentManager.ContentType.PNG, home.getName());
				try
				{
					if (pngFile != null)
					{
						final File photoFile = new File(pngFile);
						ImageIO.write(photoComponent.getImage(), "PNG", photoFile);
						Renovations3DActivity.logFireBaseLevelUp("Photo Saved as", pngFile);

						if(share)
						{
							Renovations3DActivity.logFireBaseContent("sharephoto_start", "photo name: " + photoFile.getName());
							final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
							emailIntent.setType("image/png");

							String subjectText = activity.getResources().getString(R.string.app_name) + " " + photoFile.getName();
							emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subjectText);
							//emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Attached");

							Uri outputFileUri = null;
							if (Build.VERSION.SDK_INT > M)
							{
								outputFileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", photoFile);
							}
							else
							{
								outputFileUri = Uri.fromFile(photoFile);
							}
							emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
							emailIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);

							activity.runOnUiThread(new Runnable(){public void run(){
							// Always use string resources for UI text.
							// This says something like "Share this photo with"
							String title = activity.getResources().getString(R.string.share);
							// Create intent to show chooser
							Intent chooser = Intent.createChooser(emailIntent, title);

							// Verify the intent will resolve to at least one activity
							if (emailIntent.resolveActivity(activity.getPackageManager()) != null) {
								activity.startActivity(chooser);
							}
							Renovations3DActivity.logFireBaseContent("sharephoto_end", "photo name: " + photoFile.getName());}});
						}
					}
				}
				catch (Exception ex)
				{
					String messageFormat = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "savePhotoError.message");
					JOptionPane.showMessageDialog(activity, String.format(messageFormat, ex.getMessage()),
							preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PhotoPanel.class, "savePhotoError.title"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		t.start();
	}

  /**
   * Manages closing of this pane.
   */
  private void close() {

      this.dismiss();

  }


	public class AndroidInterface implements UserInterface
	{
		private int min;
		private int max;
		private float invP;
		private String task;
		private int lastP;
		private TextView statusLabel;

		public AndroidInterface(TextView statusLabel) {
			this.statusLabel = statusLabel;
		}

		public void print(UI.Module m, UI.PrintLevel level, String s) {
			if (BuildConfig.DEBUG)
				System.out.println(UI.formatOutput(m, level, s));
		}

		public void taskStart(String s, int min, int max) {
			task = s;
			this.min = min;
			this.max = max;
			lastP = -1;
			invP = 100.0f / (max - min);
			EventQueue.invokeLater(new Runnable(){public void run(){
			statusLabel.setText(task + "...");}});
		}

		public void taskUpdate(int current) {
			final int p = (min == max) ? 0 : (int) ((current - min) * invP);
			if (p != lastP)
			{
				EventQueue.invokeLater(new Runnable(){public void run(){
				statusLabel.setText(task + " [" + (lastP = p) + "%]");}});
				System.out.print(task + " [" + (lastP = p) + "%]\r");
			}
		}

		public void taskStop() {
			EventQueue.invokeLater(new Runnable(){public void run(){
			statusLabel.setText(task + " complete");}});
			System.out.print("                                                                      \r");
		}
	}



}
