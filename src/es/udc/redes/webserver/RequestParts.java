package es.udc.redes.webserver;

import java.io.BufferedReader;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RequestParts
{
	final String method, httpV;
	final File file;
	final Map<String, String> param;

	RequestParts(String method, File file, String httpV, Map<String, String> param)
	{
		this.method = method;
		this.file = file;
		this.httpV = httpV;
		this.param = param;
	}

	public static RequestParts processRequest(BufferedReader in, String root) throws Exception
	{
		RequestParts content = null;
		HashMap<String, String> param;
		String[] petParts, urlParam, pNameVal;
		String fileName, request = in.readLine();
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
