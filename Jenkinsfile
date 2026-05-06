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
        KUBECONFIG_CREDENTIALS_ID = 'KUBECONFIG_MINIKUBE'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build & Test') {
            steps {
                bat """
                    mvn clean verify ^
                    -Dtest="!*Page*,!AttendanceFlowTest" ^
                    -DfailIfNoTests=false
                """
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

        /*stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: true
                }
            }
        }*/

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

        stage('Apply K8s Base Resources') {
            steps {
                withCredentials([file(
                    credentialsId: "${KUBECONFIG_CREDENTIALS_ID}",
                    variable: 'KUBECONFIG'
                )]) {
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
        }

       stage('Patch Ingress Nginx to LoadBalancer') {
           steps {
               withCredentials([file(
                   credentialsId: "${KUBECONFIG_CREDENTIALS_ID}",
                   variable: 'KUBECONFIG'
               )]) {
                   bat """
                       kubectl patch svc ingress-nginx-controller ^
                           --namespace=ingress-nginx ^
                           --type=merge ^
                           --patch-file k8s/ingress-nginx-patch.yaml
                   """
               }
           }
       }

      stage('Start Minikube Tunnel') {
          steps {
              bat """
                  start /B minikube tunnel --profile minikube
                  ping -n 16 127.0.0.1 > nul
              """
          }
      }

        stage('Wait for MySQL') {
            steps {
                withCredentials([file(
                    credentialsId: "${KUBECONFIG_CREDENTIALS_ID}",
                    variable: 'KUBECONFIG'
                )]) {
                    bat """
                        kubectl rollout status deployment/mysql ^
                            --namespace=%K8S_NAMESPACE% ^
                            --timeout=120s
                    """
                }
            }
        }

        stage('Deploy Backend to Kubernetes') {
            steps {
                withCredentials([file(
                    credentialsId: "${KUBECONFIG_CREDENTIALS_ID}",
                    variable: 'KUBECONFIG'
                )]) {
                    bat """
                        kubectl apply -f k8s/backend-deployment.yaml

                        kubectl rollout restart deployment/backend ^
                            --namespace=%K8S_NAMESPACE%

                        kubectl rollout status deployment/backend ^
                            --namespace=%K8S_NAMESPACE% ^
                            --timeout=300s
                    """
                }
            }
        }

        stage('Verify Deployment') {
            steps {
                withCredentials([file(
                    credentialsId: "${KUBECONFIG_CREDENTIALS_ID}",
                    variable: 'KUBECONFIG'
                )]) {
                    bat """
                        kubectl get pods --namespace=%K8S_NAMESPACE%
                        kubectl get services --namespace=%K8S_NAMESPACE%
                        kubectl get ingress --namespace=%K8S_NAMESPACE%
                    """
                }
            }
        }
    }

    post {
        success {
            echo "Pipeline succeeded!"
            echo "Backend image: ${env.DOCKER_USERNAME}/${env.BACKEND_IMAGE_REPO}:${env.DOCKER_IMAGE_TAG_LATEST}"
            echo "Access via: http://attendance.local/api/"
        }

        failure {
            echo "Pipeline failed -- rolling back backend..."
            withCredentials([file(
                credentialsId: 'KUBECONFIG_MINIKUBE',
                variable: 'KUBECONFIG'
            )]) {
                bat """
                    kubectl rollout undo deployment/backend ^
                        --namespace=%K8S_NAMESPACE%
                """
            }
        }

        always {
            cleanWs()
        }
    }
}