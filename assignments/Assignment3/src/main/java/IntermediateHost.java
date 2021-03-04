import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;

/**
 * This class now is a two way channel, it basically implements TCP but using the UDP sockets and datagrams.
 *
 * Only one channel can be functioning at one time. The workflow is as so:
 *
 * Client sends a request to server, the intermediate receives it and sends an ack message to the client saying it got
 * the message. It will then place the packet in the queue go back to listening for requests
 *
 * The server will keep attempting to get a packet from the queue but unless a packet exists, it will not succeed BUT
 * will receive an ack message. If there is a packet, then the serverClient thread can send a message to the client.
 *
 */
public class IntermediateHost extends Thread {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;

    private Queue packetQueue;
    private int destinationPort;

    public IntermediateHost(Queue packetQueue, int destinationPort){
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to port 23
            // on the local host machine
            receiveSocket = new DatagramSocket(23);

            //packetQueue = (Queue) Collections.synchronizedList(new LinkedList<DatagramPacket>());
            this.packetQueue = packetQueue;
            this.destinationPort = destinationPort;

        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get packet from either server or client, depending on the thread, and send to the opposite
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

        //send back an ack package always
        byte[] ack = new byte[100];
        ack[0] = 0;
        ack[1] = 0; //00 ack flag
        sendPacket = new DatagramPacket(ack, receivePacket.getLength(),
                receivePacket.getAddress(), receivePacket.getPort());

        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Slow things down (wait 5 seconds)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e ) {
            e.printStackTrace();
            System.exit(1);
        }

        //2 2 is a request from server - only the serverClient thread cares about this
        if(data[0] == 2 && data[1] == 2){
            //do not add request packages to this queue, these only come from the server
            //break out since there's nothing to send
            return;
        } else {
            packetQueue.add(receivePacket);
        }

        /*
        Now take a look at the top packet, and send it. if we are coming from the server then this should
         */

        try {
            DatagramPacket packetToSend = ((DatagramPacket)(packetQueue.remove()));

            DatagramPacket sendPacketClient = new DatagramPacket(packetToSend.getData(), packetToSend.getLength(),
                    packetToSend.getAddress(), destinationPort);

            sendSocket.send(sendPacketClient);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        /*

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


        }
        else {
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


         */


    }

    @Override
    public void run(){
        receiveAndSend();
    }

    public static void main( String args[] ) {

        Queue packetQueue = (Queue) Collections.synchronizedList(new LinkedList<DatagramPacket>()); //shared queue

        IntermediateHost clientServer = new IntermediateHost(packetQueue,69); //channel from client to server
        IntermediateHost serverClient = new IntermediateHost(packetQueue, 8080); //channel from server to client

        clientServer.start();
        serverClient.start();

    }
}
