pipeline {
    agent any

    stages {
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
                // Generates XML + HTML under **/target/site/jacoco/
                bat 'mvn -B jacoco:report'
            }
        }

        stage('Publish Coverage Report') {
            steps {
                // Coverage plugin step (JaCoCo parser)
                recordCoverage(
                    tools: [[parser: 'JACOCO', pattern: '**/target/site/jacoco/jacoco.xml']],
                    id: 'jacoco',
                    name: 'JaCoCo Coverage',
                    sourceCodeRetention: 'EVERY_BUILD'
                )
            }
        }
    }
}