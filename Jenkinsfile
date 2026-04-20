pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    environment {
        /*
        DOCKER_USERNAME          = ''
        */
        DOCKERHUB_CREDENTIALS_ID = 'Docker_Hub'
        BACKEND_IMAGE_REPO       = 'classroom-attendance-backend'
        FRONTEND_IMAGE_REPO      = 'classroom-attendance-frontend'
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
                bat 'mvn clean verify'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Generate Coverage Report') {
            steps {
                bat 'mvn jacoco:report'
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
                         mvn sonar:sonar ^
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} ^
                        -Dsonar.projectName="${SONAR_PROJECT_NAME}" ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml ^
                        -Dsonar.sourceEncoding=UTF-8
                    """
                }
            }
        }

        /*
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
                        -t %DOCKER_USERNAME%/%BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG% ^
                        -t %DOCKER_USERNAME%/%BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% .
                """
            }
        }

        stage('Build Frontend Docker Image') {
            steps {
                bat """
                    docker build ^
                        -f frontend\\frontend.dockerfile ^
                        -t %DOCKER_USERNAME%/%FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG% ^
                        -t %DOCKER_USERNAME%/%FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST% .
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

                        docker push %DOCKER_USERNAME%/%BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                        docker push %DOCKER_USERNAME%/%BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST%

                        docker push %DOCKER_USERNAME%/%FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG%
                        docker push %DOCKER_USERNAME%/%FRONTEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG_LATEST%

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
            echo "Pipeline succeeded!"
            echo "Backend:  ${DOCKER_USERNAME}/${BACKEND_IMAGE_REPO}:${DOCKER_IMAGE_TAG}"
            echo "Frontend: ${DOCKER_USERNAME}/${FRONTEND_IMAGE_REPO}:${DOCKER_IMAGE_TAG}"
        }

        failure {
            echo "Pipeline failed. Check logs."
        }

        always {
            cleanWs()
        }
    }
}
