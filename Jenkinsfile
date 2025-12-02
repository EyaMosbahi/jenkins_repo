pipeline {
    agent any
    
    tools {
        maven 'Maven'
    }
    
    environment {
        MAVEN_HOME = "${tool 'Maven'}"
        PATH = "${env.MAVEN_HOME}/bin:${env.PATH}"
        
        // Variables Docker
        DOCKER_IMAGE = "eyamosbahi/student-management"
        DOCKER_TAG = "1.0.${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = "dockerhub-credentials"
        
        // Variables SonarQube
        SONAR_PROJECT_KEY = "student-management"
        SONAR_HOST_URL = "http://172.18.66.151:9000"
        SONAR_TOKEN = "7faa9a738feeecb4b412ae19f6e32546625ae312"
    }
    
    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/EyaMosbahi/jenkins_repo.git'
            }
        }
        
        stage('Compile') {
            steps {
                sh 'mvn clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh 'mvn test'
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }
        
        // 🆕 STAGE SONARQUBE ANALYSIS
        stage('SonarQube Analysis') {
            steps {
                script {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                                -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                                -Dsonar.host.url=${SONAR_HOST_URL} \
                                -Dsonar.login=${SONAR_TOKEN}
                        """
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    echo "Building Docker image: ${DOCKER_IMAGE}:${DOCKER_TAG}"
                    sh """
                        docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} .
                        docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest
                    """
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                script {
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh """
                            echo \$DOCKER_PASSWORD | docker login -u \$DOCKER_USERNAME --password-stdin
                            docker push ${DOCKER_IMAGE}:${DOCKER_TAG}
                            docker push ${DOCKER_IMAGE}:latest
                            docker logout
                        """
                    }
                }
            }
        }
    }
    
    post {
        always {
            sh """
                docker rmi ${DOCKER_IMAGE}:${DOCKER_TAG} || true
                docker rmi ${DOCKER_IMAGE}:latest || true
            """
            cleanWs()
        }
        success {
            echo 'Pipeline réussi avec succès!'
            echo "Image Docker publiée: ${DOCKER_IMAGE}:${DOCKER_TAG}"
            echo "Analyse SonarQube disponible: ${SONAR_HOST_URL}/dashboard?id=${SONAR_PROJECT_KEY}"
        }
        failure {
            echo 'Pipeline a échoué!'
        }
    }
}
