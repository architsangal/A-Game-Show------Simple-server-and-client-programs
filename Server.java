import java.net.*;  
import java.io.*;
import java.util.*;  
class Server
{
    public static void main(String args[])throws Exception
    {  
        // Scanner object for inputs
        Scanner sc = new Scanner(System.in);
        
        // assigning Question bank to question
        List<ArrayList<String>> question = new ArrayList<ArrayList<String>>();
        Question obj =new Question();
        obj.addQuestionBank(question);
        
        //  Array of serverThread and buzzerThread objects
        ServerThread serverThreads[] = new ServerThread[3];
        Buzzer buzzerThreads[] = new Buzzer[3];
        
        try
        {
            ServerSocket serverSocket = new ServerSocket(20006);//port number
            System.out.println("Server is Live....");
            
            // Telling the host that server is live
            int present_players = 0;
            System.out.println("Waiting for players...");
            for(int i=0;i<3;i++)
            {
                // Connecting to Clients.
                Socket clientSocket = serverSocket.accept();

                //instantiation of objects of arrays
                serverThreads[i] = new ServerThread(clientSocket);
                buzzerThreads[i] = new Buzzer(clientSocket,i);
                
                //passing index
                serverThreads[i].out.writeUTF(i+"");
                
                // there is a difference between output statements of index 0,1 and 2
                // refer to client program line 41
                if(i ==  2)
                {
                    System.out.println("Players are now connected.... GAME SHOW IS Live..");
                    for(int j=0;j<3;j++)
                    {
                        serverThreads[j].out.writeUTF("Players are now connected.... GAME SHOW IS Live..");
                    }
                }
                else
                    serverThreads[i].out.writeUTF("Player is connected.... Please Wait..");
            }
            
            // starting the threads for inputting the names
            for(int i=0;i<3;i++)
            {
                serverThreads[i].start();
            }

            // this loop is used many time in this code to actually make threads are at same pace
            // it is a continuous while loop which ends on when all the threads are waiting
            boolean stop =false;
            int counter = 0;
            while(!stop)
            {
                if(serverThreads[counter].getState() == Thread.State.WAITING)
                    counter++;
                if(counter == 3)
                    stop = true;
            }

            // Now we move on to next set of threads i.e. buzzerThreads
            for(int i=0;i<3;i++)
            {
                // we copy the information from one thread to other corresponding thread. here, it is the name of the player
                buzzerThreads[i].name = serverThreads[i].name;
            }
            
            // we generate a random number to generate a random question
            Random random = new Random();
            // random number must be between [0,question.size()]
            int rand = random.nextInt(question.size());
            // temp is a temporary variable to store the question and answer..
            ArrayList<String> temp = question.get(rand);
            // We want question not to repeat so after storing the question in temp we delete that question
            question.remove(rand);

            for(int i=0;i<3;i++)
            {
                // updating the class variable values
                buzzerThreads[i].question = temp.get(0);
                buzzerThreads[i].answer = temp.get(1);
                buzzerThreads[i].serverThreads = buzzerThreads;
                // we start the threads for asking question simultaneously
                buzzerThreads[i].start();
            }
        
            // that same loop so that threads are at same pace
            stop = false;
            counter = 0; 
            while(!stop)
            {
                if(buzzerThreads[counter].getState() == Thread.State.WAITING)
                    counter++;
                if(counter == 3)
                    stop = true;
            }

            //conditions of how to end the game
            /*
                if any of the players get a score of 5 or greater or if the question in Question gets finished off..
            */
            while(buzzerThreads[0].score < 5 && buzzerThreads[1].score < 5 && buzzerThreads[2].score < 5 && question.size() >0 )
            {
                // as previous threads are dead so we generate new threads and pass the data of old threads to new threads..
                for(int i = 0;i<3;i++)
                {
                    //passing data to new thread to a temporary object

                    //creation of a new thread
                    Buzzer tempThread = new Buzzer(buzzerThreads[i].socket,i);
                    tempThread.name = buzzerThreads[i].name;
                    tempThread.score = buzzerThreads[i].score;
                    
                    //ending the old thread
                    buzzerThreads[i].toNotify();

                    // passing the object to array of threads.
                    buzzerThreads[i] = tempThread;
                }

                //giving reference of other threads to each thread
                for(int i=0;i<3;i++)
                {
                    buzzerThreads[i].serverThreads = buzzerThreads;
                }

                //getting a random Question(for details see lines 79 to 86)
                rand = random.nextInt(question.size());
                temp = question.get(rand);
                question.remove(rand);
                
                //passing questions to each thread
                for(int i=0;i<3;i++)
                {
                    buzzerThreads[i].question = temp.get(0);
                    buzzerThreads[i].answer = temp.get(1);
                    
                    // starting each thread
                    buzzerThreads[i].start();
                }

                // loop so that other threads are at same pace
                stop = false;
                counter = 0; 
                while(!stop)
                {
                    if(buzzerThreads[counter].getState() == Thread.State.WAITING)
                        counter++;
                    if(counter == 3)
                        stop = true;
                }

            }

            // when the loop ends that means game is over so we send zero to all
            // the clients that GAME SHOW is over...
            for(int i=0;i<3;i++)
            {
                buzzerThreads[i].out.writeUTF("0");
            }

            // finds if we have a winner or not
            boolean winner = false;
            
            // information related to winner
            String winner_name ="";
            int w_index = -1;
            
            for(int i=0;i<3;i++)
            {
                //checks if we have winner;; winner is the one who has a score above or equal to 5
                if(buzzerThreads[i].score >= 5)
                {
                    winner = true;

                    // storing data of winner
                    winner_name = buzzerThreads[i].name;
                    w_index = buzzerThreads[i].index;

                    //our task of finding winner has been completed so we break the loop
                    break;
                }

            }

            // Storing the final scores
            String scores = "Final Scores are : \n";
            for(int i=0;i<3;i++)
            {
                scores = scores + buzzerThreads[i].name + " : " + buzzerThreads[i].score + "\n";
            }

            // Sending information of the winner and the score board to each and every player
            if(winner)
            {
                for(int i=0; i<3;i++)
                {
                    if(w_index==buzzerThreads[i].index)
                        buzzerThreads[i].out.writeUTF("You are the winner !!!\n"+ scores);
                    else
                        buzzerThreads[i].out.writeUTF(winner_name + " is the winner !!!\n"+ scores);
                }
            }
            else
            {
                for(int i=0; i<3;i++)
                {
                    buzzerThreads[i].out.writeUTF("No Winner...\n"+ scores);
                }
            }

            if(winner)
            {
                System.out.println("Winner is " + winner_name);
            }
            serverSocket.close();
            sc.close();
            System.exit(0);
        }
        catch(Exception e)
        {
            System.out.println("Main " + e.toString());
            System.exit(0);
        }
    }
}
