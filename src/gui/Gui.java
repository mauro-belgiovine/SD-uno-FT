package gui;

import game.*;
import game.Action;
import net.*;
import net.Event;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Date;

import static java.lang.Thread.sleep;

// cd Documents/workspace/SD-uno/out/production/SD-uno/
public class Gui extends JPanel{

    static Match my_match;

    // VARIABILI:
    private int free_space, show_space, diff, n_cards, card_index=-1;
    private boolean picked=false, played=false;
    private boolean playable=false;
    private game.Color chosen_color= game.Color.NONE;
    boolean endGameGUI = false;

    //private Player me = my_match.getMe(); // giocatore locale

    //List<Player> players = my_match.getPlayers(); // lista giocatori
    //private int players_size = my_match.getNPlayer();

    private Image retro = new ImageIcon("cards/999.png").getImage(); //retro carta
    private Image clockwise = new ImageIcon("icons/clockwise.png").getImage();
    private Image cclockwise = new ImageIcon("icons/cclockwise.png").getImage();
    private Image arrow = new ImageIcon("icons/arrowup.png").getImage();

    private Toolkit toolkit = Toolkit.getDefaultToolkit();
    private Image image = toolkit.getImage("icons/denied.png");
    private Cursor denied = toolkit.createCustomCursor(image , new Point(0,0), "img");

    //*********************************//
    //////// FUNZIONI:

    private boolean isMyTurn(){return (my_match.getMyIndex()==my_match.getPturn());}
    //public void UiRepaint(){repaint();}

    public Gui() {

        // Listener per le azioni di click con cursore del mouse
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                //System.out.println("x="+e.getX()+" , y="+e.getY());
            }

            @Override
            public void mouseReleased(MouseEvent e) {}

            @Override
            public void mousePressed(MouseEvent e) {

                if (isMyTurn()) {
                    int x = e.getX();
                    int y = e.getY();
                    Card c;
                    //Deck d = my_match.getDeck();
                    Game g = my_match.getGame();

                    // viene GIOCATA UNA CARTA, se e' giocabile e non e' stata giocata gia' un'altra carta
                    if (checkHandRange(x, y) && playable && !played) {
                        c = my_match.getMe().throwCard(card_index); // rimuove la carta dalla mano del giocatore
                        g.card2Table(c);     // (gioca) - mette la carta sul tavolo

                        // funzione per gestire le azioni speciali:
                        specialActions(c);

                        played = true;
                        setCursor(denied);

                        //generiamo l'evento THROW
                        Map<String, Object> m = new HashMap<String, Object>();
                        m.put("player", my_match.getMyIndex());
                        m.put("card_i", card_index);
                        GameEvent evt = new GameEvent(Event.THROW, m);
                        try {
                            my_match.getInstance().pushEvent(evt);
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }

                        // se il giocatore ha ancora carte in mano
                        if(my_match.getMe().getHand().size()>0) {

                            if (!my_match.getShowColors()) goToNextRound();
                            else repaint();
                        }
                        // se il giocatore ha finito le carte, ha vinto
                        else {

                            //generiamo l'evento FINISH
                            Map<String, Object> m1 = new HashMap<String, Object>();
                            m1.put("player", my_match.getMyIndex());
                            GameEvent evt1 = new GameEvent(Event.FINISH, m1);
                            try {
                                my_match.getInstance().pushEvent(evt1);
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }

                            // ULTIMA SENDUPDATE per comunicare a tutti che la partita è finita
                            try {
                                my_match.sendUpdates();
                            } catch (RemoteException e1) {
                                e1.printStackTrace();
                            }
                            my_match.setFinish(true);
                            repaint();
                        }

                    }

                    // viene PESCATA UNA CARTA, se non e' stata gia' pescata un'altra carta
                    if (checkStackRange(x, y) && !picked) {
                        c = g.popCard();
                        my_match.getMe().card2Hand(c);
                        picked = true;
                        setCursor(denied);

                        // generiamo l'evento PICKUP
                        Map<String, Object> m = new HashMap<String, Object>();
                        m.put("player", my_match.getMyIndex());
                        GameEvent evt = new GameEvent(net.Event.PICKUP, m);
                        try {
                            my_match.getInstance().pushEvent(evt); //aggiungi questo evento alla coda
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }

                        if (checkPlayableHand(my_match.getMe())) repaint();
                        else goToNextRound();
                    }

                    // viene SCELTO IL COLORE di una carta speciale
                    if (checkColorRange(x, y)) { // checkColorRange imposta anche chosen_color
                        my_match.setExtraColor(chosen_color);
                        chosen_color = game.Color.NONE;
                        my_match.setShowColors(false);

                        // generiamo l'evento di EXTRA_COL
                        Map<String, Object> m_extra = new HashMap<String, Object>();
                        m_extra.put("extra", my_match.getExtraColor());
                        GameEvent e_extra = new GameEvent(Event.EXTRA_COL, m_extra);
                        try {
                            my_match.getInstance().pushEvent(e_extra);
                        } catch (RemoteException e1) {
                            e1.printStackTrace();
                        }

                        goToNextRound();
                    }


                } // fine isMyTurn

            }// fine MouseClicked
        }); // fine MouseListener

        // Listener per il movimento del cursore del mouse
        // Cambia l'icona del cursore del mouse in base alle operazioni eseguibili (giocare,pescare,..)
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {

                if (isMyTurn()) {

                    int x = e.getX();
                    int y = e.getY();
                    Deck d = my_match.getDeck();

                    // se il cursore e' posto su una carta della mano, cambia l'icona in giocabile/non_giocabile
                    if (checkHandRange(x,y)) {

                        // se e' un carta giocabile
                        playable = my_match.getMe().checkPlayable(card_index, d.last_c, my_match.getExtraColor());
                        if( playable && !played) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                        else setCursor(denied);
                    }
                    // se il cursore e' invece sul mazzo, cambia l'icona in pescabile/non_pescabile (gia' pescato)
                    else if (checkStackRange(x,y)){
                        if (!picked) {
                            setCursor(new Cursor(Cursor.HAND_CURSOR));
                        }
                        else setCursor(denied);

                    }

                    // se invece e' il caso di scelta colore, ed il cursore si trova su un colore
                    else if ( checkColorRange(x,y)){
                        setCursor(new Cursor(Cursor.HAND_CURSOR));
                    }

                    else setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }

            } // fine MouseMoved
        }); // fine MouseMotionListere
    }

    // INPUT  --> il java graphic
    // COSA FA--> funzione principale che si occupa di disegnare tutto, richiamando tutte le altre funzioni
    public void paintComponent(Graphics g) {

    	if(endGameGUI) {
		try {sleep(5000);
		System.exit(0);
		}
		catch (Exception e){}
	}


        super.paintComponent(g);
        String name = my_match.getMe().getName();
        int ln = name.length();
        Font f = new Font("Serif Plain", Font.BOLD, 15);
        g.setFont(f);


        // 3 sezioni principali
        this.setBackground(new Color(0,76,153));
            g.setColor(new Color(70,155,239));
        g.fillRect(10,10,745,160);
            g.setColor(new Color(102,178,255));
        g.fillRoundRect(695,345,50,40,20,20);
        g.fillRect(10,170,745,200);
            g.setColor(new Color(153,204,255));
        g.fillRoundRect(10,345,20+(ln*10),40,20,20);
        g.fillRect(10,370,745,200);

        // disegna le carte e le informazioni
        drawHand(g,my_match.getMe());
        drawPlayerInfo(g,name);
        drawTable(g);
        drawRivalsInfo(g,f);

        // indicatore direzione turno
        if (!my_match.getReverse()) g.drawImage(clockwise,20,185,45,45,this);
        else g.drawImage(cclockwise,20,185,45,45,this);

	

        // se non e' il mio turno, applico un filtro oscurato alle carte in mano
        if(!isMyTurn()) {
            g.setColor(new Color(64,64,64,100));
            g.fillRect(10,370,745,200);
        }

        // se e' stata giocata una carta speciale, bisogna scegliere un colore
        if (my_match.getShowColors()) drawColorSelection(g);

        // se e' stato impostato un colore per una carta speciale
        if(my_match.getExtraColor() != game.Color.NONE) drawExtraColor(g);

        // se la partita è finita
        if ( my_match.getGame().isFinish() ) endGameNotify(g);
	

    }

    // INPUT  --> il java graphic e il gicatore corrente
    // COSA FA--> disegna le carta in mano al giocatore, sovrapponendole nel caso siano > 7
    private void drawHand(Graphics gr, Player pl){

        Image im;
        String pngname;
        List<Card> hand = (my_match.getMe()).getHand();
        int x=10, y=395;
        int card_x = 95, card_y=150;
        n_cards = pl.getNumCards();      // numero carte in mano al giocatore
        //n_cards = hand.size();
        free_space= 745-(card_x*n_cards); // totale spazio libero

        // se le carte non sono sovrapposte - massimo 7 carte
        if (free_space>1){
            int total_free = free_space;
            free_space =(int) ( Math.floor ( ((double)free_space)/(n_cards+1)) );  // porzione di spazio libero tra una carta e l'altra
            diff = total_free - (free_space*(n_cards+1));
            x+=free_space;
            for (int i=0;i<n_cards;i++) {
                    pngname = (hand.get(i)).getId();
                    im = new ImageIcon("cards/" + pngname + ".png").getImage();
                    gr.drawImage(im, x, y, card_x, card_y, this);
                    x += free_space + card_x;
            }
        }

        // se le carte sono sovrapposte - minimo 8 carte
        else{
            int draw_space = 725-card_x;            // spazio di una carta interna e n-1 sovrapposte
            show_space= (int) ( Math.floor( ((double)draw_space)/(n_cards-1)) );
            diff = draw_space - (show_space*(n_cards-1));
            int local_diff = diff;
            x+=10;  //margine

            // disegna la prima carta
            pngname = (hand.get(0)).getId();
            im = new ImageIcon("cards/"+pngname+".png").getImage();
            gr.drawImage(im, x, y, card_x, card_y, this);

            for (int i=1;i<n_cards;i++) {

                pngname = (hand.get(i)).getId();
                im = new ImageIcon("cards/"+pngname+".png").getImage();

                if (local_diff>0){    //recupero lo spazio perso dall'arrotondamento
                    x+=show_space+1;
                    gr.drawImage(im, x, y, card_x, card_y, this);
                    local_diff--;
                }
                else{
                    x+=show_space;
                    gr.drawImage(im, x, y, card_x, card_y, this);
                }
            }
        }
    }

    // INPUT   --> graphic, nome giocatore
    // COSA FA --> disegna le informazioni del giocatore (nome)
    private void drawPlayerInfo(Graphics gr, String name){

        gr.setColor(new Color(0,76,153));
        gr.drawString(name, 20, 364);
        
     // mostro indice
        String ind = Integer.toString(my_match.getMyIndex());
        gr.drawString("index=", 20, 295);
        gr.drawString(ind, 95, 295);

        // mostro il numero di carte in mano
        List<Card> hand = (my_match.getMe()).getHand();
        String nc = Integer.toString(hand.size());
        gr.drawString("n_cards=", 20, 315);
        gr.drawString(nc, 95, 315);

        // mostro l'inidice di chi è il turno
        String t = Integer.toString(my_match.getPturn());
        gr.drawString("p_turn=", 20, 335);
        gr.drawString(t, 85, 335);



    }

    // INPUT  --> il java graphic
    // COSA FA--> disegna le carte sul tavolo: mazzo con carta retro e la carta scoperta
    private void drawTable(Graphics gr){

        Image im;
        Deck deck = my_match.getDeck();
        int x=272, y=195;
        int card_x = 95, card_y=150;
        String pngname;

        // disegna il mazzo - carte retro
        //im = new ImageIcon("cards/999.png").getImage();
        for (int i=0;i<2;i++) {
            gr.drawImage(retro, x, y, card_x, card_y, this);
            x+=10; //spazio tra le carte
        }
        x += card_x;

        // disegna la carta scoperta
        if ( deck.table.size() > 1){
            x+=10;
            pngname = deck.getPrevTableCard();
            im = new ImageIcon("cards/" + pngname + ".png").getImage();
            gr.drawImage(im, x, y, card_x, card_y, this);
            x-=10;
            pngname = deck.getTableCard();
            im = new ImageIcon("cards/" + pngname + ".png").getImage();
            gr.drawImage(im, x, y, card_x, card_y, this);

        }
        else {
            pngname = deck.getTableCard();
            im = new ImageIcon("cards/" + pngname + ".png").getImage();
            gr.drawImage(im, x, y, card_x, card_y, this);
        }

    }

    // INPUT  --> graphic e font
    // COSA FA--> disegna nell'area sopra le informazioni relative agli avversari: retro carta, nome e numero di carte
    private void drawRivalsInfo(Graphics gr, Font f){

        int next_pl = (my_match.getMyIndex()+1)%(my_match.getNPlayer()); //indice avversario successivo
        Font f2;
        String name =  ((my_match.getPlayers()).get(next_pl)).getName();  //nome primo avversario
        String numb = String.valueOf (((my_match.getPlayers()).get(next_pl)).getNumCards()); // numero di carte avversario

        f = new Font("Serif Plain", Font.BOLD, 14);
        f2 = new Font("Serif Plain", Font.BOLD, 15);
        gr.setFont(f);
        gr.setColor(new Color(0,76,153));

        int x=10,y=24;
        int card_x=64, card_y=94, name_x, name_y=138, numb_x=8, numb_y=45;
        int arrow_x,arrow_y=140;
        int n=my_match.getNPlayer()-1;  //numeri di avversari (n-1)
        int r_free_space= 745-(card_x*n);  // totale spazio libero, tolti mazzi degli altri
        r_free_space = r_free_space/(n+1);  // porzione di spazio libero tra un mazzo e l'altro
        x+=r_free_space;
        arrow_x = x+16;

        // info primo avversario
        int l_name = name.length();
        name_x = (int) ((card_x-(l_name*7.8))/2);
        //numb_x = (int ) ((card_x-(l_numb*8.2))/2);
        gr.drawImage(retro, x, y, card_x, card_y, this); // disegna il mazzo avversario
        if(my_match.getPturn()==next_pl) gr.drawImage(arrow,arrow_x, arrow_y, 32, 32,this);
        gr.drawString(name, x+name_x,name_y);   // scrive il nome
        gr.setFont(f2);                     // font piu' grande per il numero
        gr.setColor(new Color(250,250,250));
        gr.drawString(numb, x+numb_x,numb_y);   // scrive il numero di carte
        gr.setFont(f);                      // ripristino font nome
        x+=r_free_space+card_x;
        arrow_x = x+16;
        n--; // giocatori escluso il locale ed il successivo gia' disegnato

        // altri eventuali avversari
        for (int i=0; i<n;i++){
            next_pl = (next_pl+1)%(my_match.getNPlayer());
            name =  ((my_match.getPlayers()).get(next_pl)).getName();
            numb = String.valueOf (((my_match.getPlayers()).get(next_pl)).getNumCards());
            l_name = name.length();
            //l_numb = numb.length();
            name_x = (int) ((card_x-(l_name*7.8))/2);
            //numb_x = (card_x-(l_numb*8))/2;
            gr.drawImage(retro, x, y, card_x, card_y, this);
            if(my_match.getPturn()==next_pl) gr.drawImage(arrow,arrow_x, arrow_y, 32, 32,this);
            gr.setColor(new Color(0,76,153));
            gr.drawString(name, x+name_x,name_y);
            gr.setFont(f2);
            gr.setColor(new Color(250,250,250));
            gr.drawString(numb, x+numb_x,numb_y);   // scrive il numero di carte
            gr.setFont(f);
            x+=r_free_space+card_x;
            arrow_x = x+16;
        }

    }

    // INPUT  --> coordinate cursore mouse
    // COSA FA --> oscura l'area di gioco e mostra la selezione colore carta
    private void drawColorSelection(Graphics gr){

        // disabilita il resto del gioco finche' non viene scelto un colore
            gr.setColor(new Color(64,64,64,160));
        gr.fillRect(10,10,745,560);

        String select = "Scegli il colore della carta:";
            gr.setColor(new Color(240,240,240));
        gr.drawString(select,46,254);

            gr.setColor(new Color(64,64,64));
        gr.fillRoundRect(35,274,32,32,10,10);
            gr.setColor(new Color(229,0,28));
        gr.fillRoundRect(36,275,30,30,10,10);

            gr.setColor(new Color(64,64,64));
        gr.fillRoundRect(95,274,32,32,10,10);
            gr.setColor(new Color(16,129,209));
        gr.fillRoundRect(96,275,30,30,10,10);

            gr.setColor(new Color(64,64,64));
        gr.fillRoundRect(155,274,32,32,10,10);
            gr.setColor(new Color(254,217,11));
        gr.fillRoundRect(156,275,30,30,10,10);

            gr.setColor(new Color(64,64,64));
        gr.fillRoundRect(215,274,32,32,10,10);
            gr.setColor(new Color(20,154,63));
        gr.fillRoundRect(216,275,30,30,10,10);
    }

    // INPUT  --> graphic
    // COSA FA --> disegna il colore impostato di un carta speciale
    private void drawExtraColor(Graphics gr){

         /*   gr.setColor(new Color(0,76,153));
        gr.drawString("Colore scelto:", 320, 362); */

            gr.setColor(new Color(64,64,64));
        gr.fillRoundRect(502,254,32,32,10,10);

        switch ((game.Color) my_match.getExtraColor()){
            case RED:
                gr.setColor((java.awt.Color) new Color(234,33,45));
                break;
            case BLUE:
                gr.setColor((java.awt.Color) new Color(24,150,215));
                break;
            case YELLOW:
                gr.setColor((java.awt.Color) new Color(254,220,50));
                break;
            case GREEN:
                gr.setColor((java.awt.Color) new Color(24,165,84));
                break;

            default:
                //
        }

        gr.fillRoundRect(503,255,30,30,10,10);

    }

    // INPUT  --> coordinate cursore mouse
    // OUTPUT --> true se il cursore e' su una delle carte in mano al giocatore
    private boolean checkHandRange(int x, int y){
        Card c;
        // se il click e' nel range y corretto
        if ((y > 395) && (y < 545) && !(my_match.getShowColors()) ) {
            int step ;
            // se le carte non sono sovrapposte
            if (free_space > 1) {
                step = 10+free_space;
                for (int i = 0; i < n_cards; i++) {    // controllo il range x
                    if (x > step && x < (step + 95)) {
                        card_index=i;
                        return true;
                    }
                    step += 95+free_space;
                }
                return false;
            }

            // se le carte sono sovrapposte
            else {
                step=20;
                int local_diff = 0, extra=0;
                if (diff>0) local_diff = diff;
                if (local_diff>0) extra=1;
                for (int i = 0; i < (n_cards-1); i++) {    // controllo il range x

                    if (x > step && x < (step + show_space + extra)) {
                        card_index=i;
                        return true;
                    }
                    local_diff--;
                    if(local_diff<1) extra=0;
                    step += show_space+extra;
                }
                //l'ultima carta e' completamente disegnata
                if (x > step && x < (step + 95)) {
                    card_index=(n_cards-1);
                    return true;
                }
                return false;
            }
        }
        return false;
    }


    // INPUT  --> coordinate cursore mouse
    // OUTPUT --> true se il cursore e' sul mazzo per pescare
    private boolean checkStackRange(int x, int y){

        // se il click e' nel range y corretto
        return ((y > 195) && (y < 346) && (x>282) && (x<379) & !(my_match.getShowColors()) );
    }

    // INPUT  --> coordinate cursore mouse
    // OUTPUT --> true se il cursore e' sulla scleta colore
    private boolean checkColorRange(int x, int y){

        // se il click e' nel range y corretto
        if ( y>274 && y<305 && (my_match.getShowColors()) ) {
            int step = 35;
            for(int i=0;i<4;i++){
                if( x>step && x<step+32){
                    switch (i){
                        case 0:
                            chosen_color= game.Color.RED;
                            break;

                        case 1:
                            chosen_color= game.Color.BLUE;
                            break;

                        case 2:
                            chosen_color= game.Color.YELLOW;
                            break;

                        case 3:
                            chosen_color= game.Color.GREEN;
                            break;

                        default:
                            chosen_color= game.Color.NONE;
                    }
                    return true;
                }
                step+=60;
            }
            return false;
        }
        else return false;
    }

    private void specialActions(Card c){

        // scelta extra_color
        if(c.getAction()==Action.DRAW4 || c.getAction()==Action.WILD) my_match.setShowColors(true);

        // gestione skip turn
        if(c.getAction()==Action.DRAW4 || c.getAction()==Action.DRAW2 || c.getAction()==Action.SKIP) my_match.setSkip(true);

    }


    // COSA FA --> calcola il giocatore successivo e passa il turno e gestisce il ribilanciamento dello stato di gioco se qualche player e' crashato
    private void goToNextRound(){

        List<Card> hand = my_match.getMe().getHand(); //TODO anche questo dovrebbe essere inutile?
        my_match.getGame().getPlayers().get(my_match.getMyIndex()).setHand(hand); //aggiorno la mia mano sullo stato del gioco

        played = picked = false;

        int i_next = my_match.nextRound(my_match.getPturn()); //calcolo next round sul mio stato attuale del gioco (senza morti)
        my_match.setSkip(false);



        if(!my_match.getInstance().anyDead()){ //se non ci sono morti

            //generiamo l'evento TURN
            Map<String, Object> m = new HashMap<String, Object>();
            m.put("next", i_next);
            GameEvent evt = new GameEvent(Event.TURN, m);
            try {
                my_match.getInstance().pushEvent(evt);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }

        }else{ //ci sono morti

            GameEvent dead;

            dead = my_match.getInstance().popDead();

	    	do{

	    		my_match.execEvent(dead); //segno nel mio stato di gioco chi e' morto

                dead = my_match.getInstance().popDead(); //estraggo il prossimo

            }while(dead != null);

            //per tutti i giocatori, a partire da quello successivo, vedo se sono morti o no
            // calcolo il successivo finché non ne trovo uno vivo
            boolean next_found = false;

            for(int i = 0; ((i < my_match.getNPlayer()) && !next_found); i++ ) {


                if (my_match.getPlayers().get(i_next).isPlaying()) { //se quello a cui dovrei passare il turno e' ancora vivo
                    next_found = true;
                } else { //se invece quello a cui sto passando il turno e' morto

                    i_next = my_match.nextRound(i_next); //calcolo IL NEXT DEL NEXT sul mio stato attuale del gioco (senza morti)

                }

            }

            //salvo il player successivo per calcolarne il nuovo indice dopo l'aggiornamento della lista dei giocatori
            Player next = my_match.getPlayers().get(i_next);

            for(int i = 0; i < my_match.getNPlayer(); i++){ //scorro la lista dei giocatori per eliminare quelli morti

                if(!my_match.getPlayers().get(i).isPlaying())  //se ho segnato il giocatore come morto

                    my_match.handleDeadPlayer(i); // elimino dal gioco il player morto

            }

            my_match.setMyIndex(my_match.getPlayers().indexOf(my_match.getMe())); //setto il mio nuovo indice

            int new_next_index = my_match.getPlayers().indexOf(next); //vedo l'indice del next dopo il bilanciamento

            Game gstate = new Game();

            try{

                gstate = my_match.getClonedGame();
                gstate.setPturn(new_next_index);

            }catch(Exception e){
                System.err.println("Clonazione non valida !!!");
                e.printStackTrace();
            }

            // generiamo l'evento GETSTATE per passare il nostro stato del gioco agli altri giocatori
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("state", gstate);
            GameEvent gs_evt = new GameEvent(Event.GETSTATE, map);
            //setto GETSTATE AL POSTO DI TURN!! e tutti riceveranno gli eventi giocati, ma riprenderanno dallo stato del gioco ri-bilanciato
            try {
                my_match.getInstance().pushEvent(gs_evt);
            } catch (RemoteException e1) {
                e1.printStackTrace();
            }

            i_next = new_next_index; //assegnamo new_next_index a i_next per settarlo nel nostro stato dopo il ribilanciamento


        }
    	
    	// aggiornamento dello stato per gli altri giocatori
        try {
            my_match.sendUpdates();
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }

        //IMPORTANTE!! setto il turno nel mio stato attuale del gioco DOPO AVER FATTO LA sendUpdate()
        //altrimenti potrei estrarre i miei eventi prima di averli inviati nella routine di ping/controllo degli eventi in coda
        my_match.setPturn(i_next);

        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));

        repaint();
    }


    // INPUT --> giocatore attuale
    // OUTPUT --> true se almeno una carta del player attuale e' giocabile
    private boolean checkPlayableHand(Player pl){
        Deck d = my_match.getDeck();

        for(int i=0; i<(pl.getHand()).size();i++ ){
            if (pl.checkPlayable(i, d.last_c, my_match.getExtraColor()) ) {
                return true;
            }
        }
        return false;

    }


    // COSA FA --> mostra un messaggio che indica la vittoria/sconfitta del giocatore 
    private void endGameNotify(Graphics gr) {

	// disabilita il resto del gioco f
            gr.setColor(new Color(64,64,64,160));
        gr.fillRect(10,10,745,560);

	// disegno il contenitore della scritta
  	    gr.setColor(new Color(64,64,64));
        gr.fillRoundRect(295,260,180,100,10,10);
            gr.setColor(new Color(153,204,255));
        gr.fillRoundRect(296,261,178,98,10,10);

	// disegno la scritta
	Font f = new Font("Serif Plain", Font.BOLD, 20);
	gr.setFont(f);
	gr.setColor(new Color(0,76,153));
        String st = "Hai vinto!";
	if (! my_match.isMyTurn()) st="Hai perso!";
        gr.drawString(st,328,318);

	endGameGUI = true;
	repaint();
    }


    /////////////////////////////////////////////////////////////////

    public static void main(String[] args) throws InterruptedException{
    	
    
        my_match = new Match();
        my_match.startClient(args);
        // **********************************************************
        JFrame fr = new JFrame("Uno Distribuito");
        //fr.setLayout(new BorderLayout());
        fr.setIconImage(Toolkit.getDefaultToolkit().getImage("cards/400.png"));
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        Gui ui = new Gui();
        fr.add(ui);
        fr.setSize(770,620);
        fr.setVisible(true);
        int x = 0;
  
            do {

                if (!my_match.isMyTurn()) {

                    // controllo se il giocatore successivo è vivo
                    //my_match.checkNextPlayer(my_match.getMyIndex());


                    //prelevo gli eventi
                    GameEvent e;
                    do {
                        e = my_match.getInstance().popEvent();
                        if (e != null) {
                            if (x==0) System.out.println("----------------------------------------");
				
                            System.out.println("ho ricevuto un evento di tipox: " + e.event+" ");
                            switch(e.event){

                                case PICKUP:
                                    System.out.println("player ("+e.params.get("player")+") "+my_match.getPlayers().get((Integer) e.params.get("player")).getName());
                                    break;

                                case THROW:	
                                    System.out.println("player ("+e.params.get("player")+") "+my_match.getPlayers().get((Integer) e.params.get("player")).getName());
                                    System.out.print("carta ("+e.params.get("card_i")+") ");
                                    my_match.getPlayers().get((Integer) e.params.get("player")).getHand().get((Integer) e.params.get("card_i")).printCard();
                                    System.out.println();
                                    break;

                                case TURN:	
                                    System.out.println("next ("+e.params.get("next")+") "+my_match.getPlayers().get((Integer) e.params.get("next")).getName());
                                    break;

                            }
                            my_match.execEvent(e);
                            ui.repaint();
                            x=1;
                        }
                    } while (e != null);
                    x=0;

                }

                if(!my_match.checkIsAlive((my_match.getMyIndex() + 1) % my_match.getNPlayer())){
                	ui.repaint();
                }

                sleep(1000);


            } while (!my_match.getGame().isFinish());

        /*my_match.waitForMyTurn();*/

    
    }


} // fine classe UI
