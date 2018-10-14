/*
 * BackgroundImageWizardStepsPanel.java 8 juin 07
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
import android.content.Context;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.LinearLayout;

import com.eteks.renovations3d.AdMobManager;
import com.eteks.renovations3d.ImageAcquireManager;
import com.eteks.renovations3d.Renovations3DActivity;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JPanel;
import com.mindblowing.swingish.JSpinner;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.viewcontroller.BackgroundImageWizardController;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import javaawt.AlphaComposite;
import javaawt.BasicStroke;
import javaawt.Color;
import javaawt.Dimension;
import javaawt.EventQueue;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.Point;
import javaawt.RenderingHints;
import javaawt.Shape;
import javaawt.geom.AffineTransform;
import javaawt.geom.Ellipse2D;
import javaawt.geom.Line2D;
import javaawt.geom.Point2D;
import javaawt.image.BufferedImage;
import javaawt.imageio.ImageIO;


/**
 * Wizard panel for background image choice.
 *
 * @author Emmanuel Puybaret
 */
public class BackgroundImageWizardStepsPanel extends JPanel implements View
{
	private static final int LARGE_IMAGE_PIXEL_COUNT_THRESHOLD = 10000000;
	private static final int LARGE_IMAGE_MAX_PIXEL_COUNT = 8000000;
	private static float POINT_RADIUS_PX = 20;
	private static final float POINT_RADIUS_DP = 20;

	private final BackgroundImageWizardController controller;
	private final Executor imageLoader;
	private JLabel imageChoiceOrChangeLabel;
	private JButton imageChoiceOrChangeButtonFile;
	private JButton imageChoiceOrChangeButtonCamera;
	private JLabel imageChoiceErrorLabel;
	private ScaledImageComponent imageChoicePreviewComponent;
	private JLabel scaleLabel;
	private JLabel scaleDistanceLabel;
	private JSpinner scaleDistanceSpinner;
	private ScaledImageComponent scalePreviewComponent;
	private JLabel originLabel;
	private JLabel xOriginLabel;
	private JSpinner xOriginSpinner;
	private JLabel yOriginLabel;
	private JSpinner yOriginSpinner;
	private ScaledImageComponent originPreviewComponent;
	private static BufferedImage waitImage;


	private LinearLayout LL_CHOICE;
	private LinearLayout LL_SCALE;
	private LinearLayout LL_ORIGIN;

	/**
	 * Creates a view for background image choice, scale and origin.
	 */
	public BackgroundImageWizardStepsPanel(BackgroundImage backgroundImage,
										   UserPreferences preferences,
										   final BackgroundImageWizardController controller,
										   Activity activity)
	{
		super(activity, R.layout.jpanel_background_image_wizard);

		Renovations3DActivity.logFireBaseContent("BackgroundImageWizardStepsPanel started");

		final float scale = getResources().getDisplayMetrics().density;
		POINT_RADIUS_PX = (int) (POINT_RADIUS_DP * scale + 0.5f);

		this.controller = controller;
		this.imageLoader = Executors.newSingleThreadExecutor();
		createComponents(preferences, controller);

		layoutComponents(preferences);

		// check in case teh image request buttons caused this activity to be destoryed then recreated
		String pendingImageName = ((Renovations3DActivity)activity).getImageAcquireManager().requestPendingChosenImageFile(ImageAcquireManager.Destination.IMPORT_BACKGROUND);
		if(pendingImageName == null)
		{
			updateController(backgroundImage, preferences);
		}
		else
		{
			try
			{
				updatePreviewComponentsWithWaitImage(preferences);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			updateController(pendingImageName, preferences, controller.getContentManager());
		}

		controller.addPropertyChangeListener(BackgroundImageWizardController.Property.STEP,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent evt)
					{
						updateStep(controller);
					}
				});
	}

	/**
	 * Creates components displayed by this panel.
	 */
	private void createComponents(final UserPreferences preferences,
								  final BackgroundImageWizardController controller)
	{
		// Get unit name matching current unit
		String unitName = preferences.getLengthUnit().getName();

		// Image choice panel components
		this.imageChoiceOrChangeLabel = new JLabel(activity, "");
		this.imageChoiceOrChangeButtonFile = new JButton(activity, "");
		this.imageChoiceOrChangeButtonFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				/*String imageName = showImageChoiceDialog(preferences, controller.getContentManager());
				if (imageName != null)
				{
					updateController(imageName, preferences, controller.getContentManager());
				}*/
				try
				{
					updatePreviewComponentsWithWaitImage(preferences);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
				//PJ replaced with intent system
				((Renovations3DActivity)activity).getImageAcquireManager().pickImage(
					new ImageAcquireManager.ImageReceiver()
					{
						@Override
						public void receivedImage(String imageName)
						{
							updateController(imageName, preferences, controller.getContentManager());
						}
					}
				, ImageAcquireManager.Destination.IMPORT_BACKGROUND);
			}
		});
		this.imageChoiceOrChangeButtonCamera = new JButton(activity, "");
		this.imageChoiceOrChangeButtonCamera.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ev)
			{
				try
				{
					updatePreviewComponentsWithWaitImage(preferences);
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}

				((Renovations3DActivity)activity).getImageAcquireManager().takeImage(
					new ImageAcquireManager.ImageReceiver()
					{
						@Override
						public void receivedImage(String imageName)
						{
							updateController(imageName, preferences, controller.getContentManager());
						}
					}
				, ImageAcquireManager.Destination.IMPORT_BACKGROUND);
			}
		});
		this.imageChoiceErrorLabel = new JLabel(activity, preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceErrorLabel.text"));
		// Make imageChoiceErrorLabel visible only if an error occurred during image content loading
		this.imageChoiceErrorLabel.setVisibility(android.view.View.GONE);
		this.imageChoicePreviewComponent = new ImageViewTouchable(null, true, activity);
		// Add a transfer handler to image preview component to let user drag and drop an image in component
  /*PJPJ drag and drop removed  this.imageChoicePreviewComponent.setTransferHandler(new TransferHandler() {
		@Override
        public boolean canImport(JComponent comp, DataFlavor [] flavors) {
          return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(JComponent comp, Transferable transferedFiles) {
          boolean success = true;
          try {
            List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
            final String imageName = files.get(0).getAbsolutePath();
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  updateController(imageName, preferences, controller.getContentManager());
                }
              });
          } catch (UnsupportedFlavorException ex) {
            success = false;
          } catch (IOException ex) {
            success = false;
          }
          if (!success) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(BackgroundImageWizardStepsPanel.this),
                      preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceError"));
                }
              });
          }
          return success;
        }
      });*/
		// this.imageChoicePreviewComponent.setBorder(SwingTools.getDropableComponentBorder());

		// Image scale panel components
		this.scaleLabel = new JLabel(activity, Html.fromHtml(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "scaleLabel.text").replace("<br>", " ")));
		this.scaleDistanceLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "scaleDistanceLabel.text", unitName));
		final float maximumLength = preferences.getLengthUnit().getMaximumLength();

		final NullableSpinnerNumberModel.NullableSpinnerLengthModel scaleDistanceSpinnerModel =
				new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, preferences.getLengthUnit().getMinimumLength(), maximumLength);
		this.scaleDistanceSpinner = new NullableSpinner(activity, scaleDistanceSpinnerModel, true);
		this.scaleDistanceSpinner.getModel().addChangeListener(new ChangeListener()
		{
			public void stateChanged(ChangeEvent ev)
			{
				// If spinner value changes update controller
				controller.setScaleDistance(
						((NullableSpinnerNumberModel.NullableSpinnerLengthModel) scaleDistanceSpinner.getModel()).getLength());
			}
		});
		PropertyChangeListener scaleDistanceChangeListener = new PropertyChangeListener()
		{
			public void propertyChange(PropertyChangeEvent ev)
			{
				// If scale distance changes updates scale spinner
				Float scaleDistance = controller.getScaleDistance();
				scaleDistanceSpinnerModel.setNullable(scaleDistance == null);
				scaleDistanceSpinnerModel.setLength(scaleDistance);
			}
		};
		scaleDistanceChangeListener.propertyChange(null);
		controller.addPropertyChangeListener(BackgroundImageWizardController.Property.SCALE_DISTANCE, scaleDistanceChangeListener);
		this.scalePreviewComponent = new ScaleImagePreviewComponent(controller);

		// Image origin panel components
		this.originLabel = new JLabel(activity, Html.fromHtml(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "originLabel.text").replace("<br>", " ")));
		this.xOriginLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "xOriginLabel.text", unitName));
		this.yOriginLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "yOriginLabel.text", unitName));
		final NullableSpinnerNumberModel.NullableSpinnerLengthModel xOriginSpinnerModel =
				new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, controller.getXOrigin(), -maximumLength, maximumLength);
		this.xOriginSpinner = new NullableSpinner(activity, xOriginSpinnerModel);
		final NullableSpinnerNumberModel.NullableSpinnerLengthModel yOriginSpinnerModel =
				new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, controller.getYOrigin(), -maximumLength, maximumLength);
		this.yOriginSpinner = new NullableSpinner(activity, yOriginSpinnerModel);
		ChangeListener originSpinnersListener = new ChangeListener()
		{
			public void stateChanged(ChangeEvent ev)
			{
				// If origin spinners value changes update controller
				controller.setOrigin(xOriginSpinnerModel.getLength(), yOriginSpinnerModel.getLength());
			}
		};
		xOriginSpinnerModel.addChangeListener(originSpinnersListener);
		yOriginSpinnerModel.addChangeListener(originSpinnersListener);
		controller.addPropertyChangeListener(BackgroundImageWizardController.Property.X_ORIGIN,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent ev)
					{
						// If origin values changes update x origin spinner
						xOriginSpinnerModel.setLength(controller.getXOrigin());
					}
				});
		controller.addPropertyChangeListener(BackgroundImageWizardController.Property.Y_ORIGIN,
				new PropertyChangeListener()
				{
					public void propertyChange(PropertyChangeEvent ev)
					{
						// If origin values changes update y origin spinner
						yOriginSpinnerModel.setLength(controller.getYOrigin());
					}
				});

		this.originPreviewComponent = new OriginImagePreviewComponent(controller);
	}

	/**
	 * Layouts components in 3 panels added to this panel as cards.
	 */
	private void layoutComponents(UserPreferences preferences)
	{

		LL_CHOICE = (LinearLayout) inflatedView.findViewById(R.id.bgiw_layoutChoice);
		swapOut(imageChoiceOrChangeLabel, R.id.bgiw_imageChoiceOrChangeLabel);
		swapOut(imageChoiceOrChangeButtonFile, R.id.bgiw_imageFromFileButton);
		swapOut(imageChoiceOrChangeButtonCamera, R.id.bgiw_imageFromCameraButton);

		swapOut(imageChoicePreviewComponent, R.id.bgiw_imageChoicePreviewComponent);
		//note not the scalable type guy, no zoomers required
		//TODO: some sort of solution for a bad image  this.imageChoiceErrorLabel

		LL_SCALE = (LinearLayout) inflatedView.findViewById(R.id.bgiw_layoutScale);
		swapOut(scaleLabel, R.id.bgiw_scaleLabel);
		swapOut(scaleDistanceLabel, R.id.bgiw_scaleDistanceLabel);
		swapOut(scaleDistanceSpinner, R.id.bgiw_scaleDistanceSpinner);
		swapOut(createScalableImageComponent(this.scalePreviewComponent, preferences), R.id.bgiw_scalePreviewComponent);

		LL_ORIGIN = (LinearLayout) inflatedView.findViewById(R.id.bgiw_layoutOrigin);
		swapOut(originLabel, R.id.bgiw_originLabel);
		swapOut(xOriginLabel, R.id.bgiw_xOriginLabel);
		swapOut(xOriginSpinner, R.id.bgiw_xOriginSpinner);
		swapOut(yOriginLabel, R.id.bgiw_yOriginLabel);
		swapOut(yOriginSpinner, R.id.bgiw_yOriginSpinner);
		swapOut(createScalableImageComponent(this.originPreviewComponent, preferences), R.id.bgiw_originPreviewComponent);

		//PJ my version of the card system
		LL_CHOICE.setVisibility(android.view.View.VISIBLE);
		LL_SCALE.setVisibility(android.view.View.GONE);
		LL_ORIGIN.setVisibility(android.view.View.GONE);



	}

	/**
	 * Returns a panel displaying the given component along with zoom in and out buttons.
	 */
	private android.view.View createScalableImageComponent(final ScaledImageComponent imageComponent,
														   UserPreferences preferences)
	{//PJ zoom buttons removed in favor of pinch zoom
		/*final JButton zoomInButton = new JButton(activity, "");
		final JButton zoomOutButton = new JButton(activity, "");


		SpannableStringBuilder builder = new SpannableStringBuilder("*");
		builder.setSpan(new ImageSpan(activity, android.R.drawable.btn_plus), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		zoomInButton.setText(builder);


		zoomInButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
//          final Rectangle viewRect = ((JViewport)imageComponent.getParent()).getViewRect();

				final Rect viewRect = new Rect();
				((ScrollView)imageComponent.getParent()).getDrawingRect(viewRect);

				imageComponent.setScaleMultiplier(2 * imageComponent.getScaleMultiplier());
				zoomOutButton.setEnabled(imageComponent.getScaleMultiplier() > 1f);
				zoomInButton.setEnabled(imageComponent.getScaleMultiplier() < 32f);
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						// Keep viewport centered on the same point
						//               ((JViewport)imageComponent.getParent()).setViewPosition(
						//                   new Point((int)(viewRect.getCenterX() * 2 - viewRect.width / 2),
						//                             (int)(viewRect.getCenterY() * 2 - viewRect.height / 2)));

						  ((ScrollView)imageComponent.getParent()).scrollTo(
								  (int)(viewRect.centerX() * 2 - viewRect.width() / 2),
										 (int)(viewRect.centerY() * 2 - viewRect.height() / 2) );
					}
				});
			}
		});

		SpannableStringBuilder builder2 = new SpannableStringBuilder("*");
		builder2.setSpan(new ImageSpan(activity, android.R.drawable.btn_minus), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		zoomOutButton.setText(builder2);

		zoomOutButton.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent ev)
			{
				//        final Rectangle viewRect = ((JViewport)imageComponent.getParent()).getViewRect();

				final Rect viewRect = new Rect();
				((ScrollView)imageComponent.getParent()).getDrawingRect(viewRect);

				imageComponent.setScaleMultiplier(.5f * imageComponent.getScaleMultiplier());
				zoomOutButton.setEnabled(imageComponent.getScaleMultiplier() > 1f);
				zoomInButton.setEnabled(imageComponent.getScaleMultiplier() < 128f);
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						// Keep viewport centered on the same point
						//              ((JViewport)imageComponent.getParent()).setViewPosition(
						//                  new Point(Math.max(0, (int)(viewRect.getCenterX() / 2 - viewRect.width / 2)),
						//                            Math.max(0, (int)(viewRect.getCenterY() / 2 - viewRect.height / 2))));
						((ScrollView)imageComponent.getParent()).scrollTo(
								Math.max(0, (int)(viewRect.centerX() / 2 - viewRect.width() / 2)),
								Math.max(0, (int)(viewRect.centerY() / 2 - viewRect.height() / 2)) );
					}
				});
			}
		});


		if (imageComponent == this.scalePreviewComponent)
		{
			swapOut(zoomInButton, R.id.bgiw_scaleZoomInButton);
			swapOut(zoomOutButton, R.id.bgiw_scaleZoomOutButton);
		}
		else if (imageComponent == this.originPreviewComponent)
		{
			swapOut(zoomInButton, R.id.bgiw_originZoomInButton);
			swapOut(zoomOutButton, R.id.bgiw_originZoomOutButton);
		}*/

		return imageComponent;
	}

	/**
	 * Switches to the view card matching current step.
	 */
	public void updateStep(BackgroundImageWizardController controller)
	{
		BackgroundImageWizardController.Step step = controller.getStep();

		LL_CHOICE.setVisibility(android.view.View.GONE);
		LL_SCALE.setVisibility(android.view.View.GONE);
		LL_ORIGIN.setVisibility(android.view.View.GONE);

		switch (step)
		{
			case CHOICE:
				LL_CHOICE.setVisibility(android.view.View.VISIBLE);
				break;
			case SCALE:
				LL_SCALE.setVisibility(android.view.View.VISIBLE);
				break;
			case ORIGIN:
				LL_ORIGIN.setVisibility(android.view.View.VISIBLE);
				break;
		}


		// TODO: I want to reset the scoll view but it doesn't work
		inflatedView.postInvalidate();

	}

	/**
	 * Updates controller initial values from <code>backgroundImage</code>.
	 */
	private void updateController(final BackgroundImage backgroundImage,
								  final UserPreferences preferences)
	{
		if (backgroundImage == null)
		{
			setImageChoiceTexts(preferences);
			updatePreviewComponentsImage(null);
		}
		else
		{
			setImageChangeTexts(preferences);
			// Read image in imageLoader executor
			this.imageLoader.execute(new Runnable()
			{
				public void run()
				{
					BufferedImage image = null;
					try
					{
						image = readImage(backgroundImage.getImage(), preferences);
					}
					catch (IOException ex)
					{
						// image is null
					}
					final BufferedImage readImage = image;
					// Update components in dispatch thread
					EventQueue.invokeLater(new Runnable()
					{
						public void run()
						{
							if (readImage != null)
							{
								controller.setImage(backgroundImage.getImage());
								controller.setScaleDistance(backgroundImage.getScaleDistance());
								controller.setScaleDistancePoints(backgroundImage.getScaleDistanceXStart(),
										backgroundImage.getScaleDistanceYStart(), backgroundImage.getScaleDistanceXEnd(),
										backgroundImage.getScaleDistanceYEnd());
								controller.setOrigin(backgroundImage.getXOrigin(), backgroundImage.getYOrigin());
							}
							else
							{
								controller.setImage(null);
								setImageChoiceTexts(preferences);
								imageChoiceErrorLabel.setVisibility(android.view.View.GONE);
							}
						}
					});
				}
			});
		}
	}

	/**
	 * Reads image from <code>imageName</code> and updates controller values.
	 */
	private void updateController(final String imageName,
								  final UserPreferences preferences,
								  final ContentManager contentManager)
	{
		// Read image in imageLoader executor
		this.imageLoader.execute(new Runnable()
		{
			public void run()
			{
				Content imageContent = null;
				try
				{
					// Copy image to a temporary content to keep a safe access to it until home is saved
					imageContent = TemporaryURLContent.copyToTemporaryURLContent(contentManager.getContent(imageName));
				}
				catch (RecorderException ex)
				{
					// Error message displayed below
				}
				catch (IOException ex)
				{
					// Error message displayed below
				}
				if (imageContent == null)
				{
					// PJ make sure everythings still lays out ok even after this failure
					controller.setImage(null);
					setImageChoiceTexts(preferences);
					EventQueue.invokeLater(new Runnable()
					{
						public void run()
						{
							JOptionPane.showMessageDialog(activity,
									preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class,
											"imageChoiceError", imageName), "", JOptionPane.ERROR_MESSAGE);
						}
					});
					return;
				}

				BufferedImage image = null;
				try
				{
					// Check image is less than 10 million pixels
					Dimension size = SwingTools.getImageSizeInPixels(imageContent);
					if (size.width * (long) size.height > LARGE_IMAGE_PIXEL_COUNT_THRESHOLD)
					{
						imageContent = readAndReduceImage(imageContent, size, preferences);
						if (imageContent == null)
						{
							return;
						}
					}
					image = readImage(imageContent, preferences);
				}
				catch (IOException ex)
				{
					// image is null
				}

				final BufferedImage readImage = image;
				final Content readContent = imageContent;
				// Update components in dispatch thread
				EventQueue.invokeLater(new Runnable()
				{
					public void run()
					{
						if (readImage != null)
						{
							controller.setImage(readContent);
							setImageChangeTexts(preferences);
							imageChoiceErrorLabel.setVisibility(android.view.View.GONE);
							BackgroundImage referenceBackgroundImage = controller.getReferenceBackgroundImage();
							if (referenceBackgroundImage != null
									&& referenceBackgroundImage.getScaleDistanceXStart() < readImage.getWidth()
									&& referenceBackgroundImage.getScaleDistanceXEnd() < readImage.getWidth()
									&& referenceBackgroundImage.getScaleDistanceYStart() < readImage.getHeight()
									&& referenceBackgroundImage.getScaleDistanceYEnd() < readImage.getHeight())
							{
								// Initialize distance and origin with values of the reference image
								controller.setScaleDistance(referenceBackgroundImage.getScaleDistance());
								controller.setScaleDistancePoints(referenceBackgroundImage.getScaleDistanceXStart(),
										referenceBackgroundImage.getScaleDistanceYStart(),
										referenceBackgroundImage.getScaleDistanceXEnd(),
										referenceBackgroundImage.getScaleDistanceYEnd());
								controller.setOrigin(referenceBackgroundImage.getXOrigin(), referenceBackgroundImage.getYOrigin());
							}
							else
							{
								// Initialize distance and origin with default values
								controller.setScaleDistance(null);
								float scaleDistanceXStart = readImage.getWidth() * 0.1f;
								float scaleDistanceYStart = readImage.getHeight() / 2f;
								float scaleDistanceXEnd = readImage.getWidth() * 0.9f;
								controller.setScaleDistancePoints(scaleDistanceXStart, scaleDistanceYStart,
										scaleDistanceXEnd, scaleDistanceYStart);
								controller.setOrigin(0, 0);
							}
						}
						else
						{//if (isShowing()){
							controller.setImage(null);
							setImageChoiceTexts(preferences);
							JOptionPane.showMessageDialog(activity,
									preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class,
											"imageChoiceFormatError"), "", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}
		});
	}

	/**
	 * Informs the user that the image size is large and returns a reduced size image if he confirms
	 * that the size should be reduced.
	 * Caution : this method must be thread safe because it's called from image loader executor.
	 */
	private Content readAndReduceImage(Content imageContent,
									   final Dimension imageSize,
									   final UserPreferences preferences) throws IOException
	{
		try
		{
			float factor = (float) Math.sqrt((float) LARGE_IMAGE_MAX_PIXEL_COUNT / (imageSize.width * (long) imageSize.height));
			final int reducedWidth = Math.round(imageSize.width * factor);
			final int reducedHeight = Math.round(imageSize.height * factor);
			final AtomicInteger result = new AtomicInteger(JOptionPane.CANCEL_OPTION);

			//TODO: currently this auto returns NO as teh
			//JOptionPane asked to showOptionDialog (String message) on EDT thread you MUST not as I will block!
			// must get it onto the right thread somehow
			EventQueue.invokeAndWait(new Runnable()
			{
				public void run()
				{
					String title = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "reduceImageSize.title");
					String confirmMessage = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class,
							"reduceImageSize.message", imageSize.width, imageSize.height, reducedWidth, reducedHeight);
					String reduceSize = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "reduceImageSize.reduceSize");
					String keepUnchanged = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "reduceImageSize.keepUnchanged");
					String cancel = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "reduceImageSize.cancel");
					result.set(JOptionPane.showOptionDialog(activity,
							confirmMessage, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
							null, new Object[]{reduceSize, keepUnchanged, cancel}, keepUnchanged));
				}
			});
			if (result.get() == JOptionPane.CANCEL_OPTION)
			{
				return null;
			}
			else if (result.get() == JOptionPane.YES_OPTION)
			{
				updatePreviewComponentsWithWaitImage(preferences);

				InputStream contentStream = imageContent.openStream();
				BufferedImage image = ImageIO.read(contentStream);
				contentStream.close();
				if (image != null)
				{
					BufferedImage reducedImage = new BufferedImage(reducedWidth, reducedHeight, image.getType());
					Graphics2D g2D = (Graphics2D) reducedImage.getGraphics();
					g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
					g2D.drawImage(image, AffineTransform.getScaleInstance(factor, factor), null);
					g2D.dispose();

					File file = OperatingSystem.createTemporaryFile("background", ".tmp");
					ImageIO.write(reducedImage, image.getTransparency() == BufferedImage.OPAQUE ? "JPEG" : "PNG", file);
					return new TemporaryURLContent(file.toURI().toURL());
				}
			}
			return imageContent;
		}
		catch (InterruptedException ex)
		{
			return imageContent;
		}
		catch (InvocationTargetException ex)
		{
			ex.printStackTrace();
			return imageContent;
		}
		catch (IOException ex)
		{
			updatePreviewComponentsImage(null);
			throw ex;
		}
	}

	/**
	 * Reads image from <code>imageContent</code>.
	 * Caution : this method must be thread safe because it's called from image loader executor.
	 */
	private BufferedImage readImage(Content imageContent,
									UserPreferences preferences) throws IOException
	{
		try
		{
			updatePreviewComponentsWithWaitImage(preferences);

			// Read the image content
			InputStream contentStream = imageContent.openStream();
			BufferedImage image = ImageIO.read(contentStream);
			contentStream.close();

			if (image != null)
			{
				updatePreviewComponentsImage(image);
				return image;
			}
			else
			{
				throw new IOException();
			}
		}
		catch (IOException ex)
		{
			updatePreviewComponentsImage(null);
			throw ex;
		}
	}

	private void updatePreviewComponentsWithWaitImage(UserPreferences preferences) throws IOException
	{
		// Display a waiting image while loading
		if (waitImage == null)
		{
			waitImage = ImageIO.read(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class.
					getResource(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "waitIcon")));
		}
		System.out.println("updatePreviewComponentsImage(waitImage);");

		updatePreviewComponentsImage(waitImage);
	}

	/**
	 * Updates the <code>image</code> displayed by preview components.
	 */
	private void updatePreviewComponentsImage(BufferedImage image)
	{
		this.imageChoicePreviewComponent.setImage(image);
		this.scalePreviewComponent.setImage(image);
		this.originPreviewComponent.setImage(image);

		this.postInvalidate();
	}

	/**
	 * Sets the texts of label and button of image choice panel with
	 * change texts.
	 */
	private void setImageChangeTexts(UserPreferences preferences)
	{
		this.imageChoiceOrChangeLabel.setText(Html.fromHtml(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChangeLabel.text").replace("<br>", " ")));
		this.imageChoiceOrChangeButtonFile.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChangeButton.text") + ": " + activity.getString(R.string.get_image_file));
		this.imageChoiceOrChangeButtonCamera.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChangeButton.text") + ": " + activity.getString(R.string.get_image_camera));

	}

	/**
	 * Sets the texts of label and button of image choice panel with
	 * choice texts.
	 */
	private void setImageChoiceTexts(UserPreferences preferences)
	{
		this.imageChoiceOrChangeLabel.setText(Html.fromHtml(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceLabel.text").replace("<br>", " ")));
		this.imageChoiceOrChangeButtonFile.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceButton.text") + ": " + activity.getString(R.string.get_image_file));
		this.imageChoiceOrChangeButtonCamera.setText(SwingTools.getLocalizedLabelText(preferences,
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceButton.text") + ": " + activity.getString(R.string.get_image_camera));

	}

	/**
	 * Returns an image chosen for a content chooser dialog.
	 */
	//PJPJ swaped for a call back system above
/*	private String showImageChoiceDialog(UserPreferences preferences,
										 ContentManager contentManager)
	{
		return contentManager.showOpenDialog(this, preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceDialog.title"), ContentManager.ContentType.IMAGE);
	}*/

	/**
	 * Returns the selection color used in preview components.
	 */
	private static Color getSelectionColor()
	{
  /*  Color selectionColor = OperatingSystem.isMacOSXLeopardOrSuperior()
        ? UIManager.getColor("List.selectionBackground")
        : UIManager.getColor("textHighlight");
    float [] hsb = new float [3];
    Color.RGBtoHSB(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), hsb);
    if (hsb [1] < 0.4f) {
      // If color is too gray, return a default blue color
      selectionColor = new Color(40, 89, 208);
    }
    return selectionColor;*/
		//PJ not sure
		return new Color(40, 89, 208);
	}

	/**
	 * Preview component for image scale distance choice.
	 */
	private enum ActionType
	{
		ACTIVATE_ALIGNMENT, DEACTIVATE_ALIGNMENT
	}

	;

	private class ScaleImagePreviewComponent extends ImageViewTouchable
	{

		private final BackgroundImageWizardController controller;

		public ScaleImagePreviewComponent(BackgroundImageWizardController controller)
		{
			super(null, true, activity);
			this.controller = controller;
			addChangeListeners(controller);
			addMouseListeners(controller);
			//PJ setBorder(null);

			//TODO: this feels useful maybe
      /*InputMap inputMap = getInputMap(WHEN_IN_FOCUSED_WINDOW);
      inputMap.put(KeyStroke.getKeyStroke("shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("alt shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("control shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("meta shift pressed SHIFT"), ActionType.ACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("alt released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("control released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      inputMap.put(KeyStroke.getKeyStroke("meta released SHIFT"), ActionType.DEACTIVATE_ALIGNMENT);
      setInputMap(WHEN_IN_FOCUSED_WINDOW, inputMap);*/
		}

		/**
		 * Adds listeners to <code>controller</code>
		 * to update the scale distance points of the origin drawn by this component.
		 */
		private void addChangeListeners(final BackgroundImageWizardController controller)
		{
			controller.addPropertyChangeListener(BackgroundImageWizardController.Property.SCALE_DISTANCE_POINTS,
					new PropertyChangeListener()
					{
						public void propertyChange(PropertyChangeEvent ev)
						{
							// If origin values changes update displayed origin
							//repaint();
							postInvalidate();
						}
					});
		}

		/**
		 * Adds to this component a mouse listeners that allows the user to move the start point
		 * or the end point of the scale distance line.
		 */
		public void addMouseListeners(final BackgroundImageWizardController controller)
		{

			android.view.View.OnTouchListener mouseListener = new android.view.View.OnTouchListener() {
			private float         deltaXMousePressed;
			private float         deltaYMousePressed;
			private boolean     distanceStartPoint;
			private boolean     distanceEndPoint;
			private Point       lastMouseLocation = new Point();
			private Point		mouseLocation = new Point();

			@Override
			public boolean onTouch(android.view.View v, MotionEvent ev)
			{
				final int action = ev.getActionMasked();

				switch (action & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
					{
						// must do both as the moved works out what's closest
						mouseMoved(ev);
						mousePressed(ev);
						break;
					}

					case MotionEvent.ACTION_MOVE:
					{
						mouseDragged(ev);
						break;
					}

					case MotionEvent.ACTION_UP:
					{
						break;
					}

					case MotionEvent.ACTION_CANCEL:
					{
						break;
					}
					case MotionEvent.ACTION_POINTER_DOWN:
					{
						break;
					}
					case MotionEvent.ACTION_POINTER_UP:
					{
						break;
					}
				}

				// consume the event if we are near the end points
				return this.distanceStartPoint
					|| this.distanceEndPoint;
			}



			//@Override
			public void mousePressed(MotionEvent ev) {
			 // if (!ev.isPopupTrigger()) {
				mouseMoved(ev);

				if (this.distanceStartPoint
					|| this.distanceEndPoint) {
				  float [][] scaleDistancePoints = controller.getScaleDistancePoints();
				  Point translationorigin = getImageTranslation();
				  float scale = getImageScale();
				  this.deltaXMousePressed = (ev.getX() - translationorigin.x);
				  this.deltaYMousePressed = (ev.getY() - translationorigin.y);
				  if (this.distanceStartPoint) {
						this.deltaXMousePressed -= scaleDistancePoints [0][0] * scale;
						this.deltaYMousePressed -= scaleDistancePoints [0][1] * scale;
				  } else {
						this.deltaXMousePressed -= scaleDistancePoints [1][0] * scale;
						this.deltaYMousePressed -= scaleDistancePoints [1][1] * scale;
				  }
				  // Set actions used to activate/deactivate alignment
			   /*   ActionMap actionMap = getActionMap();
				  actionMap.put(ActionType.ACTIVATE_ALIGNMENT, new AbstractAction() {
					  public void actionPerformed(ActionEvent ev) {
						mouseDragged(null, true);
					  }
					});
				  actionMap.put(ActionType.DEACTIVATE_ALIGNMENT, new AbstractAction() {
					  public void actionPerformed(ActionEvent ev) {
						mouseDragged(null, false);
					  }
					});
				  setActionMap(actionMap);*/
				}
			 // }
			  this.lastMouseLocation.setLocation(ev.getX(), ev.getY());
			}

		   /* @Override
			public void mouseReleased(MouseEvent ev) {
			  ActionMap actionMap = getActionMap();
			  // Remove actions used to activate/deactivate alignment
			  actionMap.remove(ActionType.ACTIVATE_ALIGNMENT);
			  actionMap.remove(ActionType.DEACTIVATE_ALIGNMENT);
			  setActionMap(actionMap);
			}*/

			//@Override
			public void mouseDragged(MotionEvent ev) {
				mouseLocation.setLocation(ev.getX(), ev.getY());
				mouseDragged(mouseLocation, false);
				this.lastMouseLocation.setLocation(mouseLocation);
			 // mouseDragged(ev.getPoint(), ev.isShiftDown());
			  //this.lastMouseLocation = ev.getPoint();
			}

			public void mouseDragged(Point mouseLocation, boolean keepHorizontalVertical) {
			  if (this.distanceStartPoint
				  || this.distanceEndPoint) {
				if (mouseLocation == null) {
				  mouseLocation = this.lastMouseLocation;
				}
				Point point = getPointConstrainedInImage((int)
						(mouseLocation.x - this.deltaXMousePressed), (int)(mouseLocation.y - this.deltaYMousePressed));
				Point translation = getImageTranslation();
				float [][] scaleDistancePoints = controller.getScaleDistancePoints();
				float [] updatedPoint;
				float [] fixedPoint;
				if (this.distanceStartPoint) {
				  updatedPoint = scaleDistancePoints [0];
				  fixedPoint   = scaleDistancePoints [1];
				} else {
				  updatedPoint = scaleDistancePoints [1];
				  fixedPoint   = scaleDistancePoints [0];
				}

				float scale = getImageScale();
				// Compute updated point of distance line
				float newX = (float)((point.getX() - translation.x) / scale);
				float newY = (float)((point.getY() - translation.y) / scale);
				// Accept new points only if distance is greater that 2 pixels
				if (Point2D.distanceSq(fixedPoint [0] * scale, fixedPoint [1] * scale,
						newX * scale, newY * scale) >= 4) {
				  // If shift is down constrain keep the line vertical or horizontal
				  if (keepHorizontalVertical) {
					double angle = Math.abs(Math.atan2(fixedPoint [1] - newY, newX - fixedPoint [0]));
					if (angle > Math.PI / 4 && angle <= 3 * Math.PI / 4) {
					  newX = fixedPoint [0];
					} else {
					  newY = fixedPoint [1];
					}
				  }
				  updatedPoint [0] = newX;
				  updatedPoint [1] = newY;
				  controller.setScaleDistancePoints(
					scaleDistancePoints [0][0], scaleDistancePoints [0][1],
					scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
				  //repaint();
					BackgroundImageWizardStepsPanel.this.postInvalidate();
				}
			  }
			}

			//@Override
			public void mouseMoved(MotionEvent ev) {
			  this.distanceStartPoint =
			  this.distanceEndPoint = false;
			  if (isPointInImage((int)ev.getX(), (int)ev.getY())) {
				float [][] scaleDistancePoints = controller.getScaleDistancePoints();
				Point translation = getImageTranslation();
				float scale = getImageScale();

				// Check if user clicked on start or end point of distance line
				if (Math.abs(scaleDistancePoints [0][0] * scale - ev.getX() + translation.x) <= POINT_RADIUS_PX
					&& Math.abs(scaleDistancePoints [0][1] * scale - ev.getY() + translation.y) <= POINT_RADIUS_PX) {
				  this.distanceStartPoint = true;
				} else if (Math.abs(scaleDistancePoints [1][0] * scale - ev.getX() + translation.x) <= POINT_RADIUS_PX
						   && Math.abs(scaleDistancePoints [1][1] * scale - ev.getY() + translation.y) <= POINT_RADIUS_PX) {
				  this.distanceEndPoint = true;
				}
			  }

			 /* if (this.distanceStartPoint || this.distanceEndPoint) {
				setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
			  } else {
				setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
			  }*/
			}
		  };
		  //addMouseListener(mouseListener);
		  //addMouseMotionListener(mouseListener);
			this.setOnTouchListener(mouseListener);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			if (getImage() != null)
			{
				Graphics2D g2D = (Graphics2D) g;
				g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				Point translation = getImageTranslation();
				float scale = getImageScale();
				// Fill image background
				//PJg2D.setColor(UIManager.getColor("window"));
				g2D.fillRect(translation.x, translation.y, (int) (getImage().getWidth() * scale),
						(int) (getImage().getHeight() * scale));

				// Paint image with a 0.5 alpha
				paintImage(g2D, (AlphaComposite) AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

				g2D.setPaint(getSelectionColor());

				AffineTransform oldTransform = g2D.getTransform();
				//PJ just skipped Stroke oldStroke = g2D.getStroke();
				// Use same origin and scale as image drawing in super class
				g2D.translate(translation.x, translation.y);
				g2D.scale(scale, scale);
				// Draw a scale distance line
				g2D.setStroke(new BasicStroke(5 / scale,
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				float[][] scaleDistancePoints = this.controller.getScaleDistancePoints();
				g2D.draw(new Line2D.Float(scaleDistancePoints[0][0], scaleDistancePoints[0][1],
						scaleDistancePoints[1][0], scaleDistancePoints[1][1]));
				// Draw start point line
				g2D.setStroke(new BasicStroke(1 / scale,
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				double angle = Math.atan2(scaleDistancePoints[1][1] - scaleDistancePoints[0][1],
						scaleDistancePoints[1][0] - scaleDistancePoints[0][0]);
				AffineTransform oldTransform2 = g2D.getTransform();
				g2D.translate(scaleDistancePoints[0][0], scaleDistancePoints[0][1]);
				g2D.rotate(angle);
				Shape endLine = new Line2D.Double(0, 5 / scale, 0, -5 / scale);
				g2D.draw(endLine);
				g2D.setTransform(oldTransform2);

				// Draw end point line
				g2D.translate(scaleDistancePoints[1][0], scaleDistancePoints[1][1]);
				g2D.rotate(angle);
				g2D.draw(endLine);
				g2D.setTransform(oldTransform);
				//PJ just skipped g2D.setStroke(oldStroke);
			}
		}
	}

	/**
	 * Preview component for image scale distance choice.
	 */
	private class OriginImagePreviewComponent extends ImageViewTouchable
	{
		private final BackgroundImageWizardController controller;

		public OriginImagePreviewComponent(BackgroundImageWizardController controller)
		{
			super(null, true, activity);
			this.controller = controller;
			addChangeListeners(controller);
			addMouseListener(controller);
			//PJ setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		}

		/**
		 * Adds listeners to <code>controller</code>
		 * to update the location of the origin drawn by this component.
		 */
		private void addChangeListeners(final BackgroundImageWizardController controller)
		{
			PropertyChangeListener originChangeListener = new PropertyChangeListener()
			{
				public void propertyChange(PropertyChangeEvent ev)
				{
					// If origin values changes update displayed origin
					postInvalidate();
				}
			};
			controller.addPropertyChangeListener(BackgroundImageWizardController.Property.X_ORIGIN, originChangeListener);
			controller.addPropertyChangeListener(BackgroundImageWizardController.Property.Y_ORIGIN, originChangeListener);
		}

		/**
		 * Adds a mouse listener to this component to update the origin stored
		 * by <code>controller</code> when the user clicks in component.
		 */
		public void addMouseListener(final BackgroundImageWizardController controller)
		{
			android.view.View.OnTouchListener mouseListener = new android.view.View.OnTouchListener() {

				private Point mouseLocation = new Point();
				@Override
				public boolean onTouch(android.view.View v, MotionEvent ev)
				{
					final int action = ev.getActionMasked();

					switch (action & MotionEvent.ACTION_MASK)
					{
						case MotionEvent.ACTION_DOWN:
						{
							mousePressed(ev);
							break;
						}

						case MotionEvent.ACTION_MOVE:
						{
							mouseDragged(ev);
							break;
						}

						case MotionEvent.ACTION_UP:
						{
							break;
						}

						case MotionEvent.ACTION_CANCEL:
						{
							break;
						}
						case MotionEvent.ACTION_POINTER_DOWN:
						{
							break;
						}
						case MotionEvent.ACTION_POINTER_UP:
						{
							break;
						}
					}

					// never consume, allow image pan
					return false;
				}

          public void mousePressed(MotionEvent ev) {
          	if( isPointInImage((int)ev.getX(), (int)ev.getY())) {
							mouseLocation.setLocation(ev.getX(), ev.getY());
              updateOrigin(mouseLocation);
            }
          }

          private void updateOrigin(Point point) {
            Point translation = getImageTranslation();
            float [][] scaleDistancePoints = controller.getScaleDistancePoints();
            float rescale = getImageScale() / BackgroundImage.getScale(controller.getScaleDistance(),
                scaleDistancePoints [0][0], scaleDistancePoints [0][1],
                scaleDistancePoints [1][0], scaleDistancePoints [1][1]);
            float xOrigin = Math.round((point.getX() - translation.x) / rescale * 10) / 10.f;
            float yOrigin = Math.round((point.getY() - translation.y) / rescale * 10) / 10.f;
            controller.setOrigin(xOrigin, yOrigin);
          }

          //@Override
          public void mouseDragged(MotionEvent ev) {
            updateOrigin(getPointConstrainedInImage((int)ev.getX(), (int)ev.getY()));
          }

        /*  @Override
          public void mouseMoved(MouseEvent ev) {
            if (isPointInImage(ev.getX(), ev.getY())) {
              setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
            } else {
              setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
          }*/
        };
      //addMouseListener(mouseAdapter);
      //addMouseMotionListener(mouseAdapter);
			this.setOnTouchListener(mouseListener);
		}

		@Override
		protected void paintComponent(Graphics g)
		{
			if (getImage() != null)
			{
				Graphics2D g2D = (Graphics2D) g;
				g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
						RenderingHints.VALUE_ANTIALIAS_ON);

				Point translation = getImageTranslation();
				// Fill image background
				//PJ g2D.setColor(UIManager.getColor("window"));
				g2D.fillRect(translation.x, translation.y, (int) (getImage().getWidth() * getImageScale()),
						(int) (getImage().getHeight() * getImageScale()));

				// Paint image with a 0.5 alpha
				paintImage(g, (AlphaComposite) AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));

				g2D.setPaint(getSelectionColor());

				AffineTransform oldTransform = g2D.getTransform();
				//PJ skipped Stroke oldStroke = g2D.getStroke();
				g2D.translate(translation.x, translation.y);
				// Rescale according to scale distance
				float[][] scaleDistancePoints = this.controller.getScaleDistancePoints();
				float scale = getImageScale() / BackgroundImage.getScale(this.controller.getScaleDistance(),
						scaleDistancePoints[0][0], scaleDistancePoints[0][1],
						scaleDistancePoints[1][0], scaleDistancePoints[1][1]);
				g2D.scale(scale, scale);

				// Draw a dot at origin
				g2D.translate(this.controller.getXOrigin(), this.controller.getYOrigin());

				float originRadius = 4 / scale;
				g2D.fill(new Ellipse2D.Float(-originRadius, -originRadius,
						originRadius * 2, originRadius * 2));

				// Draw a cross
				g2D.setStroke(new BasicStroke(1 / scale,
						BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
				g2D.draw(new Line2D.Double(8 / scale, 0, -8 / scale, 0));
				g2D.draw(new Line2D.Double(0, 8 / scale, 0, -8 / scale));
				g2D.setTransform(oldTransform);
				//PJ skipped g2D.setStroke(oldStroke);
			}
		}
	}

	public class ImageViewTouchable extends ScaledImageComponent
	{
		private OnTouchListener alternativeListener;

		private static final int INVALID_POINTER_ID = -1;

		private float mPosX;
		private float mPosY;

		private float mLastTouchX;
		private float mLastTouchY;
		private float mLastGestureX;
		private float mLastGestureY;
		private int mActivePointerId = INVALID_POINTER_ID;

		private ScaleGestureDetector mScaleDetector;
		//private float mScaleFactor = 1.f;

		public ImageViewTouchable(Context context) {
			super(context);
			mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
		}

		public ImageViewTouchable(BufferedImage image,
									boolean imageEnlargementEnabled, Context context) {
			super(image, imageEnlargementEnabled, context);
			mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
		}


  		public void setOnTouchListener(OnTouchListener alternativeListener)
  		{
	  		this.alternativeListener = alternativeListener;
  		}

		@Override
		public boolean onTouchEvent(MotionEvent ev)
		{
			// Let the ScaleGestureDetector inspect all events.
			mScaleDetector.onTouchEvent(ev);

			// scale detector gets priority over alternative
			if (alternativeListener != null && !mScaleDetector.isInProgress())
			{
				// don't process the event if the alternative consumed it
				if (alternativeListener.onTouch(this, ev))
				{
					// stop the parent scrollview from scroll, notice very specific
					this.getParent().requestDisallowInterceptTouchEvent(true);
					return true;
				}
			}
			if (image != null)
			{
				final int action = ev.getAction();
				switch (action & MotionEvent.ACTION_MASK)
				{
					case MotionEvent.ACTION_DOWN:
					{
						if (!mScaleDetector.isInProgress())
						{
							final float x = ev.getX();
							final float y = ev.getY();

							mLastTouchX = x;
							mLastTouchY = y;
							mActivePointerId = ev.getPointerId(0);
						}
						break;
					}
					case MotionEvent.ACTION_POINTER_DOWN:
					{
						if (mScaleDetector.isInProgress())
						{
							final float gx = mScaleDetector.getFocusX();
							final float gy = mScaleDetector.getFocusY();
							mLastGestureX = gx;
							mLastGestureY = gy;
						}
						break;
					}
					case MotionEvent.ACTION_MOVE:
					{

						// Only move if the ScaleGestureDetector isn't processing a gesture.
						if (!mScaleDetector.isInProgress())
						{
							final int pointerIndex = ev.findPointerIndex(mActivePointerId);
							final float x = ev.getX(pointerIndex);
							final float y = ev.getY(pointerIndex);

							final float dx = x - mLastTouchX;
							final float dy = y - mLastTouchY;

							mPosX += dx;
							mPosY += dy;

							mLastTouchX = x;
							mLastTouchY = y;
						}
						else
						{
							final float gx = mScaleDetector.getFocusX();
							final float gy = mScaleDetector.getFocusY();

							final float gdx = gx - mLastGestureX;
							final float gdy = gy - mLastGestureY;

							mPosX += gdx;
							mPosY += gdy;

							mLastGestureX = gx;
							mLastGestureY = gy;
						}


						float scale = getImageScale();

						float compWidth = getWidth();
						float compHeight = getHeight();
						float imageWidth = Math.round(getImage().getWidth() * scale);
						float imageHeight = Math.round(getImage().getHeight() * scale);
						//float imageLeft = ((compWidth - imageWidth) / 2) + (int)mPosX;
						//float imageTop = ((compHeight - imageHeight) / 2) + (int)mPosY;
						//float imageRight = imageX + imageWidth;
						//float imageBottom  = imageY + imageHeight;

						// no pan if smaller, that's pointless
						if(imageWidth > compWidth)
						{
							mPosX = (int)mPosX < ((compWidth - imageWidth) / 2) ? ((compWidth - imageWidth) / 2) : mPosX;
							mPosX = (int)mPosX > ((imageWidth - compWidth) / 2) ? ((imageWidth - compWidth ) / 2) : mPosX;
						}
						else
						{
							mPosX = 0;
						}
						if(imageHeight > compHeight)
						{
							mPosY = (int)mPosY < ((compHeight - imageHeight) / 2) ? ((compHeight - imageHeight) / 2) : mPosY;
							mPosY = (int)mPosY > ((imageHeight - compHeight) / 2) ? ((imageHeight - compHeight ) / 2) : mPosY;
						}
						else
						{
							mPosY = 0;
						}

						invalidate();
						// stop the parent scrollview from scroll, notice very specific
						if(imageWidth > compWidth || imageHeight > compHeight)
							this.getParent().requestDisallowInterceptTouchEvent(true);

						break;
					}
					case MotionEvent.ACTION_UP:
					{
						mActivePointerId = INVALID_POINTER_ID;
						break;
					}
					case MotionEvent.ACTION_CANCEL:
					{
						mActivePointerId = INVALID_POINTER_ID;
						break;
					}
					case MotionEvent.ACTION_POINTER_UP:
					{
						final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
								>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
						final int pointerId = ev.getPointerId(pointerIndex);
						if (pointerId == mActivePointerId)
						{
							Log.d("DEBUG", "mActivePointerId");
							// This was our active pointer going up. Choose a new
							// active pointer and adjust accordingly.
							final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
							mLastTouchX = ev.getX(newPointerIndex);
							mLastTouchY = ev.getY(newPointerIndex);
							mActivePointerId = ev.getPointerId(newPointerIndex);
						}

						break;
					}
				}
			}

			return true;
		}

/*		@Override
		public void onDraw(Canvas canvas) {

			canvas.save();

			canvas.translate(mPosX, mPosY);

			if (mScaleDetector.isInProgress()) {
				canvas.scale(scaleMultiplier, scaleMultiplier, mScaleDetector.getFocusX(), mScaleDetector.getFocusY());
			}
			else{
				canvas.scale(scaleMultiplier, scaleMultiplier);
			}
			super.onDraw(canvas);
			canvas.restore();
		}*/

		private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
			@Override
			public boolean onScale(ScaleGestureDetector detector) {
				scaleMultiplier *= detector.getScaleFactor();

				// Don't let the object get too small or too large.
				scaleMultiplier = Math.max(0.1f, Math.min(scaleMultiplier, 10.0f));

				invalidate();
				return true;
			}
		}

		@Override
		protected Point getImageTranslation() {
			float scale = getImageScale();
			return new Point(((getWidth() - Math.round(image.getWidth() * scale)) / 2) + (int)mPosX,
					((getHeight() - Math.round(image.getHeight() * scale)) / 2) + (int)mPosY);
		}

	}

	@Override
	public void dismissed()
	{
		((Renovations3DActivity)activity).getAdMobManager().eventTriggered(AdMobManager.InterstitialEventType.IMPORT_BACKGROUND);
		((Renovations3DActivity)activity).getAdMobManager().interstitialDisplayPoint();
	}
}
