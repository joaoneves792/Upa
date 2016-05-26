package example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketClient {

    public static void main( String[] args ) throws IOException {
        // Check arguments
        if (args.length < 3) {
            System.err.println("Argument(s) missing!");
            System.err.printf("Usage: java %s host port file%n", SocketClient.class.getName());
            return;
        }

        String host = args[0];
        // Convert port from String to int
        int port = Integer.parseInt(args[1]);
        // Concatenate arguments using a string builder
        StringBuilder sb = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            sb.append(args[i]);
            if (i < args.length-1) {
                sb.append(" ");
            }
        }
        String text = sb.toString();
		
        // Create client socket
        Socket socket = new Socket(host, port);
        System.out.printf("Connected to server %s on port %d %n", host, port);
        
		// streams to receive from and send to server, and to read from comand line
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		BufferedReader cmd = new BufferedReader(new InputStreamReader(System.in));
		
        // Send text to server as bytes and receive data (from the example)
        out.writeBytes(text);
        out.writeBytes("\n");
        System.out.println("Sent text: " + text);
		System.out.printf("[SERVER]: '%s'%n%n", in.readLine());
		
		String response;
		
		// read and send loop
        while (true) {
			text = cmd.readLine();
			
			if (text.equals("quit") || text.equals("exit"))
				break;
			
			else {
				out.writeBytes(text);
				out.writeBytes("\n");
				System.out.println("Sent text: " + text);
			}
			
			response = in.readLine();
			System.out.printf("[SERVER]: sent '%s'%n%n", response);

			if (response.equals("OK"))
				break;
        }
        
        // Close client socket
        socket.close();
        System.out.println("Connection closed");
    }
}

