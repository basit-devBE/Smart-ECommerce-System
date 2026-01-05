package org.commerce.services;

import org.commerce.common.Result;
import org.commerce.entities.ActivityLog;
import org.commerce.repositories.ActivityLogRepository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for Activity Logging
 */
public class ActivityLogService {
    private final ActivityLogRepository logRepository;
    
    public ActivityLogService() {
        this.logRepository = new ActivityLogRepository();
    }
    
    /**
     * Logs user activity
     */
    public void logActivity(int userId, String userName, String action) {
        try {
            ActivityLog log = new ActivityLog(userId, userName, action);
            logRepository.log(log);
        } catch (Exception e) {
            // Silent fail - logging should not break application
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
    
    /**
     * Logs activity with entity details
     */
    public void logActivity(int userId, String userName, String action, 
                           String entityType, Integer entityId) {
        try {
            ActivityLog log = new ActivityLog(userId, userName, action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            logRepository.log(log);
        } catch (Exception e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
    
    /**
     * Logs activity with full details
     */
    public void logActivity(ActivityLog activityLog) {
        try {
            logRepository.log(activityLog);
        } catch (Exception e) {
            System.err.println("Failed to log activity: " + e.getMessage());
        }
    }
    
    /**
     * Gets user activity logs
     */
    public Result<List<ActivityLog>> getUserLogs(int userId) {
        try {
            List<ActivityLog> logs = logRepository.findByUserId(userId);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.failure("Failed to fetch logs: " + e.getMessage());
        }
    }
    
    /**
     * Gets logs by action type
     */
    public Result<List<ActivityLog>> getLogsByAction(String action) {
        try {
            List<ActivityLog> logs = logRepository.findByAction(action);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.failure("Failed to fetch logs: " + e.getMessage());
        }
    }
    
    /**
     * Gets recent activity logs
     */
    public Result<List<ActivityLog>> getRecentLogs(int limit) {
        try {
            List<ActivityLog> logs = logRepository.getRecentLogs(limit);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.failure("Failed to fetch recent logs: " + e.getMessage());
        }
    }
    
    /**
     * Gets logs within date range
     */
    public Result<List<ActivityLog>> getLogsByDateRange(LocalDateTime start, LocalDateTime end) {
        try {
            List<ActivityLog> logs = logRepository.findByDateRange(start, end);
            return Result.success(logs);
        } catch (Exception e) {
            return Result.failure("Failed to fetch logs: " + e.getMessage());
        }
    }
    
    /**
     * Gets activity count
     */
    public Result<Long> getActivityCount(int userId, String action) {
        try {
            long count = logRepository.getActivityCount(userId, action);
            return Result.success(count);
        } catch (Exception e) {
            return Result.failure("Failed to get activity count: " + e.getMessage());
        }
    }
}
