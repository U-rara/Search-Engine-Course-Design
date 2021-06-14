package cn.khala.datadisplay.repository;


import cn.khala.datadisplay.model.NewsInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsInfoRepository extends JpaRepository<NewsInfo,Integer> {
    NewsInfo findById(int id);
}
