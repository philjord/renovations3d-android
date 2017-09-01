/*
 * PolylinePanel.java 
 *
 * Copyright (c) 2009 Plan PHP All Rights Reserved.
 */
package com.eteks.renovations3d.android;


import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mindblowing.swingish.DefaultComboBoxModel;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JSpinner;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.swingish.ChangeListener;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.Polyline.ArrowStyle;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.PolylineController;
import com.mindblowing.renovations3d.R;

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
public class PolylinePanel extends AndroidDialogView implements DialogView {
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

  /**
   * Creates a preferences panel that layouts the editable properties
   * of its <code>controller</code>. 
   */
  public PolylinePanel(UserPreferences preferences,
                              PolylineController controller, Activity activity) {
	  //super(new GridBagLayout());
	  super(preferences, activity, R.layout.dialog_polylinepanel);
    this.controller = controller;
    createComponents(preferences, controller);
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
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel thicknessSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, preferences.getLengthUnit().getMinimumLength(), 20f);
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
	  this.arrowsStyleComboBox.setAdapter(new ArrayAdapter<ArrowsStyle>(activity, android.R.layout.simple_list_item_1, arrowsStyles)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView(int position, View convertView, ViewGroup parent)
		  {
			  ArrowsStyle arrowsStyle = (ArrowsStyle)arrowsStyleComboBox.getItemAtPosition(position);
			  ArrowStyleImageView imageView;
			  if (convertView == null)
			  {
				  // if it's not recycled, initialize some attributes
				  imageView = new ArrowStyleImageView(getContext());
				  imageView.setMinimumWidth(64);
				  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				  imageView.setPadding(15, 15, 15, 15);
				  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
			  }
			  else
			  {
				  imageView = (ArrowStyleImageView) convertView;
			  }

			  imageView.setArrowStyle(arrowsStyle);

			  return imageView;
		  }
	  });

    this.arrowsStyleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
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
			  Polyline.JoinStyle joinStyle = (Polyline.JoinStyle)joinStyleComboBox.getItemAtPosition(position);
			  PolylineJoinStyleImageView imageView;
			  if (convertView == null)
			  {
				  // if it's not recycled, initialize some attributes
				  imageView = new PolylineJoinStyleImageView(activity);
				  imageView.setMinimumWidth(64);
				  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				  imageView.setPadding(15, 15, 15, 15);
				  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
			  }
			  else
			  {
				  imageView = (PolylineJoinStyleImageView) convertView;
			  }
			  imageView.setJoinStyle(joinStyle);

			  return imageView;
		  }
	  });

    this.joinStyleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
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
			  Polyline.DashStyle dashStyle = (Polyline.DashStyle)dashStyleComboBox.getItemAtPosition(position);
			  PolylineDashStyleImageView imageView;
			  if (convertView == null)
			  {
				  // if it's not recycled, initialize some attributes
				  imageView = new PolylineDashStyleImageView(activity);
				  imageView.setMinimumWidth(64);
				  imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				  imageView.setPadding(15, 15, 15, 15);
				  //imageView.setBackgroundColor(android.graphics.Color.WHITE);
			  }
			  else
			  {
				  imageView = (PolylineDashStyleImageView) convertView;
			  }

			  imageView.setDashStyle(dashStyle);

			  return imageView;
		  }
	  });

    this.dashStyleComboBox.setSelectedItem(controller.getDashStyle());
    this.dashStyleComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent ev) {
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
  }


	private class ArrowStyleImageView extends  android.support.v7.widget.AppCompatImageView
	{
		private ArrowsStyle arrowsStyle;
		public ArrowStyleImageView(Context context)
		{
			super(context);
		}

		public void setArrowStyle(ArrowsStyle arrowsStyle)
		{
			this.arrowsStyle = arrowsStyle;
		}

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
	}

	public static class PolylineJoinStyleImageView extends  android.support.v7.widget.AppCompatImageView
	{
		private Polyline.JoinStyle joinStyle;

		private static GeneralPath joinPath = new GeneralPath();
		static{
			joinPath.moveTo(4, 4);
			joinPath.lineTo(58, 4);
			joinPath.lineTo(36, 14);}

		//PJ Desktop take a 3 o'clock start and positive = counter clock wise
		// hence android starts at 9 o'clock + angle and a clockwise (android is always)
		private static Shape curvedPath = new Arc2D.Float(-7, 6, 80, 40, 47 + 180, 86, Arc2D.OPEN);

		public PolylineJoinStyleImageView(Context context)
		{
			super(context);
		}

		public void setJoinStyle(Polyline.JoinStyle joinStyle)
		{
			this.joinStyle = joinStyle;
		}

		@Override
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


	}
	private class PolylineDashStyleImageView extends android.support.v7.widget.AppCompatImageView
	{
		private Polyline.DashStyle dashStyle;
		public PolylineDashStyleImageView(Context context)
		{
			super(context);
		}

		public void setDashStyle(Polyline.DashStyle dashStyle)
		{
			this.dashStyle = dashStyle;
		}

		@Override
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


	}

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {
	  swapOut(this.thicknessLabel, R.id.polyline_panel_thicknessLabel);
	  swapOut(this.thicknessSpinner, R.id.polyline_panel_thicknessSpinner);
	  swapOut(this.arrowsStyleLabel, R.id.polyline_panel_arrowsLabel);
	  swapOut(this.arrowsStyleComboBox, R.id.polyline_panel_arrowsCombobox);
	  swapOut(this.joinStyleLabel, R.id.polyline_panel_joinLabel);
	  swapOut(this.joinStyleComboBox, R.id.polyline_panel_joinCombobox);

	  //PJ remvoed as not usable in VMGraphics2D
	  removeView(R.id.polyline_panel_dashLabel);
	  removeView(R.id.polyline_panel_dashCombobox);

	  //swapOut(this.dashStyleLabel, R.id.polyline_panel_dashLabel);
	  //swapOut(this.dashStyleComboBox, R.id.polyline_panel_dashCombobox);
	  swapOut(this.colorLabel, R.id.polyline_panel_colorLabel);
	  swapOut(this.colorButton, R.id.polyline_panel_colorButton);

	  this.setTitle(dialogTitle);
	  swapOut(closeButton, R.id.polyline_panel_closeButton);
  }

  /**
   * Displays this panel in a dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
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
