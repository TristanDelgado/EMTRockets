package org.SwEng.accountSystem;

import java.io.*;
import java.util.*;

public class PaymentDB {
    private static final String FILE_PATH = "payments.txt";

    public static void savePayment(PaymentSys payment) {
        try (FileWriter fw = new FileWriter(FILE_PATH, true)) {
            fw.write(payment.getEmail() + "," + payment.getAmount() + "," +
                    payment.getMethod() + "," + payment.getDate() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<PaymentSys> loadPayments() {
        List<PaymentSys> payments = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 4) {
                    payments.add(new PaymentSys(data[0],
                            Double.parseDouble(data[1]),
                            data[2],
                            data[3]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return payments;
    }
}
