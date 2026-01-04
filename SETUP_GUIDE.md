# Setup Guide - Fix Java Version and Database Connection

## Current Issue

Your system has:
- ✅ Java 21 installed at: `C:\Program Files\Java\jdk-21`
- ❌ JAVA_HOME pointing to: `C:\Program Files\Java\jdk-17`
- ⚠️ Maven is using Java 17 (from JAVA_HOME) to compile Java 21 code

## Solution Options

Choose ONE of the following solutions:

---

## Option 1: Update JAVA_HOME (Recommended)

This makes Java 21 the default for all Java projects.

### Windows PowerShell (Temporary - Current Session Only):
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
$env:PATH = "C:\Program Files\Java\jdk-21\bin;" + $env:PATH

# Verify
java -version
echo $env:JAVA_HOME

# Now run the application
cd D:\projects\java\ember-api
.\mvnw.cmd spring-boot:run
```

### Windows PowerShell (Permanent):
```powershell
# Set JAVA_HOME permanently (requires admin rights)
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "User")
[System.Environment]::SetEnvironmentVariable("PATH", "C:\Program Files\Java\jdk-21\bin;" + $env:PATH, "User")

# Restart PowerShell, then verify
java -version
echo $env:JAVA_HOME
```

### Windows GUI Method (Permanent):
1. Open **System Properties** → **Advanced** → **Environment Variables**
2. Under "User variables" or "System variables":
   - Find `JAVA_HOME`
   - Click **Edit**
   - Change value to: `C:\Program Files\Java\jdk-21`
3. Find `Path` variable:
   - Click **Edit**
   - Make sure `%JAVA_HOME%\bin` is near the top
4. Click **OK** on all windows
5. **Restart your terminal/IDE**
6. Verify: `java -version` should show `21.0.9`

---

## Option 2: Change Project to Use Java 17

If you prefer to keep using Java 17 (maybe other projects need it).

### Update pom.xml:
```xml
<!-- Change this line in pom.xml -->
<java.version>17</java.version>  <!-- Changed from 21 to 17 -->
```

**Note:** Spring Boot 4.0.1 works fine with Java 17.

### Steps:
1. Open `pom.xml`
2. Find line 30: `<java.version>21</java.version>`
3. Change to: `<java.version>17</java.version>`
4. Save file
5. Run: `.\mvnw.cmd spring-boot:run`

---

## After Fixing Java Version

Once Java is configured correctly, test the database connection:

### Step 1: Ensure SQL Server is Running

```powershell
# Check if SQL Server service is running
Get-Service | Where-Object {$_.Name -like "*SQL*"}

# Start SQL Server if needed (requires admin)
Start-Service MSSQLSERVER
```

### Step 2: Create Database

Connect to SQL Server and run:
```sql
CREATE DATABASE lilknig_ember;
```

Or use SQL Server Management Studio (SSMS):
1. Open SSMS
2. Connect to `localhost`
3. Right-click "Databases" → "New Database"
4. Name: `lilknig_ember`
5. Click OK

### Step 3: Verify Database Configuration

Check `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=lilknig_ember;encrypt=false
spring.datasource.username=lilknig_admin
spring.datasource.password=admin
spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver
```

**IMPORTANT:** Update username and password to match your SQL Server credentials!

### Step 4: Run the Application

```powershell
cd D:\projects\java\ember-api
.\mvnw.cmd spring-boot:run
```

### Step 5: Check for Success

You should see:
```
Started EmberApiApplication in X.XXX seconds
```

And in the logs, you'll see:
```
Hibernate: create table users (...)
```

This means:
- ✅ Application started successfully
- ✅ Connected to database
- ✅ Created `users` table automatically

---

## Common SQL Server Issues

### Issue 1: "Cannot connect to localhost:1433"

**Solution:**
- Ensure SQL Server is running
- Check if port 1433 is open:
  ```powershell
  Test-NetConnection -ComputerName localhost -Port 1433
  ```

### Issue 2: "Login failed for user 'lilknig_admin'"

**Solution:**
Update credentials in `application.properties` to match your SQL Server:
```properties
spring.datasource.username=YOUR_SQL_USERNAME
spring.datasource.password=YOUR_SQL_PASSWORD
```

### Issue 3: Don't have SQL Server?

**Option A: Install SQL Server Express (Free)**
1. Download: https://www.microsoft.com/en-us/sql-server/sql-server-downloads
2. Install SQL Server Express
3. Install SQL Server Management Studio (SSMS)

**Option B: Use H2 Database (In-Memory, No Installation)**

Update `application.properties`:
```properties
# Comment out SQL Server config
#spring.datasource.url=jdbc:sqlserver://localhost:1433;databaseName=lilknig_ember
#spring.datasource.username=lilknig_admin
#spring.datasource.password=admin
#spring.datasource.driver-class-name=com.microsoft.sqlserver.jdbc.SQLServerDriver

# Add H2 config
spring.datasource.url=jdbc:h2:mem:emberdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect

# Enable H2 Console (optional)
spring.h2.console.enabled=true
```

Then access H2 console at: http://localhost:8080/h2-console

---

## Test API Endpoints

Once the application is running:

### Test 1: Register User
```powershell
curl -X POST http://localhost:8080/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    \"email\": \"test@example.com\",
    \"password\": \"password123\",
    \"name\": \"Test User\"
  }'
```

### Test 2: Login
```powershell
curl -X POST http://localhost:8080/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    \"email\": \"test@example.com\",
    \"password\": \"password123\"
  }'
```

### Test 3: Check Database

In SSMS or SQL Server query:
```sql
USE lilknig_ember;
SELECT * FROM users;
```

You should see the registered user!

---

## Quick Start Summary

```powershell
# 1. Fix Java version (choose one option above)
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# 2. Ensure SQL Server is running
Get-Service MSSQLSERVER

# 3. Create database (in SSMS or sqlcmd)
CREATE DATABASE lilknig_ember;

# 4. Update credentials in application.properties
# (if needed)

# 5. Run application
cd D:\projects\java\ember-api
.\mvnw.cmd spring-boot:run

# 6. Test endpoint
curl -X POST http://localhost:8080/auth/register `
  -H "Content-Type: application/json" `
  -d '{\"email\":\"test@test.com\",\"password\":\"test123\",\"name\":\"Test\"}'
```

---

## Troubleshooting

### Application won't start?
1. Check Java version: `java -version` (should be 21 or 17)
2. Check JAVA_HOME: `echo $env:JAVA_HOME`
3. Check SQL Server is running
4. Check logs for specific error

### Still getting "release version 21 not supported"?
- Restart your terminal after changing JAVA_HOME
- Or use Option 2 (change to Java 17)

### Need Help?
Check the logs when running `.\mvnw.cmd spring-boot:run` - they show exactly what went wrong!

---

**You're almost there! Follow Option 1 or Option 2 above and your application will connect to the database successfully! 🚀**
