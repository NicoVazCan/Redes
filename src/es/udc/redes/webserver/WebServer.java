package es.udc.redes.webserver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Properties;

/**This class runs the main thread which accepts client connections.
 *
 * @author 64Y
 */
public class WebServer
{
    public static Properties config;
    public static FileWriter access, error;

    /**Loads server properties from the given file.
     *
     * @param file: The file which contains the server properties.
     * @param def: If it's true, the file is filled with default properties.
     * @return a instance of the class Properties with the server properties from the file.
     * @throws Exception if the file doesn't exist or you don't have the privileges to open and write it.
     */
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
    /**Opens the access and error log files, accepts client connections and creates a new thread
     * to attend their request with the socket to listen and write the client, the server properties,
     * and the log file to write the request historic.
     */
    public static void main(String[] args)
    {
        ServerSocket sSocket = null;
        int port;
        Socket cSocket = null;
        es.udc.redes.webserver.ServerThread thread = null;

        try
        {
            config = initProperties("cte/config", false);
            access = new FileWriter("p1-files/log/access.log", true);
            error = new FileWriter("p1-files/log/error.log", true);
            port = Integer.parseInt(config.getProperty("PORT", "1111"));

            sSocket = new ServerSocket(port);

            sSocket.setSoTimeout(300000);

            while(true)
            {
                cSocket = sSocket.accept();

                thread = new ServerThread(cSocket, config, access, error);

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
                access.close();
                error.close();
                sSocket.close();
            }
            catch(IOException e)
            {
                e.printStackTrace();
            }
        }
    }
}
