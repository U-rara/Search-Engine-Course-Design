package cn.khala.datadisplay.model;

import javax.persistence.*;

@Entity
@Table(name = "news")
public class News {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private int id;

    @Column(nullable = false, length = 16)
    private String language;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, unique = true, length = 256)
    private String url;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String content;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String processed_content;

    public long getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getProcessed_content() {

        return processed_content.replace(","," | ");
    }

    public void setProcessed_content(String processed_content) {
        this.processed_content = processed_content;
    }
}
