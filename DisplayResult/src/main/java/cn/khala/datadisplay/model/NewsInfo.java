package cn.khala.datadisplay.model;

import javax.persistence.*;

@Entity
@Table(name = "news_info")
public class NewsInfo {
    @Id
    @Column(nullable = false, unique = true)
    private int id;

    @Column(nullable = false, length = 16)
    private String language;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(nullable = false, unique = true, length = 256)
    private String url;

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
}
