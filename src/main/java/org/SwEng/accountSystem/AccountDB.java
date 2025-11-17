package org.SwEng.accountSystem;

import java.io.*;
import java.util.*;

public class AccountDB {
    //TODO: Always change this to your local computer's path.
    private static final String FILE_PATH = "src\\main\\java\\org\\SwEng\\accountSystem\\accounts.txt";

    // Save account to file
    public static void saveAccount(Account account) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true)) {
            fw.write(account.getEmail() + "," + account.getPassword() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read all accounts
    public static List<Account> loadAccounts() {
        List<Account> accounts = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 2) {
                    accounts.add(new Account(data[0], data[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return accounts;
    }
}
