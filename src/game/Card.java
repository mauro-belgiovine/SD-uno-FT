package game;

import java.io.Serializable;

public class Card implements Serializable{    // a card

    private Color color;    //card color
    private int number;  //card number (0..9 or -1 if cards doesn't have a number)
    private Action action;  //card action
    private String id;
    
    public Card(Color col, int n, Action act, String ident){
        
        color = col;
        number = n;
        action = act;
        id = ident;

    }
    
    public void printCard(){
        System.out.print("["+color+" "+ number+" "+action+"]");
    }

    public String serializeCard(){
        String s = "["+color+" "+ number+" "+action+"]";
        return s;
    }
    
    public Color getColor(){
        return color;
    }
    
    public boolean isColor(Color c){
        return (color == c);
    }
    
    public int getNumber(){
        return number;
    }
    
    public Action getAction(){
        return action;
    }
    
    public boolean sameColor(Card c){
        return (color == c.getColor());
    }
    
    public boolean sameNumber(Card c){
        return (number == c.getNumber());
    }

    public boolean sameAction(Card c) { return (action == c.getAction()); }

    public String getId(){ return id; }
}