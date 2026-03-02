import config.DatabaseInitializer;

public class Main {

    public static void main(String[] args) {
        DatabaseInitializer.init();
        System.out.println("Backend started successfully");
    }

}

