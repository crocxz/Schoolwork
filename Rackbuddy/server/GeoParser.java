package ubc.cs.cpsc310.rackbuddy.server;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;


public class GeoParser {

	private final String USER_AGENT = "Mozilla/5.0";
	private String BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=";
	private String apiKey;
	private HashMap<String, JSONObject> cache;


	public GeoParser(String apiKey) {
		this.apiKey = apiKey;
		cache = new HashMap<String, JSONObject>();
	}

	//public methods for geocoding suggested bike racks
	public double getLatitude(String address) {
		if(!cache.containsKey(address)) {
			getGeoCodeJSON(address);	//get it and add to cache
		}
		JSONObject locationObject = cache.get(address);
		if(locationObject == null) {
			System.out.println("ERROR: Couldn't get Latitude for the given address");
			return 0.0;
		}
		
		try {
			return (double) locationObject.get("lat");
		} catch(Exception e) {
			System.out.println("ERROR: Google Maps API replied but it didn't contain the latitude of the given address.");
			return 0.0;
		}
	}

	public double getLongitude(String address) {
		if(!cache.containsKey(address)) {
			getGeoCodeJSON(address);	//get it and add to cache
		}
		JSONObject locationObject = cache.get(address);
		if(locationObject == null) {
			System.out.println("ERROR: Couldn't get Longitude for the given address");
			return 0.0;
		}
		
		try {
			return (double) locationObject.get("lng");
		} catch(Exception e) {
			System.out.println("ERROR: Google Maps API replied but it didn't contain the latitude of the given address.");
			return 0.0;
		}
	}


	//address can contain spaces
	public String makeRequestURL(String address) {
		String addressNoSpaces = address.replaceAll("\\s+", "+");
		return BASE_URL + addressNoSpaces + "&key=" + apiKey;
	}


	public void getGeoCodeJSON(String address) {
		StringBuffer response = new StringBuffer();
		try {
			String stringURL = makeRequestURL(address);

			URL url = new URL(stringURL);

			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;

			while ((line = reader.readLine()) != null) {
				response.append(line);
			}

			reader.close();
			JSONObject jsonObject = new JSONObject(response.toString());
			JSONObject locationObject = getLocationJSONObject(jsonObject);
			cache.put(address, locationObject);
			
		} catch(Exception e) {
			System.out.println("ERROR: Could not retrieve information from Google Maps API");
			cache.put(address, null);
		}

	}

	public JSONObject getLocationJSONObject(JSONObject jsonObject) {
		try {
			JSONArray arr = (JSONArray) jsonObject.get("results");
			JSONObject resultsObject = (JSONObject) arr.get(0);
			JSONObject geometryObject = (JSONObject) resultsObject.get("geometry");
			JSONObject locationObject = (JSONObject) geometryObject.get("location");
			return locationObject;
		} catch (Exception e) {
			System.out.println("ERROR: The Google Maps API replied but the json received did not contain coordinate information. Does this place exist?");
			return null;
		}
	}

}
