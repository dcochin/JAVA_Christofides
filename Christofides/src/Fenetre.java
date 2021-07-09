import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Fenetre {

    /**
     * Propriétés pour l'interface
     **/
    private JPanel panneau;
    private JList list_Points;
    private JTextField tF_PointName;
    private JButton bt_AddPoint;
    private JButton bt_RemovePoint;
    private JComboBox cB_PointA;
    private JComboBox cB_PointB;
    private JButton bt_AddEdge;
    private JList list_Edges;
    private JTabbedPane tabbedPane1;
    private JTextField tF_Distance;
    private JButton bt_JeuEssai;
    private JButton bt_Algo;
    private JList list_PrimPoints;
    private JList list_PrimEdges;
    private JButton bt_Reset;
    private JTextArea txt_Eulerien;
    private JTextArea txt_Hamilton;


    /**
     * Propriété pour le programme
     **/
    private ArrayList<PointDePassage> arbreDePoidsMinimal = new ArrayList<>();
    private HashMap<String, PointDePassage> grapheBase = new HashMap<>();
    private ArrayList<PointDePassage> grapheCouplageParfait = new ArrayList<>();
    private ArrayList<String> circuitEulerien = new ArrayList<>();
    private ArrayList<String> circuitHamiltonien = new ArrayList<>();
    private ArrayList<Route> routeParfaite = new ArrayList<>();

    public Fenetre() {
        JFrame fenetre = new JFrame("Algorithme de Christofides");
        fenetre.setContentPane(this.panneau);
        fenetre.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fenetre.pack();
        fenetre.setVisible(true);

        /** Bouton pour ajouter un point dans le graphe **/
        bt_AddPoint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                grapheBase.put(tF_PointName.getText(), new PointDePassage(tF_PointName.getText()));
                majJlist_PointGrapheBase();
            }
        });


        /** Bouton pour Réinitialiser le programme **/
        bt_Reset.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /** les listes du programme **/
                arbreDePoidsMinimal.clear();
                grapheBase.clear();
                grapheCouplageParfait.clear();
                circuitEulerien.clear();
                circuitHamiltonien.clear();
                routeParfaite.clear();

                /** les champs du formulaire  **/
                cB_PointA.removeAllItems();
                cB_PointB.removeAllItems();
                majJlist_PointGrapheBase();
                majJlist_ArbrePointMinimal(arbreDePoidsMinimal);
                txt_Eulerien.setText("");
                txt_Hamilton.setText("");
                tF_PointName.setText("");
                tF_Distance.setText("");
            }
        });

        /** Bouton pour supprimer un point du graphe **/
        bt_RemovePoint.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /** on appelle la méthode qui supprimera correctement le point et toutes les aretes liées **/
                removePoint(list_Points.getSelectedValue().toString());
            }
        });

        /** Bouton pour lancer l'agorithme **/
        bt_Algo.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String pointDeDepart = list_Points.getSelectedValue().toString();

                /** Prim **/
                arbreDePoidsMinimal(grapheBase.get(pointDeDepart).getNom());
                /** Couplage Parfait **/
                couplageParfait();
                /** Hierholzer **/
                Hierholzer();

            }
        });

        /** Bouton pour ajouter un jeu d'essai**/
        bt_JeuEssai.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                /** on ajoute les points **/
                ajoutGraphe();
                /** on met à jour la JList de gauche**/
                majJlist_PointGrapheBase();
            }
        });

        /** Bouton pour ajouter un sommet et le lié à ses points **/
        bt_AddEdge.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                String cBvalueA = (String) cB_PointA.getSelectedItem();
                String cBvalueB = (String) cB_PointB.getSelectedItem();

                ajoutChemin(cBvalueA, cBvalueB, Integer.parseInt(tF_Distance.getText()));

                majJlist_PointGrapheBase();

            }
        });

        /** Dans l'onglet "Données" OnClick sur la list de gauche, on affiche sa liste d'arrete dans la liste de droite **/
        list_Points.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DefaultListModel ModeleListRoute = new DefaultListModel();
                /** pour chaque element du hashmap on recherche ses routes pour les ajouter au model qui servira à aliment la Jlist **/
                for (Map.Entry mapentry : grapheBase.entrySet()) {

                    if (grapheBase.get(mapentry.getKey()).getNom() == list_Points.getSelectedValue()) {
                        for (Route route : grapheBase.get(mapentry.getKey()).getListeRoute()) {
                            ModeleListRoute.addElement(route.getPointA().getNom() + " - " + route.getPointB().getNom() + " - " + route.getDistance() + " - " + route);
                        }
                    }
                }
                list_Edges.setModel(ModeleListRoute);
            }
        });

        /** Dans l'onglet "Résultat" OnClick sur la list de gauche, on affiche sa liste d'arrete dans la liste de droite **/
        list_PrimPoints.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                DefaultListModel ModeleListRoute = new DefaultListModel();

                /** pour chaque element de l'arraylist on recherche ses routes pour les ajouter au model qui servira à aliment la Jlist **/
                for (int i = 0; i < arbreDePoidsMinimal.size(); i++) {
                    if (arbreDePoidsMinimal.get(i).getNom() == list_PrimPoints.getSelectedValue()) {
                        for (Route route : arbreDePoidsMinimal.get(i).getListeRoute()) {
                            ModeleListRoute.addElement(route.getPointA().getNom() + " - " + route.getPointB().getNom() + " - " + route.getDistance() + " - " + route);
                        }
                    }
                }

                list_PrimEdges.setModel(ModeleListRoute);
            }
        });
    }

    public void removePoint(String point) {

        Iterator<String> iterator = grapheBase.keySet().iterator();
        while (iterator.hasNext()) {
            String element = iterator.next();
            if (element.contains(point)) {
                /** Pour chaque route dans la liste de route du point actuel **/
                for (int i = 0; i < grapheBase.get(point).getListeRoute().size(); i++) {
                    String autrePoint;
                    /** Si le point A = le point actuel, on traite le point B **/
                    if (grapheBase.get(point).getListeRoute().get(i).getPointA().getNom().equals(grapheBase.get(point).getNom())) {
                        autrePoint = grapheBase.get(point).getListeRoute().get(i).getPointB().getNom();
                    } else {
                        autrePoint = grapheBase.get(point).getListeRoute().get(i).getPointA().getNom();

                    }
                    /** Pour chaque route dans la liste de route du point B **/
                    for (int j = 0; j < grapheBase.get(autrePoint).getListeRoute().size(); j++) {
                        /** Si le point A = le point actuel, on supprime la route **/
                        if (grapheBase.get(autrePoint).getListeRoute().get(j).getPointB().getNom().equals(grapheBase.get(point).getNom())
                                || grapheBase.get(autrePoint).getListeRoute().get(j).getPointA().getNom().equals(grapheBase.get(point).getNom())) {
                            grapheBase.get(autrePoint).getListeRoute().remove(j);
                        }
                    }
                }
                /** on supprime le point **/
                iterator.remove();
            }
        }
        /** on met à jour la Jliste **/
        majJlist_PointGrapheBase();
    }

    public void ajoutGraphe() {
        ajoutChemin("Nancy", "Metz", 57);
        ajoutChemin("Nancy", "Strasbourg", 157);
        ajoutChemin("Nancy", "Reims", 208);
        ajoutChemin("Nancy", "Mulhouse", 189);
        ajoutChemin("Nancy", "Troyes", 247);
        ajoutChemin("Nancy", "Colmar", 147);
        ajoutChemin("Nancy", "Sélestat", 130);
        ajoutChemin("Nancy", "Saint-Dié-des-Vosges", 85);
        ajoutChemin("Nancy", "Verdun", 121);
        ajoutChemin("Metz", "Strasbourg", 163);
        ajoutChemin("Metz", "Reims", 190);
        ajoutChemin("Metz", "Mulhouse", 248);
        ajoutChemin("Metz", "Troyes", 252);
        ajoutChemin("Metz", "Colmar", 206);
        ajoutChemin("Metz", "Sélestat", 184);
        ajoutChemin("Metz", "Saint-Dié-des-Vosges", 144);
        ajoutChemin("Metz", "Verdun", 80);
        ajoutChemin("Strasbourg", "Reims", 347);
        ajoutChemin("Strasbourg", "Mulhouse", 117);
        ajoutChemin("Strasbourg", "Troyes", 409);
        ajoutChemin("Strasbourg", "Colmar", 75);
        ajoutChemin("Strasbourg", "Sélestat", 53);
        ajoutChemin("Strasbourg", "Saint-Dié-des-Vosges", 95);
        ajoutChemin("Strasbourg", "Verdun", 236);
        ajoutChemin("Reims", "Mulhouse", 433);
        ajoutChemin("Reims", "Troyes", 125);
        ajoutChemin("Reims", "Colmar", 391);
        ajoutChemin("Reims", "Sélestat", 368);
        ajoutChemin("Reims", "Saint-Dié-des-Vosges", 286);
        ajoutChemin("Reims", "Verdun", 121);
        ajoutChemin("Mulhouse", "Troyes", 391);
        ajoutChemin("Mulhouse", "Colmar", 42);
        ajoutChemin("Mulhouse", "Sélestat", 65);
        ajoutChemin("Mulhouse", "Saint-Dié-des-Vosges", 104);
        ajoutChemin("Mulhouse", "Verdun", 322);
        ajoutChemin("Troyes", "Colmar", 316);
        ajoutChemin("Troyes", "Sélestat", 320);
        ajoutChemin("Troyes", "Saint-Dié-des-Vosges", 275);
        ajoutChemin("Troyes", "Verdun", 183);
        ajoutChemin("Colmar", "Sélestat", 24);
        ajoutChemin("Colmar", "Saint-Dié-des-Vosges", 63);
        ajoutChemin("Colmar", "Verdun", 281);
        ajoutChemin("Sélestat", "Saint-Dié-des-Vosges", 45);
        ajoutChemin("Sélestat", "Verdun", 259);
        ajoutChemin("Saint-Dié-des-Vosges", "Verdun", 206);


    }

    public void ajoutChemin(String pointA, String pointB, int distance) {

        /** Si le point A n'existe pas on le creer **/
        if (!grapheBase.containsKey(pointA)) {
            grapheBase.put(pointA, new PointDePassage(pointA));
        }
        /** Si le point B n'existe pas on le creer **/
        if (!grapheBase.containsKey(pointB)) {
            grapheBase.put(pointB, new PointDePassage(pointB));
        }

        /** créer la route **/
        Route nouvelleRoute = new Route(grapheBase.get(pointA), grapheBase.get(pointB), distance);

        /** on ajoute la route à la liste de route des points concernés **/
        grapheBase
                .get(pointA)
                .getListeRoute().add(nouvelleRoute);

        grapheBase
                .get(pointB)
                .getListeRoute().add(nouvelleRoute);
    }

    public void majJlist_PointGrapheBase() {
        DefaultListModel ModeleListPoints = new DefaultListModel();
        DefaultComboBoxModel ModelComboBox1 = new DefaultComboBoxModel();
        DefaultComboBoxModel ModelComboBox2 = new DefaultComboBoxModel();

        /** on met la Jlist à jour en passant par un ListModel **/
        for (Map.Entry mapentry : grapheBase.entrySet()) {
            ModeleListPoints.addElement(grapheBase.get(mapentry.getKey()).getNom());
            ModelComboBox1.addElement(grapheBase.get(mapentry.getKey()).getNom());
            ModelComboBox2.addElement(grapheBase.get(mapentry.getKey()).getNom());
        }

        /** on met les combobox à jour **/
        if (grapheBase.isEmpty()) {
            ModeleListPoints.removeAllElements();
            ModelComboBox1.removeAllElements();
            ModelComboBox2.removeAllElements();
        }

        cB_PointA.setModel(ModelComboBox1);
        cB_PointB.setModel(ModelComboBox2);
        list_Points.setModel(ModeleListPoints);
        tF_PointName.setText("");
    }

    public void majJlist_ArbrePointMinimal(ArrayList<PointDePassage> listePrim) {

        /** on met la Jlist à jour en passant par un ListModel **/
        DefaultListModel ModeleListPoints = new DefaultListModel();

        for (PointDePassage ptPassage : listePrim) {
            ModeleListPoints.addElement(ptPassage.getNom());
        }
        if (listePrim.isEmpty()) {
            ModeleListPoints.removeAllElements();
        }
        list_PrimPoints.setModel(ModeleListPoints);

    }

    public void arbreDePoidsMinimal(String point) {

        /** Algorithme de Prim
         * On traite le graphe à partir du Hashmap listePointsDePassage
         * Puis on ajoute les points traités dans arbreDePoidsMinimal avec leurs arretes respectives
         * **/

        /** On ajoute le point de départ dans l'arbre de poids minimal **/
        arbreDePoidsMinimal.add(new PointDePassage(point));

        /** Tant que la liste de point dans l'arbre de poids minimal n'est pas egal à la liste des points de passage **/
        while (arbreDePoidsMinimal.size() != grapheBase.size()) {

            /** les variables qui serviront dans l'agorithme **/
            Integer distanceMini = null;
            PointDePassage pointMini = null;
            PointDePassage pointDeDepartGraphe = null;
            PointDePassage pointDeDepartArbre = null;
            Route routeMini = null;

            /** Pour chaque Point déjà présent dans l'arbre de poids minimum **/
            for (int i = 0; i < arbreDePoidsMinimal.size(); i++) {
                /** On Stocke le Point du graphe Actuellement traité dans un variable **/
                PointDePassage pointActuel = grapheBase.get(arbreDePoidsMinimal.get(i).getNom());
                /**  Pour chaque route relié au point **/
                for (int j = 0; j < pointActuel.getListeRoute().size(); j++) {
                    Route route = pointActuel.getListeRoute().get(j);
                    /**  Si la route n'est pas parcourue **/
                    if (!route.isParcouru()) {

                        /**  initialisation de route Mini si null **/
                        if (distanceMini == null) {
                            distanceMini = route.getDistance();
                        }
                        /**  initialisation de la variable de l'autre point relié par l'arrete **/
                        PointDePassage autrePoint;
                        if (route.getPointA().getNom().equals(pointActuel.getNom())) {
                            autrePoint = route.getPointB();
                        } else {
                            autrePoint = route.getPointA();
                        }

                        /** on verifie si que l'autre point ne soit pas parcouru **/
                        if (!autrePoint.isParcouru()) {
                            /** Si la distance est inferieur à distanceMini on stocke les données dans plusieurs variables
                             * qui serviront à tracer l'arbre de poids mini **/
                            if (route.getDistance() <= distanceMini) {
                                pointMini = autrePoint;
                                distanceMini = route.getDistance();
                                routeMini = route;
                                pointDeDepartGraphe = grapheBase.get(arbreDePoidsMinimal.get(i).getNom());
                                pointDeDepartArbre = arbreDePoidsMinimal.get(i);
                            }
                        }
                    }
                }
            }

            if (routeMini != null) {
                /** On indique les elements stockés comme parcourue **/
                routeMini.setParcouru(true);
                pointMini.setParcouru(true);
                pointDeDepartGraphe.setParcouru(true);

                /** On creer le nouveau point parcourue et la l'arrete qula relie au point de depart **/
                PointDePassage pointTrouvee = new PointDePassage(pointMini.getNom());
                Route routeTrouvee = new Route(pointDeDepartArbre, pointTrouvee, routeMini.getDistance());
                /** On ajoute la route à la liste de route au point d'arrivé **/
                pointTrouvee.getListeRoute().add(routeTrouvee);

                /** On ajoute la route à la liste de route du point de départ **/
                pointDeDepartArbre.getListeRoute().add(routeTrouvee);

                /** On ajoute le point d'arrivée à la l'arbre de poids mini **/
                arbreDePoidsMinimal.add(pointTrouvee);
            }
        }

        /** on regarde quelle sommet a un degré pair et on le retire du graphe de base **/
        for (PointDePassage ptPassage : arbreDePoidsMinimal) {
            if (ptPassage.getListeRoute().size() % 2 == 0) {
                removePoint(ptPassage.getNom());
            }
        }

        /** On met à jour l'interface
         *  **/
        majJlist_PointGrapheBase();
        majJlist_ArbrePointMinimal(arbreDePoidsMinimal);

    }

    public void couplageParfait() {

        /** on lance la méthode pour reinitailiser toutes les propriétés des points et des routes du graphe de Base à False **/
        graphResetParcouru();

        /** Les variables **/
        Iterator<String> iterator = grapheBase.keySet().iterator();
        Integer distanceMini;
        PointDePassage pointMini = null;
        PointDePassage pointDeDepartGraphe = null;


        /** Tant que la liste de point dans le graphe de couplage parfait n'est pas egal à la liste des points de passage **/
        while (grapheBase.size() != grapheCouplageParfait.size()) {

            /** pour chaque itération du Hashmap grapheBase **/
            while (iterator.hasNext()) {

                String it = iterator.next();
                distanceMini = null;
                pointMini = null;
                PointDePassage pointActuelGraphe = grapheBase.get(it);

                /** si le point actuel n'est pas parcouru **/
                if (!pointActuelGraphe.isParcouru()) {

                    /** Pour chaque route du point actuel **/
                    for (int i = 0; i < pointActuelGraphe.getListeRoute().size(); i++) {

                        /**  initialisation de la variable de l'autre point relié par l'arrete **/
                        PointDePassage autrePoint;
                        if (pointActuelGraphe.getListeRoute().get(i).getPointA() == pointActuelGraphe) {
                            autrePoint = pointActuelGraphe.getListeRoute().get(i).getPointB();
                        } else {
                            autrePoint = pointActuelGraphe.getListeRoute().get(i).getPointA();
                        }

                        /** si l'autre point n'est pas parcouru **/
                        if (!autrePoint.isParcouru()) {

                            /** on initalise distance mini si est null **/
                            if (distanceMini == null) {
                                distanceMini = pointActuelGraphe.getListeRoute().get(i).getDistance();
                            }

                            /** on stocke les variables du des points et distance mini si la distance est plus petite **/
                            if (pointActuelGraphe.getListeRoute().get(i).getDistance() <= distanceMini) {
                                distanceMini = pointActuelGraphe.getListeRoute().get(i).getDistance();
                                pointMini = autrePoint;
                                pointDeDepartGraphe = pointActuelGraphe;
                            }
                        }
                    }

                    /** Si le resultat est null on casse la boucle
                     * utilise si il y a un nombre de point impaire
                     * pour arreter la boucle**/
                    if (pointMini == null) {
                        break;
                    }

                    /** on note les points comme parcourue **/
                    pointMini.setParcouru(true);
                    pointDeDepartGraphe.setParcouru(true);


                    /** on verifie que si le point depart n'existe pas déja pour l'ajouter  **/
                    boolean existe = false;
                    for (PointDePassage ptPassage : grapheCouplageParfait) {
                        if (ptPassage.getNom().equals(pointDeDepartGraphe.getNom())) {
                            existe = true;
                        }
                    }
                    if (!existe) {
                        grapheCouplageParfait.add(new PointDePassage(pointDeDepartGraphe.getNom()));
                    }


                    /** on ajoute le point d'arrivé **/
                    PointDePassage nouveauPoint = new PointDePassage(pointMini.getNom());
                    grapheCouplageParfait.add(nouveauPoint);

                    /** initialisation de la variable nouvelle route **/
                    Route nouvelleRoute = null;


                    /** on relie la route aux points **/
                    for (PointDePassage ptArbre : grapheCouplageParfait) {
                        if (ptArbre.getNom().equals(pointDeDepartGraphe.getNom())) {
                            nouvelleRoute = new Route(ptArbre, nouveauPoint, distanceMini);
                            routeParfaite.add(nouvelleRoute);
                            ptArbre.getListeRoute().add(nouvelleRoute);
                        }
                        if (ptArbre.getNom().equals(nouveauPoint.getNom())) {
                            ptArbre.getListeRoute().add(nouvelleRoute);
                        }

                    }

                }

            }
            /** Si le resultat est null on casse la boucle
             * utilise si il y a un nombre de point impaire
             * pour arreter la boucle**/
            if (pointMini == null) {
                break;
            }
        }


        /** On fusionne l'arbre de poids minimal avec le graphe de couplage parfait**/
        /** Pour chaque points du graphe de couplage parfait **/
        for (int ptParfait = 0; ptParfait < grapheCouplageParfait.size(); ptParfait = ptParfait + 2) {
            /** Pour chaque points de l'arbre minimal **/
            for (PointDePassage ptMinimal : arbreDePoidsMinimal) {
                /** si les noms coincide on opere **/
                if (grapheCouplageParfait.get(ptParfait).getNom().equals(ptMinimal.getNom())) {

                    /** variables **/
                    PointDePassage autrePoint;
                    Route nouvelleRoute;

                    /** on identifit l'autre point **/
                    if (grapheCouplageParfait.get(ptParfait) == grapheCouplageParfait.get(ptParfait).getListeRoute().get(0).getPointA()) {
                        autrePoint = grapheCouplageParfait.get(ptParfait).getListeRoute().get(0).getPointB();
                    } else {
                        autrePoint = grapheCouplageParfait.get(ptParfait).getListeRoute().get(0).getPointA();
                    }

                    /** on recherche l'autre point pour creer et ajouter la route aux points concernés **/
                    for (PointDePassage ptMinimal2 : arbreDePoidsMinimal) {
                        if (ptMinimal2.getNom().equals(autrePoint.getNom())) {
                            nouvelleRoute = new Route(ptMinimal, ptMinimal2, grapheCouplageParfait.get(ptParfait).getListeRoute().get(0).getDistance());
                            ptMinimal.getListeRoute().add(nouvelleRoute);
                            ptMinimal2.getListeRoute().add(nouvelleRoute);

                        }
                    }
                }
            }
        }

        /**
         * on met a jour l'interface
         **/
        majJlist_PointGrapheBase();

        majJlist_ArbrePointMinimal(arbreDePoidsMinimal);


    }


    public void graphResetParcouru() {
        Iterator<String> iterator = grapheBase.keySet().iterator();
        while (iterator.hasNext()) {
            String point = iterator.next();

            /** Pour chaque route dans la liste de route du point actuel
             * on reset la propriété parcou à false afin réutiliser le Hashmap pour la suite **/
            for (int i = 0; i < grapheBase.get(point).getListeRoute().size(); i++) {
                grapheBase.get(point).getListeRoute().get(i).setParcouru(false);
                grapheBase.get(point).setParcouru(false);
            }
        }
    }


    public void Hierholzer() {

        /** une liste de liste afin de stocker tout les circuits **/
        ArrayList<ArrayList<PointDePassage>> listeCycle = new ArrayList<ArrayList<PointDePassage>>();
        listeCycle.add(new ArrayList<PointDePassage>());

        /** on ajoute le premier point au premier cycle **/
        listeCycle.get(0).add(new PointDePassage(arbreDePoidsMinimal.get(0).getNom()));

        /** Boucle Cycle **/
        for (int liste = 0; liste < listeCycle.size(); liste++) {

            /** Boucle Point **/
            for (int pointCycle = 0; pointCycle < listeCycle.get(liste).size(); pointCycle++) {

                /** initialisation des variables **/
                PointDePassage pointActuelArbreCouplé = null;
                PointDePassage autrePoint = null;
                Route routeActuelleGlouton = null;
                boolean cycleFerme = false;
                PointDePassage nouveauPointCycle = null;
                Route passageRoute = null;


                /** on recherche le point correspondant dans l'arbre minimal + couplage parfait
                 * et on le stock dans la variable pointActuelGlouton  **/
                for (PointDePassage pointDePassage : arbreDePoidsMinimal) {
                    if (pointDePassage.getNom().equals(listeCycle.get(liste).get(pointCycle).getNom())) {
                        pointActuelArbreCouplé = pointDePassage;
                    }
                }

                /** Par Sécurité, mais pas sure que ce soit utile **/
                if (pointActuelArbreCouplé == null) {
                    break;
                }

                /** Pour chaque route du point du graphe complet **/
                for (int j = 0; j < pointActuelArbreCouplé.getListeRoute().size(); j++) {

                    routeActuelleGlouton = pointActuelArbreCouplé.getListeRoute().get(j);

                    /** Si la route n'est pas parcourue **/
                    if (!routeActuelleGlouton.isParcouru()) {

                        /** on identifit l'autre point **/
                        if (pointActuelArbreCouplé.getListeRoute().get(j).getPointA() == pointActuelArbreCouplé) {
                            autrePoint = pointActuelArbreCouplé.getListeRoute().get(j).getPointB();
                        } else {
                            autrePoint = pointActuelArbreCouplé.getListeRoute().get(j).getPointA();
                        }
                        nouveauPointCycle = autrePoint;
                        passageRoute = routeActuelleGlouton;
                        /** si l'autre point = le point de depart alors on ferme le circuit **/
                        if (autrePoint.getNom().equals(listeCycle.get(liste).get(0).getNom())) {
                            cycleFerme = true;
                            break;
                        }
                    }
                }

                String ajoutPoint = null;
                if (nouveauPointCycle != null) {
                    /** on recupere le dernier point traité
                     * on indique la route comme parcouru
                     * et on ajoute le point à la liste
                     */
                    passageRoute.setParcouru(true);
                    listeCycle.get(liste).add(new PointDePassage(nouveauPointCycle.getNom()));
                }

                /** Si le cycle est fermé
                 * on recherche un point du premier cycle qui a encore une route non parcourue
                 * puis on l'ajoute au prochain cycle*/
                if (cycleFerme) {
                    for (int point = 0; point < listeCycle.get(liste).size(); point++) {
                        for (PointDePassage pointDePassage : arbreDePoidsMinimal) {
                            if (pointDePassage.getNom().equals(listeCycle.get(liste).get(point).getNom())) {
                                for (Route route : pointDePassage.getListeRoute()) {
                                    if (!route.isParcouru()) {
                                        if (route.getPointA().getNom().equals(pointDePassage.getNom()) || route.getPointB().getNom().equals(pointDePassage.getNom())) {
                                            ajoutPoint = pointDePassage.getNom();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (ajoutPoint != null) {
                        listeCycle.add(new ArrayList<PointDePassage>());
                        listeCycle.get(liste + 1).add(new PointDePassage(ajoutPoint));
                        break;
                    }
                }

            }

        }

        /** tracé le circuit eulérien
         * */
        /** pour chaque liste de liste de point */
        for (int liste = 0; liste < listeCycle.size(); liste++) {
            /** pour chaque point de la liste */
            for (int point = 0; point < listeCycle.get(liste).size() - 1; point++) {
                /** si la taille du cycle est supérieur a 1 */
                if (listeCycle.size() > 1) {
                    try {
                        /** si la le premier point de  liste + 1  = le point en cours de liste
                         * et on ajoute le cycle au circuit eulérien
                         * sinon on ajoute le point en cours au circuit*/
                        if (listeCycle.get(liste + 1).get(0).getNom().equals(listeCycle.get(liste).get(point).getNom())) {
                            for (int point2 = 0; point2 < listeCycle.get(liste + 1).size(); point2++) {
                                circuitEulerien.add(listeCycle.get(liste + 1).get(point2).getNom());
                            }
                        } else {
                            circuitEulerien.add(listeCycle.get(liste).get(point).getNom());
                        }
                    } catch (IndexOutOfBoundsException ignored) {
                    }

                } else {
                    circuitEulerien.add(listeCycle.get(liste).get(point).getNom());
                }
            }
        }
        circuitEulerien.add(listeCycle.get(0).get(listeCycle.get(0).size() - 1).getNom());

        /** on alimente la zone de texte correspondante */
        for (String pointDePassage : circuitEulerien) {
            txt_Eulerien.append(pointDePassage + " -> ");
        }

        /** On ajoute chaque point du circuit eulérien dans dans le circuit hamiltonien
         * en verifiant si il existe déjà dnas le circuit Hamiltonien */
        for (int pointEulerien = 0; pointEulerien < circuitEulerien.size() ; pointEulerien++) {
            boolean existeDansHamilton = false;
            if (circuitHamiltonien.isEmpty()) {
                circuitHamiltonien.add(circuitEulerien.get(0));
            } else {
                /** ce If permet de ne pas faire la verification pour le dernier point */
                if (pointEulerien != circuitEulerien.size()-1) {
                    for (int pointHamiltonien = 0; pointHamiltonien < circuitHamiltonien.size(); pointHamiltonien++) {
                        if (circuitEulerien.get(pointEulerien).equals(circuitHamiltonien.get(pointHamiltonien))) {
                            existeDansHamilton = true;
                            break;
                        }
                    }
                }

                /** Si il n'exite pas on l'ajoute */
                if (!existeDansHamilton) {
                    circuitHamiltonien.add(circuitEulerien.get(pointEulerien));
                }
            }
        }
        /** on alimente la zone de texte correspondante */
        for (String pointDePassage : circuitHamiltonien) {
            txt_Hamilton.append(pointDePassage + " -> ");
        }


    }

}


