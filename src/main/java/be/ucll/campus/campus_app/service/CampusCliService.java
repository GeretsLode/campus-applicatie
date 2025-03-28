package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.model.Campus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;
import java.util.Scanner;

@Service
public class CampusCliService {
    private final WebClient webClient;

    public CampusCliService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public void campusMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Campusbeheer ---");
            System.out.println("1. Alle campussen bekijken");
            System.out.println("2. Specifieke campus opvragen");
            System.out.println("3. Campus toevoegen");
            System.out.println("4. Campus verwijderen");
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
                case 1 -> getAllCampuses();
                case 2 -> getCampusById(scanner);
                case 3 -> addCampus(scanner);
                case 4 -> deleteCampus(scanner);
                case 5 -> {
                    return;
                }
                default -> System.out.println("Ongeldige keuze.");
            }
        }
    }

    private void getAllCampuses() {
        List<Campus> campuses = webClient.get()
                .uri("/campus")
                .retrieve()
                .bodyToFlux(Campus.class)
                .collectList()
                .block(); // bodyToFlux vervangen door block

        if (campuses == null || campuses.isEmpty()) {
            System.out.println("Er zijn geen campussen gevonden.");
        } else {
            campuses.forEach(campus -> System.out.println(
                    "Naam: " + campus.getNaam() + ", Adres: " + campus.getAdres() + ", Parkeerplaatsen: " + campus.getParkeerplaatsen()
            ));
        }
    }

    public void getCampusById(Scanner scanner) {
        try {
            // Campussen ophalen
            List<CampusDTO> campussen = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToFlux(CampusDTO.class)
                    .collectList()
                    .block();

            if (campussen == null || campussen.isEmpty()) {
                System.out.println("⚠️ Geen campussen gevonden.");
                return;
            }

            System.out.println("Beschikbare campussen:");
            campussen.forEach(c -> System.out.println("- " + c.naam));

            System.out.print("Kies campusnaam: ");
            String campusNaam = scanner.nextLine().trim();

            boolean geldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!geldig) {
                System.out.println("❌ Ongeldige campusnaam.");
                return;
            }

            // Campus ophalen en tonen
            CampusDTO campus = webClient.get()
                    .uri("/campus/" + campusNaam)
                    .retrieve()
                    .bodyToMono(CampusDTO.class)
                    .block();

            if (campus != null) {
                System.out.println("Campusgegevens:");
                System.out.printf("- Naam: %s%n- Adres: %s%n- Parkeerplaatsen: %d%n",
                        campus.naam, campus.adres, campus.parkeerplaatsen);
            } else {
                System.out.println("⚠️ Campus niet gevonden.");
            }

        } catch (Exception e) {
            System.out.println("❌ Fout bij ophalen van campus: " + e.getMessage());
        }
    }


    private void addCampus(Scanner scanner) {
        try {
            // Bestaat al een campus met deze naam?
            List<CampusDTO> bestaande = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToFlux(CampusDTO.class)
                    .collectList()
                    .block();

            // Input vragen
            System.out.print("Geef de naam van de nieuwe campus: ");
            String naam = scanner.nextLine().trim();

            if (naam.isEmpty()) {
                System.out.println("❌ Naam mag niet leeg zijn.");
                return;
            }

            boolean bestaatAl = bestaande != null && bestaande.stream().anyMatch(c -> c.naam.equalsIgnoreCase(naam));
            if (bestaatAl) {
                System.out.println("❌ Er bestaat al een campus met deze naam.");
                return;
            }

            System.out.print("Geef het adres van de campus: ");
            String adres = scanner.nextLine().trim();
            if (adres.isEmpty()) {
                System.out.println("❌ Adres mag niet leeg zijn.");
                return;
            }

            System.out.print("Geef het aantal parkeerplaatsen: ");
            int parkeerplaatsen = Integer.parseInt(scanner.nextLine().trim());
            if (parkeerplaatsen < 0) {
                System.out.println("❌ Parkeerplaatsen moeten positief zijn.");
                return;
            }

            // POST versturen
            Campus nieuweCampus = new Campus(naam, adres, parkeerplaatsen);
            Campus response = webClient.post()
                    .uri("/campus")
                    .bodyValue(nieuweCampus)
                    .retrieve()
                    .bodyToMono(Campus.class)
                    .block();

            if (response != null) {
                System.out.println("✅ Campus succesvol toegevoegd: " + response.getNaam());
            } else {
                System.out.println("❌ Fout bij het toevoegen van de campus.");
            }

        } catch (NumberFormatException e) {
            System.out.println("❌ Ongeldig aantal parkeerplaatsen.");
        } catch (Exception e) {
            System.out.println("❌ Fout bij toevoegen van campus: " + e.getMessage());
        }
    }
    private void deleteCampus(Scanner scanner) {
        try {
            CampusDTO[] campussen = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToMono(CampusDTO[].class)
                    .block();

            if (campussen == null || campussen.length == 0) {
                System.out.println("⚠️ Geen campussen beschikbaar.");
                return;
            }

            System.out.println("Beschikbare campussen:");
            for (int i = 0; i < campussen.length; i++) {
                System.out.printf("%d. %s%n", i + 1, campussen[i].naam);
            }

            System.out.print("Kies het nummer van de campus die je wilt verwijderen: ");
            int keuze = Integer.parseInt(scanner.nextLine().trim());

            if (keuze < 1 || keuze > campussen.length) {
                System.out.println("❌ Ongeldige keuze.");
                return;
            }

            String naam = campussen[keuze - 1].naam;

            webClient.delete()
                    .uri("/campussen/" + naam)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            System.out.println("✅ Campus '" + naam + "' werd verwijderd.");
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().value() == 409) {
                System.out.println("❌ Deze campus kan niet verwijderd worden omdat ze nog lokalen of reservaties bevat.");
            } else {
                System.out.println("Fout bij verwijderen van campus: " + e.getMessage());
            }
        } catch (Exception e) {
            System.out.println("Onverwachte fout: " + e.getMessage());
        }
    }


    private static class CampusDTO {
        public String naam;
        public String adres;
        public int parkeerplaatsen;
    }
}
