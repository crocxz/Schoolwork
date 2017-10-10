package ubc.cs.cpsc310.rackbuddy.client;

import java.io.Serializable;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;

import com.google.gwt.view.client.ProvidesKey;


@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class BikeRackData implements Serializable {
	
    /**
     * The key provider that provides the unique ID of a contact.
     */
    public static final ProvidesKey<BikeRackData> KEY_PROVIDER = new ProvidesKey<BikeRackData>() {

		@Override
		public Long getKey(BikeRackData item) {
			
			return item.getId() == null? -1 : item.getId();
		}
    };
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent
	private boolean isFave;
	
	@Persistent
	protected String streetNumber; 
	
	@Persistent
	protected String bia;
	
	@Persistent
	protected String streetName;

	@Persistent
	protected String streetSide;

	@Persistent
	protected String skytrainStation;
	
	@Persistent
	protected int numRacks; 

	@Persistent
	protected String yearInstalled;
	
	@Persistent
	protected double lat;
	
	@Persistent
	protected double lng;
	
	public BikeRackData() {
		this.streetNumber = "";
		this.bia = "";
		this.streetName = "";
		this.streetSide = "";
		this.skytrainStation = "";
		this.numRacks = 0;
		this.yearInstalled = "";
		this.lat = 0;
		this.lng = 0;
		this.isFave = false;
	}
	/**
	 * This constructor is only for the JUNIT tests
	 * since it is only testing parsing of the data,
	 * we don't need to worry about lat lng.
	 * So just set lat lng to default value
	 */
	public BikeRackData(String streetNumber, String bia, String streetName,
			String streetSide, String skytrainStation, int numRacks,
			String yearInstalled) {

		this.streetNumber = streetNumber;
		this.bia = bia;
		this.streetName = streetName;
		this.streetSide = streetSide;
		this.skytrainStation = skytrainStation;
		this.numRacks = numRacks;
		this.yearInstalled = yearInstalled;
		this.lat = 0;
		this.lng = 0;
		this.isFave = false;

	}

	public BikeRackData(String streetNumber,
						String bia,
						String streetName,
						String streetSide,
						String skytrainStation,
						int numRacks,
						String yearInstalled,
						double lat,
						double lng,
						boolean isFave
						){
		
		this.streetNumber = streetNumber;
		this.bia = bia;
		this.streetName = streetName;
		this.streetSide = streetSide;
		this.skytrainStation = skytrainStation;
		this.numRacks = numRacks;
		this.yearInstalled = yearInstalled;
		this.lat = lat;
		this.lng = lng;
		this.isFave = isFave;
	}
	
	/**
	 * Copy constructor for cloning
	 * so that the object can be updated
	 * @param other
	 */
	public BikeRackData(BikeRackData other){
		this.streetNumber = other.streetNumber;
		this.bia = other.bia;
		this.streetName = other.streetName;
		this.streetSide = other.streetSide;
		this.skytrainStation = other.skytrainStation;
		this.numRacks = other.numRacks;
		this.yearInstalled = other.yearInstalled;
		this.lat = other.lat;
		this.lng = other.lng;
		this.isFave = other.isFave;
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

	public String getBia() {
		return bia;
	}
	public void setBia(String bia) {
		this.bia = bia;
	}
	public Long getId() {
		return id;
	}

	public String getStreetNumber() {
		return streetNumber;
	}

	public void setStreetNumber(String streetNumber) {
		this.streetNumber = streetNumber;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public String getStreetSide() {
		return streetSide;
	}

	public void setStreetSide(String streetSide) {
		this.streetSide = streetSide;
	}

	public String getSkytrainStation() {
		return skytrainStation;
	}

	public void setSkytrainStation(String skytrainStation) {
		this.skytrainStation = skytrainStation;
	}

	public int getNumRacks() {
		return numRacks;
	}

	public void setNumRacks(int numRacks) {
		this.numRacks = numRacks;
	}

	public String getYearInstalled() {
		return yearInstalled;
	}

	public void setYearInstalled(String yearInstalled) {
		this.yearInstalled = yearInstalled;
	}

	
	
	public boolean isFave() {
		return isFave;
	}
	public void setFave(boolean isFave) {
		this.isFave = isFave;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bia == null) ? 0 : bia.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + (isFave ? 1231 : 1237);
		long temp;
		temp = Double.doubleToLongBits(lat);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(lng);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + numRacks;
		result = prime * result
				+ ((skytrainStation == null) ? 0 : skytrainStation.hashCode());
		result = prime * result
				+ ((streetName == null) ? 0 : streetName.hashCode());
		result = prime * result
				+ ((streetNumber == null) ? 0 : streetNumber.hashCode());
		result = prime * result
				+ ((streetSide == null) ? 0 : streetSide.hashCode());
		result = prime * result
				+ ((yearInstalled == null) ? 0 : yearInstalled.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BikeRackData other = (BikeRackData) obj;
		if (bia == null) {
			if (other.bia != null)
				return false;
		} else if (!bia.equals(other.bia))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isFave != other.isFave)
			return false;
		if (Double.doubleToLongBits(lat) != Double.doubleToLongBits(other.lat))
			return false;
		if (Double.doubleToLongBits(lng) != Double.doubleToLongBits(other.lng))
			return false;
		if (numRacks != other.numRacks)
			return false;
		if (skytrainStation == null) {
			if (other.skytrainStation != null)
				return false;
		} else if (!skytrainStation.equals(other.skytrainStation))
			return false;
		if (streetName == null) {
			if (other.streetName != null)
				return false;
		} else if (!streetName.equals(other.streetName))
			return false;
		if (streetNumber == null) {
			if (other.streetNumber != null)
				return false;
		} else if (!streetNumber.equals(other.streetNumber))
			return false;
		if (streetSide == null) {
			if (other.streetSide != null)
				return false;
		} else if (!streetSide.equals(other.streetSide))
			return false;
		if (yearInstalled == null) {
			if (other.yearInstalled != null)
				return false;
		} else if (!yearInstalled.equals(other.yearInstalled))
			return false;
		return true;
	}
	
}
