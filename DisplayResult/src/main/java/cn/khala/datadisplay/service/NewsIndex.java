package cn.khala.datadisplay.service;


import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import cn.khala.datadisplay.model.News;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.springframework.scheduling.annotation.Async;

public class NewsIndex{
    private volatile static NewsIndex instance;

    private final static String INDEX_DIR = "./data/LuceneIndex";

    private static class SingletonHolder{
        private final static NewsIndex instance=new NewsIndex();
    }

    public static NewsIndex getInstance(){
        return SingletonHolder.instance;
    }

    @Async
    public void indexAllNews(List<News> newsList) throws IOException {

        long startTime = System.currentTimeMillis();//记录索引开始时间

        Analyzer analyzer = new SmartChineseAnalyzer();
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(directory, config);

        for(News news: newsList){
            Document doc = new Document();
            doc.add(new IntField("id", (int) news.getId(),Field.Store.YES));
            doc.add(new TextField("title", news.getTitle(), Field.Store.YES));
            doc.add(new TextField("content", news.getContent(), Field.Store.YES));
            indexWriter.addDocument(doc);
        }

        indexWriter.commit();
        System.out.println("共索引了"+indexWriter.numDocs()+"篇文章");
        indexWriter.close();
        System.out.println("创建索引所用时间："+(System.currentTimeMillis()-startTime)+"毫秒");
    }
}