/*
 * Search.java
 *
 * Created on 11 Март 2007 г., 17:39
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import java.util.Enumeration;
import java.util.Vector;
import javax.microedition.lcdui.*;

/**
 * @author  Nikolai Varankine
 */
public class Search extends Form implements ItemCommandListener, CommandListener
{
    private CityList callback;
    private TextField query;
    private ChoiceGroup result;
    private String searching, nothing, alot;
    private Vector found;
    private Command CMD_BACK, CMD_SELECT, CMD_SEARCH, CMD_STOP;
    public Ticker help;
    private Searching running;
    private class Searching implements Runnable
    {
        Enumeration e;
        int sts;
        private boolean running;
        public Searching( Enumeration a_cities, int a_status )
        {
            e = a_cities;
            sts = a_status;
            running = true;
        }
        public void run()
        {
            query.removeCommand( CMD_SEARCH );
            removeCommand( CMD_BACK );
            addCommand( CMD_STOP );
            while( running && e.hasMoreElements() && found.size() < found.capacity() )
            {
                IndexRecord sr = (IndexRecord) e.nextElement();
                if( sr == null ) continue;
                found.addElement( sr );
                String name = sr.name;
                if( sr.title != null ) name += ", " + sr.title;
                result.append( name, null ); // add more info here
            }
            delete( sts );
            if( result.size() > 0 ) 
            {
                result.addCommand( CMD_SELECT );
                if( found.size() == found.capacity() ) append( alot );
            }
            else
                append( nothing );
            removeCommand( CMD_STOP );
            query.addCommand( CMD_SEARCH );
            addCommand( CMD_BACK );
        }
        public void stop()
        {
            running = false;
        }
    }

    /** Creates a new instance of Search */
    public Search( Dispatcher a_parent, CityList a_callback )
    {
        super( null );
        callback = a_callback;
        found = new Vector( 20 );
        help = new Ticker( a_parent.getString(this,"Ticker") );
        CMD_BACK = new Command( a_parent.getString(".Menu.Back"), Command.CANCEL, 2 );
        CMD_STOP = new Command( a_parent.getString("HttpLinkMonitor.Menu.Stop"), Command.STOP, 1 );
        CMD_SELECT = new Command( a_parent.getString("Weather.Menu.Run"), Command.ITEM, 1 );
        CMD_SEARCH = new Command( a_parent.getString("CityList.Menu.Search"), Command.ITEM, 3 );
        query = new TextField( a_parent.getString(this,"Search"), null, 64, 
                    TextField.INITIAL_CAPS_WORD|TextField.NON_PREDICTIVE );
        result = new ChoiceGroup( a_parent.getString(this,"Results"), 
                    ChoiceGroup.EXCLUSIVE );
        searching = a_parent.getString(this,"Searching"); 
        nothing = a_parent.getString(this,"Nothing"); 
        alot = a_parent.getString(this,"Alot"); 

        // complete GUI
        setTitle( a_parent.getString(this,"Title") );
        setTicker( a_parent.getHelpMode() != Dispatcher.HELP_NO ? help : null );
        append( query );  
        query.addCommand( CMD_SEARCH );
        query.setItemCommandListener( this );
        append( result ); 
        result.setItemCommandListener( this );
        addCommand( CMD_BACK );
        setCommandListener( this );
    }

    public IndexRecord getChoice()
    {
        return found.size() > 0 ? (IndexRecord) found.elementAt( result.getSelectedIndex() ) : null;
    }

    private final void cleanup()
    {
        // cleanup previous results
        result.deleteAll();
        result.removeCommand( CMD_SELECT );
        found.removeAllElements();
        while( size() >=3 ) delete( 2 );
    }

    public void commandAction( Command a_command, Item a_item )
    {
        if( a_command == CMD_SEARCH && a_item == query 
            && query.getString().trim().length() > 0 )
        {
            // make search and stuff the list with findings
            cleanup();
            running = new Searching( callback.elements( query.getString().trim() ), 
                append( searching ) );
            ( new Thread( running ) ).start();
        }

        else if( ( a_command == CMD_SELECT || a_command == List.SELECT_COMMAND ) 
            && a_item == result )
        {
            callback.commandAction( a_command, this );
            cleanup(); // after parent retrieves data
        }
    }

    public void commandAction( Command a_command, Displayable a_displayable )
    {
        if( a_command == CMD_BACK )
        {
            cleanup();
            callback.commandAction( a_command, a_displayable );
        }
        else if( a_command == CMD_STOP )
        {
            running.stop();
        }
    }
}
