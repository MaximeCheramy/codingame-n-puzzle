import com.codingame.gameengine.runner.SoloGameRunner;

public class Main {
    public static void main(String[] args) {
        
        SoloGameRunner gameRunner = new SoloGameRunner();
        gameRunner.setTestCase("test14.json");
        gameRunner.setAgent("python3 /home/max/programmation/taquin/taquin.py");

        // gameRunner.addAgent("python3 /home/user/player.py");
        
        gameRunner.start();
    }
}
