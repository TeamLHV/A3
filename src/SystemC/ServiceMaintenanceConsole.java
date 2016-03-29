package SystemC;

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
import TermioPackage.*;

import java.util.HashMap;

import MessagePackage.*;

public class ServiceMaintenanceConsole
{
	public static void main(String args[])
	{
		Termio UserInput = new Termio();	// Termio IO Object
		boolean Done = false;				// Main loop flag
		String Option = null;				// Menu choice from user
		Message Msg = null;					// Message object
		boolean Error = false;				// Error flag
		ServiceMaintenanceMonitor Monitor = null;			// The environmental control system monitor

		/////////////////////////////////////////////////////////////////////////////////
		// Get the IP address of the message manager
		/////////////////////////////////////////////////////////////////////////////////

		if ( args.length != 0 )
		{
			// message manager is not on the local system

			Monitor = new ServiceMaintenanceMonitor( args[0] );

		} else {

			Monitor = new ServiceMaintenanceMonitor();

		} // if


		// Here we check to see if registration worked. If ef is null then the
		// message manager interface was not properly created.

		if (Monitor.IsRegistered() )
		{
			Monitor.start(); // Here we start the monitoring and control thread
			System.out.println( "\n\n\n\n" );
			System.out.println( "Service Maintenance (SM) Command Console: \n" );

			if (args.length != 0)
				System.out.println( "Using message manger at: " + args[0] + "\n" );
			else
				System.out.println( "Using local message manger \n" );

			while (!Done)
			{
				// Here, the main thread continues and provides the main menu

				System.out.println( "Select an Option: \n" );
				System.out.println( "1: Get active devices" );
				System.out.println( "X: Stop System\n" );
				System.out.print( "\n>>>> " );
				Option = UserInput.KeyboardReadString();

				//////////// option 1 ////////////
				if ( Option.equalsIgnoreCase( "1" ) )
				{	
					System.out.println("Devices connected:\n");
					HashMap<Long, String> activeDecices = Monitor.PrintActiveDevices();
					for (Long k : activeDecices.keySet())
					{
						System.out.println(activeDecices.get(k));
					} 
					System.out.println();
				}

				try {
					Thread.sleep(1000);                 //1000 milliseconds is one second.
				} catch(InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
				
				//////////// option X ////////////

				if ( Option.equalsIgnoreCase( "X" ) )
				{
					// Here the user is done, so we set the Done flag and halt
					// the environmental control system. The monitor provides a method
					// to do this. Its important to have processes release their queues
					// with the message manager. If these queues are not released these
					// become dead queues and they collect messages and will eventually
					// cause problems for the message manager.

					Done = true;
					System.out.println( "\nConsole Stopped... Exit monitor mindow to return to command prompt." );

				} // if


				//} // if
			} // while

		} else {

			System.out.println("\n\nUnable start the monitor.\n\n" );

		} // if

	} // main

} // SMConsole
