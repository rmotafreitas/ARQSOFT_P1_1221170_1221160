package pt.psoft.g1.psoftg1.lendingmanagement.repositories;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import pt.psoft.g1.psoftg1.authormanagement.model.Author;
import pt.psoft.g1.psoftg1.authormanagement.repositories.AuthorRepository;
import pt.psoft.g1.psoftg1.bookmanagement.model.Book;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.genremanagement.model.Genre;
import pt.psoft.g1.psoftg1.genremanagement.repositories.GenreRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.readermanagement.model.ReaderDetails;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;
import pt.psoft.g1.psoftg1.usermanagement.model.Reader;
import pt.psoft.g1.psoftg1.usermanagement.repositories.UserRepository;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
public class LendingRepositoryIntegrationTest {

    @Autowired
    private LendingRepository lendingRepository;
    @Autowired
    private ReaderRepository readerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private GenreRepository genreRepository;
    @Autowired
    private AuthorRepository authorRepository;

    private Lending lending;
    private ReaderDetails readerDetails;
    private Reader reader;
    private Book book;
    private Author author;
    private Genre genre;

    @BeforeEach
    public void setUp() {
        author = new Author("Manuel Antonio Pina",
                "Manuel António Pina foi um jornalista e escritor português, premiado em 2011 com o Prémio Camões",
                null);
        authorRepository.save(author);

        genre = new Genre("Género");
        genreRepository.save(genre);

        List<Author> authors = List.of(author);
        book = new Book("9782826012092",
                "O Inspetor Max",
                "conhecido pastor-alemão que trabalha para a Judiciária, vai ser fundamental para resolver um importante caso de uma rede de malfeitores que quer colocar uma bomba num megaconcerto de uma ilustre cantora",
                genre,
                authors,
                null);
        bookRepository.save(book);

        reader = Reader.newReader("manuel@gmail.com", "Manuelino123!", "Manuel Sarapinto das Coives");
        userRepository.save(reader);

        readerDetails = new ReaderDetails(1,
                reader,
                "2000-01-01",
                "919191919",
                true,
                true,
                true,
                null,
                null);
        readerRepository.save(readerDetails);

        // Create and save the lending
        lending = Lending.newBootstrappingLending(book,
                readerDetails,
                LocalDate.now().getYear(),
                999,
                LocalDate.of(LocalDate.now().getYear(), 1,1),
                LocalDate.of(LocalDate.now().getYear(), 1,11),
                15,
                300);
        lendingRepository.save(lending);
    }

    @AfterEach
    public void tearDown(){
        lendingRepository.delete(lending);
        readerRepository.delete(readerDetails);
        userRepository.delete(reader);
        bookRepository.delete(book);
        genreRepository.delete(genre);
        authorRepository.delete(author);
    }

    @Test
    public void testSave() {
        Lending newLending = new Lending(lending.getBook(), lending.getReaderDetails(), 2, 14, 50);
        Lending savedLending = lendingRepository.save(newLending);
        assertThat(savedLending).isNotNull();
        assertThat(savedLending.getLendingNumber()).isEqualTo(newLending.getLendingNumber());
        lendingRepository.delete(savedLending);
    }

    @Test
    public void testFindByLendingNumber() {
        String ln = lending.getLendingNumber();
        Optional<Lending> found = lendingRepository.findByLendingNumber(ln);
        assertThat(found).isPresent();
        assertThat(found.get().getLendingNumber()).isEqualTo(ln);
    }

    @Test
    public void testListByReaderNumberAndIsbn() {
        List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(lending.getReaderDetails().getReaderNumber(), lending.getBook().getIsbn());
        assertThat(lendings).isNotEmpty();
        assertThat(lendings).contains(lending);
    }

    @Test
    public void testGetCountFromCurrentYear() {
        int count = lendingRepository.getCountFromCurrentYear();
        assertThat(count).isEqualTo(1);
        var lending2 = Lending.newBootstrappingLending(book,
                readerDetails,
                LocalDate.now().getYear(),
                998,
                LocalDate.of(LocalDate.now().getYear(), 5,31),
                null,
                15,
                300);
        lendingRepository.save(lending2);
        count = lendingRepository.getCountFromCurrentYear();
        assertThat(count).isEqualTo(2);
    }

    @Test
    public void testListOutstandingByReaderNumber() {
        var lending2 = Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                998,
                LocalDate.of(2024, 5,31),
                null,
                15,
                300);
        lendingRepository.save(lending2);
        List<Lending> outstandingLendings = lendingRepository.listOutstandingByReaderNumber(lending.getReaderDetails().getReaderNumber());
        assertThat(outstandingLendings).contains(lending2);
    }

    @Test
    public void testGetAverageDuration() {
        double lendingDuration1 = ChronoUnit.DAYS.between(lending.getStartDate(), lending.getReturnedDate());
        Double averageDuration = lendingRepository.getAverageDuration();
        assertNotNull(averageDuration);
        assertEquals(lendingDuration1, lendingRepository.getAverageDuration(), 0.001);

        var lending2 = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                998,
                LocalDate.of(2024, 2,1),
                LocalDate.of(2024, 4,4),
                15,
                300));
        double lendingDuration2 = ChronoUnit.DAYS.between(lending2.getStartDate(), lending2.getReturnedDate());
        double expectedAvg = (lendingDuration1 + lendingDuration2) / 2 ;
        assertEquals(expectedAvg, lendingRepository.getAverageDuration(), 0.001);

        var lending3 = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                997,
                LocalDate.of(2024, 3,1),
                LocalDate.of(2024, 4,25),
                15,
                300));
        double lendingDuration3 = ChronoUnit.DAYS.between(lending3.getStartDate(), lending3.getReturnedDate());
        expectedAvg = (lendingDuration1 + lendingDuration2 + lendingDuration3) / 3 ;
        assertEquals(expectedAvg, lendingRepository.getAverageDuration(), 0.001);

    }

    @Test
    public void testGetOverdue() {
        var returnedLateLending = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                998,
                LocalDate.of(2024, 1,1),
                LocalDate.of(2024, 2,1),
                15,
                300));
        var notReturnedLending = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                997,
                LocalDate.of(2024, 3,1),
                null,
                15,
                300));
        var notReturnedAndNotOverdueLending = lendingRepository.save(Lending.newBootstrappingLending(book,
                readerDetails,
                2024,
                996,
                LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(),LocalDate.now().getDayOfMonth()),
                null,
                15,
                300));
        Page page = new Page(1, 10);
        List<Lending> overdueLendings = lendingRepository.getOverdue(page);
        assertThat(overdueLendings).doesNotContain(returnedLateLending);
        assertThat(overdueLendings).contains(notReturnedLending);
        assertThat(overdueLendings).doesNotContain(notReturnedAndNotOverdueLending);
    }
}
