package es.udc.redes.webserver;

import java.util.Map;

/**
 * This class must be filled to complete servlets option (.do requests).
 */
public class YourServlet implements MiniServlet
{
	public YourServlet()
	{

	}

	/**Makes a operation
	 *
	 * @param parameters: a Map<String, String> with '1number' as the first argument
	 *  and '2number' as the second argument of the 'operator' operation given.
	 * @return a string containing the operation result
	 */
	@Override
	public String doGet (Map<String, String> parameters){
		double num1 = Float.parseFloat(parameters.get("1number")),
						num2 = Float.parseFloat(parameters.get("2number")),
						result = switch(parameters.get("operator"))
										{
											case "sum" -> num1 + num2;
											case "sub" -> num1 - num2;
											case "mul" -> num1 * num2;
											case "div" -> num1 / num2;
											case "pow" -> Math.pow(num1,num2);
											case "log" -> Math.log(num2)/Math.log(num1);
											default -> 0;
										};

		return printHeader() + printBody(String.valueOf(result)) + printEnd();
	}

	private String printHeader() {
		return "<html><head> <title>The result:</title> </head> ";
	}

	private String printBody(String result) {
		return "<body> <h1> Result: " + result + "</h1></body>";
	}

	private String printEnd() {
		return "</html>";
	}
}
