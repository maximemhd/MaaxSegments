package io.github.maximemhd.maaxsegments;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.WebView;

public class WebActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web);

        WebView webview = (WebView) findViewById(R.id.myWebView);
        Intent intent = getIntent();

        String url = intent.getStringExtra("url");

        webview.loadUrl(url);


    }
}
