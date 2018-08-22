/**
 * Receiver.java
 *
 * THIS SAMPLE APPLICATION AND INFORMATION IS PROVIDED "AS IS" WITHOUT WARRANTY OF
 * ANY KIND BY CISCO, EITHER EXPRESSED OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY FITNESS FOR A PARTICULAR PURPOSE,
 * NONINFRINGEMENT, SATISFACTORY QUALITY OR ARISING FROM A COURSE OF DEALING, LAW,
 * USAGE, OR TRADE PRACTICE.  CISCO TAKES NO RESPONSIBILITY REGARDING ITS USAGE IN AN
 * APPLICATION, AND IT IS PRESENTED ONLY AS AN EXAMPLE.  THE SAMPLE CODE HAS NOT BEEN
 * THOROUGHLY TESTED AND IS PROVIDED AS AN EXAMPLE ONLY, THEREFORE CISCO DOES NOT
 * GUARANTEE OR MAKE ANY REPRESENTATIONS REGARDING ITS RELIABILITY, SERVICEABILITY,
 * OR FUNCTION.  IN NO EVENT DOES CISCO WARRANT THAT THE SOFTWARE IS ERROR FREE OR THAT
 * CUSTOMER WILL BE ABLE TO OPERATE THE SOFTWARE WITHOUT PROBLEMS OR INTERRUPTIONS.
 * NOR DOES CISCO WARRANT THAT THE SOFTWARE OR ANY EQUIPMENT ON WHICH THE SOFTWARE IS
 * USED WILL BE FREE OF VULNERABILITY TO INTRUSION OR ATTACK.  THIS SAMPLE APPLICATION
 * IS NOT SUPPORTED BY CISCO IN ANY MANNER. CISCO DOES NOT ASSUME ANY LIABILITY ARISING
 * FROM THE USE OF THE APPLICATION. FURTHERMORE, IN NO EVENT SHALL CISCO OR ITS SUPPLIERS
 * BE LIABLE FOR ANY INCIDENTAL OR CONSEQUENTIAL DAMAGES, LOST PROFITS, OR LOST DATA,
 * OR ANY OTHER INDIRECT DAMAGES EVEN IF CISCO OR ITS SUPPLIERS HAVE BEEN INFORMED OF THE
 * POSSIBILITY THEREOF.
 */

import javax.telephony.*;
import javax.telephony.events.*;
import javax.telephony.callcontrol.*;
import javax.telephony.callcontrol.events.*;

public class Receiver extends Actor
{
	Address		address;
	StopSignal	stopSignal;
	Originator	originator;

	public Receiver ( Address address, Trace trace, int actionDelayMillis, Originator originator ) {
		super ( trace, address, actionDelayMillis );
		this.address = address;
		this.originator = originator;
	}

	protected final void metaEvent ( CallEv [] eventList ) {
		for ( int i = 0; i < eventList.length; i++ ) {
			TerminalConnection tc = null;
			try {
				CallEv curEv = eventList[i];

				if ( curEv instanceof CallCtlTermConnRingingEv ) {
					tc = ((CallCtlTermConnRingingEv)curEv).getTerminalConnection ();
					delay ( "answering" );
					bufPrintln ( "Answering TerminalConnection " + tc );
					tc.answer ();
					stopSignal.canStop ();
				}
			}
			catch ( Exception e ) {
				bufPrintln ( "Caught exception " + e );
				bufPrintln ( "tc = " + tc );
			}
			finally {
				flush ();
			}
		}
	}

	protected final void onStart () {
		stopSignal = new StopSignal ();
	}

	protected final void onStop () {
		stopSignal.stop ();
		Connection[] connections = address.getConnections ();
		try {
			if ( connections != null ) {
				for (int i=0; i< connections.length; i++ ) {
					connections[i].disconnect ();
				}
			}
		}catch ( Exception e ) {
			println (" Caught Exception " + e);
		}
	}

	protected final void fireStateChanged () {
		originator.setReceiverState ( state );
	}
}
