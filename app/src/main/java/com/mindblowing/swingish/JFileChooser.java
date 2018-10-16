package com.mindblowing.swingish;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
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
import java.util.List;

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

	private FileFilter[] fileFilters = null;

	private FileSelectedListener fileListener;

	private static FolderReviewer folderReviewer;
	public static void setFolderReviewer(FolderReviewer folderReviewer)
	{
		JFileChooser.folderReviewer = folderReviewer;
	}

	/**
	 * MUST be on a thread with Looper called
	 * @param activity
	 */
	public JFileChooser(Activity activity)
	{
		this(activity, null, false, false, null);
	}
	/**
	 * MUST be on a thread with Looper called
	 * @param activity
	 */
	public JFileChooser(Activity activity, boolean allowNewName)
	{
		this(activity, null, allowNewName, false, null);
	}
	/**
	 * MUST be on a thread with Looper called
	 * @param activity
	 */
	public JFileChooser(Activity activity, File startFolder)
	{
		this(activity, startFolder, false, false, null);
	}
	/**
	 * MUST be on a thread with Looper called
	 * @param context
	 */
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



		if(folderReviewer != null)
		{
			startFolder = folderReviewer.reviewFolder(startFolder);
		}

		if (startFolder.isFile())
		{
			startFolder = startFolder.getParentFile();
		}
		refresh(startFolder);
	}

	private void buildView()
	{
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 0, 1.0f);
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
			LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			LinearLayout linearLayout2 = new LinearLayout(context);
			linearLayout2.setOrientation(LinearLayout.VERTICAL);
			linearLayout2.setPadding(10,10,10,10);
			TextView nameInputLabel = new TextView(context);
			nameInputLabel.setText("Save file name");
			nameInputLabel.setTextAppearance(context, android.R.style.TextAppearance_Medium);
			nameInput = new EditText(context);
			nameInput.setEms(10);
			nameInput.setMaxLines(1);
			nameInput.setSingleLine(true);


			rootView.addView(nameInputLabel, lp2);
			rootView.addView(nameInput, lp2);
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

		// this can only be done after the dialog has had show called!!!
		if(allowNewName)
		{
			nameInput.addTextChangedListener(new TextWatcher()
			{

				@Override
				public void afterTextChanged(Editable s)
				{
				}

				@Override
				public void beforeTextChanged(CharSequence s, int start,
											  int count, int after)
				{
				}

				@Override
				public void onTextChanged(CharSequence s, int start,
										  int before, int count)
				{
					((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(s.length() != 0);
				}
			});

			((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

			//nameInput.requestFocus();
			//InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			//imm.showSoftInput(nameInput, InputMethodManager.SHOW_IMPLICIT);
		}
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
		// in case of a locked up situation just attempt to go back to the downloads folder
		if(currentPath == null && folderReviewer != null)
		{
			currentPath = folderReviewer.reviewFolder(currentPath);
		}

		// note downloads might be returning null
		if (currentPath != null && path.exists())
		{
			if(path.isDirectory())
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
								return false;
							}
							else
							{
								if (fileFilters != null)
								{
									for (FileFilter ff : fileFilters)
									{
										if (ff.accept(file))
											return true;
									}
									return false;
								}
								else
								{
									return true;
								}
							}
						}
						else
						{
							return false;
						}
					}
				});


				// sometimes we are in a bad hierarchy and the new path has
				// null files or null dirs (but should have at least the previous one)
				if(files != null && dirs != null)
				{
					this.currentPath = path;

					// convert to an array
					int i = 0;
					String[] fileList;

					File parent = path.getParentFile();
					// try to avoid the .. if the parent is odd
					if (parent != null && parent.list() != null)
					{
						fileList = new String[dirs.length + files.length + 1];
						fileList[i++] = PARENT_DIR;
					}
					else
					{
						fileList = new String[dirs.length + files.length];
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
					// take off the useless start folders
					dialog.setTitle(currentPath.getPath().replace("/storage/emulated/0", ""));
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

					list.requestLayout();;
					list.invalidate();
					rootView.requestLayout();
					rootView.invalidate();

				}
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
		List<FileFilter> currentFilters = Arrays.asList(fileFilters);
		if(!currentFilters.contains(filter))
		{
			currentFilters.add(filter);
			fileFilters = currentFilters.toArray(fileFilters);
		}
	}

	public void setFileFilters(FileFilter[] filters)
	{
		fileFilters = filters;
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


	public interface FolderReviewer
	{
		File reviewFolder(File folder);
	}
}