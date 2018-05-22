package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

public class Dialog{
    ArrayList<Image> images = new ArrayList<>();
    Iterator<Image> iterator;
    Rectangle background;
    Image image;
    boolean started = false;
    Dialog(ArrayList<Image> images, Rectangle background){
        this.images = images;
        iterator = images.iterator();
        this.background = background;
    }
    public void enable(){
        started = true;
    }
    public void nextDialog(){
        System.out.println("Change");
        image = iterator.next();
    }
    public void drawDialog(Graphics g){
        Point point = new Point(background.getX() + background.getWidth()/2 - image.getWidth(null)/2, background.getY() + background.getHeight() - image.getHeight(null));
        g.setColor(Color.green);
        g.fillRect((int)point.x, (int)point.y, image.getWidth(null), image.getHeight(null));
        g.drawImage(image, (int)point.x, (int)point.y, null);
    }
    public boolean dialogHas(){
        return iterator.hasNext();
    }
}
