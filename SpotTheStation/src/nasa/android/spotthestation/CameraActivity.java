/*
 * "CameraActivity.java"
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

@SuppressLint("SimpleDateFormat")
public class CameraActivity extends Activity {
	private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 2;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	private Uri fileUri;
	private double latitude, longitude;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);
		Bundle extra = getIntent().getExtras();
		latitude = extra.getDouble("Lat");
		longitude = extra.getDouble("Long");
		// Create a file to save the image
		fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
		// Start native camera app
		Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
		startActivityForResult(cameraIntent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
	}


	public void shareToNASA(View view) {
		Intent activityIntent = new Intent(this, UploadToNasaServerActivity.class);
		activityIntent.putExtra("PhotoURI", fileUri);
		activityIntent.putExtra("Lat", latitude);
		activityIntent.putExtra("Long", longitude);
		startActivity(activityIntent);
	}

	public void shareToTwitter(View view) {
		SharedPreferences settings = getApplicationContext().getSharedPreferences("MyPref", 0);
		Editor editor = settings.edit();
		editor.putString("fileUri", fileUri.getPath());
		editor.commit();
		Intent twitterActivityIntent = new Intent(CameraActivity.this, TwitterActivity.class);
		twitterActivityIntent.putExtra("fileUri", fileUri);
		startActivity(twitterActivityIntent);
		finish();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != RESULT_CANCELED) {
			if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
				Log.v("Test", "Is data null??" + data);
				// Bitmap photo = (Bitmap) data.getExtras().get("data");
				// ByteArrayOutputStream baos = new ByteArrayOutputStream();
				// photo.compress(Bitmap.CompressFormat.JPEG, 100, baos);
				// photoByteArray = baos.toByteArray();
			}
		}
	}

	private static Uri getOutputMediaFileUri(int type) {
		return Uri.fromFile(getOutputMediaFile(type));
	}

	private static File getOutputMediaFile(int type) {
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.
		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.
		// Create the storage directory if it does not exist
		if (!mediaStorageDir.exists()) {
			if (!mediaStorageDir.mkdirs()) {
				Log.d("Test", "failed to create directory");
				return null;
			}
		}
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
		}
		else if (type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
		}
		else {
			return null;
		}
		return mediaFile;
	}
}
