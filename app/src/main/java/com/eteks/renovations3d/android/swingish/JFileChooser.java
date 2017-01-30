package com.eteks.renovations3d.android.swingish;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * TODO: must add cancel button that return a null file, but ignore for now
 */
public class JFileChooser
{
	private static final String PARENT_DIR = "..";

	private final Activity activity;
	private ListView list;
	private Dialog dialog;
	private File currentPath;

	// filter on file extension
	private String extension = null;

	// MUCH sexier system
	private FileFilter fileFilter = null;

	/**
	 * Note replaces current and therefore only last set is used (and you can't pick anyway!)
	 * @param filter
	 */
	public void addChoosableFileFilter(FileFilter filter)
	{
		fileFilter = filter;
	}

	public void setFileFilter(FileFilter filter)
	{
		fileFilter = filter;
	}

	public JFileChooser setExtension(String extension)
	{
		this.extension = (extension == null) ? null :
				extension.toLowerCase();
		return this;
	}

	// file selection event handling
	public interface FileSelectedListener
	{
		void fileSelected(File file);
	}

	public JFileChooser setFileListener(FileSelectedListener fileListener)
	{
		this.fileListener = fileListener;
		return this;
	}

	private FileSelectedListener fileListener;

	public JFileChooser(Activity activity)
	{
		this(activity, null);
	}

	public JFileChooser(Activity activity, File startFolder)
	{
		this.activity = activity;
		dialog = new Dialog(activity);
		list = new ListView(activity);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int which, long id)
			{
				String fileChosen = (String) list.getItemAtPosition(which);
				File chosenFile = getChosenFile(fileChosen);
				if (chosenFile.isDirectory())
				{
					refresh(chosenFile);
				}
				else
				{
					if (fileListener != null)
					{
						fileListener.fileSelected(chosenFile);
					}
					dialog.dismiss();
				}
			}
		});
		dialog.setContentView(list);
		dialog.getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		if (startFolder == null)
		{
			//this doesn't work no navigate from here
			//startFolder = Environment.getExternalStorageDirectory();

			startFolder = new File( System.getenv("EXTERNAL_STORAGE"));
		}
		else if (startFolder.isFile())
		{
			startFolder = startFolder.getParentFile();
		}
		refresh(startFolder);
	}

	public Dialog getDialog()
	{
		return dialog;
	}

	public void showDialog()
	{
		refresh();
		dialog.show();
	}
	public void refresh()
	{
		refresh(currentPath);
	}

	/**
	 * Sort, filter and display the files for the given path.
	 */
	private void refresh(File path)
	{
		this.currentPath = path;
		if (path.exists())
		{
			File[] dirs = path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					return (file.isDirectory() && file.canRead());
				}
			});
			File[] files = path.listFiles(new FileFilter()
			{
				@Override
				public boolean accept(File file)
				{
					if (!file.isDirectory())
					{
						if (!file.canRead())
						{
							return fileFilter !=null ? fileFilter.accept(file) : false;
						}
						else if (extension == null)
						{
							return fileFilter !=null ? fileFilter.accept(file) : true;
						}
						else
						{
							return file.getName().toLowerCase().endsWith(extension);
						}
					}
					else
					{
						return false;
					}
				}
			});

			// convert to an array
			int i = 0;
			String[] fileList;
			if (path.getParentFile() == null)
			{
				fileList = new String[dirs.length + files.length];
			}
			else
			{
				fileList = new String[dirs.length + files.length + 1];
				fileList[i++] = PARENT_DIR;
			}
			Arrays.sort(dirs);
			Arrays.sort(files);
			for (File dir : dirs)
			{
				fileList[i++] = dir.getName();
			}
			for (File file : files)
			{
				fileList[i++] = file.getName();
			}

			// refresh the user interface
			dialog.setTitle(currentPath.getPath());
			list.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, fileList)
			{
				@Override
				public View getView(int pos, View view, ViewGroup parent)
				{
					view = super.getView(pos, view, parent);
					((TextView) view).setSingleLine(true);
					return view;
				}
			});
		}
	}


	/**
	 * Convert a relative filename into an actual File object.
	 */
	private File getChosenFile(String fileChosen)
	{
		if (fileChosen.equals(PARENT_DIR))
		{
			return currentPath.getParentFile();
		}
		else
		{
			return new File(currentPath, fileChosen);
		}
	}
}