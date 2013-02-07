// Get messages from the server
// and act accordingly.
// Lawrie Matthews, 06/01/2009.

import java.net.*;
import java.util.*;
import java.io.*;

public class MessageReceiver extends Thread
{
	Socket socket;
	BufferedWriter out;		// Sends text to socket
	BufferedReader in;		// Gets text from socket
	final String CRLF = "\r\n";
	String response;		// Holds each message from remote host
	private boolean suppressmessages = false;	// After the MOTD has been displayed, set to true.
	ArrayList<String> names = new ArrayList<String>();; // Holds users in a room.
	
	/* The following boolean is necessary because
	 * some servers (EFnet for instance) include
	 * supported CTCP commands in the MOTD, which
	 * confuses us if we see them too soon.		*/
	private boolean motdFinished = false;	// Has the MOTD been read?	
		
	private boolean newNameRequest = true;	// Is this the first RPL_NAMREPLY message? 
	
	public MessageReceiver(Socket server, BufferedReader input, BufferedWriter output)
	{
		socket = server;
		in = input;
		out = output;
	}
	
	public void run()
	{
		try
		{	
			/* The OS and platform Java is running on.
			 * Used for returning CTCP VERSION requests. */
			String osver = "IRCTest " + IRCTest.VERSION + " " + System.getProperty("os.name") + " " + System.getProperty("os.arch");
			System.out.println(osver);
			
			// Loop to read messages from remote host
        	while ((response = in.readLine( )) != null) 
        	{
        		// Message received
            	if (!suppressmessages)
            	{
            		/* After the MOTD has been displayed, we
            		 * don't need to see all the incoming messages,
            		 * so we suppress them once it's all done.    
            		 */
            		
            		// Trim the MOTD to fit an 80 column display.
            		if (response.indexOf("372") >= 0)
            		{
            			response = response.substring(response.indexOf("372"));
            			response = response.substring(response.indexOf(":")+1);
            		}
            		System.out.println(response);
            	}
            	   	
            	/* CTCP requests are not, strictly speaking,
            	 * necessary. They are, however, very useful,
            	 * and easy to implement.					
            	 */
            	
            	// CTCP requests, etc.
            	if (response.contains("PRIVMSG") && motdFinished)
            	{
            		// Get nickname of sender
            		String returnnickname = response.substring(1, response.indexOf("!"));
            		
            		// VERSION request
            		if (response.contains("VERSION") && response.contains("\001"))
            		{
            			// Sends NOTICE message to that nick, with version string.
            			out.write("NOTICE " + returnnickname + " :\001VERSION " + osver + "\001" + " " + CRLF);	// DO NOT CHANGE THIS - VOODOO PROGRAMMING
            		}
            		else if (response.contains("PING"))
            		{
            			out.write("NOTICE " + returnnickname + ":\001PING\001" + CRLF);
            		}
            		// Chat to channels and private messages
            		else
            		{
            			// Where was it sent?
            			String destination = response.substring(response.indexOf("PRIVMSG"));
            			destination = destination.substring(8, destination.indexOf(":"));	// 8 characters = "PRIVMSG" plus one space
            			
            			// What was said?
            			String payload = response.substring(response.indexOf("PRIVMSG"));
            			payload = payload.substring((payload.indexOf(":"))+1);
            			
            			// Print it.
            			System.out.println(returnnickname + " says to " + destination + ": " + payload);
            		}
            		out.flush();
            	} //if
            	else
            	{
            		// PING/PONG challenge/response
	            	if (response.startsWith("PING "))       
	                {
	                	// Respond to ping with correct code.
	                	String pong = "PONG " + response.substring(5) + CRLF;
	                	out.write(pong);
	                	System.out.print(pong);
	               		out.flush();
	                }
	                else if (response.indexOf("NICK ") >=0)
	                {
	                	// Somebody (possibly us) has changed their nick
	                	
	                	// Who was it?
	                	String oldnick = response.substring(1, response.indexOf("!"));
	                	
	                	// Who is it?
	                	response = response.substring(response.indexOf("NICK"));
	                	String newnick = response.substring(response.indexOf(":")+1);
	                	System.out.println(oldnick + " has changed their nickname to " + newnick);
	                }  	                              
	            	// Messages from the server.
	            	else if (response.indexOf("001") >= 0) 
	            	{               	
	                	// Logged in!
	                	System.out.println("Logged in");
	                	IRCTest.loggedIn = true;
	            	}
	            	else if (response.indexOf("301") >= 0)
	            	{
	            		// User is away.
	            		
	            		// Find out their nickname
	            		response = response.substring(response.indexOf("301")+3);
	            		response = response.trim();
	            		String nickname = response.substring(response.indexOf(" "), response.indexOf(":"));
	            		nickname = nickname.trim();
	            		
	            		// and their away message
	            		String awaymsg = response.substring(response.indexOf(":")+1);
	            		System.out.println(nickname + " is away: " + awaymsg);
	            	}
	            	else if (response.indexOf("305") >= 0)
	            	{
	            		// No longer away
	            		System.out.println("You are no longer marked as away.");
	            	}
	            	else if (response.indexOf("306") >= 0)
	            	{
	            		// User is set as away
	            		System.out.println("You are now marked as away.");
	            	}
	            	else if (response.indexOf("332") >= 0)
	            	{
	            		/* Joined a channel!
	            		 * 331: no topic set
	            		 * 332: topic		
	            		 */
	            				            		
	            		// Channel topic
	            		response = response.substring(1); // Trim leading colon
	            		String topic  = response.substring(response.indexOf(":")+1);
	            		System.out.println(IRCTest.channel + " topic: " + topic);
	            	}
	            	else if (response.indexOf("353") >= 0)
	            	{
	            		// Names list (who is in this channel)
	            		
	            		if (newNameRequest)
	            		{
	            			// Empty names list
	            			names = new ArrayList<String>();
	            		}
	            		newNameRequest = false;
	            		
	            		response = response.substring(response.indexOf("353")+3);
	            		if (response.indexOf("=") >= 0)
	            		{
	            			// regular channel
	            			response = response.substring(response.indexOf("="));
	            		}
	            		
	            		names.add(response.substring(response.indexOf(":")+1));
	            	}
	            	else if (response.indexOf("366") >= 0)
	            	{
	            		// End of NAMES list.
	            		
	            		/* When we receive this message, we have the complete 
	            		 * list of everyone in the channel and know we have 
	            		 * connected successfully.
	            		 * We will ALWAYS get a member list, even if we
	            		 * are the only person in a channel.				 
	            		 */
	            		
	            		// Trim away message number.
	            		String channelname = response.substring(response.indexOf("366")+3);
	            		channelname = channelname.trim();	            		
	            		// Trim our nickname
	            		channelname = channelname.substring(channelname.indexOf(" "));
	            		channelname = channelname.trim();	            		
	            		// And finally get our channel.
	            		channelname = channelname.substring(0, channelname.indexOf(" "));
	            		if (channelname.equals("*"))
	            		{
	            			// This is the response from a /names request, ignore.
	            		}
	            		else
	            		{
	            			IRCTest.channel = channelname;
	            			System.out.println("Chatting in : " + IRCTest.channel);	            
	            		}
	            		
	            		// End of names list - print them	            		
	            		if (names.size() > 0)
	            		{
		            		System.out.print("Visible users : ");
		            		for (int i = 0; i < names.size(); i++)
		            		{
		            			System.out.print(names.get(i));
		            			System.out.print(" ");
		            		}
		            		System.out.print("\n");
	            		}
	            		else
	            		{
	            			System.out.println("You feel lonely.");
	            		}
	            		
	            		// Reset variable for next time.
	            		newNameRequest = true;
	            	}
	            	else if (response.indexOf("376") >= 0)
	            	{
	            		// End of MOTD.
	            		motdFinished = true;
	            		suppressmessages = true;
	            	}
	            	/* Error replies have codes 
	            	 * from 400-599:		  	*/
	            	else if (response.indexOf("403") >= 0)
	            	{	            		
	            		// No such channel.
	            		response = response.substring(response.indexOf("403")+3);
	            		response = response.trim();
	            		// Trim our current nickname.
	            		response = response.substring(response.indexOf(" "));
	            		response = response.trim();
	            		// Get the channel name:
	            		String desiredchannel = response.substring(0, response.indexOf(" "));
	            		desiredchannel = desiredchannel.trim();	            		
	            		// Error message
	            		String channelmsg = response.substring(response.indexOf(":")+1);
	            		System.out.print("Cannot join \'" + desiredchannel + "\': ");
	            		System.out.println(channelmsg);
	            		// Once again programming voodoo strikes.
	            	}
	            	else if (response.indexOf("433") >= 0)
	            	{
	            		// Nickname in use.
	            		response = response.substring(response.indexOf("433")+3);
	            		response = response.trim();
	            		// Trim our current nickname
	            		response = response.substring(response.indexOf(" "));
	            		response = response.trim();	            		
	            		// Isolate our desired nickname
	            		String desirednick = response.substring(0, response.indexOf(" "));
	            		// Clash message
	            		String clashmsg = response.substring(response.indexOf(":")+1);
	            		System.out.print("Cannot change nickname to \'" + desirednick + "\': ");
	            		System.out.println(clashmsg);
	            	}
            	}
        	} //while
		}
		catch (IOException e) { System.out.println(e); }
	}
}
