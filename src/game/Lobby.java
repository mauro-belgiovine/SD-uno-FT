package game;

import net.GameInstance;
import net.RemoteGame;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by mauro on 24/05/16.
 */
public class Lobby {

    static String name = "net.Lobby";
    static Registry reg;

    public static void main(String[] args) {

        try {

            GameInstance lobby_game = new GameInstance();
            lobby_game.initGame(); //init a new game
            RemoteGame stub = (RemoteGame) UnicastRemoteObject.exportObject(lobby_game, 50000);

            // Bind the remote object's stub in the registry
            reg = LocateRegistry.createRegistry(50000);
            reg.bind(name, stub);

            System.out.println("net.Lobby bound");


        } catch (Exception e) {
            System.err.println("net.Lobby exception:");
            e.printStackTrace();
        }
    }

}
