package Bank;

/**
 * Created by BradWilliams on 6/23/16.
 */

import java.io.*;
import java.net.*;

public class Bank {
    static String [] accountNumbers = new String [] {"4","6","8","10"};
    static String [] passwords =  new String [] {"AAA","BBB","CCC","DDD"};
    static int [] balances = new int [] {10,20,30,40};

    public static void main(String[] args) throws Exception{



        DatagramSocket serverSocket = new DatagramSocket(1574);
        byte[] receiveData = new byte[1024];
        byte[] sendData = new byte[1024];

        while(true)
        {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivePacket);

            InetAddress IPAddress = receivePacket.getAddress();
            int port = receivePacket.getPort();


            String stringReceived = new String (receivePacket.getData()).substring(0,receivePacket.getLength());
            System.out.println("RECEIVED: " + stringReceived);

            String [] stringArray = stringReceived.split(" ");

            String responseSentence = "";

            int k = check(stringArray[1],stringArray[2]);

            if (k == -1) {
                responseSentence = "The information you have entered is invalid";
            }


            else if(stringArray[0].equals("Login")){
                responseSentence = "Login Successful";
            }
            else if(stringArray[0].equals("Deposit")){
                balances[k]+= Integer.parseInt(stringArray[3]);

                responseSentence = stringArray[3] + " deposited. Your new balance is " + balances[k];
            }
            else if(stringArray[0].equals("Withdrawal")){
                balances[k]-= Integer.parseInt(stringArray[3]);

                responseSentence = stringArray[3] + " withdrew. Your new balance is " + balances[k];
            }
            else if(stringArray[0].equals("Balance")){
                responseSentence = "Your balance is " + balances[k];
            }
            else
                responseSentence = "The information you have entered is invalid";

            sendData = responseSentence.getBytes();

            DatagramPacket sendPacket =
                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
            serverSocket.send(sendPacket);


        }
    }

    public static int check(String receivedNum, String receivedPassword) {

        for (int i = 0; i < accountNumbers.length; i++)
            if (receivedNum.equals(accountNumbers[i]) && receivedPassword.equals(passwords[i]))
                return i;

        return -1;
    }


}
