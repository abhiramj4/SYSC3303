import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;

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
 */
public class IntermediateHost extends Thread {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;

    private List packetQueue = new LinkedList<DatagramPacket>();

    public IntermediateHost(int port){
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to the input port
            // on the local host machine
            receiveSocket = new DatagramSocket(port);

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
    public synchronized void rpcReply(){

    }

    @Override
    public void run(){
        while(true){
            rpcReply();
        }

    }

    public static void main(String args[]) {
        IntermediateHost clientHost = new IntermediateHost(23);
        IntermediateHost serverHost = new IntermediateHost(35);

        clientHost.start();
        serverHost.start();
    }


}
