package es.udc.redes.webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class WebServer
{
    public static void main(String[] args)
    {
        ServerSocket sSocket = null;
        int port = 1111;
        Socket cSocket = null;
        es.udc.redes.webserver.ServerThread thread = null;

        try
        {
            // Create a server socket
            sSocket = new ServerSocket(port);
            // Set a timeout of 300 secs
            sSocket.setSoTimeout(300000);

            while(true)
            {
                // Wait for connections
                cSocket = sSocket.accept();
                // Create a ServerThread object, with the new connection as parameter
                thread = new ServerThread(cSocket);
                // Initiate thread using the start() method
                thread.start();
            }
        }
        catch(SocketTimeoutException e)
        {
            System.err.println("Nothing received in 300 secs");
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
                sSocket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    
}
