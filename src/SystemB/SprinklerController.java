/******************************************************************************************************************
* File:FireController.java
* Course: 17655
* Project: Assignment A3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
*
* Description:
*
* This class simulates a device that controls a sprinkler. It polls the message manager for message
* ids = 6 and reacts to them by turning on or off the sprinkler. 
* Parameters: IP address of the message manager (on command line). If blank, it is assumed that the message manager is
* on the local machine.
*
* Internal Methods:
*	static private void ConfirmMessage(MessageManagerInterface ei, String m )
*
******************************************************************************************************************/
package SystemB;

import InstrumentationPackage.Indicator;
import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;
import SecurityPackage.MessageEncryptor;

class SprinklerController {
	public static void main(String args[]) {
		String MsgMgrIP; // Message Manager IP address
		Message Msg = null; // Message object
		MessageQueue eq = null; // Message Queue
		MessageManagerInterface em = null;
		int Delay = 1000; // The loop delay (1 seconds)
		boolean Done = false; // Loop termination flag
		boolean SprinklerOn = false;

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

		if (args.length == 0) {
			// message manager is on the local system

			System.out.println("\n\nAttempting to register on the local machine...");

			try {
				// Here we create an message manager interface object. This
				// assumes
				// that the message manager is on the local machine

				em = new MessageManagerInterface();
			}

			catch (Exception e) {
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} else {

			// message manager is not on the local system

			MsgMgrIP = args[0];

			System.out.println("\n\nAttempting to register on the machine:: " + MsgMgrIP);

			try {
				// Here we create an message manager interface object. This
				// assumes
				// that the message manager is NOT on the local machine

				em = new MessageManagerInterface(MsgMgrIP);
			}

			catch (Exception e) {
				System.out.println("Error instantiating message manager interface: " + e);

			} // catch

		} // if

		// Here we check to see if registration worked. If em is null then the
		// message manager interface was not properly created.

		if (em != null) {
			System.out.println("Registered with the message manager.");

			/*
			 * Now we create the humidity control status and message panel We
			 * put this panel about 2/3s the way down the terminal, aligned to
			 * the left of the terminal. The status indicators are placed
			 * directly under this panel
			 */

			float WinPosX = 0.0f; // This is the X position of the message
									// window in terms
									// of a percentage of the screen height
			float WinPosY = 0.60f; // This is the Y position of the message
									// window in terms
									// of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Humidity Controller Status Console", WinPosX, WinPosY);

			// Now we put the indicators directly under the humitity status and
			// control panel

			Indicator si = new Indicator("Sprinkler OFF", mw.GetX(), mw.GetY() + mw.Height());

			mw.WriteMessage("Registered with the message manager.");

			try {
				mw.WriteMessage("   Participant id: " + em.GetMyId());
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime());

			} // try

			catch (Exception e) {
				System.out.println("Error:: " + e);

			} // catch

			/********************************************************************
			 ** Here we start the main simulation loop
			 *********************************************************************/

			while (!Done) {
				try {
					eq = em.GetMessageQueue();

				} // try

				catch (Exception e) {
					mw.WriteMessage("Error getting message queue::" + e);

				} // catch

				int qlen = eq.GetSize();

				for (int i = 0; i < qlen; i++) {
					Msg = eq.GetMessage();

					if (!MessageEncryptor.isGranted(Msg)) {
						mw.WriteMessage("Unknown message detected! Ignored.");
						continue;
					}
					if (Msg.GetMessageId() == Constant.MESSAGE_ID_SPRINKLER) {
						if (Msg.GetMessage().equalsIgnoreCase("S1")) // on
						{
							SprinklerOn = true;
							mw.WriteMessage("Received sprinkler on message");

							ConfirmMessage(em, "S1");
						} // if

						if (Msg.GetMessage().equalsIgnoreCase("S0")) // off
						{
							SprinklerOn = false;
							mw.WriteMessage("Received sprinkler off message");

							ConfirmMessage(em, "S0");
						} // if

					} // if

					// If the message ID == 99 then this is a signal that the
					// simulation
					// is to end. At this point, the loop termination flag is
					// set to
					// true and this process unregisters from the message
					// manager.

					if (Msg.GetMessageId() == 99) {
						Done = true;

						try {
							em.UnRegister();

						} // try

						catch (Exception e) {
							mw.WriteMessage("Error unregistering: " + e);

						} // catch

						mw.WriteMessage("\n\nSimulation Stopped. \n");

						// Get rid of the indicators. The message panel is left
						// for the
						// user to exit so they can see the last message posted.

						si.dispose();

					} // if

				} // for

				// Update the lamp status

				if (SprinklerOn) {
					// Set to green, sprinkler is on

					si.SetLampColorAndMessage("Sprinkler ON", 1);

				} else {

					// Set to black, sprinkler is off
					si.SetLampColorAndMessage("Sprinkler OFF", 0);

				} // if

				try {
					Thread.sleep(Delay);

				} // try

				catch (Exception e) {
					System.out.println("Sleep error:: " + e);

				} // catch

			} // while

		} else {

			System.out.println("Unable to register with the message manager.\n\n");

		} // if

	} // main

	/***************************************************************************
	 * CONCRETE METHOD:: ConfirmMessage Purpose: This method posts the specified
	 * message to the specified message manager. This method assumes an message
	 * ID of -4 which indicates a confirma- tion of a command.
	 *
	 * Arguments: MessageManagerInterface ei - this is the messagemanger
	 * interface where the message will be posted.
	 *
	 * string m - this is the received command.
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private void ConfirmMessage(MessageManagerInterface ei, String m) {
		// Here we create the message.

		Message msg = new Message(Constant.MESSAGE_ID_SPRINKLER_CONFIRM, m);

		// Here we send the message to the message manager.

		try {
			ei.SendMessage(MessageEncryptor.encryptMsg(msg));

		} // try

		catch (Exception e) {
			System.out.println("Error Confirming Message:: " + e);

		} // catch

	} // PostMessage

} // HumidityControllers