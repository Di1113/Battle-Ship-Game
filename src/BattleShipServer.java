import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

public class BattleShipServer {
    public static void main(String args[]){

        HashMap<String, ArrayList<Socket>> channels = new HashMap<>();

        /* check for user input
         */
        if (args.length != 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        try (
                //Listen to port on Server for incoming connections
                ServerSocket serverSocket =
                        new ServerSocket(Integer.parseInt(args[0]));
        ) {
            //Loop for continuously listening for new incoming connections
            while(true) {
                System.out.println("Accepting new socket.");
                //Connection is handed over from listening port to a different port
                Socket clientSocket = serverSocket.accept();
                //Grab input stream from socket
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("Which room do you want to join?");
                String channel = in.readLine();

                // create a channel by putting sockets in an arraylist
                ArrayList<Socket> channelList;
                // if hashmap contains required channel(ArrayList of socks), return found channel
                if (channels.containsKey(channel)) {
                    channelList = channels.get(channel);
                }else {
                    // else, create a new channel, and add required channel
                    channelList = new ArrayList<>();
                    channels.put(channel, channelList);
                }
                //Add socket to the channel
                channelList.add(clientSocket);
                if(channelList.size() == 2) {
                    ChannelListner cl = new ChannelListner(channelList);
                    cl.start();
                } else if (channelList.size() == 1){
                    // out to player in the room (pir)
                    PrintWriter outpir = new PrintWriter(channelList.get(0).getOutputStream(), true);
                    outpir.println("Notice: Waiting for another player to enter the room...");
                }
                System.out.println("Game Log: Hi how's it going");
            }
        } catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        }
    }
}

class ChannelListner extends Thread{
    ArrayList<Socket> channels;

    ChannelListner(ArrayList<Socket> channels){
        this.channels = channels;
    }

    public void run(){
        GameRoom room = new GameRoom(channels);
        room.start();
    }

}

/*
Extended thread class to handle a while loop
waiting for input from client
*/
class GameRoom extends Thread {
    ArrayList<Socket> clients;
    Semaphore semaphore = new Semaphore(1);

    GameRoom(ArrayList<Socket> clients) {
        this.clients = clients;
    }

    public void run() {

        // setting up the game
        Socket player1 = clients.get(0);
        Socket player2 = clients.get(1);
        //for player 1
        GameBoards gameBoard1 = new GameBoards();
        //for player 2
        GameBoards gameBoard2 = new GameBoards();
        gameBoard1.main_map.fillMap();
        gameBoard1.masked_map.fillMap();
        gameBoard2.main_map.fillMap();
        gameBoard2.masked_map.fillMap();

        // ask players to place ships
        ShipRequest shipRequest1 = new ShipRequest(gameBoard1, player1);
        shipRequest1.start();
        ShipRequest shipRequest2 = new ShipRequest(gameBoard2, player2);
        shipRequest2.start();

        try {
            shipRequest1.join();
            shipRequest2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        //announce game begins
        for (int i = 0; i < clients.size(); i++) {
            Socket curSocket = clients.get(i);
            try {
                PrintWriter out = new PrintWriter(curSocket.getOutputStream(), true);
                out.println("Notice: Game begins.");
                out.println("Notice: \n - You can place your bomb by entering a coordinate; \n" +
                        "- for eg: '0,1' places a bomb on coordinate (0, 1). \n " +
                        "- '&' indicates a miss and '/' indicates a hit." );
            }catch (IOException e){
                e.printStackTrace();
                System.err.println("Starting game failed.");
            }
        }


        boolean p1lost = false;
        boolean p2lost = false;

        //when no one has lost, the game continues
        while (!p1lost && !p2lost) {
            // ask players to place bombs
            PlaceBomb placeBomb1 = new PlaceBomb(gameBoard1, gameBoard2, player1, player2, semaphore);
            PlaceBomb placeBomb2 = new PlaceBomb(gameBoard2, gameBoard1, player2, player1, semaphore);
            placeBomb1.start();
            placeBomb2.start();
            try {
                placeBomb1.join();
                placeBomb2.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            p1lost = gameBoard1.main_map.gameOver();
            p2lost = gameBoard2.main_map.gameOver();

            System.out.println("first round bombing over");
            System.out.println("game continuing?");
        }
        System.out.println("game over");

    }
}

class ShipRequest extends Thread{
    GameBoards gb;
    Socket player;
    ShipRequest(GameBoards gameBoards, Socket socket){
        this.gb = gameBoards;
        this.player = socket;
    }

    public void run(){
        try {
            PrintWriter out = new PrintWriter(player.getOutputStream(), true);
            out.println("Notice: \n - Place your first ship by giving the head and tail " +
                    "coordinates in the following line \n - (for eg: '0,1 0,4' places a size-4 " +
                    "ship in the 0 row horizontally from point 1 to 4). \n - Game will start" +
                    "after you input coordinates for all three ships.");

            BufferedReader p1in = new BufferedReader(
                    new InputStreamReader(player.getInputStream()));

            String inputLine1;
            //Wait for incoming message from player1
            while(gb.main_map.shipUnplaced != 0){
                out.println("Notice: Please place your ship.");
                int flag;
                if ((inputLine1 = p1in.readLine()) != null) {
                    try {
                        String[] coordinates = inputLine1.split(" ", 2);
                        String[] head_xy = coordinates[0].split(",", 2);
                        String[] tail_xy = coordinates[1].split(",", 2);
                        int h_x = Integer.valueOf(head_xy[0]);
                        int h_y = Integer.valueOf(head_xy[1]);
                        int t_x = Integer.valueOf(tail_xy[0]);
                        int t_y = Integer.valueOf(tail_xy[1]);
                        flag = gb.main_map.placingShip(h_x, h_y, t_x, t_y);
                    } catch (ArrayIndexOutOfBoundsException ie){
                        flag = 5;
                    }

                    switch (flag){
                        case 0:
                            out.println("Ship placed successfully.");
                            break;
                        case 1:
                            out.println("Ship placement failed. You can only place " +
                                    "a ship with 2, 3, or 4 holes.");
                            break;
                        case 2:
                            out.println("Ship placement failed. You have placed a ship " +
                                    "with this size. Please place another ship.");
                            break;
                        case 3:
                            out.println("Ship placement failed. This ship overlaps with existing ship.");
                            break;
                        case 4:
                            out.println("Ship placement failed. Please place the ship " +
                                    "horizontally or vertically. Diagonal placement is forbidden.");
                            break;
                        case 5:
                            out.println("Ship placement failed. Please input correct coordinates.");
                            break;
                    }
                    out.println(gb.main_map.printMap("Your field"));
                }
            } out.println("Notice: All three ships are ready. Waiting for game to begin...");

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Requesting ship coordinates failed.");
        }
    }
}


class PlaceBomb extends Thread{
    GameBoards selfgb;
    GameBoards oppgb;
    Socket player1;
    Socket player2;
    Semaphore goPass;

    PlaceBomb(GameBoards ownBoard, GameBoards oppBorad, Socket socket1, Socket socket2, Semaphore semaphore){
        this.selfgb = ownBoard;
        this.oppgb = oppBorad;
        this.player1 = socket1;
        this.player2 = socket2;
        this.goPass = semaphore;
    }

    public int askBombSite(Socket player1) {
        try {
            BufferedReader in1 = new BufferedReader(
                    new InputStreamReader(player1.getInputStream()));

            String coord = in1.readLine();
            String[] coordinate;
            if (coord != null) {
                coordinate = coord.split(",", 2);
                int x = Integer.valueOf(coordinate[0]);
                int y = Integer.valueOf(coordinate[1]);
                int hit = oppgb.main_map.shipHit(x, y);
                selfgb.masked_map.markBombed(hit, x, y);
                return hit;
            }
        }catch(IOException e){
                e.printStackTrace();
        }
        return -3;
    }

    public void run(){

        try {
            goPass.acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {

            PrintWriter out1 = new PrintWriter(player1.getOutputStream(), true);
            PrintWriter out2 = new PrintWriter(player2.getOutputStream(), true);
            // check if player has lost
            if(selfgb.main_map.gameOver()){
                goPass.release();
                return;
            }

            out1.println("Notice: Your turn.");
            out2.println("Notice: Opponent player's turn.");

            int hit = askBombSite(player1);

            while (hit == -2){
                out1.println("Notice: You have already bombed this site. Choose another site.");
                hit = askBombSite(player1);
            }

            if (hit == -1){
                out1.println("Notice: It was a miss...");
                out2.println(oppgb.main_map.printMap("Map Update: There was a miss. Your ships are safe."));
            }  else {
                out1.println("Notice: It was a hit!");
                out2.println(oppgb.main_map.printMap("Map Update: Your ship is hit..."));
                if (hit == 100){
                    out1.println("Notice: You have sunk a ship!");
                    out2.println("Notice: One of your ship has sunk");
                    if(oppgb.main_map.gameOver()){
                        out1.println("Notice: You won. Game over.");
                        out2.println("Notice: You lost. Game over.");
                        goPass.release();
                        return;
                    }
                }
            }
            out1.println(selfgb.masked_map.printMap("Map Update: opponent's field"));
            out1.println(selfgb.main_map.printMap("Map Update: your own field"));
        }
         catch (IOException e) {
            e.printStackTrace();
        }
        goPass.release();
    }
}

