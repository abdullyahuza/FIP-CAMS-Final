package persistence;

import model.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class DataPersistence {
    private static final String DATA_DIR = "data";
    private static final String MEMBERS_FILE = DATA_DIR + "/members.dat";
    private static final String TRANSACTIONS_FILE = DATA_DIR + "/transactions.dat";
    private static final String LOANS_FILE = DATA_DIR + "/loans.dat";
    private static final String USERS_FILE = DATA_DIR + "/users.dat";

    public DataPersistence() {
        createDataDirectory();
    }

    private void createDataDirectory() {
        File dataDir = new File(DATA_DIR);
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }
    }

    public void saveMembers(List<Member> members) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(MEMBERS_FILE))) {
            oos.writeObject(members);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Member> loadMembers() throws IOException, ClassNotFoundException {
        File file = new File(MEMBERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Member>) ois.readObject();
        }
    }

    public void saveTransactions(List<Transaction> transactions) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(TRANSACTIONS_FILE))) {
            oos.writeObject(transactions);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Transaction> loadTransactions() throws IOException, ClassNotFoundException {
        File file = new File(TRANSACTIONS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Transaction>) ois.readObject();
        }
    }

    public void saveLoans(List<Loan> loans) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(LOANS_FILE))) {
            oos.writeObject(loans);
        }
    }

    @SuppressWarnings("unchecked")
    public List<Loan> loadLoans() throws IOException, ClassNotFoundException {
        File file = new File(LOANS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Loan>) ois.readObject();
        }
    }

    public void saveUsers(List<User> users) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
        }
    }

    @SuppressWarnings("unchecked")
    public List<User> loadUsers() throws IOException, ClassNotFoundException {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<User>) ois.readObject();
        }
    }

    // Backup functionality
    public void createBackup() throws IOException {
        String backupDir = DATA_DIR + "/backup_" + java.time.LocalDateTime.now().toString().replace(":", "-");
        File backup = new File(backupDir);
        backup.mkdirs();

        // Copy all data files to backup directory
        copyFile(MEMBERS_FILE, backupDir + "/members.dat");
        copyFile(TRANSACTIONS_FILE, backupDir + "/transactions.dat");
        copyFile(LOANS_FILE, backupDir + "/loans.dat");
        copyFile(USERS_FILE, backupDir + "/users.dat");
    }

    private void copyFile(String source, String destination) throws IOException {
        File sourceFile = new File(source);
        if (!sourceFile.exists()) return;

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(destination)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
}