import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class BattleShipClient {
    public static void main(String args[]){

        // check if user import host name, port number, channel for connecting to server
        if (args.length != 2) {
            System.err.println(
                    "Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        // create socket, establish connection
        // get host name
        String hostName = args[0];
        // get port number
        int portNumber = Integer.parseInt(args[1]);

        try(
            Socket playerSocket = new Socket(hostName, portNumber);
            PrintWriter out = new PrintWriter(playerSocket.getOutputStream(), true);
            BufferedReader stdIn = new BufferedReader(new InputStreamReader(System.in))
        ){
            String userInput;

            ServerListener serverListener = new ServerListener(playerSocket);
            serverListener.start();

//            // get channel number
//            String channel = args[2];
//
//            out.println(channel);

            while ((userInput = stdIn.readLine()) != null){
                out.println(userInput);
            }

        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.err.println("Couldn't get I/O for the connection to " +
                    hostName);
        }

    }
}

class ServerListener extends Thread {
    Socket socket;
    ServerListener(Socket socket){
        this.socket = socket;
    }

    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String serverInput;
            while ((serverInput = in.readLine()) != null){
                System.out.println(serverInput);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}