package com.mlopez.deportesburgos;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class AcercaDeActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TextView tv = new TextView(this);
		tv.setText("Aplicación desarrollada por mlopez");
		setContentView(tv);
	}
}
