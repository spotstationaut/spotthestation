/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {
	@Override
	protected void onError(Context arg0, String arg1) {}

	@Override
	protected void onMessage(Context arg0, Intent arg1) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, MapActivity.class);
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
		Log.v("Test", "1");
		// Build notification
		// Actions are just fake
		Notification noti = new Notification.Builder(this).setContentTitle("Sighting Notification").setContentText("You have a sighting notification now").setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent).build();
		Log.v("Test", "2");
		NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		Log.v("Test", "3");
		// Hide the notification after its selected
		noti.flags |= Notification.FLAG_AUTO_CANCEL;
		Log.v("Test", "4");
		notificationManager.notify(0, noti);
	}

	@SuppressWarnings("unused")
	@Override
	protected void onRegistered(Context theContext, String regID) {
		Log.v("Register", "Registering now...");
		String responseString = "";
		DefaultHttpClient client = new DefaultHttpClient();
		// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@ change the address of the server
		HttpGet doGet = new HttpGet("http://matai.aut.ac.nz:8080/NASAControlServer/RegistrationSer" + "vlet?regID=" + regID + "&location=4,9");
		try {
			HttpResponse response = client.execute(doGet);
			HttpEntity entity = response.getEntity();
			InputStream responseContent = entity.getContent();
			// BufferedReader br = new BufferedReader(new
			// InputStreamReader(responseContent));
			//
			// String temp = "";
			// while((temp = br.readLine()) != null)
			// {
			// responseString = responseString + temp;
			// }
		}
		catch (ClientProtocolException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onUnregistered(Context arg0, String arg1) {
		// TODO Auto-generated method stub
	}
}
