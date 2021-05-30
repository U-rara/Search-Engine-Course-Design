package cn.khala.datadisplay.service;

import cn.khala.datadisplay.model.News;

import java.util.HashMap;
import java.util.List;

public interface NewsService {
    public List<News> getNewsList();

    public News findNewsById(int id);

    public void getIDF();
}
