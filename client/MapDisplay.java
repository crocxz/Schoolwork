package ubc.cs.cpsc310.rackbuddy.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Icon;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.maps.client.overlay.MarkerOptions;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;


public class MapDisplay   {

	private static final String NO_FAVORITE_BIKE_RACK_MARKERS_TO_DISPLAY = "No favorite bike rack markers to display...";
	private static final String TOGGLE_TO_DISPLAY_FAVORITE_MARKERS_ON_MAP = "Toggle to display favorite markers on map: ";
	private MapWidget map;
	private static final int ZOOM_LEVEL = 12;
	private static final String UNABLE_TO_DISPLAY_BIKE_RACK_LOCATION_ON_MAP = "Unable to display bike rack location on map...";
	private static final String MARKERS_ARE_ADDED = "Markers were added to the map...";
	private static final String SEARCH_RADIUS = "Search radius: ";
	private static final String POINT_OF_INTEREST = "Point of interest:";
	private static final String _1_KM = "1 km";
	private static final String _500_M = "500 m";
	private static final String _100_M = "100 m";
	private static final String SEARCH = "Search...";
	public static final String INVALID_ADDRESS = "Please input a valid address.";
	protected static final String UNABLE_TO_DISPLAY_POI_ON_MAP = "Unable to display POI on map...";
	protected static final String POI_ICON = "http://maps.google.com/mapfiles/arrow.png";
	private static final String FAVE_MARKER = "http://www.google.com/mapfiles/markerF.png";

	private VerticalPanel searchPanel;

	private Button searchButton;
	private TextBox address;
	private ListBox searchRadius;
	private GeoParserServiceAsync geoParserService = GWT.create(GeoParserService.class);
	private JDOServiceAsync jdoService = GWT.create(JDOService.class);
	private LayoutPanel rackMapPanel = new LayoutPanel();

	private  List<BikeRackData> tempList;	
	private VerticalPanel bigTable = new VerticalPanel();
	
	private LoginInfo loginInfo;
	private Table brt;
	private Table favRackTable;
	private CheckBox showFaves;

	
	public MapDisplay(LoginInfo loginInfo) {
		
		this.loginInfo = loginInfo;
		brt = new BikeRackTable(loginInfo);		
		favRackTable = new FavRackTable(loginInfo);
		tempList = new ArrayList<BikeRackData>();
		Maps.loadMapsApi("", "2", false, new Runnable() {
			public void run() {
				buildUi();		
			
			}
		});
		
	}
	

	
	
	private void buildUi() {
		// Open a map centered on Vancouver, BC, Canada
		LatLng vancouver = LatLng.newInstance(49.261226, -123.1139268);
		map = new MapWidget(vancouver, 2);
		map.setSize("60%", "100%");
		map.setZoomLevel(ZOOM_LEVEL);
		
		displayTable();

		// Add some controls for the zoom level
		map.addControl(new LargeMapControl());

		rackMapPanel.setHeight("40em");
		rackMapPanel.setWidth("100em");
		rackMapPanel.addStyleName("rackMap");
		rackMapPanel.add(map);

		initSearchPanel();
		
		
	}
	
	private void displayTable() {	
		bigTable.clear();
		final TabPanel p = new TabPanel();
		
	    p.add(brt, BikeRackTable.BIKE_RACK_LOCATIONS_IN_THE_CITY_OF_VANCOUVER, false);
	    p.add(favRackTable, Table.USER_S_FAVORITE_BIKE_RACK_LOCATION, false);

	    p.selectTab(0);

	    bigTable.add(p);		

		RootPanel.get("bigTable").add(bigTable);
	}
	
	
	// displaying all bike rack data from the data store as markers on the map.
	public void displayAllMarkers() {
		
		if (jdoService == null) {
			jdoService = GWT.create(JDOService.class);
		}

		jdoService.getAllData(new AsyncCallback<List<BikeRackData>>() {
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(UNABLE_TO_DISPLAY_BIKE_RACK_LOCATION_ON_MAP);
			}

			@Override
			public void onSuccess(List<BikeRackData> result) {
				map.clearOverlays();			
				 tempList.clear();
				for (BikeRackData brd : result) {
					LatLng latlng = LatLng.newInstance(brd.getLat(),
							brd.getLng());
					
					map.addOverlay(new Marker(latlng));				
					if (map.getBounds().containsLatLng(latlng)) {									
						tempList.add(brd);		
						
					}
				}			
				
				brt.updateTable(tempList);
				brt.sortTable(tempList);	
				brt.saveList(tempList);						
				
				
			}
			
		});
	
	
	}

	/**
	 * Creates widgets to allow searching of bike racks from specified POI
	 */
	private void initSearchPanel() {
		searchPanel = new VerticalPanel();

		searchButton = new Button(SEARCH);
		searchButton.addStyleName("marginTop");

		searchButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				handleSearchButtonEvent();

			}
		});
		
		searchButton.addKeyPressHandler(new KeyPressHandler(){

			@Override
			public void onKeyPress(KeyPressEvent event) {
				if(event.getCharCode() == KeyCodes.KEY_ENTER){
					handleSearchButtonEvent();
				}
				
			}
			
		});
		
		address = new TextBox();
		address.addStyleName("paddedLeft");
		address.setWidth("40em");
		address.setHeight("10px");
		
		address.addKeyPressHandler(new KeyPressHandler(){
			 
             @Override
             public void onKeyPress(KeyPressEvent event) {
                     if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                     searchButton.click();
                   }
             }
     });

		searchRadius = new ListBox();
		searchRadius.setVisibleItemCount(1);
		searchRadius.addItem(_100_M);
		searchRadius.addItem(_500_M);
		searchRadius.addItem(_1_KM);
		
		showFaves = new CheckBox();
		showFaves.setValue(false);
		
		showFaves.addValueChangeHandler(new ValueChangeHandler<Boolean>(){

			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				if(event.getValue() == true){
					displayFaveMarkers();
				}else{
					displayCurrentList();
				}
				
			}

		});
		HorizontalPanel h1 = new HorizontalPanel();
		h1.setStyleName("marginTop");

		HorizontalPanel h2 = new HorizontalPanel();
		h2.setStyleName("marginTop");
		
		HorizontalPanel h3 = new HorizontalPanel();
		h3.setStyleName("marginTop");

		h1.add(new Label(POINT_OF_INTEREST));
		h1.add(address);
		
		h2.add(new Label(SEARCH_RADIUS));
		h2.add(searchRadius);
		
		h3.add(new Label(TOGGLE_TO_DISPLAY_FAVORITE_MARKERS_ON_MAP));
		h3.add(showFaves);

		searchPanel.add(h1);
		searchPanel.add(h2);
		searchPanel.add(searchButton);
		searchPanel.add(h3);
	
		
		RootPanel.get("rackMap").add(searchPanel);
	}
	
	private void displayFaveMarkers() {
		jdoService.getListofFaves(loginInfo, new AsyncCallback<List<BikeRackData>>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(List<BikeRackData> result) {

				Icon icon = Icon.newInstance(FAVE_MARKER);
				MarkerOptions ops = MarkerOptions.newInstance(icon);
				
				if(!result.isEmpty()){
					map.clearOverlays();
					for (BikeRackData brd : result) {
						LatLng poi = LatLng.newInstance(brd.getLat(),
								brd.getLng());

						Marker marker = new Marker(poi, ops);
						map.addOverlay(marker);
					}
					
				}else{
					Window.alert(NO_FAVORITE_BIKE_RACK_MARKERS_TO_DISPLAY);
				}
				
			}
			
		});
	}

	
	
	public VerticalPanel getSearchPanel() {
		return searchPanel;
	}

	private void displayBikeRacks(final String seachRadius) {
		
		if (jdoService == null) {
			jdoService = GWT.create(JDOService.class);
		}

		jdoService.getAllData(new AsyncCallback<List<BikeRackData>>() {

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(UNABLE_TO_DISPLAY_BIKE_RACK_LOCATION_ON_MAP);
			}

			@Override
			public void onSuccess(List<BikeRackData> result) {
				double radius = getSearchRadius(seachRadius);
					
				LatLng center = map.getCenter();
				 tempList.clear();
				
				for (BikeRackData brd : result) {
					LatLng latlng = LatLng.newInstance(brd.getLat(),
							brd.getLng());

					if (center.distanceFrom(latlng) <= radius) {
						
						Marker marker = new Marker(latlng);
						
						map.addOverlay(marker);
						if (map.getBounds().containsLatLng(latlng)) {
							
							tempList.add(brd);		
						}		
						
					}
				}			
				brt.sortTable(tempList);
				brt.updateTable(tempList);		
				brt.saveList(tempList);
			}

		

		});
		
	}
	
	/**
	 * Displays all bike rack from a specified POI address with a given radius
	 */
	public void displayRacksFromPOI(String poiAddress, final String searchRadius) {
		map.clearOverlays();

		if (geoParserService == null) {
			geoParserService = GWT.create(GeoParserService.class);
		}
	
		geoParserService.getMarkerLocation(poiAddress,
				new AsyncCallback<MarkerLocation>() {

					@Override
					public void onFailure(Throwable caught) {
						Window.alert(UNABLE_TO_DISPLAY_POI_ON_MAP);
					}

					@Override
					public void onSuccess(MarkerLocation result) {
						map.clearOverlays();

						LatLng poi = LatLng.newInstance(result.getLat(),
								result.getLng());

						Icon icon = Icon.newInstance(POI_ICON);
						MarkerOptions ops = MarkerOptions.newInstance(icon);
						Marker marker = new Marker(poi, ops);
						map.addOverlay(marker);
						
						map.setCenter(poi);

						map.setZoomLevel(ZOOM_LEVEL);

						displayBikeRacks(searchRadius);
					}

				});

	}

	public static double getSearchRadius(String searchRadius) {
		double meter = 0.0;

		if (searchRadius.equals(_100_M)) {
			meter = 100.0;
			return meter;
		}

		if (searchRadius.equals(_500_M)) {
			meter = 500.0;
			return meter;
		}

		if (searchRadius.equals(_1_KM)) {
			meter = 1000.0;
			return meter;
		}

		return meter;
	}

	/**
	 * Checks if specified text is valid
	 */
	public static boolean isTextValid(String text) {

		if (text == null) {
			return false;
		}

		if (text.isEmpty()) {
			return false;
		}

		if (text.matches("\\s+")) {
			return false;
		}

		return true;
	}

	public MapWidget getMapWidget() {
		return this.map;
	}

	public LayoutPanel getMapPanel() {
		return this.rackMapPanel;
	}




	private void handleSearchButtonEvent() {
		if (isTextValid(address.getValue()) == true) {

			map.clearOverlays();

			displayRacksFromPOI(address.getValue().trim(), searchRadius
					.getValue(searchRadius.getSelectedIndex()));
		} else {
			Window.alert(INVALID_ADDRESS);
		}
	}




	private void displayCurrentList() {
		List<BikeRackData> data = brt.getSavedList();
		map.clearOverlays();
		
		for(BikeRackData brd : data){
			LatLng latlng = LatLng.newInstance(brd.getLat(),
					brd.getLng());
			
			map.addOverlay(new Marker(latlng));
		}
	}
	


}
