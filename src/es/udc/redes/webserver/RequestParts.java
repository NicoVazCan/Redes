package es.udc.redes.webserver;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**This class stores all information about request line.
 *
 * @author 64Y
 */
public class RequestParts
{
	final String method, httpV;
	final File file;
	final Map<String, String> param;

	/**Creates a new instance which contains the parameters.
	 *
	 * @param method: The request method.
	 * @param file: The requested file.
	 * @param httpV: The request protocol version.
	 * @param param: The URL parameters.
	 */
	RequestParts(String method, File file, String httpV, Map<String, String> param)
	{
		this.method = method;
		this.file = file;
		this.httpV = httpV;
		this.param = param;
	}

	/**Gives a RequestParts instance containing the method, file, protocol, and url
	 * parameters from the request line.
	 *
	 * @param request: The request line.
	 * @param root: The base directory from the server.
	 * @return a new instance which contains each part of the line.
	 */
	public static RequestParts processRequest(String request, String root)
	{
		RequestParts content = null;
		HashMap<String, String> param;
		String[] petParts, urlParam, pNameVal;
		String fileName;
		int paramPos;
		File file;

		if(request != null  && (petParts = request.split(" ")).length == 3)
		{
			param = new HashMap<>();
			paramPos =  petParts[1].indexOf('?');
			fileName = root + (paramPos == -1? petParts[1]: petParts[1].substring(0,paramPos));
			file = new File(fileName);

			if(petParts[1].contains("&"))
			{
				urlParam = petParts[1].substring(paramPos+1).split("&");

				for(String p: urlParam)
				{
					pNameVal = p.split("=");
					param.put(pNameVal[0], pNameVal[1]);
				}
			}
			content = new RequestParts(petParts[0], file, petParts[2], param);
		}
		return content;
	}
}
