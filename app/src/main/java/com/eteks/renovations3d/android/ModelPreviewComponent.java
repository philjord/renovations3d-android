/*
 * ModelPreviewComponent 16 jan. 2010
 *
 * Sweet Home 3D, Copyright (c) 2007-2010 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.graphics.Bitmap;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import javaawt.Color;
import javaawt.Dimension;
import javaawt.EventQueue;
import javaawt.GraphicsConfiguration;
import javaawt.Point;
import javaawt.geom.Point2D;
import javaawt.image.BufferedImage;
import javaawt.image.VMBufferedImage;
import javaawt.imageio.ImageIO;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.jogamp.java3d.AmbientLight;
import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingBox;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.ColoringAttributes;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.Group;
import org.jogamp.java3d.Light;
import org.jogamp.java3d.Link;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.RenderingAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Texture;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.View;
import org.jogamp.java3d.utils.geometry.Cone;
import org.jogamp.java3d.utils.geometry.Cylinder;
import org.jogamp.java3d.utils.pickfast.PickCanvas;
import org.jogamp.java3d.utils.picking.PickResult;
import org.jogamp.java3d.utils.shader.SimpleShaderAppearance;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.java3d.utils.universe.Viewer;
import org.jogamp.java3d.utils.universe.ViewingPlatform;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Matrix3f;
import org.jogamp.vecmath.Point2d;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Point3f;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;

import com.eteks.renovations3d.j3d.Component3DManager;
import com.eteks.sweethome3d.j3d.HomePieceOfFurniture3D;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLException;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.JPanel;

/**
 * Super class of 3D preview component for model. 
 */
public class ModelPreviewComponent extends JPanel {
  private static final int MODEL_PREFERRED_SIZE = Math.round(240 * SwingTools.getResolutionScale());
  
  private SimpleUniverse          universe;
//  private JPanel component3DPanel;

//  private Component               component3D;
  private Canvas3D                canvas3D;
  private BranchGroup             sceneTree;
  private float                   viewYaw   = (float) Math.PI / 8;
  private float                   viewPitch = -(float) Math.PI / 16;
  private float                   viewScale = 1;
  private boolean                 parallelProjection;
  private Object                  iconImageLock;
  private HomePieceOfFurniture    previewedPiece;
  private boolean                 internalRotationAndSize;
  private Map<Texture, Texture>   pieceTextures = new HashMap<Texture, Texture>();

  private ScaledImageComponent 	  				modelPreviewImageComponent;

  private boolean postureChangeOccuring = false;
	private ScaleGestureDetector mScaleDetector;

  /**
   * Returns an 3D model preview component that lets the user change its yaw.
   */
  public ModelPreviewComponent(Activity activity) {
    this(false, activity);
  }
  
  /**
   * Returns an 3D model preview component that lets the user change its pitch and scale 
   * if <code>pitchAndScaleChangeSupported</code> is <code>true</code>.
   */
  public ModelPreviewComponent(boolean pitchAndScaleChangeSupported, Activity activity) {
    this(true, pitchAndScaleChangeSupported, pitchAndScaleChangeSupported, activity);
  }

	/**
	 * Returns an 3D model preview component that lets the user change its yaw, pitch and scale
	 * according to parameters.
	 */
	public ModelPreviewComponent(boolean yawChangeSupported,
															 boolean pitchChangeSupported,
															 boolean scaleChangeSupported, Activity activity) {
		this(yawChangeSupported, pitchChangeSupported, scaleChangeSupported, false, activity);
	}

  /**
   * Returns an 3D model preview component that lets the user change its yaw, pitch and scale 
   * according to parameters.
   */
  public ModelPreviewComponent(boolean yawChangeSupported, 
                               boolean pitchChangeSupported,
                               boolean scaleChangeSupported,
															 boolean transformationsChangeSupported, Activity activity) {
	  super(activity, R.layout.jpanel_model_preview_component);
   // setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));

    this.sceneTree = createSceneTree(transformationsChangeSupported);

		// swap the imagePreviewComponent out for a component that just uses the getIcon to draw an image
		modelPreviewImageComponent = new ScaledImageComponent(null, activity);
		this.swapOut(modelPreviewImageComponent, R.id.mpc_imagePreviewPanel);
		modelPreviewImageComponent.setPreferredSize(MODEL_PREFERRED_SIZE);// will be converted to px, after this call use the getPreferredSize() method
		modelPreviewImageComponent.getLayoutParams().width = getPreferredSize().width;
		modelPreviewImageComponent.getLayoutParams().height = getPreferredSize().height;
		modelPreviewImageComponent.setMinimumWidth(getPreferredSize().width);
		modelPreviewImageComponent.setMinimumHeight(getPreferredSize().height);

//    this.component3DPanel = new JPanel();
    //setLayout(new BorderLayout());
    //add(this.component3DPanel);

//    GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
//    if (graphicsEnvironment.getScreenDevices().length == 1) {
      // If only one screen device is available, create 3D component immediately, 
      // otherwise create it once the screen device of the parent is known
    	//PJPJPJPJ
      createComponent3D(null,//graphicsEnvironment.getDefaultScreenDevice().getDefaultConfiguration(), 
          yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);

	  // taken from ancestor below
	  createUniverse();
//    }

    // Add an ancestor listener to create 3D component and its universe once this component is made visible 
    // and clean up universe once its parent frame is disposed
    addAncestorListener(yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);


  }

  /**
   * Returns component preferred size.
   */
//  @Override
  public Dimension getPreferredSize() {
  //  if (isPreferredSizeSet()) {
  //    return super.getPreferredSize();
  //  } else {
      return new Dimension(modelPreviewImageComponent.getPreferredSize().width, modelPreviewImageComponent.getPreferredSize().height);
  //  }
  }


  public void addMouseMotionListener(final MouseListener l) {
    //super.addMouseMotionListener(l);
    if (this.canvas3D != null) {
			//TODO: make a touch listener to forward events on
      /* this.canvas3D.getGLWindow().addMouseListener(new MouseAdapter() {
          public void mouseMoved(MouseEvent ev) {
            l.mouseMoved(ev);
          }
          
          public void mouseDragged(MouseEvent ev) {
            l.mouseDragged(ev);
          }

        });*/
    }
  }

  // create a listener list that the touch handler can call to re-implement a general listener facility
	// that doesn't exist in the parent heirachy
	private List<MouseListener> mouseListeners = new  ArrayList<MouseListener>();
  public void addMouseListener(final MouseListener l) {
		mouseListeners.add(l);
  }
  
  /**
   * Returns the 3D component of this component.
   */
 // JComponent getComponent3D() {
 //   return this.component3DPanel;
//  }

  /**
   * Adds an ancestor listener to this component to manage the creation of the 3D component and its universe 
   * and clean up the universe.  
   */
  private void addAncestorListener(final boolean yawChangeSupported,
                                   final boolean pitchChangeSupported,
                                   final boolean scaleChangeSupported,
																	 final boolean transformationsChangeSupported) {
	  this.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
		  public void onViewAttachedToWindow(android.view.View v) {
				// PJ note the create is just moved to the constructor
		  }

		  public void onViewDetachedFromWindow(android.view.View v) {
				if (universe != null) {
					// Dispose universe later to avoid conflicts if canvas is currently being redrawn
					EventQueue.invokeLater(new Runnable() {
						public void run() {
			  			disposeUniverse();
							canvas3D.removeNotify();
							canvas3D = null;
						}
					});
				}
		  }
	  });
  }
  
  /**
   * Creates the 3D component associated with the given <code>configuration</code> device.
   */
  private void createComponent3D(GraphicsConfiguration graphicsConfiguration, 
                                 boolean yawChangeSupported, 
                                 boolean pitchChangeSupported,
                                 boolean scaleChangeSupported,
																 boolean transformationsChangeSupported) {
		try {
		/*	GLCapabilities caps = new GLCapabilities(null);

		  caps.setDoubleBuffered(true);
		  caps.setDepthBits(16);
		  caps.setStencilBits(8);
		  caps.setHardwareAccelerated(true);
		  caps.setBackgroundOpaque(true);

			GLWindow gl_window = GLWindow.create(caps);*/
	    canvas3D = Component3DManager.getInstance().getOffScreenCanvas3D(getPreferredSize().width,
							getPreferredSize().height);
		} catch (GLException e) {
		}

	  //PJPJPJPJPJ
/*    if (Boolean.getBoolean("com.eteks.sweethome3d.j3d.useOffScreen3DView")) {
      GraphicsConfigTemplate3D gc = new GraphicsConfigTemplate3D();
      gc.setSceneAntialiasing(GraphicsConfigTemplate3D.PREFERRED);
      try {
        // Instantiate JCanvas3DWithNotifiedPaint inner class by reflection
        // to be able to run under Java 3D 1.3
        this.component3D = (Component)Class.forName(ModelPreviewComponent.class.getName() + "$JCanvas3DWithNotifiedPaint").
            getConstructor(ModelPreviewComponent.class, GraphicsConfigTemplate3D.class).newInstance(this, gc);
      } catch (ClassNotFoundException ex) {
        throw new UnsupportedOperationException("Java 3D 1.5 required to display an offscreen 3D view");
      } catch (Exception ex) {
        UnsupportedOperationException ex2 = new UnsupportedOperationException();
        ex2.initCause(ex);
        throw ex2;
      }
    } else {
    	//PJPJPJPJPJ seperate these 2 into 2
      this.component3D = new JPanel();
    		  
      canvas3D =  Component3DManager.getInstance().getOnscreenCanvas3D(graphicsConfiguration,
          new Component3DManager.RenderingObserver() {
            public void canvas3DPreRendered(Canvas3D canvas3d) {
            }
            
            public void canvas3DPostRendered(Canvas3D canvas3d) {
            }
            
            public void canvas3DSwapped(Canvas3D canvas3d) {
              ModelPreviewComponent.this.canvas3DSwapped();
            }            
          });
    }
    this.component3D.setBackground(new Color(0xE5E5E5));

    // Layout 3D component
    this.component3DPanel.setLayout(new GridLayout());
    this.component3DPanel.add(this.component3D);
    this.component3D.setFocusable(false);*/
    addMouseListeners(this.canvas3D.getGLWindow(), yawChangeSupported, pitchChangeSupported, scaleChangeSupported, transformationsChangeSupported);
  }

  /**
   * A <code>JCanvas</code> canvas that sends a notification when it's drawn.
   * @param pi
   */
  //PJPJPJPJ
/*  private static class JCanvas3DWithNotifiedPaint extends JCanvas3D {
    private final ModelPreviewComponent homeComponent3D;

    public JCanvas3DWithNotifiedPaint(ModelPreviewComponent component,
                                      GraphicsConfigTemplate3D template) {
      super(template);
      this.homeComponent3D = component;
    }

    public void paintComponent(Graphics g) {
      super.paintComponent(g);
      this.homeComponent3D.canvas3DSwapped();
    }
  }*/
  
  /**
   * Adds an AWT mouse listener to component that will update view platform transform.  
   */
  private void addMouseListeners(final GLWindow component3D,
                                 final boolean yawChangeSupported, 
                                 final boolean pitchChangeSupported,
                                 final boolean scaleChangeSupported,
																 final boolean transformationsChangeSupported) {
    final float ANGLE_FACTOR = 0.02f;
    final float ZOOM_FACTOR = 0.02f;
    MouseAdapter mouseListener = new MouseAdapter() {
        private int xLastMouseMove;
        private int yLastMouseMove;
        private boolean        boundedPitch;
        private TransformGroup pickedTransformGroup;
        private Point2d 			 pivotCenterPixel;
        private Transform3D    translationFromOrigin;
        private Transform3D    translationToOrigin;
        private BoundingBox    modelBounds;

        private Point getMouseLocation(MouseEvent ev) {
          /*if (!OperatingSystem.isMacOSX()
              && OperatingSystem.isJavaVersionGreaterOrEqual("1.9")) {
            try {
              // Dirty hack that scales mouse coordinates with xcale and yscale private fields of Canvas3D
              Field xscaleField = Canvas3D.class.getDeclaredField("xscale");
              xscaleField.setAccessible(true);
              double xscale = (Double)(xscaleField.get(ev.getSource()));
              Field yscaleField = Canvas3D.class.getDeclaredField("yscale");
              yscaleField.setAccessible(true);
              double yscale = (Double)(yscaleField.get(ev.getSource()));
              return new Point((int)(ev.getX() * xscale), (int)(ev.getY() * yscale));
            } catch (Exception ex) {
            }
          }*/
          return new Point(ev.getX(), ev.getY());
        }

				@Override
				public void mouseReleased(MouseEvent ev) {
					postureChangeOccuring = false;
					updateIconImage();
				}

        @Override
        public void mousePressed(MouseEvent ev) {
          Point mouseLocation = getMouseLocation(ev);
          this.xLastMouseMove = mouseLocation.x;
          this.yLastMouseMove = mouseLocation.y;
          this.pickedTransformGroup = null;
          this.pivotCenterPixel = null;
					postureChangeOccuring = false;
          this.boundedPitch = true;
          if (transformationsChangeSupported
              && getModelNode() != null) {
            ModelManager modelManager = ModelManager.getInstance();
            this.boundedPitch = !modelManager.containsDeformableNode(getModelNode());
            Canvas3D canvas = canvas3D;
            //if (component3D instanceof JCanvas3D) {
            //  canvas = ((JCanvas3D)component3D).getOffscreenCanvas3D();
            //} else {
            //  canvas = (Canvas3D)component3D;
            //}
            PickCanvas pickCanvas = new PickCanvas(canvas, getModelNode());
            pickCanvas.setTolerance(0.0f); // make sure it's a ray not a cone
            pickCanvas.setFlags(PickInfo.NODE | PickInfo.SCENEGRAPHPATH);
            pickCanvas.setMode(PickInfo.PICK_GEOMETRY);

						//Canvas is not the same size as the touched scaledImage so, adjust mouse locaiton to match
						mouseLocation.x = (int)(((float)mouseLocation.x / (float)modelPreviewImageComponent.getWidth()) * (float)canvas3D.getWidth());
						mouseLocation.y = (int)(((float)mouseLocation.y / (float)modelPreviewImageComponent.getHeight()) * (float)canvas3D.getHeight());

            pickCanvas.setShapeLocation(mouseLocation.x, mouseLocation.y);
            PickInfo pi = pickCanvas.pickClosest();
            if (pi != null) {
              PickResult result = new PickResult(pi.getSceneGraphPath(), pickCanvas.getPickShape());
              this.pickedTransformGroup = (TransformGroup)result.getNode(PickResult.TRANSFORM_GROUP);
              if (pickedTransformGroup != null) {
                // The pivot node is the first sibling node which is not a transform group
                Group group = (Group)this.pickedTransformGroup.getParent();
                int i = group.indexOfChild(pickedTransformGroup) - 1;
                while (i >= 0 && (group.getChild(i) instanceof TransformGroup)) {
                  i--;
                }
                if (i >= 0) {
                  Node referenceNode = group.getChild(i);
                  Point3f nodeCenter = modelManager.getCenter(referenceNode);
                  Point3f nodeCenterAtScreen = new Point3f(nodeCenter);
                  Transform3D pivotTransform = getTransformBetweenNodes(referenceNode.getParent(), sceneTree);
                  pivotTransform.transform(nodeCenterAtScreen);
                  Transform3D transformToCanvas = new Transform3D();
                  canvas.getVworldToImagePlate(transformToCanvas);
                  transformToCanvas.transform(nodeCenterAtScreen);
                  this.pivotCenterPixel = new Point2d();
									postureChangeOccuring = true;
									updateIconImage();
                  canvas.getPixelLocationFromImagePlate(new Point3d(nodeCenterAtScreen), this.pivotCenterPixel);

                  String transformationName = (String)this.pickedTransformGroup.getUserData();System.out.println("transformationName " +transformationName);
                  this.translationFromOrigin = new Transform3D();
                  this.translationFromOrigin.setTranslation(new Vector3d(nodeCenter));
                  Transform3D transformBetweenNodes = getTransformBetweenNodes(referenceNode.getParent(), getModelNode());
                  transformBetweenNodes.setTranslation(new Vector3d());
                  transformBetweenNodes.invert();
                  this.translationFromOrigin.mul(transformBetweenNodes);

                  Transform3D pitchRotation = new Transform3D();
                  pitchRotation.rotX(viewPitch);
                  Transform3D yawRotation = new Transform3D();
                  yawRotation.rotY(viewYaw);

                  if (transformationName.startsWith(ModelManager.HINGE_PREFIX)
                      || transformationName.startsWith(ModelManager.RAIL_PREFIX)) {
                    Transform3D rotation = new Transform3D();
                    Vector3f nodeSize = modelManager.getSize(referenceNode);
                    getTransformBetweenNodes(getModelRoot(referenceNode), getModelNode()).transform(nodeSize);
                    nodeSize.absolute();

                    Transform3D modelRotationAtScreen = new Transform3D(yawRotation);
                    modelRotationAtScreen.mul(pitchRotation);
                    modelRotationAtScreen.invert();

                    // Set rotation around (or translation along) hinge largest dimension
                    // taking into account the direction of the axis at screen
                    if (nodeSize.y > nodeSize.x && nodeSize.y > nodeSize.z) {
                      Vector3f yAxisAtScreen = new Vector3f(0, 1, 0);
                      modelRotationAtScreen.transform(yAxisAtScreen);
                      if (transformationName.startsWith(ModelManager.RAIL_PREFIX)
                          ? yAxisAtScreen.y > 0
                          : yAxisAtScreen.z < 0) {
                        rotation.rotX(Math.PI / 2);
                      } else {
                        rotation.rotX(-Math.PI / 2);
                      }
                    } else if (nodeSize.z > nodeSize.x && nodeSize.z > nodeSize.y) {
                      Vector3f zAxisAtScreen = new Vector3f(0, 0, 1);
                      modelRotationAtScreen.transform(zAxisAtScreen);
                      if (transformationName.startsWith(ModelManager.RAIL_PREFIX)
                          ? zAxisAtScreen.x > 0
                          : zAxisAtScreen.z < 0) {
                      	rotation.rotX(Math.PI);
                      }
                    } else {
                      Vector3f xAxisAtScreen = new Vector3f(1, 0, 0);
                      modelRotationAtScreen.transform(xAxisAtScreen);
                      if (transformationName.startsWith(ModelManager.RAIL_PREFIX)
                          ? xAxisAtScreen.x > 0
                          : xAxisAtScreen.z < 0) {
                        rotation.rotY(-Math.PI / 2);
                      } else {
                        rotation.rotY(Math.PI / 2);
                      }
                    }
                    this.translationFromOrigin.mul(rotation);
                  } else {
                    // Set rotation in the screen plan for mannequin or ball handling
                    this.translationFromOrigin.mul(yawRotation);
                    this.translationFromOrigin.mul(pitchRotation);
                  }

                  this.translationToOrigin = new Transform3D(this.translationFromOrigin);
                  this.translationToOrigin.invert();

                  this.modelBounds = modelManager.getBounds(getModelNode());
                }
              }
            }
          }
        }

        private Transform3D getTransformBetweenNodes(Node node, Node parent) {
          Transform3D transform = new Transform3D();
          if (node instanceof TransformGroup) {
            ((TransformGroup)node).getTransform(transform);
          }
          if (node != parent ) {
            Node nodeParent = node.getParent();
            if (nodeParent instanceof Group) {
              transform.mul(getTransformBetweenNodes(nodeParent, parent), transform);
            } else {
              throw new IllegalStateException("Can't retrieve node transform");
            }
          }
          return transform;
        }

        private BranchGroup getModelRoot(Node node) {
          // Return the branch group parent which stores the model content
          if (node instanceof BranchGroup
              && node.getUserData() instanceof Content) {
            return (BranchGroup)node;
          } else if (node.getParent() != null) {
            return getModelRoot(node.getParent());
          } else {
            return null;
          }
        }
  
        @Override
        public void mouseDragged(MouseEvent ev) {
          Point mouseLocation = getMouseLocation(ev);
          if (getModelNode() != null) {
            if (this.pivotCenterPixel != null) {
              String transformationName = (String)this.pickedTransformGroup.getUserData();
              Transform3D additionalTransform = new Transform3D();
              if (transformationName.startsWith(ModelManager.RAIL_PREFIX)) {
                additionalTransform.setTranslation(new Vector3f(0, 0,
                    (float)Point2D.distance(mouseLocation.x, mouseLocation.y, this.xLastMouseMove, this.yLastMouseMove) * Math.signum(this.xLastMouseMove - mouseLocation.x)));
              } else {
                double angle = Math.atan2(this.pivotCenterPixel.y - mouseLocation.y, mouseLocation.x - this.pivotCenterPixel.x)
                    - Math.atan2(this.pivotCenterPixel.y - this.yLastMouseMove, this.xLastMouseMove - this.pivotCenterPixel.x);
                additionalTransform.rotZ(angle);
              }

              additionalTransform.mul(additionalTransform, this.translationToOrigin);
              additionalTransform.mul(this.translationFromOrigin, additionalTransform);

              Transform3D newTransform = new Transform3D();
              this.pickedTransformGroup.getTransform(newTransform);
              newTransform.mul(additionalTransform, newTransform);
              this.pickedTransformGroup.setTransform(newTransform);

              // Update size with model normalization and main transformation
              Point3d modelLower = new Point3d();
              this.modelBounds.getLower(modelLower);
              Point3d modelUpper = new Point3d();
              this.modelBounds.getUpper(modelUpper);
              ModelManager modelManager = ModelManager.getInstance();
              BoundingBox newBounds = modelManager.getBounds(getModelNode());
              Point3d newLower = new Point3d();
              newBounds.getLower(newLower);
              Point3d newUpper = new Point3d();
              newBounds.getUpper(newUpper);
              previewedPiece.setX(previewedPiece.getX() + (float)(newUpper.x + newLower.x) / 2 - (float)(modelUpper.x + modelLower.x) / 2);
              previewedPiece.setY(previewedPiece.getY() + (float)(newUpper.z + newLower.z) / 2 - (float)(modelUpper.z + modelLower.z) / 2);
              previewedPiece.setElevation(previewedPiece.getElevation() + (float)(newLower.y - modelLower.y));
              previewedPiece.setWidth((float)(newUpper.x - newLower.x));
              previewedPiece.setDepth((float)(newUpper.z - newLower.z));
              previewedPiece.setHeight((float)(newUpper.y - newLower.y));
              this.modelBounds = newBounds;

              // Update matching piece of furniture transformations array
              Transformation[] transformations = previewedPiece.getModelTransformations();
              ArrayList<Transformation> transformationsList = new ArrayList<Transformation>();
              if (transformations != null) {
                transformationsList.addAll(Arrays.asList(transformations));
              }
              transformationName = transformationName.substring(0, transformationName.length() - ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX.length());
              for (Iterator<Transformation> it = transformationsList.iterator(); it.hasNext();) {
                Transformation transformation = it.next();
                if (transformationName.equals(transformation.getName())) {
                  it.remove();
                  break;
                }
              }
              float [] matrix = new float [16];
              newTransform.get(matrix);
              transformationsList.add(new Transformation(transformationName, new float [][] {
                  {matrix [0], matrix [1], matrix [2], matrix [3]},
                  {matrix [4], matrix [5], matrix [6], matrix [7]},
                  {matrix [8], matrix [9], matrix [10], matrix [11]}}));
              previewedPiece.setModelTransformations(transformationsList.toArray(new Transformation [transformationsList.size()]));

							updateIconImage();
            } else {
							if (yawChangeSupported) {
								// Mouse move along X axis changes yaw
									setViewYaw(getViewYaw() - ANGLE_FACTOR * (mouseLocation.x - this.xLastMouseMove));
							}

							if (scaleChangeSupported && ev.isAltDown()) {
								// Mouse move along Y axis with Alt down changes scale
									setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float)Math.exp((mouseLocation.y - this.yLastMouseMove) * ZOOM_FACTOR))));
							} else if (pitchChangeSupported && !ev.isAltDown()) {
								// Mouse move along Y axis changes pitch
									float viewPitch = getViewPitch() - ANGLE_FACTOR * (mouseLocation.y - this.yLastMouseMove);
									if (this.boundedPitch) {
										setViewPitch(Math.max(-(float)Math.PI / 4, Math.min(0, viewPitch)));
									} else {
										// Allow any rotation around the model
										setViewPitch(viewPitch);
									}
							}
						}
        	}
          this.xLastMouseMove = mouseLocation.x;
          this.yLastMouseMove = mouseLocation.y;
        }
      };

      //added to scaled image instead
      //canvas3D.getGLWindow().addMouseListener(mouseListener);
    
    if (scaleChangeSupported) {
			/* canvas3D.getGLWindow().addMouseListener(new MouseAdapter() {
          public void mouseWheelMoved(MouseEvent ev) {
            // Mouse move along Y axis with Alt down changes scale
            setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float)Math.exp(ev.getRotation()[0] * ZOOM_FACTOR))));
          }
        });*/
			if(getContext() != null){
				mScaleDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
			}
    }
    
    // Redirect mouse events to the 3D component
/*    for (final MouseListener l : getListeners(MouseListener.class)) {
    	canvas3D.getGLWindow().addMouseListener(new MouseAdapter() {
          public void mouseMoved(MouseEvent ev) {
            l.mouseMoved(ev);
          }
          
          public void mouseDragged(MouseEvent ev) {
            l.mouseDragged(ev);
          }


        });
    }
    for (final MouseListener l : getListeners(MouseListener.class)) {
      canvas3D.getGLWindow().addMouseListener(new MouseAdapter() {
          public void mouseReleased(MouseEvent ev) {
            l.mouseReleased(ev);
          }
          
          public void mousePressed(MouseEvent ev) {
            l.mousePressed(ev);
          }
          
          public void mouseExited(MouseEvent ev) {
            l.mouseExited(ev);
          }
          
          public void mouseEntered(MouseEvent ev) {
            l.mouseEntered(ev);
          }
          
          public void mouseClicked(MouseEvent ev) {
            l.mouseClicked(ev);
          }
        });
    }*/


		modelPreviewImageComponent.setOnTouchListener(new TouchListener(mouseListener));
  }
	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

		/**
		 * @param detector
		 * @return
		 */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			//float yd = (mScaleDetector.getCurrentSpan() - mScaleDetector.getPreviousSpan());
			//setViewScale(Math.max(0.5f, Math.min(1.3f, getViewScale() * (float)Math.exp(yd))));

			float oldScale = getViewScale();
			float newScale = getViewScale() * (mScaleDetector.getPreviousSpan() / mScaleDetector.getCurrentSpan() );
			// Don't let the object get too small or too large.
			newScale = Math.max(0.1f, Math.min(newScale, 10.0f));
			setViewScale(newScale);

			if (newScale != oldScale ) {
				updateIconImage();
			}
			return true;
		}
	}
	private class TouchListener implements OnTouchListener
	{
		private static final int INVALID_POINTER_ID = -1;

		private MouseListener mouseListener;

		// The ‘active pointer’ is the one currently moving our object.
		private int mActivePointerId = INVALID_POINTER_ID;
		// for single finger moves
		private float xLastMouseMove = -1;
		private float yLastMouseMove = -1;

		// for 2 fingers
		private float xLastMouseMove2 = -1;
		private float yLastMouseMove2 = -1;

		public TouchListener(MouseListener mouseListener)
		{
			this.mouseListener = mouseListener;
		}
		@Override
		public boolean onTouch(android.view.View v, MotionEvent ev) {

			// Let the ScaleGestureDetector inspect all events, it will do move camera if it likes
			if(mScaleDetector != null ) {
				mScaleDetector.onTouchEvent(ev);
				if (mScaleDetector.isInProgress())
					return true;
			}
			final int action = ev.getActionMasked();
			MouseEvent me = new MouseEvent((short)0,this,0,0,(int)ev.getX(), (int)ev.getY(),(short)1,(short)0,null,0);
			switch (action & MotionEvent.ACTION_MASK) {
				case MotionEvent.ACTION_DOWN:
					if( ev.getPointerCount() == 1 ) {
						this.xLastMouseMove = ev.getX();
						this.yLastMouseMove = ev.getY();
						mouseListener.mousePressed(me);
						for (final MouseListener l : mouseListeners) {
							l.mousePressed(me);
						}
					}
					this.xLastMouseMove2 = -1;
					this.yLastMouseMove2 = -1;
					break;
				case MotionEvent.ACTION_POINTER_DOWN:
					break;
				case MotionEvent.ACTION_MOVE:
					if (ev.getPointerCount() == 1) {
						if(this.xLastMouseMove != -1 && this.yLastMouseMove != -1) {
							mouseListener.mouseDragged(me);
							for (final MouseListener l : mouseListeners) {
								l.mouseDragged(me);
							}
						}
						this.xLastMouseMove = ev.getX();
						this.yLastMouseMove = ev.getY();
					} else if (ev.getPointerCount() > 1) {
						if(this.xLastMouseMove2 != -1 && this.yLastMouseMove2 != -1) {

						}

						this.xLastMouseMove2 = ev.getX();
						this.yLastMouseMove2 = ev.getY();
					}
					break;
				case MotionEvent.ACTION_UP: // fall through!
				case MotionEvent.ACTION_CANCEL:
					mActivePointerId = INVALID_POINTER_ID;

					mouseListener.mouseReleased(me);
					for (final MouseListener l : mouseListeners) {
						l.mouseReleased(me);
					}
					this.xLastMouseMove = -1;
					this.yLastMouseMove = -1;

					this.xLastMouseMove2 = -1;
					this.yLastMouseMove2 = -1;
					break;
				case MotionEvent.ACTION_POINTER_UP:
					//second finger has been released
					final int pointerIndex = ev.getActionIndex();
					final int pointerId = ev.getPointerId(pointerIndex);

					if (pointerId == mActivePointerId) {
						// This was our active pointer going up. Choose a new
						// active pointer and adjust accordingly.
						final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
						this.xLastMouseMove2 = -1;
						this.yLastMouseMove2 = -1;
						mActivePointerId = ev.getPointerId(newPointerIndex);
					}

					// reduce the jitter when leaving 2 finger mode
					this.xLastMouseMove = -1;
					this.yLastMouseMove = -1;

					break;
			}

			return true;
		}
	}

  /**
   * Creates universe bound to the 3D component.
   */
  private void createUniverse() {
	  //PJPJPJ already create in createComponent3D
  /*  Canvas3D canvas3D;
   if (this.component3D instanceof Canvas3D) {
      canvas3D = (Canvas3D)this.component3D;
    } else {
      try {
        // Call JCanvas3D#getOffscreenCanvas3D by reflection to be able to run under Java 3D 1.3
        canvas3D = (Canvas3D)Class.forName("com.sun.j3d.exp.swing.JCanvas3D").getMethod("getOffscreenCanvas3D").invoke(this.component3D);
      } catch (Exception ex) {
        UnsupportedOperationException ex2 = new UnsupportedOperationException();
        ex2.initCause(ex);
        throw ex2;
      }
    } */   
    // Create a universe bound to component 3D
    ViewingPlatform viewingPlatform = new ViewingPlatform();
    Viewer viewer = new Viewer(canvas3D);
    this.universe = new SimpleUniverse(viewingPlatform, viewer);
    // Link scene to universe
    this.universe.addBranchGraph(this.sceneTree);
    this.universe.getViewer().getView().setProjectionPolicy(this.parallelProjection
        ? View.PARALLEL_PROJECTION 
        : View.PERSPECTIVE_PROJECTION);
    // Set viewer location 
    updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
        getViewYaw(), getViewPitch(), getViewScale());

//    revalidate();
 //   repaint();
 /*   if (OperatingSystem.isMacOSX()) {
      final Component root = SwingUtilities.getRoot(this);
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            // Request focus again even if dialog isn't supposed to have lost focus !
            if (KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow() != root) {
              root.requestFocus();
            }
          }
        });
    } */
  }
  
  /**
   * Disposes universe bound to canvas.
   */
  private void disposeUniverse() {
    // Unlink scene to universe
    this.universe.getLocale().removeBranchGraph(this.sceneTree);
    this.universe.cleanup();
    this.universe = null;
  }
  
  /**
   * Returns the <code>yaw</code> angle used by view platform transform.
   */
  protected float getViewYaw() {
    return this.viewYaw;
  }
  
  /**
   * Sets the <code>yaw</code> angle used by view platform transform.
   */
  protected void setViewYaw(float viewYaw) {
    this.viewYaw = viewYaw;
    if (this.universe != null) {
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch(), getViewScale());
    }
  }

  /**
   * Returns the zoom factor used by view platform transform.
   */
  protected float getViewScale() {
    return this.viewScale;
  }
  
  /**
   * Sets the zoom factor used by view platform transform.
   */
  protected void setViewScale(float viewScale) {
    this.viewScale = viewScale;
    if (this.universe != null) {
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch(), getViewScale());
    }
  }

  /**
   * Returns the <code>pitch</code> angle used by view platform transform.
   */
  protected float getViewPitch() {
    return this.viewPitch;
  }

  /**
   * Sets the <code>pitch</code> angle used by view platform transform.
   */
  protected void setViewPitch(float viewPitch) {
    this.viewPitch = viewPitch;
    if (this.universe != null) {
      updateViewPlatformTransform(this.universe.getViewingPlatform().getViewPlatformTransform(), 
          getViewYaw(), getViewPitch(), getViewScale());
    }
  }
  
  /**
   * Sets whether the component 3D should use parallel or perspective projection.
   */
  protected void setParallelProjection(boolean parallelProjection) {
    this.parallelProjection = parallelProjection;
    if (this.universe != null) {
      this.universe.getViewer().getView().setProjectionPolicy(parallelProjection 
          ? View.PARALLEL_PROJECTION 
          : View.PERSPECTIVE_PROJECTION);
    }
  }

  /**
   * Returns <code>true</code> if the component 3D uses parallel projection.
   */
  protected boolean isParallelProjection() {
    return this.parallelProjection;
  }
  
  /**
   * Updates the given view platform transformation from yaw angle, pitch angle and scale. 
   */
  private void updateViewPlatformTransform(TransformGroup viewPlatformTransform,
                                           float viewYaw, float viewPitch,
                                           float viewScale) {
    // Default distance used to view a 2 unit wide scene
    double nominalDistanceToCenter = 1.4 / Math.tan(Math.PI / 8);
    // We don't use a TransformGroup in scene tree to be able to share the same scene 
    // in the different views displayed by OrientationPreviewComponent class 
    Transform3D translation = new Transform3D();
    translation.setTranslation(new Vector3d(0, 0, nominalDistanceToCenter));
    Transform3D pitchRotation = new Transform3D();
    pitchRotation.rotX(viewPitch);
    Transform3D yawRotation = new Transform3D();
    yawRotation.rotY(viewYaw);
    Transform3D scale = new Transform3D();
    scale.setScale(viewScale);
    
    pitchRotation.mul(translation);
    yawRotation.mul(pitchRotation);
    scale.mul(yawRotation);
    viewPlatformTransform.setTransform(scale);

    // Update axes transformation to show current orientation and display it in bottom left corner
    Transform3D axesTransform = new Transform3D();
    axesTransform.setScale(viewScale);
    pitchRotation.rotX(-viewPitch);
    yawRotation.rotY(-viewYaw);
    axesTransform.mul(yawRotation, axesTransform);
    axesTransform.mul(pitchRotation, axesTransform);
    translation = new Transform3D();
    translation.setTranslation(new Vector3f(-.82f * viewScale, -.82f * viewScale, .82f * viewScale));
    pitchRotation.rotX(viewPitch);
    yawRotation.rotY(viewYaw);
    axesTransform.mul(translation, axesTransform);
    axesTransform.mul(pitchRotation, axesTransform);
    axesTransform.mul(yawRotation, axesTransform);
    ((TransformGroup)this.sceneTree.getChild(2)).setTransform(axesTransform);

		updateIconImage();
  }
  
  /**
   * Returns scene tree root.
   */
  private BranchGroup createSceneTree(boolean visibleAxes) {
    BranchGroup root = new BranchGroup();
    root.setCapability(BranchGroup.ALLOW_DETACH);
    root.setCapability(BranchGroup.ALLOW_CHILDREN_READ);
    root.setPickable(true);
    root.setCapability(Node.ALLOW_PARENT_READ);
    // Build scene tree
    root.addChild(createModelTree());
    root.addChild(createBackgroundNode());
    root.addChild(createAxes(visibleAxes));
    for (Light light : createLights()) {
      root.addChild(light);
    }
    return root;
  }
  
  /**
   * Returns the background node.  
   */
  private Node createBackgroundNode() {
    Background background = new Background(new Color3f(0.9f, 0.9f, 0.9f));
    background.setCapability(Background.ALLOW_COLOR_WRITE);
    background.setApplicationBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
    return background;
  }
  
  /**
   * Sets the background color.
   */
  public void setBackground(Color backgroundColor) {
//    super.setBackground(backgroundColor);
    ((Background)this.sceneTree.getChild(1)).setColor(new Color3f(backgroundColor.getRed() / 255f,backgroundColor.getGreen() / 255f,backgroundColor.getBlue() / 255f));
  }
  
  /**
   * Returns a RGB axes system.
   */
  private Node createAxes(boolean visible) {
    RenderingAttributes renderingAttributes = new RenderingAttributes();
    renderingAttributes.setVisible(visible);
    Appearance red = new SimpleShaderAppearance();
    red.setColoringAttributes(new ColoringAttributes(new Color3f(1, 0, 0), ColoringAttributes.SHADE_FLAT));
    red.setRenderingAttributes(renderingAttributes);
    Appearance green = new SimpleShaderAppearance();
    green.setColoringAttributes(new ColoringAttributes(new Color3f(0, 1,0), ColoringAttributes.SHADE_FLAT));
    green.setRenderingAttributes(renderingAttributes);
    Appearance blue = new SimpleShaderAppearance();
    blue.setColoringAttributes(new ColoringAttributes(new Color3f(0, 0, 1), ColoringAttributes.SHADE_FLAT));
    blue.setRenderingAttributes(renderingAttributes);

    Group axesGroup = new Group();
    Transform3D axisRotation = new Transform3D();
    axisRotation.rotZ(-Math.PI / 2);
    axesGroup.addChild(createAxis(axisRotation, red));
    axesGroup.addChild(createAxis(new Transform3D(), blue));
    axisRotation.rotX(Math.PI / 2);
    axesGroup.addChild(createAxis(axisRotation, green));
    TransformGroup axes = new TransformGroup();
    axes.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    axes.addChild(axesGroup);
    return axes;
  }

  private Node createAxis(Transform3D axisRotation, Appearance appearance) {
    Cylinder cylinder = new Cylinder(0.00275f, 0.2f, appearance);
    Transform3D cylinderTranslation = new Transform3D();
    cylinderTranslation.setTranslation(new Vector3f(0, 0.1f, 0));
    TransformGroup cylinderGroup = new TransformGroup(cylinderTranslation);
    cylinderGroup.addChild(cylinder);

    Cone cone = new Cone(0.01f, 0.04f, appearance);
    Transform3D coneTranslation = new Transform3D();
    coneTranslation.setTranslation(new Vector3f(0, 0.2f, 0));
    TransformGroup coneGroup = new TransformGroup(coneTranslation);
    coneGroup.addChild(cone);

    TransformGroup axisGroup = new TransformGroup(axisRotation);
    axisGroup.addChild(coneGroup);
    axisGroup.addChild(cylinderGroup);
    return axisGroup;
  }

  /**
   * Returns the lights of the scene.
   */
  private Light [] createLights() {
    Light [] lights = {
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(1.732f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(-1.732f, -0.8f, -1)), 
        new DirectionalLight(new Color3f(0.9f, 0.9f, 0.9f), new Vector3f(0, -0.8f, 1)), 
        new DirectionalLight(new Color3f(0.66f, 0.66f, 0.66f), new Vector3f(0, 1f, 0)), 
        new AmbientLight(new Color3f(0.2f, 0.2f, 0.2f))}; 

    for (Light light : lights) {
      light.setInfluencingBounds(new BoundingSphere(new Point3d(0, 0, 0), 100));
    }
    return lights;
  }

  /**
   * Returns the root of model tree.
   */
  private Node createModelTree() {
    TransformGroup modelTransformGroup = new TransformGroup();      
    //  Allow transform group to have new children
    modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_READ);
    modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_WRITE);
    modelTransformGroup.setCapability(Group.ALLOW_CHILDREN_EXTEND);
    // Allow the change of the transformation that sets model size and position
    modelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_WRITE);
    modelTransformGroup.setCapability(TransformGroup.ALLOW_TRANSFORM_READ);
    modelTransformGroup.setPickable(true);
    modelTransformGroup.setCapability(Node.ALLOW_PARENT_READ);
    return modelTransformGroup;
  }

  /**
   * Returns the 3D model content displayed by this component.
   */
  public Content getModel() {
    return this.previewedPiece != null
        ? this.previewedPiece.getModel()
        : null;
  }

  /**
   * Sets the 3D model content displayed by this component. 
   * The model is shown at its default orientation and in a box 1 unit wide.
   */
  public void setModel(Content model) {
    setModel(model, false, null, -1, -1, -1);
  }

  /**
   * Sets the 3D model content displayed by this component. 
   */
  void setModel(final Content model, final boolean backFaceShown, final float [][] modelRotation,
                final float width, final float depth, final float height) {
    final TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
    modelTransformGroup.removeAllChildren();
    this.previewedPiece = null;
    this.pieceTextures.clear();
    if (model != null) {
      final AtomicReference<IllegalArgumentException> exception = new AtomicReference<IllegalArgumentException>();
      // Load content model synchronously (or get it from cache)
      ModelManager.getInstance().loadModel(model, true, new ModelManager.ModelObserver() {        
          public void modelUpdated(BranchGroup modelRoot) {
            if (modelRoot.numChildren() > 0) {
              try {
                Vector3f size = width < 0 
                    ? ModelManager.getInstance().getSize(modelRoot)
                    : new Vector3f(width, height, depth);
                internalRotationAndSize = modelRotation != null;
                previewedPiece = new HomePieceOfFurniture(
                    new CatalogPieceOfFurniture(null, null, model, 
                        size.x, size.z, size.y, 0, false, null, modelRotation, backFaceShown, 0, false));
                previewedPiece.setX(0);
                previewedPiece.setY(0);
                previewedPiece.setElevation(-previewedPiece.getHeight() / 2);
                
                Transform3D modelTransform = new Transform3D();
                modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
                modelTransformGroup.setTransform(modelTransform);

                HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(previewedPiece, null, true, true);
                //if (OperatingSystem.isMacOSX()) {
                //  cloneTextures(piece3D, pieceTextures);
                //}
                modelTransformGroup.addChild(piece3D);
              } catch (IllegalArgumentException ex) {
                // Model is empty
              }
            }

            //Oddly the back faces appear to start off inverted or something? not sure?
			  		// but this make 2 calls to render icon
			  		setBackFaceShown(true);
			  		setBackFaceShown(backFaceShown);
          }
          
          public void modelError(Exception ex) {
            exception.set(new IllegalArgumentException("Couldn't load model", ex));
          }
        });
      
      if (exception.get() != null) {
        throw exception.get(); 
      }
    }
  }

  /**
   * Sets the back face visibility of the children nodes of the displayed 3D model.
   */
  protected void setBackFaceShown(boolean backFaceShown) {
    if (this.previewedPiece != null) {
      // Create a new piece from the existing one with an updated backFaceShown flag 
      this.previewedPiece = new HomePieceOfFurniture(
          new CatalogPieceOfFurniture(null, null, this.previewedPiece.getModel(), 
              this.previewedPiece.getWidth(), 
              this.previewedPiece.getDepth(),
              this.previewedPiece.getHeight(),
              0, false, this.previewedPiece.getColor(), 
              this.previewedPiece.getModelRotation(), backFaceShown, 0, false));
      this.previewedPiece.setX(0);
      this.previewedPiece.setY(0);
      this.previewedPiece.setElevation(-previewedPiece.getHeight() / 2);
    
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      HomePieceOfFurniture3D piece3D = new HomePieceOfFurniture3D(previewedPiece, null, true, true);
     // if (OperatingSystem.isMacOSX()) {
     //   this.pieceTextures.clear();
     //   cloneTextures(piece3D, this.pieceTextures);
    //  }
      modelTransformGroup.addChild(piece3D);
      if (modelTransformGroup.numChildren() > 1) {
        modelTransformGroup.removeChild(0);
      }
    }

		updateIconImage();
  }

  /**
   * Returns the 3D model node displayed by this component. 
   */
  private HomePieceOfFurniture3D getModelNode() {
    TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
    if (modelTransformGroup.numChildren() > 0) {
      return (HomePieceOfFurniture3D)modelTransformGroup.getChild(0);
    } else {
      return null;
    }
  }
  
  /**
   * Updates the rotation of the 3D model displayed by this component. 
   * The model is shown at its default size.
   */
  protected void setModelRotation(float [][] modelRotation) {
    BranchGroup modelNode = getModelNode();
    if (modelNode != null && modelNode.numChildren() > 0) {
      // Check rotation isn't set on model node 
      if (this.internalRotationAndSize) {
        throw new IllegalStateException("Can't set rotation");
      }
      // Apply model rotation
      Transform3D rotationTransform = new Transform3D();
      if (modelRotation != null) {
        Matrix3f modelRotationMatrix = new Matrix3f(modelRotation [0][0], modelRotation [0][1], modelRotation [0][2],
            modelRotation [1][0], modelRotation [1][1], modelRotation [1][2],
            modelRotation [2][0], modelRotation [2][1], modelRotation [2][2]);
        rotationTransform.setRotation(modelRotationMatrix);
      }
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      Vector3f size = ModelManager.getInstance().getSize(modelNode);
      modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
      modelTransform.mul(rotationTransform);
      
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.setTransform(modelTransform);

			updateIconImage();
    }
  }
  
  /**
   * Updates the rotation and the size of the 3D model displayed by this component. 
   */
  protected void setModelRotationAndSize(float [][] modelRotation,
                                         float width, float depth, float height) {
    BranchGroup modelNode = getModelNode();
    if (modelNode != null && modelNode.numChildren() > 0) {
      // Check rotation isn't set on model node
      if (this.internalRotationAndSize) {
        throw new IllegalStateException("Can't set rotation and size");
      }
      Transform3D normalization = ModelManager.getInstance().getNormalizedTransform(modelNode, modelRotation, 1f);
      // Scale model to its size
      Transform3D scaleTransform = new Transform3D();
      if (width != 0 && depth != 0 && height != 0) {
        scaleTransform.setScale(new Vector3d(width, height, depth));
      }
      scaleTransform.mul(normalization);
      // Scale model to make it fit in a 1.8 unit wide box      
      Transform3D modelTransform = new Transform3D();
      if (width != 0 && depth != 0 && height != 0) {
        modelTransform.setScale(1.8 / Math.max(Math.max(width, height), depth));
      } else {
        Vector3f size = ModelManager.getInstance().getSize(modelNode);
        modelTransform.setScale(1.8 / Math.max(Math.max(size.x, size.z), size.y));
      }
      modelTransform.mul(scaleTransform);
      
      TransformGroup modelTransformGroup = (TransformGroup)this.sceneTree.getChild(0);
      modelTransformGroup.setTransform(modelTransform);

			updateIconImage();
    }
  }

  /**
   * Sets the color applied to 3D model.
   */
  protected void setModelColor(Integer color) {
    if (this.previewedPiece != null
        && this.previewedPiece.getColor() != color) {
      this.previewedPiece.setColor(color);
      getModelNode().update();

			updateIconImage();
    }
  }

  /**
   * Sets the materials applied to 3D model.
   */
  public void setModelMaterials(HomeMaterial [] materials) {
    if (this.previewedPiece != null) {
      this.previewedPiece.setModelMaterials(materials);
      getModelNode().update();
      // Replace textures by clones because Java 3D doesn't accept all the time to share textures 
      cloneTextures(getModelNode(), this.pieceTextures);

			updateIconImage();
    }
  }

	/**
	 * Sets the transformations applied to 3D model.
	 */
	public void setModelTranformations(Transformation[] transformations) {
		if (this.previewedPiece != null) {
			this.previewedPiece.setModelTransformations(transformations);
			getModelNode().update();

			updateIconImage();
		}
	}

  void resetModelTranformations() {
    if (this.previewedPiece != null) {
      ModelManager modelManager = ModelManager.getInstance();
      BoundingBox oldBounds = modelManager.getBounds(getModelNode());
      Point3d oldLower = new Point3d();
      oldBounds.getLower(oldLower);
      Point3d oldUpper = new Point3d();
      oldBounds.getUpper(oldUpper);

      resetTranformations(getModelNode());

      BoundingBox newBounds = modelManager.getBounds(getModelNode());
      Point3d newLower = new Point3d();
      newBounds.getLower(newLower);
      Point3d newUpper = new Point3d();
      newBounds.getUpper(newUpper);
      previewedPiece.setX(previewedPiece.getX() + (float)(newUpper.x + newLower.x) / 2 - (float)(oldUpper.x + oldLower.x) / 2);
      previewedPiece.setY(previewedPiece.getY() + (float)(newUpper.z + newLower.z) / 2 - (float)(oldUpper.z + oldLower.z) / 2);
      previewedPiece.setElevation(previewedPiece.getElevation() + (float)(newLower.y - oldLower.y));
      this.previewedPiece.setWidth((float)(newUpper.x - newLower.x));
      this.previewedPiece.setDepth((float)(newUpper.z - newLower.z));
      this.previewedPiece.setHeight((float)(newUpper.y - newLower.y));
      this.previewedPiece.setModelTransformations(null);

			updateIconImage();
    }
  }

  private void resetTranformations(Node node) {
    if (node instanceof Group) {
      if (node instanceof TransformGroup
          && node.getUserData() instanceof String
          && ((String)node.getUserData()).endsWith(ModelManager.DEFORMABLE_TRANSFORM_GROUP_SUFFIX)) {
        ((TransformGroup)node).setTransform(new Transform3D());
      }
      Iterator<Node> enumeration = ((Group)node).getAllChildren();
      while (enumeration.hasNext()) {
        resetTranformations(enumeration.next());
      }
    }
  }

  /**
   * Returns the transformations applied to 3D model.
   */
  Transformation [] getModelTransformations() {
    if (this.previewedPiece != null) {
      return this.previewedPiece.getModelTransformations();
    } else {
      return null;
    }
  }

  /**
   * Returns the abscissa of the 3D model.
   */
  float getModelX() {
    return this.previewedPiece.getX();
  }

  /**
   * Returns the ordinate of the 3D model.
   */
  float getModelY() {
    return this.previewedPiece.getY();
  }

  /**
   * Returns the elevation of the 3D model.
   */
  float getModelElevation() {
    return this.previewedPiece.getElevation();
  }

  /**
   * Returns the width of the 3D model.
   */
  float getModelWidth() {
    return this.previewedPiece.getWidth();
  }

  /**
   * Returns the depth of the 3D model.
   */
  float getModelDepth() {
    return this.previewedPiece.getDepth();
  }

  /**
   * Returns the height of the 3D model.
   */
  float getModelHeight() {
    return this.previewedPiece.getHeight();
  }

  /**
   * Replace the textures set on <code>node</code> shapes by clones. 
   */
  private void cloneTextures(Node node, Map<Texture, Texture> replacedTextures) {
    if (node instanceof Group) {
      // Enumerate children
      Iterator<Node> enumeration = ((Group)node).getAllChildren(); 
      while (enumeration.hasNext()) {
        cloneTextures((Node)enumeration.next(), replacedTextures);
      }
    } else if (node instanceof Link) {
      cloneTextures(((Link)node).getSharedGroup(), replacedTextures);
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

  private void updateIconImage() {
		if (canvas3D != null) {
			// indicate model posture change occuring
			if (postureChangeOccuring) {
				setBackground(Color.LIGHT_GRAY);
			} else {
				setBackground(Color.WHITE);
			}
			canvas3D.renderOffScreenBuffer();
			canvas3D.waitForOffScreenRendering();

			BufferedImage image = canvas3D.getOffScreenBuffer().getImage();
			// must be carefully swapped to get BGRA to RGBA
			int[] imagePixels = PlanComponent.PieceOfFurnitureModelIcon.getImagePixels(image);
			Bitmap bm = Bitmap.createBitmap(imagePixels, image.getWidth(), image.getHeight(), Bitmap.Config.RGB_565);// note smaller config size

			modelPreviewImageComponent.setImage(new VMBufferedImage(bm));
		}
	}

  /**
   * Returns the icon image matching the displayed view.  
   */
  private BufferedImage getIconImage(int maxWaitingDelay) {
		if(canvas3D != null) {
			setBackground(Color.WHITE);
	  	canvas3D.renderOffScreenBuffer();
	  	canvas3D.waitForOffScreenRendering();
		  BufferedImage imageWithWhiteBackgound = canvas3D.getOffScreenBuffer().getImage();
		  int[] imageWithWhiteBackgoundPixels = PlanComponent.PieceOfFurnitureModelIcon.getImagePixels(imageWithWhiteBackgound);

		  // Render scene with a black background
		  setBackground(Color.BLACK);
		  canvas3D.renderOffScreenBuffer();
		  canvas3D.waitForOffScreenRendering();
			BufferedImage imageWithBlackBackgound = canvas3D.getOffScreenBuffer().getImage();
			int[] imageWithBlackBackgoundPixels = PlanComponent.PieceOfFurnitureModelIcon.getImagePixels(imageWithBlackBackgound);

			// Create an image with transparent pixels where model isn't drawn
			for (int i = 0; i < imageWithBlackBackgoundPixels.length; i++) {
				if (imageWithBlackBackgoundPixels[i] != imageWithWhiteBackgoundPixels[i]
								&& imageWithBlackBackgoundPixels[i] == 0xFF000000
								&& imageWithWhiteBackgoundPixels[i] == 0xFFFFFFFF) {
					imageWithWhiteBackgoundPixels[i] = 0;
				}
			}


			Bitmap bm = Bitmap.createBitmap(imageWithWhiteBackgoundPixels, imageWithWhiteBackgound.getWidth(), imageWithWhiteBackgound.getHeight(), Bitmap.Config.ARGB_8888);
			return new VMBufferedImage(bm);
	  }
	  return null;
  }

  /**
   * Returns a temporary content of the icon matching the displayed view.
   */
  public Content getIcon(int maxWaitingDelay) throws IOException {
    File tempIconFile = OperatingSystem.createTemporaryFile("icon", ".png");
    ImageIO.write(getIconImage(maxWaitingDelay), "png", tempIconFile);
    return new TemporaryURLContent(tempIconFile.toURI().toURL());
  }
  
  /**
   * Notifies the canvas 3D displayed by this component was swapped.
   */
  private void canvas3DSwapped() {
    if (this.iconImageLock != null) {
      synchronized (this.iconImageLock) {
        this.iconImageLock.notify();
      }
    }
  }
}