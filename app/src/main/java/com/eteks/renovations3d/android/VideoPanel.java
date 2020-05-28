/*
 * VideoPanel.java 15 fevr. 2010
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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import javaawt.AlphaComposite;
import javaawt.BasicStroke;

import javaawt.Color;

import javaawt.Composite;

import javaawt.Dimension;
import javaawt.EventQueue;
import javaawt.Graphics;
import javaawt.Graphics2D;

import javaawt.Image;
import javaawt.Insets;


import javaawt.Point;
import javaawt.VMGraphics2D;
import javaawt.geom.AffineTransform;
import javaawt.geom.Ellipse2D;
import javaawt.geom.GeneralPath;
import javaawt.geom.Line2D;
import javaawt.geom.Rectangle2D;
import javaawt.image.BufferedImage;
import javaawt.image.VMBufferedImage;
import javaxswing.ImageIcon;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.GregorianCalendar;
import java.util.Hashtable;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.TimeZone;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jogamp.vecmath.Point3f;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.utils.AndroidFloatingView;
import com.eteks.renovations3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.PhotoRenderer;
import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeEnvironment;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;
import com.eteks.sweethome3d.viewcontroller.VideoController;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ActioningTimerTask;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JComponent.DrawableView;
import com.mindblowing.swingish.JImageButton;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JPanel;
import com.mindblowing.swingish.JProgressBar;
import com.mindblowing.swingish.JSlider;
import com.mindblowing.swingish.JSpinnerDate;
import com.mindblowing.swingish.SpinnerDateModel;

import net.cachapa.expandablelayout.ExpandableLayout;

import static android.os.Build.VERSION_CODES.M;

/**
 * A panel used for video creation. 
 * @author Emmanuel Puybaret
 */
public class VideoPanel extends AndroidFloatingView implements DialogView {
  private enum ActionType {START_VIDEO_CREATION, STOP_VIDEO_CREATION, SAVE_VIDEO, SHARE_VIDEO, //close button handled by super class CLOSE,
		DELETE_CAMERA_PATH, PLAYBACK, PAUSE, RECORD, SEEK_BACKWARD, SEEK_FORWARD, SKIP_BACKWARD, SKIP_FORWARD, DELETE_LAST_RECORD}

  //private static final String VIDEO_DIALOG_X_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.VideoPanel.VideoDialogX";
  //private static final String VIDEO_DIALOG_Y_VISUAL_PROPERTY = "com.eteks.sweethome3d.swing.VideoPanel.VideoDialogY";

  private static final int MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING = 30000;
  
  private static final VideoFormat [] VIDEO_FORMATS = {
      new VideoFormat(VideoFormat.JPEG, new Dimension(176, 132), Format.NOT_SPECIFIED, Format.byteArray, 12), // 4/3
      new VideoFormat(VideoFormat.JPEG, new Dimension(320, 240), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(480, 360), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(640, 480), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(720, 540), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1024, 768), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1280, 960), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(720, 405), Format.NOT_SPECIFIED, Format.byteArray, 25), // 16/9
      new VideoFormat(VideoFormat.JPEG, new Dimension(1280, 536), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1280, 720), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1920, 800), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(1920, 1080), Format.NOT_SPECIFIED, Format.byteArray, 25),
      new VideoFormat(VideoFormat.JPEG, new Dimension(3840, 2160), Format.NOT_SPECIFIED, Format.byteArray, 25),
      //new VideoFormat(VideoFormat.JPEG, new Dimension(4096, 1728), Format.NOT_SPECIFIED, Format.byteArray, 25),
      //new VideoFormat(VideoFormat.JPEG, new Dimension(7680, 4320), Format.NOT_SPECIFIED, Format.byteArray, 25)
  };

  private static final String TIP_CARD      = "tip";
  private static final String PROGRESS_CARD = "progress";

  private final Home            home;
  private final UserPreferences preferences;
  private final Object3DFactory object3dFactory;
  private final VideoController controller;
  private PlanComponent         planComponent; 
  //private JToolBar              videoToolBar;
  private JImageButton					playbackPauseButton;
  private Timer 								playbackTimer;
  private ActioningTimerTask 		timerTask;
  private ListIterator<Camera>  cameraPathIterator;
  //private CardLayout            statusLayout;
  private JPanel 								statusPanel;
  private JLabel 								tipLabel;
  private JLabel                progressLabel;
  private JProgressBar 					progressBar;
  private JLabel                videoFormatLabel;
  private JComboBox 						videoFormatComboBox;
  private String                videoFormatComboBoxFormat;
  private JLabel                qualityLabel;
  private JSlider 							qualitySlider;
	private JLabel                fastLabel;
	private JLabel                bestLabel;
  //private Component             advancedComponentsSeparator;
  private JLabel                dateLabel;
  private JSpinnerDate 					dateSpinner;
  private JLabel                timeLabel;
  private JSpinnerDate          timeSpinner;
  private ImageView             dayNightLabel;
  private JCheckBox 						ceilingLightEnabledCheckBox;
  private String                dialogTitle;
  private ExecutorService       videoCreationExecutor;
  private long                  videoCreationStartTime;
  private File                  videoFile;
  private JButton               createButton;
  private JButton               saveButton;
	private JButton               shareButton;

  private static VideoPanel     currentVideoPanel; // Support only one video panel opened at a time

	private int rotationAngle1 = 90;
	private int rotationAngle2 = 270;

  /**
   * Creates a video panel with default object 3D factory.
   */
  public VideoPanel(Home home, 
                    UserPreferences preferences, 
                    VideoController controller, Activity activity) {
    this(home, preferences, null, controller, activity);
  }
  
  public VideoPanel(Home home, 
                    UserPreferences preferences, 
                    Object3DFactory object3dFactory,
                    VideoController controller, Activity activity) {
    super(preferences, activity, activity.getWindow().getDecorView(), R.layout.videopanelpopup);
    this.home = home;
    this.preferences = preferences;
    this.object3dFactory = object3dFactory;
    this.controller = controller;
		createActions(preferences);
    createComponents(home, preferences, controller);
    setMnemonics(preferences);
    layoutComponents();    

    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, new LanguageChangeListener(this));
  }
  
  /**
   * Creates actions for variables.
   */
  private void createActions(UserPreferences preferences) {
		final ActionMap actions = getActionMap();
		actions.put(ActionType.PLAYBACK,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "PLAYBACK.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.PLAYBACK).setEnabled(false);
		actions.get(ActionType.PLAYBACK).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				playback();
			}
		});
		actions.put(ActionType.PAUSE,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "PAUSE.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.PAUSE).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				pausePlayback();
			}
		});
		actions.put(ActionType.RECORD,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "RECORD.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.RECORD).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				recordCameraLocation();
			}
		});
		actions.put(ActionType.SEEK_BACKWARD,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "SEEK_BACKWARD.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.SEEK_BACKWARD).setEnabled(false);
		actions.get(ActionType.SEEK_BACKWARD).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				seekBackward();
			}
		});
		actions.put(ActionType.SEEK_FORWARD,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "SEEK_FORWARD.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.SEEK_FORWARD).setEnabled(false);
		actions.get(ActionType.SEEK_FORWARD).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				seekForward();
			}
		});
		actions.put(ActionType.SKIP_BACKWARD,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "SKIP_BACKWARD.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.SKIP_BACKWARD).setEnabled(false);
		actions.get(ActionType.SKIP_BACKWARD).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				skipBackward();
			}
		});
		actions.put(ActionType.SKIP_FORWARD,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "SKIP_FORWARD.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.SKIP_FORWARD).setEnabled(false);
		actions.get(ActionType.SKIP_FORWARD).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				skipForward();
			}
		});
		actions.put(ActionType.DELETE_LAST_RECORD,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "DELETE_LAST_RECORD.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.DELETE_LAST_RECORD).setEnabled(false);
		actions.get(ActionType.DELETE_LAST_RECORD).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				deleteLastRecordedCameraLocation();
			}
		});
		actions.put(ActionType.DELETE_CAMERA_PATH,
						new JImageButton(activity, (Bitmap) SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource(
						SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "DELETE_CAMERA_PATH.SmallIcon"))).getImage().getDelegate()));
		actions.get(ActionType.DELETE_CAMERA_PATH).setEnabled(false);
		actions.get(ActionType.DELETE_CAMERA_PATH).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				deleteCameraPath();
			}
		});

		actions.put(ActionType.START_VIDEO_CREATION,
						new JButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "START_VIDEO_CREATION.Name")));
		actions.get(ActionType.START_VIDEO_CREATION).setEnabled(false);
		actions.get(ActionType.START_VIDEO_CREATION).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				startVideoCreation();
			}
		});
		actions.put(ActionType.STOP_VIDEO_CREATION,
						new JButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "STOP_VIDEO_CREATION.Name")));
		actions.get(ActionType.STOP_VIDEO_CREATION).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				stopVideoCreation(true);
			}
		});
		actions.put(ActionType.SAVE_VIDEO,
						new JButton(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.VideoPanel.class, "SAVE_VIDEO.Name")));
		actions.get(ActionType.SAVE_VIDEO).setEnabled(false);
		actions.get(ActionType.SAVE_VIDEO).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				saveVideo(false);
			}
		});
		actions.put(ActionType.SHARE_VIDEO,
						new JButton(activity, activity.getResources().getString(R.string.share)));
		actions.get(ActionType.SHARE_VIDEO).setEnabled(false);
		actions.get(ActionType.SHARE_VIDEO).setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				saveVideo(true);
			}
		});

  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(final Home home, 
                                final UserPreferences preferences,
                                final VideoController controller) {
    final Dimension preferredSize = new Dimension(404, 404);//getToolkit().getScreenSize().width <= 1024 ? 324 : 404, 404);
    this.planComponent = new PlanComponent() {//constructor parameters moved to init call after anonymous class definition
        private void updateScale() {
          if (getWidth() > 0 && getHeight() > 0) {
            // Adapt scale to always view the home  
            //float oldScale = getScale();
            Dimension preferredSize = getPreferredSize();
            //Insets insets = getInsets();
            //float planWidth = (preferredSize.width - insets.left - insets.right) / oldScale;
            //float planHeight = (preferredSize.height - insets.top - insets.bottom) / oldScale;
            //setScale(Math.min((getWidth() - insets.left - insets.right) / planWidth,
            //    (getHeight() - insets.top - insets.bottom) / planHeight));
						setScale(Math.min((float)preferredSize.width/(float)getWidth(),  (float)preferredSize.height/(float)getHeight()), false);// single image no scroll
          }
        }

				@Override
				public int getWidth() {
					return (int)getPlanBounds().getWidth();
				}
				@Override
				public int getHeight() {
					return (int)getPlanBounds().getHeight();
				}
        //@Override
        public Dimension getPreferredSize() {
          return preferredSize;
        }

				protected void invalidate(boolean invalidatePlanBoundsCache) {
          super.invalidate(invalidatePlanBoundsCache);
					preferredSize.setSize(getDrawableView().getWidth(), getDrawableView().getHeight());
          if(invalidatePlanBoundsCache)
          	updateScale();
        }

        //@Override
        public void setBounds(int x, int y, int width, int height) {
          //super.setBounds(x, y, width, height);
          updateScale();
        }
        
        @Override
        protected List<Selectable> getPaintedItems() {
          List<Selectable> paintedItems = super.getPaintedItems();
          // Take into account camera locations in plan bounds
          for (Camera camera : controller.getCameraPath()) {
            paintedItems.add(new ObserverCamera(camera.getX(), camera.getY(), camera.getZ(), 
                camera.getYaw(), camera.getPitch(), camera.getFieldOfView()));
          }
          return paintedItems;
        }
        
        @Override
        protected Rectangle2D getItemBounds(Graphics g, Selectable item) {
          if (item instanceof ObserverCamera) {
            return new Rectangle2D.Float(((ObserverCamera)item).getX() - 1, ((ObserverCamera)item).getY() - 1, 2, 2);
          } else {
            return super.getItemBounds(g, item);
          }
        }
        
        @Override
        public void paintComponent(Graphics g) {
          Graphics2D g2D = (Graphics2D)g;
          g2D.setColor(getBackground());
          g2D.fillRect(0, 0, getWidth(), getHeight());
          super.paintComponent(g);
        }

        @Override
        protected void paintHomeItems(Graphics g, float planScale, Color backgroundColor, Color foregroundColor,
                                      PaintMode paintMode) throws InterruptedIOException {
          Graphics2D g2D = (Graphics2D)g;
          Composite oldComposite = g2D.getComposite();
          g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.25f));
          super.paintHomeItems(g, planScale, backgroundColor, foregroundColor, paintMode);
          
          // Paint recorded camera path
          g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1));
          g2D.setColor(getSelectionColor());
          float cameraCircleRadius = 7 / getScale();
          Ellipse2D ellipse = new Ellipse2D.Float(-cameraCircleRadius, -cameraCircleRadius, 
              2 * cameraCircleRadius, 2 * cameraCircleRadius);
          List<Camera> cameraPath = controller.getCameraPath();
          for (int i = 0; i < cameraPath.size(); i++) {
            Camera camera = cameraPath.get(i);
            AffineTransform previousTransform = g2D.getTransform();
            g2D.translate(camera.getX(), camera.getY());
            g2D.rotate(camera.getYaw());
            // Paint camera location
            g2D.fill(ellipse);
            // Paint field of sight angle
            double sin = (float)Math.sin(camera.getFieldOfView() / 2);
            double cos = (float)Math.cos(camera.getFieldOfView() / 2);
            float xStartAngle = (float)(1.2f * cameraCircleRadius * sin);
            float yStartAngle = (float)(1.2f * cameraCircleRadius * cos);
            float xEndAngle = (float)(2.5f * cameraCircleRadius * sin);
            float yEndAngle = (float)(2.5f * cameraCircleRadius * cos);
            GeneralPath cameraFieldOfViewAngle = new GeneralPath();
            g2D.setStroke(new BasicStroke(1 / getScale()));
            cameraFieldOfViewAngle.moveTo(xStartAngle, yStartAngle);
            cameraFieldOfViewAngle.lineTo(xEndAngle, yEndAngle);
            cameraFieldOfViewAngle.moveTo(-xStartAngle, yStartAngle);
            cameraFieldOfViewAngle.lineTo(-xEndAngle, yEndAngle);
            g2D.draw(cameraFieldOfViewAngle);
            g2D.setTransform(previousTransform);
            
            if (i > 0) {
              g2D.setStroke(new BasicStroke(2 / getScale()));
              g2D.draw(new Line2D.Float(camera.getX(), camera.getY(), 
                  cameraPath.get(i - 1).getX(), cameraPath.get(i - 1).getY()));
            }
          }
          g2D.setComposite(oldComposite);
        }

      };
    //PJ constructor parameters
		this.planComponent.init(home, preferences, null);
		//Undo the saved scroll position set that just occured
		planComponent.setScrollPosition(new Point(0,0));

		DrawableView drawableView = (DrawableView) findViewById(R.id.videopanel_planComponent);
		drawableView.setDrawer(planComponent);
		planComponent.setDrawableView(drawableView);

		this.planComponent.setSelectedItemsOutlinePainted(false);
    this.planComponent.setBackgroundPainted(false);
    //this.planComponent.setBorder(BorderFactory.createEtchedBorder());
    this.controller.addPropertyChangeListener(VideoController.Property.CAMERA_PATH, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            planComponent.revalidate();
            updatePlaybackTimer();
          }
        });

    // Create tool bar to play recorded animation in 3D view 
    //this.videoToolBar = new JToolBar();
    //this.videoToolBar.setFloatable(false);
    final ActionMap actionMap = getActionMap();
    //this.videoToolBar.add(actionMap.get(ActionType.DELETE_CAMERA_PATH));
    //this.videoToolBar.addSeparator();
    //this.videoToolBar.add(actionMap.get(ActionType.SKIP_BACKWARD));
    //this.videoToolBar.add(actionMap.get(ActionType.SEEK_BACKWARD));
    //this.videoToolBar.add(actionMap.get(ActionType.RECORD));
		this.playbackPauseButton =  (JImageButton)actionMap.get(ActionType.PLAYBACK);
    //this.videoToolBar.add(this.playbackPauseButton);
    //this.videoToolBar.add(actionMap.get(ActionType.SEEK_FORWARD));
    //this.videoToolBar.add(actionMap.get(ActionType.SKIP_FORWARD));
    //this.videoToolBar.addSeparator();
    //this.videoToolBar.add(actionMap.get(ActionType.DELETE_LAST_RECORD));
    //for (int i = 0; i < videoToolBar.getComponentCount(); i++) {
    //  Component component = this.videoToolBar.getComponent(i);
    //  if (component instanceof JButton) {
    //    JButton button = (JButton)component;
    //    button.setBorderPainted(true);
    //    button.setFocusable(true);
    //  }
    //}

    this.tipLabel = new JLabel(activity, "");
    //Font toolTipFont = UIManager.getFont("ToolTip.font");
    //this.tipLabel.setFont(toolTipFont);
    
    this.progressLabel = new JLabel(activity, "");
    //this.progressLabel.setFont(toolTipFont);
    //this.progressLabel.setHorizontalAlignment(JLabel.CENTER);
    
    this.progressBar = new JProgressBar(activity, 0, 1, 0);
    this.progressBar.addChangeListener(new ChangeListener() {
			private long timeAfterFirstImage;

        public void stateChanged(ChangeEvent ev) {
          int progressValue = progressBar.getValue();
          progressBar.setIndeterminate(progressValue <= progressBar.getMinimum() + 1);
          if (progressValue == progressBar.getMinimum()
              || progressValue == progressBar.getMaximum()) {
            progressLabel.setText("");
            if (progressValue == progressBar.getMinimum()) {
              int framesCount = progressBar.getMaximum() - progressBar.getMinimum();
              String progressLabelFormat = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "progressStartLabel.format");
              progressLabel.setText(String.format(progressLabelFormat, framesCount,
                  formatDuration(framesCount * 1000 / controller.getFrameRate())));
            }
          } else if (progressValue == progressBar.getMinimum() + 1) {
            this.timeAfterFirstImage = System.currentTimeMillis(); 
          } else {
            // Update progress label once the second image is generated 
            // (the first one can take more time because of initialization process)
            String progressLabelFormat = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "progressLabel.format");
            long estimatedRemainingTime = (System.currentTimeMillis() - this.timeAfterFirstImage) 
                / (progressValue - 1 - progressBar.getMinimum())  
                * (progressBar.getMaximum() - progressValue - 1);
            String estimatedRemainingTimeText = formatDuration(estimatedRemainingTime);
            progressLabel.setText(String.format(progressLabelFormat, 
                progressValue, progressBar.getMaximum(), estimatedRemainingTimeText));
          }          
        }

        /**
         * Returns a localized string of <code>duration</code> in millis.
         */
        private String formatDuration(long duration) {
          long durationInSeconds = duration / 1000;
          if (duration - durationInSeconds * 1000 >= 500) {
            durationInSeconds++;
          }
          String estimatedRemainingTimeText;
          if (durationInSeconds < 60) {
            estimatedRemainingTimeText = String.format(preferences.getLocalizedString(
										com.eteks.sweethome3d.android_props.VideoPanel.class, "seconds.format"), durationInSeconds);
          } else if (durationInSeconds < 3600) {
            estimatedRemainingTimeText = String.format(preferences.getLocalizedString(
										com.eteks.sweethome3d.android_props.VideoPanel.class, "minutesSeconds.format"), durationInSeconds / 60, durationInSeconds % 60);
          } else {
            long hours = durationInSeconds / 3600;
            long minutes = (durationInSeconds % 3600) / 60;
            estimatedRemainingTimeText = String.format(preferences.getLocalizedString(
										com.eteks.sweethome3d.android_props.VideoPanel.class, "hoursMinutes.format"), hours, minutes);
          }
          return estimatedRemainingTimeText;
        }
      });
    
    // Create video format label and combo box bound to WIDTH, HEIGHT, ASPECT_RATIO and FRAME_RATE controller properties
    this.videoFormatLabel = new JLabel(activity, "");
    this.videoFormatComboBox = new JComboBox(activity, VIDEO_FORMATS);
    this.videoFormatComboBox.setMaximumRowCount(VIDEO_FORMATS.length);
		this.videoFormatComboBox.setAdapter(new ArrayAdapter<VideoFormat>(activity, android.R.layout.simple_list_item_1, VIDEO_FORMATS) {
			@Override
			public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
				return getDropDownView(position, convertView, parent);
			}

			@Override
			public android.view.View getDropDownView(int position, android.view.View convertView, ViewGroup parent) {
				TextView ret = new TextView(getContext());
				VideoFormat value = (VideoFormat) videoFormatComboBox.getItemAtPosition(position);
				VideoFormat videoFormat = (VideoFormat)value;
				String aspectRatio;
				Dimension size = videoFormat.getSize();
				switch (getAspectRatio(size.width, size.height)) {
					case RATIO_4_3 :
						aspectRatio = preferences.getLocalizedString(
										com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "aspectRatioComboBox.4_3Ratio.text");
						break;
					case RATIO_24_10 :
						aspectRatio = preferences.getLocalizedString(
										com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "aspectRatioComboBox.2.40_1Ratio.text");
						break;
					default :
					case RATIO_16_9 :
						aspectRatio = preferences.getLocalizedString(
										com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "aspectRatioComboBox.16_9Ratio.text");
						break;
				}
				Dimension videoSize = size;
				String displayedValue = String.format(videoFormatComboBoxFormat, videoSize.width, videoSize.height,
								aspectRatio, (int)videoFormat.getFrameRate());
				ret.setText(displayedValue);
				return ret;
			}
		});
    this.videoFormatComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          VideoFormat videoFormat = (VideoFormat)videoFormatComboBox.getSelectedItem();
          Dimension size = videoFormat.getSize();
          controller.setWidth(videoFormat.getSize().width);
          controller.setAspectRatio(getAspectRatio(size.width, size.height));
          controller.setFrameRate((int)videoFormat.getFrameRate());
        }
      });
    PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          videoFormatComboBox.setSelectedItem(getVideoFormat(controller.getWidth(), controller.getAspectRatio(), controller.getFrameRate()));
        }
      };
		this.videoFormatComboBox.setSelectedItem(videoFormatComboBox.getItemAtPosition(0));
    controller.addPropertyChangeListener(VideoController.Property.WIDTH, propertyChangeListener);
    controller.addPropertyChangeListener(VideoController.Property.HEIGHT, propertyChangeListener);
    controller.addPropertyChangeListener(VideoController.Property.ASPECT_RATIO, propertyChangeListener);
    controller.addPropertyChangeListener(VideoController.Property.FRAME_RATE, propertyChangeListener);

    // Quality label and slider bound to QUALITY controller property
    this.qualityLabel = new JLabel(activity, "");
    Dimension imageSize;
    try {
      imageSize = SwingTools.getImageSizeInPixels(new ResourceURLContent(com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class,
            "resources/quality0.jpg"));
    } catch (IOException ex) {
      // Shouldn't happen since resource exists
      imageSize = null;
    }
    float resolutionScale = SwingTools.getResolutionScale();
    final int imageWidth = (int)(imageSize.width * resolutionScale);
    final int imageHeight = (int)(imageSize.height * resolutionScale);
		//PJ can't use non 0 min so just go 0-3 and note teh min calls below sort it out when send to from controller (which is 0-3)
    this.qualitySlider = new JSlider(activity, 0, controller.getQualityLevelCount() - 1) {
        /*@Override
        public String getToolTipText(MouseEvent ev) {
          float valueUnderMouse = getSliderValueAt(this, ev.getX(), preferences);
          float valueToTick = valueUnderMouse - (float)Math.floor(valueUnderMouse);
          if (valueToTick < 0.25f || valueToTick > 0.75f) {
            // Display a tooltip that explains the different quality levels
            URL imageUrl = new ResourceURLContent(com.eteks.sweethome3d.android_props.VideoPanel.class, "resources/quality" + Math.round(valueUnderMouse - qualitySlider.getMinimum()) + ".jpg").getURL();
            String imageHtmlCell = "<td><img border='1' width='" + imageWidth + "' height='" + imageHeight + "' src='" + imageUrl + "'></td>";
            String description = preferences.getLocalizedString(PhotoSizeAndQualityPanel.class, "quality" + Math.round(valueUnderMouse - qualitySlider.getMinimum()) + "DescriptionLabel.text");
            boolean leftToRightOrientation = false;//qualitySlider.getComponentOrientation().isLeftToRight();
            String descriptionHtmlCell = "<td align='" + (leftToRightOrientation ? "left" : "right") + "'>" + description + "</td>";
            return "<html><table><tr valign='middle'>"
                + (leftToRightOrientation ? imageHtmlCell + descriptionHtmlCell : descriptionHtmlCell + imageHtmlCell)
                + "</tr></table>";
          } else {
            return null;
          }
        }*/
      };
    // Add a listener that displays also the tool tip when user clicks on the slider
    /*this.qualitySlider.addMouseListener(new MouseAdapter() {
        @Override
        public void mousePressed(final MouseEvent ev) {
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                float valueUnderMouse = getSliderValueAt(qualitySlider, ev.getX(), preferences);
                if (qualitySlider.getValue() == Math.round(valueUnderMouse)) {
                  ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
                  int initialDelay = toolTipManager.getInitialDelay();
                  toolTipManager.setInitialDelay(Math.min(initialDelay, 150));
                  toolTipManager.mouseMoved(ev);
                  toolTipManager.setInitialDelay(initialDelay);
                }
              }
            });
        }
      });*/
    //this.qualitySlider.setPaintLabels(true);
    this.qualitySlider.setPaintTicks(true);    
    this.qualitySlider.setMajorTickSpacing(1);
    this.qualitySlider.setSnapToTicks(true);
    final boolean offScreenImageSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    this.qualitySlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (!offScreenImageSupported) {
            // Can't support 2 first quality levels if offscreen image isn't supported 
            qualitySlider.setValue(Math.max(qualitySlider.getMinimum() + 2, qualitySlider.getValue()));
          }
          controller.setQuality(qualitySlider.getValue() - qualitySlider.getMinimum());
        }
      });
    controller.addPropertyChangeListener(VideoController.Property.QUALITY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            qualitySlider.setValue(qualitySlider.getMinimum() + controller.getQuality());
            updateAdvancedComponents();
          }
        });
    this.qualitySlider.setValue(this.qualitySlider.getMinimum() + controller.getQuality());

		this.fastLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "fastLabel.text"));
		this.bestLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "bestLabel.text"));
    //this.advancedComponentsSeparator = new JSeparator();

    // Create date and time labels and spinners bound to TIME controller property
    Date time = new Date(Camera.convertTimeToTimeZone(controller.getTime(), TimeZone.getDefault().getID()));
    this.dateLabel = new JLabel(activity, "");
    final SpinnerDateModel dateSpinnerModel = new SpinnerDateModel();
    dateSpinnerModel.setValue(time);
    this.dateSpinner = new JSpinnerDate(activity, dateSpinnerModel);
    String datePattern = ((SimpleDateFormat)DateFormat.getDateInstance(DateFormat.SHORT)).toPattern();
    if (datePattern.indexOf("yyyy") == -1) {
      datePattern = datePattern.replace("yy", "yyyy");
    }
		dateSpinner.setTimePattern(datePattern);
    //JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(this.dateSpinner, datePattern);
    //this.dateSpinner.setEditor(dateEditor);
    //SwingTools.addAutoSelectionOnFocusGain(dateEditor.getTextField());
    
    this.timeLabel = new JLabel(activity, "");
    final SpinnerDateModel timeSpinnerModel = new SpinnerDateModel();
    timeSpinnerModel.setValue(time);
    this.timeSpinner = new JSpinnerDate(activity, timeSpinnerModel);
    // From http://en.wikipedia.org/wiki/12-hour_clock#Use_by_country
    String [] twelveHoursCountries = { 
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
    if ("en".equals(Locale.getDefault().getLanguage())) {
      if (Arrays.binarySearch(twelveHoursCountries, Locale.getDefault().getCountry()) >= 0) {
        timeInstance = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, Locale.US); // 12 hours notation
      } else {
        timeInstance = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT, Locale.UK); // 24 hours notation
      }
    } else {
      timeInstance = (SimpleDateFormat)DateFormat.getTimeInstance(DateFormat.SHORT);
    }
		timeSpinner.setTimePattern(timeInstance.toPattern());

    final PropertyChangeListener timeChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        Date date = new Date(Camera.convertTimeToTimeZone(controller.getTime(), TimeZone.getDefault().getID()));
        dateSpinnerModel.setValue(date);
        timeSpinnerModel.setValue(date);
      }
    };
    controller.addPropertyChangeListener(VideoController.Property.TIME, timeChangeListener);
    final ChangeListener dateTimeChangeListener = new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(VideoController.Property.TIME, timeChangeListener);
          // Merge date and time
          GregorianCalendar dateCalendar = new GregorianCalendar();
          dateCalendar.setTime((Date)dateSpinnerModel.getValue());
          GregorianCalendar timeCalendar = new GregorianCalendar();
          timeCalendar.setTime((Date)timeSpinnerModel.getValue());
          Calendar utcCalendar = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
          utcCalendar.set(GregorianCalendar.YEAR, dateCalendar.get(GregorianCalendar.YEAR));
          utcCalendar.set(GregorianCalendar.MONTH, dateCalendar.get(GregorianCalendar.MONTH));
          utcCalendar.set(GregorianCalendar.DAY_OF_MONTH, dateCalendar.get(GregorianCalendar.DAY_OF_MONTH));
          utcCalendar.set(GregorianCalendar.HOUR_OF_DAY, timeCalendar.get(GregorianCalendar.HOUR_OF_DAY));
          utcCalendar.set(GregorianCalendar.MINUTE, timeCalendar.get(GregorianCalendar.MINUTE));
          utcCalendar.set(GregorianCalendar.SECOND, timeCalendar.get(GregorianCalendar.SECOND));
          controller.setTime(utcCalendar.getTimeInMillis());
          controller.addPropertyChangeListener(VideoController.Property.TIME, timeChangeListener);
        }
      };
    dateSpinnerModel.addChangeListener(dateTimeChangeListener);
    timeSpinnerModel.addChangeListener(dateTimeChangeListener);

    this.dayNightLabel = new ImageView(activity);
    final ImageIcon dayIcon = SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource("resources/day.png"));
    final ImageIcon nightIcon = SwingTools.getScaledImageIcon(com.eteks.sweethome3d.android_props.VideoPanel.class.getResource("resources/night.png"));
    PropertyChangeListener dayNightListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          if (home.getCompass().getSunElevation(
                Camera.convertTimeToTimeZone(controller.getTime(), home.getCompass().getTimeZone())) > 0) {
						dayNightLabel.setImageBitmap(((Bitmap) dayIcon.getImage().getDelegate()));
          } else {
						dayNightLabel.setImageBitmap(((Bitmap) nightIcon.getImage().getDelegate()));
          }
        }
      };
    controller.addPropertyChangeListener(VideoController.Property.TIME, dayNightListener);
    home.getCompass().addPropertyChangeListener(dayNightListener);
    dayNightListener.propertyChange(null);
    
    this.ceilingLightEnabledCheckBox = new JCheckBox(activity, "");
    this.ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
    controller.addPropertyChangeListener(VideoController.Property.CEILING_LIGHT_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            ceilingLightEnabledCheckBox.setSelected(controller.getCeilingLightColor() > 0);
          }
        });
    this.ceilingLightEnabledCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setCeilingLightColor(ceilingLightEnabledCheckBox.isSelected() ? 0xD0D0D0 : 0);
        }
      });

		this.createButton = (JButton)actionMap.get(ActionType.START_VIDEO_CREATION);
		this.saveButton = (JButton)actionMap.get(ActionType.SAVE_VIDEO);
		this.shareButton = (JButton)actionMap.get(ActionType.SAVE_VIDEO);
		//this.closeButton = (JButton)actionMap.get(ActionType.CLOSE);

		setComponentTexts(preferences);
		updatePlaybackTimer();

		this.videoFormatComboBox.setSelectedItem(getVideoFormat(controller.getWidth(), controller.getAspectRatio(), controller.getFrameRate()));

		//now make the expander work
		android.view.View expander1 = findViewById(R.id.video_settings_expander);
		expander1.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				rotationAngle1 = rotationAngle1 == 90 ? 270 : 90;  //toggle
				view.animate().rotation(rotationAngle1).setDuration(500).start();
				((ExpandableLayout)findViewById(R.id.expandable_layout_video_settings)).toggle();
			}
		});
		expander1.animate().rotation(90).setDuration(5).start();

		android.view.View expander2 = findViewById(R.id.plan_panel_expander);
		expander2.setOnClickListener(new android.view.View.OnClickListener() {
			public void onClick(android.view.View view) {
				rotationAngle2 = rotationAngle2 == 270 ? 90 : 270;  //toggle
				view.animate().rotation(rotationAngle2).setDuration(500).start();
				((ExpandableLayout)findViewById(R.id.expandable_layout_plan_panel)).toggle();
			}
		});
		expander2.animate().rotation(270).setDuration(5).start();


  }

  private VideoFormat getVideoFormat(int width, AspectRatio aspectRatio, int frameRate) {
    for (VideoFormat videoFormat : VIDEO_FORMATS) {
      if (videoFormat.getSize().width == width
          && getAspectRatio(videoFormat.getSize().width, videoFormat.getSize().height) == aspectRatio
          && videoFormat.getFrameRate() == frameRate) {
        return videoFormat;
      }
    }
    return VIDEO_FORMATS [0];
  }

  /**
   * Returns the slider value matching a given x.
   */
/*  private float getSliderValueAt(JSlider qualitySlider, int x, UserPreferences preferences) {
    int fastLabelOffset = OperatingSystem.isLinux() 
        ? 0
        : new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "fastLabel.text")).getPreferredSize().width / 2;
    int bestLabelOffset = OperatingSystem.isLinux() 
        ? 0
        : new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "bestLabel.text")).getPreferredSize().width / 2;
    int sliderWidth = qualitySlider.getWidth() - fastLabelOffset - bestLabelOffset;
    return qualitySlider.getMinimum()
        + (float)(qualitySlider.getComponentOrientation().isLeftToRight()
                    ? x - fastLabelOffset
                    : sliderWidth - x + bestLabelOffset)
        / sliderWidth * (qualitySlider.getMaximum() - qualitySlider.getMinimum());
  }*/
  
  /**
   * Sets the texts of the components.
   */
  private void setComponentTexts(UserPreferences preferences) {
    this.tipLabel.setText(Html.fromHtml(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "tipLabel.text").replace("<br>", " ")));
    this.videoFormatLabel.setText(preferences.getLocalizedString(
						com.eteks.sweethome3d.android_props.VideoPanel.class, "videoFormatLabel.text"));
    this.videoFormatComboBoxFormat = preferences.getLocalizedString(
						com.eteks.sweethome3d.android_props.VideoPanel.class, "videoFormatComboBox.format");
    //frames/seonds is far to long knock it all back to fps regardless of text
		videoFormatComboBoxFormat = videoFormatComboBoxFormat.substring(0,videoFormatComboBoxFormat.lastIndexOf(" ")) + " fps";
    this.qualityLabel.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "qualityLabel.text"));
    this.dateLabel.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "dateLabel.text"));
    this.timeLabel.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "timeLabel.text"));
    this.ceilingLightEnabledCheckBox.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "ceilingLightEnabledCheckBox.text"));
    this.fastLabel.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "fastLabel.text"));
    //if (!Component3DManager.getInstance().isOffScreenImageSupported()) {
    //  fastLabel.setEnabled(false);
    //}
    this.bestLabel.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.android_props.VideoPanel.class, "bestLabel.text"));
    //Dictionary<Integer, JComponent> qualitySliderLabelTable = new Hashtable<Integer,JComponent>();
    //qualitySliderLabelTable.put(this.qualitySlider.getMinimum(), fastLabel);
    //qualitySliderLabelTable.put(this.qualitySlider.getMaximum(), bestLabel);
    //this.qualitySlider.setLabelTable(qualitySliderLabelTable);
    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "createVideo.title");
    //Window window = SwingUtilities.getWindowAncestor(this);
    //if (window != null) {
    //  ((JDialog)window).setTitle(this.dialogTitle);
    //}
    // Buttons text changes automatically through their action
  }

  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
  }

  /**
   * Preferences property listener bound to this panel with a weak reference to avoid
   * strong link between user preferences and this panel.  
   */
  public static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<VideoPanel> videoPanel;

    public LanguageChangeListener(VideoPanel videoPanel) {
      this.videoPanel = new WeakReference<VideoPanel>(videoPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If video panel was garbage collected, remove this listener from preferences
      VideoPanel videoPanel = this.videoPanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (videoPanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        //videoPanel.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        videoPanel.setComponentTexts(preferences);
        videoPanel.setMnemonics(preferences);
      }
    }
  }

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
    // First row
		swapOut(this.planComponent.getView(), R.id.videopanel_planComponent);
    // Second row
		final ActionMap actions = getActionMap();
		swapOut(actions.get(ActionType.DELETE_CAMERA_PATH), R.id.videopanel_DELETE_CAMERA_PATHButton);
		swapOut(actions.get(ActionType.SKIP_BACKWARD), R.id.videopanel_SKIP_BACKWARDButton);
		swapOut(actions.get(ActionType.SEEK_BACKWARD), R.id.videopanel_SEEK_BACKWARDButton);
		swapOut(actions.get(ActionType.RECORD), R.id.videopanel_RECORDButton);
		swapOut(this.playbackPauseButton, R.id.videopanel_PLAYBACKButton);
		swapOut(actions.get(ActionType.SEEK_FORWARD), R.id.videopanel_SEEK_FORWARDButton);
		swapOut(actions.get(ActionType.SKIP_FORWARD), R.id.videopanel_SKIP_FORWARDButton);
		swapOut(actions.get(ActionType.DELETE_LAST_RECORD), R.id.videopanel_SDELETE_LAST_RECORDButton);
    // Third row
		swapOut(this.tipLabel, R.id.videopanel_tipLabel);
		swapOut(this.progressBar, R.id.videopanel_progressBar);
    // Fourth row
		swapOut(this.videoFormatLabel, R.id.videopanel_videoFormatLabel);
		swapOut(this.videoFormatComboBox, R.id.videopanel_videoFormatComboBox);
    // Fifth row
		swapOut(this.qualityLabel, R.id.videopanel_qualityLabel);
		swapOut(this.qualitySlider, R.id.videopanel_qualitySlider);
    // Sixth row
    // Seventh row
		swapOut(this.dateLabel, R.id.videopanel_dateLabel);
		swapOut(this.dateSpinner, R.id.videopanel_dateSpinner);
		swapOut(this.timeLabel, R.id.videopanel_timeLabel);
		swapOut(this.timeSpinner, R.id.videopanel_timeSpinner);
		swapOut(this.dayNightLabel, R.id.videopanel_dayNightLabel);
    // Last row
		swapOut(this.ceilingLightEnabledCheckBox, R.id.videopanel_ceilingLightEnabledCheckBox);

		this.setTitle(dialogTitle);
		swapOut(actions.get(ActionType.START_VIDEO_CREATION), R.id.videopanel_createButton);
		swapOut(actions.get(ActionType.SAVE_VIDEO), R.id.videopanel_saveButton);
		swapOut(actions.get(ActionType.SHARE_VIDEO), R.id.videopanel_shareButton);
		//swapOut(this.closeButton, R.id.videopanel_closeButton);

  }
  
  private void updateAdvancedComponents() {
    //Component root = SwingUtilities.getRoot(this);
    //if (root != null) {
      boolean highQuality = controller.getQuality() >= 2;
      //boolean advancedComponentsVisible = this.advancedComponentsSeparator.isVisible();
      //if (advancedComponentsVisible != highQuality) {
      //  int componentsHeight = this.advancedComponentsSeparator.getPreferredSize().height + 6
      //      + this.ceilingLightEnabledCheckBox.getPreferredSize().height;
      //  this.advancedComponentsSeparator.setVisible(highQuality);
        this.dateLabel.setEnabled(highQuality);
        this.dateSpinner.setEnabled(highQuality);
        this.timeLabel.setEnabled(highQuality);
        this.timeSpinner.setEnabled(highQuality);
        this.dayNightLabel.setEnabled(highQuality);
        this.ceilingLightEnabledCheckBox.setEnabled(highQuality);
      //  root.setSize(root.getWidth(),
      //      root.getHeight() + (advancedComponentsVisible ? -componentsHeight : componentsHeight));
     // }
    //}
  }

  /**
   * Displays this panel in a non modal dialog.
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
      if (currentVideoPanel != null) {
        currentVideoPanel.close();
      }

			this.setOnDismissListener(new PopupWindow.OnDismissListener() {
				@Override
				public void onDismiss() {
					stopVideoCreation(false);
					if (playbackTimer != null) {
						pausePlayback();
					}
					if (videoFile != null) {
						videoFile.delete();
					}
					currentVideoPanel = null;
				}
			});
      
      updateAdvancedComponents();

			this.showView();
      currentVideoPanel = this;
  }


  /**
   * Records the location and the angles of the current camera.
   */
  private void recordCameraLocation() {
    List<Camera> cameraPath = this.controller.getCameraPath();
    Camera camera = this.home.getCamera();    
    Camera lastCamera = null;    
    if (cameraPath.size() > 0) {
      lastCamera = cameraPath.get(cameraPath.size() - 1);
    }
    if (lastCamera == null      
        || !compareCameraLocation(lastCamera, camera)) {
      // Record only new locations
      cameraPath = new ArrayList<Camera>(cameraPath);
      Camera recordedCamera = camera.clone();
      recordedCamera.setLens(Camera.Lens.PINHOLE);
      recordedCamera.setTime(this.controller.getTime());
      cameraPath.add(recordedCamera);
      this.controller.setCameraPath(cameraPath);
    }
  }
  
  /**
   * Returns <code>true</code> if the given cameras are at the same location.
   */
  private boolean compareCameraLocation(Camera camera1, Camera camera2) {
    return camera1.getX() == camera2.getX() 
        && camera1.getY() == camera2.getY() 
        && camera1.getZ() == camera2.getZ() 
        && camera1.getYaw() == camera2.getYaw()
        && camera1.getPitch() == camera2.getPitch() 
        && camera1.getFieldOfView() == camera2.getFieldOfView()
        && camera1.getTime() == camera2.getTime();
  }

  /**
   * Updates the timer used for playback.
   */
  private void updatePlaybackTimer() {
    final List<Camera> cameraPath = this.controller.getCameraPath();
    final ActionMap actionMap = getActionMap();
    boolean playable = cameraPath.size() > 1;
    if (playable) {
      Camera [] videoFramesPath = getVideoFramesPath(this.controller.getSpeed(), 12);
      // Find current camera location
      Camera homeCamera = home.getCamera();
      int index = videoFramesPath.length;
      while (--index > 0 
          && !compareCameraLocation(videoFramesPath [index], homeCamera)) {        
      }
      // Prefer last location
      if (index < 0 || index == videoFramesPath.length - 1) {
        index = videoFramesPath.length;
      }
      this.cameraPathIterator = Arrays.asList(videoFramesPath).listIterator(index);
			this.timerTask = new ActioningTimerTask( new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					if ("backward".equals(ev.getActionCommand())) {
						if (cameraPathIterator.hasPrevious()) {
							Camera camera = cameraPathIterator.previous();
							home.getCamera().setCamera(camera);
							controller.setTime(camera.getTime());
						} else {
							pausePlayback();
						}
					} else {
						if (cameraPathIterator.hasNext()) {
							Camera camera = cameraPathIterator.next();
							home.getCamera().setCamera(camera);
							controller.setTime(camera.getTime());
						} else {
							pausePlayback();
						}
					}
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							boolean pathEditable = videoCreationExecutor == null;// && !((Timer) ev.getSource()).isRunning();
							actionMap.get(ActionType.RECORD).setEnabled(pathEditable);
							actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(pathEditable && cameraPath.size() > 0);
							actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(pathEditable && cameraPath.size() > 0);
							actionMap.get(ActionType.SEEK_BACKWARD).setEnabled(cameraPathIterator.hasPrevious());
							actionMap.get(ActionType.SKIP_BACKWARD).setEnabled(cameraPathIterator.hasPrevious());
							actionMap.get(ActionType.SEEK_FORWARD).setEnabled(cameraPathIterator.hasNext());
							actionMap.get(ActionType.SKIP_FORWARD).setEnabled(cameraPathIterator.hasNext());
						}});
				}
			});
			if (this.playbackTimer != null) {
				this.playbackTimer.cancel();
			}
			this.playbackTimer = new Timer("playbackTimer",true);
    }
		actionMap.get(ActionType.PLAYBACK).setEnabled(playable);
		actionMap.get(ActionType.RECORD).setEnabled(this.videoCreationExecutor == null);
    boolean emptyCameraPath = cameraPath.isEmpty();
		actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(this.videoCreationExecutor == null && !emptyCameraPath);
		actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(this.videoCreationExecutor == null && !emptyCameraPath);
		actionMap.get(ActionType.SEEK_BACKWARD).setEnabled(playable && this.cameraPathIterator.hasPrevious());
		actionMap.get(ActionType.SKIP_BACKWARD).setEnabled(playable && this.cameraPathIterator.hasPrevious());
		actionMap.get(ActionType.SEEK_FORWARD).setEnabled(playable && this.cameraPathIterator.hasNext());
		actionMap.get(ActionType.SKIP_FORWARD).setEnabled(playable && this.cameraPathIterator.hasNext());
		actionMap.get(ActionType.START_VIDEO_CREATION).setEnabled(playable);
  }

  /**
   * Deletes the last recorded camera location.
   */
  private void deleteLastRecordedCameraLocation() {
    List<Camera> cameraPath = new ArrayList<Camera>(this.controller.getCameraPath());
    cameraPath.remove(cameraPath.size() - 1);
    this.controller.setCameraPath(cameraPath);
  }

  /**
   * Deletes the recorded camera path.
   */
  private void deleteCameraPath() {
    List<Camera> cameraPath = Collections.emptyList();
    this.controller.setCameraPath(cameraPath);
  }

  /**
   * Plays back the camera locations. 
   */
  private void playback() {
    if (!this.cameraPathIterator.hasNext()) {
      skipBackward();
    }
		updatePlaybackTimer();
		this.playbackTimer.schedule(timerTask, 0,1000 / 12);
		this.playbackPauseButton = (JImageButton)getActionMap().get(ActionType.PAUSE);
		swapOut(this.playbackPauseButton, R.id.videopanel_PLAYBACKButton);
  }

  /**
   * Pauses play back. 
   */
  private void pausePlayback() {
    this.playbackTimer.cancel();
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				playbackPauseButton = (JImageButton) getActionMap().get(ActionType.PLAYBACK);
				swapOut(playbackPauseButton, R.id.videopanel_PLAYBACKButton);
				getActionMap().get(ActionType.RECORD).setEnabled(videoCreationExecutor == null);
				boolean emptyCameraPath = controller.getCameraPath().isEmpty();
				getActionMap().get(ActionType.DELETE_CAMERA_PATH).setEnabled(videoCreationExecutor == null && !emptyCameraPath);
				getActionMap().get(ActionType.DELETE_CAMERA_PATH).setEnabled(videoCreationExecutor == null && !emptyCameraPath);
			}});
  }

  /**
   * Moves quickly camera 10 steps backward. 
   */
  private void seekBackward() {
    for (int i = 0; i < 10 && this.cameraPathIterator.hasPrevious(); i++) {
      this.timerTask.getActionListeners() [0].actionPerformed(
          new ActionListener.ActionEvent(this.playbackTimer, 0, "backward", System.currentTimeMillis(), 0));
    }
  }
  
  /**
   * Moves quickly camera 10 steps forward. 
   */
  private void seekForward() {
    for (int i = 0; i < 10 && this.cameraPathIterator.hasNext(); i++) {
      this.timerTask.getActionListeners() [0].actionPerformed(
          new ActionListener.ActionEvent(this.playbackTimer, 0, "forward", System.currentTimeMillis(), 0));
    }
  }

  /**
   * Moves camera to animation start and restarts animation if it was running. 
   */
  private void skipBackward() {
    while (this.cameraPathIterator.hasPrevious()) {
      seekBackward();
    }
  }
  
  /**
   * Moves camera to animation end and stops animation. 
   */
  private void skipForward() {
    while (this.cameraPathIterator.hasNext()) {
      seekForward();
    }
  }

  /**
   * Returns the camera path that should be used to create each frame of an animation. 
   */
  private Camera [] getVideoFramesPath(final float speed, int frameRate) {
    List<Camera> videoFramesPath = new ArrayList<Camera>();
    final float moveDistancePerFrame = speed * 100f / frameRate;  // speed is in m/s
    final float moveAnglePerFrame = (float)(Math.PI / 120 * 30 * speed / frameRate);
    final float elapsedTimePerFrame = 345600 / frameRate * 25; // 250 frame/day at 25 frame/second
    
    List<Camera> cameraPath = this.controller.getCameraPath();
    Camera camera = cameraPath.get(0);
    float x = camera.getX(); 
    float y = camera.getY(); 
    float z = camera.getZ();
    float yaw = camera.getYaw(); 
    float pitch = camera.getPitch(); 
    float fieldOfView = camera.getFieldOfView();
    long  time = camera.getTime();
    videoFramesPath.add(camera.clone());
    
    for (int i = 1; i < cameraPath.size(); i++) {
      camera = cameraPath.get(i);                  
      float newX = camera.getX(); 
      float newY = camera.getY(); 
      float newZ = camera.getZ();
      float newYaw = camera.getYaw(); 
      float newPitch = camera.getPitch(); 
      float newFieldOfView = camera.getFieldOfView();
      long  newTime = camera.getTime();
      
      float distance = new Point3f(x, y, z).distance(new Point3f(newX, newY, newZ));
      float moveCount = distance / moveDistancePerFrame;
      float yawAngleCount = Math.abs(newYaw - yaw) / moveAnglePerFrame;
      float pitchAngleCount = Math.abs(newPitch - pitch) / moveAnglePerFrame;
      float fieldOfViewAngleCount = Math.abs(newFieldOfView - fieldOfView) / moveAnglePerFrame;
      float timeCount = Math.abs(newTime - time) / elapsedTimePerFrame;

      int frameCount = (int)Math.max(moveCount, Math.max(yawAngleCount, 
          Math.max(pitchAngleCount, Math.max(fieldOfViewAngleCount, timeCount))));
      
      float deltaX = (newX - x) / frameCount;
      float deltaY = (newY - y) / frameCount;
      float deltaZ = (newZ - z) / frameCount;
      float deltaYawAngle = (newYaw - yaw) / frameCount;
      float deltaPitchAngle = (newPitch - pitch) / frameCount;
      float deltaFieldOfViewAngle = (newFieldOfView - fieldOfView) / frameCount;
      long deltaTime = Math.round(((double)newTime - time) / frameCount);
      
      for (int j = 1; j <= frameCount; j++) {
        videoFramesPath.add(new Camera(
            x + deltaX * j, y + deltaY * j, z + deltaZ * j, 
            yaw + deltaYawAngle * j, pitch + deltaPitchAngle * j, 
            fieldOfView + deltaFieldOfViewAngle * j,
            time + deltaTime * j,
            Camera.Lens.PINHOLE));
      }
      
      x = newX;
      y = newY;
      z = newZ;
      yaw = newYaw;
      pitch = newPitch;
      fieldOfView = newFieldOfView;
      time = newTime;
    }

    return videoFramesPath.toArray(new Camera [videoFramesPath.size()]);
  }

  /**
   * Creates the video image depending on the quality requested by the user.
   */
  private void startVideoCreation() {
		ActionMap actionMap = getActionMap();
		actionMap.get(ActionType.SAVE_VIDEO).setEnabled(false);
		actionMap.get(ActionType.SHARE_VIDEO).setEnabled(false);
		this.createButton = (JButton)getActionMap().get(ActionType.STOP_VIDEO_CREATION);
		swapOut(createButton, R.id.videopanel_createButton);
		actionMap.get(ActionType.RECORD).setEnabled(false);
		actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(false);
		actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(false);
		//getRootPane().setDefaultButton(this.createButton);
    this.videoFormatComboBox.setEnabled(false);
    this.qualitySlider.setEnabled(false);
    this.dateSpinner.setEnabled(false);
    this.timeSpinner.setEnabled(false);
    this.ceilingLightEnabledCheckBox.setEnabled(false);
    //this.statusLayout.show(this.statusPanel, PROGRESS_CARD);
    this.progressBar.setIndeterminate(true);
    this.progressLabel.setText("");

    // Compute video in an other executor thread
    // Use a clone of home because the user can modify home during video computation
    final Home home = this.home.clone();
    this.videoCreationExecutor = Executors.newSingleThreadExecutor();
    this.videoCreationExecutor.execute(new Runnable() {
        public void run() {
          computeVideo(home);
        }
      });
  }

  /**
   * Computes the video of the given home.
   * Caution : this method must be thread safe because it's called from an executor. 
   */
  private void computeVideo(Home home) {
    this.videoCreationStartTime = System.currentTimeMillis();
    int frameRate = this.controller.getFrameRate();
    int quality = this.controller.getQuality();
    int width = this.controller.getWidth();
    int height = this.controller.getHeight();
    float speed = this.controller.getSpeed();
    final Camera [] videoFramesPath = getVideoFramesPath(speed, frameRate);
    // Set initial camera location because its type may change rendering setting
    home.setCamera(videoFramesPath [0]);
    EventQueue.invokeLater(new Runnable() {
        public void run() {
					progressBar.setMin(0);
					progressBar.setMax(videoFramesPath.length - 1);
					progressBar.setValue(0);
        }
      });
    FrameGenerator frameGenerator = null;
    // Delete previous file if it exists 
    if (this.videoFile != null) {
      this.videoFile.delete();
      this.videoFile = null;
    }
    File file = null;
    try {
      file = OperatingSystem.createTemporaryFile("video", ".mov"); 
      if (quality >= 2) {
        frameGenerator = new PhotoImageGenerator(home, width, height, this.object3dFactory, 
            quality == 2 
              ? PhotoRenderer.Quality.LOW
              : PhotoRenderer.Quality.HIGH);        
      } else {
        frameGenerator = new Image3DGenerator(home, this.preferences, width, height, this.object3dFactory,
            quality == 1
            && (!this.preferences.isDrawingModeEnabled()
                || home.getEnvironment().getDrawingMode() != HomeEnvironment.DrawingMode.OUTLINE));
      }
      if (!Thread.currentThread().isInterrupted()) {
        ImageDataSource sourceStream = new ImageDataSource((VideoFormat)this.videoFormatComboBox.getSelectedItem(), 
            frameGenerator, videoFramesPath, progressBar);
				BitmapToVideoEncoder bitmapToVideoEncoder = new BitmapToVideoEncoder(new BitmapToVideoEncoder.IBitmapToVideoEncoderCallback() {
					@Override
					public void onEncodingComplete(File outputFile) {
						videoFile = outputFile;
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								ActionMap actionMap = getActionMap();
								actionMap.get(ActionType.SAVE_VIDEO).setEnabled(videoFile != null);
								actionMap.get(ActionType.SHARE_VIDEO).setEnabled(videoFile != null);
								createButton = (JButton)getActionMap().get(ActionType.START_VIDEO_CREATION);
								swapOut(createButton, R.id.videopanel_createButton);
								actionMap.get(ActionType.RECORD).setEnabled(true);
								actionMap.get(ActionType.DELETE_CAMERA_PATH).setEnabled(true);
								actionMap.get(ActionType.DELETE_LAST_RECORD).setEnabled(true);
								videoFormatComboBox.setEnabled(true);
								qualitySlider.setEnabled(true);
								dateSpinner.setEnabled(true);
								timeSpinner.setEnabled(true);
								ceilingLightEnabledCheckBox.setEnabled(true);
								videoCreationExecutor = null;
							}
						});
					}
				});
				Renovations3DActivity.logFireBaseLevelUp("computeVideoStart", "quality " + quality + " width " + width + " height " + height);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						DrawableView dv = (DrawableView)planComponent.getDrawableView();
						dv.setVisibility(android.view.View.GONE);
						ImageView iv = (ImageView) findViewById(R.id.video_image_panel);
						iv.setVisibility(android.view.View.VISIBLE);
					}});
				bitmapToVideoEncoder.startEncoding(width, height, frameRate, file);
				while(!sourceStream.getStreams().endOfStream()) {
					//check for a stop called
					if(this.videoCreationExecutor == null)
						break;

					//only 2 in the queue at a time to avoid too much memory allocation
					if(bitmapToVideoEncoder.getActiveBitmaps() < 2) {
						final BufferedImage bm = sourceStream.getStreams().read();
						Bitmap forIV = ((Bitmap)bm.getDelegate()).copy(((Bitmap)bm.getDelegate()).getConfig(),false);
						bitmapToVideoEncoder.queueFrame((Bitmap)bm.getDelegate());

						EventQueue.invokeLater(new Runnable() {
							 public void run() {
								 ImageView iv = (ImageView) findViewById(R.id.video_image_panel);
								 iv.setImageBitmap(forIV);
							 }});
					} else {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				bitmapToVideoEncoder.stopEncoding();
				Renovations3DActivity.logFireBaseLevelUp("computeVideoEnd", "quality " + quality + " width " + width + " height " + height);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						DrawableView dv = (DrawableView)planComponent.getDrawableView();
						dv.setVisibility(android.view.View.VISIBLE);
						ImageView iv = (ImageView) findViewById(R.id.video_image_panel);
						iv.setVisibility(android.view.View.GONE);
					}});
      }
    } catch (InterruptedIOException ex) {
      if (file != null) {
        file.delete();
        file = null;
      }
    } catch (IOException ex) {
      showError("createVideoError.message", ex.getMessage());
      file = null;
    } catch (OutOfMemoryError ex) {
      showError("createVideoError.message", 
          this.preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "outOfMemory.message"));
      file = null;
    }
  }

  /**
   * Shows a message error dialog. 
   */
  private void showError(final String messageKey, 
                         final String messageDetail) {
    EventQueue.invokeLater(new Runnable() {
        public void run() {
          String messageFormat = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, messageKey);
          JOptionPane.showMessageDialog(activity, String.format(messageFormat, messageDetail),
              preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "videoError.title"), JOptionPane.ERROR_MESSAGE);
        }
      });
  }
  
  /**
   * Stops video creation.
   */
  private void stopVideoCreation(boolean confirmStop) {
    if (this.videoCreationExecutor != null ) {
			Thread t = new Thread() {
				public void run() {
					// Confirm the stop if a rendering has been running for more than 30 s
					if (!confirmStop
									|| System.currentTimeMillis() - videoCreationStartTime < MINIMUM_DELAY_BEFORE_DISCARDING_WITHOUT_WARNING
									|| JOptionPane.showConfirmDialog(activity,
									preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "confirmStopCreation.message"),
									preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "confirmStopCreation.title"),
									JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
						if (videoCreationExecutor != null) { // Check a second time in case rendering stopped meanwhile
							// Interrupt executor thread
							videoCreationExecutor.shutdownNow();
							videoCreationExecutor = null;
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									createButton = (JButton)getActionMap().get(ActionType.START_VIDEO_CREATION);
									swapOut(createButton, R.id.videopanel_createButton);
								}});
						}
					}
				}
			};
			t.start();
    }
  }

  /**
   * Saves the created video.
   */
  private void saveVideo(final boolean share) {
		Thread t = new Thread(new Runnable(){
			public void run(){
				final String movFileName = controller.getContentManager().showSaveDialog(VideoPanel.this,
						preferences.getLocalizedString(com.eteks.sweethome3d.android_props.VideoPanel.class, "saveVideoDialog.title"),
						ContentManager.ContentType.MOV, home.getName());
				if (movFileName != null) {
					//final Component rootPane = SwingUtilities.getRoot(this);
					//final Cursor defaultCursor = rootPane.getCursor();
					//rootPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

					// Disable panel actions
					ActionMap actionMap = getActionMap();
					final boolean[] actionEnabledStates = new boolean[ActionType.values().length];
					EventQueue.invokeLater(new Runnable() {
						public void run() {
							for (ActionType action : ActionType.values()) {
								actionEnabledStates[action.ordinal()] = actionMap.get(action).isEnabled();
								actionMap.get(action).setEnabled(false);
							}
						}});
					Executors.newSingleThreadExecutor().execute(new Runnable() {
							public void run() {
								OutputStream out = null;
								InputStream in = null;
								IOException exception = null;
								try {
									// Copy temporary file to home file
									// Overwriting home file will ensure that its rights are kept
									out = new FileOutputStream(movFileName);
									byte [] buffer = new byte [8192];
									in = new FileInputStream(videoFile);
									int size;
									while ((size = in.read(buffer)) != -1) {
										out.write(buffer, 0, size);
									}
								} catch (IOException ex) {
									exception = ex;
								} finally {
									try {
										if (out != null) {
											out.close();
										}
									} catch (IOException ex) {
										if (exception == null) {
											exception = ex;
										}
									}
									try {
										if (in != null) {
											in.close();
										}
									} catch (IOException ex) {
										// Ignore close exception
									}
									// Delete saved file in case of error or if panel was closed meanwhile
									if (exception != null) {
										new File(movFileName).delete();
										//if (!isDisplayable()) {
										//	exception = null;
										//}
									} else if(share) {
										Renovations3DActivity.logFireBaseContent("sharemovFile_start", "movFileName: " + movFileName);
										final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
										emailIntent.setType("image/png");

										String subjectText = activity.getResources().getString(R.string.app_name) + " " + movFileName;
										emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subjectText);
										//emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Attached");

										Uri outputFileUri = null;
										if (Build.VERSION.SDK_INT > M) {
											outputFileUri = FileProvider.getUriForFile(activity, activity.getApplicationContext().getPackageName() + ".provider", new File(movFileName));
										} else {
											outputFileUri = Uri.fromFile(new File(movFileName));
										}
										emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
										emailIntent.putExtra(Intent.EXTRA_STREAM, outputFileUri);

										activity.runOnUiThread(new Runnable(){public void run(){
										String title = activity.getResources().getString(R.string.share);
										// Create intent to show chooser
										Intent chooser = Intent.createChooser(emailIntent, title);

										// Verify the intent will resolve to at least one activity
										if (emailIntent.resolveActivity(activity.getPackageManager()) != null) {
											activity.startActivity(chooser);
										}
										Renovations3DActivity.logFireBaseContent("sharemovFile_end", "movFileName: " + movFileName);}});
									}

									final IOException caughtException = exception;
									EventQueue.invokeLater(new Runnable() {
											public void run() {
												// Restore action state
												ActionMap actionMap = getActionMap();
												for (ActionType action : ActionType.values()) {
													actionMap.get(action).setEnabled(actionEnabledStates [action.ordinal()]);
												}

												//rootPane.setCursor(defaultCursor);
												if (caughtException != null) {
													showError("saveVideoError.message", caughtException.getMessage());
												}
											}
										});
								}
							}
						});
				}
			}
		});
		t.start();
  }

  /**
   * Manages closing of this pane.
   */
  private void close() {
		this.dismissView();
		((Renovations3DActivity)activity).getAdMobManager().interstitialDisplayPoint();
  }
  
  /**
   * Returns the video aspect ration of the given size.
   */
  private AspectRatio getAspectRatio(int width, int height) {
    return Math.abs((float)width / height - 4f / 3) < 0.001f
       ? AspectRatio.RATIO_4_3 
       : (Math.abs((float)width / height - 24f / 10) < 0.03f
             ? AspectRatio.RATIO_24_10
             : AspectRatio.RATIO_16_9);
  }

  /**
   * A data source able to create JPEG buffers on the fly from camera path 
   * and turn that into a stream of JMF buffers. 
   */
  private static class ImageDataSource  {
    private ImageSourceStream stream;

    public ImageDataSource(VideoFormat format,
                           FrameGenerator frameGenerator,
                           Camera []      framesPath,
													 JProgressBar progressBar) {
      this.stream = new ImageSourceStream(format, frameGenerator, framesPath, progressBar);
    }

    public ImageSourceStream getStreams() {
      return this.stream;
    }
  }

  /**
   * A source of video images. 
   */
  private static class ImageSourceStream {
    private final FrameGenerator                 frameGenerator;
    private final Camera []                      framesPath;
    private final JProgressBar        			      progressBar;
    private final VideoFormat 										format;
    private int                                  imageIndex;

    public ImageSourceStream(VideoFormat format, 
                             FrameGenerator frameGenerator,
                             Camera [] framesPath, 
                             final JProgressBar progressBar) {
      this.frameGenerator = frameGenerator;
      this.framesPath = framesPath;
      this.progressBar = progressBar;
      this.format = format;
    }

    /**
     * Return <code>false</code> because source stream doesn't 
     * need to block assuming data can be created on demand.
     */
    public boolean willReadBlock() {
      return false;
    }

    /**
     * This is called from the Processor to read a frame worth of video data.
     */
    public BufferedImage read() throws IOException {
			BufferedImage frame = this.frameGenerator.renderImageAt(this.framesPath [this.imageIndex],
					this.imageIndex == this.framesPath.length - 1);

			final int progressionValue = this.imageIndex++;
			EventQueue.invokeLater(new Runnable() {
					public void run() {
						progressBar.setValue(progressionValue);
					}
				});
			return frame;
    }

		/**
		 * Checks that app context is correct.
		 */
		private void checkAppContext() {
		}

    /**
     * Return the format of each video frame. That will be JPEG.
     */
    public Format getFormat() {
      return this.format;
    }

    public long getContentLength() {
      return 0;
    }

		public boolean endOfStream() {
			return this.imageIndex == this.framesPath.length;
		}
  }

  
  /**
   * An object able to generate a frame of a video at a camera location. 
   */
  private static abstract class FrameGenerator {
    private Thread launchingThread;

    protected FrameGenerator() {
      this.launchingThread = Thread.currentThread();
    }
    
    public abstract BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException;

		public void checkLaunchingThreadIsntInterrupted() throws InterruptedIOException {
      if (this.launchingThread.isInterrupted()) {
        throw new InterruptedIOException("Launching thread interrupted");
      }
    }
  }

  /**
   * A frame generator using photo renderer.
   */
  private static class PhotoImageGenerator extends FrameGenerator {
    private PhotoRenderer renderer;
    private BufferedImage image;

    public PhotoImageGenerator(Home home, int width, int height,
                               Object3DFactory object3dFactory,
                               PhotoRenderer.Quality quality) throws IOException {
      this.renderer = new PhotoRenderer(home, object3dFactory, quality);
			// I can't reuse the images as the encode function might still be holding them
      this.image = new VMBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException {
      try {
        checkLaunchingThreadIsntInterrupted();
        this.renderer.render(this.image, frameCamera, null);
				// must be carefully swapped to get BGRA to RGBA
				int[] imagePixels = PlanComponent.PieceOfFurnitureModelIcon.getImagePixels(image);
				Bitmap bm = Bitmap.createBitmap(imagePixels, image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
				VMBufferedImage image2 = new VMBufferedImage(bm);
        checkLaunchingThreadIsntInterrupted();
        return image2;
      } catch(InterruptedIOException ex) {
        this.renderer = null;
        throw ex;
      } finally {
        if (last) {
          this.renderer.dispose();
          this.renderer = null;
        }
      }
    }
  }

  /**
   * A frame generator using 3D offscreen images.
   */
  private static class Image3DGenerator extends FrameGenerator {
    private final Home      home;
    private HomeComponent3D homeComponent3D;
    private BufferedImage   image;
		private int width, height;

    public Image3DGenerator(Home home, UserPreferences preferences, int width, int height,
                            Object3DFactory object3dFactory, 
                            boolean displayShadowOnFloor) {
      this.home = home;
      this.homeComponent3D = new HomeComponent3D();
      homeComponent3D.init(home, preferences, object3dFactory, displayShadowOnFloor, null);
      this.homeComponent3D.startOffscreenImagesCreation();
      // I can't reuse the images as the encode function might still be holding them
      //this.image = new VMBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
			this.width = width;
			this.height = height;
    }

    public BufferedImage renderImageAt(Camera frameCamera, boolean last) throws IOException {
      try {
        checkLaunchingThreadIsntInterrupted();

        // Replace home camera with frameCamera to avoid animation interpolator in 3D component 
        this.home.setCamera(frameCamera);
				this.image = new VMBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        // Get a twice bigger offscreen image for better quality 
        // (antialiasing isn't always available for offscreen canvas)
        BufferedImage offScreenImage = this.homeComponent3D.getOffScreenImage(
            2 * this.image.getWidth(), 2 * this.image.getHeight());

        checkLaunchingThreadIsntInterrupted();
        Graphics graphics = this.image.getGraphics();
        graphics.drawImage(offScreenImage.getScaledInstance(
            this.image.getWidth(), this.image.getHeight(), Image.SCALE_DEFAULT), 0, 0, null);
        graphics.dispose();
        checkLaunchingThreadIsntInterrupted();
        return this.image;
      } catch(InterruptedIOException ex) {
        this.homeComponent3D.endOffscreenImagesCreation();
        throw ex;
      } finally {
        if (last) {
          this.homeComponent3D.endOffscreenImagesCreation();
        }
      }
    }
  }

	//PPJPJPJPJP replacing jmf class
	public static class Format {

		public static final int NOT_SPECIFIED = 0;
		public static int byteArray = 2;
	}
	private static class VideoFormat extends Format {

		public static final int JPEG = 0;
		Dimension dimension;
		int fps;
		public VideoFormat(int jpeg, Dimension dimension, int notSpecified, int byteArray, int fps) {
			this.dimension = dimension;
			this.fps = fps;
		}
		public Dimension getSize() {
			return dimension;
		}
		public int getFrameRate() {
			return fps;
		}
	}
}
