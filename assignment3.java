import java.io.*;
import java.net.*;

public class assignment3 {

    static int i = 0;
    private static final Object lock = new Object();

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 4) {
            System.err.println("Usage: java assignment3 -l <listening port> -p <pattern>");
            System.exit(1);
        }

        int portNumber = 1234;
        String pattern = "";

        if(args[0].equals("-l") && args[2].equals("-p")){
            portNumber = Integer.parseInt(args[1]);
            pattern = args[3];
        }else if(args[0].equals("-p") && args[2].equals("-l")){
            portNumber = Integer.parseInt(args[3]);
            pattern = args[1];
        }else{
            System.out.println(args[0]);
            System.out.println(args[1]);
            System.out.println(args[2]);
            System.out.println(args[3]);
            System.err.println("Usage: java assignment3 -l <listening port> -p <pattern>");
            System.exit(1);
        }

        System.out.println("Listening on port: "+portNumber);

        if(portNumber<1024 || portNumber>65535){
            System.err.println("Port number must be between 1024 and 65535");
            System.exit(1);
        }

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread thread = new Thread(new ClientHandler(clientSocket));
                thread.start();
                synchronized (lock) {
                    i++;
                }
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
        private static final Object fileLock = new Object();

        ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                FileWriter writer = null;
                //FileWriter writer = new FileWriter("book_"+String.format("%02d",i)+".txt", true);
                String inputLine = in.readLine();
                Node head = new Node(inputLine);
                Node currentNode = head;

                while (inputLine != null) {
                    inputLine = in.readLine();
                    synchronized (fileLock) {
                        writer = new FileWriter("book_" + String.format("%02d", i) + ".txt", true);
                        writer.write(currentNode.data + "\n");
                        writer.close();
                    }
                    synchronized (lock) {
                        currentNode.book_Next = new Node(inputLine);
                        currentNode = currentNode.book_Next;
                    }
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}