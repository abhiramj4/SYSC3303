import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

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

    public void sendAndReceive() {
        // Prepare a DatagramPacket and send it via sendReceiveSocket
        // to port 5000 on the destination host.

        String s = "test.txt";
        System.out.println("Client: sending a packet containing:\n" + s);

        // Java stores characters as 16-bit Unicode values, but
        // DatagramPackets store their messages as byte arrays.
        // Convert the String into bytes according to the platform's
        // default character encoding, storing the result into a new
        // byte array.

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for(int i = 0; i < 14; i++){

            //alternate between read and write requests.
            outputStream.write(0);
            if(i % 2 == 0){
                //even number
                outputStream.write(1);
            } else {
                outputStream.write(2);
            }


            byte msg[] = s.getBytes();
            try {
                outputStream.write(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            outputStream.write(0);

            String stringMode = "netascii";
            byte mode[] = stringMode.getBytes();

            try {
                outputStream.write(mode);
            } catch (IOException e) {
                e.printStackTrace();
            }

            outputStream.write(0);

            byte[] finalMessage = outputStream.toByteArray();

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
            System.out.print("Containing: ");
            System.out.println(new String(sendPacket.getData(),0,len)); // or could print "s"

            // Send the datagram packet to the server via the send/receive socket.

            try {
                sendReceiveSocket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }

            System.out.println("Client: Packet sent.\n");

            try {
                Thread.sleep(10000);
            } catch (InterruptedException e ) {
                e.printStackTrace();
                System.exit(1);
            }


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

            // Form a String from the byte array.
            String received = new String(data,0,len);
            System.out.println(received);

        }

        // We're finished, so close the socket.
        sendReceiveSocket.close();
    }

    public static void main(String args[]) {
        Client c = new Client();
        c.sendAndReceive();
    }
}
