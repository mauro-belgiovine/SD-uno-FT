package net;

/**
 * Created by mauro on 09/06/16.
 */
public enum Event {
    PICKUP, //  pescata una carta dal mazzo
    THROW,  //   messa una carta sul tavolo
    TURN ,   // cambiato il tavolo
    EXTRA_COL,  // stato scelto un colore (WILD o DRAW4)
    FINISH,
    GETSTATE, // tutti devono aggiornare lo stato di gioco
    DEAD

    //TODO ...?
}
