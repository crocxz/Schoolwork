package ubc.cs.cpsc310.rackbuddy.client;

import java.io.Serializable;
import javax.jdo.annotations.Extension;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)

public class LoginInfo implements Serializable {
	
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    @Extension(vendorName="datanucleus", key="gae.encoded-pk", value="true")
    private String encodedKey;

    @NotPersistent	
    private boolean admin = false;
    
    @NotPersistent
	private boolean loggedIn = false;
    
    @NotPersistent
	private String loginUrl;
    
    @NotPersistent
	private String logoutUrl;
    
    @Persistent
	private String emailAddress;
    
    @NotPersistent
	private String nickname;
	
	@Persistent
	private Long bikeRackID;

	public Long getBikeRackID() {
		return bikeRackID;
	}

	public void setBikeRackID(Long bikeRackIDs) {
		this.bikeRackID = bikeRackIDs;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public void setLoggedIn(boolean loggedIn) {
		this.loggedIn = loggedIn;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getLogoutUrl() {
		return logoutUrl;
	}

	public void setLogoutUrl(String logoutUrl) {
		this.logoutUrl = logoutUrl;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public boolean getAdmin() {
		if (this.emailAddress
				.matches("timyzfeng@gmail.com|xwei7778@gmail.com|williamwlxu@gmail.com|sanghyeokpark7@gmail.com")) {
			this.admin = true;
		}
		else {this.admin = false;
		}
		return this.admin;
	}

}