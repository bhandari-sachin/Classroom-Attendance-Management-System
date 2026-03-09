pipeline {
    agent any
    tools {
        jdk 'JDK21'
      }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main',
                        url: 'https://github.com/bhandari-sachin/Classroom-Attendance-Management-System.git'
            }
        }
        stage('Check Java') {
          steps { bat 'java -version && echo %JAVA_HOME%' }
        }

        stage('Build') {
          steps {
            bat '''
              set "JAVA_HOME=C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.10.7-hotspot"
              set "PATH=%JAVA_HOME%\\bin;%PATH%"
              java -version
              mvn -version
              mvn clean test
            '''
          }
        }
        stage('Test') {
            steps {
                bat 'mvn test'
            }
        }
        stage('Code Coverage') {
            steps {
                bat 'mvn jacoco:report'
            }
        }
        stage('Publish Test Results') {
            steps {
                junit '**/target/surefire-reports/*.xml'
            }
        }
        stage('Publish Coverage Report') {
            steps {
                jacoco()
            }
        }
    }
}