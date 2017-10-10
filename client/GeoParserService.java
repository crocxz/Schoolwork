package ubc.cs.cpsc310.rackbuddy.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("GeoParserService")
public interface GeoParserService extends RemoteService {
	
	public MarkerLocation getMarkerLocation(String address);

}
