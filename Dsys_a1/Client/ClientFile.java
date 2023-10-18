import java.io.*;
import java.net.*;
import java.util.Scanner;

public class ClientFile {
    public static void main(String[] args) {
        // default address and port to connect to server
        String serverAddress = "127.0.0.1";
        int serverPort = 12345;
        // declarations socket and scanner to reciever user input
        Socket socket = null;
        Scanner scanner = new Scanner(System.in);

        try {
            socket = new Socket(serverAddress, serverPort);
            System.out.println("Connected to server");

            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            while (true) { // prompting user for sign in register or exit the program
                System.out.println("Would you like to register or sign in: ");
                System.out.println("Commands (register) (sign in) (quit)");

                String answer = scanner.nextLine();
                out.println(answer); // Send the user's choice to the server

                if (answer.equals("register")) {
                    ClientFunctions.registerUser(out, in, scanner);
                } else if (answer.equals("sign in")) {
                    ClientFunctions.signInUser(out, in, scanner);
                    break;
                } else if (answer.equalsIgnoreCase("quit")) {
                    socket.close();
                    System.exit(0); // Exit the entire program
                } else {
                    System.out.println("Invalid action.");
                }
            }

            while (true) { // while statement for user response to edit user list or exit the application
                System.out.println("Do you want to add an item to your list, remove an item, view your list, or exit");
                System.out.println("Commands: (add) (remove) (display) (exit)");
                String answer = scanner.nextLine(); // item to be parse

                if (answer.equalsIgnoreCase("add")) { // add item to list case
                    System.out.println("Enter a task you want to add:");
                    String item = scanner.nextLine();
                    System.out.println("Enter a deadline in (yyyyMMdd)"); // deadline for item
                    String deadline = scanner.nextLine();
                    ClientFunctions.updateUserList(out, in, "add", item, deadline);
                } else if (answer.equalsIgnoreCase("remove")) { // remove item from list case
                    System.out.println("Enter the item you want to remove:");
                    String item = scanner.nextLine();
                    System.out.println("Enter the deadline in (yyyyMMdd)"); // deadline of removed item
                    String deadline = scanner.nextLine();
                    ClientFunctions.updateUserList(out, in, "remove", item, deadline);
                    System.out.println("Removed: " + item);
                } else if (answer.equalsIgnoreCase("exit")) {
                    break;
                } else if (answer.equalsIgnoreCase("display")) { // Display user list action
                    ClientFunctions.displayUserList(out, in);
                } else {
                    System.out.println("Invalid action."); // if user does not make a proper request
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

