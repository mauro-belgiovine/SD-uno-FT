package game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Game implements Serializable, Cloneable{
    
    int p_turn;

    int max_n_player;
    
    Color extra_col;
    
    Deck deck;

    List<Player> players;

    boolean finish ;
    
    boolean reverse;

    boolean skip = false;

    boolean show_colors = false; // se true, deve essere mostrata la scelta colori nella UI

    public Game(){

        max_n_player = 10;
        p_turn = 0;

        deck = new Deck();
        deck.Init_Deck();
        deck.shuffle(); //deck shuffling

        extra_col = Color.NONE;

        players = new ArrayList<Player>();

        finish = false;
        reverse = false;

    }

    public Game clone() throws CloneNotSupportedException {
        return (Game) super.clone();
    }
    
    
    public void print() {
    	
    	System.out.println("******** GAME STATE *******");
    	System.out.println("p_turn = "+p_turn);
    	for(int i = 0; i < players.size(); i++){
    		System.out.println(i+" "+players.get(i).getName()+" "+"carte "+players.get(i).getNumCards());
    		players.get(i).printHand();
    		System.out.println();
    	}
    	System.out.println("***************************");
    	
    }

    public Card popCard(){
        return deck.pop();
    }

    /* public Color getExtraCol(){
        return extra_col;
    } */
    
    public int nextPlayer(int p_turn){
        
        int p_next;
        
        if(!reverse) p_next = (p_turn + 1)% players.size();
        else{
            if(p_turn == 0) p_next = players.size()-1;
            else p_next = p_turn-1;
        }
        
        return p_next;
    }

    public void playCard(Card c){

        deck.card2Table(c); //he put it on the table
        execEffect(deck.last_c.getAction());

    }
    
     public void execEffect(Action a){ //apply effect
        
        //Player p = players.get(p_turn);
        int p_next = nextPlayer(p_turn);
        Player next = players.get(p_next);
        
        if (extra_col != Color.NONE) extra_col = Color.NONE; //reset extra color (from DRAW4 or WILD) if a new card has been played
        
        
        switch(a){
                
            case DRAW2://pesca 2 carte
            	System.out.println(players.get(p_next).getName()+" ("+p_next+") sta pescando 2 carte");
                for(int i = 0; i < 2; i++) next.card2Hand(deck.pop());
                break;
                
            case DRAW4:    //pesca 4 carte + cambia colore
            	System.out.println(players.get(p_next).getName()+" ("+p_next+") sta pescando 4 carte");
                for(int i = 0; i < 4; i++) next.card2Hand(deck.pop());
                break;
                
            case REVERSE:     //inverti giro di mano
                reverse = !reverse;
                break;
                
            case SKIP:       //il successivo salta un turno
                break;
                
            case WILD: //cambia colore
                //extra_col = chooseColor();
                break;
                
            default:
                //do nothing
        }
        

    }

    public boolean addPlayer(Player p) {

        boolean output = false;

        if((players.size() + 1) <= max_n_player){

            players.add(p);
            output = true;

        }

        if(output){
            System.out.println("Actual players are:");
            for(int i = 0; i < players.size(); i++){
                System.out.println("\tPlayer IP "+ players.get(i).getIp()+" UUID "+players.get(i).getUuid());
            }
        }

        return output;
    }

    public int getNPlayer() {
        return players.size();
    }

    public int getMaxNPlayer(){
        return max_n_player;
    }

    public int voteStart(Player p) {
        int i = players.indexOf(p);
        players.get(i).startPlaying();

        return i; //return the player index in the game state
    }

    public boolean checkAllPlaying() {

        boolean out = false;
        int num_p = players.size();
        int started = 0;

        for(int i = 0; i < num_p; i++){
            if(players.get(i).isPlaying()) started++;
        }

        if((started == num_p) && (num_p > 1)) out = true;

        return out;

    }

    public boolean isFinish() { return finish; }

    public Card getLastCard() {
        return deck.last_c;
    }

    public void card2Table(Card c) {
        //System.out.println(p.getName()+" played "+c.serializeCard());
        playCard(c);
    }

    public void setupGame(){

        //each player picks 7 card from the deck, one by one
        for(int i = 0; i < 7; i++){

            for(int y = 0; y < players.size(); y++){
                players.get(y).card2Hand(deck.pop());
            }
        }

        //put a card on the table
        // TODO controllare che non sia una carta speciale
        deck.card2Table(deck.pop());
    }

    public List<Card> getPHand(Player p){

        int i = players.indexOf(p);
        return players.get(i).getHand();
    }

    public int getPturn(){ return p_turn;}
    public void setPturn(int i) { p_turn = i; }
    public Deck getDeck(){ return deck;}
    public void addToDeck(Card c){ deck.addToDeck(c);}
    public Color getExtraColor(){return extra_col;}
    public void setExtraColor(Color c) { extra_col=c;}
    public boolean getShowColors (){return show_colors;}
    public void setShowColors(boolean b) {show_colors = b;}
    public void setSkip(boolean b) {skip=b;}
    public int nextRound(int p_turn){
        int next = nextPlayer(p_turn);
        if (skip) next = nextPlayer(next);
        return next;
    }
    public List<Player> getPlayers() {return players;}
    

}