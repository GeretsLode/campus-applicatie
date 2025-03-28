package be.ucll.campus.campus_app.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class UserCliService {
    private final WebClient webClient;

    public UserCliService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public void userMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n1. Bekijk alle gebruikers");
            System.out.println("2. Gebruiker toevoegen");
            System.out.println("3. Gebruiker verwijderen");
            System.out.println("4. Zoek gebruikers op naam");
            System.out.println("5. Terug naar hoofdmenu");
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
                case 1 -> getUsers();
                case 2 -> addUser(scanner);
                case 3 -> deleteUser(scanner);
                case 4 -> searchUsers(scanner);
                case 5 -> { return; }
                default -> System.out.println("Ongeldige keuze.");
            }
        }
    }

    private void getUsers() {
        try {
            KlantDTO[] gebruikers = webClient.get()
                    .uri("/users")
                    .retrieve()
                    .bodyToMono(KlantDTO[].class)
                    .block();

            if (gebruikers == null || gebruikers.length == 0) {
                System.out.println("Geen gebruikers gevonden.");
            } else {
                int teller = 1;
                for (KlantDTO g : gebruikers) {
                    System.out.printf("%d. %s %s (ID: %d, %s, geb. %s)%n",
                            teller++, g.voornaam, g.naam, g.id, g.email, g.geboortedatum);
                }
            }
        } catch (WebClientResponseException e) {
            System.out.println("Fout bij het ophalen van gebruikers: " + e.getMessage());
        }
    }

    private void addUser(Scanner scanner) {
        try {
            // Invoer
            System.out.print("Voornaam: ");
            String voornaam = scanner.nextLine().trim();
            System.out.print("Naam: ");
            String naam = scanner.nextLine().trim();
            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            if (voornaam.isEmpty() || naam.isEmpty() || email.isEmpty()) {
                System.out.println("Voornaam, naam en e-mail mogen niet leeg zijn.");
                return;
            }

            if (!email.contains("@")) {
                System.out.println("Ongeldig e-mailadres.");
                return;
            }

            System.out.print("Geboortedatum (YYYY-MM-DD): ");
            LocalDate geboortedatum = null;
            while (geboortedatum == null) {
                try {
                    geboortedatum = LocalDate.parse(scanner.nextLine().trim());
                } catch (DateTimeParseException e) {
                    System.out.print("Ongeldig formaat, gebruik YYYY-MM-DD: ");
                }
            }

            // Bestaat gebruiker al?
            KlantDTO[] bestaandeGebruikers = webClient.get()
                    .uri("/users")
                    .retrieve()
                    .bodyToMono(KlantDTO[].class)
                    .block();

            boolean bestaat = Arrays.stream(bestaandeGebruikers)
                    .anyMatch(g -> g.voornaam.equalsIgnoreCase(voornaam) && g.naam.equalsIgnoreCase(naam));

            if (bestaat) {
                System.out.println("Deze gebruiker bestaat al.");
                return;
            }

            // Versturen als KlantRequestDTO
            KlantRequestDTO nieuweGebruiker = new KlantRequestDTO();
            nieuweGebruiker.voornaam = voornaam;
            nieuweGebruiker.naam = naam;
            nieuweGebruiker.email = email;
            nieuweGebruiker.geboortedatum = geboortedatum;

            KlantDTO response = webClient.post()
                    .uri("/users")
                    .bodyValue(nieuweGebruiker)
                    .retrieve()
                    .bodyToMono(KlantDTO.class)
                    .block();

            if (response != null) {
                System.out.println("Gebruiker toegevoegd: " + response.voornaam + " " + response.naam);
            } else {
                System.out.println("Toevoegen mislukt.");
            }

        } catch (WebClientResponseException e) {
            System.out.println("Fout bij toevoegen van gebruiker: " + e.getMessage());
        }
    }

    private void deleteUser(Scanner scanner) {
        try {
            KlantDTO[] gebruikers = webClient.get()
                    .uri("/users")
                    .retrieve()
                    .bodyToMono(KlantDTO[].class)
                    .block();

            if (gebruikers == null || gebruikers.length == 0) {
                System.out.println("Geen gebruikers beschikbaar.");
                return;
            }

            System.out.println("Beschikbare gebruikers:");
            for (KlantDTO g : gebruikers) {
                System.out.printf("- ID %d: %s %s%n", g.id, g.voornaam, g.naam);
            }

            System.out.print("Voer het ID in van de gebruiker die je wilt verwijderen: ");
            String input = scanner.nextLine().trim();
            long id;
            try {
                id = Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Ongeldig ID-formaat. Geef een numeriek ID op.");
                return;
            }

            boolean geldig = Arrays.stream(gebruikers).anyMatch(g -> g.id.equals(id));
            if (!geldig) {
                System.out.println("Ongeldig ID gekozen.");
                return;
            }

            webClient.delete()
                    .uri("/users/" + id)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            System.out.println("Gebruiker verwijderd.");
        } catch (WebClientResponseException e) {
            System.out.println("Fout bij verwijderen van gebruiker: " + e.getMessage());
        }
    }

    private void searchUsers(Scanner scanner) {
        System.out.print("Voer (deel van) de naam in om te zoeken: ");
        String zoekterm = scanner.nextLine().trim();

        try {
            KlantDTO[] gebruikers = webClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/users")
                            .queryParam("search", zoekterm)
                            .build())
                    .retrieve()
                    .bodyToMono(KlantDTO[].class)
                    .block();

            if (gebruikers == null || gebruikers.length == 0) {
                System.out.println("Geen gebruikers gevonden.");
            } else {
                int teller = 1;
                for (KlantDTO g : gebruikers) {
                    System.out.printf("%d. %s %s (ID: %d, %s, geb. %s)%n",
                            teller++, g.voornaam, g.naam, g.id, g.email, g.geboortedatum);
                }
            }
        } catch (WebClientResponseException e) {
            System.out.println("Fout bij zoeken naar gebruikers: " + e.getMessage());
        }
    }

    private static class KlantDTO {
        public Long id;
        public String naam;
        public String voornaam;
        public String email;
        public LocalDate geboortedatum;
    }

    private static class KlantRequestDTO {
        public String naam;
        public String voornaam;
        public String email;
        public LocalDate geboortedatum;
    }
}
