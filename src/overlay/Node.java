package overlay;


import java.io.Serializable;

import java.net.*;
import java.util.UUID;

public class Node implements Serializable {
    String uuid;
    Node prevNode, nextNode;
    String ip;

    public Node() {
        this.uuid = UUID.randomUUID().toString();
        //net = new HashMap <String,Node>  () ;

        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    }

    public String getUuid() {
        return this.uuid;
    }

    public String getIp() {
        return this.ip;
    }

    public void registry() {

    }

    public void bind() {

    }

    public void setNext(Node node) {
        this.nextNode = node;
    }

    public void setPrev(Node node) {
        this.prevNode = node;
    }
}
