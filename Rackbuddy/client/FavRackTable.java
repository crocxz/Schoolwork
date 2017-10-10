package ubc.cs.cpsc310.rackbuddy.client;

import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class FavRackTable extends Table {

	public FavRackTable(LoginInfo loginInfo) {
		super(loginInfo);
	}

	@Override
	public Widget asWidget() {
				
				Column<BikeRackData, String> removeFave = new Column<BikeRackData, String>(new ButtonCell()) {

		            @Override
		            public String getValue(final BikeRackData object) {
		                return BikeRackTable.YES;
		            }
		        };
		        
		        removeFave.setFieldUpdater(new FieldUpdater<BikeRackData,String>(){

					@Override
					public void update(int index, BikeRackData object, String value) {
						object.setFave(false);
						loginInfo.setBikeRackID(object.getId());
						deleteFavBikeRack(loginInfo);
						
					}
		        	
		        });
		        
		        table.addColumn(removeFave, BikeRackTable.UNMARK_AS_FAVORITE);

				displayListOfFavorites();
				
				AppUtils.EVENT_BUS.addHandler(AddFaveEvent.TYPE, new AddFaveEventHandler(){

					@Override
					public void onFaveAdded(AddFaveEvent event) {
						handleAddEvent(event);
						
					}
					
				});
				
				AppUtils.EVENT_BUS.addHandler(RemoveFaveEvent.TYPE, new RemoveFaveEventHandler(){

					@Override
					public void onFaveRemoved(RemoveFaveEvent event) {
						handleRemoveEvent(event);
						
					}
					
				});

				VerticalPanel vp = new VerticalPanel();
				vp.add(table);
				vp.add(pager);
				return vp;
	}

	private void displayListOfFavorites() {
		jdoService.getListofFaves(loginInfo, new AsyncCallback<List<BikeRackData>>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
			}

			@Override
			public void onSuccess(List<BikeRackData> result) {
				dataProvider.getList().clear();
				dataProvider.getList().addAll(result);
			    dataProvider.flush();
			    dataProvider.refresh();
			    table.redraw();	
			}
			
		});
	}

	private void deleteFavBikeRack(final LoginInfo loginInfo) {
		if(jdoService == null){
			jdoService = GWT.create(JDOService.class);
		}
		
		jdoService.removeFavRack(loginInfo, new AsyncCallback<Void>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Error has occured: " +caught.getMessage());
			}

			@Override
			public void onSuccess(Void result) {
				
				AppUtils.EVENT_BUS.fireEvent(new RemoveFaveEvent(loginInfo));
			}
			
		});
		
	}

	private void handleAddEvent(AddFaveEvent event) {
		jdoService.findDataById(event.getLoginInfo().getBikeRackID(), new AsyncCallback<BikeRackData>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				
			}

			@Override
			public void onSuccess(BikeRackData result) {
				dataProvider.getList().add(result);
				dataProvider.flush();
				dataProvider.refresh();
				table.redraw();
				
			}
			
		});
	}

	private void handleRemoveEvent(RemoveFaveEvent event) {
		jdoService.findDataById(event.getLoginInfo().getBikeRackID(), new AsyncCallback<BikeRackData>(){

			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.getMessage());
				
			}

			@Override
			public void onSuccess(BikeRackData result) {
				if(dataProvider.getList().contains(result)){
					dataProvider.getList().remove(result);
					dataProvider.flush();
					dataProvider.refresh();
					table.redraw();
				}
				
			}
			
		});
	}
}