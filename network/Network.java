package network;


/*

I DID NOT WRITE THIS CODE

It was written by the professor of the related networking class
 */



import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Random;
import tcp.Segment;

public class Network
{
	private Random r;
	private double lossRate;

	public Network(Random r, double lossRate)
	{
		this.r = r;
		this.lossRate = lossRate;
	}

	public void send(DatagramSocket socket, String hostName, int destPort, Segment segment) throws Exception
	{
		//	let the segment through if its a syn (or a syn-ack)
		if (segment.isSyn || r.nextDouble() > lossRate)
		{
			InetAddress address = InetAddress.getByName(hostName);
			ByteArrayOutputStream byteStream = new ByteArrayOutputStream(5000);
			ObjectOutputStream os = new ObjectOutputStream(new BufferedOutputStream(byteStream));
			os.flush();
			os.writeObject(segment);
			os.flush();
			byte[] sendBuf = byteStream.toByteArray();
			DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, address, destPort);
			System.out.println("Sending : " + segment);
			socket.send(packet);
			os.close();
		}
		else
		{
			System.out.println("Dropping : " + segment);
		}
	}

	public void send(DatagramSocket socket, String hostName, int destPort, Segment... segments) throws Exception
	{
		permute(segments);
		for (Segment s : segments)
		{
			send(socket, hostName, destPort, s);
		}
	}

	public Segment receive(DatagramSocket socket) throws Exception
	{
		byte[] recvBuf = new byte[5000];
		DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
		try
		{
			socket.receive(packet);
		}
		catch (SocketTimeoutException ex)
		{
			return null;
		}
		ByteArrayInputStream byteStream = new ByteArrayInputStream(recvBuf);
		ObjectInputStream is = new ObjectInputStream(new BufferedInputStream(byteStream));
		Segment o = (Segment) is.readObject();
		is.close();
		return (o);
	}

	private void permute(Segment[] xs)
	{
		for (int i = xs.length; --i > 0;)
		{
			int pos = r.nextInt(i);
			Segment temp = xs[pos];
			xs[pos] = xs[i];
			xs[i] = temp;
		}
	}
}
