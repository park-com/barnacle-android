package com.gobarnacle.maps;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.gobarnacle.PageDetailActivity;
import com.gobarnacle.PageListActivity;
import com.gobarnacle.R;
import com.gobarnacle.utils.BarnacleClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * A fragment representing a single Page detail screen. This fragment is either
 * contained in a {@link PageListActivity} in two-pane mode (on tablets) or a
 * {@link PageDetailActivity} on handsets.
 */
public class MapActivity extends FragmentActivity implements
								ConnectionCallbacks,
								OnConnectionFailedListener,
								LocationListener,
								OnMyLocationButtonClickListener, android.location.LocationListener {
	
    private static AsyncHttpClient client = new AsyncHttpClient();
	 
    public final static String TrackUri = "/track/updateloc";
	public final static String TAG = "MapActivity";
	public static final Integer ZOOM = 8;
	
    private GoogleMap mMap;
    private LocationManager locationManager;
    private static final long MIN_TIME = 400;
    private static final float MIN_DISTANCE = 1000;

    private LocationClient mLocationClient;
    private TextView mRouteView;	
    private static TextView mAddrView;	
    private static CheckBox mAutoSub;
    private static NumberPicker mMins;
    private static EditText mMsg;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public MapActivity() {
	}
    // These settings are the same as the settings for the map. They will in fact give you updates
    // at the maximal rates currently possible.
    private static final LocationRequest REQUEST = LocationRequest.create()
            .setInterval(5000)         // 5 seconds
            .setFastestInterval(16)    // 16ms = 60fps
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sendloc);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN); 
        mRouteView = (TextView) findViewById(R.id.route_text);
        //mRouteView.setText("Location = " + location);
        mAddrView = (TextView) findViewById(R.id.addr_text);
        
        mAutoSub = (CheckBox) findViewById(R.id.auto_submit);
        mMsg = (EditText) findViewById(R.id.msg_text);
        mMins = (NumberPicker) findViewById(R.id.mininterval);
        mMins.setMaxValue(60);
        mMins.setMinValue(1);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME, MIN_DISTANCE, this); //You can also use LocationManager.GPS_PROVIDER and LocationManager.PASSIVE_PROVIDER        
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
        setUpLocationClientIfNeeded();
        mLocationClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mLocationClient != null) {
            mLocationClient.disconnect();
        }
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map0))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                mMap.setMyLocationEnabled(true);
                mMap.setOnMyLocationButtonClickListener(this);
            }
        }
    }

    private void setUpLocationClientIfNeeded() {
        if (mLocationClient == null) {
            mLocationClient = new LocationClient(
                    getApplicationContext(),
                    this,  // ConnectionCallbacks
                    this); // OnConnectionFailedListener
        }
    }

    /**
     * Button to submit Location. 
     * @throws ClientProtocolException 
     * @throws IOException 
     */
    public void submitLocation(View view) throws ClientProtocolException, IOException {
        if (mLocationClient != null && mLocationClient.isConnected()) {
        	Location lastLoc = mLocationClient.getLastLocation();

        	String msg = "Location = " + lastLoc;
        	Context context = this.getApplicationContext();
			
			try {
				getStringFromLocation(lastLoc, context);
				// updateLocation(lastLoc, context);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /**
     * Implementation of {@link LocationListener}.
     */
    @Override
    public void onLocationChanged(Location location) {

    }


    public static void getStringFromLocation(final Location loc, final Context context)
            throws ClientProtocolException, IOException, JSONException {

    	
        String address = String
                .format(Locale.ENGLISH, "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language="
                                + Locale.getDefault().getCountry(), loc.getLatitude(), loc.getLongitude());
        
        client.get(address, null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                String status;
                String indiStr;
                Log.d(TAG, response.toString());
				try {
					status = response.getString("status");
		            if ("OK".equalsIgnoreCase(status)) {
		                JSONArray results = response.getJSONArray("results");
	                    indiStr = results.getJSONObject(0).getString("formatted_address");
	                    mAddrView.setText(indiStr);
	                }			        
		            updateLocation(loc,context);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
	    });
    }    
    public static void updateLocation(Location loc, final Context context) throws JSONException, UnsupportedEncodingException {
    	
    	JSONObject locParams = new JSONObject();
    	locParams.put("lat",loc.getLatitude());
    	locParams.put("lon",loc.getLongitude());
    	
    	String locstr = (String) mAddrView.getText();
    	locParams.put("locstr",locstr);
    	String msg = mMsg.getText().toString();
    	locParams.put("msg",msg);
    	Integer mins = mMins.getValue();
    	Boolean auto = mAutoSub.isChecked();
    	
    	BarnacleClient.postJSON(context, TrackUri, locParams, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(JSONObject response) {
                String status;
				try {
					status = response.getString("status");
					Log.d(TAG, status);
			        if (status.equals("ok")) {
				      	// drop marker
			        	showToastMessage("Location updated at Barnacle.", context);
			        } else {
			        	showToastMessage("Barnacle login failed.", context);
			        }
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        });		
    	
    }    
	 static void showToastMessage(String message, Context context){
		  Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
	 }    
	 @Override
	 public void onStatusChanged(String provider, int status, Bundle extras) { }

	 @Override
	 public void onProviderEnabled(String provider) { }

	 @Override
	 public void onProviderDisabled(String provider) { }
	    /**
	     * Callback called when connected to GCore. Implementation of {@link ConnectionCallbacks}.
	     */
	    @Override
	    public void onConnected(Bundle connectionHint) {
	        mLocationClient.requestLocationUpdates(
	                REQUEST,
	                this);  // LocationListener
	        Location location = mLocationClient.getLastLocation();
	        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
	        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM);
	        mMap.animateCamera(cameraUpdate);
	        locationManager.removeUpdates(this);
	    }

	    /**
	     * Callback called when disconnected from GCore. Implementation of {@link ConnectionCallbacks}.
	     */
	    @Override
	    public void onDisconnected() {
	        // Do nothing
	    }

	    /**
	     * Implementation of {@link OnConnectionFailedListener}.
	     */
	    @Override
	    public void onConnectionFailed(ConnectionResult result) {
	        // Do nothing
	    }

	    @Override
	    public boolean onMyLocationButtonClick() {
	        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
	        // Return false so that we don't consume the event and the default behavior still occurs
	        // (the camera animates to the user's current position).
	        return false;
	    }
	    	 
}        
	

