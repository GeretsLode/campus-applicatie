package be.ucll.campus.campus_app.service;

import org.springframework.stereotype.Service;
import java.util.Scanner;

@Service
public class CliService {
    private final CampusCliService campusCliService;
    private final LokaalCliService lokaalCliService;
    private final UserCliService userCliService;
    private final ReservatieCliService reservatieCliService;

    public CliService(CampusCliService campusCliService, LokaalCliService lokaalCliService,
                      UserCliService userCliService, ReservatieCliService reservatieCliService) {
        this.campusCliService = campusCliService;
        this.lokaalCliService = lokaalCliService;
        this.userCliService = userCliService;
        this.reservatieCliService = reservatieCliService;
    }

    public void start() {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n--- Campus Beheer CLI ---");
            System.out.println("1. Campusbeheer");
            System.out.println("2. Lokaalbeheer");
            System.out.println("3. Gebruikersbeheer");
            System.out.println("4. Reservatiebeheer");
            System.out.println("5. Afsluiten");
            System.out.print("Kies een optie: ");

            String input = scanner.nextLine().trim();
            int keuze;

            try {
                keuze = Integer.parseInt(input);
                if (keuze < 1 || keuze > 5) {
                    System.out.println("Ongeldige keuze. Kies een getal tussen 1 en 5.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ongeldige invoer. Gelieve een getal in te geven.");
                continue;
            }

            switch (keuze) {
                case 1 -> campusCliService.campusMenu(scanner);
                case 2 -> lokaalCliService.lokaalMenu(scanner);
                case 3 -> userCliService.userMenu(scanner);
                case 4 -> reservatieCliService.reservatieMenu(scanner);
                case 5 -> {
                    System.out.println("Programma afgesloten.");
                    return;
                }
                default -> System.out.println("Ongeldige keuze, probeer opnieuw.");
            }
        }
    }
}
