package es.udc.redes.webserver;

import java.io.File;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class LsServlet implements MiniServlet
{
	public LsServlet()
	{

	}

	public String doGet(Map<String, String> parameters) throws Exception
	{
		return printHeader() + printBody(lsDir(parameters.get("dir"),
						parameters.get("root"))) + printEnd();
	}

	private String lsDir(String dir, String root) throws Exception
	{
		File file;
		StringBuilder content = new StringBuilder();
		String rootDir = dir.replaceAll(root, "");

		try(DirectoryStream<Path> stream = Files.newDirectoryStream(Path.of(dir)))
		{
			for(Path path: stream)
			{
				file = path.toFile();
				content.append("<a href=\"").
								append(rootDir).
								append('/')
								.append(file.getName())
								.append("\">")
								.append(file.getName())
								.append("</a>")
								.append("<h1>\n</h1>");
			}
		}

		return content.toString();
	}

	private String printHeader() {
		return "<html><head> <title>Directory's content</title> </head> ";
	}

	private String printBody(String content) {
		return "<body>" + content + "</body>";
	}

	private String printEnd() {
		return "</html>";
	}

}
