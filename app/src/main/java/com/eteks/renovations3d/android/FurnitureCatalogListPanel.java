package com.eteks.renovations3d.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.CollectionEvent;
import com.eteks.sweethome3d.model.CollectionListener;

import com.eteks.sweethome3d.model.FurnitureCatalog;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.SelectionEvent;
import com.eteks.sweethome3d.model.SelectionListener;
import com.eteks.sweethome3d.model.UserPreferences;

import com.eteks.sweethome3d.viewcontroller.FurnitureCatalogController;
import com.eteks.sweethome3d.viewcontroller.HomeController;
import com.eteks.sweethome3d.viewcontroller.VCView;

import com.eteks.renovations3d.SweetHomeAVRActivity;
import com.eteks.renovations3d.android.swingish.JComponent;
import com.mindblowing.renovations3d.R;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;


import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import javaawt.EventQueue;
import javaawt.Graphics;
import javaxswing.Icon;
import javaxswing.ImageIcon;


/**
 * Created by phil on 11/22/2016.
 */

public class FurnitureCatalogListPanel extends JComponent implements VCView
{
	public FurnitureCatalogListPanel()
	{
	}
	@Override
	public void paintComponent(Graphics g)
	{
		//PJ do I ever need this
	}

	private GridView gridView;
	private FurnitureImageView selectedFiv = null;

	@Override
	public View onCreateView(LayoutInflater inflater,
							 ViewGroup container, Bundle savedInstanceState)
	{
		this.setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.furniture_catalog_list, container, false);

		gridView = (GridView) rootView.findViewById(R.id.main_grid);
		gridView.setAdapter(new ImageAdapter(this.getContext()));

		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id)
			{
				selectedFiv = (FurnitureImageView)v;
				for (int j = 0; j < gridView.getChildCount(); j++) {
					final ImageView iv = (ImageView) gridView.getChildAt(j);
					if(iv == v)
						iv.setBackgroundColor(Color.CYAN);
					else
						iv.setBackgroundColor(Color.WHITE);
				}

			}
		});

		rootView.findViewById(R.id.furnitureAdd).setOnClickListener(buttonListener);

		return rootView;
	}

	View.OnClickListener buttonListener = new View.OnClickListener()
	{
		public void onClick(View view)
		{
			// now set the action to the action
			switch (view.getId())
			{
				case R.id.furnitureAdd:


						if (selectedFiv != null)
						{
							ArrayList<HomePieceOfFurniture> al = new ArrayList<HomePieceOfFurniture>();
							al.add(new HomePieceOfFurniture(selectedFiv.getCatalogPieceOfFurniture()));
							((SweetHomeAVRActivity) FurnitureCatalogListPanel.this.getActivity())
									.sweetHomeAVR.getHomeController().getFurnitureController().addFurniture(al);
							Toast.makeText(FurnitureCatalogListPanel.this.getActivity(), "Furniture added" , Toast.LENGTH_SHORT).show();
						}

					break;
			}
		}
	};

	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.furniture_cat_list_menu, menu);

		super.onCreateOptionsMenu(menu, inflater);
	}
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		menu.findItem(R.id.import_furniture_lib).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_FURNITURE_LIBRARY.Name"));
		menu.findItem(R.id.import_texture_lib).setTitle(preferences.getLocalizedString(
				com.eteks.sweethome3d.android_props.HomePane.class, "IMPORT_TEXTURES_LIBRARY.Name"));
		super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.import_furniture_lib:
				Thread t2 = new Thread(){public void run(){
					HomeController controller = SweetHomeAVRActivity.sweetHomeAVR.getHomeController();
					if(controller != null)
					{
						//We can't use this as it gets onto the the EDT and cause much trouble, so we just copy out
						//controller.importFurnitureLibrary();
						String furnitureLibraryName = controller.getView().showImportFurnitureLibraryDialog();
						if(furnitureLibraryName != null) {
							controller.importFurnitureLibrary(furnitureLibraryName);
						}
					}}};
				t2.start();
				return true;
			case R.id.import_texture_lib:
				Thread t3 = new Thread(){public void run(){
					HomeController controller2 = SweetHomeAVRActivity.sweetHomeAVR.getHomeController();
					if(controller2 != null)
					{
						//We can't use this as it gets onto the the EDT and cause much trouble, so we just copy out
						//	controller.importTexturesLibrary();
						String texturesLibraryName = controller2.getView().showImportTexturesLibraryDialog();
						if(texturesLibraryName != null) {
							controller2.importTexturesLibrary(texturesLibraryName);
						}
					}
				}};
				t3.start();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	private Paint mTextPaint = new Paint();

	public class ImageAdapter extends BaseAdapter
	{
		private Context mContext;

		public ImageAdapter(Context c) {
				mContext = c;
		}

		public int getCount() {
			return catalogListModel.getSize();
		}

		public Object getItem(int position) {
			return   catalogListModel.getElementAt(position);
		}

		public long getItemId(int position) {
			return 0;
		}

		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			  CatalogPieceOfFurniture  catalogPieceOfFurniture  = (CatalogPieceOfFurniture) catalogListModel.getElementAt(position);
			ImageView imageView;
			//can't recycle as the furniture catalog is wrong
			//if (convertView == null) {
				// if it's not recycled, initialize some attributes
				imageView = new FurnitureImageView(mContext, catalogPieceOfFurniture);

				imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(8, 8, 8, 8);
				imageView.setBackgroundColor(Color.WHITE);
				int DEFAULT_ICON_HEIGHT = 80;//see below
				Icon icon = IconManager.getInstance().getIcon(catalogPieceOfFurniture.getIcon(), DEFAULT_ICON_HEIGHT, null);
				if(icon instanceof ImageIcon)
				{
					imageView.setImageBitmap(((Bitmap) ((ImageIcon)icon).getImage().getDelegate()));
				}
			//} else {
			//	imageView = (ImageView) convertView;
			//}


			return imageView;
		}

	}

	class FurnitureImageView extends ImageView
	{
		private CatalogPieceOfFurniture catalogPieceOfFurniture;
		public FurnitureImageView(Context c,  CatalogPieceOfFurniture catalogPieceOfFurniture )
		{
			super(c);
			this.catalogPieceOfFurniture =catalogPieceOfFurniture;
		}

		public CatalogPieceOfFurniture getCatalogPieceOfFurniture()
		{
			return catalogPieceOfFurniture;
		}

		public void onDraw(Canvas canvas)
		{
			super.onDraw(canvas);
			canvas.drawText(catalogPieceOfFurniture.getName(), 10, 85, mTextPaint);
		}
	};

	private FurnitureCatalogListModel catalogListModel;

	private FurnitureCatalog catalog;
	private UserPreferences preferences;
	private FurnitureCatalogController controller;


	//private ListSelectionListener listSelectionListener;
	//private JLabel                categoryFilterLabel;
	//private JComboBox             categoryFilterComboBox;
	//private JLabel                searchLabel;
	//private JTextField            searchTextField;
	//private JList                 catalogFurnitureList;

	public void init(FurnitureCatalog catalog, UserPreferences preferences, FurnitureCatalogController controller)
	{
		this.catalog = catalog;
		this.preferences = preferences;
		this.controller = controller;

		createComponents(catalog, preferences, controller);
		setMnemonics(preferences);
		layoutComponents();
	}

	/**
	 * Creates the components displayed by this panel.
	 */
	private void createComponents(FurnitureCatalog catalog,
								  final UserPreferences preferences,
								  final FurnitureCatalogController controller) {
		    catalogListModel = new FurnitureCatalogListModel(catalog);
/*		this.catalogFurnitureList = new JList(catalogListModel) {
			private CatalogItemToolTip toolTip = new CatalogItemToolTip(false, preferences);
			private boolean mousePressed;
			private boolean firstScroll;

			{
				addMouseListener(new MouseAdapter() {
					@Override
					public void mousePressed(MouseEvent ev) {
						firstScroll = true;
						mousePressed = true;
					}

					@Override
					public void mouseReleased(MouseEvent ev) {
						mousePressed = false;
					}
				});
			}

			@Override
			public JToolTip createToolTip() {
				if (this.toolTip.isTipTextComplete()) {
					// Use toolTip object only for its text returned in getToolTipText
					return super.createToolTip();
				} else {
					this.toolTip.setComponent(this);
					return this.toolTip;
				}
			}

			@Override
			public String getToolTipText(MouseEvent ev) {
				// Return a tooltip for furniture pieces described in the list.
				int index = locationToIndex(ev.getPoint());
				if (index != -1) {
					this.toolTip.setCatalogItem((CatalogPieceOfFurniture)getModel().getElementAt(index));
					return this.toolTip.getTipText();
				} else {
					return null;
				}
			}

			@Override
			public void scrollRectToVisible(Rectangle rectangle) {
				if (!this.mousePressed
						|| this.firstScroll) {
					// During a drag and drop, let's accept only the first viewport adjustment provoked by UI
					this.firstScroll = false;
					super.scrollRectToVisible(rectangle);
				}
			}
		};
		this.catalogFurnitureList.setLayoutOrientation(JList.HORIZONTAL_WRAP);
		this.catalogFurnitureList.setCellRenderer(new CatalogCellRenderer());
		this.catalogFurnitureList.setAutoscrolls(false);
		if (OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
			this.catalogFurnitureList.setDragEnabled(true);
		}
		this.catalogFurnitureList.setTransferHandler(null);
		// Remove Select all action
		this.catalogFurnitureList.getActionMap().getParent().remove("selectAll");
		addDragListener(this.catalogFurnitureList);
		addMouseListeners(this.catalogFurnitureList, controller);
*/
/*		catalogListModel.addListDataListener(new ListDataListener() {
			public void contentsChanged(ListDataEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void intervalAdded(ListDataEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void intervalRemoved(ListDataEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}
		});*/
/*		this.catalogFurnitureList.addAncestorListener(new AncestorListener() {
			public void ancestorAdded(AncestorEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void ancestorMoved(AncestorEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}

			public void ancestorRemoved(AncestorEvent ev) {
			}
		});*/
/*		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent ev) {
				spreadFurnitureIconsAlongListWidth();
			}
		});*/

		updateListSelectedFurniture(catalog, controller);
		addSelectionListeners(catalog, controller);

//		this.categoryFilterLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
//				FurnitureCatalogListPanel.class, "categoryFilterLabel.text"));
		List<FurnitureCategory> categories = new ArrayList<FurnitureCategory>();
		categories.add(null);
		categories.addAll(catalog.getCategories());
/*		this.categoryFilterComboBox = new JComboBox(new DefaultComboBoxModel(categories.toArray())) {
			@Override
			public Dimension getMinimumSize() {
				return new Dimension(60, super.getMinimumSize().height);
			}
		};
		this.categoryFilterComboBox.setMaximumRowCount(20);
		this.categoryFilterComboBox.setRenderer(new DefaultListCellRenderer() {
			public Component getListCellRendererComponent(JList list, Object value,
														  int index, boolean isSelected, boolean cellHasFocus) {
				if (value == null) {
					return super.getListCellRendererComponent(list,
							preferences.getLocalizedString(FurnitureCatalogListPanel.class, "categoryFilterComboBox.noCategory"),
							index, isSelected, cellHasFocus);
				} else {
					return super.getListCellRendererComponent(list,
							((FurnitureCategory)value).getName(), index, isSelected, cellHasFocus);
				}
			}
		});
		this.categoryFilterComboBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent ev) {
				catalogListModel.setFilterCategory((FurnitureCategory)categoryFilterComboBox.getSelectedItem());
				catalogFurnitureList.clearSelection();
			}
		});*/

/*		this.searchLabel = new JLabel(SwingTools.getLocalizedLabelText(preferences,
				FurnitureCatalogListPanel.class, "searchLabel.text"));
		this.searchTextField = new JTextField(5);
		this.searchTextField.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent ev) {
				Object selectedValue = catalogFurnitureList.getSelectedValue();
				catalogListModel.setFilterText(searchTextField.getText());
				catalogFurnitureList.clearSelection();
				catalogFurnitureList.setSelectedValue(selectedValue, true);

				if (catalogListModel.getSize() == 1) {
					catalogFurnitureList.setSelectedIndex(0);
				}
			}

			public void insertUpdate(DocumentEvent ev) {
				changedUpdate(ev);
			}

			public void removeUpdate(DocumentEvent ev) {
				changedUpdate(ev);
			}
		});*/
/*		this.searchTextField.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "deleteContent");
		this.searchTextField.getActionMap().put("deleteContent", new AbstractAction() {
			public void actionPerformed(ActionEvent ev) {
				searchTextField.setText("");
			}
		});
		if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
			this.searchTextField.putClientProperty("JTextField.variant", "search");
		}*/

		PreferencesChangeListener preferencesChangeListener = new PreferencesChangeListener(this);
		preferences.addPropertyChangeListener(UserPreferences.Property.LANGUAGE, preferencesChangeListener);
		catalog.addFurnitureListener(preferencesChangeListener);
	}

/**
 * Language and catalog listener bound to this component with a weak reference to avoid
 * strong link between preferences and this component.
 */
private static class PreferencesChangeListener implements PropertyChangeListener, CollectionListener<CatalogPieceOfFurniture> {
	private final WeakReference<FurnitureCatalogListPanel> furnitureCatalogPanel;

	public PreferencesChangeListener(FurnitureCatalogListPanel furnitureCatalogPanel) {
		this.furnitureCatalogPanel = new WeakReference<FurnitureCatalogListPanel>(furnitureCatalogPanel);
	}

	public void propertyChange(PropertyChangeEvent ev) {
		// If panel was garbage collected, remove this listener from preferences
		FurnitureCatalogListPanel furnitureCatalogPanel = this.furnitureCatalogPanel.get();
		UserPreferences preferences = (UserPreferences)ev.getSource();
		if (furnitureCatalogPanel == null) {
			preferences.removePropertyChangeListener(UserPreferences.Property.LANGUAGE, this);
		} else {
/*			furnitureCatalogPanel.categoryFilterLabel.setText(SwingTools.getLocalizedLabelText(preferences,
					FurnitureCatalogListPanel.class, "categoryFilterLabel.text"));
			furnitureCatalogPanel.searchLabel.setText(SwingTools.getLocalizedLabelText(preferences,
					FurnitureCatalogListPanel.class, "searchLabel.text"));*/
			furnitureCatalogPanel.setMnemonics(preferences);
			// Categories listed in combo box are updated through collectionChanged
		}
	}

	public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
		// If panel was garbage collected, remove this listener from catalog
		FurnitureCatalogListPanel furnitureCatalogPanel = this.furnitureCatalogPanel.get();
		FurnitureCatalog catalog = (FurnitureCatalog)ev.getSource();
		if (furnitureCatalogPanel == null) {
			catalog.removeFurnitureListener(this);
		} else {
/*			DefaultComboBoxModel model =
					(DefaultComboBoxModel)furnitureCatalogPanel.categoryFilterComboBox.getModel();
			FurnitureCategory category = ev.getItem().getCategory();
			List<FurnitureCategory> categories = catalog.getCategories();
			if (!categories.contains(category)) {
				model.removeElement(category);
				furnitureCatalogPanel.categoryFilterComboBox.setSelectedIndex(0);
			} else if (model.getIndexOf(category) == -1) {
				model.insertElementAt(category, categories.indexOf(category) + 1);
			}*/
		}
	}
}

	/**
	 * Adds mouse listeners that will select only the piece under mouse cursor in the furniture list
	 * before the start of a drag operation, ensuring only one piece can be dragged at a time.
	 */
/*	private void addDragListener(final JList catalogFurnitureList) {
		MouseInputAdapter mouseListener = new MouseInputAdapter() {
			private CatalogPieceOfFurniture exportedPiece;

			@Override
			public void mousePressed(MouseEvent ev) {
				this.exportedPiece = null;
				if (SwingUtilities.isLeftMouseButton(ev)
						&& catalogFurnitureList.getSelectedValue() != null
						&& catalogFurnitureList.getTransferHandler() != null) {
					int index = catalogFurnitureList.locationToIndex(ev.getPoint());
					if (index != -1) {
						this.exportedPiece = (CatalogPieceOfFurniture)catalogFurnitureList.getModel().getElementAt(index);
					}
				}
			}

			public void mouseDragged(MouseEvent ev) {
				if (this.exportedPiece != null) {
					if (catalogFurnitureList.getSelectedIndices().length > 1) {
						catalogFurnitureList.clearSelection();
						catalogFurnitureList.setSelectedValue(this.exportedPiece, false);
					}
					if (!OperatingSystem.isJavaVersionGreaterOrEqual("1.6")) {
						catalogFurnitureList.getTransferHandler().exportAsDrag(catalogFurnitureList, ev, DnDConstants.ACTION_COPY);
					}
					this.exportedPiece = null;
				}
			}
		};

		catalogFurnitureList.addMouseListener(mouseListener);
		catalogFurnitureList.addMouseMotionListener(mouseListener);
	}*/

	/**
	 * Adds mouse listeners to the furniture list to modify selected furniture
	 * and manage links in piece information.
	 */
/*	private void addMouseListeners(final JList catalogFurnitureList,
								   final FurnitureCatalogController controller) {
		final Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
		MouseInputAdapter mouseListener = new MouseInputAdapter () {
			@Override
			public void mouseClicked(MouseEvent ev) {
				if (SwingUtilities.isLeftMouseButton(ev)) {
					if (ev.getClickCount() == 2) {
						int clickedPieceIndex = catalogFurnitureList.locationToIndex(ev.getPoint());
						if (clickedPieceIndex != -1) {
							controller.modifySelectedFurniture();
						}
					} else {
						URL url = getURLAt(ev.getPoint(), catalogFurnitureList);
						if (url != null) {
							SwingTools.showDocumentInBrowser(url);
						}
					}
				}
			}

			@Override
			public void mouseMoved(MouseEvent ev) {
				final URL url = getURLAt(ev.getPoint(), catalogFurnitureList);
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						if (url != null) {
							setCursor(handCursor);
						} else {
							setCursor(Cursor.getDefaultCursor());
						}
					}
				});
			}

			private URL getURLAt(Point point, JList list) {
				int pieceIndex = list.locationToIndex(point);
				if (pieceIndex != -1) {
					CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)list.getModel().getElementAt(pieceIndex);
					String information = piece.getInformation();
					if (information != null) {
						JComponent rendererComponent = (JComponent)list.getCellRenderer().
								getListCellRendererComponent(list, piece, pieceIndex, list.isSelectedIndex(pieceIndex), false);
						for (JEditorPane pane : SwingTools.findChildren(rendererComponent, JEditorPane.class)) {
							Rectangle cellBounds = list.getCellBounds(pieceIndex, pieceIndex);
							point.x -= cellBounds.x;
							point.y -= cellBounds.y + pane.getY();
							if (point.x > 0 && point.y > 0) {
								// Search in information pane if point is over a HTML link
								int position = pane.viewToModel(point);
								if (position > 1
										&& pane.getDocument() instanceof HTMLDocument) {
									HTMLDocument hdoc = (HTMLDocument)pane.getDocument();
									Element element = hdoc.getCharacterElement(position);
									AttributeSet a = element.getAttributes();
									AttributeSet anchor = (AttributeSet)a.getAttribute(HTML.Tag.A);
									if (anchor != null) {
										String href = (String)anchor.getAttribute(HTML.Attribute.HREF);
										if (href != null) {
											try {
												return new URL(href);
											} catch (MalformedURLException ex) {
												// Ignore malformed URL
											}
										}
									}
								}
							}
						}
					}
				}
				return null;
			}
		};
		catalogFurnitureList.addMouseListener(mouseListener);
		catalogFurnitureList.addMouseMotionListener(mouseListener);
	}*/

	/**
	 * Sets components mnemonics and label / component associations.
	 */
	private void setMnemonics(UserPreferences preferences) {
/*		if (!OperatingSystem.isMacOSX()) {
			this.categoryFilterLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
					FurnitureCatalogListPanel.class, "categoryFilterLabel.mnemonic")).getKeyCode());
			this.categoryFilterLabel.setLabelFor(this.categoryFilterComboBox);
			this.searchLabel.setDisplayedMnemonic(KeyStroke.getKeyStroke(preferences.getLocalizedString(
					FurnitureCatalogListPanel.class, "searchLabel.mnemonic")).getKeyCode());
			this.searchLabel.setLabelFor(this.searchTextField);
		}*/
	}

	/**
	 * Layouts the components displayed by this panel.
	 */
	private void layoutComponents() {
/*		int labelAlignment = OperatingSystem.isMacOSX()
				? GridBagConstraints.LINE_END
				: GridBagConstraints.LINE_START;
		// First row
		Insets labelInsets = new Insets(0, 2, 5, 3);
		Insets componentInsets = new Insets(0, 2, 3, 0);
		if (!OperatingSystem.isMacOSX()) {
			labelInsets.top = 2;
			componentInsets.top = 2;
			componentInsets.right = 2;
		}
		add(this.categoryFilterLabel, new GridBagConstraints(
				0, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL, labelInsets, 0, 0));
		add(this.categoryFilterComboBox, new GridBagConstraints(
				1, 0, 1, 1, 0, 0, GridBagConstraints.LINE_START,
				GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
		// Second row
		if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
			add(this.searchTextField, new GridBagConstraints(
					0, 1, 2, 1, 0, 0, GridBagConstraints.LINE_START,
					GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 0), 0, 0));
		} else {
			add(this.searchLabel, new GridBagConstraints(
					0, 1, 1, 1, 0, 0, labelAlignment,
					GridBagConstraints.NONE, labelInsets, 0, 0));
			add(this.searchTextField, new GridBagConstraints(
					1, 1, 1, 1, 0, 0, GridBagConstraints.LINE_START,
					GridBagConstraints.HORIZONTAL, componentInsets, 0, 0));
		}
		// Last row
		JScrollPane listScrollPane = new JScrollPane(this.catalogFurnitureList);
		listScrollPane.getVerticalScrollBar().addAdjustmentListener(
				SwingTools.createAdjustmentListenerUpdatingScrollPaneViewToolTip(listScrollPane));
		listScrollPane.setPreferredSize(new Dimension(250, 250));
		listScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		add(listScrollPane,
				new GridBagConstraints(
						0, 2, 2, 1, 1, 1, GridBagConstraints.CENTER,
						GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		SwingTools.installFocusBorder(this.catalogFurnitureList);

		setFocusTraversalPolicyProvider(true);
		setFocusTraversalPolicy(new LayoutFocusTraversalPolicy() {
			@Override
			public Component getDefaultComponent(Container aContainer) {
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						// Return furniture list only at the first request
						setFocusTraversalPolicyProvider(false);
					}
				});
				return catalogFurnitureList;
			}
		});*/
	}

	/**
	 * Computes furniture list visible row count to ensure its horizontal scrollbar
	 * won't be seen.
	 */
/*	private void spreadFurnitureIconsAlongListWidth() {
		ListModel model = this.catalogFurnitureList.getModel();
		int size = model.getSize();
		int extentWidth = ((JViewport)this.catalogFurnitureList.getParent()).getExtentSize().width;
		ListCellRenderer cellRenderer = this.catalogFurnitureList.getCellRenderer();
		// Search max width and height
		int maxCellWidth = 1;
		int maxCellHeight = 0;
		for (int i = 0; i < size; i++) {
			Dimension cellPreferredSize = cellRenderer.getListCellRendererComponent(this.catalogFurnitureList, model.getElementAt(i),
					i, this.catalogFurnitureList.isSelectedIndex(i), false).getPreferredSize();
			maxCellWidth = Math.max(maxCellWidth, cellPreferredSize.width);
			maxCellHeight = Math.max(maxCellHeight, cellPreferredSize.height);
		}
		// Compute a fixed cell width that will spread
		int visibleItemsPerRow = Math.max(1, extentWidth / maxCellWidth);
		this.catalogFurnitureList.setVisibleRowCount(size % visibleItemsPerRow == 0
				? size / visibleItemsPerRow
				: size / visibleItemsPerRow + 1);
		this.catalogFurnitureList.setFixedCellWidth(maxCellWidth + (extentWidth % maxCellWidth) / visibleItemsPerRow);
		// Set also cell height otherwise first calls to repaint done by icon manager won't repaint it
		// because the list have a null size at the beginning
		this.catalogFurnitureList.setFixedCellHeight(maxCellHeight);
	}*/

	/**
	 * Adds the listeners that manage selection synchronization in this tree.
	 */
	private void addSelectionListeners(final FurnitureCatalog catalog,
									   final FurnitureCatalogController controller) {
		final SelectionListener modelSelectionListener = new SelectionListener() {
			public void selectionChanged(SelectionEvent selectionEvent) {
				updateListSelectedFurniture(catalog, controller);
			}
		};
/*		this.listSelectionListener = new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent ev) {
				// Updates selected furniture in catalog from selected nodes in tree.
				controller.removeSelectionListener(modelSelectionListener);
				controller.setSelectedFurniture(getSelectedFurniture());
				controller.addSelectionListener(modelSelectionListener);
			}
		};*/

		controller.addSelectionListener(modelSelectionListener);
//		this.catalogFurnitureList.getSelectionModel().addListSelectionListener(this.listSelectionListener);
	}

	/**
	 * Updates selected items in list from <code>controller</code> selected furniture.
	 */
	private void updateListSelectedFurniture(FurnitureCatalog catalog,
											 FurnitureCatalogController controller) {
/*		if (this.listSelectionListener != null) {
			this.catalogFurnitureList.getSelectionModel().removeListSelectionListener(this.listSelectionListener);
		}

		this.catalogFurnitureList.clearSelection();
		List<CatalogPieceOfFurniture> selectedFurniture = controller.getSelectedFurniture();
		if (selectedFurniture.size() > 0) {
			ListModel model = this.catalogFurnitureList.getModel();
			List<Integer> selectedIndices = new ArrayList<Integer>();
			for (CatalogPieceOfFurniture piece : selectedFurniture) {
				for (int i = 0, n = model.getSize(); i < n; i++) {
					if (piece == model.getElementAt(i)) {
						selectedIndices.add(i);
						break;
					}
				}
			}
			if (selectedIndices.size() > 0) {
				int [] indices = new int [selectedIndices.size()];
				for (int i = 0; i < indices.length; i++) {
					indices [i] = selectedIndices.get(i);
				}
				this.catalogFurnitureList.setSelectedIndices(indices);
				this.catalogFurnitureList.ensureIndexIsVisible(indices [0]);
			}
		}

		if (this.listSelectionListener != null) {
			this.catalogFurnitureList.getSelectionModel().addListSelectionListener(this.listSelectionListener);
		}*/
	}

	/**
	 * Returns the selected furniture in list.
	 */
/*	private List<CatalogPieceOfFurniture> getSelectedFurniture() {
		Object [] selectedValues = this.catalogFurnitureList.getSelectedValues();
		CatalogPieceOfFurniture [] selectedFurniture = new CatalogPieceOfFurniture [selectedValues.length];
		System.arraycopy(selectedValues, 0, selectedFurniture, 0, selectedValues.length);
		return Arrays.asList(selectedFurniture);
	}*/

	/**
	 * Sets the transfer handler of the list displayed by this panel.
	 */
/*	@Override
	public void setTransferHandler(TransferHandler handler) {
		this.catalogFurnitureList.setTransferHandler(handler);
	}*/

	/**
	 * Returns the transfer handler of the list displayed by this panel.
	 */
/*	@Override
	public TransferHandler getTransferHandler() {
		return this.catalogFurnitureList.getTransferHandler();
	}*/

	/**
	 * Sets the popup menu of the list displayed by this panel.
	 */
/*	@Override
	public void setComponentPopupMenu(JPopupMenu popup) {
		this.catalogFurnitureList.setComponentPopupMenu(popup);
	}*/

	/**
	 * Returns the popup menu of the list displayed by this panel.
	 */
/*	@Override
	public JPopupMenu getComponentPopupMenu() {
		return this.catalogFurnitureList.getComponentPopupMenu();
	}*/

/**
 * Cell renderer for the furniture list.
 */
/*private static class CatalogCellRenderer extends JComponent implements ListCellRenderer {
	private static final int DEFAULT_ICON_HEIGHT = Math.round(48 * SwingTools.getResolutionScale());
	private Font                    defaultFont;
	private Font                    modifiablePieceFont;
	private DefaultListCellRenderer nameLabel;
	private JEditorPane             informationPane;

	public CatalogCellRenderer() {
		setLayout(null);
		this.nameLabel = new DefaultListCellRenderer() {
			@Override
			public Dimension getPreferredSize() {
				return new Dimension(DEFAULT_ICON_HEIGHT * 3 / 2 + 5, super.getPreferredSize().height);
			}
		};
		this.nameLabel.setHorizontalTextPosition(JLabel.CENTER);
		this.nameLabel.setVerticalTextPosition(JLabel.BOTTOM);
		this.nameLabel.setHorizontalAlignment(JLabel.CENTER);
		this.nameLabel.setText("-");
		this.nameLabel.setIcon(IconManager.getInstance().getWaitIcon(DEFAULT_ICON_HEIGHT));
		this.defaultFont = UIManager.getFont("ToolTip.font");
		this.modifiablePieceFont = new Font(this.defaultFont.getFontName(), Font.ITALIC, this.defaultFont.getSize());
		this.nameLabel.setFont(this.defaultFont);

		this.informationPane = new JEditorPane("text/html", "-");
		this.informationPane.setOpaque(false);
		this.informationPane.setEditable(false);
		String bodyRule = "body { font-family: " + this.defaultFont.getFamily() + "; "
				+ "font-size: " + this.defaultFont.getSize() + "pt; "
				+ "text-align: center; }";
		((HTMLDocument)this.informationPane.getDocument()).getStyleSheet().addRule(bodyRule);

		add(this.nameLabel);
		add(this.informationPane);
	}

	public Component getListCellRendererComponent(JList list,
												  Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus) {
		CatalogPieceOfFurniture piece = (CatalogPieceOfFurniture)value;
		// Configure name label with its icon, background and focus colors
		this.nameLabel.getListCellRendererComponent(list,
				value, index, isSelected, cellHasFocus);
		this.nameLabel.setText(" " + piece.getName() + " ");
		this.nameLabel.setIcon(getLabelIcon(list, piece.getIcon()));
		this.nameLabel.setFont(piece.isModifiable()
				? this.modifiablePieceFont : this.defaultFont);

		this.informationPane.setText(piece.getInformation());
		return this;
	}

	@Override
	public void doLayout() {
		Dimension namePreferredSize = this.nameLabel.getPreferredSize();
		this.nameLabel.setSize(getWidth(), namePreferredSize.height);
		this.informationPane.setBounds(0, namePreferredSize.height,
				getWidth(), getHeight() - namePreferredSize.height);
	}

	@Override
	public Dimension getPreferredSize() {
		Dimension preferredSize = this.nameLabel.getPreferredSize();
		preferredSize.height += this.informationPane.getPreferredSize().height + 2;
		return preferredSize;
	}*/

	/**
	 * The following methods are overridden for performance reasons.
	 */
/*	@Override
	public void revalidate() {
	}

	@Override
	public void repaint(long tm, int x, int y, int width, int height) {
	}

	@Override
	public void repaint(Rectangle r) {
	}

	@Override
	public void repaint() {
	}

	private Icon getLabelIcon(JList list, Content content) {
		return IconManager.getInstance().getIcon(content, DEFAULT_ICON_HEIGHT, list);
	}

	@Override
	protected void paintChildren(Graphics g) {
		// Force text anti aliasing on texts
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		super.paintChildren(g);
	}
}*/

/**
 * List model adaptor to CatalogPieceOfFurniture instances of catalog.
 */
private class FurnitureCatalogListModel
{//extends AbstractListModel {
	private FurnitureCatalog catalog;
	private List<CatalogPieceOfFurniture> furniture;
	private FurnitureCategory filterCategory;
	private String filterText;

	public FurnitureCatalogListModel(FurnitureCatalog catalog)
	{
		this.catalog = catalog;
		this.filterText = "";
		catalog.addFurnitureListener(new FurnitureCatalogListener(this));
	}

	public void setFilterCategory(FurnitureCategory filterCategory)
	{
		this.filterCategory = filterCategory;
		resetFurnitureList();
	}

	public void setFilterText(String filterText)
	{
		this.filterText = filterText;
		resetFurnitureList();
	}

	public Object getElementAt(int index)
	{
		checkFurnitureList();
		return this.furniture.get(index);
	}

	public int getSize()
	{
		checkFurnitureList();
		return this.furniture.size();
	}

	private void resetFurnitureList()
	{
		if (this.furniture != null)
		{
			this.furniture = null;
			EventQueue.invokeLater(new Runnable()
			{
				public void run()
				{
					//fireContentsChanged(this, -1, -1);
					// called in cases like where language changes or perhaps catalog imported
					gridView.setAdapter(new ImageAdapter(FurnitureCatalogListPanel.this.getContext()));
					FurnitureCatalogListPanel.this.getView().postInvalidate();
				}
			});
		}
	}

	private void checkFurnitureList()
	{
		if (this.furniture == null)
		{
			this.furniture = new ArrayList<CatalogPieceOfFurniture>();
			this.furniture.clear();
			for (FurnitureCategory category : this.catalog.getCategories())
			{
				for (CatalogPieceOfFurniture piece : category.getFurniture())
				{
					if ((this.filterCategory == null
							|| piece.getCategory().equals(this.filterCategory))
							&& piece.matchesFilter(this.filterText))
					{
						furniture.add(piece);
					}
				}
			}
			Collections.sort(this.furniture);
		}
	}
}
	/**
	 * Catalog furniture listener bound to this list model with a weak reference to avoid
	 * strong link between catalog and this list.
	 */
	private static class FurnitureCatalogListener implements CollectionListener<CatalogPieceOfFurniture>
	{
		private WeakReference<FurnitureCatalogListModel> listModel;

		public FurnitureCatalogListener(FurnitureCatalogListModel catalogListModel) {
			this.listModel = new WeakReference<FurnitureCatalogListModel>(catalogListModel);
		}

		public void collectionChanged(CollectionEvent<CatalogPieceOfFurniture> ev) {
			// If catalog list model was garbage collected, remove this listener from catalog
			FurnitureCatalogListModel listModel = this.listModel.get();
			FurnitureCatalog catalog = (FurnitureCatalog)ev.getSource();
			if (listModel == null) {
				catalog.removeFurnitureListener(this);
			} else {
				listModel.resetFurnitureList();
			}
		}
	}
}

