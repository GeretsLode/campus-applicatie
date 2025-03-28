package be.ucll.campus.campus_app.service;

import be.ucll.campus.campus_app.model.Lokaal;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

@Service
public class LokaalCliService {
    private final WebClient webClient;

    public LokaalCliService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public void lokaalMenu(Scanner scanner) {
        while (true) {
            System.out.println("\n--- Lokaalbeheer ---");
            System.out.println("1. Alle lokalen binnen een campus bekijken");
            System.out.println("2. Specifiek lokaal opvragen");
            System.out.println("3. Lokaal toevoegen aan campus");
            System.out.println("4. Lokaal verwijderen");
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
                case 1 -> getAllLokalen(scanner);
                case 2 -> getLokaalById(scanner);
                case 3 -> addLokaal(scanner);
                case 4 -> deleteLokaal(scanner);
                case 5 -> {
                    return;
                }
                default -> System.out.println("Ongeldige keuze.");
            }
        }
    }

    public void addLokaal(Scanner scanner) {
        try {
            // 1. Campussen ophalen
            List<CampusDTO> campussen = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToFlux(CampusDTO.class)
                    .collectList()
                    .block();

            if (campussen == null || campussen.isEmpty()) {
                System.out.println("Geen campussen beschikbaar.");
                return;
            }

            System.out.println("Beschikbare campussen:");
            campussen.forEach(c -> System.out.println("- " + c.naam));

            System.out.print("Kies campusnaam: ");
            String campusNaam = scanner.nextLine().trim();

            boolean geldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!geldig) {
                System.out.println("Ongeldige campusnaam.");
                return;
            }

            // 2. Lokaalgegevens vragen
            System.out.print("Geef de naam van het lokaal: ");
            String naam = scanner.nextLine().trim();
            if (naam.isEmpty()) {
                System.out.println("Naam mag niet leeg zijn.");
                return;
            }
            // Check op dubbele lokaalnaam in dezelfde campus
            List<LokaalDTO> bestaandeLokalen = webClient.get()
                    .uri("/campus/" + campusNaam + "/rooms")
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (bestaandeLokalen != null &&
                    bestaandeLokalen.stream().anyMatch(l -> l.naam.equalsIgnoreCase(naam))) {
                System.out.println("Er bestaat al een lokaal met deze naam binnen deze campus.");
                return;
            }

            System.out.print("Geef het type lokaal: ");
            String type = scanner.nextLine().trim();
            if (type.isEmpty()) {
                System.out.println("Type mag niet leeg zijn.");
                return;
            }

            int capaciteit = -1;
            while (capaciteit <= 0) {
                System.out.print("Geef de capaciteit: ");
                String input = scanner.nextLine().trim();
                try {
                    capaciteit = Integer.parseInt(input);
                    if (capaciteit <= 0) {
                        System.out.println("Capaciteit moet een positief getal zijn.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ongeldige invoer. Geef een geldig getal op.");
                }
            }

            int verdieping;
            while (true) {
                System.out.print("Geef de verdieping: ");
                String input = scanner.nextLine().trim();
                try {
                    verdieping = Integer.parseInt(input);
                    break;
                } catch (NumberFormatException e) {
                    System.out.println("Ongeldige invoer. Geef een geldig getal op.");
                }
            }

            // 3. Lokaal aanmaken en versturen
            Lokaal lokaal = new Lokaal();
            lokaal.setNaam(naam);
            lokaal.setType(type);
            lokaal.setCapaciteit(capaciteit);
            lokaal.setVerdieping(verdieping);

            Lokaal response = webClient.post()
                    .uri("/campus/" + campusNaam + "/rooms")
                    .bodyValue(lokaal)
                    .retrieve()
                    .bodyToMono(Lokaal.class)
                    .block();

            if (response != null) {
                System.out.println("Lokaal toegevoegd: " + response.getNaam());
            } else {
                System.out.println("Fout bij toevoegen lokaal.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Ongeldige numerieke invoer.");
        } catch (Exception e) {
            System.out.println("Fout bij toevoegen lokaal: " + e.getMessage());
        }
    }


    public void getAllLokalen(Scanner scanner) {
        try {
            // 1. Campussen ophalen
            List<CampusDTO> campussen = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToFlux(CampusDTO.class)
                    .collectList()
                    .block();

            if (campussen == null || campussen.isEmpty()) {
                System.out.println("Geen campussen gevonden.");
                return;
            }

            // 2. Campussen tonen
            System.out.println("Beschikbare campussen:");
            campussen.forEach(c -> System.out.println("- " + c.naam));

            System.out.print("Kies campusnaam: ");
            String campusNaam = scanner.nextLine().trim();

            boolean geldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!geldig) {
                System.out.println("Ongeldige campusnaam.");
                return;
            }

            // 3. Lokalen ophalen
            List<LokaalDTO> lokalen = webClient.get()
                    .uri("/campus/" + campusNaam + "/rooms")
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (lokalen == null || lokalen.isEmpty()) {
                System.out.println("Geen lokalen gevonden voor deze campus.");
                return;
            }

            // 4. Lokalen tonen
            System.out.println("Lokalen in campus " + campusNaam + ":");
            lokalen.forEach(l -> System.out.printf("- ID %d: %s (%d plaatsen), verdieping %d%n",
                    l.id, l.naam, l.capaciteit, l.verdieping));

        } catch (Exception e) {
            System.out.println("Fout bij ophalen van lokalen: " + e.getMessage());
        }
    }


    public void getLokaalById(Scanner scanner) {
        try {
            // 1. Campussen ophalen en tonen
            List<CampusDTO> campussen = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToFlux(CampusDTO.class)
                    .collectList()
                    .block();

            if (campussen == null || campussen.isEmpty()) {
                System.out.println("Geen campussen gevonden.");
                return;
            }

            System.out.println("Beschikbare campussen:");
            campussen.forEach(c -> System.out.println("- " + c.naam));

            System.out.print("Kies campusnaam: ");
            String campusNaam = scanner.nextLine().trim();

            boolean geldigeCampus = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!geldigeCampus) {
                System.out.println("Ongeldige campusnaam.");
                return;
            }

            // 2. Lokalen ophalen voor die campus
            List<LokaalDTO> lokalen = webClient.get()
                    .uri("/campus/" + campusNaam + "/rooms")
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (lokalen == null || lokalen.isEmpty()) {
                System.out.println("Geen lokalen gevonden in deze campus.");
                return;
            }

            System.out.println("Lokalen in " + campusNaam + ":");
            lokalen.forEach(l -> System.out.printf("- ID %d: %s%n", l.id, l.naam));

            // 3. Lokaal ID kiezen met invoercontrole
            Long lokaalId = null;
            while (lokaalId == null) {
                System.out.print("Kies lokaal ID: ");
                String input = scanner.nextLine().trim();
                try {
                    long gekozenId = Long.parseLong(input);
                    boolean geldigLokaal = lokalen.stream().anyMatch(l -> l.id.equals(gekozenId));
                    if (geldigLokaal) {
                        lokaalId = gekozenId;
                    } else {
                        System.out.println("Ongeldig lokaal ID. Kies uit de lijst.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ongeldige invoer. Geef een geldig cijfer in.");
                }
            }

            // 4. Lokaalgegevens ophalen en tonen
            LokaalDTO lokaal = webClient.get()
                    .uri("/campus/" + campusNaam + "/rooms/" + lokaalId)
                    .retrieve()
                    .bodyToMono(LokaalDTO.class)
                    .block();

            if (lokaal != null) {
                System.out.printf("Lokaalgegevens:%n- Naam: %s%n- Capaciteit: %d%n- Verdieping: %d%n",
                        lokaal.naam, lokaal.capaciteit, lokaal.verdieping);
            } else {
                System.out.println("Lokaal niet gevonden.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Ongeldig ID-formaat.");
        } catch (Exception e) {
            System.out.println("Fout bij ophalen lokaal: " + e.getMessage());
        }
    }

    public void deleteLokaal(Scanner scanner) {
        try {
            // 1. Campussen ophalen
            List<CampusDTO> campussen = webClient.get()
                    .uri("/campus")
                    .retrieve()
                    .bodyToFlux(CampusDTO.class)
                    .collectList()
                    .block();

            if (campussen == null || campussen.isEmpty()) {
                System.out.println("Geen campussen gevonden.");
                return;
            }

            System.out.println("Beschikbare campussen:");
            campussen.forEach(c -> System.out.println("- " + c.naam));

            System.out.print("Kies campusnaam: ");
            String campusNaam = scanner.nextLine().trim();

            boolean geldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!geldig) {
                System.out.println("Ongeldige campusnaam.");
                return;
            }

            // 2. Lokalen van deze campus ophalen
            List<LokaalDTO> lokalen = webClient.get()
                    .uri("/campus/" + campusNaam + "/rooms")
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (lokalen == null || lokalen.isEmpty()) {
                System.out.println("Geen lokalen in deze campus.");
                return;
            }

            System.out.println("Beschikbare lokalen:");
            lokalen.forEach(l -> System.out.printf("- ID %d: %s%n", l.id, l.naam));

            // 3. ID opvragen met inputvalidatie
            Long lokaalId = null;
            while (lokaalId == null) {
                System.out.print("Kies lokaal ID om te verwijderen: ");
                String input = scanner.nextLine().trim();
                try {
                    long gekozenId = Long.parseLong(input);
                    boolean geldigLokaal = lokalen.stream().anyMatch(l -> l.id.equals(gekozenId));
                    if (geldigLokaal) {
                        lokaalId = gekozenId;
                    } else {
                        System.out.println("Ongeldig lokaal ID. Kies een ID uit de lijst.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Ongeldige invoer. Geef een numeriek ID in.");
                }
            }


            // 4. Verwijderen
            webClient.delete()
                    .uri("/campus/" + campusNaam + "/rooms/" + lokaalId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            System.out.println("Lokaal verwijderd.");

        } catch (NumberFormatException e) {
            System.out.println("Ongeldige invoer.");
        } catch (Exception e) {
            System.out.println("Fout bij verwijderen lokaal: " + e.getMessage());
        }
    }


    private static class CampusDTO {
        public String naam;
    }

    private static class LokaalDTO {
        public Long id;
        public String naam;
        public int capaciteit;
        public int verdieping;
    }
}

