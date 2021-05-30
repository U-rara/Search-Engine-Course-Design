package cn.khala.datadisplay.web;

import cn.khala.datadisplay.model.News;
import cn.khala.datadisplay.model.NewsInfo;
import cn.khala.datadisplay.service.*;
import cn.khala.datadisplay.service.impl.NewsServiceImpl;
import javafx.util.Pair;
import org.apache.lucene.queryparser.classic.ParseException;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EnableAsync
@Controller
public class NewsController {

    @Resource
    NewsService newsService;

    @Resource
    NewsInfoService newsInfoService;

    @Bean
    public ApplicationRunner applicationRunner() {
        return applicationArguments -> {
            long startTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + "：开始调用异步业务");
            newsService.getIDF();
            long endTime = System.currentTimeMillis();
            System.out.println(Thread.currentThread().getName() + "：调用异步业务结束，耗时：" + (endTime - startTime));
        };
    }

    @RequestMapping("/")
    public String index() {
        return "index";
    }

    @RequestMapping("/loading")
    public String loading() throws IOException {
        new Thread(() -> {
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
        }).start();
        return "loading";
    }

    @RequestMapping("/showResult")
    public String list(Model model) {
        List<NewsInfo> newsInfoList = newsInfoService.getNewsInfoList();
        model.addAttribute("News", newsInfoList);
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

        Map<Integer, String> resNews = null;
        List<Pair<News, String>> newsList = new ArrayList<>();

        try {
            resNews = NewsSearch.searchContent(searchWord);
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        assert resNews != null;
        for (Integer id : resNews.keySet()) {
            News news = newsService.findNewsById(id);
            newsList.add(new Pair<>(news, resNews.get(id)));
        }
        model.addAttribute("News", newsList);
        model.addAttribute("searchWord", searchWord);
        return "searchResultList";
    }

    @RequestMapping("/showSimilarity/{id1}&{id2}")
    public String showSimilarity(Model model, @PathVariable int id1, @PathVariable int id2) {
        News news1 = newsService.findNewsById(id1);
        News news2 = newsService.findNewsById(id2);
        HashMap<String, Double> idf=new HashMap<>();

        File csv ;
        if (news1.getLanguage().equals("EN")) {
            csv=new File("./data/idf_en.csv");
        } else {
            csv=new File("./data/idf_zh.csv");
        }
        try{
            BufferedReader textFile = new BufferedReader(new FileReader(csv));
            String lineDta = "";

            while ((lineDta = textFile.readLine()) != null){
                if(lineDta.equals("")) continue;
                String[] line=lineDta.split(",");
                idf.put(line[0],Double.parseDouble(line[1]));
            }
            textFile.close();
        }catch (FileNotFoundException e){
            System.out.println("没有找到指定文件");
        }catch (IOException e){
            System.out.println("文件读写出错");
        }

        double similarity = new NewsSimilarity(news1, news2, idf).getCosineDistance();
        similarity = (double) Math.round(similarity * 1000000) / 1000000;
        model.addAttribute("news1", news1);
        model.addAttribute("news2", news2);
        model.addAttribute("similarity", similarity);
        return "showSimilarity";
    }
}
