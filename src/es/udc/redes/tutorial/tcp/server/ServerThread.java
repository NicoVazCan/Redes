package es.udc.redes.tutorial.tcp.server;
import java.net.*;
import java.io.*;

/** Thread that processes an echo server connection. */

public class ServerThread extends Thread
{

  private Socket socket;

  public ServerThread(Socket s)
  {
    // Store the socket s
    socket = s;
  }

  @Override
  public void run()
  {
    String msg = null;
    try
    {
      // Set the input channel
      BufferedReader input = new BufferedReader(new InputStreamReader(
              socket.getInputStream()));
      // Set the output channel
      PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
      // Receive the client message
      msg = input.readLine();
      System.out.println("SERVER: Received " + msg + " from " +
              socket.getLocalAddress() + ":" + socket.getPort());
      // Send response to the client
      output.println();
      System.out.println("SERVER: Sending " + msg +
              socket.getLocalAddress() + ":" + socket.getPort());
      // Close the streams
      output.close();
      input.close();
    }
    catch(SocketTimeoutException e)
    {
      System.err.println("Nothing received in 300 secs");
    }
    catch(Exception e)
    {
      System.err.println("Error: " + e.getMessage());
    }
    finally
    {
      // Close the socket
      try
      {
        socket.close();
      }
      catch(IOException e)
      {
        e.printStackTrace();
      }
    }
  }
}
