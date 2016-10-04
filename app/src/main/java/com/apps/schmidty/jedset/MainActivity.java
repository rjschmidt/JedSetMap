package com.apps.schmidty.jedset;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IMapController;
import org.osmdroid.api.IMapView;
import org.osmdroid.google.wrapper.MyLocationOverlay;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.TilesOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class MainActivity extends Activity {

        MapView mMapView;
        TilesOverlay mTilesOverlay;
        MapTileProviderBasic mProvider;
        MyLocationNewOverlay mLocationOverlay;
        RotationGestureOverlay mRotationGestureOverlay;
        CompassOverlay mCompassOverlay;
        ArrayList<OverlayItem> mOverlayItems;
        protected Context mContext;


        // ===========================================================
        // Constructors
        // ===========================================================
        /** Called when the activity is first created. */
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Request permissions to support Android Marshmallow and above devices
            if (Build.VERSION.SDK_INT >= 23) {
                checkPermissions();
            }

            // Setup base map
            final RelativeLayout rl = new RelativeLayout(this);

            mMapView = new MapView(this);
            mMapView.setTilesScaledToDpi(true);
            rl.addView(mMapView, new RelativeLayout.LayoutParams(MapView.LayoutParams.FILL_PARENT,
                    MapView.LayoutParams.FILL_PARENT));
            mMapView.setBuiltInZoomControls(true);
            mMapView.setMultiTouchControls(true);
            mMapView.setUseDataConnection(false); //keeps the mapView from loading online tiles using network connection.

            mOverlayItems = new ArrayList<OverlayItem>();
            mMapView.getController().setZoom(16);

            // Add tiles layer
            mProvider = new MapTileProviderBasic(getApplicationContext());
            mProvider.setTileSource(TileSourceFactory.MAPNIK);
            mTilesOverlay = new TilesOverlay(mProvider, this.getBaseContext());
            mMapView.getOverlays().add(mTilesOverlay);

            mLocationOverlay = new MyLocationNewOverlay(this.getBaseContext(), new GpsMyLocationProvider(getApplicationContext()),mMapView);
            mLocationOverlay.enableMyLocation();
            mMapView.getOverlays().add(mLocationOverlay);

            mCompassOverlay = new CompassOverlay(this.getBaseContext(), new InternalCompassOrientationProvider(getApplicationContext()), mMapView);
            mMapView.getOverlays().add(mCompassOverlay);

            mRotationGestureOverlay = new RotationGestureOverlay(this.getBaseContext(), mMapView);
            mRotationGestureOverlay.setEnabled(true);
            mMapView.setMultiTouchControls(true);
            mMapView.getOverlays().add(mRotationGestureOverlay);

            setContentView(rl);

            addLocationMarker("Hello","I am a marker", new GeoPoint(28.064283d, -82.566480d), this);
            mLocationOverlay.enableFollowLocation();
        }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    public void addLocationMarker(String title, String description, GeoPoint location, Context context) {
        OverlayItem tempItem = new OverlayItem(title, description, location); // Lat/Lon decimal degrees
        mContext = context;
//        tempItem.setMarker(ContextCompat.getDrawable(getApplicationContext(), R.drawable.sfgpuci));
        mOverlayItems.add(tempItem); // Lat/Lon decimal degrees

//        ItemizedIconOverlay<OverlayItem> mItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(this, mOverlayItems, null);
//        mMapView.getOverlays().add(mItemizedIconOverlay);
        ResourceProxy resourceProxy = (ResourceProxy) new DefaultResourceProxyImpl(mContext);

        ItemizedIconOverlay<OverlayItem> mItemizedIconOverlay = new ItemizedIconOverlay<OverlayItem>(
                mOverlayItems, ContextCompat.getDrawable(getApplicationContext(), R.drawable.sfgpuci),
                new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                return onSingleTapUpHelper(item);
            }

            @Override public boolean onItemLongPress(final int index, final OverlayItem item) {
                return true;
            }
        }, resourceProxy);
        mMapView.getOverlays().add(mItemizedIconOverlay);
    }

    public boolean onSingleTapUpHelper(OverlayItem item) {
        //Toast.makeText(mContext, "Item " + i + " has been tapped!", Toast.LENGTH_SHORT).show();
        AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setTitle(item.getTitle());
        dialog.setMessage(item.getSnippet());
        dialog.show();
        return true;
    }

    // START PERMISSION CHECK
    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this, "Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    // END PERMISSION CHECK
}
