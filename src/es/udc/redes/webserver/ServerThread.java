package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.util.Date;


public class ServerThread extends Thread
{
	private Socket socket;

	public ServerThread(Socket s)
	{
		// Store the socket s
		this.socket = s;
	}

	private void processHTTP(InputStream in, OutputStream out) throws Exception
	{
		final String ROOTPATH = "p1-files";
		final String DEFAULTFILE = "/index.html";
		final Boolean ALLOW = false;
		final String ROOTPACK = "es.udc.redes.webserver";
		final String[] METHODS = {"GET", "HEAD"};

		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		RequestParts reqParts = RequestParts.processRequest(
						input, ROOTPATH, DEFAULTFILE);
		HeadParts headParts = HeadParts.processHead(input);
		String dynReq;
		boolean valid = false;

		if(reqParts != null)
		{
			for(String m: METHODS) { valid |= m.equals(reqParts.method); }
			dynReq = processDynRequest(reqParts, ROOTPACK);
			out.write(printState(reqParts, valid).getBytes());
			out.write(printHeader(reqParts, valid).getBytes());
			if(reqParts.method.equals("GET"))
			{
				out.write(dynReq != null? dynReq.getBytes(): printBody(reqParts));
			}
		}
	}

	private String processDynRequest(RequestParts reqParts, String root) throws Exception
	{
		String className = reqParts.file, answer = null;

		if(className.endsWith(".do"))
		{
			className = root + '.' +
							className.substring(className.lastIndexOf('/')+1,
							                    className.lastIndexOf('.'));
			answer = ServerUtils.processDynRequest(className, reqParts.param);
		}
		return answer;
	}

	private String printState(RequestParts reqParts, boolean valid)
	{
		File file = new File(reqParts.file);

		return !valid?          "HTTP/1.0 400 Bad Request\n":
		       !file.exists()?  "HTTP/1.0 404 Not Found\n":
		       !file.canRead()? "HTTP/1.0 403 Forbidden\n":
		                        "HTTP/1.0 200 OK\n";
	}

	private String printHeader(RequestParts reqParts, boolean valid)
	{
		File file = new File(reqParts.file);
		Date fechaAct = new Date(System.currentTimeMillis()),
						fechaMod = new Date(file.lastModified());
		String type = switch(reqParts.file.substring(
						reqParts.file.lastIndexOf('.')+1))
		{
			case "html" -> "text/html";
			case "txt" -> "text/plain";
			case "png" -> "image/png";
			case "gif" -> "image/gif";
			default -> "application/octet-stream";
		};

		return "Date: " + fechaAct + "\n" +
		       "Server: WebServer_64Y\n" +
		       (valid && file.exists() && file.canRead()?
						"Last-Modified: " + fechaMod + "\n" +
						"Content-Length: " + file.length() + "\n" +
						"Content-Type: " + type + "\n\n": "\n");
	}

	private byte[] printBody(RequestParts reqParts)
	{
		try
		{
			InputStream file = new FileInputStream(reqParts.file);
			return file.readAllBytes();
		}
		catch(IOException e)
		{
			return "".getBytes();
		}
	}

	public void run()
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			// This code processes HTTP requests and generates
			// HTTP responses
			in = socket.getInputStream();
			out = socket.getOutputStream();

			processHTTP(in, out);

			in.close();
			out.close();
		}
		/*catch(SocketTimeoutException e)
		{
			System.err.println("Nothing received in 300 secs");
		}*/
		catch(Exception e)
		{
			System.err.println("Error: " + e.getMessage());
		}
		finally
		{
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
