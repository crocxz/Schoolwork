package ubc.cs.cpsc310.rackbuddy.client;

import com.google.gwt.event.shared.EventHandler;

public interface RemoveFaveEventHandler  extends EventHandler {
	
	void onFaveRemoved(RemoveFaveEvent event);

}
