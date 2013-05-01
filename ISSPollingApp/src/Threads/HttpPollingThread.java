package Threads;

import Utilities.ZoneCalculator;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.swing.JLabel;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author xxc9071
 */
public class HttpPollingThread implements ActionListener
{

    private static final String ISS_URL = "http://api.open-notify.org/iss-now/v1/";
    private static final String NASA_CONTROL_SERVER = "http://matai:8080/NASAControlServer/"
            + "resources/notification/notify/";
    private JLabel statusLabel;

    public HttpPollingThread(JLabel statusLabel)
    {
        this.statusLabel = statusLabel;
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        URL url;
        HttpURLConnection connection = null;
        try
        {
            // Obtain location of the ISS
            url = new URL(ISS_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Send the connection
            InputStream is = connection.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String response = rd.readLine();

            String latitude = response.substring(response.indexOf("latitude") + 11);
            latitude = latitude.substring(0, latitude.indexOf(","));
            double latitudeDouble = Double.parseDouble(latitude);

            String longitude = response.substring(response.indexOf("longitude") + 12);
            longitude = longitude.substring(0, longitude.indexOf("}"));
            double longitudeDouble = Double.parseDouble(longitude);
            rd.close();
            connection.disconnect();


            int issZone = ZoneCalculator.calculateZone(longitudeDouble, latitudeDouble);

            // Upload data to our server !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            url = new URL(NASA_CONTROL_SERVER + 49);
            System.out.println("url: " + url);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Send the connection
            is = connection.getInputStream();


            // Update status
            statusLabel.setText("ISS last location: " + latitude + ", " + longitude);

        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
        finally
        {
            if (connection != null)
            {
                connection.disconnect();
            }
        }
    }
}
