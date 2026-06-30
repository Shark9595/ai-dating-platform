package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.VipPackage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VipPackageRepository extends JpaRepository<VipPackage, Long>, JpaSpecificationExecutor<VipPackage> {
    List<VipPackage> findByStatusOrderBySortAsc(Integer status);
}
