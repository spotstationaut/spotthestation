/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Utilities;

/**
 *
 * @author xxc9071
 */
public class ZoneCalculator
{

    public static int calculateZone(double longitude, double latitude)
    {

        longitude = (longitude + 180) / 36;
        int iLongitude = (int) (longitude + 1);

        latitude = (latitude + 90) / 36;
        int iLatitude = (int) (latitude + 1);

        String zoneString = "" + iLatitude + iLongitude;



        return Integer.parseInt(zoneString);
    }

}
