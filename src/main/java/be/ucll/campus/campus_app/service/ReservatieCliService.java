package be.ucll.campus.campus_app.service;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@Service
public class ReservatieCliService {

    private final WebClient webClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public ReservatieCliService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("http://localhost:8080").build();
    }

    public void reservatieMenu(Scanner scanner) {
        boolean running = true;
        while (running) {
            System.out.println("\n--- Reservatie Beheer ---");
            System.out.println("1. Bekijk alle reservaties");
            System.out.println("2. Voeg een reservatie toe");
            System.out.println("3. Verwijder een reservatie");
            System.out.println("4. Toon reservaties per lokaal binnen een campus");
            System.out.println("5. Bekijk beschikbare lokalen");
            System.out.println("6. Voeg een lokaal toe aan een reservatie");
            System.out.println("7. Terug naar hoofdmenu");
            System.out.print("Kies een optie: ");

            String input = scanner.nextLine().trim();
            int keuze;

            try {
                keuze = Integer.parseInt(input);
                if (keuze < 1 || keuze > 7) {
                    System.out.println("Ongeldige keuze. Kies een getal tussen 1 en 7.");
                    continue;
                }
            } catch (NumberFormatException e) {
                System.out.println("Ongeldige invoer. Gelieve een getal in te geven.");
                continue;
            }

            switch (keuze) {
                    case 1 -> viewAllReservations();
                    case 2 -> addReservation(scanner);
                    case 3 -> deleteReservation(scanner);
                    case 4 -> toonReservatiesPerLokaal(scanner);
                    case 5 -> bekijkBeschikbareLokalen(scanner);
                    case 6 -> voegLokaalToeAanReservatie(scanner);
                    case 7 -> running = false;
                    default -> System.out.println("Ongeldige keuze.");

            }
        }
    }

    private void viewAllReservations() {
        try {
            var reservaties = webClient.get()
                    .uri("/reservaties")
                    .retrieve()
                    .bodyToFlux(ReservatieDTO.class)
                    .collectList()
                    .block();

            if (reservaties == null || reservaties.isEmpty()) {
                System.out.println("Geen reservaties gevonden.");
                return;
            }

            System.out.println("Reservaties:");
            for (ReservatieDTO r : reservaties) {
                System.out.printf("- ID %d: %s → %s (campus: %s)%n", r.id, r.startTijd, r.eindTijd, r.campusNaam);
                if (r.lokalen != null && r.lokalen.length > 0) {
                    for (LokaalDTO l : r.lokalen) {
                        System.out.printf("    * Lokaal %s (ID %d, capaciteit %d)%n", l.naam, l.id, l.capaciteit);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println("Fout bij ophalen reservaties: " + e.getMessage());
        }
    }


    public void addReservation(Scanner scanner) {
        try {
            // === 1. Gebruikers ophalen ===
            List<KlantDTO> gebruikers = webClient.get()
                    .uri("/users")
                    .retrieve()
                    .bodyToFlux(KlantDTO.class)
                    .collectList()
                    .block();

            if (gebruikers == null || gebruikers.isEmpty()) {
                System.out.println("Geen gebruikers gevonden.");
                return;
            }

            System.out.println("Beschikbare gebruikers:");
            gebruikers.forEach(g -> System.out.printf("- ID %d: %s %s%n", g.id, g.voornaam, g.naam));

            System.out.print("Kies user ID: ");
            String userInput = scanner.nextLine().trim();
            Long userId;
            try {
                userId = Long.parseLong(userInput);
            } catch (NumberFormatException e) {
                System.out.println("Ongeldige user ID.");
                return;
            }

            boolean userGeldig = gebruikers.stream().anyMatch(g -> g.id.equals(userId));
            if (!userGeldig) {
                System.out.println("User ID niet gevonden.");
                return;
            }

            // === 2. Campussen ophalen ===
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
            campussen.forEach(c -> System.out.printf("- %s%n", c.naam));

            System.out.print("Kies campusnaam: ");
            String campusNaam = scanner.nextLine().trim();
            boolean campusGeldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!campusGeldig) {
                System.out.println("Ongeldige campus.");
                return;
            }

            // === 3. Tijdstippen vragen ===
            System.out.print("Voer starttijd (yyyy-MM-dd'T'HH:mm) in: ");
            String startInput = scanner.nextLine().trim();
            String start = (startInput.length() == 16) ? startInput + ":00" : startInput;

            System.out.print("Voer eindtijd (yyyy-MM-dd'T'HH:mm) in: ");
            String eindInput = scanner.nextLine().trim();
            String eind = (eindInput.length() == 16) ? eindInput + ":00" : eindInput;

            try {
                LocalDateTime startTijd = LocalDateTime.parse(start, FORMATTER);
                LocalDateTime eindTijd = LocalDateTime.parse(eind, FORMATTER);

                if (startTijd.isAfter(eindTijd)) {
                    System.out.println("Starttijd moet vóór eindtijd liggen.");
                    return;
                }

            } catch (DateTimeParseException e) {
                System.out.println("Ongeldig datumformaat. Gebruik yyyy-MM-dd'T'HH:mm");
                return;
            }


            // === 4. Beschikbare lokalen ophalen ===
            List<LokaalDTO> beschikbareLokalen = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reservaties/beschikbare-lokalen")
                            .queryParam("campusNaam", campusNaam)
                            .queryParam("startTijd", start)
                            .queryParam("eindTijd", eind)
                            .build())
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (beschikbareLokalen == null || beschikbareLokalen.isEmpty()) {
                System.out.println("Geen beschikbare lokalen op deze tijd.");
                return;
            }

            System.out.println("Beschikbare lokalen:");
            beschikbareLokalen.forEach(l -> System.out.printf("- ID %d: %s (capaciteit %d)%n", l.id, l.naam, l.capaciteit));

            System.out.print("Kies lokaal ID's (gescheiden door komma's): ");
            String[] gekozenIds = scanner.nextLine().trim().split("\\s*,\\s*");
            Set<Long> lokaalIds = Arrays.stream(gekozenIds)
                    .map(Long::parseLong)
                    .filter(id -> beschikbareLokalen.stream().anyMatch(l -> l.id.equals(id)))
                    .collect(Collectors.toSet());

            if (lokaalIds.isEmpty()) {
                System.out.println("Geen geldige lokaal ID's geselecteerd.");
                return;
            }

            // === 5. Reservatie aanmaken ===
            String requestBody = String.format(
                    "{\"startTijd\":\"%s\", \"eindTijd\":\"%s\", \"commentaar\":\"Geen commentaar\"}",
                    start, eind
            );

            Optional<String> response = Optional.ofNullable(webClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reservaties/user/" + userId)
                            .queryParam("lokaalIds", lokaalIds.stream()
                                    .map(String::valueOf)
                                    .collect(Collectors.joining(",")))
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block());

            response.ifPresentOrElse(
                    res -> System.out.println("Reservatie toegevoegd: " + res),
                    () -> System.out.println("Fout bij toevoegen van reservatie.")
            );

        } catch (Exception e) {
            System.out.println("Fout: " + e.getMessage());
        }
    }


    private void deleteReservation(Scanner scanner) {
        try {
            // === 1. Campussen tonen ===
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
                System.out.println("Ongeldige campus.");
                return;
            }

            // === 2. Reservaties per campus opvragen ===
            List<ReservatieDTO> reservaties = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reservaties/per-lokaal-campus")
                            .queryParam("campusNaam", campusNaam)
                            .build())
                    .retrieve()
                    .bodyToFlux(ReservatieDTO.class)
                    .collectList()
                    .block();

            if (reservaties == null || reservaties.isEmpty()) {
                System.out.println("Geen reservaties gevonden.");
                return;
            }

            // === 3. Filter op toekomstige reservaties ===
            List<ReservatieDTO> toekomst = reservaties.stream()
                    .filter(r -> LocalDateTime.parse(r.eindTijd).isAfter(LocalDateTime.now()))
                    .toList();

            if (toekomst.isEmpty()) {
                System.out.println("Geen geplande reservaties in deze campus.");
                return;
            }

            System.out.println("Geplande reservaties:");
            toekomst.forEach(r -> {
                System.out.printf("- ID %d: %s → %s, lokalen: ",
                        r.id, r.startTijd, r.eindTijd);
                if (r.lokalen != null && r.lokalen.length > 0) {
                    String lijst = Arrays.stream(r.lokalen)
                            .map(l -> l.naam)
                            .collect(Collectors.joining(", "));
                    System.out.print(lijst);
                } else {
                    System.out.print("geen lokalen");
                }
                System.out.println();
            });

            System.out.print("Kies reservatie-ID om te verwijderen: ");
            String input = scanner.nextLine().trim();
            long gekozenId;
            try {
                gekozenId = Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Ongeldig ID-formaat. Voer een numeriek ID in.");
                return;
            }

            boolean idGeldig = toekomst.stream().anyMatch(r -> r.id == gekozenId);
            if (!idGeldig) {
                System.out.println("Ongeldige reservatie-ID.");
                return;
            }

            // === 4. Verwijderen met bevestiging ===
            System.out.print("Ben je zeker dat je reservatie " + gekozenId + " wil verwijderen? (J/N): ");
            String bevestiging = scanner.nextLine().trim().toLowerCase();

            if (!bevestiging.equals("j") && !bevestiging.equals("ja")) {
                System.out.println("Verwijderen geannuleerd.");
                return;
            }

            webClient.delete()
                    .uri("/reservaties/" + gekozenId)
                    .retrieve()
                    .toBodilessEntity()
                    .block();

            System.out.println("Reservatie verwijderd.");

        } catch (Exception e) {
            System.out.println("Fout bij verwijderen reservatie: " + e.getMessage());
        }
    }

    private void bekijkBeschikbareLokalen(Scanner scanner) {
        try {
            // === 1. Campussen ophalen ===
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

            System.out.print("Campusnaam: ");
            String campusNaam = scanner.nextLine().trim();
            boolean campusGeldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campusNaam));
            if (!campusGeldig) {
                System.out.println("Ongeldige campusnaam.");
                return;
            }

            // === 2. Tijden vragen ===
            System.out.print("Starttijd (yyyy-MM-dd'T'HH:mm): ");
            String startInput = scanner.nextLine().trim();
            String start;
            LocalDateTime startTijd;
            try {
                startTijd = LocalDateTime.parse(
                        (startInput.length() == 16 ? startInput + ":00" : startInput),
                        FORMATTER
                );
                start = startTijd.format(FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Ongeldig starttijdformaat. Gebruik bv. 2025-05-07T08:00");
                return;
            }

            System.out.print("Eindtijd (yyyy-MM-dd'T'HH:mm): ");
            String eindInput = scanner.nextLine().trim();
            String eind;
            LocalDateTime eindTijd;
            try {
                eindTijd = LocalDateTime.parse(
                        (eindInput.length() == 16 ? eindInput + ":00" : eindInput),
                        FORMATTER
                );
                eind = eindTijd.format(FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Ongeldig eindtijdformaat. Gebruik bv. 2025-05-07T13:00");
                return;
            }

            if (startTijd.isAfter(eindTijd)) {
                System.out.println("Starttijd moet vóór eindtijd liggen.");
                return;
            }

            // === 3. Minimum capaciteit vragen (optioneel) ===
            System.out.print("Minimum aantal personen (optioneel): ");
            String minInput = scanner.nextLine().trim();
            Integer parsedMinAantal = null;
            if (!minInput.isEmpty()) {
                try {
                    parsedMinAantal = Integer.parseInt(minInput);
                } catch (NumberFormatException e) {
                    System.out.println("Ongeldige invoer voor minimum aantal personen.");
                    return;
                }
            }
            final Integer minAantal = parsedMinAantal;

            // === 4. Beschikbare lokalen opvragen ===
            List<LokaalDTO> lokalen = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reservaties/beschikbare-lokalen")
                            .queryParam("campusNaam", campusNaam)
                            .queryParam("startTijd", start)
                            .queryParam("eindTijd", eind)
                            .queryParamIfPresent("minAantal", Optional.ofNullable(minAantal))
                            .build())
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (lokalen == null || lokalen.isEmpty()) {
                System.out.println("Geen beschikbare lokalen gevonden.");
                return;
            }

            System.out.println("Beschikbare lokalen:");
            lokalen.forEach(l -> System.out.printf("- %s (capaciteit %d)%n", l.naam, l.capaciteit));

        } catch (Exception e) {
            System.out.println("Fout bij ophalen van beschikbare lokalen: " + e.getMessage());
        }
    }


    private void voegLokaalToeAanReservatie(Scanner scanner) {
        try {
            // === 1. Gebruikers tonen ===
            List<KlantDTO> gebruikers = webClient.get()
                    .uri("/users")
                    .retrieve()
                    .bodyToFlux(KlantDTO.class)
                    .collectList()
                    .block();

            if (gebruikers == null || gebruikers.isEmpty()) {
                System.out.println("Geen gebruikers gevonden.");
                return;
            }

            System.out.println("Gebruikers:");
            gebruikers.forEach(g -> System.out.printf("- ID %d: %s %s%n", g.id, g.voornaam, g.naam));

            String userInput = scanner.nextLine().trim();
            Long userId;
            try {
                userId = Long.parseLong(userInput);
            } catch (NumberFormatException e) {
                System.out.println("Ongeldige invoer voor user ID.");
                return;
            }
            boolean geldigUser = gebruikers.stream().anyMatch(g -> g.id.equals(userId));
            if (!geldigUser) {
                System.out.println("Ongeldige gebruiker.");
                return;
            }

            // === 2. Reservaties van gebruiker ophalen ===
            var reservaties = webClient.get()
                    .uri("/reservaties/user/" + userId)
                    .retrieve()
                    .bodyToFlux(ReservatieDTO.class)
                    .collectList()
                    .block();

            if (reservaties == null || reservaties.isEmpty()) {
                System.out.println("Deze gebruiker heeft geen reservaties.");
                return;
            }

            System.out.println("Reservaties van gebruiker:");
            reservaties.forEach(r -> System.out.printf("- ID %d: %s → %s%n", r.id, r.startTijd, r.eindTijd));

            System.out.print("Reservatie ID (uit bovenstaande lijst): ");
            String reservatieInput = scanner.nextLine().trim();
            Long reservatieId;
            try {
                reservatieId = Long.parseLong(reservatieInput);
            } catch (NumberFormatException e) {
                System.out.println("Ongeldige invoer voor reservatie ID.");
                return;
            }

            var gekozen = reservaties.stream().filter(r -> r.id.equals(reservatieId)).findFirst();
            if (gekozen.isEmpty()) {
                System.out.println("Ongeldige reservatie ID voor deze gebruiker.");
                return;
            }

            var reservatie = gekozen.get();

            // === 3. Beschikbare lokalen tijdens reservatieperiode ===
            String start = reservatie.startTijd;
            String eind = reservatie.eindTijd;
            var beschikbareLokalen = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/reservaties/beschikbare-lokalen")
                            .queryParam("startTijd", start)
                            .queryParam("eindTijd", eind)
                            .queryParam("campusNaam", reservatie.campusNaam)
                            .queryParam("reservatieId", reservatie.id)
                            .build())
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (beschikbareLokalen == null || beschikbareLokalen.isEmpty()) {
                System.out.println("Geen lokalen beschikbaar tijdens deze reservatieperiode.");
                return;
            }

            System.out.println("Beschikbare lokalen tijdens reservatie:");
            beschikbareLokalen.forEach(l -> System.out.printf("- ID %d: %s (%d plaatsen)%n", l.id, l.naam, l.capaciteit));

            Long lokaalId = null;
            while (true) {
                System.out.print("Lokaal ID (uit bovenstaande lijst): ");
                String lokaalInput = scanner.nextLine().trim();
                try {
                    lokaalId = Long.parseLong(lokaalInput);
                } catch (NumberFormatException e) {
                    System.out.println("Ongeldige invoer. Geef een numeriek ID.");
                    continue;
                }
                Long finalLokaalId = lokaalId;
                boolean geldigLokaal = beschikbareLokalen.stream().anyMatch(l -> l.id.equals(finalLokaalId));
                if (!geldigLokaal) {
                    System.out.println("Ongeldig lokaal ID gekozen. Probeer opnieuw.");
                } else {
                    break;
                }
            }

            var result = webClient.put()
                    .uri("/reservaties/user/" + userId + "/reservations/" + reservatieId + "/rooms/" + lokaalId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Reservatie geüpdatet: " + result);
        } catch (Exception e) {
            System.out.println("Fout bij toevoegen lokaal: " + e.getMessage());
        }
    }

    private void toonReservatiesPerLokaal(Scanner scanner) {
        try {
            // === 1. Campussen ophalen ===
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
            String campus = scanner.nextLine().trim();
            boolean campusGeldig = campussen.stream().anyMatch(c -> c.naam.equalsIgnoreCase(campus));
            if (!campusGeldig) {
                System.out.println("Ongeldige campusnaam.");
                return;
            }

            // === 2. Lokalen binnen de campus ophalen ===
            List<LokaalDTO> lokalen = webClient.get()
                    .uri("/campus/" + campus + "/rooms")
                    .retrieve()
                    .bodyToFlux(LokaalDTO.class)
                    .collectList()
                    .block();

            if (lokalen == null || lokalen.isEmpty()) {
                System.out.println("Geen lokalen gevonden voor deze campus.");
                return;
            }

            System.out.println("Lokalen binnen campus " + campus + ":");
            lokalen.forEach(l -> System.out.printf("- ID %d: %s (%d plaatsen)%n", l.id, l.naam, l.capaciteit));

            System.out.print("Kies lokaal ID: ");
            String input = scanner.nextLine().trim();
            Long lokaalId;
            try {
                lokaalId = Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Ongeldig ID-formaat. Voer een numeriek ID in.");
                return;
            }

            boolean lokaalGeldig = lokalen.stream().anyMatch(l -> l.id.equals(lokaalId));
            if (!lokaalGeldig) {
                System.out.println("Ongeldig lokaal ID.");
                return;
            }

            // === 3. Reservaties opvragen ===
            var reservaties = webClient.get()
                    .uri("/reservaties/campus/" + campus + "/rooms/" + lokaalId + "/reservations")
                    .retrieve()
                    .bodyToFlux(ReservatieDTO.class)
                    .collectList()
                    .block();

            if (reservaties == null || reservaties.isEmpty()) {
                System.out.println("Geen reservaties gevonden voor dit lokaal.");
            } else {
                System.out.println("Reservaties voor lokaal " + lokaalId + " in campus " + campus + ":");
                reservaties.forEach(r ->
                        System.out.printf("- ID %d: %s → %s%n", r.id, r.startTijd, r.eindTijd)
                );
            }

        } catch (Exception e) {
            System.out.println("Fout bij ophalen reservaties: " + e.getMessage());
        }
    }


    private static class ReservatieDTO {
        public Long id;
        public String startTijd;
        public String eindTijd;
        public String campusNaam;
        public LokaalDTO[] lokalen;
    }
    private static class LokaalDTO {
        public Long id;
        public String naam;
        public int capaciteit;
    }

    private static class CampusDTO {
        public String naam;
    }

    private static class KlantDTO {
        public Long id;
        public String voornaam;
        public String naam;
    }


}