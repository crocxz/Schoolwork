package ca.ubc.cpsc210.waldo.map;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.api.IGeoPoint;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedIconOverlay.OnItemGestureListener;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.OverlayManager;
import org.osmdroid.views.overlay.PathOverlay;
import org.osmdroid.views.overlay.SimpleLocationOverlay;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import ca.ubc.cpsc210.waldo.R;
import ca.ubc.cpsc210.waldo.model.Bus;
import ca.ubc.cpsc210.waldo.model.BusRoute;
import ca.ubc.cpsc210.waldo.model.BusStop;
import ca.ubc.cpsc210.waldo.model.Trip;
import ca.ubc.cpsc210.waldo.model.Waldo;
import ca.ubc.cpsc210.waldo.translink.TranslinkService;
import ca.ubc.cpsc210.waldo.util.LatLon;
import ca.ubc.cpsc210.waldo.util.Segment;
import ca.ubc.cpsc210.waldo.waldowebservice.WaldoService;

/**
 * Fragment holding the map in the UI.
 * 
 * @author CPSC 210 Instructor
 */
public class MapDisplayFragment extends Fragment {

	/**
	 * Log tag for LogCat messages
	 */
	private final static String LOG_TAG = "MapDisplayFragment";

	/**
	 * Location of some points in lat/lon for testing and for centering the map
	 */
	private final static GeoPoint ICICS = new GeoPoint(49.261182, -123.2488201);
	private final static GeoPoint CENTERMAP = ICICS;

	/**
	 * Preference manager to access user preferences
	 */
	private SharedPreferences sharedPreferences;

	/**
	 * View that shows the map
	 */
	private MapView mapView;

	/**
	 * Map controller for zooming in/out, centering
	 */
	private MapController mapController;

	// **************** Overlay fields **********************

	/**
	 * Overlay for the device user's current location.
	 */
	private SimpleLocationOverlay userLocationOverlay;

	/**
	 * Overlay for bus stop to board at
	 */
	private ItemizedIconOverlay<OverlayItem> busStopToBoardOverlay;

	/**
	 * Overlay for bus stop to disembark
	 */
	private ItemizedIconOverlay<OverlayItem> busStopToDisembarkOverlay;

	/**
	 * Overlay for Waldo
	 */
	private ItemizedIconOverlay<OverlayItem> waldosOverlay;

	/**
	 * Overlay for displaying bus routes
	 */
	private List<PathOverlay> routeOverlays;

	/**
	 * Selected bus stop on map
	 */
	private OverlayItem selectedStopOnMap;

	/**
	 * Bus selected by user
	 */
	private OverlayItem selectedBus;

	// ******************* Application-specific *****************

	/**
	 * Wraps Translink web service
	 */
	private TranslinkService translinkService;

	/**
	 * Wraps Waldo web service
	 */
	private WaldoService waldoService;

	/**
	 * Waldo selected by user
	 */
	private Waldo selectedWaldo;

	/*
	 * The name the user goes by
	 */
	private String userName;

	// locationmanager and locationlistener fields
	private LocationManager locationManager;
	private LocationListener locationListener;
	

	
	private class LocationThingy implements LocationListener {
		
	 // calls updatelocation when location changes
		 @Override
	        public void onLocationChanged(Location location) {
			 updateLocation(location);
		 }

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
		
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
		
		}
		
	}

	// ***************** Android hooks *********************

	/**
	 * Help initialize the state of the fragment
	 */
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		setHasOptionsMenu(true);

		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		initializeWaldo();
		waldoService = new WaldoService();
		translinkService = new TranslinkService();
		routeOverlays = new ArrayList<PathOverlay>();
		
		//get locationmanager and locationlistener, register for updates
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
		System.out.println("Manager made");
		locationListener = new LocationThingy();
		System.out.println("Listener made");
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		System.out.println("updates asked for");
		
	}

	/**
	 * Initialize the Waldo web service
	 */
	private void initializeWaldo() {
		String s = null;
		new InitWaldo().execute(s);
	}

	/**
	 * Set up map view with overlays for buses, selected bus stop, bus route and
	 * current location.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		if (mapView == null) {
			mapView = new MapView(getActivity(), null);

			mapView.setTileSource(TileSourceFactory.MAPNIK);
			mapView.setClickable(true);
			mapView.setBuiltInZoomControls(true);

			mapController = mapView.getController();
			mapController.setZoom(mapView.getMaxZoomLevel() - 4);
			mapController.setCenter(CENTERMAP);

			userLocationOverlay = createLocationOverlay();
			busStopToBoardOverlay = createBusStopToBoardOverlay();
			busStopToDisembarkOverlay = createBusStopToDisembarkOverlay();
			waldosOverlay = createWaldosOverlay();

			// Order matters: overlays added later are displayed on top of
			// overlays added earlier.
			mapView.getOverlays().add(waldosOverlay);
			mapView.getOverlays().add(busStopToBoardOverlay);
			mapView.getOverlays().add(busStopToDisembarkOverlay);
			mapView.getOverlays().add(userLocationOverlay);
		}

		return mapView;
	}

	/**
	 * Helper to reset overlays
	 */
	private void resetOverlays() {
		OverlayManager om = mapView.getOverlayManager();
		om.clear();
		om.addAll(routeOverlays);
		om.add(busStopToBoardOverlay);
		om.add(busStopToDisembarkOverlay);
		om.add(userLocationOverlay);
		om.add(waldosOverlay);
	}

	/**
	 * Helper to clear overlays
	 */
	private void clearOverlays() {
		waldosOverlay.removeAllItems();
		clearAllOverlaysButWaldo();
		OverlayManager om = mapView.getOverlayManager();
		om.add(waldosOverlay);
	}

	/**
	 * Helper to clear overlays, but leave Waldo overlay untouched
	 */
	private void clearAllOverlaysButWaldo() {
		if (routeOverlays != null) {
			routeOverlays.clear();
			busStopToBoardOverlay.removeAllItems();
			busStopToDisembarkOverlay.removeAllItems();

			OverlayManager om = mapView.getOverlayManager();
			om.clear();
			om.addAll(routeOverlays);
			om.add(busStopToBoardOverlay);
			om.add(busStopToDisembarkOverlay);
			om.add(userLocationOverlay);
		}
	}

	/**
	 * When view is destroyed, remove map view from its parent so that it can be
	 * added again when view is re-created.
	 */
	@Override
	public void onDestroyView() {
		((ViewGroup) mapView.getParent()).removeView(mapView);
		super.onDestroyView();
	}

	/**
	 * Shut down the various services
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	/**
	 * Update the overlay with user's current location. Request location
	 * updates.
	 */
	@Override
	public void onResume() {

		// CPSC 210 students, you'll need to handle parts of location updates
		// here...
		// register for updates on resume
		
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
		
		super.onResume();
	}

	/**
	 * Cancel location updates.
	 */
	@Override
	public void onPause() {
		// CPSC 210 students, you'll need to do some work with location updates
		// here...
		// deregister from social update
		locationManager.removeUpdates(locationListener);
		super.onPause();
	}

	/**
	 * Update the marker for the user's location and repaint.
	 */
	public void updateLocation(Location location) {
		// CPSC 210 Students: Implement this method. mapView.invalidate is
		// needed to redraw
		// the map and should come at the end of the method.
		
		//get lat and long values and create geopoint then set to overlay
		
		int lat = (int) (location.getLatitude() * 1E6);
	     int lng = (int) (location.getLongitude() * 1E6);
	     System.out.println("Location is: " + Integer.toString(lat) + ", " + Integer.toString(lng));
	     GeoPoint point = new GeoPoint(lat, lng);
		userLocationOverlay.setLocation(point); 
		
		mapView.invalidate();
	}

	/**
	 * Save map's zoom level and centre.
	 */
	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (mapView != null) {
			outState.putInt("zoomLevel", mapView.getZoomLevel());
			IGeoPoint cntr = mapView.getMapCenter();
			outState.putInt("latE6", cntr.getLatitudeE6());
			outState.putInt("lonE6", cntr.getLongitudeE6());
		}
	}

	/**
	 * Retrieve Waldos from the Waldo web service
	 */
	public void findWaldos() {
		clearOverlays();
		// Find out from the settings how many waldos to retrieve, default is 1
		String numberOfWaldosAsString = sharedPreferences.getString(
				"numberOfWaldos", "5");
		int numberOfWaldos = Integer.valueOf(numberOfWaldosAsString);
		new GetWaldoLocations().execute(numberOfWaldos);
		mapView.invalidate();
	}

	/**
	 * Clear waldos from view
	 */
	public void clearWaldos() {
		clearOverlays();
		mapView.invalidate();

	}

	// ******************** Overlay Creation ********************

	/**
	 * Create the overlay for bus stop to board at marker.
	 */
	private ItemizedIconOverlay<OverlayItem> createBusStopToBoardOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

		OnItemGestureListener<OverlayItem> gestureListener = new OnItemGestureListener<OverlayItem>() {

			/**
			 * Display bus stop description in dialog box when user taps stop.
			 * 
			 * @param index
			 *            index of item tapped
			 * @param oi
			 *            the OverlayItem that was tapped
			 * @return true to indicate that tap event has been handled
			 */
			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem oi) {

				new AlertDialog.Builder(getActivity())
						.setPositiveButton(R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (selectedStopOnMap != null) {
									selectedStopOnMap.setMarker(getResources()
											.getDrawable(R.drawable.pin_blue));

									mapView.invalidate();
								}
							}
						}).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
						.show();

				oi.setMarker(getResources().getDrawable(R.drawable.pin_blue));
				selectedStopOnMap = oi;
				mapView.invalidate();
				return true;
			}

			@Override
			public boolean onItemLongPress(int index, OverlayItem oi) {
				// do nothing
				return false;
			}
		};

		return new ItemizedIconOverlay<OverlayItem>(
				new ArrayList<OverlayItem>(), getResources().getDrawable(
						R.drawable.pin_blue), gestureListener, rp);
	}

	/**
	 * Create the overlay for bus stop to disembark at marker.
	 */
	private ItemizedIconOverlay<OverlayItem> createBusStopToDisembarkOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

		OnItemGestureListener<OverlayItem> gestureListener = new OnItemGestureListener<OverlayItem>() {

			/**
			 * Display bus stop description in dialog box when user taps stop.
			 * 
			 * @param index
			 *            index of item tapped
			 * @param oi
			 *            the OverlayItem that was tapped
			 * @return true to indicate that tap event has been handled
			 */
			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem oi) {

				new AlertDialog.Builder(getActivity())
						.setPositiveButton(R.string.ok, new OnClickListener() {
							@Override
							public void onClick(DialogInterface arg0, int arg1) {
								if (selectedStopOnMap != null) {
									selectedStopOnMap.setMarker(getResources()
											.getDrawable(R.drawable.pin_blue));

									mapView.invalidate();
								}
							}
						}).setTitle(oi.getTitle()).setMessage(oi.getSnippet())
						.show();

				oi.setMarker(getResources().getDrawable(R.drawable.pin_blue));
				selectedStopOnMap = oi;
				mapView.invalidate();
				return true;
			}

			@Override
			public boolean onItemLongPress(int index, OverlayItem oi) {
				// do nothing
				return false;
			}
		};

		return new ItemizedIconOverlay<OverlayItem>(
				new ArrayList<OverlayItem>(), getResources().getDrawable(
						R.drawable.pin_blue), gestureListener, rp);
	}

	/**
	 * Create the overlay for Waldo markers.
	 */
	private ItemizedIconOverlay<OverlayItem> createWaldosOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());
		OnItemGestureListener<OverlayItem> gestureListener = new OnItemGestureListener<OverlayItem>() {

			/**
			 * Display Waldo point description in dialog box when user taps
			 * icon.
			 * 
			 * @param index
			 *            index of item tapped
			 * @param oi
			 *            the OverlayItem that was tapped
			 * @return true to indicate that tap event has been handled
			 */
			@Override
			public boolean onItemSingleTapUp(int index, OverlayItem oi) {
				
			selectedWaldo = waldoService.getWaldos().get(index);
				Date lastSeen = selectedWaldo.getLastUpdated();
				SimpleDateFormat dateTimeFormat = new SimpleDateFormat(
						"MMM dd, hh:mmaa", Locale.CANADA);

				new AlertDialog.Builder(getActivity())
						.setPositiveButton(R.string.get_route,
								new OnClickListener() {
									@Override
									public void onClick(DialogInterface arg0,
											int arg1) {

										// CPSC 210 STUDENTS. You must set
										// currCoord to
										// the user's current location.
									
										Location locationl = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
										
										 LatLon currCoord = new LatLon(locationl.getLatitude(), locationl.getLongitude());

										// CPSC 210 Students: Set currCoord...

										LatLon destCoord = selectedWaldo
												.getLastLocation();

										new GetRouteTask().execute(currCoord,
												destCoord);

									}
								})
						.setNegativeButton(R.string.ok, null)
						.setTitle(selectedWaldo.getName())
						.setMessage(
								"Last seen  " + dateTimeFormat.format(lastSeen))
						.show();

				mapView.invalidate();
				return true;
			}

			@Override
			public boolean onItemLongPress(int index, OverlayItem oi) {
				// do nothing
				return false;
			}
		};

		return new ItemizedIconOverlay<OverlayItem>(
				new ArrayList<OverlayItem>(), getResources().getDrawable(
						R.drawable.map_pin_thumb_blue), gestureListener, rp);
	}

	/**
	 * Create overlay for a bus route.
	 */
	private PathOverlay createPathOverlay() {
		PathOverlay po = new PathOverlay(Color.parseColor("#cf0c7f"),
				getActivity());
		Paint pathPaint = new Paint();
		pathPaint.setColor(Color.parseColor("#cf0c7f"));
		pathPaint.setStrokeWidth(4.0f);
		pathPaint.setStyle(Style.STROKE);
		po.setPaint(pathPaint);
		return po;
	}

	/**
	 * Create the overlay for the user's current location.
	 */
	private SimpleLocationOverlay createLocationOverlay() {
		ResourceProxy rp = new DefaultResourceProxyImpl(getActivity());

		return new SimpleLocationOverlay(getActivity(), rp) {
			@Override
			public boolean onLongPress(MotionEvent e, MapView mapView) {
				new GetMessagesFromWaldo().execute();
				return true;
			}

		};
	}

	/**
	 * Plot endpoints
	 */
	private void plotEndPoints(Trip trip) {
		GeoPoint pointStart = new GeoPoint(trip.getStart().getLatLon()
				.getLatitude(), trip.getStart().getLatLon().getLongitude());

		OverlayItem overlayItemStart = new OverlayItem(Integer.valueOf(
				trip.getStart().getNumber()).toString(), trip.getStart()
				.getDescriptionToDisplay(), pointStart);
		GeoPoint pointEnd = new GeoPoint(trip.getEnd().getLatLon()
				.getLatitude(), trip.getEnd().getLatLon().getLongitude());
		OverlayItem overlayItemEnd = new OverlayItem(Integer.valueOf(
				trip.getEnd().getNumber()).toString(), trip.getEnd()
				.getDescriptionToDisplay(), pointEnd);
		busStopToBoardOverlay.removeAllItems();
		busStopToDisembarkOverlay.removeAllItems();

		busStopToBoardOverlay.addItem(overlayItemStart);
		busStopToDisembarkOverlay.addItem(overlayItemEnd);
	}

	/**
	 * Plot bus route onto route overlays
	 * 
	 * @param rte
	 *            : the bus route
	 * @param start
	 *            : location where the trip starts
	 * @param end
	 *            : location where the trip ends
	 */
	private void plotRoute(Trip trip) {

		// Put up the end points
		
	//	routeOverlays = new ArrayList<PathOverlay>();
		LatLon start = trip.getStart().getLatLon();
		LatLon end = trip.getEnd().getLatLon();
		plotEndPoints(trip);
		
		// get segments from route
        BusRoute rte = trip.getRoute();
        List<Segment> segments =  rte.getSegments();
        //go through segments, create base overlay
        for (int i = 0; i < segments.size(); i ++) {
        	Iterator<LatLon> is = segments.get(i).iterator();
        	PathOverlay po = createPathOverlay();
        	// iterate through segment and add point to overlay if inbetween startand end points
        	while (is.hasNext()) {
        		LatLon nextpoint = is.next();
        		if (LatLon.inbetween(nextpoint, start, end)) {
        			
        		po.addPoint(new GeoPoint(nextpoint.getLatitude(), nextpoint.getLongitude()));
        		
        		}
        	}
        	// add finished overlay to routeoverlays
        	if (po.getNumberOfPoints() != 0) {
        	routeOverlays.add(po);
        	}
        	System.out.println("overlay added for segment with this amount of points:" + po.getNumberOfPoints());
        //	po.clearPath();
        	
        }
		

		// This should be the last method call in this method to redraw the map
        resetOverlays();
		mapView.invalidate();
	}

	/**
	 * Plot a Waldo point on the specified overlay.
	 */
	private void plotWaldos(List<Waldo> waldos) {

		// CPSC 210 STUDENTS: Complete the implementation of this method
		//set variables 
		OverlayItem waldoToAdd = null;
		
		GeoPoint waldoLocation = null;
		LatLon waldoLatLon;
		String waldoName = null;
		String waldoDescription = "A WALDO";
		
		// iterate through list of waldos, making an overlay for each and adding it to the waldo overlays
		for(Waldo w: waldos){
			
			waldoLatLon = w.getLastLocation();
			waldoName = w.getName();
			
			waldoLocation = new GeoPoint(waldoLatLon.getLatitude(), waldoLatLon.getLongitude());
			
			waldoToAdd = new OverlayItem(waldoName, waldoDescription, waldoLocation);
			
			waldosOverlay.addItem(waldoToAdd);
			
		}

		// This should be the last method call in this method to redraw the map
		System.out.println("all waldos added to overlay!");
		mapView.invalidate();
	}

	/**
	 * Helper to create simple alert dialog to display message
	 * 
	 * @param msg
	 *            message to display in alert dialog
	 * @return the alert dialog
	 */
	private AlertDialog createSimpleDialog(String msg) {
		AlertDialog.Builder dialogBldr = new AlertDialog.Builder(getActivity());
		dialogBldr.setMessage(msg);
		dialogBldr.setNeutralButton(R.string.ok, null);
		return dialogBldr.create();
	}

	/**
	 * Asynchronous task to get a route between two endpoints. Displays progress
	 * dialog while running in background.
	 */
	private class GetRouteTask extends AsyncTask<LatLon, Void, Trip> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());
		private LatLon startPoint;
		private LatLon endPoint;

		@Override
		protected void onPreExecute() {
			translinkService.clearModel();
			dialog.setMessage("Retrieving route...");
			dialog.show();
		}

		protected Trip doInBackground(LatLon... routeEndPoints) {

			// THe start and end point for the route
			startPoint = routeEndPoints[0];
			endPoint = routeEndPoints[1];
			
			// set up variables
			Trip userTrip = null;
			String directionNS;
			String directionEW;
			
			Set<BusStop> stopsNearStart = new HashSet<BusStop>();
			Set<BusStop> stopsNearEnd = new HashSet<BusStop>();
			
			Set<BusRoute> routesBetweenStops = new HashSet<BusRoute>();
			
			
			String distance = sharedPreferences.getString("stopDistance", "500");
			int distanceI = Integer.parseInt(distance);
		// find stops near start and end points
			stopsNearStart = translinkService.getBusStopsAround(startPoint, distanceI);
			stopsNearEnd = translinkService.getBusStopsAround(endPoint, distanceI);
			
			
			//find out if the stops are in walking distance by comparing stops near endpoints
			for(BusStop aStop: stopsNearStart){
				if (stopsNearEnd.contains(aStop)) {
					userTrip = new Trip(null, null, null, null, true);
							return userTrip;
					}
				}
			// return null if either start or end has no bus stops nearby
						if (stopsNearStart.isEmpty() || stopsNearEnd.isEmpty()) {
							return null;
						}
			// set directions
			if(startPoint.getLatitude() > endPoint.getLatitude()) {
				directionNS = "SOUTH";
			} else if(startPoint.getLatitude() == endPoint.getLatitude()) {
				directionNS = null;
			} else {
				directionNS = "NORTH";
			}
			
			if(startPoint.getLongitude() > endPoint.getLongitude())  {
				directionEW = "EAST";
			} else if(startPoint.getLongitude() == endPoint.getLongitude()) {
				directionEW = null;
			} else {
				directionEW = "WEST";
			}
			System.out.println("Direction:" + directionNS + "-" + directionEW);
			// walking distance case
			for(BusStop aStop: stopsNearStart){
				if (stopsNearEnd.contains(aStop)) {
					userTrip = new Trip(null, null, null, null, true);
							return userTrip;
					}
				}
			// set up routes at start and end, find common routes
			Set<BusRoute> routesNearStart = new HashSet<BusRoute>();
			Set<BusRoute> routesNearEnd = new HashSet<BusRoute>();
			
			for(BusStop aStop: stopsNearStart){
				for(BusRoute aRoute: aStop.getRoutes()){
					routesNearStart.add(aRoute);
					}}
			for(BusStop aStop: stopsNearEnd){
				for(BusRoute aRoute: aStop.getRoutes()){
					routesNearEnd.add(aRoute);
					}}
			for(BusRoute r: routesNearStart) {
				if (routesNearEnd.contains(r)) {
					routesBetweenStops.add(r);
				}}
			// if no route between two points
			if (routesBetweenStops.isEmpty()) {
				return null;
			}
			String routestring = "";
			for (BusRoute r: routesBetweenStops) {
				routestring = routestring + r.toString() + ", ";
			}
			System.out.println("Routes set:" + routestring);
			
		//remove routes with wrong directions
			for(BusRoute aRoute: routesBetweenStops){
				for(Bus aBus: aRoute.getBuses()){
					if(!aBus.getDirection().equals(directionEW) || !aBus.getDirection().equals(directionNS)) {
						routesBetweenStops.remove(aRoute);
					    break;
					}
					}
			}
			routestring = "";
			for (BusRoute r: routesBetweenStops) {
				routestring = routestring + r.toString() + ", ";
			}
			System.out.println("Routes filtered:" + routestring);
			
			
			// Select appropriate route based on settings
			String routingType = sharedPreferences.getString("routingOptions", "closest_stop_me");
			List<BusStop> Stoplist = new ArrayList<BusStop>();
			if (routingType.equals("closest_stop_me")) {
				//Find list of nearby stops that contain a valid route
				for (BusRoute aRoute: routesBetweenStops) {
					for (BusStop s: stopsNearStart) {
						if (s.getRoutes().contains(aRoute)) {	
							Stoplist.add(s);
						}
					}
				} System.out.println("stoplist made!");
				//find the closest stop of the list above
				BusStop testStop = Stoplist.get(0);
				for (int i = 1; i < Stoplist.size(); i++) {
					if (LatLon.distanceBetweenTwoLatLon(startPoint, Stoplist.get(i).getLatLon()) < LatLon.distanceBetweenTwoLatLon(startPoint, testStop.getLatLon()) ) {
						testStop = Stoplist.get(i);
					}
				}System.out.println("closest stop found!");
				//Get any route from the set of valid routes that is associated with the closest stop 
				translinkService.getBusEstimatesForStop(testStop);
				System.out.println("getting estimates for stop");
				BusRoute chosenroute = null;
				for (BusRoute aRoute: routesBetweenStops) {
					if (testStop.getRoutes().contains(aRoute)) {
						chosenroute = aRoute;
						break;
					}}
				System.out.println("got the route we will use");
					//find end stop
					BusStop endStop = null;
					for (BusStop s: stopsNearEnd) {
						if (s.getRoutes().contains(chosenroute)) {
							endStop = s;
						}
					}
					System.out.println("endstop found");
				// parse it, then make new trip
				String routenumber = chosenroute.getRouteNumber();
				chosenroute = translinkService.lookupRoute(routenumber);
				translinkService.parseKMZ(chosenroute);
				System.out.println("segments added");
				
				//set descriptions
				Set<Bus> thebuses = chosenroute.getBuses();
				String description = routenumber + ": " + "Minutes left til a bus arrives: ";
				for (Bus b: thebuses) {
					if (b.getStop() == testStop) {
						description = description + Integer.toString(b.getMinutesToDeparture()) + ", ";
					}
				}
				testStop.setDescriptionToDisplay(description);
				endStop.setDescriptionToDisplay(description);
				userTrip = new Trip(testStop, endStop, directionEW, chosenroute, false);
			} else if (routingType.equals("closest_stop_dest")) {
				//Find list of nearby stops that contain a valid route
				for (BusRoute aRoute: routesBetweenStops) {
					for (BusStop s: stopsNearEnd) {
						if (s.getRoutes().contains(aRoute)) {
							Stoplist.add(s);
						}
					}
				} //find the closest stop of the list above
				BusStop testStop = Stoplist.get(0);
				for (int i = 1; i < Stoplist.size(); i++) {
					if (LatLon.distanceBetweenTwoLatLon(startPoint, Stoplist.get(i).getLatLon()) < LatLon.distanceBetweenTwoLatLon(startPoint, testStop.getLatLon()) ) {
						testStop = Stoplist.get(i);
					}
				} //Get any route from the set of valid routes that is associated with the closest stop 
				translinkService.getBusEstimatesForStop(testStop);
				BusRoute chosenroute = null;
				for (BusRoute aRoute: routesBetweenStops) {
					if (testStop.getRoutes().contains(aRoute)) {
						chosenroute = aRoute;
						break;
					}}
					//find start stop
					BusStop startStop = null;
					for (BusStop s: stopsNearStart) {
						if (s.getRoutes().contains(chosenroute)) {
						 startStop = s;
						}
					}
					//parse route and create trip object
					String routenumber = chosenroute.getRouteNumber();
					chosenroute = translinkService.lookupRoute(routenumber);
					translinkService.parseKMZ(chosenroute);
					System.out.println("segments added");
					
					//set descriptions
					Set<Bus> thebuses = chosenroute.getBuses();
					String description = routenumber + ": " + "Minutes left til a bus arrives: ";
					for (Bus b: thebuses) {
						if (b.getStop() == startStop) {
							description = description + Integer.toString(b.getMinutesToDeparture()) + ", ";
						}
					}
					testStop.setDescriptionToDisplay(description);
					startStop.setDescriptionToDisplay(description);
					
					userTrip = new Trip(startStop, testStop, directionEW, chosenroute, false);
					//
			
			}
			return userTrip;
		}

		@Override
		protected void onPostExecute(Trip trip) {
			dialog.dismiss();

			if (trip != null && !trip.inWalkingDistance()) {
				// Remove previous start/end stops
				busStopToBoardOverlay.removeAllItems();
				busStopToDisembarkOverlay.removeAllItems();

				// Removes all but the selected Waldo
				waldosOverlay.removeAllItems();
				List<Waldo> waldos = new ArrayList<Waldo>();
				waldos.add(selectedWaldo);
				plotWaldos(waldos);

				// Plot the route
				plotRoute(trip);

				// Move map to the starting location
				LatLon startPointLatLon = trip.getStart().getLatLon();
				mapController.setCenter(new GeoPoint(startPointLatLon
						.getLatitude(), startPointLatLon.getLongitude()));
				mapView.invalidate();
			} else if (trip != null && trip.inWalkingDistance()) {
				AlertDialog dialog = createSimpleDialog("You are in walking distance!");
				dialog.show();
			} else {
				AlertDialog dialog = createSimpleDialog("Unable to retrieve bus location info, no trip possible");
				dialog.show();
			}
		}
	}

	/**
	 * Asynchronous task to initialize or re-initialize access to the Waldo web
	 * service.
	 */
	private class InitWaldo extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... arg0) {

			// Initialize the service passing the name of the Waldo to use. If
			// you have
			// passed an argument to this task, then it will be used as the
			// name, otherwise
			// nameToUse will be null
			String nameToUse = arg0[0];
			userName = waldoService.initSession(nameToUse);

			return null;
		}

	}

	/**
	 * Asynchronous task to get Waldo points from Waldo web service. Displays
	 * progress dialog while running in background.
	 */
	private class GetWaldoLocations extends
			AsyncTask<Integer, Void, List<Waldo>> {
		private ProgressDialog dialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Retrieving locations of waldos...");
			dialog.show();
		}

		@Override
		protected List<Waldo> doInBackground(Integer... i) {
			Integer numberOfWaldos = i[0];
			return waldoService.getRandomWaldos(numberOfWaldos);
		}

		@Override
		protected void onPostExecute(List<Waldo> waldos) {
			dialog.dismiss();
			if (waldos != null) {
				plotWaldos(waldos);
			}
		}
	}

	/**
	 * Asynchronous task to get messages from Waldo web service. Displays
	 * progress dialog while running in background.
	 */
	private class GetMessagesFromWaldo extends
			AsyncTask<Void, Void, List<String>> {

		private ProgressDialog dialog = new ProgressDialog(getActivity());

		@Override
		protected void onPreExecute() {
			dialog.setMessage("Retrieving messages...");
			dialog.show();
		}

		@Override
		protected List<String> doInBackground(Void... params) {
			return waldoService.getMessages();
		}

		@Override
		protected void onPostExecute(List<String> messages) {
			// CPSC 210 Students: Complete this method
			
			// show it
			dialog.dismiss();
			if (messages.isEmpty()) {
				AlertDialog Adialog = createSimpleDialog("No messages");
				Adialog.show();
		} else {
			for (String s : messages) {
				AlertDialog Adialog = createSimpleDialog(s);
				Adialog.show();
				}
			
		}
			
		}	
		
	}

}
