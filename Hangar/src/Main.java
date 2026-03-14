import UI.NewReservation;
import java.util.Scanner;

public class Main {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String DIVIDER = "================================================================";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Simulated login — replace with your team's auth module
        String loggedInUser = "gian";
        String userRole     = "FRONT DESK";

        // ── Main menu loop ────────────────────────────────────────────────────
        boolean running = true;
        while (running) {
            printMenu(loggedInUser, userRole);
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    new NewReservation(scanner, loggedInUser, userRole).run();
                    break;
                case "2":
                    // TODO: teammate — wire to ModifyReservation.java
                    System.out.println("\n  [Modify Reservation — not yet implemented]\n");
                    break;
                case "3":
                    // TODO: teammate — wire to CancelReservation.java
                    System.out.println("\n  [Cancel Reservation — not yet implemented]\n");
                    break;
                case "4":
                    // TODO: teammate — wire to ViewReservationsByCustomer.java
                    System.out.println("\n  [View by Customer — not yet implemented]\n");
                    break;
                case "5":
                    // TODO: teammate — wire to ViewReservationsByAircraft.java
                    System.out.println("\n  [View by Aircraft — not yet implemented]\n");
                    break;
                case "0":
                    System.out.println("\n  Logging out...\n");
                    running = false;
                    break;
                default:
                    System.out.println("\n  [!] Invalid choice. Please enter 0-5.\n");
            }
        }

        scanner.close();
    }

    // ── Print the main menu ───────────────────────────────────────────────────
    private static void printMenu(String loggedInUser, String userRole) {
        System.out.println(DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(DIVIDER);
        System.out.println();
        System.out.println("RESERVATION MANAGEMENT");
        System.out.println();
        System.out.println("[1] New Reservation");
        System.out.println("[2] Modify Reservation");
        System.out.println("[3] Cancel Reservation");
        System.out.println("[4] View Reservations by Customer");
        System.out.println("[5] View Reservations by Aircraft");
        System.out.println();
        System.out.println("[0] Logout");
        System.out.println();
        System.out.println(DIVIDER);
    }
}