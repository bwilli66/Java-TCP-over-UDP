package main;

import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Random;

import network.Network;
import tcp.Segment;



public class MainReceiver
{

	public final static double LOSS_RATE = 0.5;
	public final static int PORT = 1574;
	public final static int TIMEOUT = 10;
	public final static String ipAddress = "10.100.5.20";

	public static void main(String[] args) throws Exception
	{
		ArrayList<Segment> segments = new ArrayList();// ArrayList to hold segments received from sender.
		int receiverSeqNum = 1000; // starting sequence number for receiver
		int currentAck = -1;

		Random r = new Random(0);
		final Network network = new Network(r, LOSS_RATE);
		final DatagramSocket socket = new DatagramSocket(1574);

		Segment syn = network.receive(socket); //wait for SYN

		if (syn.isSyn == true && syn.isAck == false) {
			System.out.println("SYN received");
			currentAck = syn.seqNum + 1;//first ACK

			Segment synAck = new Segment(true, true, receiverSeqNum, currentAck, 1); //SYN ACK
			network.send(socket, ipAddress, PORT, synAck);
			System.out.println("SYN ACK sent");
			System.out.println();

		}

		while (true)
		{

			Segment s = network.receive(socket);

			if (s != null) {
				System.out.println("Received: " + s );

				for (int i = 0; i < segments.size(); ++i) {
					System.out.println(segments.get(i));
				}
				System.out.println("currentack: " + currentAck);
				System.out.println();

				if (s.seqNum < currentAck) {
					Segment segment = new Segment(false, true, receiverSeqNum + 1, currentAck, 0); //ACK
					network.send(socket, ipAddress, PORT, segment);
					System.out.println("Ack Sent Again: " + currentAck);
				}
				else {

					int lastAck = currentAck;


					//check for duplicates
					boolean addSeg = true;

					for(int i = 0; i < segments.size(); ++i) {
						if (s.seqNum == segments.get(i).seqNum) {
							addSeg = false;
							break;
						}
					}

					// Add segment only if is not a duplicate
					if(addSeg) {
						segments.add(s);// add new segments to ArrayList
					}


					//Check so see if any segments can be acknowledged
					while (true) {
						Segment x = null;
						for (int i = 0; i < segments.size(); ++i) {
							if (segments.get(i).seqNum == currentAck) {
								x = segments.get(i);
								break;
							}
						}
						if (x == null)
							break;
						else {
							currentAck = x.seqNum + x.length;
							segments.remove(x);
						}

					}


					// if the ack wasn't updated
					if (lastAck != currentAck) {
						Segment segment = new Segment(false, true, receiverSeqNum + 1, currentAck, 0); //ACK
						network.send(socket, ipAddress, PORT, segment);
						System.out.println("Ack Sent: " + currentAck + "\n");
					}


					for (int i = 0; i < segments.size(); ++i) {
						System.out.println(segments.get(i));
					}
					System.out.println("currentack: " + currentAck);
					System.out.println();
				}


			}
		}

		//socket.close();
	}
}
