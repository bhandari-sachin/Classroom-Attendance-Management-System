pipeline {
    agent any

    environment {
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"

        DOCKERHUB_CREDENTIALS_ID = '11de06b8-c29b-4e4c-bf92-2d6a8d92868e'

        BACKEND_IMAGE_REPO  = 'sachinbhandari/classroom-attendance-backend'
        FRONTEND_IMAGE_REPO = 'sachinbhandari/classroom-attendance-frontend'

        DOCKER_IMAGE_TAG_LATEST = 'latest'
        DOCKER_IMAGE_TAG_BUILD  = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Check Docker') {
            steps {
                bat 'docker --version'
                bat 'docker info'
            }
        }

        stage('Checkout') {
            steps {
                git branch: 'fullstack',
                    url: 'https://github.com/bhandari-sachin/Classroom-Attendance-Management-System.git'
            }
        }

        stage('Build and Test') {
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

        stage('Build Backend Docker Image') {
            steps {
                bat """
                    docker build ^
                      -f backend\\backend.dockerfile ^
                      -t %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_BUILD% ^
                      -t %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% ^
                      .
                """
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                bat """
                    docker build ^
                      -f frontend\\frontend.dockerfile ^
                      -t %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_BUILD% ^
                      -t %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% ^
                      .
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
                        echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin

                        docker push %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_BUILD%
                        docker push %BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST%

                        docker push %FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_BUILD%
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
        always {
            echo "Pipeline finished: ${currentBuild.currentResult}"
        }
    }
}