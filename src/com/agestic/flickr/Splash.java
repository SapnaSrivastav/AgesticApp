package com.agestic.flickr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class Splash extends Activity {

	// Splash screen timer
	private static int SPLASH_TIME_OUT = 2000;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		
			showBasicSplash();
	

	}

	private void showBasicSplash() {

		new Handler().postDelayed(new Runnable() {

			/*
			 * Showing splash screen with a timer.
			 */

			@Override
			public void run() {
				// This method will be executed once the timer is over
				Intent intent=new Intent(Splash.this, MainActivity.class);
				startActivity(intent);

				// close this activity
				finish();
			}
		}, SPLASH_TIME_OUT);

	}


}
