package es.udc.redes.webserver;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

public class HeadParts
{
	Map<String, String> entries;

	HeadParts(Map<String, String> entries)
	{
		this.entries = entries;
	}

	public static HeadParts processHead(BufferedReader in) throws Exception
	{
		String line;
		String[] nameAndVal;
		Map<String, String> entries = new HashMap<>();

		while((line = in.readLine()) != null && !(line).isEmpty())
		{
			nameAndVal = line.split(": ");
			entries.put(nameAndVal[0], nameAndVal[1]);
		}
		return new HeadParts(entries);
	}
}
