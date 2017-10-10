package ubc.cs.cpsc310.rackbuddy.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;





import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import ubc.cs.cpsc310.rackbuddy.shared.AlreadyFavoritedException;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ListDataProvider;

public class BikeRackTable extends Table implements IsWidget {

	private Button filterButton;
	private Button searchButton;
	private Button removeFilter;
	private Button quitButton;
	private PopupPanel popUp;
	private TextBox textbox;
	private ListBox filterby;
//	private List<BikeRackData> savedList = new ArrayList<BikeRackData>();
	private List<BikeRackData> stNameList = new ArrayList<BikeRackData>();
	private List<BikeRackData> stNumList = new ArrayList<BikeRackData>();
	private VerticalPanel vp = new VerticalPanel();

	public BikeRackTable(LoginInfo loginInfo) {
		super(loginInfo);
	}

	@Override
	public Widget asWidget() {
		stNum.setSortable(true);

		stName.setSortable(true);

		Column<BikeRackData, String> addFave = new Column<BikeRackData, String>(
				new ButtonCell()) {

			@Override
			public String getValue(final BikeRackData object) {
				return YES;
			}
		};

		addFave.setFieldUpdater(new FieldUpdater<BikeRackData, String>() {

			@Override
			public void update(int index, BikeRackData object, String value) {
				object.setFave(true);
				loginInfo.setBikeRackID(object.getId());
				addNewFavBikeRack(loginInfo);
			}

		});
		
		table.addColumn(addFave, MARK_AS_FAVORITE);

		Column<BikeRackData, String> commentCol = new Column<BikeRackData, String>(
				new ButtonCell()) {

			@Override
			public String getValue(final BikeRackData object) {
				return VIEW_COMMENTS;
			}
		};

		commentCol.setFieldUpdater(new FieldUpdater<BikeRackData, String>() {

			@Override
			public void update(int index, BikeRackData object, String value) {
				CommentDialogBox box = new CommentDialogBox(object, loginInfo);
				box.center();
				box.show();
			}
		});

		table.addColumn(commentCol, COMMENTS);

		vp.add(new Label(BIKE_RACK_LOCATIONS_IN_THE_CITY_OF_VANCOUVER));
		initFilter();
		vp.add(table);
		vp.add(pager);

		return vp;
	}

	private void addNewFavBikeRack(final LoginInfo loginInfo) {
		if (jdoService == null) {
			jdoService = GWT.create(JDOService.class);
		}

		jdoService.addNewFavRack(loginInfo, new AsyncCallback<Void>() {

			@Override
			public void onFailure(Throwable caught) {

				if (caught instanceof AlreadyFavoritedException) {
					Window.alert(BIKE_RACK_NOT_MARKED_AS_FAVORITE_THIS_BIKE_RACK_HAS_ALREADY_BEEN_MARKED_AS_FAVORITE);
				} else {
					Window.alert("Error has occured: " + caught.getMessage());
				}
			}

			@Override
			public void onSuccess(Void result) {
				AppUtils.EVENT_BUS.fireEvent(new AddFaveEvent(loginInfo));
			}

		});

	}

	// updates table according to the markers on the map
	@Override
	public void updateTable(final List<BikeRackData> list) {

		AsyncDataProvider<BikeRackData> provider = new AsyncDataProvider<BikeRackData>() {

			@Override
			protected void onRangeChanged(HasData<BikeRackData> display) {

				int start = display.getVisibleRange().getStart();
				int end = start + display.getVisibleRange().getLength();
				end = end >= list.size() ? list.size() : end;
				List<BikeRackData> sub = list.subList(start, end);
				updateRowData(start, sub);
				updateRowCount(list.size(),true);
			}
		};
				
		provider.addDataDisplay(table);
		AsyncHandler columnSortHandler = new AsyncHandler(table);
		table.addColumnSortHandler(columnSortHandler);
		
		
		
	}

	// saves list made from displayAllMarkers
	@Override
	public void saveList(List<BikeRackData> list) {
		savedList = list;
		
	}

	// sortTable function
	@Override
	public void sortTable(final List<BikeRackData> list) {

		ListHandler<BikeRackData> sortHandler = new ListHandler<BikeRackData>(
				list);

		sortHandler.setComparator(stNum, new Comparator<BikeRackData>() {

			@Override
			public int compare(BikeRackData o1, BikeRackData o2) {
			
					return Integer.valueOf(o1.getStreetNumber()).compareTo(
							Integer.valueOf(o2.getStreetNumber()));
		      

			}
		});

		sortHandler.setComparator(stName, new Comparator<BikeRackData>() {

			@Override
			public int compare(BikeRackData o1, BikeRackData o2) {

				return o1.getStreetName().compareTo(o2.getStreetName());
			}

		});
		
		table.addColumnSortHandler(sortHandler);
	
		
	}

	// initializes filter function.
	public void initFilter() {

		VerticalPanel PopVp = new VerticalPanel();
		PopVp.setSize("40em", "10em");

		// add Pop up panel
		popUp = new PopupPanel();
		popUp.add(PopVp);

		filterby = new ListBox();
		filterby.setVisibleItemCount(1);
		filterby.addItem(ST_NAME);
		filterby.addItem(ST_NUMBER);

		textbox = new TextBox();
		textbox.addStyleName("paddedLeft");
		textbox.setWidth("40em");
		textbox.setHeight("10px");

		HorizontalPanel h1 = new HorizontalPanel();
		h1.setStyleName("marginTop");
		HorizontalPanel h2 = new HorizontalPanel();
		h2.setStyleName("marginTop");
		HorizontalPanel h3 = new HorizontalPanel();
		h3.setStyleName("marginTop");
		HorizontalPanel h4 = new HorizontalPanel();
		h4.setStyleName("marginTop");

		h1.add(new Label(FILTER_BY));
		h1.add(filterby);
		h2.add(textbox);

		PopVp.add(h1);
		PopVp.add(h2);
		// Quit button within the PopupPanel
		quitButton = new Button("Quit");
		quitButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				popUp.hide();

			}

		});
		// Search button within the PopupPanel
		searchButton = new Button("Filter..");
		searchButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if (MapDisplay.isTextValid(textbox.getValue()) == true) {

					if (filterby.getValue(filterby.getSelectedIndex()) == ST_NAME) {
						matchStName(savedList, textbox.getValue());
						sortTable(stNameList);
						updateTable(stNameList);
						popUp.hide();

					}
					
					if (filterby.getValue(filterby.getSelectedIndex()) == ST_NUMBER) {
						int inputint = 0;
						try {
							inputint = Integer.parseInt(textbox.getValue());
							matchStNum(savedList,
									Integer.valueOf(textbox.getValue()));
							sortTable(stNumList);
							updateTable(stNumList);
							popUp.hide();
						} catch (Exception e) {
							Window.alert(INVAILD_ST_NUM);
						}
					}

				} else {
					Window.alert(INVAILD_INPUT);
				}

			}

		});
		h3.add(searchButton);
		h3.add(quitButton);
		PopVp.add(h3);
		// filter button on the main page
		removeFilter = new Button(REMOVE_FILTER);
		removeFilter.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				updateTable(savedList);
				sortTable(savedList);
			}
			
		});
		filterButton = new Button(FILTER_BY);
		filterButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				popUp.center();
			}

		});
		HorizontalPanel h5 = new HorizontalPanel();
		h5.setStyleName("marginTop");
		h5.add(filterButton);
		h5.add(removeFilter);
		
		vp.add(h5);

	}

	public void matchStName(List<BikeRackData> list, String stName) {
			stNameList.clear();
			for (BikeRackData brd : list) {
				if (brd.getStreetName().toLowerCase().contains(stName.toLowerCase())) {
					stNameList.add(brd);
				}
			}

	}

	public void matchStNum(List<BikeRackData> list, int stNum) {
		stNumList.clear();

		for (BikeRackData brd : list) {
			if (Integer.valueOf(brd.getStreetNumber()) == stNum) {
				stNumList.add(brd);
			}
		}

	}

	
	
}
