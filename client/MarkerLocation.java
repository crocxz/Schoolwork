package ubc.cs.cpsc310.rackbuddy.client;

import java.io.Serializable;

public class MarkerLocation implements Serializable {
	
	private double lat;
	private double lng;

	public MarkerLocation() {
	lat = 0;
	lng = 0;
	}
	
	public MarkerLocation(double lat, double lng) {
	this.lat = lat;
	this.lng = lng;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}
	
	
}
