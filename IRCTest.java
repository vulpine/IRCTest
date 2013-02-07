// Prototype IRC client
// Lawrie Matthews, 05/01/2009.

import java.net.*;
import java.util.*;
import java.io.*;

public class IRCTest
{		
	public static boolean loggedIn = false;
	public static String nickname;
	public static String realname;
	public static String channel;
	public final static String VERSION = "0.0.6";
	
	public static void main(String[] args)
	{	
		BufferedReader in;				// Gets text from socket
		BufferedWriter out;				// Sends text to socket
		String serveraddr = "example.org";	// IRC server address
		int port = 6667;				// IRC server port
		nickname = "nickname";		// Username
		realname = "Joe Bloggs";	// Real name
		Socket socket;
				
		try
		{
			socket = new Socket(serveraddr, port);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
			
			// Create the threads to deal with messages
			MessageReceiver listener = new MessageReceiver(socket, in, out);
			MessageSender sender = new MessageSender(socket, in, out);
			listener.start();
			sender.start();	
		}
		catch (UnknownHostException e) 
		{
			System.out.println("A connection to the remote server could not be established.");
			System.out.println("(" + e + ")");
			System.exit(1);
		}
		catch (IOException e) { System.out.println(e); }	
	}
	
	public static void shutdown(Socket socket)
	{
		// Quit cleanly.
        try
        {
        	socket.close();
        	System.exit(0);
        }
        catch (IOException e) { System.out.println(e); }	
	}
}
