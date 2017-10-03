package ubc.cs317.rtsp.client.model;
import java.io.*;
import java.net.*;
import java.util.*;

import javax.swing.Timer;

import ubc.cs317.rtsp.client.model.Frame;

import java.util.LinkedList;

public class FrameBuffer {

	private LinkedList<Frame> frames;
	private int bufferSize;
	
	
	


public FrameBuffer(int size) {
		frames = new LinkedList<Frame>();
		bufferSize = size;
		
	}


public void setSize(int size) {
	bufferSize = size;
} 
public int getSize() {
	return bufferSize;
}

//public void addFrame(Frame f) {
	//frames.add(f);
 //}
public boolean hasFrame() {
	if (!frames.isEmpty()) {
		return true;
	}
	return false;
}

public void addFrame(Frame f) {
	//just add if empty buffer
	if (frames.isEmpty()) {
		frames.add(f);
		System.out.println("added first frame");
		return;
	}
	//check if buffer full
	if (frames.size() == bufferSize) {
		System.out.println("Full buffer");
		return; 
	}
	//check if packet time within valid range
	int offset = (f.getTimestamp() - frames.getLast().getTimestamp());
	// 40 is arbitrary
	if (offset >= 40|| offset <= -40) {
		System.out.println("packet invalid");
		return;
	}
	
	//check if single frame, add onto back if bigger, drop if not
	if (frames.size() == 1) {
		if (f.getSequenceNumber() > frames.getFirst().getSequenceNumber()) {
			frames.add(f);
			System.out.println("added frame at end");
			return;
		} else return;
	}
	//check this frames seq
	//check all frame seq
    int fseq = f.getSequenceNumber(); 
    int pos = 0;
    if (fseq > frames.getLast().getSequenceNumber()) {
    	frames.add(f);
    	System.out.println("added frame at end");
    } else {
    for (Frame fr : frames) {
  
    	//get position of the first frame that is later than insertion frame
    	if (fseq < fr.getSequenceNumber()) 
    		pos = fr.getSequenceNumber();
    }
    //add frame at previous latest frame, pushing it back
    frames.add(pos, f);
    }
    	
	
	
	
}

public Frame getFrame() {
	Frame frame = frames.getFirst();
	frames.removeFirst();
	System.out.println("dequeued frame");
	return frame;
	
}
//public void organize() {
///	ArrayList<Frame> newf = new ArrayList<Frame>();
//	Frame tempf = null;
////	int num = 0xFFFFFFFF;
///	do {
////			for (Frame f : frames) {
//				if (f.getSequenceNumber() < num) 
					//num = f.getSequenceNumber();
					//tempf = f; 
				//	} 
			//	newf.add(tempf);
		//		frames.remove(tempf);
	//} while (!frames.isEmpty());
//frames = newf;
//}

}