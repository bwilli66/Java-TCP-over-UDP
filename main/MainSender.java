package main;

import network.Network;

import tcp.Segment;


import java.net.DatagramSocket;
import java.util.*;


public class MainSender
{
    public final static double LOSS_RATE = 0.2;
    public final static int PORT = 1574;
    public final static int TIMEOUT = 10;
    public final static String ipAddress = "192.168.1.3";
    public static int senderSeqNum = 2000;//sender sequence number


	public static void main(String[] args) throws Exception
	{
		//start at zero
		int time = 0;

		//HashMap, populated sent segments and their corresponding timeouts
		HashMap<Segment, Integer> segmentsSent = new HashMap();

		Random r = new Random(0);

		//create a network object
		final Network network = new Network(r, LOSS_RATE);
		//set up socket
		final DatagramSocket socket = new DatagramSocket(1574);

		Segment segment = new Segment(true, false, senderSeqNum, 0, 1); //THIS  A SYN
		// Send SYN
		network.send(socket, ipAddress, PORT, segment);
		System.out.println("SYN Sent");

		// wait for SYN ACK
		Segment synAck = network.receive(socket);

		System.out.println("SYN ACK Received");

		updateSeqNum(1);// Update seqNum with ack

		// Generate random lengths for segments
		int [] lengths = new int [10];
		for(int i = 0; i < lengths.length; ++i)
			lengths[i] = r.nextInt(50);

		// Create a bunch of segments
		Segment [] segments = new Segment [] {
				new Segment(false, false, senderSeqNum, 1, lengths[0]),
				new Segment(false, false, updateSeqNum(lengths[0]), 1, lengths[1]),
				new Segment(false, false, updateSeqNum(lengths[1]), 1, lengths[2]),
				new Segment(false, false, updateSeqNum(lengths[2]), 1, lengths[3]),
				new Segment(false, false, updateSeqNum(lengths[3]), 1, lengths[4]),
				new Segment(false, false, updateSeqNum(lengths[4]), 1, lengths[5]),
				new Segment(false, false, updateSeqNum(lengths[5]), 1, lengths[6]),
				new Segment(false, false, updateSeqNum(lengths[6]), 1, lengths[7]),
				new Segment(false, false, updateSeqNum(lengths[7]), 1, lengths[8]),
				new Segment(false, false, updateSeqNum(lengths[8]), 1, lengths[9]),
		};

		// Add segments to HashMap, with a time value
		for(int i = 0; i < segments.length; ++i){
			segmentsSent.put(segments[i], 100);
		}

		// Send segments
		network.send(socket, ipAddress, PORT, segments);

		// Set socket timeout
		socket.setSoTimeout(TIMEOUT);

		while (true)
		{

			Segment s = network.receive(socket);

			if (s != null) {

				//remember ackNum
				int ackFromReceiver = s.ackNum;

				//if ack
				if(s.isSyn == false && s.isAck == true) {

					//take in new ack and delete segs that have been acknowledged
					Iterator<Map.Entry<Segment, Integer>> it = segmentsSent.entrySet().iterator();
					while (it.hasNext()) {
						Map.Entry<Segment, Integer> pair = it.next();

						Segment key = pair.getKey();

						if (key.seqNum < ackFromReceiver) {
							it.remove();
							System.out.println(key + " acknowledged!");
						}
					}

				}
			}

			else{
				//send everything not acknowledged

				time += TIMEOUT;//update time
				System.out.println("Time: " + time);

				// Iterate through hashmap to check for segs to be resent
				Iterator it = segmentsSent.entrySet().iterator();
				while (it.hasNext()) {
					Map.Entry<Segment, Integer> pair = (Map.Entry)it.next();

					Segment key = pair.getKey();
					int timeOut = pair.getValue();

					// If time reaches timeout in hashmap, send segment again
					if(timeOut == time){
						network.send(socket, ipAddress, PORT, key);

						// Timeout quicker at higher LOSS_RATE
						if(LOSS_RATE < 0.8)
							segmentsSent.put(key, timeOut * 2);
						else
							segmentsSent.put(key, timeOut + 200);

						System.out.println("Time:" + time);
						System.out.println("TimeOut:" + timeOut + "\n");
					}
				}

			}

		}

		//socket.close();
	}
        
        public static int updateSeqNum(int length){
            senderSeqNum += length;
            return senderSeqNum;
        }
}
