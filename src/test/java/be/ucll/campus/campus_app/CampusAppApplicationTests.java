package be.ucll.campus.campus_app;

import be.ucll.campus.campus_app.exception.InvalidReservationException;
import be.ucll.campus.campus_app.model.User;
import be.ucll.campus.campus_app.repository.*;
import be.ucll.campus.campus_app.service.ReservatieService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
class CampusAppApplicationTests {

	@Autowired
	private MockMvc mockMvc;
	@Autowired
	private CampusRepository campusRepository;

	@Test
	void contextLoads() {
	}

	@Test
	void testCampusAanmakenViaApi() throws Exception {
		// Verwijder eventuele oude testcampus
		campusRepository.deleteById("TESTCAMPUS");

		String json = "{\"naam\":\"TESTCAMPUS\",\"adres\":\"Teststraat 1\",\"parkeerplaatsen\":20}";

		mockMvc.perform(post("/campus")
						.contentType(MediaType.APPLICATION_JSON)
						.content(json))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.naam").value("TESTCAMPUS"))
				.andExpect(jsonPath("$.adres").value("Teststraat 1"))
				.andExpect(jsonPath("$.parkeerplaatsen").value(20));

		// Ruim de rommel terug op
		campusRepository.deleteById("TESTCAMPUS");
	}

	@Test
	void testReservatieInVerledenGeeftFout() {
		ReservatieRepository reservatieRepo = mock(ReservatieRepository.class);
		UserRepository userRepo = mock(UserRepository.class);
		LokaalRepository lokaalRepo = mock(LokaalRepository.class);
		ReservatieLokaalRepository rlRepo = mock(ReservatieLokaalRepository.class);

		ReservatieService service = new ReservatieService(reservatieRepo, userRepo, lokaalRepo, rlRepo);

		// Mock gebruiker
		User mockUser = new User();
		mockUser.setId(1L);
		when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));

		String start = LocalDateTime.now().minusDays(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
		String eind = LocalDateTime.now().minusDays(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

		InvalidReservationException ex = assertThrows(InvalidReservationException.class,
				() -> service.addReservatie(1L, start, eind, Set.of(1L)));

		assertTrue(ex.getMessage().contains("Reservatie kan niet in het verleden liggen"));
	}
	@Test
	void testStarttijdNaEindtijdGeeftFout() {
		ReservatieRepository reservatieRepo = mock(ReservatieRepository.class);
		UserRepository userRepo = mock(UserRepository.class);
		LokaalRepository lokaalRepo = mock(LokaalRepository.class);
		ReservatieLokaalRepository rlRepo = mock(ReservatieLokaalRepository.class);

		ReservatieService service = new ReservatieService(reservatieRepo, userRepo, lokaalRepo, rlRepo);

		// Mock gebruiker
		User mockUser = new User();
		mockUser.setId(1L);
		when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));

		String start = LocalDateTime.now().plusHours(2).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
		String eind = LocalDateTime.now().plusHours(1).format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

		InvalidReservationException ex = assertThrows(InvalidReservationException.class,
				() -> service.addReservatie(1L, start, eind, Set.of(1L)));

		assertTrue(ex.getMessage().contains("Starttijd moet vóór de eindtijd liggen"));
	}

	@Test
	void testOverlappendeReservatieGeeftFout() {
		ReservatieRepository reservatieRepo = mock(ReservatieRepository.class);
		UserRepository userRepo = mock(UserRepository.class);
		LokaalRepository lokaalRepo = mock(LokaalRepository.class);
		ReservatieLokaalRepository rlRepo = mock(ReservatieLokaalRepository.class);

		ReservatieService service = new ReservatieService(reservatieRepo, userRepo, lokaalRepo, rlRepo);

		// Mock gebruiker
		User mockUser = new User();
		mockUser.setId(1L);
		when(userRepo.findById(1L)).thenReturn(Optional.of(mockUser));

		String start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
				.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
		String eind = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)
				.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

		// Simuleer dat het lokaal in die periode al gereserveerd is
		when(reservatieRepo.existsByLokaalIdAndOverlappingTijd(1L,
				LocalDateTime.parse(start), LocalDateTime.parse(eind)))
				.thenReturn(true);

		InvalidReservationException ex = assertThrows(InvalidReservationException.class,
				() -> service.addReservatie(1L, start, eind, Set.of(1L)));

		assertTrue(ex.getMessage().contains("Lokaal is al gereserveerd in deze periode"));
	}
}
