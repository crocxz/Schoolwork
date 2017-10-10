package ubc.cs.cpsc310.rackbuddy.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CommentDialogBox extends DialogBox {
	public CommentDialogBox(BikeRackData object, LoginInfo loginInfo) {
        // Set the dialog box's caption.
        setText("Comments");

        // Enable animation.
        setAnimationEnabled(true);

        // Enable glass background.
        setGlassEnabled(true);

        // DialogBox is a SimplePanel, so you have to set its widget 
        // property to whatever you want its contents to be.
        Button close = new Button("Close");
        close.addClickHandler(new ClickHandler() {
           public void onClick(ClickEvent event) {
              CommentDialogBox.this.hide();
           }
        });
        
        //share button
        Button share = new Button("Share");
        share.setStyleName("TopRight");
        share.addClickHandler(new ClickHandler() {
		      public void onClick(ClickEvent event) {
		        RackBuddy.shareFb();
		      }
		    });
       
        
        HorizontalPanel top = new HorizontalPanel();
        VerticalPanel panel = new VerticalPanel();
        CommentTable comments = new CommentTable(object, loginInfo);
        top.add(close);
        top.add(share);
        panel.add(top);
        panel.add(comments);
        setWidth("45em");
        setWidget(panel);
     }
}
