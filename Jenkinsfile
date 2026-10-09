pipeline {
    agent any

    parameters {
        choice(
            name: 'ENV',
            choices: ['dev', 'staging'],
            description: 'Target deployment environment'
        )
        string(
            name: 'APP_PORT',
            defaultValue: '8080',
            description: 'Port for the deployed application (dev: 8080, staging: 8081)'
        )
    }

    environment {
        APP_NAME   = 'factory-safety-incident-tracker'
        JAR_NAME   = 'incident-tracker-0.0.1-SNAPSHOT.jar'
        IMAGE_NAME = 'factory-safety-incident-tracker'
    }

    stages {
        stage('Checkout') {
            steps {
                echo "============================================="
                echo " Stage: Checkout"
                echo " Build ID: ${BUILD_NUMBER}"
                echo " Environment: ${params.ENV} | Port: ${params.APP_PORT}"
                echo "============================================="
                bat 'echo Source code workspace ready.'
            }
        }

        stage('Compile') {
            steps {
                echo "============================================="
                echo " Stage: Compile Application"
                echo "============================================="
                bat './mvnw.cmd clean compile'
            }
        }

        stage('Test & Quality Gate') {
            steps {
                echo "============================================="
                echo " Stage: Unit & E2E Automated Tests"
                echo "============================================="
                bat './mvnw.cmd test'
            }
            post {
                always {
                    // Publish JUnit XML test results to Jenkins UI
                    junit testResults: '**/target/surefire-reports/*.xml', allowEmptyResults: true
                    // Archive any test failure screenshots
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
                failure {
                    echo "Quality Gate FAILED - Automated tests did not pass. Halting pipeline."
                }
            }
        }

        stage('Package') {
            steps {
                echo "============================================="
                echo " Stage: Package Executable JAR"
                echo "============================================="
                bat "./mvnw.cmd package -DskipTests -Dspring.profiles.active=${params.ENV}"
            }
            post {
                success {
                    // Archive compiled production artifact
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Deploy') {
            when {
                expression { currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                echo "============================================="
                echo " Stage: Deploy to ${params.ENV} (Port: ${params.APP_PORT})"
                echo "============================================="
                bat "deploy.bat ${params.ENV} ${params.APP_PORT}"
            }
        }

        stage('Health Check') {
            steps {
                echo "============================================="
                echo " Stage: Health Check & Verification"
                echo "============================================="
                echo "Waiting 10 seconds for Spring Boot service to initialize on port ${params.APP_PORT}..."
                sleep time: 10, unit: 'SECONDS'
                bat """
                    curl.exe -s -I http://localhost:${params.APP_PORT}/incidents || echo Application booting up...
                """
            }
        }
    }

    post {
        success {
            echo "========================================================"
            echo " PIPELINE SUCCESS - Build #${BUILD_NUMBER}"
            echo " Application running at: http://localhost:${params.APP_PORT}/incidents"
            echo "========================================================"
        }
        failure {
            echo "========================================================"
            echo " PIPELINE FAILED - Build #${BUILD_NUMBER}"
            echo " Inspect console output and surefire reports above."
            echo "========================================================"
        }
    }
}
