# Compile and Deploy Script

## For NetBeans (Easiest)
1. Open NetBeans
2. File → New Project → Java Web → Web Application
3. Name: AdminBackend
4. Server: Apache Tomcat
5. Copy AdminStatsServlet.java to src/java/
6. Copy web.xml to web/WEB-INF/
7. Add json library (see README.md)
8. Click Run (Green Play button)

## Manual Deployment (Without NetBeans)

### Step 1: Install Requirements
- Java JDK 8 or higher
- Apache Tomcat 9
- JSON library (json-20230227.jar)

### Step 2: Create Project Structure
```
AdminBackend/
├── WEB-INF/
│   ├── web.xml
│   ├── classes/
│   └── lib/
│       └── json-20230227.jar
└── index.html (optional)
```

### Step 3: Compile Servlet
```bash
# Windows
javac -cp "C:\apache-tomcat\lib\servlet-api.jar;json-20230227.jar" AdminStatsServlet.java

# Linux/Mac
javac -cp /path/to/tomcat/lib/servlet-api.jar:json-20230227.jar AdminStatsServlet.java
```

### Step 4: Create WAR file (Optional)
```bash
# Copy compiled class
mkdir -p WEB-INF/classes
mv AdminStatsServlet.class WEB-INF/classes/

# Create WAR
jar cvf AdminBackend.war *
```

### Step 5: Deploy to Tomcat
**Option A: Auto-deploy**
- Copy `AdminBackend.war` to `tomcat/webapps/`
- Tomcat will auto-extract and deploy

**Option B: Manual deploy**
- Create folder: `tomcat/webapps/AdminBackend/`
- Copy compiled classes to: `AdminBackend/WEB-INF/classes/`
- Copy web.xml to: `AdminBackend/WEB-INF/`
- Copy json jar to: `AdminBackend/WEB-INF/lib/`

### Step 6: Start Tomcat
```bash
# Windows
cd C:\apache-tomcat\bin
startup.bat

# Linux/Mac
cd /path/to/tomcat/bin
./startup.sh
```

### Step 7: Test Servlet
Open browser: http://localhost:8080/AdminBackend/api/admin-stats

You should see JSON response!

## Troubleshooting

### Port 8080 in use?
Edit `tomcat/conf/server.xml`:
```xml
<Connector port="8081" protocol="HTTP/1.1" ... />
```
Then update dashboard.html fetch URL to port 8081

### ClassNotFoundException?
- Make sure servlet-api.jar is in classpath during compilation
- Don't include servlet-api.jar in WEB-INF/lib (Tomcat provides it)

### JSON library error?
Download from: https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar
Place in: WEB-INF/lib/

## Quick Test (Before Frontend)
Use curl or browser:
```bash
curl http://localhost:8080/AdminBackend/api/admin-stats
```

Should return:
```json
{"totalProducts":156,"totalUsers":89,...}
```

## Frontend Configuration
If you changed Tomcat port, update dashboard.html:
```javascript
const response = await fetch('http://localhost:8081/AdminBackend/api/admin-stats', {
    method: 'GET'
});
```
