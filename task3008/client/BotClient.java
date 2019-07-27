package com.codegym.task.task30.task3008.client;

import com.codegym.task.task30.task3008.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static java.lang.Math.random;

public class BotClient extends Client {
    public static void main (String[] args) {
        BotClient botClient = new BotClient();
        botClient.run();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected String getUserName() {
        String botName = "date_bot_"+ ((int)(Math.random()*100));
        return botName;
    }

    public class BotSocketThread extends SocketThread {
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            sendTextMessage("Hello, there. I'm a bot. I understand the following commands: date, day, month, year, time, hour, minutes, seconds.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            if (message != null) {
                ConsoleHelper.writeMessage(message);
                SimpleDateFormat format = null;
                if (message.contains(": ")) {
                    String[] massiv = message.split(": ");
                    if (massiv.length == 2 && massiv[1] != null) {
                        String name = massiv[0];
                        String text = massiv[1];
                        switch (text) {
                            case "date":
                                format = new SimpleDateFormat("d.MM.YYYY");
                                break;
                            case "day":
                                format = new SimpleDateFormat("d");
                                break;
                            case "month":
                                format = new SimpleDateFormat("MMMM");
                                break;
                            case "year":
                                format = new SimpleDateFormat("YYYY");
                                break;
                            case "time":
                                format = new SimpleDateFormat("H:mm:ss");
                                break;
                            case "hour":
                                format = new SimpleDateFormat("H");
                                break;
                            case "minutes":
                                format = new SimpleDateFormat("m");
                                break;
                            case "seconds":
                                format = new SimpleDateFormat("s");
                                break;

                        }
                        if (format != null) {
                            sendTextMessage(String.format("Information for %s: %s", name, format.format(Calendar.getInstance().getTime())));
                        }
                    }
                }
            }
        }
    }
}
