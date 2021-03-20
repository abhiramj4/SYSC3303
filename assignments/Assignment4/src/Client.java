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

            //the client has a receive and send socket of 8080.
            sendReceiveSocket = new DatagramSocket(8080);
        } catch (SocketException se) {   // Can't create the socket.
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Send and receive packets through the sendReceiveSocket - sends to the intermediate host at port 23
     */
    public void sendAndReceive() {
        // Prepare a DatagramPacket and send it via sendReceiveSocket

        String s = "testestAbhiTest.txt"; //change to whatever text

        byte[] request; //either read or write request
        byte[] finalMessage; //the final message that is sent
        byte[] zero = new byte[1]; //just a single 0 byte

        //go 11 times
        for(int i = 0; i < 1000; i++){

            //alternate between read and write requests.
            request = new byte[2];

            if(i % 2 == 0){
                //even number

                request[1] = 1;
            } else {

                request[1] = 2;
            }


            byte[] stringMessage = s.getBytes(); //string message from earlier as bytes

            String stringMode = "netAsCii"; //mode
            byte[] mode = (stringMode.toLowerCase()).getBytes(); //byte version of mode.

            //final message as combination of all lengths that will be in the message
            finalMessage = new byte[request.length + stringMessage.length + mode.length + 2];

            //copy all other arrays into teh final message
            System.arraycopy(request, 0, finalMessage, 0, request.length);
            System.arraycopy(stringMessage,0,finalMessage,request.length,stringMessage.length);
            System.arraycopy(zero,0, finalMessage, stringMessage.length + 2, zero.length);
            System.arraycopy(mode,0,finalMessage,stringMessage.length + 3,mode.length);

            //create send packet
            try {
                sendPacket = new DatagramPacket(finalMessage, finalMessage.length,
                        InetAddress.getLocalHost(), 23);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: Sending packet: " + i);
            System.out.println("To host: " + sendPacket.getAddress());
            System.out.println("Destination host port: " + sendPacket.getPort());
            int len = sendPacket.getLength();
            System.out.println("Length: " + len);

            //try and send to intermediate host at port 23
            try {
                sendReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: Packet sent.\n");

            //so the client has now sent a packet to the intermediate responsible for client server transmission
            //now wait for the ack package before continuing


            // Construct a DatagramPacket for receiving packets up
            // to 1000 bytes long (the length of the byte array).

            byte[] data = new byte[1000];
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
            System.out.println("Client: Packet received:");
            System.out.println("From host: " + receivePacket.getAddress());
            System.out.println("Host port: " + receivePacket.getPort());
            len = receivePacket.getLength();
            System.out.println("Length: " + len);

            /*
            //wait for a bit here :
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e ) {
                e.printStackTrace();
                System.exit(1);
            }
            */
            //now try making a request for the data

            boolean waitForData = true;

            while(waitForData){

                //send a request for data
                byte[] requestData = new byte[1000];
                requestData[0] = 0;
                requestData[1] = 7;
                requestData[2] = 3;
                try {
                    sendPacket = new DatagramPacket(requestData, requestData.length,
                            InetAddress.getLocalHost(), 23);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                //send it
                try {
                    sendReceiveSocket.send(sendPacket);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                System.out.println("Client: Request packet sent.\n");

                try {
                    // Block until a datagram is received via sendReceiveSocket
                    System.out.println("Waiting..."); // waiting
                    sendReceiveSocket.receive(receivePacket);
                } catch(IOException e) {
                    e.printStackTrace();
                    System.exit(1);
                }

                if(data[1] == 3){
                    System.out.println("Valid read request from server");
                    waitForData = false;
                } else if (data[1] == 4){
                    System.out.println("Valid write request from server");
                    waitForData = false;
                } else {
                    // theres no data from the server yet, send another request
                    System.out.println("Acknowledged: But can't retrieve data, trying to request again");

                }

                //wait 5 before sending another request
                /*
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e ) {
                    e.printStackTrace();
                    System.exit(1);
                }

                 */
            }
        }

        //close the socket after the 14 iterations
        sendReceiveSocket.close();
    }

    public static void main(String args[]) {
        Client c = new Client();
        c.sendAndReceive();
    }
}
