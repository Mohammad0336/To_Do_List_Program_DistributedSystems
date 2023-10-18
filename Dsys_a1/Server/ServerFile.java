import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ServerUser {
    public static void main(String[] args) {
        int port = 12345;
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("Server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Create a new thread to handle the client connection
                Runnable clientHandler = new ClientHandler(clientSocket);
                executor.execute(clientHandler);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Define a separate client handler class
    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private PrintWriter out;
        private String username; // Store the signed-in username

        public ClientHandler(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            try {
                // Create input and output streams for communication
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

                String request;
                boolean userSignedIn = false; // Track whether the user is signed in

                while ((request = in.readLine()) != null) {
                    if (request.equals("register")) {
                        // Handle user registration request
                        registerUser(in, out);
                        if (username != null) {
                            userSignedIn = true;
                        }
                    } else if (request.equals("signin")) {
                        // Handle user sign-in request
                        username = signInUser(in, out);
                        if (username != null) {
                            userSignedIn = true;
                        }
                    } else if (request.equals("updateList")) {
                        // Handle user update list request
                        if (!userSignedIn) {
                            out.println("You must sign in first to update the list.");
                        } else {
                            String action = in.readLine(); // Read the action (add or remove)
                            String itemName = in.readLine();   // Read the item name
                            String itemDeadline = in.readLine(); // Read the item deadline

                            updateUserList(username, action, itemName, itemDeadline, out);
                        }
                    } else if (request.equals("display")) { // display user list request when signed in
                        if (!userSignedIn) {
                            out.println("You must sign in first to display your list.");
                        } else {
                            displayUserList(out, username);
                        }
                    }
                }

                clientSocket.close();
                System.out.println("Client disconnected");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public String registerUser(BufferedReader in, PrintWriter out) {
            String username;
            String password;
            boolean isUsernameUnique = false;

            while (!isUsernameUnique) {
                try {
                    username = in.readLine(); // reads inputs until valid username is found
                    password = in.readLine();
                } catch (IOException e) {
                    System.err.println("Error reading user registration data: " + e.getMessage());
                    return null;
                }

                try {
                    File file = new File("user_data.txt"); // database file
                    if (file.exists()) {
                        BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line;

                        // Check if the username already exists
                        while ((line = reader.readLine()) != null) {
                            if (line.startsWith("Username: " + username)) {
                                reader.close();
                                out.println("Username already exists. Please choose a different username.");
                                return null; // Terminate the registration process
                            }
                        }

                        if (line == null) {
                            // The username does not exist, so it's unique
                            isUsernameUnique = true;
                            reader.close();
                        }
                    } else {
                        isUsernameUnique = true;
                    }

                    if (isUsernameUnique) {
                        // Process and save user registration data
                        String userData = "Username: " + username + "\nPassword: " + password;
                        System.out.println("User Information has been saved: \n" + userData);

                        // Save user data to a file or database

                        // Write the user information text file exist if not create
                        if (!file.exists()) {
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                System.err.println("Error creating the file: " + e.getMessage());
                                out.println("Failed to register.");
                                return null;
                            }
                        }
                        // Write the user information to the text file
                        try (FileWriter fileWriter = new FileWriter(file, true)) {
                            fileWriter.write(userData + "\n"); // Append user info to the existing content
                            out.println("Registration successful: " + username);
                            return username;
                        } catch (IOException e) {
                            System.err.println("Error writing to the file: " + e.getMessage());
                            out.println("Failed to register.");
                            return null;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading the user data file: " + e.getMessage());
                    out.println("Failed to register.");
                    return null;
                }
            }
            return null; // This line should not be reached
        }

        public String signInUser(BufferedReader in, PrintWriter out) {
            String username;
            String password;
            try { // try retrieving username, password to parse for rest of function
                username = in.readLine();
                password = in.readLine();
            } catch (IOException e) {
                System.err.println("Error reading user sign-in data: " + e.getMessage());
                return null;
            }

            try { // in the case user trys to sign in with no data file existing
                File file = new File("user_data.txt");
                if (!file.exists()) {
                    out.println("User data file does not exist. Please register first.");
                    return null;
                }

                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;
                boolean isUserFound = false;

                // validation that username and password are valid and in the txt file
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("Username: " + username)) {
                        String storedPassword = reader.readLine(); // Read the password line

                        if (storedPassword.equals("Password: " + password)) {
                            isUserFound = true;
                            System.out.println("User has appropriate credentials and is now logged in");
                            out.println("Sign-in successful. Welcome, " + username + "!");
                            return username;
                        }
                    }
                }

                reader.close();

                if (!isUserFound) {
                    out.println("Sign-in failed. Invalid username or password.");
                }
            } catch (IOException e) {
                System.err.println("Error reading the user data file: " + e.getMessage());
            }
            return null;
        }

        public void updateUserList(String username, String action, String itemName, String itemDeadline, PrintWriter out) {
            try {
                // Modify the DateTimeFormatter pattern to match "yyyyMMdd"
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                LocalDate deadline = LocalDate.parse(itemDeadline, formatter);

                if (action.equalsIgnoreCase("add")) {
                    // Append item and deadline to the user's list file
                    File userFile = new File(username + "_list.txt");
                    FileWriter fileWriter = new FileWriter(userFile, true); // Append to file

                    // Format the deadline in the desired "yyyyMMdd" format
                    String formattedDeadline = deadline.format(formatter);
                    fileWriter.write(itemName + " Due: " + formattedDeadline + "\n");

                    fileWriter.close();
                    System.out.println("Item added to the list for user: " + username);
                    out.println("Item added");
                } else if (action.equalsIgnoreCase("remove")) {
                    // Remove the item from the user's list file
                    File userFile = new File(username + "_list.txt");
                    File tempFile = new File(username + "_list_temp.txt");

                    BufferedReader reader = new BufferedReader(new FileReader(userFile));
                    BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile));

                    String line;
                    boolean itemFound = false;

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith(itemName)) { // Check if the line starts with the item name
                            itemFound = true;
                        } else {
                            writer.write(line + "\n");
                        }
                    }

                    reader.close();
                    writer.close();

                    if (itemFound) { // if loop for decision on how to handle item situations
                        // Rename the temporary file to the original user's list file
                        if (tempFile.renameTo(userFile)) {
                            System.out.println("Item removed from the list for user: " + username);
                            out.println("Item removed");
                        } else {
                            out.println("Failed to remove the item.");
                        }
                    } else {
                        out.println("Item not found in the list.");
                    }
                } else {
                    out.println("Invalid action. Please use 'add' or 'remove'.");
                }
            } catch (DateTimeParseException e) {
                out.println("Invalid deadline format. Use yyyyMMdd");
            } catch (IOException e) {
                System.err.println("Error updating user list data: " + e.getMessage());
                out.println("Failed to update the list.");
            }
        }


        // function to display all lines of the user list saved in the user text files
        public void displayUserList(PrintWriter out, String username) {
            try {
                File userFile = new File(username + "_list.txt");
                if (userFile.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(userFile));
                    String line;
                    List<String> items = new ArrayList<>();

                    while ((line = reader.readLine()) != null) {
                        items.add(line);
                    }

                    // Sort the items by deadline using a custom Comparator
                    Collections.sort(items, new DeadlineComparator());

                    System.out.println("User's Items");
                    for (String item : items) {
                        out.println(item);
                        System.out.println(item); // Print to the server's console
                    }

                    reader.close();
                } else {
                    out.println("Your list is empty.");
                }
            } catch (IOException e) {
                System.err.println("Error displaying user list: " + e.getMessage());
                out.println("Failed to display items.");
            }
        }

        // Define a custom Comparator to sort items by deadline
        private class DeadlineComparator implements Comparator<String> {
            @Override
            public int compare(String item1, String item2) {
                // Parse and compare the deadlines
                LocalDate deadline1 = parseDeadline(item1);
                LocalDate deadline2 = parseDeadline(item2);
                return deadline1.compareTo(deadline2);
            }

            private LocalDate parseDeadline(String item) {
                try {
                    // Extract the deadline substring and parse it into a LocalDate
                    String deadlineSubstring = item.substring(item.indexOf(" Due: ") + 6);
                    return LocalDate.parse(deadlineSubstring, DateTimeFormatter.ofPattern("yyyyMMdd"));
                } catch (DateTimeParseException e) {
                    // Handle parsing errors
                    return LocalDate.MIN;
                }
            }
        }

    }

}

