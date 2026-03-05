import java.util.*;

class ParkingSpot {
    String licensePlate;
    long entryTime;
    boolean occupied;

    ParkingSpot() {
        this.licensePlate = null;
        this.entryTime = 0;
        this.occupied = false;
    }
}

public class ParkingLot {
    private ParkingSpot[] spots;
    private int capacity;
    private int occupiedCount;
    private List<Long> durations; // for statistics
    private int totalProbes;

    public ParkingLot(int capacity) {
        this.capacity = capacity;
        this.spots = new ParkingSpot[capacity];
        for (int i = 0; i < capacity; i++) {
            spots[i] = new ParkingSpot();
        }
        this.occupiedCount = 0;
        this.durations = new ArrayList<>();
        this.totalProbes = 0;
    }

    // Hash function: licensePlate → preferred spot
    private int hash(String licensePlate) {
        return Math.abs(licensePlate.hashCode()) % capacity;
    }

    // Park vehicle with linear probing
    public String parkVehicle(String licensePlate) {
        int index = hash(licensePlate);
        int probes = 0;

        while (spots[index].occupied) {
            probes++;
            index = (index + 1) % capacity;
        }

        spots[index].licensePlate = licensePlate;
        spots[index].entryTime = System.currentTimeMillis();
        spots[index].occupied = true;
        occupiedCount++;
        totalProbes += probes;

        return "Assigned spot #" + index + " (" + probes + " probes)";
    }

    // Exit vehicle and calculate fee
    public String exitVehicle(String licensePlate) {
        int index = hash(licensePlate);

        while (spots[index].occupied && !spots[index].licensePlate.equals(licensePlate)) {
            index = (index + 1) % capacity;
        }

        if (!spots[index].occupied) {
            return "Vehicle not found!";
        }

        long durationMs = System.currentTimeMillis() - spots[index].entryTime;
        long durationMinutes = durationMs / (1000 * 60);
        double fee = durationMinutes * 0.1; // $0.10 per minute

        durations.add(durationMinutes);
        spots[index].occupied = false;
        spots[index].licensePlate = null;
        occupiedCount--;

        return "Spot #" + index + " freed, Duration: " + durationMinutes + " min, Fee: $" + String.format("%.2f", fee);
    }

    // Statistics
    public String getStatistics() {
        double occupancyRate = (occupiedCount * 100.0) / capacity;
        double avgProbes = occupiedCount == 0 ? 0 : (double) totalProbes / occupiedCount;
        long peakHour = (System.currentTimeMillis() / (1000 * 60 * 60)) % 24; // simplistic peak hour

        return "Occupancy: " + String.format("%.1f", occupancyRate) + "%, Avg Probes: " +
                String.format("%.2f", avgProbes) + ", Peak Hour: " + peakHour + ":00";
    }

    // Demo
    public static void main(String[] args) throws InterruptedException {
        ParkingLot lot = new ParkingLot(500);

        System.out.println(lot.parkVehicle("ABC-1234"));
        System.out.println(lot.parkVehicle("ABC-1235"));
        System.out.println(lot.parkVehicle("XYZ-9999"));

        Thread.sleep(2000); // simulate parking duration

        System.out.println(lot.exitVehicle("ABC-1234"));
        System.out.println(lot.getStatistics());
    }
}
