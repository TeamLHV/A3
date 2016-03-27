/******************************************************************************************************************
* File:ECSConsole.java
* Course: 17655
* Project: Assignment 3
* Copyright: Copyright (c) 2009 Carnegie Mellon University
* Versions:
*	1.0 February 2009 - Initial rewrite of original assignment 3 (ajl).
*
* Description: This class is the console for the museum environmental control system. This process consists of two
* threads. The ECSMonitor object is a thread that is started that is responsible for the monitoring and control of
* the museum environmental systems. The main thread provides a text interface for the user to change the temperature
* and humidity ranges, as well as shut down the system.
*
* Parameters: None
*
* Internal Methods: None
*
******************************************************************************************************************/
import TermioPackage.Termio;

public class SecurityConsole
{
	public static void main(String args[])
	{
    	Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		SecurityMonitor monitor = null;			// The environmental control system monitor

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

 		if ( args.length != 0 )
 		{
			// message manager is not on the local system

			monitor = new SecurityMonitor( args[0] );

		} else {

			monitor = new SecurityMonitor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (monitor.IsRegistered() )
		{
			monitor.start(); // Here we start the monitoring and control thread

			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "\n\n\n\n" );
				System.out.println( "Security Command Console: \n" );

				if (args.length != 0)
					System.out.println( "Using message manger at: " + args[0] + "\n" );
				else
					System.out.println( "Using local message manger \n" );

				System.out.println( "Select an Option: \n" );
				System.out.println( "1: To Arm Security Systems" );
				System.out.println( "2: To Disarm Security Systems" );
				System.out.println( "X: Stop System\n" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////

				if ( Option.equals( "1" ) )
				{
					monitor.setSecurityArmed(true);
					System.out.println( "All the Security Systems are Armed" );
				} // if

				//////////// option 2 ////////////

				if ( Option.equals( "2" ) )
				{
					monitor.setSecurityArmed(false);
					System.out.println( "All the Security Systems are DisArmed" );
				} // if

				//////////// option X ////////////

				if ( Option.equalsIgnoreCase( "X" ) )
				{
					// Here the user is done, so we set the Done flag and halt
					// the environmental control system. The monitor provides a method
					// to do this. Its important to have processes release their queues
					// with the message manager. If these queues are not released these
					// become dead queues and they collect messages and will eventually
					// cause problems for the message manager.

					monitor.Halt();
					Done = true;
					System.out.println( "\nConsole Stopped... Exit monitor mindow to return to command prompt." );
					monitor.Halt();

				} // if

			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

  	} // main

} // SecurityConsole
