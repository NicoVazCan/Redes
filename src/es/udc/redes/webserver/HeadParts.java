package es.udc.redes.webserver;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.Map;

/**This class stores all information about HTTP header entries.
 *
 * @author 64Y
 */
public class HeadParts
{
	Map<String, String> entries;

	/**Creates a new instance which contains all the entries.
	 *
	 * @param entries: a Map using the entry name to store it's info.
	 */
	HeadParts(Map<String, String> entries)
	{
		this.entries = entries;
	}

	/**Gives a HeadParts instance with all the entries splitted into a Map.
	 *
	 * @param in: a BufferedReader instance starting by the end of the request line.
	 * @return a instance with all the entries info located by it's name.
	 * @throws Exception when the BufferedReader fails reading the next line.
	 */
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
