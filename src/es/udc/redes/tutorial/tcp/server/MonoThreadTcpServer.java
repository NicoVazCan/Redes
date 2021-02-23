package es.udc.redes.tutorial.tcp.server;

import java.net.*;
import java.io.*;

/**
 * MonoThread TCP echo server.
 */
public class MonoThreadTcpServer
{

    public static void main(String argv[])
    {
        ServerSocket sSocket = null;
        int port = -1;
        Socket cSocket = null;
        String msg = null;

        if(argv.length != 1)
        {
            System.err.println("Format: es.udc.redes.tutorial.tcp.server.MonoThreadTcpServer <port>");
            System.exit(-1);
        }
        try
        {
            // Create a server socket
            port = Integer.parseInt(argv[0]);
            sSocket = new ServerSocket(port);
            // Set a timeout of 300 secs
            sSocket.setSoTimeout(300000);

            while(true)
            {
                // Wait for connections
                cSocket = sSocket.accept();
                // Set the input channel
                BufferedReader input = new BufferedReader(new InputStreamReader(
                        cSocket.getInputStream()));
                // Set the output channel
                PrintWriter output = new PrintWriter(cSocket.getOutputStream(), true);
                // Receive the client message
                msg = input.readLine();
                System.out.println("SERVER: Received " + msg + " from " +
                        cSocket.getLocalAddress() + ":" + cSocket.getPort());
                // Send response to the client
                output.println(msg);
                System.out.println("SERVER: Sending " + msg + " to " +
                        cSocket.getLocalAddress() + ":" + cSocket.getPort());
                // Close the streams
                output.close();
                input.close();
            }
        }
        catch (SocketTimeoutException e)
        {
                System.err.println("Nothing received in 300 secs ");
        }
        catch(Exception e)
        {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
        finally
        {
            //Close the socket
            try
            {
                cSocket.close();
                sSocket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
