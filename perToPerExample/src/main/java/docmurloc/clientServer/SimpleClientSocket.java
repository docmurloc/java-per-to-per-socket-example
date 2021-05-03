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

import java.io.*;
import java.net.*;

import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author pierre
 */
public class SimpleClientSocket {

    private static int newId = 0;

    private Socket socket = null;
    private int id;

    private BufferedWriter os;
    private OutputStream rawOs;

    private BufferedReader is;
    private InputStream rawIs;
    private final ReentrantLock lock = new ReentrantLock();

    private byte[] toBytes(int i) {
        byte[] result = new byte[4];

        result[0] = (byte) (i >> 24);
        result[1] = (byte) (i >> 16);
        result[2] = (byte) (i >> 8);
        result[3] = (byte) (i);

        return result;
    }

    private int fromByteArray(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24)
                | ((bytes[1] & 0xFF) << 16)
                | ((bytes[2] & 0xFF) << 8)
                | ((bytes[3] & 0xFF) << 0);
    }

    public InetAddress getInetAdress() {
        if (this.socket != null) {
            return this.socket.getInetAddress();
        }
        
        return null;
    }
    
    public int getPort() {
        if (this.socket != null) {
            return this.socket.getPort();
        }
        
        return 0;
    }
    
    public SimpleClientSocket(String host, int port) {

        int buffer = 0;

        try {
            this.socket = new Socket(host, port);

            System.out.println("socket connected to " + host + " at " + port);

            this.rawOs = this.socket.getOutputStream();
            this.os = new BufferedWriter(new OutputStreamWriter(this.rawOs));

            this.rawIs = this.socket.getInputStream();
            this.is = new BufferedReader(new InputStreamReader(this.rawIs));

            this.lock.lock();
            try {
                buffer = this.newId++;
            } finally {
                this.lock.unlock();
            }

            this.id = buffer;

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
        }

    }

    public SimpleClientSocket(InetAddress address, int port) {

        String host = address.getHostAddress();
        int buffer = 0;
        
        System.out.println("socket connected to " + host + " at " + port);


        try {
            this.socket = new Socket(address, port);

            this.rawOs = this.socket.getOutputStream();
            this.os = new BufferedWriter(new OutputStreamWriter(this.rawOs));

            this.rawIs = this.socket.getInputStream();
            this.is = new BufferedReader(new InputStreamReader(this.rawIs));

            this.lock.lock();
            try {
                buffer = this.newId++;
            } finally {
                this.lock.unlock();
            }

            this.id = buffer;

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host);
        }

    }

    public SimpleClientSocket(Socket newSocket) {

        InetAddress connectAdress = newSocket.getInetAddress();

        String host = connectAdress.getHostAddress();
        int port = newSocket.getPort();

        int buffer = 0;

        try {
            this.socket = newSocket;

            this.rawOs = this.socket.getOutputStream();
            this.os = new BufferedWriter(new OutputStreamWriter(this.rawOs));

            this.rawIs = this.socket.getInputStream();
            this.is = new BufferedReader(new InputStreamReader(this.rawIs));

            this.lock.lock();
            try {
                buffer = this.newId++;
            } finally {
                this.lock.unlock();
            }

            this.id = buffer;

        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host + " and " + port);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + host + " and " + port);
        }

    }

    public void sendMessage(char[] message) {

        String lenMessage = Integer.toString(message.length);

        try {
            this.os.write(lenMessage);
            this.os.newLine();
            this.os.flush();
            this.os.write(message, 0, message.length);
            this.os.flush();

        } catch (IOException e) {
            System.err.println("Couldn't send message I/O" + message);
        }
    }

    public void sendData(byte[] message) {

        byte[] lenMessage = this.toBytes(message.length);

        try {
            this.rawOs.write(lenMessage, 0, lenMessage.length);
            this.rawOs.flush();

            this.rawOs.write(message, 0, message.length);
            this.rawOs.flush();

        } catch (IOException e) {
            System.err.println("Couldn't send message I/O" + message);
        }
    }

    public void sendMessage(String message) {
        this.sendMessage(message.toCharArray());
    }

    public int getId() {
        return this.id;
    }

    public char[] readMessage() {
        String lenLine = null;
        char[] message = null;

        if (!this.isNewMessage()) {
            return null;
        }

        try {

            lenLine = this.is.readLine();

            if (lenLine != null) {
                message = new char[Integer.parseInt(lenLine)];

                this.is.read(message);
            }
        } catch (IOException e) {
            System.err.println("Couldn't read message I/O");
        }

        return message;
    }

    public byte[] readData() {
        byte[] lenLine = null;
        int lenMessage;
        byte[] message = null;

        if (!this.isNewMessage()) {
            return null;
        }

        try {
            lenLine = this.rawIs.readNBytes(4);
            lenMessage = this.fromByteArray(lenLine);

            if (lenLine != null) {
                message = new byte[lenMessage];

                this.rawIs.readNBytes(message, 0, lenMessage);
            }
        } catch (IOException e) {
            System.err.println("Couldn't read message I/O");
        }

        return message;
    }

    public boolean isNewMessage() {
        try {
            return this.is.ready();
        } catch (IOException e) {
            System.err.println("Couldn't read message I/O");
            return false;
        }
    }

    public void closeConnection() {
        try {
            os.close();
            is.close();
            this.socket.close();
        } catch (IOException e) {
            System.err.println("Couldn't close I/O connection");
            return;
        }
    }

    public static void main(String[] args) {

        final String serverHost = "localhost";
        final int portHost = 9999;

        SimpleClientSocket mySocket = new SimpleClientSocket(serverHost, portHost);

        mySocket.sendMessage("test 1");
        mySocket.sendMessage("test 2");
        mySocket.sendMessage("test 3");
        mySocket.sendMessage("QUIT");

        char[] message;
        int loop = 0;

        do {
            System.out.println("waiting for message");

            if (mySocket.isNewMessage()) {
                message = mySocket.readMessage();

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

        System.out.println("END");

    }
}
