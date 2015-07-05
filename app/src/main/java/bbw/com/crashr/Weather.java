package bbw.com.crashr;

import java.net.URL;
import java.util.HashMap;
import java.util.Scanner;

/**
 * Created by cjb60 on 5/07/15.
 */
public class Weather {

    private static HashMap<String,String> map = null;

    static {
        map = new HashMap<>();
        map.put("thunderstorm with light rain","L");
        map.put("thunderstorm with rain","L");
        map.put("thunderstorm with heavy rain","H");
        map.put("thunderstorm with light drizzle","L");
        map.put("thunderstorm with drizzle","L");
        map.put("thunderstorm with heavy drizzle","H");

        map.put("light intensity drizzle","L");
        map.put("drizzle","L");
        map.put("heavy intensity drizzle","H");
        map.put("light intensity drizzle rain","L");
        map.put("drizzle rain","L");
        map.put("heavy intensity drizzle rain","H");
        map.put("shower drizzle","L");

        map.put("light rain","L");
        map.put("moderate rain","L");
        map.put("heavy intensity rain","H");
        map.put("very heavy rain","H");
        map.put("extreme rain","H");
        map.put("freezing rain","H");
        map.put("light intensity shower rain","L");
        map.put("shower rain","L");
        map.put("heavy intensity shower rain","H");

        map.put("light snow","S");
        map.put("snow","S");
        map.put("heavy snow","S");
        map.put("sleet","S");
        map.put("shower snow","S");

        map.put("mist","M");
        map.put("smoke","M");
        map.put("haze","M");
        map.put("sand/dust whirls","M");
        map.put("fog","M");

        map.put("sky is clear","F");
        map.put("few clouds","F");
        map.put("scattered clouds","F");
        map.put("broken clouds","F");
        map.put("overcast clouds","F");

    }

    /**
     * Get the description of weather according to OpenWeatherMap API.
     * @param longitude
     * @param latitude
     * @return a string describing the weather
     * @throws Exception
     */
    private static String getWeatherDescription(double longitude, double latitude) throws Exception {
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=" +
                latitude + "&lon=" + longitude + "&mode=xml";
        String xml = new Scanner(new URL(url).openStream(), "UTF-8").useDelimiter("\\A").next();

        //System.out.println(xml);

        String weatherString = xml.substring( xml.lastIndexOf("<weather"), xml.lastIndexOf("/>"));
        String weather = weatherString.split("value=")[1];
        weather = weather.substring(0, weather.indexOf("icon")).replace("\"", "").trim();
        weather = weather.toLowerCase();
        return weather;
    }

    /**
     * Given a location, return a weather code that corresponds to the one in the NZTA
     * appendix, e.g. F = fine, M = mist, L = light rain, H = heavy rain, and S = snow.
     * @param longitude
     * @param latitude
     * @return a code, or null if we could not map the weather to a code
     * @throws Exception
     */
    public static String getWeatherCode(double longitude, double latitude) throws Exception {
        return map.get( getWeatherDescription(longitude, latitude) );
    }

}
