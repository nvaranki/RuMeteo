/*
 * Setup.java
 *
 * Created on 2 Март 2007 г., 22:04
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import com.varankin.mobile.PropertyResourceBundle;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStoreException;

/**
 * @author  Nikolai Varankine
 */
public class SetupOptions extends Form
{
    private final static String RKEY_VALID = "Setup.Validity"; // ms
    private final static String PROP_VALID = "Validity"; // hrs
    private final static String RKEY_AUTO = "Setup.Auto";
    private final static String RKEY_ZOOM = "Setup.Zoom";
    private final static String RKEY_SERVER = "Setup.Server"; // URL
    private final static String PROP_SERVER = "Server"; // URL
    private Command CMD_OK, CMD_BACK;
    private ChoiceGroup choices;
    private TextField url;
    private TextField validity;
    private final static long vtk = 3600000L; // hours to ms
    public Ticker help;

    /** Creates a new instance of Setup */
    public SetupOptions( Application a_parent, CommandListener a_callback )
    {
        super( null );
        help = new Ticker( a_parent.getString(this,"Ticker") );
        CMD_OK = new Command( a_parent.getString(this,"Menu.Done"), Command.OK, 1 );
        CMD_BACK = new Command( a_parent.getString(".Menu.Back"), Command.CANCEL, 2 );

        // Auto refresh setup
        choices = new ChoiceGroup( a_parent.getString( this, "Options" ), Choice.MULTIPLE, 
            new String[]
            { 
                a_parent.getString( this, "Auto" ), 
                a_parent.getString( this, "Help" ), 
                a_parent.getString( this, "Zoom" ) 
            }, null );
        choices.setSelectedIndex( 0, a_parent.getAuto() );
        choices.setSelectedIndex( 1, a_parent.getHelpMode( a_parent ) == a_parent.HELP_TICKER );
        choices.setSelectedIndex( 2, a_parent.getZoom() != 1 );
        append( choices );

        // validity
        validity = new TextField( a_parent.getString( this, "Validity" ), 
            String.valueOf( a_parent.getValidity()/vtk ), 10, TextField.DECIMAL );
        append( validity );

        // server URL
        url = new TextField( a_parent.getString( this, "Server" ), 
            a_parent.getURL(), 512, TextField.URL );
        url.setString( a_parent.getURL() );
        append( url );
        
        // free memory
        System.gc();
        append( new TextField( a_parent.getString( this, "Memory" ), 
            String.valueOf( a_parent.runtime.freeMemory()/1024L ), 
            10, TextField.UNEDITABLE ) );

        // allowance
        append( new TextField( a_parent.getString( "Weather.Expires" ), 
            a_parent.formatDateTime( a_parent.getDeadline( a_parent ) ), 
            20, TextField.UNEDITABLE ) );

        // allowance
        append( new TextField( a_parent.getString( this, "Id" ), 
            a_parent.getCopyID( a_parent ), 60, TextField.UNEDITABLE ) );

        // complete GUI
        setTitle( a_parent.getString(this,"Title") );
        setTicker( a_parent.getHelpMode() != Dispatcher.HELP_NO ? help : null );
        addCommand( CMD_OK );
        addCommand( CMD_BACK );
        setCommandListener( a_callback );
    }

    /**
     * Returns Auto Refresh setting from screen form.
     */
    public boolean getHelpMode()
    {
        boolean[] selection = new boolean[ choices.size() ];
        choices.getSelectedFlags( selection );
        return selection[1];
    }
    /**
     * Returns Auto Refresh setting from screen form.
     */
    public boolean getAuto()
    {
        boolean[] selection = new boolean[ choices.size() ];
        choices.getSelectedFlags( selection );
        return selection[0];
    }
    /**
     * Returns Auto Refresh setting from RMS.
     */
    public static boolean getAuto( Dispatcher a_dispatcher )
    {
        String rgstr = a_dispatcher.registry.getValue( RKEY_AUTO );
        return rgstr == null || rgstr.compareTo( String.valueOf( true ) ) == 0;
    }
    /**
     * Saves supplied Auto Refresh setting into RMS.
     */
    public static void setAuto( Dispatcher a_dispatcher, boolean a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_AUTO, String.valueOf( a_value ) );
    }

    /**
     * Returns Picture Scale setting from screen form.
     */
    public boolean getZoom()
    {
        boolean[] selection = new boolean[ choices.size() ];
        choices.getSelectedFlags( selection );
        return selection[2];
    }
    /**
     * Returns Picture Scale setting from RMS.
     */
    public static int getZoom( Dispatcher a_dispatcher )
    {
        String rgstr = a_dispatcher.registry.getValue( RKEY_ZOOM );
        return rgstr != null ? Integer.parseInt( rgstr ) : 1;
    }
    /**
     * Saves supplied Picture Scale setting into RMS.
     */
    public static void setZoom( Dispatcher a_dispatcher, int a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_ZOOM, String.valueOf( a_value ) );
    }

    /**
     * Returns Server URL setting from screen form.
     */
    public String getURL()
    {
        return url.getString();
    }
    /**
     * Returns Server URL setting from RMS.
     */
    public static String getURL( Dispatcher a_dispatcher )
    {
        String rgstr = a_dispatcher.registry.getValue( RKEY_SERVER );
        return rgstr != null ? rgstr : a_dispatcher.getAppProperty( PROP_SERVER );
    }
    /**
     * Saves supplied Server URL setting into RMS.
     */
    public static void setURL( Dispatcher a_dispatcher, String a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_SERVER, String.valueOf( a_value ) );
    }

    /**
     * Returns Validity setting from screen form.
     */
    public long getValidity()
    {
        try { return Long.parseLong( validity.getString() )*vtk; }
        catch( NumberFormatException e ) { return 0L; }
    }
    /**
     * Returns Validity setting from RMS.
     */
    public static long getValidity( Dispatcher a_dispatcher )
    {
        long rv = 0L;
        String rgstr = a_dispatcher.registry.getValue( RKEY_VALID );
        if( rgstr != null )
            try { rv = Long.parseLong( rgstr ); }
            catch( NumberFormatException e ) { rgstr = null; }
        if( rgstr == null )
        {
            rgstr = a_dispatcher.getAppProperty( PROP_VALID );
            try { rv = Long.parseLong( rgstr )*vtk; }
            catch( NumberFormatException e ) { rv = 0L; }
        }
        return rv;
    }
    /**
     * Saves supplied Validity setting into RMS.
     */
    public static void setValidity( Dispatcher a_dispatcher, long a_value )
        throws RecordStoreException
    {
        a_dispatcher.registry.setValue( RKEY_VALID, String.valueOf( a_value ) );
    }
}
