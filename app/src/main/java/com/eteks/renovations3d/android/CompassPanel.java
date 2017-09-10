/*
 * CompassPanel.java 22 sept. 2010
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

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JSpinnerJogDial;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.SpinnerNumberModel;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.swingish.ChangeListener;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.CompassController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.mindblowing.renovations3d.R;


import javaawt.BasicStroke;
import javaawt.Color;
import javaawt.Graphics2D;
import javaawt.RenderingHints;
import javaawt.VMGraphics2D;
import javaawt.geom.Ellipse2D;
import javaawt.geom.GeneralPath;
import javaawt.geom.Line2D;


/**
 * Compass editing panel.
 * @author Emmanuel Puybaret
 */
public class CompassPanel extends AndroidDialogView implements DialogView {
	private static final float DIRECTION_COMP_DP = 100;
	private final CompassController controller;
  private JLabel xLabel;
  private JSpinner xSpinner;
  private JLabel                  yLabel;
  private JSpinner ySpinner;
  private JLabel                  diameterLabel;
  private JSpinner diameterSpinner;
  private JCheckBox visibleCheckBox;
  private ImageView northDirectionComponent;
  private JLabel                  longitudeLabel;
  private AutoCommitSpinner longitudeSpinner;
  private JLabel                  latitudeLabel;
  private AutoCommitSpinner latitudeSpinner;
  private JLabel                  timeZoneLabel;
  private JComboBox timeZoneComboBox;
  private JLabel                  northDirectionLabel;
  private JSpinnerJogDial northDirectionSpinner;
  private String                  dialogTitle;

  /**
   * Creates a panel that displays compass data.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public CompassPanel(UserPreferences preferences,
                      CompassController controller,
		  				Activity activity) {

	  super(preferences, activity, R.layout.dialog_compasspanel);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents(preferences);
  }

  /**
   * Creates and initializes components.
   */
  private void createComponents(UserPreferences preferences, 
                                final CompassController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();

    // Create X label and its spinner bound to X controller property
    this.xLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.CompassPanel.class, "xLabel.text", unitName));
    float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel xSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xSpinner = new NullableSpinner(activity, xSpinnerModel, true);
    xSpinnerModel.setLength(controller.getX());
    final PropertyChangeListener xChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.X, xChangeListener);
    xSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.X, xChangeListener);
          controller.setX((float)xSpinnerModel.getLength());
          controller.addPropertyChangeListener(CompassController.Property.X, xChangeListener);
        }
      });
    
    // Create Y label and its spinner bound to Y controller property
    this.yLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.CompassPanel.class, "yLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel ySpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.ySpinner = new NullableSpinner(activity, ySpinnerModel, true);
    ySpinnerModel.setLength(controller.getY());
    final PropertyChangeListener yChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ySpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.Y, yChangeListener);
    ySpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.Y, yChangeListener);
          controller.setY(ySpinnerModel.getLength());
          controller.addPropertyChangeListener(CompassController.Property.Y, yChangeListener);
        }
      });
    
    // Create diameter label and its spinner bound to DIAMETER controller property
    this.diameterLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.CompassPanel.class, "diameterLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel diameterSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences,
            preferences.getLengthUnit().getMinimumLength(), preferences.getLengthUnit().getMaximumLength()  / 10);
    this.diameterSpinner = new NullableSpinner(activity, diameterSpinnerModel, true);
    diameterSpinnerModel.setLength(controller.getDiameter());
    final PropertyChangeListener diameterChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          diameterSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.DIAMETER, 
        diameterChangeListener);
    diameterSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.DIAMETER, 
              diameterChangeListener);
          controller.setDiameter(diameterSpinnerModel.getLength());
          controller.addPropertyChangeListener(CompassController.Property.DIAMETER, 
              diameterChangeListener);
        }
      });
    
    // Create visible check box bound to VISIBLE controller property
    this.visibleCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.CompassPanel.class, "visibleCheckBox.text"));
    this.visibleCheckBox.setSelected(controller.isVisible());
    final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
      public void propertyChange(PropertyChangeEvent ev) {
        visibleCheckBox.setSelected((Boolean)ev.getNewValue());
      }
    };
    controller.addPropertyChangeListener(CompassController.Property.VISIBLE, visibleChangeListener);
    this.visibleCheckBox.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.VISIBLE, visibleChangeListener);
          controller.setVisible(visibleCheckBox.isSelected());
          controller.addPropertyChangeListener(CompassController.Property.VISIBLE, visibleChangeListener);
        }
      });

    this.latitudeLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.CompassPanel.class, "latitudeLabel.text"));
    final SpinnerNumberModel latitudeSpinnerModel = new SpinnerNumberModel(new Float(0), new Float(-90), new Float(90), new Float(5));
    this.latitudeSpinner = new AutoCommitSpinner(activity, latitudeSpinnerModel);
	  latitudeSpinner.setFormat(new DecimalFormat("N ##0;S ##0"));
    // Change positive / negative notation by North / South
//    JFormattedTextField textField = ((DefaultEditor)this.latitudeSpinner.getEditor()).getTextField();
//    NumberFormatter numberFormatter = (NumberFormatter)((DefaultFormatterFactory)textField.getFormatterFactory()).getDefaultFormatter();
//    numberFormatter.setFormat(new DecimalFormat("N ##0.000;S ##0.000"));
//    textField.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
//    SwingTools.addAutoSelectionOnFocusGain(textField);
    latitudeSpinnerModel.setValue(controller.getLatitudeInDegrees());
    final PropertyChangeListener latitudeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          latitudeSpinnerModel.setValue(ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.LATITUDE_IN_DEGREES, latitudeChangeListener);
    latitudeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.LATITUDE_IN_DEGREES, latitudeChangeListener);
          controller.setLatitudeInDegrees(((Number)latitudeSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(CompassController.Property.LATITUDE_IN_DEGREES, latitudeChangeListener);
        }
      });
    
    this.longitudeLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.CompassPanel.class, "longitudeLabel.text"));
    final SpinnerNumberModel longitudeSpinnerModel = new SpinnerNumberModel(new Float(0), new Float(-180), new Float(180), new Float(5));
    this.longitudeSpinner = new AutoCommitSpinner(activity, longitudeSpinnerModel);
	  longitudeSpinner.setFormat(new DecimalFormat("E ##0;W ##0"));
    // Change positive / negative notation by East / West
//    textField = ((DefaultEditor)this.longitudeSpinner.getEditor()).getTextField();
//    numberFormatter = (NumberFormatter)((DefaultFormatterFactory)textField.getFormatterFactory()).getDefaultFormatter();
//    numberFormatter.setFormat(new DecimalFormat("E ##0.000;W ##0.000"));
//    textField.setFormatterFactory(new DefaultFormatterFactory(numberFormatter));
//    SwingTools.addAutoSelectionOnFocusGain(textField);
    longitudeSpinnerModel.setValue(controller.getLongitudeInDegrees());
    final PropertyChangeListener longitudeChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          longitudeSpinnerModel.setValue(ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.LONGITUDE_IN_DEGREES, longitudeChangeListener);
    longitudeSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.LONGITUDE_IN_DEGREES, longitudeChangeListener);
          controller.setLongitudeInDegrees(((Number)longitudeSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(CompassController.Property.LONGITUDE_IN_DEGREES, longitudeChangeListener);
        }
      });

    this.timeZoneLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.CompassPanel.class, "timeZoneLabel.text"));
    List<String> timeZoneIds = new ArrayList<String>(Arrays.asList(TimeZone.getAvailableIDs()));
    // Remove synonymous time zones
    timeZoneIds.remove("GMT");
    timeZoneIds.remove("GMT0");
    timeZoneIds.remove("Etc/GMT0");
    timeZoneIds.remove("Etc/GMT-0");
    timeZoneIds.remove("Etc/GMT+0");
    // Replace Etc/GMT... ids by their English value that are less misleading
    for (int i = 0; i < timeZoneIds.size(); i++) {
      String timeZoneId = timeZoneIds.get(i);
      if (timeZoneId.startsWith("Etc/GMT")) {
        timeZoneIds.set(i, TimeZone.getTimeZone(timeZoneId).getDisplayName(Locale.ENGLISH));
      }
    }
    String [] timeZoneIdsArray = timeZoneIds.toArray(new String [timeZoneIds.size()]);
    Arrays.sort(timeZoneIdsArray);
    this.timeZoneComboBox = new JComboBox(activity, timeZoneIdsArray);

    final PropertyChangeListener timeZoneChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
			timeZoneComboBox.setSelectedItem(ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.TIME_ZONE, timeZoneChangeListener);
	  this.timeZoneComboBox.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, timeZoneIdsArray)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView(int position, View convertView, ViewGroup parent)
		  {
			  String timeZoneId = (String)timeZoneComboBox.getItemAtPosition(position);
			  if (timeZoneId.startsWith("GMT")) {
				  //if (!OperatingSystem.isMacOSX()) {
					  //setToolTipText(timeZoneId);
				  //}
			  } else {
				  String timeZoneDisplayName = TimeZone.getTimeZone(timeZoneId).getDisplayName();
				  //if (OperatingSystem.isMacOSX()) {
					  timeZoneId = timeZoneId + " - " + timeZoneDisplayName;
				  //} else {
					  // Use tool tip do display the complete time zone information
					  //setToolTipText(timeZoneId + " - " + timeZoneDisplayName);
				  //}
			  }
			  TextView ret = new TextView(getContext());
			  ret.setText(timeZoneId);
			  return ret;
		  }
	  });

	  // moved to after the adpater is set, as the android adapter is both model and renderer
	  this.timeZoneComboBox.setSelectedItem(controller.getTimeZone());
    /*this.timeZoneComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                                                      boolean cellHasFocus) {
          String timeZoneId = (String)value;
          if (timeZoneId.startsWith("GMT")) {
            if (!OperatingSystem.isMacOSX()) {
              setToolTipText(timeZoneId);
            }
          } else {
            String timeZoneDisplayName = TimeZone.getTimeZone(timeZoneId).getDisplayName();
            if (OperatingSystem.isMacOSX()) {
              value = timeZoneId + " - " + timeZoneDisplayName;
            } else {
              // Use tool tip do display the complete time zone information
              setToolTipText(timeZoneId + " - " + timeZoneDisplayName);
            }
          }
          return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
      });*/

    this.timeZoneComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.TIME_ZONE, timeZoneChangeListener);
          controller.setTimeZone((String)timeZoneComboBox.getSelectedItem());
          controller.addPropertyChangeListener(CompassController.Property.TIME_ZONE, timeZoneChangeListener);
        }
      });
    //this.timeZoneComboBox.setPrototypeDisplayValue("GMT");
    
    this.northDirectionLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.CompassPanel.class, "northDirectionLabel.text"));
    // Create a spinner model able to choose an angle modulo 360
    final SpinnerNumberModel northDirectionSpinnerModel = new SpinnerModuloNumberModel(0, 0, 360, 5);
    this.northDirectionSpinner = new AutoCommitSpinnerJogDial(activity, northDirectionSpinnerModel);
    northDirectionSpinnerModel.setValue(new Integer(Math.round(controller.getNorthDirectionInDegrees())));
    this.northDirectionComponent = new android.support.v7.widget.AppCompatImageView(activity){//JComponent() {
		public void onDraw(Canvas canvas)
		{

			Graphics2D g2D = new VMGraphics2D(canvas);//(Graphics2D)g;

			g2D.setColor(Color.black);

          g2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2D.translate(getWidth() / 2, getHeight() / 2);
          g2D.scale(getWidth() / 2, getWidth() / 2);
          g2D.rotate(Math.toRadians(controller.getNorthDirectionInDegrees()));
          // Draw a round arc
          g2D.setStroke(new BasicStroke(0.5f / getWidth()));
          g2D.draw(new Ellipse2D.Float(-0.7f, -0.7f, 1.4f, 1.4f));
          g2D.draw(new Line2D.Float(-0.85f, 0, -0.7f, 0));
          g2D.draw(new Line2D.Float(0.85f, 0, 0.7f, 0));
          g2D.draw(new Line2D.Float(0, -0.8f, 0, -0.7f));
          g2D.draw(new Line2D.Float(0, 0.85f, 0, 0.7f));
          // Draw a N
          GeneralPath path = new GeneralPath();
          path.moveTo(-0.1f, -0.8f);
          path.lineTo(-0.1f, -1f);
          path.lineTo(0.1f, -0.8f);
          path.lineTo(0.1f, -1f);
          g2D.setStroke(new BasicStroke(1.5f / getWidth()));
          g2D.draw(path);
          // Draw the needle
          GeneralPath needlePath = new GeneralPath();
          needlePath.moveTo(0, -0.75f);
          needlePath.lineTo(0.2f, 0.7f);
          needlePath.lineTo(0, 0.5f);
          needlePath.lineTo(-0.2f, 0.7f);
          needlePath.closePath();
          needlePath.moveTo(-0.02f, 0);
          needlePath.lineTo(0.02f, 0);
          g2D.setStroke(new BasicStroke(4 / getWidth()));
          g2D.draw(needlePath);



        }
      };
	  final float scale = activity.getResources().getDisplayMetrics().density;
	  int iconHeightPx = (int) (DIRECTION_COMP_DP * scale + 0.5f);
	  northDirectionComponent.setMinimumWidth(iconHeightPx);
	  northDirectionComponent.setMinimumHeight(iconHeightPx);
    //this.northDirectionComponent.setOpaque(false);
    final PropertyChangeListener northDirectionChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          northDirectionSpinnerModel.setValue(((Number)ev.getNewValue()).intValue());
          //northDirectionComponent.repaint();
			northDirectionComponent.postInvalidate();
        }
      };
    controller.addPropertyChangeListener(CompassController.Property.NORTH_DIRECTION_IN_DEGREES, northDirectionChangeListener);
    northDirectionSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(CompassController.Property.NORTH_DIRECTION_IN_DEGREES, northDirectionChangeListener);
          controller.setNorthDirectionInDegrees(((Number)northDirectionSpinnerModel.getValue()).floatValue());
          //northDirectionComponent.repaint();
			northDirectionComponent.postInvalidate();
          controller.addPropertyChangeListener(CompassController.Property.NORTH_DIRECTION_IN_DEGREES, northDirectionChangeListener);
        }
      });

    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.CompassPanel.class, "compass.title");



	 Button setToLocationButton = (Button)this.findViewById(R.id.compasspanel_locationButton);
	  if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
	  {
		  setToLocationButton.setOnClickListener(new View.OnClickListener()
		  {
			  @Override
			  public void onClick(View v)
			  {
				  if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
				  {
					  // Acquire a reference to the system Location Manager
					  LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

					  Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
					  if(location == null)
					  {
						  location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
					  }

					  // possibly nothing has been returned
					  if(location != null)
					  {
						  longitudeSpinner.setValue(location.getLongitude());
						  latitudeSpinner.setValue(location.getLatitude());
						  longitudeSpinner.postInvalidate();
						  latitudeSpinner.postInvalidate();
					  }
				  }
			  }
		  });
	  }
	  else
	  {
		  setToLocationButton.setEnabled(false);
	  }
  }

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences) {

	  JLabel compassRosePanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.CompassPanel.class, "compassRosePanel.title"));
	  swapOut(compassRosePanel, R.id.compasspanel_compassRosePanel);
	  swapOut(this.xLabel, R.id.compasspanel_xLabel);
	  swapOut(this.xSpinner, R.id.compasspanel_xSpinner);
	  swapOut(this.visibleCheckBox, R.id.compasspanel_visibleCheckBox);
	  swapOut(this.yLabel, R.id.compasspanel_yLabel);
	  swapOut(this.ySpinner, R.id.compasspanel_ySpinner);
	  swapOut(this.diameterLabel, R.id.compasspanel_diameterLabel);
	  swapOut(this.diameterSpinner, R.id.compasspanel_diameterSpinner);

	  JLabel geographicLocationPanel = new JLabel(activity, preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.CompassPanel.class, "geographicLocationPanel.title"));
	  swapOut(geographicLocationPanel, R.id.compasspanel_geographicLocationPanel);
	  swapOut(this.latitudeLabel, R.id.compasspanel_latitudeLabel);
	  swapOut(this.latitudeSpinner, R.id.compasspanel_latitudeSpinner);
	  swapOut(this.northDirectionLabel, R.id.compasspanel_northDirectionLabel);
	  swapOut(this.northDirectionSpinner, R.id.compasspanel_northDirectionSpinner);
	  swapOut(this.northDirectionComponent, R.id.compasspanel_northDirectionComponent);
	  swapOut(this.longitudeLabel, R.id.compasspanel_longitudeLabel);
	  swapOut(this.longitudeSpinner, R.id.compasspanel_longitudeSpinner);
	  swapOut(this.timeZoneLabel, R.id.compasspanel_timeZoneLabel);
	  swapOut(this.timeZoneComboBox, R.id.compasspanel_timeZoneComboBox);

	  this.setTitle(dialogTitle);
	  swapOut(closeButton, R.id.compasspanel_closeButton);
  }

  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
  /*  JFormattedTextField northDirectionTextField =
        ((JSpinner.DefaultEditor)this.northDirectionSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, northDirectionTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyCompass();
    }*/
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modifyCompass();
		  }
	  });
	  this.show();
  }
}
