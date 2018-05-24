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
class TestinLevel {
    ArrayList<Image> questions = new ArrayList<>();
    ArrayList<ArrayList<Answer>> answers = new ArrayList<>();
    Iterator<Image> iterator_questions;
    Iterator<ArrayList<Answer>> iterator_answers;
    Image current_question;
    ArrayList<Answer> current_answers;
    int cursor = 0;
    Point pointAnswer;
    Point pointRoom;
    int lives = 3;
    int offset = 0;

    public boolean isWork() {
        return work;
    }

    public static class Answer{
        Point point;
        final boolean right;
        final Image image;

        Answer(boolean right, Image image) {
            this.right = right;
            this.image = image;
        }
        public Image getImage() {
            return image;
        }
        public boolean isRight() {
            return right;
        }
        int border = 5;
        static int interval = 20;
        public void draw(Graphics g, Point point, boolean selected){
            if (selected){
                g.setColor(Color.red);
                g.fillRect((int)point.x - border, (int) point.y - border, (int)image.getWidth(null) + border*2, image.getHeight(null) + border*2);
            }
            g.drawImage(image, (int)point.x, (int)point.y, null);
            point.change(point.x + image.getWidth(null) + interval, point.y);

        }
    }
    Rectangle rectangleroom;
    Rectangle rectangleTest;
    TestinLevel(Room room){
        rectangleroom = new Rectangle((int)room.background.point.x, (int)room.background.point.y, room.background.getWidth(), room.background.getHeight());
        pointAnswer = new Point(0,0);
    }
    boolean work = true;
    void nextQuestion(){
        if (!iterator_answers.hasNext() || !iterator_questions.hasNext()) {
            work = false;
            return;
        }
        current_question = iterator_questions.next();
        current_answers = iterator_answers.next();
        pointAnswer.change(rectangleroom.getX() + rectangleroom.getWidth()/2 - current_question.getWidth(null)/2, rectangleroom.getY());
        int sum = Answer.interval * (current_answers.size()-1);
        for (int i = 0; i < current_answers.size(); i++)
            sum += current_answers.get(i).getImage().getWidth(null);
        offset = sum - current_question.getWidth(null);
        int borderTest = 5;
        if (offset != 0)
            offset /= 2;
        int wight = 0;
        if (offset > 0)
            wight = sum + borderTest*2;
        else
            wight = current_question.getWidth(null) + borderTest *2;
        int height = current_answers.get(0).getImage().getHeight(null) + borderTest;
        for (int i = 0; i < current_answers.size(); i++)
            if (current_answers.get(i).getImage().getHeight(null) > height)
                height = current_answers.get(i).getImage().getHeight(null);
        height += interval + current_question.getHeight(null) ;
        int x = 0;
        if (offset > 0)
            x = (int)pointAnswer.x - borderTest - offset;
        else
            x = (int)pointAnswer.x - borderTest;
        rectangleTest = new Rectangle(x, (int)pointAnswer.y, wight, height);
    }
    void startTest(){
        iterator_questions = questions.iterator();
        iterator_answers = answers.iterator();
        nextQuestion();
    }
    void addQuestionImage(Image image){
        questions.add(image);
    }
    void addAnswers(ArrayList<Answer> answers){
        this.answers.add(answers);
    }
    int border = 5;
    static int interval = 10;
    void draw(Graphics g){
        if (current_question != null && current_answers != null) {
            g.setColor(Color.white);
            g.fillRect((int)rectangleTest.getX(), (int)rectangleTest.getY(), (int)rectangleTest.getWidth(), (int)rectangleTest.getHeight());
            if (selected_ans != -1){
                if (!current_answers.get(selected_ans).right) {
                    g.setColor(Color.red);
                    lives--;
                    System.out.println("Lives: " + lives);
                }else
                    g.setColor(Color.green);
                //g.fillRect((int)pointAnswer.x - border, (int) pointAnswer.y - border, (int)current_question.getWidth(null) + border*2, current_question.getHeight(null) + border*2);
                selected_ans = -1;
                nextQuestion();
            }
            g.drawImage(current_question, (int) pointAnswer.x, (int) pointAnswer.y,null);
            Point point = new Point(pointAnswer.x - offset, pointAnswer.y + current_question.getHeight(null) + interval);
            for (int i = 0; i < current_answers.size(); i++){
                current_answers.get(i).draw(g, point, cursor == i);
            }
        }

    }
    long last_press = 0;
    int delay = 100;
    int selected_ans = -1;
    void update(Game game){
        if (!(lives > 0)){
            work = false;
            return;
        }
        if (game.current_time - last_press > delay && (game.leftPressed || game.rightPressed || game.dialogKeyPressed)) {
            last_press = game.current_time;
            if (game.rightPressed && cursor < current_answers.size()) {
                if (cursor == current_answers.size()-1)
                    cursor = 0;
                else
                    cursor++;
            }
            else if (game.leftPressed && cursor >= 0) {
                if (cursor == 0)
                    cursor = current_answers.size()-1;
                else
                    cursor--;
            }
            else if (game.dialogKeyPressed){
                selected_ans = cursor;
            }
        }
        //System.out.println(cursor + " "  + (game.current_time - last_press));
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
        gameObjects.add(kolona_1.makeThisDialog(arrayList, new Rectangle((int)background.point.x, (int)background.point.y, background.getWidth(), background.getHeight())));

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



        kolona_1.makeThisTester(test);
    }
    final static int ID = 0;
    public GameObject mike_1 = new GameObject(getImage("pic/mike.png"), 300, 350);
    public GameObject mike_2 = new GameObject(getImage("pic/mike.png"), 150, 200);
    public GameObject kolona_1 = new GameObject(getImage("pic/kalonna_test.png"), 610, 250);
    //public GameObject stena_1 = new GameObject(getImage("pic/vhodv_down_c.png"),307, 673);
    //public GameObject stena_2 = new GameObject(getImage("pic/vhodv_down_c.png"),682, 673);
    public First_room getRoom(Game game){
        background = new Sprite(getImage("pic/Vhod.png"));

//        BufferedImage img = getBufferedImage("pic/vhodv2_original.png");
//        Image image = img.getScaledInstance(Game.WIDTH, Game.HEIGHT, Image.SCALE_SMOOTH);
//        background = new Sprite(image);

        background.set_coord((game.getWidth() - background.getWidth())/2, (game.getHeight()-background.getHeight())/2);
        game.hero.set_coord(background.point.x + background.getWidth()/2, background.point.y+background.getHeight()-game.hero.getHeight()-70);
        gameObjects.add(mike_1.enableCollision());
        gameObjects.add(mike_2.enableCollision());
        //LoadEnemy();
        LoadTest();
        //loadDialog();
        LoadWall(loadImage("/pic/VhodVkoolegeRamka.png"));
        gameObjects.add(kolona_1);
        //gameObjects.add(kolona_1.makeThisDoor(Second_room.ID));
        //gameObjects.add(stena_1);
        //gameObjects.add(stena_2);
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