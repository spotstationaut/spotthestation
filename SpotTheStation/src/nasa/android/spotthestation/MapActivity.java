/*
 * "MapActivity.java": Shows a GoogleMap on which are shown both the location of the
 * user along with the location of the International Space Station,
 * taken from coordinates given by "http://api.open-notify.org/iss-now/v1/".
 * 
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 * 
 */

package nasa.android.spotthestation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Switch;

import nasa.android.spotthestation.CameraPlusActivity;
import com.google.android.gcm.GCMRegistrar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity implements CompoundButton.OnCheckedChangeListener
{
    // Static variables
    private static final String WUNDERGROUND_API_KEY = "INSERT_CUSTOM_WUNDERGROUND_KEY";
    private static final String GET_USER_WEATHER_URL = "http://api.wunderground.com/api/" + WUNDERGROUND_API_KEY + "/forecast/q/";
    private static final String MARKER_DOWNLOAD_URL = "http://matai.aut.ac.nz:8080/NASAControlServer/MarkerDownloadServlet";
    private static final String GET_ISS_LOCATION_NOW_URL = "http://api.open-notify.org/iss-now/v1/";
    private static final String TITLE_LOCATION_ISS = "ISS Location";
    private static final String TITLE_LOCATION_USER = "You are here";
    private static final int ISS_UPDATE_FREQUENCE = 3000; // ms
    private static final String GCM_PROJECT_ID = "202762191232";
    private static final int GCM_REGISTRATION_DELAY = 5000;// ms

    // Instance variables
    private GoogleMap googleMap;
    private boolean mapPropertiesSet;
    private Marker userLocationMarker;
    private Marker issLocationMarker;
    private String weatherInfoForUser;
    private boolean weatherRetrieved;
    private LatLng latLngOfISS;
    private String[] menuHeaders =
    { "Share ISS Sighting", "Go to Your Location", "Find the ISS", "Use CameraPlus", "Help" };
    private Timer issTimer;

    @SuppressWarnings("static-access")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_map);
	// Initialize variables
	mapPropertiesSet = false;
	weatherRetrieved = false;
	weatherInfoForUser = "";
	userLocationMarker = null;
	issLocationMarker = null;
	// Obtain reference to map
	googleMap = ((SupportMapFragment) (getSupportFragmentManager().findFragmentById(R.id.map))).getMap();
	// Set map listener
	googleMap.setOnInfoWindowClickListener(new InfoWindowListener());
	// Start GPS
	LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	locationManager.requestLocationUpdates(locationManager.NETWORK_PROVIDER, 100, 1, new LocationFinder());
	// Timer issTimer = new Timer();
	// issTimer.scheduleAtFixedRate(new ISSTimerTask(), 0,
	// ISS_UPDATE_FREQUENCE);
	// Set to on by default
	Switch imageMarkerSwitch = (Switch) findViewById(R.id.markerSwitch);
	imageMarkerSwitch.setChecked(true);
	imageMarkerSwitch.setOnCheckedChangeListener(this);
	// Start GCM registration after delay to allow for GPS to start
	new Timer().schedule(new RegisterTimerTask(), GCM_REGISTRATION_DELAY);
    }

    @Override
    protected void onResume()
    {
	super.onResume();
	// Downloading of image marker data initially and when returning to the
	// map
	new DownloadImageMarkerDataTask().execute("");

	// Start updating iss location values
	issTimer = new Timer();
	issTimer.scheduleAtFixedRate(new ISSTimerTask(), 0, ISS_UPDATE_FREQUENCE);
    }

    @Override
    protected void onPause()
    {
	super.onPause();
	// Stop updating of iss location when activity is suspended
	issTimer.cancel();
    }

    private class RegisterTimerTask extends TimerTask
    {
	@Override
	public void run()
	{
	    Context applicationContext = getApplicationContext();
	    GCMRegistrar.checkDevice(applicationContext);
	    GCMRegistrar.checkManifest(applicationContext);
	    final String regId = GCMRegistrar.getRegistrationId(applicationContext);
	    if (regId.equals(""))
	    {
		GCMRegistrar.register(applicationContext, GCM_PROJECT_ID);
	    }
	    else
	    {
		Log.v("regId", regId);
	    }
	}
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
	menu.add(menuHeaders[2]);
	menu.add(menuHeaders[1]);
	menu.add(menuHeaders[0]);
	menu.add(menuHeaders[3]);
	menu.add(menuHeaders[4]);
	// Return true to create the menu
	return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
	CharSequence menuHeader = item.getTitle();
	if (menuHeader.equals(menuHeaders[0]))
	{
	    Intent cameraActivityIntent = new Intent(this, CameraActivity.class);
	    cameraActivityIntent.putExtra("Lat", userLocationMarker.getPosition().latitude);
	    cameraActivityIntent.putExtra("Long", userLocationMarker.getPosition().longitude);
	    startActivity(cameraActivityIntent);
	}
	else if (menuHeader.equals(menuHeaders[1]))
	{
	    goToLocation(userLocationMarker, 10);
	}
	else if (menuHeader.equals(menuHeaders[2]))
	{
	    goToLocation(issLocationMarker, 10);
	}
	else if (menuHeader.equals(menuHeaders[3]))
	{
	    // CameraPlus
	    Intent cameraPlusActivityIntent = new Intent(MapActivity.this, CameraPlusActivity.class);
	    startActivity(cameraPlusActivityIntent);
	}
	else if (menuHeader.equals(menuHeaders[4]))
	{
	    // About or Help
	    Intent helpActivityIntent = new Intent(MapActivity.this, HelpActivity.class);
	    startActivity(helpActivityIntent);
	}
	return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
	if (isChecked)
	{
	    new DownloadImageMarkerDataTask().execute("");
	}
	else
	{
	    // Remove all
	    googleMap.clear();
	    // Re-add other markers
	    addOrUpdateUserMarker(userLocationMarker.getPosition());
	}
    }

    private void goToLocation(Marker location, float zoomLevel)
    {
	if (googleMap != null && location != null)
	{
	    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location.getPosition(), zoomLevel));
	}
    }

    private void setMapProperties(LatLng latLng)
    {
	mapPropertiesSet = true;
	googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
	googleMap.getUiSettings().setCompassEnabled(true);
	googleMap.getUiSettings().setZoomControlsEnabled(true);
	googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10));
    }

    private void addOrUpdateUserMarker(LatLng userLatLng)
    {
	if (userLocationMarker != null)
	{
	    userLocationMarker.remove();
	}
	userLocationMarker = googleMap.addMarker(new MarkerOptions().position(userLatLng).title(TITLE_LOCATION_USER).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
	if (weatherInfoForUser != null)
	{
	    userLocationMarker.setSnippet(weatherInfoForUser);
	}
	if (issLocationMarker != null)
	{
	    issLocationMarker.remove();
	}
	if (latLngOfISS != null && latLngOfISS.latitude != 0 && latLngOfISS.longitude != 0)
	{
	    issLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLngOfISS).title(TITLE_LOCATION_ISS).icon(BitmapDescriptorFactory.fromResource(R.drawable.isstracker_logo)));
	}
    }

    private class DownloadImageMarkerDataTask extends AsyncTask<String, String, JSONArray>
    {
	@Override
	protected JSONArray doInBackground(String... params)
	{
	    // Download image marker data
	    HttpClient httpclient = new DefaultHttpClient();
	    String responseString = null;
	    JSONArray markerData = null;
	    try
	    {
		HttpResponse response = httpclient.execute(new HttpGet(MARKER_DOWNLOAD_URL));
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK)
		{
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    response.getEntity().writeTo(out);
		    out.close();
		    responseString = out.toString();
		    JSONObject responseJSON = new JSONObject(responseString);
		    markerData = responseJSON.getJSONArray("Locations");
		}
		else
		{
		    // Closes the connection.
		    response.getEntity().getContent().close();
		    throw new IOException(statusLine.getReasonPhrase());
		}
	    }
	    catch (ClientProtocolException e)
	    {
		e.printStackTrace();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    catch (JSONException e)
	    {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	    return markerData;
	}

	@Override
	protected void onPostExecute(JSONArray markerData)
	{
	    super.onPostExecute(markerData);
	    if (markerData != null)
	    {
		for (int i = 0; i < markerData.length(); i++)
		{
		    JSONObject location;
		    try
		    {
			// Setup marker data
			location = (JSONObject) markerData.get(i);
			LatLng coordinate = new LatLng(Double.parseDouble(location.getString("Lat")), Double.parseDouble(location.getString("Long")));
			MarkerOptions mapMarker = new MarkerOptions();
			mapMarker.position(coordinate);
			mapMarker.title("Image Marker");
			mapMarker.snippet("Click here for location image");
			// Add marker to map
			googleMap.addMarker(mapMarker);
		    }
		    catch (JSONException e)
		    {
			// TODO Auto-generated catch block
			e.printStackTrace();
		    }
		}
	    }
	}
    }

    private class InfoWindowListener implements OnInfoWindowClickListener
    {
	@Override
	public void onInfoWindowClick(Marker markerWindowSelected)
	{
	    LatLng latLngClicked = markerWindowSelected.getPosition();
	    double latitude = latLngClicked.latitude;
	    double longitude = latLngClicked.longitude;
	    Intent activityIntent = new Intent(MapActivity.this, ImageViewActivity.class);
	    activityIntent.putExtra("Lat", latitude);
	    activityIntent.putExtra("Long", longitude);
	    startActivity(activityIntent);
	}
    }

    private class ISSTimerTask extends TimerTask
    {
	@Override
	public void run()
	{
	    // Locate ISS
	    new RequestISSLocationTask().execute(GET_ISS_LOCATION_NOW_URL);
	}
    }

    private class RequestWeatherTask extends AsyncTask<String, String, String>
    {
	@Override
	protected String doInBackground(String... uri)
	{
	    String weatherInfo = getWeatherInfoForUsersLocation(uri[0]);
	    return weatherInfo;
	}

	private String getWeatherInfoForUsersLocation(String url)
	{
	    HttpClient httpclient = new DefaultHttpClient();
	    String responseString = null;
	    try
	    {
		HttpResponse response = httpclient.execute(new HttpGet(url));
		StatusLine statusLine = response.getStatusLine();
		if (statusLine.getStatusCode() == HttpStatus.SC_OK)
		{
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    response.getEntity().writeTo(out);
		    out.close();
		    responseString = out.toString();
		    JSONObject responseJSON = new JSONObject(responseString);
		    JSONObject forecastJSON = responseJSON.getJSONObject("forecast");
		    JSONObject txt_forecastJSON = forecastJSON.getJSONObject("txt_forecast");
		    JSONArray forecastdayJSON = txt_forecastJSON.getJSONArray("forecastday");
		    JSONObject weatherForecast = (JSONObject) forecastdayJSON.get(0); // Latitude
		    String finalString = weatherForecast.getString("fcttext_metric");
		    weatherInfoForUser = finalString;
		    weatherRetrieved = true;
		}
		else
		{
		    // Closes the connection.
		    response.getEntity().getContent().close();
		    throw new IOException(statusLine.getReasonPhrase());
		}
	    }
	    catch (ClientProtocolException e)
	    {
		e.printStackTrace();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    catch (JSONException e)
	    {
		e.printStackTrace();
	    }
	    return responseString;
	}
    }

    private class LocationFinder implements LocationListener
    {
	@Override
	public void onLocationChanged(Location location)
	{
	    double latitude = location.getLatitude();
	    double longitude = location.getLongitude();
	    LatLng latLng = new LatLng(latitude, longitude);
	    if (!mapPropertiesSet)
	    {
		setMapProperties(latLng);
	    }
	    addOrUpdateUserMarker(latLng);
	    if (!weatherRetrieved)
	    {
		String getUserWeatherURL = GET_USER_WEATHER_URL + latitude + "," + longitude + ".json";
		new RequestWeatherTask().execute(getUserWeatherURL);
	    }
	}

	@Override
	public void onProviderDisabled(String provider)
	{
	    // TODO Auto-generated method stub
	}

	@Override
	public void onProviderEnabled(String provider)
	{
	    // TODO Auto-generated method stub
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
	    // TODO Auto-generated method stub
	}
    }

    private class RequestISSLocationTask extends AsyncTask<String, String, String>
    {
	@Override
	protected String doInBackground(String... uri)
	{
	    HttpClient httpclient = new DefaultHttpClient();
	    String responseString = null;
	    try
	    {
		responseString = getISSCurrentLocation(httpclient, responseString, uri);
	    }
	    catch (ClientProtocolException e)
	    {
		e.printStackTrace();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace();
	    }
	    catch (JSONException e)
	    {
		e.printStackTrace();
	    }
	    return responseString;
	}

	private String getISSCurrentLocation(HttpClient httpclient, String responseString, String... uri) throws IOException, ClientProtocolException, JSONException
	{
	    HttpResponse response = httpclient.execute(new HttpGet(uri[0]));
	    StatusLine statusLine = response.getStatusLine();
	    if (statusLine.getStatusCode() == HttpStatus.SC_OK)
	    {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.getEntity().writeTo(out);
		out.close();
		responseString = out.toString();
		JSONObject responseJSON = new JSONObject(responseString);
		JSONObject issPosition = responseJSON.getJSONObject("iss_position");
		String latitudeOfSS = issPosition.getString("latitude"); // Latitude
		String longitudeOfSS = issPosition.getString("longitude"); // Longitude
		latLngOfISS = new LatLng(Double.valueOf(latitudeOfSS), Double.valueOf(longitudeOfSS));
	    }
	    else
	    {
		// Closes the connection.
		response.getEntity().getContent().close();
		throw new IOException(statusLine.getReasonPhrase());
	    }
	    return responseString;
	}
    }
}
