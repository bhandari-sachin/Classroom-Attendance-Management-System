pipeline {
    agent any
    tools {
        maven 'Maven3'
        jdk   'JDK21'
    }
    environment {

        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        BACKEND_IMAGE_REPO       = 'sachinbhandari/classroom-attendance-backend'
        FRONTEND_IMAGE_REPO      = 'sachinbhandari/classroom-attendance-frontend'
        DOCKER_IMAGE_TAG         = "build-${env.BUILD_NUMBER}"
        DOCKER_IMAGE_TAG_LATEST  = 'latest'
        SONAR_PROJECT_KEY        = 'classroom_attendance_management'
        SONAR_PROJECT_NAME       = 'Classroom Attendance Management'
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build & Test') {
            steps {
                bat 'mvn -B clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        stage('Generate Coverage Report') {
            steps {
                bat 'mvn -B jacoco:report'
            }
        }
        stage('Publish Coverage Report') {
            steps {
                recordCoverage(
                    tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                    id: 'jacoco',
                    name: 'JaCoCo Coverage',
                    sourceCodeRetention: 'EVERY_BUILD'
                )
            }
        }
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQubeServer') {
                    bat """
                        ${tool 'SonarScanner'}\\bin\\sonar-scanner ^
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} ^
                        -Dsonar.projectName="${SONAR_PROJECT_NAME}" ^
                        -Dsonar.sources=src/main/java ^
                        -Dsonar.tests=src/test/java ^
                        -Dsonar.java.binaries=target/classes ^
                        -Dsonar.java.test.binaries=target/test-classes ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml ^
                        -Dsonar.sourceEncoding=UTF-8
                    """
                }
            }
        }
        /*
        OPTIONAL
        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }
        */
        stage('Build Backend Docker Image') {
            steps {
                bat """
                    docker build ^
                        -f backend\\backend.dockerfile ^
                        -t %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG% ^
                        -t %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% .
                """
            }
        }
        stage('Build Frontend Docker Image') {
            steps {
                bat """
                    docker build ^
                        -f frontend\\frontend.dockerfile ^
                        -t %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG% ^
                        -t %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% .
                """
            }
        }
        stage('Push Docker Images to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                        docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                        docker push %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                        docker push %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST%
                        docker push %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                        docker push %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST%
                        docker logout
                    """
                }
            }
        }
        stage('Deploy with Docker Compose') {
            steps {
                bat 'docker compose down'
                bat 'docker compose pull'
                bat 'docker compose up -d'
            }
        }
    }
    post {
        success {
            echo "Pipeline succeeded! Backend: ${BACKEND_IMAGE_REPO}:${DOCKER_IMAGE_TAG}"
            echo "Pipeline succeeded! Frontend: ${FRONTEND_IMAGE_REPO}:${DOCKER_IMAGE_TAG}"
        }
        failure {
            echo "Pipeline failed. Check logs above."
        }
        always {
            cleanWs()
        }
    }
}