package game.engine;

import game.engine.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger.verbose(); // Set to verbose for development
        GameLoop game = new GameLoop();
        game.run();
    }
}