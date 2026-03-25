package com.crimeaware.crimeawareness.repository;

import com.crimeaware.crimeawareness.entity.News;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NewsRepository extends JpaRepository<News, Long> {

    List<News> findByQuery(String query);

}