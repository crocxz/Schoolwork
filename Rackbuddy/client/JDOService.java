package ubc.cs.cpsc310.rackbuddy.client;

import java.util.ArrayList;
import java.util.List;

import ubc.cs.cpsc310.rackbuddy.shared.AlreadyFavoritedException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("jdoService")
public interface JDOService extends RemoteService {

	public void addBikeRackData(BikeRackData data);
	
	public void removeBikeRackData(BikeRackData data);
	
	public List<BikeRackData> getAllData();
	
	public void updateBikeRackData(BikeRackData oldData, BikeRackData updatedData);
	
	public  BikeRackData getBikeRackObject(BikeRackData data);
	
	public void removeAll();
	
	public BikeRackData findDataById(Long id);
	
	public void removeDataById(Long id);
	
	public void updateDataById(BikeRackData data);

	public void addBikeRackData(ArrayList<BikeRackData> racks);

	public void loadRacks(String url, String year);

	public void addNewFavRack(LoginInfo loginInfo) throws AlreadyFavoritedException;
	
	public void removeFavRack(LoginInfo loginInfo);
	
	public List<BikeRackData> getListofFaves(LoginInfo loginInfo);

	public void addComment(Comment comment);

	public void removeCommentByID(Long id);
	
	public List<Comment> getRackComments(BikeRackData data);

	public Comment findCommentByID(Long id);
	
	public void addRackComment(BikeRackData data, LoginInfo loginInfo, Comment newComment);

}
