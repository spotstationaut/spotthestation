/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import java.io.File;
import java.util.concurrent.ExecutionException;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import nasa.android.spotthestation.R;
import nasa.android.spotthestation.TwitterWebviewActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class TwitterActivity extends Activity {
	private final static String TWITPIC_API_KEY = "INSERT_CUSTOM_TWITPIC_API_KEY";
	private Twitter twitter;
	private RequestToken requestToken;
	private String OAUTH_CONSUMER_KEY = "INSERT_CUSTOM_OAUTH_CONSUMER_KEY";
	private String OAUTH_CONSUMER_SECRET = "INSERT_CUSTOM_OATH_CONSUMER_SECRET";
	private Context context;
	private SharedPreferences settings;
	// Preference Constants
	static String PREFERENCE_NAME = "twitter_oauth";
	static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
	static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
	static final String PREF_KEY_TWITTER_LOGIN = "isTwitterLogedIn";
	static final String TWITTER_CALLBACK_URL = "oauth://t4jsample";
	// Twitter oauth urls
	static final String URL_TWITTER_AUTH = "auth_url";
	static final String URL_TWITTER_OAUTH_VERIFIER = "oauth_verifier";
	static final String URL_TWITTER_OAUTH_TOKEN = "oauth_token";
	static final int TWITTER_AUTH = 102;
	private String fileUriPath;
	private Button updateStatusButton;
	private EditText statusText;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_twitter);
		settings = getApplicationContext().getSharedPreferences("MyPref", 0);
		fileUriPath = settings.getString("fileUri", "file path not found");
		twitter = new TwitterFactory().getInstance();
		requestToken = null;
		twitter.setOAuthConsumer(OAUTH_CONSUMER_KEY, OAUTH_CONSUMER_SECRET);
		context = getApplicationContext();
		AsyncTask<String, String, String> requestTokenTask = new RequestTokenTask();
		requestTokenTask.execute("");
		try {
			requestTokenTask.get();
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
		catch (ExecutionException e) {
			e.printStackTrace();
		}
		Intent i = new Intent(context, TwitterWebviewActivity.class);
		i.putExtra("URL", requestToken.getAuthenticationURL());
		startActivityForResult(i, TWITTER_AUTH);
		this.statusText = (EditText) this.findViewById(R.id.txtUpdateStatus);
		this.updateStatusButton = (Button) this.findViewById(R.id.btnUpdateStatus);
		this.updateStatusButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Toast.makeText(TwitterActivity.this, "Tweeting .....", Toast.LENGTH_LONG).show();
				String status = statusText.getText().toString();
				UpdateStatusTask updateTask = new UpdateStatusTask(status);
				updateTask.execute("");
				try {
					updateTask.get();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				catch (ExecutionException e) {
					e.printStackTrace();
				}
				finish();
			}
		});
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == Activity.RESULT_OK) {
			OAuthAccessTokenTask oAuthTask = new OAuthAccessTokenTask();
			oAuthTask.setOauthVerifier((String) data.getExtras().get("oauth_verifier"));
			oAuthTask.execute("");
			try {
				oAuthTask.get();
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
			catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
	}

	class UpdateStatusTask extends AsyncTask<String, String, String> {
		public String statusString;

		public UpdateStatusTask(String statusString) {
			this.statusString = statusString;
		}

		@Override
		protected String doInBackground(String... uri) {
			publishProgress("");
			String accessToken = settings.getString("twitter_access_token", null);
			String accessTokenSecret = settings.getString("twitter_access_token_secret", null);
			Configuration conf = new ConfigurationBuilder().setMediaProviderAPIKey(TWITPIC_API_KEY).setOAuthConsumerKey(OAUTH_CONSUMER_KEY).setOAuthConsumerSecret(OAUTH_CONSUMER_SECRET).setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret).build();
			Twitter twitter = new TwitterFactory(conf).getInstance();
			try {
				File file = new File(fileUriPath);
				if (file != null) {
					StatusUpdate status = new StatusUpdate(statusString);
					status.setMedia(file);
					twitter.updateStatus(status);
				}
			}
			catch (TwitterException e) {
				Log.d("TAG", "Twitter Exception: Photo not uploaded" + e.getErrorMessage());
			}
			return "Done";
		}

		protected void onProgressUpdate(String... progress) {
			Toast.makeText(context, "Tweet sent", Toast.LENGTH_SHORT).show();
		}
	}

	private class RequestTokenTask extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... uri) {
			try {
				requestToken = twitter.getOAuthRequestToken(TWITTER_CALLBACK_URL);
			}
			catch (TwitterException e) {
				e.printStackTrace();
			}
			return "Done";
		}
	}

	class OAuthAccessTokenTask extends AsyncTask<String, String, String> {
		private String oauthVerifier;

		protected void setOauthVerifier(String oauthVerifier) {
			this.oauthVerifier = oauthVerifier;
		}

		@Override
		protected String doInBackground(String... uri) {
			try {
				// Pair up our request with the response
				AccessToken at = null;
				at = twitter.getOAuthAccessToken(requestToken, oauthVerifier);
				settings.edit().putString("twitter_access_token", at.getToken()).putString("twitter_access_token_secret", at.getTokenSecret()).commit();
			}
			catch (TwitterException e) {
				e.printStackTrace();
			}
			return "Done";
		}
	}
}
