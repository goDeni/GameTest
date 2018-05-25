package com.company;

import sun.plugin2.message.Message;

import java.awt.*;

public class GameMessage {
    Image image;
    GameMessage afterMessage = null;
    long time_start;
    int delay = 1000*5;
    GameMessage(Image image){
        this.image = image;
        time_start = System.currentTimeMillis();
    }
    GameMessage afterMessage(GameMessage message){
        this.afterMessage = message;
        return this;
    }
    void draw(Graphics g){
        g.drawImage(image, 0,0,image.getWidth(null), image.getHeight(null), null);
    }
    boolean theEnd(long current_time){
        if (((int) (current_time - time_start)) > delay){
            if (afterMessage != null){
                image = afterMessage.image;
                time_start = current_time;
                afterMessage = afterMessage.afterMessage;
            } else
                return true;
        }
        return false;
    }
}
