import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Gas Station Loyalty Points Processor
 * 
 * This program processes credit card transaction files to award loyalty points
 * to gas station chain customers. It can resume processing from where it left off
 * if the program is interrupted.
 */
public class LoyaltyPointsProcessor {
    
    // Configuration constants
    private static final String CHECKPOINT_FILE = "checkpoint.txt";
    private static final String OUR_MERCHANT_ID = "GAS123"; // Assuming this identifies our gas station chain
    private static final double POINTS_PER_DOLLAR = 1.0;    // 1 point per dollar spent
    
    // In-memory database of loyalty members (in a real system, this would be a database)
    private static Map<String, Customer> loyaltyMembers = new HashMap<>();
    
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java LoyaltyPointsProcessor <transaction_file_path>");
            return;
        }
        
        String transactionFilePath = args[0];
        
        // Load loyalty member data (in a real system, this would be from a database)
        loadLoyaltyMemberData();
        
        // Process the transaction file
        try {
            processTransactionFile(transactionFilePath);
            System.out.println("Processing completed successfully!");
            
            // Display updated points (for demo purposes)
            displayUpdatedPoints();
            
            // In a real system, we would save the updated points to a database
            saveLoyaltyMemberData();
            
            // Clear the checkpoint after successful completion
            Files.deleteIfExists(Paths.get(CHECKPOINT_FILE));
            
        } catch (IOException e) {
            System.err.println("Error processing file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Process the transaction file, with resume capability
     */
    private static void processTransactionFile(String filePath) throws IOException {
        long startLine = readCheckpoint();
        long currentLine = 0;
        
        System.out.println("Starting processing from line: " + startLine);
        
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            
            // Skip to the last processed line
            while (currentLine < startLine && (line = reader.readLine()) != null) {
                currentLine++;
            }
            
            // Process remaining lines
            while ((line = reader.readLine()) != null) {
                currentLine++;
                
                // Process the current transaction line
                processTransaction(line);
                
                // Update checkpoint periodically (e.g., every 1000 lines)
                if (currentLine % 1000 == 0) {
                    writeCheckpoint(currentLine);
                    System.out.println("Processed " + currentLine + " lines...");
                }
            }
            
            // Final checkpoint update
            writeCheckpoint(currentLine);
        }
    }
    
    /**
     * Process a single transaction line
     */
    private static void processTransaction(String transactionLine) {
        // Parse the transaction line
        // Format assumed: Date,CreditCardNumber,MerchantID,Amount
        String[] fields = transactionLine.split(",");
        if (fields.length < 4) {
            return; // Skip malformed lines
        }
        
        String date = fields[0];
        String creditCardNumber = fields[1];
        String merchantId = fields[2];
        double amount = Double.parseDouble(fields[3]);
        
        // Check if this transaction is from our gas stations
        if (!merchantId.equals(OUR_MERCHANT_ID)) {
            return; // Skip transactions not from our chain
        }
        
        // Check if the credit card belongs to a loyalty member
        if (loyaltyMembers.containsKey(creditCardNumber)) {
            // Award points
            Customer customer = loyaltyMembers.get(creditCardNumber);
            int pointsToAward = (int) (amount * POINTS_PER_DOLLAR);
            customer.addPoints(pointsToAward);
            
            System.out.println("Awarded " + pointsToAward + " points to customer " + 
                              customer.getName() + " for transaction on " + date);
        }
    }
    
    /**
     * Read the checkpoint to determine where to resume processing
     */
    private static long readCheckpoint() throws IOException {
        Path checkpointPath = Paths.get(CHECKPOINT_FILE);
        if (Files.exists(checkpointPath)) {
            List<String> lines = Files.readAllLines(checkpointPath);
            if (!lines.isEmpty()) {
                return Long.parseLong(lines.get(0));
            }
        }
        return 0; // Start from the beginning if no checkpoint
    }
    
    /**
     * Write the current line number to the checkpoint file
     */
    private static void writeCheckpoint(long lineNumber) throws IOException {
        Files.write(Paths.get(CHECKPOINT_FILE), 
                   String.valueOf(lineNumber).getBytes(),
                   StandardOpenOption.CREATE,
                   StandardOpenOption.TRUNCATE_EXISTING);
    }
    
    /**
     * Load the loyalty member data
     * In a real system, this would come from a database
     */
    private static void loadLoyaltyMemberData() {
        // Sample data for demonstration
        loyaltyMembers.put("1234567890123456", new Customer("John Doe", "1234567890123456", 100));
        loyaltyMembers.put("2345678901234567", new Customer("Jane Smith", "2345678901234567", 250));
        loyaltyMembers.put("3456789012345678", new Customer("Bob Johnson", "3456789012345678", 50));
    }
    
    /**
     * Save the loyalty member data
     * In a real system, this would update a database
     */
    private static void saveLoyaltyMemberData() {
        // In a real implementation, this would update a database
        System.out.println("Loyalty member data saved to database.");
    }
    
    /**
     * Display updated points (for demo purposes)
     */
    private static void displayUpdatedPoints() {
        System.out.println("\nUpdated Loyalty Points:");
        System.out.println("------------------------");
        for (Customer customer : loyaltyMembers.values()) {
            System.out.println(customer.getName() + ": " + customer.getPoints() + " points");
        }
        System.out.println();
    }
    
    /**
     * Customer class to represent loyalty program members
     */
    static class Customer {
        private String name;
        private String creditCardNumber;
        private int points;
        
        public Customer(String name, String creditCardNumber, int points) {
            this.name = name;
            this.creditCardNumber = creditCardNumber;
            this.points = points;
        }
        
        public String getName() {
            return name;
        }
        
        public String getCreditCardNumber() {
            return creditCardNumber;
        }
        
        public int getPoints() {
            return points;
        }
        
        public void addPoints(int pointsToAdd) {
            this.points += pointsToAdd;
        }
    }
}