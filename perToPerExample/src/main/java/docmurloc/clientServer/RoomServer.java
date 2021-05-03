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

import java.util.ArrayList;

/**
 *
 * @author pierre
 */
public class RoomServer {

    private String name = null;

    private ArrayList<SimpleClientSocket> clients;

    public RoomServer(final String roomName) {
        this.clients = new ArrayList<SimpleClientSocket>();

        this.name = roomName;
    }

    public String getName() {
        return this.name;
    }

    public void addClient(SimpleClientSocket client) {
        this.clients.add(client);
    }

    public ArrayList<SimpleClientSocket> getClientsRoom() {
        return this.clients;
    }

    public boolean isClientInRoom(int id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == id) {
                return true;
            }
        }

        return false;
    }

    public boolean isClientInRoom(SimpleClientSocket client) {

        if (client != null) {
            int id = client.getId();

            return this.isClientInRoom(id);
        }

        return false;
    }

    public int getIndexClient(int id) {
        for (int i = 0; i < this.clients.size(); i++) {
            if (this.clients.get(i).getId() == id) {
                return i;
            }
        }

        return -1;
    }

    public int getIndexClient(SimpleClientSocket client) {

        if (client != null) {
            int id = client.getId();

            return this.getIndexClient(id);
        }

        return -1;
    }

    public SimpleClientSocket getClientByIndex(int index) {
        if (index >= 0 && index < this.clients.size()) {
            return this.clients.get(index);
        }

        return null;
    }

    public void removeClient(int id) {

        int index = this.getIndexClient(id);

        if (index != -1) {
            this.clients.remove(index);
        }

    }

    public void removeClient(SimpleClientSocket client) {

        int index = this.getIndexClient(client);

        if (index != -1) {
            this.clients.remove(index);
        }

    }

    public void sendMessageToAll(String message) {
        for (int i = 0; i < this.clients.size(); i++) {
            this.clients.get(i).sendMessage(message);
        }
    }

    public void sendMessageToAll(char[] message) {
        for (int i = 0; i < this.clients.size(); i++) {
            this.clients.get(i).sendMessage(message);
        }
    }
    
    public void sendDataToAll(byte[] data) {
        for (int i = 0; i < this.clients.size(); i++) {
            this.clients.get(i).sendData(data);
        }
    }
}
