/*
 * ModelMaterialsComponent.java 26 oct. 2012
 *
 * Sweet Home 3D, Copyright (c) 2012 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import org.jogamp.java3d.BranchGroup;

import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.android.utils.CheckableImageView;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.HomeMaterial;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ButtonGroup;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JList;
import com.mindblowing.swingish.JRadioButton;
import com.mindblowing.swingish.JSlider;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import javaawt.Color;
import javaawt.Font;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.VMFont;
import javaawt.VMGraphics2D;
import javaawt.geom.AffineTransform;
import javaxswing.Icon;

/**
 * Button giving access to materials editor. When the user clicks
 * on this button a dialog appears to let him choose materials.
 */
public class ModelMaterialsComponent  extends JButton implements View {

	private Activity activity;
	private final UserPreferences preferences;

  /**
   * Creates a texture button.
   */
  public ModelMaterialsComponent(final UserPreferences preferences,
                                 final ModelMaterialsController controller, final Activity activity) {
		super(activity, "");
		this.activity = activity;
		this.preferences = preferences;
		final float scale = getResources().getDisplayMetrics().density;
		ModelMaterialsComponent.ModelMaterialsPanel.iconSizePx = (int) (ModelMaterialsComponent.ModelMaterialsPanel.ICON_SIZE_DP * scale + 0.5f);
		ModelMaterialsComponent.ModelMaterialsPanel.fontSizePx = (int) (ModelMaterialsComponent.ModelMaterialsPanel.fontSizeDp * scale + 0.5f);
		ModelMaterialsComponent.ModelMaterialsPanel.namePadBottomPx = (int) (ModelMaterialsComponent.ModelMaterialsPanel.namePadBottomDp * scale + 0.5f);
    //setText(SwingTools.getLocalizedLabelText(preferences, ModelMaterialsComponent.class, "modifyButton.text"));
    //if (!OperatingSystem.isMacOSX()) {
    //  setMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
    //      ModelMaterialsComponent.class, "modifyButton.mnemonic")).getKeyCode());
    //}
    // Add a listener to update materials
    addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent ev) {
          final ModelMaterialsPanel texturePanel = new ModelMaterialsPanel(preferences, controller, activity);
          texturePanel.displayView(ModelMaterialsComponent.this);
        }
      });
  }
  
  /**
   * A panel that displays available textures in a list to let user make choose one. 
   */
  private static class ModelMaterialsPanel extends AndroidDialogView {//JPanel {
		public static int ICON_SIZE_DP = 48;
		public static int iconSizePx = 48;
		public static int fontSizeDp = 12;
		public static int namePadBottomDp = 6;
		public static int fontSizePx = 12;
		public static int namePadBottomPx = 6;

    private final ModelMaterialsController controller;
    
    private JLabel 								 previewLabel;
    private ModelPreviewComponent  previewComponent;
    private JLabel                 materialsLabel;
    private JList 								 materialsList;
    private JLabel                 colorAndTextureLabel;
    private JRadioButton 					 defaultColorAndTextureRadioButton;
    private JRadioButton           invisibleRadioButton;
    private JRadioButton           colorRadioButton;
    private ColorButton            colorButton;
    private JRadioButton           textureRadioButton;
    private JButton 						   textureComponent;
    private JLabel                 shininessLabel;
    private JSlider 							 shininessSlider;
    private PropertyChangeListener textureChangeListener;

		private Activity activity;

    public ModelMaterialsPanel(UserPreferences preferences, 
                               ModelMaterialsController controller, Activity activity) {
			super(preferences, activity, R.layout.dialog_modelmaterialspanel);
			this.activity = activity;
      this.controller = controller;
      createComponents(preferences, controller);
      setMnemonics(preferences);
      layoutComponents();
    }
  
    /**
     * Creates and initializes components.
     */
    private void createComponents(final UserPreferences preferences, 
                                  final ModelMaterialsController controller) {
      this.materialsLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "materialsLabel.text"));
			MaterialsListModel materialsListModel = new MaterialsListModel(controller);
      this.materialsList = new JList(activity, materialsListModel);
      this.materialsList.setSelectionMode(JList.ListSelectionModel.SINGLE_SELECTION);
      this.materialsList.setCellRenderer(new MaterialListCellRenderer(activity, materialsListModel.toList()));
      
      this.previewLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "previewLabel.text"));
      this.previewComponent = new ModelPreviewComponent(true, activity);
      this.previewComponent.setFocusable(false);
			this.previewComponent.setMinimumWidth(this.previewComponent.getPreferredSize().width);
			this.previewComponent.setMinimumHeight(this.previewComponent.getPreferredSize().height);
      ModelManager.getInstance().loadModel(controller.getModel(), new ModelManager.ModelObserver() {
          public void modelUpdated(BranchGroup modelRoot) {
            final MaterialsListModel materialsListModel = (MaterialsListModel)materialsList.getModel();
            previewComponent.setModel(controller.getModel(), controller.isBackFaceShown(), controller.getModelRotation(),
                controller.getModelWidth(), controller.getModelDepth(), controller.getModelHeight());
            previewComponent.setModelMaterials(materialsListModel.getMaterials());
            previewComponent.setModelTranformations(controller.getModelTransformations());
            materialsListModel.addListDataListener(new JList.ListDataListener() {
                public void contentsChanged(JList.ListDataEvent ev) {
                  previewComponent.setModelMaterials(materialsListModel.getMaterials());
                }
                
                public void intervalRemoved(JList.ListDataEvent ev) {
                }
                
                public void intervalAdded(JList.ListDataEvent ev) {
                }
              });
          }
  
          public void modelError(Exception ex) {
            // Should happen only if model is missing
            previewLabel.setEnabled(false);
            previewComponent.setEnabled(false);
          }
        });
      
      this.colorAndTextureLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "colorAndTextureLabel.text"));

      // Create color and texture image radio buttons properties
      this.defaultColorAndTextureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "defaultColorAndTextureRadioButton.text"));
      final ChangeListener defaultChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (defaultColorAndTextureRadioButton.isEnabled() && defaultColorAndTextureRadioButton.isSelected()) {
              for (int index : materialsList.getSelectedIndices()) {
                HomeMaterial material = (HomeMaterial)materialsList.getItemAtPosition(index);
              ((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), null, null, material.getShininess()),
                    index);
              }
            }
          }
        };
      this.defaultColorAndTextureRadioButton.addChangeListener(defaultChoiceChangeListener);

      this.invisibleRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "invisibleRadioButton.text"));
      final ChangeListener invisibleChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (invisibleRadioButton.isEnabled() && invisibleRadioButton.isSelected()) {
              for (int index : materialsList.getSelectedIndices()) {
                HomeMaterial material = (HomeMaterial)materialsList.getItemAtPosition(index);
              	((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), 0, null, material.getShininess()),
                    index);
              }
            }
          }
        };
      this.invisibleRadioButton.addChangeListener(invisibleChoiceChangeListener);

      this.colorRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "colorRadioButton.text"));
      final ChangeListener colorChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (colorRadioButton.isEnabled() && colorRadioButton.isSelected()) {
              for (int index : materialsList.getSelectedIndices()) {
                HomeMaterial material = (HomeMaterial)materialsList.getItemAtPosition(index);
              	Integer defaultColor = ((MaterialsListModel)materialsList.getModel()).
                    getDefaultMaterialAt(index).getColor();
              	Integer color = defaultColor != colorButton.getColor()
                  ? colorButton.getColor()
                  : null;
              	((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), color, null, material.getShininess()),
                    index);
              }
            }
          }
        };
      this.colorRadioButton.addChangeListener(colorChoiceChangeListener);
      this.colorButton = new ColorButton(activity, preferences);
      this.colorButton.setColorDialogTitle(preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "colorDialog.title"));
      final PropertyChangeListener colorChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (!colorRadioButton.isSelected()) {
              colorRadioButton.setSelected(true);
            } else {
              colorChoiceChangeListener.stateChanged(null);
            }
          }
        };
      this.colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, colorChangeListener);
      
      final TextureChoiceController textureController = controller.getTextureController();
      this.textureRadioButton = new JRadioButton(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "textureRadioButton.text"));
      final ChangeListener textureChoiceChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            if (textureRadioButton.isEnabled() && textureRadioButton.isSelected()) {
              for (int index : materialsList.getSelectedIndices()) {
                HomeMaterial material = (HomeMaterial)materialsList.getItemAtPosition(index);
              	HomeTexture defaultTexture = ((MaterialsListModel)materialsList.getModel()).
                    getDefaultMaterialAt(index).getTexture();
              	HomeTexture texture = defaultTexture != textureController.getTexture()
                  ? textureController.getTexture()
                  : null;
              	((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                  new HomeMaterial(material.getName(), null, texture, material.getShininess()),
                    index);
              }
            }
          }
        };
      this.textureRadioButton.addChangeListener(textureChoiceChangeListener);
      this.textureComponent = (JButton)textureController.getView();
      this.textureChangeListener = new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            if (!textureRadioButton.isSelected()) {
              textureRadioButton.setSelected(true);
            } else {
              textureChoiceChangeListener.stateChanged(null);
            }
          }
        };
      // Listener added and removed in displayView()
        
      ButtonGroup buttonGroup = new ButtonGroup();
      buttonGroup.add(this.defaultColorAndTextureRadioButton);
      buttonGroup.add(this.invisibleRadioButton);
      buttonGroup.add(this.colorRadioButton);
      buttonGroup.add(this.textureRadioButton);
      
      this.shininessLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "shininessLabel.text"));
      this.shininessSlider = new JSlider(activity, 0, 128);
      JLabel mattLabel = new JLabel(activity, preferences.getLocalizedString(
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "mattLabel.text"));
      JLabel shinyLabel = new JLabel(activity, preferences.getLocalizedString(
							com.eteks.sweethome3d.android_props.ModelMaterialsComponent.class, "shinyLabel.text"));
      Dictionary<Integer,JLabel> shininessSliderLabelTable = new Hashtable<Integer,JLabel>();
      shininessSliderLabelTable.put(0, mattLabel);
      shininessSliderLabelTable.put(128, shinyLabel);
      //TODO: find equivilents to these 2 methods
      //this.shininessSlider.setLabelTable(shininessSliderLabelTable);
      //this.shininessSlider.setPaintLabels(true);
      this.shininessSlider.setPaintTicks(true);
      this.shininessSlider.setMajorTickSpacing(16);
      final ChangeListener shininessChangeListener = new ChangeListener() {
          public void stateChanged(ChangeEvent ev) {
            for (int index : materialsList.getSelectedIndices()) {
              HomeMaterial material = (HomeMaterial)materialsList.getItemAtPosition(index);
              int shininess = shininessSlider.getValue();
              ((MaterialsListModel)materialsList.getModel()).setMaterialAt(
                new HomeMaterial(material.getName(), material.getColor(), material.getTexture(), shininess / 128f),
                  index);
            }
          }
        };
      this.shininessSlider.addChangeListener(shininessChangeListener);

			this.materialsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
              if (!(materialsList.getCheckedItemCount() == 0)) {
                defaultColorAndTextureRadioButton.removeChangeListener(defaultChoiceChangeListener);
                invisibleRadioButton.removeChangeListener(invisibleChoiceChangeListener);
                colorRadioButton.removeChangeListener(colorChoiceChangeListener);
                textureRadioButton.removeChangeListener(textureChoiceChangeListener);
                colorButton.removePropertyChangeListener(ColorButton.COLOR_PROPERTY, colorChangeListener);
                if (((TextureChoiceComponent)textureController.getView()).isEnabled()) {
                  // Remove listener only if its texture component is shown because its listener is added later
                  textureController.removePropertyChangeListener(TextureChoiceController.Property.TEXTURE, textureChangeListener);
                }
                shininessSlider.removeChangeListener(shininessChangeListener);
                
                HomeMaterial material = (HomeMaterial)materialsList.getItemAtPosition(position);
                HomeTexture texture = material.getTexture();
                Integer color = material.getColor();
                Float shininess = material.getShininess();
                HomeMaterial defaultMaterial = ((MaterialsListModel)materialsList.getModel()).getDefaultMaterialAt(position);
                if (color == null && texture == null) {
                  defaultColorAndTextureRadioButton.setSelected(true);
                  // Display default color or texture in buttons
                  texture = defaultMaterial.getTexture();
                  if (texture != null) {
                    colorButton.setColor(null);
                    controller.getTextureController().setTexture(texture);
                  } else {
                    color = defaultMaterial.getColor();
                    if (color != null) {
                      textureController.setTexture(null);
                      colorButton.setColor(color);
                    }
                  }
                } else if (texture != null) {
                  textureRadioButton.setSelected(true);
                  colorButton.setColor(null);
                  textureController.setTexture(texture);
                } else if ((color.intValue() & 0xFF000000) == 0) {
                  invisibleRadioButton.setSelected(true);
                  // Display default color or texture in buttons
                  texture = defaultMaterial.getTexture();
                  if (texture != null) {
                    colorButton.setColor(null);
                    controller.getTextureController().setTexture(texture);
                  } else {
                    color = defaultMaterial.getColor();
                    if (color != null) {
                      textureController.setTexture(null);
                      colorButton.setColor(color);
                    }
                  }
                } else {
                  colorRadioButton.setSelected(true);
                  textureController.setTexture(null);
                  colorButton.setColor(color);
                }         

                if (shininess != null) {
                  shininessSlider.setValue((int)(shininess * 128));
                } else {
                  shininessSlider.setValue((int)(defaultMaterial.getShininess() * 128));
                }
                
                defaultColorAndTextureRadioButton.addChangeListener(defaultChoiceChangeListener);
                invisibleRadioButton.addChangeListener(invisibleChoiceChangeListener);
                colorRadioButton.addChangeListener(colorChoiceChangeListener);
                textureRadioButton.addChangeListener(textureChoiceChangeListener);
                colorButton.addPropertyChangeListener(ColorButton.COLOR_PROPERTY, colorChangeListener);
                if (((JButton)textureController.getView()).isEnabled()) {
                  textureController.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE, textureChangeListener);
                }
                shininessSlider.addChangeListener(shininessChangeListener);
              }
              enableComponents();
            }
          });
      
      /*this.materialsList.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent ev) {
          // Open color or texture choice dialogs on double clicks
          if (ev.getClickCount() == 2 && !materialsList.isSelectionEmpty()) {
            if (colorButton.getColor() != null) {
              colorButton.doClick(200);
            } else if (controller.getTextureController().getTexture() != null
                       && textureComponent instanceof AbstractButton) {
              ((AbstractButton)textureComponent).doClick(200);
            }
          }
        }
      });*/

      //PJ is this important?
      /*if (this.materialsList.getModel().getSize() > 0) {
        this.materialsList.setSelectedIndex(0);
      } else {
        // Add a listener that will select first row as soon as the list contains some data      
        this.materialsList.getModel().addListDataListener(new ListDataListener() {
            public void intervalAdded(ListDataEvent ev) {     
              materialsList.setSelectedIndex(0);
              ((ListModel)ev.getSource()).removeListDataListener(this);
            }
            
            public void contentsChanged(ListDataEvent ev) {
              intervalAdded(ev);
            }
            
            public void intervalRemoved(ListDataEvent ev) {
            }          
          });
      }*/

      enableComponents();
    }
  
    /**
     * Renderer used to display the materials in list. 
     */
    private class MaterialListCellRenderer extends ArrayAdapter<HomeMaterial> {
      private Font defaultFont;
      private Icon emptyIcon = new Icon() {
          public void paintIcon(Object c, Graphics g, int x, int y) {
          }
  
          public int getIconHeight() {
            return iconSizePx;//defaultFont.getSize();
          }
    
          public int getIconWidth() {
            return getIconHeight();
          }
        };
			public MaterialListCellRenderer(Context context, List<HomeMaterial> catalog) {
				super(context, android.R.layout.simple_list_item_1, catalog);
				this.defaultFont = new VMFont(Typeface.DEFAULT, fontSizePx);//getFont();
			}

			@Override
			public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
				return getDropDownView(position, convertView, parent);
			}
			@Override
			public android.view.View getDropDownView (final int position, android.view.View convertView, ViewGroup parent) {
        final HomeMaterial material = (HomeMaterial) this.getItem(position);
        HomeTexture materialTexture = material.getTexture();
        Integer materialColor = material.getColor();
        HomeMaterial defaultMaterial = ((MaterialsListModel)materialsList.getModel()).getDefaultMaterialAt(position);
        if (materialTexture == null && materialColor == null) {
          materialTexture = defaultMaterial.getTexture();
          if (materialTexture == null) {
            materialColor = defaultMaterial.getColor();
          }
        }
				final Icon icon;
        if (materialTexture != null) {
          // Display material texture image with an icon  
          final HomeTexture texture = materialTexture;
					icon = new Icon() {
              public int getIconHeight() {
                return iconSizePx;//defaultFont.getSize();
              }
        
              public int getIconWidth() {
                return getIconHeight();
              }
              
              public void paintIcon(Object c, Graphics g, int x, int y) {
                Icon icon = IconManager.getInstance().getIcon(texture.getImage(), getIconHeight(), null);
                if (icon.getIconWidth() != icon.getIconHeight()) {
                  Graphics2D g2D = (Graphics2D)g;
                  AffineTransform previousTransform = g2D.getTransform();
                  g2D.translate(x, y);
                  g2D.scale((float)icon.getIconHeight() / icon.getIconWidth(), 1);
                  icon.paintIcon(c, g2D, 0, 0);
                  g2D.setTransform(previousTransform);
                } else {
                  icon.paintIcon(c, g, x, y);
                }
              }
            };
        } else if (materialColor != null 
                   && (materialColor.intValue() & 0xFF000000) != 0) {
          // Display material color with an icon  
          final Color color = new Color(materialColor);
					icon = new Icon () {
              public int getIconHeight() {
                return iconSizePx;//defaultFont.getSize();
              }
      
              public int getIconWidth() {
                return getIconHeight();
              }
      
              public void paintIcon(Object c, Graphics g, int x, int y) {
                int squareSize = getIconHeight();                
                g.setColor(color);          
                g.fillRect(x + 2, y + 2, squareSize - 3, squareSize - 3);
                g.setColor(Color.BLACK);//c.getParent().getParent().getForeground());
                g.drawRect(x + 1, y + 1, squareSize - 2, squareSize - 2);
              }
            };
        } else {
					icon = this.emptyIcon;
        }
				ImageView ret = new CheckableImageView(getContext()) {
					public void onDraw(Canvas canvas) {
						Graphics2D g2D = new VMGraphics2D(canvas);
						icon.paintIcon(null, g2D, 0, 0);
						String value = material.getName();
						g2D.setFont(defaultFont);
						g2D.setColor(Color.BLACK);
						g2D.drawString(value, 0, this.getHeight() - namePadBottomPx);// at the bottom
						if (isChecked()) {
							g2D.setColor(Color.DARK_GRAY);
							g2D.drawRect(0, 0, icon.getIconWidth(), icon.getIconHeight());
							g2D.drawRect(1, 1, icon.getIconWidth() - 2, icon.getIconHeight() - 2);
							g2D.drawRect(2, 2, icon.getIconWidth() - 3, icon.getIconHeight() - 3);
						}
					}
				};
				ret.setMinimumWidth(icon.getIconWidth());
				ret.setMinimumHeight(icon.getIconHeight());

				return ret;
      }
    }
  
    /**
     * Enables editing components according to current selection in materials list.
     */
    private void enableComponents() {
      boolean selectionEmpty = this.materialsList.getCheckedItemCount() == 0;
      defaultColorAndTextureRadioButton.setEnabled(!selectionEmpty);
      invisibleRadioButton.setEnabled(!selectionEmpty);
      textureRadioButton.setEnabled(!selectionEmpty);
      textureComponent.setEnabled(!selectionEmpty);
      colorRadioButton.setEnabled(!selectionEmpty);
      colorButton.setEnabled(!selectionEmpty);
      shininessSlider.setEnabled(!selectionEmpty);
    }

    /**
     * Sets components mnemonics and label / component associations.
     */
    private void setMnemonics(UserPreferences preferences) {
    }
    
    /**
     * Layouts components in panel with their labels. 
     */
    private void layoutComponents() {
      int standardGap = Math.round(5 * SwingTools.getResolutionScale());
      // Preview
			swapOut(this.previewLabel, R.id.mmp_previewLabel);
      //float resolutionScale = SwingTools.getResolutionScale();
      //this.previewComponent.setPreferredSize(new Dimension((int)(250 * resolutionScale), (int)(250 * resolutionScale)));
			swapOut(this.previewComponent, R.id.mmp_previewComponent);
      // Materials list
			swapOut(this.materialsLabel, R.id.mmp_materialsLabel);
			swapOut(this.materialsList, R.id.mmp_materialsList);
      //JScrollPane scrollPane = new JScrollPane(this.materialsList);
      //Dimension preferredSize = scrollPane.getPreferredSize();
      //scrollPane.setPreferredSize(new Dimension(Math.min(200, preferredSize.width), preferredSize.height));
      //SwingTools.installFocusBorder(this.materialsList);
      
      // Color and Texture
			swapOut(this.colorAndTextureLabel, R.id.mmp_colorAndTextureLabel);
			swapOut(this.defaultColorAndTextureRadioButton, R.id.mmp_defaultColorAndTextureRadioButton);
			swapOut(this.invisibleRadioButton, R.id.mmp_invisibleRadioButton);
			swapOut(this.colorRadioButton, R.id.mmp_colorRadioButton);
			swapOut(this.colorButton, R.id.mmp_colorButton);
			swapOut(this.textureRadioButton, R.id.mmp_textureRadioButton);
			swapOut(this.textureComponent, R.id.mmp_textureComponent);
      //this.textureComponent.setPreferredSize(this.colorButton.getPreferredSize());

      // Shininess
			swapOut(this.shininessLabel, R.id.mmp_shininessLabel);
			swapOut(this.shininessSlider, R.id.mmp_shininessSlider);

			this.setTitle(controller.getDialogTitle());
			swapOut(closeButton, R.id.mmp_closeButton);
    }
    
    public void displayView(View parent) {
      // Show panel in a resizable modal dialog
      /*final JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE,
          JOptionPane.OK_CANCEL_OPTION);
      JComponent parentComponent = SwingUtilities.getRootPane((JComponent)parent);
      if (parentComponent != null) {
        optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
      }
      final JDialog dialog = optionPane.createDialog(parentComponent, controller.getDialogTitle());
      dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
      dialog.setResizable(true);
      // Pack again because resize decorations may have changed dialog preferred size
      dialog.pack();
      dialog.setMinimumSize(getPreferredSize());*/
      this.controller.getTextureController().addPropertyChangeListener(
          TextureChoiceController.Property.TEXTURE, this.textureChangeListener);
      // Add a listener that transfer focus to focusable field of texture panel when dialog is shown
     /* dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent(ModelMaterialsPanel.this);
            dialog.removeComponentListener(this);
          }
        });
      // Run a timer that will make blink the selected material in preview
      final MaterialBlinker selectedMaterialBlinker = new MaterialBlinker();
      selectedMaterialBlinker.start();
      this.materialsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(ListSelectionEvent ev) {
            selectedMaterialBlinker.restart();
          }
        });

      dialog.setVisible(true);
      dialog.dispose();
      
      selectedMaterialBlinker.stop();

      this.controller.getTextureController().removePropertyChangeListener(
          TextureChoiceController.Property.TEXTURE, this.textureChangeListener);
      if (Integer.valueOf(JOptionPane.OK_OPTION).equals(optionPane.getValue())) {
        this.controller.setMaterials(((MaterialsListModel)this.materialsList.getModel()).getMaterials());
      }*/

			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
			this.setOnDismissListener(new OnDismissListener() {
				@Override
				public void onDismiss(DialogInterface dialog)
				{
					controller.getTextureController().removePropertyChangeListener(
									TextureChoiceController.Property.TEXTURE, textureChangeListener);
					controller.setMaterials(((MaterialsListModel)materialsList.getModel()).getMaterials());
				}
			});
			this.show();
    }
 
    
    /**
     * Model for materials list.
     */
    private static class MaterialsListModel extends JList.AbstractListModel  {
      private HomeMaterial [] defaultMaterials;
      private HomeMaterial [] materials;

      public MaterialsListModel(final ModelMaterialsController controller) {
          this.materials = controller.getMaterials();
          ModelManager.getInstance().loadModel(controller.getModel(), 
            new ModelManager.ModelObserver() {
              public void modelUpdated(BranchGroup modelRoot) {
                defaultMaterials = ModelManager.getInstance().getMaterials(modelRoot, controller.getModelCreator());
								if (materials != null) {
									// Keep only materials that are defined in default materials set
									// (the list can be different if the model loader interprets differently a 3D model file
									// or if materials come from a paste style action)
									HomeMaterial [] updatedMaterials = new HomeMaterial [defaultMaterials.length];
									boolean foundInDefaultMaterials = false;
									for (int i = 0; i < defaultMaterials.length; i++) {
										String materialName = defaultMaterials [i].getName();
										for (int j = 0; j < materials.length; j++) {
											if (materials [j] != null
													&& materials [j].getName().equals(materialName)) {
												updatedMaterials [i] = materials [j];
												foundInDefaultMaterials = true;
												break;
											}
										}
									}
									if (foundInDefaultMaterials) {
										materials = updatedMaterials;
									} else {
										materials = null;
									}
								}
								fireContentsChanged(MaterialsListModel.this, 0, defaultMaterials.length);
							}

							public void modelError(Exception ex) {
								// Let the list be empty
							}
          	});
      }

      public Object getElementAt(int index) {
        if (this.materials != null
            && this.materials [index] != null
            && this.materials [index].getName() != null
            && this.materials [index].getName().equals(this.defaultMaterials [index].getName())) {
          return this.materials [index];
        } else {
          return new HomeMaterial(this.defaultMaterials [index].getName(), null, null, null); 
        }
      }

      public int getSize() {
        if (this.defaultMaterials != null) {
          return this.defaultMaterials.length;
        } else {
          return 0;
        }
      }
      
      public HomeMaterial getDefaultMaterialAt(int index) {
        return this.defaultMaterials [index];
      }

      /**
       * Sets the material at the given <code>index</code>.
       */
      public void setMaterialAt(HomeMaterial material, int index) {
        if (this.materials != null
            && material.getColor() == null
            && material.getTexture() == null
            && material.getShininess() == null) {
          this.materials [index] = null;
          boolean containsOnlyNull = true;
          for (HomeMaterial m : this.materials) {
            if (m != null) {
              containsOnlyNull = false;
              break;
            }
          }
          if (containsOnlyNull) {
            this.materials = null;
          }
        } else {
          if (this.materials == null || this.materials.length != this.defaultMaterials.length) {
            this.materials = new HomeMaterial [this.defaultMaterials.length];
          }
          this.materials [index] = material;
        }
        fireContentsChanged(this, index, index);
      }

      /**
       * Returns the edited materials. 
       */
      public HomeMaterial [] getMaterials() {
        return this.materials;
      }

			public List<HomeMaterial> toList() {
      	List<HomeMaterial> ret =  new ArrayList<HomeMaterial>();
				for(int i = 0 ; i <  getSize(); i++)
					ret.add((HomeMaterial) getElementAt(i));
				return ret;
			}
    }
    
    /**
     * A timer that makes blink the selected material in preview.
     */
   /* private class MaterialBlinker extends Timer {
      public MaterialBlinker() {
        super(500, null);
        addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
              toggleBlinkingState();
            }
          });
      }

      private void toggleBlinkingState() {
        MaterialsListModel listModel = (MaterialsListModel)materialsList.getModel();
        HomeMaterial [] materials = listModel.getMaterials();
        if (listModel.getSize() > 1) {
          if (getDelay() != 1000) {
            setDelay(1000);
            for (int index : materialsList.getSelectedIndices()) {
              if (materials == null) {
                materials = new HomeMaterial [listModel.getSize()];
              } else {
                materials = materials.clone();
              }
              HomeMaterial defaultMaterial = listModel.getDefaultMaterialAt(index);
              HomeMaterial selectedMaterial = materials [index] != null
                  ? materials [index]
                  : defaultMaterial;
              int blinkColor = materialsList.getSelectionBackground().darker().getRGB();
              if (selectedMaterial.getTexture() == null) {
                Integer selectedColor = selectedMaterial.getColor();
                if (selectedColor == null) {
                  selectedColor = defaultMaterial.getColor();
                }
                int red   = (selectedColor >> 16) & 0xFF;
                int green = (selectedColor >> 8) & 0xFF;
                int blue  = selectedColor & 0xFF;
                if (Math.max(red, Math.max(green, blue)) > 0x77) {
                  // Display a darker color for a bright color
                  blinkColor = new Color(selectedColor).darker().darker().getRGB();
                } else if ((red + green + blue) / 3 > 0x0F) {
                  // Display a brighter color for a dark color
                  blinkColor = new Color(selectedColor).brighter().brighter().getRGB();
                }
              }
              materials [index] =
                  new HomeMaterial(selectedMaterial.getName(), blinkColor, null, selectedMaterial.getShininess());
              previewComponent.setModelMaterials(materials);
            }
          } else {
            setDelay(100);
            previewComponent.setModelMaterials(materials);
          }
        }
      }
      
      @Override
      public void restart() {
        setInitialDelay(100);
        setDelay(100);
        super.restart();
      }
    }*/
  }
}