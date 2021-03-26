package es.udc.redes.webserver;

import java.net.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**This class process, responds and stores client requests.
 *
 * @author 64Y
 */
public class ServerThread extends Thread
{
	private final Socket socket;
	private final Properties config;
	private final FileWriter access;
	private final FileWriter error;

	
	public ServerThread(Socket socket, Properties config, FileWriter access, FileWriter error)
	{
		// Store the socket s
		this.socket = socket;
		this.config = config;
		this.access = access;
		this.error = error;
	}

	/**The main method who process, responds and stores client request from the socket using
	 * another methods and classes.
	 * 1º, divides and stores all the information readed from the socket. .
	 * 2º, if the information received respect the HTTP protocol, checks if the requested file is a
	 * directory or a dynamic page, else does nothing.
	 * 3º, if the request method is valid and the file was modified then sends it to the client, else it
	 * sends an error page according to the fault.
	 * 4º, when any exception is catched, a internal error is sent to the client.
	 * 5º, finally if a error is sent, writes the information into the error log file, else into the access log.
	 *
	 * @throws Exception when the socket inputStream or outputStream fails
	 * because in that case, the server can't answer client.
	 */
	private void processHTTP() throws Exception
	{
		final String ROOTPATH = config.getProperty("BASE_DIRECTORY", "p1-files");
		final String DEFAULTFILE = config.getProperty("DEFAULT_FILE", "/index.html");
		final boolean ALLOW = Boolean.parseBoolean(config.getProperty("ALLOW", "false"));

		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		OutputStream out = socket.getOutputStream();
		StringBuilder log = new StringBuilder("NEW REQUEST:\n");
		String reqLine = in.readLine(), dynReq, state, head;
		RequestParts reqParts = RequestParts.processRequest(reqLine, ROOTPATH);
		HeadParts headParts = HeadParts.processHead(in);
		Date fechaAct = new Date(System.currentTimeMillis());
		boolean err = false;

		log.append("Request line: ").append(reqLine).append('\n');
		log.append("Client IP: ").append(socket.getInetAddress()).append('\n');
		log.append("Date: ").append(fechaAct).append('\n');

		try
		{
			if(reqParts != null)
			{
				reqParts = manageLsDir(reqParts, ALLOW, DEFAULTFILE, ROOTPATH);
				dynReq = manageDynPage(reqParts);

				if(dynReq != null)
				{
					out.write(dynReq.getBytes());
					log.append("State code: 200\n");
					log.append("Content-Length: ").append(dynReq.getBytes().length).append('\n');
				}
				else
				{
					state = printState(reqParts,headParts);
					out.write(state.getBytes());

					err = manageErrPage(reqParts) != reqParts;
					reqParts = manageErrPage(reqParts);

					head = printHeader(reqParts, headParts, fechaAct);
					out.write(head.getBytes());
					out.write(printBody(reqParts, headParts));

					if(!err)
					{
						log.append("State code: ").append(state, state.indexOf(' ')+1,
										state.indexOf(' ', state.indexOf(' ')+1)).append('\n');
						if(head.contains("Content-Length: "))
						{
							head = head.substring(head.indexOf("Content-Length: "));
							log.append(head, 0, head.indexOf('\n'));
						}
						else { log.append("Content-Length: 0\n"); }
					}
					else
					{
						log.append("Error mensage: ").append(state,
										state.indexOf(' ', state.indexOf(' ')+1)+1,
										state.length());
					}
				}
			}
		}
		catch(Exception e)
		{
			int end = log.indexOf("State code: ");
			out.write(("HTTP/1.0 500 Internal Server Error\n" +
			           "Date: " + new Date(System.currentTimeMillis()) + "\n" +
			           "Server: WebServer_64Y\n\n").getBytes());

			log.replace(end != -1? end: log.length(), log.length(),
							"Error mensage: Internal Server Error\n");
			err = true;
		}
		finally
		{
			log.append('\n');
			(err ? error: access).append(log);
			in.close();
			out.close();
		}
	}

	/**Checks if the request method received is valid and implemented.
	 *
	 * @param reqParts: a RequestParts instance with the method asked.
	 * @return true if the method is valid and implemented, false otherwise.
	 */
	private boolean isValid(RequestParts reqParts)
	{
		final String[] METHODS = {"GET", "HEAD"};

		boolean valid = false;

		for(String m: METHODS) { valid |= m.equals(reqParts.method); }

		return valid;
	}

	/**Checks if the requested file was modified since the last ask from the client.
	 *
	 * @param reqParts: a RequestParts instance with the file.
	 * @param headParts: a HeadParts instance with the date of the file last version known by the client.
	 * @return true if the server has a newer version of the asked file, false otherwise.
	 */
	private boolean isModif(RequestParts reqParts, HeadParts headParts)
	{
		final SimpleDateFormat toDate =
						new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy",
										new Locale("en_US"));

		long fechaIMS, fechaMod;

		try
		{
			fechaIMS = toDate.parse(headParts.entries.get("If-Modified-Since")).getTime();
			fechaMod = reqParts.file.lastModified();

			return fechaMod - fechaIMS >= 1000; //Los microsegundos son irrelevantes.
		}
		catch(Exception ignored) { return true; }
	}

	/**Depending of the reqParts parameter file, this method doesn't modify the reqParts and returns
	 * a RequestParts instance with a file which can be:
	 *  The default file, if the requested file is a directory and contains it.
	 *  A dynamic page showing all the directory files, if the file is a directory,
	 *  default file doesn't exist on it, and the server is allowed to send this.
	 * If all previous conditions are false, returns the same instance of the reqParts parameter.
	 *
	 * @param reqParts: a RequestParts instance with the request line parts.
	 * @param allow: a boolean which represents if the server can list directories content to client.
	 * @param defFile: the default file name to open when the client ask for a directory.
	 * @param rootPath: the server base directory containing all the files that can be requested.
	 * @return a RequestParts instance acording to the above conditions.
	 */
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

				aux = new RequestParts("GET", new File("LsServlet.do"), "HTTP/1.0", param);
			}
		}

		return aux;
	}

	/**Depending of the reqParts parameter file, this method doesn't modify the reqParts and returns
	 * a RequestParts instance with a file which can be:
	 *  The error400.html error file, if the reqParts method is not valid.
	 *  The error404.html error file, if the reqParts file is not found.
	 *  The error404.html error file, if the reqParts file is a directory or the server has not
	 *  permission to open it.
	 * If all previous conditions are false, returns the same instance of the reqParts parameter.
	 *
	 * @param reqParts: a RequestParts instance with the request line parts.
	 * @return a RequestParts instance acording to the above conditions.
	 */
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

	/**If the requested file type is 'do', this method write the state line, header,
	 * and body correspondent to the dynamic page asked. For that, this method calls dynamically
	 * another doGet method from the class with the same name of the requested file, which returns
	 * a string of html code written on the body.
	 *
	 * @param reqParts: a RequestParts instance with the request line parts.
	 * @return a string containing the dynamic page state line, header, and body.
	 * @throws Exception if the requested file name doesn't match with any class name
	 * that imiplements MiniServlet interface.
	 */
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

	/**This method gives a error state line if the requested method is not valid,
	 * the file doesn't exits, can't be opened or it's a directory, or a ok state line
	 * if the file was modified since the last time the client asked for it, else a
	 * not modified state line.
	 *
	 * @param reqParts: a RequestParts instance with the request line parts.
	 * @param headParts: a HeadParts instance with the request header entries.
	 * @return a string with the state line depending of the client request.
	 */
	private String printState(RequestParts reqParts, HeadParts headParts)
	{
		return !isValid(reqParts)?                           "HTTP/1.0 400 Bad Request\n":
					 !isModif(reqParts, headParts)?                "HTTP/1.0 304 Not Modified\n":
		       !reqParts.file.exists()?                      "HTTP/1.0 404 Not Found\n":
		       reqParts.file.isDirectory() || !reqParts.file.canRead()?
		                                                     "HTTP/1.0 403 Forbidden\n":
		                                                     "HTTP/1.0 200 OK\n";
	}

	/**This method gives the next header entries:
	 *  Date: the date and hour when the server received the client request.
	 *  Server: the server's name which is 'WebServer_64Y' by default.
	 *  Last-Modified: the date and hour when the requested file was modified.
	 *  Content-Length: the requested file size on bytes.
	 *  Content-Type: the MIME type acording to the requested file type.
	 * This three last entries doesn't show if the request method is not valid,
	 * the file wasn't modified since the last time the client asked for it,
	 * doesn't exits or can't be opened or it's a directory.
	 *
	 * @param reqParts: a RequestParts instance with the request line parts.
	 * @param headParts: a HeadParts instance with the request header entries.
	 * @param fechaAct: the date and hour when the server received the client request.
	 * @return a string with the header entries depending of the client request.
	 */
	private String printHeader(RequestParts reqParts, HeadParts headParts, Date fechaAct)
	{
		final String serverName = "WebServer_64Y";

		Date fechaMod = new Date(reqParts.file.lastModified());
		boolean valid = isValid(reqParts), modif = isModif(reqParts, headParts);
		String type = switch(reqParts.file.getName().substring(
						reqParts.file.getName().lastIndexOf('.')+1))
		{
			case "html" -> "text/html";
			case "txt" -> "text/plain";
			case "png" -> "image/png";
			case "gif" -> "image/gif";
			case "ico" -> "image/vnd.microsoft.icon";
			default -> "application/octet-stream";
		};

		return "Date: " + fechaAct + "\n" +
		       "Server: " + serverName + "\n" +
		       (modif && valid && reqParts.file.exists() &&
						       reqParts.file.canRead() && !reqParts.file.isDirectory()?
						"Last-Modified: " + fechaMod + "\n" +
						"Content-Length: " + reqParts.file.length() + "\n" +
						"Content-Type: " + type + "\n\n": "\n");
	}

	/**This method gives the file body as a byte array if the requested method is GET,
	 * the file wasn't modified since the last time the client asked for it,
	 * doesn't exits or can't be opened or it's a directory.
	 *
	 * @param reqParts: a RequestParts instance with the request line parts.
	 * @param headParts: a HeadParts instance with the request header entries.
	 * @return a byte array cantaining the requested file.
	 * @throws Exception if the requested file can't be readed.
	 */
	private byte[] printBody(RequestParts reqParts, HeadParts headParts) throws Exception
	{
		if(reqParts.method.equals("GET") && isModif(reqParts, headParts)
						&& reqParts.file.canRead() && !reqParts.file.isDirectory())
		{
			InputStream file = new FileInputStream(reqParts.file);
			return file.readAllBytes();
		}
		return "".getBytes();
	}

	/**This method waits for client comunication 300 millisenconds, then close the socket.
	 * If client comunicates before that, it process the request and store it on the log.
	 */
	public void run()
	{
		try
		{
			socket.setSoTimeout(300000);
			processHTTP();
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
				access.flush();
				error.flush();
				socket.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
