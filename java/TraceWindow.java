/**
 * TraceWindow.java
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


import java.awt.*;
import java.awt.event.*;

public class TraceWindow extends Frame implements Trace
{

	TextArea textArea;
	boolean	traceEnabled = true;
	StringBuffer buffer = new StringBuffer ();

	public TraceWindow (String name ) {
		super ( name );
		initWindow ();
	}

	public TraceWindow(){
		this("");
	}


	private void initWindow() {
		this.addWindowListener(new WindowAdapter () {
			public void windowClosing(WindowEvent e){
				dispose ();
				System.exit(0);
			}
		});
		textArea = new TextArea();
		setSize(400,400);
		add(textArea);
		setEnabled(true);
		this.show();

	}


	public final void bufPrint ( String str ) {
		if ( traceEnabled ) {
			buffer.append ( str );
		}

	}

	public final void print ( String str ) {
		if ( traceEnabled ) {
			buffer.append ( str );
			flush ();
		}
    }
    public final void print ( char character ) {
		if ( traceEnabled ) {
			buffer.append ( character );
			flush ();
		}
    }
    public final void print ( int integer ) {
		if ( traceEnabled ) {
			buffer.append ( integer );
			flush ();
		}
    }
	public final void println ( String str ) {
		if ( traceEnabled ) {
			print ( str );
			print ( '\n' );
			flush ();
		}
    }
    public final void println ( char character ) {
		if ( traceEnabled ) {
			print ( character );
			print ( '\n' );
			flush ();
		}
    }
    public final void println ( int integer ) {
		if ( traceEnabled ) {
			print ( integer );
			print ( '\n' );
			flush ();
		}
    }

    public final void setTrace ( boolean traceEnabled ) {
        this.traceEnabled = traceEnabled;
    }

	public final void flush () {
		if ( traceEnabled ) {
			textArea.append ( buffer.toString());
			buffer = new StringBuffer ();
		}
	}

    public final void clear () {

        textArea.setText("");
    }
}

