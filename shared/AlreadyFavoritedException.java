package ubc.cs.cpsc310.rackbuddy.shared;

public class AlreadyFavoritedException extends Exception {
	
	public AlreadyFavoritedException() {
		super();
	}
	
	public AlreadyFavoritedException(String message){
		super(message);
	}

}
