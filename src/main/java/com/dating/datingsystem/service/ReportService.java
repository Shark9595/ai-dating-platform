package com.dating.datingsystem.service;

import com.dating.datingsystem.entity.Blacklist;
import com.dating.datingsystem.entity.ReportRecord;
import com.dating.datingsystem.entity.User;
import com.dating.datingsystem.repository.BlacklistRepository;
import com.dating.datingsystem.repository.ReportRecordRepository;
import com.dating.datingsystem.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private ReportRecordRepository reportRecordRepository;

    @Autowired
    private BlacklistRepository blacklistRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public ReportRecord submitReport(Long reporterId, Long reportedId, Integer reportType,
                                     Integer targetType, Long targetId, String reason, String evidence) {
        ReportRecord report = new ReportRecord();
        report.setReporterId(reporterId);
        report.setReportedId(reportedId);
        report.setReportType(reportType);
        report.setTargetType(targetType);
        report.setTargetId(targetId);
        report.setReason(reason);
        report.setEvidence(evidence);
        return reportRecordRepository.save(report);
    }

    public List<Map<String, Object>> getMyReports(Long userId) {
        List<ReportRecord> records = reportRecordRepository.findByReporterIdOrderByCreateTimeDesc(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (ReportRecord record : records) {
            Map<String, Object> item = new HashMap<>();
            item.put("record", record);
            User reportedUser = userRepository.findById(record.getReportedId()).orElse(null);
            item.put("reportedUser", reportedUser);
            result.add(item);
        }
        return result;
    }

    @Transactional
    public void blockUser(Long userId, Long blackedUserId) {
        if (blacklistRepository.existsByUserIdAndBlackedUserId(userId, blackedUserId)) {
            return;
        }
        Blacklist blacklist = new Blacklist();
        blacklist.setUserId(userId);
        blacklist.setBlackedUserId(blackedUserId);
        blacklistRepository.save(blacklist);
    }

    @Transactional
    public void unblockUser(Long userId, Long blackedUserId) {
        blacklistRepository.findByUserIdAndBlackedUserId(userId, blackedUserId)
                .ifPresent(blacklistRepository::delete);
    }

    public List<Map<String, Object>> getBlacklist(Long userId) {
        List<Blacklist> blacklists = blacklistRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Blacklist blacklist : blacklists) {
            Map<String, Object> item = new HashMap<>();
            item.put("blacklist", blacklist);
            User blackedUser = userRepository.findById(blacklist.getBlackedUserId()).orElse(null);
            item.put("blackedUser", blackedUser);
            result.add(item);
        }
        return result;
    }

    public boolean isBlocked(Long userId, Long targetUserId) {
        return blacklistRepository.existsByUserIdAndBlackedUserId(targetUserId, userId);
    }
}
