package SystemB;

import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;
import TermioPackage.Termio;

public class FireSensor {

	public static void main(String args[]) {
		Termio UserInput = new Termio();
		String MsgMgrIP;
		Message Msg = null;
		MessageQueue eq = null;
		MessageManagerInterface em = null;
		boolean Done = false; // Loop termination flag

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

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (em != null) {

			// We create a message window. Note that we place this panel about
			// 1/2 across
			// and 2/3s down the screen

			float WinPosX = 0.5f; // This is the X position of the message
									// window in terms
									// of a percentage of the screen height
			float WinPosY = 0.60f; // This is the Y position of the message
									// window in terms
									// of a percentage of the screen height

			MessageWindow mw = new MessageWindow("Fire Sensor", WinPosX, WinPosY);

			mw.WriteMessage("Registered with the message manager.");

			try {
				mw.WriteMessage("   Participant id: " + em.GetMyId());
				mw.WriteMessage("   Registration Time: " + em.GetRegistrationTime());

			} // try

			catch (Exception e) {
				mw.WriteMessage("Error:: " + e);

			} // catch

			mw.WriteMessage("\nInitializing Fire Simulation::");

			/********************************************************************
			 ** Here we start the main simulation loop
			 *********************************************************************/

			mw.WriteMessage("Beginning Simulation... ");

			while (!Done) {
				
				System.out.println("\n\n\n\nSelect an option:");
				System.out.println("1. Simulate fire alarm");
//				System.out.println("2. Stop simulating");
				
				String cmd = UserInput.KeyboardReadString();

				if (cmd.equalsIgnoreCase("1")) {

					PostFireAlarm(em);

					mw.WriteMessage("Fire alarm!");
				}

				// if the message id == 99 then this is a signal that the
				// simulation
				// is to end. At this point, the loop termination flag is
				// set to
				// true and this process unregisters from the message
				// manager.
				try {
					eq = em.GetMessageQueue();

				} // try

				catch (Exception e) {
					mw.WriteMessage("Error getting message queue::" + e);

				} // catch

				int qlen = eq.GetSize();
				for (int i = 0; i < qlen; i++) {
					Msg = eq.GetMessage();
					if (Msg.GetMessageId() == 99) {
						Done = true;
						try {
							em.UnRegister();

						} // try
						catch (Exception e) {
							mw.WriteMessage("Error unregistering: " + e);

						} // catch
						mw.WriteMessage("\n\nSimulation Stopped. \n");
					} // if
				} // for
			} // while
		} else {
			System.out.println("Unable to register with the message manager.\n\n");
		} // if

	} // main

	/***************************************************************************
	 * CONCRETE METHOD:: PostHumidity Purpose: This method posts the specified
	 * relative humidity value to the specified message manager. This method
	 * assumes an message ID of 2.
	 *
	 * Arguments: MessageManagerInterface ei - this is the messagemanger
	 * interface where the message will be posted.
	 *
	 * float humidity - this is the humidity value.
	 *
	 * Returns: none
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	static private void PostFireAlarm(MessageManagerInterface ei) {
		// Here we create the message.

		Message msg = new Message((int) Constant.MESSAGE_ID_FIREALARM, "");

		// Here we send the message to the message manager.

		try {
			ei.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error Posting Relative Fire Alarm:: " + e);

		} // catch

	} // PostHumidity

}
