package ubc.cs.cpsc310.rackbuddy.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface GeoParserServiceAsync {

	void getMarkerLocation(String address,
			AsyncCallback<MarkerLocation> callback);

}
