package ubc.cs.cpsc310.rackbuddy.client;

import com.google.gwt.event.shared.GwtEvent;

public class RemoveFaveEvent extends GwtEvent<RemoveFaveEventHandler> {
	
	public static Type<RemoveFaveEventHandler> TYPE = new Type<RemoveFaveEventHandler>();
	
	private LoginInfo loginInfo;
	
	public RemoveFaveEvent(LoginInfo loginInfo){
		this.loginInfo = loginInfo;
	}

	@Override
	public com.google.gwt.event.shared.GwtEvent.Type<RemoveFaveEventHandler> getAssociatedType() {
		return TYPE;
	}

	@Override
	protected void dispatch(RemoveFaveEventHandler handler) {
		handler.onFaveRemoved(this);
	}
	
	public LoginInfo getLoginInfo(){
		return this.loginInfo;
	}

}
