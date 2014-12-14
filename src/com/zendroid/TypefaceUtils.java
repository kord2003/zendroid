package com.zendroid;

import android.graphics.Typeface;
import android.widget.TextView;

public class TypefaceUtils {
	public static final String FONT_CONFIDENTIAL = "fonts/ConfidentialC.otf";
	
	public static void setCustomTypeface(TextView targetView) {
		Typeface typeface = Typeface.createFromAsset(targetView.getContext().getAssets(), FONT_CONFIDENTIAL);
		targetView.setTypeface(typeface);
	}

}
