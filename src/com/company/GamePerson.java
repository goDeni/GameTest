package com.company;

import java.awt.*;
import java.util.ArrayList;

class GamePerson extends Sprite {
    public int room_id = 0;

    Image foot;
    Image stayImageRight;
    Image stayImageLeft;
    ArrayList<Image> animationRight = new ArrayList<>();
    ArrayList<Image> animationLeft = new ArrayList<>();
    CollisionRect collisionHero;

    int cout_step = 0;
    boolean watchLeft = false;
    public int enemy_delay = 10000;
    public long last_use_hack = System.currentTimeMillis();

    GamePerson(Image image, int x, int y) {
        super(image, x, y);
        stayImageRight = image;
        stayImageLeft = stayImageRight;
    }

    @Override
    public void draw(Graphics g) {
        super.draw(g, image, new Point(point.x, point.y - image.getHeight(null) + foot.getHeight(null)));
        //g.fillRect( (int)point.x , (int)point.y, getWidth(), getHeight());
        //super.draw(g, foot, point);
    }
    @Override
    public int getWidth() {
        if (foot != null)
            return foot.getWidth(null);
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        if (foot != null)
            return foot.getHeight(null);
        return super.getHeight();
    }
    int size_hero = 6;
    Image stayImageLeft_original;
    Image stayImageRight_original;
    ArrayList<Image> animationRight_original;
    ArrayList<Image> animationLeft_original;
    Image foot_original;

    void setStayImageLeft(Image image){
        stayImageLeft_original = image;
        stayImageLeft = ImageManager.biggerImage(image, size_hero);
    }
    void setStayImageRight(Image image){
        stayImageRight_original = image;
        stayImageRight = ImageManager.biggerImage(image, size_hero);
    }
    void setAnimationRight(ArrayList<Image> animationRight){
        animationRight_original = animationRight;
        this.animationRight = new ArrayList<>();
        for (int i = 0; i < animationRight.size(); i++)
            this.animationRight.add(ImageManager.biggerImage(animationRight.get(i), size_hero));
    }
    void setAnimationLeft(ArrayList<Image> animationLeft){
        animationLeft_original = animationLeft;
        this.animationLeft = new ArrayList<>();
        for (int i = 0; i < animationLeft.size(); i++)
            this.animationLeft.add(ImageManager.biggerImage(animationLeft.get(i), size_hero));
    }
    void setFoot(Image image){
        foot_original = image;
        this.foot = ImageManager.biggerImage(image, size_hero);
        //point.change(point.x, point.y + this.image.getHeight(null) - foot.getHeight(null));
        collisionRect = new CollisionRect(point.x, point.y, getWidth(), getHeight());
    }
    void updateSize() {
        setStayImageLeft(stayImageLeft_original);
        setStayImageRight(stayImageRight_original);
        setAnimationRight(animationRight_original);
        setAnimationLeft(animationLeft_original);
        setFoot(foot_original);
    }
    void animRight(){
        if (image != animationRight.get(0))
            setImage(animationRight.get(0));
        else
            setImage(animationRight.get(1));
        watchLeft = false;
    }
    void animLeft(){
        if (image != animationLeft.get(0))
            setImage(animationLeft.get(0));
        else
            setImage(animationLeft.get(1));
        watchLeft = true;
    }

    @Override
    public void updateCollision() {
        collisionHero = new CollisionRect(point.x, point.y + getHeight() - image.getHeight(null), getWidth(), image.getHeight(null));
        super.updateCollision();
    }

    void animDown(){
        if (!watchLeft)
            animRight();
        else
            animLeft();
    }
    void animUp(){
        if (!watchLeft)
            animRight();
        else
            animLeft();
    }
    void animStay(){
        if (!watchLeft)
            setImage(stayImageRight);
        else
            setImage(stayImageLeft);
    }
    double step = 5;
    final int change_step = 5;
    void move(final double x, final double y, final Game game) {
        if (x != 0 || y != 0) {
            if (!game.room.CheckCollisionWith(game, new CollisionRect(point.x + x, point.y + y, getWidth(), getHeight())))
                point.change(point.x + x, point.y + y);
            else if (!game.room.CheckCollisionWith(game, new CollisionRect(point.x + x, point.y, getWidth(), getHeight())))
                point.change(point.x + x, point.y);
            else if (!game.room.CheckCollisionWith(game, new CollisionRect(point.x, point.y + y, getWidth(), getHeight())))
                point.change(point.x, point.y + y);
        }
        cout_step++;
        if (cout_step >= change_step) {
            cout_step = 0;
            if (x > 0 || (x > y && y > 0))
                animRight();
            else if (x < 0)
                animLeft();
            else if (y > 0)
                animDown();
            else if (y < 0)
                animUp();
            else if (y == 0 && x == 0)
                animStay();
        }
    }
    boolean immortality = false;
    void updateCoords(Game game, boolean move) {
        double x_move = 0;
        double y_move = 0;
        if (move) {
            if (game.rightPressed) {
                x_move = step;
            }
            if (game.leftPressed) {
                x_move = -step;
            }
            if (game.upPressed) {
                y_move = -step;
            }
            if (game.downPressed) {
                y_move = step;
            }
            if (game.keyMinus && game.delay_keypress > 100){
                game.delay_keypress = 0;
                if (size_hero > 1) {
                    size_hero--;
                    updateSize();
                }
            } else if (game.keyPlus && game.delay_keypress > 100){
                game.delay_keypress = 0;
                if (size_hero < 12){
                    size_hero++;
                    updateSize();
                }
            }
        }
        if (game.keyF12)
            immortality = !immortality;
        move(x_move, y_move, game);
        updateCollision();
    }

    public void reset() {
        if (size_hero != 6){
            size_hero = 6;
            updateSize();
        }
    }
}