package cn.khala.datadisplay.service.impl;

import cn.khala.datadisplay.model.News;
import cn.khala.datadisplay.repository.NewsRepository;
import cn.khala.datadisplay.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

@Service
@Component
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

    @Override
    @Async
    public void getIDF() {
        long startTime = System.currentTimeMillis();    //记录索引开始时间
        int newsNumZH = 0;
        int newsNumEN = 0;
        HashMap<String, Integer> wordMapEN = new HashMap<>();
        HashMap<String, Integer> wordMapZH = new HashMap<>();

        HashMap<String, Double> idfEN = new HashMap<>();
        HashMap<String, Double> idfZH = new HashMap<>();

        List<News> newsList = getNewsList();
        for (News news : newsList) {
            String[] wordVec = news.getProcessed_content().replace(" | ", ",").split(",");
            HashSet<String> wordSet = new HashSet<>(Arrays.asList(wordVec));
            if (news.getLanguage().equals("EN")) {
                newsNumEN++;
                for (String word : wordSet) {
                    wordMapEN.put(word, wordMapEN.get(word) == null ? 1 : wordMapEN.get(word) + 1);
                }
            } else {
                newsNumZH++;
                for (String word : wordSet) {
                    wordMapZH.put(word, wordMapZH.get(word) == null ? 1 : wordMapZH.get(word) + 1);
                }
            }
        }
        for (String word : wordMapEN.keySet()) {
            double idf = Math.log((double) newsNumEN / (wordMapEN.get(word) + 1));
            idfEN.put(word, idf);
        }

        for (String word : wordMapZH.keySet()) {
            double idf = Math.log((double) newsNumZH / (wordMapZH.get(word) + 1));
            idfZH.put(word, idf);
        }

        File writeFile1 = new File("./data/idf_en.csv");
        File writeFile2 = new File("./data/idf_zh.csv");

        try{
            BufferedWriter writeText1 = new BufferedWriter(new FileWriter(writeFile1));
            BufferedWriter writeText2 = new BufferedWriter(new FileWriter(writeFile2));

            for(String word:idfEN.keySet()){
                writeText1.newLine();
                writeText1.write(word+","+idfEN.get(word));
            }

            for(String word:idfZH.keySet()){
                writeText2.newLine();
                writeText2.write(word+","+idfZH.get(word));
            }

            writeText1.flush();
            writeText1.close();
            writeText2.flush();
            writeText2.close();
        }catch (FileNotFoundException e){
            System.out.println("没有找到指定文件");
        }catch (IOException e){
            System.out.println("文件读写出错");
        }

        System.out.println("IDF 计算完毕,耗时："+(System.currentTimeMillis()-startTime)+"毫秒");
    }
}
