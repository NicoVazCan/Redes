package es.udc.redes.tutorial.copy;

import java.io.*;

public class Copy
{
    public static void main(String[] args) throws IOException
    {
        BufferedReader readerStream = null;
        BufferedWriter writerStream = null;
        BufferedInputStream inputStream = null;
        BufferedOutputStream outputStream = null;
        int c;

        if(args.length == 2)
        {
            if(args[0].endsWith(".txt") && args[1].endsWith(".txt"))
            {
                try
                {
                    readerStream = new BufferedReader(new FileReader(args[0]));
                    writerStream = new BufferedWriter(new FileWriter(args[1]));

                    while((c = readerStream.read()) != -1)
                    {
                        writerStream.write(c);
                    }
                }
                catch(FileNotFoundException e)
                {
                    System.err.println("The first file must exist");
                }
                finally
                {
                    if(readerStream != null)
                    {
                        readerStream.close();
                    }
                    if(writerStream != null)
                    {
                        writerStream.close();
                    }
                }
            }
            else
            {
                try
                {
                    inputStream = new BufferedInputStream(new FileInputStream(args[0]));
                    outputStream = new  BufferedOutputStream(new FileOutputStream(args[1]));

                    while((c = inputStream.read()) != -1)
                    {
                        outputStream.write(c);
                    }
                }
                catch(FileNotFoundException e)
                {
                    System.err.println("The first file must exist");
                }
                finally
                {
                    if(inputStream != null)
                    {
                        inputStream.close();
                    }
                    if(outputStream != null)
                    {
                        outputStream.close();
                    }
                }
            }
        }
        else
        {
            System.err.println("Format: es.udc.redes.tutorial.copy.Copy <fichero origen> <fichero destino>");
        }
    }
}
