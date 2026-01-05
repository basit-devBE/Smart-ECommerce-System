package org.commerce.repositories;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.commerce.config.MongoDBConfig;
import org.commerce.entities.ActivityLog;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;

/**
 * Repository for Activity Log operations in MongoDB
 */
public class ActivityLogRepository {
    private final MongoCollection<Document> collection;
    
    public ActivityLogRepository() {
        if (MongoDBConfig.isConnected()) {
            this.collection = MongoDBConfig.getDatabase().getCollection("activity_logs");
            createIndexes();
        } else {
            this.collection = null;
        }
    }
    
    private void createIndexes() {
        if (collection != null) {
            collection.createIndex(new Document("userId", 1));
            collection.createIndex(new Document("timestamp", -1));
            collection.createIndex(new Document("action", 1));
            collection.createIndex(new Document("entityType", 1));
        }
    }
    
    /**
     * Logs an activity
     */
    public ActivityLog log(ActivityLog activityLog) {
        if (collection == null) return null;
        
        Document doc = new Document()
            .append("userId", activityLog.getUserId())
            .append("userName", activityLog.getUserName())
            .append("action", activityLog.getAction())
            .append("timestamp", activityLog.getTimestamp());
        
        if (activityLog.getEntityType() != null) {
            doc.append("entityType", activityLog.getEntityType());
        }
        if (activityLog.getEntityId() != null) {
            doc.append("entityId", activityLog.getEntityId());
        }
        if (activityLog.getMetadata() != null && !activityLog.getMetadata().isEmpty()) {
            doc.append("metadata", activityLog.getMetadata());
        }
        
        collection.insertOne(doc);
        activityLog.setId(doc.getObjectId("_id"));
        return activityLog;
    }
    
    /**
     * Gets logs by user
     */
    public List<ActivityLog> findByUserId(int userId) {
        if (collection == null) return new ArrayList<>();
        
        List<ActivityLog> logs = new ArrayList<>();
        collection.find(eq("userId", userId))
            .sort(Sorts.descending("timestamp"))
            .forEach(doc -> logs.add(documentToActivityLog(doc)));
        return logs;
    }
    
    /**
     * Gets logs by action
     */
    public List<ActivityLog> findByAction(String action) {
        if (collection == null) return new ArrayList<>();
        
        List<ActivityLog> logs = new ArrayList<>();
        collection.find(eq("action", action))
            .sort(Sorts.descending("timestamp"))
            .forEach(doc -> logs.add(documentToActivityLog(doc)));
        return logs;
    }
    
    /**
     * Gets logs within a time range
     */
    public List<ActivityLog> findByDateRange(LocalDateTime start, LocalDateTime end) {
        if (collection == null) return new ArrayList<>();
        
        List<ActivityLog> logs = new ArrayList<>();
        collection.find(and(
                gte("timestamp", start),
                lte("timestamp", end)
            ))
            .sort(Sorts.descending("timestamp"))
            .forEach(doc -> logs.add(documentToActivityLog(doc)));
        return logs;
    }
    
    /**
     * Gets recent logs
     */
    public List<ActivityLog> getRecentLogs(int limit) {
        if (collection == null) return new ArrayList<>();
        
        List<ActivityLog> logs = new ArrayList<>();
        collection.find()
            .sort(Sorts.descending("timestamp"))
            .limit(limit)
            .forEach(doc -> logs.add(documentToActivityLog(doc)));
        return logs;
    }
    
    /**
     * Gets user activity count by action
     */
    public long getActivityCount(int userId, String action) {
        if (collection == null) return 0;
        
        return collection.countDocuments(and(
            eq("userId", userId),
            eq("action", action)
        ));
    }
    
    /**
     * Converts MongoDB Document to ActivityLog entity
     */
    private ActivityLog documentToActivityLog(Document doc) {
        ActivityLog log = new ActivityLog();
        log.setId(doc.getObjectId("_id"));
        log.setUserId(doc.getInteger("userId"));
        log.setUserName(doc.getString("userName"));
        log.setAction(doc.getString("action"));
        
        if (doc.containsKey("entityType")) {
            log.setEntityType(doc.getString("entityType"));
        }
        if (doc.containsKey("entityId")) {
            log.setEntityId(doc.getInteger("entityId"));
        }
        if (doc.containsKey("metadata")) {
            log.setMetadata((Map<String, Object>) doc.get("metadata"));
        }
        if (doc.get("timestamp") != null) {
            log.setTimestamp((LocalDateTime) doc.get("timestamp"));
        }
        
        return log;
    }
}
