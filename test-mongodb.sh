#!/bin/bash
# Test MongoDB Connection
echo "🔍 Testing MongoDB Connection..."

# Set your MongoDB Atlas URI (replace <db_password> with your actual password)
export MONGO_URI="mongodb+srv://basit:YOUR_PASSWORD_HERE@cluster0.ms5lqsu.mongodb.net/?appName=Cluster0"
export MONGO_DB="ecommerce_reviews"

echo ""
echo "📋 Configuration:"
echo "  Database: $MONGO_DB"
echo "  URI: ${MONGO_URI:0:30}..."
echo ""

# Compile and run the test
mvn compile -q && mvn exec:java -Dexec.mainClass="org.commerce.MongoDBTest" -q
