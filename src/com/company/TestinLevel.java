package com.company;

import com.company.Game;
import com.company.Point;
import com.company.Room;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;

class TestinLevel {
    ArrayList<Image> questions = new ArrayList<>();
    ArrayList<ArrayList<Answer>> answers = new ArrayList<>();
    Iterator<Image> iterator_questions;
    Iterator<ArrayList<Answer>> iterator_answers;
    Image current_question;
    ArrayList<Answer> current_answers;
    int cursor = 0;
    com.company.Point pointAnswer;
    com.company.Point pointRoom;
    int lives = 3;
    int offset = 0;

    public boolean isWork() {
        return work;
    }

    public static class Answer{
        com.company.Point point;
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
        public void draw(Graphics g, com.company.Point point, boolean selected){
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
        pointAnswer = new com.company.Point(0,0);
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
            com.company.Point point = new com.company.Point(pointAnswer.x - offset, pointAnswer.y + current_question.getHeight(null) + interval);
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