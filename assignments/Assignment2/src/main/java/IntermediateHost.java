import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class IntermediateHost {

    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;

    public IntermediateHost(){
        try {
            // Construct a datagram socket and bind it to any available
            // port on the local host machine. This socket will be used to
            // send UDP Datagram packets.
            sendSocket = new DatagramSocket();

            // Construct a datagram socket and bind it to port 23
            // on the local host machine. This socket will be used to
            // receive UDP Datagram packets.
            receiveSocket = new DatagramSocket(23);

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
        System.out.println("Intermediate: Waiting for Packet.\n");

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
        System.out.println("Intermediate: Packet received:");
        System.out.println("From host: " + receivePacket.getAddress());
        System.out.println("Host port: " + receivePacket.getPort());
        int len = receivePacket.getLength();
        System.out.println("Length: " + len);
        System.out.print("Containing: " );

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

        //goes to the actual server

        if(receivePacket.getPort() == 8080){
            //packet came from the client and needs to go to the server.

            sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                    receivePacket.getAddress(), 69);

        } else {
            //packet came from the server.
            sendPacket = new DatagramPacket(data, receivePacket.getLength(),
                    receivePacket.getAddress(), 8080);

        }


        System.out.println( "Intermediate: Sending packet:");
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

        System.out.println("Intermediate: packet sent");


    }

    public static void main( String args[] ) {
        IntermediateHost intermediateHost = new IntermediateHost();

        while(true) {
            intermediateHost.receiveAndEcho();
        }
    }
}
