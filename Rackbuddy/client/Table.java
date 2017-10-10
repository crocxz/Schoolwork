package ubc.cs.cpsc310.rackbuddy.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.view.client.ListDataProvider;

public abstract class Table implements IsWidget {

	public static final String USER_S_FAVORITE_BIKE_RACK_LOCATION = "Favorite Locations";
	public static final String BIKE_RACK_NOT_MARKED_AS_FAVORITE_THIS_BIKE_RACK_HAS_ALREADY_BEEN_MARKED_AS_FAVORITE = "Bike rack not marked as favorite: This bike rack has already been marked as favorite.";
	public static final String MARK_AS_FAVORITE = "Mark as favorite?";
	public static final String UNMARK_AS_FAVORITE = "Unmark as favorite?";
	public static final String YES = "Yes";

	public static final int NUM_DATA_PER_PAGE = 10;

	public static final String YEARS_INSTALLED = "Years Installed";

	public static final String NUM_RACKS = "# of Racks";

	public static final String BIA2 = "BIA";

	public static final String SKYTRAIN_STATION_NAME = "Skytrain Station Name";

	public static final String ST_SIDE = "St. Side";

	public static final String ST_NAME = "St. Name";

	public static final String ST_NUMBER = "St. Number";

	public static final String BIKE_RACK_LOCATIONS_IN_THE_CITY_OF_VANCOUVER = "Bike Rack Locations";

	public static final String FILTER_BY = "Filter by : ";

	public static final String INVAILD_INPUT = "Invalid input";

	public static final String INVAILD_ST_NAME = "Invalid St. Name";

	public static final String INVAILD_ST_NUM = "Invalid St. Number";
	
	public static final String REMOVE_FILTER = "Clear filter";
	public static final String COMMENTS = "Comments";
	public static final String VIEW_COMMENTS = "View Comments";
	
	protected LoginInfo loginInfo;
	protected JDOServiceAsync jdoService = GWT.create(JDOService.class);
	protected final CellTable<BikeRackData> table = new CellTable<BikeRackData>();
	protected SimplePager pager;
	protected ListDataProvider<BikeRackData> dataProvider;
	
	protected TextColumn<BikeRackData> stNum;
	protected TextColumn<BikeRackData> stName;
	
	protected List<BikeRackData> savedList = new ArrayList<BikeRackData>();

	public Table(LoginInfo loginInfo) {
		this.loginInfo = loginInfo;
		initTable();
        
	}

	protected void initTable() {
		
		dataProvider = new ListDataProvider<BikeRackData>();
		
		table.setPageSize(NUM_DATA_PER_PAGE);

		 stNum = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return object.getStreetNumber();
			}
		};
		table.addColumn(stNum, ST_NUMBER);
		
		stName = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return object.getStreetName();
			}
		};
		table.addColumn(stName, ST_NAME);
		
		TextColumn<BikeRackData> stSide = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return object.getStreetSide();
			}
		};
		table.addColumn(stSide, ST_SIDE);
		
		TextColumn<BikeRackData> skytrainName = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return object.getSkytrainStation();
			}
		};
		table.addColumn(skytrainName, SKYTRAIN_STATION_NAME);
		
		TextColumn<BikeRackData> bia = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return object.getBia();
			}
		};
		table.addColumn(bia, BIA2);
		
		TextColumn<BikeRackData> numRacks = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return String.valueOf(object.getNumRacks());
			}
		};
		table.addColumn(numRacks, NUM_RACKS);
		
		TextColumn<BikeRackData> yearsInstalled = new TextColumn<BikeRackData>() {
			@Override
			public String getValue(BikeRackData object) {
				return object.getYearInstalled();
			}
		};
		table.addColumn(yearsInstalled, YEARS_INSTALLED);
		
		pager = new SimplePager(TextLocation.CENTER, true, true);
		pager.setDisplay(table);
		
		dataProvider.addDataDisplay(table);
		
	}

	public void updateTable(List<BikeRackData> tempList) {
		//Do nothing. 
	}

	public void saveList(List<BikeRackData> tempList) {
		//Do nothing
	}

	public void sortTable(List<BikeRackData> tempList) {
		//Do nothing
	}
	

	public List<BikeRackData> getSavedList() {
		return savedList;
	}

}