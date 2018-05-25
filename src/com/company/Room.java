package com.company;

import sun.plugin2.message.Message;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

class Door {
    final int target;
    final boolean isDoor;
    Point point_hero = null;

    Door(int target, boolean isDoor) {
        this.target = target;
        this.isDoor = isDoor;
    }

    Door setPoint(double x, double y) {
        point_hero = new Point(x, y);
        return this;
    }

    boolean hasPoint() {
        return point_hero != null;
    }
}

class Room {
    boolean drawWall = false;
    TestinLevel testinLevel = null;
    boolean test_mod = false;

    Sprite background;
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<GameObject> gameObjects = new ArrayList<>();
    ArrayList<GameObject> gameWall = new ArrayList<>();
    ArrayList<GameMessage> gameMessages = new ArrayList<>();

    Room() {
    }

    void MakeMessage(Image image) {
        MakeMessage(new GameMessage(image));
    }
    void MakeMessage(GameMessage message){
        gameMessages.add(message);
    }

    public void setGameObjects(ArrayList<GameObject> gameObjects) {
        this.gameObjects = gameObjects;
    }

    public BufferedImage loadImage(String path) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(getClass().getResource(path));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }

    void compressWall() {
        int size_before = 0;
        int size_after = 0;
        do {
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

    protected void LoadWall(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        int size = 1;
        for (int xx = 0; xx < w; xx += size) {
            for (int yy = 0; yy < h; yy += size) {
                int pixel = image.getRGB(xx, yy);
                int red = (pixel >> 16) & 0xff;
                int green = (pixel >> 8) & 0xff;
                int blue = (pixel) & 0xff;

                boolean white = (red == 255) && red == green && red == blue;
                if (!white) {
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

    protected boolean CheckCollisionWith(Game game, CollisionRect rect) {
        for (int i = 0; i < gameObjects.size(); i++) {
            if (gameObjects.get(i).enableCollision && rect.CollidesWith(gameObjects.get(i).collisionRect)) {
                if (gameObjects.get(i).door.isDoor) {
                    Point point = gameObjects.get(i).door.point_hero;
                    game.loadLevel(gameObjects.get(i).door.target, point);
                } else if (gameObjects.get(i).dialog != null && gameObjects.get(i).dialog.dialogHas()) {
                    gameObjects.get(i).dialog.enable();
                    gameObjects.get(i).dialog.nextDialog();
                    dialog_mode = true;
                    dialog_target = i;
                } else if (gameObjects.get(i).tester != null) {
                    testinLevel = gameObjects.get(i).tester;
                    gameObjects.get(i).tester = null;
                    testinLevel.startTest();
                    test_mod = true;
                } else
                    return true;
            }
        }
        for (int i = 0; i < gameWall.size(); i++) {
            if (gameWall.get(i).enableCollision && rect.CollidesWith(gameWall.get(i).collisionRect)) {
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
            if (gameObjects.get(i).dialog != null && gameObjects.get(i).dialog.started) {
                gameObjects.get(i).dialog.drawDialog(g);
            }
        }
        for (int i = 0; i < enemies.size(); i++)
            enemies.get(i).draw(g);
        if (drawWall)
            for (int i = 0; i < gameWall.size(); i++) {
                if (i % 2 == 0)
                    g.setColor(Color.red);
                else
                    g.setColor(Color.green);
                g.fillRect((int) gameWall.get(i).point.x, (int) gameWall.get(i).point.y, gameWall.get(i).getWidth(), gameWall.get(i).getHeight());
                gameWall.get(i).draw(g);
            }
        game.hero.draw(g);

        if (testinLevel != null && test_mod)
            testinLevel.draw(g);

        for (int i = 0; i < gameMessages.size(); i++)
            gameMessages.get(i).draw(g);
    }

    boolean last_press_dialog = false;

    void update(final Game game, int delay) {
        if (!dialog_mode && !test_mod)
            game.hero.updateCoords(game, true);
        else if (dialog_mode) {
            if (game.dialogKeyPressed && game.delay_keypress > 100) {
                System.out.println("This");
                if (gameObjects.get(dialog_target).dialog.dialogHas()) {
                    gameObjects.get(dialog_target).dialog.nextDialog();
                    game.delay_keypress = 0;
                } else if (last_press_dialog) {
                    gameObjects.get(dialog_target).dialog = null;
                    dialog_mode = false;
                    dialog_target = -1;
                    last_press_dialog = false;
                } else
                    last_press_dialog = true;
            }
            game.hero.updateCoords(game, false);
        } else if (test_mod) {
            if (testinLevel.isWork() || testinLevel.killerEnabled) {
                //System.out.println("Enter");
                testinLevel.update(game);
            }else {
                System.out.println("End test");
                System.out.println("Lives: " + testinLevel.lives);
                if (testinLevel.lives > 0) {
                    testinLevel = null;
                    test_mod = false;
                }else{
                    System.out.println("You will die");
                    testinLevel.enableKiller();
                }
            }
            //game.hero.updateCoords(game, false);
        }
        for (int i = 0; i < gameMessages.size(); i++) {
            if (gameMessages.get(i).theEnd(game.current_time))
                gameMessages.remove(i);
        }
        for (int i = 0; i < enemies.size(); i++)
            enemies.get(i).update(game);
    }
}

class First_room extends Room {
    void LoadEnemy() {
        enemies.add(new Enemy(getImage("pic/HeroStayB.png"), 500, 200, getImage("Testbullet.png")));// Пример пуль
    }

    void loadDialog() {
        ArrayList<Image> arrayList = new ArrayList<>();
        arrayList.add(getImage("pic/mike.png"));
        arrayList.add(getImage("pic/HeroStayB.png"));
        arrayList.add(getImage("pic/test1.png"));
        //gameObjects.add(kolona_1.makeThisDialog(arrayList, new Rectangle((int)background.point.x, (int)background.point.y, background.getWidth(), background.getHeight())));

    }
    void LoadTest() {
        TestinLevel test = new TestinLevel(this);
        ArrayList<TestinLevel.Answer> answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/1/Q.png"));
        answers.add(new TestinLevel.Answer(true, getImage("pic/tests/1/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/1/A2.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/1/A3.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/1/A4.png")));
        test.addAnswers(answers);

        answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/2/Q.png"));
        answers.add(new TestinLevel.Answer(true, getImage("pic/tests/2/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/2/A2.png")));
        test.addAnswers(answers);

        answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/3/Q.png"));
        answers.add(new TestinLevel.Answer(true, getImage("pic/tests/3/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/3/A2.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/3/A3.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/3/A4.png")));
        test.addAnswers(answers);

        answers = new ArrayList<>();

        test.addQuestionImage(getImage("pic/tests/4/Q.png"));
        answers.add(new TestinLevel.Answer(true, getImage("pic/tests/4/A1.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/4/A2.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/4/A3.png")));
        answers.add(new TestinLevel.Answer(false, getImage("pic/tests/4/A4.png")));
        test.addAnswers(answers);

        GameObject enemy = new GameObject(getImage("pic/enemy/testenemy.png"),200, 200).makeThisTester(test);
        gameObjects.add(enemy);
        //kolona_1.makeThisTester(test);
    }

    void LoadBackground(Game game) {

        background = new Sprite(ImageManager.biggerImage(getImage("pic/R1Vhod.png"), Game.WIDTH, Game.HEIGHT));


        Image img = getBufferedImage("pic/R1vhodramka.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(Game.WIDTH, Game.HEIGHT, Image.SCALE_SMOOTH)));
    }

    void LoadDoor() {
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2 - 100 - 18, background.point.y + 100, 100, 10)
                .makeThisDoor(Second_room.ID, null).enableCollision());
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2 + 30, background.point.y + 100, 100, 10)
                .makeThisDoor(Second_room.ID, new Point(background.point.x + background.getWidth() / 2 + 30, background.point.y + background.getHeight() - 50)).enableCollision());
    }

    final static int ID = 0;

    public First_room getRoom(Game game, Point customPoint) {
        LoadBackground(game);
        LoadTest();
        LoadDoor();
        if (customPoint == null)
            game.hero.set_coord(background.point.x + background.getWidth() / 2, background.point.y + background.getHeight() - game.hero.getHeight() - 80);
        else
            game.hero.set_coord(customPoint.x, customPoint.y);
        return this;
    }
}

class Second_room extends Room {
    final static int ID = 1;

    void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("pic/R2.png"), Game.WIDTH, Game.HEIGHT));
        Image img = getBufferedImage("pic/R2Ramka.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(Game.WIDTH, Game.HEIGHT, Image.SCALE_SMOOTH)));
    }

    void LoadDoor() {
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2 - 180, background.point.y + background.getHeight() - 20, 100, 20)
                .makeThisDoor(First_room.ID, new Point(background.point.x + background.getWidth() / 2 - 110, 120)).enableCollision());
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2, background.point.y + background.getHeight() - 20, 100, 20)
                .makeThisDoor(First_room.ID, new Point(background.point.x + background.getWidth() / 2 + 20, 120)).enableCollision());
    }

    public Second_room getRoom(Game game, Point custom_point) {
        //drawWall = true;
        LoadBackground(game);
        LoadDoor();
        if (custom_point == null)
            game.hero.set_coord(background.point.x + background.getWidth() / 2 - 150, background.point.y + background.getHeight() - game.hero.getHeight() - 30);
        else
            game.hero.set_coord(custom_point.x, custom_point.y);
        //MakeMessage(new GameMessage(getImage("pic/texts/test.png")).afterMessage(new GameMessage(getImage("pic/texts/Welcome.png"))));
        return this;
    }
}