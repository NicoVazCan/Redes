package es.udc.redes.webserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

public class WebServer
{
    public static Properties config;

    private static Properties initProperties(String file, boolean def) throws Exception
    {
        FileInputStream in;
        FileOutputStream out;
        Properties config = new Properties();

        if(def)
        {
            out = new FileOutputStream(file);

            config.setProperty("PORT", "1111");
            config.setProperty("DEFAULT_FILE", "/index.html");
            config.setProperty("BASE_DIRECTORY", "p1-files");
            config.setProperty("ALLOW", "false");

            config.store(out, "Default server configuration parameters:");
            out.close();
        }
        else
        {
            in = new FileInputStream(file);

            config.load(in);

            in.close();
        }

        return config;
    }

    public static void main(String[] args)
    {
        ServerSocket sSocket = null;
        int port;
        Socket cSocket = null;
        es.udc.redes.webserver.ServerThread thread = null;

        try
        {
            config = initProperties("cte/config", false);
            port = Integer.parseInt(config.getProperty("PORT", "1111"));
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
