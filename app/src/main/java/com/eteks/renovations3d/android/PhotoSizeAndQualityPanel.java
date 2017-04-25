/*
 * PhotoSizeAndQualityPanel.java 25 nov 2012
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
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;


import com.eteks.renovations3d.android.swingish.ChangeListener;
import com.eteks.renovations3d.android.swingish.DefaultComboBoxModel;
import com.eteks.renovations3d.android.swingish.ItemListener;
import com.eteks.renovations3d.android.swingish.JCheckBox;
import com.eteks.renovations3d.android.swingish.JComboBox;
import com.eteks.renovations3d.android.swingish.JLabel;
import com.eteks.renovations3d.android.swingish.JSlider;
import com.eteks.renovations3d.android.swingish.JSpinner;
import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.renovations3d.j3d.Component3DManager;
import com.eteks.sweethome3d.model.AspectRatio;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.UserPreferences;

import com.eteks.sweethome3d.viewcontroller.AbstractPhotoController;
import com.mindblowing.renovations3d.R;


/**
 * A panel to edit photo size and quality. 
 * @author Emmanuel Puybaret
 */
public class PhotoSizeAndQualityPanel extends LinearLayout implements com.eteks.sweethome3d.viewcontroller.View
{
  private JLabel                        widthLabel;
  private JSpinner widthSpinner;
  private JLabel                        heightLabel;
  private JSpinner heightSpinner;
  private JCheckBox                     applyProportionsCheckBox;
  private JComboBox                     aspectRatioComboBox;
  private JLabel                        qualityLabel;
  private JSlider                       qualitySlider;
  private JLabel                        fastQualityLabel;
  private JLabel                        bestQualityLabel;

	private final UserPreferences preferences;
	private Activity activity;

	protected ViewGroup rootView;

  public PhotoSizeAndQualityPanel(Home home, 
                    UserPreferences preferences, 
                    AbstractPhotoController controller,
								  final Activity activity) {
	  super(activity);
	  this.activity = activity;
	  this.preferences = preferences;

	  rootView = (ViewGroup)activity.getLayoutInflater().inflate(R.layout.photosizeandqualityview, null);
	  this.addView(rootView);

    createComponents(home, preferences, controller);
    layoutComponents();

    preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, new LanguageChangeListener(this));
  }
  
  /**
   * Creates and initializes components.
   */
  private void createComponents(final Home home, 
                                final UserPreferences preferences,
                                final AbstractPhotoController controller) {
    // Create width label and spinner bound to WIDTH controller property
    this.widthLabel = new JLabel(activity, "");
    final SpinnerNumberModel widthSpinnerModel = new SpinnerNumberModel(480, 10, 10000, 10);
    this.widthSpinner = new AutoCommitSpinner(activity, widthSpinnerModel);
	  if(controller.getWidth() < 2048 )
	  	widthSpinnerModel.setValue(controller.getWidth());
	  else
		  widthSpinnerModel.setValue(480);
    widthSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setWidth(((Number)widthSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(AbstractPhotoController.Property.WIDTH, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            widthSpinnerModel.setValue(controller.getWidth());
          }
        });

    
    // Create height label and spinner bound to HEIGHT controller property
    this.heightLabel = new JLabel(activity, "");
    final SpinnerNumberModel heightSpinnerModel = new SpinnerNumberModel(480, 10, 10000, 10);
    this.heightSpinner = new AutoCommitSpinner(activity, heightSpinnerModel);
	  //PJ controller width sometimes comes back with over large value
	  if(controller.getHeight() < 2048 )
    	heightSpinnerModel.setValue(controller.getHeight());
	  else
		  heightSpinnerModel.setValue(480);
    heightSpinnerModel.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          controller.setHeight(((Number)heightSpinnerModel.getValue()).intValue());
        }
      });
    controller.addPropertyChangeListener(AbstractPhotoController.Property.HEIGHT, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
			  heightSpinnerModel.setValue(controller.getHeight());
          }
        });

    // Create apply proportions check box bound to ASPECT_RATIO controller property
    boolean notFreeAspectRatio = controller.getAspectRatio() != AspectRatio.FREE_RATIO;
    this.applyProportionsCheckBox = new JCheckBox(activity, "");
    this.applyProportionsCheckBox.setSelected(notFreeAspectRatio);
    this.applyProportionsCheckBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
          controller.setAspectRatio(applyProportionsCheckBox.isSelected()
              ? (AspectRatio)aspectRatioComboBox.getSelectedItem()
              : AspectRatio.FREE_RATIO);
        }
      });

	  AspectRatio[] nonFreeAspectRatios = new AspectRatio [] {
			  AspectRatio.VIEW_3D_RATIO,
			  AspectRatio.SQUARE_RATIO,
			  AspectRatio.RATIO_4_3,
			  AspectRatio.RATIO_3_2,
			  AspectRatio.RATIO_16_9,
			  AspectRatio.RATIO_2_1};
	  this.aspectRatioComboBox = new JComboBox(activity, new DefaultComboBoxModel(nonFreeAspectRatios));
	  aspectRatioComboBox.setAdapter(new ArrayAdapter<AspectRatio>(activity, android.R.layout.simple_list_item_1, nonFreeAspectRatios)
	  {
		  @Override
		  public View getView(int position, View convertView, ViewGroup parent)
		  {
			  return getDropDownView(position, convertView, parent);
		  }
		  @Override
		  public View getDropDownView (int position, View convertView, ViewGroup parent)
		  {
			  TextView ret = new TextView(activity);
			  AspectRatio aspectRatio = (AspectRatio)aspectRatioComboBox.getItemAtPosition(position);
			  String displayedValue = "";
			  if (aspectRatio != AspectRatio.FREE_RATIO) {
				  switch (aspectRatio) {
					  case VIEW_3D_RATIO :
						  displayedValue = preferences.getLocalizedString(
								  com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "aspectRatioComboBox.view3DRatio.text");
						  break;
					  case SQUARE_RATIO :
						  displayedValue = preferences.getLocalizedString(
								  com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "aspectRatioComboBox.squareRatio.text");
						  break;
					  case RATIO_4_3 :
						  displayedValue = "4/3";
						  break;
					  case RATIO_3_2 :
						  displayedValue = "3/2";
						  break;
					  case RATIO_16_9 :
						  displayedValue = "16/9";
						  break;
					  case RATIO_2_1 :
						  displayedValue = "2/1";
						  break;
				  }
			  }
			  ret.setText(displayedValue);
			  return ret;
		  }
	  });

    this.aspectRatioComboBox.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent ev) {
       		controller.setAspectRatio((AspectRatio)aspectRatioComboBox.getSelectedItem());
        }
      });
    this.aspectRatioComboBox.setEnabled(notFreeAspectRatio);
    this.aspectRatioComboBox.setSelectedItem(controller.getAspectRatio());
    controller.addPropertyChangeListener(AbstractPhotoController.Property.ASPECT_RATIO,
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            boolean notFreeAspectRatio = controller.getAspectRatio() != AspectRatio.FREE_RATIO;
            applyProportionsCheckBox.setSelected(notFreeAspectRatio);
            aspectRatioComboBox.setEnabled(notFreeAspectRatio);
            aspectRatioComboBox.setSelectedItem(controller.getAspectRatio());
          }
        });

    // Quality label and slider bound to QUALITY controller property
    this.qualityLabel = new JLabel(activity, "");
    this.fastQualityLabel = new JLabel(activity, "");
    this.bestQualityLabel = new JLabel(activity, "");
	  //PJ can't use non 0 min so just go 0-3 and note teh min calls below sort it out when send to from controller (which is 0-3)
    this.qualitySlider = new JSlider(activity, 0, controller.getQualityLevelCount() - 1);

    this.qualitySlider.setPaintTicks(true);    
    this.qualitySlider.setMajorTickSpacing(1);
    this.qualitySlider.setSnapToTicks(true);
    final boolean offScreenImageSupported = Component3DManager.getInstance().isOffScreenImageSupported();
    this.qualitySlider.addChangeListener(new ChangeListener() {
        public void stateChanged(ChangeEvent ev) {
          if (!offScreenImageSupported) {
            // Can't support 2 first quality levels if offscreen image isn't supported 
            qualitySlider.setValue(Math.max(qualitySlider.getMinimum() + 2, qualitySlider.getValue()));
          }
          controller.setQuality((qualitySlider.getValue() - qualitySlider.getMinimum()));
        }
      });
    controller.addPropertyChangeListener(AbstractPhotoController.Property.QUALITY, 
        new PropertyChangeListener() {
          public void propertyChange(PropertyChangeEvent ev) {
            qualitySlider.setValue((qualitySlider.getMinimum() + controller.getQuality()));
          }
        });
    this.qualitySlider.setValue((qualitySlider.getMinimum() + controller.getQuality()));

	  //PJ We are is a dialog so 3d view aspect ratio could only change by screen rotate, not major

    // Listener on 3D view notified when its size changes
    /*final HomeComponent3D view3D = (HomeComponent3D)controller.get3DView();
    final ComponentAdapter view3DSizeListener = new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent ev) {
          controller.set3DViewAspectRatio((float)view3D.getView().getWidth() / view3D.getView().getHeight());
        }
      };

    addAncestorListener(new AncestorListener() {        
      public void ancestorAdded(AncestorEvent ev) {
        view3D.addComponentListener(view3DSizeListener);
      }
      
      public void ancestorRemoved(AncestorEvent ev) {
        view3D.removeComponentListener(view3DSizeListener);
      }
      
      public void ancestorMoved(AncestorEvent ev) {
      }        
    });*/

    setComponentTexts(preferences);
  }


  
  /**
   * Sets the texts of the components.
   */
  private void setComponentTexts(UserPreferences preferences) {
    this.widthLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "widthLabel.text"));
    this.heightLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "heightLabel.text"));
    this.applyProportionsCheckBox.setText(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "applyProportionsCheckBox.text"));
    this.qualityLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "qualityLabel.text"));
    this.fastQualityLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "fastLabel.text"));
    if (!Component3DManager.getInstance().isOffScreenImageSupported()) {
      this.fastQualityLabel.setEnabled(false);
    }
    this.bestQualityLabel.setText(SwingTools.getLocalizedLabelText(preferences,
			com.eteks.sweethome3d.android_props.PhotoSizeAndQualityPanel.class, "bestLabel.text"));
  }


  /**
   * Preferences property listener bound to this panel with a weak reference to avoid
   * strong link between user preferences and this panel.  
   */
  public static class LanguageChangeListener implements PropertyChangeListener {
    private final WeakReference<PhotoSizeAndQualityPanel> photoPanel;

    public LanguageChangeListener(PhotoSizeAndQualityPanel photoPanel) {
      this.photoPanel = new WeakReference<PhotoSizeAndQualityPanel>(photoPanel);
    }

    public void propertyChange(PropertyChangeEvent ev) {
      // If photo panel was garbage collected, remove this listener from preferences
      PhotoSizeAndQualityPanel photoPanel = this.photoPanel.get();
      UserPreferences preferences = (UserPreferences)ev.getSource();
      if (photoPanel == null) {
        preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
      } else {
        //photoPanel.setComponentOrientation(ComponentOrientation.getOrientation(Locale.getDefault()));
        photoPanel.setComponentTexts(preferences);
        //photoPanel.setMnemonics(preferences);
      }
    }
  }

  /**
   * Layouts panel components in panel with their labels. 
   */
  private void layoutComponents() {

	  swapOut(this.widthLabel, R.id.photosizeandqualityview_widthLabel);
	  swapOut(this.widthSpinner, R.id.photosizeandqualityview_widthSpinner);
	  swapOut(this.heightLabel, R.id.photosizeandqualityview_heightLabel);
	  swapOut(this.heightSpinner, R.id.photosizeandqualityview_heightSpinner);
	  swapOut(this.applyProportionsCheckBox, R.id.photosizeandqualityview_applyProportionsCheckBox);
	  swapOut(this.aspectRatioComboBox, R.id.photosizeandqualityview_aspectRatioComboBox);
	  //swapOut(this.qualityLabel, R.id.photosizeandqualityview_qualityLabel);
	  swapOut(this.qualitySlider, R.id.photosizeandqualityview_qualitySlider);
	  swapOut(this.fastQualityLabel, R.id.photosizeandqualityview_fastQualityLabel);
	  swapOut(this.bestQualityLabel, R.id.photosizeandqualityview_bestQualityLabel);
  }
	protected void swapOut(android.view.View newView, int placeHolderId)
	{
		android.view.View placeHolder = rootView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		AndroidDialogView.replaceView(placeHolder, newView);
	}
  
  /**
   * Enables or disables this panel and its components.
   */
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    this.widthSpinner.setEnabled(enabled);
    this.heightSpinner.setEnabled(enabled);
    this.applyProportionsCheckBox.setEnabled(enabled);
    this.aspectRatioComboBox.setEnabled(enabled);
    this.qualitySlider.setEnabled(enabled);
  }
  
  /**
   * Enables or disables components that allow to force proportions.
   */
  public void setProportionsChoiceEnabled(boolean enabled) {
    this.applyProportionsCheckBox.setEnabled(enabled); 
    this.aspectRatioComboBox.setEnabled(enabled && this.applyProportionsCheckBox.isSelected()); 
  }
}
