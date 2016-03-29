package SystemC;

/******************************************************************************************************************
 * File:ECSMonitor.java
 * Course: 17655
 * Project: Assignment A3
 * Copyright: Copyright (c) 2009 Carnegie Mellon University
 * Versions:
 *	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
 *
 * Description:
 *
 * This class monitors the environmental control systems that control museum temperature and humidity. In addition to
 * monitoring the temperature and humidity, the ECSMonitor also allows a user to set the humidity and temperature
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
import InstrumentationPackage.*;
import MessagePackage.*;
import SecurityPackage.MessageEncryptor;

import java.util.*;

class ServiceMaintenanceMonitor extends Thread
{
	private MessageManagerInterface em = null;	// Interface object to the message manager
	private String MsgMgrIP = null;				// Message Manager IP address
	boolean Registered = true;					// Signifies that this class is registered with an message manager.
	MessageWindow mw = null;					// This is the message window
	private static HashMap<Long, String> deviceHistory = new HashMap<Long, String>();
	String senderDesc = "SMMonitor";

	public ServiceMaintenanceMonitor()
	{
		// message manager is on the local system

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is on the local machine

			em = new MessageManagerInterface();

		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} //Constructor

	public ServiceMaintenanceMonitor( String MsgIpAddress )
	{ 
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try
		{
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine

			em = new MessageManagerInterface( MsgMgrIP );
		}

		catch (Exception e)
		{
			System.out.println("ECSMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

	} // Constructor

	public void run()
	{
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int	Delay = 3000;				// The loop delay (1 second)
		boolean Done = false;			// Loop termination flag


		if (em != null)
		{
			// Now we create the SM status and message panel
			// Note that we set up two indicators that are initially yellow. This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the other

			mw = new MessageWindow("Service Maintenance Monitoring Console", 0, 0);
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

				HashMap<Long, String> elementsConnected = new HashMap<Long, String>();
				this.Ping();
				
				// If there are messages in the queue, we read through them.
				// We are looking for MessageIDs = 1 or 2. Message IDs of 1 are temperature
				// readings from the temperature sensor; message IDs of 2 are humidity sensor
				// readings. Note that we get all the messages at once... there is a 1
				// second delay between samples,.. so the assumption is that there should
				// only be a message at most. If there are more, it is the last message
				// that will effect the status of the temperature and humidity controllers
				// as it would in reality.

				int qlen = eq.GetSize();

				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();
					try {
						if (!MessageEncryptor.isGranted(Msg))
						{
							mw.WriteMessage("Intrusion!!!");
							continue;
						}
					} catch (Exception e1) {
						continue;
					}

					if (Msg.GetMessageId() == -66)
					{
						elementsConnected.put(Msg.GetSenderId(), Msg.GetMessage());
						//mw.WriteMessage("Value received " + Msg.GetSenderId() + "Desc " + Msg.GetMessage());
					}

					if (Msg.GetMessageId() == 66)
					{
						//mw.WriteMessage("Received ping message" );

						// Confirm that the message was recieved and acted on

						AckMessage( em, senderDesc );

					} // try

					// If the message ID == 99 then this is a signal that the simulation
					// is to end. At this point, the loop termination flag is set to
					// true and this process unregisters from the message manager.

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

						//hi.dispose();
						//ti.dispose();

					} // if

				} // for

				try{
					Set<Long> keySetOld = deviceHistory.keySet();
					Set<Long> keySetNew = elementsConnected.keySet();
					for (Long k : keySetNew)
					{
						if(!keySetOld.contains(k)){
							mw.WriteMessage(elementsConnected.get(k) + " device connected!");
						}
					}
					for (Long k : keySetOld)
					{
						if(!keySetNew.contains(k)){
							mw.WriteMessage(deviceHistory.get(k) + " device disconnected!");
						}
					}
				} catch (NullPointerException np) {
					mw.WriteMessage("error comparing device lists");
				}
				deviceHistory = elementsConnected;

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

	/***************************************************************************
	 * CONCRETE METHOD:: IsRegistered
	 * Purpose: This method returns the registered status
	 *
	 * Arguments: none
	 *
	 * Returns: boolean true if registered, false if not registered
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	public boolean IsRegistered()
	{
		return( Registered );

	} // SetTemperatureRange


	/***************************************************************************
	 * CONCRETE METHOD:: Halt
	 * Purpose: This method posts an message that stops the environmental control
	 *		   system.
	 *
	 * Arguments: none
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	public void Ping()
	{
		//mw.WriteMessage( "***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***" );

		// Here we create the stop message.

		Message msg;

		msg = new Message( (int) 66, "XXX" );

		// Here we send the message to the message manager.

		try
		{
			msg.SetEncryptedToken(MessageEncryptor.encrypt(String.valueOf(msg.GetMessageId())));
			em.SendMessage( msg );
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} // try

		catch (Exception e)
		{
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	static private void AckMessage(MessageManagerInterface ei, String m )
	{
		// Here we create the message.

		Message msg = new Message( (int) -66, m );

		// Here we send the message to the message manager.

		try
		{
			msg.SetEncryptedToken(MessageEncryptor.encrypt(String.valueOf(msg.GetMessageId())));
			ei.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error Confirming Message:: " + e);

		} // catch

	} // AckMessage

	public HashMap<Long, String> PrintActiveDevices() {
		return deviceHistory;	
	}

} // ECSMonitor