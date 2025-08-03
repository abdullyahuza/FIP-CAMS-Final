import ui.ConsoleUI;
import ui.ThriftAssociationGUI;
import service.AssociationService;
import javax.swing.SwingUtilities;

public class ThriftAssociationApp {
    public static void main(String[] args) {
        try {
            System.out.println("Enhanced Thrift Association Management System");
            System.out.println("============================================");
            
            // Check for GUI mode argument
            boolean guiMode = args.length > 0 && "--gui".equals(args[0]);
            
            if (guiMode) {
                System.out.println("Starting in GUI mode...");
                SwingUtilities.invokeLater(() -> {
                    
                    
                    // Create a temporary service for authentication
                    AssociationService tempService = new AssociationService();
                    
                    // Show login dialog
                    String username = javax.swing.JOptionPane.showInputDialog(
                        null, "Enter username:", "Login Required", 
                        javax.swing.JOptionPane.QUESTION_MESSAGE);
                    
                    if (username != null) {
                        javax.swing.JPasswordField passwordField = new javax.swing.JPasswordField();
                        int option = javax.swing.JOptionPane.showConfirmDialog(
                            null, passwordField, "Enter password:", 
                            javax.swing.JOptionPane.OK_CANCEL_OPTION, 
                            javax.swing.JOptionPane.QUESTION_MESSAGE);
                        
                        if (option == javax.swing.JOptionPane.OK_OPTION) {
                            String password = new String(passwordField.getPassword());
                            
                            if (tempService.authenticateUser(username, password)) {
                                new ThriftAssociationGUI(tempService).setVisible(true);
                            } else {
                                javax.swing.JOptionPane.showMessageDialog(
                                    null, "Invalid credentials", "Login Failed", 
                                    javax.swing.JOptionPane.ERROR_MESSAGE);
                                System.exit(1);
                            }
                        } else {
                            System.exit(0);
                        }
                    } else {
                        System.exit(0);
                    }
                });
            } else {
                System.out.println("Starting in console mode...");
                System.out.println("Tip: Use '--gui' argument to start in GUI mode");
                System.out.println();
                
                ConsoleUI ui = new ConsoleUI();
                ui.start();
            }
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}