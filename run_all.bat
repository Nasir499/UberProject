@echo off
echo ========================================================
echo   Launching Uber Microservices Stack (7 Microservices)
echo ========================================================

echo Starting 1/7: UberServiceDiscovery (Eureka :8761)...
start "UberServiceDiscovery" cmd /k "cd UberServiceDiscovery && gradlew.bat bootRun"

echo Waiting 8 seconds for Eureka to initialize...
ping 127.0.0.1 -n 9 > NUL

echo Starting 2/7: UberApiGateway (:8080)...
start "UberApiGateway" cmd /k "cd UberApiGateway && gradlew.bat bootRun"

echo Starting 3/7: UberProject-AuthService (:7474)...
start "UberProject-AuthService" cmd /k "cd UberProject-AuthService && gradlew.bat bootRun"

echo Starting 4/7: UberBookingService (:7777)...
start "UberBookingService" cmd /k "cd UberBookingService && gradlew.bat bootRun"

echo Starting 5/7: UberProject-LocationService (:7478)...
start "UberProject-LocationService" cmd /k "cd UberProject-LocationService && gradlew.bat bootRun"

echo Starting 6/7: UberReviewService (:7475)...
start "UberReviewService" cmd /k "cd UberReviewService && gradlew.bat bootRun"

echo Starting 7/7: ClientSocketService (:8086)...
start "ClientSocketService" cmd /k "cd ClientSocketService && gradlew.bat bootRun"

echo ========================================================
echo   All 7 microservices launched!
echo   Eureka Server: http://localhost:8761
echo   API Gateway:   http://localhost:8080
echo ========================================================
