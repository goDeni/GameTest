package com.company;

import org.omg.PortableServer.POA;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class Game extends Canvas implements Runnable {
    public boolean leftPressed = false;
    public boolean rightPressed = false;
    public boolean upPressed = false;
    public boolean downPressed = false;
    public boolean dialogKeyPressed = false;
    public boolean keyMinus = false;
    public boolean keyPlus = false;
    public boolean keyPress = false;
    public boolean keyX = false;
    long last_keypress = 0;
    int delay_keypress = 0;
    boolean running = false;
    class KeyInputHandler extends KeyAdapter {
        public void keyPressed(KeyEvent e) { //клавиша нажата
            delay_keypress = (int) (System.currentTimeMillis() - last_keypress);
            last_keypress = System.currentTimeMillis();
            keyPress = true;
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = true;
            }else
            if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = true;
            }else
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
                upPressed = true;
            }else
            if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
                downPressed = true;
            }else
            if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ENTER){
                dialogKeyPressed = true;
            }else if (e.getKeyCode() == KeyEvent.VK_CONTROL){
                keyMinus = true;
            } else if (e.getKeyCode() == KeyEvent.VK_SHIFT){
                keyPlus = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_X){
                keyX = true;
            }
        }
        public void keyReleased(KeyEvent e) { //клавиша отпущена
            keyPress = false;
            if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_A) {
                leftPressed = false;
            }else
            if (e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_D) {
                rightPressed = false;
            }else
            if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_W){
                upPressed = false;
            }else
            if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_S){
                downPressed = false;
            } else
            if (e.getKeyCode() == KeyEvent.VK_E || e.getKeyCode() == KeyEvent.VK_ENTER){
                dialogKeyPressed = false;
            } else if (e.getKeyCode() == KeyEvent.VK_CONTROL){
                keyMinus = false;
            }else if (e.getKeyCode() == KeyEvent.VK_SHIFT){
                keyPlus = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_X){
                keyX = false;
            }
        }
    }
    public long current_time = 0;
    @Override
    public void run() {
        this.requestFocus();
        long LastTime = System.nanoTime();
        double amountOfTicks=60;
        double ns = 1000000000/amountOfTicks;
        double delta=0;
        long timer=System.currentTimeMillis();
        int frames=0;

        init();

        while (running){
            long now = System.nanoTime();
            delta+=(now-LastTime)/ns;
            LastTime=now;
            while (delta>=1){
                delta--;
                current_time = System.currentTimeMillis();
                update(current_time/1000);
                render();
                frames++;
            }
            if (System.currentTimeMillis()-timer>1000){
                timer+=1000;
                System.out.println("FPS: " + frames);
                frames=0;
            }
        }
    }
    public GamePerson hero;
    Room room;
    Room save_room;
    First_room first_room;
    Second_room second_room;
    Third_room third_room;
    Four_room four_room;
    Fight_room fight_room;
    Five_room five_room;
    Six_room six_room;
    void loadLevels(){
        first_room = new First_room().getRoom(this);
        second_room = new Second_room().getRoom(this);
        third_room = new Third_room().getRoom(this);
        fight_room = new Fight_room().getRoom(this);
        four_room = new Four_room().getRoom(this);
        five_room = new Five_room().getRoom(this);
        six_room = new Six_room().getRoom(this);
    }
    void LoadFight(){
        save_room = room;
        room = fight_room.setTest(this.room);
    }
    void EndFight(){
        room = save_room;
        room.setHero(this, null);
        save_room = null;
        room.testinLevel = null;
        room.test_mod = false;
    }
    void loadLevel(int level, Point point){
        System.out.println(level);
        switch (level){
            case First_room.ID:
                room = first_room.setHero(this, point);
                break;
            case Second_room.ID:
                room = second_room.setHero(this, point);
                break;
            case Third_room.ID:
                room = third_room.setHero(this, point);
                break;
            case Four_room.ID:
                room = four_room.setHero(this, point);
                break;
            case Five_room.ID:
                room = five_room.setHero(this, point);
                break;
            case Six_room.ID:
                room = six_room.setHero(this,point);
                break;
        }
    }
    public int size_hero = 6;
    void loadHero(){
        loadHero(size_hero, new Point(0,0));
    }
    void loadHero(int size, Point point){
        Image heroStay = getImage("pic/Hero1/HeroStay.png");
        Image foot = getImage("pic/Hero1/foot.png");
        Image heroStayL = getImage("pic/Hero1/HeroStayL.png");
        Image heroLeft = getImage("pic/Hero1/HeroLeft.png");
        Image heroLeft2 = getImage("pic/Hero1/HeroLeft2.png");
        Image heroRight = getImage("pic/Hero1/HeroRight.png");
        Image heroRight2 = getImage("pic/Hero1/HeroRight2.png");
        hero = new GamePerson(heroStay, (int)point.x, (int)point.y);
        ArrayList<Image> heroImages = new ArrayList<>();

        hero.setFoot(foot);

        hero.setStayImageLeft(heroStayL);
        hero.setStayImageRight(heroStay);

        heroImages.add(heroLeft);
        heroImages.add(heroLeft2);

        hero.setAnimationLeft(heroImages);
        heroImages = new ArrayList<>();
        heroImages.add(heroRight);
        heroImages.add(heroRight2);

        hero.setAnimationRight(heroImages);


    }
    public void init(){
        addKeyListener(new KeyInputHandler());
        loadHero();
        loadLevels();
        loadLevel(First_room.ID, null);
    }

    private static int x = 0;
    private static int y = 0;
    public void render(){
        BufferStrategy bs = getBufferStrategy();
        if (bs == null){
            createBufferStrategy(2);
            requestFocus();
            return;
        }

        Graphics g = bs.getDrawGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0,0,getWidth(),getHeight());

        room.draw(g, this);

        g.dispose();
        bs.show();
    }
    long last_update;
    public void update(double time){
        long time_now = System.currentTimeMillis();
        int delay = (int) (last_update - time_now);
        last_update = time_now;
        if (room != null)
            room.update(this, delay);
    }
    public void start(){
        running = true;
        new Thread(this).start();
    }
    public Image getImage(String path) {
        BufferedImage sourceImage = null;

        try {
            URL url = this.getClass().getClassLoader().getResource(path);
            sourceImage = ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Toolkit.getDefaultToolkit().createImage(sourceImage.getSource());
    }
    //public static int WIDTH = 1200; //ширина
    public static int gameHeight = 800;
    public static int gameWidth = 1200;
    //public static int HEIGHT = 1000; //высота
    public static String NAME = "Симулятор студента"; //заголовок окна
    public static void main(String[] args) {
        Dimension sSize = Toolkit.getDefaultToolkit ().getScreenSize ();
        Game game = new Game();
        game.setPreferredSize(new Dimension((int)sSize.getWidth(), (int)sSize.getHeight()));

        JFrame frame = new JFrame(Game.NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
        game.start();

    }
}
