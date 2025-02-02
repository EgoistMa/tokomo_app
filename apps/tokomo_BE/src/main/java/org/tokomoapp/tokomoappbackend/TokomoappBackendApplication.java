package org.tokomoapp.tokomoappbackend;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.tokomoapp.tokomoappbackend.model.VipCode;
import org.tokomoapp.tokomoappbackend.repository.VipCodeRepository;
import org.tokomoapp.tokomoappbackend.model.PaymentCode;
import org.tokomoapp.tokomoappbackend.repository.PaymentCodeRepository;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootApplication
public class TokomoappBackendApplication implements CommandLineRunner {

    @Autowired
    private VipCodeRepository vipCodeRepository;
    
    @Autowired
    private PaymentCodeRepository paymentCodeRepository;

    public static void main(String[] args) {
        SpringApplication.run(TokomoappBackendApplication.class, args);
    }

    private int parseTimeString(String timeStr) {
        int totalDays = 0;
        Pattern pattern = Pattern.compile("(\\d+)([ymwd])");
        Matcher matcher = pattern.matcher(timeStr);
        
        while (matcher.find()) {
            int value = Integer.parseInt(matcher.group(1));
            String unit = matcher.group(2);
            
            switch (unit) {
                case "y" -> totalDays += value * 365;
                case "m" -> totalDays += value * 30;
                case "w" -> totalDays += value * 7;
                case "d" -> totalDays += value;
            }
        }
        
        return totalDays;
    }

    @Override
    public void run(String... args) {
        try {
            Scanner scanner = new Scanner(System.in);
            System.out.println("backend started,listenning from port 8080");
            System.out.print("> ");

            while (true) {
                if (scanner.hasNextLine()) {
                    String command = scanner.nextLine().trim();
                    if (command.startsWith("genvip ")) {
                        String timeStr = command.substring(7);
                        try {
                            int days = parseTimeString(timeStr);
                            if (days > 0) {
                                // 生成VIP码
                                String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                                VipCode vipCode = new VipCode();
                                vipCode.setCode(code);
                                vipCode.setValidDays(days);
                                vipCode.setUsed(false);
                                
                                vipCodeRepository.save(vipCode);
                                System.out.println("Generated VIP code: " + code + " (Valid for " + days + " days)");
                            } else {
                                System.out.println("Invalid time format. Please use combinations of y(years), m(months), w(weeks), d(days)");
                                System.out.println("Example: 1y2m3w4d = 1 year 2 months 3 weeks 4 days");
                            }
                        } catch (Exception e) {
                            System.out.println("Error parsing time format: " + e.getMessage());
                        }
                    } else if (command.startsWith("genpay ")) {
                        try {
                            int points = Integer.parseInt(command.substring(7));
                            if (points > 0) {
                                // 生成充值码
                                String code = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                                PaymentCode paymentCode = new PaymentCode();
                                paymentCode.setCode(code);
                                paymentCode.setPoints(points);
                                paymentCode.setUsed(false);
                                
                                paymentCodeRepository.save(paymentCode);
                                System.out.println("Generated payment code: " + code + " (Points: " + points + ")");
                            } else {
                                System.out.println("Points must be greater than 0");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid points format. Please enter a number");
                        } catch (Exception e) {
                            System.out.println("Error generating payment code: " + e.getMessage());
                        }
                    } else {
                        switch (command.toLowerCase()) {
                            case "reload" -> System.out.println("reloaded!");
                            case "stop" -> {
                                System.out.println("stopping backend...");
                                System.exit(0);
                            }
                            case "help" -> {
                                System.out.println("available commands:");
                                System.out.println("  reload: reload the backend");
                                System.out.println("  stop: stop the backend");
                                System.out.println("  genvip <time>: generate a VIP code valid for specified time");
                                System.out.println("    time format: combination of y(years), m(months), w(weeks), d(days)");
                                System.out.println("    example: genvip 1y2m3w4d = 1 year 2 months 3 weeks 4 days");
                                System.out.println("  genpay <points>: generate a payment code for specified points");
                                System.out.println("    example: genpay 100");
                            }
                            default -> System.out.println("unknown command, type \"help\" for more information");
                        }
                    }
                    System.out.print("> ");
                }
            }
        } catch (Exception e) {
            System.out.println("error: " + e.getMessage());
        }
    }
}