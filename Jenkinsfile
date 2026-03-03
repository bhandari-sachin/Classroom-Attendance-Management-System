pipeline {
    agent any
    tools {
        jdk 'JDK21'
      }
    stages {
        stage('Checkout') {
            steps {
                git branch: 'admin-api',
                        url: 'https://github.com/bhandari-sachin/Classroom-Attendance-Management-System.git'
            }
        }
        stage('Check Java') {
          steps { bat 'java -version && echo %JAVA_HOME%' }
        }
        stage('Build') {
            steps {
                bat 'mvn clean install'
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