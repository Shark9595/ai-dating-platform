package com.dating.datingsystem.repository;

import com.dating.datingsystem.entity.GiftRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GiftRecordRepository extends JpaRepository<GiftRecord, Long>, JpaSpecificationExecutor<GiftRecord> {
    List<GiftRecord> findBySenderIdOrReceiverIdOrderByCreateTimeDesc(Long senderId, Long receiverId);
}
