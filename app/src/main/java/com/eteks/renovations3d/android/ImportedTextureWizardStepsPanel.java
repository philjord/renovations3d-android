/*
 * ImportedTextureWizardStepsPanel.java 01 oct. 2008
 *
 * Sweet Home 3D, Copyright (c) 2008 Emmanuel PUYBARET / eTeks <info@eteks.com>
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
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;


import com.eteks.renovations3d.ImageAcquireManager;
import com.eteks.renovations3d.Renovations3DActivity;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JComboBox;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JPanel;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.JTextField;
import com.eteks.sweethome3d.model.CatalogTexture;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.TexturesCategory;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;
import com.eteks.sweethome3d.tools.TemporaryURLContent;
import com.eteks.sweethome3d.viewcontroller.ContentManager;
import com.eteks.sweethome3d.viewcontroller.ImportedTextureWizardController;

import com.mindblowing.renovations3d.R;

import javaawt.Color;
import javaawt.Dimension;
import javaawt.EventQueue;
import javaawt.Graphics;
import javaawt.Graphics2D;
import javaawt.TexturePaint;
import javaawt.geom.Rectangle2D;
import javaawt.image.BufferedImage;
import javaawt.imageio.ImageIO;

/**
 * Wizard panel for background image choice. 
 * @author Emmanuel Puybaret
 */
public class ImportedTextureWizardStepsPanel extends JPanel implements com.eteks.sweethome3d.viewcontroller.View {
	//PJPJ increased these sizes a bit, because reduction always happens now, as mobiles have tight limtis
  private static final int LARGE_IMAGE_PIXEL_COUNT_THRESHOLD = 1280 * 1280;
  private static final int IMAGE_PREFERRED_MAX_SIZE = 1024;
  private static final int LARGE_IMAGE_MAX_PIXEL_COUNT = IMAGE_PREFERRED_MAX_SIZE * IMAGE_PREFERRED_MAX_SIZE;
  
  private static final int IMAGE_PREFERRED_SIZE_DP = 200;//Math.round(150 * SwingTools.getResolutionScale());
  private static int IMAGE_PREFERRED_SIZE_PX = 200;

  private final ImportedTextureWizardController controller;
  private JLabel imageChoiceOrChangeLabel;
  private JButton imageChoiceOrChangeButtonFile;
  private JButton imageChoiceOrChangeButtonCamera;

  //private JButton                         findImagesButton;
  private JLabel                          imageChoiceErrorLabel;
  private ScaledImageComponent            imageChoicePreviewComponent;
  private JLabel                          attributesLabel;
  private JLabel                          nameLabel;
  private JTextField nameTextField;
  private JLabel                          categoryLabel;
  private JComboBox categoryComboBox;
  private JLabel                          widthLabel;
  private JSpinner widthSpinner;
  private JLabel                          heightLabel;
  private JSpinner                        heightSpinner;
  private ScaledImageComponent            attributesPreviewComponent;
  private Executor                        imageLoader;
  private static BufferedImage waitImage;


	private LinearLayout imageChoiceTopPanel;
	private LinearLayout attributesPanel;

  /**
   * Creates a view for texture image choice and attributes. 
   */
  public ImportedTextureWizardStepsPanel(CatalogTexture catalogTexture, 
                                         String textureName, 
                                         UserPreferences preferences, 
                                         final ImportedTextureWizardController controller,
										 Activity activity)
  {
	  super(activity, R.layout.jpanel_import_texture_wizard);

	  Renovations3DActivity.logFireBaseContent("ImportedTextureWizardStepsPanel started");

	  final float scale = getResources().getDisplayMetrics().density;
	  IMAGE_PREFERRED_SIZE_PX = (int) (IMAGE_PREFERRED_SIZE_DP * scale + 0.5f);
    this.controller = controller;
    this.imageLoader = Executors.newSingleThreadExecutor();
    createComponents(preferences, controller);
    layoutComponents();
    updateController(catalogTexture, preferences);
    if (textureName != null) {
      updateController(textureName, controller.getContentManager(), preferences, true);
    }
    else
	{
		String pendingImageName = ((Renovations3DActivity) activity).getImageAcquireManager().requestPendingChosenImageFile(ImageAcquireManager.Destination.IMPORT_TEXTURE);
		if (pendingImageName != null)
		{
			try
			{
				updatePreviewComponentsWithWaitImage(preferences);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			updateController(pendingImageName, controller.getContentManager(), preferences, true);
		}
	}
    
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.STEP, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent evt) {
            updateStep(controller);
          }
        });
  }

  /**
   * Creates components displayed by this panel.
   */
  private void createComponents(final UserPreferences preferences, 
                                final ImportedTextureWizardController controller) {
    // Get unit name matching current unit 
    String unitName = preferences.getLengthUnit().getName();

    // Image choice panel components
    this.imageChoiceOrChangeLabel = new JLabel(activity, "");
    this.imageChoiceOrChangeButtonFile = new JButton(activity, "");
    this.imageChoiceOrChangeButtonFile.addActionListener(new ActionListener()
	{
		public void actionPerformed(ActionEvent ev)
		{
				/*String imageName = showImageChoiceDialog(preferences, controller.getContentManager());
				if (imageName != null)
				{
					updateController(imageName, controller.getContentManager(), preferences, false);
				}*/
			try
			{
				updatePreviewComponentsWithWaitImage(preferences);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			//PJ replaced with intent system
			((Renovations3DActivity)activity).getImageAcquireManager().pickImage(
					new ImageAcquireManager.ImageReceiver()
					{
						@Override
						public void receivedImage(String imageName)
						{
							updateController(imageName, controller.getContentManager(), preferences, false);
						}
					}
			, ImageAcquireManager.Destination.IMPORT_TEXTURE);
		}
      });
	  this.imageChoiceOrChangeButtonCamera = new JButton(activity, "");
	  this.imageChoiceOrChangeButtonCamera.addActionListener(new ActionListener() {
		  public void actionPerformed(ActionEvent ev) {
			  /*String imageName = showImageChoiceDialog(preferences, controller.getContentManager());
			  if (imageName != null) {
				  updateController(imageName, controller.getContentManager(), preferences, false);
			  }*/
			  try
			  {
				  updatePreviewComponentsWithWaitImage(preferences);
			  }
			  catch (IOException e)
			  {
				  e.printStackTrace();
			  }
			  //PJ replaced with intent system
			  ((Renovations3DActivity)activity).getImageAcquireManager().takeImage(
					  new ImageAcquireManager.ImageReceiver()
					  {
						  @Override
						  public void receivedImage(String imageName)
						  {
							  updateController(imageName, controller.getContentManager(), preferences, false);
						  }
					  }
			  , ImageAcquireManager.Destination.IMPORT_TEXTURE);
		  }
	  });
   /* try {
      this.findImagesButton = new JButton(SwingTools.getLocalizedLabelText(preferences, 
          ImportedTextureWizardStepsPanel.class, "findImagesButton.text"));
      final String findImagesUrl = preferences.getLocalizedString(
          ImportedTextureWizardStepsPanel.class, "findImagesButton.url");
      this.findImagesButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ev) {
            boolean documentShown = false;
            try { 
              // Display Find models page in browser
              documentShown = SwingTools.showDocumentInBrowser(new URL(findImagesUrl)); 
            } catch (MalformedURLException ex) {
              // Document isn't shown
            }
            if (!documentShown) {
              // If the document wasn't shown, display a message 
              // with a copiable URL in a message box 
              JTextArea findImagesMessageTextArea = new JTextArea(preferences.getLocalizedString(
                  ImportedTextureWizardStepsPanel.class, "findImagesMessage.text"));
              String findImagesTitle = preferences.getLocalizedString(
                  ImportedTextureWizardStepsPanel.class, "findImagesMessage.title");
              findImagesMessageTextArea.setEditable(false);
              findImagesMessageTextArea.setOpaque(false);
              JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedTextureWizardStepsPanel.this),
                  findImagesMessageTextArea, findImagesTitle, 
                  JOptionPane.INFORMATION_MESSAGE);
            }
          }
        });
    } catch (IllegalArgumentException ex) {
      // Don't create findImagesButton if its text or url isn't defined
    }*/
    this.imageChoiceErrorLabel = new JLabel(activity, preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "imageChoiceErrorLabel.text"));
    // Make imageChoiceErrorLabel visible only if an error occurred during image content loading
    this.imageChoiceErrorLabel.setVisibility(android.view.View.GONE);
    this.imageChoicePreviewComponent = new ScaledImageComponent(activity);
    // Add a transfer handler to image preview component to let user drag and drop an image in component
  /*  this.imageChoicePreviewComponent.setTransferHandler(new TransferHandler() {
        @Override
        public boolean canImport(JComponent comp, DataFlavor [] flavors) {
          return Arrays.asList(flavors).contains(DataFlavor.javaFileListFlavor);
        }
        
        @Override
        public boolean importData(JComponent comp, Transferable transferedFiles) {
          boolean success = false;
          try {
            List<File> files = (List<File>)transferedFiles.getTransferData(DataFlavor.javaFileListFlavor);
            for (File file : files) {
              final String textureName = file.getAbsolutePath();
              // Try to import the first file that would be accepted by content manager
              if (controller.getContentManager().isAcceptable(textureName, ContentManager.ContentType.IMAGE)) {
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                      updateController(textureName, controller.getContentManager(), preferences, false);
                    }
                  });
                success = true;
                break;
              }
            }
          } catch (UnsupportedFlavorException ex) {
            // No success
          } catch (IOException ex) {
            // No success
          }
          if (!success) {
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  JOptionPane.showMessageDialog(SwingUtilities.getRootPane(ImportedTextureWizardStepsPanel.this), 
                      preferences.getLocalizedString(ImportedTextureWizardStepsPanel.class, "imageChoiceErrorLabel.text"));
                }
              });
          }
          return success;
        }
      });
    this.imageChoicePreviewComponent.setBorder(SwingTools.getDropableComponentBorder());
    */
    // Attributes panel components
    this.attributesLabel = new JLabel(activity, Html.fromHtml(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "attributesLabel.text").replace("<br>", " ")));
    this.nameLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "nameLabel.text"));
    this.nameTextField = new JTextField(activity, "");
	  nameTextField.addTextChangedListener(new TextWatcher(){
		  public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {}
		  public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {}
		  public void afterTextChanged(Editable arg0) {
			  nameTextField.removeTextChangedListener(this);
			  controller.setName(nameTextField.getText().toString().trim());
			  nameTextField.addTextChangedListener(this);
		  }
	  });
   /* DocumentListener nameListener = new DocumentListener() {
        public void changedUpdate(DocumentEvent ev) {
          nameTextField.getDocument().removeDocumentListener(this);
          controller.setName(nameTextField.getText().trim());
          nameTextField.getDocument().addDocumentListener(this);
        }
  
        public void insertUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
  
        public void removeUpdate(DocumentEvent ev) {
          changedUpdate(ev);
        }
      };
    this.nameTextField.getDocument().addDocumentListener(nameListener);*/
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.NAME,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If name changes update name text field
            if (!nameTextField.getText().toString().trim().equals(controller.getName())) {
              nameTextField.setText(controller.getName());
            }
          }
        });

    this.categoryLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "categoryLabel.text"));
	  ArrayList<TexturesCategory> cats = new ArrayList<TexturesCategory>(preferences.getTexturesCatalog().getCategories());
    this.categoryComboBox = new JComboBox(activity, cats);
	  categoryComboBox.setAdapter(new ArrayAdapter<TexturesCategory>(activity, android.R.layout.simple_list_item_1, cats)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView (int position, View convertView, ViewGroup parent)
		  {
			  TextView ret = new TextView(getContext());
			  ret.setText(((TexturesCategory)categoryComboBox.getItemAtPosition(position)).getName());
			  return ret;
		  }
	  });
   //TODO: android spinners certianly can't be randomly edited
	  // this.categoryComboBox.setEditable(true);
   /* final ComboBoxEditor defaultEditor = this.categoryComboBox.getEditor();
    // Change editor to edit category name
    this.categoryComboBox.setEditor(new ComboBoxEditor() {
        public Object getItem() {
          String name = (String)defaultEditor.getItem();
          name = name.trim();
          // If category is empty, replace it by the last selected item
          if (name.length() == 0) {
            Object selectedItem = categoryComboBox.getSelectedItem();
            setItem(selectedItem);
            return selectedItem;
          } else {
            TexturesCategory category = new TexturesCategory(name);
            // Search an existing category
            List<TexturesCategory> categories = preferences.getTexturesCatalog().getCategories();
            int categoryIndex = Collections.binarySearch(categories, category);
            if (categoryIndex >= 0) {
              return categories.get(categoryIndex);
            }
            // If no existing category was found, return a new one          
            return category;
          }
        }
      
        public void setItem(Object value) {
          if (value != null) {
            TexturesCategory category = (TexturesCategory)value;          
            defaultEditor.setItem(category.getName());
          } 
        }

        public void addActionListener(ActionListener l) {
          defaultEditor.addActionListener(l);
        }

        public Component getEditorComponent() {
          return defaultEditor.getEditorComponent();
        }

        public void removeActionListener(ActionListener l) {
          defaultEditor.removeActionListener(l);
        }

        public void selectAll() {
          defaultEditor.selectAll();
        }
      });
    this.categoryComboBox.setRenderer(new DefaultListCellRenderer() {
        public Component getListCellRendererComponent(JList list, Object value, int index,
													  boolean isSelected, boolean cellHasFocus) {
          TexturesCategory category = (TexturesCategory)value;
          return super.getListCellRendererComponent(list, category.getName(), index, isSelected, cellHasFocus);
        }
      });*/
    this.categoryComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setCategory((TexturesCategory)categoryComboBox.getItemAtPosition(categoryComboBox.getSelectedItemPosition()));
        }
      });
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.CATEGORY,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If category changes update category combo box
            TexturesCategory category = controller.getCategory();
            if (category != null) {
				// on desktop if the jcombobox is free form edited this call will add it (e.g. USer catagory
				// however on Android spinners no edit, no select until adapter updated
				ArrayAdapter aa = ((ArrayAdapter)categoryComboBox.getAdapter());
				if(aa.getPosition(category) == -1)
				{
					aa.add(category);
					aa.notifyDataSetChanged();
				}
              categoryComboBox.setSelectedItem(category);
            }
          }
        });

    this.widthLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "widthLabel.text", unitName));
    float minimumLength = preferences.getLengthUnit().getMinimumLength();
    float maximumLength = preferences.getLengthUnit().getMaximumLength();
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel widthSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
    this.widthSpinner = new NullableSpinner(activity, widthSpinnerModel, true);
    widthSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          widthSpinnerModel.removeChangeListener(this);
          // If width spinner value changes update controller
          controller.setWidth(widthSpinnerModel.getLength());
          widthSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.WIDTH,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If width changes update width spinner
            widthSpinnerModel.setLength(controller.getWidth());
          }
        });
    
    this.heightLabel = new JLabel(activity, SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "heightLabel.text", unitName));
    final NullableSpinnerNumberModel.NullableSpinnerLengthModel heightSpinnerModel =
        new NullableSpinnerNumberModel.NullableSpinnerLengthModel(preferences, minimumLength, maximumLength);
    this.heightSpinner = new NullableSpinner(activity, heightSpinnerModel, true);
    heightSpinnerModel.addChangeListener(new ChangeListener () {
        public void stateChanged(ChangeEvent ev) {
          heightSpinnerModel.removeChangeListener(this);
          // If width spinner value changes update controller
          controller.setHeight(heightSpinnerModel.getLength());
          heightSpinnerModel.addChangeListener(this);
        }
      });
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.HEIGHT,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            // If height changes update height spinner
            heightSpinnerModel.setLength(controller.getHeight());
          }
        });
    
    this.attributesPreviewComponent = new ScaledImageComponent(activity);

    PropertyChangeListener imageAttributesListener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent ev) {
          updateAttributesPreviewImage();
        }
      };
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.IMAGE, imageAttributesListener);
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.WIDTH, imageAttributesListener);
    controller.addPropertyChangeListener(ImportedTextureWizardController.Property.HEIGHT, imageAttributesListener);
  }

  
  /**
   * Layouts components in 3 panels added to this panel as cards. 
   */
  private void layoutComponents() {
    
	this.imageChoiceTopPanel = (LinearLayout) inflatedView.findViewById(R.id.itw_imageChoiceTopPanel);
      swapOut(this.imageChoiceOrChangeLabel, R.id.itw_imageChoiceOrChangeLabel);
	  swapOut(this.imageChoiceOrChangeButtonFile, R.id.itw_imageFromFileButton);
	  swapOut(this.imageChoiceOrChangeButtonCamera, R.id.itw_imageFromCameraButton);

	  //swapOut(this.imageChoiceErrorLabel, ???
	  swapOut(this.imageChoicePreviewComponent,R.id.itw_imageChoicePreviewComponent);

	  this.attributesPanel = (LinearLayout) inflatedView.findViewById(R.id.itw_attributesPanel);
	  swapOut(this.attributesLabel, R.id.itw_attributesLabel);
	  swapOut(this.attributesPreviewComponent, R.id.itw_attributesPreviewComponent);
	  swapOut(this.nameLabel, R.id.itw_nameLabel);
	  swapOut(this.nameTextField, R.id.itw_nameTextField);
	  swapOut(this.categoryLabel, R.id.itw_categoryLabel);
	  swapOut(this.categoryComboBox, R.id.itw_categoryComboBox);
	  swapOut(this.widthLabel, R.id.itw_widthLabel);
	  swapOut(this.widthSpinner, R.id.itw_widthSpinner);
	  swapOut(this.heightLabel, R.id.itw_heightLabel);
	  swapOut(this.heightSpinner, R.id.itw_heightSpinner);

   // add(imageChoicePanel, ImportedTextureWizardController.Step.IMAGE.name());
   // add(attributesPanel, ImportedTextureWizardController.Step.ATTRIBUTES.name());
  }
  
  /**
   * Switches to the component card matching current step.   
   */
  public void updateStep(ImportedTextureWizardController controller) {
    ImportedTextureWizardController.Step step = controller.getStep();
	  this.imageChoiceTopPanel.setVisibility(android.view.View.GONE);
	  this.attributesPanel.setVisibility(android.view.View.GONE);
	  switch (step) {
      case IMAGE:
		  imageChoiceTopPanel.setVisibility(android.view.View.VISIBLE);
        break;
      case ATTRIBUTES:
		  attributesPanel.setVisibility(android.view.View.VISIBLE);
        break;
    }
  }

  /**
   * Updates controller initial values from <code>textureImage</code>. 
   */
  private void updateController(final CatalogTexture catalogTexture,
                                final UserPreferences preferences) {
    if (catalogTexture == null) {
      setImageChoiceTexts(preferences);
      updatePreviewComponentsImage(null);
    } else {
      setImageChangeTexts(preferences);
      // Read image in imageLoader executor
      this.imageLoader.execute(new Runnable() {
          public void run() {
            BufferedImage image = null;
            try {
              image = readImage(catalogTexture.getImage(), preferences);
            } catch (IOException ex) {
              // image is null
            }
            final BufferedImage readImage = image;
            // Update components in dispatch thread
            EventQueue.invokeLater(new Runnable() {
                public void run() {
                  if (readImage != null) {
                    controller.setImage(catalogTexture.getImage());
                    controller.setName(catalogTexture.getName());
                    controller.setCategory(catalogTexture.getCategory());
                    controller.setWidth(catalogTexture.getWidth());
                    controller.setHeight(catalogTexture.getHeight());
                  } else {
                    controller.setImage(null);
                    setImageChoiceTexts(preferences);
                    imageChoiceErrorLabel.setVisibility(android.view.View.GONE);
                  }
                } 
              });
            
          } 
        });
    }
  }

  /**
   * Reads image from <code>imageName</code> and updates controller values.
   */
  private void updateController(final String imageName,
                                final ContentManager contentManager,
                                final UserPreferences preferences,
                                final boolean ignoreException) {
    // Read image in imageLoader executor
    this.imageLoader.execute(new Runnable() {
        public void run() {
          Content imageContent = null;
          try {
            // Copy image to a temporary content to keep a safe access to it until home is saved
            imageContent = TemporaryURLContent.copyToTemporaryURLContent(
                contentManager.getContent(imageName));
          } catch (RecorderException ex) {
            // Error message displayed below 
          } catch (IOException ex) {
            // Error message displayed below 
          }
          if (imageContent == null) {
            if (!ignoreException) {
              EventQueue.invokeLater(new Runnable() {
                  public void run() {
                    JOptionPane.showMessageDialog(activity,
                        preferences.getLocalizedString(
								com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "imageChoiceError", imageName), "", JOptionPane.ERROR_MESSAGE);
                  }
                });
            }
            return;
          }

          BufferedImage image = null;
          try {
            // Check image is less than 10 million pixels
            Dimension size = SwingTools.getImageSizeInPixels(imageContent);
            if (size.width * (long)size.height > LARGE_IMAGE_PIXEL_COUNT_THRESHOLD) {
              imageContent = readAndReduceImage(imageContent, size, preferences);
              if (imageContent == null) {
                return;
              }
            }
            image = readImage(imageContent, preferences);
          } catch (IOException ex) {
            // image is null
          }
          
          final BufferedImage readImage = image;
          final Content       readContent = imageContent;
          // Update components in dispatch thread
          EventQueue.invokeLater(new Runnable() {
              public void run() {
                if (readImage != null) {
                  controller.setImage(readContent);
                  setImageChangeTexts(preferences);
                  imageChoiceErrorLabel.setVisibility(android.view.View.GONE);
                  // Initialize attributes with default values
                  controller.setName(contentManager.getPresentationName(imageName,
                      ContentManager.ContentType.IMAGE));
                  // Use user category as default category and create it if it doesn't exist
                  TexturesCategory userCategory = new TexturesCategory(preferences.getLocalizedString(
						  com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "userCategory"));
                  for (TexturesCategory category : preferences.getTexturesCatalog().getCategories()) {
                    if (category.equals(userCategory)) {
                      userCategory = category;
                      break;
                    }
                  }
                  controller.setCategory(userCategory);
                  float defaultWidth = 20;
                  LengthUnit lengthUnit = preferences.getLengthUnit();
                  if (lengthUnit == LengthUnit.INCH
                      || lengthUnit == LengthUnit.INCH_DECIMALS) {
                    defaultWidth = LengthUnit.inchToCentimeter(8);
                  }
                  controller.setWidth(defaultWidth);
                  controller.setHeight(defaultWidth / readImage.getWidth() * readImage.getHeight());
                } else {//if (isShowing()) {
                  controller.setImage(null);
                  setImageChoiceTexts(preferences);
                  JOptionPane.showMessageDialog(activity,
                      preferences.getLocalizedString(
							  com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "imageChoiceFormatError"), "", JOptionPane.ERROR_MESSAGE);
                }
              }
            });
        }
      });
  }

  /**
   * Informs the user that the image size is larger and returns a reduced size image if he confirms 
   * that the size should be reduced.
   * Caution : this method must be thread safe because it's called from image loader executor. 
   */
  private Content readAndReduceImage(Content imageContent, 
                                     final Dimension imageSize, 
                                     final UserPreferences preferences) throws IOException {
   // try {
      float factor;
      float ratio = (float)imageSize.width / imageSize.height;
      if (ratio < .5f || ratio > 2.) {
        factor = (float)Math.sqrt((float)LARGE_IMAGE_MAX_PIXEL_COUNT / (imageSize.width * (long)imageSize.height));
      } else if (ratio < 1f) {
        factor = (float)IMAGE_PREFERRED_MAX_SIZE / imageSize.height;
      } else {
        factor = (float)IMAGE_PREFERRED_MAX_SIZE / imageSize.width;
      }
      final int reducedWidth = Math.round(imageSize.width * factor);
      final int reducedHeight = Math.round(imageSize.height * factor);
      final AtomicInteger result = new AtomicInteger(JOptionPane.CANCEL_OPTION);
		// ok giant texture are bad news, lets not just say ok to them let's reduce them
		// in fact on a little device they are always better to be reasonable size, so don't even ask!
	  // there is a GL_MAX_TEXTURE_SIZE figure which can be 4k or less and camera always return big images these days
		result.set(JOptionPane.YES_OPTION);
      /*EventQueue.invokeAndWait(new Runnable() {
          public void run() {
            String title = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "reduceImageSize.title");
            String confirmMessage = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class,
                "reduceImageSize.message", imageSize.width, imageSize.height, reducedWidth, reducedHeight);
            String reduceSize = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "reduceImageSize.reduceSize");
            String keepUnchanged = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "reduceImageSize.keepUnchanged");
            String cancel = preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "reduceImageSize.cancel");


			 result.set(JOptionPane.showOptionDialog(activity,
                  confirmMessage, title, JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                 null, new Object [] {reduceSize, keepUnchanged, cancel}, keepUnchanged));

          }
        });*/
      if (result.get() == JOptionPane.CANCEL_OPTION) {
        return null;
      } else if (result.get() == JOptionPane.YES_OPTION) {
        updatePreviewComponentsWithWaitImage(preferences);
        
        InputStream contentStream = imageContent.openStream();
        BufferedImage image = ImageIO.read(contentStream);
        contentStream.close();
        if (image != null) {
          BufferedImage reducedImage = new BufferedImage(reducedWidth, reducedHeight, image.getType());
          //Graphics2D g2D = (Graphics2D)reducedImage.getGraphics();
          //g2D.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
          //g2D.drawImage(image, AffineTransform.getScaleInstance(factor, factor), null);
          //g2D.dispose();
			 //PJ AffineTransform version doesn't work
			Graphics g = reducedImage.getGraphics();
			g.drawImage(image.getScaledInstance(reducedWidth, reducedHeight, 0), 0, 0, null);
			g.dispose();
          
          File file = OperatingSystem.createTemporaryFile("texture", ".tmp");
          ImageIO.write(reducedImage, image.getTransparency() == BufferedImage.OPAQUE ? "JPEG" : "PNG", file);
          return new TemporaryURLContent(file.toURI().toURL());
        }
      }
      return imageContent;
  //  } catch (InterruptedException ex) {
  //    return imageContent;
  //  } catch (InvocationTargetException ex) {
  //    ex.printStackTrace();
  //    return imageContent;
  //  } catch (IOException ex) {
   //   updatePreviewComponentsImage(null);
  //    throw ex;
  //  }
  }

  /**
   * Reads image from <code>imageContent</code>. 
   * Caution : this method must be thread safe because it's called from image loader executor. 
   */
  private BufferedImage readImage(Content imageContent,
                                  UserPreferences preferences) throws IOException {
    try {
      updatePreviewComponentsWithWaitImage(preferences);
      
      // Read the image content
      InputStream contentStream = imageContent.openStream();
      BufferedImage image = ImageIO.read(contentStream);
      contentStream.close();

      if (image != null) {
        updatePreviewComponentsImage(image);
        return image;
      } else {
        throw new IOException();
      }
    } catch (IOException ex) {
      updatePreviewComponentsImage(null);
      throw ex;
    } 
  }
  
  private void updatePreviewComponentsWithWaitImage(UserPreferences preferences) throws IOException {
    // Display a waiting image while loading
    if (waitImage == null) {
      waitImage = ImageIO.read(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class.getResource(
          preferences.getLocalizedString(com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "waitIcon")));
    }
    updatePreviewComponentsImage(waitImage);
  }
  
  /**
   * Updates the <code>image</code> displayed by preview components.  
   */
  private void updatePreviewComponentsImage(final BufferedImage image) {
    if (EventQueue.isDispatchThread()) {
      this.imageChoicePreviewComponent.setImage(image);
      this.attributesPreviewComponent.setImage(image);
    } else {
      EventQueue.invokeLater(new Runnable() {
          public void run() {
            updatePreviewComponentsImage(image);
          }
        });
    }
  }

  /**
   * Sets the texts of label and button of image choice panel with
   * change texts. 
   */
  private void setImageChangeTexts(UserPreferences preferences) {
    this.imageChoiceOrChangeLabel.setText(Html.fromHtml(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "imageChangeLabel.text").replace("<br>", " ")));
	  this.imageChoiceOrChangeButtonFile.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChangeButton.text") + ": " + activity.getString(R.string.get_image_file));
	  this.imageChoiceOrChangeButtonCamera.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChangeButton.text") + ": " + activity.getString(R.string.get_image_camera));

  }

  /**
   * Sets the texts of label and button of image choice panel with
   * choice texts. 
   */
  private void setImageChoiceTexts(UserPreferences preferences) {
    this.imageChoiceOrChangeLabel.setText(Html.fromHtml(preferences.getLocalizedString(
			com.eteks.sweethome3d.android_props.ImportedTextureWizardStepsPanel.class, "imageChoiceLabel.text").replace("<br>", " ")));
	  this.imageChoiceOrChangeButtonFile.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceButton.text") + ": " + activity.getString(R.string.get_image_file));
	  this.imageChoiceOrChangeButtonCamera.setText(SwingTools.getLocalizedLabelText(preferences,
			  com.eteks.sweethome3d.android_props.BackgroundImageWizardStepsPanel.class, "imageChoiceButton.text") + ": " + activity.getString(R.string.get_image_camera));

  }
  
  /**
   * Returns an image name chosen for a content chooser dialog.
   */
  //PJPJ swaped for a call back system above
/*	 private String showImageChoiceDialog(UserPreferences preferences,
                                       ContentManager contentManager) {
    return contentManager.showOpenDialog(this, 
        preferences.getLocalizedString(ImportedTextureWizardStepsPanel.class, "imageChoiceDialog.title"), 
        ContentManager.ContentType.IMAGE);
  }*/

  /**
   * Updates the image shown in attributes panel.
   */
  private void updateAttributesPreviewImage() {
    BufferedImage attributesPreviewImage = this.attributesPreviewComponent.getImage();
    if (attributesPreviewImage == null
        || attributesPreviewImage == this.imageChoicePreviewComponent.getImage()) {
      attributesPreviewImage = new BufferedImage(IMAGE_PREFERRED_SIZE_PX, IMAGE_PREFERRED_SIZE_PX, imageChoicePreviewComponent.getImage().getType());
      this.attributesPreviewComponent.setImage(attributesPreviewImage);
    }
    // Fill image with a white background
    Graphics2D g2D = (Graphics2D)attributesPreviewImage.getGraphics();
    g2D.setPaint(Color.WHITE);
    g2D.fillRect(0, 0, IMAGE_PREFERRED_SIZE_PX, IMAGE_PREFERRED_SIZE_PX);
    BufferedImage textureImage = this.imageChoicePreviewComponent.getImage();
    if (textureImage != null) {
      // Draw the texture image as if it will be shown on a 250 x 250 cm wall
      g2D.setPaint(new TexturePaint(textureImage,
          new Rectangle2D.Float(0, 0,
              this.controller.getWidth() / 250 * IMAGE_PREFERRED_SIZE_PX,
              this.controller.getHeight() / 250 * IMAGE_PREFERRED_SIZE_PX)));
      g2D.fillRect(0, 0, IMAGE_PREFERRED_SIZE_PX, IMAGE_PREFERRED_SIZE_PX);
    }
    g2D.dispose();
    //this.attributesPreviewComponent.repaint();
	  postInvalidate();
  }
}
