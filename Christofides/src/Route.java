public class Route {

    private PointDePassage pointA;
    private PointDePassage pointB;
    private int distance;
    private boolean parcouru;

    //Construtor
    public Route(PointDePassage pointA, PointDePassage pointB,int distance) {
        this.pointA = pointA;
        this.pointB = pointB;
        this.distance = distance;
        this.parcouru = false;
    }

    //Getter
    public PointDePassage getPointA() {
        return pointA;
    }

    public PointDePassage getPointB() {
        return pointB;
    }

    public boolean isParcouru() {return parcouru; }

    public int getDistance() {
        return distance;
    }

    //Setter
    public void setParcouru(boolean parcouru) {
        this.parcouru = parcouru;
    }

}
