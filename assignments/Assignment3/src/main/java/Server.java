import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class Server {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;


    //the server behaves in the reverse order of the client, sending requests first then pushing to the intermediate
    public Server() {
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to port 69
            // on the local host machine
            receiveSocket = new DatagramSocket(69);

        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Receive and send back to intermediateHost
     */
    public void receiveAndSend() {

        boolean waitForData = true;
        byte[] data = new byte[100];
        while(waitForData) {

            receivePacket = new DatagramPacket(data, data.length);
            try {
                //try and get some data
                data[0] = 0;
                data[1] = 8;
                data[2] = 5;
                sendPacket = new DatagramPacket(data, data.length,
                        InetAddress.getLocalHost(), 32);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }
            //send it
            try {
                sendSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            try {
                // Block until a datagram is received via sendReceiveSocket
                System.out.println("Waiting..."); // waiting
                receiveSocket.receive(receivePacket);
            } catch(IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            if(data[1] == 1){
                System.out.println("Valid read request received");
                waitForData = false;
            } else if (data[1] == 2){
                System.out.println("Valid write request received");
                waitForData = false;
            } else {
                // theres no data from the server yet, send another request - this is also the ack
                System.out.println("Can't retrieve data, trying to request again");

            }

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e ) {
                e.printStackTrace();
                System.exit(1);
            }

        }

        // Construct a DatagramPacket for receiving packets up
        // to 100 bytes long

        byte[] returnMessage = new byte[4];

        // Process the received datagram
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.println("Containing: " );

        ByteArrayOutputStream textOutputStream = new ByteArrayOutputStream();

        //convert bytes into string
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

        //depending on if the first digits are 01 or 02, either read or write request
        if(data[0] == 0 && data[1] == 1){
            //read request
            System.out.println("read request" );

            returnMessage[0] = 0;
            returnMessage[1] = 3;
            returnMessage[2] = 0;
            returnMessage[3] = 1;

        } else {

            //write request
            System.out.println("write request" );

            returnMessage[0] = 0;
            returnMessage[1] = 4;
            returnMessage[2] = 0;
            returnMessage[3] = 0;

        }

        System.out.print("as bytes: ");
        for(int i = 0; i < data.length; i++){
            System.out.print(data[i] + " ");
        }
        System.out.println("\n");

        sendPacket = new DatagramPacket(returnMessage, returnMessage.length,
                receivePacket.getAddress(), 32);


        System.out.println( "Server: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");

        if(data[1] == 3 ){
            System.out.print("valid read request \n");
        } else {
            System.out.print("valid write request \n");
        }

        // Send the datagram packet to the client via the send socket
        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Server: packet sent \n");

        //get acknowledgment
        try {
            // Block until a datagram is received via sendReceiveSocket
            System.out.println("Waiting for acknowledgment..."); // waiting
            receiveSocket.receive(receivePacket);
        } catch(IOException e) {
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

    }


    public static void main( String args[] ) {
        Server s = new Server();

        //run infinitely
        while(true){
            s.receiveAndSend();
        }

    }
}
