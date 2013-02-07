// Receive input from the user
// and send messages over IRC.
// Lawrie Matthews, 06/01/2009.

import java.net.*;
import java.util.*;
import java.io.*;

public class MessageSender extends Thread
{
	Scanner scan = new Scanner(System.in);
	Socket socket = null;
	BufferedReader in = null;
	BufferedWriter out = null;
	final String CRLF = "\r\n";
	
	public MessageSender(Socket server, BufferedReader input, BufferedWriter output)
	{
		socket = server;
		in = input;
		out = output;
	}
	
	public void run()
	{
		try
		{
			// Send nickname and user registration.
			System.out.println("Sending nick");
			out.write("NICK " + IRCTest.nickname + CRLF);
			System.out.println("Sending user");
			out.write("USER " + IRCTest.nickname + " 0 * :" + IRCTest.realname + CRLF);
			out.flush();
			
			// We always want to accept user input
			while(true)
			{
				String message = scan.nextLine();
				
				// Handle chat commands.
				if (message.startsWith("/quit"))
				{
					// Send quit message, if any.
					String quitmsg;
					try
					{
						quitmsg = message.substring(message.indexOf("t")+2);
					}
					catch (StringIndexOutOfBoundsException e) { quitmsg = "Quit IRC."; }
					out.write("QUIT :" + quitmsg + CRLF);
					out.flush();
					
					IRCTest.shutdown(socket);
				}
				else if (message.startsWith("/nick"))
				{
					// Attept to change the user's nickname.
					String newnick = message.substring(6);	// 6 = "/nick" and a space
					out.write("NICK :" + newnick + CRLF);
					out.flush();					
				}
				else if (message.startsWith("/msg"))
				{
					// Private message to a user.
					String target = message.substring(5); // 5 characters = "/msg" and a space
					String payload = target.substring(target.indexOf(" "));
					target = target.substring(0, target.indexOf(" "));
					
					out.write("PRIVMSG " + target + " :" + payload + CRLF);
					out.flush();
					System.out.println("Sent to " + target + ": " + payload);
				}
				else if (message.startsWith("/join"))
				{
					// Request to join a channel.
					String channel = message.substring(6); // 6 characters = "/join" and a space
					channel = channel.trim();	// Whitespace is the enemy
					
					if (channel.equals("0"))
					{
						/* This is a special JOIN message -
						 * it really means quit all channels */
						 
						 out.write("JOIN 0" + CRLF);
						 out.flush();
						 
						 IRCTest.channel = "";
					}
					else
					{
						out.write("JOIN " + channel + CRLF);
						out.flush();
					}
				}
				else if (message.startsWith("/away"))
				{
					// User setting AWAY status
					String awaymsg = "";
					try
					{
						awaymsg = message.substring(6); // 6 characters - "/away" and a space
					}
					catch (StringIndexOutOfBoundsException e)
					{
						// No away message specified.
					}
					
					if (awaymsg.equals(""))
					{
						// We don't want a colon if there's no away message.
						out.write("AWAY" + CRLF);
					}
					else
					{
						out.write("AWAY :" + awaymsg + CRLF);
					}					
					out.flush();
					// The server deals with deciding if we are away or returned.
				}
				else if (message.startsWith("/names"))
				{
					// NAMES request (who can we see?)
					
					message = message.trim();
					if (message.equals("/names"))
					{
						// No parameters so just send request
						out.write("NAMES" + CRLF);
					}
					else
					{
						// Should be a list of channels
						String namesmsg = message.substring(7);	// 7 = /names plus a space
						out.write("NAMES :" + namesmsg + CRLF);
					}
					out.flush();
				}
				else if (message.equals("/debug"))
				{
					// TODO
				}
				else
				{
					// Any other typing is just chat.
					out.write("PRIVMSG " + IRCTest.channel + " :" + message + CRLF);
					out.flush();
					System.out.println("Sent to " + IRCTest.channel + " : " + message);
				}
			}//while			
		}//try
		catch (IOException e) { System.out.println(e); }
	}
}
