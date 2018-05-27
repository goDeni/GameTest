package com.company;

class Calc{
    //    x = r*cos(fi)
    //    y = r*sin(fi)
    static double shapeX(double r, double fi){
        return r * Math.cos(fi);
    }
    static double shapeY(double r, double fi){
        return r * Math.sin(fi);
    }
    static double findX(Point A, Point B, double cy){
        return ((cy - A.y) * (B.x - A.x)) / (B.y - A.y) + A.x;
    }
    static double findY(Point A, Point B, double cx){
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