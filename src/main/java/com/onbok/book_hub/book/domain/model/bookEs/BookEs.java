package com.onbok.book_hub.book.domain.model.bookEs;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;

@Document(indexName = "books")
@Setting(settingPath = "/elasticsearch/book-settings.json")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BookEs {
    @Id
    @Field(type = FieldType.Keyword)
    private String bookId;      // UUID style

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "my_nori_analyzer", searchAnalyzer = "my_nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String title;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "my_nori_analyzer", searchAnalyzer = "my_nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String author;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "my_nori_analyzer", searchAnalyzer = "my_nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String company;

    @Field(type = FieldType.Integer)
    private int price;

    @Field(type = FieldType.Keyword)
    private String imageUrl;

    @Field(type = FieldType.Text, analyzer = "my_nori_analyzer", searchAnalyzer = "my_nori_analyzer")
    private String summary;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "my_nori_analyzer", searchAnalyzer = "my_nori_analyzer"),
            otherFields = {
                    @InnerField(suffix = "keyword", type = FieldType.Keyword)
            }
    )
    private String category;    // 카테고리 (예: "소설", "IT/컴퓨터", "자기계발" 등)

    @Builder
    public BookEs(String bookId, String title, String author, String company, int price, String imageUrl, String summary, String category) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.company = company;
        this.price = price;
        this.imageUrl = imageUrl;
        this.summary = summary;
        this.category = category;
    }

    public void updateSummary(String summary) {
        this.summary = summary;
    }
}
