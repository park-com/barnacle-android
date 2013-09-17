package com.gobarnacle.utils;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

public class LocationConverterAsync extends AsyncTask<LocationContext,Void,String> {
	private static final String MAP_URI = "http://maps.googleapis.com/maps/api/geocode/json?latlng=%1$f,%2$f&sensor=true&language=";
	private static final String TAG = "LocationConverter";
    private static AsyncHttpClient client = new AsyncHttpClient();
    
    
	class LocResponseHandler extends JsonHttpResponseHandler {
		String formAddress = "";
		ArrayList<Route> routes = new ArrayList<Route>();
		
		@Override
        public void onSuccess(JSONObject response) {
            String status;
			try {
				status = response.getString("status");
	            if ("OK".equalsIgnoreCase(status)) {
	                JSONArray results = response.getJSONArray("results");
                    formAddress = results.getJSONObject(0).getString("formatted_address");
                    Log.d(TAG, formAddress);
                }			        
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }	    
	    public String getAddress(){
	    	return formAddress;
	    }
	}
			
	@Override
	protected String doInBackground(LocationContext... lc) {		      		 		 
		LocResponseHandler lr = new LocResponseHandler();		 
		Location loc = lc[0].getLocation();
		
		String address = String
                .format(Locale.ENGLISH, MAP_URI + Locale.getDefault().getCountry(), loc.getLatitude(), loc.getLongitude());
        
        client.get(address, null, lr);
        String addr = lr.getAddress();
		return addr;
	}
   protected void onPostExecute(String result) {
       returnAddress(result);
   }
   private String returnAddress(String result) {
   	return result;
   }
}	
