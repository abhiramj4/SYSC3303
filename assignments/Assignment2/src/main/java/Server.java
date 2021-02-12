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


        if(data[0] == 0 && data[1] == 1){
            //read request
            System.out.println("read request: " );



            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            outputStream.write(0);
            outputStream.write(3);
            outputStream.write(0);
            outputStream.write(1);

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
