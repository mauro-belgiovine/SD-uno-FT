package game;

import java.io.Serializable;
import java.util.*;

public class Deck implements Serializable{
    
    public int n_cards = 108;
    
    public Card[] cards;

    //TODO - cambiare stack e table in queue???

    public List<Card> stack; // stack is a list of int, which correspond to the indexes of cards[]
    public int n_current = n_cards;
    
    public List<Card> table; //pile of cards thrown by players
    public Card last_c;

    
    public Deck(){
        
        cards = new Card[n_cards];
        stack = new ArrayList<Card>();
        table = new ArrayList<Card>();
        last_c = null;
    }
    
    public void shuffle(){
        Collections.shuffle(this.stack, new Random());
    }

    public void addToDeck(Card c){ //inserisco le carte nel mazzo (caso in cui crasha un giocatore)

        stack.add(c);
        n_current++;

    }
    
    public Card pop() {

        if (n_current == 0){

            int table_size = table.size();
            for(int i = 0; i < (table_size-1); i++){
                Card cc = table.get(i);
                stack.add(cc);
            }
            table.clear();
            table.add(last_c);

            n_current = stack.size();
            shuffle();

        }

        n_current--;
        Card c = stack.get(n_current);
        stack.remove(n_current);

        return c;
    }
    
    public void card2Table(Card c){
        table.add(c);
        last_c = c;
    }
    
    /*public void Init_Deck(){
        
        //init all cards
        
        int count = 0;
        
        cards[count] = new Card(Color.YELLOW, 0, Action.NONE); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.YELLOW, i, Action.NONE);   //two cards per number
            cards[count+i+9] = new Card(Color.YELLOW, i, Action.NONE);
        }
        count += 19;
        
        cards[count] = new Card(Color.BLUE, 0, Action.NONE); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.BLUE, i, Action.NONE);   //two cards per number
            cards[count+i+9] = new Card(Color.BLUE, i, Action.NONE);
        }
        count += 19;
        
        cards[count] = new Card(Color.RED, 0, Action.NONE); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.RED, i, Action.NONE);   //two cards per number
            cards[count+i+9] = new Card(Color.RED, i, Action.NONE);
        }
        count += 19;
        
        cards[count] = new Card(Color.GREEN, 0, Action.NONE); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.GREEN, i, Action.NONE);   //two cards per number
            cards[count+i+9] = new Card(Color.GREEN, i, Action.NONE);
        }
        count += 19;
        
        cards[count++] = new Card(Color.YELLOW, -1, Action.DRAW2);
        cards[count++] = new Card(Color.YELLOW, -1, Action.DRAW2);
        cards[count++] = new Card(Color.BLUE, -1, Action.DRAW2);
        cards[count++] = new Card(Color.BLUE, -1, Action.DRAW2);
        cards[count++] = new Card(Color.RED, -1, Action.DRAW2);
        cards[count++] = new Card(Color.RED, -1, Action.DRAW2);
        cards[count++] = new Card(Color.GREEN, -1, Action.DRAW2);
        cards[count++] = new Card(Color.GREEN, -1, Action.DRAW2);
        cards[count++] = new Card(Color.YELLOW, -1, Action.SKIP);
        cards[count++] = new Card(Color.YELLOW, -1, Action.SKIP);
        cards[count++] = new Card(Color.BLUE, -1, Action.SKIP);
        cards[count++] = new Card(Color.BLUE, -1, Action.SKIP);
        cards[count++] = new Card(Color.RED, -1, Action.SKIP);
        cards[count++] = new Card(Color.RED, -1, Action.SKIP);
        cards[count++] = new Card(Color.GREEN, -1, Action.SKIP);
        cards[count++] = new Card(Color.GREEN, -1, Action.SKIP);
        cards[count++] = new Card(Color.YELLOW, -1, Action.REVERSE);
        cards[count++] = new Card(Color.YELLOW, -1, Action.REVERSE);
        cards[count++] = new Card(Color.BLUE, -1, Action.REVERSE);
        cards[count++] = new Card(Color.BLUE, -1, Action.REVERSE);
        cards[count++] = new Card(Color.RED, -1, Action.REVERSE);
        cards[count++] = new Card(Color.RED, -1, Action.REVERSE);
        cards[count++] = new Card(Color.GREEN, -1, Action.REVERSE);
        cards[count++] = new Card(Color.GREEN, -1, Action.REVERSE);
        
        
        for(int i = 0; i < 4; i++) cards[count+i] = new Card(Color.NONE, -1, Action.DRAW4);
        count += 4;
        
        for(int i = 0; i < 4; i++) cards[count+i] = new Card(Color.NONE, -1, Action.WILD);
        count += 4;
        
        //init the deck stack
        
        for(int i = 0; i < this.n_cards; i++){
            stack.add(cards[i]);
        }
    }*/


    public void Init_Deck(){

        //init all cards

        int count = 0;

        // RED cards
        cards[count] = new Card(Color.RED, 0, Action.NONE,"000"); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.RED, i, Action.NONE, "00"+i);   //two cards per number
            cards[count+i+9] = new Card(Color.RED, i, Action.NONE, "00"+i);
        }
        count += 19;

        // YELLOW cards
        cards[count] = new Card(Color.YELLOW, 0, Action.NONE, "100"); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.YELLOW, i, Action.NONE, "10"+i);   //two cards per number
            cards[count+i+9] = new Card(Color.YELLOW, i, Action.NONE, "10"+i) ;
        }
        count += 19;

        //GREEN cards
        cards[count] = new Card(Color.GREEN, 0, Action.NONE, "200"); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.GREEN, i, Action.NONE, "20"+i);   //two cards per number
            cards[count+i+9] = new Card(Color.GREEN, i, Action.NONE, "20"+i);
        }
        count += 19;

        // BLUE cards
        cards[count] = new Card(Color.BLUE, 0, Action.NONE, "300"); // 0 card
        for(int i = 1; i <= 9; i++){
            cards[count+i] = new Card(Color.BLUE, i, Action.NONE, "30"+i);   //two cards per number
            cards[count+i+9] = new Card(Color.BLUE, i, Action.NONE, "30"+i);
        }
        count += 19;

        // SKIP
        cards[count++] = new Card(Color.RED, -1, Action.SKIP, "010");
        cards[count++] = new Card(Color.RED, -1, Action.SKIP, "010");
        cards[count++] = new Card(Color.YELLOW, -1, Action.SKIP, "110");
        cards[count++] = new Card(Color.YELLOW, -1, Action.SKIP, "110");
        cards[count++] = new Card(Color.GREEN, -1, Action.SKIP, "210");
        cards[count++] = new Card(Color.GREEN, -1, Action.SKIP, "210");
        cards[count++] = new Card(Color.BLUE, -1, Action.SKIP, "310");
        cards[count++] = new Card(Color.BLUE, -1, Action.SKIP, "310");
        //REVERSE
        cards[count++] = new Card(Color.RED, -1, Action.REVERSE, "011");
        cards[count++] = new Card(Color.RED, -1, Action.REVERSE, "011");
        cards[count++] = new Card(Color.YELLOW, -1, Action.REVERSE, "111");
        cards[count++] = new Card(Color.YELLOW, -1, Action.REVERSE, "111");
        cards[count++] = new Card(Color.GREEN, -1, Action.REVERSE, "211");
        cards[count++] = new Card(Color.GREEN, -1, Action.REVERSE, "211");
        cards[count++] = new Card(Color.BLUE, -1, Action.REVERSE, "311");
        cards[count++] = new Card(Color.BLUE, -1, Action.REVERSE, "311");
        //DRAW2
        cards[count++] = new Card(Color.RED, -1, Action.DRAW2, "012");
        cards[count++] = new Card(Color.RED, -1, Action.DRAW2, "012");
        cards[count++] = new Card(Color.YELLOW, -1, Action.DRAW2, "112");
        cards[count++] = new Card(Color.YELLOW, -1, Action.DRAW2, "112");
        cards[count++] = new Card(Color.GREEN, -1, Action.DRAW2, "212");
        cards[count++] = new Card(Color.GREEN, -1, Action.DRAW2, "212");
        cards[count++] = new Card(Color.BLUE, -1, Action.DRAW2, "312");
        cards[count++] = new Card(Color.BLUE, -1, Action.DRAW2, "312");

        //WILD
        for(int i = 0; i < 4; i++) cards[count+i] = new Card(Color.NONE, -1, Action.WILD, "400");
        count += 4;

        //DRAW4
        for(int i = 0; i < 4; i++) cards[count+i] = new Card(Color.NONE, -1, Action.DRAW4, "401");

        //init the deck stack

        for(int i = 0; i < this.n_cards; i++){
            stack.add(cards[i]);
        }
    }

    public String getPrevTableCard(){
        return (table.get(table.size()-2)).getId();
    }

    public String getTableCard(){ return (table.get(table.size()-1)).getId(); }
    

}