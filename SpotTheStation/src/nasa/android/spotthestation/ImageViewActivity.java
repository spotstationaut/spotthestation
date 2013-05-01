/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.util.ByteArrayBuffer;
import org.jarjar.apache.commons.codec.binary.Base64;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageViewActivity extends Activity {
	private static final String UPLOAD_IMAGE_URL = "http://matai.aut.ac.nz:8080/NASAControlServer/ImageDownloadServlet?location=";
	private static final int MAX_DOWNLOAD_SIZE = 1024 * 1024; // 1MB
	// Instance variables
	private ImageView imageView;
	private TextView commentTextView;
	private String LOG_TAG = "Test";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_image_view);
		imageView = (ImageView) this.findViewById(R.id.imageView);
		commentTextView = (TextView) this.findViewById(R.id.comment);
		Bundle intentData = getIntent().getExtras();
		double latitude = intentData.getDouble("Lat");
		double longitude = intentData.getDouble("Long");
		new DownloadImageDataTask().execute(latitude + "", longitude + "");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.image_view, menu);
		return true;
	}

	class DownloadImageDataTask extends AsyncTask<String, String, Bitmap> {
		private String comment;

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap imageBitmap = null;
			String latitude = params[0];
			String longitude = params[1];
			String url = UPLOAD_IMAGE_URL + latitude + "A" + longitude;
			Log.d(LOG_TAG, url);
			try {
				URL urlObject = new URL(url);
				HttpURLConnection connection = (HttpURLConnection) urlObject.openConnection();
				connection.connect();
				InputStream input = connection.getInputStream();
				BufferedInputStream bis = new BufferedInputStream(input);
				ByteArrayBuffer baf = new ByteArrayBuffer(MAX_DOWNLOAD_SIZE);
				int current = 0;
				while ((current = bis.read()) != -1) {
					baf.append((byte) current);
				}
				byte[] data = baf.buffer();
				// Calculate the number of bytes for the image comment. {0, 0,
				// 0, data value} as 4 bytes are needed to extract an integer
				byte[] commentByteLength = { (byte) 0, (byte) 0, (byte) 0, data[0] };
				ByteBuffer commentByteLengthBuffer = ByteBuffer.wrap(commentByteLength);
				int commentLength = commentByteLengthBuffer.getInt();
				// Obtain comment
				byte[] commentBytes = new byte[commentLength];
				System.arraycopy(data, 1, commentBytes, 0, commentLength);
				comment = new String(Base64.decodeBase64(commentBytes));
				// Obtain image data
				int imageSrcPos = 1 + commentLength;
				byte[] imageData = new byte[data.length - imageSrcPos];
				System.arraycopy(data, imageSrcPos, imageData, 0, data.length - imageSrcPos);
				imageData = Base64.decodeBase64(imageData);
				imageBitmap = BitmapFactory.decodeByteArray(imageData, 0, imageData.length);
			}
			catch (ClientProtocolException e) {
				e.printStackTrace();
				Log.d(LOG_TAG, "ClientProtocolException");
			}
			catch (IOException e) {
				e.printStackTrace();
				Log.d(LOG_TAG, "IOException");
			}
			return imageBitmap;
		}

		@Override
		protected void onPostExecute(Bitmap photo) {
			imageView.setImageBitmap(photo);
			// Update UI
			commentTextView.setText(comment);
		}
	}
}
