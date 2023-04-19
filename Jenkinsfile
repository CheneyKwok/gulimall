pipeline {
  agent {
    node {
      label 'maven'
    }
  }

  parameters {
      string(name: 'PROJECT_VERSION', defaultValue: 'v1.0', description: '项目版本')
      string(name: 'PROJECT_NAME', defaultValue: 'gulimall-cart', description: '构建模块')
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

    stage('拉取代码') {
      agent none
      steps {
        git(url: 'https://github.com/CheneyKwok/gulimall.git', credentialsId: 'github-id', branch: 'master', changelog: true, poll: false)
        container('maven') {
        sh 'mvn clean install -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml'
        }
      }
    }


    stage('sonarqube 代码质量分析') {
      steps {
        container ('maven') {
          withCredentials([string(credentialsId: "$SONAR_CREDENTIAL_ID", variable: 'SONAR_TOKEN')]) {
            withSonarQubeEnv('sonar') {
             sh "mvn sonar:sonar -o -gs `pwd`/mvn-settings.xml -Dsonar.login=$SONAR_TOKEN"
            }
          }
        }
      }
    }


    stage ('构建镜像 & 推送镜像') {
        steps {
            container ('maven') {
                sh 'mvn -o -Dmaven.test.skip=true -gs `pwd`/mvn-settings.xml clean package'
                sh 'cd $PROJECT_NAME && docker build --no-cache -f Dockerfile -t $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER .'
                withCredentials([usernamePassword(passwordVariable : 'DOCKER_PASSWORD' ,usernameVariable : 'DOCKER_USERNAME' ,credentialsId : "$DOCKER_CREDENTIAL_ID" ,)]) {
                    sh 'echo "$DOCKER_PASSWORD" | docker login $REGISTRY -u "$DOCKER_USERNAME" --password-stdin'
                    sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER'
                }
            }
        }
    }

    stage('推送最新镜像'){
       steps{
            container ('maven') {
              sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
              sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:latest '
            }
       }
    }


    stage('部署到集群') {

      steps {
        input(id: 'deploy-to-dev', message: "是否将 $PROJECT_NAME 部署到集群中?")
        kubernetesDeploy(configs: "$PROJECT_NAME/deploy/**", enableConfigSubstitution: true, kubeconfigId: "$KUBECONFIG_CREDENTIAL_ID")
      }
    }


    stage('发布版本'){
      when{
        expression{
          return params.PROJECT_VERSION =~ /v.*/
        }
      }
      steps {
          container ('maven') {
            input(id: 'release-image-with-tag', message: '发布当前版本镜像吗?')
              withCredentials([usernamePassword(credentialsId: "$GITHUB_CREDENTIAL_ID", passwordVariable: 'GIT_PASSWORD', usernameVariable: 'GIT_USERNAME')]) {
                sh 'git config --global user.email "2399214024.com" '
                sh 'git config --global user.name "cheneykwok" '
                sh 'git tag -a $PROJECT_VERSION -m "$PROJECT_VERSION" '
                sh 'git push http://$GIT_USERNAME:$GIT_PASSWORD@github.com/$GITHUB_ACCOUNT/devops-java-sample.git --tags --ipv4'
              }
            sh 'docker tag  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:SNAPSHOT-$BRANCH_NAME-$BUILD_NUMBER $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
            sh 'docker push  $REGISTRY/$DOCKERHUB_NAMESPACE/$PROJECT_NAME:$PROJECT_VERSION '
      }
      }
    }



  }
}