/******************************************************************************************************************
* File:TemperatureSensor.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a temperature sensor. It polls the message manager for messages corresponding to changes in state
* of the heater or chiller and reacts to them by trending the ambient temperature up or down. The current ambient
* room temperature is posted to the message manager.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	float GetRandomNumber()
*	boolean CoinToss()
*   void PostTemperature(MessageManagerInterface ei, float temperature )
*
******************************************************************************************************************/
import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;

class DoorBreakSecuritySensor
{
	public static void main(String args[])
	{
		String MsgMgrIP;				// Message Manager IP address
		Message Msg = null;				// Message object
		MessageQueue eq = null;			// Message Queue
		int MsgId = 0;					// User specified message ID
		MessageManagerInterface em = null;// Interface object to the message manager
		boolean isDoorSecure = false;		// Current simulated ambient room temperature
		int	Delay = 1000;				// The loop delay (2.5 seconds)
		boolean Done = false;			// Loop termination flag

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			System.out.println("\n\nAttempting to register on the local machine..." );
			try
			{
				em = new MessageManagerInterface();
			}
			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {
			MsgMgrIP = args[0];
			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );
			try
			{
				em = new MessageManagerInterface( MsgMgrIP );
			}
			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch
		} // if

		if (em != null)
		{
			float WinPosX = 0.5f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Door Security Sensor", WinPosX, WinPosY );

			mw.WriteMessage("Registered with the message manager." );

	    	try
	    	{
				mw.WriteMessage("   Participant id: " + em.GetMyId() );
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime() );

			} // try

	    	catch (Exception e)
			{
				mw.WriteMessage("Error:: " + e);

			} // catch

			mw.WriteMessage("\nInitializing Door Security Simulation::" );



			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");

			int count = 0;
			while ( !Done )
			{
				try
				{
					Message msg = new Message( (int) 11, "D0");
					String message = "Door safe";
					count++;
					if(count%3==0){
						msg = new Message( (int) 11, "D1");
						message = "Door unsafe..please check";
						count = 0;
					}
					em.SendMessage(msg);
					eq = em.GetMessageQueue();
					mw.WriteMessage(message);

				} // try
				catch( Exception e )
				{
					mw.WriteMessage("Error getting message queue::" + e );

				} // catch
				int qlen = eq.GetSize();
				for ( int i = 0; i < qlen; i++ )
				{
					Msg = eq.GetMessage();
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
				    	mw.WriteMessage("\n\nSimulation Stopped. \n");
					} // if
				} // for
				try
				{
					Thread.sleep( Delay );

				} // try
				catch( Exception e )
				{
					mw.WriteMessage("Sleep error:: " + e );

				} // catch

			} // while
		} else {
			System.out.println("Unable to register with the message manager.\n\n" );
		} // if
	} // main
} // TemperatureSensor