/**
 * makecall.java
 * 27.08.2018 08:57
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

//import com.ms.wfc.app.*;
import java.util.*;
import javax.telephony.ProviderObserver;
import javax.telephony.*;
import javax.telephony.events.*;
import com.cisco.cti.util.Condition;

public class makecall extends TraceWindow implements ProviderObserver
{
	Vector		actors = new Vector ();
	Condition	conditionInService = new Condition ();
	Provider	provider;

	public makecall ( String [] args ) {

		super ( "makecall" + ": "+ new CiscoJtapiVersion());
		try {

			System.out.println ( "Initializing Jtapi" );
			int curArg = 0;
			String providerName = args[curArg++];
			String login = args[curArg++];
			String passwd = args[curArg++];
			int actionDelayMillis = Integer.parseInt ( args[curArg++] );
			String src = null;
			String dest = null;

			JtapiPeer peer = JtapiPeerFactory.getJtapiPeer ( null );
			if ( curArg < args.length ) {

				String providerString = providerName + ";login=" + login + ";passwd=" + passwd;
				System.out.println ( "Opening " + providerString + "...\n" );
				provider = peer.getProvider ( providerString );
				provider.addObserver ( this );
				conditionInService.waitTrue ();

				System.out.println ( "Constructing actors" );

				for ( ; curArg < args.length; curArg++ ) {
					if ( src == null ) {
						src = args[curArg];
					}
					else {
						dest = args[curArg];
						Originator originator = new Originator ( provider.getAddress ( src ), dest, (Trace)this, actionDelayMillis );
						actors.addElement ( originator );
						actors.addElement (
							new Receiver ( provider.getAddress ( dest ), (Trace)this, actionDelayMillis, originator )
							);
						src = null;
						dest = null;
					}
				}
				if ( src != null ) {
					System.out.println ( "Skipping last originating address \"" + src + "\"; no destination specified" );
				}

			}

			Enumeration e = actors.elements ();
			while ( e.hasMoreElements () ) {
				Actor actor = (Actor) e.nextElement ();
				actor.initialize ();
			}

			Enumeration en = actors.elements ();
			while ( en.hasMoreElements () ) {
				Actor actor = (Actor) en.nextElement ();
				actor.start ();
			}
		}
		catch ( Exception e ) {
			System.out.println( "Caught exception " + e );
		}
	}

	public void dispose () {
		System.out.println ( "Stopping actors" );
		Enumeration e = actors.elements ();
		while ( e.hasMoreElements () ) {
			Actor actor = (Actor) e.nextElement ();
			actor.dispose ();
		}
	}

	public static void main ( String [] args )
	{
		if ( args.length < 6 ) {
			System.out.println ( "Usage: makecall <server> <login> <password> <delay> <origin> <destination> ..." );
			System.exit ( 1 );
		}
		new makecall ( args );
	}

	public void providerChangedEvent ( ProvEv [] eventList ) {
		if ( eventList != null ) {
			for ( int i = 0; i < eventList.length; i++ )
			{
				if ( eventList[i] instanceof ProvInServiceEv ) {
					conditionInService.set ();
				}
			}
		}
	}
}

