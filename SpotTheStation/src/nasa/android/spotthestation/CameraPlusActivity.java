/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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

import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Time;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

// Derived from: https://github.com/RaghavSood/ProAndroidAugmentedReality/tree/master/Pro%20Android%20AR%203%20Widget%20Overlay
public class CameraPlusActivity extends Activity
{
    SurfaceView cameraPreview;
    SurfaceHolder previewHolder;
    Camera mCamera;
    boolean inPreview;
    final static String LOG_TAG = "Test";
    SensorManager sensorManager;
    int orientationSensor;
    float headingAngle;
    float pitchAngle;
    float rollAngle;
    int accelerometerSensor;
    float xAxis;
    float yAxis;
    float zAxis;
    LocationManager locationManager;
    double latitude;
    double longitude;
    double altitude;
    TextView latitudeValue;
    TextView longitudeValue;
    // Compass stuff
    private static SensorManager sensorService;
    private Sensor sensor;
    private float azimuth;
    // Capture stuff
    private Button captureButton;
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private boolean takePhoto = true;
    private MyCompassView compassView;
    // ISS data variables
    private static final String GET_ISS_LOCATION_AND_PASS_URL = "http://api.open-notify.org/iss/v1/?";
    private static final String GET_ISS_LOCATION_NOW_URL = "http://api.open-notify.org/iss-now/v1/";
    private long nextPassTimeMillis;
    private long nextPassDurationMillis;
    private float issAzimuth;
    private boolean isISSVisible;
    private TextView isISSVisibleValue;
    private TextView nextVisbilityValue1;
    private TextView nextVisbilityValue2;
    private Timer issTimer;
    private static final int ISS_UPDATE_FREQUENCE = 6000; // ms
    public boolean haveViewsChanged = false;
    private TextView nextVisbilityDurationValue;
    private boolean hasLocationBeenUpdated = false;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_cameraplus);
	locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 2, locationListener);
	sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	orientationSensor = Sensor.TYPE_ORIENTATION;
	accelerometerSensor = Sensor.TYPE_ACCELEROMETER;
	sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
	sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
	inPreview = false;
	cameraPreview = (SurfaceView) findViewById(R.id.cameraPreview);
	previewHolder = cameraPreview.getHolder();
	previewHolder.addCallback(surfaceCallback);
	previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	longitudeValue = (TextView) findViewById(R.id.longitudeValue);
	latitudeValue = (TextView) findViewById(R.id.latitudeValue);
	isISSVisibleValue = (TextView) findViewById(R.id.visbilityValue);
	nextVisbilityValue1 = (TextView) findViewById(R.id.nextVisbilityValue1);
	nextVisbilityValue2 = (TextView) findViewById(R.id.nextVisbilityValue2);
	nextVisbilityDurationValue = (TextView) findViewById(R.id.nextVisbilityDurationValue);
	latitudeValue.setTextColor(Color.WHITE);
	longitudeValue.setTextColor(Color.WHITE);
	// Compass stuff
	compassView = new MyCompassView(this);
	LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
	addContentView(compassView, params);
	sensorService = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
	sensor = sensorService.getDefaultSensor(Sensor.TYPE_ORIENTATION);
	if (sensor != null)
	{
	    sensorService.registerListener(mySensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
	    Log.i("Compass MainActivity", "Registerered for ORIENTATION Sensor");
	}
	else
	{
	    Log.e("Compass MainActivity", "Registerered for ORIENTATION Sensor");
	    Toast.makeText(this, "ORIENTATION Sensor not found", Toast.LENGTH_LONG).show();
	    finish();
	}
	// Capture stuff
	captureButton = (Button) findViewById(R.id.button_capture);
	captureButton.setOnClickListener(new View.OnClickListener()
	{
	    @Override
	    public void onClick(View v)
	    {
		if (takePhoto)
		{
		    // get an image from the camera
		    mCamera.takePicture(null, null, mPicture);
		    captureButton.setText("Release");
		}
		else
		{
		    mCamera.startPreview();
		    captureButton.setText("Capture");
		}
		takePhoto = !takePhoto;
	    }
	});
    }

    private PictureCallback mPicture = new PictureCallback()
    {
	@Override
	public void onPictureTaken(byte[] data, Camera camera)
	{
	    File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
	    if (pictureFile == null)
	    {
		Log.d(LOG_TAG, "Error creating media file, check storage permissions");
		return;
	    }
	    else
	    {
		Log.d(LOG_TAG, "Picture taken");
	    }
	    try
	    {
		FileOutputStream fos = new FileOutputStream(pictureFile);
		fos.write(data);
		fos.close();
	    }
	    catch (FileNotFoundException e)
	    {
		Log.d(LOG_TAG, "File not found: " + e.getMessage());
	    }
	    catch (IOException e)
	    {
		Log.d(LOG_TAG, "Error accessing file: " + e.getMessage());
	    }
	}
    };

    /** Create a File for saving an image or video */
    @SuppressLint("SimpleDateFormat")
    private static File getOutputMediaFile(int type)
    {
	// To be safe, you should check that the SDCard is mounted
	// using Environment.getExternalStorageState() before doing this.
	File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyCameraApp");
	// This location works best if you want the created images to be shared
	// between applications and persist after your app has been uninstalled.
	// Create the storage directory if it does not exist
	if (!mediaStorageDir.exists())
	{
	    if (!mediaStorageDir.mkdirs())
	    {
		Log.d(LOG_TAG, "Filed to create directory");
		return null;
	    }
	}
	// Create a media file name
	String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	File mediaFile;
	if (type == MEDIA_TYPE_IMAGE)
	{
	    mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
	}
	else if (type == MEDIA_TYPE_VIDEO)
	{
	    mediaFile = new File(mediaStorageDir.getPath() + File.separator + "VID_" + timeStamp + ".mp4");
	}
	else
	{
	    return null;
	}
	return mediaFile;
    }

    private SensorEventListener mySensorEventListener = new SensorEventListener()
    {
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
	    // angle between the magnetic north direction
	    // 0=North, 90=East, 180=South, 270=West
	    azimuth = event.values[0];
	    // azimuthValue.setText(String.valueOf(azimuth));
	    compassView.updateData(azimuth, issAzimuth);
	}
    };

    @Override
    protected void onDestroy()
    {
	super.onDestroy();
	if (sensor != null)
	{
	    sensorService.unregisterListener(mySensorEventListener);
	}
    }

    LocationListener locationListener = new LocationListener()
    {
	public void onLocationChanged(Location location)
	{
	    latitude = location.getLatitude();
	    longitude = location.getLongitude();
	    altitude = location.getAltitude();
	    DecimalFormat decimalFormatter = new DecimalFormat("#.##");
	    decimalFormatter.format(latitude);
	    latitudeValue.setText(decimalFormatter.format(latitude));
	    longitudeValue.setText(decimalFormatter.format(longitude));
	    // azimuthValue.setText(String.valueOf(altitude));
	    if (!hasLocationBeenUpdated)
	    {
		// Start updating iss location values.
		// Only start timer once latitude and longitude have been
		// obtained
		issTimer = new Timer();
		issTimer.scheduleAtFixedRate(new ISSTimerTask(), 0, ISS_UPDATE_FREQUENCE);
		hasLocationBeenUpdated = true;
	    }
	}

	public void onProviderDisabled(String arg0)
	{
	}

	public void onProviderEnabled(String arg0)
	{
	}

	public void onStatusChanged(String arg0, int arg1, Bundle arg2)
	{
	}
    };
    final SensorEventListener sensorEventListener = new SensorEventListener()
    {
	@SuppressWarnings("deprecation")
	public void onSensorChanged(SensorEvent sensorEvent)
	{
	    if (sensorEvent.sensor.getType() == Sensor.TYPE_ORIENTATION)
	    {
		headingAngle = sensorEvent.values[0];
		pitchAngle = sensorEvent.values[1];
		rollAngle = sensorEvent.values[2];
	    }
	    else if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
	    {
		xAxis = sensorEvent.values[0];
		yAxis = sensorEvent.values[1];
		zAxis = sensorEvent.values[2];
	    }
	}

	public void onAccuracyChanged(Sensor senor, int accuracy)
	{
	}
    };

    @Override
    public void onResume()
    {
	super.onResume();
	locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 2000, 2, locationListener);
	sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(orientationSensor), SensorManager.SENSOR_DELAY_NORMAL);
	sensorManager.registerListener(sensorEventListener, sensorManager.getDefaultSensor(accelerometerSensor), SensorManager.SENSOR_DELAY_NORMAL);
	mCamera = Camera.open();

	// Start updating iss location values
//	if (!hasLocationBeenUpdated)
//	{
//	    // Start updating iss location values.
//	    // Only start timer once latitude and longitude have been obtained
//	    issTimer = new Timer();
//	    issTimer.scheduleAtFixedRate(new ISSTimerTask(), 0, ISS_UPDATE_FREQUENCE);
//	    hasLocationBeenUpdated = true;
//	}
	Log.d(LOG_TAG, "Starting ISS timer");
    }

    @Override
    public void onPause()
    {
	super.onPause();
	if (inPreview)
	{
	    mCamera.stopPreview();
	}
	locationManager.removeUpdates(locationListener);
	sensorManager.unregisterListener(sensorEventListener);
	releaseCamera(); // release the camera immediately on pause event
	inPreview = false;
	issTimer.cancel();
	Log.d(LOG_TAG, "cancelled issTimer");
	super.onPause();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters)
    {
	Camera.Size result = null;
	for (Camera.Size size : parameters.getSupportedPreviewSizes())
	{
	    if (size.width <= width && size.height <= height)
	    {
		if (result == null)
		{
		    result = size;
		}
		else
		{
		    int resultArea = result.width * result.height;
		    int newArea = size.width * size.height;
		    if (newArea > resultArea)
		    {
			result = size;
		    }
		}
	    }
	}
	return (result);
    }

    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
    {
	public void surfaceCreated(SurfaceHolder holder)
	{
	    try
	    {
		mCamera.setPreviewDisplay(previewHolder);
	    }
	    catch (Throwable t)
	    {
		Log.e(LOG_TAG, "Exception in setPreviewDisplay()", t);
	    }
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
	    Camera.Parameters parameters = mCamera.getParameters();
	    Camera.Size size = getBestPreviewSize(width, height, parameters);
	    if (size != null)
	    {
		parameters.setPreviewSize(size.width, size.height);
		mCamera.setParameters(parameters);
		mCamera.startPreview();
		inPreview = true;
	    }
	}

	public void surfaceDestroyed(SurfaceHolder holder)
	{
	}
    };
    public String visibilityValue1String;
    public String visibilityValue2String;

    private void releaseCamera()
    {
	if (mCamera != null)
	{
	    mCamera.release(); // release the camera for other applications
	    mCamera = null;
	}
    }

    private class ISSTimerTask extends TimerTask
    {
	@Override
	public void run()
	{
	    // Locate ISS
	    new RequestISSLocationTaskAndNextPass().execute(GET_ISS_LOCATION_NOW_URL);
	}
    }

    private class RequestISSLocationTaskAndNextPass extends AsyncTask<String, String, String>
    {
	@Override
	protected String doInBackground(String... uri)
	{
	    HttpClient httpclient = new DefaultHttpClient();
	    String responseString = null;
	    try
	    {
		responseString = getISSCurrentLocationAndNextPass(httpclient, responseString, uri);
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

	private String getISSCurrentLocationAndNextPass(HttpClient httpclient, String responseString, String... uri) throws IOException, ClientProtocolException, JSONException
	{
	    HttpResponse response = httpclient.execute(new HttpGet(uri[0]));
	    StatusLine statusLine = response.getStatusLine();
	    long currentTimeMillis = System.currentTimeMillis();
	    if (statusLine.getStatusCode() == HttpStatus.SC_OK)
	    {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		response.getEntity().writeTo(out);
		out.close();
		responseString = out.toString();
		JSONObject responseJSON = new JSONObject(responseString);
		JSONObject issPosition = responseJSON.getJSONObject("iss_position");
		double issLatitude = issPosition.getDouble("latitude");
		double issLongitude = issPosition.getDouble("longitude");

		// Update current azimuth value - see Wikipedia for azimuth
		// calculation
//		double L = longitude - issLongitude;
		double L = issLongitude-longitude;
		double tan_alpha = Math.sin(Math.toRadians(L)) / (Math.cos(Math.toRadians(latitude)) * Math.tan(Math.toRadians(issLatitude)) - Math.sin(Math.toRadians(latitude)) * Math.cos(Math.toRadians(L)));
		Log.d("Test4", "tan_alpha = " + tan_alpha);
		issAzimuth = (float) Math.atan(tan_alpha);
		
		// Another method for obtaining azimuth:
		// http://keisan.casio.com/has10/SpecExec.cgi?path=06000000.Science%2F02100100.
		// Earth%20science%2F13000300.Distance%20and%20azimuth%20between%20two%20cities%2Fdefault.xml&charset=utf-8
		// 
		// double deltaX = issLatitude-latitude;
		// issAzimuth =
		// (float)(Math.PI/2-Math.atan2(Math.sin(Math.toRadians(deltaX)),
		// Math.cos(Math.toRadians(longitude))*Math.tan(Math.toRadians(issLongitude))-Math.sin(Math.toRadians(longitude))*Math.cos(Math.toRadians(deltaX))));
		
		// Add declination to the magnetic north to obtain true north
		GeomagneticField gf = new GeomagneticField((float) latitude, (float) longitude, (float) 0, currentTimeMillis);
		float declination = (float) Math.toRadians(gf.getDeclination());
		Log.d("Test4", "issAzimuth: "+Math.toDegrees(issAzimuth));
		Log.d("Test4", "declination: "+gf.getDeclination());
		issAzimuth += declination;
	    }
	    else
	    {
		// Closes the connection.
		response.getEntity().getContent().close();
		throw new IOException(statusLine.getReasonPhrase());
	    }

	    // Check if currentTime >= nextPassTime+nextPassDuration to ensure
	    // updating of nextPass Time
	    // long currentTimeMillis = System.currentTimeMillis();
	    if (currentTimeMillis >= (nextPassTimeMillis + nextPassDurationMillis))
	    {
		HttpClient httpclientnew = new DefaultHttpClient();
		String newUri = GET_ISS_LOCATION_AND_PASS_URL + "lat=" + latitude + "&lon=" + longitude + "&alt=100&n=1";
		Log.d("Test2", newUri);
		HttpResponse passResponse = httpclientnew.execute(new HttpGet(newUri));
		StatusLine passStatusLine = passResponse.getStatusLine();
		if (passStatusLine.getStatusCode() == HttpStatus.SC_OK)
		{
		    ByteArrayOutputStream out = new ByteArrayOutputStream();
		    passResponse.getEntity().writeTo(out);
		    out.close();
		    responseString = out.toString();
		    JSONObject responseJSON = new JSONObject(responseString);
		    JSONArray responseArray = responseJSON.getJSONArray("response");
		    JSONObject arrayObject = (JSONObject) responseArray.get(0);
		    nextPassDurationMillis = arrayObject.getLong("duration") * 1000;
		    nextPassTimeMillis = arrayObject.getLong("risetime") * 1000;

		    Time timeText = new Time();
		    timeText.set(nextPassTimeMillis);
		    // Use timeText.MONTH instead of timeText.month, as this is
		    // more reliable.
		    visibilityValue1String = timeText.monthDay + "/" + timeText.MONTH + "/" + timeText.year;

		    // Add leading zero to minutes if they only consist of one
		    // digit
		    int timeMinute = timeText.minute;
		    String minutes = null;
		    if (timeMinute < 10)
		    {
			minutes = "0" + timeMinute;
		    }
		    else
		    {
			minutes = "" + timeMinute;
		    }
		    visibilityValue2String = "@" + timeText.hour + ":" + minutes + " " + timeText.getCurrentTimezone();
		    haveViewsChanged = true;
		    Log.d("Test2", "Fetched new timing: " + timeText.toString());
		    Log.d("Test2", "month: " + timeText.MONTH);
		    Log.d("Test2", "timestamp: " + nextPassTimeMillis);
		}
	    }
	    // Check if ISS is currently visible
	    else if (currentTimeMillis >= nextPassTimeMillis)
	    {
		Log.d("Test5", "Checking");
		if (!isISSVisible)
		{
		    isISSVisible = true;
		    isISSVisibleValue.setTextColor(Color.GREEN);
		    isISSVisibleValue.setText("Yes");
		    Log.d("Test5", "It was false");
		}
	    }
	    else
	    {
		if (isISSVisible)
		{
		    isISSVisible = false;
		    isISSVisibleValue.setTextColor(Color.RED);
		    isISSVisibleValue.setText("No");
		}
	    }

	    return responseString;
	}

	@Override
	protected void onPostExecute(String responseString)
	{
	    if (haveViewsChanged)
	    {
		Log.d("Test2", "Changing text");
		nextVisbilityValue1.setText(visibilityValue1String);
		nextVisbilityValue2.setText(visibilityValue2String);
		Log.d("Test2", "nextVisbilityValue1: " + nextVisbilityValue1.getText());
		Log.d("Test2", "nextVisbilityValue2: " + nextVisbilityValue2.getText());
		nextVisbilityDurationValue.setText(nextPassDurationMillis / 1000 + "s");
		haveViewsChanged = false;
	    }
	}
    }
}