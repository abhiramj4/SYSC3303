import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;

public class Server {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;


    public Server() {
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine. This socket will be used to
            // send UDP Datagram packets.
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to port 69
            // on the local host machine. This socket will be used to
            // receive UDP Datagram packets.
            receiveSocket = new DatagramSocket(69);

            // to test socket timeout (2 seconds)
            //receiveSocket.setSoTimeout(2000);
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    public void receiveAndEcho() {
        // Construct a DatagramPacket for receiving packets up
        // to 100 bytes long (the length of the byte array).

        byte data[] = new byte[100];
        receivePacket = new DatagramPacket(data, data.length);
        System.out.println("Server: Waiting for Packet.\n");

        // Block until a datagram packet is received from receiveSocket.
        try {
            System.out.println("Waiting..."); // so we know we're waiting
            receiveSocket.receive(receivePacket);
        } catch (IOException e) {
            System.out.print("IO Exception: likely:");
            System.out.println("Receive Socket Timed Out.\n" + e);
            e.printStackTrace();
            System.exit(1);
        }

        // Process the received datagram.
        System.out.println("Server: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );


        if(data[0] == 0 && data[1] == 1){
            //read request
            System.out.println("read request: " );

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
            byte[] modeOutput = textOutputStream.toByteArray();
            String mode = new String(modeOutput, StandardCharsets.UTF_8);
            System.out.println("Mode: " + mode);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            if(data[0] == 0 && data[1] == 1){
                outputStream.write(0);
                outputStream.write(3);
                outputStream.write(0);
                outputStream.write(1);
            } else {
                outputStream.write(0);
                outputStream.write(4);
                outputStream.write(0);
                outputStream.write(0);
            }




            byte[] readMsg = outputStream.toByteArray();


            sendPacket = new DatagramPacket(readMsg, readMsg.length,
                    receivePacket.getAddress(), 23);

        } else {

            System.out.println("write request: " );
            System.out.println("text content: " );
            System.out.println("Mode: ");

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(0);
            outputStream.write(4);
            outputStream.write(0);
            outputStream.write(0);

            byte[] writeMsg = outputStream.toByteArray();

            sendPacket = new DatagramPacket(writeMsg, writeMsg.length,
                    receivePacket.getAddress(), 23);

        }



        // Form a String from the byte array.
        String received = new String(data,0,len);
        System.out.println(received + "\n");

        // Slow things down (wait 5 seconds)
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e ) {
            e.printStackTrace();
            System.exit(1);
        }

        // Create a new datagram packet containing the string received from the client.

        // Construct a datagram packet that is to be sent to a specified port
        // on a specified host.
        // The arguments are:
        //  data - the packet data (a byte array). This is the packet data
        //         that was received from the client.
        //  receivePacket.getLength() - the length of the packet data.
        //    Since we are echoing the received packet, this is the length
        //    of the received packet's data.
        //    This value is <= data.length (the length of the byte array).
        //  receivePacket.getAddress() - the Internet address of the
        //     destination host. Since we want to send a packet back to the
        //     client, we extract the address of the machine where the
        //     client is running from the datagram that was sent to us by
        //     the client.
        //  receivePacket.getPort() - the destination port number on the
        //     destination host where the client is running. The client
        //     sends and receives datagrams through the same socket/port,
        //     so we extract the port that the client used to send us the
        //     datagram, and use that as the destination port for the echoed
        //     packet.



        System.out.println( "Server: Sending packet:");
        System.out.println("To host: " + sendPacket.getAddress());
        System.out.println("Destination host port: " + sendPacket.getPort());
        len = sendPacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: ");
        System.out.println(new String(sendPacket.getData(),0,len));
        // or (as we should be sending back the same thing)
        // System.out.println(received);

        // Send the datagram packet to the client via the send socket.
        try {
            sendSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Server: packet sent");

    }


    public static void main( String args[] ) {
        Server s = new Server();
        while(true){
            s.receiveAndEcho();
        }

    }
}
