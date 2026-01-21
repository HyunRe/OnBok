package com.onbok.book_hub.book.infrastructure;

import com.onbok.book_hub.book.application.service.book.BookCommandService;
import com.onbok.book_hub.book.application.service.bookEs.BookEsService;
import com.onbok.book_hub.book.domain.model.book.Book;
import com.onbok.book_hub.book.domain.model.bookEs.BookEs;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class CsvFileReaderService {
    private final ResourceLoader resourceLoader;
    private final BookCommandService bookCommandService;
    private final BookEsService bookEsService;

    public void csvFileToDB() {
        try {
            Resource resource = resourceLoader.getResource("classpath:/static/data/20241114_yes24_국내도서_새로나온_상품.csv");
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                int count = 0;
                for (CSVRecord record: csvParser) {
                    String title = record.get("title");
                    String author = record.get("author");
                    String company = record.get("company");
                    String _price = record.get("price");
                    int price = Integer.parseInt(_price);
                    String imageUrl = record.get("imageUrl");
                    String summary = record.get("summary");
                    Book book = Book.builder()
                            .title(title).author(author).company(company).price(price).imageUrl(imageUrl).summary(summary)
                            .build();
                    bookCommandService.insertBook(book);

                    if (count ++ == 100)
                        break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void csvFileToElasticSearch() {
        try {
            Resource resource = resourceLoader.getResource("classpath:/static/data/yes24_국내도서_새로나온_상품.csv");
            try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                 CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT.withFirstRecordAsHeader())) {
                int count = 0;
                for (CSVRecord record: csvParser) {
                    String title = record.get("title");
                    String author = record.get("author");
                    String company = record.get("company");
                    String _price = record.get("price");
                    int price = Integer.parseInt(_price);
                    String imageUrl = record.get("imageUrl");
                    String summary = record.get("summary");
                    BookEs bookEs = BookEs.builder()
                            .title(title).author(author).company(company).price(price).imageUrl(imageUrl).summary(summary)
                            .build();
                    bookEsService.insertBookEs(bookEs);

                    if (count++ % 1000 == 0)
                        System.out.println("count = " + count);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
