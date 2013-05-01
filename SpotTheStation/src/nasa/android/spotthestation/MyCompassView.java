/*
 * Written by Eleanor Da Fonseca, Weixiong Cen, Harrison Black & Boris Feron
 */

package nasa.android.spotthestation;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

public class MyCompassView extends View
{
    private Paint paint;
    private float azimuth = 0;
    private float issAzimuth = 0;

    public MyCompassView(Context context)
    {
	super(context);
	init();
    }

    private void init()
    {
	paint = new Paint();
	paint.setAntiAlias(true);
	paint.setStrokeWidth(2);
	paint.setTextSize(25);
	paint.setStyle(Paint.Style.STROKE);
	paint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
	int xCentre = getMeasuredWidth() / 2;
	int yCentre = getMeasuredHeight() / 2;
	float radius = (float) (Math.max(xCentre, yCentre) * 0.45);
	paint.setColor(Color.WHITE);
	canvas.drawCircle(xCentre, yCentre, radius, paint);
	paint.setColor(Color.RED);
	float xNorth = (float) (xCentre + radius * Math.sin((double)Math.toRadians(-azimuth)));
	float yNorth = (float) (yCentre - radius * Math.cos((double)Math.toRadians(-azimuth)));
	canvas.drawLine(xCentre, yCentre, xNorth, yNorth, paint);
	canvas.drawText("N", xNorth, yNorth, paint);
	paint.setColor(Color.BLUE);

	float xISS = (float) (xCentre + radius * Math.sin((double)Math.toRadians(-(azimuth - issAzimuth))));
	float yISS = (float) (yCentre - radius * Math.cos((double)Math.toRadians(-(azimuth - issAzimuth))));
	canvas.drawLine(xCentre, yCentre, xISS, yISS, paint);
	canvas.drawText("ISS", xISS, yISS, paint);
	paint.setColor(Color.GREEN);
	canvas.drawLine(xCentre, yCentre, xCentre, yCentre - radius, paint);
	canvas.drawText("User", xCentre, yCentre - radius, paint);
    }

    public void updateData(float azimuthUpdated, float issAzimuth)
    {
	this.azimuth = (azimuthUpdated + 90) % 360; // The compass of our phone is offset by 90 degrees
						    // of our screen orientation and hence we must add 90.
						    // We take the value modulo 360 to ensure we have no
						    // angle greater than this number.
	this.issAzimuth = (float)-Math.toDegrees(issAzimuth);
	invalidate();
    }
}
