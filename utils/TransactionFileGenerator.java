import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * Generates a sample transaction file for testing the LoyaltyPointsProcessor
 */
public class TransactionFileGenerator {
    
    private static final String[] MERCHANT_IDS = {
        "GAS123",  // Our gas station chain's merchant ID
        "SHOP456", // Some other merchant
        "REST789", // Restaurant
        "GROC012", // Grocery store
        "CLTH345"  // Clothing store
    };
    
    private static final String[] LOYALTY_MEMBER_CARDS = {
        "1234567890123456", // John Doe
        "2345678901234567", // Jane Smith
        "3456789012345678"  // Bob Johnson
    };
    
    private static final Random RANDOM = new Random();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java TransactionFileGenerator <output_file_path> <num_transactions>");
            return;
        }
        
        String outputFilePath = args[0];
        int numTransactions = Integer.parseInt(args[1]);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(outputFilePath))) {
            LocalDate yesterday = LocalDate.now().minusDays(1);
            String date = yesterday.format(DATE_FORMATTER);
            
            for (int i = 0; i < numTransactions; i++) {
                String merchantId = MERCHANT_IDS[RANDOM.nextInt(MERCHANT_IDS.length)];
                String creditCard = generateCreditCard(i);
                double amount = 10 + RANDOM.nextDouble() * 90; // Random amount between $10 and $100
                
                // Format: Date,CreditCardNumber,MerchantID,Amount
                writer.println(date + "," + creditCard + "," + merchantId + "," + String.format("%.2f", amount));
            }
            
            System.out.println("Generated " + numTransactions + " transactions to " + outputFilePath);
        } catch (IOException e) {
            System.err.println("Error generating file: " + e.getMessage());
        }
    }
    
    /**
     * Generate a credit card number - either one of our loyalty members (10% chance)
     * or a random one (90% chance)
     */
    private static String generateCreditCard(int index) {
        // 10% chance to use a loyalty member card
        if (RANDOM.nextInt(10) == 0 && LOYALTY_MEMBER_CARDS.length > 0) {
            return LOYALTY_MEMBER_CARDS[RANDOM.nextInt(LOYALTY_MEMBER_CARDS.length)];
        }
        
        // Otherwise generate a random 16-digit number
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}