import java.util.ArrayList;
import java.util.HashMap;

//FIXME update design document for HIT and SUNK methods
public class GameBoards {
    SeaMap main_map = new SeaMap("self filed");
    SeaMap masked_map = new SeaMap("opponent field");

    public static void main(String args[]){
        SeaMap map1 = new SeaMap("Enemy's field");
        SeaMap map2 = new SeaMap("Watching enemy's field");
        map2.fillMap();
        map1.fillMap();
        System.out.println("ship placed: " + map1.placingShip(3,3, 4,3));
        System.out.println("ship placed: " + map1.placingShip(3,1, 0,1));

        System.out.println("ship placed: " + map1.placingShip(3,2, 1,2));

        //System.out.println("ship hit: " + map1.shipHit(3, 3));

        // update the Map when a user makes a move
        int hitship = map1.shipHit(3, 2);
        map2.markBombed(hitship, 3, 2);

        System.out.println(map2.printMap(map2.name));
        System.out.println(map1.printMap(map1.name));

    }
}


class Ship{
    int size;
    boolean sunk = false;
    int hole_hit = 0;
    ArrayList<Integer> coordinates = new ArrayList<>();

    Ship(int size){
        this.size = size;
    }

    // store ship's body coordinates for checking if sunk later
    public boolean addCoord (int c){
        for (int i = 0; i < coordinates.size(); i++){
            if (c == i){
                System.out.println("Ship coordinates overlapped.");
                return false;
            }
        }
        coordinates.add(c);
        return true;
    }
}



// the game board, where the battle happens
class SeaMap implements GameMap {
    int shipsunk = 0;
    int shipUnplaced = 3;
    // each user has 3 ships, each with size 2, 3, 4 to place on the board
    int ships[][] = {{2, 0}, {3, 0}, {4, 0}};
    // since this is a 10x10 board
    int index[] = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
    // index are integer keys; values are characters
    HashMap<Integer, char[]> map = new HashMap<>();
    // for keeping track of if ships are sunk
    HashMap<Integer, Ship> shipTeam = new HashMap<>();
    // for searching if new ship overlaps with existing ships
    ArrayList<Integer> coordinates = new ArrayList<>();

    String name;

    SeaMap(String mapName){
        this.name = mapName;
    }

    // initialize the empty field map
    @Override
    public void fillMap(){
        for (int i = 0; i < 10; i++){
            char point[] = new char[10];
            for (int j = 0; j < 10; j++){
                // '+': empty field
                point[j] = '+';
            }
            map.put(index[i], point);
        }
    }


    // print out the map
    @Override
    public String printMap(String mapname){

        String mapstring = "";
        mapstring = mapstring + "\n" + mapname + ": " + "\n" + "\n" + "   " ;

        for(int i = 0; i < 10; i++){
            mapstring = mapstring + index[i] + "  ";
        }

        mapstring = mapstring + "\n";

        // print each rows
        for(int i = 0; i < 10; i++){
            mapstring = mapstring + index[i] + "  ";
            for (int j = 0; j < 10; j++){
                mapstring = mapstring + map.get(i)[j] + "  ";
            }
            mapstring = mapstring + "\n";
        }

        return mapstring;
    }


    // check if ship with such size is un-placed fot placingShip() method
    // if so, place it and change the size in ships[] to 0 to show it's placed
    @Override
    public boolean shipPlaced(int size){
        boolean placed = false;

        for(int i = 0; i < ships.length; i++){
            if (ships[i][0] == size) {
                if (ships[i][1] == 1)
                placed = true;
            }
        }
        return placed;
    }


    // placing ships by changing '+' to 'o'
    @Override
    public int placingShip(int x1, int y1, int x2, int y2){

        // if the ship is placed vertically
        if (x1 == x2){
            int temp;

            // check if boat of such size is legal to place or have been placed
            int boatsize = Math.abs(y2 - y1) + 1;
            if (boatsize != 2 && boatsize != 3 && boatsize != 4){
                System.out.println("You can only place a ship with 2, 3, or 4 holes.");
                return 1;
            }

            if (shipPlaced(boatsize)){
                System.out.println("You have placed a ship with size " + boatsize +
                        " . Please place an unplaced ship.");
                return 2;
            }

            // making sure y1 < y2 for the following for loop
            if(y1 > y2){
                temp = y1;
                y1 = y2;
                y2 = temp;
            }

            Ship ship = new Ship(boatsize);

            // cast boatsize integer into a char and display it as boat body on map
            for(int i = y1; i <= y2; i++) {
                // check if new ship overlaps with old ships
                int coord = x1 * 10 + i;
                for (int j = 0; j < coordinates.size(); j++) {
                    if (coordinates.get(j) == coord) {
                        // return overlap flag
                        return 3;
                    }
                }
            }

            for(int i = y1; i <= y2; i++){
                int coord = x1 * 10 + i;

                // add ship to the hashmap for later search for checking if sunk
                System.out.println("put ship with size " + boatsize + "into the team.");
                // store ship body coordinates as integers with following function
                ship.addCoord(coord);
                coordinates.add(coord);
                System.out.println("Adding coordinate " + coord + " for ship " + boatsize);
                // change empty field '+' to ship body 'o'
                map.get(i)[x1] = 'o';
            }

            for(int i = 0; i < ships.length; i++){
                if (ships[i][0] == boatsize) {
                    ships[i][1] = 1;
                }
            }

            shipTeam.put(boatsize, ship);
            shipUnplaced--;

            return 0;
        }
        // if the ship is placed horizontally
        else if (y1 == y2){
            int temp;

            // check if boat of such size is placed
            int boatsize = Math.abs(x2 - x1) + 1;

            if (boatsize != 2 && boatsize != 3 && boatsize != 4){
                System.out.println("You can only place a ship with 2, 3, or 4 holes.");
                return 1;
            }

            if (shipPlaced(boatsize)){
                System.out.println("You have placed a ship with size " + boatsize +
                        " . Please place an unplaced ship.");
                return 2;
            }

            // making sure y1 < y2 for the following for loop
            if(x1 > x2){
                temp = x1;
                x1 = x2;
                x2 = temp;
            }

            Ship ship = new Ship(boatsize);

            // change inside row (in the same char[])
            for(int i = x1; i <= x2; i++){
                // check if new ship overlaps with old ships
                int coord = i * 10 + y1;
                for(int j = 0; j < coordinates.size(); j++){
                    if(coordinates.get(j) == coord){
                        // return overlap flag
                        return 3;
                    }
                }
            }

            for(int i = x1; i <= x2; i++){
                int coord = i * 10 + y1;

                System.out.println("put ship with size " + boatsize + " into the team.");
                // store ship body coordinates as integers with following function
                ship.addCoord(coord);
                coordinates.add(coord);
                System.out.println("Adding coordinate " + (i * 10 + y1) + " for ship " + boatsize);
                // change empty field '+' to ship body 'o'
                map.get(y1)[i] = 'o';
            }

            for(int i = 0; i < ships.length; i++){
                if (ships[i][0] == boatsize) {
                    ships[i][1] = 1;
                }
            }

            // add ship to the hashmap for later search for checking if sunk
            shipTeam.put(boatsize, ship);
            shipUnplaced--;
            return 0;
        }
        // otherwise, it is placed diagonally
        else{
            System.out.println("Please place the ship horizontally or vertically. " +
                    "Diagonal placement is forbidden.");
            return 4;
        }
    }


    // Update the bombed sites for the masked view map; '&' for missed, '/' for hit
    @Override
    public void markBombed(int hit, int x, int y){
        if(hit == -1){
            map.get(y)[x] = '&';
        }
        else if (hit == 100 || hit == -100){
            map.get(y)[x] = '/';
        }
    }

    // Update the bombed sites for the main map; '&' for missed, '/' for hit
    @Override
    public int shipHit(int x, int y){
        // missed by hitting empty site '+'
        if (map.get(y)[x] == '+'){
            // mark the site
            //map.get(y)[x] = '&';
            System.out.println("You missed.");
            return -1;
        }
        // hit ship body 'o'
        else if (map.get(y)[x] == 'o'){
            // '/': a hit hole on the ship
            map.get(y)[x] = '/';

            // search through which ship has being hit
            // and return the found ship's size
            for(int i = 2; i < 5; i++){
                ArrayList<Integer> tempCord = shipTeam.get(i).coordinates;
                for(int j = 0; j < tempCord.size(); j++){
                    if(x * 10 + y == tempCord.get(j)){
                        shipTeam.get(i).hole_hit++;
                        int sunk = shipSunk(i);
                        System.out.println("Ship " + i + " is hit.");
                        return sunk;
                    }
                }
            }
        }
        // missed by hitting hit hole
        else if (map.get(y)[x] == '/'){
            System.out.println("You have hit this place before.");
            return -2;
        }
        return -1;
    }

    // check if the hit ship has sunk by comparing to their hit hole and body size
    @Override
    public int shipSunk(int ship){

        if(shipTeam.get(ship).hole_hit == shipTeam.get(ship).size){
            shipsunk++;
            System.out.println("Your ship with size " + shipTeam.get(ship).size + " has sunk.");
            return 100;
        }

        System.out.println("Ship " + ship + " is still here.");
        return -100;
    }

    @Override
    public boolean gameOver(){
        if(shipsunk == 3){
            System.out.println("All your ships have sunk. Game over.");
            return true;
        } else {
            System.out.println(shipsunk + " ships have sunk.");
            return false;
        }

    }

}
