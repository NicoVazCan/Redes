package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class ServerThread extends Thread
{
	private Socket socket;
	private Properties config = WebServer.config;

	public ServerThread(Socket s)
	{
		// Store the socket s
		this.socket = s;
	}

	private void processHTTP(InputStream in, OutputStream out) throws Exception
	{
		final String ROOTPATH = config.getProperty("BASE_DIRECTORY", "p1-files");
		final String DEFAULTFILE = config.getProperty("DEFAULT_FILE", "/index.html");
		final boolean ALLOW = Boolean.parseBoolean(config.getProperty("ALLOW", "false"));

		try
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(in));
			RequestParts reqParts = RequestParts.processRequest(input, ROOTPATH);
			HeadParts headParts = HeadParts.processHead(input);
			String dynReq;

			if(reqParts != null)
			{
				reqParts = manageLsDir(reqParts, ALLOW, DEFAULTFILE, ROOTPATH);
				dynReq = manageDynPage(reqParts);

				if(dynReq != null)
				{
					out.write(dynReq.getBytes());
				}
				else
				{
					out.write(printState(reqParts,headParts).getBytes());
					reqParts = manageErrPage(reqParts);
					out.write(printHeader(reqParts, headParts).getBytes());
					out.write(printBody(reqParts, headParts));
				}
			}
		}
		catch(Exception ignore)
		{
			out.write(("HTTP/1.0 500 Internal Server Error\n" +
			           "Date: " + new Date(System.currentTimeMillis()) + "\n" +
			           "Server: WebServer_64Y\n\n").getBytes());
		}
	}

	private boolean isValid(RequestParts reqParts)
	{
		final String[] METHODS = {"GET", "HEAD"};

		boolean valid = false;

		for(String m: METHODS) { valid |= m.equals(reqParts.method); }

		return valid;
	}

	private boolean isModif(RequestParts reqParts, HeadParts headParts)
	{
		final SimpleDateFormat toDate =
						new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
										new Locale("en_US"));

		String ifModDate = headParts.entries.get("If-Modified-Since");

		try
		{
			return (ifModDate == null || !reqParts.file.exists() || !reqParts.file.canRead() ||
							reqParts.file.isDirectory() ||
							(new Date(reqParts.file.lastModified())).after(toDate.parse(ifModDate)));
		}
		catch(Exception ignored) { return true; }
	}

	private RequestParts manageLsDir(RequestParts reqParts, boolean allow,
	                                 String defFile, String rootPath)
	{
		RequestParts aux = reqParts;
		Map<String, String> param;
		File index;

		if(reqParts.file.isDirectory())
		{
			index = new File(reqParts.file.toPath() + defFile);

			if(index.exists())
			{
				aux = new RequestParts(reqParts.method, index, reqParts.httpV, reqParts.param);
			}
			else if(allow)
			{
				param = new HashMap<>();

				param.put("dir", reqParts.file.getPath().replaceFirst(defFile, ""));
				param.put("root", rootPath);

				aux = new RequestParts("GET", new File("LsServlet.do"),
								"HTTP/1.0", param);
			}
		}

		return aux;
	}

	private RequestParts manageErrPage(RequestParts reqParts)
	{
		final String[] ERROR = {"/error400.html", "/error404.html", "/error403.html"};
		final String ERRPATH = "p1-files/error";
		int i = -1;

		if(!isValid(reqParts))
		{ i = 0; }
		else if(!reqParts.file.exists())
		{ i = 1; }
		else if(reqParts.file.isDirectory() || !reqParts.file.canRead())
		{ i = 2; }

		return i != -1? new RequestParts("GET",
					new File(ERRPATH + ERROR[i]), "HTTP/1.0", null):
						reqParts;
	}

	private String manageDynPage(RequestParts reqParts) throws Exception
	{
		final String ROOTPACK = "es.udc.redes.webserver";

		Date fechaAct = new Date(System.currentTimeMillis());
		String className = reqParts.file.getName(), state, head, body, answer = null;
		boolean valid = isValid(reqParts);

		if(className.endsWith(".do"))
		{
			className = ROOTPACK + '.' +
							className.substring(0, className.lastIndexOf('.'));

			state = !valid? "HTTP/1.0 400 Bad Request\n":
											"HTTP/1.0 200 OK\n";
			body = ServerUtils.processDynRequest(className, reqParts.param);
			head = "Date: " + fechaAct + "\n" +
							"Server: WebServer_64Y\n" +
							(valid?
							"Content-Length: " + body.getBytes().length + "\n" +
							"Content-Type: text/html\n\n": "\n");

			answer = state + head;
			if(reqParts.method.equals("GET") && valid) { answer += body; }
		}

		return answer;
	}
	
		private String printState(RequestParts reqParts, HeadParts headParts)
	{
		return !isValid(reqParts)?                           "HTTP/1.0 400 Bad Request\n":
					 !isModif(reqParts, headParts)?                "HTTP/1.0 304 Not Modified\n":
		       !reqParts.file.exists()?                      "HTTP/1.0 404 Not Found\n":
		       reqParts.file.isDirectory() ||
						       !reqParts.file.canRead()?             "HTTP/1.0 403 Forbidden\n":
		                                                     "HTTP/1.0 200 OK\n";
	}

	private String printHeader(RequestParts reqParts, HeadParts headParts)
	{
		Date fechaAct = new Date(System.currentTimeMillis()),
						fechaMod = new Date(reqParts.file.lastModified());
		boolean valid = isValid(reqParts), modif = isModif(reqParts, headParts);
		String type = switch(reqParts.file.getName().substring(
						reqParts.file.getName().lastIndexOf('.')+1))
		{
			case "html" -> "text/html";
			case "txt" -> "text/plain";
			case "png" -> "image/png";
			case "gif" -> "image/gif";
			default -> "application/octet-stream";
		};

		return "Date: " + fechaAct + "\n" +
		       "Server: WebServer_64Y\n" +
		       (modif && valid && reqParts.file.exists() &&
						       reqParts.file.canRead() && !reqParts.file.isDirectory()?
						"Last-Modified: " + fechaMod + "\n" +
						"Content-Length: " + reqParts.file.length() + "\n" +
						"Content-Type: " + type + "\n\n": "\n");
	}

	private byte[] printBody(RequestParts reqParts, HeadParts headParts) throws Exception
	{
		if(reqParts.method.equals("GET") && isValid(reqParts) && isModif(reqParts, headParts)
						&& !reqParts.file.isDirectory())
		{
			InputStream file = new FileInputStream(reqParts.file);
			return file.readAllBytes();
		}
		return "".getBytes();
	}

	public void run()
	{
		InputStream in = null;
		OutputStream out = null;
		try
		{
			// This code processes HTTP requests and generates
			// HTTP responses
			socket.setSoTimeout(300000);

			in = socket.getInputStream();
			out = socket.getOutputStream();

			processHTTP(in, out);

			in.close();
			out.close();
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
