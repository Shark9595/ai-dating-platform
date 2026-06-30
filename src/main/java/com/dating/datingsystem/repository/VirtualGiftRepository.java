package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.VirtualGift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VirtualGiftRepository extends JpaRepository<VirtualGift, Long>, JpaSpecificationExecutor<VirtualGift> {
    List<VirtualGift> findByStatusOrderBySortAsc(Integer status);
}
