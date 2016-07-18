package net;

import game.Card;
import game.Color;
import game.Game;
import game.Player;

import java.rmi.RemoteException;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by mauro on 26/05/16.
 */
public class GameInstance implements RemoteGame {

    Game g;
    Queue<GameEvent> update_queue;
    Queue<GameEvent> dead_queue;

    boolean started;
    Object lock; //variabile oggetto per mettere in wait i giocatori che aspettano il mio turno


    public GameInstance(){
        update_queue = new LinkedList<GameEvent>();
        dead_queue = new LinkedList<GameEvent>();
        started = false;
        lock = new Object();
    }

    //REMOTE METHODS

    public void initGame(){
        g = new Game();
    }

    public boolean addPlayer(Player p) throws RemoteException {
        return g.addPlayer(p);
    }

    public int getNPlayer() throws RemoteException{
        return g.getNPlayer();
    }

    public int voteStart(Player p) throws RemoteException{
        return g.voteStart(p);
    }

    public boolean checkAllPlaying() throws RemoteException{

        boolean out = false;

        if(g.checkAllPlaying()){ //if all players want to play, give 'em their first hand

            if(!started){
                g.setupGame();
                started = true;
            }
            out = true;
        }

        return out;

    }

    public boolean isFinish() throws RemoteException{ return g.isFinish(); }

    public Card getLastCard() throws RemoteException{
        return g.getLastCard();
    }

    public Card remotePop() throws RemoteException{ //TODO - non serve?
        return g.popCard();
    }

    public void card2Table(Card c) throws RemoteException{
        g.card2Table(c);
    }

    public Color getExtraColor() throws RemoteException{
        return g.getExtraColor();
    }

    public Game getState() throws RemoteException{
        return g;
    }

    public void setState(Game state) throws RemoteException{
        g = state;
    }

    public void pushEvent(GameEvent e) throws RemoteException{
        update_queue.add(e); //save locally the event received
    }

    public GameEvent popEvent(){
        return update_queue.poll();
    }

    public Queue<GameEvent> getUpdates() {
        return update_queue;
    }

    public void clearUpdates(){
        update_queue.clear();
    }

    public void sendUpdates(Queue<GameEvent> q) throws RemoteException{
        update_queue = q;
    }

    /*public void waitEvent() throws RemoteException {

        try {

            System.out.println("mi sto bloccando...");
            lock.wait();
            System.out.println("mi sono sbloccato!");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }*/

    public boolean isAlive() throws RemoteException{
        return true;
    }

    public void pushDead(GameEvent e) throws RemoteException{
        dead_queue.add(e); // save locally the dead event
        System.out.println("GameInstance: ho ricevuto un evento DEAD");
    }
    
    public GameEvent popDead() {
    	return dead_queue.poll();
    }

    public boolean anyDead(){

        boolean out = false;

        if(dead_queue.size() > 0) out = true;

        return out;

    }

}
