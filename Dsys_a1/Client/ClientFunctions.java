import java.io.*;
import java.util.Scanner;

public class ClientFunctions {
    // function for registration 
    public static void registerUser(PrintWriter out, BufferedReader in, Scanner scanner) {
        try {
            // prompt to get user to input a username and password to later use in other functions
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();
            System.out.println("Enter your password: ");
            String password = scanner.nextLine();

            out.println(username); // sends username to server
            out.println(password); // sends password to server

            // Handle registration response from the server
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // function for logging in to server
    public static void signInUser(PrintWriter out, BufferedReader in, Scanner scanner) {
        try {
            out.println("signin"); // SendS the "sign in" response to the server
            // prompt to get user to input a username and password to later use in other functions            
            System.out.println("Enter your username: ");
            String username = scanner.nextLine();
            System.out.println("Enter your password: ");
            String password = scanner.nextLine();

            out.println(username); // sends username to server
            out.println(password); // sends password to server

            // Handle sign-in response from the server
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) { // in case of error in reading input
            e.printStackTrace();
        }
    }
    // function for adding / removing values to a specific users list
    public static void updateUserList(PrintWriter out, BufferedReader in, String action, String item, String deadline) {
        // Send the request to update the user's list to the server
        out.println("updateList");

        out.println(action); // add or remove an action to manipulate a user list
        out.println(item); // item that is being manipulated in list
        out.println(deadline); // deadline for item being implemented in list

        try { // try catch block to send server response and catch error
            String response = in.readLine();
            System.out.println("Server response: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Function for displaying User List
    public static void displayUserList(PrintWriter out, BufferedReader in) {
        // Send the request to display user list to the server        
        out.println("display");
        try {
            String response = in.readLine();
            System.out.println("Server response: \n" + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



}

