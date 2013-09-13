package com.gobarnacle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebViewActivity extends FragmentActivity{

	private String track_url;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_track);
        
		Intent intent = getIntent();
		track_url = intent.getStringExtra(ManageActivity.TRACK_URL);

        
    	WebView myWebView = (WebView) findViewById(R.id.view_track);
    	WebSettings webSettings = myWebView.getSettings();
    	webSettings.setJavaScriptEnabled(true);
    	myWebView.loadUrl(track_url);
        
        

    }
	
}
