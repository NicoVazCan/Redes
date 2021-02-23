package es.udc.redes.tutorial.tcp.server;
import java.io.IOException;
import java.net.*;

/** Multithread TCP echo server. */

public class TcpServer
{

  public static void main(String argv[])
  {
    ServerSocket sSocket = null;
    int port = -1;
    Socket cSocket = null;
    ServerThread thread = null;

    if(argv.length != 1)
    {
      System.err.println("Format: es.udc.redes.tutorial.tcp.server.TcpServer <port>");
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
