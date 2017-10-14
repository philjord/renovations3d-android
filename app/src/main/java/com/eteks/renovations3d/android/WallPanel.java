/*
 * WallPanel.java 29 mai 07
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
import android.content.DialogInterface;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;


import com.mindblowing.swingish.ButtonGroup;
import com.mindblowing.swingish.DefaultComboBoxModel;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JRadioButton;
import com.mindblowing.swingish.JSpinnerJogDial;
import com.mindblowing.swingish.JSpinner;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.mindblowing.swingish.ChangeListener;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.WallController;
import com.mindblowing.renovations3d.R;

/**
 * Wall editing panel.
 * @author Emmanuel Puybaret
 */
public class WallPanel extends AndroidDialogView implements DialogView {
  private final WallController controller;
  private JLabel               xStartLabel;
  private JSpinner xStartSpinner;
  private JLabel               yStartLabel;
  private JSpinner yStartSpinner;
  private JLabel               xEndLabel;
  private JSpinner xEndSpinner;
  private JLabel               yEndLabel;
  private JSpinner yEndSpinner;
  private JLabel               distanceToEndPointLabel;
  private JSpinner distanceToEndPointSpinner;
  private JRadioButton         leftSideColorRadioButton;
  private ColorButton          leftSideColorButton;
  private JRadioButton         leftSideTextureRadioButton;
  private JButton           leftSideTextureComponent;
  private JRadioButton         leftSideMattRadioButton;
  private JButton              leftSideBaseboardButton;
  private JRadioButton         leftSideShinyRadioButton;
  private JRadioButton         rightSideColorRadioButton;
  private ColorButton          rightSideColorButton;
  private JRadioButton         rightSideTextureRadioButton;
  private JButton           rightSideTextureComponent;
  private JRadioButton         rightSideMattRadioButton;
  private JRadioButton         rightSideShinyRadioButton;
  private JButton              rightSideBaseboardButton;
  private JLabel               patternLabel;
  private JComboBox            patternComboBox;
  private JLabel               topColorLabel;
  private JRadioButton         topDefaultColorRadioButton;
  private JRadioButton         topColorRadioButton;
  private ColorButton          topColorButton;
  private JRadioButton         rectangularWallRadioButton;
  private JLabel               rectangularWallHeightLabel;
  private JSpinner rectangularWallHeightSpinner;
  private JRadioButton         slopingWallRadioButton;
  private JLabel               slopingWallHeightAtStartLabel;
  private JSpinner slopingWallHeightAtStartSpinner;
  private JLabel               slopingWallHeightAtEndLabel;
  private JSpinner slopingWallHeightAtEndSpinner;
  private JLabel               thicknessLabel;
  private JSpinner thicknessSpinner;
  private JLabel               arcExtentLabel;
  private JSpinnerJogDial arcExtentSpinner;
  private JLabel               wallOrientationLabel;
  private String               dialogTitle;


  /**
   * Creates a panel that displays wall data according to the units set in
   * <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public WallPanel(UserPreferences preferences,
                   WallController controller, Activity activity) {
	  //super(new GridBagLayout());
	  super(preferences, activity, R.layout.dialog_wallpanel);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents(preferences, controller);
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(final UserPreferences preferences, 
                                final WallController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();
    
    // Create X start label and its spinner bound to X_START controller property
    this.xStartLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "xLabel.text", unitName));
    final float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel xStartSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xStartSpinner = new NullableSpinner(activity, xStartSpinnerModel, true);
    xStartSpinnerModel.setNullable(controller.getXStart() == null);
    xStartSpinnerModel.setLength(controller.getXStart());
    final PropertyChangeListener xStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xStartSpinnerModel.setNullable(ev.getNewValue() == null);
          xStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
    xStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
          controller.setXStart(xStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.X_START, xStartChangeListener);
        }
      });
    
    // Create Y start label and its spinner bound to Y_START controller property
    this.yStartLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "yLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel yStartSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yStartSpinner = new NullableSpinner(activity, yStartSpinnerModel, true);
    yStartSpinnerModel.setNullable(controller.getYStart() == null);
    yStartSpinnerModel.setLength(controller.getYStart());
    final PropertyChangeListener yStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yStartSpinnerModel.setNullable(ev.getNewValue() == null);
          yStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
    yStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
          controller.setYStart(yStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.Y_START, yStartChangeListener);
        }
      });
    
    // Create X end label and its spinner bound to X_END controller property
    this.xEndLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "xLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel xEndSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.xEndSpinner = new NullableSpinner(activity, xEndSpinnerModel, true);
    xEndSpinnerModel.setNullable(controller.getXEnd() == null);
    xEndSpinnerModel.setLength(controller.getXEnd());
    final PropertyChangeListener xEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          xEndSpinnerModel.setNullable(ev.getNewValue() == null);
          xEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
    xEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
          controller.setXEnd(xEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.X_END, xEndChangeListener);
        }
      });
    
    // Create Y end label and its spinner bound to Y_END controller property
    this.yEndLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "yLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel yEndSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, -maximumLength, maximumLength);
    this.yEndSpinner = new NullableSpinner(activity, yEndSpinnerModel, true);
    yEndSpinnerModel.setNullable(controller.getYEnd() == null);
    yEndSpinnerModel.setLength(controller.getYEnd());
    final PropertyChangeListener yEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          yEndSpinnerModel.setNullable(ev.getNewValue() == null);
          yEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
    yEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
          controller.setYEnd(yEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.Y_END, yEndChangeListener);
        }
      });

    // Create distance to end point label and its spinner bound to DISTANCE_TO_END_POINT controller property
    this.distanceToEndPointLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "distanceToEndPointLabel.text", unitName));
    float minimumLength = preferences.getLengthUnit().getMinimumLength();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel distanceToEndPointSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, 2 * maximumLength * (float)Math.sqrt(2));
    this.distanceToEndPointSpinner = new NullableSpinner(activity, distanceToEndPointSpinnerModel, true);
    distanceToEndPointSpinnerModel.setNullable(controller.getLength() == null);
    distanceToEndPointSpinnerModel.setLength(controller.getDistanceToEndPoint());
    final PropertyChangeListener distanceToEndPointChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          distanceToEndPointSpinnerModel.setNullable(ev.getNewValue() == null);
          distanceToEndPointSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.DISTANCE_TO_END_POINT, 
        distanceToEndPointChangeListener);
    distanceToEndPointSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.DISTANCE_TO_END_POINT, 
              distanceToEndPointChangeListener);
          controller.setDistanceToEndPoint(distanceToEndPointSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.DISTANCE_TO_END_POINT, 
              distanceToEndPointChangeListener);
        }
      });

    // Left side color and texture buttons bound to left side controller properties
    this.leftSideColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideColorRadioButton.text"));
    this.leftSideColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideColorRadioButton.isSelected()) {
            controller.setLeftSidePaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateLeftSideColorRadioButtons(controller);
          }
        });
    
    this.leftSideColorButton = new ColorButton(activity, preferences);
    this.leftSideColorButton.setColorDialogTitle(preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideColorDialog.title"));
    this.leftSideColorButton.setColor(controller.getLeftSideColor());
    this.leftSideColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setLeftSideColor(leftSideColorButton.getColor());
            controller.setLeftSidePaint(WallController.WallPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            leftSideColorButton.setColor(controller.getLeftSideColor());
          }
        });

    this.leftSideTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideTextureRadioButton.text"));
    this.leftSideTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideTextureRadioButton.isSelected()) {
            controller.setLeftSidePaint(WallController.WallPaint.TEXTURED);
          }
        }
      });
    
    this.leftSideTextureComponent = (JButton)controller.getLeftSideTextureController().getView();

    ButtonGroup leftSideColorButtonGroup = new ButtonGroup();
    leftSideColorButtonGroup.add(this.leftSideColorRadioButton);
    leftSideColorButtonGroup.add(this.leftSideTextureRadioButton);
    updateLeftSideColorRadioButtons(controller);    

    // Left side shininess radio buttons bound to LEFT_SIDE_SHININESS controller property
    this.leftSideMattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideMattRadioButton.text"));
    this.leftSideMattRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideMattRadioButton.isSelected()) {
            controller.setLeftSideShininess(0f);
          }
        }
      });
    PropertyChangeListener leftSideShininessListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateLeftSideShininessRadioButtons(controller);
        }
      };
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_SHININESS, 
        leftSideShininessListener);

    this.leftSideShinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideShinyRadioButton.text"));
    this.leftSideShinyRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (leftSideShinyRadioButton.isSelected()) {
            controller.setLeftSideShininess(0.25f);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.LEFT_SIDE_SHININESS, 
        leftSideShininessListener);
    
    ButtonGroup leftSideShininessButtonGroup = new ButtonGroup();
    leftSideShininessButtonGroup.add(this.leftSideMattRadioButton);
    leftSideShininessButtonGroup.add(this.leftSideShinyRadioButton);
    updateLeftSideShininessRadioButtons(controller);

   /* this.leftSideBaseboardButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(preferences, com.eteks.sweethome3d.android_props.WallPanel.class, "MODIFY_LEFT_SIDE_BASEBOARD", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            editBaseboard((JComponent)ev.getSource(), 
                preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideBaseboardDialog.title"),
                controller.getLeftSideBaseboardController());
          }
        }));*/
	  this.leftSideBaseboardButton = new JButton(activity,
			  SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.WallPanel.class, "MODIFY_LEFT_SIDE_BASEBOARD.Name"));
	  leftSideBaseboardButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View view)
		  {
			  editBaseboard(null,
					  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "leftSideBaseboardDialog.title"),
					  controller.getLeftSideBaseboardController());
		  }
	  });
    
    // Right side color and texture buttons bound to right side controller properties
    this.rightSideColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideColorRadioButton.text"));
    this.rightSideColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (rightSideColorRadioButton.isSelected()) {
            controller.setRightSidePaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateRightSideColorRadioButtons(controller);
          }
        });

    this.rightSideColorButton = new ColorButton(activity, preferences);
    this.rightSideColorButton.setColor(controller.getRightSideColor());
    this.rightSideColorButton.setColorDialogTitle(preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideColorDialog.title"));
    this.rightSideColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setRightSideColor(rightSideColorButton.getColor());
            controller.setRightSidePaint(WallController.WallPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            rightSideColorButton.setColor(controller.getRightSideColor());
          }
        });
    
    this.rightSideTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideTextureRadioButton.text"));
    this.rightSideTextureRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent e) {
          if (rightSideTextureRadioButton.isSelected()) {
            controller.setRightSidePaint(WallController.WallPaint.TEXTURED);
          }
        }
      });
  
    this.rightSideTextureComponent = (JButton)controller.getRightSideTextureController().getView();

    ButtonGroup rightSideColorButtonGroup = new ButtonGroup();
    rightSideColorButtonGroup.add(this.rightSideColorRadioButton);
    rightSideColorButtonGroup.add(this.rightSideTextureRadioButton);
    updateRightSideColorRadioButtons(controller);

    // Right side shininess radio buttons bound to LEFT_SIDE_SHININESS controller property
    this.rightSideMattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideMattRadioButton.text"));
    this.rightSideMattRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rightSideMattRadioButton.isSelected()) {
            controller.setRightSideShininess(0f);
          }
        }
      });
    PropertyChangeListener rightSideShininessListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateRightSideShininessRadioButtons(controller);
        }
      };
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_SHININESS, 
        rightSideShininessListener);

    this.rightSideShinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideShinyRadioButton.text"));
    this.rightSideShinyRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rightSideShinyRadioButton.isSelected()) {
            controller.setRightSideShininess(0.25f);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.RIGHT_SIDE_SHININESS, 
        rightSideShininessListener);
    
    ButtonGroup rightSideShininessButtonGroup = new ButtonGroup();
    rightSideShininessButtonGroup.add(this.rightSideMattRadioButton);
    rightSideShininessButtonGroup.add(this.rightSideShinyRadioButton);
    updateRightSideShininessRadioButtons(controller);
    
   /* this.rightSideBaseboardButton = new JButton(new ResourceAction.ButtonAction(
        new ResourceAction(preferences, com.eteks.sweethome3d.android_props.WallPanel.class, "MODIFY_RIGHT_SIDE_BASEBOARD", true) {
          @Override
          public void actionPerformed(ActionEvent ev) {
            editBaseboard((JComponent)ev.getSource(), 
                preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideBaseboardDialog.title"), 
                controller.getRightSideBaseboardController());
          }
        }));*/
	  this.rightSideBaseboardButton = new JButton(activity,
			  SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.WallPanel.class, "MODIFY_RIGHT_SIDE_BASEBOARD.Name"));
	  rightSideBaseboardButton.setOnClickListener(new View.OnClickListener(){
		  public void onClick(View view)
		  {
			  editBaseboard(null,
					  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "rightSideBaseboardDialog.title"),
					  controller.getRightSideBaseboardController());
		  }
	  });
    
    // Top pattern and 3D color
    this.patternLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "patternLabel.text"));    
    List<TextureImage> patterns = preferences.getPatternsCatalog().getPatterns();
    if (controller.getPattern() == null) {
      patterns = new ArrayList<TextureImage>(patterns);
      patterns.add(0, null);
    }
    this.patternComboBox = new JComboBox(activity, new DefaultComboBoxModel(patterns.toArray()));
	  //PJPJPJ notice reuse of user preferences panel
	  this.patternComboBox.setAdapter( new UserPreferencesPanel.PatternRenderer(activity, patterns.toArray()) );

   /* this.patternComboBox.setRenderer(new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list, 
            Object value, int index, boolean isSelected, boolean cellHasFocus) {
          TextureImage pattern = (TextureImage)value;
          final Component component = super.getListCellRendererComponent(
              list, pattern == null ? " " : "", index, isSelected, cellHasFocus);
          if (pattern != null) {
            final BufferedImage patternImage = SwingTools.getPatternImage(
                pattern, list.getBackground(), list.getForeground());
            setIcon(new Icon() {
                public int getIconWidth() {
                  return patternImage.getWidth() * 4 + 1;
                }
          
                public int getIconHeight() {
                  return patternImage.getHeight() + 2;
                }
          
                public void paintIcon(Component c, Graphics g, int x, int y) {
                  Graphics2D g2D = (Graphics2D)g;
                  for (int i = 0; i < 4; i++) {
                    g2D.drawImage(patternImage, x + i * patternImage.getWidth(), y + 1, list);
                  }
                  g2D.setColor(list.getForeground());
                  g2D.drawRect(x, y, getIconWidth() - 2, getIconHeight() - 1);
                }
              });
          }
          return component;
        }
      });*/
    this.patternComboBox.setSelectedItem(controller.getPattern());
    this.patternComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setPattern((TextureImage)patternComboBox.getSelectedItem());
        }
      });
    controller.addPropertyChangeListener(WallController.Property.PATTERN, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            patternComboBox.setSelectedItem(controller.getPattern());
          }
        });
    
    this.topColorLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "topColorLabel.text"));
    this.topDefaultColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "topDefaultColorRadioButton.text"));
    this.topDefaultColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (topDefaultColorRadioButton.isSelected()) {
            controller.setTopPaint(WallController.WallPaint.DEFAULT);
          }
        }
      });
    this.topColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "topColorRadioButton.text"));
    this.topColorRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (topColorRadioButton.isSelected()) {
            controller.setTopPaint(WallController.WallPaint.COLORED);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.TOP_PAINT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateTopColorRadioButtons(controller);
          }
        });
    this.topColorButton = new ColorButton(activity, preferences);
    this.topColorButton.setColorDialogTitle(preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.WallPanel.class, "topColorDialog.title"));
    this.topColorButton.setColor(controller.getTopColor());
    this.topColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            controller.setTopColor(topColorButton.getColor());
            controller.setTopPaint(WallController.WallPaint.COLORED);
          }
        });
    controller.addPropertyChangeListener(WallController.Property.TOP_COLOR, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            topColorButton.setColor(controller.getTopColor());
          }
        });
    
    ButtonGroup topColorGroup = new ButtonGroup();
    topColorGroup.add(this.topDefaultColorRadioButton);
    topColorGroup.add(this.topColorRadioButton);
    updateTopColorRadioButtons(controller);

    // Create height label and its spinner bound to RECTANGULAR_WALL_HEIGHT controller property
    this.rectangularWallRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "rectangularWallRadioButton.text"));
    this.rectangularWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (rectangularWallRadioButton.isSelected()) {
            controller.setShape(WallController.WallShape.RECTANGULAR_WALL);
          }
        }
      });
    controller.addPropertyChangeListener(WallController.Property.SHAPE, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateWallShapeRadioButtons(controller);
          }
        });

    this.rectangularWallHeightLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.android_props.WallPanel.class, "rectangularWallHeightLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel rectangularWallHeightSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
    this.rectangularWallHeightSpinner = new NullableSpinner(activity, rectangularWallHeightSpinnerModel, true);
    rectangularWallHeightSpinnerModel.setNullable(controller.getRectangularWallHeight() == null);
    rectangularWallHeightSpinnerModel.setLength(controller.getRectangularWallHeight());
    final PropertyChangeListener rectangularWallHeightChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          rectangularWallHeightSpinnerModel.setNullable(ev.getNewValue() == null);
          rectangularWallHeightSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT, 
        rectangularWallHeightChangeListener);
    rectangularWallHeightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT, 
              rectangularWallHeightChangeListener);
          controller.setRectangularWallHeight(rectangularWallHeightSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.RECTANGULAR_WALL_HEIGHT, 
              rectangularWallHeightChangeListener);
        }
      });
   
    this.slopingWallRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "slopingWallRadioButton.text"));
    this.slopingWallRadioButton.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (slopingWallRadioButton.isSelected()) {
            controller.setShape(WallController.WallShape.SLOPING_WALL);
          }
        }
      });
    ButtonGroup wallHeightButtonGroup = new ButtonGroup();
    wallHeightButtonGroup.add(this.rectangularWallRadioButton);
    wallHeightButtonGroup.add(this.slopingWallRadioButton);
    updateWallShapeRadioButtons(controller);

    // Create height at start label and its spinner bound to SLOPING_WALL_HEIGHT_AT_START controller property
    this.slopingWallHeightAtStartLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "slopingWallHeightAtStartLabel.text"));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel slopingWallHeightAtStartSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
    this.slopingWallHeightAtStartSpinner = new NullableSpinner(activity, slopingWallHeightAtStartSpinnerModel, true);
    slopingWallHeightAtStartSpinnerModel.setNullable(controller.getSlopingWallHeightAtStart() == null);
    slopingWallHeightAtStartSpinnerModel.setLength(controller.getSlopingWallHeightAtStart());
    final PropertyChangeListener slopingWallHeightAtStartChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          slopingWallHeightAtStartSpinnerModel.setNullable(ev.getNewValue() == null);
          slopingWallHeightAtStartSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START, 
        slopingWallHeightAtStartChangeListener);
    slopingWallHeightAtStartSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START, 
              slopingWallHeightAtStartChangeListener);
          controller.setSlopingWallHeightAtStart(slopingWallHeightAtStartSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_START, 
              slopingWallHeightAtStartChangeListener);
        }
      });
    
    // Create height at end label and its spinner bound to SLOPING_WALL_HEIGHT_AT_END controller property
    this.slopingWallHeightAtEndLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "slopingWallHeightAtEndLabel.text"));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel slopingWallHeightAtEndSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
    this.slopingWallHeightAtEndSpinner = new NullableSpinner(activity, slopingWallHeightAtEndSpinnerModel, true);
    slopingWallHeightAtEndSpinnerModel.setNullable(controller.getSlopingWallHeightAtEnd() == null);
    slopingWallHeightAtEndSpinnerModel.setLength(controller.getSlopingWallHeightAtEnd());
    final PropertyChangeListener slopingWallHeightAtEndChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          slopingWallHeightAtEndSpinnerModel.setNullable(ev.getNewValue() == null);
          slopingWallHeightAtEndSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END, 
        slopingWallHeightAtEndChangeListener);
    slopingWallHeightAtEndSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END, 
              slopingWallHeightAtEndChangeListener);
          controller.setSlopingWallHeightAtEnd(slopingWallHeightAtEndSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.SLOPING_WALL_HEIGHT_AT_END, 
              slopingWallHeightAtEndChangeListener);
        }
      });

    // Create thickness label and its spinner bound to THICKNESS controller property
    this.thicknessLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "thicknessLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel thicknessSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength / 10);
    this.thicknessSpinner = new NullableSpinner(activity, thicknessSpinnerModel, true);
    thicknessSpinnerModel.setNullable(controller.getThickness() == null);
    thicknessSpinnerModel.setLength(controller.getThickness());
    final PropertyChangeListener thicknessChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          thicknessSpinnerModel.setNullable(ev.getNewValue() == null);
          thicknessSpinnerModel.setLength((Float)ev.getNewValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.THICKNESS, 
        thicknessChangeListener);
    thicknessSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.THICKNESS, 
              thicknessChangeListener);
          controller.setThickness(thicknessSpinnerModel.getLength());
          controller.addPropertyChangeListener(WallController.Property.THICKNESS, 
              thicknessChangeListener);
        }
      });
    
    // Create arc extent label and its spinner bound to ARC_EXTENT_IN_DEGREES controller property
    this.arcExtentLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
        com.eteks.sweethome3d.android_props.WallPanel.class, "arcExtentLabel.text", unitName));
    final NullableSpinnerNumberModel arcExtentSpinnerModel =
        new NullableSpinnerNumberModel(new Float(0), new Float(-270), new Float(270), new Float(5));
    this.arcExtentSpinner = new NullableSpinnerJogDial(activity, arcExtentSpinnerModel);
    arcExtentSpinnerModel.setNullable(controller.getArcExtentInDegrees() == null);
    arcExtentSpinnerModel.setValue(controller.getArcExtentInDegrees());
    final PropertyChangeListener arcExtentChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          arcExtentSpinnerModel.setNullable(ev.getNewValue() == null);
          arcExtentSpinnerModel.setValue(((Number)ev.getNewValue()).floatValue());
        }
      };
    controller.addPropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES, 
        arcExtentChangeListener);
    arcExtentSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.removePropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES, 
              arcExtentChangeListener);
          controller.setArcExtentInDegrees(((Number)arcExtentSpinnerModel.getValue()).floatValue());
          controller.addPropertyChangeListener(WallController.Property.ARC_EXTENT_IN_DEGREES, 
              arcExtentChangeListener);
        }
      });
    
    // wallOrientationLabel shows an HTML explanation of wall orientation with an image URL in resource
    this.wallOrientationLabel = new JLabel(activity, preferences.getLocalizedString(
            com.eteks.sweethome3d.android_props.WallPanel.class, "wallOrientationLabel.text", 
            new ResourceURLContent(com.eteks.sweethome3d.android_props.WallPanel.class, "resources/wallOrientation.png").getURL()));//,
        //JLabel.CENTER);
    // Use same font for label as tooltips
    //this.wallOrientationLabel.setFont(UIManager.getFont("ToolTip.font"));

    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "wall.title");
  }

  /**
   * Updates left side color radio buttons. 
   */
  private void updateLeftSideColorRadioButtons(WallController controller) {
    if (controller.getLeftSidePaint() == WallController.WallPaint.COLORED) {
      this.leftSideColorRadioButton.setSelected(true);
    } else if (controller.getLeftSidePaint() == WallController.WallPaint.TEXTURED) {
      this.leftSideTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.leftSideColorRadioButton, this.leftSideTextureRadioButton);
    }
  }

  /**
   * Updates left side shininess radio buttons. 
   */
  private void updateLeftSideShininessRadioButtons(WallController controller) {
    if (controller.getLeftSideShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.leftSideMattRadioButton, this.leftSideShinyRadioButton);
    } else if (controller.getLeftSideShininess() == 0) {
      this.leftSideMattRadioButton.setSelected(true);
    } else { // null
      this.leftSideShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates right side color radio buttons. 
   */
  private void updateRightSideColorRadioButtons(WallController controller) {
    if (controller.getRightSidePaint() == WallController.WallPaint.COLORED) {
      this.rightSideColorRadioButton.setSelected(true);
    } else if (controller.getRightSidePaint() == WallController.WallPaint.TEXTURED) {
      this.rightSideTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.rightSideColorRadioButton, this.rightSideTextureRadioButton);
    }
  }

  /**
   * Updates right side shininess radio buttons. 
   */
  private void updateRightSideShininessRadioButtons(WallController controller) {
    if (controller.getRightSideShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.rightSideMattRadioButton, this.rightSideShinyRadioButton);
    } else if (controller.getRightSideShininess() == 0) {
      this.rightSideMattRadioButton.setSelected(true);
    } else { // null
      this.rightSideShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates top color radio buttons. 
   */
  private void updateTopColorRadioButtons(WallController controller) {
    if (controller.getTopPaint() == WallController.WallPaint.COLORED) {
      this.topColorRadioButton.setSelected(true);
    } else if (controller.getTopPaint() == WallController.WallPaint.DEFAULT) {
      this.topDefaultColorRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.topColorRadioButton, this.topDefaultColorRadioButton);
    }
  }

  /**
   * Updates rectangular and sloping wall radio buttons. 
   */
  private void updateWallShapeRadioButtons(WallController controller) {
    if (controller.getShape() == WallController.WallShape.SLOPING_WALL) {
      this.slopingWallRadioButton.setSelected(true);
    } else if (controller.getShape() == WallController.WallShape.RECTANGULAR_WALL) {
      this.rectangularWallRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.slopingWallRadioButton, this.rectangularWallRadioButton);
    }
  }

	private boolean baseBoardDialogShowing = false;
  /**
   * Edits the baseboard values in an option pane dialog.
   */
  private void editBaseboard(final JComponent parent, final String title, 
                             final BaseboardChoiceController baseboardChoiceController) {

	  // we can't display the view in two dialog at the same time
	  if(baseBoardDialogShowing)
		  return;


    // Add baseboard component to a panel with a flow layout to avoid it getting too large
	 final LinearLayout view = (LinearLayout)baseboardChoiceController.getView();

    //JPanel panel = new JPanel();
    //panel.add(view);
   // if (SwingTools.showConfirmDialog(parent, panel, title, (JComponent)view.getComponent(0)) != JOptionPane.OK_OPTION) {

	//I must get off the EDT and ask the question in a blocking manner
	  Thread t2 = new Thread()
	  {
		  public void run()
		  {
			  Boolean visible = baseboardChoiceController.getVisible();
			  Integer color = baseboardChoiceController.getColor();
			  HomeTexture texture = baseboardChoiceController.getTextureController().getTexture();
			  BaseboardChoiceController.BaseboardPaint paint = baseboardChoiceController.getPaint();
			  Float thickness = baseboardChoiceController.getThickness();
			  Float height = baseboardChoiceController.getHeight();

			  baseBoardDialogShowing = true;
			  boolean confirmed = JOptionPane.showOptionDialog(activity, view, title,
					  JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
					  null, new Object [] {"OK", "cancel"}, "cancel") == JOptionPane.OK_OPTION;
			  baseBoardDialogShowing = false;
			  if(!confirmed)
			  {
				  // Restore initial values
				  baseboardChoiceController.setVisible(visible);
				  baseboardChoiceController.setColor(color);
				  baseboardChoiceController.getTextureController().setTexture(texture);
				  baseboardChoiceController.setPaint(paint);
				  baseboardChoiceController.setThickness(thickness);
				  baseboardChoiceController.setHeight(height);
			  }
		  }
	  };
	  t2.start();

  }
  

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences, 
                                final WallController controller) {

	  JLabel startPointPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "startPointPanel.title"));
	  swapOut(startPointPanel, R.id.wall_panel_startPanel);
	  swapOut(this.xStartLabel, R.id.wall_panel_xStartLabel);
	  swapOut(this.xStartSpinner, R.id.wall_panel_xStartSpinner);
	  swapOut(this.yStartLabel, R.id.wall_panel_yStartLabel);
	  swapOut(this.yStartSpinner, R.id.wall_panel_yStartSpinner);


	  JLabel endPointPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "endPointPanel.title"));
	  swapOut(endPointPanel, R.id.wall_panel_endPanel);
	  swapOut(this.xEndLabel, R.id.wall_panel_xEndLabel);
	  swapOut(this.xEndSpinner, R.id.wall_panel_xEndSpinner);
	  swapOut(this.yEndLabel, R.id.wall_panel_yEndLabel);
	  swapOut(this.yEndSpinner, R.id.wall_panel_yEndSpinner);

	  swapOut(this.distanceToEndPointLabel, R.id.wall_panel_distanceLabel);
	  swapOut(this.distanceToEndPointSpinner, R.id.wall_panel_distanceSpinner);

	  JLabel leftSidePanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "leftSidePanel.title"));
	  swapOut(leftSidePanel, R.id.wall_panel_leftSideLabel);
	  swapOut(this.leftSideColorRadioButton, R.id.wall_panel_leftSideColorRadioButton);
	  swapOut(this.leftSideColorButton, R.id.wall_panel_leftSideColorButton);
	  swapOut(this.leftSideTextureRadioButton, R.id.wall_panel_leftSideTextureRadioButton);
	  swapOut(this.leftSideTextureComponent, R.id.wall_panel_leftSideTextureButton);
	  swapOut(this.leftSideMattRadioButton, R.id.wall_panel_leftSideMattRadioButton);
	  swapOut(this.leftSideShinyRadioButton, R.id.wall_panel_leftSideShininessRadioButton);
	  swapOut(this.leftSideBaseboardButton, R.id.wall_panel_leftSideModifyBaseboardButton);


	  JLabel rightSidePanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "rightSidePanel.title"));
	  swapOut(rightSidePanel, R.id.wall_panel_rightSideLabel);
	  swapOut(this.rightSideColorRadioButton, R.id.wall_panel_rightSideColorRadioButton);
	  swapOut(this.rightSideColorButton, R.id.wall_panel_rightSideColorButton);
	  swapOut(this.rightSideTextureRadioButton, R.id.wall_panel_rightSideTextureRadioButton);
	  swapOut(this.rightSideTextureComponent, R.id.wall_panel_rightSideTextureButton);
	  swapOut(this.rightSideMattRadioButton, R.id.wall_panel_rightSideMattRadioButton);
	  swapOut(this.rightSideShinyRadioButton, R.id.wall_panel_rightSideShininessRadioButton);
	  swapOut(this.rightSideBaseboardButton, R.id.wall_panel_rightSideModifyBaseboardButton);

	  JLabel topPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "topPanel.title"));
	  swapOut(topPanel, R.id.wall_panel_wallTopPanel);
	  swapOut(this.patternLabel, R.id.wall_panel_wallTopTextureLabel);
	  swapOut(this.patternComboBox, R.id.wall_panel_wallTopTextureSpinner);
	  swapOut(this.topColorLabel, R.id.wall_panel_wallTopColorLabel);
	  swapOut(this.topDefaultColorRadioButton, R.id.wall_panel_wallTopColorDefaultRadioButton);
	  swapOut(this.topColorRadioButton, R.id.wall_panel_wallTopColorColorRadioButton);
	  swapOut(this.topColorButton, R.id.wall_panel_wallTopColorButton);


	  JLabel heightPanel = new JLabel(activity,
			  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.WallPanel.class, "heightPanel.title"));
	  swapOut(heightPanel, R.id.wall_panel_heightPanel);
	  swapOut(this.rectangularWallRadioButton, R.id.wall_panel_rectangularWallRadioButton);
	  swapOut(this.rectangularWallHeightLabel, R.id.wall_panel_heightRectangularLabel);
	  swapOut(this.rectangularWallHeightSpinner, R.id.wall_panel_heightRectangularSpinner);
	  swapOut(this.slopingWallRadioButton, R.id.wall_panel_slopingWallRadioButton);
	  swapOut(this.slopingWallHeightAtStartLabel, R.id.wall_panel_heightStartSlopingLabel);
	  swapOut(this.slopingWallHeightAtStartSpinner, R.id.wall_panel_heightStartSlopingSpinner);
	  swapOut(this.slopingWallHeightAtEndLabel, R.id.wall_panel_heightEndSlopingLabel);
	  swapOut(this.slopingWallHeightAtEndSpinner, R.id.wall_panel_heightEndSlopingSpinner);


	  swapOut(this.thicknessLabel, R.id.wall_panel_thicknessLabel);
	  swapOut(this.thicknessSpinner, R.id.wall_panel_thicknessSpinner);
	  swapOut(this.arcExtentLabel, R.id.wall_panel_ArcLabel);
	  swapOut(this.arcExtentSpinner, R.id.wall_panel_ArcSpinner);

    // Make startPointPanel and endPointPanel visible depending on editable points property
    controller.addPropertyChangeListener(WallController.Property.EDITABLE_POINTS, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
			  //TODO: all this cool visibility stuff
            //startPointPanel.setVisible(controller.isEditablePoints());
            //endPointPanel.setVisible(controller.isEditablePoints());
            //arcExtentLabel.setVisible(controller.isEditablePoints());
            //arcExtentSpinner.setVisible(controller.isEditablePoints());
          }
        });
	  //TODO: cool visibility via setEnabled
    //startPointPanel.setVisible(controller.isEditablePoints());
    //endPointPanel.setVisible(controller.isEditablePoints());
    //this.arcExtentLabel.setVisible(controller.isEditablePoints());
   // this.arcExtentSpinner.setVisible(controller.isEditablePoints());

	  this.setTitle(dialogTitle);
	  swapOut(closeButton, R.id.wall_panel_closeButton);
  }

  
  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
  /*  Component homeRoot = SwingUtilities.getRoot((Component)parentView);
    if (homeRoot != null) {
      JOptionPane optionPane = new JOptionPane(this, 
          JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
      JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parentView);
      if (parentView != null) {
        optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
      }
      JDialog dialog = optionPane.createDialog(parentComponent, this.dialogTitle);
      Dimension screenSize = getToolkit().getScreenSize();
      Insets screenInsets = getToolkit().getScreenInsets(getGraphicsConfiguration());
      // Check dialog isn't too high
      int screenHeight = screenSize.height - screenInsets.top - screenInsets.bottom;
      if (OperatingSystem.isLinux() && screenHeight == screenSize.height) {
        // Let's consider that under Linux at least an horizontal bar exists 
        screenHeight -= 30;
      }
      if (dialog.getHeight() > screenHeight) {
        this.wallOrientationLabel.setVisible(false);
      }
      dialog.pack();
      if (dialog.getHeight() > screenHeight) {
        this.patternLabel.getParent().setVisible(false);
      }
      dialog.dispose();
    }

    JFormattedTextField thicknessTextField = 
        ((JSpinner.DefaultEditor)thicknessSpinner.getEditor()).getTextField();
    if (SwingTools.showConfirmDialog((JComponent)parentView, 
            this, this.dialogTitle, thicknessTextField) == JOptionPane.OK_OPTION) {
      this.controller.modifyWalls();
    }*/
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modifyWalls();
		  }
	  });
	  this.show();
  }
}
