import UI.NewReservation;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Simulated login — replace with your team's auth module
        String loggedInUser = "gian";
        String userRole     = "FRONT DESK";

        // Launch the reservation menu
        new NewReservation(scanner, loggedInUser, userRole).run();

        scanner.close();
    }
}