package com.mcnc.parecis.bizmob.view;

import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

import com.mcnc.parecis.bizmob.def.Def;

public class ImpHomeActivity extends ImpMainActivity {

	@Override
	protected void setHeader(LinearLayout baseLayout) {

		headerLayout = new LinearLayout(this);
		LinearLayout.LayoutParams headerLayoutParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.TITLE_H, 0 );
		LinearLayout.LayoutParams headerInParam = new LinearLayout.LayoutParams(
				Def.TOP_BUTTON_W, Def.TITLE_H, 0);
		LinearLayout.LayoutParams headerCenterParam = new LinearLayout.LayoutParams(
				LayoutParams.FILL_PARENT, Def.TITLE_H, 1);
		headerLeftLayout = new LinearLayout(this);
		headerTitleLayout = new LinearLayout(this);
		headerRightLayout = new LinearLayout(this);

		headerTitleLayout.setOnTouchListener(this);

		headerLayout.addView(headerLeftLayout, headerInParam);
		headerLayout.addView(headerTitleLayout, headerCenterParam);
		headerLayout.addView(headerRightLayout, headerInParam);
		
		baseLayout.addView(headerLayout, headerLayoutParam);
		headerLayout.setVisibility(View.GONE);
	}
	
}
