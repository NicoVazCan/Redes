package es.udc.redes.tutorial.copy;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

public class Copy
{
    //java es.udc.redes.tutorial.copy.Copy <fichero origen> <fichero destino>
    public static void main(String[] args) throws IOException
    {
        if(args.length == 2)
        {
            if(args[0].endsWith(".txt") && args[1].endsWith(".txt"))
            {
                BufferedReader inputStream = null;
                BufferedWriter outputStream = null;
                int c;

                try
                {
                    inputStream = new BufferedReader(new FileReader(args[0]));
                    outputStream = new BufferedWriter(new FileWriter(args[1]));

                    while((c = inputStream.read()) != -1)
                    {
                        outputStream.write(c);
                    }
                } finally
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
            else
            {
                BufferedInputStream inputStream = null;
                BufferedOutputStream outputStream = null;
                int c;

                try
                {
                    inputStream = new BufferedInputStream(new FileInputStream(args[0]));
                    outputStream = new  BufferedOutputStream(new FileOutputStream(args[1]));

                    while((c = inputStream.read()) != -1)
                    {
                        outputStream.write(c);
                    }
                } finally
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
    }
}
