pipeline {
    agent any

    tools {
        maven 'Maven3'
        jdk 'JDK21'
    }

    environment {
        DOCKER_USERNAME           = 'sachinbhandari'
        DOCKERHUB_CREDENTIALS_ID  = 'Docker_Hub'
        BACKEND_IMAGE_REPO        = 'classroom-attendance-backend'
        DOCKER_IMAGE_TAG          = "build-${env.BUILD_NUMBER}"
        DOCKER_IMAGE_TAG_LATEST   = 'latest'

        SONAR_PROJECT_KEY         = 'classroom_attendance_management'
        SONAR_PROJECT_NAME        = 'Classroom Attendance Management'

        K8S_NAMESPACE             = 'attendance-app'
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
                        -Dsonar.projectKey=%SONAR_PROJECT_KEY% ^
                        -Dsonar.projectName="%SONAR_PROJECT_NAME%" ^
                        -Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml ^
                        -Dsonar.sourceEncoding=UTF-8
                    """
                }
            }
        }

        /* stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        } */

        // Only backend - JavaFX frontend is NOT containerized
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

        stage('Push Backend Image to Docker Hub') {
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

                        docker logout
                    """
                }
            }
        }

        //  Apply base resources only on first run — idempotent, safe to re-run
        stage('Apply K8s Base Resources') {
            steps {
                bat """
                    kubectl apply -f k8s/namespace.yaml
                    kubectl apply -f k8s/secret.yaml
                    kubectl apply -f k8s/mysql-pvc.yaml
                    kubectl apply -f k8s/mysql-deployment.yaml
                    kubectl apply -f k8s/mysql-service.yaml
                    kubectl apply -f k8s/backend-service.yaml
                    kubectl apply -f k8s/ingress.yaml
                """
            }
        }

        //  Wait for MySQL to be healthy before deploying backend
        stage('Wait for MySQL') {
            steps {
                bat """
                    kubectl rollout status deployment/mysql ^
                        --namespace=%K8S_NAMESPACE% ^
                        --timeout=120s
                """
            }
        }

        //  Rolling update — zero downtime, replaces Docker Compose deploy
        stage('Deploy Backend to Kubernetes') {
            steps {
                bat """
                    kubectl apply -f k8s/backend-deployment.yaml

                    kubectl set image deployment/backend ^
                        backend=%DOCKER_USERNAME%/%BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG% ^
                        --namespace=%K8S_NAMESPACE%

                    kubectl rollout status deployment/backend ^
                        --namespace=%K8S_NAMESPACE% ^
                        --timeout=120s
                """
            }
        }

        //  Verify pods are actually running after deployment
        stage('Verify Deployment') {
            steps {
                bat """
                    kubectl get pods --namespace=%K8S_NAMESPACE%
                    kubectl get services --namespace=%K8S_NAMESPACE%
                    kubectl get ingress --namespace=%K8S_NAMESPACE%
                """
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded!"
            echo "Backend image: %DOCKER_USERNAME%/%BACKEND_IMAGE_REPO%:%DOCKER_IMAGE_TAG%"
            echo "Access via: http://attendance.local/api/"
            echo "Or run: minikube service backend-service -n attendance-app"
        }

        failure {
            // Auto rollback on failure
            echo "Pipeline failed — rolling back backend..."
            bat """
                kubectl rollout undo deployment/backend ^
                    --namespace=%K8S_NAMESPACE%
            """
        }

        always {
            cleanWs()
        }
    }
}