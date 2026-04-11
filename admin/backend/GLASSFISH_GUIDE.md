# GlassFish Deployment Guide

## Quick Setup in NetBeans with GlassFish

### Prerequisites
✅ NetBeans IDE installed
✅ GlassFish Server configured in NetBeans
✅ Java JDK 8 or higher

### Step-by-Step Instructions

#### 1. Create Web Application
1. Open NetBeans
2. **File → New Project**
3. Select **Java Web → Web Application**
4. Click **Next**

#### 2. Configure Project
- **Project Name**: `AdminBackend`
- **Server**: Select **GlassFish Server**
- **Java EE Version**: Java EE 7 Web or Java EE 8 Web
- **Context Path**: `/AdminBackend`
- Click **Finish**

#### 3. Add Servlet Code
1. Navigate to project's `src/java/` folder
2. Copy `AdminStatsServlet.java` into this folder
3. (Optional) Copy `web.xml` to `web/WEB-INF/`
   - Not required if using `@WebServlet` annotation

#### 4. Add JSON Library
**Download:**
https://repo1.maven.org/maven2/org/json/json/20230227/json-20230227.jar

**Add to Project:**
1. Right-click project → **Properties**
2. Click **Libraries** category
3. Under **Compile** tab, click **Add JAR/Folder**
4. Browse and select `json-20230227.jar`
5. Click **OK**

#### 5. Build and Deploy
1. Right-click project → **Clean and Build**
2. Right-click project → **Run**
3. GlassFish will automatically:
   - Start the server (if not running)
   - Deploy your application
   - Open a browser

#### 6. Test the Servlet
**In Browser, visit:**
```
http://localhost:8080/AdminBackend/api/admin-stats
```

**Expected Response (JSON):**
```json
{
  "totalProducts": 156,
  "totalUsers": 89,
  "totalOrders": 234,
  "revenue": 45670.5,
  "serverTime": 1234567890123,
  "serverStatus": "Active",
  "message": "Statistics fetched from Java Servlet Backend"
}
```

### GlassFish Server Ports

**Default Ports:**
- HTTP: `8080`
- HTTPS: `8181`
- Admin Console: `4848`

**To check your ports:**
1. In NetBeans: **Services** tab
2. Right-click **GlassFish Server**
3. Select **Properties**
4. Check **HTTP Port** value

**If using different port**, update dashboard.html:
```javascript
const response = await fetch('http://localhost:YOUR_PORT/AdminBackend/api/admin-stats', {
    method: 'GET'
});
```

### Access GlassFish Admin Console
```
http://localhost:4848
```
- View deployed applications
- Monitor server status
- Check logs

### Troubleshooting

#### Issue: Port already in use
**Solution:**
1. Services → Servers → GlassFish → Properties
2. Change HTTP Port (e.g., 8081)
3. Restart GlassFish
4. Update dashboard URL

#### Issue: Application not deploying
**Solution:**
1. Clean and Build project
2. Restart GlassFish: Services → GlassFish → Restart
3. Check GlassFish logs in Output window

#### Issue: 404 Not Found
**Check:**
- URL: `http://localhost:8080/AdminBackend/api/admin-stats`
- Context path matches project name
- Servlet mapping is correct

#### Issue: JSON library error
**Solution:**
- Download `json-20230227.jar`
- Add to project Libraries (Compile tab)
- Clean and Build

#### Issue: CORS error in browser
**Already handled** - Servlet includes CORS headers:
```java
response.setHeader("Access-Control-Allow-Origin", "*");
```

### Verify Deployment

**Check in GlassFish Admin Console:**
1. Go to http://localhost:4848
2. Navigate to **Applications**
3. You should see `AdminBackend` listed
4. Status should show green checkmark

### Undeploy Application
If you need to remove:
1. In NetBeans: Services → GlassFish → Applications
2. Right-click `AdminBackend`
3. Select **Undeploy**

### Project Structure
```
AdminBackend/
├── src/
│   └── java/
│       └── AdminStatsServlet.java
├── web/
│   ├── WEB-INF/
│   │   ├── web.xml (optional)
│   │   └── lib/
│   │       └── json-20230227.jar
│   └── index.html (optional)
└── build.xml
```

### Benefits of GlassFish
✅ Full Java EE support
✅ Built-in with NetBeans
✅ Easy deployment
✅ Admin console for monitoring
✅ Better for enterprise applications

### For Your Teacher

**What to say:**
- "I deployed my servlet on GlassFish Server"
- "GlassFish is a full Java EE application server"
- "It provides enterprise-grade features"
- "NetBeans integrates seamlessly with GlassFish"

**Demo Steps:**
1. Show deployed app in GlassFish admin console
2. Open servlet URL in browser (show JSON response)
3. Open dashboard (show frontend calling servlet)
4. Show servlet code (explain the logic)

Good luck! 🚀
