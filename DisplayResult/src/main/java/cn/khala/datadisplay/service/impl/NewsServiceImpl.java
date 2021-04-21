package cn.khala.datadisplay.service.impl;

import cn.khala.datadisplay.model.News;
import cn.khala.datadisplay.repository.NewsRepository;
import cn.khala.datadisplay.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsServiceImpl implements NewsService {
    @Autowired
    private NewsRepository newsRepository;

    @Override
    public List<News> getNewsList() {
        return newsRepository.findAll();
    }

    @Override
    public News findNewsById(int id) {
        return newsRepository.findById(id);
    }
}
