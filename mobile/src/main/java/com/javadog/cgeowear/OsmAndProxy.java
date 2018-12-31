package com.javadog.cgeowear;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class OsmAndProxy extends Activity {
    private static final String INTENT_ACTION = "cgeo.geocaching.wear.NAVIGATE_TO";
    private static final String INTENT_PACKAGE = "com.javadog.cgeowear";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if(intent != null) {
            final String action = intent.getAction();
            if ("android.intent.action.VIEW".equals(action)) {

                Uri data = intent.getData();
                Double latitude = Double.parseDouble(data.getQueryParameter("lat"));
                Double longitude = Double.parseDouble(data.getQueryParameter("lon"));
                String cacheName = data.getQueryParameter("name");
                String geocode = data.getQueryParameter("name");


                final Intent launchIntent = new Intent(INTENT_ACTION);
                launchIntent.setPackage(INTENT_PACKAGE);
                launchIntent.putExtra(MobileService.EXTRA_CACHE_NAME, cacheName)
                        .putExtra(MobileService.EXTRA_GEOCODE, geocode)
                        // original c:geo
                        //.putExtra(Intents.EXTRA_LATITUDE, coords.getLatitude())
                        //.putExtra(Intents.EXTRA_LONGITUDE, coords.getLongitude());
                        // original c:geo + hardcoded strings removed inbetween
                        .putExtra(MobileService.EXTRA_LATITUDE, latitude)
                        .putExtra(MobileService.EXTRA_LONGITUDE, longitude)
                // align with https://github.com/ztNFny/cgeo-wear/commit/64c0880068f104fb48c11c48d3c71ac8e67446d1
                //.putExtra(Intents.EXTRA_COORDS, coords.getCoords());
                ;
                getApplicationContext().startService(launchIntent);
            }
        }

        finish();
    }
}
