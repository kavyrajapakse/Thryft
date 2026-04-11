# Manual Deployment (No NetBeans Required)

## For Low RAM Situations

If NetBeans is too heavy for your system, you can deploy manually.

### What You Need
- Java JDK installed
- GlassFish server installed
- Text editor (Notepad++, VS Code, or even Notepad)

### Step 1: Edit Servlet (if needed)
Use any text editor to edit `AdminStatsServlet.java`

### Step 2: Compile Manually

**Windows:**
```batch
javac -cp "C:\glassfish6\glassfish\lib\javaee.jar;json-20230227.jar" AdminStatsServlet.java
```

**Or use the compile.bat script:**
1. Edit `compile.bat` - set your GlassFish and Java paths
2. Double-click `compile.bat`

### Step 3: Create Webapp Structure
```
AdminBackend/
├── WEB-INF/
│   ├── classes/
│   │   └── AdminStatsServlet.class
│   ├── lib/
│   │   └── json-20230227.jar
│   └── web.xml
```

**Create folders:**
```batch
mkdir AdminBackend
mkdir AdminBackend\WEB-INF
mkdir AdminBackend\WEB-INF\classes
mkdir AdminBackend\WEB-INF\lib
```

**Copy files:**
```batch
copy AdminStatsServlet.class AdminBackend\WEB-INF\classes\
copy json-20230227.jar AdminBackend\WEB-INF\lib\
copy web.xml AdminBackend\WEB-INF\
```

### Step 4: Deploy to GlassFish

**Copy entire AdminBackend folder to:**
```
C:\glassfish6\glassfish\domains\domain1\autodeploy\
```

GlassFish will automatically deploy it!

### Step 5: Start GlassFish

**Windows:**
```batch
cd C:\glassfish6\glassfish\bin
asadmin start-domain
```

### Step 6: Test
Open browser: `http://localhost:8080/AdminBackend/api/admin-stats`

### Step 7: Run Frontend
Just open `index.html` in your browser!

---

## Advantages of Manual Deployment
✅ No IDE needed
✅ Much less RAM usage
✅ Faster on slow computers
✅ Learn deployment process better

## Simple Workflow
1. Edit servlet in Notepad/VS Code
2. Compile with compile.bat
3. Copy .class file to autodeploy
4. GlassFish auto-deploys
5. Open frontend in browser

## Stop GlassFish
```batch
cd C:\glassfish6\glassfish\bin
asadmin stop-domain
```

## Check if GlassFish is Running
```batch
cd C:\glassfish6\glassfish\bin
asadmin list-domains
```

## Undeploy Application
```batch
cd C:\glassfish6\glassfish\bin
asadmin undeploy AdminBackend
```

## Tips for Low RAM
- Close other applications
- Use lightweight text editor
- Don't run IDE and server together
- Use command line tools
- Deploy manually to autodeploy folder

---

## For Your Demo
You can still impress your teacher by:
1. Showing the servlet code (in text editor)
2. Explaining the deployment process
3. Showing it working in browser
4. No need to run heavy IDE!
