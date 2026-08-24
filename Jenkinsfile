pipeline {
    agent any

    tools {
        maven 'Maven-3'
        jdk 'JDK-17'
    }

    parameters {
        choice(
            name: 'ENV',
            choices: ['dev', 'staging'],
            description: 'Target deployment environment'
        )
        string(
            name: 'APP_PORT',
            defaultValue: '8080',
            description: 'Port to run the deployed application on'
        )
    }

    environment {
        APP_NAME     = 'incident-tracker'
        JAR_NAME     = 'incident-tracker-0.0.1-SNAPSHOT.jar'
        DEPLOY_DIR   = "C:\\deploy\\${params.ENV}"
        PROPS_FILE   = "src/main/resources/application-${params.ENV}.properties"
    }

    stages {

        stage('Checkout') {
            steps {
                echo "=============================="
                echo " Stage: Checkout"
                echo " Environment: ${params.ENV}"
                echo "=============================="
                // For local build the source is already on disk.
                // For GitHub-connected Jenkins, replace with:
                // git branch: 'develop',
                //     credentialsId: 'github-token',
                //     url: 'https://github.com/<YOU>/factory-safety-incident-tracker.git'
                echo "Source code ready."
            }
        }

        stage('Build') {
            steps {
                echo "=============================="
                echo " Stage: Build (compile)"
                echo "=============================="
                bat './mvnw.cmd clean compile'
            }
        }

        stage('Test') {
            steps {
                echo "=============================="
                echo " Stage: Test"
                echo "=============================="
                bat './mvnw.cmd test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
                failure {
                    echo "Tests FAILED — blocking deployment!"
                }
            }
        }

        stage('Package') {
            steps {
                echo "=============================="
                echo " Stage: Package JAR"
                echo "=============================="
                bat "./mvnw.cmd package -DskipTests -Dspring.profiles.active=${params.ENV}"
            }
            post {
                success {
                    archiveArtifacts artifacts: "**/target/${env.JAR_NAME}", fingerprint: true
                    echo "Artifact archived: ${env.JAR_NAME}"
                }
            }
        }

        stage('Deploy') {
            when {
                expression { currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                echo "=============================="
                echo " Stage: Deploy to ${params.ENV}"
                echo " Port: ${params.APP_PORT}"
                echo "=============================="

                // Create deploy directory if it doesn't exist
                bat "if not exist \"${env.DEPLOY_DIR}\" mkdir \"${env.DEPLOY_DIR}\""

                // Copy JAR to deploy directory
                bat "copy /Y target\\${env.JAR_NAME} ${env.DEPLOY_DIR}\\${env.APP_NAME}-${params.ENV}.jar"

                // Stop existing process if running (Windows: find and kill by port)
                bat """
                    for /f \"tokens=5\" %%a in ('netstat -aon ^| findstr :${params.APP_PORT} ^| findstr LISTENING') do (
                        echo Stopping process on port ${params.APP_PORT} PID=%%a
                        taskkill /PID %%a /F 2>nul || echo No process to kill
                    )
                """

                // Start the application in background
                bat """
                    start /B javaw -jar ${env.DEPLOY_DIR}\\${env.APP_NAME}-${params.ENV}.jar ^
                        --server.port=${params.APP_PORT} ^
                        --spring.profiles.active=${params.ENV} ^
                        > ${env.DEPLOY_DIR}\\app.log 2>&1
                """

                echo "Application started at http://localhost:${params.APP_PORT}/incidents"
                echo "Logs: ${env.DEPLOY_DIR}\\app.log"
            }
        }
    }

    post {
        success {
            echo "============================================"
            echo " PIPELINE SUCCESS — Build #${BUILD_NUMBER}"
            echo " App running at: http://localhost:${params.APP_PORT}/incidents"
            echo "============================================"
        }
        failure {
            echo "============================================"
            echo " PIPELINE FAILED — Build #${BUILD_NUMBER}"
            echo " Check console output for details."
            echo "============================================"
        }
        always {
            echo "Pipeline completed for ENV=${params.ENV}, Build=${BUILD_NUMBER}"
        }
    }
}
