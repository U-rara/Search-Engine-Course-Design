package cn.khala.datadisplay.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Paths;
import java.util.*;

public class NewsSearch {
    private final static String INDEX_DIR = "./data/LuceneIndex";

    public static Map<Integer, String> searchContent(String queryStr) throws IOException, ParseException {
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIR)); //实例化Directory
        DirectoryReader reader = DirectoryReader.open(directory); //读取Directory
        IndexSearcher searcher = new IndexSearcher(reader); //实例化IndexSearcher
        Analyzer analyzer = new SmartChineseAnalyzer(); //中文搜索
        QueryParser parser = new QueryParser("content", analyzer);
        Query query = parser.parse(queryStr);  //解析搜索内容

        long startTime = System.currentTimeMillis();
        TopDocs docs = searcher.search(query, 10); //返回TopN结果

        System.out.println("查找" + queryStr + "所用时间：" + (System.currentTimeMillis() - startTime));
        System.out.println("查询到" + docs.totalHits + "条记录");

        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter("<b><font color=red>", "</font></b>");
        QueryScorer scorer = new QueryScorer(query); //计算查询结果最高的得分
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer); //根据得分算出一个片段
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);
        highlighter.setTextFragmenter(fragmenter); //设置显示高亮的片段

        //遍历查询结果
        Map<Integer, String> resNews = new HashMap<>();
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            String content = doc.get("content");
            Integer id = Integer.parseInt(doc.get("id"));
            if (content != null) {
                TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(content));
                String summary = null;
                try {
                    summary = highlighter.getBestFragment(tokenStream, content);
                } catch (InvalidTokenOffsetsException e) {
                    e.printStackTrace();
                }
                summary += "<br/><b>Score：" + scoreDoc.score + "</b>";
                resNews.put(id, summary);
            }
        }
        reader.close();
        return resNews;
    }
}
