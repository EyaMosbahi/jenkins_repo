pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = "eyamosbahi/student-management"
        DOCKER_TAG = "1.0.${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = "dockerhub-credentials"
        SONAR_PROJECT_KEY = "student-management"
        SONAR_HOST_URL = "http://localhost:9000"
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
                junit '**/target/surefire-reports/*.xml'
            }
        }
        
        stage('SonarQube Analysis') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh '''
                        mvn sonar:sonar \
                        -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                        -Dsonar.host.url=${SONAR_HOST_URL} \
                        -Dsonar.login=${SONAR_TOKEN}
                    '''
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
                    sh "docker build -t ${DOCKER_IMAGE}:${DOCKER_TAG} ."
                    sh "docker tag ${DOCKER_IMAGE}:${DOCKER_TAG} ${DOCKER_IMAGE}:latest"
                }
            }
        }
        
        stage('Docker Push') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: "${DOCKER_CREDENTIALS_ID}", 
                                                     usernameVariable: 'DOCKER_USER', 
                                                     passwordVariable: 'DOCKER_PASS')]) {
                        sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                        sh "docker push ${DOCKER_IMAGE}:${DOCKER_TAG}"
                        sh "docker push ${DOCKER_IMAGE}:latest"
                    }
                }
            }
        }
        
        stage('Deploy to Kubernetes') {
            steps {
                script {
                    sh """
                        kubectl set image deployment/spring-app spring-app=${DOCKER_IMAGE}:${DOCKER_TAG} -n devops
                        kubectl rollout status deployment/spring-app -n devops
                        kubectl get pods -n devops
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo '✅ Pipeline réussi ! Application déployée sur Kubernetes !'
        }
        failure {
            echo '❌ Pipeline échoué !'
        }
    }
}
