/******************************************************************************************************************
* File:TemperatureController.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 March 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description:
*
* This class simulates a device that controls a heater and chiller. It polls the message manager for message ids = 5
* and reacts to them by turning on or off the heater or chiller. The following command are valid strings for con
* trolling the heater and chiller:
*
*	H1 = heater on
*	H0 = heater off
*	C1 = chillerer on
*	C0 = chiller off
*
* The state (on/off) is graphically displayed on the terminal in the indicator. Command messages are displayed in
* the message window. Once a valid command is recieved a confirmation message is sent with the id of -5 and the command in
* the command string.
*
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	static private void ConfirmMessage(MessageManagerInterface ei, String m )
*
******************************************************************************************************************/
import InstrumentationPackage.Indicator;
import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;
import SecurityPackage.MessageEncryptor;

class SecurityController
{
	public static void main(String args[])
	{
		String MsgMgrIP;					// Message Manager IP address
		Message Msg = null;					// Message object
		MessageQueue eq = null;				// Message Queue
		MessageManagerInterface em = null;	// Interface object to the message manager
		boolean doorSecurityState = false;		// Heater state: false == off, true == on
		boolean windowSecurityState = false;
		boolean motionSecurityState = false;
		boolean allAlarms = false;
		int	Delay = 1000;					// The loop delay (1 seconds)
		boolean Done = false;				// Loop termination flag
		String senderDesc = "SecurityController";

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length == 0 )
 		{
			// message manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine..." );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is on the local machine

				em = new MessageManagerInterface();
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {

			// message manager is not on the local system

			MsgMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP );

			try
			{
				// Here we create an message manager interface object. This assumes
				// that the message manager is NOT on the local machine

				em = new MessageManagerInterface( MsgMgrIP );
			}

			catch (Exception e)
			{
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (em != null)
		{
			System.out.println("Registered with the message manager." );

			/* Now we create the temperature control status and message panel
			** We put this panel about 1/3 the way down the terminal, aligned to the left
			** of the terminal. The status indicators are placed directly under this panel
			*/

			float WinPosX = 0.0f; 	//This is the X position of the message window in terms
								 	//of a percentage of the screen height
			float WinPosY = 0.3f; 	//This is the Y position of the message window in terms
								 	//of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Security Controller Status Console", WinPosX, WinPosY);

			// Put the status indicators under the panel...

			Indicator di = new Indicator ("D_Alarm", mw.GetX(), mw.GetY()+mw.Height());
			Indicator wi = new Indicator ("W_Alarm", mw.GetX()+(di.Width()*2), mw.GetY()+mw.Height());
			Indicator mi = new Indicator ("M_Alarm", mw.GetX()+(wi.Width()*2), mw.GetY()+mw.Height());

			mw.WriteMessage("Registered with the message manager." );

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
					if (!MessageEncryptor.isGranted(Msg)) {
						mw.WriteMessage("Unknown message detected! Ignored.");
						continue;
					}
					if ( Msg.GetMessageId() == 14 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("D1"))
						{
							doorSecurityState = true;
							allAlarms = false;
							mw.WriteMessage("Door Security Alarm message!!!" );
						} // if
					}
					
					if ( Msg.GetMessageId() == 15 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("W1"))
						{
							windowSecurityState = true;
							allAlarms = false;
							mw.WriteMessage("Window Security Alarm message!!!" );

						} // if
					}
					if ( Msg.GetMessageId() == 16 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("M1"))
						{
							motionSecurityState = true;
							allAlarms = false;
							mw.WriteMessage("Motion Security Alarm message!!!" );

						} // if
					}
					if ( Msg.GetMessageId() == 17 )
					{
						if (Msg.GetMessage().equalsIgnoreCase("AA"))
						{
							allAlarms = true;
							mw.WriteMessage("All alarms idle..." );
	
						} // if
					}
					
					if ( Msg.GetMessageId() == 66 )
					{
						mw.WriteMessage("Received ping message" );

						// Confirm that the message was recieved and acted on

						AckMessage( em, senderDesc );

					} // try
					
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

						di.dispose();
						wi.dispose();
						mi.dispose();

					} // if
					// Update the lamp status

				} // for
				try{
					if (doorSecurityState)
					{
						di.SetLampColorAndMessage("D_Alarm", 3);
						Message msg = new Message(-14,"D1_ACK");
						em.SendMessage(MessageEncryptor.encryptMsg(msg));
					} 
					if (windowSecurityState)
					{
						wi.SetLampColorAndMessage("W_Alarm", 3);
						Message msg = new Message(-15,"W1_ACK");
						em.SendMessage(MessageEncryptor.encryptMsg(msg));
					} 
					if (motionSecurityState)
					{
						mi.SetLampColorAndMessage("M_Alarm", 3);
						Message msg = new Message(-16,"M1_ACK");
						em.SendMessage(MessageEncryptor.encryptMsg(msg));
					}
					if(allAlarms){
						doorSecurityState = false;
						windowSecurityState = false;
						motionSecurityState = false;
						di.SetLampColorAndMessage("DS Idle", 0);
						wi.SetLampColorAndMessage("WS Idle", 0);
						mi.SetLampColorAndMessage("MS Idle", 0);
					}
				}catch(Exception e){
					System.out.println("Error while sending message");
				}
				try
				{
					Thread.sleep( Delay );

				} // try

				catch( Exception e )
				{
					System.out.println( "Sleep error:: " + e );

				} // catch
			} // while
		}
	}// main
	
	static private void AckMessage(MessageManagerInterface ei, String m )
	{
		// Here we create the message.

		Message msg = new Message( (int) -66, m );

		// Here we send the message to the message manager.

		try
		{
			ei.SendMessage( msg );

		} // try

		catch (Exception e)
		{
			System.out.println("Error Confirming Message:: " + e);

		} // catch

	} // AckMessage

} // TemperatureController