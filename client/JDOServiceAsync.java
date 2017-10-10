package ubc.cs.cpsc310.rackbuddy.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface JDOServiceAsync {

	void addBikeRackData(BikeRackData data, AsyncCallback<Void> callback);

	void removeBikeRackData(BikeRackData data, AsyncCallback<Void> callback);

	void getAllData(AsyncCallback<List<BikeRackData>> callback);

	void updateBikeRackData(BikeRackData oldData, BikeRackData updatedData,
			AsyncCallback<Void> callback);

	void getBikeRackObject(BikeRackData data, AsyncCallback<BikeRackData> callback);

	void removeAll(AsyncCallback<Void> callback);

	void findDataById(Long id, AsyncCallback<BikeRackData> callback);

	void removeDataById(Long id, AsyncCallback<Void> callback);

	void updateDataById(BikeRackData data, AsyncCallback<Void> callback);

	void loadRacks(String url, String year, AsyncCallback<Void> callback);
	
	void addBikeRackData(ArrayList<BikeRackData> racks, AsyncCallback<Void> callback);

	void addNewFavRack(LoginInfo loginInfo, AsyncCallback<Void> callback);

	void removeFavRack(LoginInfo loginInfo, AsyncCallback<Void> callback);

	void getListofFaves(LoginInfo loginInfo,
			AsyncCallback<List<BikeRackData>> callback);
	
	void addComment(Comment comment, AsyncCallback<Void> callback);
	
	void removeCommentByID(Long id, AsyncCallback<Void> callback);

	void getRackComments(BikeRackData data, AsyncCallback<List<Comment>> callback);
	
	void findCommentByID(Long id, AsyncCallback<Comment> callback);

	void addRackComment(BikeRackData data, LoginInfo loginInfo,
			Comment newComment, AsyncCallback<Void> asyncCallback);
}
