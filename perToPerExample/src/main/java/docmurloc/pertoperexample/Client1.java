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
package docmurloc.pertoperexample;

import docmurloc.clientServer.SimpleClientSocket;
import docmurloc.clientServer.SimpleServerSocket;

/**
 *
 * @author pierre
 */
public class Client1 {
    public static void main(String args[]) {

        SimpleServerSocket server = new SimpleServerSocket(3001);

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
        
        //final String serverHost = "localhost";
        final int portHost = 3000;

        SimpleClientSocket mySocket = new SimpleClientSocket(client.getInetAdress(), portHost);

        char[] message;
        int loop = 0;
        
        mySocket.sendMessage("test 1");
        mySocket.sendMessage("test 2");
        mySocket.sendMessage("test 3");
        mySocket.sendMessage("QUIT");
        
        client.closeConnection();
        
        mySocket.closeConnection();

        /*do {
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
        server.sendMessageToRoom("all room 3", "ALL");*/

        server.stop();
        
        
         System.out.println("Sever stopped!");
    }
}
