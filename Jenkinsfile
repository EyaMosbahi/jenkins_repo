pipeline {
    agent any

    environment {
        DOCKER_SPRING_IMAGE = "eyamosbahi/student-management"
        DOCKER_ANGULAR_IMAGE = "eyamosbahi/student-frontend"
        DOCKER_TAG = "1.0.${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = "dockerhub-credentials"
        SONAR_PROJECT_KEY = "student-management"
        SONAR_HOST_URL = "http://localhost:9000"
        // Le token SonarQube sera injecté via credentials
    }

    stages {
        stage('Checkout Spring Boot') {
            steps {
                echo '✅ Spring Boot code already in workspace'
            }
        }

        stage('Compile Spring Boot') {
            steps {
                sh 'mvn clean compile'
            }
        }

        stage('Test Spring Boot') {
            steps {
                sh 'mvn test'
                junit '**/target/surefire-reports/*.xml'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                    withSonarQubeEnv('SonarQube') {
                        sh """
                            mvn sonar:sonar \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.host.url=${SONAR_HOST_URL} \
                              -Dsonar.login=$SONAR_TOKEN
                        """
                    }
                }
            }
        }

        stage('Package Spring Boot') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }

        stage('Docker Build Spring Boot') {
            steps {
                sh "docker build -t ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG} ."
                sh "docker tag ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG} ${DOCKER_SPRING_IMAGE}:latest"
            }
        }

        stage('Docker Push Spring Boot') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS_ID}", 
                    usernameVariable: 'DOCKER_USER', 
                    passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker push ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_SPRING_IMAGE}:latest"
                }
            }
        }

        stage('Deploy Spring Boot to Kubernetes') {
            steps {
                sh """
                    minikube image load ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}
                    kubectl set image deployment/spring-app spring-app=${DOCKER_SPRING_IMAGE}:${DOCKER_TAG} -n devops
                    kubectl rollout status deployment/spring-app -n devops --timeout=5m
                """
            }
        }

        stage('Checkout Angular') {
            steps {
                echo '✅ Angular code already in workspace or directory'
                // Si besoin : checkout repo du frontend ici !
            }
        }

        stage('Build Angular Docker Image') {
            steps {
                script {
                    dir('/mnt/c/Users/ahmed/student-frontend') { // adapte ce chemin selon ton workspace
                        sh "docker build -t ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} ."
                        sh "docker tag ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} ${DOCKER_ANGULAR_IMAGE}:latest"
                    }
                }
            }
        }

        stage('Push Angular Docker Image') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS_ID}", 
                    usernameVariable: 'DOCKER_USER', 
                    passwordVariable: 'DOCKER_PASS')]) {
                    sh 'echo $DOCKER_PASS | docker login -u $DOCKER_USER --password-stdin'
                    sh "docker push ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}"
                    sh "docker push ${DOCKER_ANGULAR_IMAGE}:latest"
                }
            }
        }

        stage('Deploy Angular to Kubernetes') {
            steps {
                sh """
                    minikube image load ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}
                    kubectl set image deployment/angular-app angular-app=${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} -n devops
                    kubectl rollout status deployment/angular-app -n devops --timeout=5m
                """
            }
        }

        stage('Verify Deployment') {
            steps {
                sh '''
                    echo "==== PODS STATUS ===="
                    kubectl get pods -n devops
                    echo "==== SERVICES ===="
                    kubectl get svc -n devops
                '''
            }
        }
    }

    post {
        success {
            echo '✅ Pipeline complet réussi !'
            echo "Spring Boot: ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}"
            echo "Angular: ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo '❌ Pipeline échoué !'
        }
    }
}
