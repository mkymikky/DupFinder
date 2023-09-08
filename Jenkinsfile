pipeline {
    agent any
    options {
        // Timeout counter starts AFTER agent is allocated
        timeout(time: 10, unit: 'MINUTES')
    }
    stages {
        stage('Build') {
            steps {
                sh 'mvn install'
            }
        }
    }
}
