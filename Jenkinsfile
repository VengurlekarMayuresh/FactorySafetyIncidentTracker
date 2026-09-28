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
            description: 'Port to map for the deployed Docker container'
        )
    }

    environment {
        APP_NAME     = 'factory-safety-incident-tracker'
        JAR_NAME     = 'incident-tracker-0.0.1-SNAPSHOT.jar'
        IMAGE_NAME   = "factory-safety-incident-tracker"
    }

    stages {
        stage('Checkout') {
            steps {
                echo "=============================="
                echo " Stage: Checkout"
                echo "=============================="
                // Replace with actual git checkout for real CI
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
                echo " Stage: Selenium & Unit Tests"
                echo "=============================="
                bat './mvnw.cmd test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                    // Archive screenshots if there were any failures
                    archiveArtifacts artifacts: 'target/screenshots/*.png', allowEmptyArchive: true
                }
                failure {
                    echo "Tests FAILED - blocking deployment! Check screenshots in artifacts."
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
        }

        stage('Docker Build') {
            steps {
                echo "=============================="
                echo " Stage: Docker Build"
                echo "=============================="
                bat "docker build -t ${env.IMAGE_NAME}:${BUILD_NUMBER} -t ${env.IMAGE_NAME}:latest ."
            }
        }

        stage('Deploy (Docker)') {
            when {
                expression { currentBuild.currentResult == 'SUCCESS' }
            }
            steps {
                echo "=============================="
                echo " Stage: Deploy Docker Container to ${params.ENV}"
                echo "=============================="

                // Stop and remove existing container if it exists
                bat """
                    docker stop ${env.APP_NAME}-${params.ENV} || echo "No container to stop"
                    docker rm ${env.APP_NAME}-${params.ENV} || echo "No container to remove"
                """

                // Run new container
                bat """
                    docker run -d --name ${env.APP_NAME}-${params.ENV} -p ${params.APP_PORT}:8080 ^
                        -e SPRING_PROFILES_ACTIVE=${params.ENV} ^
                        ${env.IMAGE_NAME}:${BUILD_NUMBER}
                """
            }
        }
    }

    post {
        success {
            echo "============================================"
            echo " PIPELINE SUCCESS - Build #${BUILD_NUMBER}"
            echo " Container deployed to http://localhost:${params.APP_PORT}/incidents"
            echo "============================================"
        }
        failure {
            echo "============================================"
            echo " PIPELINE FAILED - Build #${BUILD_NUMBER}"
            echo "============================================"
        }
    }
}
