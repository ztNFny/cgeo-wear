/*
	Copyright 2014 Cullin Moran
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at

		http://www.apache.org/licenses/LICENSE-2.0

	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
 */

package com.javadog.cgeowear;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.DecimalFormat;

public class CompassActivity extends Activity {

	/**
	 * Used to trigger a minimal UI refresh (grabs newest distance/direction values from service).
	 */
	public static final String ACTION_TRIGGER_MINIMAL_REFRESH = "/cgeowear/compass/refresh_minimal";

	private cgeoWearService service;
	private boolean serviceBound = false;

	private AlertDialog launchDialog;
	private TextView tv_cacheName;
	private TextView tv_geocode;
	private TextView tv_distance;
	private CircularTextView tv_ratingDifficulty;
	private CircularTextView tv_ratingTerrain;
	private CircularTextView tv_cacheSize;
	private ImageView iv_compass;

	private float currentRotation;

	private LocalBroadcastManager localBroadcastManager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_compass_radar);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	protected void onStart() {
		super.onStart();
		final Intent launchIntent = getIntent();

		getUiReferences();

		final boolean properLaunch = verifyLaunchAction(launchIntent.getAction());
		if(properLaunch) {
			bindToService();
			listenForUpdates();
		}
	}

	/**
	 * Let users know not to launch the app directly if they did (it's not necessary).
	 *
	 * @param launchAction The action sent with the Intent which launched the activity.
	 * @return Whether the app was launched correctly (through c:geo).
	 */
	private boolean verifyLaunchAction(final String launchAction) {
		final boolean launchedCorrectly = ListenerService.PATH_INIT.equals(launchAction);
		if(!launchedCorrectly) {
			launchDialog = new AlertDialog.Builder(this)
					.setMessage(getString(R.string.app_direct_launch_warning))
					.setOnDismissListener(new DialogInterface.OnDismissListener() {
						@Override
						public void onDismiss(DialogInterface dialog) {
							CompassActivity.this.finish();
						}
					})
					.setPositiveButton(android.R.string.ok, null)
					.show();
		}
		Log.d(cgeoWearService.DEBUG_TAG, "Launch verification " + ((launchedCorrectly) ? "passed!" : "failed!"));
		return launchedCorrectly;
	}

	private void getUiReferences() {
		tv_cacheName = (TextView) findViewById(R.id.textview_cache_name);
		tv_geocode = (TextView) findViewById(R.id.textview_geocode);
		tv_distance = (TextView) findViewById(R.id.textview_distance);
		iv_compass = (ImageView) findViewById(R.id.compass);
		tv_cacheSize = (CircularTextView) findViewById(R.id.size);
		tv_ratingDifficulty = (CircularTextView) findViewById(R.id.ratingDifficulty);
		tv_ratingTerrain = (CircularTextView) findViewById(R.id.ratingTerrain);
	}

	/**
	 * Populates all fields on the screen.
	 */
	private void populateScreen(final String cacheName, final String geocode, final float distance, final float direction, final String difficulty, final String terrain, final String size) {
		tv_cacheName.setText(cacheName);
		if ("".equals(size)) {
			tv_geocode.setVisibility(View.INVISIBLE);
		} else {
			tv_geocode.setVisibility(View.VISIBLE);
			tv_geocode.setText(geocode);
		}
		if ("".equals(difficulty)) {
			tv_ratingDifficulty.setVisibility(View.INVISIBLE);
		} else {
			String difficultyColor = "#000000";
			switch (difficulty.substring(0, 1)) {
				case "1":
					difficultyColor = "#00992A";
					break;
				case "2":
					difficultyColor = "#009BB8";
					break;
				case "3":
					difficultyColor = "#085388";
					break;
				case "4":
					difficultyColor = "#EF7E26";
					break;
				case "5":
					difficultyColor = "#940015";
					break;
			}
			tv_ratingDifficulty.setVisibility(View.VISIBLE);
			tv_ratingDifficulty.setText(difficulty);
			tv_ratingDifficulty.setSolidColor(difficultyColor);
		}
		if ("".equals(terrain)) {
			tv_ratingTerrain.setVisibility(View.INVISIBLE);
		} else {
			String terrainColor = "#000000";
			switch (terrain.substring(0, 1)) {
				case "1":
					terrainColor = "#00992A";
					break;
				case "2":
					terrainColor = "#009BB8";
					break;
				case "3":
					terrainColor = "#085388";
					break;
				case "4":
					terrainColor = "#EF7E26";
					break;
				case "5":
					terrainColor = "#940015";
					break;
			}
			tv_ratingTerrain.setVisibility(View.VISIBLE);
			tv_ratingTerrain.setText(terrain);
			tv_ratingTerrain.setSolidColor(terrainColor);
		}
		if ("".equals(size)) {
			tv_cacheSize.setVisibility(View.INVISIBLE);
		} else {
			String sizeFirstChar = size.substring(0, 1);
			// Micro, Small, Regular, Large, Other
			String sizeColor = "#000000";
			switch (sizeFirstChar) {
				case "M":
					sizeColor = "#00992A";
					break;
				case "S":
					sizeColor = "#009BB8";
					break;
				case "R":
					sizeColor = "#085388";
					break;
				case "L":
					sizeColor = "#EF7E26";
					break;
				case "O":
					sizeColor = "#940015";
					break;
			}
			tv_cacheSize.setVisibility(View.VISIBLE);
			tv_cacheSize.setText(sizeFirstChar);
			tv_cacheSize.setSolidColor(sizeColor);
		}
		setDistanceFormatted(distance);
		rotateCompass(direction);
	}

	private void bindToService() {
		if(!serviceBound) {
			Intent bindIntent = new Intent(this, cgeoWearService.class);
			bindService(bindIntent, serviceConnection, Context.BIND_ABOVE_CLIENT);
		}
	}

	private void unbindFromService() {
		if(serviceConnection != null && service != null) {
			unbindService(serviceConnection);
		}
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			cgeoWearService.LocalBinder binder = (cgeoWearService.LocalBinder) iBinder;
			service = binder.getService();
			serviceBound = true;

			populateScreen(service.getCacheName(), service.getGeocode(), service.getDistance(), service.getDirection(), service.getDifficulty(), service.getTerrain(), service.getSize());
		}

		@Override
		public void onServiceDisconnected(ComponentName componentName) {
			serviceBound = false;
		}
	};

	private void listenForUpdates() {
		localBroadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
		IntentFilter updateFilter = new IntentFilter();
		updateFilter.addAction(ACTION_TRIGGER_MINIMAL_REFRESH);
		updateFilter.addAction(ListenerService.PATH_KILL_APP);
		localBroadcastManager.registerReceiver(localBroadcastReceiver, updateFilter);
	}

	private void stopListeningForUpdates() {
		if(localBroadcastManager != null) {
			localBroadcastManager.unregisterReceiver(localBroadcastReceiver);
		}
	}

	/**
	 * Sets the distance TextView's text with appropriate formatting.
	 *
	 * @param dist The distance to the geocache, in meters.
	 */
	private void setDistanceFormatted(final float dist) {
		DecimalFormat format = new DecimalFormat("0");
		tv_distance.setText(format.format(dist));
	}

	float newRot = 0;
	/**
	 * Handles rotation of the compass to a new direction.
	 *
	 * @param newDirectionRaw Direction to turn to, in degrees.
	 */
	private void rotateCompass(final float newDirectionRaw) {

		//Rotate through smallest angle
		float apparent;
		apparent = newRot % 360;
		if(apparent < 0) {
			apparent += 360;
		}
		if(apparent < 180 && (newDirectionRaw > (apparent + 180)) ) {
			newRot -= 360;
		}
		if(apparent >= 180 && (newDirectionRaw <= (apparent - 180)) ) {
			newRot += 360;
		}
		newRot += (newDirectionRaw - apparent);

		//Play animation
		if(currentRotation != newRot) {
			RotateAnimation anim = new RotateAnimation(
					currentRotation,
					newRot,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			anim.setDuration(200l);
			anim.setFillAfter(true);
			iv_compass.startAnimation(anim);

			currentRotation = newRot;
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if(serviceBound) {
            populateScreen(service.getCacheName(), service.getGeocode(), service.getDistance(), service.getDirection(), service.getDifficulty(), service.getTerrain(), service.getSize());
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		if(launchDialog != null) {
			launchDialog.dismiss();
		}

		unbindFromService();
		stopListeningForUpdates();
	}

	/**
	 * Receives messages from cgeoWearService which trigger UI updates. Can also kill the Activity when user exits.
	 */
	private BroadcastReceiver localBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			//Distance/direction update available, grab latest values from service
			if(ACTION_TRIGGER_MINIMAL_REFRESH.equals(intent.getAction()) && serviceBound) {
				setDistanceFormatted(service.getDistance());
				rotateCompass(service.getDirection());

				//Kill app
			} else if(ListenerService.PATH_KILL_APP.equals(intent.getAction())) {
				Log.d(cgeoWearService.DEBUG_TAG, "Phone service stopped; killing watch app.");
				CompassActivity.this.finish();
			}
		}
	};
}
