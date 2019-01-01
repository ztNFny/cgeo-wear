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

                final Intent launchIntent = new Intent(INTENT_ACTION);
                launchIntent.setPackage(INTENT_PACKAGE);
                launchIntent.putExtra(MobileService.EXTRA_CACHE_NAME, cacheName)
                        .putExtra(MobileService.EXTRA_LATITUDE, latitude)
                        .putExtra(MobileService.EXTRA_LONGITUDE, longitude);
                getApplicationContext().startService(launchIntent);
            }
        }

        finish();
    }
}
