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
import SecurityPackage.MessageEncryptor;
import TermioPackage.Termio;

class MotionSecuritySensor
{
	
	static boolean Done = false;			// Loop termination flag
	
	public static void main(String args[])
	{
		Termio UserInput = new Termio();	// Termio IO Object
		String Option = null;				// Menu choice from user
		boolean Error = false;				// Error flag
		String MsgMgrIP;				// Message Manager IP address
		MessageQueue eq = null;			// Message Queue
		MessageManagerInterface em = null;// Interface object to the message manager

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

			MessageWindow mw = new MessageWindow("Motion Security Sensor", WinPosX, WinPosY );

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

			mw.WriteMessage("\nInitializing Motion Security Simulation::" );



			/********************************************************************
			** Here we start the main simulation loop
			*********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");
			
			while (!Done)
			{
				System.out.println( "\n\n\n\n" );
				System.out.println( "Motion Detection Manual Simulation Command Console: \n" );

				if (args.length != 0)
					System.out.println( "Using message manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local message manger \n" );

				System.out.println( "Select an Option: \n" );
				System.out.println( "1: Stimulate Motion Break" );
				System.out.println( "X: Stop Stimulation\n" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////

				if ( Option.equals( "1" ) )
				{
					// Here we get the temperature ranges

					Error = true;

					while (Error)
					{
						// Here we get the low temperature range

						while (Error)
						{
							if (UserInput.IsNumber(Option))
							{
								Error = false;
								sendAttackMessage(eq, em, mw);

							} else{

								System.out.println( "Not a number, please try again..." );

							} // if

						} // while
					} // while

				} // if
				else  if("X".equalsIgnoreCase(Option)){
					try {
						Done = true;
						em.UnRegister();
						mw.WriteMessage("\n\nSimulation Stopped");
						System.out.println("Simulation Stopped");
					} catch (Exception e) {
						System.out.println("Error during unregistering");
					}
				}
			} 
		}
	} // main

	private static void sendAttackMessage(MessageQueue eq, MessageManagerInterface em, MessageWindow mw) {
		Message Msg;
		try
		{
			eq = em.GetMessageQueue();
			Message msg = new Message( (int) 13, "M1");
			String message = "Motion Detected..please check";
			em.SendMessage(MessageEncryptor.encryptMsg(msg));
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
				try {
					Done = true;
					em.UnRegister();
					mw.WriteMessage("\n\nSimulation Stopped");
					System.out.println("Simulation Stopped");
				} catch (Exception e) {
					System.out.println("Error during unregistering");
				}
				
			} // if
		} // for
	}
} // TemperatureSensor