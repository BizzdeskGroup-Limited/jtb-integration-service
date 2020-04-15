node {
    stage 'Check out'
    echo 'Checking out...'
    checkout scm
    stage 'Build Jar'
    echo 'Building Jar file...'
    sh 'mvn clean package -DskipTests'
    archiveArtifacts artifacts: '**/target/*.jar', fingerprint: true
    stage 'Build  Docker Image'
        docker.withRegistry("${env.REGISTRY_PROTOCOL}://${env.REGISTRY_HOST}", 'docker_registry_credentials_id') {
            stage 'Build Docker Image'
            echo 'Building docker image....'
            String imageName = "${env.REGISTRY_HOST}/jtb-integration-service:latest"
            sh "docker build -t ${imageName}  ."
            def img = docker.image(imageName)
            stage 'Push Docker Image'
            echo 'Pushing docker image....'
            img.push()
        }
    stage 'Deploy to Kubernetes'
    echo 'Deploying....'
}