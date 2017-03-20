package com.eteks.renovations3d.android;

import android.content.Context;

import com.eteks.renovations3d.android.swingish.SpinnerNumberModel;
import com.eteks.sweethome3d.model.LengthUnit;
import com.eteks.sweethome3d.model.UserPreferences;

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
		super(context, model,
				model instanceof NullableSpinnerNumberModel.NullableSpinnerLengthModel
						? ((NullableSpinnerNumberModel.NullableSpinnerLengthModel) model).getLengthUnit().getFormat()
						: null);
	}

}
