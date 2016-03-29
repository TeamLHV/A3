/******************************************************************************************************************
* File:SecurityMonitor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class monitors the environmental control systems that control museum temperature and humidity. In addition to
* monitoring the temperature and humidity, the SecurityMonitor also allows a user to set the humidity and temperature
* ranges to be maintained. If temperatures exceed those limits over/under alarm indicators are triggered.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	static private void Heater(MessageManagerInterface ei, boolean ON )
*	static private void Chiller(MessageManagerInterface ei, boolean ON )
*	static private void Humidifier(MessageManagerInterface ei, boolean ON )
*	static private void Dehumidifier(MessageManagerInterface ei, boolean ON )
*
******************************************************************************************************************/
import InstrumentationPackage.Indicator;
import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;

class SecurityMonitor extends Thread
{
	private MessageManagerInterface em = null;	// Interface object to the message manager
	private String MsgMgrIP = null;				// Message Manager IP address
	boolean Registered = true;					// Signifies that this class is registered with an message manager.
	MessageWindow mw = null;					// This is the message window
	Indicator di;								// Door Security indicator
	Indicator wi;								// Window Security indicator
	Indicator mi;								// Motion Security indicator
	private boolean securityArmed = true;
	String message = "Monitors Idle";

	public boolean isSecurityArmed() {
		return securityArmed;
	}

	public void setSecurityArmed(boolean securityArmed) {
		this.securityArmed = securityArmed;
		if(securityArmed){
			di.SetLampColorAndMessage("DS On", 1);
			wi.SetLampColorAndMessage("WS On", 1);
			mi.SetLampColorAndMessage("MS On", 1);
			message = "Monitors Idle";
		}else{
			di.SetLampColorAndMessage("DS Off", 0);
			wi.SetLampColorAndMessage("WS Off", 0);
			mi.SetLampColorAndMessage("MS Off", 0);
			message = "Monitors Off";
			Message msg = new Message((int) 17, "AA");
			try {
				em.SendMessage(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public SecurityMonitor()
	{
		try
		{
			em = new MessageManagerInterface();
		}
		catch (Exception e)
		{
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;
		} // catch

	} //Constructor

	public SecurityMonitor( String MsgIpAddress )
	{
		MsgMgrIP = MsgIpAddress;
		try
		{
			em = new MessageManagerInterface( MsgMgrIP );
		}
		catch (Exception e)
		{
			System.out.println("SecurityMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int	Delay = 2000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag

		if (em != null)
		{
			// Now we create the Security status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("Security Monitoring Console", 0, 0);
			di = new Indicator ("DS On", mw.GetX()+ mw.Width(), 0,1);
			wi = new Indicator ("WS On", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 1 );
			mi = new Indicator ("MS On", mw.GetX()+ mw.Width(), (int)(mw.Height()/2), 1 );

			mw.WriteMessage( "Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			while ( !Done )
			{
				// Here we get our message queue from the message manager

				try
				{
					eq = em.GetMessageQueue();

				} // try

				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch

				int qlen = eq.GetSize();
				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();
					if (securityArmed) {
						if (Msg.GetMessageId() == 11) // Door reading
						{
							try {
								if (Msg.GetMessage().equalsIgnoreCase("D1")) {
									message = "Door is unsafe..Alert!!!";
									Message msg = new Message((int) 14, "D1");
									em.SendMessage(msg);
									di.SetLampColorAndMessage("DS-ALERT!", 3);
								}
							} // try
							catch (Exception e) {
								mw.WriteMessage("Error reading Door messages: " + e);
							} // catch
						} // if
						if (Msg.GetMessageId() == 12) // Window reading
						{
							try {
								if (Msg.GetMessage().equalsIgnoreCase("W1")) {
									message = "Window is unsafe..Alert!!!";
							 		Message msg = new Message((int) 15, "W1");
									em.SendMessage(msg);
									wi.SetLampColorAndMessage("WS-ALERT!", 3);
								}

							} // try

							catch (Exception e) {
								mw.WriteMessage("Error reading window messages: " + e);

							} // catch

						} // if
						if (Msg.GetMessageId() == 13) // Motion reading
						{
							try {
								if (Msg.GetMessage().equalsIgnoreCase("M1")) {
									message = "Motion detected: status unsafe..Alert!!!";
									Message msg = new Message((int) 16, "M1");
									em.SendMessage(msg);
									mi.SetLampColorAndMessage("MS-ALERT!", 3);
								}

							} // try

							catch (Exception e) {
								mw.WriteMessage("Error reading motion detection messages: " + e);

							} // catch

						} // if
							// If the message ID == 99 then this is a signal that the simulation
							// is to end. At this point, the loop termination flag is set to
							// true and this process unregisters from the message manager.
					}
					if ( Msg.GetMessageId() == 99 )
					{
						Done = true;

						try
						{
							em.UnRegister();

				    	} // try

				    	catch (Exception e)
				    	{
							mw.WriteMessage("Error unregistering: " + e);

				    	} // catch

				    	mw.WriteMessage( "\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left for the
						// user to exit so they can see the last message posted.

						di.dispose();
						wi.dispose();
						mi.dispose();

					} // if

				} // for

				mw.WriteMessage("Security Status:: " + message);

				// This delay slows down the sample rate to Delay milliseconds

				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n" );

		} // if

	} // main

	public boolean IsRegistered()
	{
		return( Registered );

	} // SetTemperatureRange

	

	public void Halt()
	{
		mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );
		Message msg;
		msg = new Message( (int) 99, "XXX" );
		try
		{
			em.SendMessage( msg );
		} // try
		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);
		} // catch
	} // Halt
} // SecurityMonitor