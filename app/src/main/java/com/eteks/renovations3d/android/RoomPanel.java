/*
 * RoomPanel.java 20 nov. 2008
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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.Tutorial;
import com.eteks.renovations3d.android.swingish.ActionListener;
import com.eteks.renovations3d.android.swingish.ButtonGroup;
import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.renovations3d.android.swingish.JButton;
import com.eteks.renovations3d.android.swingish.JCheckBox;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JRadioButton;
import com.eteks.renovations3d.android.swingish.JTextField;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.RoomController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Room editing panel.
 * @author Emmanuel Puybaret
 */
public class RoomPanel extends AndroidDialogView implements DialogView {
  private final RoomController  controller;
  private JLabel nameLabel;
  private JTextField nameTextField;
  private NullableCheckBox areaVisibleCheckBox;
  private NullableCheckBox      floorVisibleCheckBox;
  private JRadioButton floorColorRadioButton;
  private ColorButton           floorColorButton;
  private JRadioButton          floorTextureRadioButton;
  private JButton floorTextureComponent;
  private JRadioButton          floorMattRadioButton;
  private JRadioButton          floorShinyRadioButton;
  private NullableCheckBox      ceilingVisibleCheckBox;
  private JRadioButton          ceilingColorRadioButton;
  private ColorButton           ceilingColorButton;
  private JRadioButton          ceilingTextureRadioButton;
  private JButton            ceilingTextureComponent;
  private JRadioButton          ceilingMattRadioButton;
  private JRadioButton          ceilingShinyRadioButton;
  private JCheckBox splitSurroundingWallsCheckBox;
  private JRadioButton          wallSidesColorRadioButton;
  private ColorButton           wallSidesColorButton;
  private JRadioButton          wallSidesTextureRadioButton;
  private JButton            wallSidesTextureComponent;
  private JRadioButton          wallSidesMattRadioButton;
  private JRadioButton          wallSidesShinyRadioButton;
  private LinearLayout            wallSidesBaseboardComponent;
  private boolean               firstWallChange;
  private String                dialogTitle;
	private boolean sendTutorialTextureChange = false;

	/**
   * Creates a panel that displays room data according to the units set in
   * <code>preferences</code>.
   * @param preferences user preferences
   * @param controller the controller of this panel
   */
  public RoomPanel(UserPreferences preferences,
                   RoomController controller, Activity activity) {
	  super(preferences, activity, R.layout.dialog_roompanel);
    this.controller = controller;
    createComponents(preferences, controller);
    layoutComponents(preferences);
    this.firstWallChange = true;
  }

  /**
   * Creates and initializes components and spinners model.
   */
  private void createComponents(UserPreferences preferences, 
                                final RoomController controller) {
    if (controller.isPropertyEditable(RoomController.Property.NAME)) {
      // Create name label and its text field bound to NAME controller property
      this.nameLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "nameLabel.text"));
      this.nameTextField = new JTextField(activity, controller.getName());//, 10, preferences.getAutoCompletionStrings("RoomName"));
    //  if (!OperatingSystem.isMacOSXLeopardOrSuperior()) {
     //   SwingTools.addAutoSelectionOnFocusGain(this.nameTextField);
      //}
      final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            nameTextField.setText(controller.getName());
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.NAME, nameChangeListener);

		nameTextField.addTextChangedListener(new TextWatcher(){
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
			public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
			public void afterTextChanged(Editable arg0) {
				controller.removePropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
				String name = nameTextField.getText().toString();
				if (name == null || name.trim().length() == 0) {
					controller.setName("");
				} else {
					controller.setName(name);
				}
				controller.addPropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
			}
		});
      /*this.nameTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            controller.removePropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
            String name = nameTextField.getText(); 
            if (name == null || name.trim().length() == 0) {
              controller.setName("");
            } else {
              controller.setName(name);
            }
            controller.addPropertyChangeListener(RoomController.Property.NAME, nameChangeListener);
          }
    
          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
    
          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });*/
    }
    
    if (controller.isPropertyEditable(RoomController.Property.AREA_VISIBLE)) {
      // Create area visible check box bound to AREA_VISIBLE controller property
      this.areaVisibleCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "areaVisibleCheckBox.text"));
      this.areaVisibleCheckBox.setNullable(controller.getAreaVisible() == null);
      this.areaVisibleCheckBox.setValue(controller.getAreaVisible());
      final PropertyChangeListener visibleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          areaVisibleCheckBox.setNullable(ev.getNewValue() == null);
          areaVisibleCheckBox.setValue((Boolean)ev.getNewValue());
        }
      };
      controller.addPropertyChangeListener(RoomController.Property.AREA_VISIBLE, visibleChangeListener);
      this.areaVisibleCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(RoomController.Property.AREA_VISIBLE, visibleChangeListener);
            controller.setAreaVisible(areaVisibleCheckBox.getValue());
            controller.addPropertyChangeListener(RoomController.Property.AREA_VISIBLE, visibleChangeListener);
          }
        });
    }
    
    if (controller.isPropertyEditable(RoomController.Property.FLOOR_VISIBLE)) {
      // Create floor visible check box bound to FLOOR_VISIBLE controller property
      this.floorVisibleCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "floorVisibleCheckBox.text"));
      this.floorVisibleCheckBox.setNullable(controller.getFloorVisible() == null);
      this.floorVisibleCheckBox.setValue(controller.getFloorVisible());
      final PropertyChangeListener floorVisibleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          floorVisibleCheckBox.setNullable(ev.getNewValue() == null);
          floorVisibleCheckBox.setValue((Boolean)ev.getNewValue());
        }
      };
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_VISIBLE, floorVisibleChangeListener);
      this.floorVisibleCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(RoomController.Property.FLOOR_VISIBLE, floorVisibleChangeListener);
            controller.setFloorVisible(floorVisibleCheckBox.getValue());
            controller.addPropertyChangeListener(RoomController.Property.FLOOR_VISIBLE, floorVisibleChangeListener);
          }
        });
    }
    
    if (controller.isPropertyEditable(RoomController.Property.FLOOR_PAINT)) {
      // Floor color and texture buttons bound to floor controller properties
      this.floorColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "floorColorRadioButton.text"));
      this.floorColorRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorColorRadioButton.isSelected()) {
              controller.setFloorPaint(RoomController.RoomPaint.COLORED);
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_PAINT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateFloorColorRadioButtons(controller);
            }
          });
      
      this.floorColorButton = new ColorButton(activity, preferences);
      this.floorColorButton.setColorDialogTitle(preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "floorColorDialog.title"));
      this.floorColorButton.setColor(controller.getFloorColor());
      this.floorColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              controller.setFloorColor(floorColorButton.getColor());
              controller.setFloorPaint(RoomController.RoomPaint.COLORED);
            }
          });
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_COLOR, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              floorColorButton.setColor(controller.getFloorColor());
            }
          });

      this.floorTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "floorTextureRadioButton.text"));
      this.floorTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorTextureRadioButton.isSelected()) {
              controller.setFloorPaint(RoomController.RoomPaint.TEXTURED);
            }
          }
        });
      
      this.floorTextureComponent = (JButton)controller.getFloorTextureController().getView();
      
      ButtonGroup floorButtonColorGroup = new ButtonGroup();
      floorButtonColorGroup.add(this.floorColorRadioButton);
      floorButtonColorGroup.add(this.floorTextureRadioButton);
      updateFloorColorRadioButtons(controller);
    }
      
    if (controller.isPropertyEditable(RoomController.Property.FLOOR_SHININESS)) {
      // Floor shininess radio buttons bound to FLOOR_SHININESS controller property
      this.floorMattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "floorMattRadioButton.text"));
      this.floorMattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorMattRadioButton.isSelected()) {
              controller.setFloorShininess(0f);
            }
          }
        });
      PropertyChangeListener floorShininessListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateFloorShininessRadioButtons(controller);
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_SHININESS, 
          floorShininessListener);
  
      this.floorShinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "floorShinyRadioButton.text"));
      this.floorShinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (floorShinyRadioButton.isSelected()) {
              controller.setFloorShininess(0.25f);
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.FLOOR_SHININESS, 
          floorShininessListener);
      
      ButtonGroup floorShininessButtonGroup = new ButtonGroup();
      floorShininessButtonGroup.add(this.floorMattRadioButton);
      floorShininessButtonGroup.add(this.floorShinyRadioButton);
      updateFloorShininessRadioButtons(controller);
    }
    
    if (controller.isPropertyEditable(RoomController.Property.CEILING_VISIBLE)) {
      // Create ceiling visible check box bound to CEILING_VISIBLE controller property
      this.ceilingVisibleCheckBox = new NullableCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingVisibleCheckBox.text"));
      this.ceilingVisibleCheckBox.setNullable(controller.getCeilingVisible() == null);
      this.ceilingVisibleCheckBox.setValue(controller.getCeilingVisible());
      final PropertyChangeListener ceilingVisibleChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          ceilingVisibleCheckBox.setNullable(ev.getNewValue() == null);
          ceilingVisibleCheckBox.setValue((Boolean)ev.getNewValue());
        }
      };
      controller.addPropertyChangeListener(RoomController.Property.CEILING_VISIBLE, ceilingVisibleChangeListener);
      this.ceilingVisibleCheckBox.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            controller.removePropertyChangeListener(RoomController.Property.CEILING_VISIBLE, ceilingVisibleChangeListener);
            controller.setCeilingVisible(ceilingVisibleCheckBox.getValue());
            controller.addPropertyChangeListener(RoomController.Property.CEILING_VISIBLE, ceilingVisibleChangeListener);
          }
        });
    }
  
    if (controller.isPropertyEditable(RoomController.Property.CEILING_PAINT)) {
      // Ceiling color and texture buttons bound to ceiling controller properties
      this.ceilingColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingColorRadioButton.text"));
      this.ceilingColorRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (ceilingColorRadioButton.isSelected()) {
              controller.setCeilingPaint(RoomController.RoomPaint.COLORED);
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.CEILING_PAINT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateCeilingColorRadioButtons(controller);
            }
          });
    
      this.ceilingColorButton = new ColorButton(activity, preferences);
      this.ceilingColorButton.setColor(controller.getCeilingColor());
      this.ceilingColorButton.setColorDialogTitle(preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingColorDialog.title"));
      this.ceilingColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              controller.setCeilingColor(ceilingColorButton.getColor());
              controller.setCeilingPaint(RoomController.RoomPaint.COLORED);
            }
          });
      controller.addPropertyChangeListener(RoomController.Property.CEILING_COLOR, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              ceilingColorButton.setColor(controller.getCeilingColor());
            }
          });
      
      this.ceilingTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingTextureRadioButton.text"));
      this.ceilingTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (ceilingTextureRadioButton.isSelected()) {
              controller.setCeilingPaint(RoomController.RoomPaint.TEXTURED);
            }
          }
        });
    
      this.ceilingTextureComponent = (JButton)controller.getCeilingTextureController().getView();
  
      ButtonGroup ceilingColorButtonGroup = new ButtonGroup();
      ceilingColorButtonGroup.add(this.ceilingColorRadioButton);
      ceilingColorButtonGroup.add(this.ceilingTextureRadioButton);
      updateCeilingColorRadioButtons(controller);
    }
    
    if (controller.isPropertyEditable(RoomController.Property.CEILING_SHININESS)) {
      // Ceiling shininess radio buttons bound to CEILING_SHININESS controller property
      this.ceilingMattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingMattRadioButton.text"));
      this.ceilingMattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (ceilingMattRadioButton.isSelected()) {
              controller.setCeilingShininess(0f);
            }
          }
        });
      PropertyChangeListener ceilingShininessListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateCeilingShininessRadioButtons(controller);
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.CEILING_SHININESS, 
          ceilingShininessListener);
  
      this.ceilingShinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingShinyRadioButton.text"));
      this.ceilingShinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (ceilingShinyRadioButton.isSelected()) {
              controller.setCeilingShininess(0.25f);
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.CEILING_SHININESS, 
          ceilingShininessListener);
      
      ButtonGroup ceilingShininessButtonGroup = new ButtonGroup();
      ceilingShininessButtonGroup.add(this.ceilingMattRadioButton);
      ceilingShininessButtonGroup.add(this.ceilingShinyRadioButton);
      updateCeilingShininessRadioButtons(controller);
    }
    
    if (controller.isPropertyEditable(RoomController.Property.SPLIT_SURROUNDING_WALLS)) {
      // Create visible check box bound to SPLIT_SURROUNDING_WALLS controller property
      this.splitSurroundingWallsCheckBox = new JCheckBox(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "splitSurroundingWallsCheckBox.text"));
      final String splitSurroundingWallsToolTip = 
          preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "splitSurroundingWallsCheckBox.tooltip");
      PropertyChangeListener splitSurroundingWallsChangeListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          splitSurroundingWallsCheckBox.setEnabled(controller.isSplitSurroundingWallsNeeded());
          if (splitSurroundingWallsToolTip.length() > 0 && controller.isSplitSurroundingWallsNeeded()) {
            splitSurroundingWallsCheckBox.setToolTipText(splitSurroundingWallsToolTip);
          } else {
            splitSurroundingWallsCheckBox.setToolTipText(null);
          }
          splitSurroundingWallsCheckBox.setSelected(controller.isSplitSurroundingWalls());
        }
      };
      splitSurroundingWallsChangeListener.propertyChange(null);
      controller.addPropertyChangeListener(RoomController.Property.SPLIT_SURROUNDING_WALLS, splitSurroundingWallsChangeListener);
      this.splitSurroundingWallsCheckBox.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            controller.setSplitSurroundingWalls(splitSurroundingWallsCheckBox.isSelected());
            firstWallChange = false;
          }
        });
    }
    
    if (controller.isPropertyEditable(RoomController.Property.WALL_SIDES_PAINT)) {
      // Wall sides color and texture buttons bound to walls controller properties
      this.wallSidesColorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesColorRadioButton.text"));
      this.wallSidesColorRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (wallSidesColorRadioButton.isSelected()) {
              controller.setWallSidesPaint(RoomController.RoomPaint.COLORED);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_PAINT, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateWallSidesRadioButtons(controller);
            }
          });
  
      this.wallSidesColorButton = new ColorButton(activity, preferences);
      this.wallSidesColorButton.setColor(controller.getWallSidesColor());
      this.wallSidesColorButton.setColorDialogTitle(preferences.getLocalizedString(
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesColorDialog.title"));
      this.wallSidesColorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              controller.setWallSidesColor(wallSidesColorButton.getColor());
            }
          });
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_COLOR, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              wallSidesColorButton.setColor(controller.getWallSidesColor());
              selectSplitSurroundingWallsAtFirstChange();
            }
          });
      
      this.wallSidesTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesTextureRadioButton.text"));
      this.wallSidesTextureRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent e) {
            if (wallSidesTextureRadioButton.isSelected()) {
              controller.setWallSidesPaint(RoomController.RoomPaint.TEXTURED);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
    
      this.wallSidesTextureComponent = (JButton)controller.getWallSidesTextureController().getView();
      controller.getWallSidesTextureController().addPropertyChangeListener(TextureChoiceController.Property.TEXTURE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              selectSplitSurroundingWallsAtFirstChange();
            }
          });
  
      ButtonGroup wallSidesButtonGroup = new ButtonGroup();
      wallSidesButtonGroup.add(this.wallSidesColorRadioButton);
      wallSidesButtonGroup.add(this.wallSidesTextureRadioButton);
      updateWallSidesRadioButtons(controller);
    }
      
    if (controller.isPropertyEditable(RoomController.Property.WALL_SIDES_SHININESS)) {
      // Wall sides shininess radio buttons bound to WALL_SIDES_SHININESS controller property
      this.wallSidesMattRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesMattRadioButton.text"));
      this.wallSidesMattRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (wallSidesMattRadioButton.isSelected()) {
              controller.setWallSidesShininess(0f);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
      PropertyChangeListener wallSidesShininessListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            updateWallSidesShininessRadioButtons(controller);
          }
        };
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_SHININESS, 
          wallSidesShininessListener);
  
      this.wallSidesShinyRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesShinyRadioButton.text"));
      this.wallSidesShinyRadioButton.addChangeListener(new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (wallSidesShinyRadioButton.isSelected()) {
              controller.setWallSidesShininess(0.25f);
              selectSplitSurroundingWallsAtFirstChange();
            }
          }
        });
      controller.addPropertyChangeListener(RoomController.Property.WALL_SIDES_SHININESS, 
          wallSidesShininessListener);
      
      ButtonGroup wallSidesShininessButtonGroup = new ButtonGroup();
      wallSidesShininessButtonGroup.add(this.wallSidesMattRadioButton);
      wallSidesShininessButtonGroup.add(this.wallSidesShinyRadioButton);
      updateWallSidesShininessRadioButtons(controller);
      
    }
    
    if (controller.isPropertyEditable(RoomController.Property.WALL_SIDES_BASEBOARD)) {
      this.wallSidesBaseboardComponent = (LinearLayout)controller.getWallSidesBaseboardController().getView();
      controller.getWallSidesBaseboardController().addPropertyChangeListener(BaseboardChoiceController.Property.VISIBLE, 
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              selectSplitSurroundingWallsAtFirstChange();
            }
          });
    }
    this.dialogTitle = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "room.title");


	  //add a tutorial listener to the floor texture
	  sendTutorialTextureChange = false;
	  controller.addPropertyChangeListener(RoomController.Property.FLOOR_PAINT,
			  new PropertyChangeListener() {
				  public void propertyChange(PropertyChangeEvent ev) {
					  if(ev.getOldValue() ==  RoomController.RoomPaint.DEFAULT && ev.getNewValue() == RoomController.RoomPaint.TEXTURED)
					  {
						  sendTutorialTextureChange = true;
					  }
				  }
			  });

  }

  /**
   * Updates floor color radio buttons. 
   */
  private void updateFloorColorRadioButtons(RoomController controller) {
    if (controller.getFloorPaint() == RoomController.RoomPaint.COLORED) {
      this.floorColorRadioButton.setSelected(true);
    } else if (controller.getFloorPaint() == RoomController.RoomPaint.TEXTURED) {
      this.floorTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.floorColorRadioButton, this.floorTextureRadioButton);
    }
  }

  /**
   * Updates floor shininess radio buttons. 
   */
  private void updateFloorShininessRadioButtons(RoomController controller) {
    if (controller.getFloorShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.floorMattRadioButton, this.floorShinyRadioButton);
    } else if (controller.getFloorShininess() == 0) {
      this.floorMattRadioButton.setSelected(true);
    } else { // null
      this.floorShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates ceiling color radio buttons. 
   */
  private void updateCeilingColorRadioButtons(RoomController controller) {
    if (controller.getCeilingPaint() == RoomController.RoomPaint.COLORED) {
      this.ceilingColorRadioButton.setSelected(true);
    } else if (controller.getCeilingPaint() == RoomController.RoomPaint.TEXTURED) {
      this.ceilingTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.ceilingColorRadioButton, this.ceilingTextureRadioButton);
    }
  }

  /**
   * Updates ceiling shininess radio buttons. 
   */
  private void updateCeilingShininessRadioButtons(RoomController controller) {
    if (controller.getCeilingShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.ceilingMattRadioButton, this.ceilingShinyRadioButton);
    } else if (controller.getCeilingShininess() == 0) {
      this.ceilingMattRadioButton.setSelected(true);
    } else { // null
      this.ceilingShinyRadioButton.setSelected(true);
    }
  }

  /**
   * Updates wall sides radio buttons. 
   */
  private void updateWallSidesRadioButtons(RoomController controller) {
    if (controller.getWallSidesPaint() == RoomController.RoomPaint.COLORED) {
      this.wallSidesColorRadioButton.setSelected(true);
    } else if (controller.getWallSidesPaint() == RoomController.RoomPaint.TEXTURED) {
      this.wallSidesTextureRadioButton.setSelected(true);
    } else { // null
      SwingTools.deselectAllRadioButtons(this.wallSidesColorRadioButton, this.wallSidesTextureRadioButton);
    }
  }

  /**
   * Updates wall sides shininess radio buttons. 
   */
  private void updateWallSidesShininessRadioButtons(RoomController controller) {
    if (controller.getWallSidesShininess() == null) {
      SwingTools.deselectAllRadioButtons(this.wallSidesMattRadioButton, this.wallSidesShinyRadioButton);
    } else if (controller.getWallSidesShininess() == 0) {
      this.wallSidesMattRadioButton.setSelected(true);
    } else { // null
      this.wallSidesShinyRadioButton.setSelected(true);
    }
  }

  private void selectSplitSurroundingWallsAtFirstChange() {
    if (this.firstWallChange
        && this.splitSurroundingWallsCheckBox != null
        && this.splitSurroundingWallsCheckBox.isEnabled()) {
      //this.splitSurroundingWallsCheckBox.doClick();
		this.splitSurroundingWallsCheckBox.setChecked(splitSurroundingWallsCheckBox.isChecked());//TODO: is this the same as the above?
      this.firstWallChange = false;
    }    
  }

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents(UserPreferences preferences)
  {
	  if (this.nameLabel != null || this.areaVisibleCheckBox != null)
	  {
		  //JLabel nameAndAreaPanel = new JLabel(activity,
			//	  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "nameAndAreaPanel.title"));
		  //swapOut(nameAndAreaPanel, R.id.roompanel_nameAndAreaPanel);
		  //PJ removed as pointless
		  removeView(R.id.roompanel_nameAndAreaPanel);

		  swapOut(this.nameLabel, R.id.roompanel_nameLabel);
		  swapOut(this.nameTextField, R.id.roompanel_nameTextField);
		  // in all cases suppresses teh name field
		  //if (this.nameTextField.getText().toString() != null && this.nameTextField.getText().toString().length() > 0)

		  swapOut(this.areaVisibleCheckBox, R.id.roompanel_areaVisibleCheckBox);
	  }
	  else
	  {
		  // no empty table must remove the whole thing
		  removeView(R.id.roompanel_nameAndAreaTable);
	  }

	  if (this.floorVisibleCheckBox != null || this.floorColorRadioButton != null || this.floorMattRadioButton != null)
	  {
		  JLabel floorPanel = new JLabel(activity,
				  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "floorPanel.title"));
		  swapOut(floorPanel, R.id.roompanel_floorPanel);
		  swapOut(this.floorVisibleCheckBox, R.id.roompanel_floorVisibleCheckBox);
		  swapOut(this.floorColorRadioButton, R.id.roompanel_floorColorRadioButton);
		  swapOut(this.floorColorButton, R.id.roompanel_floorColorButton);
		  swapOut(this.floorTextureRadioButton, R.id.roompanel_floorTextureRadioButton);
		  swapOut(this.floorTextureComponent, R.id.roompanel_floorTextureComponent);
		  swapOut(this.floorMattRadioButton, R.id.roompanel_floorMattRadioButton);
		  swapOut(this.floorShinyRadioButton, R.id.roompanel_floorShinyRadioButton);
	  }
	  else
	  {
		  removeView(R.id.roompanel_floorPanelTable);
	  }
	  if (this.ceilingVisibleCheckBox != null || this.ceilingColorRadioButton != null || this.ceilingMattRadioButton != null)
	  {
		  JLabel ceilingPanel = new JLabel(activity,
				  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "ceilingPanel.title"));
		  swapOut(ceilingPanel, R.id.roompanel_ceilingPanel);
		  swapOut(this.ceilingVisibleCheckBox, R.id.roompanel_ceilingVisibleCheckBox);
		  swapOut(this.ceilingColorRadioButton, R.id.roompanel_ceilingColorRadioButton);
		  swapOut(this.ceilingColorButton, R.id.roompanel_ceilingColorButton);
		  swapOut(this.ceilingTextureRadioButton, R.id.roompanel_ceilingTextureRadioButton);
		  swapOut(this.ceilingTextureComponent, R.id.roompanel_ceilingTextureComponent);
		  swapOut(this.ceilingMattRadioButton, R.id.roompanel_ceilingMattRadioButton);
		  swapOut(this.ceilingShinyRadioButton, R.id.roompanel_ceilingShinyRadioButton);
	  }
	  else
	  {
		  removeView(R.id.roompanel_ceilingPanelTable);
	  }
	  if (this.wallSidesColorRadioButton != null || this.wallSidesMattRadioButton != null)
	  {
		  JLabel wallSidesPanel = new JLabel(activity,
				  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesPanel.title"));
		  swapOut(wallSidesPanel, R.id.roompanel_wallSidesPanel);
		  swapOut(this.splitSurroundingWallsCheckBox, R.id.roompanel_splitSurroundingWallsCheckBox);
		  swapOut(this.wallSidesColorRadioButton, R.id.roompanel_wallSidesColorRadioButton);
		  swapOut(this.wallSidesColorButton, R.id.roompanel_wallSidesColorButton);
		  swapOut(this.wallSidesTextureRadioButton, R.id.roompanel_wallSidesTextureRadioButton);
		  swapOut(this.wallSidesTextureComponent, R.id.roompanel_wallSidesTextureComponent);
		  swapOut(this.wallSidesMattRadioButton, R.id.roompanel_wallSidesMattRadioButton);
		  swapOut(this.wallSidesShinyRadioButton, R.id.roompanel_wallSidesShinyRadioButton);
	  }
	  else
	  {
		  removeView(R.id.roompanel_wallSidesPanelTable);
	  }
	  if (this.wallSidesBaseboardComponent != null)
	  {
		  JLabel wallSidesBaseboardPanel = new JLabel(activity,
				  preferences.getLocalizedString(com.eteks.sweethome3d.android_props.RoomPanel.class, "wallSidesBaseboardPanel.title"));
		  swapOut(wallSidesBaseboardPanel, R.id.roompanel_wallSidesBaseboardPanel);
		  swapOut(this.wallSidesBaseboardComponent, R.id.roompanel_wallSidesBaseboardComponent);
	  }
	  else
	  {
		  removeView(R.id.roompanel_wallSidesBaseboardPanel);
		  removeView(R.id.roompanel_wallSidesBaseboardComponent);
	  }

	  this.setTitle(dialogTitle);
	  swapOut(closeButton, R.id.roompanel_closeButton);

  }

  
  /**
   * Displays this panel in a modal dialog box. 
   */
  public void displayView(com.eteks.sweethome3d.viewcontroller.View parentView) {
   /* if (SwingTools.showConfirmDialog((JComponent)parentView,
            this, this.dialogTitle, this.nameTextField) == JOptionPane.OK_OPTION
        && this.controller != null) {
      this.controller.modifyRooms();
    }*/
	  getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
	  this.setOnDismissListener(new OnDismissListener()
	  {
		  @Override
		  public void onDismiss(DialogInterface dialog)
		  {
			  controller.modifyRooms();
			  if(sendTutorialTextureChange)
			  	((Renovations3DActivity)activity).getTutorial().actionComplete(Tutorial.TutorialAction.ROOM_FLOOR_TEXTURE_CHANGED);
		  }
	  });
	  this.show();
  }
}
