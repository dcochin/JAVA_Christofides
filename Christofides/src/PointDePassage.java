import java.util.ArrayList;

public class PointDePassage {
    private String nom;
    private boolean parcouru;
    private ArrayList<Route> listeRoute = new ArrayList<>();

    //Constructeur
    public PointDePassage(String nom) {
        this.nom = nom;
        this.parcouru = false;
    }

    //Getter
    public String getNom() {
        return nom;
    }

    public boolean isParcouru() { return parcouru;
    }
    public ArrayList<Route> getListeRoute() {
        return listeRoute;
    }

    //Setter
    public void setParcouru(boolean parcouru) {
        this.parcouru = parcouru;
    }

}
