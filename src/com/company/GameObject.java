package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Random;

class Calc{

    double findX(Point A, Point B, double cy){
        return ((cy - A.y) * (B.x - A.x)) / (B.y - A.y) + A.x;
    }
    double findY(Point A, Point B, double cx){
        return ((cx - A.x) * (B.y - A.y)) / (B.x - A.x) + A.y;
    }
    static Point findPoint(Point A, Point B, double findLenPoint){
        // C = A + (B - A) * ( findlen / fulllen)
        double M = findLenPoint / Point.dist(A, B);
        double x = A.x + ((B.x - A.x) * M);
        double y = A.y + ((B.y - A.y) * M);
        return new Point(x, y);
    }
}
class Point {
    double x;
    double y;

    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }
    void change(double x, double y){this.x = x; this.y = y;}
    static double dist(Point A, Point B) {
        return Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y) * (B.y - A.y));
    }
}

//  Oleg
class Bullet extends Sprite{
   //double x=1000,y=-1000;//x,y нужно узнать позицию глав. героя ?
    Point target;
    double step = 6;
    boolean work = true;
    public Bullet(Image image, double x, double y) {
        super(image, x, y);
    }
    Bullet setTarget(Point target){
        this.target = new Point(target.x, target.y);
        return this;
    }
    @Override
    public void draw(Graphics g) {
        super.draw(g);
    }

    boolean collisionObject(Game game){
        if (collisionRect.CollidesWith(game.hero.collisionHero)) {
            System.out.println("Die");
            game.loadLevel(First_room.ID);
            return true;
        }
        for (int i = 0; i < game.room.gameObjects.size(); i++)
            if (game.room.gameObjects.get(i).enableCollision && collisionRect.CollidesWith(game.room.gameObjects.get(i).collisionRect)) {
                //System.out.println("YES");
                return true;
            }

        return false;
    }
    public void update(Game game) {
        if (Point.dist(point, target) == 0 || Point.dist(point, target) < step || collisionObject(game)) {
            work = false;
            return;
        }
        Point X = Calc.findPoint(point, target, step);
        point.change(X.x, X.y);
        collisionRect.move(point.x, point.y);
    }
}
//
class Enemy extends Sprite{
    Random r=new Random();
    double last_bullet;
    public ArrayList<Bullet> bullet=new ArrayList<>();
    int delay = 1000;
    Image bulletImage;// Oleg

    public Enemy(Image image, double x, double y,Image OlegImage) {
        super(image, x, y);
        this.bulletImage = OlegImage;
    }
    public void update(Game game) {
        if (bullet.size() == 0 || System.currentTimeMillis() - last_bullet > delay) {
            last_bullet = System.currentTimeMillis();
            bullet.add(new Bullet(bulletImage, point.x + getWidth()/2, point.y + getHeight()/2).setTarget(new Point(game.hero.point.x + game.hero.getWidth()/2, game.hero.point.y)));
        } else
            for (int i = 0; i < bullet.size(); i++) {
                if (bullet.get(i).work)
                    bullet.get(i).update(game);
                else
                    bullet.remove(i);
            }
    }
    @Override
    public void draw(Graphics g) {
        super.draw(g);
        if (bullet.size() > 0)
            for (int i = 0; i < bullet.size(); i++)
                bullet.get(i).draw(g);
    }
}
// -Oleg
class CollisionRect {

    double x, y, width, height;

    public CollisionRect(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public void move(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public boolean CollidesWith(CollisionRect rect) {
        return x < rect.x + rect.width && y < rect.y + rect.height && x + width > rect.x && y + height > rect.y;
    }

}

class Sprite {
    protected Image image; //изображение
    int weight = 0;
    int height = 0;
    CollisionRect collisionRect;
    final Point point = new Point(0,0);
    protected boolean enableCollision = false;

    public Sprite(Image image) {
        if (image != null) {
            setImage(image);
        }
    }
    public void update(){/**need be override**/};
    protected Sprite disableCollision(){enableCollision = false; return this;}
    protected Sprite enableCollision(){enableCollision = true; return this;}
    public void setImage(Image image){
        this.image = image;
        collisionRect = new CollisionRect(point.x, point.y, getWidth(), getHeight());
    }
    public void set_coord(double x, double y){
        this.point.x = x;
        this.point.y = y;
    }

    public void updateCollision() {
        collisionRect.move(point.x, point.y);
    }

    public Sprite(Image image, double x, double y) {
        this.image = image;
        collisionRect = new CollisionRect(x, y, getWidth(), getHeight());
        this.point.x = x;
        this.point.y = y;
    }
    public Sprite(int x, int y, int weight, int height){
        this.image = null;
        this.point.x = x;
        this.point.y = y;
        this.weight = weight;
        this.height = height;
        collisionRect = new CollisionRect(x, y, getWidth(), getHeight());
    }

    public int getWidth() { //получаем ширину картинки
        if (image == null)
            return weight;
        return image.getWidth(null);
    }

    public int getHeight() { //получаем высоту картинки
        if (image == null)
            return height;
        return image.getHeight(null);
    }

    public void draw(Graphics g, Image image, Point point){
        if (image != null)
            g.drawImage(image, (int) point.x, (int) point.y, null);
        else {
          //  g.setColor(Color.blue);
            //g.fillRect((int) point.x, (int) point.y, getWidth(), getHeight());
        }
    }
    public void draw(Graphics g) { //рисуем картинку
        draw(g, image, point);
    }

    public void draw(Graphics g, int x, int y) {
        g.drawImage(image, x, y, null);
    }
    public boolean CollisionWith(Sprite sprite) {
        return sprite.collisionRect.CollidesWith(this.collisionRect);
    }
}

class GamePerson extends Sprite {


    Image foot;
    Image stayImageRight;
    Image stayImageLeft;
    ArrayList<Image> animationRight = new ArrayList<>();
    ArrayList<Image> animationLeft = new ArrayList<>();
    CollisionRect collisionHero;

    int cout_step = 0;
    boolean watchLeft = false;
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

    void setStayImageLeft(Image image){
        stayImageLeft = image;
    }
    void setStayImageRight(Image image){
        stayImageRight = image;
    }
    void setAnimationRight(ArrayList<Image> animationRight){this.animationRight = animationRight;}
    void setAnimationLeft(ArrayList<Image> animationLeft){
        this.animationLeft = animationLeft;
    }
    void setFoot(Image image){
        this.foot = image;
        //point.change(point.x, point.y + this.image.getHeight(null) - foot.getHeight(null));
        collisionRect = new CollisionRect(point.x, point.y, getWidth(), getHeight());
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
        }
        move(x_move, y_move, game);
        updateCollision();
    }
}

public class GameObject extends Sprite {
    Dialog dialog = null;
    Door door = new Door(0, false);
    public GameObject(Image image, double x, double y) {
        super(image, x, y);
        enableCollision();
    }
    public GameObject makeThisDialog(ArrayList<Image> images, Rectangle background){
        dialog = new Dialog(images, background);
        return this;
    }
    GameObject(int x, int y, int weight, int height){
        super(x, y, weight, height);
    }
    public GameObject makeThisDoor(int target){
        door = new Door(target, true);
        return this;
    }

    @Override
    public void draw(Graphics g) {
        //g.fillRect( (int)point.x , (int)point.y, getWidth(), getHeight());
        super.draw(g);
    }

    @Override
    protected GameObject enableCollision() {
        return (GameObject) super.enableCollision();
    }

    @Override
    protected GameObject disableCollision() {
        return (GameObject)super.disableCollision();
    }
}
