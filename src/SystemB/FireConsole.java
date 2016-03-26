package SystemB;

import TermioPackage.Termio;

public class FireConsole {
	private String ip = null;
	private boolean isAlarmOn = false; // indicates the fire alarm is on
	private boolean isSprinklerOn = false; // indicates the sprinkler is on
	private boolean turnSprinklerOff = false; // indicate the user wants to turn
												// the sprinkler off
	private String userAction = "";

	public FireConsole(String string) {
		this.ip = string;
	}

	public FireConsole() {
	}

	public static void main(String[] args) {
		FireConsole Console = null; // The environmental control system console
		FireMonitor Monitor = null;
		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

		if (args.length != 0) {
			// message manager is not on the local system

			Console = new FireConsole(args[0]);
			Monitor = new FireMonitor(args[0], Console);

		} else {

			Console = new FireConsole();
			Monitor = new FireMonitor(Console);

		} // if

		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (Monitor.IsRegistered()) {

			Monitor.start(); // Here we start the monitoring and control
								// thread
			Console.run();

		} else {

			System.out.println("\n\nUnable to start the monitor.\n\n");

		} // if

	}

	private void run() {
		Termio UserInput = new Termio(); // Termio IO Object

		// Here, the main thread continues and provides the main menu

		System.out.println("\n\n\n\n");
		System.out.println("Fire Alarm System (FAS) Command Console: \n");

		if (this.ip != null)
			System.out.println("Using message manger at: " + this.ip + "\n");
		else
			System.out.println("Using local message manger \n");

		System.out.println("Monitoring fire alarm... \n");

		while (true) {

			if (isSprinklerOn) {
				System.out.println("\n\nSprinkler is on!");
				System.out.println("Do you want turn off the sprinkler?");
				System.out.println("Select an Option:");
				System.out.println("1: Yes, turn it off.");
				System.out.println("2: No, leave it on.");

				if (UserInput.KeyboardReadString().equals("1")) {
					turnSprinklerOff = true;
				} else {
					turnSprinklerOff = false;
				}
			} else if (isAlarmOn) {
				System.out.println("Fire alarm detected!");
				System.out.println("\n\nDo you want turn on the sprinkler?");
				System.out.println("(The sprinkler will be turned on automatically after 10 seconds.)");
				System.out.println("Select an Option:");
				System.out.println("1: Confirm");
				System.out.println("2: Cancel");

				userAction = UserInput.KeyboardReadString();
				
				System.out.println("You selected " + userAction + ".");
				isAlarmOn = false;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} // while
	}

	public void reportAlarm() {
		this.isAlarmOn = true;
	}

	public void setSprinklerStatus(boolean on) {
		this.isSprinklerOn = on;
	}
	
	public boolean getTurnSrinklerOff() {
		boolean result = this.turnSprinklerOff;
		this.turnSprinklerOff = false; // revert
		return result;
	}

	public String getUserAction() {
		return userAction;
	}

}
