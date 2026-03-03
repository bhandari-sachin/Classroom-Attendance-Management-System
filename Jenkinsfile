pipeline {
    agent any

    environment {
        // Docker CLI path for Windows agents
        PATH = "C:\\Program Files\\Docker\\Docker\\resources\\bin;${env.PATH}"

        // Jenkins Credentials (Username/Password) for Docker Hub
        DOCKERHUB_CREDENTIALS_ID = '11de06b8-c29b-4e4c-bf92-2d6a8d92868e'   // <-- your Jenkins credential ID

        // Docker Hub repo (username/repo)
        DOCKERHUB_REPO = 'sachinbhandari/classroom_attendance_sys' // <-- change this

        // Tags
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
                git branch: 'Test',
                    url: 'https://github.com/bhandari-sachin/Classroom-Attendance-Management-System.git'
            }
        }

        stage('Build') {
            steps {
                bat 'mvn -B clean install'
            }
        }

        stage('Test') {
            steps {
                bat 'mvn -B test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Code Coverage') {
            steps {
                // Generates JaCoCo reports (multi-module => multiple targets)
                bat 'mvn -B jacoco:report'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                // Coverage plugin (JaCoCo parser)
                recordCoverage(
                    tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                    id: 'jacoco',
                    name: 'JaCoCo Coverage',
                    sourceCodeRetention: 'EVERY_BUILD'
                )
            }
        }

        stage('Build Docker Image') {
            steps {
                bat """
                    docker build ^
                      -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG_BUILD% ^
                      -t %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG_LATEST% ^
                      .
                """
            }
        }

        stage('Push Docker Image to Docker Hub') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKERHUB_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat """
                        docker login -u %DOCKER_USER% -p %DOCKER_PASS%
                        docker push %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG_BUILD%
                        docker push %DOCKERHUB_REPO%:%DOCKER_IMAGE_TAG_LATEST%
                        docker logout
                    """
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished: ${currentBuild.currentResult}"
        }
    }
}