# 🚀 Deploying Factory Safety Incident Tracker on Render.com

This guide provides end-to-end instructions for hosting the **Factory Safety Incident Tracker** on [Render.com](https://render.com) (Free Tier compatible).

---

## 🌟 Why Render is Ideal for this Application
1. **Free Automatic HTTPS (`https://<app>.onrender.com`)**: 
   - Browsers **require HTTPS** to grant camera access (`navigator.mediaDevices.getUserMedia`). Deploying on Render gives you automatic SSL, allowing live photo capture to work seamlessly on any mobile phone, tablet, or laptop.
2. **Cloudinary Cloud Integration**:
   - Camera snapshots and uploaded evidence are hosted directly on Cloudinary CDN, surviving dyno restarts.
3. **Docker Multi-Stage Optimization**:
   - The included [`Dockerfile`](../Dockerfile) packages a lightweight Alpine JRE 17 runtime tuned with container memory limits (`MaxRAMPercentage=75%`) to run reliably within Render's 512MB RAM free tier.

---

## Option 1: 1-Click Blueprint Deployment (Recommended)

1. **Log in to Render**: Go to [dashboard.render.com](https://dashboard.render.com/).
2. Click **New +** in the top navigation bar and select **Blueprint**.
3. Connect your GitHub repository:
   - Repository: `https://github.com/VengurlekarMayuresh/FactorySafetyIncidentTracker`
   - Branch: `develop` (or `master`)
4. Render will automatically read [`render.yaml`](../render.yaml) from your repository root and configure:
   - **Service Name**: `factory-safety-tracker`
   - **Runtime**: `Docker`
   - **Plan**: `Free`
   - **Health Check Path**: `/incidents`
   - **Environment Variables**: `PORT`, Cloudinary credentials.
5. Click **Apply**.
6. Render will automatically build the container and deploy your service!

---

## Option 2: Manual Web Service Deployment

If you prefer setting up the Web Service manually via Render UI:

### Step 1: Create New Web Service
1. In Render Dashboard, click **New +** &rarr; **Web Service**.
2. Select **Build and deploy from a Git repository**.
3. Choose `VengurlekarMayuresh/FactorySafetyIncidentTracker` and click **Connect**.

### Step 2: Configure Service Details
Fill in the following settings:

| Setting | Value |
| :--- | :--- |
| **Name** | `factory-safety-tracker` (or your choice) |
| **Region** | `Oregon (US West)` or `Frankfurt (EU Central)` |
| **Branch** | `develop` (or `master`) |
| **Root Directory** | *(leave blank)* |
| **Runtime** | **Docker** |
| **Instance Type** | **Free** ($0/month) |

### Step 3: Advanced Settings (Environment Variables)
Scroll down to **Advanced** &rarr; **Add Environment Variable**:

| Key | Value | Purpose |
| :--- | :--- | :--- |
| `PORT` | `8080` | Port for Spring Boot Web Server |
| `CLOUDINARY_CLOUD_NAME` | `ds20dwlrs` | Cloudinary Cloud Name |
| `CLOUDINARY_API_KEY` | `352138977813842` | Cloudinary API Key |
| `CLOUDINARY_API_SECRET` | `6E-paKyWXggniEdu_AQS97IJJcA` | Cloudinary API Secret |

### Step 4: Health Check Path
Under **Health Check Path**, enter:
```
/incidents
```

### Step 5: Deploy
Click **Create Web Service**. 
Render will begin building the Docker image. Once complete, you will see:
```text
==> Your service is live 🎉 at https://factory-safety-tracker-xxxx.onrender.com
```

---

## 👥 Default Login Credentials

Once your app is live, test both roles:

| Role | Username | Password | Features Accessible |
| :--- | :--- | :--- | :--- |
| **Admin / Safety Officer** | `admin` | `admin123` | View all incidents, change status (OPEN &rarr; IN_PROGRESS &rarr; CLOSED), take resolution proof photos |
| **Worker / Reporter** | `worker1` | `worker123` | Log new safety incidents with camera / file upload, track status, view notifications |

---

## 🛠️ Verifying Live Features
1. Open `https://<your-render-url>/login`.
2. Login as `worker1` / `worker123`.
3. Click **+ Log New Incident**.
4. Test **Live Camera Capture**:
   - Click "Enable Camera".
   - Grant browser camera permission.
   - Click "Capture Photo".
   - Submit the incident report.
5. Log out and login as `admin` / `admin123`.
6. Open the newly logged incident, change status to `RESOLVED` / `CLOSED`, capture work-done proof photo, and save.
