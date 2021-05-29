package cn.khala.datadisplay.service.impl;

import cn.khala.datadisplay.model.NewsInfo;
import cn.khala.datadisplay.repository.NewsInfoRepository;
import cn.khala.datadisplay.service.NewsInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NewsInfoServiceImpl implements NewsInfoService {
    @Autowired
    private NewsInfoRepository newsInfoRepository;

    @Override
    public NewsInfo findById(int id){return newsInfoRepository.findById(id);}

    @Override
    public List<NewsInfo> getNewsInfoList() { return newsInfoRepository.findAll(); }
}
