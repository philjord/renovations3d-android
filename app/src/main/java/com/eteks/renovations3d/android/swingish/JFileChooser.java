package com.eteks.renovations3d.android.swingish;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

public class JFileChooser
{
	private static final String PARENT_DIR = "..";

	private final Context context;
	private ListView list;
	private LinearLayout rootView;
	private EditText nameInput;
	private Dialog dialog;
	private File currentPath;
	private boolean allowNewName = false;
	private boolean allowDirSelect = false;

	// filter on file extension
	private String extension = null;

	// MUCH sexier system
	private FileFilter fileFilter = null;

	private FileSelectedListener fileListener;

	public JFileChooser(Activity activity)
	{
		this(activity, null, false, false, null);
	}

	public JFileChooser(Activity activity, boolean allowNewName)
	{
		this(activity, null, allowNewName, false, null);
	}

	public JFileChooser(Activity activity, File startFolder)
	{
		this(activity, startFolder, false, false, null);
	}

	public JFileChooser(Context context, File startFolder, final boolean allowNewName, final boolean allowDirSelect, String[] okCancel)
	{
		this.context = context;
		this.allowNewName = allowNewName;
		this.allowDirSelect = allowDirSelect;

		buildView();

		final AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle(allowDirSelect ? "Select folder" : "Select File");
		builder.setView(rootView);
		if (allowDirSelect || allowNewName)
		{
			builder.setPositiveButton(okCancel != null ? okCancel[0] : "OK",
					new DialogInterface.OnClickListener()
					{
						@Override
						public void onClick(DialogInterface d, int w)
						{
							String fileChosen = null;
							if( list.getSelectedItem() !=null)
								fileChosen = (String)list.getSelectedItem();
							else
								fileChosen = nameInput.getText().toString();

							File chosenFile = getChosenFile(fileChosen);
							if (!chosenFile.isDirectory() || allowDirSelect)
							{
								dialog.dismiss();
								if (fileListener != null)
									fileListener.fileSelected(chosenFile);
							}
						}
					});
		}
		builder.setNegativeButton(okCancel != null ? okCancel[1] : "Cancel",
				new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface d, int w)
					{
						dialog.dismiss();
					}
				});

		builder.setCancelable(true);
		dialog = builder.create();

		if (startFolder == null)
		{
			startFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
		}
		else if (startFolder.isFile())
		{
			startFolder = startFolder.getParentFile();
		}
		refresh(startFolder);
	}

	private void buildView()
	{
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

		list = new ListView(context);
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
					if(allowNewName)
					{
						// just give it as the start point for a potential edit, and wait for ok button
						nameInput.setText(fileChosen);
					}
					else
					{
						dialog.dismiss();
						if (fileListener != null)
							fileListener.fileSelected(chosenFile);
					}
				}
			}
		});
		rootView = new LinearLayout(context);
		rootView.setOrientation(LinearLayout.VERTICAL);
		rootView.setPadding(10,10,10,10);
		rootView.addView(list, lp);

		if(allowNewName)
		{
			TextView nameInputLabel = new TextView(context);
			nameInputLabel.setText("New name");
			nameInputLabel.setTextAppearance(context, android.R.style.TextAppearance_Medium);
			nameInput = new EditText(context);
			nameInput.setMaxLines(1);
			rootView.addView(nameInputLabel, lp);
			rootView.addView(nameInput, lp);
		}

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


		if (path.exists())
		{
			if(path.isDirectory())
			{
				this.currentPath = path;
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
								return false;
							}
							else if (extension == null)
							{
								return fileFilter != null ? fileFilter.accept(file) : true;
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
				list.setAdapter(new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, fileList)
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
			else
			{
				System.err.println("JFileChooser given a file to refresh not a dir! " + path.getAbsolutePath());
			}
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

	/**
	 * Note replaces current and therefore only last set is used (and you can't pick anyway!)
	 *
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

	public JFileChooser setFileListener(FileSelectedListener fileListener)
	{
		this.fileListener = fileListener;
		return this;
	}

	// file selection event handling
	public interface FileSelectedListener
	{
		void fileSelected(File file);
	}
}