package bbw.com.crashr.db;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;
import java.util.concurrent.TimeUnit;

import bbw.com.crashr.CauseHelper;
import bbw.com.crashr.CrashrMain;

/**
 * Created by Sam on 5/07/2015.
 */
public class IncidentHelper {
    private static final long LOCATION_INTERVAL = TimeUnit.SECONDS.toMillis(30);
    private static IncidentHelper instance_;
    private final GoogleApiClient googleAPIClient;
    private final LocationRequest locationRequest;
    private CauseHelper causeHelper_;

    // SQL access
    private CrashDataSource dataSource_;
    private double area_ = 1 / 110d;

    private IncidentHelper(Context c) {
        dataSource_ = new CrashDataSource(c);
        dataSource_.open();

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(LOCATION_INTERVAL);
        locationRequest.setFastestInterval(LOCATION_INTERVAL);
        googleAPIClient = new GoogleApiClient.Builder(c)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) c)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) c)
                .addApi(LocationServices.API)
                .build();
        if (googleAPIClient != null)
            googleAPIClient.connect();

        causeHelper_ = new CauseHelper(c.getResources());
    }

    public List<Incident> getIncidents() {
        Location location = LocationServices.FusedLocationApi.getLastLocation(googleAPIClient);
        if (location == null)
            return null;
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        return dataSource_.getLocalisedIncidents(lat - area_, lat + area_, lon - area_, lon + area_);
    }

    public static IncidentHelper getInstance(Context c) {
        if (instance_ == null)
            instance_ = new IncidentHelper(c);
        return instance_;
    }

    public CauseHelper getCauseHelper() {
        return causeHelper_;
    }

    public void onConnected(Context c) {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleAPIClient, locationRequest, (com.google.android.gms.location.LocationListener) c);
    }
}
