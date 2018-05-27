package com.company;


import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

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

abstract class Room {
    //int default_size_room = 10;
    int ID;
    boolean drawWall;
    TestinLevel testinLevel;
    boolean test_mod;

    Sprite background;
    ArrayList<Enemy> enemies = new ArrayList<>();
    ArrayList<GameObject> gameObjects = new ArrayList<>();
    ArrayList<GameObject> gameWall = new ArrayList<>();
    ArrayList<GameMessage> gameMessages = new ArrayList<>();
    boolean dialog_mode;
    int dialog_target;
    void initialize(Game game){
        drawWall = false;
        testinLevel = null;
        test_mod = false;
        enemies = new ArrayList<>();
        gameObjects = new ArrayList<>();
        gameWall = new ArrayList<>();
        gameMessages = new ArrayList<>();
        dialog_mode = false;
        dialog_target = -1;
    }
    Room() {

    }
    abstract void LoadTest();
    abstract void LoadDialog();
    abstract  void LoadBackground(Game game);
    abstract void LoadDoor();
    abstract Room getRoom(Game game);
    abstract Room setHero(Game game, Point point);

    void reset(Game game){
        initialize(game);
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
                    game.LoadFight();
                    game.room.setHero(game, null);
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
                    game.EndFight();
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
    final static int ID = 0;
    void LoadDialog() {
        ArrayList<Image> arrayList = new ArrayList<>();
        arrayList.add(getImage("pic/mike.png"));
        arrayList.add(getImage("pic/HeroStayB.png"));
        arrayList.add(getImage("pic/test1.png"));
        //gameObjects.add(kolona_1.makeThisDialog(arrayList, new Rectangle((int)background.point.x, (int)background.point.y, background.getWidth(), background.getHeight())));

    }

    public void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("R1Vhod.png"), Game.gameWidth, Game.gameHeight));
        background.set_coord(game.getWidth()/2-background.getWidth()/2,game.getHeight()/2 - background.getHeight()/2);
        Image img = getBufferedImage("pic/R1vhodramka.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(Game.gameWidth, Game.gameHeight, Image.SCALE_SMOOTH)));
    }

    void LoadDoor() {
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2 - 100 - 18, background.point.y + 100, 100, 10)
                .makeThisDoor(Second_room.ID, null).enableCollision());
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2 + 30, background.point.y + 100, 100, 10)
                .makeThisDoor(Second_room.ID,
                        new Point(background.getWidth() / 2 + 30, background.getHeight() - 50)).enableCollision());
//        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2, background.point.y + background.getHeight()/2, 100, 10)
//                .makeThisDoor(Third_room.ID, null).enableCollision());
    }


    public First_room setHero(Game game, Point point){
        game.hero.room_id = ID;
        if (point == null)
            game.hero.set_coord(background.point.x + background.getWidth() / 2, background.point.y + background.getHeight() - game.hero.getHeight() - 80);
        else
            game.hero.set_coord(point.x, point.y);
        return this;
    }

    @Override
    void initialize(Game game) {
        super.initialize(null);
        LoadBackground(game);
        LoadDoor();
    }

    @Override
    void LoadTest() {

    }

    public First_room getRoom(Game game) {
        initialize(game);
        return this;
    }
}
class Third_room extends Room {
    final static int ID = 2;
    @Override
    void initialize(Game game) {
        super.initialize(game);
        LoadBackground(game);
        LoadDoor();
    }

    @Override
    void LoadTest() {

    }

    @Override
    void LoadDialog() {

    }

    @Override
    void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("R3kor.png"), 11));
        background.set_coord(game.getWidth()/2-background.getWidth()/2,game.getHeight()/2 - background.getHeight()/2);
        Image img = getImage("R3korRamka.png");
        LoadWall(ImageManager.toBufferedImage(ImageManager.biggerImage(img, 11)));
    }

    @Override
    void LoadDoor() {
        gameObjects.add(new GameObject(
                background.point.x + background.getWidth()-20, background.point.y + background.getHeight()/2-100, 20, 300
        ).makeThisDoor(Second_room.ID,
                new Point(30, 500)).enableCollision());
        gameObjects.add(new GameObject(
                background.point.x, background.point.y + background.getHeight()/2-100, 20, 300
        ).makeThisDoor(Four_room.ID, new Point(
                Game.gameWidth-80, Game.gameHeight/2
        )).enableCollision());
    }

    @Override
    public Third_room getRoom(Game game) {
        //drawWall = true;
        initialize(game);
        return this;
    }

    @Override
    Room setHero(Game game, Point point) {
        if (point == null)
            game.hero.set_coord(background.point.x + background.getWidth()-100, background.point.y + background.getHeight()/2);
        else
            game.hero.set_coord(background.point.x + point.x, background.point.y + point.y);
        return this;
    }
}
class Four_room extends Room{
    final static int ID = 3;
    @Override
    void LoadTest() {

    }

    @Override
    void LoadDialog() {

    }

    @Override
    void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("R4ugol.png"), Game.gameWidth, Game.gameHeight));
        background.set_coord(game.getWidth()/2-background.getWidth()/2,game.getHeight()/2 - background.getHeight()/2);
        Image img = getBufferedImage("R4ugolRamka.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(background.getWidth(), background.getHeight(), Image.SCALE_SMOOTH)));
    }

    @Override
    void LoadDoor() {
        gameObjects.add(new GameObject(
                background.point.x + background.getWidth()-20,
                background.point.y + background.getHeight()/2-200, 20, 400).makeThisDoor(
                        Third_room.ID, new Point(50, 250)
        ).enableCollision());
    }

    @Override
    void initialize(Game game) {
        super.initialize(game);
        LoadBackground(game);
        LoadDoor();
    }

    @Override
    Four_room getRoom(Game game) {
        initialize(game);
        return this;
    }

    @Override
    Four_room setHero(Game game, Point point) {
        if (point == null)
            game.hero.set_coord(background.point.x + background.getWidth()/2, background.point.y + background.getHeight()/2);
        else
            game.hero.set_coord(background.point.x + point.x, background.point.y + point.y);
        return this;
    }
}
class Six_room extends Room {
    final static int ID = 5;

    public void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("pic/Room116.png"), 10));
        background.set_coord(game.getWidth()/2-background.getWidth()/2,game.getHeight()/2 - background.getHeight()/2);
        Image img = getBufferedImage("pic/RKabinet116.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(background.getWidth(), background.getHeight(), Image.SCALE_SMOOTH)));
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

        GameObject enemy = new GameObject(getImage("pic/enemy/testenemy.png"),
                background.point.x + 130,
                background.point.y + 280).makeThisTester(test);
        gameObjects.add(enemy);
        //kolona_1.makeThisTester(test);
    }
    void LoadDoor() {

    }
    Six_room setHero(Game game, Point custom_point){
        game.hero.room_id = ID;
        if (custom_point == null)
            game.hero.set_coord(background.point.x + background.getWidth()/2, background.point.y + background.getHeight()/2);
        else
            game.hero.set_coord(custom_point.x, custom_point.y);
        return this;
    }

    @Override
    void initialize(Game game) {
        super.initialize(null);
        LoadBackground(game);
        LoadTest();
    }

    @Override
    void LoadDialog() {

    }

    public Six_room getRoom(Game game) {
        //drawWall = false;
        initialize(game);
        //MakeMessage(new GameMessage(getImage("pic/texts/test.png")).afterMessage(new GameMessage(getImage("pic/texts/Welcome.png"))));
        return this;
    }
}

class Second_room extends Room {
    final static int ID = 1;

    public void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("pic/R2.png"), Game.gameWidth, Game.gameHeight));
        background.set_coord(game.getWidth()/2-background.getWidth()/2,game.getHeight()/2 - background.getHeight()/2);
        Image img = getBufferedImage("pic/R2Ramka.png");
        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(Game.gameWidth, Game.gameHeight, Image.SCALE_SMOOTH)));
    }

    void LoadDoor() {
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2 - 180, background.point.y + background.getHeight() - 20, 100, 20)
                .makeThisDoor(First_room.ID, new Point(background.point.x + background.getWidth() / 2 - 110, background.point.y + 120)).enableCollision());
        gameObjects.add(new GameObject(background.point.x + background.getWidth() / 2, background.point.y + background.getHeight() - 20, 100, 20)
                .makeThisDoor(First_room.ID, new Point(background.point.x + background.getWidth() / 2 + 20, background.point.y + 120)).enableCollision());
        gameObjects.add(new GameObject(
                background.point.x, background.point.y + background.getHeight()/2, 20, 200).makeThisDoor(Third_room.ID,
                null).enableCollision());
    }
    Second_room setHero(Game game, Point custom_point){
        game.hero.room_id = ID;
        if (custom_point == null)
            game.hero.set_coord(background.point.x + background.getWidth() / 2 - 150, background.point.y + background.getHeight() - game.hero.getHeight() - 30);
        else
            game.hero.set_coord(background.point.x + custom_point.x, background.point.y + custom_point.y);
        return this;
    }

    @Override
    void initialize(Game game) {
        super.initialize(null);
        LoadBackground(game);
        LoadDoor();
    }

    @Override
    void LoadTest() {

    }

    @Override
    void LoadDialog() {

    }

    public Second_room getRoom(Game game) {
        //drawWall = true;
        initialize(game);
        //MakeMessage(new GameMessage(getImage("pic/texts/test.png")).afterMessage(new GameMessage(getImage("pic/texts/Welcome.png"))));
        return this;
    }
}

class Fight_room extends Room{
    Fight_room setTest(Room room){
        test_mod = true;
        testinLevel = room.testinLevel;
        return this;
    }
    @Override
    void initialize(Game game) {
        super.initialize(null);
        LoadBackground(game);
    }

    @Override
    void LoadTest() {

    }

    @Override
    void LoadDialog() {

    }

    public void LoadBackground(Game game) {
        background = new Sprite(ImageManager.biggerImage(getImage("Arena.png"), Game.gameWidth, Game.gameHeight));
        background.set_coord(game.getWidth()/2-background.getWidth()/2,game.getHeight()/2 - background.getHeight()/2);
//        Image img = getBufferedImage("pic/R2Ramka.png");
//        LoadWall(ImageManager.toBufferedImage(img.getScaledInstance(Game.gameWidth, Game.gameHeight, Image.SCALE_SMOOTH)));
    }

    @Override
    void LoadDoor() {

    }

    public Fight_room getRoom(Game game){
        initialize(game);
        return this;
    }

    @Override
    Room setHero(Game game, Point point) {
        return null;
    }
}