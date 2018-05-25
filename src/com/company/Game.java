package com.company;

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
    public boolean keyPress = false;
    long last_keypress = 0;
    int delay_keypress = 0;
    boolean running = false;
    class KeyInputHandler extends KeyAdapter {
        public void keyPressed(KeyEvent e) { //клавиша нажата
            delay_keypress = (int) (System.currentTimeMillis() - last_keypress);
            last_keypress = System.currentTimeMillis();
            keyPress = true;
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP){
                upPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN){
                downPressed = true;
            }
            if (e.getKeyCode() == KeyEvent.VK_E){
                dialogKeyPressed = true;
            }
        }
        public void keyReleased(KeyEvent e) { //клавиша отпущена
            keyPress = false;
            if (e.getKeyCode() == KeyEvent.VK_LEFT) {
                leftPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                rightPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_UP){
                upPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_DOWN){
                downPressed = false;
            }
            if (e.getKeyCode() == KeyEvent.VK_E){
                dialogKeyPressed = false;
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
    void loadLevel(int level){
        System.out.println(level);
        switch (level){
            case First_room.ID:
                room = new First_room().getRoom(this);
                break;
            case Second_room.ID:
                room = new Second_room().getRoom(this);
                break;
        }
    }

    void loadHero(){
        int size = 6;
        Image heroStay = ImageManager.biggerImage(getImage("pic/Hero1/HeroStay.png"),size);
        Image foot = ImageManager.biggerImage(getImage("pic/Hero1/foot.png"),size);
        Image heroStayL = ImageManager.biggerImage(getImage("pic/Hero1/HeroStayL.png"),size);
        Image heroLeft = ImageManager.biggerImage(getImage("pic/Hero1/HeroLeft.png"),size);
        Image heroLeft2 = ImageManager.biggerImage(getImage("pic/Hero1/HeroLeft2.png"),size);
        Image heroRight = ImageManager.biggerImage(getImage("pic/Hero1/HeroRight.png"),size);
        Image heroRight2 = ImageManager.biggerImage(getImage("pic/Hero1/HeroRight2.png"),size);
        hero = new GamePerson(heroStay, 0, 0);
        ArrayList<Image> heroImages = new ArrayList<>();

        //hero.setStayImage(getImage("pic/Hero/HeroStay.png"));

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
        loadLevel(First_room.ID);
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
    public static int WIDTH = 1200; //ширина
    public static int HEIGHT = 800; //высота
    public static String NAME = "TUTORIAL 1"; //заголовок окна
    public static void main(String[] args) {
        Game game = new Game();
        game.setPreferredSize(new Dimension(WIDTH, HEIGHT));

        JFrame frame = new JFrame(Game.NAME);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(game, BorderLayout.CENTER);
        frame.pack();
        //frame.setResizable(false);
        frame.setVisible(true);

        game.start();

    }
}
