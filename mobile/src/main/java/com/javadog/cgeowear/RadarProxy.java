package com.javadog.cgeowear;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class RadarProxy extends Activity {
    private static final String INTENT_ACTION = "cgeo.geocaching.wear.NAVIGATE_TO";
    private static final String INTENT_PACKAGE = "com.javadog.cgeowear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

        if(intent != null) {
            Double latitude = ((Float)intent.getFloatExtra("latitude", 0)).doubleValue();
            Double longitude = ((Float)intent.getFloatExtra("longitude", 0)).doubleValue();
            String cacheName = intent.getStringExtra("name");
            String geocode = intent.getStringExtra("code");
            String cacheSize = intent.getStringExtra("size");
            String ratingTerrain = intent.getStringExtra("terrain");
            String ratingDifficulty = intent.getStringExtra("difficulty");

            final Intent launchIntent = new Intent(INTENT_ACTION);
            launchIntent.setPackage(INTENT_PACKAGE);
            launchIntent.putExtra(MobileService.EXTRA_CACHE_NAME, cacheName)
                    .putExtra(MobileService.EXTRA_GEOCODE, geocode)
                    .putExtra(MobileService.EXTRA_LATITUDE, latitude)
                    .putExtra(MobileService.EXTRA_LONGITUDE, longitude)
                    .putExtra(MobileService.EXTRA_CACHE_SIZE, cacheSize)
                    .putExtra(MobileService.EXTRA_CACHE_TERRAIN, ratingTerrain)
                    .putExtra(MobileService.EXTRA_CACHE_DIFFICULTY, ratingDifficulty)
            ;
            getApplicationContext().startService(launchIntent);
        }

        finish();
    }
}
