package ca.ubc.cpsc210.waldo.waldowebservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import ca.ubc.cpsc210.waldo.exceptions.WaldoException;
import ca.ubc.cpsc210.waldo.model.Waldo;
import ca.ubc.cpsc210.waldo.util.LatLon;

public class WaldoService {

	private final static String WALDO_WEB_SERVICE_URL = "http://kramer.nss.cs.ubc.ca:8080/";
private String name; 
private String key;
private List<String> messages;

private List<Waldo> waldos;

	/**
	 * Constructor
	 */
	public WaldoService() {
		name = "";
		key = ""; 
		waldos = new ArrayList<Waldo>();
	}

	/**
	 * Initialize a session with the Waldo web service. The session can time out
	 * even while the app is active...
	 * 
	 * @param nameToUse
	 *            The name to go register, can be null if you want Waldo to
	 *            generate a name
	 * @return The name that Waldo gave you
	 */
	public String initSession(String nameToUse) {
		// CPSC 210 Students. You will need to complete this method
		StringBuilder urlBuilder = new StringBuilder(
				WALDO_WEB_SERVICE_URL);
		System.out.println("urlbuilder started");
		if (nameToUse == null) {
			urlBuilder.append("initsession/" + "");
		} else {
			urlBuilder.append("initsession/" + nameToUse);
			}
		System.out.println("urlbuilder started:" + urlBuilder);
		InputStream in = null;
		try {
			String s = makeJSONQuery(urlBuilder);
			String gottename =  parseInitSession(s);
			return gottename;
		} catch (Exception e) {
			// Return an empty set of stuff but let developer know
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new WaldoException(
						"initSession: Unable to open or read return from http request.");
			}
		}
		return "";
	}
		
	public String parseInitSession(String input) {
		JSONObject obj;
		try {
		obj = (JSONObject) new JSONTokener(input).nextValue();	
		if (obj != null) {
			this.name = obj.getString("Name");
			
			this.key = obj.getString("Key");
			return name; 
		}
		
		
		}  catch (JSONException e) {
			// Let the developer know but just return whatever is in stopsFound. Probably there was an
			// error in the JSON returned.
			e.printStackTrace();
		}
		return name; 
	}

	/**
	 * Get waldos from the Waldo web service.
	 * 
	 * @param numberToGenerate
	 *            The number of Waldos to try to retrieve
	 * @return Waldo objects based on information returned from the Waldo web
	 *         service
	 */
	public List<Waldo> getRandomWaldos(int numberToGenerate) {
		// CPSC 210 Students: You will need to complete this method
	
		//set up url
		System.out.println("key is:" + key);
		StringBuilder urlBuilder = new StringBuilder(
				WALDO_WEB_SERVICE_URL);
		urlBuilder.append("getwaldos/" + key + "/" + numberToGenerate);
		
		InputStream in = null;
		try {
			//make url query and run parser, return output
			String s = makeJSONQuery(urlBuilder);
			List<Waldo> output = parseGetRandomWaldos(s);
			System.out.println("parser ran!");
			return output;
		} catch (Exception e) {
			// Return an empty set  but let developer know
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new WaldoException(
						": Unable to open or read return from http request.");
			}
		}
		//something went wrong so we return emptyset
		return new ArrayList<Waldo>();
	}

	public List<Waldo> parseGetRandomWaldos(String input) {
		 List<Waldo> waldolist = new ArrayList<Waldo>();
		JSONArray obj1;
		
		try {
		obj1 = (JSONArray) new JSONTokener(input).nextValue();	
		if (obj1 != null) {
			for (int i = 0; i < obj1.length(); i++) {
				
				//Retrieve waldo and its fields
				JSONObject waldo = obj1.getJSONObject(i);
				String waldoname = waldo.getString("Name");
				JSONObject loc;
				Long tstamp;
				
				//get location object and make date and latlon objects
				loc = (JSONObject) new JSONTokener(waldo.getString("Loc")).nextValue();
				System.out.println("loc read into json object");
				LatLon location = new LatLon(loc.getDouble("Lat"), loc.getDouble("Long"));
				System.out.println("Latlon created from json object");
				Date time = new Date();
				tstamp = loc.getLong("Tstamp");
				System.out.println("Tstamp obtained:" + tstamp.toString());
				time.setTime(tstamp*1000);
				System.out.println("Time set:" + time.toString());
				
				//make waldo and add
				Waldo newaldo = new Waldo(waldoname, time, location);
				waldolist.add(newaldo);
				waldos.add(newaldo);
				System.out.println("waldo made and added!");
			}
			System.out.println("all waldos added to list!");
			
		}
		} catch (JSONException e) {
			// Let the developer know but just return whatever is in stopsFound. Probably there was an
			// error in the JSON returned.
			e.printStackTrace();
		}
		System.out.println("finishing parsing of waldolist");
		return waldolist; 
	}
	/**
	 * Return the current list of Waldos that have been retrieved
	 * 
	 * @return The current Waldos
	 */
	public List<Waldo> getWaldos() {
		// CPSC 210 Students: You will need to complete this method
		return waldos;
	}

	/**
	 * Retrieve messages available for the user from the Waldo web service
	 * 
	 * @return A list of messages
	 */
	public List<String> getMessages() {
		// CPSC 210 Students: You will need to complete this method
		// set up url
		StringBuilder urlBuilder = new StringBuilder(
				WALDO_WEB_SERVICE_URL);
		System.out.println("key is:" + key);
		urlBuilder.append("getmsgs/" + key + "/");
		
		InputStream in = null;
		try {
			// make query and run parser
			String s = makeJSONQuery(urlBuilder);
			List<String> output = parseMessages(s);
			System.out.println("parser ran!");
			return output;
		} catch (Exception e) {
			// Return an empty set  but let developer know
			e.printStackTrace();
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ioe) {
				throw new WaldoException(
						"getMessages: Unable to open or read return from http request.");
			}
		} // Something went wrong, return empty list
		return new ArrayList<String>();
	}

	public List<String> parseMessages(String input) {
	
		 List<String> messages = new ArrayList<String>();
		JSONObject obj;
		JSONArray ary;
		try {
		obj = (JSONObject) new JSONTokener(input).nextValue();	
		if (obj != null) {
		ary = (JSONArray) new JSONTokener(obj.getString("Messages")).nextValue();	
			if (ary != null) {
			for (int i = 0; i < ary.length(); i++) {
				
				//Retrieve strings and construct messages, add to list
				JSONObject messageobj = ary.getJSONObject(i);
				String waldoname = messageobj.getString("Name");
				String message = messageobj.getString("Message");
				String processedmsg = (message + " - From: " + waldoname);
				
				messages.add(processedmsg);
			}
			System.out.println("messages added!");
			
			}
			}
		} catch (JSONException e) {
				// Let the developer know but just return whatever is in stopsFound. Probably there was an
				// error in the JSON returned.
				e.printStackTrace();
			}
			return messages;
		}
	
	/**
	 * Execute a given query 
	 * 
	 * @param urlBuilder The query with everything but http:
	 * @return The JSON returned from the query 
	 */
	private String makeJSONQuery(StringBuilder urlBuilder) {
		try {
			URL url = new URL( urlBuilder.toString());
			HttpURLConnection client = (HttpURLConnection) url.openConnection();
			client.setRequestProperty("accept", "application/json");
			InputStream in = client.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String returnString = br.readLine();
			client.disconnect();
			return returnString;
		} catch (Exception e) {
			throw new WaldoException("Unable to make JSON query: " + urlBuilder.toString());
		}
	}
}

