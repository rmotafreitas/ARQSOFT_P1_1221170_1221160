package pt.psoft.g1.psoftg1.lendingmanagement.services;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import pt.psoft.g1.psoftg1.bookmanagement.repositories.BookRepository;
import pt.psoft.g1.psoftg1.exceptions.LendingForbiddenException;
import pt.psoft.g1.psoftg1.exceptions.NotFoundException;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Fine;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.FineRepository;
import pt.psoft.g1.psoftg1.lendingmanagement.repositories.LendingRepository;
import pt.psoft.g1.psoftg1.readermanagement.repositories.ReaderRepository;
import pt.psoft.g1.psoftg1.shared.services.Page;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@PropertySource({"classpath:config/library.properties"})
public class LendingServiceImpl implements LendingService{
    private final LendingRepository lendingRepository;
    private final FineRepository fineRepository;
    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;

    @Value("${lendingDurationInDays}")
    private int lendingDurationInDays;
    @Value("${fineValuePerDayInCents}")
    private int fineValuePerDayInCents;

    @Override
    public Optional<Lending> findByLendingNumber(String lendingNumber){
        return lendingRepository.findByLendingNumber(lendingNumber);
    }

    @Override
    public List<Lending> listByReaderNumberAndIsbn(String readerNumber, String isbn, Optional<Boolean> returned){
        List<Lending> lendings = lendingRepository.listByReaderNumberAndIsbn(readerNumber, isbn);
        if(returned.isEmpty()){
            return lendings;
        }else{
            for(int i = 0; i < lendings.size(); i++){
                if ((lendings.get(i).getReturnedDate() == null) == returned.get()){
                    lendings.remove(i);
                    i--;
                }
            }
        }
        return lendings;
    }

    @Override
    public Lending create(final CreateLendingRequest resource) {
        int count = 0;

        Iterable<Lending> lendingList = lendingRepository.listOutstandingByReaderNumber(resource.getReaderNumber());
        for (Lending lending : lendingList) {
            //Business rule: cannot create a lending if user has late outstanding books to return.
            if (lending.getDaysDelayed() > 0) {
                throw new LendingForbiddenException("Reader has book(s) past their due date");
            }
            count++;
            //Business rule: cannot create a lending if user already has 3 outstanding books to return.
            if (count >= 3) {
                throw new LendingForbiddenException("Reader has three books outstanding already");
            }
        }

        final var b = bookRepository.findByIsbn(resource.getIsbn())
                .orElseThrow(() -> new NotFoundException("Book not found"));
        final var r = readerRepository.findByReaderNumber(resource.getReaderNumber())
                .orElseThrow(() -> new NotFoundException("Reader not found"));
        int seq = lendingRepository.getCountFromCurrentYear()+1;
        final Lending l = new Lending(b,r,seq, lendingDurationInDays, fineValuePerDayInCents );

        return lendingRepository.save(l);
    }

    @Override
    public Lending setReturned(final String lendingNumber, final SetLendingReturnedRequest resource, final long desiredVersion) {

        var lending = lendingRepository.findByLendingNumber(lendingNumber)
                .orElseThrow(() -> new NotFoundException("Cannot update lending with this lending number"));

        lending.setReturned(desiredVersion, resource.getCommentary());

        if(lending.getDaysDelayed() > 0){
            final var fine = new Fine(lending);
            fineRepository.save(fine);
        }

        return lendingRepository.save(lending);
    }

    @Override
    public Double getAverageDuration(){
        Double avg = lendingRepository.getAverageDuration();
        return Double.valueOf(String.format(Locale.US,"%.1f", avg));
    }

    @Override
    public List<Lending> getOverdue(Page page) {
        if (page == null) {
            page = new Page(1, 10);
        }
        return lendingRepository.getOverdue(page);
    }

    @Override
    public Double getAvgLendingDurationByIsbn(String isbn){
        Double avg = lendingRepository.getAvgLendingDurationByIsbn(isbn);
        return Double.valueOf(String.format(Locale.US,"%.1f", avg));
    }

    @Override
    public List<Lending> searchLendings(Page page, SearchLendingQuery query){
        LocalDate startDate = null;
        LocalDate endDate = null;

        if (page == null) {
            page = new Page(1, 10);
        }
        if (query == null)
            query = new SearchLendingQuery("",
                    "",
                    null,
                    LocalDate.now().minusDays(10L).toString(),
                    null);

        try {
            if(query.getStartDate()!=null)
                startDate = LocalDate.parse(query.getStartDate());
            if(query.getEndDate()!=null)
                endDate = LocalDate.parse(query.getEndDate());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Expected format is YYYY-MM-DD");
        }

        return lendingRepository.searchLendings(page,
                query.getReaderNumber(),
                query.getIsbn(),
                query.getReturned(),
                startDate,
                endDate);

    }



}
