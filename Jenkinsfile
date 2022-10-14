// Validate
// JENKINS_URL=jenkins-jenkins.itzroks-3100015379-raqclh-6ccd7f378ae819553d37d5f2ee142bd6-0000.au-syd.containers.appdomain.cloud
// curl --user "admin:Passw0rd!" -X POST -F "jenkinsfile=Jenkinsfile" https://$JENKINS_URL/pipeline-model-converter/validate

// Image variables
def buildBarImage = "image-registry.openshift-image-registry.svc:5000/jenkins/ace-buildbar:12.0.4.0-ubuntu"
def ocImage = "image-registry.openshift-image-registry.svc:5000/jenkins/oc-deploy:4.10"

// Params for Git Checkout-Stage
def gitCp4iDevOpsUtilsRepo = "https://github.com/khongks/cp4i-devops-utils.git"
def gitRepo = "https://github.com/khongks/cp4i-ace-books.git"
def gitDomain = "github.com"

// Params for Build Bar Stage
def appName = "Books"
def barName = "Books"
def projectDir = "cp4i-ace-books"
def utilsDir = "cp4i-devops-utils"

// Params for Deploy Bar Stage
def serverName = "books"
def namespace = "ace"
def configurationList = ""


// ACE dashboard configurations
def aceDashboardHost = "ace-dashboard-dash.ace.svc.cluster.local"
def port = "3443"
def ibmAceSecretName = "ace-dashboard-dash"

// ACE integration server
def aceVersion = "12.0.5.0-r4"
def aceLicense = "L-KSBM-CJ2KWU"
def replicas = "1"

// Artifactory configurations
def artifactoryHost = "artifactory-tools.itzroks-3100015379-raqclh-6ccd7f378ae819553d37d5f2ee142bd6-0000.au-syd.containers.appdomain.cloud"
def artifactoryPort = "443"
def artifactoryRepo = "generic-local"
def artifactoryBasePath = "cp4i"
def artifactoryCredentials = "artifactory_credentials" // defined in Jenkins credentials

podTemplate(
    serviceAccount: "jenkins",
    containers: [
        containerTemplate(name: 'ace-buildbar', image: "${buildBarImage}", workingDir: "/home/jenkins", ttyEnabled: true, envVars: [
            envVar(key: 'BAR_NAME', value: "${barName}"),
            envVar(key: 'APP_NAME', value: "${appName}"),
            envVar(key: 'PROJECT_DIR', value: "${projectDir}"),
        ]),
        containerTemplate(name: 'oc-deploy', image: "${ocImage}", workingDir: "/home/jenkins", ttyEnabled: true, envVars: [
            envVar(key: 'NAMESPACE', value: "${namespace}"),
            envVar(key: 'APP_NAME', value: "${appName}"),
            envVar(key: 'SERVER_NAME', value: "${serverName}"),
            envVar(key: 'BAR_NAME', value: "${barName}"),            
            envVar(key: 'CONFIGURATION_LIST', value: "${configurationList}"),
            envVar(key: 'ACE_DASHBOARD_HOST', value: "${aceDashboardHost}"),
            envVar(key: 'PORT', value: "${port}"),
            envVar(key: 'API_KEY_NAME', value: "${ibmAceSecretName}"),
            envVar(key: 'PROJECT_DIR', value: "${projectDir}"),
            envVar(key: 'ARTIFACTORY_HOST', value: "${artifactoryHost}"),
            envVar(key: 'ARTIFACTORY_PORT', value: "${artifactoryPort}"),
            envVar(key: 'ARTIFACTORY_REPO', value: "${artifactoryRepo}"),
            envVar(key: 'ARTIFACTORY_BASE_PATH', value: "${artifactoryBasePath}"),
            envVar(key: 'ARTIFACTORY_CREDENTIALS', value: "${artifactoryCredentials}"),
            envVar(key: 'ACE_VERSION', value: "${aceVersion}"),
            envVar(key: 'ACE_LICENSE', value: "${aceLicense}"),
            envVar(key: 'REPLICAS', value: "${replicas}"),
        ]),
        containerTemplate(name: 'jnlp', image: "jenkins/jnlp-slave:latest", ttyEnabled: true, workingDir: "/home/jenkins", envVars: [
            envVar(key: 'HOME', value: '/home/jenkins'),
            envVar(key: 'GIT_DOMAIN', value: "${gitDomain}"),
            envVar(key: 'GIT_REPO', value: "${gitRepo}"),
            envVar(key: 'PROJECT_DIR', value: "${projectDir}"),
            envVar(key: 'GIT_CP4I_DEVOPS_UTILS_REPO', value: "${gitCp4iDevOpsUtilsRepo}"),
            envVar(key: 'CP4I_DEVOPS_UTILS_DIR', value: "${utilsDir}"),
        ])
  ]) {
    node(POD_LABEL) {
        stage('Git Checkout') {
            container("jnlp") {
                sh """
                    git clone $GIT_CP4I_DEVOPS_UTILS_REPO
                    git clone $GIT_REPO
                    cp -p $CP4I_DEVOPS_UTILS_DIR/templates/integration-server.yaml.tmpl $PROJECT_DIR
                    cp -p $CP4I_DEVOPS_UTILS_DIR/scripts/*.sh $PROJECT_DIR
                    ls -la
                """
            }
        }
        stage('Build Bar File') {
            container("ace-buildbar") {
                sh label: '', script: '''#!/bin/bash
                    Xvfb -ac :99 &
                    export DISPLAY=:99
                    export LICENSE=accept
                    pwd
                    source /opt/ibm/ace-12/server/bin/mqsiprofile
                    cd $PROJECT_DIR
                    BAR_FILE="${BAR_NAME}_${BUILD_NUMBER}.bar"
                    mqsicreatebar -data . -b $BAR_FILE -a $APP_NAME -cleanBuild -trace -configuration . 
                    ls -lha
                    '''
            }
        }
        stage('Upload Bar File') {
            container("oc-deploy") {
                withCredentials([usernamePassword(credentialsId: 'artifactory_credentials', usernameVariable: 'ARTIFACTORY_USER', passwordVariable: 'ARTIFACTORY_PASSWORD')]) {
                    sh label: '', script: '''#!/bin/bash
                        set -e
                        cd $PROJECT_DIR
                        ls -lha
                        echo "Calling upload-barfile-to-artifactory.sh ${ARTIFACTORY_HOST} ${ARTIFACTORY_REPO} ${ARTIFACTORY_BASE_PATH} "${BAR_NAME}_${BUILD_NUMBER}.bar" ${ARTIFACTORY_USER} ${ARTIFACTORY_PASSWORD}"
                        ./upload-barfile-to-artifactory.sh ${ARTIFACTORY_HOST} ${ARTIFACTORY_REPO} ${ARTIFACTORY_BASE_PATH} "${BAR_NAME}_${BUILD_NUMBER}.bar" ${ARTIFACTORY_USER} ${ARTIFACTORY_PASSWORD}
                        '''
                }
            }
        }
        stage('Deploy Intergration Server') {
            container("oc-deploy") {
                sh label: '', script: '''#!/bin/bash
                    set -e
                    cd $PROJECT_DIR
                    BAR_FILE="${BAR_NAME}_${BUILD_NUMBER}.bar"
                    cat integration-server.yaml.tmpl
                    sed -e "s/{{NAME}}/$SERVER_NAME/g" \
                        -e "s/{{NAMESPACE}}/$NAMESPACE/g" \
                        -e "s/{{ARTIFACTORY_HOST}}/$ARTIFACTORY_HOST/g" \
                        -e "s/{{ARTIFACTORY_PORT}}/$ARTIFACTORY_PORT/g" \
                        -e "s/{{ARTIFACTORY_REPO}}/$ARTIFACTORY_REPO/g" \
                        -e "s/{{ARTIFACTORY_BASE_PATH}}/$ARTIFACTORY_BASE_PATH/g" \
                        -e "s/{{BAR_FILE}}/$BAR_FILE/g" \
                        -e "s/{{CONFIGURATION_LIST}}/$CONFIGURATION_LIST/g" \
                        -e "s/{{ACE_VERSION}}/$ACE_VERSION/g" \
                        -e "s/{{ACE_LICENSE}}/$ACE_LICENSE/g" \
                        -e "s/{{REPLICAS}}/$REPLICAS/g" \
                        integration-server.yaml.tmpl > integration-server.yaml
                    cat integration-server.yaml
                    oc apply -f integration-server.yaml
                    echo "Wait for integration server to be Ready"
                    oc wait --for=condition=Ready integrationserver/${SERVER_NAME} --timeout=120s -n ${NAMESPACE}
                    '''
            }
        }
        stage('Unit Test') {
            container("oc-deploy") {
                sh label: '', script: '''#!/bin/bash
                    HOSTNAME=$(oc get route -n ${NAMESPACE} ${SERVER_NAME}-http -ogo-template --template='{{.spec.host}}')
                    curl -k http://${HOSTNAME}/api/v1/${SERVER_NAME} | jq -r .
                '''
            }
        }
    }
}
