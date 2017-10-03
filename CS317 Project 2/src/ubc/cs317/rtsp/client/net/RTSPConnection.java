/*
 * University of British Columbia
 * Department of Computer Science
 * CPSC317 - Internet Programming
 * Assignment 2
 * 
 * Author: Jonatan Schroeder
 * January 2013
 * 
 * This code may not be used without written consent of the authors, except for 
 * current and future projects and assignments of the CPSC317 course at UBC.
 */

package ubc.cs317.rtsp.client.net;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import ubc.cs317.rtsp.client.exception.RTSPException;
import ubc.cs317.rtsp.client.model.Frame;
import ubc.cs317.rtsp.client.model.FrameBuffer;
import ubc.cs317.rtsp.client.model.Session;

/**
 * This class represents a connection with an RTSP server.
 */
public class RTSPConnection {

	private static final int BUFFER_LENGTH = 15000;
	private static final long MINIMUM_DELAY_READ_PACKETS_MS = 20;
	private static final long MINIMUM_DELAY_PLAY_FRAME_MS = 30;

	private Session session;
	private Timer rtpTimer;

	private String server;
	private int port;
	private Socket rtspSocket;

	private DatagramSocket rtpSocket;
	private int cseq;
	private String sessionID;
	private String videoName;

	private BufferedWriter writer;
	private BufferedReader reader;
	
	private static int packetsReceived;
	private static int previousSeqNum;
	private static int outOfOrderPackets;
	private static int lostPackets;
	
	private Timer playTimer;
	private FrameBuffer frames;

	
	// TODO Add additional fields, if necessary

	/**
	 * Establishes a new connection with an RTSP server. No message is sent at
	 * this point, and no stream is set up.
	 * 
	 * @param session
	 *            The Session object to be used for connectivity with the UI.
	 * @param server
	 *            The hostname or IP address of the server.
	 * @param port
	 *            The TCP port number where the server is listening to.
	 * @throws RTSPException
	 *             If the connection couldn't be accepted, such as if the host
	 *             name or port number are invalid or there is no connectivity.
	 */
	public RTSPConnection(Session session, String server, int port)
			throws RTSPException {
		// TODO
		this.session = session;
		this.server = server;
		this.port = port;

		try {
			rtspSocket = new Socket(server, port);
			writer = new BufferedWriter(new OutputStreamWriter(
					rtspSocket.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(
					rtspSocket.getInputStream()));
		} catch (UnknownHostException e) {
			throw new RTSPException("Could not connect to " + server
					+ " on port " + port);
		} catch (IOException e) {
			throw new RTSPException("Could not connect to " + server
					+ " on port " + port);
		}
	}

	/**
	 * Sends a SETUP request to the server. This method is responsible for
	 * sending the SETUP request, receiving the response and retrieving the
	 * session identification to be used in future messages. It is also
	 * responsible for establishing an RTP datagram socket to be used for data
	 * transmission by the server. The datagram socket should be created with a
	 * random UDP port number, and the port number used in that connection has
	 * to be sent to the RTSP server for setup. This datagram socket should also
	 * be defined to timeout after 1 second if no packet is received.
	 * 
	 * @param videoName
	 *            The name of the video to be setup.
	 * @throws RTSPException
	 *             If there was an error sending or receiving the RTSP data, or
	 *             if the RTP socket could not be created, or if the server did
	 *             not return a successful response.
	 */
	public synchronized void setup(String videoName) throws RTSPException {
		// TODO
		cseq = 1;
		this.videoName = videoName;
		frames = new FrameBuffer(100);
		
		packetsReceived = 0;
		previousSeqNum = 0;
		outOfOrderPackets = 0;
		lostPackets = 0;

		Random random = new Random();
		int lowerBound = 1025;
		int upperBound = 65535;
		int udpPort = random.nextInt(upperBound - lowerBound) + lowerBound;

		try {
			rtpSocket = new DatagramSocket(udpPort);
			rtpSocket.setSoTimeout(1000);
		} catch (SocketException e1) {
			throw new RTSPException("Could not create RTP socket");
		}

		String request = "SETUP " + videoName + " RTSP/1.0\n" + "CSeq: " + cseq
				+ "\n" + "Transport: RTP/UDP; client_port= " + udpPort + "\n";

		try {
			sendRequest(request);
			RTSPResponse response = RTSPResponse.readRTSPResponse(reader);
			sessionID = response.getHeaderValue("Session");
			cseq++;
		} catch (IOException e) {
			throw new RTSPException(e.getMessage());
		}
	}

	/**
	 * Sends a PLAY request to the server. This method is responsible for
	 * sending the request, receiving the response and, in case of a successful
	 * response, starting the RTP timer responsible for receiving RTP packets
	 * with frames.
	 * 
	 * @throws RTSPException
	 *             If there was an error sending or receiving the RTSP data, or
	 *             if the server did not return a successful response.
	 */
	public synchronized void play() throws RTSPException {

		// TODO

		String request = "PLAY " + videoName + " RTSP/1.0\n" + "CSeq: " + cseq
				+ "\n" + "Session: " + sessionID + "\n";

		try {
			sendRequest(request);
			RTSPResponse response = RTSPResponse.readRTSPResponse(reader);
			if (response.getResponseCode() == 200) {
				startRTPTimer();
				startPlayTimer();
				cseq++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * Starts a timer that reads RTP packets repeatedly. The timer will wait at
	 * least MINIMUM_DELAY_READ_PACKETS_MS after receiving a packet to read the
	 * next one.
	 */
	private void startRTPTimer() {

		rtpTimer = new Timer();
		rtpTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				receiveRTPPacket();
			}
		}, 0, MINIMUM_DELAY_READ_PACKETS_MS);
	}
	
	private void startPlayTimer() {
		playTimer = new Timer();
		playTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				// Probably need to use frame timestamps to decide when to play a frame rather than just playing at a constant rate
				// Maybe something like delay = current timestamp - previous timestamp 
				playFrame();
			}
		}, 1400, MINIMUM_DELAY_PLAY_FRAME_MS);
	}

	/**
	 * Receives a single RTP packet and processes the corresponding frame. The
	 * data received from the datagram socket is assumed to be no larger than
	 * BUFFER_LENGTH bytes. This data is then parsed into a Frame object (using
	 * the parseRTPPacket method) and the method session.processReceivedFrame is
	 * called with the resulting packet. In case of timeout no exception should
	 * be thrown and no frame should be processed.
	 */
	private void receiveRTPPacket() {
		// TODO
		byte[] buffer = new byte[BUFFER_LENGTH];
		DatagramPacket rtpPacket = new DatagramPacket(buffer, BUFFER_LENGTH);
		try {
			rtpSocket.receive(rtpPacket);
			Frame frame = parseRTPPacket(rtpPacket.getData(),
					rtpPacket.getLength());
			frames.addFrame(frame);
			packetsReceived++;
			//System.out.println(packetsReceived);
			// session.processReceivedFrame(frame);
		} catch (SocketTimeoutException e) {
			// Do nothing
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void playFrame() {
		if (frames.hasFrame()) {
		session.processReceivedFrame(frames.getFrame()); 
		}
	}

	/**
	 * Sends a PAUSE request to the server. This method is responsible for
	 * sending the request, receiving the response and, in case of a successful
	 * response, cancelling the RTP timer responsible for receiving RTP packets
	 * with frames.
	 * 
	 * @throws RTSPException
	 *             If there was an error sending or receiving the RTSP data, or
	 *             if the server did not return a successful response.
	 */
	public synchronized void pause() throws RTSPException {

		// TODO

		String request = "PAUSE " + videoName + " RTSP/1.0\n" + "CSeq: " + cseq
				+ "\n" + "Session: " + sessionID + "\n";

		try {
			sendRequest(request);
			RTSPResponse response = RTSPResponse.readRTSPResponse(reader);
			if (response.getResponseCode() == 200) {
				rtpTimer.cancel();
				playTimer.cancel();
				cseq++;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Sends a TEARDOWN request to the server. This method is responsible for
	 * sending the request, receiving the response and, in case of a successful
	 * response, closing the RTP socket. This method does not close the RTSP
	 * connection, and a further SETUP in the same connection should be
	 * accepted. Also this method can be called both for a paused and for a
	 * playing stream, so the timer responsible for receiving RTP packets will
	 * also be cancelled.
	 * 
	 * @throws RTSPException
	 *             If there was an error sending or receiving the RTSP data, or
	 *             if the server did not return a successful response.
	 */
	public synchronized void teardown() throws RTSPException {

		// TODO
		if (rtpSocket == null)
			return;

		String request = "TEARDOWN " + videoName + " RTSP/1.0\n" + "CSeq: "
				+ cseq + "\n" + "Session: " + sessionID + "\n";

		try {
			sendRequest(request);
			RTSPResponse response = RTSPResponse.readRTSPResponse(reader);
			if (response.getResponseCode() == 200) {
				rtpSocket.close();
				rtpSocket = null;
				if (rtpTimer != null) {
					rtpTimer.cancel();
					rtpTimer = null;
				}
				if (playTimer != null) {
					playTimer.cancel();
					playTimer = null;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			throw new RTSPException(e.getMessage());
		}
	}

	/**
	 * Closes the connection with the RTSP server. This method should also close
	 * any open resource associated to this connection, such as the RTP
	 * connection, if it is still open.
	 */
	public synchronized void closeConnection() {
		try {
			if (rtpSocket != null)
				teardown();
			rtspSocket.close();
			rtspSocket = null;
		} catch (RTSPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Parses an RTP packet into a Frame object.
	 * 
	 * @param packet
	 *            the byte representation of a frame, corresponding to the RTP
	 *            packet.
	 * @return A Frame object.
	 */
	private static Frame parseRTPPacket(byte[] packet, int length) {

		// TODO
		// Extract payload type bits from packet and make into byte
		byte payloadType = (byte) ((packet[1] & 0xff) & 0x7f);

		// Check the marker bit to set marker boolean
		boolean marker;
		if ((byte) ((packet[1] & 0xff) & 0x80) == (byte) 0x80)
			marker = true;
		else
			marker = false;

		// Combine the 2 sequence number bytes to get a short
		short sequenceNumber = (short) ((packet[2] << 8) | (packet[3] & 0xff));
		
		if (sequenceNumber < previousSeqNum) {
			outOfOrderPackets++;
			lostPackets--;
		} else if (sequenceNumber != (previousSeqNum + 1)) {
			lostPackets += (sequenceNumber - (previousSeqNum + 1));
			previousSeqNum = sequenceNumber;
		} else {
			previousSeqNum = sequenceNumber;
		}
		
		// Combine the 4 timestamp bytes to get an int
		int timestamp = (((packet[4] & 0xff) << 24)
				| ((packet[5] & 0xff) << 16) | ((packet[6] & 0xff) << 8) | (packet[7] & 0xff));
		
		if (lostPackets < 0) lostPackets = 0;
		System.out.println("Number of packets received: " + packetsReceived);
		System.out.println("Number of packets received out of order: " + outOfOrderPackets);
		System.out.println("Number of packets lost: " + lostPackets);
		System.out.println("Current time (ms): " + System.currentTimeMillis());
		
		// Create payload byte using the rest of the packet
		byte[] payload = new byte[packet.length - 12];
		System.arraycopy(packet, 12, payload, 0, payload.length);

		Frame frame = new Frame(payloadType, marker, sequenceNumber, timestamp,
				payload);

		return frame;
	}

	private void sendRequest(String request) throws IOException {
		System.out.println(request);
		writer.write(request + "\r\n");
		writer.flush();
	}
}
