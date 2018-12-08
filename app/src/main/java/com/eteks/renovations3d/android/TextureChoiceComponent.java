/*
 * TextureChoiceComponent.java 05 oct. 2007
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
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eteks.renovations3d.android.utils.CheckableImageView;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JList;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.JTextField;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;
import com.eteks.sweethome3d.model.HomeTexture;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.TexturesCatalog;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceView;
import com.eteks.sweethome3d.viewcontroller.View;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.SpinnerNumberModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javaawt.Color;
import javaawt.Dimension;
import javaawt.EventQueue;
import javaawt.Font;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.Shape;
import javaawt.VMFont;
import javaawt.VMGraphics2D;
import javaawt.geom.AffineTransform;
import javaawt.imageio.ImageIO;
import javaxswing.Icon;



/**
 * Button displaying a texture as an icon. When the user clicks
 * on this button a dialog appears to let him choose an other texture.
 */
public class TextureChoiceComponent extends JButton implements TextureChoiceView {

	private Activity activity;
	private final UserPreferences preferences;

  /**
   * Creates a texture button.
   */
  public TextureChoiceComponent(final UserPreferences preferences,
                                final TextureChoiceController controller, final Activity activity) {
	  super(activity, "");
	  this.activity = activity;
    this.preferences = preferences;
    //JLabel dummyLabel = new JLabel(activity, "Text");
    //Dimension iconDimension = dummyLabel.getPreferredSize();
    //final int iconHeight = iconDimension.height;

	  final float scale = getResources().getDisplayMetrics().density;
	  TexturePanel.TextureIcon.sizePx = (int) (TexturePanel.TextureIcon.SIZE_DP * scale + 0.5f);
	  TexturePanel.fontSizePx = (int) (TexturePanel.fontSizeDp * scale + 0.5f);
	  TexturePanel.namePadBottomPx = (int) (TexturePanel.namePadBottomDp * scale + 0.5f);

    controller.addPropertyChangeListener(TextureChoiceController.Property.TEXTURE,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            postInvalidate();
			  //repaint();
          }
        });

	  setBackgroundDrawable(new Drawable()
	  {
		  @Override
		  public void draw(Canvas canvas)
		  {
			  int iconHeight = getHeight();
			  int buttonWidth = getWidth();
			  int x = 0;
			  int y = 0;
			  Graphics2D g2D = new VMGraphics2D(canvas);
			 // g.setColor(Color.BLACK);
			 // g.drawRect(x + 2, y + 2, buttonWidth - 4, iconHeight - 4);

			  Paint pp = new Paint();
			  pp.setColor(Color.white.getRGB());
			  pp.setShadowLayer(10.0f, 0.0f, 2.0f, 0xFF000000);
			  RectF rectangle = new RectF(5, 5, buttonWidth - 10, iconHeight - 10);
			  canvas.drawRoundRect (rectangle, 6, 6, pp);

			  HomeTexture texture = controller.getTexture();
			  if (texture != null) {
				  Icon icon = IconManager.getInstance().getIcon(
						  texture.getImage(), iconHeight - 6, null);//TextureChoiceComponent.this);
				  if (icon.getIconWidth() != icon.getIconHeight()) {
					  AffineTransform previousTransform = g2D.getTransform();
					  g2D.translate(x + 3 + ((buttonWidth/2) - (iconHeight/2)), y + 3);
					  g2D.scale((float)icon.getIconHeight() / icon.getIconWidth(), 1);
					  icon.paintIcon(null, g2D, 0, 0);
					  g2D.setTransform(previousTransform);
				  } else {
					  icon.paintIcon(null, g2D, x + 3 + ((buttonWidth/2) - (iconHeight/2)), y + 3);
				  }
			  }
		  }

		  @Override
		  public void setAlpha(int alpha)
		  {
		  }

		  @Override
		  public void setColorFilter(ColorFilter colorFilter)
		  {
		  }

		  @Override
		  public int getOpacity()
		  {
			  return PixelFormat.OPAQUE;
		  }
	  });
    /*setIcon(new Icon() {
        public int getIconWidth() {
          return iconHeight;
        }
  
        public int getIconHeight() {
          return iconHeight;
        }
  
        public void paintIcon(Object c, Graphics g, int x, int y) {
          g.setColor(Color.BLACK);
          g.drawRect(x + 2, y + 2, iconHeight - 5, iconHeight - 5);
          HomeTexture texture = controller.getTexture();
          if (texture != null) {
            Icon icon = IconManager.getInstance().getIcon(
                texture.getImage(), iconHeight - 6, null);//TextureChoiceComponent.this);
            if (icon.getIconWidth() != icon.getIconHeight()) {
              Graphics2D g2D = (Graphics2D)g;
              AffineTransform previousTransform = g2D.getTransform();
              g2D.translate(x + 3, y + 3);
              g2D.scale((float)icon.getIconHeight() / icon.getIconWidth(), 1);
              icon.paintIcon(c, g2D, 0, 0);
              g2D.setTransform(previousTransform);
            } else {
              icon.paintIcon(c, g, x + 3, y + 3);
            }
          }
        }
      });*/

    // Add a listener to update texture
    addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ev) {
        final TexturePanel texturePanel = new TexturePanel(preferences, controller, activity);
        texturePanel.displayView(TextureChoiceComponent.this);
      }
    });
  }

  /**
   * Displays a dialog that let user choose whether he wants to delete
   * the selected texture from catalog or not.
   * @return <code>true</code> if user confirmed to delete.
   */
  public boolean confirmDeleteSelectedCatalogTexture() {
    // Retrieve displayed text in buttons and message
    String message = this.preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "confirmDeleteSelectedCatalogTexture.message");
    String title = this.preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "confirmDeleteSelectedCatalogTexture.title");
    String delete = this.preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "confirmDeleteSelectedCatalogTexture.delete");
    String cancel = this.preferences.getLocalizedString(
        com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "confirmDeleteSelectedCatalogTexture.cancel");

    return JOptionPane.showOptionDialog(
        activity, message, title,
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
        null, new Object [] {delete, cancel}, cancel) == JOptionPane.OK_OPTION;
  }

  /**
   * A panel that displays available textures in a list to let user make choose one.
   */
  private static class TexturePanel extends AndroidDialogView{//JPanel {

	  public static int fontSizeDp = 12;
	  public static int namePadBottomDp = 6;
	  public static int fontSizePx = 12;
	  public static int namePadBottomPx = 6;

	  private static final int PREVIEW_ICON_SIZE = Math.round(128 * SwingTools.getResolutionScale());;
    private static String    searchFilterText = "";
    private static Dimension dialogPreferredSize;

    private TextureChoiceController controller;

    private TextureImage            previewTexture;
    private JLabel                  searchLabel;
    private JTextField 							searchTextField;
    private JLabel                  chosenTextureLabel;
    private ScaledImageComponent    texturePreviewComponent;
    private JLabel                  availableTexturesLabel;
    private JList 									availableTexturesList;
    private JLabel                  angleLabel;
    private JSpinner 				 				angleSpinner;
    private JLabel                  scaleLabel;
    private JSpinner                scaleSpinner;
    private JButton 								importTextureButton;
    private JButton                 modifyTextureButton;
    private JButton                 deleteTextureButton;
    //private JPanel                  recentTexturesPanel;
    //private CatalogItemToolTip      toolTip;

	  private Activity activity;

    public TexturePanel(UserPreferences preferences,
                        TextureChoiceController controller, Activity activity) {
			super(preferences, activity, R.layout.dialog_texturechoice);
			this.activity = activity;
      this.controller = controller;
      createComponents(preferences, controller);
      layoutComponents();
    }

    /**
     * Creates and initializes components.
     */
    private void createComponents(final UserPreferences preferences,
                                  final TextureChoiceController controller) {
      this.availableTexturesLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "availableTexturesLabel.text"));
      final TexturesCatalogListModel texturesListModel = new TexturesCatalogListModel(preferences.getTexturesCatalog());
      this.availableTexturesList = new JList(activity, texturesListModel);
      this.availableTexturesList.setSelectionMode(JList.ListSelectionModel.SINGLE_SELECTION);
      this.availableTexturesList.setCellRenderer(new TextureListCellRenderer(activity, texturesListModel.toList()));
      this.availableTexturesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, android.view.View view, int position, long id) {
					CatalogTexture selectedTexture = (CatalogTexture) availableTexturesList.getItemAtPosition(position);

              if (selectedTexture != null) {
                setPreviewTexture(selectedTexture);
              }
              if (modifyTextureButton != null) {
                modifyTextureButton.setEnabled(selectedTexture != null && selectedTexture.isModifiable());
              }
              if (deleteTextureButton != null) {
                deleteTextureButton.setEnabled(selectedTexture != null && selectedTexture.isModifiable());
              }
							availableTexturesList.postInvalidate();
            }
          });

      this.searchLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
          com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "searchLabel.text"));
      this.searchTextField = new JTextField(activity, 5);
			searchTextField.setHint(R.string.search_hint);
			searchTextField.addTextChangedListener(new TextWatcher(){
				public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
					texturesListModel.setFilterText(searchTextField.getText().toString());
				}
				public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
				public void afterTextChanged(Editable arg0) {
				}
			});
   /*   this.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
          public void changedUpdate(DocumentEvent ev) {
            Object selectedValue = availableTexturesList.getSelectedValue();
            texturesListModel.setFilterText(searchTextField.getText());
            if (selectedValue != null) {
              availableTexturesList.clearSelection();
              availableTexturesList.setSelectedValue(selectedValue, true);
              
              if (texturesListModel.getSize() == 1) {
                availableTexturesList.setSelectedIndex(0);
              }
            }
          }
    
          public void insertUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
    
          public void removeUpdate(DocumentEvent ev) {
            changedUpdate(ev);
          }
        });
      this.searchTextField.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "deleteContent");
      this.searchTextField.getActionMap().put("deleteContent", new AbstractAction() {
          public void actionPerformed(ActionEvent ev) {
            searchTextField.setText("");
          }
        });
*/

      this.chosenTextureLabel = new JLabel(activity, preferences.getLocalizedString(
          com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "chosenTextureLabel.text"));
      this.texturePreviewComponent = new ScaledImageComponent(null, true, activity) {
          protected void paintComponent(Graphics g) {
            Graphics2D g2D = (Graphics2D)g;
            Shape oldClip = g2D.getClip();
            AffineTransform oldTransform = g2D.getTransform();
						g2D.rotate(Math.toRadians(((Number)angleSpinner.getModel().getValue()).doubleValue()), getWidth() / 2, getHeight() / 2);
            super.paintComponent(g);
            g2D.setTransform(oldTransform);
            g2D.setClip(oldClip);
          }
        };
			this.texturePreviewComponent.setMinimumWidth(this.texturePreviewComponent.getPreferredSize().width);
			this.texturePreviewComponent.setMinimumHeight(this.texturePreviewComponent.getPreferredSize().height);

      try {
        String importTextureButtonText = SwingTools.getLocalizedLabelText(
            preferences, com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "importTextureButton.text");
        //this.texturePreviewComponent.setBorder(SwingTools.getDropableComponentBorder());
        // Add to label a transfer handler to let user drag and drop a file on it
      /*  this.texturePreviewComponent.setTransferHandler(new TransferHandler() {
            @Override
            public boolean canImport(JComponent comp, DataFlavor [] flavors) {
              return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
            }
            
            @Override
            public boolean importData(JComponent comp, Transferable transferedFiles) {
              try {
                List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
                final String textureName = files.get(0).getAbsolutePath();
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      controller.importTexture(textureName);
                    }
                  });
                return true;
              } catch (UnsupportedFlavorException ex) {
                return false;
              } catch (IOException ex) {
                return false;
              }
            }
          });*/

        this.angleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.TextureChoiceComponent.class,
            "angleLabel.text"));
				final NullableSpinnerNumberModel.NullableSpinnerModuloNumberModel angleSpinnerModel = new NullableSpinnerNumberModel.NullableSpinnerModuloNumberModel(
								0, 0, 360, 15);
				//this.angleSpinner = new NullableSpinnerJogDial(activity, angleSpinnerModel);
				this.angleSpinner = new AutoCommitSpinner(activity, angleSpinnerModel,true);
				angleSpinnerModel.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent ev) {
						texturePreviewComponent.postInvalidate();
					}
				});

				this.scaleLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences, com.eteks.sweethome3d.android_props.TextureChoiceComponent.class,
								"scaleLabel.text"));
				final SpinnerNumberModel scaleSpinnerModel = new SpinnerNumberModel(new Float(100f), new Float(1f), new Float(10000f), new Float(5f));
				this.scaleSpinner = new AutoCommitSpinner(activity, scaleSpinnerModel,true);
				scaleSpinnerModel.addChangeListener(new ChangeListener() {
					public void stateChanged(ChangeEvent ev) {
						texturePreviewComponent.postInvalidate();
					}
				});

        this.importTextureButton = new JButton(activity, importTextureButtonText);
        this.importTextureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
              controller.importTexture();
            }
          });
        this.modifyTextureButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "modifyTextureButton.text"));
        this.modifyTextureButton.setEnabled(false);
        this.modifyTextureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
							if(previewTexture != null && previewTexture instanceof CatalogTexture)
							{
								controller.modifyTexture((CatalogTexture) previewTexture);
							}
							//PJ this can be broken if search is touched
							//int c = availableTexturesList.getCheckedItemPosition();
							//CatalogTexture ct = (CatalogTexture)availableTexturesList.getItemAtPosition(c);
							//controller.modifyTexture(ct);
              //controller.modifyTexture((CatalogTexture)availableTexturesList.getSelectedItem());
            }
          });
        this.deleteTextureButton = new JButton(activity, SwingTools.getLocalizedLabelText(preferences,
            com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "deleteTextureButton.text"));
        this.deleteTextureButton.setEnabled(false);
        this.deleteTextureButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
				// this will include a blocking confirm so we must get off the EDT thread
				Thread t = new Thread(){public void run(){
					if(previewTexture != null && previewTexture instanceof CatalogTexture)
					{
						controller.deleteTexture((CatalogTexture) previewTexture);
					}
					//PJ this can be broken if search is touched
					//	int c = availableTexturesList.getCheckedItemPosition();
					//	CatalogTexture ct = (CatalogTexture)availableTexturesList.getItemAtPosition(c);
					//	controller.deleteTexture(ct);
					//controller.deleteTexture((CatalogTexture)availableTexturesList.getSelectedItem());
					}};
				t.start();
            }
          });

        preferences.getTexturesCatalog().addTexturesListener(new TexturesCatalogListener(this));
      } catch (IllegalArgumentException ex) {
        // Do not support import texture if importTextureButton.text isn't defined
        //this.texturePreviewComponent.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      }
      //TODO: add back the recent textures panel
      //this.recentTexturesPanel = SwingTools.createTitledPanel(preferences.getLocalizedString(
      //    com.eteks.sweethome3d.android_props.TextureChoiceComponent.class, "recentPanel.title"));
      // Reduce border to align it on list left border
      //int reducedBorderWidth = OperatingSystem.isMacOSXLeopardOrSuperior() ? -8 : -2;
      //this.recentTexturesPanel.setBorder(BorderFactory.createCompoundBorder(
       //   BorderFactory.createEmptyBorder(0, reducedBorderWidth, 0, reducedBorderWidth), this.recentTexturesPanel.getBorder()));
      /*preferences.addPropertyChangeListener(UserPreferences.Property.RECENT_TEXTURES,
          new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
              updateRecentTextures(preferences);
            }
          });
      updateRecentTextures(preferences);
      this.recentTexturesPanel.setOpaque(false);*/

      //Border border = this.texturePreviewComponent.getBorder();
      // Update edited texture in texture panel
      HomeTexture texture = controller.getTexture();
      setPreviewTexture(texture);
      if (texture instanceof HomeTexture) {
        this.angleSpinner.setValue(new Float(Math.toDegrees(texture.getAngle())));
        this.scaleSpinner.setValue(new Float(texture.getScale() * 100f));
			}
     // Insets insets = new Insets(5,5,5,5);//border.getBorderInsets(this.texturePreviewComponent);
     // this.texturePreviewComponent.setPreferredSize(
     //     new Dimension(PREVIEW_ICON_SIZE + insets.left + insets.right, PREVIEW_ICON_SIZE + insets.top + insets.bottom));
    }

    /**
     * Renderer used to display the textures in list.
     */
    private  class TextureListCellRenderer extends ArrayAdapter<CatalogTexture> {
			private Font defaultFont;
			private Font modifiablePieceFont;

			public TextureListCellRenderer(Context context, List<CatalogTexture> catalog) {
				super(context, android.R.layout.simple_list_item_1, catalog);
				this.defaultFont = new VMFont(Typeface.DEFAULT, fontSizePx);//getFont();
				this.modifiablePieceFont = new VMFont(Typeface.defaultFromStyle(Typeface.ITALIC), fontSizePx);//new Font(this.defaultFont.getFontName(), Font.ITALIC, this.defaultFont.getSize());
			}

		 @Override
		 public android.view.View getView(int position, android.view.View convertView, ViewGroup parent) {
			 return getDropDownView(position, convertView, parent);
		 }
		@Override
		public android.view.View getDropDownView (final int position, android.view.View convertView, ViewGroup parent) {

			final CatalogTexture texture = (CatalogTexture) this.getItem(position);
			final TextureIcon ti = new TextureIcon(texture, null);
			ImageView ret = new CheckableImageView(getContext()) {
				public void onDraw(Canvas canvas) {
					Graphics2D g2D = new VMGraphics2D(canvas);
					ti.paintIcon(null, g2D, 0, 0);

					String value = texture.getName();
					value = texture.getCategory().getName() + " - " + value;
					g2D.setFont(texture.isModifiable() ? modifiablePieceFont : defaultFont);
					g2D.setColor(Color.BLACK);
					g2D.drawString(value, 0, this.getHeight() - namePadBottomPx);// at the bottom
					if (isChecked())
					{
						g2D.setColor(Color.DARK_GRAY);
						g2D.drawRect(0, 0, this.getWidth(), this.getHeight());
					}
				}
			};
			ret.setMinimumWidth(TextureIcon.sizePx);
			ret.setMinimumHeight(TextureIcon.sizePx + fontSizePx + namePadBottomPx);

			return ret;
			}
    }

    /**
     * Icon displaying a texture.
     */
    private static class TextureIcon implements Icon {
      public static int SIZE_DP = 96;
			public static int sizePx = 96;

      private TextureImage texture;
      private JComponent   component;

      public TextureIcon(TextureImage texture,
                         JComponent component) {
        this.texture = texture;
        this.component = component;
      }

      public int getIconWidth() {
        return sizePx;
      }

      public int getIconHeight() {
        return sizePx;
      }

      public void paintIcon(Object c, Graphics g, int x, int y) {
        Icon icon = IconManager.getInstance().getIcon(
            this.texture.getImage(), getIconHeight(), this.component);
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
    }

    /**
     * Catalog listener that updates textures list each time a texture
     * is deleted or added in textures catalog. This listener is bound to this component
     * with a weak reference to avoid strong link between catalog and this component.
     */
    private static class TexturesCatalogListener implements CollectionListener<CatalogTexture> {
      private WeakReference<TexturePanel> texturePanel;

      public TexturesCatalogListener(TexturePanel texturePanel) {
        this.texturePanel = new WeakReference<TexturePanel>(texturePanel);
      }

      public void collectionChanged(final CollectionEvent<CatalogTexture> ev) {
        // If controller was garbage collected, remove this listener from catalog
        final TexturePanel texturePanel = this.texturePanel.get();
        if (texturePanel == null) {
          ((TexturesCatalog)ev.getSource()).removeTexturesListener(this);
        } else {
          switch (ev.getType()) {
            case ADD:
            	//PJ we do everything here that should be in the JList and it's model!
							ArrayAdapter aa = (ArrayAdapter)texturePanel.availableTexturesList.getAdapter();
							aa.add(ev.getItem());
							aa.notifyDataSetChanged();
							int p = aa.getPosition(ev.getItem());
							texturePanel.availableTexturesList.performItemClick(
									texturePanel.availableTexturesList.getAdapter().getView(p, null, null),
									p, texturePanel.availableTexturesList.getAdapter().getItemId(p));

             //PJ not in yet: texturePanel.searchTextField.setText("");

              // Select added texture only when the list is showing on screen to reduce the delay
              // spent on fully reloading the textures list after a language change
             /* if (texturePanel.availableTexturesList.isShowing()) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      // Select value later to ensure scrolling works
                      texturePanel.availableTexturesList.setSelection(ev.getItem(), true);
                    }
                  });
              } else {
                texturePanel.availableTexturesList.clearSelection();
              }*/
              break;
            case DELETE:
            	// we've likely got here on non edt from delete button above, so to touch the gui we must get back on edt
							EventQueue.invokeLater(new Runnable(){public void run() {
								ArrayAdapter aa2 = (ArrayAdapter) texturePanel.availableTexturesList.getAdapter();
								int p = aa2.getPosition(ev.getItem());
								aa2.remove(ev.getItem());
								aa2.notifyDataSetChanged();
								// select next thing along, to ensure buttons updated etc (p will be one down after remove above)
								if( p != -1 ) {
									texturePanel.availableTexturesList.performItemClick(
											texturePanel.availableTexturesList.getAdapter().getView(p, null, null),
											p, texturePanel.availableTexturesList.getAdapter().getItemId(p));
									//texturePanel.availableTexturesList.clearSelection();
								}
							}});
              break;
          }
        }
      }
    }

    /**
     * Updates the displayed list of recent textures.
     */
  /*  public void updateRecentTextures(UserPreferences preferences) {
      this.recentTexturesPanel.removeAll();
      List<TextureImage> recentTextures = preferences.getRecentTextures();
      //Border labelBorder = BorderFactory.createLineBorder(Color.GRAY);
      for (int i = 0; i < recentTextures.size(); i++) {
        final TextureImage recentTexture = recentTextures.get(i);
        JLabel textureLabel = new JLabel(activity, "");
        textureLabel.setIcon(new TextureIcon(recentTexture, textureLabel));
        //textureLabel.setBorder(labelBorder);
        //textureLabel.setToolTipText(recentTexture.getName());
        textureLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
              setPreviewTexture(recentTexture);
              if (ev.getClickCount() == 2) {
                JRootPane rootPane = (JRootPane)SwingUtilities.getAncestorOfClass(JRootPane.class, recentTexturesPanel);
                if (rootPane != null) {
                  for (JButton button : SwingTools.findChildren(rootPane, JButton.class)) {
                    if ("OK".equals(button.getActionCommand())) {
                      button.doClick();
                    }
                  }
                }
              }
            }
          });
        this.recentTexturesPanel.add(textureLabel, new GridBagConstraints(
            i, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, 
            GridBagConstraints.NONE, new Insets(0, 0, 0, 2), 0, 0));
      }
      this.recentTexturesPanel.setVisible(!recentTextures.isEmpty());
    }*/

		/**
		 * Sets components mnemonics and label / component associations.
		 */
		private void setMnemonics(UserPreferences preferences) {
		}

    /**
     * Layouts components in panel with their labels.
     */
    private void layoutComponents() {
			// First row
			swapOut(this.availableTexturesLabel, R.id.texture_choice_availableLabel);
			// Second row
			swapOut(this.availableTexturesList, R.id.texture_choice_availableTextureList);
			// Third row
			// leftPanel.add(this.searchLabel,
			swapOut(this.searchTextField, R.id.texture_choice_searchTextField);

			// First row
			swapOut(this.chosenTextureLabel, R.id.texture_choice_chosenLabel);
			// Second row
			swapOut(this.texturePreviewComponent, R.id.texture_choice_chosenView);
			// Third row
			if (this.controller.isRotationSupported()) {
				swapOut(this.angleLabel, R.id.texture_choice_angleLabel);
				swapOut(this.angleSpinner, R.id.texture_choice_angleSpinner);
				// Fourth row
				swapOut(this.scaleLabel, R.id.texture_choice_scaleLabel);
				swapOut(this.scaleSpinner, R.id.texture_choice_scaleSpinner);
				// Fifth row
			} else {
				// remove the holding views
				// table rows can't be empty and I can't work out how to remove them
				swapOut(new TextView(activity),R.id.texture_choice_angleLabel);
				removeView(R.id.texture_choice_angleSpinner);
				removeView(R.id.texture_choice_scaleLabel);
				removeView(R.id.texture_choice_scaleSpinner);
			}
			// Sixth row
			if (this.importTextureButton != null) {
				swapOut(this.importTextureButton, R.id.texture_choice_importTextureButton);
				swapOut(this.modifyTextureButton, R.id.texture_choice_modifyTextureButton);
				swapOut(this.deleteTextureButton, R.id.texture_choice_deleteTextureButton);
			} else {
				removeView(R.id.texture_choice_importTextureButton);
				removeView(R.id.texture_choice_modifyTextureButton);
				removeView(R.id.texture_choice_deleteTextureButton);
			}


			this.setTitle(controller.getDialogTitle());
			swapOut(closeButton, R.id.texture_choice_closeButton);
    }

    /**
     * Returns the chosen texture.
     */
    private TextureImage getPreviewTexture() {
      return this.previewTexture;
    }

    /**
     * Sets the chosen texture.
     */
    private void setPreviewTexture(TextureImage previewTexture) {
      this.previewTexture = previewTexture;
      if (previewTexture != null) {
        //this.texturePreviewComponent.setToolTipText(previewTexture.getName());
        InputStream iconStream = null;
        try {
          iconStream = previewTexture.getImage().openStream();
          this.texturePreviewComponent.setImage(ImageIO.read(iconStream));
        } catch (IOException ex) {
        } finally {
          if (iconStream != null) {
            try {
              iconStream.close();
            } catch (IOException ex) {
            }
          }
        }
      } else {
        //this.texturePreviewComponent.setToolTipText(null);
        this.texturePreviewComponent.setImage(null);
      }
      // Update selection in texture list
    //  if (this.availableTexturesList.getSelectedItem() != previewTexture) {
    //     this.availableTexturesList.setSelection(-1);
    //  }
    }

    public void displayView(View textureChoiceComponent) {
      // Show panel in a resizable modal dialog
 /*     final JOptionPane optionPane = new JOptionPane(this, JOptionPane.PLAIN_MESSAGE,
          JOptionPane.OK_CANCEL_OPTION);
      JComponent parentComponent = SwingUtilities.getRootPane((JComponent)textureChoiceComponent);
      if (parentComponent != null) {
        optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
      }*/
  /*    final JDialog dialog = optionPane.createDialog(parentComponent, controller.getDialogTitle());
      dialog.applyComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
      dialog.setResizable(true);
      // Pack again because resize decorations may have changed dialog preferred size
      //dialog.pack();
      dialog.setMinimumSize(dialog.getPreferredSize());
      if (dialogPreferredSize != null
          && dialogPreferredSize.width >= dialog.getWidth()
          && dialogPreferredSize.height >= dialog.getHeight()) {
        dialog.setSize(dialogPreferredSize);
      }*/
      // Initialize search field value once list preferred size is set
     // searchTextField.setText(searchFilterText);
      // Add a listener that transfer focus to focusable field of texture panel when dialog is shown
   /*   dialog.addComponentListener(new ComponentAdapter() {
          @Override
          public void componentShown(ComponentEvent ev) {
            SwingTools.requestFocusInWindow(searchTextField);
            dialog.removeComponentListener(this);
          }
        });*/
    /*  this.availableTexturesList.addMouseListener(new MouseAdapter() {
          @Override
          public void mouseClicked(MouseEvent ev) {
            // Close on double clicks in texture list
            if (ev.getClickCount() == 2) {
              optionPane.setValue(JOptionPane.OK_OPTION);
              availableTexturesList.removeMouseListener(this);
            }
          }
        });*/

      //ToolTipManager.sharedInstance().registerComponent(this.availableTexturesList);
    /*  dialog.setVisible(true);
      dialog.dispose();
     // ToolTipManager.sharedInstance().unregisterComponent(this.availableTexturesList);
      dialogPreferredSize = dialog.getSize();
      searchFilterText = this.searchTextField.getText().toString();
      if (Integer.valueOf(JOptionPane.OK_OPTION).equals(optionPane.getValue())) {
        HomeTexture selectedTexture = getSelectedTexture();
        this.controller.setTexture(selectedTexture);
        if (selectedTexture != null) {
          this.controller.addRecentTexture(selectedTexture);
        }
      }*/
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		this.setOnDismissListener(new OnDismissListener() {
			@Override
			public void onDismiss(DialogInterface dialog)
			{
				HomeTexture selectedTexture = getSelectedTexture();
				controller.setTexture(selectedTexture);
				if (selectedTexture != null) {
					controller.addRecentTexture(selectedTexture);
				}
			}
		});
		this.show();
	}

    /**
     * Returns the texture selected by the user.
     */
    private HomeTexture getSelectedTexture() {
      TextureImage previewTexture = getPreviewTexture();
      if (previewTexture == null) {
        return null;
      } else {
        float angleInRadians = (float)Math.toRadians(((Number)this.angleSpinner.getModel().getValue()).doubleValue());
				float scale = ((Number)this.scaleSpinner.getModel().getValue()).floatValue() / 100f;
        return new HomeTexture(previewTexture, angleInRadians, scale, true);
      }
    }

    /**
     * List model adaptor to CatalogTexture instances of catalog.
     */
    private static class TexturesCatalogListModel extends JList.AbstractListModel {
      private TexturesCatalog        catalog;
      private List<CatalogTexture>   textures;
      private String                 filterText;

      public TexturesCatalogListModel(TexturesCatalog catalog) {
        this.catalog = catalog;
        this.filterText = "";
        catalog.addTexturesListener(new TexturesCatalogListener(this));
      }

      public void setFilterText(String filterText) {
        this.filterText = filterText;
        resetFurnitureList();
      }

      public Object getElementAt(int index) {
        checkFurnitureList();
        return this.textures.get(index);
      }

      public int getSize() {
        checkFurnitureList();
        return this.textures.size();
      }

      private void resetFurnitureList() {
        if (this.textures != null) {
          this.textures = null;
					//PJ added now to force an update, before the fire update refreshs the list
					checkFurnitureList();

          EventQueue.invokeLater(new Runnable() {
              public void run() {
				  fireContentsChanged(this, -1, -1);
              }
            });
        }
      }

		private void checkFurnitureList() {
        if (this.textures == null) {
          this.textures = new ArrayList<CatalogTexture>();
          this.textures.clear();
          for (TexturesCategory category : this.catalog.getCategories()) {
            for (CatalogTexture texture : category.getTextures()) {
              if (texture.matchesFilter(this.filterText)) {
                textures.add(texture);
              }
            }
          }
        }
      }

		public List<CatalogTexture> toList() {
			checkFurnitureList();
			return new ArrayList<CatalogTexture>(textures);
		}

		/**
       * Catalog textures listener bound to this list model with a weak reference to avoid
       * strong link between catalog and this list.
       */
      private static class TexturesCatalogListener implements CollectionListener<CatalogTexture> {
        private WeakReference<TexturesCatalogListModel>  listModel;

        public TexturesCatalogListener(TexturesCatalogListModel catalogListModel) {
          this.listModel = new WeakReference<TexturesCatalogListModel>(catalogListModel);
        }

        public void collectionChanged(CollectionEvent<CatalogTexture> ev) {
          // If catalog list model was garbage collected, remove this listener from catalog
          TexturesCatalogListModel listModel = this.listModel.get();
          TexturesCatalog catalog = (TexturesCatalog)ev.getSource();
          if (listModel == null) {
            catalog.removeTexturesListener(this);
          } else {
            listModel.resetFurnitureList();
          }
        }
      }
    }
  }
}