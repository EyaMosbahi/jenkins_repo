pipeline {
    agent any

    environment {
        DOCKER_SPRING_IMAGE = "eyamosbahi/student-management"
        DOCKER_ANGULAR_IMAGE = "eyamosbahi/student-frontend"
        DOCKER_GRAFANA_IMAGE = "grafana/grafana"
        DOCKER_PROMETHEUS_IMAGE = "prom/prometheus"
        DOCKER_TAG = "1.0.${BUILD_NUMBER}"
        DOCKER_CREDENTIALS_ID = "dockerhub-credentials"
        SONAR_PROJECT_KEY = "student-management"
        SONAR_HOST_URL = "http://localhost:9000"
    }

    stages {
        ///////////////////////////////////////
        // SPRING BOOT BACKEND
        ///////////////////////////////////////
        stage('Checkout Spring Boot') {
            steps {
                // Si déjà en workspace, adapte en fonction de ton multibranch.
                echo '✅ Code Spring Boot déjà dans le workspace'
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

        ///////////////////////////////////////
        // ANGULAR FRONTEND
        ///////////////////////////////////////
        stage('Checkout Angular') {
            steps {
                echo '✅ Code Angular déjà dans le workspace / copie manuelle'
                // Si besoin, utiliser git checkout ou dir('...') pour localiser le frontend
            }
        }
        stage('Docker Build Angular') {
            steps {
                script {
                    dir('/mnt/c/Users/ahmed/student-frontend') {
                        sh "docker build -t ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} ."
                        sh "docker tag ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG} ${DOCKER_ANGULAR_IMAGE}:latest"
                    }
                }
            }
        }
        stage('Docker Push Angular') {
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

        ///////////////////////////////////////
        // GRAFANA (généralement image officielle)
        ///////////////////////////////////////
        stage('Deploy Grafana to Kubernetes') {
            steps {
                sh """
                    # Si tu veux forcer un redeploiement sur nouvelle version officielle :
                    kubectl set image deployment/grafana grafana=${DOCKER_GRAFANA_IMAGE}:latest -n devops
                    kubectl rollout status deployment/grafana -n devops --timeout=5m
                """
            }
        }

        ///////////////////////////////////////
        // PROMETHEUS (généralement image officielle)
        ///////////////////////////////////////
        stage('Deploy Prometheus to Kubernetes') {
            steps {
                sh """
                    # Idem, pour redéployer une version officielle 
                    kubectl set image deployment/prometheus prometheus=${DOCKER_PROMETHEUS_IMAGE}:latest -n devops
                    kubectl rollout status deployment/prometheus -n devops --timeout=5m
                """
            }
        }

        ///////////////////////////////////////
        // VERIFICATION GLOBALE
        ///////////////////////////////////////
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
            echo '✅ Pipeline complet réussi !'
            echo "Spring Boot: ${DOCKER_SPRING_IMAGE}:${DOCKER_TAG}"
            echo "Angular: ${DOCKER_ANGULAR_IMAGE}:${DOCKER_TAG}"
        }
        failure {
            echo '❌ Pipeline échoué !'
        }
    }
}
