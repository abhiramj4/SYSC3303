import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The way that this class now works is that the intermediateHost program spawns 2 threads, each with two
 * different ports. Port 23 handles requests from the client and port 35 handles requests coming from the server
 *
 * The "run" of each thread continuously listens for requests from their respective clients, and upon receiving
 * a packet, they will send back an acknowledge packet. IntermediateHost will implement a synchronized queue that the
 * intermediate will add to when it receives a packet from a client. The synchronized queue will mean that
 * only one of the intermediate threads can access it at once. If a packet in the queue has a destination different than
 * the the port's assigned client, it will break. However, if the destination is for the assigned client, then the
 * queue will dequeue and send it along.
 *
 * Note that each intermediate has a destination port, when its done sending the ack it will check the queue head to
 * see if the packet there was sent from the server.
 *
 *
 */
public class IntermediateHost extends Thread {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;
    DatagramSocket sendReceiveSocket;

    private int port;
    private int destinationPort;

    private static Queue packetQueue;

    public IntermediateHost(int port, int destinationPort){
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to the input port
            // on the local host machine
            receiveSocket = new DatagramSocket(port);

            sendReceiveSocket = new DatagramSocket(port);

            packetQueue = (Queue) Collections.synchronizedCollection(new LinkedList<DatagramPacket>());
            this.port = port;
            //packetQueue = new LinkedList<DatagramPacket>();

        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }



    /**
     * Get packet from either server or client and send to the opposite
     */
    public void receiveAndSend() {
        // Construct a DatagramPacket for receiving packets up
        // to 100 bytes long


        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Intermediate: Waiting for Packet.");

        // Block until a datagram packet is received from receiveSocket
        try {
            System.out.println("Waiting...\n"); //waiting
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram
        System.out.println("Intermediate: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);


        // Slow things down (wait 5 seconds)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e ) {
            e.printStackTrace();
            System.exit(1);
        }

        if(receivePacket.getPort() == 8080){
            //packet came from the client and needs to go to the server

            sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                    receivePacket.getAddress(), 69);

            //print out what is contained
            System.out.println("containing: ");

            if(data[1] == 1){
                System.out.println("Read request");
            } else if (data[1] == 2){
                System.out.println("Write request");
            }

            ByteArrayOutputStream textOutputStream = new ByteArrayOutputStream();

            //convert bytes into string and read out text content
            for(int i = 2; i < data.length; i++){
                if(data[i] == 0){
                    break;
                } else {
                    textOutputStream.write(data[i]);
                }
            }
            byte[] textOutput = textOutputStream.toByteArray();
            String textContent = new String(textOutput, StandardCharsets.UTF_8);
            System.out.println("text content: "+ textContent);
            textOutputStream.reset();


            for(int i = textOutput.length + 3; i < data.length; i++){
                if(data[i] == 0){
                    break;
                } else {
                    textOutputStream.write(data[i]);
                }
            }
            String mode = textOutputStream.toString(StandardCharsets.UTF_8);
            System.out.println("Mode: " + mode);


        } else {
            //packet came from the server and needs to go to client
            sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                    receivePacket.getAddress(), 8080);

            if(data[1] == 3){
                System.out.println("Valid read request met");
            } else if (data[1] == 4){
                System.out.println("Valid write request met");
            } else {
                System.out.println("Error from server");
            }
        }

        System.out.print("as bytes: ");
        for(int i = 0; i < data.length; i++){
            System.out.print(data[i] + " ");
        }

        System.out.println(" \n");

        System.out.println( "Intermediate: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        len = sendPacket.getLength();
        System.out.println("Length: " + len);

        // Send the datagram packet to the client via the send socket
        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Intermediate: packet sent");
        System.out.println("");

    }

    /**
     * reply to server or client
     */
    public synchronized void rpcReceive(){

        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);

        try {
            // Block until a datagram is received via sendReceiveSocket
            System.out.println("Waiting..."); // waiting
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram
        System.out.println("Intermediate: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);

        //do something different for server, since it is the only program to make requests to teh intermediate host
        //need to check if the receive packet is a request from

        packetQueue.add(receivePacket); //add packet to queue in safe method

        //byte ackData[] = new byte[100];
        String ack = "Acknowledged";
        byte[] ackData = ack.getBytes();

        sendPacket = new DatagramPacket(ackData, receivePacket.getLength(),
                receivePacket.getAddress(), destinationPort);

        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Intermediate: packet sent");
        System.out.println("");

    }

    public synchronized void rpcSend(){

        assert packetQueue.peek() != null;
        if(((DatagramPacket)packetQueue.peek()).getPort() != this.destinationPort){

            /*
              if the packet in the queue comes from another port it is intended for the opposite client
              packets in the queue will only have ports from 8080 or 8081.
             */

            byte data[] = new byte[100];

            sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                    receivePacket.getAddress(), destinationPort);

            if(data[1] == 3){
                System.out.println("Valid read request met");
            } else if (data[1] == 4){
                System.out.println("Valid write request met");
            } else {
                System.out.println("Error");
            }

            System.out.print("as bytes: ");
            for(int i = 0; i < data.length; i++){
                System.out.print(data[i] + " ");
            }

            System.out.println(" \n");

            System.out.println( "Intermediate: Sending packet:");
            System.out.println("To host: " + sendPacket.getAddress());
            System.out.println("Destination host port: " + sendPacket.getPort());
            int len = sendPacket.getLength();
            System.out.println("Length: " + len);

            // Send the datagram packet to the client via the send socket
            try {
                sendSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Intermediate: packet sent");
            System.out.println("");

        }

        //else just end and go back to receiving.


    }

    @Override
    public void run(){
        while(true){
            rpcReceive();
            rpcSend();
        }

    }

    public static void main(String args[]) {
        IntermediateHost clientHost = new IntermediateHost(23, 8080);
        IntermediateHost serverHost = new IntermediateHost(35, 8081);

        clientHost.start();
        serverHost.start();
    }


}
