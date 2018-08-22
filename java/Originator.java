/**
 * originator.java
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

//import com.ms.com.*;
import com.cisco.jtapi.extensions.*;

public class Originator extends Actor
{
	Address		srcAddress;
	String		destAddress;
	int			iteration;
	StopSignal	stopSignal;
	boolean		ready = false;
	int			receiverState = Actor.ACTOR_OUT_OF_SERVICE;
	boolean callInIdle = true;

	public Originator ( Address srcAddress, String destAddress, Trace trace, int actionDelayMillis ) {
		super ( trace, srcAddress, actionDelayMillis );	// observe srcAddress
		this.srcAddress = srcAddress;
		this.destAddress = destAddress;
		this.iteration = 0;
	}

	protected final void metaEvent ( CallEv [] eventList ) {
		for ( int i = 0; i < eventList.length; i++ ) {
			try {
				CallEv curEv = eventList[i];

				if ( curEv instanceof CallCtlTermConnTalkingEv ) {
					TerminalConnection tc = ((CallCtlTermConnTalkingEv)curEv).getTerminalConnection ();
					Connection conn = tc.getConnection ();
					if ( conn.getAddress ().getName ().equals ( destAddress ) ) {
						delay ( "disconnecting" );
						bufPrintln ( "Disconnecting Connection " + conn );
						conn.disconnect ();
					}
				}
				else if ( curEv instanceof CallCtlConnDisconnectedEv ) {
					Connection conn = ((CallCtlConnDisconnectedEv)curEv).getConnection ();
					if ( conn.getAddress ().equals ( srcAddress ) ) {
						stopSignal.canStop ();
						setCallProgressState ( true );
					}
				}
			}
			catch ( Exception e ) {
				println ( "Caught exception " + e );
			}
			finally {
				flush ();
			}

		}
	}

	protected void makecall ()
		throws ResourceUnavailableException, InvalidStateException,
			   PrivilegeViolationException, MethodNotSupportedException,
			   InvalidPartyException, InvalidArgumentException {
			println ( "Making call #" + ++iteration + " from " + srcAddress + " to " + destAddress +  " " + Thread.currentThread ().getName () );
			Call call = srcAddress.getProvider ().createCall ();
			call.connect ( srcAddress.getTerminals ()[0], srcAddress, destAddress );
			setCallProgressState ( false );
			println ( "Done making call" );
	}


	protected final void onStart () {
		stopSignal = new StopSignal ();
		new ActionThread ().start ();
	}

	protected final void fireStateChanged () {
		checkReadyState ();
	}

	protected final void onStop () {
		stopSignal.stop ();
		Connection[] connections = srcAddress.getConnections ();
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

	public int getReceiverState () {
		return receiverState;
	}

	public void setReceiverState ( int state ) {
		if ( receiverState != state ){
			 receiverState = state;
			 checkReadyState ();
		}
	}



	public synchronized void checkReadyState () {
		if ( receiverState == Actor.ACTOR_IN_SERVICE && state == Actor.ACTOR_IN_SERVICE ) {
			ready = true;
		}else {
			ready = false;
		}
		notifyAll ();
	}

	public synchronized void setCallProgressState ( boolean isCallInIdle ) {
		callInIdle = isCallInIdle;
		notifyAll ();
	}

	public synchronized void doAction () {
		if ( !ready || !callInIdle ) {
			try {
				wait ();
			}catch ( Exception e ) {
				println (" Caught Exception from wait state" + e );
			}
		} else {
			if ( actionDelayMillis != 0 ) {
				println ( "Pausing " + actionDelayMillis + " milliseconds before making call " );
				flush ();
				try {
					wait ( actionDelayMillis );
				}
				catch ( Exception ex ) {}
			}
			//make call after waking up, recheck the flags before making the call
			if ( ready && callInIdle ) {
				try {
					makecall ();
				}catch ( Exception e ) {
					println (" Caught Exception in MakeCall " + e + " Thread =" + Thread.currentThread ().getName ());
				}
			}
		}
	}


	class ActionThread extends Thread {

		ActionThread ( ) {
			super ( "ActionThread");
		}

		public void run () {
			while ( true ) {
				doAction ();
			}
		}
	}

}
