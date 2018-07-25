/*
 * NullableSpinner.java 29 mai 07
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

import android.content.Context;

import com.mindblowing.swingish.SpinnerNumberModel;

/**
 * Spinner that accepts empty string values. In this case the returned value is <code>null</code>.
 */
public class NullableSpinner extends AutoCommitSpinner
{

	public NullableSpinner(Context context)
	{
		this(context, new NullableSpinnerNumberModel(0, 0, 100, 1));
	}

	public NullableSpinner(Context context, SpinnerNumberModel model)
	{
		this( context, model, false);
	}
	public NullableSpinner(Context context, SpinnerNumberModel model, boolean allowTextEntry)
	{
		super(context, model,
				model instanceof NullableSpinnerNumberModel.NullableSpinnerLengthModel
						? ((NullableSpinnerNumberModel.NullableSpinnerLengthModel) model).getLengthUnit().getFormat()
						: null, allowTextEntry);
	}
}
