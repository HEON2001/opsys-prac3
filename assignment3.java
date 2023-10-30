import java.io.*;
import java.net.*;

public class assignment3 {

    static int i = 1;
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: java assignment3 <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);

        if(portNumber<1024 || portNumber>65535){
            System.err.println("Port number must be between 1024 and 65535");
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
                i++;
            }
        } catch (IOException e) {
            System.err.println("Could not listen on port " + portNumber);
            System.exit(-1);
        }
    }

    static class Node {
        String data;
        Node next;
        Node book_Next;

        Node(String data) {
            this.data = data;
            this.next = null;
            this.book_Next = null;
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket clientSocket;

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                FileWriter writer = new FileWriter("book_"+String.format("%02d",i)+".txt", true);
                String inputLine = in.readLine();
                Node head = new Node(inputLine);
                Node currentNode = head;

                while (inputLine != null) {
                    inputLine = in.readLine();
                    writer.write(currentNode.data+"\n");
                    currentNode.book_Next = new Node(inputLine);
                    currentNode = currentNode.book_Next;
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}