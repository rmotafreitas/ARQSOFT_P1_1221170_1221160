package pt.psoft.g1.psoftg1.lendingmanagement.api;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pt.psoft.g1.psoftg1.lendingmanagement.model.Lending;
import pt.psoft.g1.psoftg1.shared.api.MapperInterface;

import java.util.*;

/**
 * Brief guides:
 * <a href="https://www.baeldung.com/mapstruct">https://www.baeldung.com/mapstruct</a>
 * <p>
 * <a href="https://medium.com/@susantamon/mapstruct-a-comprehensive-guide-in-spring-boot-context-1e7202da033e">https://medium.com/@susantamon/mapstruct-a-comprehensive-guide-in-spring-boot-context-1e7202da033e</a>
 * */
@Mapper(componentModel = "spring")
public abstract class LendingViewMapper extends MapperInterface {

    @Mapping(target = "lendingNumber", source = "lendingNumber")
    @Mapping(target = "bookTitle", source = "book.title")
    @Mapping(target = "fineValueInCents", expression = "java(lending.getFineValueInCents().orElse(null))")
    @Mapping(target = "_links.self", source = ".", qualifiedByName = "lendingLink")
    @Mapping(target = "_links.book", source = "book", qualifiedByName = "bookLink")
    @Mapping(target = "returnedDate", source = "returnedDate")
    @Mapping(target = "_links.reader", source = "readerDetails", qualifiedByName = "readerLink")
    public abstract LendingView toLendingView(Lending lending);

    public abstract List<LendingView> toLendingView(List<Lending> lendings);

    public abstract LendingsAverageDurationView toLendingsAverageDurationView(Double lendingsAverageDuration);
}
