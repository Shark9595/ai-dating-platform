package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, Long>, JpaSpecificationExecutor<SensitiveWord> {
    List<SensitiveWord> findByStatus(Integer status);
}
