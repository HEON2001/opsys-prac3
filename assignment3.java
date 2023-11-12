import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class assignment3 {

    static int i = 0; //thread counter
    private static final Object lock = new Object(); //lock for thread counter

    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 4) {
            System.err.println("Usage: java Assignment3 -l <listening port> -p <pattern>");
            System.exit(1);
        }

        int portNumber = 1234; //default port number
        String pattern = "";

        if(args[0].equals("-l") && args[2].equals("-p")){
            portNumber = Integer.parseInt(args[1]);
            pattern = args[3];
        }else if(args[0].equals("-p") && args[2].equals("-l")){
            portNumber = Integer.parseInt(args[3]);
            pattern = args[1];
        }else{
            System.err.println("Usage: java Assignment3 -l <listening port> -p <pattern>");
            System.exit(1);
        }

        if(portNumber < 1024){
            System.err.println("Port number must be greater than 1024");
            System.exit(1);
        }

        System.out.println("Listening on port: " + portNumber);
        System.out.println("Search pattern: " + pattern + "\n");

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            while (true) {
                //accept connection
                Socket clientSocket = serverSocket.accept();
                //create new thread
                Thread thread = new Thread(new ClientHandler(clientSocket, pattern));
                thread.start();
                //increment thread counter
                synchronized (lock) {
                    i++;
                }
                System.out.println("Client " + i + " connected");
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
        int line;

        Node(String data) {
            this.data = data;
            this.next = null;
            this.book_Next = null;
            this.line = 0;
        }
    }

    static class ClientHandler implements Runnable {

        private final Socket clientSocket;
        private static final Object fileLock = new Object(); //lock for file writing
        private final String pattern; //pattern to search for
        private final List<String> patternLines; //list of lines containing pattern

        ClientHandler(Socket socket, String pattern) {
            this.clientSocket = socket;
            this.pattern = pattern;
            this.patternLines = new ArrayList<>();
        }

        @Override
        public void run() {
            try (
                    PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
            ) {
                int threadId = 0;
                //getting thread id
                synchronized (lock) {
                    threadId = i;
                }
                
                FileWriter writer = new FileWriter("book_" + String.format("%02d", threadId) + ".txt", true);
                String inputLine = in.readLine();
                Node head = new Node(inputLine);
                Node currentNode = head;

                int patternCount = 0;
                int lineCount = 0;

                while (inputLine != null) {
                    //read next line
                    inputLine = in.readLine();
                    //synchronize file writing to book with thread id
                    synchronized (fileLock) {
                    writer.write(currentNode.data + "\n");
                    //get line number of current node
                    currentNode.line = lineCount;
                    lineCount++;
                        //check if current node contains pattern
                        if (currentNode.data.contains(pattern)) {
                            patternLines.add("(ln."+lineCount +") " + currentNode.data);
                            patternCount++;
                        }
                    }
                    //set the node that links to next item in the same book
                    currentNode.book_Next = new Node(inputLine);
                    currentNode = currentNode.book_Next;
                }

                //print results
                System.out.println("\nPattern '" + pattern + "' appears in book_" +
                String.format("%02d", threadId) + ": " + patternCount + " times");
                
                System.out.println("Lines that contain '" + pattern + "' in book_" +
                String.format("%02d", threadId) + " are:\n"); 

                for (String line : patternLines) {
                    System.out.println(line);
                }
                System.out.println("\n");

                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
