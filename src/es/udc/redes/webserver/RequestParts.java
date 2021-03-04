package es.udc.redes.webserver;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class RequestParts
{
	final String method, file, httpV;
	final Map<String, String> param;

	private RequestParts(String method, String file, String httpV, Map<String, String> param)
	{
		this.method = method;
		this.file = file;
		this.httpV = httpV;
		this.param = param;
	}

	public static RequestParts processRequest(BufferedReader in, String root, String defFile) throws Exception
	{
		RequestParts content = null;
		HashMap<String, String> param;
		String[] petParts, urlParam, pNameVal;
		String file, request = in.readLine();
		int paramPos;

		if(request != null  && (petParts = request.split(" ")).length == 3)
		{
			param = new HashMap<>();
			paramPos =  petParts[1].indexOf('?');
			file = petParts[1].equals("/")? defFile:
							paramPos == -1? petParts[1]: petParts[1].substring(0,paramPos);
			if(petParts[1].contains("&"))
			{
				urlParam = petParts[1].substring(paramPos+1).split("&");

				for(String p: urlParam)
				{
					pNameVal = p.split("=");
					param.put(pNameVal[0], pNameVal[1]);
				}
			}
			content = new RequestParts(petParts[0], root + file, petParts[2], param);
		}
		return content;
	}
}
