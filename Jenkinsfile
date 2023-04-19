pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  parameters {
      string(name: 'PROJECT_VERSION', defaultValue: '', description: '')
      string(name: 'PROJECT_NAME', defaultValue: '', description: '')
  }

  environment {
      DOCKER_CREDENTIAL_ID = 'aliyun-dockerhub-id'
      GITHUB_CREDENTIAL_ID = 'github-id'
      KUBECONFIG_CREDENTIAL_ID = 'kubeconfig'
      REGISTRY = 'registry.cn-hangzhou.aliyuncs.com'
      DOCKERHUB_NAMESPACE = '2399214024'
      GITHUB_ACCOUNT = 'CheneyKwok'
      SONAR_CREDENTIAL_ID = 'sonar-token'
      BRANCH_NAME='master'

  }


  stages {

    stage('clone code') {
      agent none
      steps {
        git(url: 'https://github.com/CheneyKwok/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
        container('maven') {
        sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml'
        }
      }
    }


    stage('sonarqube analysis') {
      steps {
        container ('maven') {
          withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
            withSonarQubeEnv('sonar') {
             sh "mvn sonar:sonar -o -gs `pwd`/mvn-settings.xml -Dsonar.login=$SONAR_TOKEN"
            }
          }
          timeout(time: 1, unit: 'HOURS') {
            waitForQualityGate abortPipeline: true
          }
        }
      }
    }


    stage ('build & push') {
        steps {
            container ('maven') {
                sh 'mvn -o -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml clean package'
                sh 'cd $PROJECT_NAME docker build --no-cache -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                    sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                }
            }
        }
    }



  }
}