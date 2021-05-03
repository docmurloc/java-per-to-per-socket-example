/*
 * The MIT License
 *
 * Copyright 2021 pierre.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package docmurloc.clientServer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.LinkedList;
import java.util.Queue;
import java.util.HashMap;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author pierre
 */
public final class SimpleServerSocket implements Runnable {

    private final String roomAll = "ALL";
    private ServerSocket listener = null;
    private final Queue<SimpleClientSocket> incomingConnection = new LinkedList<>();
    private final HashMap<String, RoomServer> room = new HashMap<String, RoomServer>();
    private boolean isRunning = false;
    private Thread t = null;
    private final ReentrantLock lock = new ReentrantLock();

    public SimpleServerSocket(int port) {
        try {
            this.listener = new ServerSocket(port);

            this.createRoom(this.roomAll);
        } catch (IOException e) {
            System.out.println(e);
            System.exit(1);
        }
    }

    public boolean isRoomCreated(String name) {
        return this.room.get(name) != null;
    }

    public void createRoom(String name) {

        if (!this.isRoomCreated(name)) {
            RoomServer newRoom = new RoomServer(name);

            this.room.put(name, newRoom);
        }
    }

    public void deleteRoom(String name) {
        if (!this.isRoomCreated(name)) {
            this.room.remove(name);
        }
    }

    @Override
    public void run() {

        while (this.isRunning) {
            this.acceptSocket();
        }

    }

    private void acceptSocket() {
        try {
            Socket incomingSocket = listener.accept();

            SimpleClientSocket newClient = new SimpleClientSocket(incomingSocket);

            this.lock.lock();
            try {
                this.incomingConnection.add(newClient);
                this.linkSocketToRoom(this.roomAll, newClient);
            } finally {
                this.lock.unlock();
            }

        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        }

    }

    public void sendMessageToAll(String message) {
        this.room.get(this.roomAll).sendMessageToAll(message);
    }

    public void sendMessageToAll(char[] message) {
        this.room.get(this.roomAll).sendMessageToAll(message);
    }
    
    public void sendDataToAll(byte[] data) {
        this.room.get(this.roomAll).sendDataToAll(data);
    }

    public void sendMessageToRoom(String message, String destinedRoom) {
        if (this.isRoomCreated(destinedRoom)) {
            this.room.get(destinedRoom).sendMessageToAll(message);
        }
    }

    public void sendMessageToRoom(char[] message, String destinedRoom) {
        if (this.isRoomCreated(destinedRoom)) {
            this.room.get(destinedRoom).sendMessageToAll(message);
        }
    }
    
    public void sendDataToRoom(byte[] data, String destinedRoom) {
        if (this.isRoomCreated(destinedRoom)) {
            this.room.get(destinedRoom).sendDataToAll(data);
        }
    }

    public boolean isNewConnection() {
        return this.incomingConnection.size() > 0;
    }

    public SimpleClientSocket getNewSocketAccepted() {

        SimpleClientSocket result = null;

        this.lock.lock();
        try {
            if (this.incomingConnection.size() > 0) {
                result = this.incomingConnection.remove();
            }
        } finally {
            this.lock.unlock();
        }
        return result;

    }

    public void linkSocketToRoom(String name, SimpleClientSocket client) {
        if (!this.isRoomCreated(name)) {
            this.createRoom(name);
        }

        this.room.get(name).addClient(client);
    }

    public void unlinkSocketFromRoom(String name, int id) {
        if (this.isRoomCreated(name)) {
            this.room.get(name).removeClient(id);
        }
    }

    public void unlinkSocketFromRoom(String name, SimpleClientSocket client) {
        if (this.isRoomCreated(name)) {
            this.room.get(name).removeClient(client);
        }
    }

    public void start() {
        if (this.t == null) {
            this.t = new Thread(this, "threadSocketServer");

            this.isRunning = true;

            this.t.start();
        }
    }

    public void stop() {
        if (this.t != null) {
            this.isRunning = false;

            try {
                this.listener.close();
            } catch (IOException e) {
                System.out.println(e);
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {

        SimpleServerSocket server = new SimpleServerSocket(9999);

        server.start();

        System.out.println("Waiting new connection");

        while (!server.isNewConnection()) {

            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }

        SimpleClientSocket client = server.getNewSocketAccepted();

        char[] message;
        int loop = 0;

        do {
            if (client.isNewMessage()) {
                message = client.readMessage();

                System.out.println("Message get = " + String.valueOf(message));
                loop++;
            } else {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        } while (loop < 4);

        loop = 0;

        client.sendMessage("test");

        server.sendMessageToAll("all 1");
        server.sendMessageToRoom("all room 2", "ALL");
        server.sendMessageToRoom("all room 3", "ALL");

        server.stop();

        System.out.println("Sever stopped!");
    }

}
