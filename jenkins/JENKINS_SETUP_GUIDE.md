# Jenkins CI Setup Guide — Factory Safety Incident Tracker

## Step 1: Start Jenkins

Double-click `start-jenkins.bat` inside the `/jenkins/` folder, OR run from PowerShell:
```powershell
cd M:\Neha\Desktop\Devops\FactorySafetyIncidentTracker\jenkins
.\start-jenkins.bat
```

Jenkins will start on `http://localhost:8082/jenkins`.  
Wait for the console output to say: `Jenkins is fully up and running`.

---

## Step 2: Initial Unlock

1. Open `http://localhost:8082/jenkins` in your browser.
2. Jenkins will show an **Unlock Jenkins** screen.
3. The initial password is printed in the console output, look for:
   ```
   Jenkins initial setup is required. An admin user has been created and a password generated.
   *************************************************************
   Jenkins initial setup is required...
   Please use the following password to proceed to installation:
   
   <YOUR_PASSWORD_HERE>
   ```
   It is also saved at:  
   `jenkins\jenkins_home\secrets\initialAdminPassword`
4. Paste the password and click **Continue**.

---

## Step 3: Install Suggested Plugins

- On the **Customize Jenkins** screen, click **Install suggested plugins**.
- Wait for all plugins to install (takes 2–5 minutes on first run).

---

## Step 4: Create Admin User

- Fill in your admin username, password, full name, and email address.
- Click **Save and Continue**, then **Save and Finish**.

---

## Step 5: Configure JDK and Maven in Jenkins

1. Go to **Manage Jenkins → Tools**.
2. Under **JDK installations**, click **Add JDK**:
   - **Name:** `JDK-17`
   - **JAVA_HOME:** `C:\Program Files\Java\jdk-17` (adjust to your actual JDK path)
3. Under **Maven installations**, click **Add Maven**:
   - **Name:** `Maven-3`
   - Check **Install automatically** and select the latest Maven 3.x version.
4. Click **Save**.

---

## Step 6: Create a Freestyle CI Job

1. On the Jenkins dashboard, click **New Item**.
2. Enter name: `factory-safety-incident-tracker`.
3. Select **Maven project** or **Freestyle project**, click **OK**.
4. Under **Source Code Management**:
   - Select **None** (we use local project path for now).
5. Under **Build Steps**, click **Add build step → Invoke top-level Maven targets**:
   - **Maven Version:** `Maven-3`
   - **Goals:** `clean package`
   - **POM:** `M:\Neha\Desktop\Devops\FactorySafetyIncidentTracker\pom.xml`
6. Under **Post-build Actions**, click **Add post-build action → Archive the artifacts**:
   - **Files to archive:** `**/target/*.jar`
7. Click **Save**.

---

## Step 7: Run the Build

1. Click **Build Now** on the job's page.
2. Click the build number under **Build History** to view the Console Output.
3. Verify the build ends with `BUILD SUCCESS`.
4. The `*.jar` file should now appear under **Last Successful Artifacts** on the job page.

---

## Week 8 Preview — Pipeline as Code (Jenkinsfile)

In Week 8, we will convert this Freestyle job into a Pipeline job that reads the `Jenkinsfile` from the repository. The `Jenkinsfile` has already been prepared at:
```
M:\Neha\Desktop\Devops\FactorySafetyIncidentTracker\jenkins\Jenkinsfile
```
