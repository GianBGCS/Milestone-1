package Model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reservation {

    // ── Constants ─────────────────────────────────────────────────────────────
    public static final DateTimeFormatter DATE_FORMAT      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final String            STATUS_ACTIVE    = "ACTIVE";
    public static final String            STATUS_CANCELLED = "CANCELLED";

    // ── Fields ────────────────────────────────────────────────────────────────
    private int       reservationId;
    private String    customerName;
    private String    aircraftTailNumber;
    private String    hangarSlot;
    private LocalDate startDate;
    private LocalDate endDate;
    private String    status;

    // ── Private constructor — only Builder can call this ──────────────────────
    private Reservation() {}

    // ── Getters ───────────────────────────────────────────────────────────────
    public int       getReservationId()      { return reservationId; }
    public String    getCustomerName()       { return customerName; }
    public String    getAircraftTailNumber() { return aircraftTailNumber; }
    public String    getHangarSlot()         { return hangarSlot; }
    public LocalDate getStartDate()          { return startDate; }
    public LocalDate getEndDate()            { return endDate; }
    public String    getStatus()             { return status; }

    // ── Setters ───────────────────────────────────────────────────────────────
    public void setReservationId(int v)         { this.reservationId = v; }
    public void setCustomerName(String v)       { this.customerName = v; }
    public void setAircraftTailNumber(String v) { this.aircraftTailNumber = v; }
    public void setHangarSlot(String v)         { this.hangarSlot = v; }
    public void setStartDate(LocalDate v)       { this.startDate = v; }
    public void setEndDate(LocalDate v)         { this.endDate = v; }
    public void setStatus(String v)             { this.status = v; }

    @Override
    public String toString() {
        return String.format(
                "  ID: %-6d | Customer: %-20s | Aircraft: %-10s | Slot: %-4s | %s to %s | [%s]",
                reservationId, customerName, aircraftTailNumber, hangarSlot,
                startDate.format(DATE_FORMAT), endDate.format(DATE_FORMAT), status
        );
    }

    // ════════════════════════════════════════════════════════════════════════
    // BUILDER
    // ════════════════════════════════════════════════════════════════════════

    public static class Builder {

        // ── Fields ────────────────────────────────────────────────────────────
        private int       reservationId = 0;
        private String    customerName;
        private String    aircraftTailNumber;
        private String    hangarSlot;
        private LocalDate startDate;
        private LocalDate endDate;
        private String    status        = STATUS_ACTIVE;

        // ── Setters ───────────────────────────────────────────────────────────
        public Builder reservationId(int val)         { this.reservationId = val;      return this; }
        public Builder customerName(String val)       { this.customerName = val;       return this; }
        public Builder aircraftTailNumber(String val) { this.aircraftTailNumber = val; return this; }
        public Builder hangarSlot(String val)         { this.hangarSlot = val;         return this; }
        public Builder startDate(LocalDate val)       { this.startDate = val;          return this; }
        public Builder endDate(LocalDate val)         { this.endDate = val;            return this; }
        public Builder status(String val)             { this.status = val;             return this; }

        // ── Build ─────────────────────────────────────────────────────────────
        public Reservation build() {
            if (customerName == null || customerName.isBlank())
                throw new IllegalStateException("Reservation requires a customer name.");
            if (aircraftTailNumber == null || aircraftTailNumber.isBlank())
                throw new IllegalStateException("Reservation requires an aircraft tail number.");
            if (hangarSlot == null || hangarSlot.isBlank())
                throw new IllegalStateException("Reservation requires a hangar slot.");
            if (startDate == null)
                throw new IllegalStateException("Reservation requires a start date.");
            if (endDate == null)
                throw new IllegalStateException("Reservation requires an end date.");
            if (endDate.isBefore(startDate))
                throw new IllegalStateException("End date cannot be before start date.");

            Reservation r        = new Reservation();
            r.reservationId      = this.reservationId;
            r.customerName       = this.customerName;
            r.aircraftTailNumber = this.aircraftTailNumber;
            r.hangarSlot         = this.hangarSlot;
            r.startDate          = this.startDate;
            r.endDate            = this.endDate;
            r.status             = this.status;
            return r;
        }
    }
}