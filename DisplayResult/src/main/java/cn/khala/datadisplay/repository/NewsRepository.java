package cn.khala.datadisplay.repository;

import cn.khala.datadisplay.model.News;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsRepository extends JpaRepository<News,Integer> {
    News findById(int id);

}
