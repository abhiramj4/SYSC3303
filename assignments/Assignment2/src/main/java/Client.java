import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.*;


public class Client {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendReceiveSocket;

    public Client(){
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine. This socket will be used to
            // send and receive UDP Datagram packets.
            sendReceiveSocket = new DatagramSocket(8080);
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Send and receive packets through the sendReceiveSocket.
     */
    public void sendAndReceive() {
        // Prepare a DatagramPacket and send it via sendReceiveSocket
        // to port 5000 on the destination host.

        String s = "test.txt";
        System.out.println("Client: sending a packet containing:\n" + s);

        byte request[];
        byte finalMessage[];
        byte zero[] = new byte[1];
        zero[0] = 0;

        for(int i = 0; i < 2; i++){

            //alternate between read and write requests.
            request =  new byte[2];

            request[0] = 0;
            if(i % 2 == 0){
                //even number

                request[1] = 1;
            } else {

                request[1] = 2;
            }


            byte stringMessage[] = s.getBytes();

            String stringMode = "netascii";
            byte mode[] = stringMode.getBytes();

            finalMessage = new byte[20];

            System.arraycopy(request, 0, finalMessage, 0, request.length);
            System.arraycopy(stringMessage,0,finalMessage,request.length,stringMessage.length);
            System.arraycopy(zero,0, finalMessage, stringMessage.length + 2, zero.length);
            System.arraycopy(mode,0,finalMessage,stringMessage.length + 3,mode.length);


            try {
                sendPacket = new DatagramPacket(finalMessage, finalMessage.length,
                        InetAddress.getLocalHost(), 23);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: Sending packet:");
            System.out.println("To host: " + sendPacket.getAddress());
            System.out.println("Destination host port: " + sendPacket.getPort());
            int len = sendPacket.getLength();
            System.out.println("Length: " + len);
            System.out.println("Containing: ");

            if (request[1] == 1){
                System.out.println("Read request");
            } else {
                System.out.println("Write request");
            }

            System.out.println(s);
            System.out.println(stringMode);

            // Send the datagram packet to the server via the send/receive socket.

            try {
                sendReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: Packet sent.\n");


            // Construct a DatagramPacket for receiving packets up
            // to 100 bytes long (the length of the byte array).

            byte data[] = new byte[100];
            receivePacket = new DatagramPacket(data, data.length);





            try {
                // Block until a datagram is received via sendReceiveSocket.
                System.out.println("Waiting..."); // so we know we're waiting
                sendReceiveSocket.receive(receivePacket);
            } catch(IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            // Process the received datagram.
            System.out.println("Client: Packet received:");
            System.out.println("From host: " + receivePacket.getAddress());
            System.out.println("Host port: " + receivePacket.getPort());
            len = receivePacket.getLength();
            System.out.println("Length: " + len);
            System.out.print("Containing: ");


            if(data[1] == 3){
                System.out.println("Valid read request met");
            } else if (data[1] == 4){
                System.out.println("Valid write request met");
            } else {
                System.out.println("Error from server");
            }

        }

        // We're finished, so close the socket.
        sendReceiveSocket.close();
    }

    public static void main(String args[]) {
        Client c = new Client();
        c.sendAndReceive();
    }
}
