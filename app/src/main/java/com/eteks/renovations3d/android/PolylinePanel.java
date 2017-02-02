/*
 * PolylinePanel.java 
 *
 * Copyright (c) 2009 Plan PHP All Rights Reserved.
 */
package com.eteks.renovations3d.android;


import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.eteks.renovations3d.android.swingish.DefaultComboBoxModel;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JComboBox;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Polyline.ArrowStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PolylineController;
import com.eteks.sweethome3d.viewcontroller.VCView;

import javaawt.BasicStroke;
import javaawt.Color;
import javaawt.Graphics2D;
import javaawt.RenderingHints;
import javaawt.Shape;
import javaawt.VMGraphics2D;
import javaawt.geom.Arc2D;
import javaawt.geom.GeneralPath;


/**
 * User preferences panel.
 * @author Emmanuel Puybaret
 */
public class PolylinePanel extends Dialog implements DialogView {
  private final PolylineController controller;
  private JLabel thicknessLabel;
  private JSpinner thicknessSpinner;
  private JLabel         arrowsStyleLabel;
  private JComboBox arrowsStyleComboBox;
  private JLabel         joinStyleLabel;
  private JComboBox      joinStyleComboBox;
  private JLabel         dashStyleLabel;
  private JComboBox      dashStyleComboBox;
  private JLabel         colorLabel;
  private ColorButton    colorButton;
  private String         dialogTitle;


	private Activity activity;
	private LinearLayout rootView;
	private Button closeButton;
  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>. 
   */
  public PolylinePanel(UserPreferences preferences,
                              PolylineController controller, Activity activity) {
	  //super(new GridBagLayout());
	  super(activity);
    this.controller = controller;
	  this.activity = activity;
	  this.rootView = new LinearLayout(activity);
	  rootView.setOrientation(LinearLayout.VERTICAL);
	  ScrollView sv = new ScrollView(activity);
	  sv.addView(rootView);
	  this.setContentView(sv);
    createComponents(preferences, controller);
    setMnemonics(preferences);
    layoutComponents();
  }
  
  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences,
                                final PolylineController controller) {
    // Create thickness label and spinner bound to controller THICKNESS property
    this.thicknessLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PolylinePanel.class, "thicknessLabel.text", preferences.getLengthUnit().getName()));
    final NullableSpinner.NullableSpinnerLengthModel thicknessSpinnerModel =
        new NullableSpinner.NullableSpinnerLengthModel(preferences, preferences.getLengthUnit().getMinimumLength(), 20f);
    this.thicknessSpinner = new NullableSpinner(activity, thicknessSpinnerModel);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setThickness(thicknessSpinnerModel.getLength());
        }
      });
    controller.addPropertyChangeListener(PolylineController.Property.THICKNESS, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            thicknessSpinnerModel.setLength(controller.getThickness());
          }
        });

    // Create cap style label and combo box bound to controller CAP_STYLE property
    this.arrowsStyleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PolylinePanel.class, "arrowsStyleLabel.text"));
    ArrowsStyle[] arrowsStyles = ArrowsStyle.getArrowsStyle();
    if (controller.getCapStyle() == null) {
      List<ArrowsStyle> arrowsStylesList = new ArrayList<ArrowsStyle>();
      arrowsStylesList.add(null);
      arrowsStylesList.addAll(Arrays.asList(arrowsStyles));
      arrowsStyles = arrowsStylesList.toArray(new ArrowsStyle [arrowsStylesList.size()]);
    }
    this.arrowsStyleComboBox = new JComboBox(activity, new DefaultComboBoxModel(arrowsStyles));
    //this.arrowsStyleComboBox.setMaximumRowCount(arrowsStyles.length);
	  this.arrowsStyleComboBox.setAdapter(new ArrayAdapter<ArrowsStyle>(activity,android.R.layout.simple_list_item_1,arrowsStyles)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView(int position, View convertView, ViewGroup parent)
		  {
			  final ArrowsStyle arrowsStyle = (ArrowsStyle)arrowsStyleComboBox.getItemAtPosition(position);;
			  ImageView imageView;
			  if (convertView == null)
			  {
				  // if it's not recycled, initialize some attributes
				  imageView = new ImageView(activity)
				  {
					  public void onDraw(Canvas canvas)
					  {
						  //super.onDraw(canvas);
						  if (arrowsStyle != null) {
							  Graphics2D g2D = new VMGraphics2D(canvas);//(Graphics2D)g;
							  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
								  g2D.translate(0, 2);
							  }
							  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							  g2D.setColor(Color.BLACK);//list.getForeground());
							  int iconWidth = getWidth();
							  g2D.setStroke(new BasicStroke(2));
							  g2D.drawLine(6, 8, iconWidth - 6, 8);
							  switch (arrowsStyle.getStartArrowStyle()) {
								  case NONE :
									  break;
								  case DISC :
									  g2D.fillOval(4, 4, 9, 9);
									  break;
								  case OPEN :
									  g2D.drawPolyline(new int [] {15, 5, 15}, new int [] {4, 8, 12}, 3);
									  break;
								  case DELTA :
									  g2D.fillPolygon(new int [] {3, 15, 15}, new int [] {8, 3, 13}, 3);
									  break;
							  }
							  switch (arrowsStyle.getEndArrowStyle()) {
								  case NONE :
									  break;
								  case DISC :
									  g2D.fillOval(iconWidth - 12, 4, 9, 9);
									  break;
								  case OPEN :
									  g2D.drawPolyline(new int [] {iconWidth - 14, iconWidth - 4, iconWidth - 14}, new int [] {4, 8, 12}, 3);
									  break;
								  case DELTA :
									  g2D.fillPolygon(new int [] {iconWidth - 2, iconWidth - 14, iconWidth - 14}, new int [] {8, 3, 13}, 3);
									  break;
							  }
						  }
					  }
				  };
				  imageView.setMinimumWidth(64);
				  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				  imageView.setPadding(15, 15, 15, 15);
				  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
			  }
			  else
			  {
				  imageView = (ImageView) convertView;
			  }

			  return imageView;
		  }
	  });
   /* this.arrowsStyleComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list, 
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final ArrowsStyle arrowsStyle = (ArrowsStyle)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          setIcon(new Icon() {
              public int getIconWidth() {
                return 64;
              }
        
              public int getIconHeight() {
                return 16;
              }
        
              public void paintIcon(Component c, Graphics g, int x, int y) {
                if (arrowsStyle != null) {
                  Graphics2D g2D = (Graphics2D)g;
                  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
                    g2D.translate(0, 2);
                  }
                  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2D.setColor(list.getForeground());
                  int iconWidth = getIconWidth();
                  g2D.setStroke(new BasicStroke(2));
                  g2D.drawLine(6, 8, iconWidth - 6, 8);
                  switch (arrowsStyle.getStartArrowStyle()) {
                    case NONE :
                      break;
                    case DISC : 
                      g2D.fillOval(4, 4, 9, 9);
                      break;
                    case OPEN :
                      g2D.drawPolyline(new int [] {15, 5, 15}, new int [] {4, 8, 12}, 3);
                      break;
                    case DELTA :
                      g2D.fillPolygon(new int [] {3, 15, 15}, new int [] {8, 3, 13}, 3);
                      break;
                  }
                  switch (arrowsStyle.getEndArrowStyle()) {
                    case NONE :
                      break;
                    case DISC : 
                      g2D.fillOval(iconWidth - 12, 4, 9, 9);
                      break;
                    case OPEN :
                      g2D.drawPolyline(new int [] {iconWidth - 14, iconWidth - 4, iconWidth - 14}, new int [] {4, 8, 12}, 3);
                      break;
                    case DELTA :
                      g2D.fillPolygon(new int [] {iconWidth - 2, iconWidth - 14, iconWidth - 14}, new int [] {8, 3, 13}, 3);
                      break;
                  }
                }
              }
            });
          return component;
        }
      });*/
    this.arrowsStyleComboBox.addItemListener(new JComboBox.ItemListener() {
      public void itemStateChanged(JComboBox.ItemEvent ev) {
          ArrowsStyle arrowsStyle = (ArrowsStyle)arrowsStyleComboBox.getSelectedItem();
          if (arrowsStyle != null) {
            controller.setStartArrowStyle(arrowsStyle.getStartArrowStyle());
            controller.setEndArrowStyle(arrowsStyle.getEndArrowStyle());
          } else {
            controller.setStartArrowStyle(null);
            controller.setEndArrowStyle(null);
          }
        }
      });
    PropertyChangeListener arrowStyleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ArrowStyle startArrowStyle = controller.getStartArrowStyle();
          ArrowStyle endArrowStyle = controller.getEndArrowStyle();
          if (startArrowStyle != null && endArrowStyle != null) {
            arrowsStyleComboBox.setSelectedItem(new ArrowsStyle(startArrowStyle, endArrowStyle));
          } else {
            arrowsStyleComboBox.setSelectedItem(null);
          }
          arrowsStyleLabel.setEnabled(controller.isArrowsStyleEditable());
          arrowsStyleComboBox.setEnabled(controller.isArrowsStyleEditable());
        }
      };
    controller.addPropertyChangeListener(PolylineController.Property.START_ARROW_STYLE, 
        arrowStyleChangeListener);
    controller.addPropertyChangeListener(PolylineController.Property.END_ARROW_STYLE, 
        arrowStyleChangeListener);
    arrowStyleChangeListener.propertyChange(null);
    
    // Create join style label and combo box bound to controller JOIN_STYLE property
    this.joinStyleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PolylinePanel.class, "joinStyleLabel.text"));
    Polyline.JoinStyle [] joinStyles = Polyline.JoinStyle.values();
    if (controller.getJoinStyle() == null) {
      List<Polyline.JoinStyle> joinStylesList = new ArrayList<Polyline.JoinStyle>();
      joinStylesList.add(null);
      joinStylesList.addAll(Arrays.asList(joinStyles));
      joinStyles = joinStylesList.toArray(new Polyline.JoinStyle [joinStylesList.size()]);
    }
    this.joinStyleComboBox = new JComboBox(activity, new DefaultComboBoxModel(joinStyles));
    final GeneralPath joinPath = new GeneralPath();
    joinPath.moveTo(4, 4);
    joinPath.lineTo(58, 4);
    joinPath.lineTo(36, 14);
    final Shape curvedPath = new Arc2D.Float(-7, 6, 80, 40, 47, 86, Arc2D.OPEN);
	  this.joinStyleComboBox.setAdapter(new ArrayAdapter<Polyline.JoinStyle>(activity, android.R.layout.simple_list_item_1, joinStyles)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView(int position, View convertView, ViewGroup parent)
		  {
			  final Polyline.JoinStyle joinStyle = (Polyline.JoinStyle)joinStyleComboBox.getItemAtPosition(position);
			  ImageView imageView;
			  if (convertView == null)
			  {
				  // if it's not recycled, initialize some attributes
				  imageView = new ImageView(activity)
				  {
					  public void onDraw(Canvas canvas)
					  {
						  //super.onDraw(canvas);
						  if (joinStyle != null) {
							  Graphics2D g2D = new VMGraphics2D(canvas);//(Graphics2D)g;
							  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
								  g2D.translate(0, 2);
							  }
							  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							  g2D.setColor(Color.BLACK);//list.getForeground());
							  g2D.setStroke(SwingTools.getStroke(6, Polyline.CapStyle.BUTT, joinStyle, Polyline.DashStyle.SOLID));
							  if (joinStyle == Polyline.JoinStyle.CURVED) {
								  g2D.draw(curvedPath);
							  } else {
								  g2D.draw(joinPath);
							  }
						  }
					  }
				  };
				  imageView.setMinimumWidth(64);
				  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				  imageView.setPadding(15, 15, 15, 15);
				  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
			  }
			  else
			  {
				  imageView = (ImageView) convertView;
			  }

			  return imageView;
		  }
	  });
    /*this.joinStyleComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list, 
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final Polyline.JoinStyle joinStyle = (Polyline.JoinStyle)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          setIcon(new Icon() {
              public int getIconWidth() {
                return 64;
              }
        
              public int getIconHeight() {
                return 16;
              }
        
              public void paintIcon(Component c, Graphics g, int x, int y) {
                if (joinStyle != null) {
                  Graphics2D g2D = (Graphics2D)g;
                  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
                    g2D.translate(0, 2);
                  }
                  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2D.setColor(list.getForeground());
                  g2D.setStroke(SwingTools.getStroke(6, Polyline.CapStyle.BUTT, joinStyle, Polyline.DashStyle.SOLID));
                  if (joinStyle == Polyline.JoinStyle.CURVED) {
                    g2D.draw(curvedPath);
                  } else {
                    g2D.draw(joinPath);
                  }
                }
              }
            });
          return component;
        }
      });*/
    this.joinStyleComboBox.addItemListener(new JComboBox.ItemListener() {
      public void itemStateChanged(JComboBox.ItemEvent ev) {
          controller.setJoinStyle((Polyline.JoinStyle)joinStyleComboBox.getSelectedItem());
        }
      });
    PropertyChangeListener joinStyleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          joinStyleLabel.setEnabled(controller.isJoinStyleEditable());
          joinStyleComboBox.setEnabled(controller.isJoinStyleEditable());
          joinStyleComboBox.setSelectedItem(controller.getJoinStyle());
        }
      };
    controller.addPropertyChangeListener(PolylineController.Property.JOIN_STYLE, 
        joinStyleChangeListener);
    joinStyleChangeListener.propertyChange(null);
    
    // Create dash style label and combo box bound to controller DASH_STYLE property
    this.dashStyleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PolylinePanel.class, "dashStyleLabel.text"));
    Polyline.DashStyle [] dashStyles = Polyline.DashStyle.values();
    if (controller.getDashStyle() == null) {
      List<Polyline.DashStyle> dashStylesList = new ArrayList<Polyline.DashStyle>();
      dashStylesList.add(null);
      dashStylesList.addAll(Arrays.asList(dashStyles));
      dashStyles = dashStylesList.toArray(new Polyline.DashStyle [dashStylesList.size()]);
    }
    this.dashStyleComboBox = new JComboBox(activity, new DefaultComboBoxModel(dashStyles));
	  this.dashStyleComboBox.setAdapter(new ArrayAdapter<Polyline.DashStyle>(activity, android.R.layout.simple_list_item_1, dashStyles)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView(int position, View convertView, ViewGroup parent)
		  {
			  final Polyline.DashStyle dashStyle = (Polyline.DashStyle)dashStyleComboBox.getItemAtPosition(position);
			  ImageView imageView;
			  if (convertView == null)
			  {
				  // if it's not recycled, initialize some attributes
				  imageView = new ImageView(activity)
				  {
					  public void onDraw(Canvas canvas)
					  {
						  if (dashStyle != null) {
							  Graphics2D g2D = new VMGraphics2D(canvas);//(Graphics2D)g;
							  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
								  g2D.translate(0, 2);
							  }
							  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
							  g2D.setColor(Color.BLACK);//list.getForeground());
							  g2D.setStroke(SwingTools.getStroke(2, Polyline.CapStyle.BUTT, Polyline.JoinStyle.MITER, dashStyle));
							  g2D.drawLine(4, 8, getWidth() - 4, 8);
						  }
					  }
				  };
				  imageView.setMinimumWidth(64);
				  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				  imageView.setPadding(15, 15, 15, 15);
				  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
			  }
			  else
			  {
				  imageView = (ImageView) convertView;
			  }

			  return imageView;
		  }
	  });
    /*this.dashStyleComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list, 
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          final Polyline.DashStyle dashStyle = (Polyline.DashStyle)value;
          final Component component = super.getListCellRendererComponent(
              list, "", index, isSelected, cellHasFocus);
          setIcon(new Icon() {
              public int getIconWidth() {
                return 64;
              }
        
              public int getIconHeight() {
                return 16;
              }
        
              public void paintIcon(Component c, Graphics g, int x, int y) {
                if (dashStyle != null) {
                  Graphics2D g2D = (Graphics2D)g;
                  if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
                    g2D.translate(0, 2);
                  }
                  g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                  g2D.setColor(list.getForeground());
                  g2D.setStroke(SwingTools.getStroke(2, Polyline.CapStyle.BUTT, Polyline.JoinStyle.MITER, dashStyle));
                  g2D.drawLine(4, 8, getIconWidth() - 4, 8);
                }
              }
            });
          return component;
        }
      });*/
    this.dashStyleComboBox.setSelectedItem(controller.getDashStyle());
    this.dashStyleComboBox.addItemListener(new JComboBox.ItemListener() {
      public void itemStateChanged(JComboBox.ItemEvent ev) {
          controller.setDashStyle((Polyline.DashStyle)dashStyleComboBox.getSelectedItem());
        }
      });
    controller.addPropertyChangeListener(PolylineController.Property.DASH_STYLE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            dashStyleComboBox.setSelectedItem(controller.getDashStyle());
          }
        });
    
    // Create color label and its button bound to COLOR controller property
    this.colorLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PolylinePanel.class, "colorLabel.text"));
    this.colorButton = new ColorButton(activity, preferences);
    this.colorButton.setColorDialogTitle(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.PolylinePanel.class, "colorDialog.title"));
    this.colorButton.setColor(controller.getColor());
    this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setColor(colorButton.getColor());
          }
        });
    controller.addPropertyChangeListener(PolylineController.Property.COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            colorButton.setColor(controller.getColor());
          }
        });


    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.PolylinePanel.class, "polyline.title");

	  this.closeButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.HomePane.class, "CLOSE.Name"));
	  closeButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View view)
		  {
			  dismiss();
		  }
	  });
  }
  
  /**
   * Sets components mnemonics and label / component associations.
   */
  private void setMnemonics(UserPreferences preferences) {
  /*  if (!OperatingSystem.isMacOSX()) {
      this.thicknessLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "thicknessLabel.mnemonic")).getKeyCode());
      this.thicknessLabel.setLabelFor(this.thicknessSpinner);
      this.arrowsStyleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "arrowsStyleLabel.mnemonic")).getKeyCode());
      this.arrowsStyleLabel.setLabelFor(this.arrowsStyleComboBox);
      this.joinStyleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "joinStyleLabel.mnemonic")).getKeyCode());
      this.joinStyleLabel.setLabelFor(this.joinStyleComboBox);
      this.dashStyleLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "dashStyleLabel.mnemonic")).getKeyCode());
      this.dashStyleLabel.setLabelFor(this.dashStyleComboBox);
      this.colorLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
          PolylinePanel.class, "colorLabel.mnemonic")).getKeyCode());
      this.colorLabel.setLabelFor(this.colorButton);
    }*/
  }
  
  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
  //  int labelAlignment = OperatingSystem.isMacOSX()
  //      ? GridBagConstraints.LINE_END
  //      : GridBagConstraints.LINE_START;
    //Insets labelInsets = new Insets(0, 0, 5, 5);
	  Resources r = activity.getResources();
	  int px5dp = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, r.getDisplayMetrics());
	  int px10dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, r.getDisplayMetrics());
	  int px15dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 15, r.getDisplayMetrics());
	  int px20dp = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, r.getDisplayMetrics());

	  rootView.setPadding(px10dp,px10dp,px10dp,px10dp);

	  LinearLayout.LayoutParams  rowInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rowInsets.setMargins(px10dp,px10dp,px10dp,px10dp);
	  LinearLayout.LayoutParams  labelInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  labelInsets.setMargins(px10dp,px10dp,px15dp,px15dp);
	  LinearLayout.LayoutParams  labelInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  labelInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px15dp);
	  LinearLayout.LayoutParams  rightComponentInsets = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rightComponentInsets.setMargins(px10dp,px10dp,px15dp,px10dp);
	  LinearLayout.LayoutParams  rightComponentInsetsWithSpace = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
	  rightComponentInsetsWithSpace.setMargins(px10dp,px10dp,px20dp,px10dp);



	  rootView.addView(this.thicknessLabel, rowInsets);
	  rootView.addView(this.thicknessSpinner, rowInsets);
	  rootView.addView(this.arrowsStyleLabel, rowInsets);
	  rootView.addView(this.arrowsStyleComboBox, rowInsets);
	  rootView.addView(this.joinStyleLabel, rowInsets);
	  rootView.addView(this.joinStyleComboBox, rowInsets);
	  rootView.addView(this.dashStyleLabel, rowInsets);
	  rootView.addView(this.dashStyleComboBox, rowInsets);
	  rootView.addView(this.colorLabel, rowInsets);
	  rootView.addView(this.colorButton, rowInsets);

    // First row
/*    add(this.thicknessLabel, new GridBagConstraints(
        0, 0, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    Insets rightComponentInsets = new Insets(0, 0, 5, 0);
    add(this.thicknessSpinner, new GridBagConstraints(
        1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Second row
    add(this.arrowsStyleLabel, new GridBagConstraints(
        0, 1, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.arrowsStyleComboBox, new GridBagConstraints(
        1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Third row
    add(this.joinStyleLabel, new GridBagConstraints(
        0, 2, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.joinStyleComboBox, new GridBagConstraints(
        1, 2, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Fourth row
    add(this.dashStyleLabel, new GridBagConstraints(
        0, 3, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, labelInsets, 0, 0));
    add(this.dashStyleComboBox, new GridBagConstraints(
        1, 3, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, rightComponentInsets, 0, 0));
    // Last row
    add(this.colorLabel, new GridBagConstraints(
        0, 4, 1, 1, 0, 0, labelAlignment, 
        GridBagConstraints.NONE, new Insets(0, 0, 0, 5), 0, 0));
    add(this.colorButton, new GridBagConstraints(
        1, 4, 1, 1, 0, 0, GridBagConstraints.LINE_START, 
        GridBagConstraints.HORIZONTAL, new Insets(0, OperatingSystem.isMacOSX() ? 2  : -1, 0, OperatingSystem.isMacOSX() ? 3  : -1), 0, 0));
        */

	  this.setTitle(dialogTitle);
	  rootView.addView(closeButton, labelInsets);
  }

  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(VCView parentView) {
  /*  if (SwingTools.showConfirmDialog((JComponent)parentView, this, this.dialogTitle,
          ((JSpinner.DefaultEditor)this.thicknessSpinner.getEditor()).getTextField()) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyPolylines();
    }*/
	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modifyPolylines();
		  }
	  });
	  this.show();
  }
  
  /**
   * A tuple storing start and end arrow styles.
   * @author Emmanuel Puybaret
   */
  private static class ArrowsStyle {
    private static List<ArrowsStyle> arrowsStyle;
    private final ArrowStyle startArrowStyle;
    private final ArrowStyle endArrowStyle;
    
    public ArrowsStyle(ArrowStyle startArrowStyle, ArrowStyle endArrowStyle) {
      this.startArrowStyle = startArrowStyle;
      this.endArrowStyle = endArrowStyle;
    }
    
    public ArrowStyle getStartArrowStyle() {
      return this.startArrowStyle;
    }
    
    public ArrowStyle getEndArrowStyle() {
      return this.endArrowStyle;
    }
    
    
    
    @Override
    public int hashCode() {
      int hashCode = 0;
      if (this.startArrowStyle != null) {
        hashCode = this.startArrowStyle.hashCode();
      }
      if (this.endArrowStyle != null) {
        hashCode += this.endArrowStyle.hashCode();
      }
      return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof ArrowsStyle) {
        ArrowsStyle arrowsStyle = (ArrowsStyle)obj;
        return this.startArrowStyle == arrowsStyle.startArrowStyle
            && this.endArrowStyle == arrowsStyle.endArrowStyle;
      } else {
        return false;
      }
    }

    public static ArrowsStyle [] getArrowsStyle() {
      if (arrowsStyle == null) {
        ArrowStyle [] arrowStyles = ArrowStyle.values();
        arrowsStyle = new ArrayList<ArrowsStyle>(arrowStyles.length * arrowStyles.length);
        for (ArrowStyle startArrowStyle : arrowStyles) {
          for (ArrowStyle endArrowStyle : arrowStyles) {
            arrowsStyle.add(new ArrowsStyle(startArrowStyle, endArrowStyle));
          }
        }
      }
      return arrowsStyle.toArray(new ArrowsStyle [arrowsStyle.size()]);
    }
  }
}
