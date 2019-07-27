package com.codegym.task.task30.task3008.client;

import com.codegym.task.task30.task3008.Connection;
import com.codegym.task.task30.task3008.ConsoleHelper;
import com.codegym.task.task30.task3008.Message;
import com.codegym.task.task30.task3008.MessageType;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main (String args[]) {
        Client client = new Client();
        client.run();
    }

    public void run() {
         SocketThread socketThread = getSocketThread();
         socketThread.setDaemon(true);

         socketThread.start();

         try {
             synchronized (this) {
                 wait();
             }

         } catch (InterruptedException e) {
             ConsoleHelper.writeMessage("Oppps!!!...Error!");
             System.exit(1);

         }

         if(clientConnected) {
             ConsoleHelper.writeMessage("Connection established. To exit, enter 'exit'.");
             while(clientConnected) {
                 String message = ConsoleHelper.readString();

                 if(!message.equalsIgnoreCase("exit")) {
                     if(shouldSendTextFromConsole()) {
                         sendTextMessage(message);
                     }
                 } else {
                     break;
                 }
             }

         } else {
             ConsoleHelper.writeMessage("An error occurred while working with the client.");
             
         }
    }


    protected String getServerAddress() {
        ConsoleHelper.writeMessage("Enter the server address: ");
        return ConsoleHelper.readString();
    }

    protected int getServerPort() {
        ConsoleHelper.writeMessage("Enter the server port: ");
        return ConsoleHelper.readInt();
    }

    protected String getUserName() {
        ConsoleHelper.writeMessage("Enter a UserName: ");
        return ConsoleHelper.readString();
    }

    protected boolean shouldSendTextFromConsole() {
        return true;
    }

    protected SocketThread getSocketThread() {
        return new SocketThread();
    }

    protected void sendTextMessage(String text) {
        try {
            connection.send(new Message(MessageType.TEXT, text));

        } catch (IOException e) {
            ConsoleHelper.writeMessage("Oppps!!!...connection fails to send your message!");
            clientConnected = false;
        }
    }

    public class SocketThread extends Thread {

        public void run() {
            String serverAddress = getServerAddress();
            int serverPort = getServerPort();

            try {
                Socket socket = new Socket(serverAddress, serverPort);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();

            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);

            }

        }

        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
        }

        protected void informAboutAddingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has joined the chat.");
        }

        protected void informAboutDeletingNewUser(String userName) {
            ConsoleHelper.writeMessage(userName + " has left the chat.");
        }

        protected void notifyConnectionStatusChanged(boolean clientConnected) {
           synchronized (Client.this) {
               Client.this.clientConnected = clientConnected;
               Client.this.notify();
           }
        }

        protected void clientHandshake() throws IOException, ClassNotFoundException {
            Message message;
            while(!clientConnected) {
                try {
                    message = connection.receive();

                } catch (IOException e) {
                    throw new IOException("Unexpected MessageType");

                }
                if(message.getType() == MessageType.NAME_REQUEST) {
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));

                } else if(message.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);

                } else {
                    throw new IOException("Unexpected MessageType");

                }
            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            Message message;
            while(true) {
                try {
                    message = connection.receive();

                } catch (IOException e) {
                    throw new IOException("Unexpected MessageType");

                }
                if(message.getType() == MessageType.TEXT) {
                    processIncomingMessage(message.getData());

                } else if(message.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(message.getData());

                } else if(message.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(message.getData());

                } else {
                    throw new IOException("Unexpected MessageType");

                }
            }
        }

    }
}
