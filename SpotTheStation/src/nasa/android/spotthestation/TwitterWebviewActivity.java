/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class TwitterWebviewActivity extends Activity {
	private Intent mIntent;
	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter_webview);
		mIntent = getIntent();
		String url = (String) mIntent.getExtras().get("URL");
		WebView webView = (WebView) findViewById(R.id.webview);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (url.contains(TWITTER_CALLBACK_URL)) {
					Uri uri = Uri.parse(url);
					String oauthVerifier = uri.getQueryParameter("oauth_verifier");
					mIntent.putExtra("oauth_verifier", oauthVerifier);
					setResult(RESULT_OK, mIntent);
					finish();
					return true;
				}
				return false;
			}
		});
		webView.loadUrl(url);
	}
}