/*
    author :- Archit Sangal(IMT2019012)
*/
import java.net.*;
import java.io.*;
import java.util.*;

public class ServerThread extends Thread
{
    public Socket socket;
    public String name;
    public DataOutputStream out;
    public DataInputStream in;

    ServerThread(Socket socket)throws IOException
    {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }
    
    // using threads asking for names and then setting threads to wait so that all threads are at same pace.
    @Override
    public void run()
    {   
        try
        {
            this.name = in.readUTF();
            toWait();
        }
        catch (Exception e)
        {
            System.out.println("Run " + e.toString());
        }
    }
    public void toNotify()
    {
        synchronized(this)
        {
            notify();
        }
    }
    public void toWait()
    {
        try
        {
            synchronized(this)
            {
                wait();
            }
        }
        catch (Exception e)
        {
            System.out.println("Run " + e.toString());
        }
    }
    public void closeIO()
    {
        try
        {
            in.close();
            out.close();
        }
        catch (Exception e)
        {
            System.out.println("close " + e.toString());
        }
    }    
}