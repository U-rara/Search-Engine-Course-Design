package cn.khala.datadisplay.web;

import cn.khala.datadisplay.model.News;
import cn.khala.datadisplay.service.NewsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.List;

@Controller
public class NewsController {

    @Resource
    NewsService newsService;

    @RequestMapping("/")
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
}
