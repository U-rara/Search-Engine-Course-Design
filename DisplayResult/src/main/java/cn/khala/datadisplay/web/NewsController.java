package cn.khala.datadisplay.web;

import cn.khala.datadisplay.model.News;
import cn.khala.datadisplay.service.NewsIndex;
import cn.khala.datadisplay.service.NewsSearch;
import cn.khala.datadisplay.service.NewsService;
import javafx.util.Pair;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class NewsController {

    @Resource
    NewsService newsService;

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/loading")
    public String loading() throws IOException {
        new Thread(new Runnable() {
            public void run() {
                try {
                    // using the Runtime exec method:
                    String[] arguments = new String[]{"python", "./main.py"};
                    Process p = Runtime.getRuntime().exec(arguments);
                    BufferedReader stdInput = new BufferedReader(
                            new InputStreamReader(p.getInputStream()));
                    BufferedReader stdError = new BufferedReader(new
                            InputStreamReader(p.getErrorStream()));
                    // read the output from the command
                    System.out.println("Here is the standard output of the command:\n");
                    String line = null;
                    while ((line = stdInput.readLine()) != null) {
                        System.out.println(line);
                    }
                    // read any errors from the attempted command
                    System.out.println("\nHere is the standard error of the command (if any):\n");
                    String errline = null;
                    while ((errline = stdError.readLine()) != null) {
                        System.out.println(errline);
                    }
                    stdInput.close();
                    stdError.close();
                    int ret = p.waitFor();
                    System.out.println("Process exit code: " + ret);
                    if (ret != 0) {
                        System.out.println("do something");
                    }
//            System.exit(0);
                } catch (
                        Exception e) {
                    System.out.println("exception happened - here's what I know: ");
                    e.printStackTrace();
                    System.exit(-1);
                }
            }
        }).start();
        return "loading";
    }

    @RequestMapping("/showResult")
    public String list(Model model) {
        List<News> newsList = newsService.getNewsList();
        model.addAttribute("News", newsList);
        return "list";
    }

    @RequestMapping("/showContent")
    public String showContent(int id, Model model) {
        News news = newsService.findNewsById(id);
        model.addAttribute("news", news);
        return "showContent";
    }

    @RequestMapping("/search/{searchWord}")
    public String Search(Model model, @PathVariable String searchWord) {

        Map<Integer,String> resNews = null;
        List<Pair<News,String>> newsList =new ArrayList<>();

        try {
            resNews = NewsSearch.searchContent(searchWord);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        assert resNews != null;
        for (Integer id : resNews.keySet()) {
            News news = newsService.findNewsById(id);
            newsList.add(new Pair<>(news,resNews.get(id)));
        }
        model.addAttribute("News", newsList);
        model.addAttribute("searchWord", searchWord);
        return "searchResultList";
    }
}
