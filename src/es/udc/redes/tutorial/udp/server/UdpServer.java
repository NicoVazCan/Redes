package es.udc.redes.tutorial.udp.server;

import java.io.BufferedReader;
import java.lang.reflect.Array;
import java.net.*;

/**
 * Implements a UDP echo sqerver.
 */
public class UdpServer {

    public static void main(String argv[]) {
        DatagramSocket socket = null;
        int port = -1;
        byte[] buf = new byte[1024];
        DatagramPacket packetIn = null;
        DatagramPacket packetOut = null;

        if (argv.length != 1) {
            System.err.println("Format: es.udc.redes.tutorial.udp.server.UdpServer <port_number>");
            System.exit(-1);
        }
        try {
            port = Integer.parseInt(argv[0]);
            // Create a server socket
            socket = new DatagramSocket(port);
            // Set max. timeout to 300 secs
            socket.setSoTimeout(300000);
            while(true) {
                // Prepare datagram for reception
                packetIn = new DatagramPacket(buf, buf.length);
                // Receive the message
                socket.receive(packetIn);
                System.out.println("SERVER: Received "
                        + new String(packetIn.getData(), 0, packetIn.getLength())
                        + " from " + packetIn.getAddress().toString() + ":"
                        + packetIn.getPort());
                // Prepare datagram to send response
                packetOut = new DatagramPacket(buf, buf.length,
                        packetIn.getAddress(), packetIn.getPort());
                // Send response
                socket.send(packetOut);
                System.out.println("SERVER: Sending "
                        + new String(packetOut.getData()) + " to "
                        + packetOut.getAddress().toString() + ":"
                        + packetOut.getPort());
            }
        } catch (SocketTimeoutException e) {
            System.err.println("No requests received in 300 secs ");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Close the socket
            socket.close();
        }
    }
}
