/*
 * CityList.java
 * Created on April 29, 2006, 12:25 AM
 * Copyright 2007 Nikolai Varankine. All rights reserved.
 *
 * This class implements city selection lists combined in hierarchy.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.lcdui.*;

/**
 * @author  Nikolai Varankine
 */
public class CityList extends List implements CommandListener
{
    private Command CMD_BACK, CMD_SELECT, CMD_SEARCH;
    private Dispatcher parent;
    private CommandListener callback;
    private Search query;
    private Index index;
    public Ticker help;

    /** Creates a new instance of CityList */
    public CityList( Dispatcher a_parent, CommandListener a_callback, int a_countryIndex ) 
    {
        super( null, List.IMPLICIT );
        parent = a_parent;
        callback = a_callback;
        index = new Index( a_countryIndex ); // stuff contents
        query = new Search( parent, this );
        help = new Ticker( a_parent.getString(this,"Ticker") );
        CMD_BACK = new Command( parent.getString(".Menu.Back"), Command.CANCEL, 2 );
        CMD_SELECT = new Command( parent.getString(this,"Menu.Select"), Command.ITEM, 1 );
        CMD_SEARCH = new Command( parent.getString(this,"Menu.Search"), Command.SCREEN, 3 );

        // finish GUI
        setTicker( parent.getHelpMode() != Dispatcher.HELP_NO ? help : null );
        setSelectCommand( CMD_SELECT ); //addCommand( CMD_SELECT );
        addCommand( CMD_BACK );
        //addCommand( CMD_SEARCH );
        setCommandListener( this );
        refresh();
    }

    /**
     * Recreates visible list based on pair of synchronized arrays
     */
    private void refresh()
    {
        // clean old visible list
        deleteAll();
        setTitle( null );

        // prepare new visible list
        Font fontIndex = null, fontCity = null;
        index.sort();
        for( int c = 0; c < index.size(); c++ ) 
        {
            String name = index.getName( c );
            int code = index.getCode( c );

            if( code == index.getId() )
            {
                append( parent.getString(this,"Search"), null ); // current index title
                setTitle( name );
                setSelectedIndex( c, true );
            }
            else if( code >= index.ROOT )
                append( name + " ...", null ); // reference item
            else
                append( name, null ); // city item
        }
    }

    public int getSelectedCode()
    {
        return index.getCode( getSelectedIndex() );
    }

    public String getSelectedName()
    {
        return index.getName( getSelectedIndex() );
    }

    public int getIndexCode()
    {
        return index.getId();
    }

    public void commandAction( Command a_command, Displayable a_displayable )
    {
        
        if( a_displayable == query )
        {
            if( a_command.getCommandType() == Command.ITEM || a_command == SELECT_COMMAND )
            {
                // determine index of selection
                IndexRecord sr = query.getChoice();
                if( sr == null ) return;
                // examine selection
                if( sr.code >= Index.ROOT )
                {
                    // load owning list
                    if(  sr.code != getIndexCode() )
                        try { index.load( sr.code ); }
                        catch( IOException e ) {}
                    refresh();
                    // let user continue selection
                    parent.setCurrent( this );
                }
                else
                {
                    // load owning list
                    if(  sr.id != getIndexCode() )
                        try { index.load( sr.id ); }
                        catch( IOException e ) {}
                    refresh();
                    setSelectedIndex( sr.position, true );
                    // really search is done
                    callback.commandAction( SELECT_COMMAND, this );
                }
            }

            else if( a_command.getCommandType() == Command.CANCEL )
            {
                parent.setCurrent( this );
            }
        }

        else if( a_command == CMD_SEARCH )
            parent.setCurrent( query );

        else if( a_command == CMD_BACK )
            callback.commandAction( a_command, a_displayable );

        else if( a_command == CMD_SELECT )
        {
            int sc = getSelectedCode();
            if( sc == index.getId() ) 
                parent.setCurrent( query );
            else if( sc < Index.ROOT ) 
                callback.commandAction( a_command, a_displayable );
            else if( sc != getIndexCode() ) 
                try { index.load( sc ); refresh(); }
                catch( IOException e ) { parent.setCurrent( AlertType.WARNING, 1, this, e ); }
            else ; // no need in redraw
        }
    }

    public Enumeration elements( String a_filter )
    {
        return index.elements( a_filter );
    }
    
}
