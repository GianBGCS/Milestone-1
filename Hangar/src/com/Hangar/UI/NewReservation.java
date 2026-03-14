package UI;

import Model.Reservation;
import Service.ReservationService;
import Service.ReservationService.ServiceResult;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class NewReservation {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final String DIVIDER = "================================================================";
    private static final String CANCEL  = "0";

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final Scanner            scanner;
    private final String             loggedInUser;
    private final String             userRole;
    private final ReservationService service;

    public NewReservation(Scanner scanner, String loggedInUser, String userRole) {
        this.scanner      = scanner;
        this.loggedInUser = loggedInUser;
        this.userRole     = userRole;
        this.service      = new ReservationService();
    }

    // ── Entry point ───────────────────────────────────────────────────────────

    public void run() {
        printHeader();
        System.out.println("  NEW RESERVATION");
        System.out.println();

        // Step 1: Customer name
        String customerName = promptString("  Enter customer name        : ");
        if (customerName == null) { cancelled(); return; }

        // Step 2: Aircraft tail number
        String tailNumber = promptString("  Enter aircraft tail number : ");
        if (tailNumber == null) { cancelled(); return; }
        tailNumber = tailNumber.toUpperCase();

        // Step 3: Aircraft dimensions
        System.out.println();
        System.out.println("  Enter aircraft dimensions for hangar size validation:");
        Double wingspan = promptPositiveDouble("  Aircraft wingspan (meters) : ");
        if (wingspan == null) { cancelled(); return; }

        Double length = promptPositiveDouble("  Aircraft length  (meters) : ");
        if (length == null) { cancelled(); return; }

        // Step 4: Show slot table and pick a slot
        printSlotTable();
        String hangarSlot = promptHangarSlot();
        if (hangarSlot == null) { cancelled(); return; }

        // Step 5: Dates
        LocalDate startDate = promptDate("  Enter start date (yyyy-MM-dd): ");
        if (startDate == null) { cancelled(); return; }

        LocalDate endDate = promptEndDate(startDate);
        if (endDate == null) { cancelled(); return; }

        // Step 6: Confirm details
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("  CONFIRM RESERVATION DETAILS");
        System.out.println(DIVIDER);
        System.out.printf("  Customer Name    : %s%n",              customerName);
        System.out.printf("  Aircraft Tail No : %s%n",              tailNumber);
        System.out.printf("  Wingspan / Length: %.1f m / %.1f m%n", wingspan, length);
        System.out.printf("  Hangar Slot      : %s%n",              hangarSlot);
        System.out.printf("  Start Date       : %s%n",              startDate.format(Reservation.DATE_FORMAT));
        System.out.printf("  End Date         : %s%n",              endDate.format(Reservation.DATE_FORMAT));
        System.out.println(DIVIDER);
        System.out.print("  Confirm? [Y/N]: ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("Y")) {
            cancelled();
            return;
        }

        // Step 7: Call service — handles validations + DB save
        ServiceResult result = service.createReservation(
                customerName, tailNumber, hangarSlot,
                wingspan, length, startDate, endDate);

        // Step 8: Show result
        System.out.println();
        System.out.println(DIVIDER);
        if (result.isSuccess()) {
            System.out.println("  [SUCCESS] Reservation created successfully!");
            System.out.println(DIVIDER);
            System.out.println(result.getData());
        } else {
            System.out.println("  [!] ERROR: " + result.getMessage());
            if (result.hasAlternatives()) {
                System.out.println();
                System.out.println("  Suggested available slots for your aircraft:");
                System.out.println();
                for (String alt : result.getAlternatives()) {
                    System.out.println(alt);
                }
            }
        }
        System.out.println(DIVIDER);
        promptEnterToContinue();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void printHeader() {
        System.out.println(DIVIDER);
        System.out.println("      AVIATION HANGAR RESERVATION AND FRONT DESK SYSTEM");
        System.out.println(DIVIDER);
        System.out.printf("  Logged in as: %-20s Role: %s%n", loggedInUser, userRole);
        System.out.println(DIVIDER);
        System.out.println();
    }

    private void printSlotTable() {
        System.out.println();
        System.out.println(DIVIDER);
        System.out.println("  HANGAR SLOTS — SIZE LIMITS");
        System.out.println(DIVIDER);
        for (String[] slot : service.getAllHangarSlots()) {
            System.out.printf(
                    "  Slot %-3s | Category: %-7s | Max Wingspan: %5s m | Max Length: %5s m%n",
                    slot[0], slot[3], slot[1], slot[2]
            );
        }
        System.out.println(DIVIDER);
        System.out.println();
    }

    private String promptString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals(CANCEL)) return null;
            if (!input.isEmpty()) return input;
            System.out.println("  [!] Input cannot be empty. Enter 0 to cancel.");
        }
    }

    private Double promptPositiveDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals(CANCEL)) return null;
            try {
                double val = Double.parseDouble(input);
                if (val > 0) return val;
                System.out.println("  [!] Value must be greater than 0.");
            } catch (NumberFormatException e) {
                System.out.println("  [!] Invalid number. Enter a value like 12.5 or 30 (0 to cancel).");
            }
        }
    }

    private String promptHangarSlot() {
        while (true) {
            System.out.print("  Enter hangar slot (or 0 to cancel): ");
            String input = scanner.nextLine().trim().toUpperCase();
            if (input.equals(CANCEL)) return null;
            if (service.isValidSlot(input)) return input;
            System.out.println("  [!] Invalid slot. Choose from the table above.");
        }
    }

    private LocalDate promptDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equals(CANCEL)) return null;
            try {
                return LocalDate.parse(input, Reservation.DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("  [!] Invalid format. Use yyyy-MM-dd (e.g. 2025-06-15).");
            }
        }
    }

    private LocalDate promptEndDate(LocalDate startDate) {
        while (true) {
            LocalDate end = promptDate("  Enter end date   (yyyy-MM-dd): ");
            if (end == null) return null;
            if (!end.isBefore(startDate)) return end;
            System.out.println("  [!] End date must be on or after start date ("
                    + startDate.format(Reservation.DATE_FORMAT) + ").");
        }
    }

    private void cancelled() {
        System.out.println();
        System.out.println("  Reservation cancelled. Returning to menu...");
        System.out.println();
    }

    private void promptEnterToContinue() {
        System.out.print("  Press Enter to return to menu...");
        scanner.nextLine();
        System.out.println();
    }
}