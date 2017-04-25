package tcp;


/*

I DID NOT WRITE THIS CODE

It was written by the professor of the related networking class
 */



import java.io.Serializable;

public class Segment implements Serializable
{
	private static final long serialVersionUID = 1L;

	public boolean isAck;
	public boolean isSyn;
	public int seqNum;
	public int ackNum;
	public int length;

	public Segment(boolean isSyn, boolean isAck, int seqNum, int ackNum, int length)
	{
		this.isAck = isAck;
		this.isSyn = isSyn;
		this.seqNum = seqNum;
		this.ackNum = ackNum;
		this.length = length;
	}

	@Override
	public String toString()
	{
		return "Segment [isAck=" + isAck + ", isSyn=" + isSyn + ", seqNum=" + seqNum + ", ackNum=" + ackNum
				+ ", length=" + length + "]";
	}

}
