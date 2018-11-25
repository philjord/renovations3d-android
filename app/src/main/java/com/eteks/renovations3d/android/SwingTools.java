/*
 * SwingTools.java 21 oct. 2008
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
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.View;

import com.mindblowing.swingish.JComponent;
import com.mindblowing.swingish.JRadioButton;
import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.model.Polyline;
import com.eteks.sweethome3d.model.TextureImage;
import com.eteks.sweethome3d.model.UserPreferences;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javaawt.BasicStroke;
import javaawt.Color;
import javaawt.Dimension;
import javaawt.Graphics2D;
import javaawt.Image;
import javaawt.Stroke;
import javaawt.image.BufferedImage;
import javaawt.imageio.ImageIO;
import javaxswing.ImageIcon;



/**
 * Gathers some useful tools for Swing.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class SwingTools {
	// Borders for focused views
	//private static Border unfocusedViewBorder;
	//private static Border focusedViewBorder;

	private SwingTools() {
		// This class contains only tools
	}

	public static Point convertPointToScreen(javaawt.Point p, JComponent c)
	{
		int[] fromCoord = new int[2];
		if(c.getView()!=null)
		c.getView().getLocationOnScreen(fromCoord);

		Point toPoint = new Point(fromCoord[0] + p.x,
				fromCoord[1] + p.y);

		return toPoint;
	}


	public static Point convertPoint(JComponent from, int x, int y, JComponent to) {
		int[] fromCoord = new int[2];
		int[] toCoord = new int[2];
		if(from.getView()!=null)
		from.getView().getLocationOnScreen(fromCoord);
		if(to.getView()!=null)
		to.getView().getLocationOnScreen(toCoord);

		return new Point(fromCoord[0] - toCoord[0] + x,
				fromCoord[1] - toCoord[1] + y);
	}

	public static Rect convertRect(Rect fromRect, View fromView, View toView) {
		int[] fromCoord = new int[2];
		int[] toCoord = new int[2];
		if(fromView!=null)
		fromView.getLocationOnScreen(fromCoord);
		if(toView!=null)
		toView.getLocationOnScreen(toCoord);

		int xShift = fromCoord[0] - toCoord[0];
		int yShift = fromCoord[1] - toCoord[1];

		return new Rect(fromRect.left + xShift, fromRect.top + yShift,
				fromRect.right + xShift, fromRect.bottom + yShift);
	}



	/**
	 * Updates the Swing resource bundles in use from the default Locale and class loader.
	 */
  public static void updateSwingResourceLanguage() {
		updateSwingResourceLanguage(Arrays.asList(new ClassLoader[]{SwingTools.class.getClassLoader()}), null);
	}

	/**
	 * Updates the Swing resource bundles in use from the preferences Locale and the class loaders of preferences.
	 */
  public static void updateSwingResourceLanguage(UserPreferences preferences) {
		updateSwingResourceLanguage(preferences.getResourceClassLoaders(), preferences.getLanguage());
	}

	/**
	 * Updates the Swing resource bundles in use from the preferences Locale and class loaders.
	 */
	private static void updateSwingResourceLanguage(List<ClassLoader> classLoaders,
                                                  String language) {
		// Clear resource cache
/*		UIManager.getDefaults().removeResourceBundle(null);
		UIManager.getDefaults().setDefaultLocale(Locale.getDefault());
		// Read Swing localized properties because Swing doesn't update its internal strings automatically
		// when default Locale is updated (see bug http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4884480)
		updateSwingResourceBundle("com.sun.swing.internal.plaf.metal.resources.metal", classLoaders, language);
		updateSwingResourceBundle("com.sun.swing.internal.plaf.basic.resources.basic", classLoaders, language);
		if (UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.gtk.GTKLookAndFeel")) {
			updateSwingResourceBundle("com.sun.java.swing.plaf.gtk.resources.gtk", classLoaders, language);
		} else if (UIManager.getLookAndFeel().getClass().getName().equals("com.sun.java.swing.plaf.motif.MotifLookAndFeel")) {
			updateSwingResourceBundle("com.sun.java.swing.plaf.motif.resources.motif", classLoaders, language);
		}*/
	}

	/**
	 * Updates a Swing resource bundle in use from the current Locale.
	 */
	private static void updateSwingResourceBundle(String swingResource,
												  List<ClassLoader> classLoaders,
												  String language) {
/*		ResourceBundle resource;
		try {
			Locale defaultLocale = language == null
					? Locale.getDefault()
					: (language.indexOf('_') == -1
					? new Locale(language)
					: new Locale(language.substring(0, 2), language.substring(3, 5)));
			resource = ResourceBundle.getBundle(swingResource, defaultLocale);
			for (ClassLoader classLoader : classLoaders) {
				ResourceBundle bundle = ResourceBundle.getBundle(swingResource, defaultLocale, classLoader);
				if (defaultLocale.equals(bundle.getLocale())) {
					resource = bundle;
					break;
				} else if (!resource.getLocale().getLanguage().equals(bundle.getLocale().getLanguage())
						&& defaultLocale.getLanguage().equals(bundle.getLocale().getLanguage())) {
					resource = bundle;
					// Don't break in case a bundle with language + country is found with an other class loader
				}
			}
		} catch (MissingResourceException ex) {
			resource = ResourceBundle.getBundle(swingResource, Locale.ENGLISH);
		}

		// Update UIManager properties
		final String textAndMnemonicSuffix = ".textAndMnemonic";
		for (Enumeration<?> it = resource.getKeys(); it.hasMoreElements(); ) {
			String key = (String)it.nextElement();
			if (key.endsWith(textAndMnemonicSuffix)) {
				String value = resource.getString(key);
				UIManager.put(key, value);
				// Decompose property value like in javax.swing.UIDefaults.TextAndMnemonicHashMap because
				// UIDefaults#getResourceCache(Locale) doesn't store the correct localized value for non English resources
				// (.textAndMnemonic suffix appeared with Java 1.7)
				String text = value.replace("&", "");
				String keyPrefix = key.substring(0, key.length() - textAndMnemonicSuffix.length());
				UIManager.put(keyPrefix + "NameText", text);
				UIManager.put(keyPrefix + "Text", text);
				int index = value.indexOf('&');
				if (index >= 0 && index < value.length() - 1) {
					UIManager.put(key.replace(textAndMnemonicSuffix, "Mnemonic"),
							String.valueOf(Character.toUpperCase(value.charAt(index + 1))));
				}
			}
		}
		// Store other properties coming from read resource and give them a higher priority if already set in previous loop
		for (Enumeration<?> it = resource.getKeys(); it.hasMoreElements(); ) {
			String key = (String)it.nextElement();
			if (!key.endsWith(textAndMnemonicSuffix)) {
				UIManager.put(key, resource.getString(key));
			}
		}*/
	}

	/**
	 * Returns a localized text for menus items and labels depending on the system.
	 */
	public static String getLocalizedLabelText(UserPreferences preferences,
											   Class<?> resourceClass,
											   String resourceKey,
											   Object... resourceParameters) {
		String localizedString = preferences.getLocalizedString(resourceClass, resourceKey, resourceParameters);
		// Under Mac OS X, remove bracketed upper case roman letter used in oriental languages to indicate mnemonic
	/*	String language = Locale.getDefault().getLanguage();
		if (OperatingSystem.isMacOSX()
				&& (language.equals(Locale.CHINESE.getLanguage())
				|| language.equals(Locale.JAPANESE.getLanguage())
				|| language.equals(Locale.KOREAN.getLanguage())
				|| language.equals("uk"))) {  // Ukrainian
			int openingBracketIndex = localizedString.indexOf('(');
			if (openingBracketIndex != -1) {
				int closingBracketIndex = localizedString.indexOf(')');
				if (openingBracketIndex == closingBracketIndex - 2) {
					char c = localizedString.charAt(openingBracketIndex + 1);
					if (c >= 'A' && c <= 'Z') {
						localizedString = localizedString.substring(0, openingBracketIndex)
								+ localizedString.substring(closingBracketIndex + 1);
					}
				}
			}
		}*/
		return localizedString;
	}



	/**
	 * Forces radio buttons to be deselected even if they belong to a button group.
	 */
	public static void deselectAllRadioButtons(JRadioButton... radioButtons) {
		for (JRadioButton radioButton : radioButtons) {
			if (radioButton != null) {
				//TODO: my radio button don't know their groups
				radioButton.setChecked(false);
				//ButtonGroup group = ((JToggleButton.ToggleButtonModel)radioButton.getModel()).getGroup();
				//group.remove(radioButton);
				//radioButton.setSelected(false);
				//group.add(radioButton);
			}
		}
	}

	/**
	 * Displays <code>messageComponent</code> in a modal dialog box, giving focus to one of its components.
	 */
/*	public static int showConfirmDialog(JComponent parentComponent,
										JComponent messageComponent,
										String title,
										final JComponent focusedComponent) {
		JOptionPane optionPane = new JOptionPane(messageComponent,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION);
		parentComponent = SwingUtilities.getRootPane(parentComponent);
		if (parentComponent != null) {
			optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
		}
		final JDialog dialog = optionPane.createDialog(parentComponent, title);
		if (focusedComponent != null) {
			// Add a listener that transfer focus to focusedComponent when dialog is shown
			dialog.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent ev) {
					requestFocusInWindow(focusedComponent);
					dialog.removeComponentListener(this);
				}
			});
		}
		dialog.setVisible(true);

		dialog.dispose();
		Object value = optionPane.getValue();
		if (value instanceof Integer) {
			return (Integer)value;
		} else {
			return JOptionPane.CLOSED_OPTION;
		}
	}*/

	/**
	 * Requests the focus for the given component.
	 */
/*	public static void requestFocusInWindow(final JComponent focusedComponent) {
		if (!focusedComponent.requestFocusInWindow()) {
			// Prefer to call requestFocusInWindow in a timer with a small delay
			// than calling it with EnventQueue#invokeLater to ensure it always works
			new Timer(50, new ActionListener() {
				public void actionPerformed(ActionEvent ev) {
					focusedComponent.requestFocusInWindow();
					((Timer)ev.getSource()).stop();
				}
			}).start();
		}
	}*/

	/**
	 * Displays <code>messageComponent</code> in a modal dialog box, giving focus to one of its components.
	 */
/*	public static void showMessageDialog(JComponent parentComponent,
										 JComponent messageComponent,
										 String title,
										 int messageType,
										 final JComponent focusedComponent) {
		JOptionPane optionPane = new JOptionPane(messageComponent, messageType, JOptionPane.DEFAULT_OPTION);
		parentComponent = SwingUtilities.getRootPane(parentComponent);
		if (parentComponent != null) {
			optionPane.setComponentOrientation(parentComponent.getComponentOrientation());
		}
		final JDialog dialog = optionPane.createDialog(parentComponent, title);
		if (focusedComponent != null) {
			// Add a listener that transfer focus to focusedComponent when dialog is shown
			dialog.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentShown(ComponentEvent ev) {
					requestFocusInWindow(focusedComponent);
					dialog.removeComponentListener(this);
				}
			});
		}
		dialog.setVisible(true);
		dialog.dispose();
	}*/

	private static Map<TextureImage, BufferedImage> patternImages;

	/**
	 * Returns the image matching a given pattern.
	 */
	public static BufferedImage getPatternImage(TextureImage pattern,
												Color backgroundColor,
												Color foregroundColor) {
		if (patternImages == null) {
			patternImages = new HashMap<TextureImage, BufferedImage>();
		}
		BufferedImage image = new BufferedImage(
				(int) pattern.getWidth(), (int) pattern.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics2D imageGraphics = (Graphics2D) image.getGraphics();
		imageGraphics.setColor(backgroundColor);
		imageGraphics.fillRect(0, 0, image.getWidth(), image.getHeight());
		// Get pattern image from cache
		BufferedImage patternImage = patternImages.get(pattern);
		if (patternImage == null) {
			try {
				InputStream imageInput = pattern.getImage().openStream();
				patternImage = ImageIO.read(imageInput);
				imageInput.close();
				patternImages.put(pattern, patternImage);
			} catch (IOException ex) {
				throw new IllegalArgumentException("Can't read pattern image " + pattern.getName());
			}
		}
		// Draw the pattern image with foreground color
		final int foregroundColorRgb = foregroundColor.getRGB() & 0xFFFFFF;
		//PJPJPJ not sure what this was but disabled for now
	/*	imageGraphics.drawImage(Toolkit.getDefaultToolkit().createImage(
				new FilteredImageSource(patternImage.getSource(),
						new RGBImageFilter() {
							{
								this.canFilterIndexColorModel = true;
							}

							@Override
							public int filterRGB(int x, int y, int argb) {
								// Always use foreground color and alpha
								return (argb & 0xFF000000) | foregroundColorRgb;
							}
						})), 0, 0, null);*/
		imageGraphics.drawImage(patternImage, 0, 0, null);
		imageGraphics.dispose();
		return image;
	}

	/**
	 * Returns the border of a component where a user may drop objects.
	 */
/*	public static Border getDropableComponentBorder() {
		Border border = null;
		if (OperatingSystem.isMacOSXLeopardOrSuperior()) {
			border = UIManager.getBorder("InsetBorder.aquaVariant");
		}
		if (border == null) {
			border = BorderFactory.createLoweredBevelBorder();
		}
		return border;
	}*/

	/**
	 * Displays the image referenced by <code>imageUrl</code> in an AWT window
	 * disposed once an instance of <code>JFrame</code> or <code>JDialog</code> is displayed.
	 * If the <code>imageUrl</code> is incorrect, nothing happens.
	 */
	public static void showSplashScreenWindow(URL imageUrl) {
	}

	/**
	 * Returns a new panel with a border and the given <code>title</code>
	 */

	/**
	 * Returns a scroll pane containing the given <code>component</code>
	 * that always displays scroll bars under Mac OS X.
	 */

	/**
	 * Returns a scroll bar adjustment listener bound to the given <code>scrollPane</code> view
	 * that updates view tool tip when its vertical scroll bar is adjusted.
	 */

	/**
	 * Returns <code>true</code> if a tool tip is showing.
	 */

	/**
	 * Adds a listener that will update the given popup menu to hide disabled menu items.
	 */

	/**
	 * A popup menu listener that displays only enabled menu items.
	 */

	/**
	 * Makes useless menu items invisible.
	 */

	/**
	 * Makes useless separators invisible.
	 */

	/**
	 * Returns <code>true</code> if the given <code>menu</code> contains
	 * at least one enabled menu item.
	 */

	/**
	 * Attempts to display the given <code>url</code> in a browser and returns <code>true</code>
	 * if it was done successfully.
	 */
	public static boolean showDocumentInBrowser(URL url) {
		return false; //PJPJPJPJ TODO: showDocumentInBrowser fully possible on Android
//    return BrowserSupport.showDocumentInBrowser(url);
	}

	/**
	 * Separated static class to be able to exclude JNLP library from classpath.
	 */
/*  private static class BrowserSupport {
	public static boolean showDocumentInBrowser(URL url) {
      try {
        // Lookup the javax.jnlp.BasicService object
        BasicService basicService = (BasicService)ServiceManager.lookup("javax.jnlp.BasicService");
        // Ignore the basic service, if it doesn't support web browser
        if (basicService.isWebBrowserSupported()) {
          return basicService.showDocument(url);
        }
      } catch (UnavailableServiceException ex) {
        // Too bad : service is unavailable
      } catch (LinkageError ex) {
        // JNLP classes not available in classpath
        System.err.println("Can't show document in browser. JNLP classes not available in classpath.");
      }
      return false;
    }
  }*/

	/**
	 * Returns the children of a component of the given class.
	 */
/*	public static <T extends Component> List<T> findChildren(JComponent parent, Class<T> childrenClass) {
		List<T> children = new ArrayList<T>();
		findChildren(parent, childrenClass, children);
		return children;
	}

	private static <T extends Component> void findChildren(JComponent parent, Class<T> childrenClass, List<T> children) {
		for (int i = 0; i < parent.getComponentCount(); i++) {
			Component child = parent.getComponent(i);
			if (childrenClass.isInstance(child)) {
				children.add((T)child);
			} else if (child instanceof JComponent) {
				findChildren((JComponent)child, childrenClass, children);
			}
		}
	}*/

	/**
	 * Returns <code>true</code> if the given rectangle is fully visible at screen.
	 */
/*	public static boolean isRectangleVisibleAtScreen(Rectangle rectangle) {
		Area devicesArea = new Area();
		GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		for (GraphicsDevice device : environment.getScreenDevices()) {
			devicesArea.add(new Area(device.getDefaultConfiguration().getBounds()));
		}
		return devicesArea.contains(rectangle);
	}*/

	/**
	 * Returns a new custom cursor.
	 */
/*	public static Cursor createCustomCursor(URL smallCursorImageUrl,
											URL largeCursorImageUrl,
											float xCursorHotSpot,
											float yCursorHotSpot,
											String cursorName,
											Cursor defaultCursor) {
		if (GraphicsEnvironment.isHeadless()) {
			return defaultCursor;
		}
		// Retrieve system cursor size
		Dimension cursorSize = Toolkit.getDefaultToolkit().getBestCursorSize(16, 16);
		URL cursorImageResource;
		// If returned cursor size is 0, system doesn't support custom cursor
		if (cursorSize.width == 0) {
			return defaultCursor;
		} else {
			// Use a different cursor image depending on system cursor size
			if (cursorSize.width > 16) {
				cursorImageResource = largeCursorImageUrl;
			} else {
				cursorImageResource = smallCursorImageUrl;
			}
			try {
				// Read cursor image
				BufferedImage cursorImage = ImageIO.read(cursorImageResource);
				// Create custom cursor from image
				return Toolkit.getDefaultToolkit().createCustomCursor(cursorImage,
						new Point(Math.min(cursorSize.width - 1, Math.round(cursorSize.width * xCursorHotSpot)),
								Math.min(cursorSize.height - 1, Math.round(cursorSize.height * yCursorHotSpot))),
						cursorName);
			} catch (IOException ex) {
				throw new IllegalArgumentException("Unknown resource " + cursorImageResource);
			}
		}
	}*/

	/**
	 * Returns <code>image</code> size in pixels.
	 * @return the size or <code>null</code> if the information isn't given in the meta data of the image
	 */
	public static Dimension getImageSizeInPixels(Content image) throws IOException {
		InputStream in = null;
		try {
			in = image.openStream();
			//http://stackoverflow.com/questions/12018620/getting-a-bitmaps-dimensions-in-android-without-reading-the-entire-file
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, options);
			int imageHeight = options.outHeight;
			int imageWidth = options.outWidth;
			return new Dimension(imageWidth, imageHeight);

		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * Returns the line stroke matching the given line styles.
	 */
	public static Stroke getStroke(float thickness,
								   Polyline.CapStyle capStyle,
								   Polyline.JoinStyle joinStyle,
								   Polyline.DashStyle dashStyle) {
		int strokeCapStyle;
		switch (capStyle) {
			case ROUND :
				strokeCapStyle = BasicStroke.CAP_ROUND;
				break;
			case SQUARE :
				strokeCapStyle = BasicStroke.CAP_SQUARE;
				break;
			default:
				strokeCapStyle = BasicStroke.CAP_BUTT;
				break;
		}

		int strokeJoinStyle;
		switch (joinStyle) {
			case ROUND :
			case CURVED :
				strokeJoinStyle = BasicStroke.JOIN_ROUND;
				break;
			case BEVEL :
				strokeJoinStyle = BasicStroke.JOIN_BEVEL;
				break;
			default:
				strokeJoinStyle = BasicStroke.JOIN_MITER;
				break;
		}

		float [] strokeDashes;
		switch (dashStyle) {
			case DOT :
				strokeDashes = new float [] {thickness, thickness};
				break;
			case DASH :
				strokeDashes = new float [] {thickness * 4, thickness * 2};
				break;
			case DASH_DOT :
				strokeDashes = new float [] {thickness * 8, thickness * 2, thickness * 2, thickness * 2};
				break;
			case DASH_DOT_DOT :
				strokeDashes = new float [] {thickness * 8, thickness * 2, thickness * 2, thickness * 2, thickness * 2, thickness * 2};
				break;
			default :
				strokeDashes = null;
				break;
		}

		return new BasicStroke(thickness, strokeCapStyle, strokeJoinStyle, 10, strokeDashes, 0);
	}

	/**
	 * Updates Swing components default size according to resolution scale.
	 */

	public static void setResolutionScale(Activity activity) {
		resolutionScale = 1f;//TODO: put back activity.getResources().getDisplayMetrics().density;
	}

		private static float resolutionScale = 1f;

	/**
	 * Returns a scale factor used to adapt user interface items to screen resolution.
	 */
	public static float getResolutionScale() {
		return resolutionScale;
		/*String resolutionScaleProperty = System.getProperty("com.eteks.sweethome3d.resolutionScale");
		if (resolutionScaleProperty != null) {
			try {
				return Float.parseFloat(resolutionScaleProperty.trim());
			} catch (NumberFormatException ex) {
				// Ignore resolution
			}
		}

		return 1f;*/
	}

	/**
	 * Returns an image icon scaled according to the value returned by {@link #getResolutionScale()}.
	 */
	public static ImageIcon getScaledImageIcon(URL imageUrl) {
		float resolutionScale = getResolutionScale();
		if (resolutionScale == 1) {
			return new ImageIcon(imageUrl);
		} else {
			try {
				BufferedImage image = ImageIO.read(imageUrl.openStream());
				Image scaledImage = image.getScaledInstance(Math.round(image.getWidth() * resolutionScale),
						Math.round(image.getHeight() * resolutionScale), 0);//Image.SCALE_SMOOTH);
				return new ImageIcon(scaledImage);
			} catch (IOException ex) {
				return null;
			}
		}
	}
}
