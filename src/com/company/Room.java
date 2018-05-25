package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

class Door{
    final int target;
    final boolean isDoor;
    Door(int target, boolean isDoor) {
        this.target = target;
        this.isDoor = isDoor;
    }
}
class Room{
    TestinLevel testinLevel = null;
    boolean test_mod = false;

    Sprite background;
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<GameObject> gameObjects = new ArrayList<>();
    ArrayList<GameObject> gameWall = new ArrayList<>();

    Room() {}
    public void setGameObjects(ArrayList<GameObject> gameObjects){
        this.gameObjects = gameObjects;
    }
    public BufferedImage loadImage(String path){
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResource(path));
        }catch (IOException e){
            e.printStackTrace();
        }
        return image;
    }
    void compressWall(){
        int size_before = 0;
        int size_after = 0;
        do{
            size_before = gameWall.size();
            for (int i = 0; i < gameWall.size(); i++) {
                GameObject object = gameWall.get(i);
                GameObject finded_obj = null;
                for (int j = 0; j < gameWall.size(); j++) {
                    GameObject o1 = gameWall.get(j);
                    if (object.point.x + object.getWidth() == o1.point.x && object.point.y == o1.point.y && object.getHeight() == o1.getHeight()) {
                        // System.out.println("YES");
                        finded_obj = o1;
                        break;
                    }
                }
                if (finded_obj != null) {
                    gameWall.remove(finded_obj);
                    gameWall.remove(object);
                    object = new GameObject((int) object.point.x, (int) object.point.y, (int) finded_obj.point.x + finded_obj.getWidth() - (int) object.point.x, object.getHeight()).enableCollision();
                    gameWall.add(object);
                }
            }
            size_after = gameWall.size();
        } while (size_before != size_after);
    }
    protected void LoadWall(BufferedImage image){
        int w=image.getWidth();
        int h=image.getHeight();
        int size = 1;
        for (int xx=0;xx<w;xx+=size){
            for (int yy=0;yy<h;yy+=size){
                int pixel = image.getRGB(xx,yy);
                int red = (pixel>>16)&0xff;
                int green = (pixel>>8)&0xff;
                int blue = (pixel)&0xff;

                boolean white = (red == 255) && red == green && red == blue;
                if (!white){
                    GameObject object = new GameObject((int) background.point.x + xx, (int) background.point.y + yy, size, size).enableCollision();
                    //gameWall.add(object);
                    if (gameWall.size() != 0) {
                        GameObject last = gameWall.get(gameWall.size() - 1);
                        if (last.point.x == object.point.x && last.point.y + last.getHeight() == object.point.y) {
                            gameWall.remove(last);
                            gameWall.add(new GameObject((int) last.point.x, (int) last.point.y, object.getWidth(), last.getHeight() + size).enableCollision());
                        } else
                            gameWall.add(object);
                    } else
                        gameWall.add(object);
                    compressWall();
                }
            }
        }
        System.out.println(gameWall.size());
    }
    protected Image getImage(String path) {
        BufferedImage sourceImage = null;

        try {
            URL url = this.getClass().getClassLoader().getResource(path);
            sourceImage = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Toolkit.getDefaultToolkit().createImage(sourceImage.getSource());
    }
    protected BufferedImage getBufferedImage(String path) {
        BufferedImage sourceImage = null;

        try {
            URL url = this.getClass().getClassLoader().getResource(path);
            sourceImage = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sourceImage;
    }
    boolean dialog_mode = false;
    int dialog_target = -1;
    protected boolean CheckCollisionWith(Game game, CollisionRect rect){
        for (int i = 0; i < gameObjects.size(); i++){
            if (gameObjects.get(i).enableCollision && rect.CollidesWith(gameObjects.get(i).collisionRect)){
                if (gameObjects.get(i).door.isDoor)
                    game.loadLevel(gameObjects.get(i).door.target);
                else if (gameObjects.get(i).dialog != null && gameObjects.get(i).dialog.dialogHas()) {
                    gameObjects.get(i).dialog.enable();
                    gameObjects.get(i).dialog.nextDialog();
                    dialog_mode = true;
                    dialog_target = i;
                } else if (gameObjects.get(i).tester != null){
                    testinLevel = gameObjects.get(i).tester;
                    gameObjects.get(i).tester = null;
                    testinLevel.startTest();
                    test_mod = true;
                } else
                    return true;
            }
        }
        for (int i = 0; i < gameWall.size(); i++){
            if (gameWall.get(i).enableCollision && rect.CollidesWith(gameWall.get(i).collisionRect)){
                 return true;
            }
        }
        Point point = new Point(rect.x, rect.y);
        if (point.x > background.point.x && point.x + game.hero.getWidth() < background.point.x + background.getWidth() &&
                point.y > background.point.y && point.y + game.hero.getHeight() < background.point.y + background.getHeight())
            return false;
        return true;
    }

    public void draw(Graphics g, Game game) {
        background.draw(g);
        for (int i = 0; i < gameObjects.size(); i++) {
            gameObjects.get(i).draw(g);
            if (gameObjects.get(i).dialog != null && gameObjects.get(i).dialog.started){
                gameObjects.get(i).dialog.drawDialog(g);
            }
        }
        for (int i = 0; i < enemies.size(); i++)
            enemies.get(i).draw(g);

//        for (int i = 0; i < gameWall.size(); i++) {
//            if (i%2 == 0)
//                g.setColor(Color.red);
//            else
//                g.setColor(Color.green);
//            g.fillRect((int)gameWall.get(i).point.x, (int)gameWall.get(i).point.y, gameWall.get(i).getWidth(), gameWall.get(i).getHeight());
//            gameWall.get(i).draw(g);
//        }
        game.hero.draw(g);

        if (testinLevel != null && test_mod)
            testinLevel.draw(g);
    }
    boolean last_press_dialog = false;
    void update(final Game game, int delay){
        new Thread(){
            @Override
            public void run() {
                if (!dialog_mode && !test_mod)
                    game.hero.updateCoords(game, true);
                else if (dialog_mode){
                    if (game.dialogKeyPressed && game.delay_keypress > 100) {
                        System.out.println("This");
                        if (gameObjects.get(dialog_target).dialog.dialogHas()) {
                            gameObjects.get(dialog_target).dialog.nextDialog();
                            game.delay_keypress = 0;
                        }
                        else if (last_press_dialog){
                            gameObjects.get(dialog_target).dialog = null;
                            dialog_mode = false;
                            dialog_target = -1;
                            last_press_dialog = false;
                        }else
                            last_press_dialog = true;
                    }
                    game.hero.updateCoords(game, false);
                } else if (test_mod){
                    if (testinLevel.isWork())
                        testinLevel.update(game);
                    else {
                        testinLevel = null;
                        test_mod = false;
                    }
                    game.hero.updateCoords(game, false);
                }
            }
        }.start();
        for (int i = 0; i < enemies.size(); i++)
            enemies.get(i).update(game);
    }
}

class First_room extends Room{
    void LoadEnemy(){
        enemies.add(new Enemy(getImage("pic/HeroStayB.png"),500,200,getImage("Testbullet.png")));// Пример пуль
    }
    void loadDialog(){
        ArrayList<Image> arrayList = new ArrayList<>();
        arrayList.add(getImage("pic/mike.png"));
        arrayList.add(getImage("pic/HeroStayB.png"));
        arrayList.add(getImage("pic/test1.png"));
        //gameObjects.add(kolona_1.makeThisDialog(arrayList, new Rectangle((int)background.point.x, (int)background.point.y, background.getWidth(), background.getHeight())));

    }
    void LoadTest(){
        TestinLevel test = new TestinLevel(this);
        ArrayList<TestinLevel.Answer> answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/1/Q.png"));
        answers.add(new TestinLevel.Answer(true ,getImage("pic/tests/1/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/1/A2.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/1/A3.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/1/A4.png")));
        test.addAnswers(answers);

        answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/2/Q.png"));
        answers.add(new TestinLevel.Answer(true ,getImage("pic/tests/2/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/2/A2.png")));
        test.addAnswers(answers);

        answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/3/Q.png"));
        answers.add(new TestinLevel.Answer(true ,getImage("pic/tests/3/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/3/A2.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/3/A3.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/3/A4.png")));
        test.addAnswers(answers);

        answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/4/Q.png"));
        answers.add(new TestinLevel.Answer(true ,getImage("pic/tests/4/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/4/A2.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/4/A3.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/4/A4.png")));
        test.addAnswers(answers);



        //kolona_1.makeThisTester(test);
    }

    void LoadBackground(Game game){

        background = new Sprite(ImageManager.biggerImage(getImage("pic/R1Vhod.png"), Game.WIDTH, Game.HEIGHT));

        background.set_coord((game.getWidth() - background.getWidth())/2, (game.getHeight()-background.getHeight())/2);

        Image img = getBufferedImage("pic/R1vhodramka.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(Game.WIDTH, Game.HEIGHT, Image.SCALE_SMOOTH)));
    }
    void LoadDoor(){
        gameObjects.add(new GameObject(background.point.x + background.getWidth()/2-100-18, background.point.y+100, 200+35, 10).makeThisDoor(1).enableCollision());
    }
    final static int ID = 0;
    public First_room getRoom(Game game){
        LoadBackground(game);
        LoadDoor();
        game.hero.set_coord(background.point.x + background.getWidth()/2, background.point.y+background.getHeight()-game.hero.getHeight()-80);
        return this;
    }
}
class Second_room extends Room{
    final static int ID = 1;
    public Second_room getRoom(Game game){
        background = new Sprite(getImage("pic/vhodv3.png"));
        background.set_coord((game.getWidth() - background.getWidth())/2, (game.getHeight()-background.getHeight())/2);
        game.hero.set_coord(background.point.x + background.getWidth()/2, background.point.y+background.getHeight()-game.hero.getHeight());
        return this;
    }
}