package com.eteks.renovations3d.android;
/*
 * IconManager.java 2 mai 2006
 *
 * Sweet Home 3D, Copyright (c) 2006 Emmanuel PUYBARET / eTeks <info@eteks.com>
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

import android.util.SparseArray;

import java.util.concurrent.Executors;
import javaawt.Graphics;
import javaawt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;

import javaawt.imageio.ImageIO;
import javaxswing.Icon;
import javaxswing.ImageIcon;

import com.eteks.sweethome3d.model.Content;
import com.eteks.sweethome3d.tools.ResourceURLContent;
import com.mindblowing.swingish.JComponent;

/**
 * Singleton managing icons cache.
 * @author Emmanuel Puybaret
 */
public class IconManager {
	private static IconManager                     instance;
	// Icon used if an image content couldn't be loaded
	private final Content                          errorIconContent;
	// Icon used while an image content is loaded
	private final Content                          waitIconContent;
	// Map storing loaded icons
	private final Map<Content, SparseArray<Icon>> icons;
	// Executor used by IconProxy to load images
	private ExecutorService                        iconsLoader;

	private IconManager() {
		// use a class from inside the jar and inside the swing package too
		this.errorIconContent = new ResourceURLContent(com.eteks.sweethome3d.swing.FileContentManager.class, "resources/error.png");
		this.waitIconContent = new ResourceURLContent(com.eteks.sweethome3d.swing.FileContentManager.class, "resources/wait.png");
		this.icons = Collections.synchronizedMap(new WeakHashMap<Content, SparseArray<Icon>>());
	}

	/**
	 * Returns an instance of this singleton.
	 */
	public static IconManager getInstance() {
		if (instance == null) {
			instance = new IconManager();
		}
		return instance;
	}

	/**
	 * Clears the loaded resources cache and shutdowns the multithreaded service
	 * that loads icons.
	 */
	public void clear() {
		if (this.iconsLoader != null) {
			this.iconsLoader.shutdownNow();
			this.iconsLoader = null;
		}
		this.icons.clear();
	}

	/**
	 * Returns the icon displayed for wrong content resized at a given height.
	 */
	public Icon getErrorIcon(int height) {
		Icon icon = getIcon(this.errorIconContent, height, null);
		if(icon instanceof TypedImageIcon)
			((TypedImageIcon)icon).isErrorIcon = true;
		return icon;
	}

	/**
	 * Returns the icon displayed for wrong content.
	 */
	public Icon getErrorIcon() {
		Icon icon = getIcon(this.errorIconContent, -1, null);
		if(icon instanceof TypedImageIcon)
			((TypedImageIcon)icon).isErrorIcon = true;
		return icon;
	}

	/**
	 * Returns <code>true</code> if the given <code>icon</code> is the error icon
	 * used by this manager to indicate it couldn't load an icon.
	 */
	//PJPJPJ this was a very expensive call on android
	public boolean isErrorIcon(Icon icon) {
		return (icon instanceof TypedImageIcon && ((TypedImageIcon)icon).isErrorIcon);
	}

	/**
	 * Returns the icon displayed while a content is loaded resized at a given height.
	 */
	public Icon getWaitIcon(int height) {
		Icon icon = getIcon(this.waitIconContent, height, null);
		if(icon instanceof TypedImageIcon)
			((TypedImageIcon)icon).isWaitIcon = true;
		return icon;
	}

	/**
	 * Returns the icon displayed while a content is loaded.
	 */
	public Icon getWaitIcon() {
		Icon icon = getIcon(this.waitIconContent, -1, null);
		if(icon instanceof TypedImageIcon)
			((TypedImageIcon)icon).isWaitIcon = true;
		return icon;
	}

	/**
	 * Returns <code>true</code> if the given <code>icon</code> is the wait icon
	 * used by this manager to indicate it's currently loading an icon.
	 */
	public boolean isWaitIcon(Icon icon) {
		return (icon instanceof TypedImageIcon && ((TypedImageIcon)icon).isWaitIcon);
	}

	/**
	 * Returns an icon read from <code>content</code>.
	 * @param content an object containing an image
	 * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
	 *            be read immediately in the current thread.
	 */
	public Icon getIcon(Content content, JComponent waitingComponent) {
		return getIcon(content, -1, waitingComponent);
	}

	/**
	 * Returns an icon read from <code>content</code> and rescaled at a given <code>height</code>.
	 * @param content an object containing an image
	 * @param height  the desired height of the returned icon
	 * @param waitingComponent a waiting component. If <code>null</code>, the returned icon will
	 *            be read immediately in the current thread.
	 */
	public Icon getIcon(Content content, final int height, JComponent waitingComponent) {
		SparseArray<Icon> contentIcons = this.icons.get(content);
		if (contentIcons == null) {
			contentIcons =  new SparseArray<Icon>();
			this.icons.put(content, contentIcons);
		}
		Icon icon = contentIcons.get(height);
		if (icon == null) {
			// Tolerate null content
			if (content == null) {
				icon = new Icon() {
					public void paintIcon(Object c, Graphics g, int x, int y) {
					}

					public int getIconWidth() {
						return Math.max(0, height);
					}

					public int getIconHeight() {
						return Math.max(0, height);
					}
				};
			} else if (content == this.errorIconContent ||
					content == this.waitIconContent) {
				// Load error and wait icons immediately in this thread
				icon = createIcon(content, height, null);
			} else if (waitingComponent == null) {
				// Load icon immediately in this thread
				icon = createIcon(content, height,
						getIcon(this.errorIconContent, height, null));
			} else {
				// For content different from error icon and wait icon,
				// load it in a different thread with a virtual proxy
				icon = new IconProxy(content, height, waitingComponent,
						getIcon(this.errorIconContent, height, null),
						getIcon(this.waitIconContent, height, null));
			}
			// Store the icon in icons map
			contentIcons.put(height, icon);
		}
		return icon;
	}

	/**
	 * Returns an icon created and scaled from its content.
	 * @param content the content from which the icon image is read
	 * @param height  the desired height of the returned icon
	 * @param errorIcon the returned icon in case of error
	 */
	private Icon createIcon(Content content, int height, Icon errorIcon) {
		try {
			// Read the icon of the piece
			InputStream contentStream = content.openStream();
			BufferedImage image = ImageIO.read(contentStream);
			contentStream.close();
			if (image != null) {
				if (height != -1 && height != image.getHeight()) {
					int width = image.getWidth() * height / image.getHeight();
					// Create a scaled image not bound to original image to let the original image being garbage collected
					BufferedImage scaledImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
					Graphics g = scaledImage.getGraphics();
					g.drawImage(image.getScaledInstance(width, height, 0), 0, 0, null);
					g.dispose();
					return new TypedImageIcon(scaledImage);
				} else {
					return new TypedImageIcon(image);
				}
			}
		} catch (IOException ex) {
			// Too bad, we'll use errorIcon
		}
		return errorIcon;
	}

	public static class TypedImageIcon extends ImageIcon {
		public boolean isWaitIcon = false;
		public boolean isErrorIcon = false;

		public TypedImageIcon(BufferedImage image) {
			super(image);
		}
	}

	/**
	 * Proxy icon that displays a temporary icon while waiting
	 * image loading completion.
	 */
	private class IconProxy implements Icon {
		private Icon icon;

		public IconProxy(final Content content, final int height,
						 final JComponent waitingComponent,
						 final Icon errorIcon, Icon waitIcon) {
			this.icon = waitIcon;
			if (iconsLoader == null) {
				iconsLoader = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
			}
			// Load the icon in a different thread
			iconsLoader.execute(new Runnable () {
				public void run() {
					icon = createIcon(content, height, errorIcon);
					waitingComponent.repaint();
				}
			});
		}

		public int getIconWidth() {
			return this.icon.getIconWidth();
		}

		public int getIconHeight() {
			return this.icon.getIconHeight();
		}

		public void paintIcon(Object c, Graphics g, int x, int y) {
			this.icon.paintIcon(c, g, x, y);
		}

		public Icon getIcon() {
			return this.icon;
		}
	}
}
