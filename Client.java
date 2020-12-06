/*
    author: -
    Archit Sangal
    IMT2019012
*/
import java.net.*;  
import java.io.*;  
import java.util.*;
class Client
{  
    public static void main(String args[])throws IOException
    {
        // address and port number
        Socket socket=new Socket("127.0.0.1",20006);

        // Informs the player that the Client is connected to 
        System.out.println("Connected to server...");
        

        // An incoming stream for recieving data   
        DataInputStream in=new DataInputStream(socket.getInputStream());

        //An outgoing stream for recieving data   
        DataOutputStream out=new DataOutputStream(socket.getOutputStream());

        // Making an object of Scanner Class for taking inputs from Players...
        Scanner sc = new Scanner(System.in);
        
        // Used as a temp variable to take inputs from the server and displaying them on player's terminal
        String output;
        output = in.readUTF(); //taking input from server
        int index = Integer.parseInt(output);
        
        /*
            index is a special number or a rank assigned to a client according to the joining...
            first one to join ==> index = 0
            and last one to join ==> index = 2
            As this code is for 3 players...
        */

        //if index is 2 then player is last one join so he will not get this message as he need not wait 
        //Message: "Player is connected.... Please Wait..""
        if(index < 2)
        {
            output = in.readUTF();
            System.out.println(output);
        }

        //Every player will get this message...
        //Players are now connected.... GAME SHOW IS Live..
        output = in.readUTF();
        System.out.println(output);
        
        // Enter You name and it is passing to server
        System.out.println("Hello  Player !\n Enter Your Name...");
        // If some previous unwanted input is there then it will erase it.
        System.in.read(new byte[System.in.available()]);
        // After earsing and clearing the buffer of input stream it takes name of the player..
        String name = sc.nextLine();
        //passing the name to server
        out.writeUTF(name);
        System.out.println("Hello " + name); 
        
        boolean stop = false;
        //1 for question ; 0 for Game Show is over..
        //for the first time it has to be 1 but we still accept it as server is sending it...
        output = in.readUTF();

        //reading and displaying the scores which here is zero for all players...
        output = in.readUTF();
        System.out.println(output);
        
        //reading and displaying the question and prompting to press the buzzer...
        output = in.readUTF();
        System.out.println(output);
        System.out.println("Enter b or B for Buzzer... You have 10 seconds to press the buzzers.");
        
        /*
            stop is false when we recieve a 1 which is an indication that we have
            atleast 1 more question to present to the player stop is set to true 
            then we find our winner of "THE GAME SHOM" and we are sure that we 
            don't have to present any more questions.....
        */

        while(!stop)
        {
            // starts counting 10 sec..
            long startBuzzer = System.currentTimeMillis();
            int f = 0;
            boolean buzzerUP = false;
            // clears the buffer for taking input..
            System.in.read(new byte[System.in.available()]);
            // this loop gives player 10 sec to press buzzer
            // it may terminate early if some other user presses the buzzer first...
            while((System.currentTimeMillis() - startBuzzer < 10000)&&(!buzzerUP))
            {
                // if there is something for input if condition is executed
                if(System.in.available() > 0)
                {
                    /*
                        input b or B for Buzzer... any other input will be taken as
                        a error from user side and player will be given a warning
                    */

                    String buz = sc.nextLine();
                    if(buz.equals("b")||buz.equals("B"))
                    {
                        buzzerUP =true;

                        //server is immediately informed that buzzer is pressed..
                        out.writeUTF(buz);

                        // Buzzer is pressed by you
                        System.out.println(in.readUTF());
                        System.out.println("Enter an option");

                        // 10 sec are given to answer the question...
                        long start = System.currentTimeMillis(); // and time starts now ...

                        // any previously entered garbage is thrown out...
                        System.in.read(new byte[System.in.available()]);

                        while(System.currentTimeMillis()-start < 10000)
                        {
                            if(System.in.available() > 0)
                            {
                                // accepts answer from user in both upper and lower cases
                                /*
                                    any other answer other than A,B,C,D,a,b,c,d will not be 
                                    accepted and he will be asked to enter it again.
                                */
                                String answerExpected = sc.nextLine();
                                answerExpected = answerExpected.toLowerCase();
                                if(answerExpected.equals("a")||answerExpected.equals("b")
                                    ||answerExpected.equals("c")||answerExpected.equals("d"))
                                {
                                    //sends the answers to server for checking...
                                    out.writeUTF(answerExpected);
                                    System.out.println(in.readUTF());
                                    System.out.println("\n");//formatting
                                    // f=1 indicates that question was not skipped
                                    f=1;
                                    //break the loop as our work is now completed...
                                    break;
                                }
                                else
                                {
                                    System.out.println("Enter a valid option");
                                }
                            }
                        }
                    }
                    else
                    {
                        System.out.println("Only b or B can be accepted as an input...");
                    }
                }
                
                /*
                    f will only be 1 when we had already answered the question.
                    So we have to break this loop as well..
                */
                if(f==1)
                {
                    break;
                }

                // this if is executed if some other player press the buzzer
                // it works as in this condition server will send this information to all the clients...
                if(socket.getInputStream().available()>0)
                {
                    // buzzer is pressed by some other player
                    output = in.readUTF();
                    System.out.println(output);

                    buzzerUP = true;
                    
                    // f=1 indicates that question was not skipped
                    f=1;
                    
                    /*
                    As some other player presses the buzzer so this 
                    player will not get a chance to aswer this question
                    */
                    break;
                }
            }

            /*
                As we make f=1 in case when outer player answers answers or this player answer
                But what about if question was a tough one and no player answer it...
                In that case we will skip the question and scores will remain the same...
            */

            if(f == 0)
            {
                System.out.println("Question is skipped..");
            }

            //waiting for 1 or 0
            output = in.readUTF();
            
            // As specified 1 means that we are hoping for next question and 0 means GAME SHOW is over..
            if(output.equals("1"))
            {
                // Scores Display..
                output = in.readUTF();
                System.out.println(output);
                // waits for the next question....
                output = in.readUTF();
                System.out.println(output);
                System.out.println("Enter b or B for Buzzer...");
            }
            else
            {
                stop = true;
            }
        }

        // Declares who is the winner and display the respective scores...
        System.out.println("\n\n"+in.readUTF());

        sc.close();
        socket.close();
        out.close();
        
        // ends the program from client side....
        System.exit(0);
    }
}