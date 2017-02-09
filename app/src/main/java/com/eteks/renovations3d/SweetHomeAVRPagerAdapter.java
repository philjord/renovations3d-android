package com.eteks.renovations3d;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.view.ViewGroup;

import com.eteks.renovations3d.android.FurnitureCatalogListPanel;
import com.eteks.renovations3d.android.FurnitureTable;
import com.eteks.renovations3d.android.HomeComponent3D;
import com.eteks.renovations3d.android.MultipleLevelsPlanPanel;

/**
 * Created by phil on 2/6/2017.
 * Replaces everything from the parent class to get access to the fragment transaction
 * because we don't want the back stack to memory leak these fragments when we swap out for a new sweethome avr
 * which is simply putting in the call mCurTransaction.remove((Fragment) object);
 */
public class SweetHomeAVRPagerAdapter extends FragmentPagerAdapter
{
	private SweetHomeAVR sweetHomeAVR;

	private final FragmentManager mFragmentManager;
	private FragmentTransaction mCurTransaction = null;
	private Fragment mCurrentPrimaryItem = null;

	public SweetHomeAVRPagerAdapter(FragmentManager fm, SweetHomeAVR sweetHomeAVR)
	{
		super(fm);
		this.sweetHomeAVR = sweetHomeAVR;
		this.mFragmentManager = fm;
	}

	public void setSweetHomeAVR(SweetHomeAVR sweetHomeAVR)
	{
		this.sweetHomeAVR = sweetHomeAVR;
	}


	@Override
	public Fragment getItem(int position)
	{
		// in case we are in an unloading phase before a new sweetHomeAVR arrives
		if (sweetHomeAVR == null || sweetHomeAVR.getHomeController() == null)
			return new Fragment();

		if (position == 0)
		{
			return (FurnitureTable) sweetHomeAVR.getHomeController().getFurnitureController().getView();
		}
		else if (position == 1)
		{
			return (MultipleLevelsPlanPanel) sweetHomeAVR.getHomeController().getPlanController().getView();
		}
		else if (position == 2)
		{
			return (FurnitureCatalogListPanel) sweetHomeAVR.getHomeController().getFurnitureCatalogController().getView();
		}
		else if (position == 3)
		{
			return (HomeComponent3D) sweetHomeAVR.getHomeController().getHomeController3D().getView();
		}
		return null;
	}

	@Override
	public int getCount()
	{
		return 4;
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position)
	{
		if (mCurTransaction == null)
		{
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		final long itemId = getItemId(position);

		// Do we already have this fragment?
		String name = makeFragmentName(container.getId(), itemId);
		Fragment fragment = mFragmentManager.findFragmentByTag(name);
		if (fragment != null)
		{
			mCurTransaction.attach(fragment);
		}
		else
		{
			fragment = getItem(position);

			mCurTransaction.add(container.getId(), fragment, makeFragmentName(container.getId(), itemId));
		}
		if (fragment != mCurrentPrimaryItem)
		{
			fragment.setMenuVisibility(false);
			fragment.setUserVisibleHint(false);
		}

		return fragment;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object)
	{
		if (mCurTransaction == null)
		{
			mCurTransaction = mFragmentManager.beginTransaction();
		}

		mCurTransaction.detach((Fragment) object);

		// they stay in mActive cos the backstack might want them, and we want to get rid of them
		// note this must be done in the same transaction as the adds etc hence the copy of the parent class
		mCurTransaction.remove((Fragment) object);
	}

	@Override
	public void setPrimaryItem(ViewGroup container, int position, Object object)
	{
		Fragment fragment = (Fragment) object;
		if (fragment != mCurrentPrimaryItem)
		{
			if (mCurrentPrimaryItem != null)
			{
				mCurrentPrimaryItem.setMenuVisibility(false);
				mCurrentPrimaryItem.setUserVisibleHint(false);
			}
			if (fragment != null)
			{
				fragment.setMenuVisibility(true);
				fragment.setUserVisibleHint(true);
			}
			mCurrentPrimaryItem = fragment;
		}
	}

	@Override
	public void finishUpdate(ViewGroup container)
	{
		if (mCurTransaction != null)
		{
			mCurTransaction.commitNowAllowingStateLoss();
			mCurTransaction = null;
		}
	}

	private String makeFragmentName(int viewId, long id)
	{
		return "android:switcher:" + viewId + ":" + id;
	}


	private long baseId = 0;

	//this is called when notifyDataSetChanged() is called
	@Override
	public int getItemPosition(Object object)
	{
		// refresh all fragments when data set changed
		return PagerAdapter.POSITION_NONE;
	}

	@Override
	public long getItemId(int position)
	{
		// give an ID different from position when position has been changed
		return baseId + position;
	}

	/**
	 * Notify that the position of a fragment has been changed.
	 * Create a new ID for each position to force recreation of the fragment
	 *
	 * @param n number of items which have been changed
	 */
	public void notifyChangeInPosition(int n)
	{
		// shift the ID returned by getItemId outside the range of all previous fragments
		baseId += getCount() + n;
	}
}
