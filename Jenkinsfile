// Image variables
def buildBarImage = "image-registry.openshift-image-registry.svc:5000/jenkins/ace-buildbar:12.0.2.0-ubuntu"
def ocImage = "image-registry.openshift-image-registry.svc:5000/jenkins/oc-deploy:latest"

// Params for Git Checkout-Stage
def gitCp4iDevOpsUtilsRepo = "https://github.com/khongks/cp4i-devops-utils.git"
def gitRepo = "https://github.com/khongks/cp4i-jenkins-ace.git"
def gitDomain = "github.com"

// Params for Build Bar Stage
def appName = "file"
def barName = "file"
def projectDir = "cp4i-jenkins-ace"
def utilsDir = "cp4i-devops-utils"

// Params for Deploy Bar Stage
def serverName = "file"
def namespace = "ace"
def configurationList = ""

// ACE dashboard configurations
def aceDashboardHost = "ace-dashboard-dash.ace.svc.cluster.local"
def port = "3443"
// oc get secret -n ace ace-dashboard-dash -ojson | jq -r .data.ibmAceControlApiKey | base64 -d
def ibmAceSecretName = "ace-dashboard-dash"
def imageName = "icr.io/appc-dev/ace-server@sha256:c58fc5a0975314e6a8e72f2780163af38465e6123e3902c118d8e24e798b7b01"
// def imagePullSecret = "ibm-entitlement-key"

// Artifactory configurations
def artifactoryHost = "artifactory-tools.itzroks-3100015379-x94hbr-6ccd7f378ae819553d37d5f2ee142bd6-0000.au-syd.containers.appdomain.cloud"
def artifactoryPort = "443"
def artifactoryRepo = "generic-local"
def artifactoryBasePath = "cp4i"

// curl -X GET -k -H "x-ibm-ace-control-apikey: bb0f7e21-8e8a-40da-9b9f-66371c6c4142" https://ace-dashboard-dash.ace.svc.cluster.local:3443/v1/directories/file | jq -r token
// curl -X GET -k -H "x-ibm-ace-control-apikey: $API_KEY" https://$HOST:$PORT/v1/directories/$BAR_FILE   

podTemplate(
    serviceAccount: "jenkins-jenkins-dev",
    containers: [
        containerTemplate(name: 'buildbar', image: "${buildBarImage}", workingDir: "/home/jenkins", ttyEnabled: true, envVars: [
            envVar(key: 'BAR_NAME', value: "${barName}"),
            envVar(key: 'APP_NAME', value: "${appName}"),
            envVar(key: 'PROJECT_DIR', value: "${projectDir}"),
        ]),
        containerTemplate(name: 'oc-deploy', image: "${ocImage}", workingDir: "/home/jenkins", ttyEnabled: true, envVars: [
            envVar(key: 'NAMESPACE', value: "${namespace}"),
            envVar(key: 'APP_NAME', value: "${appName}"),
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
            envVar(key: 'ARTIFACTORY_USER', value: "admin"),
            envVar(key: 'ARTIFACTORY_PASSWORD', value: "Passw0rd!"),
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
            container("buildbar") {
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
                sh label: '', script: '''#!/bin/bash
                    set -e
                    cd $PROJECT_DIR
                    ls -lha
                    echo "Calling upload-barfile-to-artifactory.sh ${ARTIFACTORY_HOST} ${ARTIFACTORY_REPO} ${ARTIFACTORY_BASE_PATH} "${BAR_NAME}_${BUILD_NUMBER}.bar" ${ARTIFACTORY_USER} ${ARTIFACTORY_PASSWORD}"
                    ./upload-barfile-to-artifactory.sh ${ARTIFACTORY_HOST} ${ARTIFACTORY_REPO} ${ARTIFACTORY_BASE_PATH} "${BAR_NAME}_${BUILD_NUMBER}.bar" ${ARTIFACTORY_USER} ${ARTIFACTORY_PASSWORD}
                    '''
            }
        }
        // https://ace-dashboard-dash:3443/v1/directories/file?1bd395f5-7cb8-4379-921c-536ea29a7af7 
        stage('Deploy Intergration Server') {
            container("oc-deploy") {
                sh label: '', script: '''#!/bin/bash
                    set -e
                    cd $PROJECT_DIR
                    BAR_FILE="${BAR_NAME}_${BUILD_NUMBER}.bar"
                    cat integration-server.yaml.tmpl
                    sed -e "s/{{NAME}}/$APP_NAME/g" \
                        -e "s/{{ARTIFACTORY_HOST}}/$ARTIFACTORY_HOST/g" \
                        -e "s/{{ARTIFACTORY_PORT}}/$ARTIFACTORY_PORT/g" \
                        -e "s/{{ARTIFACTORY_REPO}}/$ARTIFACTORY_REPO/g" \
                        -e "s/{{ARTIFACTORY_BASE_PATH}}/$ARTIFACTORY_BASE_PATH/g" \
                        -e "s/{{BAR_FILE}}/$BAR_FILE/g" \
                        -e "s/{{CONFIGURATION_LIST}}/$CONFIGURATION_LIST/g" \
                        integration-server.yaml.tmpl > integration-server.yaml
                    cat integration-server.yaml
                    oc apply -f integration-server.yaml
                    '''
            }
        }
    }
}
