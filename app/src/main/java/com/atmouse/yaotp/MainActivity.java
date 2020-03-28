package com.atmouse.yaotp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.lang.Math.floor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        final String pri_key = sharedPref.getString("pref_key", "aaaa");
        final String pin = sharedPref.getString("pref_pin", "aaaa");
        final String user_name = sharedPref.getString("pref_user_name", "aaaa");
        final String user_pass = sharedPref.getString("pref_user_pass", "aaaa");
        final String checkotp_url = sharedPref.getString("pref_api_url", "aaaa");

        ProgressBar pb = findViewById(R.id.progressBar1);
        pb.setVisibility(View.INVISIBLE);

        MyWebViewClient wvc = new MyWebViewClient();
        wvc.setProgressBar(pb);

        FloatingActionButton fav = findViewById(R.id.fav);
        fav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String pdata = "user_name=" + user_name
                        + "&user_pass=" + user_pass
                        + "&otp=" + stringFromJNI(pri_key, pin)
                        + "&language=cn"
                        + "&client_id=8e3fed43f08530c7";

                WebView wv = findViewById(R.id.webview1);
                wv.postUrl(checkotp_url, pdata.getBytes());
            }
        });

        ImageView iv = findViewById(R.id.imageView1);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WebView wv = findViewById(R.id.webview1);
                wv.loadData(stringFromJNI(pri_key, pin), "text/html" ,null);
                //tv.setText(stringFromJNI());
            }
        });

        WebView wv = findViewById(R.id.webview1);
        wv.setWebViewClient(wvc);
        wv.setVerticalScrollBarEnabled(false);
        // Example of a call to a native method
        wv.loadData(stringFromJNI(pri_key, pin), "text/html" ,null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, PrefsActivity.class);
            startActivity(intent);
            return true;

        }

        return super.onOptionsItemSelected(item);
    }

    class MyWebViewClient extends WebViewClient {
        private ProgressBar pb;

        void setProgressBar(ProgressBar progressBar) {
            this.pb = progressBar;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            pb.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            pb.setVisibility(View.GONE);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     * @param pri_key
     * @param pin
     */
    public String stringFromJNI(String pri_key, String pin) {
        Long cur_time = System.currentTimeMillis()/1000;
        String part = (int)floor(cur_time/10) + pri_key + pin;
        // Log.v("MainActivity", String.valueOf(part));
        String out = md5(part).substring(0, 6);
        return out.replace('a', '0')
                .replace('b', '1')
                .replace('c', '2')
                .replace('d', '3')
                .replace('e', '4')
                .replace('f', '5');
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }
}
