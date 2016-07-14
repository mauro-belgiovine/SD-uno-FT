package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import overlay.*;

public class Player extends Node implements Serializable{

    private String name;
    private int id;
    private List<Card> hand;    // player's hand, containing his cards
    private boolean playing;
    
    public Player(String myname, int myid){
        name = myname;
        id = myid;
        hand = new ArrayList<Card>();
        playing = false;
    }

    // facciamo in modo che il metodo equals per la classe Player confronti l'UUID
    // invece che l'hash del suo indirizzo di memoria
    @Override public boolean equals(Object o){
        if(null != o) {
            if( o instanceof Player) {
                return this.getUuid().equals(((Player)o).getUuid());
            }
        }
        return false;

    }

    //TODO verifica che questo non serva effettivamente
    /*@Override public int hashCode() {

        return this.getUuid().hashCode();

    }*/


    public void printPlayer(){
        System.out.println(this.name+" "+this.id+" Cards: "+hand.size());
    }

    public boolean isPlaying(){ return playing; }

    public void setDead() { playing = false; }

    public void startPlaying(){ playing = true; }

    public String getName(){
        return name;
    }

    public List<Card> getHand(){
        return hand;
    }

    public void setHand(List<Card> hand){ this.hand = hand;}

    public void printHand(){

    	for (Card c:hand) {

            System.out.print(" ");
            c.printCard();
        }
    }
    
    public void card2Hand(Card c){
        hand.add(c);
    }
    
    public Card throwCard(int i){
        Card c = hand.get(i);
        hand.remove(i);
        return c;
    }
    
    public int getNumCards(){
        return hand.size();
    }
    
    //TODO: usare Card invece che l'indice?
    //TODO: fix prima carta DRAW4/WILD

    public boolean checkPlayable(int i, Card last, Color extra){
        // "i" is the index of my chosen card; "last" is the current card on top of the table;
        // "extra" is set in case DRAW4 or WILD is on the table
        
        boolean out = false;

        if(i < hand.size()) {

            Card c = hand.get(i);

            if(c.getColor() == Color.NONE) { // WILD or DRAW4

                out = true; //always playable

            } else if (last.getColor() != Color.NONE) { //if last card has a color

                if (c.sameColor(last) || (c.sameNumber(last) && c.getNumber() != -1) || (c.sameAction(last) && (c.getAction() != Action.NONE))) {
                    out = true;
                }

            } else if (last.getColor() == Color.NONE){ //if last has no color

                if(extra == Color.NONE) out = true; //nel caso la prima carta sia un WILD/DRAW4
                else if (c.isColor(extra)) out = true;

            }
        }
        
        return out;
    }

}