package AccountSystem;

import java.io.*;
import java.util.*;

public class AccountDB {
    private static final String FILE_PATH = "accounts.txt";

    // Save account to file
    public static void saveAccount(AccountSys account) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true)) {
            fw.write(account.getEmail() + "," + account.getPassword() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read all accounts
    public static List<AccountSys> loadAccounts() {
        List<AccountSys> accounts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    accounts.add(new AccountSys(data[0], data[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accounts;
    }
}
