package com.company;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
class Door{
    final int target;
    final boolean isDoor;
    Door(int target, boolean isDoor) {
        this.target = target;
        this.isDoor = isDoor;
    }
}
class Room{
    Sprite background;
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

    public void draw(Graphics g) {
        background.draw(g);
        for (int i = 0; i < gameObjects.size(); i++) {
            gameObjects.get(i).draw(g);
            if (gameObjects.get(i).dialog != null && gameObjects.get(i).dialog.started){
                gameObjects.get(i).dialog.drawDialog(g);
            }
        }
//        for (int i = 0; i < gameWall.size(); i++) {
//            if (i%2 == 0)
//                g.setColor(Color.red);
//            else
//                g.setColor(Color.green);
//            gameWall.get(i).draw(g);
//        }
    }
    boolean last_press_dialog = false;
    void update(Game game, int delay){
        if (!dialog_mode)
            game.hero.updateCoords(game, true);
        else{
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
        }
    }
}
class First_room extends Room{
    void loadDialog(){
        ArrayList<Image> arrayList = new ArrayList<>();
        arrayList.add(getImage("pic/mike.png"));
        arrayList.add(getImage("pic/HeroStayB.png"));
        arrayList.add(getImage("pic/test1.png"));
        gameObjects.add(kolona_1.makeThisDialog(arrayList, new Rectangle((int)background.point.x, (int)background.point.y, background.getWidth(), background.getHeight())));

    }
    final static int ID = 0;
    public GameObject mike_1 = new GameObject(getImage("pic/mike.png"), 300, 350);
    public GameObject mike_2 = new GameObject(getImage("pic/mike.png"), 150, 200).disableCollision();
    public GameObject kolona_1 = new GameObject(getImage("pic/kalonna_test.png"), 610, 250);
    //public GameObject stena_1 = new GameObject(getImage("pic/vhodv_down_c.png"),307, 673);
    //public GameObject stena_2 = new GameObject(getImage("pic/vhodv_down_c.png"),682, 673);
    public First_room getRoom(Game game){
        background = new Sprite(getImage("pic/Vhod.png"));
        background.set_coord((game.getWidth() - background.getWidth())/2, (game.getHeight()-background.getHeight())/2);
        game.hero.set_coord(background.point.x + background.getWidth()/2, background.point.y+background.getHeight()-game.hero.getHeight()-70);
        gameObjects.add(mike_1);
        gameObjects.add(mike_2);
        loadDialog();
        LoadWall(loadImage("/pic/VhodVkoolegeRamka.png"));
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