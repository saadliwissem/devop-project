pipeline {
    agent any
    
    tools {
        maven 'Maven3'
        jdk 'jdk17'
    }
    
    environment {
        // GitHub
        GIT_REPO = 'https://github.com/saadliwissem/devop-project.git'
        GIT_BRANCH = 'main'
        
        // Docker
        DOCKER_IMAGE = 'saadli/devops-spring'
        DOCKER_TAG = "${BUILD_NUMBER}"
        
        // SonarQube
        SONAR_HOST_URL = 'http://sonarqube:9000'
        
        // Nexus
        NEXUS_URL = 'http://nexus:8081'
        NEXUS_CREDENTIALS = credentials('nexus-credentials')

        DOCKER_USERNAME = "saadli"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: "${GIT_BRANCH}",
                    url: "${GIT_REPO}",
                    credentialsId: 'github-credentials'
            }
        }
        
        stage('Unit Tests') {
            steps {
                sh 'mvn clean test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn sonar:sonar'
                }
            }
        }
        
        stage('Build & Package') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }
        
        stage('Deploy to Nexus') {
            steps {
                sh '''
                    mvn deploy:deploy-file \
                        -Dfile=target/demo-0.0.1-SNAPSHOT.jar \
                        -DgroupId=com.devops \
                        -DartifactId=demo \
                        -Dversion=1.0.${BUILD_NUMBER} \
                        -Dpackaging=jar \
                        -Durl=${NEXUS_URL}/repository/maven-releases/ \
                        -DrepositoryId=nexus \
                        -DserverId=nexus
                '''
            }
        }
        
        stage('Build Docker Image') {
            steps {
                script {
                    docker.build("${DOCKER_IMAGE}:${DOCKER_TAG}")
                }
            }
        }
        
        stage('Push to Docker Hub') {
            steps {
                script {
                    docker.withRegistry('', 'docker-hub-credentials') {
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push()
                        docker.image("${DOCKER_IMAGE}:${DOCKER_TAG}").push('latest')
                    }
                }
            }
        }
        
        stage('Deploy with Docker Compose') {
            steps {
                sh '''
                    export DOCKER_IMAGE=${DOCKER_IMAGE}
                    export DOCKER_TAG=${DOCKER_TAG}
                    docker-compose down
                    docker-compose up -d
                '''
            }
        }
    }
    
    post {
        success {
            echo 'Pipeline completed successfully!'
            // Optional email notification
            emailext (
                to: 'saadliwissem88@gmail.com',
                subject: "Pipeline Success: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "The pipeline completed successfully.\nCheck: ${env.BUILD_URL}"
            )
        }
        failure {
            echo 'Pipeline failed!'
            emailext (
                to: 'saadliwissem88@gmail.com',
                subject: "Pipeline Failed: ${env.JOB_NAME} - ${env.BUILD_NUMBER}",
                body: "The pipeline failed. Check logs at: ${env.BUILD_URL}"
            )
        }
    }
}