# Java Servlet Backend Setup Guide

## What This Is
A simple Java Servlet that returns admin statistics as JSON. This demonstrates backend server-side programming using Java Servlets.

## Files Created
- `AdminStatsServlet.java` - Main servlet that handles GET requests
- `WEB-INF/web.xml` - Servlet configuration file

## Setup in NetBeans

### Step 1: Install Apache Tomcat
1. Download Apache Tomcat 9 from: https://tomcat.apache.org/download-90.cgi
2. Extract it to a folder (e.g., `C:\apache-tomcat-9.0.xx`)

### Step 2: Add Tomcat to NetBeans
1. Open NetBeans
2. Go to: **Tools → Servers → Add Server**
3. Select **Apache Tomcat**
4. Browse to your Tomcat installation folder
5. Click **Finish**

### Step 3: Create Web Application in NetBeans
1. **File → New Project → Java Web → Web Application**
2. Project Name: `AdminBackend`
3. Server: Select **Apache Tomcat**
4. Java EE Version: **Jakarta EE 8 Web** or **Java EE 7 Web**
5. Click **Finish**

### Step 4: Add the Servlet
1. Copy `AdminStatsServlet.java` to `src/java/` folder
2. Copy `web.xml` to `web/WEB-INF/` folder (replace if exists)

### Step 5: Add JSON Library
1. Download `json-20230227.jar` from: https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar
2. In NetBeans: Right-click project → **Properties → Libraries**
3. Click **Add JAR/Folder** → Select the downloaded `json-20230227.jar`
4. Click **OK**

### Step 6: Run the Servlet
1. Right-click project → **Clean and Build**
2. Right-click project → **Run**
3. Tomcat will start and deploy your servlet
4. Test at: `http://localhost:8080/AdminBackend/api/admin-stats`

You should see JSON output like:
```json
{
  "totalProducts": 156,
  "totalUsers": 89,
  "totalOrders": 234,
  "revenue": 45670.5,
  "serverTime": 1234567890,
  "serverStatus": "Active",
  "message": "Statistics fetched from Java Servlet Backend"
}
```

## Frontend Integration
The dashboard will automatically call this servlet at:
- URL: `http://localhost:8080/AdminBackend/api/admin-stats`
- Method: GET
- Response: JSON

## Troubleshooting

### Error: "Cannot find servlet-api.jar"
- Make sure you selected Apache Tomcat when creating the project
- Or add servlet-api.jar manually from Tomcat's lib folder

### Error: "Port 8080 already in use"
- Change Tomcat port: In NetBeans, go to Services → Servers → Apache Tomcat → Properties
- Change HTTP Port to 8081 or another free port
- Update the URL in dashboard accordingly

### CORS Error in Browser
- Make sure the servlet has CORS headers (already included)
- Clear browser cache and try again

## What Your Teacher Will See
✅ Java Servlet backend code
✅ web.xml configuration
✅ RESTful API endpoint
✅ JSON response
✅ Frontend calling backend via HTTP
✅ Full-stack integration

## Alternative: Quick Test Without NetBeans
If you just want to test quickly, you can:
1. Compile: `javac -cp servlet-api.jar AdminStatsServlet.java`
2. Deploy to Tomcat/webapps manually
3. Start Tomcat: `bin/startup.bat` (Windows) or `bin/startup.sh` (Linux/Mac)
