package ubc.cs.cpsc310.rackbuddy.client;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
public class Comment implements Serializable{
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long commentID;
	
	@Persistent
	protected String message;
	
	@Persistent
	protected Long bikeRackId;
	
	@Persistent
	protected String email;
	
	public Long getCommentID(){
		return commentID;
	}
	
	public Long getBikeRackId() {
		return bikeRackId;
	}

	public void setBikeRackID(Long bikeRackIds) {
		this.bikeRackId = bikeRackIds;
	}

	public void setMessage(String message){
		this.message = message;
	}
	public String getMessage() {
		return message;
	}

	public void setEmail(String email){
		this.email = email;
	}
	public String getEmail() {
		return email;
	}
}
