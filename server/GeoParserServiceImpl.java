package ubc.cs.cpsc310.rackbuddy.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import ubc.cs.cpsc310.rackbuddy.client.GeoParserService;
import ubc.cs.cpsc310.rackbuddy.client.MarkerLocation;

public class GeoParserServiceImpl extends RemoteServiceServlet implements GeoParserService {
	
	private GeoParser myGeoParser;
	private static final String MY_API_KEY = "";
	
	public GeoParserServiceImpl() {
		this.myGeoParser = new GeoParser(MY_API_KEY);
	}

	@Override
	public MarkerLocation getMarkerLocation(String address) {
		double lat = myGeoParser.getLatitude(address);
		double lng = myGeoParser.getLongitude(address);
		MarkerLocation result = new MarkerLocation(lat,lng);
		return result;
	}

}
