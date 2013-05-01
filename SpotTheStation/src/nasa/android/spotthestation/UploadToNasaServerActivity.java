/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jarjar.apache.commons.codec.binary.Base64;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class UploadToNasaServerActivity extends Activity {
	// Static variables
	private final String UPLOAD_TO_NASA_URL = "http://matai.aut.ac.nz:8080/NASAControlServer/ImageUploadServlet";
	// Instance variables
	private EditText commentField;
	private double latitude, longitude;
	private Bitmap imageBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_upload_to_nasa_server);
		Bundle intentBundle = getIntent().getExtras();
		Uri photoURI = (Uri) intentBundle.get("PhotoURI");
		BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();
		bmpFactoryOptions.inSampleSize = 10; // 1/10 of the quality
		bmpFactoryOptions.inJustDecodeBounds = false;
		imageBitmap = BitmapFactory.decodeFile(photoURI.getPath(), bmpFactoryOptions);
		Log.v("Test", "Bitmap = " + imageBitmap);
		commentField = (EditText) findViewById(R.id.commentField);
		latitude = intentBundle.getDouble("Lat");
		longitude = intentBundle.getDouble("Long");
		Log.v("Test", "Lat = " + latitude + " Long = " + longitude);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.upload_to_nasa_server, menu);
		return true;
	}

	public void nasaUpload(View v) {
		new UploadToNASA().execute("");
	}

	private class UploadToNASA extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... uri) {
			ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
			imageBitmap.compress(CompressFormat.JPEG, 100, imageBytes);
			Log.v("Test", "image byte length: " + imageBytes.toByteArray().length);
			uploadToNASA(imageBytes.toByteArray());
			return "Done";
		}

		private void uploadToNASA(byte[] photo, String... uri) {
			String photoBase64Encoded = Base64.encodeBase64URLSafeString(photo);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			String commentBase64ed = Base64.encodeBase64URLSafeString(("" + latitude + ";" + longitude + ";" + photo.length + ";" + commentField.getText().toString()).getBytes());
			Log.v("Test", "commentBase64ed: " + commentBase64ed);
			nameValuePairs.add(new BasicNameValuePair(commentBase64ed, photoBase64Encoded));
			try {
				HttpClient httpclient = new DefaultHttpClient();
				HttpPost httppost = new HttpPost(UPLOAD_TO_NASA_URL);
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				httpclient.execute(httppost);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void onPostExecute(String data) {
			Toast.makeText(getApplicationContext(), "Message sent", Toast.LENGTH_SHORT).show();
			Intent activityIntent = new Intent(getApplicationContext(), MapActivity.class);
			startActivity(activityIntent);
		}
	}
}
