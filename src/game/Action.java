package game;

public enum Action {
    DRAW2,      //pesca 2 carte
    DRAW4,      //pesca 4 carte / cambia colore
    REVERSE,     //inverti giro di mano
    SKIP,       //salta un turno
    WILD, //cambia colore
    NONE;       //nessuna azione
}