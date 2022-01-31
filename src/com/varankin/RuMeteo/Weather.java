/*
 * Weather.java
 * Created on April 29, 2006, 12:25 AM
 * Copyright 2006 Nikolai Varankine. All rights reserved.
 *
 * This class implements main view.
 */

package com.varankin.RuMeteo;

import com.varankin.mobile.Dispatcher;
import com.varankin.mobile.http.*;
import java.io.IOException;
import java.util.*;
import javax.microedition.io.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStoreException;

/**
 * @author  Nikolai Varankine
 */
public class Weather extends Canvas implements CommandListener, HttpLinker
{
    private Command CMD_BM[], CMD_EXIT, CMD_SETUP;
    public Command CMD_INQUIRE, CMD_CITY; 
    protected Application parent;
    private int bookmarkId;
    private Bookmark bookmark = null;
    private CityList city_list = null;
    private SetupView view;
    private SetupOptions setup = null;
    private HttpLinkMonitor monitor;
    private int zoom;
    private boolean low_memory;
    public Ticker help;

    
    /** Creates a new instance of Weather */
    public Weather( Application a_parent, boolean a_low_memory ) 
    {
        super();
        parent = a_parent;
        low_memory = a_low_memory;
        help = new Ticker( parent.getString(this,"Ticker") );
        view = new SetupView( a_parent, this );
        
        CMD_INQUIRE = new Command( parent.getString(this,"Menu.Run"), Command.SCREEN, 5 );
        CMD_CITY = new Command( parent.getString(this,"Menu.City"), Command.ITEM, 1 );
        CMD_EXIT = new Command( parent.getString(this,"Menu.Exit"), Command.EXIT, 100 );
        CMD_SETUP = new Command( parent.getString(this,"Menu.Setup"), Command.SCREEN, 90 );

        // generate bookmark menu
        CMD_BM = new Command[ parent.bookmarks.length ];
        for( int bm = 0; bm < CMD_BM.length; bm++ )
        {
            String title = Bookmark.getTitle( parent, bm );
            if( title == null ) title = parent.getString("Bookmark.Unknown") 
                + " #" + String.valueOf( (bm + 1)%CMD_BM.length );
            CMD_BM[ bm ] = new Command( title, Command.SCREEN, 10 + bm );
            if( ! low_memory ) addCommand( CMD_BM[ bm ] );
        }

        // last displayed city goes first
        bookmarkId = SetupView.getView( parent );

        // complete GUI
        setTitle( parent.getString( this, "Title" ) );
        setTicker( parent.getHelpMode() != Dispatcher.HELP_NO ? help : null );
        addCommand( CMD_CITY );
        addCommand( CMD_INQUIRE );
        addCommand( CMD_SETUP );
        addCommand( CMD_EXIT );
        setCommandListener( this );
    }

    protected void paint( Graphics g )
    {
        if( bookmark.image == null ) try
        {
            bookmark.image = bookmark.data.length > 0
                ? Image.createImage( bookmark.data, 0, bookmark.data.length )
                : SetupView.createBookmarkImage( view.logo, 
                    bookmark.title = CMD_BM[ bookmarkId ].getLabel() );
            // release memory with downloaded data
            if( bookmark.data.length > 0 ) bookmark.data = Bookmark.EMPTY;
        }
        catch( Exception e ) {}
        SetupView.paintImage( g, bookmark.image, view.logo50, getWidth(), getHeight(), 
            low_memory ? 1 : parent.getZoom() );
    }

    private void inquire( Alert a_message )
    {
        // stuff HTTP data
        Hashtable acc = new Hashtable(), qry = new Hashtable(); 
        acc.put( "Accept", "text/*" );
        acc.put( "Accept-Charset", "utf-8, *" );
        qry.put( "value", String.valueOf( bookmark.city ) );
        qry.put( "type", view.getView( bookmark.view ) );
        qry.put( "is", "" );
        
        if( parent.isLicenseValid() )
        {
            // make request
            System.gc();
            monitor = new HttpLinkMonitor( HttpConnection.GET, parent.getURL(), 
                qry, acc,  new Item[] 
                    {
                        new ImageItem( null, view.logo50, 
                            Item.LAYOUT_NEWLINE_AFTER, null ),
                        new StringItem( null, bookmark.title + "\n" 
                            + parent.getString( this, "Getting" ) )
                    }, 
                this );
            if( a_message != null )
                parent.setCurrent( a_message, monitor );
            else
                parent.setCurrent( monitor );
        }
        else
        {
            // show excuse
            Alert notification = new Alert( null, 
                parent.getString( this, "Expires" ) + 
                parent.formatDateTime( parent.getDeadline( parent ) ), 
                null, AlertType.WARNING );
            if( a_message != null )
            {
                // combine alerts into one
                if( a_message.getString() != null )
                    notification.setString( a_message.getString() + "\n" 
                        + notification.getString() );
                notification.setImage( a_message.getImage() );
                notification.setType( a_message.getType() );
            }
            parent.setCurrent( notification, this );
        }
    }

    /**
     * Indicates that a command event has occurred on Displayable.
     */
    public void commandAction( Command a_command, Displayable a_displayable ) 
    {
        Alert notification;

        if( a_displayable == city_list )
        {
            if( a_command == List.SELECT_COMMAND 
                || a_command.getCommandType() == Command.ITEM )

              if( city_list.getSelectedCode() == bookmark.city )
                parent.setCurrent( this );

              else
              {
                // copy new city selection
                bookmark.title = city_list.getSelectedName();
                bookmark.city = city_list.getSelectedCode();
                bookmark.list = city_list.getIndexCode();
                bookmark.image = null; // indirectly invalidate current image
                bookmark.data = Bookmark.EMPTY;
                bookmark.created = Bookmark.INFINITY;

                // destroy old image in RMS
                bookmark.save(); 

                // replace related menu command
                Command cmd = CMD_BM[ bookmarkId ];
                CMD_BM[ bookmarkId ] = new Command( bookmark.title, null, 
                  cmd.getCommandType(), cmd.getPriority() );
                removeCommand( cmd );
                if( ! low_memory ) addCommand( CMD_BM[ bookmarkId ] );

                // notify and inquire forecast
                notification = new Alert( null, parent.getString( this, 
                    "Alert.6.Message" ) + bookmark.title, 
                    null, AlertType.CONFIRMATION );
                notification.setTimeout( 1*1000 ); // standard one is too long
                System.gc();
                inquire( notification );
              }
                
            else //if( a_command.getCommandType() == Command.BACK )
                parent.setCurrent( this );
            
            if( low_memory ) city_list = null;
        }

        else if( a_displayable == view )
        {
            switch( a_command.getCommandType() )
            {
                case Command.ITEM:
                case Command.OK:
                    // notify and inquire forecast
                    notification = new Alert( null, parent.getString( this, "Alert.3.Message" ), 
                        view.createSampleImage(), AlertType.CONFIRMATION );
                    //notification.setTimeout( 1*1000 ); // standard one is too long
                    int old_view = bookmark.view;
                    bookmark.view = view.getViewIndex();
                    if( old_view != bookmark.view )
                        if( bookmark.city < Index.ROOT ) inquire( notification );
                        else parent.setCurrent( notification, this ); 
                    else
                        parent.setCurrent( this ); 
                    break;
                case Command.SCREEN:
                    // display next screen
                    setup = new SetupOptions( parent, this );
                    parent.setCurrent( setup );
                    break;
                case Command.BACK:
                default:
                    parent.setCurrent( this ); break;
            }
        }

        else if( a_displayable == setup )
        {
            if( a_command.getCommandType() == Command.OK ) try
            {
                // copy settings
                parent.setAuto( setup.getAuto() );
                parent.setZoom( setup.getZoom() ? 2 : 1 );
                parent.setURL( setup.getURL() );
                parent.setValidity( setup.getValidity() );
                boolean hm = setup.getHelpMode();
                parent.setHelpMode( hm ? Dispatcher.HELP_TICKER : Dispatcher.HELP_NO );
                setTicker( hm ? help : null );
                if( city_list != null ) city_list.setTicker( hm ? city_list.help : null );
                if( setup != null ) setup.setTicker( hm ? setup.help : null );
                if( view != null ) view.setTicker( hm ? view.help : null );
            }
            catch( Exception e ) {} //? TODO
            parent.setCurrent( this ); 
            setup = null; // to reset contents on next call
        }

        else if( a_command == CMD_INQUIRE ) 
        {
            if( bookmark.city < Index.ROOT ) inquire( null );
            else if( true ) commandAction( CMD_CITY, this );
        }

        else if( a_command == CMD_CITY ) 
        {
            if( city_list == null ) 
                city_list = new CityList( parent, this, bookmark.list );
            parent.setCurrent( city_list );
        }

        else if( a_command == CMD_SETUP ) 
        {
            // enter [color] setup mode
            view.setViewIndex( bookmark.view );
            parent.setCurrent( view );
        }
        
        else if( a_command == CMD_EXIT ) 
        {
            parent.exitRequested();
        }
        
        else for( int bm = 0; bm < CMD_BM.length; bm ++ )
        {
            if( a_command == CMD_BM[ bm ] ) setBookmark( bm );
        }

    }
    
    protected void keyPressed( int a_keyCode )
    {
        String keyName = getKeyName( a_keyCode );
        
        if( a_keyCode == KEY_POUND )
        {
            commandAction( CMD_SETUP, this );
        }

        else if( a_keyCode == KEY_STAR || keyName.compareTo( "SELECT" ) == 0 )
        {
            commandAction( CMD_CITY, this );
        }

        else if( keyName.compareTo( "SEND" ) == 0 )
        {
            commandAction( CMD_INQUIRE, this );
        }

        else if( a_keyCode != KEY_NUM6 && 
            ( keyName.compareTo( "RIGHT" ) == 0 || keyName.charAt( 0 ) == 0x2192 )
             ||  a_keyCode != KEY_NUM8 && 
            ( keyName.compareTo( "DOWN" ) == 0 || keyName.charAt( 0 ) == 0x2193 )  )
        {
            // next bookmark
            setBookmark( bookmarkId + 1 );
        }

        else if( a_keyCode != KEY_NUM4 && 
            ( keyName.compareTo( "LEFT" ) == 0 || keyName.charAt( 0 ) == 0x2190 )
             ||  a_keyCode != KEY_NUM2 && 
            ( keyName.compareTo( "UP" ) == 0 || keyName.charAt( 0 ) == 0x2191 )  )
        {
            // previous bookmark
            setBookmark( bookmarkId -1 + parent.bookmarks.length );
        }

        else switch( a_keyCode ) // change bookmark
        {
            case KEY_NUM1: setBookmark( 0 ); break;
            case KEY_NUM2: setBookmark( 1 ); break;
            case KEY_NUM3: setBookmark( 2 ); break;
            case KEY_NUM4: setBookmark( 3 ); break;
            case KEY_NUM5: setBookmark( 4 ); break;
            case KEY_NUM6: setBookmark( 5 ); break;
            case KEY_NUM7: setBookmark( 6 ); break;
            case KEY_NUM8: setBookmark( 7 ); break;
            case KEY_NUM9: setBookmark( 8 ); break;
            case KEY_NUM0: setBookmark( 9 ); break;
            default: break; // play sound here
        }
    }

    public void setBookmark()
    {
        setBookmark( bookmarkId );
    }
    /**
     * Body of Change Bookmark command.
     */
    private void setBookmark( int a_bookmarkId )
    {
        a_bookmarkId %= parent.bookmarks.length;

        // do nothing if no action
        if( bookmark != null && ( a_bookmarkId == bookmarkId || low_memory ) ) return;

        // check bookmark availability
        if( parent.bookmarks[ a_bookmarkId ] == null )
        {
            parent.bookmarks[ a_bookmarkId ] = new Bookmark( parent, a_bookmarkId );
            if( parent.bookmarks[ a_bookmarkId ] == null ) 
                if( bookmark != null )
                    return; // ignore fatal error
                else
                    parent.exitRequested(); // fatal startup error
        }

        // make formal change
        bookmark = parent.bookmarks[ bookmarkId = a_bookmarkId ];

        // remember ID of last shown bookmark
        try { SetupView.setView( parent, bookmarkId ); }
        catch( RecordStoreException e ) {} // non fatal, default value on restart

        // invalidate obsolete forecast image
        if( bookmark.isExpired( parent.getValidity() ) ) 
        {
            bookmark.image = null;
            bookmark.data = Bookmark.EMPTY;
            bookmark.created = Bookmark.INFINITY;
        }

        // time to show up new picture
        if( parent.getAuto() )
        {
            if( bookmark.city >= Index.ROOT )
                commandAction( CMD_CITY, this );

            else if( bookmark.created == Bookmark.INFINITY )
                commandAction( CMD_INQUIRE, this );

            else if( parent.display.getCurrent() == this ) 
                repaint(); // as-is

            else 
                parent.setCurrent( this ); //re-paints
        }
        else if( parent.display.getCurrent() == this ) 
        {
            repaint(); // as-is
        } 
        else 
        {
            parent.setCurrent( this ); //re-paints
        }
    }
    
    public void completed( byte[] a_reply, int a_reply_size, HttpConnection a_conn )
    {
        try 
        { 
            String content_type = a_conn.getType();
            
            if( content_type == null ) 
                throw new IllegalArgumentException( parent.getString( this, "Alert.2.Message" ) );
            
            else if( content_type.startsWith( "image/" ) )
            {
                // determine creation time of image, reported by server
                long modified, now = ( new Date() ).getTime();
                long validity = parent.getValidity(); // estimated forecast validity
                try { modified = HttpLink.getLastModified( a_conn ); }
                catch( Exception e ) { modified = 0L; }
                // tolerate uncertain date
                if( modified == 0L ) 
                    modified = now - validity*9L/10L;

                // remember image and its expiration date
                bookmark.data = a_reply;
                bookmark.created = modified;
                bookmark.save();

                // release network memory
                monitor = null; 
                
                // display new image on screen
                bookmark.image = null;
                parent.setCurrent( new Alert( null, parent.getString( this, "Alert.1.Message" ) 
                    + parent.formatDateTime( modified ), null, 
                    now <= modified + validity ? AlertType.CONFIRMATION 
                    : AlertType.WARNING ), this );
            }

            else if( content_type.startsWith( "text/" ) )
            {
                String encoding = a_conn.getEncoding();
                if( encoding == null )
                {
                    int cs = content_type.indexOf( "charset=" );
                    if( cs >= 0 ) encoding = content_type.substring( cs+8 );
                }
                String implant;
                try { implant = encoding == null 
                    ? new String( a_reply, 0, a_reply_size )
                    : new String( a_reply, 0, a_reply_size, encoding ); }
                catch( java.io.UnsupportedEncodingException e ) 
                {
                    // strip non ASCII
                    for( int b = 0; b < a_reply_size; b ++ )
                        if( a_reply[b] < 0x20 || a_reply[b] >= 0x7F ) a_reply[b] = ' ';
                    implant = new String( a_reply, 0, a_reply_size );
                }
                // find out eligible reference to image
                String href = null;
                for( int imgi = implant.indexOf( "<img" ); imgi >= 0; imgi = implant.indexOf( "<img", imgi+4 ) )
                {
                    String imgs = implant.substring( imgi, implant.indexOf( '>', imgi )+1 ); // <img ... >
                    int srci = imgs.indexOf( "src=" );
                    if( srci > 0 )
                    {
                        // src=\\\"http://....png\\\"
                        int http = imgs.indexOf( "http:", srci );
                        if( http > 0 )
                        {
                            String delimiter = imgs.substring( srci+4, http );
                            href = imgs.substring( http, imgs.indexOf( delimiter, http ) );
                            // check excerpt
                            if( href.endsWith( ".png" ) || href.endsWith( ".jpg" ) 
                                || href.endsWith( ".gif" ) || href.endsWith( ".bmp" ) )
                                break; // looks like valid image
                            else
                                href = null;
                        }
                    }
                }
                if( href == null ) throw new IllegalArgumentException();

                // start second connection to download image, using same gauge
                Hashtable acc = new Hashtable(); acc.put( "Accept", "image/*" );
                a_conn.close(); // supposely no more than one connection at a time :(
                (new HttpLink( HttpConnection.GET, href, null, acc, 
                    monitor != null ? monitor.m_progress : null, this )).start();
            }
            else
                throw new IllegalArgumentException( content_type );
        }
        catch( Exception e ) { parent.setCurrent( AlertType.ERROR, 2, this, e ); }
        monitor = null; // it releases memory
    }

    public void interrupted( Exception a_problem ) 
    {
        parent.setCurrent( AlertType.ERROR, 7, this, a_problem );
        monitor = null; // it releases memory
    }

    public Dispatcher getDispatcher()
    {
        return parent;
    }
    
}
