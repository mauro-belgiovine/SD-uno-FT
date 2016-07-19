package game;

import java.awt.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.List;

import static java.lang.Thread.sleep;

import net.*;
import net.Event;

public class Match{

    Game g;
    Player me;
    GameInstance instance;
    RemoteGame r_game;

    Scanner scan;
    int my_index;

    boolean local = false; //debug

    public boolean tryBind(Player p, String host){

        Registry registry;

        String name = "net.Lobby";

        boolean out = false;

        try {

            registry = LocateRegistry.getRegistry(host, 50000);
            r_game = (RemoteGame) registry.lookup(name);
            if(!r_game.addPlayer(p)) {
                System.out.println("Game is FULL!! Sorry.");
            }else{
                out = true;
            }

        } catch (Exception e) {
            System.err.println("tryBind() exception:");
            e.printStackTrace();

        }

        return out;
    }
    

	public void startClient(String[] args) {

		String p_name = "";
        String host = "";

        if(args.length > 1){
            p_name = args[0];
            host = args[1];
        }
        else if(args.length > 0){
        	p_name = args[0];
        }else{

            System.out.println("Usage: \n\tnet.Client <player_name> <host name>(?)");
            return;
        }

        scan = new Scanner(System.in);

        me = new Player(p_name, 777); //crea nuovo giocatore


        if(tryBind(me,host)) {

            try{

                instance = new GameInstance(); //create local instance of the game state

                my_index = -1;

                while(!me.isPlaying()){
                    System.out.println("<s> to vote for match start");

                    if(scan.next().equals("s")){
                        me.startPlaying();
                        my_index = r_game.voteStart(me);          //ok per l'avvio della partita
                    }
                }

                if(my_index < 0) {
                    System.out.println("Game error.");
                    return;
                }



                int my_port;
                if(local) my_port = 50000+1+my_index;
                else my_port = 50000;

                String name = "player."+me.getUuid();

                RemoteGame stub = (RemoteGame) UnicastRemoteObject.exportObject(instance, my_port);
                // Bind the remote object's stub in the registry
                Registry reg = LocateRegistry.createRegistry(my_port);
                reg.bind(name, stub);

                while(!r_game.checkAllPlaying()){
                    System.out.println("Waiting for other players...");
                    System.out.println("There are "+r_game.getNPlayer()+" connected to this game");
                    sleep(1000);
                }

                g = r_game.getState();

                me.setHand(g.getPHand(me)); //prendo le mie carte dallo stato del gioco


            } catch (Exception e) {
                System.err.println("in-game exception:");
                e.printStackTrace();

            }
        }

        scan.close();
		
	}

    public void execEvent(GameEvent e) {

        int p_index;
        Player p;

        switch(e.event){

            case PICKUP:
                //il giocatore indicato deve pescare una carta dal mazzo
                p_index = (Integer) e.params.get("player");
                p = g.players.get(p_index);
                Card pu = g.popCard(); //pesca una carta
                p.card2Hand(pu);
                break;

            case THROW:
                //il giocatore indicato deve mettere sul tavolo la carta
                p_index = (Integer) e.params.get("player");
                p = g.players.get(p_index);
                Card thrown = p.throwCard( (Integer) e.params.get("card_i") );
                g.card2Table(thrown); //mette la carta sul tavolo
                break;

            case EXTRA_COL:
                //si cambia l'extra_col (DRAW4 o WILD)
                g.extra_col = (Color) e.params.get("extra");
                break;

            case TURN:
                //setta il prossimo giocatore
                g.p_turn = (Integer) e.params.get("next");
                break;

            case FINISH:
                //la partita è finita è ha vinto il giocatore corrente (g.p_turn)
                g.finish = true;
                System.out.println("PARTITA FINITA! Vincitore: "+g.players.get(g.p_turn).getName());
                break;

            case GETSTATE:
                //devo aggiornare lo stato di gioco dal balancer

                Game gstate = (Game) e.params.get("state");
                                
                /******* quando ricevo un GETSTATE, controllo se nella dead_queue esiste ancora quel giocatore:
                 * se non esiste, scarto l'evento, altrimenti lo ripropago(?)
                 * ******/
                
                GameEvent dead;
                
                do{
                	
                	dead = instance.popDead();
                
	                if(dead != null){
	                	
	                	int suspicious_dead_index = gstate.players.indexOf((Player) dead.params.get("player_obj"));
	                	
	                	if(suspicious_dead_index > -1){ //se e' ancora nella lista, vuol dire che non e' stato ancora ribilanciato
	                		
	                		notifyDead(suspicious_dead_index); //ripropago il messaggio dead a tutti e chi stara' giocando, si occupera' di gestire il bilanciamento
	                		
	                	}//altrimenti, scarto l'evento (non faccio nulla)
	                	
	                }
	                
                }while(dead != null);

                //aggiorno il mio indice rispetto alla situazione attuale
                my_index = gstate.players.indexOf(me);

                //aggiorno il riferimento di me dallo stato del gioco
                me = gstate.players.get(my_index);

                //setto lo stato attuale come quello ricevuto
                g = gstate;

                break;

            case DEAD:
                //crash di un utente
                int p_dead = (Integer) e.params.get("player");

                g.players.get(p_dead).setDead(); //segno che quell'utente non sta più giocando
                System.out.println("Il giocatore "+g.players.get(p_dead).getName()+" ("+p_dead+") è MORTO!");

                break;

        }


    }


    public void sendUpdates() throws RemoteException {

        instance.setState(g); //settiamo lo stato attuale nell'interfaccia remota



        for(int i = 0; i < g.getNPlayer(); i++){

            if(i != my_index){ //mandiamo a tutti, tranne a me stesso

                String name = "player." + g.players.get(i).getUuid();
                try {

                    Registry registry;
                    if(local) registry = LocateRegistry.getRegistry(g.players.get(i).getIp(), 50000+1+i);
                    else registry = LocateRegistry.getRegistry(g.players.get(i).getIp(), 50000);
                    r_game = (RemoteGame) registry.lookup(name);

                    Queue<GameEvent> queue = instance.getUpdates();
                    r_game.sendUpdates(queue); //invia la lista degli eventi ad ogni client

                } catch (Exception e) {
                    System.err.println("sendUpdate() exception: ");
                    e.printStackTrace();

                }

            }
        }

        instance.clearUpdates(); //rimuoviamo dalla nostra coda gli eventi inviati

    }

    public boolean checkIsAlive(int i){

        boolean out = true;

        if(g.players.get(i).isPlaying()) { // se non ho già segnalato che è morto...


            String name = "player." + g.players.get(i).getUuid();

            try {

                Registry registry;
                if(local) registry = LocateRegistry.getRegistry(g.players.get(i).getIp(), 50000+1+i);
                else registry = LocateRegistry.getRegistry(g.players.get(i).getIp(), 50000);
                r_game = (RemoteGame) registry.lookup(name);

                r_game.isAlive();
                
                System.out.println("isAlive(): player "+g.players.get(i).getName()+" ("+i+") is OK.");


            } catch (Exception e) {

                out = false; //usato per UI

                System.err.println("isAlive() exception: player "+g.players.get(i).getName()+" ("+i+") is not responding!!");
                e.printStackTrace();

                //GESTIONE DEI CRASH

                if(i == getPturn()) //SE E' CRASHATO IL GIOCATORE IN TURNO
                {
                    //me.setHand(g.players.get(my_index).getHand()); //TODO CHECK!!

                    int i_next = nextRound(getPturn()); //vediamo chi sarebbe il giocatore successivo nella lista dei giocatori

                    //per tutti i giocatori, a partire da quello successivo, vedo se sono morti o no
                    // calcolo il successivo finché non ne trovo uno vivo
                    boolean next_found = false;

                    for(int y = 0; ((y < g.getNPlayer()) && !next_found); y++ ) {


                        if ( g.getPlayers().get(i_next).isPlaying()) { //se quello a cui dovrei passare il turno e' ancora vivo
                            next_found = true;
                        } else { //se invece quello a cui sto passando il turno e' morto

                            i_next = nextRound(i_next); //calcolo IL NEXT DEL NEXT sul mio stato attuale del gioco (senza morti)

                        }

                    }

                    //salvo il player successivo per calcolarne il nuovo indice dopo l'aggiornamento della lista dei giocatori
                    Player next = g.players.get(i_next);

                    handleDeadPlayer(i); //rimuovo il giocatore di turno dalla lista dei giocatori

                    my_index = g.players.indexOf(me); //ottengo il nuovo indice di gioco

                    int new_next_index = g.players.indexOf(next); //vedo l'indice del next dopo il bilanciamento

                    Game gstate = new Game();

                    try{

                        gstate = getClonedGame();
                        gstate.setPturn(new_next_index);//settiamo il nuovo next nel nuovo stato di gioco

                    }catch(Exception e1){
                        System.err.println("Clonazione non valida !!!");
                        e1.printStackTrace();
                    }

                    // generiamo l'evento GETSTATE per passare il nostro stato del gioco agli altri giocatori
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("state", gstate);
                    GameEvent gs_evt = new GameEvent(Event.GETSTATE, map);
                    try {
                        instance.pushEvent(gs_evt);
                        sendUpdates();
                    } catch (RemoteException e1) {
                        e1.printStackTrace();
                    }

                    g = gstate; //settiamo il nuovo stato del gioco nel nostro

                }else{

                    //invio a tutti l'evento DEAD
                    notifyDead(i);

                }

            }
        }
        return out;
    }

    public void handleDeadPlayer(int i){

        List<Card> dead_hand = g.players.get(i).getHand();

        //reinserisco le carte in fondo al mazzo
        for(int y = 0; y < dead_hand.size(); y++){
            g.addToDeck(dead_hand.get(y));
        }

        dead_hand.clear(); //rimuovo la mano del giocatore crashato

        g.players.remove(i); //rimuovo il giocatore dalla lista di gioco

    }

    public void notifyDead(int i){

            g.players.get(i).setDead(); //segnalo che è morto

            // generiamo l'evento DEAD per notificare il giocatore crashato
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("player", i);
            map.put("player_obj", g.players.get(i));
            GameEvent evt = new GameEvent(Event.DEAD, map);
            try {

                instance.pushDead(evt); //inserisco l'evento nella mia coda dei morti

            } catch (RemoteException e) {
                e.printStackTrace();
            }

            for (int y = 0; y < g.getNPlayer(); y++) {

                if ((y != i) && (y != my_index)) { //inviamo a tutti, tranne a me (perché ce l'ho già nella mia coda degli eventi) e a quello morto

                    String name = "player." + g.players.get(y).getUuid();
                    try {

                        Registry registry;
                        if(local)  registry = LocateRegistry.getRegistry(g.players.get(y).getIp(), 50000+1+y);
                        else registry = LocateRegistry.getRegistry(g.players.get(y).getIp(), 50000);
                        r_game = (RemoteGame) registry.lookup(name);

                        r_game.pushDead(evt); //invia la lista degli eventi ad ogni client

                    } catch (Exception e) {
                        System.err.println("notifyDead() exception: ");
                        e.printStackTrace();

                    }
                }
            }

    }

    public boolean isMyTurn(){return (my_index==getPturn());}
    public int getPturn(){ return g.getPturn();}
    public void setPturn(int i) { g.setPturn(i); }
    public Deck getDeck(){ return g.getDeck();}
    public int getNPlayer() { return g.getNPlayer();}
    public boolean getReverse(){return g.reverse;}
    public Color getExtraColor(){return g.extra_col;}
    public void setExtraColor(Color c) { g.setExtraColor(c);}
    public void setSkip(boolean b) {g.setSkip(b);}
    public int nextRound(int p_turn){ return g.nextRound(p_turn);}
    public List<Player> getPlayers() {return g.getPlayers();}
    //public void execEffect(Action a){ g.execEffect(a);}
    public Player getMe(){return me;}
    public Game getGame(){return g;}
    public Game getClonedGame() throws CloneNotSupportedException {return g.clone();}
    public boolean getShowColors(){return g.getShowColors();}
    public void setShowColors(boolean b){g.setShowColors(b);}
    public int getMyIndex(){return my_index;}
    public void setMyIndex(int new_i){ my_index = new_i;} 
    public GameInstance getInstance(){return instance;}
    public void setFinish(boolean b){g.finish=b;}


}
