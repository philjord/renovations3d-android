/*
 * HomeStreamRecorder.java 29 aout 2021
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
package com.eteks.sweethome3d.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import com.eteks.sweethome3d.model.DamagedHomeRecorderException;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomeRecorder;
import com.eteks.sweethome3d.model.InterruptedRecorderException;
import com.eteks.sweethome3d.model.NotEnoughSpaceRecorderException;
import com.eteks.sweethome3d.model.RecorderException;
import com.eteks.sweethome3d.model.UserPreferences;
import com.eteks.sweethome3d.tools.OperatingSystem;

/**
 * Recorder that stores and reads homes in streams with {@link DefaultHomeOutputStream} and
 * {@link DefaultHomeInputStream}.
 * @author Emmanuel Puybaret and Philip Jordan
 */
public class HomeStreamRecorder implements HomeRecorder {
  private final int             compressionLevel;
  private final boolean         includeOnlyTemporaryContent;
  private final UserPreferences preferences;
  private final boolean         preferPreferencesContent;
  private final boolean         preferXmlEntry;
  private final boolean         acceptUrl;

  /**
   * Creates a home recorder able to write and read homes in uncompressed files.
   */
  public HomeStreamRecorder() {
    this(0);
  }

  /**
   * Creates a home recorder able to write and read homes in files compressed
   * at a level from 0 to 9.
   * @param compressionLevel 0 (uncompressed) to 9 (compressed).
   */
  public HomeStreamRecorder(int compressionLevel) {
    this(compressionLevel, false);
  }

  /**
   * Creates a home recorder able to write and read homes in files compressed
   * at a level from 0 to 9.
   * @param compressionLevel 0-9
   * @param includeOnlyTemporaryContent if <code>true</code>, content instances of
   *            <code>TemporaryURLContent</code> class referenced by the saved home
   *            as well as the content previously saved with it will be written.
   *            If <code>false</code>, all the content instances
   *            referenced by the saved home will be written in the zip stream.
   */
  public HomeStreamRecorder(int     compressionLevel,
                            boolean includeOnlyTemporaryContent) {
    this(compressionLevel, includeOnlyTemporaryContent, null, false);
  }

  /**
   * Creates a home recorder able to write and read homes in files compressed
   * at a level from 0 to 9.
   * @param compressionLevel 0-9
   * @param includeOnlyTemporaryContent if <code>true</code>, content instances of
   *            <code>TemporaryURLContent</code> class referenced by the saved home
   *            as well as the content previously saved with it will be written.
   *            If <code>false</code>, all the content instances
   *            referenced by the saved home will be written in the zip stream.
   * @param preferences If not <code>null</code>, the furniture and textures contents
   *            it references might be used to replace the one of read homes
   *            when they are equal.
   * @param preferPreferencesContent If <code>true</code>, the furniture and textures contents
   *            referenced by <code>preferences</code> will replace the one of read homes
   *            as often as possible when they are equal. Otherwise, these contents will be
   *            used only to replace damaged content that might be found in read home files.
   */
  public HomeStreamRecorder(int             compressionLevel,
                            boolean         includeOnlyTemporaryContent,
                            UserPreferences preferences,
                            boolean         preferPreferencesContent) {
    this(compressionLevel, includeOnlyTemporaryContent, preferences, preferPreferencesContent, false);
  }

  /**
   * Creates a home recorder able to write and read homes in files compressed
   * at a level from 0 to 9.
   * @param compressionLevel 0-9
   * @param includeOnlyTemporaryContent if <code>true</code>, content instances of
   *            <code>TemporaryURLContent</code> class referenced by the saved home
   *            as well as the content previously saved with it will be written.
   *            If <code>false</code>, all the content instances
   *            referenced by the saved home will be written in the zip stream.
   * @param preferences If not <code>null</code>, the furniture and textures contents
   *            it references might be used to replace the one of read homes
   *            when they are equal.
   * @param preferPreferencesContent If <code>true</code>, the furniture and textures contents
   *            referenced by <code>preferences</code> will replace the one of read homes
   *            as often as possible when they are equal. Otherwise, these contents will be
   *            used only to replace damaged content that might be found in read home files.
   * @param preferXmlEntry If <code>true</code>, an additional <code>Home.xml</code> entry
   *            will be saved in files and read in priority from saved files.
   */
  public HomeStreamRecorder(int             compressionLevel,
                            boolean         includeOnlyTemporaryContent,
                            UserPreferences preferences,
                            boolean         preferPreferencesContent,
                            boolean         preferXmlEntry) {
    this(compressionLevel, includeOnlyTemporaryContent, preferences, preferPreferencesContent, preferXmlEntry, false);
  }

  /**
   * Creates a home recorder able to write and read homes in files compressed
   * at a level from 0 to 9.
   * @param compressionLevel 0-9
   * @param includeOnlyTemporaryContent if <code>true</code>, content instances of
   *            <code>TemporaryURLContent</code> class referenced by the saved home
   *            as well as the content previously saved with it will be written.
   *            If <code>false</code>, all the content instances
   *            referenced by the saved home will be written in the zip stream.
   * @param preferences If not <code>null</code>, the furniture and textures contents
   *            it references might be used to replace the one of read homes
   *            when they are equal.
   * @param preferPreferencesContent If <code>true</code>, the furniture and textures contents
   *            referenced by <code>preferences</code> will replace the one of read homes
   *            as often as possible when they are equal. Otherwise, these contents will be
   *            used only to replace damaged content that might be found in read home files.
   * @param preferXmlEntry If <code>true</code>, an additional <code>Home.xml</code> entry
   *            will be saved in files and read in priority from saved files.
   * @param acceptUrl If <code>true</code>, this recorder will try to read a home from a URL
   *            if the path passed as parameter to {@link #readHome(String) readHome} isn't a file.
   */
  public HomeStreamRecorder(int             compressionLevel,
                            boolean         includeOnlyTemporaryContent,
                            UserPreferences preferences,
                            boolean         preferPreferencesContent,
                            boolean         preferXmlEntry,
                            boolean         acceptUrl) {
    this.compressionLevel = compressionLevel;
    this.includeOnlyTemporaryContent = includeOnlyTemporaryContent;
    this.preferences = preferences;
    this.preferPreferencesContent = preferPreferencesContent;
    this.preferXmlEntry = preferXmlEntry;
    this.acceptUrl = acceptUrl;
  }

  /**
   * Writes home data.
   * @throws RecorderException if a problem occurred while writing home.
   */
  public void writeHome(Home home, OutputStream out) throws RecorderException {
    DefaultHomeOutputStream homeOut = null;
    try {
      homeOut = new DefaultHomeOutputStream(out,
          this.compressionLevel,
          this.includeOnlyTemporaryContent
              ? ContentRecording.INCLUDE_TEMPORARY_CONTENT
              : ContentRecording.INCLUDE_ALL_CONTENT,
          true,
          this.preferXmlEntry
              ? getHomeXMLExporter()
              : null);
      // Write home with HomeOuputStream
      homeOut.writeHome(home);
    } catch (InterruptedIOException ex) {
      throw new InterruptedRecorderException("Save " + home.getName() + " interrupted");
    } catch (IOException ex) {
      throw new RecorderException("Can't save home " + home.getName(), ex);
    } finally {
      try {
        if (homeOut != null) {
          homeOut.close();
        }
      } catch (IOException ex) {
        throw new RecorderException("Can't close stream " + home.getName(), ex);
      }
    }
  }

  /**
   * Returns an exporter able to generate the content of a <code>Home.xml</code> entry.
   */
  protected HomeXMLExporter getHomeXMLExporter() {
    return new HomeXMLExporter();
  }

  /**
   * Returns a home instance read from its file <code>name</code> or an URL if it can be opened as a file.
   * @throws RecorderException if a problem occurred while reading home,
   *   or if file or URL <code>name</code> doesn't exist.
   */
  public Home readHome(InputStream in) throws RecorderException {
    DefaultHomeInputStream homeInputStream = null;
    try {
      // Read home with HomeInputStream
      homeInputStream = new DefaultHomeInputStream(in, ContentRecording.INCLUDE_ALL_CONTENT,
          this.preferXmlEntry ? getHomeXMLHandler() : null,
          this.preferences, this.preferPreferencesContent);
      Home home = homeInputStream.readHome();
      return home;
    } catch (InterruptedIOException ex) {
      throw new InterruptedRecorderException("Read " + "stream" + " interrupted");
    } catch (DamagedHomeIOException ex) {
      throw new DamagedHomeRecorderException(ex.getDamagedHome(), ex.getInvalidContent());
    } catch (IOException ex) {
      throw new RecorderException("Can't read home from " + "stream", ex);
    } catch (ClassNotFoundException ex) {
      throw new RecorderException("Missing classes to read home from " + "stream", ex);
    } finally {
      try {
        if (homeInputStream != null) {
          homeInputStream.close();
        }
      } catch (IOException ex) {
        throw new RecorderException("Can't close " + "stream", ex);
      }
    }
  }

  /**
   * Returns a SAX XML handler able to interpret the information contained in the
   * <code>Home.xml</code> entry.
   */
  protected HomeXMLHandler getHomeXMLHandler() {
    return new HomeXMLHandler(this.preferences);
  }



  //PJPJPJ added to implement interface HomeRecorder, but not applicable for Streamed
  @Override
  public void writeHome(Home home, String s) throws RecorderException {
    throw new UnsupportedOperationException();
  }

  @Override
  public Home readHome(String s) throws RecorderException {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean exists(String s) throws RecorderException {
    throw new UnsupportedOperationException();
  }
}
