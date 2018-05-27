package com.company;

import java.awt.*;
import java.util.ArrayList;
import java.util.Random;



//  Oleg
class Bullet extends Sprite{
    Point target;
    static double step = 2;
    boolean work = true;
    long time_create;
    Point mainPoint;
    public Bullet(Image image, double x, double y) {
        super(image, x, y);
        time_create = System.currentTimeMillis();
        mainPoint = new Point(point.x, point.y);
    }
    final static int line_attack = 1;
    final static int shape_attack = 2;
    int type_attack;
    int time_change_attack;
    int time_life = 100000;
    Random random = new Random();
    Bullet setTarget(Point target, double speed, int type_attack){
        this.type_attack = type_attack;
        //this.target = new Point(target.x, target.y);
        if (speed > 0)
            step = speed;
        this.target = Calc.findPoint(point, target, Game.gameHeight + Game.gameWidth);
        if (random.nextBoolean())
            time_change_attack = new Random().nextInt(10000);
        else
            time_change_attack = 120000;
        return this;
    }
    Bullet setTarget(Point target){
        return setTarget(point, 0, 1);
    }
    @Override
    public void draw(Graphics g) {
        super.draw(g);
    }
    boolean collisionObject(Game game){
//        if (collisionRect.CollidesWith(game.hero.collisionHero)) {
//            System.out.println("Die");
//            game.hero.reset();
//            game.room.reset(game);
//            game.reloadLevel(game.hero.room_id, null);
//            return true;
//        }
        for (int i = 0; i < game.room.gameObjects.size(); i++)
            if (game.room.gameObjects.get(i).enableCollision && collisionRect.CollidesWith(game.room.gameObjects.get(i).collisionRect)) {
                //System.out.println("YES");
                return true;
            }
        for (int i = 0; i < game.room.gameWall.size(); i++)
            if (game.room.gameWall.get(i).enableCollision && collisionRect.CollidesWith(game.room.gameWall.get(i).collisionRect)) {
                //System.out.println("YES");
                return true;
            }

        return false;
    }
    double fi = 0;
    double r = 0;
    double step_fi = 0.2;
    double step_r = 0.3;
    public void update(Game game) {
        if (Point.dist(point, target) == 0 || Point.dist(point, target) < step || collisionObject(game)
                ||(type_attack == shape_attack &&game.current_time - time_create > time_life)) {
            work = false;
            return;
        }
        Point X;

        switch (type_attack){
            case line_attack:
                X = Calc.findPoint(point, target, step);
                point.change(X.x, X.y);
                collisionRect.move(point.x, point.y);
                break;
            case shape_attack:
                X = new Point(mainPoint.x + Calc.shapeX(r, fi), mainPoint.y + Calc.shapeY(r, fi));
                if (System.currentTimeMillis() - time_create > time_change_attack){
                    target = Calc.findPoint(point, X, Game.gameWidth);
                    type_attack = line_attack;
                    break;
                }
                fi += step_fi;
                r += step_r;
                if (fi >= 360)
                    fi = 0;
                if (r >= Game.gameHeight/2-1 || r <= 0)
                    step_r *= -1;
                point.change(X.x, X.y);
                collisionRect.move(point.x, point.y);
                break;
        }

    }
}
//

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



public class GameObject extends Sprite {

    Dialog dialog = null;
    TestinLevel tester = null;
    Door door = new Door(0, false);
    public GameObject(Image image, double x, double y) {
        super(image, x, y);
        enableCollision();
    }
    public GameObject makeThisDialog(ArrayList<Image> images, Rectangle background){
        dialog = new Dialog(images, background);
        return this;
    }
    public GameObject makeThisTester(TestinLevel test){
        this.tester = test;
        return this;
    }
    GameObject(int x, int y, int weight, int height){
        super(x, y, weight, height);
    }
    GameObject(double x, double y, double weight, double height){
        super((int)x, (int)y, (int)weight,(int) height);
    }
    public GameObject makeThisDoor(int target, Point point){
        door = new Door(target, true);
        if (point != null)
            door.setPoint(point.x, point.y);
        return this;
    }

    @Override
    public void draw(Graphics g) {
        if (image == null){
//            g.setColor(Color.yellow);
//            g.fillRect( (int)point.x , (int)point.y, getWidth(), getHeight());
        }
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
