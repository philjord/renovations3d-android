/*
 * HomeFurniturePanel.java 16 mai 07
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
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.eteks.renovations3d.Renovations3DActivity;
import com.eteks.renovations3d.android.utils.AndroidDialogView;
import com.eteks.sweethome3d.j3d.ModelManager;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.Transformation;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.viewcontroller.BaseboardChoiceController;
import com.eteks.sweethome3d.viewcontroller.DialogView;
import com.eteks.sweethome3d.viewcontroller.FurnitureController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.HomeFurnitureController;
import com.eteks.sweethome3d.viewcontroller.ModelMaterialsController;
import com.eteks.sweethome3d.viewcontroller.TextureChoiceController;
import com.eteks.sweethome3d.viewcontroller.View;
import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.mindblowing.renovations3d.R;
import com.mindblowing.swingish.ActionListener;
import com.mindblowing.swingish.ButtonGroup;
import com.mindblowing.swingish.ChangeListener;
import com.mindblowing.swingish.ItemListener;
import com.mindblowing.swingish.JButton;
import com.mindblowing.swingish.JCheckBox;
import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JLabel;
import com.mindblowing.swingish.JOptionPane;
import com.mindblowing.swingish.JRadioButton;
import com.mindblowing.swingish.JSpinner;
import com.mindblowing.swingish.JTextField;

import org.jogamp.java3d.BranchGroup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.security.AccessControlException;

import javaawt.Graphics;

/**
 * COmbo Home Name editor and other data like autosave
 * as well as home furniture table below
 */
public class HomeDataPanel extends JComponent {

	// if we are not initialized then ignore onCreateViews
	private boolean initialized = false;

	private Home home;
	private UserPreferences preferences;
	private HomeController controller;
	private Activity activity;

	private android.view.View rootView;

	private JLabel nameLabel;
	private JTextField nameTextField;

	private JLabel furnitureTableLabel;
	private FurnitureTable furnitureTable;

	public HomeDataPanel() {

	}

	@Override
	public void paintComponent(Graphics g) {
		//PJ do I ever need this
	}

  	public void init(Home home,
				   	UserPreferences preferences,
                    HomeController controller,
                    Activity activity) {
	  initialized = true;
	  this.home = home;
	  this.preferences = preferences;
	  this.controller = controller;
	  this.activity = activity;

  	}

	@Override
	public android.view.View onCreateView(LayoutInflater inflater,
										  ViewGroup container,
										  Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.home_data_panel, container, false);
		if (initialized) {
			this.setHasOptionsMenu(true);

			createComponents(preferences, controller);
			layoutComponents();
		}

		// make the right swiper work
		ImageButton furnitureTableRightSwiper = (ImageButton)rootView.findViewById(R.id.homedata_RightSwiper);
		furnitureTableRightSwiper.setOnClickListener(new android.view.View.OnClickListener() {
			@Override
			public void onClick(android.view.View v) {
				((Renovations3DActivity)getActivity()).getViewPager().setCurrentItem(1, true);
			}
		});

		return rootView;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		//inflater.inflate(R.menu.furniture_cat_list_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		//TODO: this is a test place holder for improve menus


		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return false;
	}

	  /**
	   * Creates and initializes components and spinners model.
	   */
	  private void createComponents(final UserPreferences preferences,
									final HomeController controller) {

		  // Create name label and its text field bound to NAME controller property
		  this.nameLabel = new JLabel(this.getContext(), SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "nameLabel.text"));

		  // force a name so the user can figure out where it might have saved,
		  // but don't bother with a path component, the save in Activity will discover its
		  // not writable and open saveas for older versions
		  if(home.getName() == null || home.getName().trim().length() == 0) {
			  home.setName(SwingTools.getLocalizedLabelText(preferences,
					  com.eteks.sweethome3d.swing.HomePane.class, "NEW_HOME.Name"));
		  } else {
			  home.setName(new File(home.getName()).getName().replace(".sh3d",""));
		  }

		  this.nameTextField = new JTextField(activity, new File(home.getName()).getName().replace(".sh3d",""));
		  final PropertyChangeListener nameChangeListener = new PropertyChangeListener() {
			  public void propertyChange(PropertyChangeEvent ev) {
				  // this field shows only the last file portion of home.getName()
				  // but it doesn't touch the home name which holds the path info (optionally)
				  // home Name is the full name of the home! including file location, but we obviously only want to present the final portion
				  // without the file extension .sh3d
				  activity.runOnUiThread(() -> {
						// strip any folder path info
						nameTextField.setText(new File(home.getName()).getName().replace(".sh3d",""));
				  });
			  }
		  };
		  home.addPropertyChangeListener(Home.Property.NAME, nameChangeListener);
		  TextWatcher textChangedListener = new TextWatcher() {
			  public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
			  }

			  public void beforeTextChanged(CharSequence s, int arg1, int arg2, int arg3) {
			  }

			  public void afterTextChanged(Editable arg0) {
				  String name = nameTextField.getText().toString();
				  // we don't want to be infinitely recursive
				  home.removePropertyChangeListener(Home.Property.NAME, nameChangeListener);
				  // attach the current Home Name's parent path to the name text and set
				  // so we don't lose any path info that was being held onto
				  File homeParentFile = new File(home.getName()).getParentFile();
				  home.setName(new File(homeParentFile, name).getPath());
				  home.addPropertyChangeListener(Home.Property.NAME, nameChangeListener);
			  }
		  };
		  nameTextField.addTextChangedListener(textChangedListener);




		  this.furnitureTableLabel = new JLabel(this.getContext(), SwingTools.getLocalizedLabelText(preferences,
				  com.eteks.sweethome3d.swing.HomePane.class, "FURNITURE_MENU.Name"));

		  addLanguageListener(preferences);

		  this.furnitureTable = new FurnitureTable(home,
												  preferences,
												  controller.getFurnitureController(),
												  activity);
	  }

	  /**
	   * Layouts panel components in panel with their labels.
	   */
	  private void layoutComponents() {

		  swapOut(this.nameLabel, R.id.homedata_nameLabel);
		  swapOut(this.nameTextField, R.id.homedata_nameTextField);
		  if (this.nameTextField.getText().toString() != null && this.nameTextField.getText().toString().length() > 0)
			  activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		  swapOut(this.furnitureTableLabel, R.id.homedata_furniture_table_label);
		  swapOut(this.furnitureTable, R.id.homedata_furniture_table_panel);
	  }

	protected void swapOut(android.view.View newView, int placeHolderId) {
		android.view.View placeHolder = rootView.findViewById(placeHolderId);
		newView.setLayoutParams(placeHolder.getLayoutParams());
		replaceView(placeHolder, newView);
	}


	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);

		// tell the furnitureTable it is also visible (or not)
		if(this.furnitureTable != null)
			this.furnitureTable.setUserVisibleHint(isVisibleToUser);

		// this gets called heaps of time, wait until we have an activity
		if(isVisibleToUser && getActivity() != null) {
			//PJ cut out as it provides almost no infomation at all now, add back when table is functional
			//JOptionPane.possiblyShowWelcomeScreen(getActivity(), WELCOME_SCREEN_UNWANTED, R.string.furnitureview_welcometext, preferences);
		}

	//TODO: what is this now? HomeDataPanel gives it in? is it useful?
		if(isVisibleToUser && getView() != null) {
			// As furniture properties values change may alter sort order and filter, update the whole table
			((FurnitureTable.FurnitureTreeTableModel) this.furnitureTable.getModel()).filterAndSortFurniture();
			// Update selected rows
			this.furnitureTable.updateTableSelectedFurniture(home);
			//storeExpandedRows(home, controller);
			//PJPJPJ note wildly expensive, must use only the value in source and update a single row
			this.furnitureTable.updateTable();
			getView().postInvalidate();
		}
	}

	/**
	 * Adds a property change listener to <code>preferences</code> to update
	 * column names when preferred language changes.
	 */
	private void addLanguageListener(UserPreferences preferences) {
		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE,
				new LanguageChangeListener(this));
	}

	/**
	 * Preferences property listener bound to this component with a weak reference to avoid
	 * strong link between preferences and this component.
	 */
	private class LanguageChangeListener implements PropertyChangeListener {
		private WeakReference<HomeDataPanel> homeDataPanel;

		public LanguageChangeListener(HomeDataPanel homeDataPanel) {
			this.homeDataPanel = new WeakReference<HomeDataPanel>(homeDataPanel);
		}

		public void propertyChange(PropertyChangeEvent ev) {
			// If furniture table column model was garbage collected, remove this listener from preferences
			HomeDataPanel homeDataPanel = this.homeDataPanel.get();
			UserPreferences preferences = (UserPreferences)ev.getSource();
			if (homeDataPanel == null) {
				preferences.removePropertyChangeListener(
						UserPreferences.Property.LANGUAGE, this);
			} else {
				nameLabel.setText(SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.swing.HomeFurniturePanel.class, "nameLabel.text"));
				furnitureTableLabel.setText( SwingTools.getLocalizedLabelText(preferences,
						com.eteks.sweethome3d.swing.HomePane.class, "FURNITURE_MENU.Name"));
			}
		}
	}
}


