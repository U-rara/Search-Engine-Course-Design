package cn.khala.datadisplay.service;

import cn.khala.datadisplay.model.NewsInfo;

import java.util.List;

public interface NewsInfoService {
    public List<NewsInfo> getNewsInfoList();
    public NewsInfo findById(int id);
}
