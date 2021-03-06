import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

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
 * all ack packages are just 00, requests from the server are 55.
 *
 * IMPORTANT - ALL PACKET STRUCTURES:
 *
 * Client datagram packet:
 * 0 1 (filename in binary) 0 (mode in binary) 0 - read request
 * OR
 * 0 2 (filename in binary) 0 (mode in binary) 0 - write request
 *
 * 0 7 3 - request for data from intermediate
 *
 * Server datagram packet:
 * 0 3 0 1 - valid read request
 * OR
 * 0 4 0 0 - valid write request
 *
 * 0 8 5 - request for data from server
 *
 * Intermediate packets (NOT stored in the queue):
 * 0 5 2 - acknowledgement to either client or server
 *
 * 0 6 4 - no data for server or client data
 *
 * otherwise the packet just returns the requested data, it is not
 * necessary to send a packet to the client or server warning about
 * incoming data.
 *
 * NOTE: once either server or client recieve a packet, they will do
 * different things depending on the first two digits of the packet data.
 * This is how RPC is implemented in this program.
 *
 */
public class IntermediateHost extends Thread {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;

    private LinkedBlockingQueue<DatagramPacket> packetQueue;
    private int destinationPort;

    public IntermediateHost(LinkedBlockingQueue packetQueue, int destinationPort, int receivePort){

        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to port 23
            // on the local host machine
            receiveSocket = new DatagramSocket(receivePort);

            //packetQueue = (Queue) Collections.synchronizedList(new LinkedList<DatagramPacket>());
            this.packetQueue = packetQueue;
            this.destinationPort = destinationPort;

        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Reply with an acknowledgment always when getting a packet
     * @param data data to reply with
     */
    public void reply(byte[] data){


        sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                receivePacket.getAddress(), destinationPort); //send this acknowledgment to the client or server

        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    /**
     * Get packet from either server or client, depending on the thread, and send to the destination
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

        byte[] ack = new byte[100];
        ack[0] = 0;
        ack[1] = 5;
        ack[2] = 2;

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e ) {
            e.printStackTrace();
            System.exit(1);
        }

        reply(ack); //reply to the received packet

        // Slow things down (wait 5 seconds)


        /*
        First if statement is receiving a packet from either client or server
         */

        if( (data[0] == 0 && data[1] == 1) || (data[0] == 0 && data[1] == 2) ){
            //this is a package from client that wants to put into the queue
            packetQueue.add(receivePacket);
        } else if ( (data[0] == 0 && data[1] == 7 && data [2] == 3) ||
                (data[0] == 0 && data[1] == 8 && data [2] == 5) ){
            //request to get the packet from the client

            DatagramPacket packetToSend = packetQueue.remove();

            //there is a packet to send back
            if(packetToSend != null){
                DatagramPacket sendPacketToDestination = new DatagramPacket(packetToSend.getData(), packetToSend.getLength(),
                        packetToSend.getAddress(), destinationPort);

                try {
                    sendSocket.send(sendPacketToDestination);

                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }


            } else {

                //so theres no packet to send back so send back a no data reply

                byte[] reply = new byte[100];
                ack[0] = 0;
                ack[1] = 6;
                ack[2] = 4;

                reply(reply);
            }

        } else {

        }

    }

    @Override
    public void run(){
        receiveAndSend();
    }

    public static void main( String args[] ) {

        //shared queue
        LinkedBlockingQueue<DatagramPacket> lbq = new LinkedBlockingQueue<DatagramPacket>();

        //sending from client at 8080 to port 23 on the intermediate thread
        IntermediateHost clientServer = new IntermediateHost(lbq,8080, 23);
        //sending from server at 69 to 32 on second intermediate thread
        IntermediateHost serverClient = new IntermediateHost(lbq, 69, 32);

        clientServer.start();
        serverClient.start();

    }
}
