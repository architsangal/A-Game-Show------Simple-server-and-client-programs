import java.net.*;
import java.io.*;
import java.util.*;
public class Buzzer extends Thread
{
    public String question;
    public String answer;
    public Socket socket;
    public String name;
    public DataOutputStream out;
    public DataInputStream in;
    public boolean buzzerUP;
    public String claimedAnswer;
    public double score;
    public Buzzer serverThreads[];
    public int index;

    Buzzer(Socket socket,int index)throws IOException
    {
        this.socket = socket;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
        this.buzzerUP = false;
        this.score = 0;
        this.index = index;
    }
    
    @Override
    public void run()
    {   
        try
        {   // 1 for 1 more question
            out.writeUTF("1");
            String scores = "\n\n";
            for(int i=0;i<3;i++)
            {
                scores = scores + serverThreads[i].name + " : " + serverThreads[i].score + "\n";
            }
            // passing score and questions respectively
            out.writeUTF(scores);
            out.writeUTF(question);

            PressBuzzer();
        }
        catch (Exception e)
        {
            System.out.println("Run " + e.toString());
        }
    }
    void PressBuzzer()
    {
        try
        {
            // allows a loop for 10 sec or till buzzer is not pressed
            long startBuzzer = System.currentTimeMillis();
            while((System.currentTimeMillis() - startBuzzer < 10000)&&(!buzzerUP))
            {
                // logic similar to client code
                //waits for input from client side
                if (socket.getInputStream().available() > 0)
                {
                    // flag is used to keep a track if expected answer is passed to server..
                    int flag=0;
                    //input of buzzer
                    String input = in.readUTF();
                    if(input.equals("b")||input.equals("B"))
                    {
                        for(int i=0;i<serverThreads.length;i++)
                        {
                            // informing other players that the buzzer is pressed by some player..
                            serverThreads[i].buzzerUP = true;
                            if(i != index)
                                serverThreads[i].out.writeUTF("Buzzer is UP... by " + this.name);
                            else
                                serverThreads[i].out.writeUTF("Buzzer is UP... by YOU\n You have 10 sec to answer.");//\nTime Left: 10
                        }

                        // giving 10sec to player to answer the question correctly
                        long start = System.currentTimeMillis();
                        while (System.currentTimeMillis() - start < 10000)
                        {
                            if (socket.getInputStream().available() > 0)
                            {
                                boolean exit = false;
                                // Expected answer recieved
                                claimedAnswer = in.readUTF();
                                String message = "";

                                // value of score,flag and exit is changed..
                                if(claimedAnswer.equals(answer))
                                {
                                    flag=1;
                                    exit = true;
                                    score++;
                                    message = "Correct Answer";
                                }
                                else
                                {
                                    flag=1;
                                    exit = true;
                                    score = score - 0.5;
                                    message = "Wrong !!!";
                                }
                                // result is passed back to client
                                out.writeUTF(message);

                                //claimed is made "" back
                                if(exit)
                                {
                                    claimedAnswer = "";
                                    break;
                                }
                            }
                        }

                        // if buzzer is pressed and user didn't answer
                        if(flag == 0)
                        {
                            score = score - 0.5;
                        }
                    }
                }
            }

            // every thread's state is changed to wait at the end
            // so that each thread can come same pace
            toWait();
        }
        catch (Exception e)
        {
            System.out.println("Q " + e.toString());
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