/*
 * PlanComponent.java 2 juin 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import android.graphics.Bitmap;
import android.graphics.Paint.FontMetrics;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import javaawt.AlphaComposite;
import javaawt.BasicStroke;
import javaawt.Color;
import javaawt.Composite;
import javaawt.EventQueue;
import javaawt.Font;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.Insets;
import javaawt.Paint;
import javaawt.Point;
import javaawt.Rectangle;
import javaawt.RenderingHints;
import javaawt.Shape;
import javaawt.Stroke;
import javaawt.TexturePaint;
import javaawt.VMFont;
import javaawt.VMGraphics2D;
import javaawt.geom.AffineTransform;
import javaawt.geom.Arc2D;
import javaawt.geom.Area;
import javaawt.geom.CubicCurve2D;
import javaawt.geom.Ellipse2D;
import javaawt.geom.GeneralPath;
import javaawt.geom.Line2D;
import javaawt.geom.PathIterator;
import javaawt.geom.Point2D;
import javaawt.geom.Rectangle2D;
import javaawt.image.BufferedImage;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.ImageIO;
import javaawt.print.PageFormat;
import javaawt.print.Printable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.security.AccessControlException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingBox;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.ImageComponent2D;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;

import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JFormattedTextField;
import com.mindblowing.swingish.JToolTip;
import com.mindblowing.swingish.JViewPort;
import com.eteks.renovations3d.ToolTipManager;
import com.mindblowing.utils.LongHoldHandler;
import javaxswing.Icon;
import javaxswing.ImageIcon;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point2f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.Tutorial;

import com.eteks.renovations3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.j3d.Object3DBranch;
import com.eteks.sweethome3d.j3d.Object3DBranchFactory;
import com.eteks.sweethome3d.j3d.TextureManager;
import com.eteks.sweethome3d.model.BackgroundImage;
import com.eteks.sweethome3d.model.Camera;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.Compass;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.DimensionLine;
import com.eteks.sweethome3d.model.Elevatable;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeDoorOrWindow;
import com.eteks.sweethome3d.model.HomeFurnitureGroup;
import com.eteks.sweethome3d.model.HomeLight;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.Label;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.Level;
import com.eteks.sweethome3d.model.ObserverCamera;
import com.eteks.sweethome3d.model.PieceOfFurniture;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Sash;
import com.eteks.sweethome3d.model.Selectable;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.TextStyle;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.Object3DFactory;
import com.eteks.sweethome3d.viewcontroller.PlanController;
import com.eteks.sweethome3d.viewcontroller.PlanView;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.Viewer;
import org.jogamp.java3d.utils.universe.ViewingPlatform;


/**
 * A component displaying the plan of a home.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class PlanComponent extends JViewPort implements PlanView, Printable {
	static int HORIZONTAL = 0; //as SwingConstants does not exist in this rt
	static int VERTICAL = 1;

	//PJ taken from HomePane
	public static final String PLAN_VIEWPORT_X_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.PlanViewportX";
	public static final String PLAN_VIEWPORT_Y_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.PlanViewportY";
	public static final String SCALE_VISUAL_PROPERTY = "com.eteks.sweethome3d.SweetHome3D.PlanScale";

  /**
   * The circumstances under which the home items displayed by this component will be painted.
   */
  protected enum PaintMode {PAINT, PRINT, CLIPBOARD, EXPORT}

  private enum ActionType {DELETE_SELECTION, ESCAPE,
      MOVE_SELECTION_LEFT, MOVE_SELECTION_UP, MOVE_SELECTION_DOWN, MOVE_SELECTION_RIGHT,
      MOVE_SELECTION_FAST_LEFT, MOVE_SELECTION_FAST_UP, MOVE_SELECTION_FAST_DOWN, MOVE_SELECTION_FAST_RIGHT,
      TOGGLE_MAGNETISM_ON, TOGGLE_MAGNETISM_OFF,
      ACTIVATE_ALIGNMENT, DEACTIVATE_ALIGNMENT,
      ACTIVATE_DUPLICATION, DEACTIVATE_DUPLICATION,
      ACTIVATE_EDITIION, DEACTIVATE_EDITIION}

  /**
   * Indicator types that may be displayed on selected items.
   */
  public static class IndicatorType {
    // Don't qualify IndicatorType as an enumeration to be able to extend IndicatorType class
    public static final IndicatorType ROTATE        = new IndicatorType("ROTATE");
    public static final IndicatorType RESIZE        = new IndicatorType("RESIZE");
    public static final IndicatorType ELEVATE       = new IndicatorType("ELEVATE");
    public static final IndicatorType RESIZE_HEIGHT = new IndicatorType("RESIZE_HEIGHT");
    public static final IndicatorType CHANGE_POWER  = new IndicatorType("CHANGE_POWER");
    public static final IndicatorType MOVE_TEXT     = new IndicatorType("MOVE_TEXT");
    public static final IndicatorType ROTATE_TEXT   = new IndicatorType("ROTATE_TEXT");
    public static final IndicatorType ROTATE_PITCH  = new IndicatorType("ROTATE_PITCH");
    public static final IndicatorType ROTATE_ROLL   = new IndicatorType("ROTATE_ROLL");
    public static final IndicatorType ARC_EXTENT    = new IndicatorType("ARC_EXTENT");

    private final String name;

    protected IndicatorType(String name) {
      this.name = name;
    }

    public final String name() {
      return this.name;
    }

    @Override
    public String toString() {
      return this.name;
    }
  };

  private static float 			MARGIN_DP = 40;
  private static float 			MARGIN = 40;

  public static float 			densityScale = 1;

  private static float 			fatFingerScaleUp = 3;// reset in setDrawable below to a density scaled figure

  public static float 			dpiMinSpanForZoom = 1.0f; //TODO: this is used to control if a scale should happen, not sure why it's so damn complex though


  private Home            		home;
  private UserPreferences 		preferences;
  private Object3DFactory 		object3dFactory;
  private float                 resolutionScale = 1;//SwingTools.getResolutionScale();
  private float                 scale = 1;//0.5f * this.resolutionScale;
  private boolean               selectedItemsOutlinePainted = true;
  private boolean               backgroundPainted = true;

  private PlanRulerComponent    horizontalRuler;
  private PlanRulerComponent    verticalRuler;

/*  private final Cursor          rotationCursor;
  private final Cursor          elevationCursor;
  private final Cursor          heightCursor;
  private final Cursor          powerCursor;
  private final Cursor          resizeCursor;
  private final Cursor          moveCursor;
  private final Cursor          panningCursor;
  private final Cursor          duplicationCursor;*/

  private Rectangle2D           rectangleFeedback;
  private Class<? extends Selectable> alignedObjectClass;
  private Selectable            alignedObjectFeedback;
  private Point2D               locationFeeback;
  private boolean               showPointFeedback;
  private Point2D               centerAngleFeedback;
  private Point2D               point1AngleFeedback;
  private Point2D               point2AngleFeedback;
  private List<Selectable>      draggedItemsFeedback;
  private List<DimensionLine>   dimensionLinesFeedback;
  private boolean               selectionScrollUpdated;
  private boolean               wallsDoorsOrWindowsModification;
  private JToolTip 				toolTip;
  //private JWindow               toolTipWindow;
  private ToolTipManager 		toolTipManager;
  private boolean               resizeIndicatorVisible;

  private Map<PlanController.EditableProperty, JFormattedTextField> toolTipEditableTextFields;
  //private KeyListener                       toolTipKeyListener;

  private List<HomePieceOfFurniture>        sortedLevelFurniture;
  private List<Room>                        sortedLevelRooms;
  private Map<TextStyle, Font>              fonts;
  private Map<TextStyle, FontMetrics>       fontsMetrics;

  private Rectangle2D                       planBoundsCache;
  private boolean                           planBoundsCacheValid = false;
  private Rectangle2D                       invalidPlanBounds;
  private BufferedImage                     backgroundImageCache;
  private Map<TextureImage, BufferedImage>  patternImagesCache;
  private Set<HomePieceOfFurniture>         invalidFurnitureTopViewIcons;
  private List<Wall>                        otherLevelsWallsCache;
  private Area                              otherLevelsWallAreaCache;
  private List<Room>                        otherLevelsRoomsCache;
  private Area                              otherLevelsRoomAreaCache;
  private Color                             wallsPatternBackgroundCache;
  private Color                             wallsPatternForegroundCache;
  private Map<Collection<Wall>, Area>       wallAreasCache;
  private Map<HomeDoorOrWindow, Area>       doorOrWindowWallThicknessAreasCache;
  private Map<HomeTexture, BufferedImage>   floorTextureImagesCache;
  private Map<HomePieceOfFurniture, HomePieceOfFurnitureTopViewIconKey> furnitureTopViewIconKeys;
  private Map<HomePieceOfFurnitureTopViewIconKey, PieceOfFurnitureTopViewIcon> furnitureTopViewIconsCache;


  private static ExecutorService            backgroundImageLoader;

  private static final Shape       POINT_INDICATOR;
  private static final GeneralPath FURNITURE_ROTATION_INDICATOR;
  private static final GeneralPath FURNITURE_PITCH_ROTATION_INDICATOR;
  private static final Shape       FURNITURE_ROLL_ROTATION_INDICATOR;
  private static final GeneralPath FURNITURE_RESIZE_INDICATOR;
  private static final GeneralPath ELEVATION_INDICATOR;
  private static final Shape       ELEVATION_POINT_INDICATOR;
  private static final GeneralPath FURNITURE_HEIGHT_INDICATOR;
  private static final Shape       FURNITURE_HEIGHT_POINT_INDICATOR;
  private static final GeneralPath LIGHT_POWER_INDICATOR;
  private static final Shape       LIGHT_POWER_POINT_INDICATOR;
  private static final GeneralPath WALL_ORIENTATION_INDICATOR;
  private static final Shape       WALL_POINT;
  private static final GeneralPath WALL_ARC_EXTENT_INDICATOR;
  private static final GeneralPath WALL_AND_LINE_RESIZE_INDICATOR;
  private static final Shape       CAMERA_YAW_ROTATION_INDICATOR;
  private static final Shape 	   CAMERA_PITCH_ROTATION_INDICATOR;
  private static final GeneralPath CAMERA_ELEVATION_INDICATOR;
  private static final Shape       CAMERA_BODY;
  private static final Shape       CAMERA_HEAD;
  private static final GeneralPath DIMENSION_LINE_END;
  private static final GeneralPath TEXT_LOCATION_INDICATOR;
  private static final GeneralPath TEXT_ANGLE_INDICATOR;
  private static final Shape       LABEL_CENTER_INDICATOR;
  private static final Shape       COMPASS_DISC;
  private static final GeneralPath COMPASS;
  private static final GeneralPath COMPASS_ROTATION_INDICATOR;
  private static final GeneralPath COMPASS_RESIZE_INDICATOR;

  private static final GeneralPath ARROW;

  private static final Stroke      INDICATOR_STROKE = new BasicStroke(1.5f);
  private static final Stroke      POINT_STROKE = new BasicStroke(2f);

  private static final float       WALL_STROKE_WIDTH = 1.5f;
  private static final float       BORDER_STROKE_WIDTH = 1f;
  private static final float       ALIGNMENT_LINE_OFFSET = 25f;

  private static final BufferedImage ERROR_TEXTURE_IMAGE;
  private static final BufferedImage WAIT_TEXTURE_IMAGE;

  private PlanController controller;

  //menu item options
  public boolean alignmentActivated = false;
  public boolean selectLasso = false;
  public boolean selectMultiple = false;
  public boolean addRoomPointActivated = false;
  public boolean deleteRoomPointActivated = false;

  static {
    POINT_INDICATOR = new Ellipse2D.Float(-1.5f, -1.5f, 3, 3);

    // Create a path that draws an round arrow used as a rotation indicator
    // at top left point of a piece of furniture
    FURNITURE_ROTATION_INDICATOR = new GeneralPath();
    FURNITURE_ROTATION_INDICATOR.append(POINT_INDICATOR, false);
    FURNITURE_ROTATION_INDICATOR.append(new Arc2D.Float(-8, -8, 16, 16, 45, 180, Arc2D.OPEN), false);
    FURNITURE_ROTATION_INDICATOR.moveTo(2.66f, -5.66f);
    FURNITURE_ROTATION_INDICATOR.lineTo(5.66f, -5.66f);
    FURNITURE_ROTATION_INDICATOR.lineTo(4f, -8.3f);

    // Create a path used as pitch rotation indicator
    // at bottom left of a piece of furniture rotated around pitch
    FURNITURE_PITCH_ROTATION_INDICATOR = new GeneralPath();
    FURNITURE_PITCH_ROTATION_INDICATOR.append(POINT_INDICATOR, false);
    FURNITURE_PITCH_ROTATION_INDICATOR.moveTo(-4.5f, 0);
    FURNITURE_PITCH_ROTATION_INDICATOR.lineTo(-5.2f, 0);
    FURNITURE_PITCH_ROTATION_INDICATOR.moveTo(-9f, 0);
    FURNITURE_PITCH_ROTATION_INDICATOR.lineTo(-10, 0);
    FURNITURE_PITCH_ROTATION_INDICATOR.append(new Arc2D.Float(-12, -8, 5, 16, 200, 320, Arc2D.OPEN), false);
    FURNITURE_PITCH_ROTATION_INDICATOR.moveTo(-10f, -4.5f);
    FURNITURE_PITCH_ROTATION_INDICATOR.lineTo(-12.3f, -2f);
    FURNITURE_PITCH_ROTATION_INDICATOR.lineTo(-12.8f, -5.8f);

    // Create a path used as pitch rotation indicator
    // at bottom left of a piece of furniture rotated around roll axis
    AffineTransform transform = AffineTransform.getRotateInstance(-Math.PI / 2);
    transform.concatenate(AffineTransform.getScaleInstance(1, -1));
    FURNITURE_ROLL_ROTATION_INDICATOR = FURNITURE_PITCH_ROTATION_INDICATOR.createTransformedShape(transform);

    ELEVATION_POINT_INDICATOR = new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f);

    // Create a path that draws a line with one arrow as an elevation indicator
    // at top right of a piece of furniture
    ELEVATION_INDICATOR = new GeneralPath();
    ELEVATION_INDICATOR.moveTo(0, -5); // Vertical line
    ELEVATION_INDICATOR.lineTo(0, 5);
    ELEVATION_INDICATOR.moveTo(-2.5f, 5);    // Bottom line
    ELEVATION_INDICATOR.lineTo(2.5f, 5);
    ELEVATION_INDICATOR.moveTo(-1.2f, 1.5f); // Bottom arrow
    ELEVATION_INDICATOR.lineTo(0, 4.5f);
    ELEVATION_INDICATOR.lineTo(1.2f, 1.5f);

    FURNITURE_HEIGHT_POINT_INDICATOR = new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f);

    // Create a path that draws a line with two arrows as a height indicator
    // at bottom left of a piece of furniture
    FURNITURE_HEIGHT_INDICATOR = new GeneralPath();
    FURNITURE_HEIGHT_INDICATOR.moveTo(0, -6); // Vertical line
    FURNITURE_HEIGHT_INDICATOR.lineTo(0, 6);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-2.5f, -6);    // Top line
    FURNITURE_HEIGHT_INDICATOR.lineTo(2.5f, -6);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-2.5f, 6);     // Bottom line
    FURNITURE_HEIGHT_INDICATOR.lineTo(2.5f, 6);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-1.2f, -2.5f); // Top arrow
    FURNITURE_HEIGHT_INDICATOR.lineTo(0f, -5.5f);
    FURNITURE_HEIGHT_INDICATOR.lineTo(1.2f, -2.5f);
    FURNITURE_HEIGHT_INDICATOR.moveTo(-1.2f, 2.5f);  // Bottom arrow
    FURNITURE_HEIGHT_INDICATOR.lineTo(0f, 5.5f);
    FURNITURE_HEIGHT_INDICATOR.lineTo(1.2f, 2.5f);

    LIGHT_POWER_POINT_INDICATOR = new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f);

    // Create a path that draws a stripped triangle as a power indicator
    // at bottom left of a not deformable lights
    LIGHT_POWER_INDICATOR = new GeneralPath();
    LIGHT_POWER_INDICATOR.moveTo(-8, 0);
    LIGHT_POWER_INDICATOR.lineTo(-6f, 0);
    LIGHT_POWER_INDICATOR.lineTo(-6f, -1);
    LIGHT_POWER_INDICATOR.closePath();
    LIGHT_POWER_INDICATOR.moveTo(-3, 0);
    LIGHT_POWER_INDICATOR.lineTo(-1f, 0);
    LIGHT_POWER_INDICATOR.lineTo(-1f, -2.5f);
    LIGHT_POWER_INDICATOR.lineTo(-3f, -1.8f);
    LIGHT_POWER_INDICATOR.closePath();
    LIGHT_POWER_INDICATOR.moveTo(2, 0);
    LIGHT_POWER_INDICATOR.lineTo(4, 0);
    LIGHT_POWER_INDICATOR.lineTo(4f, -3.5f);
    LIGHT_POWER_INDICATOR.lineTo(2f, -2.8f);
    LIGHT_POWER_INDICATOR.closePath();

    // Create a path used as a resize indicator
    // at bottom right point of a piece of furniture
    FURNITURE_RESIZE_INDICATOR = new GeneralPath();
    FURNITURE_RESIZE_INDICATOR.append(new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f), false);
    FURNITURE_RESIZE_INDICATOR.moveTo(5, -4);
    FURNITURE_RESIZE_INDICATOR.lineTo(7, -4);
    FURNITURE_RESIZE_INDICATOR.lineTo(7, 7);
    FURNITURE_RESIZE_INDICATOR.lineTo(-4, 7);
    FURNITURE_RESIZE_INDICATOR.lineTo(-4, 5);
    FURNITURE_RESIZE_INDICATOR.moveTo(3.5f, 3.5f);
    FURNITURE_RESIZE_INDICATOR.lineTo(9, 9);
    FURNITURE_RESIZE_INDICATOR.moveTo(7, 9.5f);
    FURNITURE_RESIZE_INDICATOR.lineTo(10, 10);
    FURNITURE_RESIZE_INDICATOR.lineTo(9.5f, 7);

    // Create a path used an orientation indicator
    // at start and end points of a selected wall
    WALL_ORIENTATION_INDICATOR = new GeneralPath();
    WALL_ORIENTATION_INDICATOR.moveTo(-4, -4);
    WALL_ORIENTATION_INDICATOR.lineTo(4, 0);
    WALL_ORIENTATION_INDICATOR.lineTo(-4, 4);

    WALL_POINT = new Ellipse2D.Float(-3, -3, 6, 6);

    // Create a path used as arc extent indicator for wall
    WALL_ARC_EXTENT_INDICATOR = new GeneralPath();
    WALL_ARC_EXTENT_INDICATOR.append(new Arc2D.Float(-4, 1, 8, 5, 210, 120, Arc2D.OPEN), false);
    WALL_ARC_EXTENT_INDICATOR.moveTo(0, 6);
    WALL_ARC_EXTENT_INDICATOR.lineTo(0, 11);
    WALL_ARC_EXTENT_INDICATOR.moveTo(-1.8f, 8.7f);
    WALL_ARC_EXTENT_INDICATOR.lineTo(0, 12);
    WALL_ARC_EXTENT_INDICATOR.lineTo(1.8f, 8.7f);

    // Create a path used as a size indicator
    // at start and end points of a selected wall
    WALL_AND_LINE_RESIZE_INDICATOR = new GeneralPath();
    WALL_AND_LINE_RESIZE_INDICATOR.moveTo(5, -2);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(5, 2);
    WALL_AND_LINE_RESIZE_INDICATOR.moveTo(6, 0);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(11, 0);
    WALL_AND_LINE_RESIZE_INDICATOR.moveTo(8.7f, -1.8f);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(12, 0);
    WALL_AND_LINE_RESIZE_INDICATOR.lineTo(8.7f, 1.8f);

    // Create a path used as yaw rotation indicator for the camera
    transform = AffineTransform.getRotateInstance(-Math.PI / 4);
    CAMERA_YAW_ROTATION_INDICATOR = FURNITURE_ROTATION_INDICATOR.createTransformedShape(transform);

    // Create a path used as pitch rotation indicator for the camera
    transform = AffineTransform.getRotateInstance(Math.PI);
    CAMERA_PITCH_ROTATION_INDICATOR = FURNITURE_PITCH_ROTATION_INDICATOR.createTransformedShape(transform);

    // Create a path that draws a line with one arrow as an elevation indicator
    // at the back of the camera
    CAMERA_ELEVATION_INDICATOR = new GeneralPath();
    CAMERA_ELEVATION_INDICATOR.moveTo(0, -4); // Vertical line
    CAMERA_ELEVATION_INDICATOR.lineTo(0, 4);
    CAMERA_ELEVATION_INDICATOR.moveTo(-2.5f, 4);    // Bottom line
    CAMERA_ELEVATION_INDICATOR.lineTo(2.5f, 4);
    CAMERA_ELEVATION_INDICATOR.moveTo(-1.2f, 0.5f); // Bottom arrow
    CAMERA_ELEVATION_INDICATOR.lineTo(0, 3.5f);
    CAMERA_ELEVATION_INDICATOR.lineTo(1.2f, 0.5f);

    // Create a path used to draw the camera
    // This path looks like a human being seen from top that fits in one cm wide square
    GeneralPath cameraBodyAreaPath = new GeneralPath();
    cameraBodyAreaPath.append(new Ellipse2D.Float(-0.5f, -0.425f, 1f, 0.85f), false); // Body
    cameraBodyAreaPath.append(new Ellipse2D.Float(-0.5f, -0.3f, 0.24f, 0.6f), false); // Shoulder
    cameraBodyAreaPath.append(new Ellipse2D.Float(0.26f, -0.3f, 0.24f, 0.6f), false); // Shoulder
    CAMERA_BODY = new Area(cameraBodyAreaPath);

    GeneralPath cameraHeadAreaPath = new GeneralPath();
    cameraHeadAreaPath.append(new Ellipse2D.Float(-0.18f, -0.45f, 0.36f, 1f), false); // Head
    cameraHeadAreaPath.moveTo(-0.04f, 0.55f); // Noise
    cameraHeadAreaPath.lineTo(0, 0.65f);
    cameraHeadAreaPath.lineTo(0.04f, 0.55f);
    cameraHeadAreaPath.closePath();
    CAMERA_HEAD = new Area(cameraHeadAreaPath);

    DIMENSION_LINE_END = new GeneralPath();
    DIMENSION_LINE_END.moveTo(-5, 5);
    DIMENSION_LINE_END.lineTo(5, -5);
    DIMENSION_LINE_END.moveTo(0, 5);
    DIMENSION_LINE_END.lineTo(0, -5);

    // Create a path that draws three arrows going left, right and down
    TEXT_LOCATION_INDICATOR = new GeneralPath();
    TEXT_LOCATION_INDICATOR.append(new Arc2D.Float(-2, 0, 4, 4, 190, 160, Arc2D.CHORD), false);
    TEXT_LOCATION_INDICATOR.moveTo(0, 4);        // Down line
    TEXT_LOCATION_INDICATOR.lineTo(0, 12);
    TEXT_LOCATION_INDICATOR.moveTo(-1.2f, 8.5f); // Down arrow
    TEXT_LOCATION_INDICATOR.lineTo(0f, 11.5f);
    TEXT_LOCATION_INDICATOR.lineTo(1.2f, 8.5f);
    TEXT_LOCATION_INDICATOR.moveTo(2f, 3f);      // Right line
    TEXT_LOCATION_INDICATOR.lineTo(9, 6);
    TEXT_LOCATION_INDICATOR.moveTo(6, 6.5f);     // Right arrow
    TEXT_LOCATION_INDICATOR.lineTo(10, 7);
    TEXT_LOCATION_INDICATOR.lineTo(7.5f, 3.5f);
    TEXT_LOCATION_INDICATOR.moveTo(-2f, 3f);     // Left line
    TEXT_LOCATION_INDICATOR.lineTo(-9, 6);
    TEXT_LOCATION_INDICATOR.moveTo(-6, 6.5f);    // Left arrow
    TEXT_LOCATION_INDICATOR.lineTo(-10, 7);
    TEXT_LOCATION_INDICATOR.lineTo(-7.5f, 3.5f);

    // Create a path used as angle indicator for texts
    TEXT_ANGLE_INDICATOR = new GeneralPath();
    TEXT_ANGLE_INDICATOR.append(new Arc2D.Float(-1.25f, -1.25f, 2.5f, 2.5f, 10, 160, Arc2D.CHORD), false);
    TEXT_ANGLE_INDICATOR.append(new Arc2D.Float(-8, -8, 16, 16, 30, 120, Arc2D.OPEN), false);
    TEXT_ANGLE_INDICATOR.moveTo(4f, -5.2f);
    TEXT_ANGLE_INDICATOR.lineTo(6.9f, -4f);
    TEXT_ANGLE_INDICATOR.lineTo(5.8f, -7f);

    LABEL_CENTER_INDICATOR = new Ellipse2D.Float(-1f, -1f, 2, 2);

    // Create the path used to draw the compass
    COMPASS_DISC = new Ellipse2D.Float(-0.5f, -0.5f, 1, 1);
    BasicStroke stroke = new BasicStroke(0.01f);
    COMPASS = new GeneralPath(stroke.createStrokedShape(COMPASS_DISC));
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(-0.6f, 0, -0.5f, 0)), false);
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(0.6f, 0, 0.5f, 0)), false);
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(0, 0.6f, 0, 0.5f)), false);
    stroke = new BasicStroke(0.04f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    COMPASS.append(stroke.createStrokedShape(new Line2D.Float(0, 0, 0, 0)), false);
    GeneralPath compassNeedle = new GeneralPath();
    compassNeedle.moveTo(0, -0.47f);
    compassNeedle.lineTo(0.15f, 0.46f);
    compassNeedle.lineTo(0, 0.32f);
    compassNeedle.lineTo(-0.15f, 0.46f);
    compassNeedle.closePath();
    stroke = new BasicStroke(0.03f);
    COMPASS.append(stroke.createStrokedShape(compassNeedle), false);
    GeneralPath compassNorthDirection = new GeneralPath();
    compassNorthDirection.moveTo(-0.07f, -0.55f); // Draws the N letter
    compassNorthDirection.lineTo(-0.07f, -0.69f);
    compassNorthDirection.lineTo(0.07f, -0.56f);
    compassNorthDirection.lineTo(0.07f, -0.7f);
    COMPASS.append(stroke.createStrokedShape(compassNorthDirection), false);

    // Create a path used as rotation indicator for the compass
    COMPASS_ROTATION_INDICATOR = new GeneralPath();
    COMPASS_ROTATION_INDICATOR.append(POINT_INDICATOR, false);
    COMPASS_ROTATION_INDICATOR.append(new Arc2D.Float(-8, -7, 16, 16, 210, 120, Arc2D.OPEN), false);
    COMPASS_ROTATION_INDICATOR.moveTo(4f, 5.66f);
    COMPASS_ROTATION_INDICATOR.lineTo(7f, 5.66f);
    COMPASS_ROTATION_INDICATOR.lineTo(5.6f, 8.3f);

    // Create a path used as a resize indicator for the compass
    COMPASS_RESIZE_INDICATOR = new GeneralPath();
    COMPASS_RESIZE_INDICATOR.append(new Rectangle2D.Float(-1.5f, -1.5f, 3f, 3f), false);
    COMPASS_RESIZE_INDICATOR.moveTo(4, -6);
    COMPASS_RESIZE_INDICATOR.lineTo(6, -6);
    COMPASS_RESIZE_INDICATOR.lineTo(6, 6);
    COMPASS_RESIZE_INDICATOR.lineTo(4, 6);
    COMPASS_RESIZE_INDICATOR.moveTo(5, 0);
    COMPASS_RESIZE_INDICATOR.lineTo(9, 0);
    COMPASS_RESIZE_INDICATOR.moveTo(9, -1.5f);
    COMPASS_RESIZE_INDICATOR.lineTo(12, 0);
    COMPASS_RESIZE_INDICATOR.lineTo(9, 1.5f);

    ARROW = new GeneralPath();
    ARROW.moveTo(-5, -2);
    ARROW.lineTo(0, 0);
    ARROW.lineTo(-5, 2);

    ERROR_TEXTURE_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Graphics g = ERROR_TEXTURE_IMAGE.getGraphics();
    g.setColor(Color.RED);
    g.drawLine(0, 0, 0, 0);
    g.dispose();

    WAIT_TEXTURE_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    g = WAIT_TEXTURE_IMAGE.getGraphics();
    g.setColor(Color.WHITE);
    g.drawLine(0, 0, 0, 0);
    g.dispose();
  }

	@Override
	public android.view.View onCreateView(LayoutInflater inflater,
										  ViewGroup container, Bundle savedInstanceState) {
		throw new RuntimeException("PlanComponent isn't an on screen thing at all!");
	}

	/**
	 * This is never on screen so getView will cause crashes
	 * @return
	 */
	@Override
	public View getView() {
		return getDrawableView();
	}
	@Override
	public void setDrawableView(DrawableView dv) {
		super.setDrawableView(dv);

		densityScale = this.getDrawableView().getResources().getDisplayMetrics().density;

		// used for removing the plan scale and enlargening the handles in one step
		fatFingerScaleUp = densityScale * 3;//TODO: is this *3 shared with any other sizing if so make it a variable

		// Now determine a finger interface size for the indicators
		//used for isItemSelectedAt, getSelectablesItesmAt, furnitureOnWall, various magnetized calls
		PlanController.PIXEL_MARGIN = 4 * (int)fatFingerScaleUp / 2;
		PlanController.INDICATOR_PIXEL_MARGIN = 5 * (int)fatFingerScaleUp;// for selection handle touching and polyline resize and room resize
		PlanController.WALL_ENDS_PIXEL_MARGIN = 2 * (int)fatFingerScaleUp * 4;//<-bonus just for wall start or end welding together

		MARGIN = (int) (MARGIN_DP * densityScale + 0.5f);

		// Set JComponent default properties
		setOpaque(true);
		// Add listeners
		addModelListeners(home, preferences, controller);
		createToolTipTextFields(preferences, controller);
		if (controller != null) {
			addMouseListeners(controller);
			//addFocusListener(controller);
			//createActions(controller);
			//installDefaultKeyboardActions();
			setFocusable(true);
			setAutoscrolls(true);
		}

		toolTipManager = new ToolTipManager(this.getDrawableView().getContext(), getView());
	}


			/**
			 * Creates a new plan that displays <code>home</code>.
			 * @param home the home to display
			 * @param preferences user preferences to retrieve used unit, grid visibility...
			 * @param controller the optional controller used to manage home items modification
			 */
  public void init(Home home,
					UserPreferences preferences,
					PlanController controller) {
  	init(home, preferences, null, controller);
  }

			/**
			 * Creates a new plan that displays <code>home</code>.
			 * @param home the home to display
			 * @param preferences user preferences to retrieve used unit, grid visibility...
			 * @param object3dFactory a factory able to create 3D objects from <code>home</code> furniture.
			 *            The {@link Object3DFactory#createObject3D(Home, Selectable, boolean) createObject3D} of
			 *            this factory is expected to return an instance of {@link Object3DBranch} in current implementation.
			 * @param controller the optional controller used to manage home items modification
			 */
  public void init(Home home,
					UserPreferences preferences,
					Object3DFactory  object3dFactory,
					PlanController controller) {
    this.home = home;
    this.preferences = preferences;
    try {
      if (object3dFactory == null && !Boolean.getBoolean("com.eteks.sweethome3d.no3D")) {
        object3dFactory = new Object3DBranchFactory();
      }
    } catch (AccessControlException ex) {
      // Can't access to properties
    }
    this.object3dFactory = object3dFactory;

    this.controller = controller;

    this.patternImagesCache = new HashMap<TextureImage, BufferedImage>();
    // Install default colors using same colors as a text field
	  //PJPJPJ general colors setting can just come form the UI manager, unless I'm drawing a label
   // super.setForeground(UIManager.getColor("TextField.foreground"));
   // super.setBackground(UIManager.getColor("TextField.background"));

    //PJ taken from HomePane
	Number viewportX = home.getNumericProperty(PLAN_VIEWPORT_X_VISUAL_PROPERTY);
	setXScroll(viewportX != null ?  viewportX.intValue() : 0);
	Number viewportY = home.getNumericProperty(PLAN_VIEWPORT_Y_VISUAL_PROPERTY);
	setYScroll(viewportY != null? viewportY.intValue(): 0);
  }

  /**
   * Adds home items and selection listeners on this component to receive
   * changes notifications from home.
   */
  private void addModelListeners(final Home home, final UserPreferences preferences,
                                 final PlanController controller) {
    // Add listener to update plan when furniture changes
    final PropertyChangeListener furnitureChangeListener = new PropertyChangeListener() {
        public void propertyChange(final PropertyChangeEvent ev) {
					if (furnitureTopViewIconKeys != null
              && (HomePieceOfFurniture.Property.MODEL.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.MODEL_ROTATION.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.BACK_FACE_SHOWN.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.MODEL_TRANSFORMATIONS.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.ROLL.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.PITCH.name().equals(ev.getPropertyName())
                  || (HomePieceOfFurniture.Property.WIDTH_IN_PLAN.name().equals(ev.getPropertyName())
                      || HomePieceOfFurniture.Property.DEPTH_IN_PLAN.name().equals(ev.getPropertyName())
                      || HomePieceOfFurniture.Property.HEIGHT_IN_PLAN.name().equals(ev.getPropertyName()))
                     && (((HomePieceOfFurniture)ev.getSource()).isHorizontallyRotated()
                         || ((HomePieceOfFurniture)ev.getSource()).getTexture() != null)
                  || HomePieceOfFurniture.Property.MODEL_MIRRORED.name().equals(ev.getPropertyName())
                     && ((HomePieceOfFurniture)ev.getSource()).getRoll() != 0)) {
            if (HomePieceOfFurniture.Property.HEIGHT_IN_PLAN.name().equals(ev.getPropertyName())) {
              sortedLevelFurniture = null;
            }
            if (!(ev.getSource() instanceof HomeFurnitureGroup)) {
                // Invalidate top icon only for individual pieces because
                // groups can't have their own texture, can't be transformed and can't be horizontally rotated
                if (controller == null || !controller.isModificationState()) {
                    furnitureTopViewIconKeys.remove((HomePieceOfFurniture)ev.getSource());
                } else {
                    // Delay computing of new top view icon
                  if (invalidFurnitureTopViewIcons == null) {
                    invalidFurnitureTopViewIcons = new HashSet<HomePieceOfFurniture>();
                    controller.addPropertyChangeListener(PlanController.Property.MODIFICATION_STATE, new PropertyChangeListener() {
                        public void propertyChange(PropertyChangeEvent ev2) {
                          for (HomePieceOfFurniture piece : invalidFurnitureTopViewIcons) {
                            furnitureTopViewIconKeys.remove(piece);
                          }
                          invalidFurnitureTopViewIcons = null;
                            repaint();
                            controller.removePropertyChangeListener(PlanController.Property.MODIFICATION_STATE, this);
                        }
                    });
                  }
                  invalidFurnitureTopViewIcons.add((HomePieceOfFurniture)ev.getSource());
                }
            }
            revalidate();
          } else if (furnitureTopViewIconKeys != null
              && (HomePieceOfFurniture.Property.PLAN_ICON.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.COLOR.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.TEXTURE.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.MODEL_MATERIALS.name().equals(ev.getPropertyName())
                  || HomePieceOfFurniture.Property.SHININESS.name().equals(ev.getPropertyName()))) {
						// From version 5.2, these changes can happen only for individual pieces because groups
						// can't have their own color, texture, materials and shininess anymore
						furnitureTopViewIconKeys.remove((HomePieceOfFurniture)ev.getSource());
						repaint();
          } else if (HomePieceOfFurniture.Property.ELEVATION.name().equals(ev.getPropertyName())
                     || HomePieceOfFurniture.Property.LEVEL.name().equals(ev.getPropertyName())
                     || HomePieceOfFurniture.Property.HEIGHT_IN_PLAN.name().equals(ev.getPropertyName())) {
            sortedLevelFurniture = null;
            repaint();
          } else if (HomePieceOfFurniture.Property.ICON.name().equals(ev.getPropertyName())
                     || HomeDoorOrWindow.Property.WALL_CUT_OUT_ON_BOTH_SIDES.name().equals(ev.getPropertyName())) {
            // Should repaint only if icons rather than plan icons or top views are drawn but this may depends on various criteria
            repaint();
          } else if (doorOrWindowWallThicknessAreasCache != null
                     && (HomePieceOfFurniture.Property.WIDTH.name().equals(ev.getPropertyName())
                         || HomePieceOfFurniture.Property.DEPTH.name().equals(ev.getPropertyName())
                         || HomePieceOfFurniture.Property.ANGLE.name().equals(ev.getPropertyName())
                         || HomePieceOfFurniture.Property.MODEL_MIRRORED.name().equals(ev.getPropertyName())
                         || HomePieceOfFurniture.Property.X.name().equals(ev.getPropertyName())
                         || HomePieceOfFurniture.Property.Y.name().equals(ev.getPropertyName())
                         || HomePieceOfFurniture.Property.LEVEL.name().equals(ev.getPropertyName())
                         || HomeDoorOrWindow.Property.WALL_THICKNESS.name().equals(ev.getPropertyName())
                         || HomeDoorOrWindow.Property.WALL_DISTANCE.name().equals(ev.getPropertyName())
                         || HomeDoorOrWindow.Property.WALL_WIDTH.name().equals(ev.getPropertyName())
                         || HomeDoorOrWindow.Property.WALL_LEFT.name().equals(ev.getPropertyName())
                         || HomeDoorOrWindow.Property.CUT_OUT_SHAPE.name().equals(ev.getPropertyName()))
                     && doorOrWindowWallThicknessAreasCache.remove(ev.getSource()) != null) {
            revalidate();
          } else {
            revalidate();
          }
        }
      };
    for (HomePieceOfFurniture piece : home.getFurniture()) {
      piece.addPropertyChangeListener(furnitureChangeListener);
      if (piece instanceof HomeFurnitureGroup) {
        for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
          childPiece.addPropertyChangeListener(furnitureChangeListener);
        }
      }
    }
    home.addFurnitureListener(new CollectionListener<HomePieceOfFurniture>() {
        public void collectionChanged(CollectionEvent<HomePieceOfFurniture> ev) {
          HomePieceOfFurniture piece = ev.getItem();
          if (ev.getType() == CollectionEvent.Type.ADD) {
            piece.addPropertyChangeListener(furnitureChangeListener);
            if (piece instanceof HomeFurnitureGroup) {
              for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
                childPiece.addPropertyChangeListener(furnitureChangeListener);
              }
            }
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            piece.removePropertyChangeListener(furnitureChangeListener);
            if (piece instanceof HomeFurnitureGroup) {
              for (HomePieceOfFurniture childPiece : ((HomeFurnitureGroup)piece).getAllFurniture()) {
                childPiece.removePropertyChangeListener(furnitureChangeListener);
              }
            }
          }
          sortedLevelFurniture = null;
          revalidate();
        }
      });

    // Add listener to update plan when walls change
    final PropertyChangeListener wallChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Wall.Property.X_START.name().equals(propertyName)
              || Wall.Property.X_END.name().equals(propertyName)
              || Wall.Property.Y_START.name().equals(propertyName)
              || Wall.Property.Y_END.name().equals(propertyName)
              || Wall.Property.WALL_AT_START.name().equals(propertyName)
              || Wall.Property.WALL_AT_END.name().equals(propertyName)
              || Wall.Property.THICKNESS.name().equals(propertyName)
              || Wall.Property.ARC_EXTENT.name().equals(propertyName)
              || Wall.Property.PATTERN.name().equals(propertyName)) {
            if (home.isAllLevelsSelection()) {
              otherLevelsWallAreaCache = null;
              otherLevelsWallsCache = null;
            }
            wallAreasCache = null;
            doorOrWindowWallThicknessAreasCache = null;
            revalidate();
          } else if (Wall.Property.LEVEL.name().equals(propertyName)
              || Wall.Property.HEIGHT.name().equals(propertyName)
              || Wall.Property.HEIGHT_AT_END.name().equals(propertyName)) {
            otherLevelsWallAreaCache = null;
            otherLevelsWallsCache = null;
            wallAreasCache = null;
            repaint();
          }
        }
      };
    for (Wall wall : home.getWalls()) {
      wall.addPropertyChangeListener(wallChangeListener);
    }
    home.addWallsListener(new CollectionListener<Wall> () {
        public void collectionChanged(CollectionEvent<Wall> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(wallChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(wallChangeListener);
          }
          otherLevelsWallAreaCache = null;
          otherLevelsWallsCache = null;
          wallAreasCache = null;
          doorOrWindowWallThicknessAreasCache = null;
          revalidate();
        }
      });

    // Add listener to update plan when rooms change
    final PropertyChangeListener roomChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Room.Property.POINTS.name().equals(propertyName)
              || Room.Property.NAME.name().equals(propertyName)
              || Room.Property.NAME_X_OFFSET.name().equals(propertyName)
              || Room.Property.NAME_Y_OFFSET.name().equals(propertyName)
              || Room.Property.NAME_STYLE.name().equals(propertyName)
              || Room.Property.NAME_ANGLE.name().equals(propertyName)
              || Room.Property.AREA_VISIBLE.name().equals(propertyName)
              || Room.Property.AREA_X_OFFSET.name().equals(propertyName)
              || Room.Property.AREA_Y_OFFSET.name().equals(propertyName)
              || Room.Property.AREA_STYLE.name().equals(propertyName)
              || Room.Property.AREA_ANGLE.name().equals(propertyName)) {
            sortedLevelRooms = null;
            otherLevelsRoomAreaCache = null;
            otherLevelsRoomsCache = null;
            revalidate();
          } else if (preferences.isRoomFloorColoredOrTextured()
                     && (Room.Property.FLOOR_COLOR.name().equals(propertyName)
                         || Room.Property.FLOOR_TEXTURE.name().equals(propertyName)
                         || Room.Property.FLOOR_VISIBLE.name().equals(propertyName))) {
            repaint();
          }
        }
      };
    for (Room room : home.getRooms()) {
      room.addPropertyChangeListener(roomChangeListener);
    }
    home.addRoomsListener(new CollectionListener<Room> () {
        public void collectionChanged(CollectionEvent<Room> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(roomChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(roomChangeListener);
          }
          sortedLevelRooms = null;
          otherLevelsRoomAreaCache = null;
          otherLevelsRoomsCache = null;
          revalidate();
        }
      });

     // Add listener to update plan when polylines change
     final PropertyChangeListener changeListener = new PropertyChangeListener() {
         public void propertyChange(PropertyChangeEvent ev) {
           String propertyName = ev.getPropertyName();
           if (Polyline.Property.COLOR.name().equals(propertyName)
               || Polyline.Property.DASH_STYLE.name().equals(propertyName)) {
             repaint();
           } else {
             revalidate();
           }
         }
       };
     for (Polyline polyline : home.getPolylines()) {
       polyline.addPropertyChangeListener(changeListener);
     }
     home.addPolylinesListener(new CollectionListener<Polyline>() {
        public void collectionChanged(CollectionEvent<Polyline> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(changeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(changeListener);
          }
          revalidate();
        }
      });

    // Add listener to update plan when dimension lines change
    final PropertyChangeListener dimensionLineChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          revalidate();
        }
      };
    for (DimensionLine dimensionLine : home.getDimensionLines()) {
      dimensionLine.addPropertyChangeListener(dimensionLineChangeListener);
    }
    home.addDimensionLinesListener(new CollectionListener<DimensionLine> () {
        public void collectionChanged(CollectionEvent<DimensionLine> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(dimensionLineChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(dimensionLineChangeListener);
          }
          revalidate();
        }
      });

    // Add listener to update plan when labels change
    final PropertyChangeListener labelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          revalidate();
        }
      };
    for (Label label : home.getLabels()) {
      label.addPropertyChangeListener(labelChangeListener);
    }
    home.addLabelsListener(new CollectionListener<Label> () {
        public void collectionChanged(CollectionEvent<Label> ev) {
          if (ev.getType() == CollectionEvent.Type.ADD) {
            ev.getItem().addPropertyChangeListener(labelChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            ev.getItem().removePropertyChangeListener(labelChangeListener);
          }
          revalidate();
        }
      });

    // Add listener to update plan when levels change
    final PropertyChangeListener levelChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Level.Property.BACKGROUND_IMAGE.name().equals(propertyName)) {
            backgroundImageCache = null;
            revalidate();
          } else if (Level.Property.ELEVATION.name().equals(propertyName)
                     || Level.Property.ELEVATION_INDEX.name().equals(propertyName)
                     || Level.Property.VIEWABLE.name().equals(propertyName)) {
            backgroundImageCache = null;
            otherLevelsWallAreaCache = null;
            otherLevelsWallsCache = null;
            otherLevelsRoomAreaCache = null;
            otherLevelsRoomsCache = null;
            wallAreasCache = null;
            doorOrWindowWallThicknessAreasCache = null;
            sortedLevelFurniture = null;
            sortedLevelRooms = null;
            repaint();
          }
        }
      };
    for (Level level : home.getLevels()) {
      level.addPropertyChangeListener(levelChangeListener);
    }
    home.addLevelsListener(new CollectionListener<Level> () {
        public void collectionChanged(CollectionEvent<Level> ev) {
          Level level = ev.getItem();
          if (ev.getType() == CollectionEvent.Type.ADD) {
            level.addPropertyChangeListener(levelChangeListener);
          } else if (ev.getType() == CollectionEvent.Type.DELETE) {
            level.removePropertyChangeListener(levelChangeListener);
          }
          revalidate();
        }
      });

    home.addPropertyChangeListener(Home.Property.CAMERA, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          revalidate();
        }
      });
    home.getObserverCamera().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Camera.Property.X.name().equals(propertyName)
              || Camera.Property.Y.name().equals(propertyName)
              || Camera.Property.FIELD_OF_VIEW.name().equals(propertyName)
              || Camera.Property.YAW.name().equals(propertyName)
              || ObserverCamera.Property.WIDTH.name().equals(propertyName)
              || ObserverCamera.Property.DEPTH.name().equals(propertyName)
              || ObserverCamera.Property.HEIGHT.name().equals(propertyName)) {
            revalidate();
          }
        }
      });
    home.getCompass().addPropertyChangeListener(new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          String propertyName = ev.getPropertyName();
          if (Compass.Property.X.name().equals(propertyName)
              || Compass.Property.Y.name().equals(propertyName)
              || Compass.Property.NORTH_DIRECTION.name().equals(propertyName)
              || Compass.Property.DIAMETER.name().equals(propertyName)
              || Compass.Property.VISIBLE.name().equals(propertyName)) {
            revalidate();
          }
        }
      });
    home.addSelectionListener(new SelectionListener () {
        public void selectionChanged(SelectionEvent ev) {
          repaint();
        }
      });
    home.addPropertyChangeListener(Home.Property.BACKGROUND_IMAGE,
      new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          backgroundImageCache = null;
          repaint();
        }
      });
    home.addPropertyChangeListener(Home.Property.SELECTED_LEVEL, new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          backgroundImageCache = null;
          otherLevelsWallAreaCache = null;
          otherLevelsWallsCache = null;
          otherLevelsRoomAreaCache = null;
          otherLevelsRoomsCache = null;
          wallAreasCache = null;
          doorOrWindowWallThicknessAreasCache = null;
          sortedLevelRooms = null;
          sortedLevelFurniture = null;
          repaint();
        }
      });
    UserPreferencesChangeListener preferencesListener = new UserPreferencesChangeListener(this);
    preferences.addPropertyChangeListener(UserPreferences.Property.UNIT, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.GRID_VISIBLE, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.DEFAULT_FONT_NAME, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.FURNITURE_VIEWED_FROM_TOP, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.FURNITURE_MODEL_ICON_SIZE, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.ROOM_FLOOR_COLORED_OR_TEXTURED, preferencesListener);
    preferences.addPropertyChangeListener(UserPreferences.Property.WALL_PATTERN, preferencesListener);
  }

  /**
   * Preferences property listener bound to this component with a weak reference to avoid
   * strong link between preferences and this component.
   */
  private static class UserPreferencesChangeListener implements PropertyChangeListener {
    private WeakReference<PlanComponent>  planComponent;

    public UserPreferencesChangeListener(PlanComponent planComponent) {
      this.planComponent = new WeakReference<PlanComponent>(planComponent);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If plan component was garbage collected, remove this listener from preferences
      PlanComponent planComponent = this.planComponent.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      UserPreferences.Property property = UserPreferences.Property.valueOf(ev.getPropertyName());
      if (planComponent == null) {
        preferences.removePropertyChangeListener(property, this);
      } else {
        switch (property) {
          case LANGUAGE :
          case UNIT :
            // Update format of tool tip text fields
            for (Map.Entry<PlanController.EditableProperty, JFormattedTextField> toolTipTextFieldEntry :
              planComponent.toolTipEditableTextFields.entrySet()) {
              updateToolTipTextFieldFormatterFactory(toolTipTextFieldEntry.getValue(),
                  toolTipTextFieldEntry.getKey(), preferences);
            }
            if (planComponent.horizontalRuler != null) {
              planComponent.horizontalRuler.repaint();
            }
            if (planComponent.verticalRuler != null) {
              planComponent.verticalRuler.repaint();
            }
            break;
          case DEFAULT_FONT_NAME :
            planComponent.fonts = null;
            planComponent.fontsMetrics = null;
            planComponent.revalidate();
            break;
          case WALL_PATTERN :
            planComponent.wallAreasCache = null;
            break;
          case FURNITURE_VIEWED_FROM_TOP :
						if (planComponent.furnitureTopViewIconKeys != null
										&& !preferences.isFurnitureViewedFromTop()) {
							planComponent.furnitureTopViewIconKeys = null;
							planComponent.furnitureTopViewIconsCache = null;
						}
            break;
          case FURNITURE_MODEL_ICON_SIZE :
						planComponent.furnitureTopViewIconKeys = null;
            planComponent.furnitureTopViewIconsCache = null;
            break;
          default:
            break;
        }
        planComponent.repaint();
      }
    }
  }

  /**
   * Revalidates and repaints this component and its rulers.
   */
  @Override
  public void revalidate() {
    invalidate(true);
  }

  /**
   * Invalidates this component voiding plan bounds cache if <code>invalidatePlanBoundsCache</code> is <code>true</code>.
	 * Validates this component and updates viewport position if it's displayed in a scrolled pane.
   */
  protected void invalidate(boolean invalidatePlanBoundsCache) {

      if (invalidatePlanBoundsCache) {
          boolean planBoundsCacheWereValid = this.planBoundsCacheValid;
          if (this.invalidPlanBounds == null) {
              this.invalidPlanBounds = getPlanBounds().getBounds2D();
          }
          if (planBoundsCacheWereValid) {
              this.planBoundsCacheValid = false;
          }
      }

    // Revalidate and repaint
    super.revalidate();
    repaint();

    if (this.invalidPlanBounds != null
      && true ) { //getParent() instanceof JViewport) {
      float planBoundsNewMinX = (float)getPlanBounds().getMinX();
      float planBoundsNewMinY = (float)getPlanBounds().getMinY();
      // If plan bounds upper left corner diminished (PJ in fact any min plan change)
      if (planBoundsNewMinX != this.invalidPlanBounds.getMinX()
          || planBoundsNewMinY != this.invalidPlanBounds.getMinY()) {
        //JViewport parent = (JViewport)getParent();
        //final Point viewPosition = getViewPosition();
        //Dimension extentSize = parent.getExtentSize();
        //Dimension viewSize = parent.getViewSize();
        // Update view position when scroll bars are visible
        //if (extentSize.width < viewSize.width
            //   || extentSize.height < viewSize.height) {
          int deltaX = Math.round(((float)this.invalidPlanBounds.getMinX() - planBoundsNewMinX));
          int deltaY = Math.round(((float)this.invalidPlanBounds.getMinY() - planBoundsNewMinY));
            this.moveScrolledX(deltaX);
            this.moveScrolledY(deltaY);
            //parent.setViewPosition(new Point(viewPosition.x + deltaX, viewPosition.y + deltaY));
        //}
      }
    }
    this.invalidPlanBounds = null;
  }

  /**
   * Adds AWT mouse listeners to this component that calls back <code>controller</code> methods.
   */
  private void addMouseListeners(final PlanController controller) {
	  mScaleDetector = new ScaleGestureDetector( this.getDrawableView().getContext(), new ScaleListener());
	  this.getDrawableView().setOnTouchListener(touchyListener);
	  longHoldHandler = new LongHoldHandler(this.getDrawableView().getResources().getDisplayMetrics(), 300, 0,
			  new LongHoldHandler.Callback (){
				  public void longHoldRepeat(MotionEvent lastMotionEvent){
					  final float x = lastMotionEvent.getX();
					  final float y = lastMotionEvent.getY();
					  // first two ensure a select then the final edits
					  controller.pressMouse(convertXPixelToModel((int) x), convertYPixelToModel((int) y),
							  1, false, false, false, false);
					  controller.releaseMouse(convertXPixelToModel((int) x), convertYPixelToModel((int) y));
					  controller.pressMouse(convertXPixelToModel((int) x), convertYPixelToModel((int) y),
							  2, false, false, false, false);
				  }
			  });
  }

	/**
	 * used to simulate a final press when changing plancontroller state
	 * @return
	 */
	public Point2f getLastDragLocation() {
	  	return touchyListener.getLastDragLocation();
	}

	TouchyListener touchyListener = new TouchyListener();
	LongHoldHandler longHoldHandler;
	private class TouchyListener implements android.view.View.OnTouchListener {
		// used to record downs so that when ups occur they can be fired off as downs
		private MotionEvent potentialSinglePress = null;

		public static final int INVALID_POINTER_ID = -1;

		// The ‘active pointer’ is the one currently moving our object.
		private int mActivePointerId = INVALID_POINTER_ID;
		// used to record previous move events so the delta can be sent through
		private float mLastTouchX;
		private float mLastTouchY;

		// used for double clicks, that off disabled by default in prefs
		private long lastMouseReleasedTime = 0;
		// used to ignore mouseMoved events that follows a mousePressed at the same location (Linux notifies this kind of events)
		private Point lastMousePressedLocation;
		//used to understand the auto pan operation for drags in selection mode
		private boolean lastDownTouchedSelections = false;

		public Point2f getLastDragLocation() {
			return new Point2f(mLastTouchX, mLastTouchY);
		}

		@Override
		public boolean onTouch(View v, MotionEvent ev) {
			if(mScaleDetector != null ) {
				mScaleDetector.onTouchEvent(ev);
				if (mScaleDetector.isInProgress())
					return true;
			}

			// Note the long hold removes the move jitters, so almost nothing completes a down/up click without it
			if (longHoldHandler != null && longHoldHandler.onTouch(v, ev))
				return true;

			final int pointerIndex = ev.getActionIndex();
			final int pointerId = ev.getPointerId(pointerIndex);
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);
			final int action = ev.getActionMasked();

			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					mLastTouchX = x;
					mLastTouchY = y;
					mActivePointerId = pointerId;

					if (ev.getPointerCount() == 1) {
						// we need to fire off the previous down event now, though lord knows how this could happen
						if (potentialSinglePress != null) {
							mousePressed(v, potentialSinglePress);
							potentialSinglePress = null;
						}

						// have we just touched down in the selected items? If so we need to record so a move item occurs when the mouse move arrives
						float modelX = convertXPixelToModel((int)x);
						float modelY = convertYPixelToModel((int)y);

						List<Selectable> itemsUnderTouch = controller.getSelectableItemsAt(modelX, modelY);
						lastDownTouchedSelections = false;
						// contains any
						for( Selectable s : itemsUnderTouch) {
							if( home.getSelectedItems().contains(s)) {
								lastDownTouchedSelections = true;
								break;
							}
						}

						// check for handles
						if (controller.getRotatedLabelAt(modelX, modelY) != null || controller.getYawRotatedCameraAt(modelX, modelY) != null
							|| controller.getPitchRotatedCameraAt(modelX, modelY) != null || controller.getElevatedLabelAt(modelX, modelY) != null
							|| controller.getElevatedCameraAt(modelX, modelY) != null || controller.getRoomNameAt(modelX, modelY) != null
							|| controller.getRoomRotatedNameAt(modelX, modelY) != null || controller.getRoomAreaAt(modelX, modelY) != null
							|| controller.getRoomRotatedAreaAt(modelX, modelY) != null || controller.getResizedDimensionLineStartAt(modelX, modelY) != null
							|| controller.getResizedDimensionLineEndAt(modelX, modelY) != null || controller.getWidthAndDepthResizedPieceOfFurnitureAt(modelX, modelY) != null
							|| controller.getResizedWallStartAt(modelX, modelY) != null || controller.getResizedWallEndAt(modelX, modelY) != null
							|| controller.getResizedRoomAt(modelX, modelY) != null || controller.getOffsetDimensionLineAt(modelX, modelY) != null
							|| controller.getResizedPolylineAt(modelX, modelY) != null || controller.getPitchRotatedPieceOfFurnitureAt(modelX, modelY) != null
							|| controller.getRollRotatedPieceOfFurnitureAt(modelX, modelY) != null || controller.getModifiedLightPowerAt(modelX, modelY) != null
							|| controller.getHeightResizedPieceOfFurnitureAt(modelX, modelY) != null || controller.getRotatedPieceOfFurnitureAt(modelX, modelY) != null
							|| controller.getElevatedPieceOfFurnitureAt(modelX, modelY) != null || controller.getPieceOfFurnitureNameAt(modelX, modelY) != null
							|| controller.getPieceOfFurnitureRotatedNameAt(modelX, modelY) != null || controller.getRotatedCompassAt(modelX, modelY) != null
							|| controller.getResizedCompassAt(modelX, modelY) != null )
						{
							lastDownTouchedSelections = true;
						}

						// this will be fired on a move or an up or another single down
						potentialSinglePress = MotionEvent.obtain(ev); //instead of mousePressed(v, ev);

					}
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					// second finger down now
					// cancel any pending single down as we are now firmly in double finger mode
					potentialSinglePress = null;
					break;
				case MotionEvent.ACTION_MOVE:
					final float dx = x - mLastTouchX;
					final float dy = y - mLastTouchY;
					if (ev.getPointerCount() > 1) {
						// might be the second finger move, we want only active moves
						if(mActivePointerId == pointerId) {
							// pan operation wants the move to be div scale as moveview does a multiply, so...
							moveView(-dx, -dy);
							mLastTouchX = x;
							mLastTouchY = y;
						}
						potentialSinglePress = null;
					} else {
						// pointer count (and hence finger count) == 1

						// stops any double taps
						lastMouseReleasedTime = 0;

						// ignore all moves if room points are activated
						if(addRoomPointActivated || deleteRoomPointActivated) {
							return true;
						}

						// this is a major divergence from the desktop function! Single finger pan during selection mode if the move is over nothing
						if (controller.getMode() == PlanController.Mode.PANNING ||
										(controller.getMode() == PlanController.Mode.SELECTION
														&& !selectLasso && !lastDownTouchedSelections	) ) {

							if(mActivePointerId == pointerId) {
								// pan operation wants the move to be div scale as moveview does a multiply, so...
								moveView(-dx, -dy);
								mLastTouchX = x;
								mLastTouchY = y;
							}

							// we also cancel any pending press cos that's been confirmed unwanted
							potentialSinglePress = null;
						} else {
							// if we have a pending click fire it off now before moving
							if (potentialSinglePress != null) {
								mousePressed(v, potentialSinglePress);
								potentialSinglePress = null;
							}
							if(mActivePointerId == pointerId) {
								mouseMoved(v, ev);
								mLastTouchX = x;
								mLastTouchY = y;
							}
						}
					}
					break;
				case MotionEvent.ACTION_UP:
					mActivePointerId = INVALID_POINTER_ID;
					lastDownTouchedSelections = false;
					// make sure this isn't the exit of a double touch too
					if (ev.getPointerCount() == 1 ) {
						// all is good, fire off the down now
						if (potentialSinglePress != null) {
							mousePressed(v, potentialSinglePress);
							potentialSinglePress = null;
						}

						mouseReleased(v, ev);
					}
					break;
				case MotionEvent.ACTION_CANCEL:
					mActivePointerId = INVALID_POINTER_ID;
					potentialSinglePress = null;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					// second finger has been released
					// Was this our active pointer going up, if so choose a new active pointer and adjust accordingly
					if (pointerId == mActivePointerId) {
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						mLastTouchX = ev.getX(newPointerIndex);
						mLastTouchY = ev.getY(newPointerIndex);
						mActivePointerId = ev.getPointerId(newPointerIndex);
					}
					break;
			}
			return true;
		}



		public void mousePressed(View v, MotionEvent ev) {
			// no selection or edits while a dialog is up, note PlanComponent is not on screen at all.
			if(((Renovations3DActivity)getDrawableView().getContext()).currentDialog == null
							|| !((Renovations3DActivity)getDrawableView().getContext()).currentDialog.isShowing()) {
				final int pointerIndex = ev.getActionIndex();
				final float x = ev.getX(pointerIndex);
				final float y = ev.getY(pointerIndex);
				this.lastMousePressedLocation = new Point((int) x, (int) y);
				if (isEnabled()) {
					//requestFocusInWindow();
					if (true) // (ev.getButton() == MouseEvent.BUTTON1)
					{
						//alignment and magnetism come from menu now
						boolean alignmentActivated = PlanComponent.this.alignmentActivated;
						boolean duplicationActivated = ((MultipleLevelsPlanPanel) controller.getView()).getIsControlKeyOn();
						boolean isShiftDown = controller.getMode() == PlanController.Mode.SELECTION && PlanComponent.this.selectMultiple;

						int clickCount = !Renovations3DActivity.DOUBLE_TAP_EDIT_2D || lastMouseReleasedTime == 0 ? 1 : (System.currentTimeMillis() - lastMouseReleasedTime < 250 ? 2 : 1);

						// if it's a double, ensure triple != double twice
						lastMouseReleasedTime = clickCount == 1 ? System.currentTimeMillis() : 0;

						// handle special cases of mouse click (from desktop right click menu)
						if(addRoomPointActivated) {
							addRoomPointActivated = false;
							controller.addPointToSelectedRoom(convertXPixelToModel((int) x), convertYPixelToModel((int) y));
						} else if (deleteRoomPointActivated) {
							deleteRoomPointActivated = false;
							controller.deletePointFromSelectedRoom(convertXPixelToModel((int) x), convertYPixelToModel((int) y));
						} else {
							try {
								controller.pressMouse(convertXPixelToModel((int) x), convertYPixelToModel((int) y),
												clickCount, isShiftDown,
												alignmentActivated, duplicationActivated, false);
							} catch (ArrayIndexOutOfBoundsException e) {
								// TODO this happens :at com.eteks.sweethome3d.viewcontroller.PlanController$PolylineResizeState.enter(PlanController.java:11465)
								//ignore for now, investigate later
								e.printStackTrace();
							}
						}
					}
				}
			}
		}

		public void mouseReleased(View v, MotionEvent ev) {
			// no selection or edits while a dialog is up, note PlanComponent is not on screen at all.
			if(((Renovations3DActivity)getDrawableView().getContext()).currentDialog == null
							|| !((Renovations3DActivity)getDrawableView().getContext()).currentDialog.isShowing()) {
				final int pointerIndex = ev.getActionIndex();
				final float x = ev.getX(pointerIndex);
				final float y = ev.getY(pointerIndex);
				if (isEnabled()) {
					try {
						controller.releaseMouse(convertXPixelToModel((int) x), convertYPixelToModel((int) y));
					} catch (ArrayIndexOutOfBoundsException e) {
						// TODO this happens :at com.eteks.sweethome3d.viewcontroller.PlanController$PolylineResizeState.enter(PlanController.java:11465)
						//ignore for now, investigate later
						e.printStackTrace();
					}
				}
			}
		}

		public void mouseMoved(View v, MotionEvent ev) {
			final int pointerIndex = ev.getActionIndex();
			final float x = ev.getX(pointerIndex);
			final float y = ev.getY(pointerIndex);
			// Ignore mouseMoved events that follows a mousePressed at the same location (Linux notifies this kind of events)
			if (this.lastMousePressedLocation != null
							&& !this.lastMousePressedLocation.equals(new Point((int) x, (int) y))) {
				this.lastMousePressedLocation = null;
			}
			if (this.lastMousePressedLocation == null) {
				if (isEnabled()) {
					try {
						controller.moveMouse(convertXPixelToModel((int) x), convertYPixelToModel((int) y));
					} catch(ArrayIndexOutOfBoundsException e) {
						// TODO this happens :at com.eteks.sweethome3d.viewcontroller.PlanController$PolylineResizeState.enter(PlanController.java:11465)
						//ignore for now, investigate later
						e.printStackTrace();
					}
				}
			}
		}

		public void mouseDragged(View v, MotionEvent ev) {
			if (isEnabled()) {
				mouseMoved(v, ev);
			}
		}
	}

	private ScaleGestureDetector mScaleDetector;

	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		/**
		 * @param detector
		 * @return
		 */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			// this is to allow 2 finger drags to work so this is not about how far the scale moves,
			// just about how close the fingers are when deciding if this is a 2 drag or not
			// 2 finger is handy as single finger panning is only on during selection or pan modes

			DisplayMetrics mDisplayMetrics = getDrawableView().getResources().getDisplayMetrics();
			float measurement = mScaleDetector.getCurrentSpan() / (float) mDisplayMetrics.densityDpi;

		/*	if(PlanComponent.this.controller.getMode() == PlanController.Mode.PANNING ||
							PlanComponent.this.controller.getMode() == PlanController.Mode.SELECTION )
				return true;
			else*/
			return measurement > dpiMinSpanForZoom;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			scaleAt(detector.getScaleFactor(), (int) detector.getFocusX(), (int) detector.getFocusY());
			return true;
		}
	}

	public void scaleAt(float scaleChangeFactor, int atX, int atY) {
		float priorX = convertXPixelToModel(atX);
		float priorY = convertYPixelToModel(atY);

		float oldScale = getScale();
		//don't want the mouse move calls so instead of controller.zoom((float)(ev.getWheelRotation() < 0 ...
		controllerSetScale(getScale() * scaleChangeFactor);

		if (getScale() != oldScale && true) {// getParent() instanceof JViewport) {
			// If scale changed, update viewport position to keep the same coordinates under mouse cursor
			//((JViewport)getParent()).setViewPosition(new Point());
			//moveView(mouseX - convertXPixelToModel(deltaX), mouseY - convertYPixelToModel(deltaY));
			moveScrolledX(priorX - convertXPixelToModel(atX));
			moveScrolledY(priorY - convertYPixelToModel(atY));
		}
	}

	/**
	 * Taken from PlanController.setScale(float scale)
	 * used here to avoid the unwanted mouse move call in the controller code
	 */
	private void controllerSetScale(float scale) {
		scale = Math.max(controller.getMinimumScale(), Math.min(scale, controller.getMaximumScale()));
		if(scale != controller.getView().getScale()) {
			//float oldScale = controller.getView().getScale();
			//controller.furnitureSidesCache.clear();
			if(controller.getView() != null) {
				//int x = controller.getView().convertXModelToScreen(controller.getXLastMouseMove());
				//int y = controller.getView().convertXModelToScreen(controller.getYLastMouseMove());
				controller.getView().setScale(scale);
				//controller.moveMouse(controller.getView().convertXPixelToModel(x), controller.getView().convertYPixelToModel(y));
			}
			//controller.propertyChangeSupport.firePropertyChange(PlanController.Property.SCALE.name(), Float.valueOf(oldScale), Float.valueOf(scale));
			home.setProperty(SCALE_VISUAL_PROPERTY, String.valueOf(scale));
		}
	}

	protected void scrollRectToVisible(Rectangle shapePixelBounds) {
		// use the margin to push on the edges a bit to make a bumper
		shapePixelBounds.grow((int) MARGIN,(int) MARGIN);
		super.scrollRectToVisible(shapePixelBounds);
	}

	/**
	 * Adds a listener to the controller to follow changes in base plan modification state.
	 */
	private void addControllerListener(final PlanController controller) {
		controller.addPropertyChangeListener(PlanController.Property.BASE_PLAN_MODIFICATION_STATE,
						new PropertyChangeListener() {
							public void propertyChange(PropertyChangeEvent ev) {
								boolean wallsDoorsOrWindowsModification = controller.isBasePlanModificationState();
								if (wallsDoorsOrWindowsModification) {
									// Limit base plan modification state to walls creation/handling and doors or windows handling
									if (controller.getMode() != PlanController.Mode.WALL_CREATION) {
										for (Selectable item : (draggedItemsFeedback != null ? draggedItemsFeedback : home.getSelectedItems())) {
											if (!(item instanceof Wall)
															&& !(item instanceof HomePieceOfFurniture && ((HomePieceOfFurniture)item).isDoorOrWindow())) {
												wallsDoorsOrWindowsModification = false;
											}
										}
									}
								}
								if (PlanComponent.this.wallsDoorsOrWindowsModification != wallsDoorsOrWindowsModification) {
									PlanComponent.this.wallsDoorsOrWindowsModification = wallsDoorsOrWindowsModification;
									repaint();
								}
							}
						});
	}

	/**
	 * Installs default keys bound to actions.
	 */
	private void installDefaultKeyboardActions() {
	}

	/**
	 * Installs keys bound to actions during edition.
	 */
	private void installEditionKeyboardActions() {
	}

	/**
	 * Creates actions that calls back <code>controller</code> methods.
	 */
	private void createActions(final PlanController controller) {
	}

	/**
	 * Creates the text fields used in tool tip and their label.
	 */
	private void createToolTipTextFields(UserPreferences preferences,
																			 final PlanController controller) {
		//PJ this is used by the editable tooltips that aren't implemented yet
    this.toolTipEditableTextFields = new HashMap<PlanController.EditableProperty, JFormattedTextField>();
    //Font toolTipFont = UIManager.getFont("ToolTip.font");
    for (final PlanController.EditableProperty editableProperty : PlanController.EditableProperty.values()) {
      final JFormattedTextField textField = new JFormattedTextField(this.getDrawableView().getContext()) {
         /* @Override
          public Dimension getPreferredSize() {
            // Enlarge preferred size of one pixel
            Dimension preferredSize = super.getPreferredSize();
            return new Dimension(preferredSize.width + 1, preferredSize.height);
          }*/
        };
      updateToolTipTextFieldFormatterFactory(textField, editableProperty, preferences);
     // textField.setFont(toolTipFont);
     // textField.setOpaque(false);
     // textField.setBorder(null);
      if (controller != null) {
        // Add a listener to notify changes to controller
       /* textField.getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent ev) {
              try {
                textField.commitEdit();
                controller.updateEditableProperty(editableProperty, textField.getValue());
              } catch (ParseException ex) {
                controller.updateEditableProperty(editableProperty, null);
              }
            }

            public void insertUpdate(DocumentEvent ev) {
              changedUpdate(ev);
            }

            public void removeUpdate(DocumentEvent ev) {
              changedUpdate(ev);
            }
          });*/
      }

      this.toolTipEditableTextFields.put(editableProperty, textField);
    }
  }

  private static void updateToolTipTextFieldFormatterFactory(JFormattedTextField textField,
                                                             PlanController.EditableProperty editableProperty,
                                                             UserPreferences preferences) {
    /*InternationalFormatter formatter;
    if (editableProperty == PlanController.EditableProperty.ANGLE) {
      formatter = new NumberFormatter(NumberFormat.getNumberInstance());
    } else {
      Format lengthFormat = preferences.getLengthUnit().getFormat();
      if (lengthFormat instanceof NumberFormat) {
        formatter = new NumberFormatter((NumberFormat)lengthFormat);
      } else {
        formatter = new InternationalFormatter(lengthFormat);
      }
    }
    textField.setFormatterFactory(new DefaultFormatterFactory(formatter));*/
	}

	/**
	 * Returns a custom cursor with a hot spot point at center of cursor.
	 */

	/**
	 * Returns a custom cursor created from images in parameters.
	 */

	/**
	 * Returns the preferred size of this component.
	 */
/*  @Override
  public Dimension getPreferredSize() {
  //PJPJ removed as layout is totally different and always fullscreen
    if (isPreferredSizeSet()) {
      return super.getPreferredSize();
    } else {
    Insets insets = getInsets();
      Rectangle2D planBounds = getPlanBounds();
      return new Dimension(
          convertLengthToPixel(planBounds.getWidth() + MARGIN * 2) + insets.left + insets.right,
          convertLengthToPixel(planBounds.getHeight() + MARGIN * 2) + insets.top + insets.bottom);
    }
  }*/

  /**
   * Returns the bounds of the plan displayed by this component.
   */
  public Rectangle2D getPlanBounds() {
    if (!this.planBoundsCacheValid || this.planBoundsCache == null) {
      // Always enlarge plan bounds only when plan component is a child of a scroll pane
      if (this.planBoundsCache == null
			    || false) {// !(getParent() instanceof JViewport)) // never not child of viewport
        // Ensure plan bounds are 10 x 10 meters wide at minimum
        this.planBoundsCache = new Rectangle2D.Float(0, 0, 1000, 1000);
      }
      // Enlarge plan bounds to include background images, home bounds and observer camera
      if (this.backgroundImageCache != null) {
        BackgroundImage backgroundImage = this.home.getBackgroundImage();
        if (backgroundImage != null) {
          this.planBoundsCache.add(-backgroundImage.getXOrigin(), -backgroundImage.getYOrigin());
          this.planBoundsCache.add(this.backgroundImageCache.getWidth() * backgroundImage.getScale() - backgroundImage.getXOrigin(),
              this.backgroundImageCache.getHeight() * backgroundImage.getScale() - backgroundImage.getYOrigin());
        }
        for (Level level : this.home.getLevels()) {
          BackgroundImage levelBackgroundImage = level.getBackgroundImage();
          if (levelBackgroundImage != null) {
            this.planBoundsCache.add(-levelBackgroundImage.getXOrigin(), -levelBackgroundImage.getYOrigin());
            this.planBoundsCache.add(this.backgroundImageCache.getWidth() * levelBackgroundImage.getScale() - levelBackgroundImage.getXOrigin(),
                this.backgroundImageCache.getHeight() * levelBackgroundImage.getScale() - levelBackgroundImage.getYOrigin());
          }
        }
      }
			Graphics2D g = (Graphics2D)getGraphics();
			if (g != null) {
				setRenderingHints(g);
			}
			Rectangle2D homeItemsBounds = getItemsBounds(g, getPaintedItems());
      if (homeItemsBounds != null) {
        this.planBoundsCache.add(homeItemsBounds);
      }
      for (float [] point : this.home.getObserverCamera().getPoints()) {
        this.planBoundsCache.add(point [0], point [1]);
      }
      this.planBoundsCacheValid = true;
    }
    return this.planBoundsCache;
  }

  /**
   * Returns the collection of walls, furniture, rooms and dimension lines of the home
   * painted by this component wherever the level they belong to is selected or not.
   */
  protected List<Selectable> getPaintedItems() {
    return this.home.getSelectableViewableItems();
  }

  /**
   * Returns the bounds of the given collection of <code>items</code>.
   */
  private Rectangle2D getItemsBounds(Graphics g, Collection<? extends Selectable> items) {
    Rectangle2D itemsBounds = null;
    for (Selectable item : items) {
      if (itemsBounds == null) {
        itemsBounds = getItemBounds(g, item);
      } else {
        itemsBounds.add(getItemBounds(g, item));
      }
    }
    return itemsBounds;
  }

  /**
   * Returns the bounds of the given <code>item</code>.
   */
  protected Rectangle2D getItemBounds(Graphics g, Selectable item) {
    // Add to bounds all the visible items
    float [][] points = item.getPoints();
    Rectangle2D itemBounds = new Rectangle2D.Float(points [0][0], points [0][1], 0, 0);
    for (int i = 1; i < points.length; i++) {
      itemBounds.add(points [i][0], points [i][1]);
    }

    // Retrieve used font
    Font componentFont;
    if (g != null) {
      componentFont = g.getFont();
    } else {
      componentFont = getFont();
    }

    if (item instanceof Room) {
      // Add to bounds the displayed name and area bounds of each room
      Room room = (Room)item;
      float xRoomCenter = room.getXCenter();
      float yRoomCenter = room.getYCenter();
      String roomName = room.getName();
      if (roomName != null && roomName.length() > 0) {
        addTextBounds(room.getClass(),
            roomName, room.getNameStyle(),
            xRoomCenter + room.getNameXOffset(),
            yRoomCenter + room.getNameYOffset(), room.getNameAngle(), itemBounds);
      }
      if (room.isAreaVisible()) {
        float area = room.getArea();
        if (area > 0.01f) {
          String areaText = this.preferences.getLengthUnit().getAreaFormatWithUnit().format(area);
          addTextBounds(room.getClass(),
              areaText, room.getAreaStyle(),
              xRoomCenter + room.getAreaXOffset(),
              yRoomCenter + room.getAreaYOffset(), room.getAreaAngle(), itemBounds);
        }
      }
    } else if (item instanceof Polyline) {
      Polyline polyline = (Polyline)item;
      return ShapeTools.getPolylineShape(polyline.getPoints(),
          polyline.getJoinStyle() == Polyline.JoinStyle.CURVED, polyline.isClosedPath()).getBounds2D();
    } else if (item instanceof HomePieceOfFurniture) {
      if (item instanceof HomeDoorOrWindow) {
        HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)item;
        // Add to bounds door and window sashes
        for (Sash sash : doorOrWindow.getSashes()) {
          itemBounds.add(getDoorOrWindowSashShape(doorOrWindow, sash).getBounds2D());
        }
      } else if (item instanceof HomeFurnitureGroup) {
        itemBounds.add(getItemsBounds(g, ((HomeFurnitureGroup)item).getFurniture()));
      }
      // Add to bounds the displayed name of the piece of furniture
      HomePieceOfFurniture piece = (HomePieceOfFurniture)item;
      String pieceName = piece.getName();
      if (piece.isVisible()
          && piece.isNameVisible()
          && pieceName.length() > 0) {
        addTextBounds(piece.getClass(),
            pieceName, piece.getNameStyle(),
            piece.getX() + piece.getNameXOffset(),
            piece.getY() + piece.getNameYOffset(), piece.getNameAngle(), itemBounds);
      }
    } else if (item instanceof DimensionLine) {
      // Add to bounds the text bounds of the dimension line length
      DimensionLine dimensionLine = (DimensionLine)item;
      float dimensionLineLength = dimensionLine.getLength();
      String lengthText = this.preferences.getLengthUnit().getFormat().format(dimensionLineLength);
      TextStyle lengthStyle = dimensionLine.getLengthStyle();
      if (lengthStyle == null) {
        lengthStyle = this.preferences.getDefaultTextStyle(dimensionLine.getClass());
      }
      FontMetrics lengthFontMetrics = getFontMetrics(componentFont, lengthStyle);
      Rectangle2D lengthTextBounds = componentFont.getStringBounds(lengthText);
      // Transform length text bounding rectangle corners to their real location
      double angle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(),
          dimensionLine.getXEnd() - dimensionLine.getXStart());
      AffineTransform transform = AffineTransform.getTranslateInstance(
          dimensionLine.getXStart(), dimensionLine.getYStart());
      transform.rotate(angle);
      transform.translate(0, dimensionLine.getOffset());
			//PJ the ascent in fontMetrics is already -ve so -fontMetrics.ascent
      transform.translate((dimensionLineLength - lengthTextBounds.getWidth()) / 2,
          dimensionLine.getOffset() <= 0
              ? lengthFontMetrics.descent + 1
              : -lengthFontMetrics.ascent - 1);
      GeneralPath lengthTextBoundsPath = new GeneralPath(lengthTextBounds);
      for (PathIterator it = lengthTextBoundsPath.getPathIterator(transform); !it.isDone(); it.next()) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          itemBounds.add(pathPoint [0], pathPoint [1]);
        }
      }
      // Add to bounds the end lines drawn at dimension line start and end
      transform.setToTranslation(dimensionLine.getXStart(), dimensionLine.getYStart());
      transform.rotate(angle);
      transform.translate(0, dimensionLine.getOffset());
      for (PathIterator it = DIMENSION_LINE_END.getPathIterator(transform); !it.isDone(); it.next()) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          itemBounds.add(pathPoint [0], pathPoint [1]);
        }
      }
      transform.translate(dimensionLineLength, 0);
      for (PathIterator it = DIMENSION_LINE_END.getPathIterator(transform); !it.isDone(); it.next()) {
        float [] pathPoint = new float[2];
        if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
          itemBounds.add(pathPoint [0], pathPoint [1]);
        }
      }
    } else if (item instanceof Label) {
      // Add to bounds the displayed text of a label
      Label label = (Label)item;
      addTextBounds(label.getClass(),
          label.getText(), label.getStyle(), label.getX(), label.getY(), label.getAngle(), itemBounds);
    } else if (item instanceof Compass) {
      Compass compass = (Compass)item;
      AffineTransform transform = AffineTransform.getTranslateInstance(compass.getX(), compass.getY());
      transform.scale(compass.getDiameter(), compass.getDiameter());
      transform.rotate(compass.getNorthDirection());
      return COMPASS.createTransformedShape(transform).getBounds2D();
    }
    return itemBounds;
  }

  /**
   * Add <code>text</code> bounds to the given rectangle <code>bounds</code>.
   */
  private void addTextBounds(Class<? extends Selectable> selectableClass,
                             String text, TextStyle style,
                             float x, float y, float angle,
                             Rectangle2D bounds) {
    if (style == null) {
      style = this.preferences.getDefaultTextStyle(selectableClass);
    }
    for (float [] points : getTextBounds(text, style, x, y, angle)) {
      bounds.add(points [0], points [1]);
    }
  }

  /**
   * Returns the coordinates of the bounding rectangle of the <code>text</code> centered at
   * the point (<code>x</code>,<code>y</code>).
   */
  public float [][] getTextBounds(String text, TextStyle style,
                                  float x, float y, float angle) {
		FontMetrics fontMetrics = getFontMetrics(getFont(), style);
		Rectangle2D textBounds = null;
		String [] lines = text.split("\n");
		Graphics2D g = (Graphics2D)getGraphics();
		if (g != null) {
			setRenderingHints(g);
		}
		for (int i = 0; i < lines.length; i++) {
			Rectangle2D lineBounds = getFont(getFont(), style).getStringBounds(lines [i]);
			if (textBounds == null
							|| textBounds.getWidth() < lineBounds.getWidth()) {
				textBounds = lineBounds;
			}
		}
		float textWidth = (float)textBounds.getWidth();
		float shiftX;
		if (style.getAlignment() == TextStyle.Alignment.LEFT) {
			shiftX = 0;
		} else if (style.getAlignment() == TextStyle.Alignment.RIGHT) {
			shiftX = -textWidth;
		} else { // CENTER
			shiftX = -textWidth / 2;
		}
		if (angle == 0) {
			float minY = (float)(y + textBounds.getY());
			float h = ((fontMetrics.bottom - fontMetrics.top) - fontMetrics.leading);
			float maxY = (float)(minY + h);
			minY -= (float)(h * (lines.length - 1));
			return new float [][] {
							{x + shiftX, minY},
							{x + shiftX + textWidth, minY},
							{x + shiftX + textWidth, maxY},
							{x + shiftX, maxY}};
		} else {
			textBounds.add(textBounds.getX(), textBounds.getY() - ((fontMetrics.bottom - fontMetrics.top) - fontMetrics.leading) * (lines.length - 1));
			// Transform text bounding rectangle corners to their real location
			AffineTransform transform = new AffineTransform();
			transform.translate(x, y);
			transform.rotate(angle);
			transform.translate(shiftX, 0);
			GeneralPath textBoundsPath = new GeneralPath(textBounds);
			List<float []> textPoints = new ArrayList<float[]>(4);
			for (PathIterator it = textBoundsPath.getPathIterator(transform); !it.isDone(); it.next()) {
				float [] pathPoint = new float[2];
				if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
					textPoints.add(pathPoint);
				}
			}
			return textPoints.toArray(new float [textPoints.size()][]);
		}
  }

  /**
   * Returns the AWT font matching a given text style.
   */
  protected Font getFont(Font defaultFont, TextStyle textStyle) {
    if (this.fonts == null) {
      this.fonts = new WeakHashMap<TextStyle, Font>();
    }
    Font font = this.fonts.get(textStyle);
    if (font == null) {
      /*int fontStyle = Font.PLAIN;
      if (textStyle.isBold()) {
        fontStyle = Font.BOLD;
      }
      if (textStyle.isItalic()) {
        fontStyle |= Font.ITALIC;
      }
      if (defaultFont == null
          || this.preferences.getDefaultFontName() != null
          || textStyle.getFontName() != null) {
        String fontName = textStyle.getFontName();
        if (fontName == null) {
          fontName = this.preferences.getDefaultFontName();
        }
        defaultFont = new Font(fontName, fontStyle, 1);
      }
      //font = defaultFont.deriveFont(fontStyle, textStyle.getFontSize());*/
      font = new VMFont(Typeface.DEFAULT, (int)textStyle.getFontSize());
      this.fonts.put(textStyle, font);
    }
    return font;
  }

  /**
   * Returns the font metrics matching a given text style.
   */
  protected FontMetrics getFontMetrics(Font defaultFont, TextStyle textStyle) {
    if (this.fontsMetrics == null) {
      this.fontsMetrics = new WeakHashMap<TextStyle, FontMetrics>();
    }
    FontMetrics fontMetrics = this.fontsMetrics.get(textStyle);
    if (fontMetrics == null) {
      fontMetrics = getFontMetrics(getFont(defaultFont, textStyle));
      this.fontsMetrics.put(textStyle, fontMetrics);
    }
    return fontMetrics;
  }

  /**
   * Sets whether plan's background should be painted or not.
   * Background may include grid and an image.
   */
  public void setBackgroundPainted(boolean backgroundPainted) {
    if (this.backgroundPainted != backgroundPainted) {
      this.backgroundPainted = backgroundPainted;
      repaint();
    }
  }

  /**
   * Returns <code>true</code> if plan's background should be painted.
   */
  public boolean isBackgroundPainted() {
    return this.backgroundPainted;
  }

  /**
   * Sets whether the outline of home selected items should be painted or not.
   */
  public void setSelectedItemsOutlinePainted(boolean selectedItemsOutlinePainted) {
    if (this.selectedItemsOutlinePainted != selectedItemsOutlinePainted) {
      this.selectedItemsOutlinePainted = selectedItemsOutlinePainted;
      repaint();
    }
  }

  /**
   * Returns <code>true</code> if the outline of home selected items should be painted.
   */
  public boolean isSelectedItemsOutlinePainted() {
    return this.selectedItemsOutlinePainted;
  }

	/**
	 * Paints this component.
	 */
	@Override
	public void paintComponent(Graphics g) {
		Graphics2D g2D = (Graphics2D)g.create();
		if (this.backgroundPainted) {
			paintBackground(g2D, getBackgroundColor(PaintMode.PAINT));
		}
		Insets insets = getInsets();
		// Clip component to avoid drawing in empty borders
		g2D.clipRect(insets.left, insets.top,
				getWidth() - insets.left - insets.right,
				getHeight() - insets.top - insets.bottom);
		// Change component coordinates system to plan system
		Rectangle2D planBounds = getPlanBounds();
		float paintScale = getScale();
		g2D.translate(insets.left + ((MARGIN - planBounds.getMinX()) * paintScale) - getXScroll(),
				insets.top + ((MARGIN - planBounds.getMinY()) * paintScale) - getYScroll());
		g2D.scale(paintScale, paintScale);
		setRenderingHints(g2D);
		try {
			paintContent(g2D, paintScale, PaintMode.PAINT);
		} catch (InterruptedIOException ex) {
			// Ignore exception because it may happen only in EXPORT paint mode
		}
		g2D.dispose();
	}

  /**
   * Returns the print preferred scale of the plan drawn in this component
   * to make it fill <code>pageFormat</code> imageable size.
   */
  public float getPrintPreferredScale(Graphics g, PageFormat pageFormat) {
    return getPrintPreferredScale(LengthUnit.inchToCentimeter((float)pageFormat.getImageableWidth() / 72),
        LengthUnit.inchToCentimeter((float)pageFormat.getImageableHeight() / 72));
  }

	/**
	 * Returns the preferred scale to ensure it can be fully printed on the given print zone.
	 */
	public float getPrintPreferredScale(float preferredWidth, float preferredHeight) {
		List<Selectable> printedItems = getPaintedItems();
		Graphics2D g = (Graphics2D)getGraphics();
		if (g != null) {
			setRenderingHints(g);
		}
		Rectangle2D printedItemBounds = getItemsBounds(g, printedItems);
		if (printedItemBounds != null) {
			float extraMargin = getStrokeWidthExtraMargin(printedItems, PaintMode.PRINT);
			// Compute the largest integer scale possible
			int scaleInverse = (int)Math.ceil(Math.max(
							(printedItemBounds.getWidth() + 2 * extraMargin) / preferredWidth,
							(printedItemBounds.getHeight() + 2 * extraMargin) / preferredHeight));
			return 1f / scaleInverse;
		} else {
			return 0;
		}
	}

  /**
   * Returns the margin that should be added around home items bounds to ensure their
   * line stroke width is always fully visible.
   */
  private float getStrokeWidthExtraMargin(List<Selectable> items, PaintMode paintMode) {
    float extraMargin = BORDER_STROKE_WIDTH;
    if (Home.getFurnitureSubList(items).size() > 0) {
      extraMargin = Math.max(extraMargin, getStrokeWidth(HomePieceOfFurniture.class, paintMode));
    }
    if (Home.getWallsSubList(items).size() > 0) {
      extraMargin = Math.max(extraMargin, getStrokeWidth(Wall.class, paintMode));
    }
    if (Home.getRoomsSubList(items).size() > 0) {
      extraMargin = Math.max(extraMargin, getStrokeWidth(Room.class, paintMode));
    }
    List<Polyline> polylines = Home.getPolylinesSubList(items);
    if (polylines.size() > 0) {
      for (Polyline polyline : polylines) {
        extraMargin = Math.max(extraMargin, polyline.getStartArrowStyle() != null ||  polyline.getEndArrowStyle() != null
            ? 1.5f * polyline.getThickness()
            : polyline.getThickness());
      }
    }
    if (Home.getDimensionLinesSubList(items).size() > 0) {
      extraMargin = Math.max(extraMargin, getStrokeWidth(DimensionLine.class, paintMode));
    }
    return extraMargin / 2;
  }

  /**
   * Returns the stroke width used to paint an item of the given class.
   */
  private float getStrokeWidth(Class<? extends Selectable> itemClass, PaintMode paintMode) {
    float strokeWidth;
    if (Wall.class.isAssignableFrom(itemClass)
        || Room.class.isAssignableFrom(itemClass)) {
      strokeWidth = WALL_STROKE_WIDTH;
    } else {
      strokeWidth = BORDER_STROKE_WIDTH;
    }
    if (paintMode == PaintMode.PRINT) {
      strokeWidth *= 0.5;
    } else {
      strokeWidth *= this.resolutionScale;
    }
    return strokeWidth;
  }

  /**
   * Prints this component plan at the scale given in the home print attributes or at a scale
   * that makes it fill <code>pageFormat</code> imageable size if this attribute is <code>null</code>.
   */
  public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
    List<Selectable> printedItems = getPaintedItems();
    Rectangle2D printedItemBounds = getItemsBounds(g, printedItems);
    if (printedItemBounds != null) {
      double imageableX = pageFormat.getImageableX();
      double imageableY = pageFormat.getImageableY();
      double imageableWidth = pageFormat.getImageableWidth();
      double imageableHeight = pageFormat.getImageableHeight();
      float printScale;
      float rowIndex;
      float columnIndex;
      int pagesPerRow;
      int pagesPerColumn;
      if (this.home.getPrint() == null || this.home.getPrint().getPlanScale() == null) {
        // Compute a scale that ensures the plan will fill the component if plan scale is null
        printScale = getPrintPreferredScale(g, pageFormat) * LengthUnit.centimeterToInch(72);
        if (pageIndex > 0) {
          return NO_SUCH_PAGE;
        }
        pagesPerRow = 1;
        pagesPerColumn = 1;
        rowIndex   = 0;
        columnIndex = 0;
      } else {
        // Apply print scale to paper size expressed in 1/72nds of an inch
        printScale = this.home.getPrint().getPlanScale().floatValue() * LengthUnit.centimeterToInch(72);
        pagesPerRow = (int)(printedItemBounds.getWidth() * printScale / imageableWidth);
        if (printedItemBounds.getWidth() * printScale != imageableWidth) {
          pagesPerRow++;
        }
        pagesPerColumn = (int)(printedItemBounds.getHeight() * printScale / imageableHeight);
        if (printedItemBounds.getHeight() * printScale != imageableHeight) {
          pagesPerColumn++;
        }
        if (pageIndex >= pagesPerRow * pagesPerColumn) {
          return NO_SUCH_PAGE;
        }
        rowIndex = pageIndex / pagesPerRow;
        columnIndex = pageIndex - rowIndex * pagesPerRow;
      }

      Graphics2D g2D = (Graphics2D)g.create();
      g2D.clip(new Rectangle2D.Double(imageableX, imageableY, imageableWidth, imageableHeight));
      // Change coordinates system to paper imageable origin
      g2D.translate(imageableX - columnIndex * imageableWidth, imageableY - rowIndex * imageableHeight);
      g2D.scale(printScale, printScale);
      float extraMargin = getStrokeWidthExtraMargin(printedItems, PaintMode.PRINT);
      g2D.translate(-printedItemBounds.getMinX() + extraMargin,
          -printedItemBounds.getMinY() + extraMargin);
      // Center plan in component if possible
      g2D.translate(Math.max(0,
              (imageableWidth * pagesPerRow / printScale - printedItemBounds.getWidth() - 2 * extraMargin) / 2),
          Math.max(0,
              (imageableHeight * pagesPerColumn / printScale - printedItemBounds.getHeight() - 2 * extraMargin) / 2));
      setRenderingHints(g2D);
      try {
        // Print component contents
        paintContent(g2D, printScale, PaintMode.PRINT);
      } catch (InterruptedIOException ex) {
        // Ignore exception because it may happen only in EXPORT paint mode
      }
      g2D.dispose();
      return PAGE_EXISTS;
    } else {
      return NO_SUCH_PAGE;
    }
  }

  /**
   * Returns an image of selected items in plan for transfer purpose.
   */
  public Object createTransferData(DataType dataType) {
    if (dataType == DataType.PLAN_IMAGE) {
      return getClipboardImage();
    } else {
      return null;
    }
  }

  /**
   * Returns an image of the selected items displayed by this component
   * (camera excepted) with no outline at scale 1/1 (1 pixel = 1cm)
   * or at a smaller scale if image is larger than 100m x 100m
   * or if free memory is missing.
   */
  public BufferedImage getClipboardImage() {
    // Create an image that contains only selected items
    Rectangle2D selectionBounds = getSelectionBounds(false);
    if (selectionBounds == null) {
      return null;
    } else {
      // Use a scale of 1 except if image is very large or free memory is missing
      float clipboardScale = 1f;
      //PJPJPJ no ignore clipboards
      float extraMargin = getStrokeWidthExtraMargin(this.home.getSelectedItems(), PaintMode.CLIPBOARD);
      BufferedImage image = new BufferedImage((int)Math.ceil(selectionBounds.getWidth() * clipboardScale + 2 * extraMargin),
              (int)Math.ceil(selectionBounds.getHeight() * clipboardScale + 2 * extraMargin), BufferedImage.TYPE_INT_ARGB);
      Graphics2D g2D = (Graphics2D)image.getGraphics();
      // Paint background in white
      g2D.setColor(Color.WHITE);
      g2D.fillRect(0, 0, image.getWidth(), image.getHeight());
      // Change component coordinates system to plan system
      g2D.scale(clipboardScale, clipboardScale);
      g2D.translate(-selectionBounds.getMinX() + extraMargin,
          -selectionBounds.getMinY() + extraMargin);
      setRenderingHints(g2D);
      try {
        // Paint component contents
        paintContent(g2D, clipboardScale, PaintMode.CLIPBOARD);
      } catch (InterruptedIOException ex) {
        // Ignore exception because it may happen only in EXPORT paint mode
        return null;
      }
      g2D.dispose();
      return image;
    }
  }

  /**
   * Returns <code>true</code> if the given format is SVG.
   */
  public boolean isFormatTypeSupported(FormatType formatType) {
    return formatType == FormatType.SVG;
  }

  /**
   * Writes this plan in the given output stream at SVG (Scalable Vector Graphics) format if this is the requested format.
   */
  public void exportData(OutputStream out, FormatType formatType, Properties settings) throws IOException {
    if  (formatType == FormatType.SVG) {
      exportToSVG(out);
    } else {
      throw new UnsupportedOperationException("Unsupported format " + formatType);
    }
  }

  /**
   * Writes this plan in the given output stream at SVG (Scalable Vector Graphics) format.
   */
  public void exportToSVG(OutputStream out) throws IOException {
  //  SVGSupport.exportToSVG(out, this);
  }

  /**
   * Separated static class to be able to exclude FreeHEP library from classpath
   * in case the application doesn't use export to SVG format.
   */
 /* private static class SVGSupport {
    public static void exportToSVG(OutputStream out,
                                   PlanComponent planComponent) throws IOException {
      List<Selectable> homeItems = planComponent.getPaintedItems();
      Rectangle2D svgItemBounds = planComponent.getItemsBounds(null, homeItems);
      if (svgItemBounds == null) {
        svgItemBounds = new Rectangle2D.Float();
      }

      float svgScale = 1f;
      float extraMargin = planComponent.getStrokeWidthExtraMargin(homeItems, PaintMode.EXPORT);
      Dimension imageSize = new Dimension((int)Math.ceil(svgItemBounds.getWidth() * svgScale + 2 * extraMargin),
          (int)Math.ceil(svgItemBounds.getHeight() * svgScale + 2 * extraMargin));

      SVGGraphics2D exportG2D = new SVGGraphics2D(out, imageSize) {
          @Override
          public void writeHeader() throws IOException {
            // Use English locale to avoid wrong encoding when localized dates contain accentuated letters
            Locale defaultLocale = Locale.getDefault();
            Locale.setDefault(Locale.ENGLISH);
            super.writeHeader();
            Locale.setDefault(defaultLocale);
          }
        };
      UserProperties properties = new UserProperties();
      properties.setProperty(SVGGraphics2D.STYLABLE, true);
      properties.setProperty(SVGGraphics2D.WRITE_IMAGES_AS, ImageConstants.PNG);
      properties.setProperty(SVGGraphics2D.TITLE,
          planComponent.home.getName() != null
              ? planComponent.home.getName()
              : "" );
      properties.setProperty(SVGGraphics2D.FOR, System.getProperty("user.name", ""));
      exportG2D.setProperties(properties);
      exportG2D.startExport();
      exportG2D.translate(-svgItemBounds.getMinX() + extraMargin,
          -svgItemBounds.getMinY() + extraMargin);

      planComponent.checkCurrentThreadIsntInterrupted(PaintMode.EXPORT);
      planComponent.paintContent(exportG2D, svgScale, PaintMode.EXPORT);
      exportG2D.endExport();
    }
  }*/

  /**
   * Throws an <code>InterruptedIOException</code> exception if current thread
   * is interrupted and <code>paintMode</code> is equal to <code>PaintMode.EXPORT</code>.
   */
  private void checkCurrentThreadIsntInterrupted(PaintMode paintMode) throws InterruptedIOException {
    if (paintMode == PaintMode.EXPORT
        && Thread.interrupted()) {
      throw new InterruptedIOException("Current thread interrupted");
    }
  }

  /**
   * Sets rendering hints used to paint plan.
   */
  private void setRenderingHints(Graphics2D g2D) {
    g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g2D.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
  }

  /**
   * Fills the background.
   */
  private void paintBackground(Graphics2D g2D, Color backgroundColor) {
    if (isOpaque()) {
      g2D.setColor(backgroundColor);
      g2D.fillRect(0, 0, getWidth(), getHeight());
    }
  }

  /**
   * Paints background image and returns <code>true</code> if an image is painted.
   */
  private boolean paintBackgroundImage(Graphics2D g2D, PaintMode paintMode) {
    Level selectedLevel = this.home.getSelectedLevel();
    Level backgroundImageLevel = null;
    if (selectedLevel != null) {
      // Search the first level at same elevation with a background image
      List<Level> levels = this.home.getLevels();
      for (int i = levels.size() - 1; i >= 0; i--) {
        Level level = levels.get(i);
        if (level.getElevation() == selectedLevel.getElevation()
            && level.getElevationIndex() <= selectedLevel.getElevationIndex()
            && level.isViewable()
            && level.getBackgroundImage() != null
            && level.getBackgroundImage().isVisible()) {
          backgroundImageLevel = level;
          break;
        }
      }
    }
    final BackgroundImage backgroundImage = backgroundImageLevel == null
        ? this.home.getBackgroundImage()
        : backgroundImageLevel.getBackgroundImage();
    if (backgroundImage != null && backgroundImage.isVisible()) {
      // Under Mac OS X, prepare background image with alpha because Java 5/6 doesn't always
      // paint images correctly with alpha, and Java 7 blocks for some images
      final boolean prepareBackgroundImageWithAlphaInMemory = OperatingSystem.isMacOSX();
      if (this.backgroundImageCache == null && paintMode == PaintMode.PAINT) {
        // Load background image in an executor
        if (backgroundImageLoader == null) {
          backgroundImageLoader = Executors.newSingleThreadExecutor();
        }
        backgroundImageLoader.execute(new Runnable() {
            public void run() {
              if (backgroundImageCache == null) {
                backgroundImageCache = readBackgroundImage(backgroundImage.getImage(), prepareBackgroundImageWithAlphaInMemory);
                revalidate();
              }
            }
          });
      } else {
        // Paint image at specified scale with 0.7 alpha
        AffineTransform previousTransform = g2D.getTransform();
        g2D.translate(-backgroundImage.getXOrigin(), -backgroundImage.getYOrigin());
        float backgroundImageScale = backgroundImage.getScale();
        g2D.scale(backgroundImageScale, backgroundImageScale);
        Composite oldComposite = null;
        if (!prepareBackgroundImageWithAlphaInMemory) {
					oldComposite = setTransparency(g2D, 0.7f);
        }
        g2D.drawImage(this.backgroundImageCache != null
            ? this.backgroundImageCache
            : readBackgroundImage(backgroundImage.getImage(), prepareBackgroundImageWithAlphaInMemory), 0, 0, null);
        if (!prepareBackgroundImageWithAlphaInMemory) {
					g2D.setComposite(oldComposite);
        }
        g2D.setTransform(previousTransform);
      }
      return true;
    }
    return false;
  }

  /**
   * Returns the foreground color used to draw content.
   */
  protected Color getForegroundColor(PaintMode mode) {
    if (mode == PaintMode.PAINT) {
      return getForeground();
    } else {
      return Color.BLACK;
    }
  }

  /**
   * Returns the background color used to draw content.
   */
  protected Color getBackgroundColor(PaintMode mode) {
    if (mode == PaintMode.PAINT) {
      return getBackground();
    } else {
      return Color.WHITE;
    }
  }

  /**
   * Returns the image contained in <code>imageContent</code> or an empty image if reading failed.
   */
  private BufferedImage readBackgroundImage(Content imageContent, boolean prepareBackgroundImageWithAlpha) {
    InputStream contentStream = null;
    try {
      try {
        contentStream = imageContent.openStream();
        BufferedImage image = ImageIO.read(contentStream);
        if (prepareBackgroundImageWithAlpha) {
          BufferedImage backgroundImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
          Graphics2D g2D = (Graphics2D)backgroundImage.getGraphics();
		  g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7f));
          g2D.drawRenderedImage(image, null);
          g2D.dispose();
          return backgroundImage;
        } else {
          return image;
        }
      } finally {
        if (contentStream != null) {
          contentStream.close();
        }
      }
    } catch (IOException ex) {
      return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
      // Ignore exceptions, the user may know its background image is incorrect
      // if he tries to modify the background image
    }
  }

  /**
   * Paints walls and rooms of lower levels or upper levels to help the user draw in the selected level.
   */
  private void paintOtherLevels(Graphics2D g2D, float planScale,
                                Color backgroundColor, Color foregroundColor) {
    List<Level> levels = this.home.getLevels();
    Level selectedLevel = this.home.getSelectedLevel();
    if (levels.size() > 1
        && selectedLevel != null) {
      boolean level0 = levels.get(0).getElevation() == selectedLevel.getElevation();
      List<Level> otherLevels = null;
      if (this.otherLevelsRoomsCache == null
          || this.otherLevelsWallsCache == null) {
        int selectedLevelIndex = levels.indexOf(selectedLevel);
        otherLevels = new ArrayList<Level>();
        if (level0) {
          // Search levels at the same elevation above level0
          int nextElevationLevelIndex = selectedLevelIndex;
          while (++nextElevationLevelIndex < levels.size()
              && levels.get(nextElevationLevelIndex).getElevation() == selectedLevel.getElevation()) {
          }
          if (nextElevationLevelIndex < levels.size()) {
            Level nextLevel = levels.get(nextElevationLevelIndex);
            float nextElevation = nextLevel.getElevation();
            do {
              if (nextLevel.isViewable()) {
                otherLevels.add(nextLevel);
              }
            } while (++nextElevationLevelIndex < levels.size()
                && (nextLevel = levels.get(nextElevationLevelIndex)).getElevation() == nextElevation);
          }
        } else {
          // Search levels at the same elevation below level0
          int previousElevationLevelIndex = selectedLevelIndex;
          while (--previousElevationLevelIndex >= 0
              && levels.get(previousElevationLevelIndex).getElevation() == selectedLevel.getElevation()) {
          }
          if (previousElevationLevelIndex >= 0) {
            Level previousLevel = levels.get(previousElevationLevelIndex);
            float previousElevation = previousLevel.getElevation();
            do {
              if (previousLevel.isViewable()) {
                otherLevels.add(previousLevel);
              }
            } while (--previousElevationLevelIndex >= 0
                && (previousLevel = levels.get(previousElevationLevelIndex)).getElevation() == previousElevation);
          }
        }

        if (this.otherLevelsRoomsCache == null) {
          if (!otherLevels.isEmpty()) {
            // Search viewable floors in levels above level0 or ceilings in levels below level0
            List<Room> otherLevelsRooms = new ArrayList<Room>();
            for (Room room : this.home.getRooms()) {
              for (Level otherLevel : otherLevels) {
                if (room.getLevel() == otherLevel
                    && (level0 && room.isFloorVisible()
                        || !level0 && room.isCeilingVisible())) {
                  otherLevelsRooms.add(room);
                }
              }
            }
            if (otherLevelsRooms.size() > 0) {
              this.otherLevelsRoomAreaCache = getItemsArea(otherLevelsRooms);
              this.otherLevelsRoomsCache = otherLevelsRooms;
            }
          }
          if (this.otherLevelsRoomsCache == null) {
            this.otherLevelsRoomsCache = Collections.emptyList();
          }
        }

        if (this.otherLevelsWallsCache == null) {
          if (!otherLevels.isEmpty()) {
            // Search viewable walls in other levels
            List<Wall> otherLevelswalls = new ArrayList<Wall>();
            for (Wall wall : this.home.getWalls()) {
              if (!isViewableAtSelectedLevel(wall)) {
                for (Level otherLevel : otherLevels) {
                  if (wall.getLevel() == otherLevel) {
                    otherLevelswalls.add(wall);
                  }
                }
              }
            }
            if (otherLevelswalls.size() > 0) {
              this.otherLevelsWallAreaCache = getItemsArea(otherLevelswalls);
              this.otherLevelsWallsCache = otherLevelswalls;
            }
          }
        }
        if (this.otherLevelsWallsCache == null) {
          this.otherLevelsWallsCache = Collections.emptyList();
        }
      }

      if (!this.otherLevelsRoomsCache.isEmpty()) {
		Composite oldComposite = setTransparency(g2D,
		  this.preferences.isGridVisible() ? 0.2f : 0.1f);
        g2D.setPaint(Color.GRAY);
        g2D.fill(this.otherLevelsRoomAreaCache);
		  g2D.setComposite(oldComposite);
      }

      if (!this.otherLevelsWallsCache.isEmpty()) {
		  Composite oldComposite = setTransparency(g2D,
		       this.preferences.isGridVisible() ? 0.2f : 0.1f);
        fillAndDrawWallsArea(g2D, this.otherLevelsWallAreaCache, planScale,
            getWallPaint(planScale, backgroundColor, foregroundColor, this.preferences.getNewWallPattern()),
            foregroundColor, PaintMode.PAINT);
		  g2D.setComposite(oldComposite);
      }
    }
  }

  /**
   * Sets the transparency composite to the given percentage and returns the old composite.
   */
  private Composite setTransparency(Graphics2D g2D, float alpha) {
    Composite oldComposite = g2D.getComposite();
    if (oldComposite instanceof AlphaComposite) {
      g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
        ((AlphaComposite)oldComposite).getAlpha() * alpha));
    } else {
      g2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
    }
    return oldComposite;
  }

  /**
   * Paints background grid lines.
   */
  private void paintGrid(Graphics2D g2D, float gridScale) {
    float gridSize = getGridSize(gridScale);
    float mainGridSize = getMainGridSize(gridScale);

    float xMin;
    float yMin;
    float xMax;
    float yMax;
    Rectangle2D planBounds = getPlanBounds();
   /* if (getParent() instanceof JViewport) {
      Rectangle viewRectangle = ((JViewport)this).getViewRect();
      xMin = convertXPixelToModel(viewRectangle.x - 1);
      yMin = convertYPixelToModel(viewRectangle.y - 1);
      xMax = convertXPixelToModel(viewRectangle.x + viewRectangle.width);
      yMax = convertYPixelToModel(viewRectangle.y + viewRectangle.height);
    } else {*/
	  xMin = (float)planBounds.getMinX() - MARGIN + (getXScroll() / getScale());
	  yMin = (float)planBounds.getMinY() - MARGIN + (getYScroll() / getScale());
	  xMax = convertXPixelToModel(getWidth());
	  yMax = convertYPixelToModel(getHeight());
   // }
/* removed for speed   boolean useGridImage = false;
    try {
      useGridImage = OperatingSystem.isMacOSX()
          && System.getProperty("apple.awt.graphics.UseQuartz", "false").equals("false");
    } catch (AccessControlException ex) {
      // Unsigned applet
    }
    if (useGridImage) {
      // Draw grid with an image texture under Mac OS X, because default 2D rendering engine
      // is too slow and can't be replaced by Quartz engine in applet environment
      int imageWidth = Math.round(mainGridSize * gridScale);
      BufferedImage gridImage = new BufferedImage(imageWidth, imageWidth, BufferedImage.TYPE_INT_ARGB);
      Graphics2D imageGraphics = (Graphics2D)gridImage.getGraphics();
      setRenderingHints(imageGraphics);
      imageGraphics.scale(gridScale, gridScale);

      paintGridLines(imageGraphics, gridScale, 0, mainGridSize, 0, mainGridSize, gridSize, mainGridSize);
      imageGraphics.dispose();

      g2D.setPaint(new TexturePaint(gridImage, new Rectangle2D.Float(0, 0, mainGridSize, mainGridSize)));

      g2D.fill(new Rectangle2D.Float(xMin, yMin, xMax - xMin, yMax - yMin));
    } else {*/
      paintGridLines(g2D, gridScale, xMin, xMax, yMin, yMax, gridSize, mainGridSize);
    //}
  }

  /**
   * Paints background grid lines from <code>xMin</code> to <code>xMax</code>
   * and <code>yMin</code> to <code>yMax</code>.
   */
  private void paintGridLines(Graphics2D g2D, float gridScale,
                              float xMin, float xMax, float yMin, float yMax,
                              float gridSize, float mainGridSize) {
    g2D.setColor(new Color(0.3f, 0.3f, 0.3f));//UIManager.getColor("controlShadow"));
    g2D.setStroke(new BasicStroke(0.5f / gridScale));
    // Draw vertical lines
    for (double x = (int)(xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
      g2D.draw(new Line2D.Double(x, yMin, x, yMax));
    }
    // Draw horizontal lines
    for (double y = (int)(yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
      g2D.draw(new Line2D.Double(xMin, y, xMax, y));
    }

    if (mainGridSize != gridSize) {
      g2D.setStroke(new BasicStroke(1.5f / gridScale,
          BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
      // Draw main vertical lines
      for (double x = (int)(xMin / mainGridSize) * mainGridSize; x < xMax; x += mainGridSize) {
        g2D.draw(new Line2D.Double(x, yMin, x, yMax));
      }
      // Draw positive main horizontal lines
      for (double y = (int)(yMin / mainGridSize) * mainGridSize; y < yMax; y += mainGridSize) {
        g2D.draw(new Line2D.Double(xMin, y, xMax, y));
      }
    }
  }

  /**
   * Returns the space between main lines grid.
   */
  private float getMainGridSize(float gridScale) {
    float [] mainGridSizes;
    LengthUnit lengthUnit = this.preferences.getLengthUnit();
    if (lengthUnit == LengthUnit.INCH
        || lengthUnit == LengthUnit.INCH_DECIMALS) {
      // Use a grid in inch and foot with a minimum grid increment of 1 inch
      float oneFoot = 2.54f * 12;
      mainGridSizes = new float [] {oneFoot, 3 * oneFoot, 6 * oneFoot,
                                    12 * oneFoot, 24 * oneFoot, 48 * oneFoot, 96 * oneFoot, 192 * oneFoot, 384 * oneFoot};
    } else {
      // Use a grid in cm and meters with a minimum grid increment of 1 cm
      mainGridSizes = new float [] {100, 200, 500, 1000, 2000, 5000, 10000};
    }
    // Compute grid size to get a grid where the space between each line is less than 50 pixels
    float mainGridSize = mainGridSizes [0];
    for (int i = 1; i < mainGridSizes.length && mainGridSize * gridScale < 50; i++) {
      mainGridSize = mainGridSizes [i];
    }
    return mainGridSize;
  }

  /**
   * Returns the space between lines grid.
   */
  private float getGridSize(float gridScale) {
    float [] gridSizes;
    LengthUnit lengthUnit = this.preferences.getLengthUnit();
    if (lengthUnit == LengthUnit.INCH
        || lengthUnit == LengthUnit.INCH_DECIMALS) {
      // Use a grid in inch and foot with a minimum grid increment of 1 inch
      float oneFoot = 2.54f * 12;
      gridSizes = new float [] {2.54f, 5.08f, 7.62f, 15.24f, oneFoot, 3 * oneFoot, 6 * oneFoot,
                                12 * oneFoot, 24 * oneFoot, 48 * oneFoot, 96 * oneFoot, 192 * oneFoot, 384 * oneFoot};
    } else {
      // Use a grid in cm and meters with a minimum grid increment of 1 cm
      gridSizes = new float [] {1, 2, 5, 10, 20, 50, 100, 200, 500, 1000, 2000, 5000, 10000};
    }
    // Compute grid size to get a grid where the space between each line is less than 10 pixels
    float gridSize = gridSizes [0];
    for (int i = 1; i < gridSizes.length && gridSize * gridScale < 10; i++) {
      gridSize = gridSizes [i];
    }
    return gridSize;
  }

  /**
   * Paints plan items.
   * @throws InterruptedIOException if painting was interrupted (may happen only
   *           if <code>paintMode</code> is equal to <code>PaintMode.EXPORT</code>).
   */
  private void paintContent(Graphics2D g2D, float planScale, PaintMode paintMode) throws InterruptedIOException {
    Color backgroundColor = getBackgroundColor(paintMode);
    Color foregroundColor = getForegroundColor(paintMode);
    if (this.backgroundPainted) {
      paintBackgroundImage(g2D, paintMode);
      if (paintMode == PaintMode.PAINT) {
        paintOtherLevels(g2D, planScale, backgroundColor, foregroundColor);
        if (this.preferences.isGridVisible()) {
          paintGrid(g2D, planScale);
        }
      }
    }

    paintHomeItems(g2D, planScale, backgroundColor, foregroundColor, paintMode);

    if (paintMode == PaintMode.PAINT) {
      List<Selectable> selectedItems = this.home.getSelectedItems();

      Color selectionColor = getSelectionColor();
      Color furnitureOutlineColor = getFurnitureOutlineColor();
      Paint selectionOutlinePaint = new Color(selectionColor.getRed(), selectionColor.getGreen(),
          selectionColor.getBlue(), 128);
      Stroke selectionOutlineStroke = new BasicStroke(6 * fatFingerScaleUp / planScale * this.resolutionScale,
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      Stroke dimensionLinesSelectionOutlineStroke = new BasicStroke(4 * fatFingerScaleUp / planScale * this.resolutionScale,
          BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
      Stroke locationFeedbackStroke = new BasicStroke(
          fatFingerScaleUp / planScale * this.resolutionScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0,
          new float [] {20 * fatFingerScaleUp / planScale, 5 * fatFingerScaleUp / planScale, 5 * fatFingerScaleUp / planScale, 5 * fatFingerScaleUp / planScale}, 4 * fatFingerScaleUp / planScale);

      paintCamera(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor,
          planScale, backgroundColor, foregroundColor);

      // Paint alignment feedback depending on aligned object class
      if (this.alignedObjectClass != null) {
        if (Wall.class.isAssignableFrom(this.alignedObjectClass)) {
          paintWallAlignmentFeedback(g2D, (Wall)this.alignedObjectFeedback, this.locationFeeback, this.showPointFeedback,
              selectionColor, locationFeedbackStroke, planScale,
              selectionOutlinePaint, selectionOutlineStroke);
        } else if (Room.class.isAssignableFrom(this.alignedObjectClass)) {
          paintRoomAlignmentFeedback(g2D, (Room)this.alignedObjectFeedback, this.locationFeeback, this.showPointFeedback,
              selectionColor, locationFeedbackStroke, planScale,
              selectionOutlinePaint, selectionOutlineStroke);
        } else if (Polyline.class.isAssignableFrom(this.alignedObjectClass)) {
          if (this.showPointFeedback) {
            paintPointFeedback(g2D, this.locationFeeback, selectionColor, planScale, selectionOutlinePaint, selectionOutlineStroke);
          }
        } else if (DimensionLine.class.isAssignableFrom(this.alignedObjectClass)) {
          paintDimensionLineAlignmentFeedback(g2D, (DimensionLine)this.alignedObjectFeedback, this.locationFeeback, this.showPointFeedback,
              selectionColor, locationFeedbackStroke, planScale,
              selectionOutlinePaint, selectionOutlineStroke);
        }
      }
      if (this.centerAngleFeedback != null) {
       paintAngleFeedback(g2D, this.centerAngleFeedback, this.point1AngleFeedback, this.point2AngleFeedback,
           planScale, selectionColor);
      }
      if (this.dimensionLinesFeedback != null) {
        List<Selectable> emptySelection = Collections.emptyList();
        paintDimensionLines(g2D, this.dimensionLinesFeedback, emptySelection,
            null, null, null, locationFeedbackStroke, planScale,
            backgroundColor, selectionColor, paintMode, true);
      }

      if (this.draggedItemsFeedback != null) {
        paintDimensionLines(g2D, Home.getDimensionLinesSubList(this.draggedItemsFeedback), this.draggedItemsFeedback,
            selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, null,
            locationFeedbackStroke, planScale, backgroundColor, foregroundColor, paintMode, false);
        paintLabels(g2D, Home.getLabelsSubList(this.draggedItemsFeedback), this.draggedItemsFeedback,
            selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, null,
            planScale, foregroundColor, paintMode);
        paintRoomsOutline(g2D, this.draggedItemsFeedback, selectionOutlinePaint, selectionOutlineStroke, null,
            planScale, foregroundColor);
        paintWallsOutline(g2D, this.draggedItemsFeedback, selectionOutlinePaint, selectionOutlineStroke, null,
            planScale, foregroundColor);
        paintFurniture(g2D, Home.getFurnitureSubList(this.draggedItemsFeedback), selectedItems, planScale, null,
            foregroundColor, furnitureOutlineColor, paintMode, false);
        paintFurnitureOutline(g2D, this.draggedItemsFeedback, selectionOutlinePaint, selectionOutlineStroke, null,
            planScale, foregroundColor);
      }

      paintRectangleFeedback(g2D, selectionColor, planScale);
    }
  }

  /**
   * Paints home items at the given scale, and with background and foreground colors.
   * Outline around selected items will be painted only under <code>PAINT</code> mode.
   */
  protected void paintHomeItems(Graphics g, float planScale,
                                Color backgroundColor, Color foregroundColor, PaintMode paintMode) throws InterruptedIOException {
    Graphics2D g2D = (Graphics2D)g;
    List<Selectable> selectedItems = this.home.getSelectedItems();
    if (this.sortedLevelFurniture == null) {
      // Sort home furniture in elevation order
      this.sortedLevelFurniture = new ArrayList<HomePieceOfFurniture>();
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (isViewableAtSelectedLevel(piece)) {
          this.sortedLevelFurniture.add(piece);
        }
      }
      Collections.sort(this.sortedLevelFurniture,
          new Comparator<HomePieceOfFurniture>() {
            public int compare(HomePieceOfFurniture piece1, HomePieceOfFurniture piece2) {
              return Float.compare(piece1.getGroundElevation(), piece2.getGroundElevation());
            }
          });
    }

    Color selectionColor = getSelectionColor();
    Paint selectionOutlinePaint = new Color(selectionColor.getRed(), selectionColor.getGreen(),
        selectionColor.getBlue(), 128);
    Stroke selectionOutlineStroke = new BasicStroke(6 / planScale * this.resolutionScale,
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    Stroke dimensionLinesSelectionOutlineStroke = new BasicStroke(4 / planScale * this.resolutionScale,
        BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
    Stroke locationFeedbackStroke = new BasicStroke(
        1 * fatFingerScaleUp / planScale * this.resolutionScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_BEVEL, 0,
        new float [] {20 * fatFingerScaleUp / planScale, 5 * fatFingerScaleUp / planScale, 5 * fatFingerScaleUp / planScale, 5 * fatFingerScaleUp / planScale}, 4 * fatFingerScaleUp / planScale);

    paintCompass(g2D, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintRooms(g2D, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintWalls(g2D, selectedItems, planScale, backgroundColor, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintFurniture(g2D, this.sortedLevelFurniture, selectedItems,
        planScale, backgroundColor, foregroundColor, getFurnitureOutlineColor(), paintMode, true);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintPolylines(g2D, this.home.getPolylines(), selectedItems, selectionOutlinePaint,
        selectionColor, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintDimensionLines(g2D, this.home.getDimensionLines(), selectedItems,
        selectionOutlinePaint, dimensionLinesSelectionOutlineStroke, selectionColor,
        locationFeedbackStroke, planScale, backgroundColor, foregroundColor, paintMode, false);

    // Paint rooms text, furniture name and labels last to ensure they are not hidden
    checkCurrentThreadIsntInterrupted(paintMode);
    paintRoomsNameAndArea(g2D, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintFurnitureName(g2D, this.sortedLevelFurniture, selectedItems, planScale, foregroundColor, paintMode);

    checkCurrentThreadIsntInterrupted(paintMode);
    paintLabels(g2D, this.home.getLabels(), selectedItems, selectionOutlinePaint, dimensionLinesSelectionOutlineStroke,
        selectionColor, planScale, foregroundColor, paintMode);

    if (paintMode == PaintMode.PAINT
        && this.selectedItemsOutlinePainted) {
      paintCompassOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor,
          planScale, foregroundColor);
      paintRoomsOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor,
          planScale, foregroundColor);
      paintWallsOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor,
          planScale, foregroundColor);
      paintFurnitureOutline(g2D, selectedItems, selectionOutlinePaint, selectionOutlineStroke, selectionColor,
          planScale, foregroundColor);
    }
  }

  /**
   * Returns the color used to draw selection outlines.
   */
	protected Color getSelectionColor() {
		return getDefaultSelectionColor(this);
	}

	/**
	 * Returns the default color used to draw selection outlines.
	 */
	static Color getDefaultSelectionColor(JComponent planComponent) {
  /*  if (OperatingSystem.isMacOSX()) {
      if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
        Window window = SwingUtilities.getWindowAncestor(planComponent);
        if (window != null && !window.isActive()) {
          Color selectionColor = UIManager.getColor("List.selectionInactiveBackground");
          if (selectionColor != null) {
            return selectionColor.darker();
          }
        }
        Color selectionColor = UIManager.getColor("List.selectionBackground");
        if (selectionColor != null) {
          return selectionColor;
        }
      }

      return   UIManager.getColor("textHighlight");
    } else {*/
      // On systems different from Mac OS X, take a darker color
	  // From desktop r=0,g=84,b=150
      return  new Color(0.0f, 84f/255f, 150f/255f);// UIManager.getColor("textHighlight").darker();
   // }
  }

  /**
   * Returns the color used to draw furniture outline of
   * the shape where a user can click to select a piece of furniture.
   */
  protected Color getFurnitureOutlineColor() {
    return new Color((getForeground().getRGB() & 0xFFFFFF) | 0x55000000, true);
  }

  /**
   * Paints rooms.
   */
  private void paintRooms(Graphics2D g2D, List<Selectable> selectedItems, float planScale,
                          Color foregroundColor, PaintMode paintMode) {
    if (this.sortedLevelRooms == null) {
      // Sort home rooms in floor / floor-ceiling / ceiling order
      this.sortedLevelRooms = new ArrayList<Room>();
      for (Room room : this.home.getRooms()) {
        if (isViewableAtSelectedLevel(room)) {
          this.sortedLevelRooms.add(room);
        }
      }
      Collections.sort(this.sortedLevelRooms,
          new Comparator<Room>() {
            public int compare(Room room1, Room room2) {
              if (room1.isFloorVisible() == room2.isFloorVisible()
                  && room1.isCeilingVisible() == room2.isCeilingVisible()) {
                return 0; // Keep default order if the rooms have the same visibility
              } else if (!room2.isFloorVisible()
                         || room2.isCeilingVisible()) {
                return 1;
              } else {
                return -1;
              }
            }
          });
    }

    Color defaultFillPaint = paintMode == PaintMode.PRINT
        ? Color.WHITE
        : Color.GRAY;
    // Draw rooms area
    g2D.setStroke(new BasicStroke(getStrokeWidth(Room.class, paintMode) / planScale));
    for (Room room : this.sortedLevelRooms) {
      boolean selectedRoom = selectedItems.contains(room);
      // In clipboard paint mode, paint room only if it is selected
      if (paintMode != PaintMode.CLIPBOARD
          || selectedRoom) {
        g2D.setPaint(defaultFillPaint);
        float textureAngle = 0;
        if (this.preferences.isRoomFloorColoredOrTextured()
            && room.isFloorVisible()) {
          // Use room floor color or texture image
          if (room.getFloorColor() != null) {
            g2D.setPaint(new Color(room.getFloorColor()));
          } else {
            final HomeTexture floorTexture = room.getFloorTexture();
            if (floorTexture != null) {
              if (this.floorTextureImagesCache == null) {
                this.floorTextureImagesCache = new WeakHashMap<HomeTexture, BufferedImage>();
              }
              BufferedImage textureImage = this.floorTextureImagesCache.get(floorTexture);
              if (textureImage == null
                  || textureImage == WAIT_TEXTURE_IMAGE) {
                final boolean waitForTexture = paintMode != PaintMode.PAINT;
				  // checks cut out for speed, always go TextureManager
           /*     if (isTextureManagerAvailable()
                    // Don't use images managed by Java3D textures
                    // to avoid InternalError "Surface not cachable" in Graphics2D#fill call
                    // See bug at https://bugs.openjdk.java.net/browse/JDK-8072618
                    && !(OperatingSystem.isLinux()
                          && OperatingSystem.isJavaVersionGreaterOrEqual("1.7"))) {*/
                  // Prefer to share textures images with texture manager if it's available
                  TextureManager.getInstance().loadTexture( floorTexture.getImage(), waitForTexture,
                      new TextureManager.TextureObserver() {
                        public void textureUpdated(Texture texture) {
                          floorTextureImagesCache.put(floorTexture,
                              ((ImageComponent2D)texture.getImage(0)).getImage());
                          if (!waitForTexture) {
                            repaint();
                          }
                        }
                      });
             /*   } else {
                    // Use icon manager if texture manager should be ignored
                  Icon textureIcon = IconManager.getInstance().getIcon(floorTexture.getImage(),
                      waitForTexture ? null : this);
                  if (IconManager.getInstance().isWaitIcon(textureIcon)) {
                      this.floorTextureImagesCache.put(floorTexture, WAIT_TEXTURE_IMAGE);
                    } else if (IconManager.getInstance().isErrorIcon(textureIcon)) {
                      this.floorTextureImagesCache.put(floorTexture, ERROR_TEXTURE_IMAGE);
                    } else {
                    BufferedImage textureIconImage = new BufferedImage(
                        textureIcon.getIconWidth(), textureIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
                    Graphics2D g2DIcon = (Graphics2D)textureIconImage.getGraphics();
                    textureIcon.paintIcon(this, g2DIcon, 0, 0);
                    g2DIcon.dispose();
                    this.floorTextureImagesCache.put(floorTexture, textureIconImage);
                  }
                }*/
                textureImage = this.floorTextureImagesCache.get(floorTexture);
              }

              float textureWidth = floorTexture.getWidth();
              float textureHeight = floorTexture.getHeight();
              if (textureWidth == -1 || textureHeight == -1) {
                textureWidth = 100;
                textureHeight = 100;
              }
              float textureScale = floorTexture.getScale();
              textureAngle = floorTexture.getAngle();
              double cosAngle = Math.cos(textureAngle);
              double sinAngle = Math.sin(textureAngle);
                g2D.setPaint(new TexturePaint(textureImage,
                  new Rectangle2D.Double(
                      floorTexture.getXOffset() * textureWidth * textureScale * cosAngle
                      - floorTexture.getYOffset() * textureHeight * textureScale * sinAngle,
                      - floorTexture.getXOffset() * textureWidth * textureScale * sinAngle
                      - floorTexture.getYOffset() * textureHeight * textureScale * cosAngle,
                      textureWidth * textureScale, textureHeight * textureScale)));
            }
          }
        }

				Composite oldComposite = setTransparency(g2D, 0.75f);
        // Rotate graphics to rotate texture with requested angle
        // and draw shape rotated with the opposite angle
        g2D.rotate(textureAngle, 0, 0);
        AffineTransform rotation = textureAngle != 0
            ? AffineTransform.getRotateInstance(-textureAngle, 0, 0)
            : null;
        Shape roomShape = ShapeTools.getShape(room.getPoints(), true, rotation);
        fillShape(g2D, roomShape, paintMode);
				g2D.setComposite(oldComposite);

        g2D.setPaint(foregroundColor);
        g2D.draw(roomShape);
        g2D.rotate(-textureAngle, 0, 0);
      }
    }
  }

  /**
   * Fills the given <code>shape</code>.
   */
  private void fillShape(Graphics2D g2D, Shape shape, PaintMode paintMode) {
    if (paintMode == PaintMode.PRINT
        && g2D.getPaint() instanceof TexturePaint
        && OperatingSystem.isMacOSX()
        && OperatingSystem.isJavaVersionBetween("1.7", "1.8.0_152")) {
      Shape clip = g2D.getClip();
      g2D.setClip(shape);
      TexturePaint paint = (TexturePaint)g2D.getPaint();
      BufferedImage image = paint.getImage();
      Rectangle2D anchorRect = paint.getAnchorRect();
      Rectangle2D shapeBounds = shape.getBounds2D();
      double firstX = anchorRect.getX() + Math.round(shapeBounds.getX() / anchorRect.getWidth()) * anchorRect.getWidth();
      if (firstX > shapeBounds.getX()) {
        firstX -= anchorRect.getWidth();
      }
      double firstY = anchorRect.getY() + Math.round(shapeBounds.getY() / anchorRect.getHeight()) * anchorRect.getHeight();
      if (firstY > shapeBounds.getY()) {
        firstY -= anchorRect.getHeight();
      }
      for (double x = firstX;
          x < shapeBounds.getMaxX(); x += anchorRect.getWidth()) {
        for (double y = firstY; y < shapeBounds.getMaxY(); y += anchorRect.getHeight()) {
          AffineTransform transform = AffineTransform.getTranslateInstance(x, y);
          transform.concatenate(AffineTransform.getScaleInstance(
              anchorRect.getWidth() / image.getWidth(), anchorRect.getHeight() / image.getHeight()));
          g2D.drawRenderedImage(image, transform);
        }
      }
      g2D.setClip(clip);
    } else {
      g2D.fill(shape);
    }
  }

  /**
   * Returns <code>true</code> if <code>TextureManager</code> can be used to manage textures.
   */
  private static boolean isTextureManagerAvailable() {
	  return true;	  //Must have 3D and can't be a mac
  }

  /**
   * Paints rooms name and area.
   */
  private void paintRoomsNameAndArea(Graphics2D g2D, List<Selectable> selectedItems, float planScale,
                                     Color foregroundColor, PaintMode paintMode) {
    g2D.setPaint(foregroundColor);
    Font previousFont = g2D.getFont();
    for (Room room : this.sortedLevelRooms) {
      boolean selectedRoom = selectedItems.contains(room);
      // In clipboard paint mode, paint room only if it is selected
      if (paintMode != PaintMode.CLIPBOARD
          || selectedRoom) {
        float xRoomCenter = room.getXCenter();
        float yRoomCenter = room.getYCenter();
        String name = room.getName();
        if (name != null) {
          name = name.trim();
          if (name.length() > 0) {
            paintText(g2D, room.getClass(), name, room.getNameStyle(), null,
                xRoomCenter + room.getNameXOffset(),
                yRoomCenter + room.getNameYOffset(),
                room.getNameAngle(), previousFont);
          }
        }
        if (room.isAreaVisible()) {
          float area = room.getArea();
          if (area > 0.01f) {
            // Draw room area
            String areaText = this.preferences.getLengthUnit().getAreaFormatWithUnit().format(area);
            paintText(g2D, room.getClass(), areaText, room.getAreaStyle(), null,
                xRoomCenter + room.getAreaXOffset(),
                yRoomCenter + room.getAreaYOffset(),
                room.getAreaAngle(), previousFont);
          }
        }
      }
    }
    g2D.setFont(previousFont);
  }

  /**
   * Paints the given <code>text</code> centered at the point (<code>x</code>,<code>y</code>).
   */
  private void paintText(Graphics2D g2D,
                         Class<? extends Selectable> selectableClass,
                         String text, TextStyle style, Integer outlineColor,
                         float x, float y, float angle,
                         Font defaultFont) {
    AffineTransform previousTransform = g2D.getTransform();
		g2D.translate(x, y);
		g2D.rotate(angle);
    if (style == null) {
      style = this.preferences.getDefaultTextStyle(selectableClass);
    }
    FontMetrics fontMetrics = getFontMetrics(defaultFont, style);
		String [] lines = text.split("\n");
		float [] lineWidths = new float [lines.length];
		float textWidth = -Float.MAX_VALUE;
		for (int i = 0; i < lines.length; i++) {
			lineWidths [i] = (float)getFont(defaultFont, style).getStringBounds(lines [i]).getWidth();
			textWidth = Math.max(lineWidths [i], textWidth);
		}
		BasicStroke stroke = null;
		Font font;
		if (outlineColor != null) {
			stroke = new BasicStroke(style.getFontSize() * 0.05f);
			TextStyle outlineStyle = style.deriveStyle(style.getFontSize() - stroke.getLineWidth());
			font = getFont(defaultFont, outlineStyle);
			g2D.setStroke(stroke);
		} else {
			font = getFont(defaultFont, style);
		}
		g2D.setFont(font);

		for (int i = lines.length - 1; i >= 0; i--) {
			String line = lines[i];
			float translationX;
			if (style.getAlignment() == TextStyle.Alignment.LEFT) {
				translationX = 0;
			} else if (style.getAlignment() == TextStyle.Alignment.RIGHT) {
				translationX = -lineWidths[i];
			} else { // CENTER
				translationX = -lineWidths[i] / 2;
			}
			if (outlineColor != null) {
				translationX += stroke.getLineWidth() / 2;
			}
			g2D.translate(translationX, 0);
			if (outlineColor != null) {
				// Draw text outline
				Color defaultColor = g2D.getColor();
				g2D.setColor(new Color(outlineColor));
				//TODO: what is a TextLayout and it's outline?
				//TextLayout textLayout = new TextLayout(line, font, g2D.getFontRenderContext());
				//g2D.draw(textLayout.getOutline(null));
				g2D.setColor(defaultColor);
			}

			// Draw text
			g2D.drawString(line, 0, 0);
			g2D.translate(-translationX, -((fontMetrics.bottom - fontMetrics.top) - fontMetrics.leading));
		}
		g2D.setTransform(previousTransform);
  }

  /**
   * Paints the outline of rooms among <code>items</code> and indicators if
   * <code>items</code> contains only one room and indicator paint isn't <code>null</code>.
   */
  private void paintRoomsOutline(Graphics2D g2D, List<Selectable> items,
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke,
                          Paint indicatorPaint, float planScale, Color foregroundColor) {
    Collection<Room> rooms = Home.getRoomsSubList(items);
 		float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
    // Draw selection border
    for (Room room : rooms) {
      if (isViewableAtSelectedLevel(room)) {
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        g2D.draw(ShapeTools.getShape(room.getPoints(), true, null));

        if (indicatorPaint != null) {
          g2D.setPaint(indicatorPaint);
          // Draw points of the room
          for (float [] point : room.getPoints()) {
			  		AffineTransform previousTransform = g2D.getTransform();//forces a save
            g2D.translate(point [0], point [1]);
            g2D.scale(scaleInverse, scaleInverse);
            g2D.setStroke(POINT_STROKE);
            g2D.fill(WALL_POINT);
            g2D.setTransform(previousTransform);
          }
        }
      }
    }

    // Draw rooms area
    g2D.setPaint(foregroundColor);
    g2D.setStroke(new BasicStroke(getStrokeWidth(Room.class, PaintMode.PAINT) / planScale));
    for (Room room : rooms) {
      if (isViewableAtSelectedLevel(room)) {
        g2D.draw(ShapeTools.getShape(room.getPoints(), true, null));
      }
    }

    // Paint resize indicators of the room if indicator paint exists
    if (items.size() == 1
        && rooms.size() == 1
        && indicatorPaint != null) {
      Room selectedRoom = rooms.iterator().next();
      if (isViewableAtSelectedLevel(selectedRoom)) {
        g2D.setPaint(indicatorPaint);
        paintPointsResizeIndicators(g2D, selectedRoom, indicatorPaint, planScale, true, 0, 0, true);
        paintRoomNameOffsetIndicator(g2D, selectedRoom, indicatorPaint, planScale);
        paintRoomAreaOffsetIndicator(g2D, selectedRoom, indicatorPaint, planScale);
      }
    }
  }

  /**
   * Paints resize indicators on selectable <code>item</code>.
   */
  private void paintPointsResizeIndicators(Graphics2D g2D, Selectable item,
                                           Paint indicatorPaint,
                                           float planScale,
                                           boolean closedPath,
                                           float   angleAtStart,
                                           float   angleAtEnd,
                                           boolean orientateIndicatorOutsideShape) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);
      float scaleInverse = 1* fatFingerScaleUp / planScale * this.resolutionScale;
      float [][] points = item.getPoints();
      Shape resizeIndicator = getIndicator(item, IndicatorType.RESIZE);
      for (int i = 0; i < points.length; i++) {
      	AffineTransform previousTransform = g2D.getTransform();//forces a save
        // Draw resize indicator at point
        float [] point = points [i];
        g2D.translate(point[0], point[1]);
        g2D.scale(scaleInverse, scaleInverse);
        float [] previousPoint = i == 0
            ? points [points.length - 1]
            : points [i -1];
        float [] nextPoint = i == points.length - 1
            ? points [0]
            : points [i + 1];
        double angle;
        if (closedPath || (i > 0 && i < points.length - 1)) {
          // Compute the angle of the mean normalized normal at point i
          float distance1 = (float)Point2D.distance(
              previousPoint [0], previousPoint [1], point [0], point [1]);
          float xNormal1 = (point [1] - previousPoint [1]) / distance1;
          float yNormal1 = (previousPoint [0] - point [0]) / distance1;
          float distance2 = (float)Point2D.distance(
              nextPoint [0], nextPoint [1], point [0], point [1]);
          float xNormal2 = (nextPoint [1] - point [1]) / distance2;
          float yNormal2 = (point [0] - nextPoint [0]) / distance2;
          angle = Math.atan2(yNormal1 + yNormal2, xNormal1 + xNormal2);
          // Ensure the indicator will be drawn outside of shape
          if (orientateIndicatorOutsideShape
                && item.containsPoint(point [0] + (float)Math.cos(angle),
                      point [1] + (float)Math.sin(angle), 0.001f)
              || !orientateIndicatorOutsideShape
                  && (xNormal1 * yNormal2 - yNormal1 * xNormal2) < 0) {
            angle += Math.PI;
          }
        } else if (i == 0) {
          angle = angleAtStart;
        } else {
          angle = angleAtEnd;
        }
        g2D.rotate(angle);
        g2D.draw(resizeIndicator);
        g2D.setTransform(previousTransform);
      }
    }
  }

  /**
   * Returns the shape of the given indicator type.
   */
  protected Shape getIndicator(Selectable item, IndicatorType indicatorType) {
    if (IndicatorType.RESIZE.equals(indicatorType)) {
      if (item instanceof HomePieceOfFurniture) {
        return FURNITURE_RESIZE_INDICATOR;
      } else if (item instanceof Compass) {
        return COMPASS_RESIZE_INDICATOR;
      } else {
        return WALL_AND_LINE_RESIZE_INDICATOR;
      }
    } else if (IndicatorType.ROTATE.equals(indicatorType)) {
      if (item instanceof HomePieceOfFurniture) {
        return FURNITURE_ROTATION_INDICATOR;
      } else if (item instanceof Compass) {
        return COMPASS_ROTATION_INDICATOR;
      } else if (item instanceof Camera) {
        return CAMERA_YAW_ROTATION_INDICATOR;
      }
    } else if (IndicatorType.ELEVATE.equals(indicatorType)) {
      if (item instanceof Camera) {
        return CAMERA_ELEVATION_INDICATOR;
      } else {
        return ELEVATION_INDICATOR;
      }
    } else if (IndicatorType.RESIZE_HEIGHT.equals(indicatorType)) {
      if (item instanceof HomePieceOfFurniture) {
        return FURNITURE_HEIGHT_INDICATOR;
      }
    } else if (IndicatorType.CHANGE_POWER.equals(indicatorType)) {
      if (item instanceof HomeLight) {
        return LIGHT_POWER_INDICATOR;
      }
    } else if (IndicatorType.MOVE_TEXT.equals(indicatorType)) {
      return TEXT_LOCATION_INDICATOR;
    } else if (IndicatorType.ROTATE_TEXT.equals(indicatorType)) {
      return TEXT_ANGLE_INDICATOR;
    } else if (IndicatorType.ROTATE_PITCH.equals(indicatorType)) {
      if (item instanceof HomePieceOfFurniture) {
        return FURNITURE_PITCH_ROTATION_INDICATOR;
      } else if (item instanceof Camera) {
      return CAMERA_PITCH_ROTATION_INDICATOR;
    }
    } else if (IndicatorType.ROTATE_ROLL.equals(indicatorType)) {
      if (item instanceof HomePieceOfFurniture) {
        return FURNITURE_ROLL_ROTATION_INDICATOR;
      }
    } else if (IndicatorType.ARC_EXTENT.equals(indicatorType)) {
      if (item instanceof Wall) {
        return WALL_ARC_EXTENT_INDICATOR;
      }
    }
    return null;
  }

  /**
   * Paints name indicator on <code>room</code>.
   */
  private void paintRoomNameOffsetIndicator(Graphics2D g2D, Room room,
                                            Paint indicatorPaint,
                                            float planScale) {
    if (this.resizeIndicatorVisible
        && room.getName() != null
        && room.getName().trim().length() > 0) {
      float xName = room.getXCenter() + room.getNameXOffset();
      float yName = room.getYCenter() + room.getNameYOffset();
      paintTextIndicators(g2D, room.getClass(), getLineCount(room.getName()),
          room.getNameStyle(), xName, yName, room.getNameAngle(), indicatorPaint, planScale);
    }
  }

  /**
   * Paints resize indicator on <code>room</code>.
   */
  private void paintRoomAreaOffsetIndicator(Graphics2D g2D, Room room,
                                            Paint indicatorPaint,
                                            float planScale) {
    if (this.resizeIndicatorVisible
        && room.isAreaVisible()
        && room.getArea() > 0.01f) {
      float xArea = room.getXCenter() + room.getAreaXOffset();
      float yArea = room.getYCenter() + room.getAreaYOffset();
      paintTextIndicators(g2D, room.getClass(), 1, room.getAreaStyle(), xArea, yArea, room.getAreaAngle(),
          indicatorPaint, planScale);
    }
  }

  /**
   * Paints text location and angle indicators at the given coordinates.
   */
  private void paintTextIndicators(Graphics2D g2D,
                                   Class<? extends Selectable> selectableClass,
                                   int lineCount, TextStyle style,
                                   float x, float y, float angle,
                                   Paint indicatorPaint,
                                   float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);
      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
      g2D.translate(x, y);
      g2D.rotate(angle);
      g2D.scale(scaleInverse, scaleInverse);
      if (Label.class.isAssignableFrom(selectableClass)) {
		  	g2D.draw(LABEL_CENTER_INDICATOR);
      } else {
        g2D.draw(getIndicator(null, IndicatorType.MOVE_TEXT));
      }
      if (style == null) {
        style = this.preferences.getDefaultTextStyle(selectableClass);
      }
			FontMetrics fontMetrics = getFontMetrics(g2D.getFont(), style);
      g2D.setTransform(previousTransform);
			AffineTransform previousTransform2 = g2D.getTransform();//force save
      g2D.translate(x, y);
      g2D.rotate(angle);
			//PJ the ascent in fontMetrics is already -ve so -fontMetrics.ascent
			g2D.translate(0, -((fontMetrics.bottom - fontMetrics.top) - fontMetrics.leading) * (lineCount - 1)
							- (-fontMetrics.ascent) * (Label.class.isAssignableFrom(selectableClass) ? 1 : 0.85));
			g2D.scale(scaleInverse, scaleInverse);
      g2D.draw(getIndicator(null, IndicatorType.ROTATE_TEXT));
      g2D.setTransform(previousTransform2);
    }
  }

	/**
	 * Returns the number of lines in the given <code>text</code>.
	 */
	private int getLineCount(String text) {
		int lineCount = 1;
		for (int i = 0, n = text.length(); i < n; i++) {
			if (text.charAt(i) == '\n') {
				lineCount++;
			}
		}
		return lineCount;
	}

  /**
   * Paints walls.
   */
  private void paintWalls(Graphics2D g2D, List<Selectable> selectedItems, float planScale,
                          Color backgroundColor, Color foregroundColor, PaintMode paintMode) {
    Collection<Wall> paintedWalls;
    Map<Collection<Wall>, Area> wallAreas;
    if (paintMode != PaintMode.CLIPBOARD) {
      wallAreas = getWallAreas();
    } else {
      // In clipboard paint mode, paint only selected walls
      paintedWalls = Home.getWallsSubList(selectedItems);
      wallAreas = getWallAreas(getDrawableWallsInSelectedLevel(paintedWalls));
    }
    float wallPaintScale = paintMode == PaintMode.PRINT
        ? planScale / 72 * 150 // Adjust scale to 150 dpi for print
        : planScale / this.resolutionScale;
    Composite oldComposite = null;
    if (paintMode == PaintMode.PAINT
        && this.backgroundPainted
        && this.backgroundImageCache != null
        && this.wallsDoorsOrWindowsModification) {
      // Paint walls with half transparent paint when a wall or a door/window in the base plan is being handled
      oldComposite = setTransparency(g2D, 0.5f);
    }
    for (Map.Entry<Collection<Wall>, Area> areaEntry : wallAreas.entrySet()) {
      TextureImage wallPattern = areaEntry.getKey().iterator().next().getPattern();
      fillAndDrawWallsArea(g2D, areaEntry.getValue(), planScale,
          getWallPaint(wallPaintScale, backgroundColor, foregroundColor,
              wallPattern != null ? wallPattern : this.preferences.getWallPattern()), foregroundColor, paintMode);
    }
    if (oldComposite != null) {
      g2D.setComposite(oldComposite);
    }
  }

  /**
   * Fills and paints the given area.
   */
  private void fillAndDrawWallsArea(Graphics2D g2D, Area area, float planScale, Paint fillPaint,
                                    Paint drawPaint, PaintMode paintMode) {
    // Fill walls area
    g2D.setPaint(fillPaint);
    fillShape(g2D, area, paintMode);
    // Draw walls area
    g2D.setPaint(drawPaint);
    g2D.setStroke(new BasicStroke(getStrokeWidth(Wall.class, paintMode) / planScale));
    g2D.draw(area);
  }

  /**
   * Paints the outline of walls among <code>items</code> and a resize indicator if
   * <code>items</code> contains only one wall and indicator paint isn't <code>null</code>.
   */
  private void paintWallsOutline(Graphics2D g2D, List<Selectable> items,
                                 Paint selectionOutlinePaint, Stroke selectionOutlineStroke,
                                 Paint indicatorPaint, float planScale, Color foregroundColor) {
		float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
    Collection<Wall> walls = Home.getWallsSubList(items);
    //AffineTransform previousTransform = g2D.getTransform();
    for (Wall wall : walls) {
      if (isViewableAtSelectedLevel(wall)) {
        // Draw selection border
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        g2D.draw(ShapeTools.getShape(wall.getPoints(), true, null));

        if (indicatorPaint != null) {
					AffineTransform previousTransform = g2D.getTransform();
          // Draw start point of the wall
          g2D.translate(wall.getXStart(), wall.getYStart());
          g2D.scale(scaleInverse, scaleInverse);
          g2D.setPaint(indicatorPaint);
          g2D.setStroke(POINT_STROKE);
          g2D.fill(WALL_POINT);

          Float arcExtent = wall.getArcExtent();
          double indicatorAngle;
          double distanceAtScale;
          float xArcCircleCenter = 0;
          float yArcCircleCenter = 0;
          double arcCircleRadius = 0;
          double startPointToEndPointDistance = wall.getStartPointToEndPointDistance();
          double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(),
              wall.getXEnd() - wall.getXStart());
          if (arcExtent != null
              && arcExtent.floatValue() != 0) {
            xArcCircleCenter = wall.getXArcCircleCenter();
            yArcCircleCenter = wall.getYArcCircleCenter();
            arcCircleRadius = Point2D.distance(wall.getXStart(), wall.getYStart(),
                xArcCircleCenter, yArcCircleCenter);
            distanceAtScale = arcCircleRadius * Math.abs(arcExtent) * planScale;
            indicatorAngle = Math.atan2(yArcCircleCenter - wall.getYStart(),
                    xArcCircleCenter - wall.getXStart())
                + (arcExtent > 0 ? -Math.PI / 2 : Math.PI /2);
          } else {
            distanceAtScale = startPointToEndPointDistance * planScale;
            indicatorAngle = wallAngle;
          }
          // If the distance between start and end points is < 30
          if (distanceAtScale < 30) {
            // Draw only one orientation indicator between the two points
            g2D.rotate(wallAngle);
            if (arcExtent != null) {
              double wallToStartPointArcCircleCenterAngle = Math.abs(arcExtent) > Math.PI
                  ? -(Math.PI + arcExtent) / 2
                  : (Math.PI - arcExtent) / 2;
              float arcCircleCenterToWallDistance = (float)(Math.tan(wallToStartPointArcCircleCenterAngle)
                  * startPointToEndPointDistance / 2);
              g2D.translate(startPointToEndPointDistance * planScale / 2,
                  (arcCircleCenterToWallDistance - arcCircleRadius * (Math.abs(wallAngle) > Math.PI / 2 ? -1: 1)) * planScale);
            } else {
              g2D.translate(distanceAtScale / 2, 0);
            }
          } else {
            // Draw orientation indicator at start of the wall
            g2D.rotate(indicatorAngle);
            g2D.translate(8, 0);
          }
          g2D.draw(WALL_ORIENTATION_INDICATOR);
          g2D.setTransform(previousTransform);
					AffineTransform previousTransform2 = g2D.getTransform();//force save
          // Draw end point of the wall
          g2D.translate(wall.getXEnd(), wall.getYEnd());
          g2D.scale(scaleInverse, scaleInverse);
          g2D.fill(WALL_POINT);
          if (distanceAtScale >= 30) {
            if (arcExtent != null) {
              indicatorAngle += arcExtent;
            }
            // Draw orientation indicator at end of the wall
            g2D.rotate(indicatorAngle);
            g2D.translate(-10, 0);
            g2D.draw(WALL_ORIENTATION_INDICATOR);
          }
          g2D.setTransform(previousTransform2);
        }
      }
    }
    // Draw walls area
    g2D.setPaint(foregroundColor);
    g2D.setStroke(new BasicStroke(getStrokeWidth(Wall.class, PaintMode.PAINT) / planScale));
    for (Area area : getWallAreas(getDrawableWallsInSelectedLevel(walls)).values()) {
      g2D.draw(area);
    }

    // Paint resize indicator of the wall if indicator paint exists
    if (items.size() == 1
        && walls.size() == 1
        && indicatorPaint != null) {
      Wall wall = walls.iterator().next();
      if (isViewableAtSelectedLevel(wall)) {
        paintWallResizeIndicators(g2D, wall, indicatorPaint, planScale);
      }
    }
  }

  /**
   * Returns <code>true</code> if the given item can be viewed in the plan at the selected level.
   */
  protected boolean isViewableAtSelectedLevel(Elevatable item) {
    Level level = item.getLevel();
    return level == null
        || (level.isViewable()
            && item.isAtLevel(this.home.getSelectedLevel()));
  }

  /**
   * Paints resize indicators on <code>wall</code>.
   */
  private void paintWallResizeIndicators(Graphics2D g2D, Wall wall,
                                        Paint indicatorPaint,
                                        float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);

      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
      float [][] wallPoints = wall.getPoints();
      int leftSideMiddlePointIndex = wallPoints.length / 4;
      double wallAngle = Math.atan2(wall.getYEnd() - wall.getYStart(),
          wall.getXEnd() - wall.getXStart());

      // Draw arc extent indicator at wall middle
      if (wallPoints.length % 4 == 0) {
        g2D.translate((wallPoints [leftSideMiddlePointIndex - 1][0] + wallPoints [leftSideMiddlePointIndex][0]) / 2,
            (wallPoints [leftSideMiddlePointIndex - 1][1] + wallPoints [leftSideMiddlePointIndex][1]) / 2);
      } else {
        g2D.translate(wallPoints [leftSideMiddlePointIndex][0], wallPoints [leftSideMiddlePointIndex][1]);
      }
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(wallAngle + Math.PI);
      g2D.draw(getIndicator(wall, IndicatorType.ARC_EXTENT));
      g2D.setTransform(previousTransform);

      Float arcExtent = wall.getArcExtent();
      double indicatorAngle;
      if (arcExtent != null
          && arcExtent.floatValue() != 0) {
        indicatorAngle = Math.atan2(wall.getYArcCircleCenter() - wall.getYEnd(),
                wall.getXArcCircleCenter() - wall.getXEnd())
            + (arcExtent > 0 ? -Math.PI / 2 : Math.PI /2);
      } else {
        indicatorAngle = wallAngle;
      }

      AffineTransform previousTransform1 = g2D.getTransform();
      // Draw resize indicator at wall end point
      g2D.translate(wall.getXEnd(), wall.getYEnd());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(indicatorAngle);
			g2D.draw(getIndicator(wall, IndicatorType.RESIZE));
      g2D.setTransform(previousTransform1);
			AffineTransform previousTransform2 = g2D.getTransform();// force a save
      if (arcExtent != null) {
        indicatorAngle += Math.PI - arcExtent;
      } else {
        indicatorAngle += Math.PI;
      }

      // Draw resize indicator at wall start point
      g2D.translate(wall.getXStart(), wall.getYStart());
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(indicatorAngle);
			g2D.draw(getIndicator(wall, IndicatorType.RESIZE));
      g2D.setTransform(previousTransform2);
    }
  }

  /**
   * Returns areas matching the union of home wall shapes sorted by pattern.
   */
  private Map<Collection<Wall>, Area> getWallAreas() {
    if (this.wallAreasCache == null) {
      this.wallAreasCache = getWallAreas(getDrawableWallsInSelectedLevel(this.home.getWalls()));
    }
    return this.wallAreasCache;
  }

  /**
   * Returns the walls that belong to the selected level in home.
   */
  private Collection<Wall> getDrawableWallsInSelectedLevel(Collection<Wall> walls) {
    List<Wall> wallsInSelectedLevel = new ArrayList<Wall>();
    for (Wall wall : walls) {
      if (isViewableAtSelectedLevel(wall)) {
        wallsInSelectedLevel.add(wall);
      }
    }
    return wallsInSelectedLevel;
  }

  /**
   * Returns areas matching the union of <code>walls</code> shapes sorted by pattern.
   */
  private Map<Collection<Wall>, Area> getWallAreas(Collection<Wall> walls) {
    if (walls.size() == 0) {
      return Collections.emptyMap();
    }
    // Check if all walls use the same pattern
    TextureImage pattern = walls.iterator().next().getPattern();
    boolean samePattern = true;
    for (Wall wall : walls) {
      if (pattern != wall.getPattern()) {
        samePattern = false;
        break;
      }
    }
    Map<Collection<Wall>, Area> wallAreas = new LinkedHashMap<Collection<Wall>, Area>();
    if (samePattern) {
      wallAreas.put(walls, getItemsArea(walls));
    } else {
      // Create walls sublists by pattern
      Map<TextureImage, Collection<Wall>> sortedWalls = new LinkedHashMap<TextureImage, Collection<Wall>>();
      for (Wall wall : walls) {
        TextureImage wallPattern = wall.getPattern();
        if (wallPattern == null) {
          wallPattern = this.preferences.getWallPattern();
        }
        Collection<Wall> patternWalls = sortedWalls.get(wallPattern);
        if (patternWalls == null) {
          patternWalls = new ArrayList<Wall>();
          sortedWalls.put(wallPattern, patternWalls);
        }
        patternWalls.add(wall);
      }
      for (Collection<Wall> patternWalls : sortedWalls.values()) {
        wallAreas.put(patternWalls, getItemsArea(patternWalls));
      }
    }
    return wallAreas;
  }

  /**
   * Returns an area matching the union of all <code>items</code> shapes.
   */
  private Area getItemsArea(Collection<? extends Selectable> items) {
    Area itemsArea = new Area();
    for (Selectable item : items) {
      itemsArea.add(new Area(ShapeTools.getShape(item.getPoints(), true, null)));
    }
    return itemsArea;
  }

  /**
   * Returns the <code>Paint</code> object used to fill walls.
   */
  private Paint getWallPaint(float planScale, Color backgroundColor, Color foregroundColor, TextureImage wallPattern) {
    BufferedImage patternImage = this.patternImagesCache.get(wallPattern);
    if (patternImage == null
        || !backgroundColor.equals(this.wallsPatternBackgroundCache)
        || !foregroundColor.equals(this.wallsPatternForegroundCache)) {
      patternImage = SwingTools.getPatternImage(wallPattern, backgroundColor, foregroundColor);
      this.patternImagesCache.put(wallPattern, patternImage);
      this.wallsPatternBackgroundCache = backgroundColor;
      this.wallsPatternForegroundCache = foregroundColor;
    }
    return new TexturePaint(patternImage,
        new Rectangle2D.Float(0, 0, 10 / planScale, 10 / planScale));
  }

  /**
   * Paints home furniture.
   */
  private void paintFurniture(Graphics2D g2D, List<HomePieceOfFurniture> furniture,
                              List<? extends Selectable> selectedItems, float planScale,
                              Color backgroundColor, Color foregroundColor,
                              Color furnitureOutlineColor,
                              PaintMode paintMode, boolean paintIcon) {
    if (!furniture.isEmpty()) {
      BasicStroke pieceBorderStroke = new BasicStroke(getStrokeWidth(HomePieceOfFurniture.class, paintMode) / planScale);
      Boolean allFurnitureViewedFromTop = null;
      // Draw furniture
      for (HomePieceOfFurniture piece : furniture) {
        if (piece.isVisible()) {
          boolean selectedPiece = selectedItems.contains(piece);
          if (piece instanceof HomeFurnitureGroup) {
            List<HomePieceOfFurniture> groupFurniture = ((HomeFurnitureGroup)piece).getFurniture();
            List<Selectable> emptyList = Collections.emptyList();
            paintFurniture(g2D, groupFurniture,
                selectedPiece
                    ? groupFurniture
                    : emptyList,
                planScale, backgroundColor, foregroundColor,
                furnitureOutlineColor, paintMode, paintIcon);
          } else if (paintMode != PaintMode.CLIPBOARD
                    || selectedPiece) {
            // In clipboard paint mode, paint piece only if it is selected
            Shape pieceShape = ShapeTools.getShape(piece.getPoints(), true, null);
            Shape pieceShape2D;
            if (piece instanceof HomeDoorOrWindow) {
              HomeDoorOrWindow doorOrWindow = (HomeDoorOrWindow)piece;
              pieceShape2D = getDoorOrWindowWallPartShape(doorOrWindow);
              if (this.draggedItemsFeedback == null
                  || !this.draggedItemsFeedback.contains(piece)) {
                paintDoorOrWindowWallThicknessArea(g2D, doorOrWindow, planScale, backgroundColor, foregroundColor, paintMode);
              }
              paintDoorOrWindowSashes(g2D, doorOrWindow, planScale, foregroundColor, paintMode);
            } else {
              pieceShape2D = pieceShape;
            }

            boolean viewedFromTop;
            if (this.preferences.isFurnitureViewedFromTop()) {
              if (piece.getPlanIcon() != null
                  || piece instanceof HomeDoorOrWindow) {
                viewedFromTop = true;
              } else {
                if (allFurnitureViewedFromTop == null) {
									allFurnitureViewedFromTop = true;// 3D must be supported
                 /* try {
                    // Evaluate allFurnitureViewedFromTop value as late as possible to avoid mandatory dependency towards Java 3D
                    allFurnitureViewedFromTop = !Boolean.getBoolean("com.eteks.sweethome3d.no3D")
                        && Component3DManager.getInstance().isOffScreenImageSupported();
                  } catch (AccessControlException ex) {
                    // If com.eteks.sweethome3d.no3D property can't be read,
                    // security manager won't allow to access to Java 3D DLLs required by PieceOfFurnitureModelIcon class too
                    allFurnitureViewedFromTop = false;
                  }*/
                }
                viewedFromTop = allFurnitureViewedFromTop.booleanValue();
              }
            } else {
              viewedFromTop = false;
            }
            if (paintIcon
                && viewedFromTop) {
              if (piece instanceof HomeDoorOrWindow) {
                // Draw doors and windows border
                g2D.setPaint(backgroundColor);
                g2D.fill(pieceShape2D);
                g2D.setPaint(foregroundColor);
                g2D.setStroke(pieceBorderStroke);
                g2D.draw(pieceShape2D);
              } else {
                paintPieceOfFurnitureTop(g2D, piece, pieceShape2D, pieceBorderStroke, planScale,
                    backgroundColor, foregroundColor, paintMode);
              }
              if (paintMode == PaintMode.PAINT) {
                // Draw selection outline rectangle
                g2D.setStroke(pieceBorderStroke);
                g2D.setPaint(furnitureOutlineColor);
                g2D.draw(pieceShape);
              }
            } else {
              if (paintIcon) {
                // Draw its icon
                paintPieceOfFurnitureIcon(g2D, piece, pieceShape2D, planScale,
                    backgroundColor, paintMode);
              }
              // Draw its border
              g2D.setPaint(foregroundColor);
              g2D.setStroke(pieceBorderStroke);
              g2D.draw(pieceShape2D);
              if (piece instanceof HomeDoorOrWindow
                  && paintMode == PaintMode.PAINT) {
                // Draw outline rectangle
                g2D.setPaint(furnitureOutlineColor);
                g2D.draw(pieceShape);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Returns the shape of the wall part of a door or a window.
   */
  private Shape getDoorOrWindowWallPartShape(HomeDoorOrWindow doorOrWindow) {
    Rectangle2D doorOrWindowWallPartRectangle = getDoorOrWindowRectangle(doorOrWindow, true);
    // Apply rotation to the rectangle
    AffineTransform rotation = AffineTransform.getRotateInstance(
        doorOrWindow.getAngle(), doorOrWindow.getX(), doorOrWindow.getY());
    PathIterator it = doorOrWindowWallPartRectangle.getPathIterator(rotation);
    GeneralPath doorOrWindowWallPartShape = new GeneralPath();
    doorOrWindowWallPartShape.append(it, false);
    return doorOrWindowWallPartShape;
  }

  /**
   * Returns the rectangle of a door or a window.
   */
  private Rectangle2D getDoorOrWindowRectangle(HomeDoorOrWindow doorOrWindow, boolean onlyWallPart) {
    // Doors and windows can't be rotated along horizontal axes
    float wallThickness = doorOrWindow.getDepth() * (onlyWallPart ? doorOrWindow.getWallThickness() : 1);
    float wallDistance  = doorOrWindow.getDepth() * (onlyWallPart ? doorOrWindow.getWallDistance()  : 0);
    String cutOutShape = doorOrWindow.getCutOutShape();
    float width = doorOrWindow.getWidth();
    float wallWidth = doorOrWindow.getWallWidth() * width;
    float x = doorOrWindow.getX() - width / 2;
    x += doorOrWindow.isModelMirrored()
        ? (1 - doorOrWindow.getWallLeft() - doorOrWindow.getWallWidth()) * width
        : doorOrWindow.getWallLeft() * width;
    if (cutOutShape != null
        && !PieceOfFurniture.DEFAULT_CUT_OUT_SHAPE.equals(cutOutShape)) {
      // In case of a complex cut out, compute location and width of the window hole at wall intersection
    	// In case of a complex cut out, compute location and width of the window hole at wall intersection
    	//PJPJPJPJ
      javaawt.Shape shape = ModelManager.getInstance().getShape(cutOutShape);
      javaawt.geom.Rectangle2D b2D = shape.getBounds2D();
      Rectangle2D bounds = new Rectangle2D.Double(b2D.getX(),b2D.getY(),b2D.getWidth(),b2D.getHeight());
      if (doorOrWindow.isModelMirrored()) {
        x += (float)(1 - bounds.getX() - bounds.getWidth()) * wallWidth;
      } else {
        x += (float)bounds.getX() * wallWidth;
      }
      wallWidth *= bounds.getWidth();
    }
		Rectangle2D doorOrWindowWallPartRectangle = new Rectangle2D.Float(
						x, doorOrWindow.getY() - doorOrWindow.getDepth() / 2 + wallDistance,
        wallWidth, wallThickness);
		return doorOrWindowWallPartRectangle;
	}

	/**
	 * Paints the shape of a door or a window in the thickness of the wall it intersects.
	 */
	private void paintDoorOrWindowWallThicknessArea(Graphics2D g2D, HomeDoorOrWindow doorOrWindow, float planScale,
																									Color backgroundColor, Color foregroundColor, PaintMode paintMode) {
		if (doorOrWindow.isWallCutOutOnBothSides()) {
			Area doorOrWindowWallArea = null;
			if (this.doorOrWindowWallThicknessAreasCache != null) {
				doorOrWindowWallArea = this.doorOrWindowWallThicknessAreasCache.get(doorOrWindow);
			}

			if (doorOrWindowWallArea == null) {
				Rectangle2D doorOrWindowRectangle = getDoorOrWindowRectangle(doorOrWindow, false);
				// Apply rotation to the rectangle
				AffineTransform rotation = AffineTransform.getRotateInstance(
								doorOrWindow.getAngle(), doorOrWindow.getX(), doorOrWindow.getY());
				PathIterator it = doorOrWindowRectangle.getPathIterator(rotation);
				GeneralPath doorOrWindowWallPartShape = new GeneralPath();
				doorOrWindowWallPartShape.append(it, false);
				Area doorOrWindowWallPartArea = new Area(doorOrWindowWallPartShape);

				doorOrWindowWallArea = new Area();
				for (Wall wall : home.getWalls()) {
					if (wall.isAtLevel(doorOrWindow.getLevel())
									&& doorOrWindow.isParallelToWall(wall)) {
						Shape wallShape = ShapeTools.getShape(wall.getPoints(), true, null);
						Area wallArea = new Area(wallShape);
						wallArea.intersect(doorOrWindowWallPartArea);
						if (!wallArea.isEmpty()) {
							Rectangle2D doorOrWindowExtendedRectangle = new Rectangle2D.Float(
											(float)doorOrWindowRectangle.getX(),
											(float)doorOrWindowRectangle.getY() - 2 * wall.getThickness(),
											(float)doorOrWindowRectangle.getWidth(),
											(float)doorOrWindowRectangle.getWidth() + 4 * wall.getThickness());
							it = doorOrWindowExtendedRectangle.getPathIterator(rotation);
							GeneralPath path = new GeneralPath();
							path.append(it, false);
							wallArea = new Area(wallShape);
							wallArea.intersect(new Area(path));
							doorOrWindowWallArea.add(wallArea);
						}
					}
				}
			}

			if (this.doorOrWindowWallThicknessAreasCache == null) {
				this.doorOrWindowWallThicknessAreasCache = new WeakHashMap<HomeDoorOrWindow, Area>();
			}
			this.doorOrWindowWallThicknessAreasCache.put(doorOrWindow, doorOrWindowWallArea);

			g2D.setPaint(backgroundColor);
			g2D.fill(doorOrWindowWallArea);
			g2D.setPaint(foregroundColor);
			g2D.setStroke(new BasicStroke(getStrokeWidth(HomePieceOfFurniture.class, paintMode) / planScale));
			g2D.draw(doorOrWindowWallArea);
		}
	}

  /**
   * Paints the sashes of a door or a window.
   */
  private void paintDoorOrWindowSashes(Graphics2D g2D, HomeDoorOrWindow doorOrWindow, float planScale,
                                       Color foregroundColor, PaintMode paintMode) {
    BasicStroke sashBorderStroke = new BasicStroke(getStrokeWidth(HomePieceOfFurniture.class, paintMode) / planScale);
    g2D.setPaint(foregroundColor);
    g2D.setStroke(sashBorderStroke);
    for (Sash sash : doorOrWindow.getSashes()) {
      g2D.draw(getDoorOrWindowSashShape(doorOrWindow, sash));
    }
  }

  /**
   * Returns the shape of a sash of a door or a window.
   */
  private GeneralPath getDoorOrWindowSashShape(HomeDoorOrWindow doorOrWindow,
                                               Sash sash) {
    // Doors and windows can't be rotated along horizontal axes
    float modelMirroredSign = doorOrWindow.isModelMirrored() ? -1 : 1;
    float xAxis = modelMirroredSign * sash.getXAxis() * doorOrWindow.getWidth();
    float yAxis = sash.getYAxis() * doorOrWindow.getDepth();
    float sashWidth = sash.getWidth() * doorOrWindow.getWidth();
    float startAngle = (float)Math.toDegrees(sash.getStartAngle());
    if (doorOrWindow.isModelMirrored()) {
      startAngle = 180 - startAngle;
    }
    float extentAngle = modelMirroredSign * (float)Math.toDegrees(sash.getEndAngle() - sash.getStartAngle());

    Arc2D arc = new Arc2D.Float(xAxis - sashWidth, yAxis - sashWidth,
        2 * sashWidth, 2 * sashWidth,
        startAngle, extentAngle, Arc2D.PIE);
    AffineTransform transformation = AffineTransform.getTranslateInstance(doorOrWindow.getX(), doorOrWindow.getY());
    transformation.rotate(doorOrWindow.getAngle());
    transformation.translate(modelMirroredSign * -doorOrWindow.getWidth() / 2, -doorOrWindow.getDepth() / 2);
    PathIterator it = arc.getPathIterator(transformation);
    GeneralPath sashShape = new GeneralPath();
    sashShape.append(it, false);
    return sashShape;
  }

  /**
   * Paints home furniture visible name.
   */
  private void paintFurnitureName(Graphics2D g2D, List<HomePieceOfFurniture> furniture,
                                  List<? extends Selectable> selectedItems, float planScale,
                                  Color foregroundColor, PaintMode paintMode) {
    Font previousFont = g2D.getFont();
    g2D.setPaint(foregroundColor);
    // Draw furniture name
    for (HomePieceOfFurniture piece : furniture) {
      if (piece.isVisible()) {
        boolean selectedPiece = selectedItems.contains(piece);
        if (piece instanceof HomeFurnitureGroup) {
          List<HomePieceOfFurniture> groupFurniture = ((HomeFurnitureGroup)piece).getFurniture();
          List<Selectable> emptyList = Collections.emptyList();
          paintFurnitureName(g2D, groupFurniture,
               selectedPiece
                   ? groupFurniture
                   : emptyList,
               planScale, foregroundColor, paintMode);
        }
        if (piece.isNameVisible()
            && (paintMode != PaintMode.CLIPBOARD
                || selectedPiece)) {
          // In clipboard paint mode, paint piece only if it is selected
          String name = piece.getName().trim();
          if (name.length() > 0) {
            // Draw piece name
            paintText(g2D, piece.getClass(), name, piece.getNameStyle(), null,
                piece.getX() + piece.getNameXOffset(),
                piece.getY() + piece.getNameYOffset(),
                piece.getNameAngle(), previousFont);
          }
        }
      }
    }
  //  g2D.setFont(previousFont);
  }

  /**
   * Paints the outline of furniture among <code>items</code> and indicators if
   * <code>items</code> contains only one piece and indicator paint isn't <code>null</code>.
   */
  private void paintFurnitureOutline(Graphics2D g2D, List<Selectable> items,
                                     Paint selectionOutlinePaint, Stroke selectionOutlineStroke,
                                     Paint indicatorPaint, float planScale,
                                     Color foregroundColor) {
    BasicStroke pieceBorderStroke = new BasicStroke(getStrokeWidth(HomePieceOfFurniture.class, PaintMode.PAINT) / planScale);
    BasicStroke pieceFrontBorderStroke = new BasicStroke(4 * getStrokeWidth(HomePieceOfFurniture.class, PaintMode.PAINT) / planScale,
        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER);

    List<HomePieceOfFurniture> furniture = Home.getFurnitureSubList(items);
    Area furnitureGroupsArea = null;
    BasicStroke furnitureGroupsStroke = new BasicStroke(15 / planScale * this.resolutionScale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND);
    HomePieceOfFurniture lastGroup = null;
    Area furnitureInGroupsArea = null;
    List<HomePieceOfFurniture> homeFurniture = this.home.getFurniture();
    for (Iterator<HomePieceOfFurniture> it = furniture.iterator(); it.hasNext();) {
      HomePieceOfFurniture piece = it.next();
      if (piece.isVisible()
          && isViewableAtSelectedLevel(piece)) {
        HomePieceOfFurniture homePieceOfFurniture = getPieceOfFurnitureInHomeFurniture(piece, homeFurniture);
        if (homePieceOfFurniture != piece) {
          Area groupArea = null;
          if (lastGroup != homePieceOfFurniture) {
            Shape groupShape = ShapeTools.getShape(homePieceOfFurniture.getPoints(), true, null);
            groupArea = new Area(groupShape);
            // Enlarge group area
            groupArea.add(new Area(furnitureGroupsStroke.createStrokedShape(groupShape)));
          }
          Area pieceArea = new Area(ShapeTools.getShape(piece.getPoints(), true, null));
          if (furnitureGroupsArea == null) {
            furnitureGroupsArea = groupArea;
            furnitureInGroupsArea = pieceArea;
          } else {
            if (lastGroup != homePieceOfFurniture) {
              furnitureGroupsArea.add(groupArea);
            }
            furnitureInGroupsArea.add(pieceArea);
          }
          // Store last group to avoid useless multiple computation
          lastGroup = homePieceOfFurniture;
        }
      } else {
        it.remove();
      }
    }
    if (furnitureGroupsArea != null) {
      // Fill the area of furniture groups around items with light outine color
      furnitureGroupsArea.subtract(furnitureInGroupsArea);
	  	Composite oldComposite = setTransparency(g2D, 0.6f);
      g2D.setPaint(selectionOutlinePaint);
      g2D.fill(furnitureGroupsArea);
	  	g2D.setComposite(oldComposite);
    }

    for (HomePieceOfFurniture piece : furniture) {
      float [][] points = piece.getPoints();
      Shape pieceShape = ShapeTools.getShape(points, true, null);

      // Draw selection border
      g2D.setPaint(selectionOutlinePaint);
      g2D.setStroke(selectionOutlineStroke);
      g2D.draw(pieceShape);

      // Draw its border
      g2D.setPaint(foregroundColor);
      g2D.setStroke(pieceBorderStroke);
      g2D.draw(pieceShape);

      // Draw its front face with a thicker line
      g2D.setStroke(pieceFrontBorderStroke);
      g2D.draw(new Line2D.Float(points [2][0], points [2][1], points [3][0], points [3][1]));

      if (items.size() == 1 && indicatorPaint != null) {
        paintPieceOFFurnitureIndicators(g2D, piece, indicatorPaint, planScale);
      }
    }
  }

  /**
   * Returns <code>piece</code> if it belongs to home furniture or the group to which <code>piece</code> belongs.
   */
  private HomePieceOfFurniture getPieceOfFurnitureInHomeFurniture(HomePieceOfFurniture piece,
                                                                  List<HomePieceOfFurniture> homeFurniture) {
    // Prefer iterate twice the furniture list rather than calling getAllFurniture uselessly
    // because subselecting won't happen often
    if (!homeFurniture.contains(piece)) {
      for (HomePieceOfFurniture homePiece : homeFurniture) {
        if (homePiece instanceof HomeFurnitureGroup
            && ((HomeFurnitureGroup)homePiece).getAllFurniture().contains(piece)) {
          return homePiece;
        }
      }
    }
    return piece;
  }

  /**
   * Paints <code>piece</code> icon with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureIcon(Graphics2D g2D, HomePieceOfFurniture piece,
                                         Shape pieceShape2D, float planScale,
                                         Color backgroundColor, PaintMode paintMode) {
    // Get piece icon
    Icon icon = IconManager.getInstance().getIcon(piece.getIcon(), 128,
        paintMode == PaintMode.PAINT ? this : null);
    paintPieceOfFurnitureIcon(g2D, piece, icon, pieceShape2D, planScale, backgroundColor);
  }

  /**
   * Paints <code>icon</code> with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureIcon(Graphics2D g2D, HomePieceOfFurniture piece, Icon icon,
                                         Shape pieceShape2D, float planScale, Color backgroundColor) {
    // Fill piece area
    g2D.setPaint(backgroundColor);
    g2D.fill(pieceShape2D);

    Shape previousClip = g2D.getClip();
    // Clip icon drawing into piece shape
    g2D.clip(pieceShape2D);
    AffineTransform previousTransform = g2D.getTransform();
    // Translate to piece center
		final Rectangle2D bounds = pieceShape2D.getBounds2D();
    g2D.translate(bounds.getCenterX(), bounds.getCenterY());
    float pieceDepth = piece.getDepthInPlan();
    if (piece instanceof HomeDoorOrWindow) {
      pieceDepth *= ((HomeDoorOrWindow)piece).getWallThickness();
    }
    // Scale icon to fit in its area
    float minDimension = Math.min(piece.getWidthInPlan(), pieceDepth);
    float iconScale = Math.min(1 / planScale, minDimension / icon.getIconHeight());
    // If piece model is mirrored, inverse x scale
    if (piece.isModelMirrored()) {
      g2D.scale(-iconScale, iconScale);
    } else {
      g2D.scale(iconScale, iconScale);
    }
    // Paint piece icon
    icon.paintIcon(this, g2D, -icon.getIconWidth() / 2, -icon.getIconHeight() / 2);
    // Revert g2D transformation to previous value
    g2D.setTransform(previousTransform);
    g2D.setClip(previousClip);
  }

  /**
   * Paints <code>piece</code> top icon with <code>g2D</code>.
   */
  private void paintPieceOfFurnitureTop(Graphics2D g2D, HomePieceOfFurniture piece,
                                        Shape pieceShape2D, BasicStroke pieceBorderStroke,
                                        float planScale,
                                        Color backgroundColor, Color foregroundColor,
                                        PaintMode paintMode) {
		if (this.furnitureTopViewIconKeys == null) {
			this.furnitureTopViewIconKeys = new WeakHashMap<HomePieceOfFurniture, HomePieceOfFurnitureTopViewIconKey>();
			this.furnitureTopViewIconsCache = new WeakHashMap<HomePieceOfFurnitureTopViewIconKey, PieceOfFurnitureTopViewIcon>();
		}
		HomePieceOfFurnitureTopViewIconKey topViewIconKey = this.furnitureTopViewIconKeys.get(piece);
		PieceOfFurnitureTopViewIcon icon;
		if (topViewIconKey == null) {
			topViewIconKey = new HomePieceOfFurnitureTopViewIconKey(piece.clone());
			icon = this.furnitureTopViewIconsCache.get(topViewIconKey);
    if (icon == null
        || icon.isWaitIcon()
           && paintMode != PaintMode.PAINT) {
      PlanComponent waitingComponent = paintMode == PaintMode.PAINT ? this : null;
      // Prefer use plan icon if it exists
      if (piece.getPlanIcon() != null) {
        icon = new PieceOfFurniturePlanIcon(piece, waitingComponent);
      } else {
        icon = new PieceOfFurnitureModelIcon(piece, this.object3dFactory, waitingComponent, this.preferences.getFurnitureModelIconSize());
      }
			this.furnitureTopViewIconsCache.put(topViewIconKey, icon);
		} else {
			// As furnitureTopViewIconKeys and furnitureTopViewIconsCache are both WeakHashMap instances,
			// use the HomePieceOfFurnitureTopViewIconKey instance that already exists in furnitureTopViewIconsCache
			// to avoid the deletion of the entry containing the new sibling when a piece is garbage collected
			for (HomePieceOfFurnitureTopViewIconKey key : furnitureTopViewIconsCache.keySet()) {
				if (key.equals(topViewIconKey)) {
					topViewIconKey = key;
					break;
				}
			}
		}
			this.furnitureTopViewIconKeys.put(piece, topViewIconKey);
		} else {
			icon = this.furnitureTopViewIconsCache.get(topViewIconKey);
    }

    if (icon.isWaitIcon() || icon.isErrorIcon()) {
      paintPieceOfFurnitureIcon(g2D, piece, icon, pieceShape2D, planScale, backgroundColor);
      g2D.setPaint(foregroundColor);
      g2D.setStroke(pieceBorderStroke);
      g2D.draw(pieceShape2D);
    } else {
      AffineTransform previousTransform = g2D.getTransform();
      // Translate to piece center
			final Rectangle2D bounds = pieceShape2D.getBounds2D();
      g2D.translate(bounds.getCenterX(), bounds.getCenterY());
      g2D.rotate(piece.getAngle());
      float pieceDepth = piece.getDepthInPlan();
      // Scale icon to fit in its area
      if (piece.isModelMirrored()
          && piece.getRoll() == 0) {
        // If piece model is mirrored when its roll rotation is 0, inverse x scale
        g2D.scale(-piece.getWidthInPlan() / icon.getIconWidth(), pieceDepth / icon.getIconHeight());
      } else {
        g2D.scale(piece.getWidthInPlan() / icon.getIconWidth(), pieceDepth / icon.getIconHeight());
      }
      // Paint piece icon
      icon.paintIcon(this, g2D, -icon.getIconWidth() / 2, -icon.getIconHeight() / 2);
      // Revert g2D transformation to previous value
      g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints rotation, elevation, height and resize indicators on <code>piece</code>.
   */
  private void paintPieceOFFurnitureIndicators(Graphics2D g2D,
                                               HomePieceOfFurniture piece,
                                               Paint indicatorPaint,
                                               float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);

      //AffineTransform previousTransform = g2D.getTransform();
      float [][] piecePoints = piece.getPoints();
      float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
      float pieceAngle = piece.getAngle();
      Shape rotationIndicator = getIndicator(piece, IndicatorType.ROTATE);
      if (rotationIndicator != null) {
      	AffineTransform previousTransform = g2D.getTransform();// force save
      	// Draw rotation indicator at top left point of the piece
        g2D.translate(piecePoints [0][0], piecePoints [0][1]);
        g2D.scale(scaleInverse, scaleInverse);
				g2D.rotate(pieceAngle);
			 	g2D.draw(rotationIndicator);
        g2D.setTransform(previousTransform);
      }

			AffineTransform previousTransform = g2D.getTransform();// force save
			Shape elevationIndicator = getIndicator(piece, IndicatorType.ELEVATE);
			if (elevationIndicator != null) {
				AffineTransform previousTransform2 = g2D.getTransform();
				// Draw elevation indicator at top right point of the piece
				g2D.translate(piecePoints[1][0], piecePoints[1][1]);
				g2D.scale(scaleInverse, scaleInverse);
				g2D.rotate(pieceAngle);
				g2D.draw(ELEVATION_POINT_INDICATOR);
				// Place elevation indicator farther but don't rotate it
				g2D.translate(6.5f , -6.5f);
				g2D.rotate(-pieceAngle);
				g2D.draw(elevationIndicator);
				g2D.setTransform(previousTransform2);
			}

			// Draw pitch, roll, light or height indicator at bottom left point of the piece
			g2D.translate(piecePoints [3][0], piecePoints [3][1]);
			g2D.scale(scaleInverse, scaleInverse);
			g2D.rotate(pieceAngle);
			if (piece.getPitch() != 0
							&& isFurnitureSizeInPlanSupported()) {
				Shape pitchIndicator = getIndicator(piece, IndicatorType.ROTATE_PITCH);
				if (pitchIndicator != null) {
					g2D.draw(pitchIndicator);
				}
			} else if (piece.getRoll() != 0
							&& isFurnitureSizeInPlanSupported()) {
				Shape rollIndicator = getIndicator(piece, IndicatorType.ROTATE_ROLL);
				if (rollIndicator != null) {
					g2D.draw(rollIndicator);
				}
			} else if (piece instanceof HomeLight) {
				Shape powerIndicator = getIndicator(piece, IndicatorType.CHANGE_POWER);
				if (powerIndicator != null) {
					g2D.draw(LIGHT_POWER_POINT_INDICATOR);
					// Place power indicator farther but don't rotate it
					g2D.translate(-7.5f, 7.5f);
					g2D.rotate(-pieceAngle);
					g2D.draw(powerIndicator);
				}
			} else if (piece.isResizable()
							&& !piece.isHorizontallyRotated()) {
				Shape heightIndicator = getIndicator(piece, IndicatorType.RESIZE_HEIGHT);
				if (heightIndicator != null) {
					g2D.draw(FURNITURE_HEIGHT_POINT_INDICATOR);
					// Place height indicator farther but don't rotate it
					g2D.translate(-7.5f, 7.5f);
					g2D.rotate(-pieceAngle);
					g2D.draw(heightIndicator);
				}
			}
			g2D.setTransform(previousTransform);

     	if (piece.isResizable()) {
        Shape resizeIndicator = getIndicator(piece, IndicatorType.RESIZE);
        if (resizeIndicator != null) {
        	AffineTransform previousTransform2 = g2D.getTransform();//force save
          // Draw resize indicator at top left point of the piece
          g2D.translate(piecePoints [2][0], piecePoints [2][1]);
          g2D.scale(scaleInverse, scaleInverse);
          g2D.rotate(pieceAngle);
					g2D.draw(resizeIndicator);
          g2D.setTransform(previousTransform2);
        }
      }

      if (piece.isNameVisible()
          && piece.getName().trim().length() > 0) {
        float xName = piece.getX() + piece.getNameXOffset();
        float yName = piece.getY() + piece.getNameYOffset();
        paintTextIndicators(g2D, piece.getClass(), getLineCount(piece.getName()),
            piece.getNameStyle(), xName, yName, piece.getNameAngle(), indicatorPaint, planScale);
      }
    }
  }

  /**
   * Paints polylines.
   */
  private void paintPolylines(Graphics2D g2D,
                              Collection<Polyline> polylines, List<Selectable> selectedItems,
                              Paint selectionOutlinePaint,
                              Paint indicatorPaint, float planScale,
                              Color foregroundColor, PaintMode paintMode) {
    // Draw polylines
    for (Polyline polyline : polylines) {
      if (isViewableAtSelectedLevel(polyline)) {
        boolean selected = selectedItems.contains(polyline);
        if (paintMode != PaintMode.CLIPBOARD
            || selected) {
          g2D.setPaint(new Color(polyline.getColor()));
          float thickness = polyline.getThickness();
          g2D.setStroke(ShapeTools.getStroke(thickness,
              polyline.getCapStyle(), polyline.getJoinStyle(), polyline.getDashPattern(), polyline.getDashOffset()));
					Shape polylineShape = ShapeTools.getPolylineShape(polyline.getPoints(),
									polyline.getJoinStyle() == Polyline.JoinStyle.CURVED, polyline.isClosedPath());
          g2D.draw(polylineShape);

          // Search angle at start and at end
          float [] firstPoint = null;
          float [] secondPoint = null;
          float [] beforeLastPoint = null;
          float [] lastPoint = null;
          for (PathIterator it = polylineShape.getPathIterator(null, 0.5); !it.isDone(); it.next()) {
            float [] pathPoint = new float [2];
            if (it.currentSegment(pathPoint) != PathIterator.SEG_CLOSE) {
              if (firstPoint == null) {
                firstPoint = pathPoint;
              } else if (secondPoint == null) {
                secondPoint = pathPoint;
              }
              beforeLastPoint = lastPoint;
              lastPoint = pathPoint;
            }
          }
          float angleAtStart = (float)Math.atan2(firstPoint [1] - secondPoint [1],
              firstPoint [0] - secondPoint [0]);
          float angleAtEnd = (float)Math.atan2(lastPoint [1] - beforeLastPoint [1],
              lastPoint [0] - beforeLastPoint [0]);
          float arrowDelta = polyline.getCapStyle() != Polyline.CapStyle.BUTT
              ? thickness / 2
              : 0;
          paintArrow(g2D, firstPoint, angleAtStart, polyline.getStartArrowStyle(), thickness, arrowDelta);
          paintArrow(g2D, lastPoint, angleAtEnd, polyline.getEndArrowStyle(), thickness, arrowDelta);

          if (selected
              && paintMode == PaintMode.PAINT) {
            g2D.setPaint(selectionOutlinePaint);
            g2D.setStroke(SwingTools.getStroke(thickness + 4 / planScale,
                polyline.getCapStyle(), polyline.getJoinStyle(), Polyline.DashStyle.SOLID));
            g2D.draw(polylineShape);

            // Paint resize indicators of the polyline if indicator paint exists
            if (selectedItems.size() == 1
                && indicatorPaint != null) {
              Polyline selectedPolyline = (Polyline)selectedItems.get(0);
              if (isViewableAtSelectedLevel(selectedPolyline)) {
                g2D.setPaint(indicatorPaint);
                paintPointsResizeIndicators(g2D, selectedPolyline, indicatorPaint, planScale,
                    selectedPolyline.isClosedPath(), angleAtStart, angleAtEnd, false);
              }
            }
          }
        }
      }
    }
  }

  /**
   * Paints polyline arrow at the given point and orientation.
   */
  private void paintArrow(Graphics2D g2D, float [] point, float angle,
                          Polyline.ArrowStyle arrowStyle, float thickness, float arrowDelta) {
    if (arrowStyle != null
        && arrowStyle != Polyline.ArrowStyle.NONE) {
      AffineTransform oldTransform = g2D.getTransform();
      g2D.translate(point [0], point [1]);
      g2D.rotate(angle);
      g2D.translate(arrowDelta, 0);
      double scale = Math.pow(thickness, 0.66f) * 2;
      g2D.scale(scale, scale);
      switch (arrowStyle) {
        case DISC :
          g2D.fill(new Ellipse2D.Float(-3.5f, -2, 4, 4));
          break;
        case OPEN :
          g2D.scale(0.9, 0.9);
          g2D.setStroke(new BasicStroke((float)(thickness / scale / 0.9), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER));
          g2D.draw(ARROW);
          break;
        case DELTA :
          g2D.translate(1.65f, 0);
          g2D.fill(ARROW);
          break;
        default:
          break;
      }
      g2D.setTransform(oldTransform);
    }
  }

  /**
   * Paints dimension lines.
   */
  private void paintDimensionLines(Graphics2D g2D,
                          Collection<DimensionLine> dimensionLines, List<Selectable> selectedItems,
                          Paint selectionOutlinePaint, Stroke selectionOutlineStroke,
                          Paint indicatorPaint, Stroke extensionLineStroke, float planScale,
                          Color backgroundColor, Color foregroundColor,
                          PaintMode paintMode, boolean feedback) {
    // In clipboard paint mode, paint only selected dimension lines
    if (paintMode == PaintMode.CLIPBOARD) {
      dimensionLines = Home.getDimensionLinesSubList(selectedItems);
    }

    // Draw dimension lines
    g2D.setPaint(foregroundColor);
    BasicStroke dimensionLineStroke = new BasicStroke(getStrokeWidth(DimensionLine.class, paintMode) / planScale);
    // Change font size
    Font previousFont = g2D.getFont();
    for (DimensionLine dimensionLine : dimensionLines) {
      if (isViewableAtSelectedLevel(dimensionLine)) {
        AffineTransform previousTransform = g2D.getTransform();
        double angle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(),
            dimensionLine.getXEnd() - dimensionLine.getXStart());
        float dimensionLineLength = dimensionLine.getLength();
        g2D.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
        g2D.rotate(angle);
        g2D.translate(0, dimensionLine.getOffset());

        if (paintMode == PaintMode.PAINT
            && this.selectedItemsOutlinePainted
            && selectedItems.contains(dimensionLine)) {
          // Draw selection border
          g2D.setPaint(selectionOutlinePaint);
          g2D.setStroke(selectionOutlineStroke);
          // Draw dimension line
          g2D.draw(new Line2D.Float(0, 0, dimensionLineLength, 0));
          // Draw dimension line ends
          g2D.draw(DIMENSION_LINE_END);
          g2D.translate(dimensionLineLength, 0);
          g2D.draw(DIMENSION_LINE_END);
          g2D.translate(-dimensionLineLength, 0);
          // Draw extension lines
          g2D.draw(new Line2D.Float(0, -dimensionLine.getOffset(), 0, -5));
          g2D.draw(new Line2D.Float(dimensionLineLength, -dimensionLine.getOffset(), dimensionLineLength, -5));

          g2D.setPaint(foregroundColor);
        }

        g2D.setStroke(dimensionLineStroke);
        // Draw dimension line
        g2D.draw(new Line2D.Float(0, 0, dimensionLineLength, 0));
        // Draw dimension line ends
        g2D.draw(DIMENSION_LINE_END);
        g2D.translate(dimensionLineLength, 0);
        g2D.draw(DIMENSION_LINE_END);
        g2D.translate(-dimensionLineLength, 0);
        // Draw extension lines
        g2D.setStroke(extensionLineStroke);
        g2D.draw(new Line2D.Float(0, -dimensionLine.getOffset(), 0, -5));
        g2D.draw(new Line2D.Float(dimensionLineLength, -dimensionLine.getOffset(), dimensionLineLength, -5));

        String lengthText = this.preferences.getLengthUnit().getFormat().format(dimensionLineLength);
        TextStyle lengthStyle = dimensionLine.getLengthStyle();
        if (lengthStyle == null) {
          lengthStyle = this.preferences.getDefaultTextStyle(dimensionLine.getClass());
        }
        if (feedback && getFont() != null) {
          // Use default for feedback
          lengthStyle = lengthStyle.deriveStyle( 9 * fatFingerScaleUp / getScale());//getFont().getSize() / getScale());
        }
        Font font = getFont(previousFont, lengthStyle);
        FontMetrics lengthFontMetrics = getFontMetrics(font, lengthStyle);
        Rectangle2D lengthTextBounds = font.getStringBounds(lengthText);
        int fontAscent = (int)lengthFontMetrics.ascent;
				//PJ the ascent in fontMetrics is already -ve so -fontMetrics.ascent
        g2D.translate((dimensionLineLength - (float)lengthTextBounds.getWidth()) / 2,
            dimensionLine.getOffset() <= 0
                ? lengthFontMetrics.descent- 1
                : -fontAscent + 1);
        if (feedback) {
					// feedback outline thingy disabled for now, but should be put back
          // Draw text outline with half transparent background color
          /*g2D.setPaint(backgroundColor);
          Composite oldComposite = setTransparency(g2D, 0.7f);
          g2D.setStroke(new BasicStroke(3 / planScale));
          FontRenderContext fontRenderContext = g2D.getFontRenderContext();
          TextLayout textLayout = new TextLayout(lengthText, font, fontRenderContext);
          g2D.draw(textLayout.getOutline(new AffineTransform()));
          g2D.setComposite(oldComposite);
          g2D.setPaint(foregroundColor);*/
        }
        // Draw dimension length in middle
        g2D.setFont(font);
        g2D.drawString(lengthText, 0, 0);

        g2D.setTransform(previousTransform);
      }
    }
    g2D.setFont(previousFont);
    // Paint resize indicator of selected dimension line
    if (selectedItems.size() == 1
        && selectedItems.get(0) instanceof DimensionLine
        && paintMode == PaintMode.PAINT
        && indicatorPaint != null) {
      paintDimensionLineResizeIndicator(g2D, (DimensionLine)selectedItems.get(0), indicatorPaint, planScale);
    }
  }

  /**
   * Paints resize indicator on a given dimension line.
   */
  private void paintDimensionLineResizeIndicator(Graphics2D g2D, DimensionLine dimensionLine,
                                                 Paint indicatorPaint,
                                                 float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);

      double wallAngle = Math.atan2(dimensionLine.getYEnd() - dimensionLine.getYStart(),
          dimensionLine.getXEnd() - dimensionLine.getXStart());

      AffineTransform previousTransform = g2D.getTransform();
      float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
      // Draw resize indicator at the start of dimension line
      g2D.translate(dimensionLine.getXStart(), dimensionLine.getYStart());
      g2D.rotate(wallAngle);
      g2D.translate(0, dimensionLine.getOffset());
      g2D.rotate(Math.PI);
      g2D.scale(scaleInverse, scaleInverse);
      Shape resizeIndicator = getIndicator(dimensionLine, IndicatorType.RESIZE);
			g2D.draw(resizeIndicator);
      g2D.setTransform(previousTransform);
			AffineTransform previousTransform2 = g2D.getTransform();//force save
      // Draw resize indicator at the end of dimension line
      g2D.translate(dimensionLine.getXEnd(), dimensionLine.getYEnd());
      g2D.rotate(wallAngle);
      g2D.translate(0, dimensionLine.getOffset());
      g2D.scale(scaleInverse, scaleInverse);
			g2D.draw(resizeIndicator);
      g2D.setTransform(previousTransform2);
			AffineTransform previousTransform3 = g2D.getTransform();//force save
      // Draw resize indicator at the middle of dimension line
      g2D.translate((dimensionLine.getXStart() + dimensionLine.getXEnd()) / 2,
          (dimensionLine.getYStart() + dimensionLine.getYEnd()) / 2);
      g2D.rotate(wallAngle);
      g2D.translate(0, dimensionLine.getOffset());
      g2D.rotate(dimensionLine.getOffset() <= 0
          ? Math.PI / 2
          : -Math.PI / 2);
      g2D.scale(scaleInverse, scaleInverse);
			g2D.draw(resizeIndicator);
      g2D.setTransform(previousTransform3);
    }
  }

  /**
   * Paints home labels.
   */
  private void paintLabels(Graphics2D g2D, Collection<Label> labels, List<Selectable> selectedItems,
                           Paint selectionOutlinePaint, Stroke selectionOutlineStroke, Paint indicatorPaint,
                           float planScale, Color foregroundColor, PaintMode paintMode) {
    Font previousFont = g2D.getFont();
    // Draw labels
    for (Label label : labels) {
      if (isViewableAtSelectedLevel(label)) {
        boolean selectedLabel = selectedItems.contains(label);
        // In clipboard paint mode, paint label only if it is selected
        if (paintMode != PaintMode.CLIPBOARD || selectedLabel) {
          String labelText = label.getText();
          float xLabel = label.getX();
          float yLabel = label.getY();
          float labelAngle = label.getAngle();
          TextStyle labelStyle = label.getStyle();
          if (labelStyle == null) {
            labelStyle = this.preferences.getDefaultTextStyle(label.getClass());
          }
          if (labelStyle.getFontName() == null && getFont() != null) {
			  		// there are basically no fonts on android, look it up
            labelStyle = labelStyle.deriveStyle("Roboto");//getFont().getFontName());
          }
          Integer color = label.getColor();
          g2D.setPaint(color != null ?  new Color(color) : foregroundColor);
          paintText(g2D, label.getClass(), labelText, labelStyle, label.getOutlineColor(),
              xLabel, yLabel, labelAngle, previousFont);

          if (paintMode == PaintMode.PAINT && this.selectedItemsOutlinePainted && selectedLabel) {
            // Draw selection border
            g2D.setPaint(selectionOutlinePaint);
            g2D.setStroke(selectionOutlineStroke);
            float [][] textBounds = getTextBounds(labelText, labelStyle, xLabel, yLabel, labelAngle);
            g2D.draw(ShapeTools.getShape(textBounds, true, null));
            g2D.setPaint(foregroundColor);
            if (indicatorPaint != null
                && selectedItems.size() == 1
                && selectedItems.get(0) == label) {
              paintTextIndicators(g2D, label.getClass(), getLineCount(labelText),
                  labelStyle, xLabel, yLabel, labelAngle, indicatorPaint, planScale);

              if (this.resizeIndicatorVisible
                  && label.getPitch() != null) {
                Shape elevationIndicator = getIndicator(label, IndicatorType.ELEVATE);
                if (elevationIndicator != null) {
                  AffineTransform previousTransform = g2D.getTransform();
                  // Draw elevation indicator bellow rotation center
									if (labelStyle.getAlignment() == TextStyle.Alignment.LEFT) {
										g2D.translate(textBounds [3][0], textBounds [3][1]);
									} else if (labelStyle.getAlignment() == TextStyle.Alignment.RIGHT) {
										g2D.translate(textBounds [2][0], textBounds [2][1]);
									} else { // CENTER
										g2D.translate((textBounds[2][0] + textBounds[3][0]) / 2, (textBounds[2][1] + textBounds[3][1]) / 2);
									}
                  float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
                  g2D.scale(scaleInverse, scaleInverse);
                  g2D.rotate(label.getAngle());
                  g2D.draw(ELEVATION_POINT_INDICATOR);
                  // Place elevation indicator farther but don't rotate it
                  g2D.translate(0, 10f);
                  g2D.rotate(-label.getAngle());
                  g2D.draw(elevationIndicator);
                  g2D.setTransform(previousTransform);
                }
              }
            }
          }
        }
      }
    }
    g2D.setFont(previousFont);
  }

  /**
   * Paints the compass.
   */
  private void paintCompass(Graphics2D g2D, List<Selectable> selectedItems, float planScale,
                            Color foregroundColor, PaintMode paintMode) {
    Compass compass = this.home.getCompass();
    if (compass.isVisible()
        && (paintMode != PaintMode.CLIPBOARD
            || selectedItems.contains(compass))) {
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(compass.getX(), compass.getY());
      g2D.rotate(compass.getNorthDirection());
      float diameter = compass.getDiameter();
      g2D.scale(diameter, diameter);
      g2D.setColor(foregroundColor);
      g2D.fill(COMPASS);
	  	g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints the outline of the compass when it's belongs to <code>items</code>.
   */
  private void paintCompassOutline(Graphics2D g2D, List<Selectable> items,
                                   Paint selectionOutlinePaint, Stroke selectionOutlineStroke,
                                   Paint indicatorPaint, float planScale, Color foregroundColor) {
    Compass compass = this.home.getCompass();
    if (items.contains(compass)
        && compass.isVisible()) {
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(compass.getX(), compass.getY());
      g2D.rotate(compass.getNorthDirection());
      float diameter = compass.getDiameter();
      g2D.scale(diameter, diameter);

      g2D.setPaint(selectionOutlinePaint);
      g2D.setStroke(new BasicStroke((5.5f + planScale) / diameter / planScale * this.resolutionScale));
      g2D.draw(COMPASS_DISC);
      g2D.setColor(foregroundColor);
      g2D.setStroke(new BasicStroke(1f / diameter / planScale * this.resolutionScale));
      g2D.draw(COMPASS_DISC);
      g2D.setTransform(previousTransform);

      // Paint indicators of the compass
      if (items.size() == 1
          && items.get(0) == compass) {
        g2D.setPaint(indicatorPaint);
        paintCompassIndicators(g2D, compass, indicatorPaint, planScale);
      }
    }
  }

  private void paintCompassIndicators(Graphics2D g2D,
                                      Compass compass, Paint indicatorPaint,
                                      float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);

      AffineTransform previousTransform = g2D.getTransform();
      // Draw rotation indicator at middle of second and third point of compass
      float [][] compassPoints = compass.getPoints();
      float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
      g2D.translate((compassPoints [2][0] + compassPoints [3][0]) / 2,
          (compassPoints [2][1] + compassPoints [3][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(compass.getNorthDirection());
      g2D.draw(getIndicator(compass, IndicatorType.ROTATE));
      g2D.setTransform(previousTransform);
			AffineTransform previousTransform2 = g2D.getTransform();//force save
      // Draw resize indicator at middle of second and third point of compass
      g2D.translate((compassPoints [1][0] + compassPoints [2][0]) / 2,
          (compassPoints [1][1] + compassPoints [2][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(compass.getNorthDirection());
      g2D.draw(getIndicator(compass, IndicatorType.RESIZE));
      g2D.setTransform(previousTransform2);
    }
  }

  /**
   * Paints wall location feedback.
   */
  private void paintWallAlignmentFeedback(Graphics2D g2D,
                                          Wall alignedWall, Point2D locationFeedback,
                                          boolean showPointFeedback,
                                          Paint feedbackPaint, Stroke feedbackStroke,
                                          float planScale, Paint pointPaint,
                                          Stroke pointStroke) {
    // Paint wall location feedback
    if (locationFeedback != null) {
      float margin = 0.5f / planScale;
      // Search which wall start or end point is at locationFeedback abscissa or ordinate
      // ignoring the start and end point of alignedWall
      float x = (float)locationFeedback.getX();
      float y = (float)locationFeedback.getY();
      float deltaXToClosestWall = Float.POSITIVE_INFINITY;
      float deltaYToClosestWall = Float.POSITIVE_INFINITY;
      for (Wall wall : getViewedItems(this.home.getWalls(), this.otherLevelsWallsCache)) {
        if (wall != alignedWall) {
          if (Math.abs(x - wall.getXStart()) < margin
              && (alignedWall == null
                  || !equalsWallPoint(wall.getXStart(), wall.getYStart(), alignedWall))) {
            if (Math.abs(deltaYToClosestWall) > Math.abs(y - wall.getYStart())) {
              deltaYToClosestWall = y - wall.getYStart();
            }
          } else if (Math.abs(x - wall.getXEnd()) < margin
                    && (alignedWall == null
                        || !equalsWallPoint(wall.getXEnd(), wall.getYEnd(), alignedWall))) {
            if (Math.abs(deltaYToClosestWall) > Math.abs(y - wall.getYEnd())) {
              deltaYToClosestWall = y - wall.getYEnd();
            }
          }

          if (Math.abs(y - wall.getYStart()) < margin
              && (alignedWall == null
                  || !equalsWallPoint(wall.getXStart(), wall.getYStart(), alignedWall))) {
            if (Math.abs(deltaXToClosestWall) > Math.abs(x - wall.getXStart())) {
              deltaXToClosestWall = x - wall.getXStart();
            }
          } else if (Math.abs(y - wall.getYEnd()) < margin
                    && (alignedWall == null
                        || !equalsWallPoint(wall.getXEnd(), wall.getYEnd(), alignedWall))) {
            if (Math.abs(deltaXToClosestWall) > Math.abs(x - wall.getXEnd())) {
              deltaXToClosestWall = x - wall.getXEnd();
            }
          }

          float [][] wallPoints = wall.getPoints();
          // Take into account only points at start and end of the wall
          wallPoints = new float [][] {wallPoints [0], wallPoints [wallPoints.length / 2 - 1],
                                       wallPoints [wallPoints.length / 2], wallPoints [wallPoints.length - 1]};
          for (int i = 0; i < wallPoints.length; i++) {
            if (Math.abs(x - wallPoints [i][0]) < margin
                && (alignedWall == null
                    || !equalsWallPoint(wallPoints [i][0], wallPoints [i][1], alignedWall))) {
              if (Math.abs(deltaYToClosestWall) > Math.abs(y - wallPoints [i][1])) {
                deltaYToClosestWall = y - wallPoints [i][1];
              }
            }
            if (Math.abs(y - wallPoints [i][1]) < margin
                && (alignedWall == null
                    || !equalsWallPoint(wallPoints [i][0], wallPoints [i][1], alignedWall))) {
              if (Math.abs(deltaXToClosestWall) > Math.abs(x - wallPoints [i][0])) {
                deltaXToClosestWall = x - wallPoints [i][0];
              }
            }
          }
        }
      }

      // Draw alignment horizontal and vertical lines
      g2D.setPaint(feedbackPaint);
      g2D.setStroke(feedbackStroke);
      if (deltaXToClosestWall != Float.POSITIVE_INFINITY) {
        if (deltaXToClosestWall > 0) {
          g2D.draw(new Line2D.Float(x + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y,
              x - deltaXToClosestWall - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y));
        } else {
          g2D.draw(new Line2D.Float(x - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y,
              x - deltaXToClosestWall + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y));
        }
      }

      if (deltaYToClosestWall != Float.POSITIVE_INFINITY) {
        if (deltaYToClosestWall > 0) {
          g2D.draw(new Line2D.Float(x, y + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale,
              x, y - deltaYToClosestWall - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale));
        } else {
          g2D.draw(new Line2D.Float(x, y - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale,
              x, y - deltaYToClosestWall + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale));
        }
      }

      // Draw point feedback
      if (showPointFeedback) {
        paintPointFeedback(g2D, locationFeedback, feedbackPaint, planScale, pointPaint, pointStroke);
      }
    }
  }

  /**
   * Returns the items viewed in the plan at the selected level.
   */
  private <T extends Elevatable> Collection<T> getViewedItems(Collection<T> homeItems, List<T> otherLevelItems) {
    List<T> viewedWalls = new ArrayList<T>();
    if (otherLevelItems != null) {
      viewedWalls.addAll(otherLevelItems);
    }
    for (T wall : homeItems) {
      if (isViewableAtSelectedLevel(wall)) {
        viewedWalls.add(wall);
      }
    }
    return viewedWalls;
  }

  /**
   * Paints point feedback.
   */
  private void paintPointFeedback(Graphics2D g2D, Point2D locationFeedback,
                                  Paint feedbackPaint, float planScale,
                                  Paint pointPaint, Stroke pointStroke) {
    g2D.setPaint(pointPaint);
    g2D.setStroke(pointStroke);
    float radius = 10 * fatFingerScaleUp;
    g2D.draw(new Ellipse2D.Float((float)locationFeedback.getX() - radius / planScale,
        (float)locationFeedback.getY() - radius / planScale, 2 * radius / planScale, 2 * radius / planScale));
    g2D.setPaint(feedbackPaint);
    g2D.setStroke(new BasicStroke(1 * fatFingerScaleUp / planScale * this.resolutionScale));
    g2D.draw(new Line2D.Float((float)locationFeedback.getX(),
        (float)locationFeedback.getY() - radius / planScale,
        (float)locationFeedback.getX(),
        (float)locationFeedback.getY() + radius  / planScale));
    g2D.draw(new Line2D.Float((float)locationFeedback.getX() - radius  / planScale,
        (float)locationFeedback.getY(),
        (float)locationFeedback.getX() + radius  / planScale,
        (float)locationFeedback.getY()));
  }

  /**
   * Returns <code>true</code> if <code>wall</code> start or end point
   * equals the point (<code>x</code>, <code>y</code>).
   */
  private boolean equalsWallPoint(float x, float y, Wall wall) {
    return x == wall.getXStart() && y == wall.getYStart()
           || x == wall.getXEnd() && y == wall.getYEnd();
  }

  /**
   * Paints room location feedback.
   */
  private void paintRoomAlignmentFeedback(Graphics2D g2D,
                                          Room alignedRoom, Point2D locationFeedback,
                                          boolean showPointFeedback,
                                          Paint feedbackPaint, Stroke feedbackStroke,
                                          float planScale, Paint pointPaint,
                                          Stroke pointStroke) {
    // Paint room location feedback
    if (locationFeedback != null) {
      float margin = 0.5f / planScale;
      // Search which room points are at locationFeedback abscissa or ordinate
      float x = (float)locationFeedback.getX();
      float y = (float)locationFeedback.getY();
      float deltaXToClosestObject = Float.POSITIVE_INFINITY;
      float deltaYToClosestObject = Float.POSITIVE_INFINITY;
      for (Room room : getViewedItems(this.home.getRooms(), this.otherLevelsRoomsCache)) {
        float [][] roomPoints = room.getPoints();
        int editedPointIndex = -1;
        if (room == alignedRoom) {
          // Search which room point could match location feedback
          for (int i = 0; i < roomPoints.length; i++) {
            if (roomPoints [i][0] == x && roomPoints [i][1] == y) {
              editedPointIndex = i;
              break;
            }
          }
        }
        for (int i = 0; i < roomPoints.length; i++) {
          if (editedPointIndex == -1 || (i != editedPointIndex && roomPoints.length > 2)) {
            if (Math.abs(x - roomPoints [i][0]) < margin
                && Math.abs(deltaYToClosestObject) > Math.abs(y - roomPoints [i][1])) {
              deltaYToClosestObject = y - roomPoints [i][1];
            }
            if (Math.abs(y - roomPoints [i][1]) < margin
                && Math.abs(deltaXToClosestObject) > Math.abs(x - roomPoints [i][0])) {
              deltaXToClosestObject = x - roomPoints [i][0];
            }
          }
        }
      }
      // Search which wall points are at locationFeedback abscissa or ordinate
      for (Wall wall : getViewedItems(this.home.getWalls(), this.otherLevelsWallsCache)) {
        float [][] wallPoints = wall.getPoints();
        // Take into account only points at start and end of the wall
        wallPoints = new float [][] {wallPoints [0], wallPoints [wallPoints.length / 2 - 1],
                                     wallPoints [wallPoints.length / 2], wallPoints [wallPoints.length - 1]};
        for (int i = 0; i < wallPoints.length; i++) {
          if (Math.abs(x - wallPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - wallPoints [i][1])) {
            deltaYToClosestObject = y - wallPoints [i][1];
          }
          if (Math.abs(y - wallPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - wallPoints [i][0])) {
            deltaXToClosestObject = x - wallPoints [i][0];
          }
        }
      }

      // Draw alignment horizontal and vertical lines
      g2D.setPaint(feedbackPaint);
      g2D.setStroke(feedbackStroke);
      if (deltaXToClosestObject != Float.POSITIVE_INFINITY) {
        if (deltaXToClosestObject > 0) {
          g2D.draw(new Line2D.Float(x + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y,
              x - deltaXToClosestObject - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y));
        } else {
          g2D.draw(new Line2D.Float(x - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y,
              x - deltaXToClosestObject + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y));
        }
      }

      if (deltaYToClosestObject != Float.POSITIVE_INFINITY) {
        if (deltaYToClosestObject > 0) {
          g2D.draw(new Line2D.Float(x, y + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale,
              x, y - deltaYToClosestObject - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale));
        } else {
          g2D.draw(new Line2D.Float(x, y - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale,
              x, y - deltaYToClosestObject + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale));
        }
      }

      if (showPointFeedback) {
        paintPointFeedback(g2D, locationFeedback, feedbackPaint, planScale, pointPaint, pointStroke);
      }
    }
  }

  /**
   * Paints dimension line location feedback.
   */
  private void paintDimensionLineAlignmentFeedback(Graphics2D g2D,
                                                   DimensionLine alignedDimensionLine, Point2D locationFeedback,
                                                   boolean showPointFeedback,
                                                   Paint feedbackPaint, Stroke feedbackStroke,
                                                   float planScale, Paint pointPaint,
                                                   Stroke pointStroke) {
    // Paint dimension line location feedback
    if (locationFeedback != null) {
      float margin = 0.5f / planScale;
      // Search which room points are at locationFeedback abscissa or ordinate
      float x = (float)locationFeedback.getX();
      float y = (float)locationFeedback.getY();
      float deltaXToClosestObject = Float.POSITIVE_INFINITY;
      float deltaYToClosestObject = Float.POSITIVE_INFINITY;
      for (Room room : getViewedItems(this.home.getRooms(), this.otherLevelsRoomsCache)) {
        float [][] roomPoints = room.getPoints();
        for (int i = 0; i < roomPoints.length; i++) {
          if (Math.abs(x - roomPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - roomPoints [i][1])) {
            deltaYToClosestObject = y - roomPoints [i][1];
          }
          if (Math.abs(y - roomPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - roomPoints [i][0])) {
            deltaXToClosestObject = x - roomPoints [i][0];
          }
        }
      }
      // Search which dimension line start or end point is at locationFeedback abscissa or ordinate
      // ignoring the start and end point of alignedDimensionLine
      for (DimensionLine dimensionLine : this.home.getDimensionLines()) {
        if (isViewableAtSelectedLevel(dimensionLine)
            && dimensionLine != alignedDimensionLine) {
          if (Math.abs(x - dimensionLine.getXStart()) < margin
              && (alignedDimensionLine == null
                  || !equalsDimensionLinePoint(dimensionLine.getXStart(), dimensionLine.getYStart(),
                          alignedDimensionLine))) {
            if (Math.abs(deltaYToClosestObject) > Math.abs(y - dimensionLine.getYStart())) {
              deltaYToClosestObject = y - dimensionLine.getYStart();
            }
          } else if (Math.abs(x - dimensionLine.getXEnd()) < margin
                    && (alignedDimensionLine == null
                        || !equalsDimensionLinePoint(dimensionLine.getXEnd(), dimensionLine.getYEnd(),
                                alignedDimensionLine))) {
            if (Math.abs(deltaYToClosestObject) > Math.abs(y - dimensionLine.getYEnd())) {
              deltaYToClosestObject = y - dimensionLine.getYEnd();
            }
          }
          if (Math.abs(y - dimensionLine.getYStart()) < margin
              && (alignedDimensionLine == null
                  || !equalsDimensionLinePoint(dimensionLine.getXStart(), dimensionLine.getYStart(),
                          alignedDimensionLine))) {
            if (Math.abs(deltaXToClosestObject) > Math.abs(x - dimensionLine.getXStart())) {
              deltaXToClosestObject = x - dimensionLine.getXStart();
            }
          } else if (Math.abs(y - dimensionLine.getYEnd()) < margin
                    && (alignedDimensionLine == null
                        || !equalsDimensionLinePoint(dimensionLine.getXEnd(), dimensionLine.getYEnd(),
                                alignedDimensionLine))) {
            if (Math.abs(deltaXToClosestObject) > Math.abs(x - dimensionLine.getXEnd())) {
              deltaXToClosestObject = x - dimensionLine.getXEnd();
            }
          }
        }
      }
      // Search which wall points are at locationFeedback abscissa or ordinate
      for (Wall wall : getViewedItems(this.home.getWalls(), this.otherLevelsWallsCache)) {
        float [][] wallPoints = wall.getPoints();
        // Take into account only points at start and end of the wall
        wallPoints = new float [][] {wallPoints [0], wallPoints [wallPoints.length / 2 - 1],
                                     wallPoints [wallPoints.length / 2], wallPoints [wallPoints.length - 1]};
        for (int i = 0; i < wallPoints.length; i++) {
          if (Math.abs(x - wallPoints [i][0]) < margin
              && Math.abs(deltaYToClosestObject) > Math.abs(y - wallPoints [i][1])) {
            deltaYToClosestObject = y - wallPoints [i][1];
          }
          if (Math.abs(y - wallPoints [i][1]) < margin
              && Math.abs(deltaXToClosestObject) > Math.abs(x - wallPoints [i][0])) {
            deltaXToClosestObject = x - wallPoints [i][0];
          }
        }
      }
      // Search which piece of furniture points are at locationFeedback abscissa or ordinate
      for (HomePieceOfFurniture piece : this.home.getFurniture()) {
        if (piece.isVisible()
            && isViewableAtSelectedLevel(piece)) {
          float [][] piecePoints = piece.getPoints();
          for (int i = 0; i < piecePoints.length; i++) {
            if (Math.abs(x - piecePoints [i][0]) < margin
                && Math.abs(deltaYToClosestObject) > Math.abs(y - piecePoints [i][1])) {
              deltaYToClosestObject = y - piecePoints [i][1];
            }
            if (Math.abs(y - piecePoints [i][1]) < margin
                && Math.abs(deltaXToClosestObject) > Math.abs(x - piecePoints [i][0])) {
              deltaXToClosestObject = x - piecePoints [i][0];
            }

          }
        }
      }

      // Draw alignment horizontal and vertical lines
      g2D.setPaint(feedbackPaint);
      g2D.setStroke(feedbackStroke);
      if (deltaXToClosestObject != Float.POSITIVE_INFINITY) {
        if (deltaXToClosestObject > 0) {
          g2D.draw(new Line2D.Float(x + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y,
              x - deltaXToClosestObject - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y));
        } else {
          g2D.draw(new Line2D.Float(x - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y,
              x - deltaXToClosestObject + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale, y));
        }
      }

      if (deltaYToClosestObject != Float.POSITIVE_INFINITY) {
        if (deltaYToClosestObject > 0) {
          g2D.draw(new Line2D.Float(x, y + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale,
              x, y - deltaYToClosestObject - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale));
        } else {
          g2D.draw(new Line2D.Float(x, y - ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale,
              x, y - deltaYToClosestObject + ALIGNMENT_LINE_OFFSET * fatFingerScaleUp / planScale));
        }
      }

      if (showPointFeedback) {
        paintPointFeedback(g2D, locationFeedback, feedbackPaint, planScale, pointPaint, pointStroke);
      }
    }
  }

  /**
   * Returns <code>true</code> if <code>dimensionLine</code> start or end point
   * equals the point (<code>x</code>, <code>y</code>).
   */
  private boolean equalsDimensionLinePoint(float x, float y, DimensionLine dimensionLine) {
    return x == dimensionLine.getXStart() && y == dimensionLine.getYStart()
           || x == dimensionLine.getXEnd() && y == dimensionLine.getYEnd();
  }

  /**
   * Paints an arc centered at <code>center</code> point that goes
   */
  private void paintAngleFeedback(Graphics2D g2D, Point2D center,
                                  Point2D point1, Point2D point2,
                                  float planScale, Color selectionColor) {
    if (!point1.equals(center) && !point2.equals(center)) {
      g2D.setColor(selectionColor);
      g2D.setStroke(new BasicStroke(1 * fatFingerScaleUp / planScale * this.resolutionScale));
      // Compute angles
      double angle1 = Math.atan2(center.getY() - point1.getY(), point1.getX() - center.getX());
      if (angle1 < 0) {
        angle1 = 2 * Math.PI + angle1;
      }
      double angle2 = Math.atan2(center.getY() - point2.getY(), point2.getX() - center.getX());
      if (angle2 < 0) {
        angle2 = 2 * Math.PI + angle2;
      }
      double extent = angle2 - angle1;
      if (angle1 > angle2) {
        extent = 2 * Math.PI + extent;
      }
      AffineTransform previousTransform = g2D.getTransform();
      // Draw an arc
      g2D.translate(center.getX(), center.getY());
      float radius = 20 * fatFingerScaleUp / planScale;
      g2D.draw(new Arc2D.Double(-radius, -radius,
          radius * 2, radius * 2, Math.toDegrees(angle1), Math.toDegrees(extent), Arc2D.OPEN));
      // Draw two radius
      radius += 5 * fatFingerScaleUp / planScale;
      g2D.draw(new Line2D.Double(0, 0, radius * Math.cos(angle1), -radius * Math.sin(angle1)));
      g2D.draw(new Line2D.Double(0, 0, radius * Math.cos(angle1 + extent), -radius * Math.sin(angle1 + extent)));
      g2D.setTransform(previousTransform);
    }
  }

  /**
   * Paints the observer camera at its current location, if home camera is the observer camera.
   */
  private void paintCamera(Graphics2D g2D, List<Selectable> selectedItems,
                           Paint selectionOutlinePaint, Stroke selectionOutlineStroke,
                           Paint indicatorPaint, float planScale,
                           Color backgroundColor, Color foregroundColor) {
    ObserverCamera camera = this.home.getObserverCamera();
    if (camera == this.home.getCamera()) {
      AffineTransform previousTransform = g2D.getTransform();
      g2D.translate(camera.getX(), camera.getY());
      g2D.rotate(camera.getYaw());

      // Compute camera drawing at scale
      float [][] points = camera.getPoints();
      double yScale = Point2D.distance(points [0][0], points [0][1], points [3][0], points [3][1]);
      double xScale = Point2D.distance(points [0][0], points [0][1], points [1][0], points [1][1]);
      AffineTransform cameraTransform = AffineTransform.getScaleInstance(xScale, yScale);
      Shape scaledCameraBody =
          new Area(CAMERA_BODY).createTransformedArea(cameraTransform);
      Shape scaledCameraHead =
          new Area(CAMERA_HEAD).createTransformedArea(cameraTransform);

      // Paint body
      g2D.setPaint(backgroundColor);
      g2D.fill(scaledCameraBody);
      g2D.setPaint(foregroundColor);
      BasicStroke stroke = new BasicStroke(getStrokeWidth(ObserverCamera.class, PaintMode.PAINT) / planScale);
      g2D.setStroke(stroke);
      g2D.draw(scaledCameraBody);

      if (selectedItems.contains(camera)
          && this.selectedItemsOutlinePainted) {
        g2D.setPaint(selectionOutlinePaint);
        g2D.setStroke(selectionOutlineStroke);
        Area cameraOutline = new Area(scaledCameraBody);
        cameraOutline.add(new Area(scaledCameraHead));
        g2D.draw(cameraOutline);
      }

      // Paint head
      g2D.setPaint(backgroundColor);
      g2D.fill(scaledCameraHead);
      g2D.setPaint(foregroundColor);
      g2D.setStroke(stroke);
      g2D.draw(scaledCameraHead);
      // Paint field of sight angle
      double sin = (float)Math.sin(camera.getFieldOfView() / 2);
      double cos = (float)Math.cos(camera.getFieldOfView() / 2);
      float xStartAngle = (float)(0.9f * yScale * sin);
      float yStartAngle = (float)(0.9f * yScale * cos);
      float xEndAngle = (float)(2.2f * yScale * sin);
      float yEndAngle = (float)(2.2f * yScale * cos);
      GeneralPath cameraFieldOfViewAngle = new GeneralPath();
      cameraFieldOfViewAngle.moveTo(xStartAngle, yStartAngle);
      cameraFieldOfViewAngle.lineTo(xEndAngle, yEndAngle);
      cameraFieldOfViewAngle.moveTo(-xStartAngle, yStartAngle);
      cameraFieldOfViewAngle.lineTo(-xEndAngle, yEndAngle);
      g2D.draw(cameraFieldOfViewAngle);
      g2D.setTransform(previousTransform);

      // Paint resize indicator of selected camera
      if (selectedItems.size() == 1
          && selectedItems.get(0) == camera) {
        paintCameraRotationIndicators(g2D, camera, indicatorPaint, planScale);
      }
    }
  }

  private void paintCameraRotationIndicators(Graphics2D g2D,
                                             ObserverCamera camera, Paint indicatorPaint,
                                             float planScale) {
    if (this.resizeIndicatorVisible) {
      g2D.setPaint(indicatorPaint);
      g2D.setStroke(INDICATOR_STROKE);

      AffineTransform previousTransform = g2D.getTransform();
      // Draw yaw rotation indicator at middle of first and last point of camera
      float [][] cameraPoints = camera.getPoints();
      float scaleInverse = 1 * fatFingerScaleUp / planScale * this.resolutionScale;
      g2D.translate((cameraPoints [0][0] + cameraPoints [3][0]) / 2,
          (cameraPoints [0][1] + cameraPoints [3][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(camera.getYaw());
      g2D.draw(getIndicator(camera, IndicatorType.ROTATE));
      g2D.setTransform(previousTransform);
			AffineTransform previousTransform2 = g2D.getTransform();//force save
      // Draw pitch rotation indicator at middle of second and third point of camera
      g2D.translate((cameraPoints [1][0] + cameraPoints [2][0]) / 2,
          (cameraPoints [1][1] + cameraPoints [2][1]) / 2);
      g2D.scale(scaleInverse, scaleInverse);
      g2D.rotate(camera.getYaw());
      g2D.draw(getIndicator(camera, IndicatorType.ROTATE_PITCH));
      g2D.setTransform(previousTransform2);

      Shape elevationIndicator = getIndicator(camera, IndicatorType.ELEVATE);
      if (elevationIndicator != null) {
      	AffineTransform previousTransform3 = g2D.getTransform();
        // Draw elevation indicator at middle of first and second point of camera
        g2D.translate((cameraPoints [0][0] + cameraPoints [1][0]) / 2,
            (cameraPoints [0][1] + cameraPoints [1][1]) / 2);
        g2D.scale(scaleInverse, scaleInverse);
        g2D.draw(POINT_INDICATOR);
        g2D.translate(Math.sin(camera.getYaw()) * 8, -Math.cos(camera.getYaw()) * 8);
				g2D.draw(elevationIndicator);
        g2D.setTransform(previousTransform3);
      }
    }
  }

  /**
   * Paints rectangle feedback.
   */
  private void paintRectangleFeedback(Graphics2D g2D, Color selectionColor, float planScale) {
    if (this.rectangleFeedback != null) {
      g2D.setPaint(new Color(selectionColor.getRed(), selectionColor.getGreen(), selectionColor.getBlue(), 32));
      g2D.fill(this.rectangleFeedback);
      g2D.setPaint(selectionColor);
      g2D.setStroke(new BasicStroke(1 * fatFingerScaleUp / planScale * this.resolutionScale));
      g2D.draw(this.rectangleFeedback);
    }
  }

  //PJPJPJ taken from ShapeTools back here to avoid javaawt shape use here
  public static class ShapeTools {
	  // SVG path Shapes
	  private static final Map<String, Shape> parsedShapes = new WeakHashMap<String, Shape>();

	  private ShapeTools() {
	    // This class contains only tools
	  }

	  /**
	   * Returns the line stroke matching the given line styles.
	   */
	  public static Stroke getStroke(float thickness,
	                                 Polyline.CapStyle capStyle,
	                                 Polyline.JoinStyle joinStyle,
	                                 float [] dashPattern,
	                                 float dashOffset) {
	    int strokeCapStyle;
	    switch (capStyle) {
	      case ROUND :
	        strokeCapStyle = BasicStroke.CAP_ROUND;
	        break;
	      case SQUARE :
	        strokeCapStyle = BasicStroke.CAP_SQUARE;
	        break;
	      default:
	        strokeCapStyle = BasicStroke.CAP_BUTT;
	        break;
	    }

	    int strokeJoinStyle;
	    switch (joinStyle) {
	      case ROUND :
	      case CURVED :
	        strokeJoinStyle = BasicStroke.JOIN_ROUND;
	        break;
	      case BEVEL :
	        strokeJoinStyle = BasicStroke.JOIN_BEVEL;
	        break;
	      default:
	        strokeJoinStyle = BasicStroke.JOIN_MITER;
	        break;
	    }

	    float dashPhase = 0;
	    if (dashPattern != null) {
	      dashPattern = dashPattern.clone();
	      for (int i = 0; i < dashPattern.length; i++) {
	        dashPattern [i] *= thickness;
	        dashPhase += dashPattern [i];
	      }
	      dashPhase *= dashOffset;
	    }

	    return new BasicStroke(thickness, strokeCapStyle, strokeJoinStyle, 10, dashPattern, dashPhase);
	  }

	  /**
	   * Returns the shape of a polyline.
	   */
	  public static Shape getPolylineShape(float [][] points, boolean curved, boolean closedPath) {
	    if (curved) {
	      GeneralPath polylineShape = new GeneralPath();
	      for (int i = 0, n = closedPath ? points.length : points.length - 1; i < n; i++) {
	        CubicCurve2D.Float curve2D = new CubicCurve2D.Float();
	        float [] previousPoint = points [i == 0 ?  points.length - 1  : i - 1];
	        float [] point         = points [i];
	        float [] nextPoint     = points [i == points.length - 1 ?  0  : i + 1];
	        float [] vectorToBisectorPoint = new float [] {nextPoint [0] - previousPoint [0], nextPoint [1] - previousPoint [1]};
	        float [] nextNextPoint     = points [(i + 2) % points.length];
	        float [] vectorToBisectorNextPoint = new float [] {point[0] - nextNextPoint [0], point[1] - nextNextPoint [1]};
	        curve2D.setCurve(point[0], point[1],
	            point [0] + (i != 0 || closedPath  ? vectorToBisectorPoint [0] / 3.625f  : 0),
	            point [1] + (i != 0 || closedPath  ? vectorToBisectorPoint [1] / 3.625f  : 0),
	            nextPoint [0] + (i != points.length - 2 || closedPath  ? vectorToBisectorNextPoint [0] / 3.625f  : 0),
	            nextPoint [1] + (i != points.length - 2 || closedPath  ? vectorToBisectorNextPoint [1] / 3.625f  : 0),
	            nextPoint [0], nextPoint [1]);
	        polylineShape.append(curve2D, true);
	      }
	      return polylineShape;
	    } else {
	      return ShapeTools.getShape(points, closedPath, null);
	    }
	  }

		/**
		 * Returns the shape matching the coordinates in <code>points</code> array.
		 */
		public static Shape getShape(float [][] points, boolean closedPath, AffineTransform transform) {
			GeneralPath path = new GeneralPath();
			path.moveTo(points [0][0], points [0][1]);
			for (int i = 1; i < points.length; i++) {
				path.lineTo(points [i][0], points [i][1]);
			}
			if (closedPath) {
				path.closePath();
			}
			if (transform != null) {
				path.transform(transform);
			}
			return path;
		}
	}

  /**
   * Sets rectangle selection feedback coordinates.
   */
  public void setRectangleFeedback(float x0, float y0, float x1, float y1) {
    this.rectangleFeedback = new Rectangle2D.Float(x0, y0, 0, 0);
    this.rectangleFeedback.add(x1, y1);
    repaint();
  }

  /**
   * Ensures selected items are visible at screen and moves
   * scroll bars if needed.
   */
  public void makeSelectionVisible() {
    // As multiple selections may happen during an action,
    // make the selection visible the latest possible to avoid multiple changes
    if (!this.selectionScrollUpdated) {
      this.selectionScrollUpdated = true;
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            selectionScrollUpdated = false;
            Rectangle2D selectionBounds = getSelectionBounds(true);
            if (selectionBounds != null) {
              Rectangle pixelBounds = getShapePixelBounds(selectionBounds);
              pixelBounds.grow(5, 5);
              Rectangle visibleRectangle = getVisibleRect();
              if (!pixelBounds.intersects(visibleRectangle)) {
                scrollRectToVisible(pixelBounds);
              }
            }
          }
        });
    }
  }

  /**
   * Returns the bounds of the selected items.
   */
  private Rectangle2D getSelectionBounds(boolean includeCamera) {
		Graphics2D g = (Graphics2D)getGraphics();
		if (g != null) {
			setRenderingHints(g);
		}
		if (includeCamera) {
			return getItemsBounds(g, this.home.getSelectedItems());
		} else {
			List<Selectable> selectedItems = new ArrayList<Selectable>(this.home.getSelectedItems());
			selectedItems.remove(this.home.getCamera());
			return getItemsBounds(g, selectedItems);
		}
  }

  /**
   * Ensures the point at (<code>x</code>, <code>y</code>) is visible,
   * moving scroll bars if needed.
   */
  public void makePointVisible(float x, float y) {
    scrollRectToVisible(getShapePixelBounds(
      new Rectangle2D.Float(x, y, 1 / getScale(), 1 / getScale())));
  }

	/**
	 * Moves the view from (dx, dy) unit in the scrolling zone it belongs to.
	 */
  public void moveView(float dx, float dy) {
   /* if (this instanceof JViewport) {
      JViewport viewport = (JViewport)getParent();
      Rectangle viewRectangle = viewport.getViewRect();
      viewRectangle.translate(Math.round(dx * getScale()), Math.round(dy * getScale()));
      viewRectangle.x = Math.min(Math.max(0, viewRectangle.x), getWidth() - viewRectangle.width);
      viewRectangle.y = Math.min(Math.max(0, viewRectangle.y), getHeight() - viewRectangle.height);
      viewport.setViewPosition(viewRectangle.getLocation());
    }*/
	  //my scrolled are -ve versus the viewport location x,y
	  moveXScroll(dx);
	  moveYScroll(dy);
	  controller.setHomeProperty(PLAN_VIEWPORT_X_VISUAL_PROPERTY, String.valueOf(getXScroll()));
	  controller.setHomeProperty(PLAN_VIEWPORT_Y_VISUAL_PROPERTY, String.valueOf(getYScroll()));
	  PlanComponent.this.revalidate();

	  //update the tutorial
	  Point2D data = new Point2D.Float(getXScroll(), getYScroll());
	  ((Renovations3DActivity)getDrawableView().getContext()).getTutorial().actionComplete(Tutorial.TutorialAction.PLAN_MOVED, data);

  }

	//model coords
	protected void moveScrolledX(float deltaScrolledX) {
		moveXScroll(deltaScrolledX * getScale());
	}

	//model coords
	protected void moveScrolledY(float deltaScrolledY) {
		moveYScroll(deltaScrolledY * getScale());
	}

  /**
   * Returns the scale used to display the plan. Factor from model coord to pixel coords
   */
  public float getScale() {
    return this.scale;
  }

  /**
   * Sets the scale used to display the plan.
   * If this component is displayed in a viewport the view position is updated
   * to ensure the center's view will remain the same after the scale change.
   */
  public void setScale(float scale) {
		setScale(scale, true);
	}
	// hand in update position info, to emulate knowing the parent jviewport status
	public void setScale(float scale, boolean updateViewport) {
		if (this.scale != scale) {
			//JViewport parent = null;
			//Rectangle viewRectangle = null;
			float xViewCenterPosition = 0;
			float yViewCenterPosition = 0;
			if (updateViewport) {//getParent() instanceof JViewport) {
				//parent = (JViewport)getParent();
				//viewRectangle = parent.getViewRect();
				xViewCenterPosition = convertXPixelToModel(getWidth() / 2);
				yViewCenterPosition = convertYPixelToModel(getHeight() / 2);
			}

			this.scale = scale;
			// Revalidate plan without computing again unchanged plan bounds
			invalidate(false);

			if (updateViewport) {// (parent instanceof JViewport) {
				//Dimension viewSize = parent.getViewSize();
				//float viewWidth = convertXPixelToModel(viewRectangle.x + viewRectangle.width)
				//				- convertXPixelToModel(viewRectangle.x);
				int xViewLocation = (int)(xViewCenterPosition - convertXPixelToModel(getWidth() / 2));
				//float viewHeight = convertYPixelToModel(viewRectangle.y + viewRectangle.height)
				//				- convertYPixelToModel(viewRectangle.y);
				int yViewLocation = (int)(yViewCenterPosition - convertYPixelToModel( getHeight() / 2));
				//parent.setViewPosition(new Point(xViewLocation, yViewLocation));
				this.setScrollPosition(new Point(xViewLocation, yViewLocation));
			}
		}
  }

  /**
   * Returns <code>x</code> converted in model coordinates space.
   */
  public float convertXPixelToModel(int x) {
		Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();
  	return (x - insets.left + getXScroll()) / getScale() - MARGIN + (float)planBounds.getMinX();
  }

  /**
   * Returns <code>y</code> converted in model coordinates space.
   */
  public float convertYPixelToModel(int y) {
		Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();
		return (y - insets.top + getYScroll()) / getScale() - MARGIN + (float)planBounds.getMinY();
  }

  /**
   * Returns <code>x</code> converted in view coordinates space.
   */
	public int convertXModelToPixel(float x) {
		Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();
	 	return (int)Math.round((x - planBounds.getMinX() + MARGIN) * getScale() + insets.left - getXScroll());
  }

  /**
   * Returns <code>y</code> converted in view coordinates space.
   */
	public int convertYModelToPixel(float y) {
		Insets insets = getInsets();
    Rectangle2D planBounds = getPlanBounds();
		return (int)Math.round((y - planBounds.getMinY() + MARGIN) * getScale() + insets.top - getYScroll());
  }

  /**
   * Returns <code>x</code> converted in screen coordinates space.
   */
  public int convertXModelToScreen(float x) {
    return convertXModelToPixel(x);
  }

  /**
   * Returns <code>y</code> converted in screen coordinates space.
   */
  public int convertYModelToScreen(float y) {
  	return convertYModelToPixel(y);
  }


  /**
   * Returns the length in centimeters of a pixel with the current scale.
   */
  public float getPixelLength() {
    return 1 / getScale();
  }

  /**
   * Returns the bounds of <code>shape</code> in pixels coordinates space.
   */
  private Rectangle getShapePixelBounds(Shape shape) {
    Rectangle2D shapeBounds = shape.getBounds2D();
    return new Rectangle(
      convertXModelToPixel((float)shapeBounds.getMinX()),
	  convertYModelToPixel((float)shapeBounds.getMinY()),
      (int)Math.round(shapeBounds.getWidth() * getScale()),
	  (int)Math.round(shapeBounds.getHeight() * getScale()));
  }

  /**
   * Sets the cursor of this component.
   */
  public void setCursor(CursorType cursorType) {
/*    switch (cursorType) {
      case DRAW :
        setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
        break;
      case ROTATION :
        setCursor(this.rotationCursor);
        break;
      case HEIGHT :
        setCursor(this.heightCursor);
        break;
      case POWER :
        setCursor(this.powerCursor);
        break;
      case ELEVATION :
        setCursor(this.elevationCursor);
        break;
      case RESIZE :
        setCursor(this.resizeCursor);
        break;
      case PANNING :
        setCursor(this.panningCursor);
        break;
      case DUPLICATION :
        setCursor(this.duplicationCursor);
        break;
      case MOVE :
        setCursor(this.moveCursor);
        break;
      case SELECTION :
      default :
        setCursor(Cursor.getDefaultCursor());
        break;
    }*/
  }

  /**
   * Sets tool tip text displayed as feedback.
   * @param toolTipFeedback the text displayed in the tool tip
   *                    or <code>null</code> to make tool tip disappear.
   */
  public void setToolTipFeedback(String toolTipFeedback, float x, float y) {
    stopToolTipPropertiesEdition();
    JToolTip toolTip = getToolTip();
    // Change tool tip text
    toolTip.setTipText(toolTipFeedback);
    showToolTipComponentAt(toolTip, x , y);
  }

  /**
   * Returns the tool tip of the plan.
   */
  private JToolTip getToolTip() {
    // Create tool tip for this component
    if (this.toolTip == null) {
      this.toolTip = new JToolTip(this.getDrawableView().getContext());
      //this.toolTip.setComponent(this);
    }
    return this.toolTip;
  }

  /**
   * Shows the given component as a tool tip.
   */
  private void showToolTipComponentAt(View toolTipComponent, float x, float y) {
    //if (this.toolTipWindow == null) {
      // Show tool tip in a window (we don't use a Swing Popup because
      // we require the tool tip window to move along with mouse pointer
      // and a Swing tooltippopup can't move without hiding then showing it again)
      //this.toolTipWindow = new JWindow(JOptionPane.getFrameForComponent(this));
      //this.toolTipWindow.setFocusableWindowState(false);
      //this.toolTipWindow.add(toolTipComponent);

      toolTipManager.showTooltip(toolTipComponent);

      // Add to window a mouse listener that redispatch mouse events to
      // plan component (if the user moves fast enough the mouse pointer in a way
      // it's in toolTipWindow, the matching event is dispatched to toolTipWindow)
      /*MouseInputAdapter mouseAdapter = new MouseInputAdapter() {
        @Override
        public void mousePressed(MouseEvent ev) {
          mouseMoved(ev);
        }

        @Override
        public void mouseReleased(MouseEvent ev) {
          mouseMoved(ev);
        }

        @Override
        public void mouseMoved(MouseEvent ev) {
          dispatchEvent(SwingUtilities.convertMouseEvent(toolTipWindow, ev, PlanComponent.this));
        }

        @Override
        public void mouseDragged(MouseEvent ev) {
          mouseMoved(ev);
        }
      };
      this.toolTipWindow.addMouseListener(mouseAdapter);
      this.toolTipWindow.addMouseMotionListener(mouseAdapter);*/
    /*} else {
      Container contentPane = this.toolTipWindow.getContentPane();
      if (contentPane.getComponent(0) != toolTipComponent) {
        contentPane.removeAll();
        contentPane.add(toolTipComponent);
      }
      toolTipComponent.revalidate();
    }*/
    // Convert (x, y) to screen coordinates
   /* Point point = new Point(convertXModelToPixel(x), convertYModelToPixel(y));
    SwingUtilities.convertPointToScreen(point, this);
    // Add to point the half of cursor size
    Dimension cursorSize = getToolkit().getBestCursorSize(16, 16);
    if (cursorSize.width != 0) {
      point.x += cursorSize.width / 2 + 3;
      point.y += cursorSize.height / 2 + 3;
    } else {
      // If custom cursor isn't supported let's consider
      // default cursor size is 16 pixels wide
      point.x += 11;
      point.y += 11;
    }
    this.toolTipWindow.setLocation(point);
    this.toolTipWindow.pack();
    // Make the tooltip visible
    // (except in Applets run with Java 7 under Mac OS X where the tooltips are buggy)
    this.toolTipWindow.setVisible(!OperatingSystem.isMacOSX()
        || !OperatingSystem.isJavaVersionGreaterOrEqual("1.7")
        || SwingUtilities.getAncestorOfClass(JApplet.class, this) == null);
    toolTipComponent.paintImmediately(toolTipComponent.getBounds());*/
  }

  /**
   * Set tool tip edition.
   */
  public void setToolTipEditedProperties(final PlanController.EditableProperty [] toolTipEditedProperties,
                                         Object [] toolTipPropertyValues,
                                         float x, float y) {

  	//PJ TODO: this is used as an editable tooltip by pressing enter when drawing a room or wall or dimension line etc
		// skipped for now as there is no way to trigger it
		throw new UnsupportedOperationException();

   /* final JPanel toolTipPropertiesPanel = new JPanel(new GridBagLayout());
    // Reuse tool tip look
    Border border = UIManager.getBorder("ToolTip.border");
    if (!OperatingSystem.isMacOSX()
        || OperatingSystem.isMacOSXLeopardOrSuperior()) {
      border = BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(0, 3, 0, 2));
    }
    toolTipPropertiesPanel.setBorder(border);
    // Copy colors from tool tip instance (on Linux, colors aren't set in UIManager)
   // JToolTip toolTip = getToolTip();
    //toolTipPropertiesPanel.setBackground(toolTip.getBackground());
    //toolTipPropertiesPanel.setForeground(toolTip.getForeground());

    // Add labels and text fields to tool tip panel
    for (int i = 0; i < toolTipEditedProperties.length; i++) {
      JFormattedTextField textField = this.toolTipEditableTextFields.get(toolTipEditedProperties [i]);
      textField.setValue(toolTipPropertyValues [i]);
      JLabel label = new JLabel(activity, this.preferences.getLocalizedString(com.eteks.renovations3d.android.PlanComponent.class,
          toolTipEditedProperties [i].name() + ".editablePropertyLabel.text") + " ");
      //label.setFont(textField.getFont());
      JLabel unitLabel = null;
      if (toolTipEditedProperties [i] == PlanController.EditableProperty.ANGLE
          || toolTipEditedProperties [i] == PlanController.EditableProperty.ARC_EXTENT) {
        unitLabel = new JLabel(activity, this.preferences.getLocalizedString(com.eteks.renovations3d.android.PlanComponent.class, "degreeLabel.text"));
      } else if (this.preferences.getLengthUnit() != LengthUnit.INCH
                 || this.preferences.getLengthUnit() != LengthUnit.INCH_DECIMALS) {
        unitLabel = new JLabel(activity, " " + this.preferences.getLengthUnit().getName());
      }

      JPanel labelTextFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
      labelTextFieldPanel.setOpaque(false);

      labelTextFieldPanel.add(label);
      labelTextFieldPanel.add(textField);
      if (unitLabel != null) {
        unitLabel.setFont(textField.getFont());
        labelTextFieldPanel.add(unitLabel);
      }
      toolTipPropertiesPanel.add(labelTextFieldPanel, new GridBagConstraints(
          0, i, 1, 1, 0, 0, GridBagConstraints.LINE_START, GridBagConstraints.NONE,
          new Insets(0, 0, 0, 0), 0, 0));
    }

    showToolTipComponentAt(toolTipPropertiesPanel, x, y);*/
    // Add a key listener that redispatches events to tool tip text fields
    // (don't give focus to tool tip window otherwise plan component window will lose focus)
    /*this.toolTipKeyListener = new KeyListener() {
        private int focusedTextFieldIndex;
        private JTextComponent focusedTextField;

        {
          // Simulate focus on first text field
          setFocusedTextFieldIndex(0);
        }

        private void setFocusedTextFieldIndex(int textFieldIndex) {
          if (this.focusedTextField != null) {
            this.focusedTextField.getCaret().setVisible(false);
            this.focusedTextField.getCaret().setSelectionVisible(false);
            this.focusedTextField.setValue(this.focusedTextField.getValue());
          }
          this.focusedTextFieldIndex = textFieldIndex;
          this.focusedTextField = toolTipEditableTextFields.get(toolTipEditedProperties [textFieldIndex]);
          if (this.focusedTextField.getText().length() == 0) {
            this.focusedTextField.getCaret().setVisible(false);
          } else {
            this.focusedTextField.selectAll();
          }
          this.focusedTextField.getCaret().setSelectionVisible(true);
        }

        public void keyPressed(KeyEvent ev) {
          keyTyped(ev);
        }

        public void keyReleased(KeyEvent ev) {
          if (ev.getKeyCode() != KeyEvent.VK_CONTROL
              && ev.getKeyCode() != KeyEvent.VK_ALT) {
            // Forward other key events to focused text field (except for Ctrl and Alt key, otherwise InputMap won't receive it)
            KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(this.focusedTextField, ev);
          }
        }

        public void keyTyped(KeyEvent ev) {
          Set<AWTKeyStroke> forwardKeys = this.focusedTextField.getFocusTraversalKeys(
              KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
          if (forwardKeys.contains(AWTKeyStroke.getAWTKeyStrokeForEvent(ev))
              || ev.getKeyCode() == KeyEvent.VK_DOWN) {
            setFocusedTextFieldIndex((this.focusedTextFieldIndex + 1) % toolTipEditedProperties.length);
            ev.consume();
          } else {
            Set<AWTKeyStroke> backwardKeys = this.focusedTextField.getFocusTraversalKeys(
                KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
            if (backwardKeys.contains(AWTKeyStroke.getAWTKeyStrokeForEvent(ev))
                || ev.getKeyCode() == KeyEvent.VK_UP) {
              setFocusedTextFieldIndex((this.focusedTextFieldIndex - 1 + toolTipEditedProperties.length) % toolTipEditedProperties.length);
              ev.consume();
            } else if ((ev.getKeyCode() == KeyEvent.VK_HOME
                          || ev.getKeyCode() == KeyEvent.VK_END)
                       && OperatingSystem.isMacOSX()
                       && !OperatingSystem.isMacOSXLeopardOrSuperior()) {
              // Support Home and End keys under Mac OS X Tiger
              if (ev.getKeyCode() == KeyEvent.VK_HOME) {
                focusedTextField.setCaretPosition(0);
              } else if (ev.getKeyCode() == KeyEvent.VK_END) {
                focusedTextField.setCaretPosition(focusedTextField.getText().length());
              }
              ev.consume();
            } else if (ev.getKeyCode() != KeyEvent.VK_ESCAPE
                       && ev.getKeyCode() != KeyEvent.VK_CONTROL
                       && ev.getKeyCode() != KeyEvent.VK_ALT) {
              // Forward other key events to focused text field (except for Esc key, otherwise InputMap won't receive it)
              KeyboardFocusManager.getCurrentKeyboardFocusManager().redispatchEvent(this.focusedTextField, ev);
              this.focusedTextField.getCaret().setVisible(true);
              toolTipWindow.pack();
            }
          }
        }
      };

    addKeyListener(this.toolTipKeyListener);
    setFocusTraversalKeysEnabled(false);
    installEditionKeyboardActions();*/
  }

  /**
   * Deletes tool tip text from screen.
   */
  public void deleteToolTipFeedback() {
    stopToolTipPropertiesEdition();
    /*if (this.toolTip != null) {
      this.toolTip.setTipText(null);
    }
    if (this.toolTipWindow != null) {
      this.toolTipWindow.setVisible(false);
    }*/
		toolTipManager.hideTooltip();
  }

  /**
   * Stops editing in tool tip text fields.
   */
  private void stopToolTipPropertiesEdition() {
    /*if (this.toolTipKeyListener != null) {
      installDefaultKeyboardActions();
      setFocusTraversalKeysEnabled(true);
      removeKeyListener(toolTipKeyListener);
      this.toolTipKeyListener = null;

      for (JFormattedTextField textField : this.toolTipEditableTextFields.values()) {
        textField.getCaret().setVisible(false);
        textField.getCaret().setSelectionVisible(false);
      }
    }*/
  }

  /**
   * Sets whether the resize indicator of selected wall or piece of furniture
   * should be visible or not.
   */
  public void setResizeIndicatorVisible(boolean resizeIndicatorVisible) {
    this.resizeIndicatorVisible = resizeIndicatorVisible;
    repaint();
  }

  /**
   * Sets the location point for alignment feedback.
   */
  public void setAlignmentFeedback(Class<? extends Selectable> alignedObjectClass,
                                   Selectable alignedObject,
                                   float x,
                                   float y,
                                   boolean showPointFeedback) {
    this.alignedObjectClass = alignedObjectClass;
    this.alignedObjectFeedback = alignedObject;
    this.locationFeeback = new Point2D.Float(x, y);
    this.showPointFeedback = showPointFeedback;
    repaint();
  }

  /**
   * Sets the points used to draw an angle in plan view.
   */
  public void setAngleFeedback(float xCenter, float yCenter,
                               float x1, float y1,
                               float x2, float y2) {
    this.centerAngleFeedback = new Point2D.Float(xCenter, yCenter);
    this.point1AngleFeedback = new Point2D.Float(x1, y1);
    this.point2AngleFeedback = new Point2D.Float(x2, y2);
  }

  /**
   * Sets the feedback of dragged items drawn during a drag and drop operation,
   * initiated from outside of plan view.
   */
  public void setDraggedItemsFeedback(List<Selectable> draggedItems) {
    this.draggedItemsFeedback = draggedItems;
    repaint();
  }

  /**
   * Sets the given dimension lines to be drawn as feedback.
   */
  public void setDimensionLinesFeedback(List<DimensionLine> dimensionLines) {
    this.dimensionLinesFeedback = dimensionLines;
    repaint();
  }

  /**
   * Deletes all elements shown as feedback.
   */
  public void deleteFeedback() {
    deleteToolTipFeedback();
    this.rectangleFeedback = null;

    this.alignedObjectClass = null;
    this.alignedObjectFeedback = null;
    this.locationFeeback = null;

    this.centerAngleFeedback = null;
    this.point1AngleFeedback = null;
    this.point2AngleFeedback = null;

    this.draggedItemsFeedback = null;

    this.dimensionLinesFeedback = null;
    repaint();
  }

  /**
   * Returns <code>true</code>.
   */
  public boolean canImportDraggedItems(List<Selectable> items, int x, int y) {
    return true;
  }

  /**
   * Returns the size of the given piece of furniture in the horizontal plan,
   * or <code>null</code> if the view isn't able to compute such a value.
   */
  public float [] getPieceOfFurnitureSizeInPlan(HomePieceOfFurniture piece) {
    if (piece.getRoll() == 0 && piece.getPitch() == 0) {
      return new float [] {piece.getWidth(), piece.getDepth(), piece.getHeight()};
    } else if (!isFurnitureSizeInPlanSupported()) {
      return null;
    } else {
      return PieceOfFurnitureModelIcon.computePieceOfFurnitureSizeInPlan(piece, this.object3dFactory);
    }
  }

  /**
   * Returns <code>true</code> if this component is able to compute the size of horizontally rotated furniture.
   */
  public boolean isFurnitureSizeInPlanSupported() {
    try {
      return !Boolean.getBoolean("com.eteks.sweethome3d.no3D");
    } catch (AccessControlException ex) {
      // If com.eteks.sweethome3d.no3D can't be read,
      // security manager won't allow to access to Java 3D DLLs required by ModelManager class too
      return false;
    }
  }

  // Scrollable implementation
/*  public Dimension getPreferredScrollableViewportSize() {
    return getPreferredSize();
  }

  public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL) {
      return visibleRect.width / 2;
    } else { // SwingConstants.VERTICAL
      return visibleRect.height / 2;
    }
  }

  public boolean getScrollableTracksViewportHeight() {
    // Return true if the plan's preferred height is smaller than the viewport height
    return getParent() instanceof JViewport
        && getPreferredSize().height < ((JViewport)getParent()).getHeight();
  }

  public boolean getScrollableTracksViewportWidth() {
    // Return true if the plan's preferred width is smaller than the viewport width
    return getParent() instanceof JViewport
        && getPreferredSize().width < ((JViewport)getParent()).getWidth();
  }

  public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
    if (orientation == SwingConstants.HORIZONTAL) {
      return visibleRect.width / 10;
    } else { // SwingConstants.VERTICAL
      return visibleRect.height / 10;
    }
  }*/

  /**
   * Returns the component used as an horizontal ruler for this plan.
   */
  public com.eteks.sweethome3d.viewcontroller.View getHorizontalRuler() {
    if (this.horizontalRuler == null) {
      this.horizontalRuler = new PlanRulerComponent();
      horizontalRuler.init(HORIZONTAL, this);
    }
    return this.horizontalRuler;
  }

  /**
   * Returns the component used as a vertical ruler for this plan.
   */
  public com.eteks.sweethome3d.viewcontroller.View getVerticalRuler() {
    if (this.verticalRuler == null) {
      this.verticalRuler = new PlanRulerComponent();
      verticalRuler.init(VERTICAL, this);
    }
    return this.verticalRuler;
  }

  /**
   * A component displaying the plan horizontal or vertical ruler associated to this plan.
   */
  //note this guy is added to a scroll pane like this, in HomePane
	//planScrollPane.setColumnHeaderView((JComponent)controller.getPlanController().getHorizontalRulerView());
  public static class PlanRulerComponent extends JComponent implements com.eteks.sweethome3d.viewcontroller.View {
	private PlanComponent pc;
  	private int   orientation;
    private Point mouseLocation;

    /**
     * Creates a plan ruler.
     * @param orientation <code>SwingConstants.HORIZONTAL</code> or
     *                    <code>SwingConstants.VERTICAL</code>.
     */
    public void init(int orientation, PlanComponent pc) {
	  this.pc = pc;
      this.orientation = orientation;
      setOpaque(true);
      // Use same font as tool tips
      setFont(new VMFont(Typeface.DEFAULT, 12));//UIManager.getFont("ToolTip.font"));
      addMouseListeners();
    }

    /**
     * Adds a mouse listener to this ruler that stores current mouse location.
     */
    private void addMouseListeners() {
    /*  MouseInputListener mouseInputListener = new MouseInputAdapter() {
          @Override
          public void mouseDragged(MouseEvent ev) {
            mouseLocation = ev.getPoint();
            repaint();
          }

          @Override
          public void mouseMoved(MouseEvent ev) {
            mouseLocation = ev.getPoint();
            repaint();
          }

          @Override
          public void mouseEntered(MouseEvent ev) {
            mouseLocation = ev.getPoint();
            repaint();
          }

          @Override
          public void mouseExited(MouseEvent ev) {
            mouseLocation = null;
            repaint();
          }
        };
      PlanComponent.this.addMouseListener(mouseInputListener);
      PlanComponent.this.addMouseMotionListener(mouseInputListener);
      addAncestorListener(new AncestorListener() {
          public void ancestorAdded(AncestorEvent ev) {
            removeAncestorListener(this);
            if (getParent() instanceof JViewport) {
              ((JViewport)getParent()).addChangeListener(new ChangeListener() {
                  public void stateChanged(ChangeEvent ev) {
                    mouseLocation = MouseInfo.getPointerInfo().getLocation();
                    SwingUtilities.convertPointFromScreen(mouseLocation, PlanComponent.this);
                    repaint();
                  }
                });
            }
          }

          public void ancestorRemoved(AncestorEvent ev) {
          }

          public void ancestorMoved(AncestorEvent ev) {
          }
        });*/
    }

    /**
     * Returns the preferred size of this component.
     */
  /*  @Override
    public Dimension getPreferredSize() {
      if (isPreferredSizeSet()) {
        return super.getPreferredSize();
      } else {
      Insets insets = getInsets();
        Rectangle2D planBounds = getPlanBounds();
        FontMetrics metrics = getFontMetrics(getFont());
        int ruleHeight = metrics.getAscent() + 6;
        if (this.orientation == SwingConstants.HORIZONTAL) {
          return new Dimension(
              Math.round(((float)planBounds.getWidth() + MARGIN * 2)
                         * getScale()) + insets.left + insets.right,
              ruleHeight);
        } else {
          return new Dimension(ruleHeight,
              Math.round(((float)planBounds.getHeight() + MARGIN * 2)
                         * getScale()) + insets.top + insets.bottom);
        }
      }
    }*/

    /**
     * Paints this component.
     */
    @Override
	public void paintComponent(Graphics g) {
			Graphics2D g2D = (Graphics2D) g.create();
			paintBackground(g2D);
			Insets insets = getInsets();
			// Clip component to avoid drawing in empty borders
			g2D.clipRect(insets.left, insets.top,
							getWidth() - insets.left - insets.right,
							getHeight() - insets.top - insets.bottom);
			// Change component coordinates system to plan system
			Rectangle2D planBounds = pc.getPlanBounds();
			float paintScale = pc.getScale();
			g2D.translate((insets.left + (MARGIN - planBounds.getMinX()) * paintScale) - pc.getXScroll(),
							(insets.top + (MARGIN - planBounds.getMinY()) * paintScale) - pc.getYScroll());
			g2D.scale(paintScale, paintScale);
			g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
			// Paint component contents
			paintRuler(g2D, paintScale);
			g2D.dispose();
    }

    /**
     * Fills the background with UI window background color.
     */
    private void paintBackground(Graphics2D g2D) {
      if (isOpaque()) {
        g2D.setColor(getBackground());
        g2D.fillRect(0, 0, getWidth(), getHeight());
      }
    }

    /**
     * Paints background grid lines.
     */
    private void paintRuler(Graphics2D g2D, float rulerScale) {
      float gridSize = pc.getGridSize(rulerScale);
      float mainGridSize = pc.getMainGridSize(rulerScale);

      float xMin;
      float yMin;
      float xMax;
      float yMax;
      float xRulerBase;
      float yRulerBase;
      Rectangle2D planBounds = pc.getPlanBounds();
      boolean leftToRightOriented = true;// TODO: right to left at some point getComponentOrientation().isLeftToRight();
      if (true) {//getParent() instanceof JViewport ) {
        Rectangle viewRectangle = new Rectangle(0, 0, getWidth(), getHeight());
        xMin = pc.convertXPixelToModel(viewRectangle.x - 1);
        yMin = pc.convertYPixelToModel(viewRectangle.y - 1);
        xMax = pc.convertXPixelToModel(viewRectangle.x + viewRectangle.width);
        yMax = pc.convertYPixelToModel(viewRectangle.y + viewRectangle.height);
        xRulerBase = leftToRightOriented
            ? pc.convertXPixelToModel(viewRectangle.x + viewRectangle.width - 1)
            : pc.convertXPixelToModel(viewRectangle.x);
        yRulerBase = pc.convertYPixelToModel(viewRectangle.y + viewRectangle.height - 1);
      } else {
        xMin = (float)planBounds.getMinX() - MARGIN + (pc.getXScroll() / pc.getScale());
        yMin = (float)planBounds.getMinY() - MARGIN + (pc.getYScroll() / pc.getScale());
        xMax = pc.convertXPixelToModel(getWidth() - 1);
        yRulerBase =
        yMax = pc.convertYPixelToModel(getHeight() - 1);
        xRulerBase = leftToRightOriented ? xMax : xMin;
      }

      FontMetrics metrics = getFontMetrics(getFont());
      int fontAscent = (int)metrics.ascent;
      float tickSize = 5 * densityScale / rulerScale;
      float mainTickSize = (fontAscent + 1) * densityScale  / rulerScale;// ascent is negative as it should be (6 swapped to 1 not sure why needed)
      NumberFormat format = NumberFormat.getIntegerInstance();

      g2D.setColor(getForeground());
      float lineWidth = 0.5f / rulerScale;
      g2D.setStroke(new BasicStroke(lineWidth));
      if (this.orientation == HORIZONTAL) {
        // Draw horizontal ruler base
        g2D.draw(new Line2D.Float(xMin, yRulerBase - lineWidth / 2, xMax, yRulerBase - lineWidth  / 2));
        // Draw small ticks
        for (double x = (int)(xMin / gridSize) * gridSize; x < xMax; x += gridSize) {
          g2D.draw(new Line2D.Double(x, yMax - tickSize, x, yMax));
        }
      } else {
        // Draw vertical ruler base
        if (leftToRightOriented) {
          g2D.draw(new Line2D.Float(xRulerBase - lineWidth / 2, yMin, xRulerBase - lineWidth / 2, yMax));
        } else {
          g2D.draw(new Line2D.Float(xRulerBase + lineWidth / 2, yMin, xRulerBase + lineWidth / 2, yMax));
        }
        // Draw small ticks
        for (double y = (int)(yMin / gridSize) * gridSize; y < yMax; y += gridSize) {
          if (leftToRightOriented) {
            g2D.draw(new Line2D.Double(xMax - tickSize, y, xMax, y));
          } else {
            g2D.draw(new Line2D.Double(xMin, y, xMin + tickSize, y));
          }
        }
      }

      if (mainGridSize != gridSize) {
        g2D.setStroke(new BasicStroke(1.5f / rulerScale,
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL));
		  	//PJPJPJ moved into loop to preserve save/restore balance
        //AffineTransform previousTransform = g2D.getTransform();
        // Draw big ticks
        if (this.orientation == HORIZONTAL) {
          for (double x = ((int)(xMin / mainGridSize) - 1) * mainGridSize; x < xMax; x += mainGridSize) {
            AffineTransform previousTransform = g2D.getTransform();
            g2D.draw(new Line2D.Double(x, yMax + mainTickSize, x, yMax));//PJ - to + not sure why needed
            // Draw unit text
            g2D.translate(x, yMax - mainTickSize);
            g2D.scale(densityScale / rulerScale, densityScale / rulerScale);
            g2D.setFont(getFont());
            g2D.drawString(getFormattedTickText(format, x), 3, fontAscent - 1);
            g2D.setTransform(previousTransform);
          }
        } else {
          for (double y = ((int)(yMin / mainGridSize) - 1) * mainGridSize; y < yMax; y += mainGridSize) {
			AffineTransform previousTransform = g2D.getTransform();
            String yText = getFormattedTickText(format, y);
            if (leftToRightOriented) {
              g2D.draw(new Line2D.Double(xMax + mainTickSize, y, xMax, y));//PJ - to + not sure why needed
              // Draw unit text with a vertical orientation
              g2D.translate(xMax - mainTickSize, y);
              g2D.scale(densityScale / rulerScale, densityScale / rulerScale);
              g2D.rotate(-Math.PI / 2);
              g2D.setFont(getFont());
              g2D.drawString(yText, -((VMGraphics2D)g2D).canvasPaint.measureText(yText, 0, yText.length()) - 3, fontAscent - 1);
              //PJ swapped for above g2D.drawString(yText, -metrics.stringWidth(yText) - 3, fontAscent - 1);
            } else {
              g2D.draw(new Line2D.Double(xMin, y, xMin - mainTickSize, y));//PJ + to - not sure why needed
              // Draw unit text with a vertical orientation
              g2D.translate(xMin + mainTickSize, y);
              g2D.scale(densityScale / rulerScale, densityScale / rulerScale);
              g2D.rotate(Math.PI / 2);
              g2D.setFont(getFont());
              g2D.drawString(yText, 3, fontAscent - 1);
            }
            g2D.setTransform(previousTransform);
          }
        }
      }

      //if (this.mouseLocation != null) {
      if (pc.touchyListener.mActivePointerId != TouchyListener.INVALID_POINTER_ID ) {
        mouseLocation = new Point((int)pc.touchyListener.getLastDragLocation().x, (int)pc.touchyListener.getLastDragLocation().y);
        g2D.setColor(pc.getSelectionColor());
        g2D.setStroke(new BasicStroke(1 * fatFingerScaleUp / rulerScale));
        if (this.orientation == HORIZONTAL) {
          // Draw mouse feedback vertical line
          float x = pc.convertXPixelToModel(this.mouseLocation.x);
          g2D.draw(new Line2D.Float(x, yMax - mainTickSize, x, yMax));
        } else {
          // Draw mouse feedback horizontal line
          float y = pc.convertYPixelToModel(this.mouseLocation.y);
          if (leftToRightOriented) {
            g2D.draw(new Line2D.Float(xMax - mainTickSize, y, xMax, y));
          } else {
            g2D.draw(new Line2D.Float(xMin, y, xMin + mainTickSize, y));
          }
        }
      }
    }

    private String getFormattedTickText(NumberFormat format, double value) {
      String text;
      if (Math.abs(value) < 1E-5) {
        value = 0; // Avoid "-0" text
      }
      LengthUnit lengthUnit = pc.preferences.getLengthUnit();
      if (lengthUnit == LengthUnit.INCH
          || lengthUnit == LengthUnit.INCH_DECIMALS) {
        text = format.format(LengthUnit.centimeterToFoot((float)value)) + "'";
      } else {
        text = format.format(value / 100);
        if (value == 0) {
          text += LengthUnit.METER.getName();
        }
      }
      return text;
    }
  }

  /**
   * A proxy for the furniture icon seen from top.
   */
  public abstract static class PieceOfFurnitureTopViewIcon implements Icon {
    private Icon icon;

    public PieceOfFurnitureTopViewIcon(Icon icon) {
      this.icon = icon;
    }

    public int getIconWidth() {
      return this.icon != null ? this.icon.getIconWidth() : 0;
    }

    public int getIconHeight() {
      return this.icon != null ? this.icon.getIconHeight() : 0;
    }

    public void paintIcon(Object c, Graphics g, int x, int y) {
			if(this.icon != null)
				this.icon.paintIcon(c, g, x, y);
    }

    public boolean isWaitIcon() {
      return IconManager.getInstance().isWaitIcon(this.icon);
    }

    public boolean isErrorIcon() {
      return IconManager.getInstance().isErrorIcon(this.icon);
    }

    protected void setIcon(Icon icon) {
      this.icon = icon;
    }
  }

  /**
   * A proxy for the furniture plan icon generated from its plan icon.
   */
  private static class PieceOfFurniturePlanIcon extends PieceOfFurnitureTopViewIcon {
    private final float pieceWidth;
    private final float pieceDepth;
    private Integer     pieceColor;
    private HomeTexture pieceTexture;

    /**
     * Creates a plan icon proxy for a <code>piece</code> of furniture.
     * @param piece an object containing a plan icon content
     * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
     *            be read immediately in the current thread.
     */
    public PieceOfFurniturePlanIcon(final HomePieceOfFurniture piece,
                                    final JComponent waitingComponent) {
      super(IconManager.getInstance().getIcon(piece.getPlanIcon(), waitingComponent));
      this.pieceWidth = piece.getWidth();
      this.pieceDepth = piece.getDepth();
      this.pieceColor = piece.getColor();
      this.pieceTexture = piece.getTexture();
    }

    @Override
    public void paintIcon(final Object c, Graphics g, int x, int y) {
      if (!isWaitIcon()
          && !isErrorIcon()) {
        if (this.pieceColor != null) {
          // Create a monochrome icon from plan icon
          BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
          Graphics imageGraphics = image.getGraphics();
          super.paintIcon(c, imageGraphics, 0, 0);
          imageGraphics.dispose();

          final int colorRed   = this.pieceColor & 0xFF0000;
          final int colorGreen = this.pieceColor & 0xFF00;
          final int colorBlue  = this.pieceColor & 0xFF;
			//PJPJPJPJ bit complex this part
          /*setIcon(new ImageIcon(c.createImage(new FilteredImageSource(image.getSource (),
              new RGBImageFilter() {
                {
                  canFilterIndexColorModel = true;
                }

                public int filterRGB (int x, int y, int argb) {
                  int alpha = argb & 0xFF000000;
                  int red   = (argb & 0x00FF0000) >> 16;
                  int green = (argb & 0x0000FF00) >> 8;
                  int blue  = argb & 0x000000FF;

                  // Approximate brightness computation to 0.375 red + 0.5 green + 0.125 blue
                  // for faster results
                  int brightness = ((red + red + red + green + green + green + green + blue) >> 4) + 0x7F;

                  red   = (colorRed   * brightness / 0xFF) & 0xFF0000;
                  green = (colorGreen * brightness / 0xFF) & 0xFF00;
                  blue  = (colorBlue  * brightness / 0xFF) & 0xFF;
                  return alpha | red | green | blue;
                }
              }))));*/
          // Don't need color information anymore
          this.pieceColor = null;
        } else if (this.pieceTexture != null) {
          if (isTextureManagerAvailable()) {
            // Prefer to share textures images with texture manager if it's available
            TextureManager.getInstance().loadTexture(this.pieceTexture.getImage(), true,
                new TextureManager.TextureObserver() {
                  public void textureUpdated(Texture texture) {
                    setTexturedIcon(c, ((ImageComponent2D)texture.getImage(0)).getImage(), pieceTexture.getAngle());
                  }
                });
          } else {
            Icon textureIcon = IconManager.getInstance().getIcon(this.pieceTexture.getImage(), null);
            if (IconManager.getInstance().isErrorIcon(textureIcon)) {
              setTexturedIcon(c, ERROR_TEXTURE_IMAGE, 0);
            } else {
              BufferedImage textureIconImage = new BufferedImage(
                  textureIcon.getIconWidth(), textureIcon.getIconHeight(), BufferedImage.TYPE_INT_ARGB);
              Graphics2D g2DIcon = (Graphics2D)textureIconImage.getGraphics();
              textureIcon.paintIcon(c, g2DIcon, 0, 0);
              g2DIcon.dispose();
              setTexturedIcon(c, textureIconImage, this.pieceTexture.getAngle());
            }
          }

          // Don't need texture information anymore
          this.pieceTexture = null;
        }
      }
      super.paintIcon(c, g, x, y);
    }

    private void setTexturedIcon(Object c, BufferedImage textureImage, float angle) {
      // Paint plan icon in an image
      BufferedImage image = new BufferedImage(getIconWidth(), getIconHeight(), BufferedImage.TYPE_INT_ARGB);
      final Graphics2D imageGraphics = (Graphics2D)image.getGraphics();
      imageGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
      PieceOfFurniturePlanIcon.super.paintIcon(c, imageGraphics, 0, 0);

      // Fill the pixels of plan icon with texture image
      imageGraphics.setPaint(new TexturePaint(textureImage,
          new Rectangle2D.Float(0, 0, -getIconWidth() / this.pieceWidth * this.pieceTexture.getWidth(),
              -getIconHeight() / this.pieceDepth * this.pieceTexture.getHeight())));
      imageGraphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
      imageGraphics.rotate(angle);
      float maxDimension = Math.max(image.getWidth(), image.getHeight());
      imageGraphics.fill(new Rectangle2D.Float(-maxDimension, -maxDimension, 3 * maxDimension, 3 * maxDimension));
      imageGraphics.fillRect(0, 0, getIconWidth(), getIconHeight());
      imageGraphics.dispose();

      setIcon(new ImageIcon(image));
    }
  }

  /**
   * A proxy for the furniture top view icon generated from its 3D model.
   */
  public static class PieceOfFurnitureModelIcon extends PieceOfFurnitureTopViewIcon {
	private static Canvas3D        canvas3D;
	private static SimpleUniverse  universe;
	private static Semaphore 	   offScreenRenderSemaphore = new Semaphore(1, true);
    private static BranchGroup     sceneRoot;
    private static ExecutorService iconsCreationExecutor;

    /**
     * Creates a top view icon proxy for a <code>piece</code> of furniture.
     * @param piece an object containing a 3D content
     * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
     *            be read immediately in the current thread.
     * @param iconSize the size in pixels of the generated icon
     */
    public PieceOfFurnitureModelIcon(final HomePieceOfFurniture piece,
									 final Object3DFactory object3dFactory,
                                     final JComponent waitingComponent,
									 final int iconSize) {
      super(IconManager.getInstance().getWaitIcon());
      ModelManager.getInstance().loadModel(piece.getModel(), waitingComponent == null,
          new ModelManager.ModelObserver() {
            public void modelUpdated(final BranchGroup modelNode) {
              // Now that it's sure that 3D model exists
              // work on a clone of the piece centered at the origin
              // with the same size to get a correct texture mapping
              final HomePieceOfFurniture normalizedPiece = piece.clone();
              if (normalizedPiece.isResizable()
                  && piece.getRoll() == 0) {
                normalizedPiece.setModelMirrored(false);
              }
              final float pieceWidth = normalizedPiece.getWidthInPlan();
              final float pieceDepth = normalizedPiece.getDepthInPlan();
              final float pieceHeight = normalizedPiece.getHeightInPlan();
              normalizedPiece.setX(0);
              normalizedPiece.setY(0);
              normalizedPiece.setElevation(-pieceHeight / 2);
              normalizedPiece.setLevel(null);
              normalizedPiece.setAngle(0);
              if (waitingComponent != null) {
                // Generate icons in an other thread to avoid blocking EDT during offscreen rendering
                if (iconsCreationExecutor == null) {
                  iconsCreationExecutor = Executors.newSingleThreadExecutor();
                }
                iconsCreationExecutor.execute(new Runnable() {
                    public void run() {
                      setIcon(createIcon((Object3DBranch)object3dFactory.createObject3D(null, normalizedPiece, true),
                          pieceWidth, pieceDepth, pieceHeight, iconSize));
                      waitingComponent.repaint();
                    }
                  });
              } else {
                setIcon(createIcon((Object3DBranch)object3dFactory.createObject3D(null, normalizedPiece, true),
                    pieceWidth, pieceDepth, pieceHeight, iconSize));
              }
            }

            public void modelError(Exception ex) {
              // Too bad, we'll use errorIcon
              setIcon(IconManager.getInstance().getErrorIcon());
              if (waitingComponent != null) {
                waitingComponent.repaint();
              }
            }
          });
    }

		public static void pauseOffScreenRendering() {
			if(offScreenRenderSemaphore != null) {
				try {
					offScreenRenderSemaphore.acquire();
				} catch (InterruptedException e) {
				}
			}
		}

		public static void unpauseOffScreenRendering() {
			if(offScreenRenderSemaphore != null) {
				offScreenRenderSemaphore.release();
			}
		}

		public static void destroyUniverse() {
			if(universe != null) {
				pauseOffScreenRendering();
				try {
					universe.cleanup();
				} catch(Exception e) {
					// in production I don't care about exceptions now, but I do care about crashing.
					e.printStackTrace();
				}
				universe = null;
				canvas3D = null;
				sceneRoot = null;
				unpauseOffScreenRendering();
			}
		}
		/**
		 * Returns the branch group bound to a universe and a canvas for the given resolution.
		 */
		private static void ensureUniverseCreated() {
			if(canvas3D == null) {
				// Create the universe used to compute top view icons
				//TODO: the nice icon size value MUST be used
				canvas3D = Component3DManager.getInstance().getOffScreenCanvas3D(128, 128);
				universe = new SimpleUniverse(canvas3D);
				ViewingPlatform viewingPlatform = universe.getViewingPlatform();
				// View model from top
				TransformGroup viewPlatformTransform = viewingPlatform.getViewPlatformTransform();
				Transform3D rotation = new Transform3D();
				rotation.rotX(-Math.PI / 2);
				Transform3D transform = new Transform3D();
				transform.setTranslation(new Vector3f(0, 5, 0));
				transform.mul(rotation);
				viewPlatformTransform.setTransform(transform);
				// Use parallel projection
				Viewer viewer = viewingPlatform.getViewers()[0];
				org.jogamp.java3d.View view = viewer.getView();
				view.setProjectionPolicy(org.jogamp.java3d.View.PARALLEL_PROJECTION);
                view.setFrontClipDistance(0.01f);
                view.setBackClipDistance(100f);
				sceneRoot = new BranchGroup();
				// Prepare scene root
				sceneRoot.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
				sceneRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
				sceneRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
				Background background = new Background(1.1f, 1.1f, 1.1f);
				background.setCapability(Background.ALLOW_COLOR_WRITE);
				background.setApplicationBounds(new BoundingBox(new Point3d(-11, -11, -11), new Point3d(11, 11, 11)));
				sceneRoot.addChild(background);
				Light[] lights = {new DirectionalLight(new Color3f(0.6f, 0.6f, 0.6f), new Vector3f(1.5f, -0.8f, -1)),
								new DirectionalLight(new Color3f(0.6f, 0.6f, 0.6f), new Vector3f(-1.5f, -0.8f, -1)),
								new DirectionalLight(new Color3f(0.6f, 0.6f, 0.6f), new Vector3f(0, -0.8f, 1)),
								new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))};
				for (Light light : lights) {
					light.setInfluencingBounds(new BoundingBox(new Point3d(-11, -11, -11), new Point3d(11, 11, 11)));
					sceneRoot.addChild(light);
				}
				universe.addBranchGraph(sceneRoot);
			}
		}
    /**
     * Returns an icon created and scaled from piece model content.
     */
    private Icon createIcon(Object3DBranch pieceNode,
                            float pieceWidth, float pieceDepth, float pieceHeight,
														int iconSize) {
	  ensureUniverseCreated();
      // Add piece model scene to a normalized transform group
      Transform3D scaleTransform = new Transform3D();
      scaleTransform.setScale(new Vector3d(2 / pieceWidth, 2 / pieceHeight, 2 / pieceDepth));
      TransformGroup modelTransformGroup = new TransformGroup();
      modelTransformGroup.setTransform(scaleTransform);
      modelTransformGroup.addChild(pieceNode);
      // Replace model textures by clones because Java 3D doesn't accept all the time
      // to share textures between offscreen and onscreen environments
	  	cloneTexture(pieceNode, new IdentityHashMap<Texture, Texture>());

      BranchGroup model = new BranchGroup();
      model.setCapability(BranchGroup.ALLOW_DETACH);
      model.addChild(modelTransformGroup);
			try {
				offScreenRenderSemaphore.acquire();
				// we may have been disposed of
				if(canvas3D != null && sceneRoot != null) {
					sceneRoot.addChild(model);

					// Render scene with a white background
					Background background = (Background) sceneRoot.getChild(0);
					background.setColor(1, 1, 1);
					canvas3D.renderOffScreenBuffer();
					canvas3D.waitForOffScreenRendering();

					//I have bug report stating canvas3D has become null after this wait, I'm not sure how as the semaphore should protect
					// https://console.firebase.google.com/u/0/project/renovations-3d/monitoring/app/android:com.mindblowing.renovations3d/cluster/46677556?duration=2592000000
					if(canvas3D != null && sceneRoot != null) {
						BufferedImage imageWithWhiteBackgound = canvas3D.getOffScreenBuffer().getImage();
						int[] imageWithWhiteBackgoundPixels = getImagePixels(imageWithWhiteBackgound);

						// Render scene with a black background
						background.setColor(0, 0, 0);
						canvas3D.renderOffScreenBuffer();
						canvas3D.waitForOffScreenRendering();
						if(canvas3D != null && sceneRoot != null) {
							BufferedImage imageWithBlackBackgound = canvas3D.getOffScreenBuffer().getImage();
							int[] imageWithBlackBackgoundPixels = getImagePixels(imageWithBlackBackgound);

							// Create an image with transparent pixels where model isn't drawn
							for (int i = 0; i < imageWithBlackBackgoundPixels.length; i++) {
								if (imageWithBlackBackgoundPixels[i] != imageWithWhiteBackgoundPixels[i]
										&& imageWithBlackBackgoundPixels[i] == 0xFF000000
										&& imageWithWhiteBackgoundPixels[i] == 0xFFFFFFFF) {
									imageWithWhiteBackgoundPixels[i] = 0;
								}
							}

							sceneRoot.removeChild(model);
							return new ImageIcon(new VMBufferedImage(Bitmap.createBitmap(imageWithWhiteBackgoundPixels,
											imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(),
											Bitmap.Config.ARGB_8888)));
						}
					}
				}
			} catch (InterruptedException e) {
			}
			finally {
				offScreenRenderSemaphore.release();
			}
			return null;
    }

    /**
     * Replace the textures set on node shapes by clones.
     */
    private void cloneTexture(Node node, Map<Texture, Texture> replacedTextures) {
      if (node instanceof Group) {
        // Enumerate children
        Iterator<Node> enumeration = ((Group)node).getAllChildren();
        while (enumeration.hasNext()) {
          cloneTexture((Node)enumeration.next(), replacedTextures);
        }
      } else if (node instanceof Link) {
        cloneTexture(((Link)node).getSharedGroup(), replacedTextures);
      } else if (node instanceof Shape3D) {
        Appearance appearance = ((Shape3D)node).getAppearance();
        if (appearance != null) {
          Texture texture = appearance.getTexture();
          if (texture != null) {
            Texture replacedTexture = replacedTextures.get(texture);
            if (replacedTexture == null) {
              replacedTexture = (Texture)texture.cloneNodeComponent(false);
              replacedTextures.put(texture, replacedTexture);
            }
            appearance.setTexture(replacedTexture);
          }
        }
      }
    }

	/**
	 * Returns the pixels of the given <code>image</code>.
	 */
	public static int [] getImagePixels(BufferedImage image) {
		if (image.getType() == BufferedImage.TYPE_INT_RGB
				|| image.getType() == BufferedImage.TYPE_INT_ARGB) {
			// Use a faster way to get pixels
			return (int [])image.getRaster().getDataElements(0, 0, image.getWidth(), image.getHeight(), null);
		} else {
			return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null,
					0, image.getWidth());
		}
	}

    /**
     * Returns the size of the given piece computed from its vertices.
     */
    private static float [] computePieceOfFurnitureSizeInPlan(HomePieceOfFurniture piece,
                                                              Object3DFactory object3dFactory) {
			Transform3D horizontalRotation = new Transform3D();
			// Change its angles around horizontal axes
			if (piece.getPitch() != 0) {
				horizontalRotation.rotX(-piece.getPitch());
			}
			if (piece.getRoll() != 0) {
				Transform3D rollRotation = new Transform3D();
				rollRotation.rotZ(-piece.getRoll());
				horizontalRotation.mul(rollRotation, horizontalRotation);
			}

      // Compute bounds of a piece centered at the origin and rotated around the target horizontal angle
      piece = piece.clone();
      piece.setX(0);
      piece.setY(0);
      piece.setElevation(-piece.getHeight() / 2);
      piece.setLevel(null);
      piece.setAngle(0);
      piece.setRoll(0);
      piece.setPitch(0);
      piece.setWidthInPlan(piece.getWidth());
      piece.setDepthInPlan(piece.getDepth());
      piece.setHeightInPlan(piece.getHeight());
      BoundingBox bounds = ModelManager.getInstance().getBounds(
          (Object3DBranch)object3dFactory.createObject3D(null, piece, true), horizontalRotation);
      Point3d lower = new Point3d();
      bounds.getLower(lower);
      Point3d upper = new Point3d();
      bounds.getUpper(upper);
      return new float [] {
          Math.max(0.001f, (float)(upper.x - lower.x)), // width in plan
          Math.max(0.001f, (float)(upper.z - lower.z)), // depth in plan
          Math.max(0.001f, (float)(upper.y - lower.y))}; // height in plan
    }
  }

	/**
	 * A map key used to compare furniture with the same top view icon.
	 */
	private static class HomePieceOfFurnitureTopViewIconKey {
		private HomePieceOfFurniture piece;
		private int                  hashCode;

		public HomePieceOfFurnitureTopViewIconKey(HomePieceOfFurniture piece) {
			this.piece = piece;
			this.hashCode = (piece.getPlanIcon() != null ? piece.getPlanIcon().hashCode() : piece.getModel().hashCode())
							+ (piece.getColor() != null ? 37 * this.piece.getColor().hashCode() : 1234);
			if (this.piece.isHorizontallyRotated()
							|| this.piece.getTexture() != null) {
				this.hashCode +=
								(piece.getTexture() != null ? 37 * this.piece.getTexture().hashCode() : 0)
												+ 37 * Float.valueOf(piece.getWidthInPlan()).hashCode()
												+ 37 * Float.valueOf(piece.getDepthInPlan()).hashCode()
												+ 37 * Float.valueOf(piece.getHeightInPlan()).hashCode();
			}
            if (piece.getRoll() != 0) {
              this.hashCode += 37 * Boolean.valueOf(piece.isModelMirrored()).hashCode();
            }
            if (piece.getPlanIcon() != null) {
				this.hashCode +=
								37 * Arrays.deepHashCode(piece.getModelRotation())
												+ 37 * Boolean.valueOf(piece.isModelCenteredAtOrigin()).hashCode()
												+ 37 * Boolean.valueOf(piece.isBackFaceShown()).hashCode()
												+ 37 * Float.valueOf(piece.getPitch()).hashCode()
												+ 37 * Float.valueOf(piece.getRoll()).hashCode()
												+ 37 * Arrays.hashCode(piece.getModelTransformations())
												+ 37 * Arrays.hashCode(piece.getModelMaterials())
                                                + (piece.getShininess() != null ? 37 * piece.getShininess().hashCode() : 3456);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof HomePieceOfFurnitureTopViewIconKey) {
				HomePieceOfFurniture piece2 = ((HomePieceOfFurnitureTopViewIconKey)obj).piece;
				// Test all furniture data that could make change the plan icon
				// (see HomePieceOfFurniture3D and PlanComponent#addModelListeners for changes conditions)
				return (this.piece.getPlanIcon() != null
								? this.piece.getPlanIcon().equals(piece2.getPlanIcon())
								: this.piece.getModel().equals(piece2.getModel()))
                        && (this.piece.getColor() == piece2.getColor()
								|| this.piece.getColor() != null && this.piece.getColor().equals(piece2.getColor()))
                        && (this.piece.getTexture() == piece2.getTexture()
								|| this.piece.getTexture() != null && this.piece.getTexture().equals(piece2.getTexture()))
                        && (!this.piece.isHorizontallyRotated()
								&& !piece2.isHorizontallyRotated()
								&& this.piece.getTexture() == null
								&& piece2.getTexture() == null
                        || this.piece.getWidthInPlan() == piece2.getWidthInPlan()
								&& this.piece.getDepthInPlan() == piece2.getDepthInPlan()
								&& this.piece.getHeightInPlan() == piece2.getHeightInPlan())
                        && (this.piece.getRoll() == 0
                        && piece2.getRoll() == 0
                        || this.piece.isModelMirrored() == piece2.isModelMirrored())
                        && (this.piece.getPlanIcon() != null
                        || Arrays.deepEquals(this.piece.getModelRotation(), piece2.getModelRotation())
								&& this.piece.isModelCenteredAtOrigin() == piece2.isModelCenteredAtOrigin()
								&& this.piece.isBackFaceShown() == piece2.isBackFaceShown()
								&& this.piece.getPitch() == piece2.getPitch()
								&& this.piece.getRoll() == piece2.getRoll()
								&& Arrays.equals(this.piece.getModelTransformations(), piece2.getModelTransformations())
								&& Arrays.equals(this.piece.getModelMaterials(), piece2.getModelMaterials())
								&& (this.piece.getShininess() == piece2.getShininess()
								|| this.piece.getShininess() != null && this.piece.getShininess().equals(piece2.getShininess())));
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return this.hashCode;
		}
	}
}
