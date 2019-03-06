public interface GameMap {
    // initialize the empty field map
    void fillMap();

    // print out the map
    String printMap(String mapname);

    // check if ship with such size is un-placed fot placingShip() method
    // if so, place it and change the size in ships[] to 0 to show it's placed
    boolean shipPlaced(int size);

    // placing ships by changing '+' to 'o'
    int placingShip(int x1, int y1, int x2, int y2);

    // Update the bombed sites for the masked view map; '&' for missed, '/' for hit
    void markBombed(int hit, int x, int y);

    // Update the bombed sites for the main map; '&' for missed, '/' for hit
    int shipHit(int x, int y);

    // check if the hit ship has sunk by comparing to their hit hole and body size
    int shipSunk(int ship);

    boolean gameOver();
}
