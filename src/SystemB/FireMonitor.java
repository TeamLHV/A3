/******************************************************************************************************************
* File:FireMonitor.java
* Course: 17655
* Project: Assignment A3
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
******************************************************************************************************************/
package SystemB;

import InstrumentationPackage.Indicator;
import InstrumentationPackage.MessageWindow;
import MessagePackage.Message;
import MessagePackage.MessageManagerInterface;
import MessagePackage.MessageQueue;
import SecurityPackage.MessageEncryptor;

class FireMonitor extends Thread {
	private FireConsole console;
	private MessageManagerInterface em = null;
	private String MsgMgrIP = null; // Message Manager IP address
	boolean Registered = true; // Signifies that this class is registered with
								// an message manager.
	MessageWindow mw = null; // This is the message window
	Indicator fi; // Fire alarm indicator
	Indicator si; // Sprinkler indicator
	private Message pendingMsg = null;
	private int noSensorMessage = 0;
	String senderDesc = "FireMonitor";

	public FireMonitor(FireConsole console) {
		// message manager is on the local system

		try {
			// Here we create an message manager interface object. This assumes
			// that the message manager is on the local machine
			em = new MessageManagerInterface();
		} catch (Exception e) {
			System.out.println("FireMonitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

		this.console = console;

	} // Constructor

	public FireMonitor(String MsgIpAddress, FireConsole console) {
		// message manager is not on the local system

		MsgMgrIP = MsgIpAddress;

		try {
			// Here we create an message manager interface object. This assumes
			// that the message manager is NOT on the local machine
			em = new MessageManagerInterface(MsgMgrIP);
		}

		catch (Exception e) {
			System.out.println("Fire Monitor::Error instantiating message manager interface: " + e);
			Registered = false;

		} // catch

		this.console = console;

	} // Constructor

	public void run() {
		Message Msg = null; // Message object
		MessageQueue eq = null; // Message Queue
		int Delay = 1000; // The loop delay (1 second)
		boolean Done = false; // Loop termination flag
		boolean turnOnSprinkler = false;
		boolean isOnFire = false;

		if (em != null) {
			// Now we create the ECS status and message panel
			// Note that we set up two indicators that are initially yellow.
			// This is
			// because we do not know if the temperature/humidity is high/low.
			// This panel is placed in the upper left hand corner and the status
			// indicators are placed directly to the right, one on top of the
			// other

			mw = new MessageWindow("Fire Monitoring Console", 0, 0);
			fi = new Indicator("FIRE UNK", mw.GetX() + mw.Width(), 0);
			si = new Indicator("SPKL UNK", mw.GetX() + mw.Width(), (int) (mw.Height() / 2), 2);

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
				noSensorMessage++;
				// Here we get our message queue from the message manager

				try {
					eq = em.GetMessageQueue();

				} // try

				catch (Exception e) {
					mw.WriteMessage("Error getting message queue::" + e);

				} // catch

				// If there are messages in the queue, we read through them.
				// We are looking for MessageID = 4.
				int qlen = eq.GetSize();

				isOnFire = false;
				for (int i = 0; i < qlen; i++) {
					Msg = eq.GetMessage();
					
					if (!MessageEncryptor.isGranted(Msg)) {
						mw.WriteMessage("Unknown message detected! Ignored.");
						continue;
					}

					if (Msg.GetMessageId() == Constant.MESSAGE_ID_SPRINKLER_CONFIRM) {
						// if confirmation is received, clear pending message.
						pendingMsg = null;

						if (Msg.GetMessage().equals("S1")) {
							console.setSprinklerStatus(true);
							si.SetLampColorAndMessage("SPKL ON", 1);
							fi.SetLampColorAndMessage("FIRE OK", 1);

							mw.WriteMessage("Sprinkler is on.");
						}
					}

					if (Msg.GetMessageId() == Constant.MESSAGE_ID_FIREALARM) {
						noSensorMessage = 0;
						if (Msg.GetMessage().equals("F1")) {
							isOnFire = true;
						}
					} // if
					
					if (Msg.GetMessageId() == 66)
					{
						//mw.WriteMessage("Received ping message" );

						// Confirm that the message was recieved and acted on

						AckMessage( em, senderDesc );

					} // try

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

					} // if

				} // for

				if (isOnFire) {
					mw.WriteMessage("Fire alarm detected! Please go to the command window for further action.");
					fi.SetLampColorAndMessage("FIRE ALARM", 3);
					console.reportAlarm();

					int waitTime = 10000; // wait for maximum 10s
					while (waitTime >= 0) {
						if (console.getUserAction().equals("1")) {
							turnOnSprinkler = true;
							break;
						} else if (console.getUserAction().equals("2")) {
							turnOnSprinkler = false;
							break;
						}

						try {
							Thread.sleep(200);
							waitTime -= 200;
						} catch (Exception e) {
							System.out.println("Sleep error:: " + e);
						}
					}

					if (waitTime <= 0) {
						turnOnSprinkler = true; // turn the sprinkler on
												// automatically
					}

					if (turnOnSprinkler) {
						Sprinkler(true);
					}
				}

				// if the user wants to turn the sprinkler off
				if (console.getTurnSprinklerOff()) {
					Sprinkler(false);
					console.setTurnSprinklerOff(false);
					console.setSprinklerStatus(false);
					si.SetLampColorAndMessage("SPKL OFF", 0);
				}

				if (pendingMsg != null) {
					try {
						em.SendMessage(pendingMsg);
						mw.WriteMessage("Sending message...");
					} catch (Exception e) {
						System.out.println("Error sending sprinkler control message::  " + e);
					} // catch
				}

				if (noSensorMessage >= 3) {
					mw.WriteMessage("No sensor detected! Please check the devices.");
				}

				// This delay slows down the sample rate to Delay milliseconds

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
	 * CONCRETE METHOD:: IsRegistered Purpose: This method returns the
	 * registered status
	 *
	 * Arguments: none
	 *
	 * Returns: boolean true if registered, false if not registered
	 *
	 * Exceptions: None
	 *
	 ***************************************************************************/

	public boolean IsRegistered() {
		return (Registered);

	} // SetTemperatureRange

	/***************************************************************************
	 * CONCRETE METHOD:: Halt Purpose: This method posts an message that stops
	 * the environmental control system.
	 *
	 * Arguments: none
	 *
	 * Returns: none
	 *
	 * Exceptions: Posting to message manager exception
	 *
	 ***************************************************************************/

	public void Halt() {
		mw.WriteMessage("***HALT MESSAGE RECEIVED - SHUTTING DOWN SYSTEM***");

		// Here we create the stop message.

		Message msg;

		msg = new Message((int) 99, "XXX");

		// Here we send the message to the message manager.

		try {
			em.SendMessage(msg);

		} // try

		catch (Exception e) {
			System.out.println("Error sending halt message:: " + e);

		} // catch

	} // Halt

	private void Sprinkler(boolean ON) {
		// Here we create the message.

		Message msg;

		if (ON) {
			msg = new Message(Constant.MESSAGE_ID_SPRINKLER, "S1");
		} else {
			msg = new Message(Constant.MESSAGE_ID_SPRINKLER, "S0");
		} // if

		pendingMsg = MessageEncryptor.encryptMsg(msg);

	} // Humidifier
	
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

} // ECSMonitor