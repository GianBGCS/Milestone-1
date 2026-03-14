package Service;

import DAO.ReservationDAO;
import Model.Reservation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * ═══════════════════════════════════════════════════
 * PACKAGE : Service
 * FILE    : ReservationService.java
 * ═══════════════════════════════════════════════════
 *
 * All business logic for the reservation system.
 * Talks to the database only through ReservationDAO.
 *
 * Features:
 *   [1] Create Aircraft Reservation
 *   [2] Validate Slot Availability
 *   [3] Validate Aircraft Size Fit
 *
 * FLOW:
 *   UI → ReservationService → ReservationDAO → SQLite DB
 *
 * HOW TO USE FROM UI:
 *
 *   ReservationService service = new ReservationService();
 *   ServiceResult result = service.createReservation(...);
 *   if (result.isSuccess()) { ... }
 */
public class ReservationService {

    // ── Constants ─────────────────────────────────────────────────────────────
    private static final int NO_EXCLUDE_ID = 0;

    // ── Hangar slot data { slotCode, maxWingspan, maxLength, category } ───────
    private static final String[][] HANGAR_SLOTS = {
            { "A1", "20.0", "15.0", "SMALL"  },
            { "A2", "20.0", "15.0", "SMALL"  },
            { "A3", "20.0", "15.0", "SMALL"  },
            { "B1", "36.0", "30.0", "MEDIUM" },
            { "B2", "36.0", "30.0", "MEDIUM" },
            { "B3", "36.0", "30.0", "MEDIUM" },
            { "C1", "65.0", "55.0", "LARGE"  },
            { "C2", "65.0", "55.0", "LARGE"  }
    };

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final ReservationDAO dao;

    public ReservationService() {
        this.dao = new ReservationDAO();
    }

    // ════════════════════════════════════════════════════════════════════════
    // Feature 1: Create Aircraft Reservation
    // ════════════════════════════════════════════════════════════════════════


    public ServiceResult createReservation(
            String    customerName,
            String    aircraftTailNumber,
            String    hangarSlot,
            double    wingspan,
            double    length,
            LocalDate startDate,
            LocalDate endDate) {

        // Validation 1 — slot exists
        String[] slot = findSlot(hangarSlot);
        if (slot == null) {
            return ServiceResult.failure("Hangar slot '" + hangarSlot + "' does not exist.");
        }

        // Validation 2 — aircraft size fit
        double maxWingspan = Double.parseDouble(slot[1]);
        double maxLength   = Double.parseDouble(slot[2]);
        if (wingspan > maxWingspan || length > maxLength) {
            String message = String.format(
                    "Aircraft does not fit in slot %s.\n" +
                            "  Slot limit    — Wingspan: %.1f m | Length: %.1f m\n" +
                            "  Your aircraft — Wingspan: %.1f m | Length: %.1f m",
                    hangarSlot, maxWingspan, maxLength, wingspan, length
            );
            return ServiceResult.failure(message, findSuitableSlots(wingspan, length, startDate, endDate));
        }

        // Validation 3 — slot availability
        if (dao.hasOverlap(hangarSlot, startDate, endDate, NO_EXCLUDE_ID)) {
            String message = "Slot " + hangarSlot + " is already booked for the selected dates.";
            return ServiceResult.failure(message, findSuitableSlots(wingspan, length, startDate, endDate));
        }

        // Build using Reservation.Builder
        Reservation reservation = new Reservation.Builder()
                .customerName(customerName)
                .aircraftTailNumber(aircraftTailNumber)
                .hangarSlot(hangarSlot)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        // Save to DB via DAO
        Reservation saved = dao.insert(reservation);
        if (saved == null) {
            return ServiceResult.failure("Database error: reservation could not be saved.");
        }

        return ServiceResult.success(saved);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Feature 2: Validate Slot Availability
    // ════════════════════════════════════════════════════════════════════════

    public boolean isSlotAvailable(String hangarSlot, LocalDate start, LocalDate end) {
        return !dao.hasOverlap(hangarSlot, start, end, NO_EXCLUDE_ID);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Feature 3: Validate Aircraft Size Fit
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Returns list of slots where aircraft fits AND is available.
     * Used to suggest alternatives when validation fails.
     */
    public List<String> findSuitableSlots(double wingspan, double length,
                                          LocalDate start, LocalDate end) {
        List<String> suitable = new ArrayList<>();
        for (String[] slot : HANGAR_SLOTS) {
            double  maxWingspan = Double.parseDouble(slot[1]);
            double  maxLength   = Double.parseDouble(slot[2]);
            boolean fits        = wingspan <= maxWingspan && length <= maxLength;
            boolean available   = !dao.hasOverlap(slot[0], start, end, NO_EXCLUDE_ID);
            if (fits && available) {
                suitable.add(String.format(
                        "  Slot %-3s | Category: %-7s | Max Wingspan: %.1f m | Max Length: %.1f m",
                        slot[0], slot[3], maxWingspan, maxLength
                ));
            }
        }
        return suitable;
    }

    // ════════════════════════════════════════════════════════════════════════
    // Read helpers (shared with other UI modules e.g. View, Modify, Cancel)
    // ════════════════════════════════════════════════════════════════════════

    public List<Reservation> getAllReservations()                  { return dao.findAll(); }
    public List<Reservation> getReservationsByCustomer(String n)  { return dao.findByCustomer(n); }
    public List<Reservation> getReservationsByAircraft(String t)  { return dao.findByAircraft(t); }
    public boolean           updateStatus(int id, String status)  { return dao.updateStatus(id, status); }
    public String[][]        getAllHangarSlots()                   { return HANGAR_SLOTS; }

    public boolean isValidSlot(String slotCode) {
        return findSlot(slotCode) != null;
    }

    // ── Private: find a slot by code ──────────────────────────────────────────
    private String[] findSlot(String slotCode) {
        for (String[] slot : HANGAR_SLOTS) {
            if (slot[0].equalsIgnoreCase(slotCode)) return slot;
        }
        return null;
    }

    // ════════════════════════════════════════════════════════════════════════
    // ServiceResult — wraps success / failure back to the UI
    // ════════════════════════════════════════════════════════════════════════

    public static class ServiceResult {

        // ── Constants ─────────────────────────────────────────────────────────
        private static final String MSG_SUCCESS = "Success";

        // ── Fields ────────────────────────────────────────────────────────────
        private final boolean      success;
        private final String       message;
        private final Reservation  data;
        private final List<String> alternatives;

        // ── Private constructor ───────────────────────────────────────────────
        private ServiceResult(boolean success, String message,
                              Reservation data, List<String> alternatives) {
            this.success      = success;
            this.message      = message;
            this.data         = data;
            this.alternatives = alternatives;
        }

        // ── Static factories ──────────────────────────────────────────────────
        public static ServiceResult success(Reservation data) {
            return new ServiceResult(true, MSG_SUCCESS, data, null);
        }

        public static ServiceResult failure(String message) {
            return new ServiceResult(false, message, null, null);
        }

        public static ServiceResult failure(String message, List<String> alternatives) {
            return new ServiceResult(false, message, null, alternatives);
        }

        // ── Getters ───────────────────────────────────────────────────────────
        public boolean      isSuccess()       { return success; }
        public String       getMessage()      { return message; }
        public Reservation  getData()         { return data; }
        public List<String> getAlternatives() { return alternatives; }
        public boolean      hasAlternatives() { return alternatives != null && !alternatives.isEmpty(); }
    }
}