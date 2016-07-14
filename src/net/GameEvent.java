package net;

import java.io.Serializable;
import java.util.Map;

/**
 * Created by mauro on 09/06/16.
 */
public class GameEvent implements Serializable{

    public Event event; //event
    public Map<String, Object> params;  //params of the event

    public GameEvent(Event e, Map<String, Object> p){
        event = e;
        params = p;
    }

}
