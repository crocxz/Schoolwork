package ubc.cs.cpsc310.rackbuddy.server;

import java.util.ArrayList;
import java.util.List;

import ubc.cs.cpsc310.rackbuddy.client.BikeRackData;
import ubc.cs.cpsc310.rackbuddy.client.Comment;
import ubc.cs.cpsc310.rackbuddy.client.JDOService;
import ubc.cs.cpsc310.rackbuddy.client.LoginInfo;
import ubc.cs.cpsc310.rackbuddy.shared.AlreadyFavoritedException;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.opencsv.CSVReader;

import java.util.logging.Logger;

import javax.jdo.JDOFatalInternalException;
import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.jdo.annotations.Transactional;

import org.datanucleus.exceptions.NucleusObjectNotFoundException;

import java.io.*;
import java.net.*;

public class JDOServiceImpl extends RemoteServiceServlet implements JDOService{
		
	private static final Logger LOG = Logger.getLogger(JDOServiceImpl.class.getName());
	private static final PersistenceManagerFactory PMF = JDOHelper.getPersistenceManagerFactory("transactions-optional");
	
	private GeoParser myGeoParser;
	
	public JDOServiceImpl() {
		this.myGeoParser = new GeoParser("");
	}
	
	@Override
	/**
	 * Adds the BikeRackData objects into the datastore
	 */
	public void addBikeRackData(ArrayList<BikeRackData> racks) {
		for (BikeRackData r : racks) {
			addBikeRackData(r);
		}
	}

	
	@Override
	/**
	 * downloads rack data from server and parses into BikeRackData objects, then stores in database
	 */
	public void loadRacks(String url, String year) {
		
		
		InputStream input;
		ArrayList<BikeRackData> racks = new ArrayList<BikeRackData>();
		try {
			//open inputstream from url
			input = new URL(url).openStream();
				try {
					// feed inputstream to reader
					Reader reader = new InputStreamReader(input, "UTF-8");
					//initiate csv reader, skip one line
					CSVReader csvReader = new CSVReader(reader, ',', '"', 2);
					String[] row = null;
					//parse csv into objects row by row
					while((row = csvReader.readNext()) != null) {
						
						if (row[6].equals(year)) {
					   BikeRackData rack = new BikeRackData();
					   rack.setStreetNumber(row[0]);
					   rack.setStreetName(row[1]);
					   rack.setStreetSide(row[2]);
					   rack.setSkytrainStation(row[3]);
					   rack.setBia(row[4]);
					  // 
					   rack.setNumRacks( Integer.parseInt(row[5]));
					   rack.setYearInstalled(row[6]);
					   
						String address = rack.getStreetNumber() + " " + rack.getStreetName() + ", Vancouver, BC";
						double lat = myGeoParser.getLatitude(address);
						double lng = myGeoParser.getLongitude(address);
						
						rack.setLat(lat);
						rack.setLng(lng);
						rack.setFave(false);
					   racks.add(rack);
					}
					}
					addBikeRackData(racks);
					//fin
					csvReader.close();
					
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	/**
	 * Adds the BikeRackData object into the datastore
	 */
	public void addBikeRackData(BikeRackData data) {
		PersistenceManager pm = getPersistenceManager();
		try{
			pm.makePersistent(data);
		}finally{
			pm.close();
		}
	}
	
	
	/**
	 * Can't remove null data
	 * http://stackoverflow.com/questions/8868449/how-to-check-the-text-is-null-or-not-using-jdo
	 * 
	 * Removes the specified BikeRackData object from the datastore
	 */
	@Override
	public void removeBikeRackData(BikeRackData data) {

		PersistenceManager pm = getPersistenceManager();

		try {

			BikeRackData result = getBikeRackObject(data);

			List<BikeRackData> datas = getAllData();

			for (BikeRackData brd : datas) {
				if (brd.equals(result)) {
					pm.deletePersistent(result);
				}
			}

		} finally {
			pm.close();
		}
	}
	
	/**
	 * Retreives all the BikeRackData objects currently in the datasore.
	 */
	@SuppressWarnings("unchecked")
	@Transactional
	@Override
	public List<BikeRackData> getAllData() {
		// http://stackoverflow.com/questions/3242217/how-do-you-make-query-results-available-after-closing-the-persistence-manager
		PersistenceManager pm = getPersistenceManager();
		List<BikeRackData> results = null;
		List<BikeRackData> detachedList = null;
		try {
			Query query = pm.newQuery(BikeRackData.class);

			results = (List<BikeRackData>) query.execute();

			detachedList = new ArrayList<BikeRackData>();

			for (BikeRackData data : results) {
				detachedList.add(pm.detachCopy(data));
			}

		}catch(NucleusObjectNotFoundException e){
			detachedList = new ArrayList<BikeRackData>();
			
		}
		finally {
			pm.close();
		}

		return detachedList;
	}
	
	/**
	 * Removes all the BikeRackData objects currently in the datastore.
	 */
	@Override
	public void removeAll() {
		PersistenceManager pm = getPersistenceManager();
		try{
			List<BikeRackData> removedDatas = getAllData();
			
			if(!removedDatas.isEmpty()){
		
				pm.deletePersistentAll(removedDatas);
			}
			
			
		}finally{
			pm.close();
		}
	}
	
	/**
	 * Updates the oldData with the updatedData
	 * 
	 * oldData: the original BikeRackData object
	 * updatedData: the updated BikeRackData object
	 */
	@Override
	public void updateBikeRackData(BikeRackData oldData, BikeRackData updatedData) {
		
		PersistenceManager pm = getPersistenceManager();
		
		String streetNumber = updatedData.getStreetNumber();	
		String streetName = updatedData.getStreetName();
		String streetSide = updatedData.getStreetSide();
		String skytrainStation = updatedData.getSkytrainStation();
		String bia = updatedData.getBia();
		int numRacks = updatedData.getNumRacks();
		String yearInstalled = updatedData.getYearInstalled();
		
		try{
			
			BikeRackData result = getBikeRackObject(oldData);
			
			List<BikeRackData> datas = getAllData();
			
			for(BikeRackData brd : datas){
				if(brd.equals(result)){
					oldData = pm.getObjectById(BikeRackData.class, brd.getId());
					oldData.setStreetName(streetName);
					oldData.setBia(bia);
					oldData.setStreetNumber(streetNumber);
					oldData.setStreetSide(streetSide);
					oldData.setSkytrainStation(skytrainStation);
					oldData.setNumRacks(numRacks);
					oldData.setYearInstalled(yearInstalled);
					
					pm.makePersistent(oldData);
				}
			}
		}finally{
			pm.close();
		}
		
	}
	
	public PersistenceManager getPersistenceManager(){
		return PMF.getPersistenceManager();
	}
	
	/**
	 * Retrieves the specified BikeRackData object from the datastore.
	 * 
	 * Returns null if unable to find it.
	 */
	@Override
	public BikeRackData getBikeRackObject(BikeRackData data) {

		PersistenceManager pm= getPersistenceManager();
		List<BikeRackData> results = null;

		List<BikeRackData> detachedList = null;
	    try{
	    	Query q = pm.newQuery(BikeRackData.class,
	    			"streetNumber == '"+data.getStreetNumber()+"'"+
	    			" && streetName == '"+data.getStreetName()+"'"+
	    			" && streetSide == '"+data.getStreetSide()+"'"+	
	    			 " && skytrainStation == '"+data.getSkytrainStation()+"'"+
                    " && numRacks == "+ data.getNumRacks() +
                    " && yearInstalled == '"+ data.getYearInstalled() + "'" 
                    		 );
	    	
	    	results = (List<BikeRackData>) q.execute();
	    	
	    	detachedList = new ArrayList<BikeRackData>();

			for (BikeRackData brd : results) {
				detachedList.add(pm.detachCopy(brd));
			}
	       
	    }
	    finally {
	        pm.close(); 
	    }
	    
	    if(!detachedList.isEmpty() && detachedList !=null){
	    	return detachedList.get(0);	
	    }
	    
	    return null;
	}
	
	/**
	 * Returns BikeRackData object from datastore with the associated id
	 */
	public BikeRackData findDataById(Long id) {
		BikeRackData detachedCopy=null, object=null;
		PersistenceManager pm= getPersistenceManager();
	    try{
	        object = pm.getObjectById(BikeRackData.class,id);
	        detachedCopy = pm.detachCopy(object);
	    }catch (JDOObjectNotFoundException e) {
	        return null; 
	    }
	    catch(JDOFatalInternalException e){
	    	return null;
	    }
	    finally {
	        pm.close(); 
	    }
	    return detachedCopy;
	}

	/**
	 * Removes BikeRackData object from datastore with the associated id
	 */
	@Override
	public void removeDataById(Long id) {
		
		BikeRackData result = findDataById(id);
		
		if(result != null){
			PersistenceManager pm= getPersistenceManager();
			try{
				pm.deletePersistent(result);
			}finally{
				pm.close();
			}
		}
		
	}

	/**
	 * Updates the BikeRackData object from the datastore with the associated id
	 */
	@Override
	public void updateDataById(BikeRackData data) {
		
		String streetNumber = data.getStreetNumber();	
		String streetName = data.getStreetName();
		String streetSide = data.getStreetSide();
		String skytrainStation = data.getSkytrainStation();
		String bia = data.getBia();
		int numRacks = data.getNumRacks();
		String yearInstalled = data.getYearInstalled();
		
		BikeRackData result = findDataById(data.getId());
		
		if(result != null){
			PersistenceManager pm= getPersistenceManager();
			
			try{
				
				result.setStreetNumber(streetNumber);
				result.setStreetName(streetName);
				result.setStreetSide(streetSide);
				result.setSkytrainStation(skytrainStation);
				result.setBia(bia);
				result.setNumRacks(numRacks);
				result.setYearInstalled(yearInstalled);
				
				pm.makePersistent(result);
			}finally{
				pm.close();
			}
		}
		
	}

	public void addNewFavRack(LoginInfo loginInfo) throws AlreadyFavoritedException{
		PersistenceManager pm = getPersistenceManager();
		try{
			
			List<BikeRackData> data = getListofFaves(loginInfo);
			
			for(BikeRackData brd : data){
				if(loginInfo.getBikeRackID().equals(brd.getId())){
					throw new AlreadyFavoritedException();
				}
			}
			
			pm.makePersistent(loginInfo);
		}finally{
			pm.close();
		}
	}

	@Override
	public void removeFavRack(LoginInfo loginInfo) {
		PersistenceManager pm = getPersistenceManager();
		try{
			Query q = pm.newQuery(LoginInfo.class,
                    "emailAddress == '"+loginInfo.getEmailAddress()+"'" +" && bikeRackID == " + loginInfo.getBikeRackID());
			
			List<LoginInfo> results = (List<LoginInfo>) q.execute();
			
			if(!results.isEmpty()){
				for(LoginInfo temp : results){
					pm.deletePersistent(temp);
				}
			}
			
		}finally{
			pm.close();
		}
	}

	@Override
	public List<BikeRackData> getListofFaves(LoginInfo loginInfo) {
		PersistenceManager pm = getPersistenceManager();
	    List<BikeRackData> data = new ArrayList<BikeRackData>();
	    try {
	    	
	    		 Query q = pm.newQuery(LoginInfo.class);
	 	        q.setFilter("emailAddress == emailAddressParam");
	 	        q.declareParameters("String emailAddressParam");
	 	        List<LoginInfo> ids = (List<LoginInfo>) q.execute(loginInfo.getEmailAddress());
	 	        for (LoginInfo login : ids) {
	 	          BikeRackData temp = this.findDataById(login.getBikeRackID());
	 	          if(temp!=null){
	 	        	 temp.setFave(true);
		 	          data.add(temp);
	 	          }
	 	         
	 	        }
	    	
	       
	      } finally {
	        pm.close();
	      }
		return data;
	}

	

	@Override
	public List<Comment> getRackComments(BikeRackData data) {
		PersistenceManager pm = getPersistenceManager();
	    List<Comment> rackComments = new ArrayList<Comment>();
	    try{
	    	Query q = pm.newQuery(Comment.class);
	    	q.setFilter("bikeRackId == bikeRackIdParam");
	    	q.declareParameters("Long bikeRackIdParam");
	    	 List<Comment> comments = (List<Comment>) q.execute(data.getId());
	    	 for (Comment c : comments) {
	    		 rackComments.add(c);
		        }
	    }
	    finally{
	    	pm.close();
	    }
		return rackComments;
	}

	@Override
	public void addComment(Comment comment) {
		PersistenceManager pm = getPersistenceManager();
		try{
			pm.makePersistent(comment);
		}finally{
			pm.close();
		}
	}
	
	@Override
	public void addRackComment(BikeRackData data, LoginInfo loginInfo, Comment newComment){
		PersistenceManager pm = getPersistenceManager();
		try{
			pm.makePersistent(newComment);
		System.out.println("sucessfully added");
		}finally{
			pm.close();
		}
	}
	
	@Override
	public void removeCommentByID(Long id) {
		
		Comment result = findCommentByID(id);
		
		if(result != null){
			PersistenceManager pm= getPersistenceManager();
			try{
				pm.deletePersistent(result);
			}finally{
				pm.close();
			}
		}
	}
	
	@Override 
	public Comment findCommentByID(Long id) {
		Comment detachedCopy=null;
		Comment object=null;
		PersistenceManager pm= getPersistenceManager();
	    try{
	        object = pm.getObjectById(Comment.class,id);
	        detachedCopy = pm.detachCopy(object);
	    }catch (JDOObjectNotFoundException e) {
	        return null; 
	    }
	    catch(JDOFatalInternalException e){
	    	return null;
	    }
	    finally {
	        pm.close(); 
	    }
	    return detachedCopy;
	}
}
