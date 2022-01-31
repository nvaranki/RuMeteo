package com.varankin.RuMeteo;


/**
 * @author  Nikolai Varankine
 */
public class IndexRecord
{
    public int id; // list index
    public String title; // list title
    public int code;   // accosiated city index
    public String name; // accosiated city name
    public int position; // in loaded list

    public IndexRecord(int a_id, String a_title, int a_code, String a_name, int a_position)
    {
        id = a_id; title = a_title; code = a_code; name = a_name; position = a_position;
    }
}