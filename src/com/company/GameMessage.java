package com.company;


import java.awt.*;

public class GameMessage {
    Image image;
    GameMessage afterMessage = null;
    long time_start = 0;
    int delay = 1000*5;
    boolean time_stop = false;
    GameMessage(Image image){
        this.image = image;
    }
    GameMessage enableStopTime(){
        time_stop = true;
        return this;
    }
    GameMessage afterMessage(GameMessage message){
        this.afterMessage = message;
        return this;
    }
    void draw(Graphics g, Game game){
        if (time_start == 0)
            time_start = System.currentTimeMillis();
        g.drawImage(image, (int) (game.Width/2 - image.getWidth(null)/2), (int) (game.Height-image.getHeight(null)),image.getWidth(null), image.getHeight(null), null);
    }
    boolean theEnd(long current_time){
        if (((int) (current_time - time_start)) > delay){
            if (afterMessage != null){
                image = afterMessage.image;
                time_start = current_time;
                time_stop = afterMessage.time_stop;
                afterMessage = afterMessage.afterMessage;
            } else
                return true;
        }
        return false;
    }
}
